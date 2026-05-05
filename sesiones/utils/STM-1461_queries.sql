-- =============================================================================
-- STM-1461: Guías Carta Porte (Shipping Guide) — Consultas de validación en BD Sodimac
-- DB: b2b_portal (PostgreSQL)
-- =============================================================================

-- 1. Ver vendors disponibles en shipping_guide
SELECT
    vendor_number,
    COUNT(*) AS total_guias
FROM tenant_finance.shipping_guide
GROUP BY vendor_number
ORDER BY total_guias DESC;
-- Resultado Sodimac:
-- (actualizar con resultado real)

-- 2. Total sin filtro (acceso -1)
SELECT COUNT(*) AS total FROM tenant_finance.shipping_guide;
-- Resultado Sodimac:

-- 3. Simular filtro TypeORM In() por vendor_number (actualizar VENDOR_REAL)
SELECT COUNT(*) AS total
FROM tenant_finance.shipping_guide
WHERE vendor_number = VENDOR_REAL;
-- Resultado Sodimac:

-- 4. Simular filtro multi-vendor (actualizar VENDOR1, VENDOR2)
SELECT
    vendor_number,
    COUNT(*) AS total
FROM tenant_finance.shipping_guide
WHERE vendor_number IN (VENDOR1, VENDOR2)
GROUP BY vendor_number
ORDER BY vendor_number;
-- Resultado Sodimac:
