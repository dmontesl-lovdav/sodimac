-- =============================================================
-- JIRA: STM-399
-- Descripcion: Ajuste al MER Financiero para registrar el pago
--              a proveedores con estructura cabecera-detalle
-- Esquema: tenant_finance
-- Autor: dmontes
-- Fecha: 2026-03-02
--
-- Cambios:
--   1. Crear tabla payment_header (cabecera de pago agrupado)
--   2. Renombrar finanzas_payments → payment_detail
--   3. Agregar FK payment_header_uuid en payment_detail (NOT NULL)
--
-- Notas:
--   - En produccion NO hay datos en finanzas_payments, por lo
--     que la FK se crea directamente como NOT NULL.
--   - El campo "year" no se almacena; se deriva de payment_date
--     con EXTRACT(YEAR FROM payment_date) cuando se necesite.
--   - La tabla fiscal_payments NO se modifica en este JIRA.
-- =============================================================

BEGIN;

-- =============================================================
-- 1. CREAR TABLA: payment_header
-- =============================================================

CREATE TABLE tenant_finance.payment_header (
    payment_header_uuid UUID           NOT NULL DEFAULT gen_random_uuid(),
    company             INTEGER        NOT NULL,
    vendor_number       INTEGER        NOT NULL,
    currency            VARCHAR(3)     NOT NULL DEFAULT 'MXN',
    total_amount        NUMERIC(15, 2) NOT NULL,
    payment_date        DATE           NOT NULL,
    status              INTEGER        NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT,
    updated_at          TIMESTAMP,
    CONSTRAINT pk_payment_header PRIMARY KEY (payment_header_uuid)
);

CREATE INDEX idx_payment_header_vendor       ON tenant_finance.payment_header (vendor_number);
CREATE INDEX idx_payment_header_company      ON tenant_finance.payment_header (company);
CREATE INDEX idx_payment_header_payment_date ON tenant_finance.payment_header (payment_date);
CREATE INDEX idx_payment_header_status       ON tenant_finance.payment_header (status);

-- =============================================================
-- 2. RENOMBRAR TABLA: finanzas_payments → payment_detail
-- =============================================================

ALTER TABLE tenant_finance.finanzas_payments RENAME TO payment_detail;

-- =============================================================
-- 3. AGREGAR FK: payment_detail → payment_header
-- =============================================================

ALTER TABLE tenant_finance.payment_detail
    ADD COLUMN payment_header_uuid UUID NOT NULL;

ALTER TABLE tenant_finance.payment_detail
    ADD CONSTRAINT fk_payment_detail_header
    FOREIGN KEY (payment_header_uuid)
    REFERENCES tenant_finance.payment_header (payment_header_uuid);

CREATE INDEX idx_payment_detail_header ON tenant_finance.payment_detail (payment_header_uuid);

COMMIT;

-- =============================================================
-- VERIFICACION POST-EJECUCION
-- =============================================================
-- Ejecutar despues del script para validar:
--
-- SELECT tablename FROM pg_tables
-- WHERE schemaname = 'tenant_finance'
--   AND tablename IN ('payment_header', 'payment_detail')
-- ORDER BY tablename;
--
-- Resultado esperado:
--   payment_detail
--   payment_header
-- =============================================================
