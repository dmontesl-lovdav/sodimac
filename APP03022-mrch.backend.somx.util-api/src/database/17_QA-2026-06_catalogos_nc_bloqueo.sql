-- ============================================================================
-- QA junio-2026: catálogos requeridos por fiscal-api /register
-- ============================================================================
-- Crea (si faltan) los catálogos que validan:
--   - Forma de pago válida para NC      -> CatFormaPagoValidoNc (BUS058)
--   - Uso CFDI válido para NC           -> CatUsoCfdiValidoNc   (BUS059)
--   - Tipos de proveedor bloqueados     -> CatBloqueoTipoProveedor (BUS2028)
--
-- Idempotente (INSERT ... WHERE NOT EXISTS por code/key). Headers con id serial;
-- fiscal-api referencia por CODE, no por id.
--
-- parent_element_id 93-96 = elementos de CatTipoProveedor (TPR001..TPR004),
-- confirmados en UAT 2026-06-15. Si en otro ambiente difieren, ajustar.
--
-- IMPORTANTE: sin estos catálogos, fiscal-api RECHAZA toda NC (forma/uso no
-- configurados) -> BUS058/BUS059. Aplicar junto con el deploy.
-- ============================================================================
BEGIN;

-- Headers
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at, created_by)
SELECT 'CatBloqueoTipoProveedor','CBP','CatBloqueoTipoProveedor','Catálogo de los tipos de proveedores bloqueados para publicar una factura','general','SECUNDARIO',1,now(),'system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_header WHERE code='CatBloqueoTipoProveedor');

INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at, created_by)
SELECT 'CatUsoCfdiValidoNc','CNC','CatUsoCfdiValidoNc','Catálogo de los usos de CFDI validos para registrar una Nota de Crédito','general','PRIMARIO',1,now(),'system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_header WHERE code='CatUsoCfdiValidoNc');

INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at, created_by)
SELECT 'CatFormaPagoValidoNc','FNC','CatFormaPagoValidoNc','Formas de pago permitidas para dar de alta una NC','general','PRIMARIO',1,now(),'system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_header WHERE code='CatFormaPagoValidoNc');

-- Forma de pago NC válida: 99 (activo)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatFormaPagoValidoNc'),'FNC0001',1976,0,1,'99','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatFormaPagoValidoNc' AND cd.value='99');

-- Uso CFDI NC válido: G02 (activo)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, created_by)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatUsoCfdiValidoNc'),'CNC0001',1977,0,1,'G02','2018-01-01','system'
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatUsoCfdiValidoNc' AND cd.value='G02');

-- Bloqueo por tipo: 4 tipos, status=0 (NINGUNO bloqueado por defecto; activar el que aplique)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, value, valid_from, valid_to, created_by, parent_catalog_id, parent_element_id)
SELECT (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatBloqueoTipoProveedor'),
       x.key, x.dict_id, 0, 0, x.value, '2018-01-01','2099-01-01','system',
       (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatTipoProveedor'), x.pelem
FROM (VALUES
  ('CBP0001',1945,'1',93),
  ('CBP0002',1946,'2',94),
  ('CBP0003',1947,'4',95),
  ('CBP0004',1948,'3',96)
) AS x(key,dict_id,value,pelem)
WHERE NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail cd JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id WHERE ch.code='CatBloqueoTipoProveedor' AND cd.key=x.key);

COMMIT;

-- Verificación
SELECT ch.code, cd.key, cd.value, cd.status
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id
WHERE ch.code IN ('CatBloqueoTipoProveedor','CatFormaPagoValidoNc','CatUsoCfdiValidoNc')
ORDER BY ch.code, cd.key;
