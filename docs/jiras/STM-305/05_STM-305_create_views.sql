-- ============================================================================
-- STM-305: Vistas de Consulta para Estado de Cuenta
-- Esquema: tenant_finance
-- Fecha: 2026-02-09
--
-- Vistas para facilitar la consulta del Estado de Cuenta:
-- 1. vw_account_statement              - EdC con descripcion de estatus
-- 2. vw_account_statement_current      - Ultima version publicada por proveedor/periodo
-- 3. vw_account_statement_summary      - EdC con totales calculados
-- 4. vw_account_statement_full         - EdC completo con todos los detalles
-- 5. vw_account_statement_balance      - Calculo del saldo (verificacion)
-- ============================================================================

SET search_path TO tenant_finance;

-- ============================================================================
-- 1. VISTA: vw_account_statement
-- Estado de cuenta con descripcion de estatus
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement AS
SELECT
    s.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    s.status,
    CASE s.status
        WHEN 1 THEN 'Generado'
        WHEN 2 THEN 'Publicado'
        WHEN 3 THEN 'Revisado'
        WHEN 4 THEN 'Rechazado'
        WHEN 5 THEN 'Reprocesado'
        ELSE 'Desconocido'
    END AS status_description,
    s.initial_balance,
    s.final_balance,
    s.process_date,
    s.review_date,
    s.issue_date,
    s.period_start_date,
    s.period_end_date,
    s.previous_statement_uuid,
    s.created_by,
    s.created_at,
    s.updated_by,
    s.updated_at
FROM tenant_finance.account_statement s;

COMMENT ON VIEW tenant_finance.vw_account_statement IS 'Vista de estados de cuenta con descripcion de estatus';


-- ============================================================================
-- 2. VISTA: vw_account_statement_current
-- Ultima version publicada por proveedor y periodo
-- Uso: Obtener el EdC vigente para mostrar al proveedor
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_current AS
SELECT DISTINCT ON (s.vendor_number, s.year, s.month)
    s.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    s.status,
    CASE s.status
        WHEN 1 THEN 'Generado'
        WHEN 2 THEN 'Publicado'
        WHEN 3 THEN 'Revisado'
        WHEN 4 THEN 'Rechazado'
        WHEN 5 THEN 'Reprocesado'
        ELSE 'Desconocido'
    END AS status_description,
    s.initial_balance,
    s.final_balance,
    s.process_date,
    s.review_date,
    s.issue_date,
    s.period_start_date,
    s.period_end_date,
    s.created_at
FROM tenant_finance.account_statement s
WHERE s.status = 2  -- Solo publicados
ORDER BY s.vendor_number, s.year, s.month, s.version DESC;

COMMENT ON VIEW tenant_finance.vw_account_statement_current IS 'Ultima version publicada de cada estado de cuenta por proveedor y periodo';


-- ============================================================================
-- 3. VISTA: vw_account_statement_summary
-- Estado de cuenta con totales de cada tipo de documento
-- Uso: Dashboard y listados con resumen
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_summary AS
SELECT
    s.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    s.status,
    CASE s.status
        WHEN 1 THEN 'Generado'
        WHEN 2 THEN 'Publicado'
        WHEN 3 THEN 'Revisado'
        WHEN 4 THEN 'Rechazado'
        WHEN 5 THEN 'Reprocesado'
        ELSE 'Desconocido'
    END AS status_description,
    s.initial_balance,
    s.final_balance,
    s.period_start_date,
    s.period_end_date,
    s.issue_date,
    -- Totales de facturas
    COALESCE(inv_pending.count, 0) AS pending_invoices_count,
    COALESCE(inv_pending.total, 0) AS pending_invoices_total,
    COALESCE(inv_paid.count, 0) AS paid_invoices_count,
    COALESCE(inv_paid.total, 0) AS paid_invoices_total,
    -- Totales de descuentos
    COALESCE(dis.count, 0) AS discounts_count,
    COALESCE(dis.total, 0) AS discounts_total,
    -- Totales de notas de credito
    COALESCE(cn.count, 0) AS credit_notes_count,
    COALESCE(cn.total, 0) AS credit_notes_total,
    -- Totales de pagos
    COALESCE(pay.count, 0) AS payments_count,
    COALESCE(pay.total, 0) AS payments_total,
    -- Totales informativos (OC y recepciones)
    COALESCE(po.count, 0) AS purchase_orders_count,
    COALESCE(po.total, 0) AS purchase_orders_total,
    COALESCE(rec.count, 0) AS receptions_count,
    COALESCE(rec.total, 0) AS receptions_total,
    s.created_at
FROM tenant_finance.account_statement s
-- Facturas pendientes
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_invoice
    WHERE invoice_type = 'PENDING'
    GROUP BY account_statement_uuid
) inv_pending ON s.account_statement_uuid = inv_pending.account_statement_uuid
-- Facturas pagadas
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_invoice
    WHERE invoice_type = 'PAID'
    GROUP BY account_statement_uuid
) inv_paid ON s.account_statement_uuid = inv_paid.account_statement_uuid
-- Descuentos
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_discount
    GROUP BY account_statement_uuid
) dis ON s.account_statement_uuid = dis.account_statement_uuid
-- Notas de credito
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_credit_note
    GROUP BY account_statement_uuid
) cn ON s.account_statement_uuid = cn.account_statement_uuid
-- Pagos
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_payment
    GROUP BY account_statement_uuid
) pay ON s.account_statement_uuid = pay.account_statement_uuid
-- Ordenes de compra
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_purchase_order
    GROUP BY account_statement_uuid
) po ON s.account_statement_uuid = po.account_statement_uuid
-- Recepciones
LEFT JOIN (
    SELECT account_statement_uuid, COUNT(*) as count, SUM(base_amount) as total
    FROM tenant_finance.account_statement_reception
    GROUP BY account_statement_uuid
) rec ON s.account_statement_uuid = rec.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_summary IS 'Estado de cuenta con totales de cada tipo de documento';


-- ============================================================================
-- 4. VISTA: vw_account_statement_balance
-- Verificacion del calculo del saldo
-- Uso: Validar que final_balance = initial_balance + facturas - (pagos + nc + desc)
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_balance AS
SELECT
    s.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    s.initial_balance,
    s.final_balance AS final_balance_stored,
    -- Calculo del saldo
    COALESCE(inv.total, 0) AS total_invoices,
    COALESCE(pay.total, 0) AS total_payments,
    COALESCE(cn.total, 0) AS total_credit_notes,
    COALESCE(dis.total, 0) AS total_discounts,
    -- Formula: SaldoFinal = SaldoInicial + Facturas - (Pagos + NC + Descuentos)
    (s.initial_balance
        + COALESCE(inv.total, 0)
        - COALESCE(pay.total, 0)
        - COALESCE(cn.total, 0)
        - COALESCE(dis.total, 0)
    ) AS final_balance_calculated,
    -- Diferencia (debe ser 0 si el calculo es correcto)
    (s.final_balance - (
        s.initial_balance
        + COALESCE(inv.total, 0)
        - COALESCE(pay.total, 0)
        - COALESCE(cn.total, 0)
        - COALESCE(dis.total, 0)
    )) AS difference,
    -- Flag de validacion
    CASE
        WHEN s.final_balance = (
            s.initial_balance
            + COALESCE(inv.total, 0)
            - COALESCE(pay.total, 0)
            - COALESCE(cn.total, 0)
            - COALESCE(dis.total, 0)
        ) THEN TRUE
        ELSE FALSE
    END AS is_balanced
FROM tenant_finance.account_statement s
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total
    FROM tenant_finance.account_statement_invoice
    WHERE invoice_type = 'PENDING'  -- Solo pendientes suman al saldo
    GROUP BY account_statement_uuid
) inv ON s.account_statement_uuid = inv.account_statement_uuid
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total
    FROM tenant_finance.account_statement_payment
    GROUP BY account_statement_uuid
) pay ON s.account_statement_uuid = pay.account_statement_uuid
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total
    FROM tenant_finance.account_statement_credit_note
    GROUP BY account_statement_uuid
) cn ON s.account_statement_uuid = cn.account_statement_uuid
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total
    FROM tenant_finance.account_statement_discount
    GROUP BY account_statement_uuid
) dis ON s.account_statement_uuid = dis.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_balance IS 'Vista para verificar el calculo del saldo final';


-- ============================================================================
-- 5. VISTA: vw_account_statement_invoices
-- Facturas con datos del estado de cuenta padre
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_invoices AS
SELECT
    i.account_statement_invoice_uuid,
    i.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    i.invoice_type,
    i.series,
    i.folio,
    i.uuid AS cfdi_uuid,
    i.stamp_date,
    i.accounting_date,
    i.payment_date,
    i.currency,
    i.amount,
    i.exchange_rate,
    i.base_currency,
    i.base_amount,
    i.invoice_status,
    i.payment_id,
    i.created_at
FROM tenant_finance.account_statement_invoice i
JOIN tenant_finance.account_statement s ON i.account_statement_uuid = s.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_invoices IS 'Facturas con informacion del estado de cuenta';


-- ============================================================================
-- 6. VISTA: vw_account_statement_payments
-- Pagos con datos del estado de cuenta padre
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_payments AS
SELECT
    p.account_statement_payment_uuid,
    p.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    p.payment_id,
    p.document_number,
    p.reference_number,
    p.payment_date,
    p.currency,
    p.amount,
    p.exchange_rate,
    p.base_currency,
    p.base_amount,
    p.status,
    p.created_at
FROM tenant_finance.account_statement_payment p
JOIN tenant_finance.account_statement s ON p.account_statement_uuid = s.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_payments IS 'Pagos con informacion del estado de cuenta';


-- ============================================================================
-- 7. VISTA: vw_account_statement_credit_notes
-- Notas de credito con datos del estado de cuenta padre
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_credit_notes AS
SELECT
    cn.account_statement_credit_note_uuid,
    cn.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    cn.document_number,
    cn.series,
    cn.folio,
    cn.uuid AS cfdi_uuid,
    cn.issue_date,
    cn.accounting_date,
    cn.currency,
    cn.amount,
    cn.exchange_rate,
    cn.base_currency,
    cn.base_amount,
    cn.status,
    cn.created_at
FROM tenant_finance.account_statement_credit_note cn
JOIN tenant_finance.account_statement s ON cn.account_statement_uuid = s.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_credit_notes IS 'Notas de credito con informacion del estado de cuenta';


-- ============================================================================
-- 8. VISTA: vw_account_statement_discounts
-- Descuentos con datos del estado de cuenta padre
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_discounts AS
SELECT
    d.account_statement_discount_uuid,
    d.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    d.document_number,
    d.reference_number,
    d.series,
    d.folio,
    d.uuid AS cfdi_uuid,
    d.discount_date,
    d.accounting_date,
    d.currency,
    d.amount,
    d.exchange_rate,
    d.base_currency,
    d.base_amount,
    d.status,
    d.created_at
FROM tenant_finance.account_statement_discount d
JOIN tenant_finance.account_statement s ON d.account_statement_uuid = s.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_discounts IS 'Descuentos con informacion del estado de cuenta';


-- ============================================================================
-- 9. VISTA: vw_account_statement_purchase_orders
-- Ordenes de compra con datos del estado de cuenta padre
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_purchase_orders AS
SELECT
    po.account_statement_purchase_order_uuid,
    po.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    po.order_number,
    po.document_date,
    po.due_date,
    po.currency,
    po.amount,
    po.exchange_rate,
    po.base_currency,
    po.base_amount,
    po.status,
    po.source_id,
    po.created_at
FROM tenant_finance.account_statement_purchase_order po
JOIN tenant_finance.account_statement s ON po.account_statement_uuid = s.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_purchase_orders IS 'Ordenes de compra con informacion del estado de cuenta';


-- ============================================================================
-- 10. VISTA: vw_account_statement_receptions
-- Recepciones con datos del estado de cuenta padre
-- ============================================================================

CREATE OR REPLACE VIEW tenant_finance.vw_account_statement_receptions AS
SELECT
    r.account_statement_reception_uuid,
    r.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    r.reception_number,
    r.order_number,
    r.document_date,
    r.reception_date,
    r.due_date,
    r.currency,
    r.amount,
    r.exchange_rate,
    r.base_currency,
    r.base_amount,
    r.status,
    r.source_id,
    r.created_at
FROM tenant_finance.account_statement_reception r
JOIN tenant_finance.account_statement s ON r.account_statement_uuid = s.account_statement_uuid;

COMMENT ON VIEW tenant_finance.vw_account_statement_receptions IS 'Recepciones con informacion del estado de cuenta';


-- ============================================================================
-- FIN DEL SCRIPT DE VISTAS
-- ============================================================================
