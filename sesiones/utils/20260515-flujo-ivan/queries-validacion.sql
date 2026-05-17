-- ============================================================================
-- Queries de validacion BD UAT - flujo demo Pago + Descuento Comercial
-- Fecha: 2026-05-15
-- ============================================================================

-- 1. Verificar pago insertado
SELECT fiscal_payment_uuid, payment_number, company, document_number,
       reference_number, vendor_number, amount, currency, document_type,
       payment_date, status, created_at
FROM tenant_finance.fiscal_payments
WHERE payment_number = 'PAY-2026-SOD-001234'
ORDER BY created_at DESC;

-- 2. Verificar stamped_rebate insertado (prerequisito FK)
SELECT stamped_rebate_uuid, document_number, reference_number, status,
       invoice_fiscal_uuid, created_at
FROM tenant_finance.stamped_rebate
WHERE document_number = 'REB-2026-Q2-VOL-001';

-- 3. Verificar rebate insertado
SELECT rebate_uuid, document_number, reference_number, sap_document,
       vendor_number, amount, source, period_id, due_date, posting_date,
       status, created_at
FROM tenant_finance.rebate
WHERE document_number = 'REB-2026-Q2-VOL-001';

-- 4. Vista consolidada (lo que regresa GET /rebates con join a stamped_rebate)
SELECT r.rebate_uuid, r.document_number, r.vendor_number, r.amount AS rebate_amount,
       sr.stamped_rebate_uuid, sr.invoice_fiscal_uuid,
       r.created_at AS rebate_created, sr.created_at AS stamped_created
FROM tenant_finance.rebate r
LEFT JOIN tenant_finance.stamped_rebate sr ON sr.document_number = r.document_number
WHERE r.document_number = 'REB-2026-Q2-VOL-001';

-- 5. Cleanup (opcional, solo si quieres limpiar el demo)
-- DELETE FROM tenant_finance.rebate          WHERE document_number = 'REB-2026-Q2-VOL-001';
-- DELETE FROM tenant_finance.stamped_rebate  WHERE document_number = 'REB-2026-Q2-VOL-001';
-- DELETE FROM tenant_finance.fiscal_payments WHERE payment_number  = 'PAY-2026-SOD-001234';
