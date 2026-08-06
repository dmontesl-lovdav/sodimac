-- ============================================================================
-- Unificación de identidad de usuario a UUID en fiscal (2026-08-06)
-- Base: b2b_portal | Esquema: tenant_fiscal
--
-- Cambia a UUID todas las columnas de identidad de usuario: created_by, updated_by,
-- user_id y changed_by (invoice_status_history). Motivo: el front y el token manejan
-- el usuario como UUID (sub); las columnas eran bigint/integer y provocaban errores
-- de binding (ver complemento idUsuario). El histórico NO importa -> USING NULL.
--
-- Las vistas vw_invoice / vw_credit_note dependen de esas columnas -> Postgres bloquea
-- el ALTER. Se capturan sus definiciones, se dropean, se altera y se recrean igual.
--
-- Idempotente: solo altera columnas que aún son int8/int4 (si ya son uuid, las salta).
-- Excluye tablas de backup (*orphan_backup*).
-- VA COORDINADO con el despliegue de: fiscal-api (entidades/DTOs UUID), front (2
-- endpoints mandan UUID) y batches de Robert (fiscal-download, invoice-status-sync).
-- ============================================================================

BEGIN;

DO $$
DECLARE
    r RECORD;
    v_vw_invoice TEXT;
    v_vw_credit  TEXT;
BEGIN
    -- 1) Capturar definición actual de las vistas dependientes
    v_vw_invoice := pg_get_viewdef('tenant_fiscal.vw_invoice'::regclass, true);
    v_vw_credit  := pg_get_viewdef('tenant_fiscal.vw_credit_note'::regclass, true);

    -- 2) Dropear las vistas para poder alterar las columnas
    DROP VIEW IF EXISTS tenant_fiscal.vw_invoice;
    DROP VIEW IF EXISTS tenant_fiscal.vw_credit_note;

    -- 3) Alterar todas las columnas de identidad int8/int4 -> uuid (USING NULL)
    FOR r IN
        SELECT c.relname AS tabla, a.attname AS columna
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_attribute a ON a.attrelid = c.oid
        JOIN pg_type t ON t.oid = a.atttypid
        WHERE n.nspname = 'tenant_fiscal'
          AND c.relkind = 'r'
          AND a.attnum > 0 AND NOT a.attisdropped
          AND a.attname IN ('created_by','updated_by','user_id','changed_by')
          AND t.typname IN ('int8','int4')
          AND c.relname NOT LIKE '%orphan_backup%'
        ORDER BY c.relname, a.attname
    LOOP
        EXECUTE format('ALTER TABLE tenant_fiscal.%I ALTER COLUMN %I TYPE uuid USING NULL', r.tabla, r.columna);
        RAISE NOTICE 'ALTER tenant_fiscal.% . % -> uuid', r.tabla, r.columna;
    END LOOP;

    -- 4) Recrear las vistas con su definición original (ahora reflejan uuid)
    EXECUTE 'CREATE VIEW tenant_fiscal.vw_invoice AS ' || v_vw_invoice;
    EXECUTE 'CREATE VIEW tenant_fiscal.vw_credit_note AS ' || v_vw_credit;
END $$;

-- Verificación: todas deben quedar 'uuid'
SELECT c.relname AS tabla, a.attname AS columna, t.typname AS tipo
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
JOIN pg_attribute a ON a.attrelid = c.oid
JOIN pg_type t ON t.oid = a.atttypid
WHERE n.nspname = 'tenant_fiscal'
  AND c.relkind IN ('r','v')
  AND a.attname IN ('created_by','updated_by','user_id','changed_by')
  AND c.relname NOT LIKE '%orphan_backup%'
ORDER BY c.relname, a.attname;

COMMIT;
