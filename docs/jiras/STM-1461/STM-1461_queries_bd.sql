-- =============================================================================
-- STM-1461: Carta Porte — Consultas de validación en BD
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
-- 2. Distribución de proveedores en shipping_guide
-- -----------------------------------------------------------------------------
SELECT
    vendor_number,
    COUNT(*) AS total_guias
FROM tenant_finance.shipping_guide
GROUP BY vendor_number
ORDER BY vendor_number;

-- Resultado actual:
-- vendor 1    → 2 guías
-- vendor 1001 → 1 guía
-- vendor 1002 → 1 guía
-- NOTA: Los vendors 11111/22222 NO existen en esta tabla.


-- -----------------------------------------------------------------------------
-- 3. Simular filtro: USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.shipping_guide
WHERE CAST(vendor_number AS TEXT) IN ('11111');
-- Esperado: 0 (vendor 11111 no existe en finanzas — ver NOTA)


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_JOSE (ATR001 = 11111, 22222)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.shipping_guide
WHERE CAST(vendor_number AS TEXT) IN ('11111', '22222');
-- Esperado: 0


-- -----------------------------------------------------------------------------
-- 5. Simular filtro: Ivan (ATR001 = -1 → sin filtro)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.shipping_guide;
-- Esperado: 4 (todas las guías)


-- -----------------------------------------------------------------------------
-- 6. Ejemplo filtro con vendor real (validar mecanismo TypeORM In)
--    Equivale a usuario con ATR001 = 1001
-- -----------------------------------------------------------------------------
SELECT
    shipping_guide_id,
    guide_number,
    vendor_number,
    status,
    delivery_date,
    created_at
FROM tenant_finance.shipping_guide
WHERE CAST(vendor_number AS TEXT) IN ('1001')
ORDER BY created_at DESC;
-- Esperado: 1 guía del proveedor 1001


-- -----------------------------------------------------------------------------
-- 7. Filtro multi-vendor con datos reales (1001 + 1002)
-- -----------------------------------------------------------------------------
SELECT vendor_number, COUNT(*) AS total
FROM tenant_finance.shipping_guide
WHERE CAST(vendor_number AS TEXT) IN ('1001', '1002')
GROUP BY vendor_number ORDER BY vendor_number;
-- Esperado: 1001→1, 1002→1 (total 2)


-- -----------------------------------------------------------------------------
-- 8. Ver detalle completo de una guía (para prueba individual por UUID)
-- -----------------------------------------------------------------------------
SELECT
    sg.shipping_guide_id,
    sg.guide_number,
    sg.vendor_number,
    sg.status,
    COUNT(doc.shipping_guide_id) AS documentos
FROM tenant_finance.shipping_guide sg
LEFT JOIN tenant_finance.shipping_guide_document doc
    ON doc.shipping_guide_id = sg.shipping_guide_id
GROUP BY sg.shipping_guide_id, sg.guide_number, sg.vendor_number, sg.status
ORDER BY sg.vendor_number;


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
