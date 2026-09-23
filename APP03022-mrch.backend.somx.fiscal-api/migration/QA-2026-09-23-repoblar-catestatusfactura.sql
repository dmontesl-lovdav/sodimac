-- =====================================================================
-- Repoblar catalogo de estatus de FACTURA en UAT (2026-09-23)
--
-- Problema: en UAT el header 'CATESTATUSFACTURA' (id 127) quedo con SOLO 1 valor
-- (status 1); faltan los estatus 2-20. Por eso los nombres de factura caen al enum
-- fallback (ej. 20 -> "Error en la contabilizacion" en vez de "Cancelada").
-- (El casing lo resuelve el fix de codigo en findCatalogDescription: UPPER(ch.code).)
--
-- Este script inserta los 19 estatus faltantes (2-20) con su descripcion ES
-- (tren v1.0(8)), bajo el header por su CODE (case-insensitive), respetando el
-- value 1 existente. Idempotente: no re-inserta lo que ya exista.
-- dict_id nuevo por estatus = MAX(dict_id)+n.
-- =====================================================================

-- 1) catalog_detail: estatus 2-20 (skip los que ya existan)
WITH base AS (
    SELECT COALESCE(MAX(dict_id), 10000) AS m FROM shared_catalogs.dictionary_lang
),
h AS (
    SELECT id FROM shared_catalogs.catalog_header
    WHERE UPPER(code) = UPPER('CatEstatusFactura') LIMIT 1
),
nuevos(val, nombre, rn) AS (
    VALUES
      ('2','Recibido Parcial',1),
      ('3','En proceso de envio',2),
      ('4','En proceso de desglose',3),
      ('5','Desglose de factura',4),
      ('6','Error en el desglose xml',5),
      ('7','Pendiente Envio',6),
      ('8','Enviada',7),
      ('9','Error registro contable',8),
      ('10','Error de Envio',9),
      ('11','Pendiente Envio i213',10),
      ('12','Enviada i213',11),
      ('13','Error i213',12),
      ('14','Pendiente de contabilizar',13),
      ('15','Pendiente de Pago',14),
      ('16','Rechazo Contable',15),
      ('17','Pendiente de complemento',16),
      ('18','Pago Manual',17),
      ('19','Completado',18),
      ('20','Cancelada',19)
)
INSERT INTO shared_catalogs.catalog_detail
    (header_id, key, dict_id, value, status, sort_order, created_by, valid_from)
SELECT h.id,
       'CEF' || lpad(n.val, 4, '0'),
       b.m + n.rn,
       n.val,
       1,
       n.val::int,
       'system',
       DATE '2018-01-01'
FROM nuevos n CROSS JOIN base b CROSS JOIN h
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = h.id AND cd.value = n.val
);

-- 2) dictionary_lang (ES, lang_id=1): descripcion por cada detalle recien creado
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT cd.dict_id, 1, m.nombre
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
JOIN (VALUES
      ('2','Recibido Parcial'),
      ('3','En proceso de envio'),
      ('4','En proceso de desglose'),
      ('5','Desglose de factura'),
      ('6','Error en el desglose xml'),
      ('7','Pendiente Envio'),
      ('8','Enviada'),
      ('9','Error registro contable'),
      ('10','Error de Envio'),
      ('11','Pendiente Envio i213'),
      ('12','Enviada i213'),
      ('13','Error i213'),
      ('14','Pendiente de contabilizar'),
      ('15','Pendiente de Pago'),
      ('16','Rechazo Contable'),
      ('17','Pendiente de complemento'),
      ('18','Pago Manual'),
      ('19','Completado'),
      ('20','Cancelada')
     ) m(val, nombre) ON m.val = cd.value
WHERE UPPER(ch.code) = UPPER('CatEstatusFactura')
  AND NOT EXISTS (
      SELECT 1 FROM shared_catalogs.dictionary_lang dl
      WHERE dl.dict_id = cd.dict_id AND dl.lang_id = 1
  );

-- 3) Verificar (esperado: 1..20 con su nombre ES)
-- SELECT cd.value, dl.description
-- FROM shared_catalogs.catalog_header ch
-- JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
-- JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id AND dl.lang_id = 1
-- WHERE UPPER(ch.code) = UPPER('CatEstatusFactura')
-- ORDER BY (cd.value)::int;
