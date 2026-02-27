-- ============================================================================
-- STM-1378: Creacion de Tabla Three Way Match
-- Esquema: tenant_finance
-- Fecha: 2026-02-11
--
-- Tabla snapshot para la conciliacion de tres vias:
-- Orden de Compra + Recepcion + Factura
--
-- Tipos de datos alineados con tablas origen:
-- - purchase_order (tenant_finance)
-- - reception (tenant_finance)
-- - invoice (tenant_fiscal)
-- - fiscal_payments (tenant_finance)
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- 1. TABLA PRINCIPAL: three_way_match
-- Snapshot de conciliacion OC + Recepcion + Factura + NC + Pago
-- ============================================================================

CREATE TABLE IF NOT EXISTS tenant_finance.three_way_match (
    -- Primary Key
    three_way_match_uuid        UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Proveedor (de purchase_order.vendor_number INTEGER)
    vendor_number               INTEGER NOT NULL,

    -- Orden de Compra (de purchase_order)
    purchase_order_number       VARCHAR(50) NOT NULL,   -- order_number VARCHAR(50)
    purchase_order_date         DATE,                   -- order_date DATE
    purchase_order_amount       NUMERIC(15,2),          -- total_amount NUMERIC(15,2)

    -- Recepcion (de reception)
    reception_number            VARCHAR(50),            -- reception_number VARCHAR(50)
    reception_date              DATE,                   -- reception_date DATE
    reception_amount            NUMERIC(16,2),          -- amount NUMERIC(16,2)

    -- Factura CFDI (de invoice tenant_fiscal)
    invoice_series              VARCHAR(25),            -- series VARCHAR(25)
    invoice_folio               VARCHAR(49),            -- folio VARCHAR(49)
    invoice_uuid                UUID,                   -- fiscal_uuid UUID
    invoice_stamp_date          TIMESTAMP,              -- certification_date TIMESTAMP
    invoice_amount              NUMERIC(16,2),          -- total NUMERIC(16,2)

    -- Nota de Credito (de invoice donde document_type='E')
    credit_note_number          VARCHAR(50),
    credit_note_amount          NUMERIC(16,2),

    -- Documento Contable (de fiscal_payments y sap_document)
    document_number             VARCHAR(100),           -- document_number VARCHAR(100)
    sap_document                VARCHAR(50),            -- sap_document VARCHAR(50)
    accounting_date             DATE,                   -- accounting_date DATE
    accounting_amount           NUMERIC(15,2),          -- amount NUMERIC(15,2)

    -- Pago (de fiscal_payments)
    payment_reference           VARCHAR(100),           -- reference_payment VARCHAR(100)
    payment_date                DATE,                   -- payment_date DATE
    payment_amount              NUMERIC(15,2),          -- amount NUMERIC(15,2)

    -- Moneda (de invoice)
    currency                    VARCHAR(3) DEFAULT 'MXN',     -- currency VARCHAR(3)
    exchange_rate               NUMERIC(18,6) DEFAULT 1.000000, -- exchange_rate NUMERIC(18,6)

    -- Estatus (catalogo CatEstatusTWM: 1-5)
    status                      INTEGER NOT NULL DEFAULT 1,

    -- Auditoria
    created_by                  BIGINT,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  BIGINT,
    updated_at                  TIMESTAMP
);

-- Comentarios
COMMENT ON TABLE tenant_finance.three_way_match IS 'Snapshot de conciliacion Three Way Match: OC + Recepcion + Factura';
COMMENT ON COLUMN tenant_finance.three_way_match.three_way_match_uuid IS 'Identificador unico del registro (PK)';
COMMENT ON COLUMN tenant_finance.three_way_match.vendor_number IS 'Numero del proveedor (de purchase_order)';
COMMENT ON COLUMN tenant_finance.three_way_match.purchase_order_number IS 'Numero de orden de compra (de purchase_order.order_number)';
COMMENT ON COLUMN tenant_finance.three_way_match.purchase_order_date IS 'Fecha de la OC (de purchase_order.order_date)';
COMMENT ON COLUMN tenant_finance.three_way_match.purchase_order_amount IS 'Monto de la OC (de purchase_order.total_amount)';
COMMENT ON COLUMN tenant_finance.three_way_match.reception_number IS 'Numero de recepcion (de reception.reception_number)';
COMMENT ON COLUMN tenant_finance.three_way_match.reception_date IS 'Fecha de recepcion (de reception.reception_date)';
COMMENT ON COLUMN tenant_finance.three_way_match.reception_amount IS 'Monto de recepcion (de reception.amount)';
COMMENT ON COLUMN tenant_finance.three_way_match.invoice_series IS 'Serie del CFDI (de invoice.series)';
COMMENT ON COLUMN tenant_finance.three_way_match.invoice_folio IS 'Folio del CFDI (de invoice.folio)';
COMMENT ON COLUMN tenant_finance.three_way_match.invoice_uuid IS 'UUID del CFDI SAT (de invoice.fiscal_uuid)';
COMMENT ON COLUMN tenant_finance.three_way_match.invoice_stamp_date IS 'Fecha timbrado (de invoice.certification_date)';
COMMENT ON COLUMN tenant_finance.three_way_match.invoice_amount IS 'Monto factura (de invoice.total)';
COMMENT ON COLUMN tenant_finance.three_way_match.credit_note_number IS 'Numero de nota de credito';
COMMENT ON COLUMN tenant_finance.three_way_match.credit_note_amount IS 'Monto de nota de credito';
COMMENT ON COLUMN tenant_finance.three_way_match.document_number IS 'Numero documento (de fiscal_payments.document_number)';
COMMENT ON COLUMN tenant_finance.three_way_match.sap_document IS 'Documento SAP (de fiscal_payments.sap_document)';
COMMENT ON COLUMN tenant_finance.three_way_match.accounting_date IS 'Fecha contable (de invoice.accounting_date)';
COMMENT ON COLUMN tenant_finance.three_way_match.accounting_amount IS 'Monto contable';
COMMENT ON COLUMN tenant_finance.three_way_match.payment_reference IS 'Referencia pago (de fiscal_payments.reference_payment)';
COMMENT ON COLUMN tenant_finance.three_way_match.payment_date IS 'Fecha pago (de fiscal_payments.payment_date)';
COMMENT ON COLUMN tenant_finance.three_way_match.payment_amount IS 'Monto pago (de fiscal_payments.amount)';
COMMENT ON COLUMN tenant_finance.three_way_match.currency IS 'Moneda (de invoice.currency)';
COMMENT ON COLUMN tenant_finance.three_way_match.exchange_rate IS 'Tipo de cambio (de invoice.exchange_rate)';
COMMENT ON COLUMN tenant_finance.three_way_match.status IS 'Estatus conciliacion (CatEstatusTWM: 1=Pendiente, 2=Parcial, 3=Conciliado, 4=Discrepancia, 5=Cerrado)';


-- ============================================================================
-- 2. CONSTRAINT UNIQUE (segun JIRA)
-- ============================================================================

ALTER TABLE tenant_finance.three_way_match
ADD CONSTRAINT uq_twm_vendor_po_reception
UNIQUE (vendor_number, purchase_order_number, reception_number);


-- ============================================================================
-- 3. INDICES
-- ============================================================================

-- Proveedor (consultas frecuentes)
CREATE INDEX IF NOT EXISTS ix_twm_vendor_number
ON tenant_finance.three_way_match (vendor_number);

-- Orden de compra
CREATE INDEX IF NOT EXISTS ix_twm_purchase_order_number
ON tenant_finance.three_way_match (purchase_order_number);

-- Recepcion
CREATE INDEX IF NOT EXISTS ix_twm_reception_number
ON tenant_finance.three_way_match (reception_number);

-- UUID Factura
CREATE INDEX IF NOT EXISTS ix_twm_invoice_uuid
ON tenant_finance.three_way_match (invoice_uuid);

-- Estatus
CREATE INDEX IF NOT EXISTS ix_twm_status
ON tenant_finance.three_way_match (status);

-- Fecha creacion
CREATE INDEX IF NOT EXISTS ix_twm_created_at
ON tenant_finance.three_way_match (created_at DESC);

-- Compuesto: Proveedor + Estatus
CREATE INDEX IF NOT EXISTS ix_twm_vendor_status
ON tenant_finance.three_way_match (vendor_number, status);

-- Documento SAP
CREATE INDEX IF NOT EXISTS ix_twm_sap_document
ON tenant_finance.three_way_match (sap_document);


-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
