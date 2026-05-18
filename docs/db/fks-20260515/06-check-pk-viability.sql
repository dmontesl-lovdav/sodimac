-- ============================================================================
-- Verifica que las columnas candidatas a PK NO tengan NULLs ni duplicados.
-- Read-only. Si todos los conteos = 0, es seguro correr 07-add-pks.sql.
-- ============================================================================

SELECT 'tenant_fiscal.tax.tax_uuid' AS columna,
       COUNT(*) FILTER (WHERE tax_uuid IS NULL) AS nulls,
       (SELECT COUNT(*) FROM (SELECT tax_uuid FROM tenant_fiscal.tax GROUP BY tax_uuid HAVING COUNT(*)>1) x) AS duplicados,
       COUNT(*) AS total_rows
FROM tenant_fiscal.tax
UNION ALL
SELECT 'tenant_finance.account_statement.account_statement_uuid',
       COUNT(*) FILTER (WHERE account_statement_uuid IS NULL),
       (SELECT COUNT(*) FROM (SELECT account_statement_uuid FROM tenant_finance.account_statement GROUP BY account_statement_uuid HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.account_statement
UNION ALL
SELECT 'tenant_finance.pac_catalog.pac_id',
       COUNT(*) FILTER (WHERE pac_id IS NULL),
       (SELECT COUNT(*) FROM (SELECT pac_id FROM tenant_finance.pac_catalog GROUP BY pac_id HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.pac_catalog
UNION ALL
SELECT 'tenant_finance.payment_header.payment_header_uuid',
       COUNT(*) FILTER (WHERE payment_header_uuid IS NULL),
       (SELECT COUNT(*) FROM (SELECT payment_header_uuid FROM tenant_finance.payment_header GROUP BY payment_header_uuid HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.payment_header
UNION ALL
SELECT 'tenant_finance.purchase_order.purchase_order_uuid',
       COUNT(*) FILTER (WHERE purchase_order_uuid IS NULL),
       (SELECT COUNT(*) FROM (SELECT purchase_order_uuid FROM tenant_finance.purchase_order GROUP BY purchase_order_uuid HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.purchase_order
UNION ALL
SELECT 'tenant_finance.reception.reception_id',
       COUNT(*) FILTER (WHERE reception_id IS NULL),
       (SELECT COUNT(*) FROM (SELECT reception_id FROM tenant_finance.reception GROUP BY reception_id HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.reception
UNION ALL
SELECT 'tenant_finance.shipping_guide.shipping_guide_id',
       COUNT(*) FILTER (WHERE shipping_guide_id IS NULL),
       (SELECT COUNT(*) FROM (SELECT shipping_guide_id FROM tenant_finance.shipping_guide GROUP BY shipping_guide_id HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.shipping_guide
UNION ALL
SELECT 'tenant_finance.twm_ejecucion.id',
       COUNT(*) FILTER (WHERE id IS NULL),
       (SELECT COUNT(*) FROM (SELECT id FROM tenant_finance.twm_ejecucion GROUP BY id HAVING COUNT(*)>1) x),
       COUNT(*)
FROM tenant_finance.twm_ejecucion
ORDER BY columna;
