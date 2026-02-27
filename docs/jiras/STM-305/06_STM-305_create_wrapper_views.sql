-- ============================================================================
-- STM-1378: Vistas Wrapper para Modulo Financiero y Fiscal
-- Fecha: 2026-02-13
--
-- Vistas wrapper que exponen las tablas base con prefijo vw_*
-- Incluyen campos de fecha contable y documento SAP donde aplica
-- ============================================================================

-- ============================================================================
-- MODULO FINANCIERO (tenant_finance)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. vw_rebate (Descuentos Comerciales)
-- Tabla origen: tenant_finance.rebate
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW tenant_finance.vw_rebate AS
SELECT
    rebate_uuid,
    document_number,
    reference_number,
    sap_document,              -- DocSap
    vendor_number,
    amount,
    source,
    period_id,
    due_date,
    posting_date,              -- Fecha contabilizacion
    status,
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_finance.rebate;

COMMENT ON VIEW tenant_finance.vw_rebate IS 'Vista wrapper de descuentos comerciales (rebate)';


-- ----------------------------------------------------------------------------
-- 2. vw_fiscal_payments (Pagos)
-- Tabla origen: tenant_finance.fiscal_payments
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW tenant_finance.vw_fiscal_payments AS
SELECT
    fiscal_payment_uuid,
    payment_number,
    company,
    document_number,
    reference_number,
    vendor_number,
    amount,
    currency,
    document_type,
    sap_document,              -- DocSap
    payment_date,              -- Fecha pago
    status,
    payment_method,
    bank_account,
    reference_payment,
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_finance.fiscal_payments;

COMMENT ON VIEW tenant_finance.vw_fiscal_payments IS 'Vista wrapper de pagos fiscales';


-- ----------------------------------------------------------------------------
-- 3. vw_purchase_order (Ordenes de Compra)
-- Tabla origen: tenant_finance.purchase_order
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW tenant_finance.vw_purchase_order AS
SELECT
    purchase_order_uuid,
    order_number,
    vendor_number,
    source_id,
    total_amount,
    currency,
    status,
    order_date,                -- Fecha OC
    delivery_date,
    terms_and_conditions,
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_finance.purchase_order;

COMMENT ON VIEW tenant_finance.vw_purchase_order IS 'Vista wrapper de ordenes de compra';


-- ----------------------------------------------------------------------------
-- 4. vw_reception (Recepcion)
-- Tabla origen: tenant_finance.reception
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW tenant_finance.vw_reception AS
SELECT
    reception_id,
    purchase_order_uuid,
    reception_number,
    origin_id,
    destination_id,
    amount,
    status,
    comment,
    reception_date,            -- Fecha recepcion
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_finance.reception;

COMMENT ON VIEW tenant_finance.vw_reception IS 'Vista wrapper de recepciones de mercancia';


-- ============================================================================
-- MODULO FISCAL (tenant_fiscal)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 5. vw_invoice (Factura - document_type = 'I')
-- Tabla origen: tenant_fiscal.invoice
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW tenant_fiscal.vw_invoice AS
SELECT
    invoice_uuid,
    fiscal_uuid,               -- UUID CFDI (SAT)
    place_of_issue,
    payment_method,
    document_type,
    total,
    exchange_rate,
    currency,
    discount,
    subtotal,
    payment_conditions,
    payment_form,
    issue_date,
    certification_date,        -- Fecha timbrado
    folio,
    series,
    version,
    xml_content,
    status,
    issuer_uuid,
    receiver_uuid,
    accounting_sent_date,
    accounting_date,           -- Fecha contabilizacion
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_fiscal.invoice
WHERE document_type = 'I';     -- Solo facturas (Ingreso)

COMMENT ON VIEW tenant_fiscal.vw_invoice IS 'Vista wrapper de facturas (CFDIs tipo Ingreso)';


-- ----------------------------------------------------------------------------
-- 6. vw_credit_note (Nota de Credito - document_type = 'E')
-- Tabla origen: tenant_fiscal.invoice
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW tenant_fiscal.vw_credit_note AS
SELECT
    invoice_uuid AS credit_note_uuid,
    fiscal_uuid,               -- UUID CFDI (SAT)
    place_of_issue,
    payment_method,
    document_type,
    total,
    exchange_rate,
    currency,
    discount,
    subtotal,
    payment_conditions,
    payment_form,
    issue_date,
    certification_date,        -- Fecha timbrado
    folio,
    series,
    version,
    xml_content,
    status,
    issuer_uuid,
    receiver_uuid,
    accounting_sent_date,
    accounting_date,           -- Fecha contabilizacion
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_fiscal.invoice
WHERE document_type = 'E';     -- Solo notas de credito (Egreso)

COMMENT ON VIEW tenant_fiscal.vw_credit_note IS 'Vista wrapper de notas de credito (CFDIs tipo Egreso)';


-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
