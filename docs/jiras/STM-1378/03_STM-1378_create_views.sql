-- ============================================================================
-- STM-1378: Vistas de Consulta para Three Way Match
-- Esquema: tenant_finance
-- Fecha: 2026-02-11
--
-- Vistas:
-- 1. vw_three_way_match         - Vista basica con descripcion de estatus
-- 2. vw_three_way_match_summary - Totales por proveedor
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- 1. VISTA: vw_three_way_match
-- Vista basica con descripcion de estatus
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_three_way_match AS
SELECT
    twm.three_way_match_uuid,
    twm.vendor_number,
    twm.purchase_order_number,
    twm.purchase_order_date,
    twm.purchase_order_amount,
    twm.reception_number,
    twm.reception_date,
    twm.reception_amount,
    twm.invoice_series,
    twm.invoice_folio,
    twm.invoice_uuid,
    twm.invoice_stamp_date,
    twm.invoice_amount,
    twm.credit_note_number,
    twm.credit_note_amount,
    twm.document_number,
    twm.sap_document,
    twm.accounting_date,
    twm.accounting_amount,
    twm.payment_reference,
    twm.payment_date,
    twm.payment_amount,
    twm.currency,
    twm.exchange_rate,
    twm.status,
    CASE twm.status
        WHEN 1 THEN 'Pendiente'
        WHEN 2 THEN 'Parcial'
        WHEN 3 THEN 'Conciliado'
        WHEN 4 THEN 'Discrepancia'
        WHEN 5 THEN 'Cerrado'
        ELSE 'Desconocido'
    END AS status_description,
    twm.created_by,
    twm.created_at,
    twm.updated_by,
    twm.updated_at
FROM tenant_finance.three_way_match twm;

COMMENT ON VIEW tenant_finance.vw_three_way_match IS 'Vista de Three Way Match con descripcion de estatus';


-- ============================================================================
-- 2. VISTA: vw_three_way_match_summary
-- Resumen de totales por proveedor
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_three_way_match_summary AS
SELECT
    twm.vendor_number,
    COUNT(*) AS total_records,
    -- Conteos por estatus
    SUM(CASE WHEN twm.status = 1 THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN twm.status = 2 THEN 1 ELSE 0 END) AS partial_count,
    SUM(CASE WHEN twm.status = 3 THEN 1 ELSE 0 END) AS reconciled_count,
    SUM(CASE WHEN twm.status = 4 THEN 1 ELSE 0 END) AS discrepancy_count,
    SUM(CASE WHEN twm.status = 5 THEN 1 ELSE 0 END) AS closed_count,
    -- Totales de montos
    COALESCE(SUM(twm.purchase_order_amount), 0) AS total_po_amount,
    COALESCE(SUM(twm.reception_amount), 0) AS total_reception_amount,
    COALESCE(SUM(twm.invoice_amount), 0) AS total_invoice_amount,
    COALESCE(SUM(twm.credit_note_amount), 0) AS total_credit_note_amount,
    COALESCE(SUM(twm.payment_amount), 0) AS total_payment_amount,
    -- Fechas
    MIN(twm.created_at) AS first_record_date,
    MAX(twm.created_at) AS last_record_date
FROM tenant_finance.three_way_match twm
GROUP BY twm.vendor_number;

COMMENT ON VIEW tenant_finance.vw_three_way_match_summary IS 'Resumen de Three Way Match por proveedor con totales';


-- ============================================================================
-- FIN DEL SCRIPT DE VISTAS
-- ============================================================================
