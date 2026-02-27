-- ============================================================================
-- STM-305: Creacion de Tablas para Estado de Cuenta
-- Esquema: tenant_finance
-- Fecha: 2026-02-06
--
-- Este script crea las 7 tablas del modelo de Estado de Cuenta:
-- 1. account_statement (tabla principal)
-- 2. account_statement_invoice
-- 3. account_statement_discount
-- 4. account_statement_credit_note
-- 5. account_statement_payment
-- 6. account_statement_purchase_order
-- 7. account_statement_reception
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- 1. TABLA PRINCIPAL: account_statement
-- Control, versionado y saldos del estado de cuenta
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement (
    -- Identificador
    account_statement_uuid  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Datos del proveedor y periodo
    vendor_number           BIGINT NOT NULL,
    year                    INTEGER NOT NULL,
    month                   SMALLINT NOT NULL CHECK (month BETWEEN 1 AND 12),
    version                 INTEGER NOT NULL DEFAULT 1,

    -- Estatus del estado de cuenta (1=Generado, 2=Publicado, 3=Revisado, 4=Rechazado, 5=Reprocesado)
    status                  INTEGER NOT NULL DEFAULT 1,

    -- Saldos
    initial_balance         NUMERIC(18,2) NOT NULL DEFAULT 0,
    final_balance           NUMERIC(18,2) NOT NULL DEFAULT 0,

    -- Fechas de proceso
    process_date            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    review_date             TIMESTAMP NULL,
    issue_date              TIMESTAMP NULL,
    period_start_date       DATE NOT NULL,
    period_end_date         DATE NOT NULL,

    -- Encadenamiento de versiones
    previous_statement_uuid UUID NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT uq_account_statement_vendor_period
        UNIQUE (vendor_number, year, month, version),
    CONSTRAINT fk_account_statement_previous
        FOREIGN KEY (previous_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement IS 'Tabla principal de estados de cuenta mensuales por proveedor';
COMMENT ON COLUMN tenant_finance.account_statement.vendor_number IS 'Numero del proveedor (referencia logica)';
COMMENT ON COLUMN tenant_finance.account_statement.version IS 'Version del estado de cuenta para reprocesos (1, 2, 3...)';
COMMENT ON COLUMN tenant_finance.account_statement.status IS '1=Generado, 2=Publicado, 3=Revisado, 4=Rechazado, 5=Reprocesado';
COMMENT ON COLUMN tenant_finance.account_statement.initial_balance IS 'Saldo inicial = final_balance del mes anterior';
COMMENT ON COLUMN tenant_finance.account_statement.final_balance IS 'Saldo final calculado';


-- ============================================================================
-- 2. TABLA: account_statement_invoice
-- Facturas pendientes y pagadas del periodo
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement_invoice (
    -- Identificador
    account_statement_invoice_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacion con estado de cuenta
    account_statement_uuid  UUID NOT NULL,

    -- Tipo de factura
    invoice_type            VARCHAR(20) NOT NULL CHECK (invoice_type IN ('PENDING', 'PAID')),

    -- Datos del CFDI
    series                  VARCHAR(50) NULL,
    folio                   VARCHAR(50) NULL,
    uuid                    VARCHAR(64) NULL,

    -- Fechas
    stamp_date              TIMESTAMP NULL,
    accounting_date         TIMESTAMP NULL,
    payment_date            TIMESTAMP NULL,

    -- Montos en moneda origen
    currency                VARCHAR(10) NOT NULL DEFAULT 'MXN',
    amount                  NUMERIC(18,2) NOT NULL,

    -- Conversion a moneda base
    exchange_rate           NUMERIC(18,6) NOT NULL DEFAULT 1,
    base_currency           VARCHAR(10) NOT NULL DEFAULT 'MXN',
    base_amount             NUMERIC(18,2) NOT NULL,

    -- Estatus y referencias
    invoice_status          VARCHAR(50) NULL,
    payment_id              BIGINT NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT fk_account_statement_invoice_statement
        FOREIGN KEY (account_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement_invoice IS 'Detalle de facturas del estado de cuenta';
COMMENT ON COLUMN tenant_finance.account_statement_invoice.invoice_type IS 'PENDING o PAID';
COMMENT ON COLUMN tenant_finance.account_statement_invoice.uuid IS 'UUID del CFDI para trazabilidad SAT';


-- ============================================================================
-- 3. TABLA: account_statement_discount
-- Descuentos comerciales del periodo
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement_discount (
    -- Identificador
    account_statement_discount_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacion con estado de cuenta
    account_statement_uuid  UUID NOT NULL,

    -- Datos del documento
    document_number         VARCHAR(50) NULL,
    reference_number        VARCHAR(50) NULL,
    series                  VARCHAR(50) NULL,
    folio                   VARCHAR(50) NULL,
    uuid                    VARCHAR(64) NULL,

    -- Fechas
    discount_date           TIMESTAMP NULL,
    accounting_date         TIMESTAMP NULL,

    -- Montos en moneda origen
    currency                VARCHAR(10) NOT NULL DEFAULT 'MXN',
    amount                  NUMERIC(18,2) NOT NULL,

    -- Conversion a moneda base
    exchange_rate           NUMERIC(18,6) NOT NULL DEFAULT 1,
    base_currency           VARCHAR(10) NOT NULL DEFAULT 'MXN',
    base_amount             NUMERIC(18,2) NOT NULL,

    -- Estatus
    status                  VARCHAR(50) NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT fk_account_statement_discount_statement
        FOREIGN KEY (account_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement_discount IS 'Detalle de descuentos comerciales del estado de cuenta';


-- ============================================================================
-- 4. TABLA: account_statement_credit_note
-- Notas de credito del periodo
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement_credit_note (
    -- Identificador
    account_statement_credit_note_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacion con estado de cuenta
    account_statement_uuid  UUID NOT NULL,

    -- Datos del documento
    document_number         VARCHAR(50) NULL,
    series                  VARCHAR(50) NULL,
    folio                   VARCHAR(50) NULL,
    uuid                    VARCHAR(64) NOT NULL,

    -- Fechas
    issue_date              TIMESTAMP NULL,
    accounting_date         TIMESTAMP NULL,

    -- Montos en moneda origen
    currency                VARCHAR(10) NOT NULL DEFAULT 'MXN',
    amount                  NUMERIC(18,2) NOT NULL,

    -- Conversion a moneda base
    exchange_rate           NUMERIC(18,6) NOT NULL DEFAULT 1,
    base_currency           VARCHAR(10) NOT NULL DEFAULT 'MXN',
    base_amount             NUMERIC(18,2) NOT NULL,

    -- Estatus
    status                  VARCHAR(50) NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT fk_account_statement_credit_note_statement
        FOREIGN KEY (account_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement_credit_note IS 'Detalle de notas de credito del estado de cuenta';
COMMENT ON COLUMN tenant_finance.account_statement_credit_note.uuid IS 'UUID de la nota de credito (requerido)';


-- ============================================================================
-- 5. TABLA: account_statement_payment
-- Pagos realizados en el periodo
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement_payment (
    -- Identificador
    account_statement_payment_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacion con estado de cuenta
    account_statement_uuid  UUID NOT NULL,

    -- Referencia al pago original
    payment_id              BIGINT NULL,

    -- Datos del documento
    document_number         VARCHAR(50) NULL,
    reference_number        VARCHAR(50) NULL,

    -- Fecha de pago
    payment_date            TIMESTAMP NULL,

    -- Montos en moneda origen
    currency                VARCHAR(10) NOT NULL DEFAULT 'MXN',
    amount                  NUMERIC(18,2) NOT NULL,

    -- Conversion a moneda base
    exchange_rate           NUMERIC(18,6) NOT NULL DEFAULT 1,
    base_currency           VARCHAR(10) NOT NULL DEFAULT 'MXN',
    base_amount             NUMERIC(18,2) NOT NULL,

    -- Estatus
    status                  VARCHAR(50) NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT fk_account_statement_payment_statement
        FOREIGN KEY (account_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement_payment IS 'Detalle de pagos del estado de cuenta';
COMMENT ON COLUMN tenant_finance.account_statement_payment.payment_id IS 'Referencia logica al pago original';


-- ============================================================================
-- 6. TABLA: account_statement_purchase_order
-- Ordenes de compra del periodo
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement_purchase_order (
    -- Identificador
    account_statement_purchase_order_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacion con estado de cuenta
    account_statement_uuid  UUID NOT NULL,

    -- Datos de la OC
    order_number            BIGINT NOT NULL,
    document_date           DATE NULL,
    due_date                DATE NULL,

    -- Montos en moneda origen
    currency                VARCHAR(10) NOT NULL DEFAULT 'MXN',
    amount                  NUMERIC(18,2) NOT NULL,

    -- Conversion a moneda base
    exchange_rate           NUMERIC(18,6) NOT NULL DEFAULT 1,
    base_currency           VARCHAR(10) NOT NULL DEFAULT 'MXN',
    base_amount             NUMERIC(18,2) NOT NULL,

    -- Estatus y trazabilidad
    status                  VARCHAR(50) NULL,
    source_id               BIGINT NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT fk_account_statement_po_statement
        FOREIGN KEY (account_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement_purchase_order IS 'Detalle de ordenes de compra del estado de cuenta';
COMMENT ON COLUMN tenant_finance.account_statement_purchase_order.source_id IS 'Referencia para trazabilidad a tabla origen';


-- ============================================================================
-- 7. TABLA: account_statement_reception
-- Recepciones de mercancia del periodo
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.account_statement_reception (
    -- Identificador
    account_statement_reception_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relacion con estado de cuenta
    account_statement_uuid  UUID NOT NULL,

    -- Datos de la recepcion
    reception_number        BIGINT NOT NULL,
    order_number            BIGINT NOT NULL,
    document_date           DATE NULL,
    reception_date          DATE NULL,
    due_date                DATE NULL,

    -- Montos en moneda origen
    currency                VARCHAR(10) NOT NULL DEFAULT 'MXN',
    amount                  NUMERIC(18,2) NOT NULL,

    -- Conversion a moneda base
    exchange_rate           NUMERIC(18,6) NOT NULL DEFAULT 1,
    base_currency           VARCHAR(10) NOT NULL DEFAULT 'MXN',
    base_amount             NUMERIC(18,2) NOT NULL,

    -- Estatus y trazabilidad
    status                  VARCHAR(50) NULL,
    source_id               BIGINT NULL,

    -- Auditoria
    created_by              BIGINT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT NULL,
    updated_at              TIMESTAMP NULL,

    -- Constraints
    CONSTRAINT fk_account_statement_reception_statement
        FOREIGN KEY (account_statement_uuid)
        REFERENCES tenant_finance.account_statement(account_statement_uuid)
);

COMMENT ON TABLE tenant_finance.account_statement_reception IS 'Detalle de recepciones del estado de cuenta';
COMMENT ON COLUMN tenant_finance.account_statement_reception.source_id IS 'Referencia para trazabilidad a tabla origen';


-- ============================================================================
-- FIN DEL SCRIPT DE CREACION DE TABLAS
-- ============================================================================
