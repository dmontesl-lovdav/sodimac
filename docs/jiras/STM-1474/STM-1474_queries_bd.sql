-- =============================================================================
-- STM-1474: Complementos de Pago — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL, puerto 5434)
-- Nota: fiscal-api filtra por addendum.supplier_number JOIN payments.payments_uuid
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
-- 2. Distribución de proveedores en addendum (solo filas con payments_uuid)
-- -----------------------------------------------------------------------------
SELECT
    CAST(a.supplier_number AS TEXT) AS supplier_number,
    COUNT(DISTINCT p.payments_uuid) AS complementos
FROM tenant_fiscal.payments p
JOIN tenant_fiscal.addendum a ON a.payments_uuid = p.payments_uuid
GROUP BY a.supplier_number
ORDER BY a.supplier_number;

-- Resultado:
-- 1 → 4 complementos
-- Vendors sin complementos en addendum retornan 0 resultados (filtro activo correctamente)


-- -----------------------------------------------------------------------------
-- 3. Total de complementos sin filtro (acceso total — Ivan)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_complementos
FROM tenant_fiscal.payments;
-- Esperado: 19 complementos


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(DISTINCT p.payments_uuid) AS total_resultado
FROM tenant_fiscal.payments p
WHERE p.payments_uuid IN (
    SELECT a.payments_uuid
    FROM tenant_fiscal.addendum a
    WHERE a.payments_uuid IS NOT NULL
    AND CAST(a.supplier_number AS TEXT) IN ('11111')
);
-- Esperado: 0 (supplier 11111 no tiene complementos en addendum.payments_uuid)


-- -----------------------------------------------------------------------------
-- 5. Simular filtro: USR_JOSE (ATR001 = 11111, 22222)
-- -----------------------------------------------------------------------------
SELECT COUNT(DISTINCT p.payments_uuid) AS total_resultado
FROM tenant_fiscal.payments p
WHERE p.payments_uuid IN (
    SELECT a.payments_uuid
    FROM tenant_fiscal.addendum a
    WHERE a.payments_uuid IS NOT NULL
    AND CAST(a.supplier_number AS TEXT) IN ('11111', '22222')
);
-- Esperado: 0


-- -----------------------------------------------------------------------------
-- 6. Simular filtro con vendor que SÍ tiene complementos (12345)
--    Demuestra que el mecanismo funciona con datos reales
-- -----------------------------------------------------------------------------
SELECT
    p.payments_uuid,
    p.folio,
    p.series,
    p.status,
    p.payment_date,
    CAST(a.supplier_number AS TEXT) AS supplier
FROM tenant_fiscal.payments p
JOIN tenant_fiscal.addendum a ON a.payments_uuid = p.payments_uuid
WHERE CAST(a.supplier_number AS TEXT) IN ('12345')
ORDER BY p.payment_date DESC;
-- Esperado: 4 complementos del proveedor 12345


-- -----------------------------------------------------------------------------
-- 7. Verificar USR_ANA → WRN7029 (tiene ATR002 pero NO ATR001)
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
-- Esperado: ATR002 = TPR001 → NO tiene ATR001 → x-user-vendors vacío → WRN7029


-- -----------------------------------------------------------------------------
-- 8. Ver todos los complementos con su supplier (para análisis completo)
-- -----------------------------------------------------------------------------
SELECT
    p.payments_uuid,
    p.folio,
    p.series,
    p.status,
    p.payment_date,
    COALESCE(CAST(a.supplier_number AS TEXT), '(sin addendum)') AS supplier
FROM tenant_fiscal.payments p
LEFT JOIN tenant_fiscal.addendum a ON a.payments_uuid = p.payments_uuid
ORDER BY CAST(a.supplier_number AS TEXT) NULLS LAST, p.payment_date DESC;
