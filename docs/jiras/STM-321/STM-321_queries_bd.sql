-- =============================================================================
-- STM-321: Three Way Match — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL)
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
-- 2. Distribución de proveedores en three_way_match
-- -----------------------------------------------------------------------------
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total_registros
FROM tenant_finance.three_way_match
GROUP BY vendor_number
ORDER BY vendor_number;

-- Resultado:
-- 11111 → 6 registros
-- 22222 → 4 registros
-- 33333 → 2 registros
-- Total → 12


-- -----------------------------------------------------------------------------
-- 3. Simular filtro: USR_FERNANDO (ATR001 = 11111), rango 2025-01-01 a 2025-06-30
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('11111')
  AND reception_date BETWEEN '2025-01-01' AND '2025-06-30';
-- Resultado: 2


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: Ivan (ATR001 = -1 → sin filtro, todos los registros del rango)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.three_way_match
WHERE reception_date BETWEEN '2025-01-01' AND '2025-06-30';
-- Resultado: 6


-- -----------------------------------------------------------------------------
-- 5. Verificar mecanismo CAST TEXT IN (replica filtro del backend TypeORM)
-- -----------------------------------------------------------------------------
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('11111', '22222')
GROUP BY vendor_number
ORDER BY vendor_number;
-- Resultado: 11111→6, 22222→4 (total 10)


-- -----------------------------------------------------------------------------
-- 6. Verificar USR_ANA → WRN7029 (tiene ATR002 pero NO ATR001)
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
-- Esperado: ATR002 = TPR001 → sin ATR001 → x-user-vendors vacío → WRN7029
