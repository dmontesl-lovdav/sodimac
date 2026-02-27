-- ============================================================================
-- STM-305: Creacion de Indices para Estado de Cuenta
-- Esquema: tenant_finance
-- Fecha: 2026-02-06
--
-- Indices requeridos para consultas rapidas a mes vencido
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- INDICES PARA: account_statement
-- ============================================================================

-- Indice principal para busqueda por proveedor y periodo
CREATE INDEX IF NOT EXISTS ix_account_statement_vendor_period
    ON tenant_finance.account_statement (vendor_number, year, month, version);

-- Indice para filtrar por estatus
CREATE INDEX IF NOT EXISTS ix_account_statement_status
    ON tenant_finance.account_statement (status);

-- Indice para buscar ultima version publicada
CREATE INDEX IF NOT EXISTS ix_account_statement_published
    ON tenant_finance.account_statement (vendor_number, year, month)
    WHERE status = 2;

-- Indice para encadenamiento de versiones
CREATE INDEX IF NOT EXISTS ix_account_statement_previous
    ON tenant_finance.account_statement (previous_statement_uuid)
    WHERE previous_statement_uuid IS NOT NULL;


-- ============================================================================
-- INDICES PARA: account_statement_invoice
-- ============================================================================

-- Indice por estado de cuenta (requerido por el JIRA)
CREATE INDEX IF NOT EXISTS ix_account_statement_invoice_statement
    ON tenant_finance.account_statement_invoice (account_statement_uuid);

-- Indice por tipo de factura para filtrado rapido
CREATE INDEX IF NOT EXISTS ix_account_statement_invoice_type
    ON tenant_finance.account_statement_invoice (account_statement_uuid, invoice_type);

-- Indice por UUID para trazabilidad
CREATE INDEX IF NOT EXISTS ix_account_statement_invoice_uuid
    ON tenant_finance.account_statement_invoice (uuid)
    WHERE uuid IS NOT NULL;


-- ============================================================================
-- INDICES PARA: account_statement_discount
-- ============================================================================

-- Indice por estado de cuenta (requerido por el JIRA)
CREATE INDEX IF NOT EXISTS ix_account_statement_discount_statement
    ON tenant_finance.account_statement_discount (account_statement_uuid);


-- ============================================================================
-- INDICES PARA: account_statement_credit_note
-- ============================================================================

-- Indice por estado de cuenta (requerido por el JIRA)
CREATE INDEX IF NOT EXISTS ix_account_statement_credit_note_statement
    ON tenant_finance.account_statement_credit_note (account_statement_uuid);

-- Indice por UUID para trazabilidad
CREATE INDEX IF NOT EXISTS ix_account_statement_credit_note_uuid
    ON tenant_finance.account_statement_credit_note (uuid);


-- ============================================================================
-- INDICES PARA: account_statement_payment
-- ============================================================================

-- Indice por estado de cuenta (requerido por el JIRA)
CREATE INDEX IF NOT EXISTS ix_account_statement_payment_statement
    ON tenant_finance.account_statement_payment (account_statement_uuid);

-- Indice por referencia al pago original
CREATE INDEX IF NOT EXISTS ix_account_statement_payment_ref
    ON tenant_finance.account_statement_payment (payment_id)
    WHERE payment_id IS NOT NULL;


-- ============================================================================
-- INDICES PARA: account_statement_purchase_order
-- ============================================================================

-- Indice por estado de cuenta (requerido por el JIRA)
CREATE INDEX IF NOT EXISTS ix_account_statement_po_statement
    ON tenant_finance.account_statement_purchase_order (account_statement_uuid);

-- Indice por numero de OC
CREATE INDEX IF NOT EXISTS ix_account_statement_po_number
    ON tenant_finance.account_statement_purchase_order (order_number);


-- ============================================================================
-- INDICES PARA: account_statement_reception
-- ============================================================================

-- Indice por estado de cuenta (requerido por el JIRA)
CREATE INDEX IF NOT EXISTS ix_account_statement_reception_statement
    ON tenant_finance.account_statement_reception (account_statement_uuid);

-- Indice por numero de recepcion
CREATE INDEX IF NOT EXISTS ix_account_statement_reception_number
    ON tenant_finance.account_statement_reception (reception_number);

-- Indice por numero de OC (para relacionar con OC)
CREATE INDEX IF NOT EXISTS ix_account_statement_reception_order
    ON tenant_finance.account_statement_reception (order_number);


-- ============================================================================
-- FIN DEL SCRIPT DE CREACION DE INDICES
-- ============================================================================
