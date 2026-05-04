-- STM-1474 - Complementos de Pago - Consultas validacion BD Sodimac
-- tenant_fiscal.payments JOIN tenant_fiscal.addendum

-- 1. Distribucion vendors en complementos de pago
SELECT
    CAST(a.supplier_number AS TEXT) AS vendor,
    COUNT(DISTINCT p.payments_uuid)  AS complementos
FROM tenant_fiscal.payments p
JOIN tenant_fiscal.addendum a ON a.payments_uuid = p.payments_uuid
GROUP BY a.supplier_number
ORDER BY complementos DESC;
-- Resultado Sodimac: 1 → 4

-- 2. Total sin filtro (acceso total -1)
SELECT COUNT(*) FROM tenant_fiscal.payments;
-- Resultado Sodimac: 19

-- 3. Simular filtro vendor 1
SELECT COUNT(DISTINCT p.payments_uuid) AS total
FROM tenant_fiscal.payments p
JOIN tenant_fiscal.addendum a ON a.payments_uuid = p.payments_uuid
WHERE a.supplier_number = 1;
-- Esperado: 4

-- 4. Simular vendor sin complementos
SELECT COUNT(DISTINCT p.payments_uuid) AS total
FROM tenant_fiscal.payments p
JOIN tenant_fiscal.addendum a ON a.payments_uuid = p.payments_uuid
WHERE a.supplier_number = 11111;
-- Esperado: 0
