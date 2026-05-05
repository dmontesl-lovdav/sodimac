-- =============================================================================
-- STM-321: Three Way Match — Consultas de validación en BD Sodimac
-- DB: b2b_portal (PostgreSQL)
-- =============================================================================

-- 1. Ver vendors disponibles en three_way_match
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total_registros
FROM tenant_finance.three_way_match
GROUP BY vendor_number
ORDER BY total_registros DESC;
-- Resultado Sodimac:
-- (actualizar con resultado real)

-- 2. Total sin filtro (acceso -1)
SELECT COUNT(*) AS total FROM tenant_finance.three_way_match;
-- Resultado Sodimac:

-- 3. Simular filtro por vendor específico (actualizar VENDOR_REAL)
SELECT COUNT(*) AS total
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('VENDOR_REAL');
-- Resultado Sodimac:

-- 4. Simular filtro multi-vendor (actualizar VENDOR1, VENDOR2)
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('VENDOR1', 'VENDOR2')
GROUP BY vendor_number
ORDER BY vendor_number;
-- Resultado Sodimac:

-- 5. Verificar CAST TEXT IN funciona en BD de Sodimac
SELECT COUNT(*) AS total
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('VENDOR_REAL');
-- Mismo resultado que query 3 = mecanismo correcto
