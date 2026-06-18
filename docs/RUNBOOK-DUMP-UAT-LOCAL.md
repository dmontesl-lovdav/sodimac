# Runbook — Dump BD UAT Sodimac → PC personal (Docker local)

> Procedimiento validado 2026-06-17. Copia la BD real `b2b_portal` de UAT a la PC personal
> para pruebas con datos reales. La PC Sodimac NO tiene Claude — el dump se genera ahí y el
> archivo `.sql` viaja a la PC personal. La restauración corre en la PC personal.

Relacionado: [BASE-DE-DATOS.md](BASE-DE-DATOS.md), [SINCRONIZACION-MIRROR-SODIMAC.md](SINCRONIZACION-MIRROR-SODIMAC.md).

---

## 0. Pre-requisito — binarios pg_dump version-matched

UAT corre **PostgreSQL 18.3**. `pg_dump` debe ser de major **>= server** (18.x). El PostgreSQL 17
que viene con DBeaver da error:

```
pg_dump: error: abortando debido a que no coincide la version del servidor
pg_dump: detalle: version del servidor: 18.3; version de pg_dump: 17.4
```

**Fix:** binarios PG18 portable (sin instalador, sin admin).

1. PC personal — descargar zip EDB (~319 MB) a carpeta compartida `sesiones/` (gitignored):
   - URL: `https://get.enterprisedb.com/postgresql/postgresql-18.0-1-windows-x64-binaries.zip`
   - md5 esperado: `cc3842ae3fc0297abc92735c34a9098e`
2. Mover zip a PC Sodimac por USB / share (NO viaja por mirror — `sesiones/` está en `.gitignore`).
3. PC Sodimac — descomprimir:
   ```powershell
   Expand-Archive <ruta-zip> -DestinationPath C:\software -Force
   ```
   Queda `C:\software\pgsql\bin\pg_dump.exe` (v18).

---

## 1. Generar dump (PC Sodimac)

> Conexión `localhost:5434` en PC Sodimac = túnel a UAT. User `wwwb2bportal` es app user (permisos limitados).

```powershell
$env:PGPASSWORD='b8@qU0YM1HU>'
C:\software\pgsql\bin\pg_dump.exe --verbose `
  --host=localhost --port=5434 --username=wwwb2bportal `
  --format=p `
  --exclude-table='core_audit.insert_log' `
  --file C:\workspace-sodimac\sesiones\db\dump-b2b_portal-full.sql `
  b2b_portal
Remove-Item Env:PGPASSWORD
```

Notas:
- **Sin `-n`** = dump completo (todos los esquemas + datos). Con `-n esquema` se limita a uno.
- `--exclude-table='core_audit.insert_log'` **obligatorio**: el app user no tiene SELECT en esa
  tabla y `pg_dump` hace `LOCK TABLE ... ACCESS SHARE` de TODAS a la vez → la primera negada aborta.
- Para detectar otras tablas negadas antes de correr:
  ```powershell
  C:\software\pgsql\bin\psql.exe -h localhost -p 5434 -U wwwb2bportal -d b2b_portal -At -c `
  "SELECT n.nspname||'.'||c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE c.relkind IN ('r','p') AND NOT has_table_privilege('wwwb2bportal', c.oid, 'SELECT') AND n.nspname NOT IN ('pg_catalog','information_schema');"
  ```
  Cada tabla listada → un `--exclude-table` más.
- `--format=p` = SQL plano (restaura con `psql`). Para BD grande: `--format=c` + `pg_restore`
  (comprimido, restauración selectiva).
- El `.sql` resultante (~15 MB) viaja a la PC personal vía mirror (`sesiones/db/` se versiona)
  o por USB.

---

## 2. Restaurar en Docker local (PC personal)

Container `sodimac-pg` (`localhost:5434`, PG16). El dump es de PG18 → restaurar como superuser
`postgres` del container.

> **Superuser local** = `postgres` vía trust DENTRO del container (sin password).
> `wwwb2bportal` NO es superuser local (no puede CREATE ROLE / DROP DATABASE).

### 2a. Crear roles faltantes que el dump referencia

```bash
docker exec sodimac-pg psql -U postgres -d postgres -v ON_ERROR_STOP=1 -c \
"DO \$\$ DECLARE r text; BEGIN FOREACH r IN ARRAY ARRAY['admin-b2b-portal','audit_user','cloudsqlsuperuser','dump_role','tnt_b2bportal'] LOOP IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname=r) THEN EXECUTE format('CREATE ROLE %I NOLOGIN', r); END IF; END LOOP; END\$\$;"
```

### 2b. Recrear DB limpia

```bash
docker exec sodimac-pg psql -U postgres -d postgres -v ON_ERROR_STOP=1 \
  -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='b2b_portal' AND pid<>pg_backend_pid();" \
  -c "DROP DATABASE IF EXISTS b2b_portal;" \
  -c "CREATE DATABASE b2b_portal OWNER \"admin-b2b-portal\";"
```

### 2c. Restaurar (pipe por stdin)

```bash
docker exec -i sodimac-pg psql -U postgres -d b2b_portal -v ON_ERROR_STOP=0 -f - \
  < /c/workspace-sodimac/sesiones/db/dump-b2b_portal-full.sql \
  > /c/workspace-sodimac/sesiones/db/restore.log 2>&1
grep -c "^ERROR:" /c/workspace-sodimac/sesiones/db/restore.log   # esperado: 0
```

> **Trampa Git Bash:** `/tmp/` se mangea a ruta Windows (MSYS pathconv) → `docker cp ... :/tmp/`
> falla. Por eso se restaura por **stdin** (`-f -`), no copiando el archivo al container.
> Si necesitas rutas internas del container, prefija `MSYS_NO_PATHCONV=1`.

### 2d. Verificar

```bash
docker exec sodimac-pg psql -U postgres -d b2b_portal -q -c "ANALYZE;"
docker exec sodimac-pg psql -U postgres -d b2b_portal -At -F'|' -c \
"SELECT schemaname, count(*) tablas, SUM(n_live_tup) filas FROM pg_stat_user_tables GROUP BY schemaname ORDER BY schemaname;"
```

Resultado esperado (2026-06-17): **128 tablas, ~19,319 filas, 0 errores**.

| Esquema | Tablas | Filas |
|---|---|---|
| core_audit | 1 | 15,299 |
| core_security | 9 | 267 |
| core_utils | 7 | 167 |
| shared_catalogs | 13 | 2,728 |
| tenant_finance | 38 | 785 |
| tenant_fiscal | 24 | 73 |
| public | 36 | 0 (vacío, legacy) |
| shared_communication | 0 | — |

---

## Notas

- `core_security` y `shared_communication` son esquemas presentes en UAT pero **no listados en CLAUDE.md**.
- `public` tiene 36 tablas pero está vacío — modelo migrado a esquemas por tenant.
- El dump PG18 trae meta-comandos `\restrict`/`\unrestrict` (psql 18). Con psql 16/`ON_ERROR_STOP=0`
  son inocuos (dan error pero continúa); con cliente v18 se ejecutan limpios.
