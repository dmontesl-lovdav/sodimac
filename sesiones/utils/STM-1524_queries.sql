-- =============================================================================
-- STM-1524: Estado de Cuenta (Account Statement) — Consultas de validación en BD Sodimac
-- DB: b2b_portal (PostgreSQL)
-- =============================================================================

-- 1. Ver vendors disponibles en account_statement (año 2026+)
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    year,
    COUNT(*) AS total_registros
FROM tenant_finance.account_statement
WHERE year >= 2026
GROUP BY vendor_number, year
ORDER BY year DESC, total_registros DESC;
-- Resultado Sodimac:
-- (actualizar con resultado real)
-- NOTA: year minimo = 2026 (validacion en schema)

-- 2. Total sin filtro año 2026 (acceso -1)
SELECT COUNT(*) AS total
FROM tenant_finance.account_statement
WHERE year = 2026;
-- Resultado Sodimac:

-- 3. Simular filtro por vendor específico (actualizar VENDOR_REAL)
SELECT COUNT(*) AS total
FROM tenant_finance.account_statement
WHERE year = 2026
  AND CAST(vendor_number AS TEXT) IN ('VENDOR_REAL');
-- Resultado Sodimac:

-- 4. Simular filtro multi-vendor (actualizar VENDOR1, VENDOR2)
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total
FROM tenant_finance.account_statement
WHERE year = 2026
  AND CAST(vendor_number AS TEXT) IN ('VENDOR1', 'VENDOR2')
GROUP BY vendor_number
ORDER BY vendor_number;
-- Resultado Sodimac:

-- 5. Ver UUIDs disponibles para probar getById (escenario 5)
SELECT account_statement_uuid, vendor_number, year, month, status
FROM tenant_finance.account_statement
WHERE year = 2026
ORDER BY vendor_number, month
LIMIT 10;
-- Resultado Sodimac:
