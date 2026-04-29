-- =============================================================================
-- STM-1525: Catálogo Proveedor — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL, puerto 5434)
-- Nota: util-api filtra shared_catalogs.supplier por supplier_type_id
--       resolviendo ATR002 keys → catalog_detail.external_key
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Configuración de seguridad: usuarios y atributos ATR002 (TipoProveedor)
-- -----------------------------------------------------------------------------
SELECT
    ud.user_data_id,
    ud.preferred_username,
    cd_type.key  AS tipo_atributo,
    cd_val.key   AS valor_atributo,
    cd_val.external_key AS supplier_type_id_externo
FROM core_security.user_data ud
JOIN core_security.user_attribute ua
    ON ua.user_data_id = ud.user_data_id
JOIN shared_catalogs.catalog_detail cd_type
    ON cd_type.id = ua.catalog_detail_attribute_type_id
LEFT JOIN shared_catalogs.catalog_detail cd_val
    ON cd_val.id = ua.catalog_detail_attribute_value_id
WHERE cd_type.key = 'ATR002'
ORDER BY ud.preferred_username;

-- Resultado esperado:
-- USR_ANA  → ATR002 = TPR001, external_key = '1' → supplier_type_id = 1


-- -----------------------------------------------------------------------------
-- 2. Mapeo ATR002 → supplier_type_id (via catalog_detail.external_key)
-- -----------------------------------------------------------------------------
SELECT
    cd.key AS atr002_value,
    cd.external_key AS supplier_type_id
FROM shared_catalogs.catalog_detail cd
WHERE cd.key IN ('TPR001', 'TPR002', 'TPR003', 'TPR004')
AND cd.external_key IS NOT NULL
ORDER BY cd.key;

-- Resultado esperado:
-- TPR001 → 1
-- TPR002 → 2
-- TPR003 → 3
-- TPR004 → 4


-- -----------------------------------------------------------------------------
-- 3. Distribución de proveedores por tipo
-- -----------------------------------------------------------------------------
SELECT
    s.supplier_type_id,
    COUNT(*) AS total_proveedores
FROM shared_catalogs.supplier s
GROUP BY s.supplier_type_id
ORDER BY s.supplier_type_id;

-- Resultado actual:
-- tipo 1 → 11 proveedores
-- tipo 2 →  3 proveedores
-- tipo 3 →  4 proveedores
-- tipo 4 →  4 proveedores
-- Total:   22 proveedores


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_ANA (ATR002 = TPR001 → supplier_type_id = 1)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM shared_catalogs.supplier s
WHERE s.supplier_type_id IN (
    SELECT CAST(cd.external_key AS INTEGER)
    FROM shared_catalogs.catalog_detail cd
    WHERE cd.key IN ('TPR001')
    AND cd.external_key IS NOT NULL
);
-- Esperado: 11 proveedores de tipo 1


-- -----------------------------------------------------------------------------
-- 5. Simular filtro multi-tipo: TPR001 + TPR002
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM shared_catalogs.supplier s
WHERE s.supplier_type_id IN (
    SELECT CAST(cd.external_key AS INTEGER)
    FROM shared_catalogs.catalog_detail cd
    WHERE cd.key IN ('TPR001', 'TPR002')
    AND cd.external_key IS NOT NULL
);
-- Esperado: 14 proveedores (11+3)


-- -----------------------------------------------------------------------------
-- 6. Acceso total (Ivan, ATR002=-1 → sin filtro)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM shared_catalogs.supplier;
-- Esperado: 22 proveedores


-- -----------------------------------------------------------------------------
-- 7. Ver proveedores filtrados con detalle (tipo 1)
-- -----------------------------------------------------------------------------
SELECT
    s.id,
    s.supplier_number,
    s.rfc,
    s.business_name,
    s.supplier_type_id,
    s.status
FROM shared_catalogs.supplier s
WHERE s.supplier_type_id = 1
ORDER BY s.supplier_number;


-- -----------------------------------------------------------------------------
-- 8. Ver proveedores filtrados con detalle (tipos 1 y 2)
-- -----------------------------------------------------------------------------
SELECT
    s.supplier_number,
    s.business_name,
    s.supplier_type_id,
    s.status
FROM shared_catalogs.supplier s
WHERE s.supplier_type_id IN (1, 2)
ORDER BY s.supplier_type_id, s.supplier_number;


-- -----------------------------------------------------------------------------
-- 9. Verificar que el filtro WRN7029 aplica a usuarios sin ATR002
--    (buscar usuarios sin ningún atributo ATR002)
-- -----------------------------------------------------------------------------
SELECT
    ud.preferred_username
FROM core_security.user_data ud
WHERE NOT EXISTS (
    SELECT 1 FROM core_security.user_attribute ua
    JOIN shared_catalogs.catalog_detail cd ON cd.id = ua.catalog_detail_attribute_type_id
    WHERE ua.user_data_id = ud.user_data_id
    AND cd.key = 'ATR002'
)
AND ud.status = 1;
-- Estos usuarios recibirán x-user-types='' del BFF → HTTP 400 WRN7029
