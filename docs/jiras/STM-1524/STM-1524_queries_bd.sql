-- =============================================================================
-- STM-1524: Estado de Cuenta — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL, puerto 5434)
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

-- Resultado esperado:
-- USR_FERNANDO  → ATR001 = 11111
-- USR_JOSE      → ATR001 = 11111, 22222
-- zedlav.sd18   → ATR001 = -1  (acceso total)
-- USR_ANA       → (sin ATR001) → WRN7029


-- -----------------------------------------------------------------------------
-- 2. Distribución de proveedores en account_statement
-- -----------------------------------------------------------------------------
SELECT
    vendor_number,
    year,
    COUNT(*) AS total_estados
FROM tenant_finance.account_statement
GROUP BY vendor_number, year
ORDER BY vendor_number, year;

-- Resultado actual:
-- 1001 → 1 estado (2026)
-- 1002 → 1 estado (2026)
-- 1003 → 2 estados (2026)
-- NOTA: Los vendors 11111/22222 NO existen en esta tabla.


-- -----------------------------------------------------------------------------
-- 3. Simular filtro: USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.account_statement
WHERE CAST(vendor_number AS TEXT) IN ('11111');
-- Esperado: 0 (vendor 11111 no existe en finanzas — ver NOTA)


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_JOSE (ATR001 = 11111, 22222)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.account_statement
WHERE CAST(vendor_number AS TEXT) IN ('11111', '22222');
-- Esperado: 0


-- -----------------------------------------------------------------------------
-- 5. Simular filtro: Ivan (ATR001 = -1 → sin filtro, todos los estados)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.account_statement;
-- Esperado: 4 (todos los registros)


-- -----------------------------------------------------------------------------
-- 6. Ejemplo filtro con vendor real (validar mecanismo CAST IN)
--    Equivale a usuario con ATR001 = 1001
-- -----------------------------------------------------------------------------
SELECT
    account_statement_uuid,
    vendor_number,
    year,
    month,
    status,
    initial_balance,
    final_balance
FROM tenant_finance.account_statement
WHERE CAST(vendor_number AS TEXT) IN ('1001')
ORDER BY year, month;
-- Esperado: 1 registro del proveedor 1001


-- -----------------------------------------------------------------------------
-- 7. Filtro multi-vendor con datos reales (1001 + 1002)
-- -----------------------------------------------------------------------------
SELECT vendor_number, COUNT(*) AS total
FROM tenant_finance.account_statement
WHERE CAST(vendor_number AS TEXT) IN ('1001', '1002')
GROUP BY vendor_number ORDER BY vendor_number;
-- Esperado: 1001→1, 1002→1 (total 2)


-- -----------------------------------------------------------------------------
-- 8. Verificar que el filtro aplica también en consulta por UUID (getById)
--    Si vendor_number del UUID no está en allowedVendors → sin resultados (404)
-- -----------------------------------------------------------------------------
-- Primero obtener un UUID de ejemplo:
SELECT account_statement_uuid, vendor_number
FROM tenant_finance.account_statement
ORDER BY vendor_number LIMIT 3;

-- Simular consulta getById con filtro de vendor 1001:
-- (Reemplazar el UUID con uno real del query anterior)
-- SELECT * FROM tenant_finance.account_statement
-- WHERE account_statement_uuid = '<UUID>'
-- AND CAST(vendor_number AS TEXT) IN ('1001');


-- -----------------------------------------------------------------------------
-- 9. Verificar USR_ANA → WRN7029 (tiene ATR002 pero NO ATR001)
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
