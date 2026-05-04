-- STM-323 - Facturas - Consultas validacion BD Sodimac
-- tenant_fiscal.invoice JOIN tenant_fiscal.addendum

-- 1. Distribucion vendors en facturas
SELECT
    CAST(a.supplier_number AS TEXT) AS vendor,
    COUNT(DISTINCT i.invoice_uuid)   AS facturas
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
GROUP BY a.supplier_number
ORDER BY facturas DESC;
-- Resultado Sodimac: 11111=15 | 54321=14 | 44444=13 | 67890=13 | 55555=13 | 33333=12 | 12345=10 | 22222=9 | 1=1

-- 2. Total sin filtro
SELECT COUNT(*) FROM tenant_fiscal.invoice;
-- Resultado Sodimac: 121

-- 3. Facturas en rango 2025-01-01 / 2025-06-30 (rango usado en pruebas)
-- Nota: fechaRecepcion filtra por created_at en fiscal-api
SELECT COUNT(DISTINCT i.invoice_uuid) AS total
FROM tenant_fiscal.invoice i
WHERE i.created_at BETWEEN '2025-01-01' AND '2025-06-30 23:59:59';
-- Resultado Sodimac: 30

-- 4. Vendor 11111 en ese rango
SELECT COUNT(DISTINCT i.invoice_uuid) AS total
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
WHERE a.supplier_number = 11111
  AND i.created_at BETWEEN '2025-01-01' AND '2025-06-30 23:59:59';
-- Resultado Sodimac: 6

-- 5. Vendor 22222 en ese rango
SELECT COUNT(DISTINCT i.invoice_uuid) AS total
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
WHERE a.supplier_number = 22222
  AND i.created_at BETWEEN '2025-01-01' AND '2025-06-30 23:59:59';
-- Resultado Sodimac: 3

-- 6. OR logico 11111,22222 en ese rango
SELECT COUNT(DISTINCT i.invoice_uuid) AS total
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
WHERE a.supplier_number IN (11111, 22222)
  AND i.created_at BETWEEN '2025-01-01' AND '2025-06-30 23:59:59';
-- Resultado Sodimac: 9 (6+3, sin traslape)
