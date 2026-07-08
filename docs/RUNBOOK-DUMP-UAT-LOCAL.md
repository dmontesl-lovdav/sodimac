# Runbook — Export BDs Sodimac → PC personal (Docker local)

> Procedimiento para traer BDs reales de Sodimac a la PC personal (Claude bloqueado en Sodimac).
> Patrón común: binario portable en carpeta compartida → PC Sodimac extrae/exporta → archivo
> viaja por USB/share → se restaura/publica en Docker local.

**2 bases cubiertas:**

| # | BD origen | Motor | Herramienta portable | Sección |
|---|---|---|---|---|
| 1 | `b2b_portal` (UAT) | PostgreSQL 18.3 | `pg_dump` (PG18 zip EDB) | [Parte A](#parte-a--postgresql-b2b_portal-uat) |
| 2 | `SODIMAC_SAP_DEV` (DEV) | SQL Server | `SqlPackage` (dacpac) | [Parte B](#parte-b--sql-server-sodimac_sap_dev-dev) |

Los binarios portable NO viajan por el mirror (`sesiones/` gitignored) → mover por USB/share.

Relacionado: [BASE-DE-DATOS.md](BASE-DE-DATOS.md), [SINCRONIZACION-MIRROR-SODIMAC.md](SINCRONIZACION-MIRROR-SODIMAC.md).

---

# Parte A — PostgreSQL `b2b_portal` (UAT)

> Validado 2026-06-17. Copia la BD real `b2b_portal` de UAT con datos. Dump `.sql` (~15-19 MB)
> se genera en PC Sodimac y viaja por mirror (`sesiones/db/` se versiona) o USB.

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

## Notas (Parte A)

- `core_security` y `shared_communication` son esquemas presentes en UAT pero **no listados en CLAUDE.md**.
- `public` tiene 36 tablas pero está vacío — modelo migrado a esquemas por tenant.
- El dump PG18 trae meta-comandos `\restrict`/`\unrestrict` (psql 18). Con psql 16/`ON_ERROR_STOP=0`
  son inocuos (dan error pero continúa); con cliente v18 se ejecutan limpios.

---

# Parte B — SQL Server `SODIMAC_SAP_DEV` (DEV)

> Iniciado 2026-07-07. Trae **solo el esquema** (sin datos) de la BD real del autofacturador
> (modelo viejo, ~70 tablas). Motivo: inventariar relaciones reales (ej. `Addenda` y su posible
> tabla intermedia). No confundir con el modelo FBC nuevo de 8 tablas que vive en el
> `sodimac-mssql` local (`docs/db/sap_dev/sap-dev-dll.sql`).

Origen: SQL Server DEV `10.138.153.10:1433`, DB `SODIMAC_SAP_DEV`, user `SodimacDevUsr` / `Pa55wordDev`.

## 0. Pre-requisito — SqlPackage portable

pg_dump no existe para SQL Server. `mssql-scripter` requiere Python/pip (bloqueado en Sodimac).
Portable self-contained sin admin = **SqlPackage** (Microsoft, .NET incluido). Exporta esquema
como `.dacpac` (por diseño = solo schema).

1. PC personal — descargar zip (~46 MB) a `sesiones/` (gitignored):
   - URL: `https://aka.ms/sqlpackage-windows` (redirige a `sqlpackage-win-x64-<ver>.zip`, validado 170.4.83.3)
2. Mover zip a PC Sodimac por USB / share.
3. PC Sodimac — descomprimir:
   ```powershell
   Expand-Archive <ruta-zip> -DestinationPath C:\software\sqlpackage -Force
   ```
   Queda `C:\software\sqlpackage\SqlPackage.exe`.

## 1. Extraer esquema (PC Sodimac, CMD una línea)

```cmd
C:\software\sqlpackage\SqlPackage.exe /Action:Extract /SourceServerName:10.138.153.10 /SourceDatabaseName:SODIMAC_SAP_DEV /SourceUser:SodimacDevUsr /SourcePassword:Pa55wordDev /SourceTrustServerCertificate:True /p:ExtractAllTableData=False /TargetFile:C:\Users\g_dco018\SODIMAC_SAP_DEV-schema.dacpac
```

- `Extract` por defecto = solo esquema. `ExtractAllTableData=False` explícito (0 filas).
- `SourceTrustServerCertificate:True` obligatorio (SqlPackage moderno fuerza encrypt).
- Genera 1 `.dacpac` con TODO el modelo (tablas + PK/FK/vistas/procs). Las FKs reales viajan → sirve para ver relaciones (`Addenda`, etc).
- CMD: una sola línea (sin backticks). Si molesta el largo, cortar con `^`.

## 2. Copiar `.dacpac` a PC personal

→ `C:\workspace-sodimac\sesiones\db\SODIMAC_SAP_DEV-schema.dacpac` (USB/share).

## 3. Publicar en Docker local (PC personal)

El mismo zip SqlPackage se extrae también en la PC personal para publicar contra el container.
**Publicar en DB NUEVA** (`SODIMAC_SAP_DEV_FULL`) para NO pisar el modelo FBC de 8 tablas del
`SODIMAC_SAP_DEV` local (Publish sincroniza esquema y puede dropear objetos).

```bash
C:/software/sqlpackage/SqlPackage.exe /Action:Publish \
  /SourceFile:C:/workspace-sodimac/sesiones/db/SODIMAC_SAP_DEV-schema.dacpac \
  /TargetServerName:localhost,1433 /TargetDatabaseName:SODIMAC_SAP_DEV_FULL \
  /TargetUser:SA /TargetPassword:'Sodimac2026#Dev' /TargetTrustServerCertificate:True
```

Publish crea la DB si no existe. Sin datos (el dacpac no los trae).

## Notas (Parte B)

- **Publicado y verificado 2026-07-07**: 70 tablas en `SODIMAC_SAP_DEV_FULL` (container `sodimac-mssql`, host **puerto 1434**).
- **`Addenda` NO tiene tabla intermedia** (0 FKs en cualquier dirección). Relación por valor confirmada
  en proc `uspRegistroOrdenCompraProveedor`: `Comprobante A INNER JOIN Addenda B ON A.Uuid = B.Uuid WHERE B.Tipo = 1`.
  Es decir `Addenda.Uuid` = `Comprobante.Uuid` (folio fiscal), `Addenda.Tipo` filtra tipo de addenda.
  La usan 5 SPs (`sp_genera_poliza_ap*`, `uspRegistroOrdenCompraProveedor`). Mismo patrón que
  `addendum_manual` FBC (join por UUID, no PK). `CREATE TABLE Addenda` suelto basta local (sin deps).
- Trampas al publicar/consultar desde Git Bash: (1) container mssql host **1434** no 1433 → `/TargetServerName:localhost,1434`;
  (2) MSYS mangea `/opt/...` y `/Action:...` → prefijar `MSYS_NO_PATHCONV=1`; (3) el `.dacpac` trae LOGIN de
  dominio Windows inexistente local → `/p:ExcludeObjectTypes="Logins;Users;RoleMembership;Permissions;ServerRoleMembership;Credentials"`.
- `.dacpac` = solo esquema. Para datos de una tabla puntual → `bcp ... out/in -n` (data-only).
- No mezclar modelos: `SODIMAC_SAP_DEV` local = FBC nuevo (8 tablas); `SODIMAC_SAP_DEV_FULL` =
  autofacturador viejo (~70 tablas, solo esquema).
