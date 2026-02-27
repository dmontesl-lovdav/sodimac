# Entorno de Desarrollo Local

> PC personal de desarrollo. La base de datos corre en Docker con volumen persistente.
> Los proyectos se compilan con Maven y se ejecutan como Spring Boot standalone.

## Prerequisitos

| Herramienta | Version | Verificar |
|-------------|---------|-----------|
| Java | 17 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | - | `docker --version` |
| Git | - | `git --version` |

## Base de Datos (Docker)

### Iniciar PostgreSQL

```bash
docker start sodimac-pg
```

Si el container no existe, crearlo con volumen persistente:

```bash
docker volume create sodimac-pgdata
docker run -d --name sodimac-pg \
  -e POSTGRES_PASSWORD=postgres \
  -p 5434:5432 \
  -v sodimac-pgdata:/var/lib/postgresql/data \
  postgres:16
```

### Verificar que esta corriendo

```bash
docker exec sodimac-pg pg_isready -U postgres
```

### Datos de conexion

| Parametro | Valor |
|-----------|-------|
| **Host** | `localhost` |
| **Puerto** | `5434` |
| **Base de datos** | `b2b_portal` |
| **Usuario** | `wwwb2bportal` |
| **Password** | `b8@qU0YM1HU>` |
| **Superusuario** | `postgres` / `postgres` |

### Esquemas cargados

| Esquema | Tablas | Proyecto |
|---------|--------|----------|
| `tenant_fiscal` | 22 | `fiscal-api` |
| `shared_catalogs` | 10 | `catalogos-api` |
| `tenant_finance` | 29 | `finanzas-api` |
| `core_audit` | 2 | `auditoria-api` |
| `core_utils` | 7 | `util-api` |
| `public` | 36 | - |

### Consulta rapida para validar la base

```sql
-- Ejecutar desde DBeaver, psql o cualquier cliente conectado a b2b_portal
SELECT table_schema, count(*) AS tablas
FROM information_schema.tables
WHERE table_schema IN ('tenant_fiscal','shared_catalogs','tenant_finance','core_audit','core_utils','public')
AND table_type = 'BASE TABLE'
GROUP BY table_schema
ORDER BY table_schema;
```

### Reconstruir la base desde cero

Si se pierde el volumen o se necesita recrear, los dumps SQL estan en `docs/db/`:

```
docs/db/fiscal/      → tenant_fiscal (DDL + datos)
docs/db/catalogs/    → shared_catalogs
docs/db/finance/     → tenant_finance
docs/db/audit/       → core_audit
docs/db/utils/       → core_utils
docs/db/public/      → public
```

Procedimiento completo:

```bash
# 1. Crear base de datos
docker exec sodimac-pg psql -U postgres -c "CREATE DATABASE b2b_portal;"

# 2. Crear usuario y esquemas
docker exec sodimac-pg psql -U postgres -d b2b_portal -c "
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'wwwb2bportal') THEN
        CREATE ROLE wwwb2bportal WITH LOGIN PASSWORD 'b8@qU0YM1HU>';
    END IF;
END
\$\$;
CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";
GRANT ALL PRIVILEGES ON DATABASE b2b_portal TO wwwb2bportal;
CREATE SCHEMA IF NOT EXISTS tenant_fiscal AUTHORIZATION wwwb2bportal;
CREATE SCHEMA IF NOT EXISTS shared_catalogs AUTHORIZATION wwwb2bportal;
CREATE SCHEMA IF NOT EXISTS tenant_finance AUTHORIZATION wwwb2bportal;
CREATE SCHEMA IF NOT EXISTS core_audit AUTHORIZATION wwwb2bportal;
CREATE SCHEMA IF NOT EXISTS core_utils AUTHORIZATION wwwb2bportal;
"

# 3. Copiar archivos SQL al container
docker cp docs/db/fiscal sodimac-pg:/tmp/fiscal
docker cp docs/db/catalogs sodimac-pg:/tmp/catalogs
docker cp docs/db/finance sodimac-pg:/tmp/finance
docker cp docs/db/audit sodimac-pg:/tmp/audit
docker cp docs/db/utils sodimac-pg:/tmp/utils
docker cp docs/db/public sodimac-pg:/tmp/public

# 4. Ejecutar DDL + datos por esquema (NOTA: en Git Bash usar MSYS_NO_PATHCONV=1 antes de docker exec)

# tenant_fiscal
docker exec sodimac-pg psql -U postgres -d b2b_portal \
  -f /tmp/fiscal/dll-fiscal.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208813169.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208819212.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208807638.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208815276.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208825048.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208811569.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208812450.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208819823.sql \
  -f /tmp/fiscal/insert-fiscal.sql.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208821156.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208822555.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208816552.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208816035.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208820540.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208808664.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208824229.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208818495.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208813908.sql \
  -f /tmp/fiscal/insert-fiscal.sql-1772208810300.sql

# shared_catalogs (supplier_type ANTES de supplier)
docker exec sodimac-pg psql -U postgres -d b2b_portal \
  -f /tmp/catalogs/catalogs-dll.sql \
  -f /tmp/catalogs/insert-catalogs-1772213039428.sql \
  -f /tmp/catalogs/insert-catalogs-1772213038401.sql \
  -f /tmp/catalogs/insert-catalogs-1772213038862.sql \
  -f /tmp/catalogs/insert-catalogs-1772213040010.sql \
  -f /tmp/catalogs/insert-catalogs-1772213040465.sql \
  -f /tmp/catalogs/insert-catalogs-1772213041101.sql \
  -f /tmp/catalogs/insert-catalogs-1772213043065.sql \
  -f /tmp/catalogs/insert-catalogs-1772213041716.sql \
  -f /tmp/catalogs/insert-catalogs-1772213042523.sql

# tenant_finance (stamped_rebate ANTES de rebate)
docker exec sodimac-pg psql -U postgres -d b2b_portal \
  -f /tmp/finance/finance-dll.sql \
  -f /tmp/finance/insert-finance.sql \
  -f /tmp/finance/insert-finance-1772212892085.sql \
  -f /tmp/finance/insert-finance-1772212892689.sql \
  -f /tmp/finance/insert-finance-1772212901751.sql \
  -f /tmp/finance/insert-finance-1772212884301.sql \
  -f /tmp/finance/insert-finance-1772212884980.sql \
  -f /tmp/finance/insert-finance-1772212885625.sql \
  -f /tmp/finance/insert-finance-1772212886304.sql \
  -f /tmp/finance/insert-finance-1772212886947.sql \
  -f /tmp/finance/insert-finance-1772212887594.sql \
  -f /tmp/finance/insert-finance-1772212888236.sql \
  -f /tmp/finance/insert-finance-1772212890770.sql \
  -f /tmp/finance/insert-finance-1772212893221.sql \
  -f /tmp/finance/insert-finance-1772212898117.sql \
  -f /tmp/finance/insert-finance-1772212893719.sql \
  -f /tmp/finance/insert-finance-1772212894539.sql \
  -f /tmp/finance/insert-finance-1772212895045.sql \
  -f /tmp/finance/insert-finance-1772212895670.sql \
  -f /tmp/finance/insert-finance-1772212896334.sql \
  -f /tmp/finance/insert-finance-1772212896983.sql \
  -f /tmp/finance/insert-finance-1772212897595.sql \
  -f /tmp/finance/insert-finance-1772212898623.sql \
  -f /tmp/finance/insert-finance-1772212899233.sql \
  -f /tmp/finance/insert-finance-1772212899880.sql \
  -f /tmp/finance/insert-finance-1772212901110.sql \
  -f /tmp/finance/insert-finance-1772212901751.sql

# core_audit
docker exec sodimac-pg psql -U postgres -d b2b_portal \
  -f /tmp/audit/audit-dll.sql \
  -f /tmp/audit/insert-audit.sql

# core_utils (cat_module y cat_message ANTES de application_msg y cat_parameter)
docker exec sodimac-pg psql -U postgres -d b2b_portal \
  -f /tmp/utils/utils-dll.sql \
  -f /tmp/utils/insert-utils-1772213244599.sql \
  -f /tmp/utils/insert-utils-1772213243978.sql \
  -f /tmp/utils/insert-utils-1772213245587.sql \
  -f /tmp/utils/insert-utils-1772213245108.sql \
  -f /tmp/utils/insert-utils-1772213246839.sql \
  -f /tmp/utils/insert-utils.sql \
  -f /tmp/utils/insert-utils-1772213246211.sql

# public
docker exec sodimac-pg psql -U postgres -d b2b_portal \
  -f /tmp/public/public-dll.sql \
  -f /tmp/public/insert-public.sql \
  -f /tmp/public/insert-public-1772213497871.sql

# 5. Otorgar permisos
docker exec sodimac-pg psql -U postgres -d b2b_portal -c "
GRANT USAGE ON SCHEMA tenant_fiscal TO wwwb2bportal;
GRANT USAGE ON SCHEMA shared_catalogs TO wwwb2bportal;
GRANT USAGE ON SCHEMA tenant_finance TO wwwb2bportal;
GRANT USAGE ON SCHEMA core_audit TO wwwb2bportal;
GRANT USAGE ON SCHEMA core_utils TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tenant_fiscal TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA shared_catalogs TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tenant_finance TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_audit TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core_utils TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA tenant_fiscal TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA shared_catalogs TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA tenant_finance TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_audit TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA core_utils TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO wwwb2bportal;
"
```

## Proyectos Backend

### Compilar un proyecto

```bash
cd APP03022-mrch.backend.somx.fiscal-api
mvn clean package -DskipTests -q
```

### Ejecutar un proyecto

```bash
java -jar target/fiscal-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Proyectos y puertos

| Proyecto | Puerto | Perfil | Swagger |
|----------|--------|--------|---------|
| `fiscal-api` | 8082 | dev | http://localhost:8082/swagger-ui/index.html |
| `catalogos-api` | 8083 | dev | http://localhost:8083/swagger-ui/index.html |
| `finanzas-api` | - | dev | pendiente verificar |
| `util-api` | - | dev | pendiente verificar |
| `auditoria-api` | - | dev | pendiente verificar |

### Verificar que un proyecto esta corriendo

```bash
curl http://localhost:8082/actuator/health
```

## Notas

- En Git Bash (Windows) usar `MSYS_NO_PATHCONV=1` antes de `docker exec` para evitar traduccion de rutas `/tmp`
- Los proyectos usan perfil `dev` por defecto (`spring.profiles.active=dev`)
- En perfil `dev` la seguridad JWT esta deshabilitada
- El container Docker persiste datos en el volumen `sodimac-pgdata` (sobrevive a `docker rm`)
- Para destruir TODO: `docker volume rm sodimac-pgdata` (esto SI borra los datos)
