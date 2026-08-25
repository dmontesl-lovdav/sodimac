-- =====================================================================================
-- Tren de Estatus Portal FBC v1.0 (5) — Ivan 2026-08-24
-- REMODEL COMPLETO del catálogo de estatus Factura + Nota de Crédito.
-- Ivan confirmó que las facturas/NC previas quedan DESCARTADAS => NO hay migración de datos.
--
-- Alcance:
--   A) status_train: reemplaza transiciones de option_id 1 (Factura) y 2 (NC).
--      (option_id 4 Carta Porte quedó idéntica en v1.0(5) => NO se toca; 5 Recepción tampoco.)
--   B) CatEstatusFactura (header 15): renombra códigos 1..20 (lang ES). 21/22 quedan huérfanos
--      (sin transición que los alcance) => se dejan como deprecados, no estorban.
--   C) CatEstatusNotaCredito (header 16): renombra 1..12 e inserta 13..20 (lang ES).
--
-- Notas:
--   - Solo se actualiza lang_id=1 (ES) — es el que consume fiscal-api (resolveStatusName).
--     Las traducciones EN/PT NO vienen en el Excel de Ivan => quedan pendientes (stale).
--   - Cambio de código Java aparte: NC "Cancelada" pasa de 11 a 20 (constante NC_CANCELADA).
--   - status_train se lee en vivo (sin redeploy). El catálogo de nombres también.
-- =====================================================================================

BEGIN;

-- --------------------------------------------------------------------------------------
-- A) status_train — Factura (1) + NC (2). Reemplazo total (facturas previas descartadas).
-- --------------------------------------------------------------------------------------
DELETE FROM shared_catalogs.status_train WHERE option_id IN (1, 2);

WITH base AS (SELECT COALESCE(MAX(id), 0) AS m FROM shared_catalogs.status_train),
t(opt, src, tgt) AS (VALUES
  -- Factura (option_id 1). 17->19: complemento de pago cierra el ciclo (Ivan 2026-08-25).
  (1,2,3),(1,2,20),(1,3,4),(1,3,5),(1,3,6),(1,3,20),(1,4,5),(1,4,6),
  (1,5,7),(1,5,8),(1,5,9),(1,5,10),(1,6,3),(1,7,8),(1,7,10),(1,8,11),
  (1,8,12),(1,8,13),(1,9,3),(1,10,3),(1,11,12),(1,11,13),(1,12,14),(1,12,15),
  (1,12,16),(1,13,3),(1,14,15),(1,14,16),(1,15,18),(1,16,3),(1,17,19),(1,18,19),
  -- Nota de Crédito (option_id 2) — mismo set + 17->19 (rebate cierra con complemento)
  (2,2,3),(2,2,20),(2,3,4),(2,3,5),(2,3,6),(2,3,20),(2,4,5),(2,4,6),
  (2,5,7),(2,5,8),(2,5,9),(2,5,10),(2,6,3),(2,7,8),(2,7,10),(2,8,11),
  (2,8,12),(2,8,13),(2,9,3),(2,10,3),(2,11,12),(2,11,13),(2,12,14),(2,12,15),
  (2,12,16),(2,13,3),(2,14,15),(2,14,16),(2,15,18),(2,16,3),(2,17,19),(2,18,19)
)
INSERT INTO shared_catalogs.status_train (id, option_id, source_status, target_status, created_by, created_at)
SELECT base.m + row_number() OVER (), t.opt, t.src, t.tgt, 1, now()
FROM t, base;

-- --------------------------------------------------------------------------------------
-- B) CatEstatusFactura (header 15) — renombra 1..20 (lang ES = 1)
-- --------------------------------------------------------------------------------------
UPDATE shared_catalogs.dictionary_lang SET description = 'No valido fiscal'            WHERE dict_id = 1051 AND lang_id = 1; -- 1
UPDATE shared_catalogs.dictionary_lang SET description = 'Recibido Parcial'            WHERE dict_id = 1053 AND lang_id = 1; -- 2
UPDATE shared_catalogs.dictionary_lang SET description = 'En proceso de envio'         WHERE dict_id = 1054 AND lang_id = 1; -- 3
UPDATE shared_catalogs.dictionary_lang SET description = 'En proceso de desglose'      WHERE dict_id = 1055 AND lang_id = 1; -- 4
UPDATE shared_catalogs.dictionary_lang SET description = 'Desglose de factura'         WHERE dict_id = 1056 AND lang_id = 1; -- 5
UPDATE shared_catalogs.dictionary_lang SET description = 'Error en en el desglose xml' WHERE dict_id = 1057 AND lang_id = 1; -- 6
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente Envio'             WHERE dict_id = 1058 AND lang_id = 1; -- 7
UPDATE shared_catalogs.dictionary_lang SET description = 'Enviada'                     WHERE dict_id = 1052 AND lang_id = 1; -- 8
UPDATE shared_catalogs.dictionary_lang SET description = 'Error registro contable'     WHERE dict_id = 1059 AND lang_id = 1; -- 9
UPDATE shared_catalogs.dictionary_lang SET description = 'Error de Envio'              WHERE dict_id = 1060 AND lang_id = 1; -- 10
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente Envio i213'        WHERE dict_id = 1972 AND lang_id = 1; -- 11
UPDATE shared_catalogs.dictionary_lang SET description = 'Enviada i213'                WHERE dict_id = 1973 AND lang_id = 1; -- 12
UPDATE shared_catalogs.dictionary_lang SET description = 'Error i213'                  WHERE dict_id = 1061 AND lang_id = 1; -- 13
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente de contabilizar'   WHERE dict_id = 1062 AND lang_id = 1; -- 14
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente de Pago'           WHERE dict_id = 1063 AND lang_id = 1; -- 15
UPDATE shared_catalogs.dictionary_lang SET description = 'Rechazo Contable'            WHERE dict_id = 1974 AND lang_id = 1; -- 16
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente de complemento'    WHERE dict_id = 1975 AND lang_id = 1; -- 17
UPDATE shared_catalogs.dictionary_lang SET description = 'Pago Manual'                 WHERE dict_id = 1064 AND lang_id = 1; -- 18
UPDATE shared_catalogs.dictionary_lang SET description = 'Completado'                  WHERE dict_id = 10219 AND lang_id = 1; -- 19
UPDATE shared_catalogs.dictionary_lang SET description = 'Cancelada'                   WHERE dict_id = 10220 AND lang_id = 1; -- 20

-- --------------------------------------------------------------------------------------
-- C.1) CatEstatusNotaCredito (header 16) — renombra 1..12 (lang ES = 1)
-- --------------------------------------------------------------------------------------
UPDATE shared_catalogs.dictionary_lang SET description = 'No valido fiscal'            WHERE dict_id = 1065 AND lang_id = 1; -- 1
UPDATE shared_catalogs.dictionary_lang SET description = 'Recibida Parcial'            WHERE dict_id = 1066 AND lang_id = 1; -- 2
UPDATE shared_catalogs.dictionary_lang SET description = 'En proceso de envio'         WHERE dict_id = 1067 AND lang_id = 1; -- 3
UPDATE shared_catalogs.dictionary_lang SET description = 'En proceso de desglose'      WHERE dict_id = 1068 AND lang_id = 1; -- 4
UPDATE shared_catalogs.dictionary_lang SET description = 'Desglose de nota de crédito' WHERE dict_id = 1986 AND lang_id = 1; -- 5
UPDATE shared_catalogs.dictionary_lang SET description = 'Error en en el desglose xml' WHERE dict_id = 1987 AND lang_id = 1; -- 6
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente Envio'             WHERE dict_id = 1988 AND lang_id = 1; -- 7
UPDATE shared_catalogs.dictionary_lang SET description = 'Enviada'                     WHERE dict_id = 1989 AND lang_id = 1; -- 8
UPDATE shared_catalogs.dictionary_lang SET description = 'Error registro contable'     WHERE dict_id = 1990 AND lang_id = 1; -- 9
UPDATE shared_catalogs.dictionary_lang SET description = 'Error de Envio'              WHERE dict_id = 1991 AND lang_id = 1; -- 10
UPDATE shared_catalogs.dictionary_lang SET description = 'Pendiente Envio i213'        WHERE dict_id = 10152 AND lang_id = 1; -- 11
UPDATE shared_catalogs.dictionary_lang SET description = 'Enviada i213'                WHERE dict_id = 10153 AND lang_id = 1; -- 12

-- --------------------------------------------------------------------------------------
-- C.2) CatEstatusNotaCredito — inserta 13..20 (nuevos). dict_id dinámico (portable UAT).
-- --------------------------------------------------------------------------------------
WITH base AS (SELECT COALESCE(MAX(dict_id), 0) AS m FROM shared_catalogs.dictionary_lang),
ins(code, name) AS (VALUES
  (13,'Error i213'),
  (14,'Pendiente de contabilizar'),
  (15,'Pendiente de descuento Pago'),
  (16,'Rechazo Contable'),
  (17,'Pendiente de complemento'),
  (18,'N/A'),
  (19,'Completado'),
  (20,'Cancelada')
),
nd AS (
  SELECT i.code, i.name, base.m + row_number() OVER (ORDER BY i.code) AS new_dict
  FROM ins i, base
  WHERE NOT EXISTS (
      SELECT 1 FROM shared_catalogs.catalog_detail cd
      WHERE cd.header_id = 16 AND cd.value = i.code::text
  )
),
det AS (
  INSERT INTO shared_catalogs.catalog_detail
      (header_id, key, value, dict_id, sort_order, status, created_at, created_by)
  SELECT 16, 'ENC00' || LPAD(code::text, 2, '0'), code::text, new_dict, code, 1, now(), 'tren-v1.0(5)'
  FROM nd
  RETURNING 1
)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
SELECT new_dict, 1, name FROM nd;

-- --------------------------------------------------------------------------------------
-- Verificación
-- --------------------------------------------------------------------------------------
SELECT 'FACTURA transiciones' AS check, count(*) FROM shared_catalogs.status_train WHERE option_id = 1;
SELECT 'NC transiciones' AS check, count(*) FROM shared_catalogs.status_train WHERE option_id = 2;
SELECT cd.value::int code, dl.description
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id AND dl.lang_id = 1
WHERE ch.code = 'CatEstatusNotaCredito'
ORDER BY cd.value::int;

COMMIT;
