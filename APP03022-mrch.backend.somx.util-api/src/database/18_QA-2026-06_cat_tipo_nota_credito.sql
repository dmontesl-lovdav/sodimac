-- ============================================================================
-- Catálogo: Tipo de Nota de Crédito (CatTipoNotaCredito)
-- ============================================================================
-- Clasifica el ORIGEN de una NC (no es estatus ni tren):
--   1 = Ajuste por Recepción
--   2 = Descuento Comercial
--
-- Requerido por el punto 6 de Fer (NC de descuento comercial): el tipo se
-- guarda en la addenda de la NC. NO confundir con:
--   - CatEstatusNotaCredito (estatus / ciclo de vida de la NC)
--   - status_train option_id=2 (transiciones de estatus de la NC)
--
-- Pobla las 3 tablas: catalog_header, dictionary_lang (ES/EN/PT) y catalog_detail.
-- Idempotente (INSERT ... WHERE NOT EXISTS). Header con id serial; el código
-- referencia por CODE. lang_id: 1=ES, 2=EN, 3=PT. dict_id manual (>10090, libre).
-- ============================================================================
BEGIN;

-- 1. Header
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at, created_by)
SELECT 'CatTipoNotaCredito','TNC','Tipo Nota Crédito','Clasificación del origen de la nota de crédito (1 Ajuste por Recepción, 2 Descuento Comercial)','fiscal','SIMPLE',1,now(),'system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_header WHERE code='CatTipoNotaCredito');

-- 2. Dictionary (descripciones por idioma)
-- dict_id 10091 = Ajuste por Recepción
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10091,1,'Ajuste por Recepción'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10091 AND lang_id=1);
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10091,2,'Reception Adjustment'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10091 AND lang_id=2);
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10091,3,'Ajuste por Recepção'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10091 AND lang_id=3);

-- dict_id 10092 = Descuento Comercial
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10092,1,'Descuento Comercial'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10092 AND lang_id=1);
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10092,2,'Commercial Discount'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10092 AND lang_id=2);
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT 10092,3,'Desconto Comercial'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.dictionary_lang WHERE dict_id=10092 AND lang_id=3);

-- 3. Detail (value = id del tipo)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatTipoNotaCredito'),'TNC0001',10091,1,1,'1','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatTipoNotaCredito' AND cd.value='1');

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatTipoNotaCredito'),'TNC0002',10092,2,1,'2','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatTipoNotaCredito' AND cd.value='2');

COMMIT;
