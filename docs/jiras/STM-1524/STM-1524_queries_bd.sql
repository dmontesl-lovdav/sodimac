-- =============================================================================
-- STM-1524: Estado de Cuenta (Account Statement) — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL)
-- year mínimo = 2026 (validación en schema)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Configuración de seguridad: usuarios y atributos ATR001 (Proveedor)
-- -----------------------------------------------------------------------------
SELECT
    ud.user_data_id,
    ud.preferred_username,
    cd_type.key  AS tipo_atributo,
    cd_val.key   AS valor_atributo
FROM core_security.user_data ud
JOIN core_security.user_attribute ua
    ON ua.user_data_id = ud.user_data_id
JOIN shared_catalogs.catalog_detail cd_type
    ON cd_type.id = ua.catalog_detail_attribute_type_id
LEFT JOIN shared_catalogs.catalog_detail cd_val
    ON cd_val.id = ua.catalog_detail_attribute_value_id
WHERE cd_type.key = 'ATR001'
ORDER BY ud.preferred_username;

-- Resultado:
-- USR_FERNANDO  → ATR001 = 11111
-- USR_JOSE      → ATR001 = 11111, 22222
-- zedlav.sd18   → ATR001 = -1  (acceso total)
-- USR_ANA       → (sin ATR001) → WRN7029


-- -----------------------------------------------------------------------------
-- 2. Distribución de proveedores en account_statement año 2026
-- -----------------------------------------------------------------------------
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    year,
    COUNT(*) AS total_estados
FROM tenant_finance.account_statement
WHERE year = 2026
GROUP BY vendor_number, year
ORDER BY vendor_number;

-- Resultado:
-- 11111 → 3 estados (meses 1, 2, 3)
-- 22222 → 2 estados (meses 1, 2)
-- 33333 → 1 estado  (mes 1)
-- Total → 6


-- -----------------------------------------------------------------------------
-- 3. Simular filtro: USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.account_statement
WHERE year = 2026
  AND CAST(vendor_number AS TEXT) IN ('11111');
-- Resultado: 3


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_JOSE (ATR001 = 11111, 22222 — OR lógico)
-- -----------------------------------------------------------------------------
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total
FROM tenant_finance.account_statement
WHERE year = 2026
  AND CAST(vendor_number AS TEXT) IN ('11111', '22222')
GROUP BY vendor_number ORDER BY vendor_number;
-- Resultado: 11111→3, 22222→2 (total 5)


-- -----------------------------------------------------------------------------
-- 5. Simular filtro: Ivan (ATR001 = -1 → sin filtro)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.account_statement
WHERE year = 2026;
-- Resultado: 6


-- -----------------------------------------------------------------------------
-- 6. Obtener UUIDs para prueba de getById con filtro (escenario 5)
-- -----------------------------------------------------------------------------
SELECT account_statement_uuid, CAST(vendor_number AS TEXT) AS vendor, year, month
FROM tenant_finance.account_statement
WHERE year = 2026
ORDER BY vendor_number, month;
-- Usar UUID de vendor 11111 con x-user-vendors=11111 → 200
-- Usar UUID de vendor 33333 con x-user-vendors=11111 → 404


-- -----------------------------------------------------------------------------
-- 7. Verificar USR_ANA → WRN7029
-- -----------------------------------------------------------------------------
SELECT
    ud.preferred_username,
    cd_type.key AS tipo_atributo,
    cd_val.key  AS valor_atributo
FROM core_security.user_data ud
JOIN core_security.user_attribute ua ON ua.user_data_id = ud.user_data_id
JOIN shared_catalogs.catalog_detail cd_type ON cd_type.id = ua.catalog_detail_attribute_type_id
LEFT JOIN shared_catalogs.catalog_detail cd_val ON cd_val.id = ua.catalog_detail_attribute_value_id
WHERE ud.preferred_username = 'USR_ANA';
-- Esperado: ATR002 = TPR001 → sin ATR001 → WRN7029
