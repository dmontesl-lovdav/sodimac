-- =============================================================================
-- STM-1461: Carta Porte (Shipping Guide) — Consultas de validación en BD
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
-- 2. Distribución de proveedores en shipping_guide
-- -----------------------------------------------------------------------------
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total_guias
FROM tenant_finance.shipping_guide
GROUP BY vendor_number
ORDER BY vendor_number;

-- Resultado:
-- 11111 → 2 guías
-- 22222 → 2 guías
-- 33333 → 1 guía
-- Total → 5


-- -----------------------------------------------------------------------------
-- 3. Simular filtro: USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.shipping_guide
WHERE vendor_number IN (11111);
-- Resultado: 2


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_JOSE (ATR001 = 11111, 22222 — OR lógico)
-- -----------------------------------------------------------------------------
SELECT
    CAST(vendor_number AS TEXT) AS vendor,
    COUNT(*) AS total
FROM tenant_finance.shipping_guide
WHERE vendor_number IN (11111, 22222)
GROUP BY vendor_number ORDER BY vendor_number;
-- Resultado: 11111→2, 22222→2 (total 4)


-- -----------------------------------------------------------------------------
-- 5. Simular filtro: Ivan (ATR001 = -1 → sin filtro)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_finance.shipping_guide;
-- Resultado: 5


-- -----------------------------------------------------------------------------
-- 6. Ver detalle con documentos
-- -----------------------------------------------------------------------------
SELECT
    sg.shipping_guide_id,
    sg.guide_number,
    CAST(sg.vendor_number AS TEXT) AS vendor,
    sg.status,
    COUNT(doc.shipping_guide_id) AS documentos
FROM tenant_finance.shipping_guide sg
LEFT JOIN tenant_finance.shipping_guide_document doc
    ON doc.shipping_guide_id = sg.shipping_guide_id
GROUP BY sg.shipping_guide_id, sg.guide_number, sg.vendor_number, sg.status
ORDER BY sg.vendor_number;


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
