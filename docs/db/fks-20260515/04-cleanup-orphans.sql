BEGIN;

-- Backup de evidencia
CREATE TABLE tenant_fiscal.tax_orphan_backup_20260515 AS
  SELECT * FROM tenant_fiscal.tax t WHERE t.invoice_uuid NOT IN (SELECT invoice_uuid FROM tenant_fiscal.invoice);

CREATE TABLE tenant_fiscal.related_documents_orphan_backup_20260515 AS
  SELECT * FROM tenant_fiscal.related_documents rd WHERE rd.document_uuid NOT IN (SELECT invoice_uuid FROM tenant_fiscal.invoice);

CREATE TABLE tenant_finance.stamped_rebate_orphan_backup_20260515 AS
  SELECT * FROM tenant_finance.stamped_rebate sr WHERE sr.invoice_fiscal_uuid IS NOT NULL AND sr.invoice_fiscal_uuid NOT IN (SELECT fiscal_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid IS NOT NULL);

-- 1. Cascada tax: borrar hijos primero
DELETE FROM tenant_fiscal.tax_detail
WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax_orphan_backup_20260515);

DELETE FROM tenant_fiscal.tax_transfer
WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax_orphan_backup_20260515);

DELETE FROM tenant_fiscal.tax_withholding
WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax_orphan_backup_20260515);

-- 2. Borrar tax huérfanos
DELETE FROM tenant_fiscal.tax t
WHERE t.invoice_uuid NOT IN (SELECT invoice_uuid FROM tenant_fiscal.invoice);

-- 3. Cascada related_documents: borrar equivalence_dr primero
DELETE FROM tenant_fiscal.equivalence_dr
WHERE related_document_uuid IN (SELECT related_document_uuid FROM tenant_fiscal.related_documents_orphan_backup_20260515);

-- 4. Borrar related_documents huérfanos
DELETE FROM tenant_fiscal.related_documents rd
WHERE rd.document_uuid NOT IN (SELECT invoice_uuid FROM tenant_fiscal.invoice);

-- 5. SET NULL en stamped_rebate (preservar registros, solo romper vínculo)
UPDATE tenant_finance.stamped_rebate
SET invoice_fiscal_uuid = NULL
WHERE invoice_fiscal_uuid IS NOT NULL
  AND invoice_fiscal_uuid NOT IN (SELECT fiscal_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid IS NOT NULL);

-- Verificación final
\echo === Verificación post-limpieza ===
SELECT 'tax huérfanos restantes' AS check, COUNT(*) AS cnt FROM tenant_fiscal.tax t LEFT JOIN tenant_fiscal.invoice i ON t.invoice_uuid=i.invoice_uuid WHERE i.invoice_uuid IS NULL
UNION ALL SELECT 'related_documents huérfanos restantes', COUNT(*) FROM tenant_fiscal.related_documents rd LEFT JOIN tenant_fiscal.invoice i ON rd.document_uuid=i.invoice_uuid WHERE i.invoice_uuid IS NULL
UNION ALL SELECT 'stamped_rebate con fiscal_uuid huérfano', COUNT(*) FROM tenant_finance.stamped_rebate sr LEFT JOIN tenant_fiscal.invoice i ON sr.invoice_fiscal_uuid=i.fiscal_uuid WHERE sr.invoice_fiscal_uuid IS NOT NULL AND i.fiscal_uuid IS NULL;

COMMIT;
