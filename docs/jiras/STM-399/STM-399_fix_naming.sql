-- =============================================================
-- JIRA: STM-399 - Correccion de naming sobre lo desplegado por Bonelli
-- Descripcion: Homologar nombres de tablas, columnas, constraints
--              e indices al estandar ingles del proyecto
-- Esquema: tenant_finance
-- Autor: dmontes
-- Fecha: 2026-03-02
--
-- Prerequisito: v3 de Bonelli ya aplicada (finanzas_payment_headers
--               y payment_detail ya existen)
--
-- Cambios:
--   1. Renombrar tabla finanzas_payment_headers → payment_header
--   2. Renombrar columnas en español → ingles
--   3. FK se mantiene nullable (hay 3 registros sin cabecera en prod)
--   4. Renombrar constraints e indices obsoletos
-- =============================================================

BEGIN;

-- =============================================================
-- 1. RENOMBRAR TABLA CABECERA
-- =============================================================

ALTER TABLE tenant_finance.finanzas_payment_headers
    RENAME TO payment_header;

-- =============================================================
-- 2. RENOMBRAR COLUMNAS EN CABECERA (payment_header)
-- =============================================================

-- id_referencia_pago → payment_header_uuid
ALTER TABLE tenant_finance.payment_header
    RENAME COLUMN id_referencia_pago TO payment_header_uuid;

-- importe → total_amount
ALTER TABLE tenant_finance.payment_header
    RENAME COLUMN importe TO total_amount;

-- Renombrar PK
ALTER TABLE tenant_finance.payment_header
    RENAME CONSTRAINT finanzas_payment_headers_pkey TO pk_payment_header;

-- Renombrar UNIQUE
ALTER TABLE tenant_finance.payment_header
    RENAME CONSTRAINT uq_finanzas_payment_headers_group TO uq_payment_header_group;

-- Renombrar indices
ALTER INDEX tenant_finance.idx_finanzas_payment_headers_company_anio
    RENAME TO idx_payment_header_company_anio;

ALTER INDEX tenant_finance.idx_finanzas_payment_headers_vendor_payment_date
    RENAME TO idx_payment_header_vendor_payment_date;

-- =============================================================
-- 3. RENOMBRAR COLUMNA FK EN DETALLE (payment_detail)
-- =============================================================

-- Primero eliminar la FK existente (referencia nombre viejo)
ALTER TABLE tenant_finance.payment_detail
    DROP CONSTRAINT fk_finanzas_payments_header;

-- Renombrar columna id_referencia_pago → payment_header_uuid
ALTER TABLE tenant_finance.payment_detail
    RENAME COLUMN id_referencia_pago TO payment_header_uuid;

-- NOTA: FK se mantiene nullable porque hay 3 registros existentes
-- sin cabecera asignada. Se hara NOT NULL cuando la app los vincule.

-- Recrear FK con nombre correcto
ALTER TABLE tenant_finance.payment_detail
    ADD CONSTRAINT fk_payment_detail_header
    FOREIGN KEY (payment_header_uuid)
    REFERENCES tenant_finance.payment_header (payment_header_uuid);

-- =============================================================
-- 4. RENOMBRAR CONSTRAINTS E INDICES EN DETALLE (payment_detail)
-- =============================================================

-- PK
ALTER TABLE tenant_finance.payment_detail
    RENAME CONSTRAINT finanzas_payments_pkey TO pk_payment_detail;

-- Check constraints
ALTER TABLE tenant_finance.payment_detail
    RENAME CONSTRAINT chk_finanzas_payments_amount TO chk_payment_detail_amount;

ALTER TABLE tenant_finance.payment_detail
    RENAME CONSTRAINT chk_finanzas_payments_status TO chk_payment_detail_status;

-- Indices
ALTER INDEX tenant_finance.idx_finanzas_payments_date
    RENAME TO idx_payment_detail_date;

ALTER INDEX tenant_finance.idx_finanzas_payments_document
    RENAME TO idx_payment_detail_document;

ALTER INDEX tenant_finance.idx_finanzas_payments_id_referencia_pago
    RENAME TO idx_payment_detail_header;

ALTER INDEX tenant_finance.idx_finanzas_payments_sap_doc
    RENAME TO idx_payment_detail_sap_doc;

ALTER INDEX tenant_finance.idx_finanzas_payments_status
    RENAME TO idx_payment_detail_status;

ALTER INDEX tenant_finance.idx_finanzas_payments_vendor
    RENAME TO idx_payment_detail_vendor;

COMMIT;

-- =============================================================
-- VERIFICACION POST-EJECUCION
-- =============================================================
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'tenant_finance'
--   AND table_name IN ('payment_header', 'payment_detail')
-- ORDER BY table_name;
--
-- Resultado esperado:
--   payment_detail
--   payment_header
--
-- SELECT column_name FROM information_schema.columns
-- WHERE table_schema = 'tenant_finance'
--   AND table_name = 'payment_header'
-- ORDER BY ordinal_position;
--
-- Resultado esperado:
--   payment_header_uuid, company, anio, vendor_number, currency,
--   total_amount, payment_date, status, created_by, created_at,
--   updated_by, updated_at
-- =============================================================
