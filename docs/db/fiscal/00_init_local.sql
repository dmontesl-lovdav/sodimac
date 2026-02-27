-- ============================================================
-- Script de inicializacion local: tenant_fiscal
-- Base de datos: b2b_portal | Esquema: tenant_fiscal
-- Puerto: 5434 | Usuario: wwwb2bportal
--
-- USO:
--   psql -h localhost -p 5434 -U postgres -f 00_init_local.sql
-- ============================================================

-- 1. Crear usuario y base de datos
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'wwwb2bportal') THEN
        CREATE ROLE wwwb2bportal WITH LOGIN PASSWORD 'b8@qU0YM1HU>';
    END IF;
END
$$;

SELECT 'Creando base de datos b2b_portal...' AS step;

-- Nota: CREATE DATABASE no se puede ejecutar dentro de un bloque transaccional.
-- Si la base ya existe, este comando fallara y es esperado.
-- Ejecutar manualmente si es necesario:
--   CREATE DATABASE b2b_portal OWNER wwwb2bportal;

-- 2. Conectar a b2b_portal
\c b2b_portal

-- 3. Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE b2b_portal TO wwwb2bportal;

-- 4. Crear esquema
CREATE SCHEMA IF NOT EXISTS tenant_fiscal AUTHORIZATION wwwb2bportal;

-- 5. Establecer search_path
SET search_path TO tenant_fiscal, public;

-- ============================================================
-- DDL: Estructura de tablas
-- ============================================================
\i /tmp/fiscal/dll-fiscal.sql

-- ============================================================
-- DML: Datos (en orden de dependencias)
-- ============================================================

-- Tablas base (sin FK)
\i /tmp/fiscal/insert-fiscal.sql-1772208813169.sql
-- ^ issuer

\i /tmp/fiscal/insert-fiscal.sql-1772208819212.sql
-- ^ receiver

\i /tmp/fiscal/insert-fiscal.sql-1772208807638.sql
-- ^ authorized_receiver_catalog

\i /tmp/fiscal/insert-fiscal.sql-1772208815276.sql
-- ^ pac_catalog

\i /tmp/fiscal/insert-fiscal.sql-1772208825048.sql
-- ^ version_catalog

-- Tablas principales
\i /tmp/fiscal/insert-fiscal.sql-1772208811569.sql
-- ^ invoice

\i /tmp/fiscal/insert-fiscal.sql-1772208812450.sql
-- ^ invoice_status_history

\i /tmp/fiscal/insert-fiscal.sql-1772208819823.sql
-- ^ related_cfdi

-- Addendums (desde archivo consolidado)
\i /tmp/fiscal/insert-fiscal.sql.sql
-- ^ addendum

-- Impuestos
\i /tmp/fiscal/insert-fiscal.sql-1772208821156.sql
-- ^ tax

-- tax_detail (vacio)
-- \i /tmp/fiscal/insert-fiscal.sql-1772208817364.sql

\i /tmp/fiscal/insert-fiscal.sql-1772208822555.sql
-- ^ tax_transfer

-- tax_withholding (vacio)
-- \i /tmp/fiscal/insert-fiscal.sql-1772208823377.sql

-- Pagos
\i /tmp/fiscal/insert-fiscal.sql-1772208816552.sql
-- ^ payments

\i /tmp/fiscal/insert-fiscal.sql-1772208816035.sql
-- ^ payment

\i /tmp/fiscal/insert-fiscal.sql-1772208820540.sql
-- ^ related_documents

\i /tmp/fiscal/insert-fiscal.sql-1772208808664.sql
-- ^ equivalence_dr

\i /tmp/fiscal/insert-fiscal.sql-1772208824229.sql
-- ^ totals

-- payment_file_registry (vacio)
-- \i /tmp/fiscal/insert-fiscal.sql-1772208821871.sql

\i /tmp/fiscal/insert-fiscal.sql-1772208818495.sql
-- ^ payment_response_catalog

\i /tmp/fiscal/insert-fiscal.sql-1772208813908.sql
-- ^ log

\i /tmp/fiscal/insert-fiscal.sql-1772208810300.sql
-- ^ flyway_schema_history

-- ============================================================
-- 6. Otorgar permisos sobre todos los objetos
-- ============================================================
GRANT USAGE ON SCHEMA tenant_fiscal TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tenant_fiscal TO wwwb2bportal;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA tenant_fiscal TO wwwb2bportal;

-- ============================================================
-- 7. Validacion
-- ============================================================
SELECT 'TABLAS CREADAS:' AS resultado, count(*) AS total
FROM information_schema.tables
WHERE table_schema = 'tenant_fiscal';

SELECT table_name,
       (xpath('/row/cnt/text()', xml_count))[1]::text AS row_count
FROM (
    SELECT table_name,
           query_to_xml(format('SELECT count(*) AS cnt FROM tenant_fiscal.%I', table_name), false, true, '') AS xml_count
    FROM information_schema.tables
    WHERE table_schema = 'tenant_fiscal'
    AND table_type = 'BASE TABLE'
    ORDER BY table_name
) t;

SELECT '=== INSTALACION COMPLETADA ===' AS status;
