-- =============================================================================
-- STM-321: Three Way Match — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL, puerto 5434)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Configuración de seguridad: usuarios y sus atributos ATR001 (Proveedor)
-- -----------------------------------------------------------------------------
SELECT
    ud.user_data_id,
    ud.preferred_username,
    ud.sub,
    cd_type.key  AS tipo_atributo,
    cd_val.key   AS valor_atributo,   -- ej. '11111', '-1'
    cd_val.external_key               -- clave externa (si aplica)
FROM core_security.user_data ud
JOIN core_security.user_attribute ua
    ON ua.user_data_id = ud.user_data_id
JOIN shared_catalogs.catalog_detail cd_type
    ON cd_type.id = ua.catalog_detail_attribute_type_id
LEFT JOIN shared_catalogs.catalog_detail cd_val
    ON cd_val.id = ua.catalog_detail_attribute_value_id
WHERE cd_type.key = 'ATR001'   -- Solo atributo Proveedor
ORDER BY ud.preferred_username;

-- Resultado esperado:
-- USR_FERNANDO  → ATR001 = 11111
-- USR_JOSE      → ATR001 = 11111, ATR001 = 22222
-- zedlav.sd18   → ATR001 = -1  (acceso total)
-- USR_ANA       → (sin filas ATR001) → WRN7029


-- -----------------------------------------------------------------------------
-- 2. Distribución de proveedores en three_way_match
--    Permite saber qué vendors existen en la tabla para mapear con atributos
-- -----------------------------------------------------------------------------
SELECT
    vendor_number,
    COUNT(*) AS total_registros
FROM tenant_finance.three_way_match
GROUP BY vendor_number
ORDER BY vendor_number;

-- Resultado actual:
-- vendor 1      → 2 registros
-- vendor 1001   → 1 registro
-- vendor 1002   → 1 registro
-- vendor 1003   → 1 registro
-- vendor 100001 → 1 registro
-- vendor 100003 → 1 registro
-- NOTA: Los vendors 11111/22222 NO existen en esta tabla.
--       Los usuarios de prueba (USR_FERNANDO, USR_JOSE) configurados con
--       ATR001=11111/22222 devuelven 0 resultados en finanzas.
--       Para prueba completa: agregar usuario con ATR001=1001 en core_security.


-- -----------------------------------------------------------------------------
-- 3. Simular filtro backend: Escenario 1 — USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('11111');
-- Esperado: 0 (vendor 11111 no existe en finanzas — ver NOTA arriba)


-- -----------------------------------------------------------------------------
-- 4. Simular filtro backend: Escenario 2 — USR_JOSE (ATR001 = 11111, 22222)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('11111', '22222');
-- Esperado: 0 (misma razón)


-- -----------------------------------------------------------------------------
-- 5. Simular filtro backend: Escenario 3 — Ivan (ATR001 = -1 → sin filtro)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.three_way_match;
-- Esperado: 7 (todos los registros)


-- -----------------------------------------------------------------------------
-- 6. Ejemplo de filtro con vendor real (validar mecanismo CAST IN)
--    Equivale a un usuario con ATR001 = 1001
-- -----------------------------------------------------------------------------
SELECT
    three_way_match_uuid,
    vendor_number,
    purchase_order_number,
    invoice_folio,
    status,
    created_at
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('1001')
ORDER BY created_at DESC;
-- Esperado: 1 registro del proveedor 1001


-- -----------------------------------------------------------------------------
-- 7. Simular filtro multi-vendor con datos reales (vendor 1001 + 1002)
-- -----------------------------------------------------------------------------
SELECT vendor_number, COUNT(*) AS total
FROM tenant_finance.three_way_match
WHERE CAST(vendor_number AS TEXT) IN ('1001', '1002')
GROUP BY vendor_number
ORDER BY vendor_number;
-- Esperado: 1001→1, 1002→1 (total 2 registros)


-- -----------------------------------------------------------------------------
-- 8. Verificar que WRN7029 aplica a USR_ANA (tiene ATR002 pero no ATR001)
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
-- Esperado: ATR002 = TPR001 (tiene atributo tipo pero NO ATR001)
-- → El BFF inyecta x-user-vendors='' (vacío)
-- → Backend retorna HTTP 400 WRN7029
