-- ============================================================
-- Re-seed CatEstatusNotaCredito -> modelo nuevo E/F (Ivan jul-2026)
-- Idempotente. Solo toca ES (lang_id=1), que es lo que lee fiscal-api.
-- ============================================================
-- A) Actualizar descripciones ES de los 10 estatus existentes
UPDATE shared_catalogs.dictionary_lang dl
SET description = m.descr
FROM (VALUES
  (1,'Rechazo Comercial'),
  (2,'Recibida Parcial'),
  (3,'En proceso de envio'),
  (4,'Pendiente de contabilizar'),
  (5,'En proceso de descarga'),
  (6,'Desglose de nota de crédito'),
  (7,'Error en el desglose de la nota de crédito'),
  (8,'Contabilizada'),
  (9,'Descontada'),
  (10,'Rechazo contable')
) AS m(val, descr)
JOIN shared_catalogs.catalog_header ch ON ch.code='CatEstatusNotaCredito'
JOIN shared_catalogs.catalog_detail cd ON cd.header_id=ch.id AND (cd.value)::int = m.val
WHERE dl.dict_id = cd.dict_id AND dl.lang_id = 1;

-- B) Agregar estatus 11 (Cancelada) y 12 (Borrada)
DO $$
DECLARE hid int; nd int; ndet int;
BEGIN
  SELECT id INTO hid FROM shared_catalogs.catalog_header WHERE code='CatEstatusNotaCredito';
  IF NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail WHERE header_id=hid AND (value)::int=11) THEN
    SELECT COALESCE(MAX(dict_id),0)+1 INTO nd FROM shared_catalogs.dictionary_lang;
    SELECT COALESCE(MAX(id),0)+1 INTO ndet FROM shared_catalogs.catalog_detail;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (nd, 1, 'Cancelada');
    INSERT INTO shared_catalogs.catalog_detail (id, header_id, key, dict_id, value, sort_order, status)
      VALUES (ndet, hid, 'ENC0011', nd, '11', 11, 1);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail WHERE header_id=hid AND (value)::int=12) THEN
    SELECT COALESCE(MAX(dict_id),0)+1 INTO nd FROM shared_catalogs.dictionary_lang;
    SELECT COALESCE(MAX(id),0)+1 INTO ndet FROM shared_catalogs.catalog_detail;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (nd, 1, 'Borrada');
    INSERT INTO shared_catalogs.catalog_detail (id, header_id, key, dict_id, value, sort_order, status)
      VALUES (ndet, hid, 'ENC0012', nd, '12', 12, 1);
  END IF;
END $$;
