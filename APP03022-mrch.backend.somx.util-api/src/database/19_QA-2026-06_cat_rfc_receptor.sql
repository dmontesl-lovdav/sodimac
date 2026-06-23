-- ============================================================================
-- Catálogo: RFC Receptor autorizado (CATRFCRECEPTOR)
-- ============================================================================
-- Reemplaza la tabla tenant_fiscal.authorized_receiver_catalog (sin pantalla de
-- mantenimiento). Lista de RFCs de receptores autorizados a recibir CFDI. Si el
-- receptor del XML no está aquí (y activo), la factura/NC/complemento se rechaza
-- (BUS008 / ERR029). Decisión Ivan 2026-06-23.
--
-- OJO casing: el code es CATRFCRECEPTOR (MAYÚSCULAS), igual que en UAT (Ivan ya
-- creó el header allá, id 100). La validación lee catalog_detail.value = RFC,
-- status=1 = activo. En UAT el header ya existe -> el INSERT del header se salta
-- (IF NOT EXISTS); solo se agregan los detalles que falten.
--
-- Los 3 RFCs coinciden con authorized_receiver_catalog de UAT (confirmado 2026-06-23).
-- lang_id: 1=ES. dict_id manual (>10100 libre). Idempotente.
-- ============================================================================
BEGIN;

-- 1. Header (se salta si ya existe, p.ej. en UAT)
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at, created_by)
SELECT 'CATRFCRECEPTOR','CRR','CatRfcReceptor','Catálogo de RFC Receptores validos para recibir documentos fiscales','general','PRIMARIO',1,now(),'system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_header WHERE code='CATRFCRECEPTOR');

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

-- 3. Detail (value = RFC). Se agregan solo los que falten.
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CATRFCRECEPTOR'),'RFC0001',10101,1,1,'CGE990101GHI','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CATRFCRECEPTOR' AND cd.value='CGE990101GHI');
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CATRFCRECEPTOR'),'RFC0002',10102,2,1,'CSD161207R2A','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CATRFCRECEPTOR' AND cd.value='CSD161207R2A');
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CATRFCRECEPTOR'),'RFC0003',10103,3,1,'LAN7008173R5','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CATRFCRECEPTOR' AND cd.value='LAN7008173R5');

COMMIT;
