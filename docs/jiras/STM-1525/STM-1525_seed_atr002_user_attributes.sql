-- =============================================================
-- JIRA: STM-1525 - Seed ATR002 (TipoProveedor) para usuarios de prueba
--
-- Propósito:
--   Asigna el atributo ATR002 (TipoProveedor) a los usuarios de prueba.
--   util-api usa ATR002 para filtrar shared_catalogs.supplier por supplier_type_id.
--
-- Usuarios:
--   ANA   (sb000002) → ATR002 = TPR001  (tipo 1, Mercancía)
--   IVAN  (sb000005) → ATR002 = -1      (acceso total, todos los tipos)
--
-- Pre-requisitos:
--   1. core_security.user_data con sb000001..sb000005 (sesiones/security_schema.sql)
--   2. shared_catalogs.catalog_detail con ATR002 en CatAtributo (header_id=62)
--   3. shared_catalogs.catalog_detail con TPR001 en CatTipoProveedor (header_id=22)
--   4. STM-1525_cat_tipo_proveedor_external_key.sql ejecutado
--
-- Esquema: core_security + shared_catalogs
-- Autor: dmontes
-- Fecha: 2026-05-08
-- =============================================================

BEGIN;

-- ANA (sb000002) → ATR002 = TPR001
INSERT INTO core_security.user_attribute
    (user_data_id, catalog_detail_attribute_type_id, catalog_detail_attribute_value_id, status, created_by)
SELECT
    (SELECT user_data_id FROM core_security.user_data WHERE sub = 'sb000002'),
    (SELECT id FROM shared_catalogs.catalog_detail WHERE key = 'ATR002'
     AND header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatAtributo')),
    (SELECT id FROM shared_catalogs.catalog_detail WHERE key = 'TPR001'
     AND header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatTipoProveedor')),
    1,
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM core_security.user_attribute
    WHERE user_data_id = (SELECT user_data_id FROM core_security.user_data WHERE sub = 'sb000002')
    AND catalog_detail_attribute_type_id = (SELECT id FROM shared_catalogs.catalog_detail WHERE key = 'ATR002'
        AND header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatAtributo'))
);

-- IVAN (sb000005) → ATR002 = -1 (acceso total)
-- Nota: -1 es catalog_detail con key='-1' en CatAtributo (mismo header que ATR001 values)
-- Si -1 no existe en CatTipoProveedor, Ivan ya tiene ATR001=-1 para vendors.
-- Para suppliers (ATR002), Ivan puede omitirse si la API trata ATR001=-1 como acceso total global.
-- Descomentar si util-api revisa ATR002 específicamente para suppliers:
--
-- INSERT INTO core_security.user_attribute
--     (user_data_id, catalog_detail_attribute_type_id, catalog_detail_attribute_value_id, status, created_by)
-- SELECT
--     (SELECT user_data_id FROM core_security.user_data WHERE sub = 'sb000005'),
--     (SELECT id FROM shared_catalogs.catalog_detail WHERE key = 'ATR002'
--      AND header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatAtributo')),
--     NULL,   -- NULL value_id = acceso total (la API interpreta NULL como -1)
--     1,
--     'SYSTEM'
-- WHERE NOT EXISTS (
--     SELECT 1 FROM core_security.user_attribute
--     WHERE user_data_id = (SELECT user_data_id FROM core_security.user_data WHERE sub = 'sb000005')
--     AND catalog_detail_attribute_type_id = (SELECT id FROM shared_catalogs.catalog_detail WHERE key = 'ATR002'
--         AND header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatAtributo'))
-- );

COMMIT;

-- =============================================================
-- VERIFICACIÓN POST-EJECUCIÓN
-- =============================================================
-- SELECT
--     ud.sub,
--     ud.preferred_username,
--     cd_type.key  AS tipo_atributo,
--     cd_val.key   AS valor_atributo
-- FROM core_security.user_data ud
-- JOIN core_security.user_attribute ua ON ua.user_data_id = ud.user_data_id
-- JOIN shared_catalogs.catalog_detail cd_type ON cd_type.id = ua.catalog_detail_attribute_type_id
-- LEFT JOIN shared_catalogs.catalog_detail cd_val ON cd_val.id = ua.catalog_detail_attribute_value_id
-- WHERE cd_type.key = 'ATR002'
-- ORDER BY ud.preferred_username;
--
-- Resultado esperado:
--   sb000002 | USR_ANA | ATR002 | TPR001
-- =============================================================
