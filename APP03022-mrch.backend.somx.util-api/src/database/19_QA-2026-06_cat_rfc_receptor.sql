-- ============================================================================
-- Catálogo: RFC Receptor autorizado (CatRfcReceptor)
-- ============================================================================
-- Reemplaza la tabla tenant_fiscal.authorized_receiver_catalog (sin pantalla de
-- mantenimiento). Lista de RFCs de receptores autorizados a recibir CFDI. Si el
-- receptor del XML no está aquí (y activo), la factura/NC/complemento se rechaza
-- (BUS008 / ERR029). Decisión Ivan 2026-06-23.
--
-- La validación lee catalog_detail.value = RFC, status=1 = activo.
-- IMPORTANTE: estos 3 RFCs salen del dump local. Antes de correr en UAT, regenerar
-- a partir de la tabla real authorized_receiver_catalog de UAT (puede tener más).
--
-- lang_id: 1=ES, 2=EN, 3=PT. dict_id manual (>10100 libre). Idempotente.
-- ============================================================================
BEGIN;

-- 1. Header
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at, created_by)
SELECT 'CatRfcReceptor','RFC','RFC Receptor','RFC de receptores autorizados a recibir CFDI (reemplaza authorized_receiver_catalog)','fiscal','SIMPLE',1,now(),'system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_header WHERE code='CatRfcReceptor');

-- 2. Dictionary (nombre del receptor)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10101,1,'CLIENTE GENERICO AUTORIZADO'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10101 AND lang_id=1);
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10102,1,'COMERCIALIZADORA SDMHC'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10102 AND lang_id=1);
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10103,1,'SODIMAC MEXICO - RECEPTOR AUTORIZADO'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10103 AND lang_id=1);

-- 3. Detail (value = RFC)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatRfcReceptor'),'RFC0001',10101,1,1,'CGE990101GHI','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatRfcReceptor' AND cd.value='CGE990101GHI');
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatRfcReceptor'),'RFC0002',10102,2,1,'CSD161207R2A','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatRfcReceptor' AND cd.value='CSD161207R2A');
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatRfcReceptor'),'RFC0003',10103,3,1,'LAN7008173R5','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatRfcReceptor' AND cd.value='LAN7008173R5');

COMMIT;
