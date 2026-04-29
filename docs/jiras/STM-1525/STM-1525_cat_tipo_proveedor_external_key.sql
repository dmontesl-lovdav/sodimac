-- =============================================================
-- JIRA: STM-1525 - Filtro seguridad catálogo proveedor por TipoProveedor
-- Script: Mapeo external_key en CatTipoProveedor → supplier_type.id
--
-- Propósito:
--   La query de filtrado por TipoProveedor necesita resolver el
--   catalog_detail.key ('TPR001'...) al supplier_type.id (1..4)
--   que usa shared_catalogs.supplier.supplier_type_id.
--   El campo external_key de catalog_detail es el puente.
--
-- Afecta: STM-1525, STM-323, STM-1474, STM-321, STM-1524
-- Esquema: shared_catalogs
-- Autor: dmontes
-- Fecha: 2026-04-28
--
-- Prerequisito: catalog_header 'CatTipoProveedor' (id=22) ya existe
--               con catalog_detail keys TPR001-TPR004
-- =============================================================

BEGIN;

-- Verificar estado antes de aplicar
-- SELECT id, key, external_key FROM shared_catalogs.catalog_detail
-- WHERE header_id = 22 ORDER BY key;

UPDATE shared_catalogs.catalog_detail
SET external_key = '1'
WHERE header_id = 22 AND key = 'TPR001';

UPDATE shared_catalogs.catalog_detail
SET external_key = '2'
WHERE header_id = 22 AND key = 'TPR002';

UPDATE shared_catalogs.catalog_detail
SET external_key = '3'
WHERE header_id = 22 AND key = 'TPR003';

UPDATE shared_catalogs.catalog_detail
SET external_key = '4'
WHERE header_id = 22 AND key = 'TPR004';

COMMIT;

-- =============================================================
-- VERIFICACIÓN POST-EJECUCIÓN
-- =============================================================
-- SELECT cd.key, cd.external_key, st.code, st.id
-- FROM shared_catalogs.catalog_detail cd
-- JOIN shared_catalogs.supplier_type st ON st.id = CAST(cd.external_key AS INTEGER)
-- WHERE cd.header_id = 22
-- ORDER BY cd.key;
--
-- Resultado esperado:
--   TPR001 | 1 | MERCANCIA  | 1
--   TPR002 | 2 | TRANSPORTE | 2
--   TPR003 | 3 | INDIRECTOS | 3
--   TPR004 | 4 | SERVICIOS  | 4
-- =============================================================
