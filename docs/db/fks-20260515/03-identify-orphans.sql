\echo === tax huérfanos ===
SELECT t.tax_uuid, t.invoice_uuid AS missing_invoice, t.created_at
FROM tenant_fiscal.tax t LEFT JOIN tenant_fiscal.invoice i ON t.invoice_uuid=i.invoice_uuid
WHERE t.invoice_uuid IS NOT NULL AND i.invoice_uuid IS NULL;

\echo === related_documents huérfanos (sin invoice destino) ===
SELECT rd.related_document_uuid, rd.payment_uuid, rd.document_uuid AS missing_invoice, rd.amount_paid
FROM tenant_fiscal.related_documents rd LEFT JOIN tenant_fiscal.invoice i ON rd.document_uuid=i.invoice_uuid
WHERE rd.document_uuid IS NOT NULL AND i.invoice_uuid IS NULL;

\echo === stamped_rebate con invoice_fiscal_uuid huérfano ===
SELECT sr.stamped_rebate_uuid, sr.document_number, sr.invoice_fiscal_uuid AS missing_fiscal_uuid, sr.created_at
FROM tenant_finance.stamped_rebate sr LEFT JOIN tenant_fiscal.invoice i ON sr.invoice_fiscal_uuid=i.fiscal_uuid
WHERE sr.invoice_fiscal_uuid IS NOT NULL AND i.fiscal_uuid IS NULL;
