SELECT 
  i.invoice_uuid,
  i.fiscal_uuid,
  i.series,
  i.folio,
  i.status,
  i.document_type,
  i.created_at,
  LENGTH(i.xml_content) AS xml_len,
  iss.rfc AS emisor_rfc,
  iss.name AS emisor_name,
  a.supplier_number AS numero_proveedor
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON iss.issuer_uuid = i.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
WHERE i.document_type = 'I'
  AND i.status = 3
  AND i.created_at >= CURRENT_DATE - INTERVAL '6 months'
  AND i.created_at <= CURRENT_DATE + INTERVAL '1 day'
ORDER BY i.created_at DESC;


SELECT 
  status, 
  COUNT(*) AS total,
  COUNT(xml_content) AS con_xml,
  COUNT(CASE WHEN xml_content IS NULL THEN 1 END) AS sin_xml
FROM tenant_fiscal.invoice
WHERE document_type = 'I'
  AND created_at >= CURRENT_DATE - INTERVAL '6 months'
GROUP BY status
ORDER BY status;



select * FROM shared_catalogs.supplier


SELECT ordinal_position, column_name, data_type, character_maximum_length, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema='shared_catalogs' AND table_name='supplier'
ORDER BY ordinal_position;



SELECT option_id, source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 1
ORDER BY source_status, target_status;

-- 2) NOTA DE CREDITO (option_id=2): para ver el cancel (target 10 "Cancelada")
SELECT option_id, source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 2
ORDER BY source_status, target_status;



 SELECT option_id, source_status, target_status
FROM shared_catalogs.status_train
ORDER BY option_id, source_status, target_status;


SELECT a.addendum_uuid, a.invoice_uuid,
       a.supplier_number, a.reception_number,
       a.purchase_order_number, a.shipping_guide_number,
       a.addenda_type, a.update_date,
       length(a.addendum_content) AS content_len,
       i.fiscal_uuid, i.document_type, i.series, i.folio
FROM tenant_fiscal.addendum a
LEFT JOIN tenant_fiscal.invoice i ON i.invoice_uuid = a.invoice_uuid
ORDER BY a.update_date DESC NULLS LAST
LIMIT 20;



SELECT invoice_uuid, status, created_at 
FROM tenant_fiscal.invoice 
WHERE series = 'A' AND folio = '0128413484'
ORDER BY created_at DESC;

DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid = '4aca4c73-6be0-4ffe-821c-1300d7b1e64e';
DELETE FROM tenant_fiscal.tax WHERE invoice_uuid = 'edd3e484-0c9e-48bb-8fd2-a7a3113e9abf';
DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid = 'edd3e484-0c9e-48bb-8fd2-a7a3113e9abf';
DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid = 'edd3e484-0c9e-48bb-8fd2-a7a3113e9abf';


DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid = '4aca4c73-6be0-4ffe-821c-1300d7b1e64e';
select * FROM tenant_fiscal.tax WHERE invoice_uuid = 'edd3e484-0c9e-48bb-8fd2-a7a3113e9abf';
select * FROM tenant_fiscal.addendum WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';
select * FROM tenant_fiscal.invoice WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';


SELECT invoice_uuid, status, created_at 
FROM tenant_fiscal.invoice 
WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';


SELECT reception_id, amount, created_at
FROM tenant_finance.reception
WHERE ABS(amount - 240585.24) <= 40
ORDER BY created_at DESC
LIMIT 5;


SELECT reception_id, amount, created_at
FROM tenant_finance.reception
ORDER BY created_at desc


DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494');
DELETE FROM tenant_fiscal.tax WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';
DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';
DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';



SELECT supplier_number, purchase_order_number FROM tenant_fiscal.invoice WHERE invoice_uuid = 'cd77a7b6-fcf1-41d4-a9c1-715e37f29494';

SELECT status, created_at FROM tenant_fiscal.invoice WHERE invoice_uuid = '34be8276-da9d-40dd-9b2a-9125dc02331e';



DELETE FROM tenant_fiscal.tax_transfer
WHERE tax_uuid IN (
  SELECT t.tax_uuid FROM tenant_fiscal.tax t
  JOIN tenant_fiscal.invoice i ON t.invoice_uuid = i.invoice_uuid
  WHERE i.fiscal_uuid = 'd2b0732b-bc14-4dbe-aa64-3d9214629b9e'
);

DELETE FROM tenant_fiscal.tax
WHERE invoice_uuid IN (
  SELECT invoice_uuid FROM tenant_fiscal.invoice
  WHERE fiscal_uuid = 'd2b0732b-bc14-4dbe-aa64-3d9214629b9e'
);

DELETE FROM tenant_fiscal.addendum
WHERE invoice_uuid IN (
  SELECT invoice_uuid FROM tenant_fiscal.invoice
  WHERE fiscal_uuid = 'd2b0732b-bc14-4dbe-aa64-3d9214629b9e'
);

DELETE FROM tenant_fiscal.invoice
WHERE fiscal_uuid = 'd2b0732b-bc14-4dbe-aa64-3d9214629b9e';


SELECT invoice_uuid, fiscal_uuid, status, created_at
FROM tenant_fiscal.invoice
WHERE series = 'A' AND folio = '0128413484'
ORDER BY created_at DESC;


DELETE FROM tenant_fiscal.tax_transfer
WHERE tax_uuid IN (
  SELECT t.tax_uuid FROM tenant_fiscal.tax t
  WHERE t.invoice_uuid = '34be8276-da9d-40dd-9b2a-9125dc02331e'
);

DELETE FROM tenant_fiscal.tax
WHERE invoice_uuid = '34be8276-da9d-40dd-9b2a-9125dc02331e';

DELETE FROM tenant_fiscal.addendum
WHERE invoice_uuid = '34be8276-da9d-40dd-9b2a-9125dc02331e';

DELETE FROM tenant_fiscal.invoice
WHERE invoice_uuid = '34be8276-da9d-40dd-9b2a-9125dc02331e';



SELECT * FROM tenant_fiscal.invoice WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';
select * from tenant_fiscal.addendum
WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';


DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92');
DELETE FROM tenant_fiscal.tax WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';
DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';
DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';


-- Confirmar estatus 3
SELECT invoice_uuid, status, created_at
FROM tenant_fiscal.invoice
WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';



-- Confirmar addenda guardada
SELECT * FROM tenant_fiscal.addendum
WHERE invoice_uuid = '137a43eb-ad66-4aaf-b46d-009a0ecdfe92';



ALTER TABLE tenant_fiscal.invoice DROP CONSTRAINT IF EXISTS chk_invoice_status;
ALTER TABLE tenant_fiscal.invoice ADD CONSTRAINT chk_invoice_status
    CHECK (status = ANY (ARRAY[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]));


SELECT option_id, source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 1
ORDER BY source_status, target_status;


SELECT con.conname, pg_get_constraintdef(con.oid)
FROM pg_constraint con
JOIN pg_class rel ON rel.oid = con.conrelid
JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
WHERE nsp.nspname = 'tenant_fiscal'
  AND rel.relname = 'invoice'
  AND con.conname = 'chk_invoice_status';


select * from tenant_fiscal.invoice;
ALTER TABLE tenant_fiscal.invoice ADD COLUMN IF NOT EXISTS pdf_gcs_object VARCHAR(500);


select *
FROM shared_catalogs.catalog_header

select *
from shared_catalogs.catalog_detail



--------------------------


-- ============================================================
-- 1. Columna PDF en invoice  (esperado: 1 fila, pdf_gcs_object varchar 500)
-- ============================================================
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_schema='tenant_fiscal' AND table_name='invoice'
  AND column_name='pdf_gcs_object';

-- ============================================================
-- 2. Tren de Estatus v1.0 Factura (option_id=1)
--    Esperado: 18 filas, y CLAVE: existe (2 -> 1) = cancelar
-- ============================================================
SELECT option_id, source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id=1
ORDER BY source_status, target_status;

-- check rápido: ¿existe la transición cancelar (2->1)?  (esperado: 1)
SELECT count(*) AS cancelar_2_a_1
FROM shared_catalogs.status_train
WHERE option_id=1 AND source_status=2 AND target_status=1;

-- ============================================================
-- 3. Constraint de estatus admite 1..18 (tren v1.0)
-- ============================================================
SELECT pg_get_constraintdef(con.oid)
FROM pg_constraint con
JOIN pg_class rel ON rel.oid=con.conrelid
JOIN pg_namespace n ON n.oid=rel.relnamespace
WHERE n.nspname='tenant_fiscal' AND rel.relname='invoice'
  AND con.conname='chk_invoice_status';


-- ============================================================
-- 4. Catálogos requeridos (esperado: 3 filas, status=1)
-- ============================================================
SELECT id, code, status, catalog_type
FROM shared_catalogs.catalog_header
WHERE code IN ('CatBloqueoTipoProveedor','CatFormaPagoValidoNc','CatUsoCfdiValidoNc')
ORDER BY code;

-- 4a. Detalle CatFormaPagoValidoNc  (esperado: value '99', status=1)
SELECT cd.key, cd.value, cd.status
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id
WHERE ch.code='CatFormaPagoValidoNc';
-- Sin resultados

-- 4b. Detalle CatUsoCfdiValidoNc  (esperado: value 'G02', status=1)
SELECT cd.key, cd.value, cd.status
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id
WHERE ch.code='CatUsoCfdiValidoNc';

-- 4c. Detalle CatBloqueoTipoProveedor (tipos bloqueados; status=1 = bloqueado)
SELECT cd.key, cd.value, cd.status, cd.parent_element_id
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id
WHERE ch.code='CatBloqueoTipoProveedor'
ORDER BY cd.key;

-- ============================================================
-- 5. Parámetros de tolerancia  (esperado: id3 monto, id4 porcentaje)
--    Revisar status (1=activo) y value
-- ============================================================
SELECT id_parameter, name, value, status
FROM core_utils.cat_parameter
WHERE id_parameter IN (3,4)
ORDER BY id_parameter;


-- ============================================================
-- 6. Mensajes de negocio nuevos (opcional - hay fallback en enum)
--    Esperado: BUS058/059/060/2028/2029 si se seedearon
-- ============================================================
SELECT cd.key, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON ch.id=cd.header_id
LEFT JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id=cd.dict_id AND dl.lang_id=1
WHERE ch.code='CatMsgNegocio'
  AND cd.key IN ('BUS057','BUS058','BUS059','BUS060','BUS2028','BUS2029')
ORDER BY cd.key;





DO $$
DECLARE new_dict integer; new_detail integer;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail WHERE key='WRN7034') THEN
    SELECT COALESCE(MAX(dict_id),0)+1 INTO new_dict FROM shared_catalogs.dictionary_lang;
    SELECT COALESCE(MAX(id),0)+1 INTO new_detail FROM shared_catalogs.catalog_detail;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
      VALUES (new_dict, 1, 'La factura será rechazada y las notas de crédito serán canceladas, ya que el monto total de la factura menos las notas de crédito son menor al monto disponible de la recepción, ¿Desea continuar?');
    INSERT INTO shared_catalogs.catalog_detail (id, header_id, key, dict_id, value, sort_order, status)
      VALUES (new_detail, 11, 'WRN7034', new_dict, '', 7034, 1);
  END IF;
END $$;


select *
from shared_catalogs.dictionary_lang


SELECT key, status FROM shared_catalogs.catalog_detail WHERE key='WRN7034';

SELECT invoice_uuid, fiscal_uuid, series, folio, subtotal, status
FROM tenant_fiscal.invoice WHERE status=2 ORDER BY created_at DESC LIMIT 5;

SELECT a.reception_number, r.amount, r.status
FROM tenant_fiscal.addendum a
JOIN tenant_finance.reception r ON r.reception_number = a.reception_number
WHERE a.invoice_uuid = 'e86b7e68-9376-4608-9a48-32a4d1ba8d14';


SELECT series, folio, subtotal, status FROM tenant_fiscal.invoice
WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';


DO $$
DECLARE r RECORD;
BEGIN
  FOR r IN SELECT invoice_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid='a1040000-0000-0000-0000-000000000031' LOOP
    DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
    DELETE FROM tenant_fiscal.tax_withholding WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
    DELETE FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid;
    DELETE FROM tenant_fiscal.related_cfdi WHERE invoice_uuid=r.invoice_uuid OR related_invoice_uuid=r.invoice_uuid;
    DELETE FROM tenant_fiscal.invoice_status_history WHERE invoice_uuid=r.invoice_uuid;
    DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid=r.invoice_uuid;
    DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid=r.invoice_uuid;
  END LOOP;
END $$;
UPDATE tenant_fiscal.invoice SET status=2 WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
UPDATE tenant_finance.reception SET status=1 WHERE reception_number='999056';


SELECT status FROM tenant_fiscal.invoice WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
SELECT count(*) FROM tenant_fiscal.invoice WHERE fiscal_uuid='a1040000-0000-0000-0000-000000000032';

SELECT 'factura' AS doc, status FROM tenant_fiscal.invoice WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185'
UNION ALL
SELECT 'nc', status FROM tenant_fiscal.invoice WHERE fiscal_uuid='a1040000-0000-0000-0000-000000000032';
SELECT reception_number, status AS recep FROM tenant_finance.reception WHERE reception_number='999056';




DO $$
DECLARE new_dict integer; new_detail integer;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail WHERE key='BUS3103') THEN
    SELECT COALESCE(MAX(dict_id),0)+1 INTO new_dict FROM shared_catalogs.dictionary_lang;
    SELECT COALESCE(MAX(id),0)+1 INTO new_detail FROM shared_catalogs.catalog_detail;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
      VALUES (new_dict, 1, 'Las fechas de recepción son obligatorias cuando no se realiza la búsqueda por UUID.');
    INSERT INTO shared_catalogs.catalog_detail (id, header_id, key, dict_id, value, sort_order, status)
      VALUES (new_detail, 11, 'BUS3103', new_dict, '', 3103, 1);
  END IF;
END $$;




------------------------------------


DO $$ DECLARE r RECORD; BEGIN
 FOR r IN SELECT invoice_uuid FROM tenant_fiscal.invoice WHERE fiscal_uuid IN
   ('d1230000-0000-0000-0000-0000000000a0','d1230000-0000-0000-0000-0000000000b0') LOOP
   DELETE FROM tenant_fiscal.related_cfdi WHERE invoice_uuid=r.invoice_uuid OR related_invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.tax_transfer WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax_withholding WHERE tax_uuid IN (SELECT tax_uuid FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid);
   DELETE FROM tenant_fiscal.tax WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice_status_history WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.addendum WHERE invoice_uuid=r.invoice_uuid;
   DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid=r.invoice_uuid;
 END LOOP; END $$;
UPDATE tenant_fiscal.invoice SET status=2 WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
UPDATE tenant_finance.reception SET status=0 WHERE reception_number='999056';


SELECT status FROM tenant_fiscal.invoice WHERE fiscal_uuid='31343515-7304-4d3e-a109-6a2102c69185';
SELECT count(*) FROM tenant_fiscal.invoice WHERE fiscal_uuid='d1230000-0000-0000-0000-0000000000c0';


SELECT document_type, folio, status FROM tenant_fiscal.invoice
WHERE fiscal_uuid IN ('31343515-7304-4d3e-a109-6a2102c69185','d1230000-0000-0000-0000-0000000000c0') ORDER BY document_type;
SELECT reception_number, status FROM tenant_finance.reception WHERE reception_number='999056';




SELECT *
FROM tenant_finance.reception
WHERE reception_number = '1';   -- <-- el numero del addendum de la factura relacionada
Hibernate lo ejecuta esperando 1 fila; si devuelve 2 → NonUniqueResultException → ERR003.

Para validar/reproducir en BD — muestra los números que traen >1 fila (esos truenan):


SELECT reception_number, COUNT(*) AS filas
FROM tenant_finance.reception
GROUP BY reception_number
HAVING COUNT(*) > 1
ORDER BY reception_number;


Y el que efectivamente tronó, sustituyendo el número de la recepción de la factura relacionada (ej. 2):


SELECT reception_id, reception_number, purchase_order_uuid, amount, status, created_at
FROM tenant_finance.reception
WHERE reception_number = '2';   -- devuelve 2 filas -> ahi truena
Para saber qué número disparó el error en el caso de QA (partiendo de la factura relacionada / su UUID fiscal):


SELECT a.reception_number, COUNT(r.reception_id) AS filas
FROM tenant_fiscal.addendum a
JOIN tenant_fiscal.invoice i ON i.invoice_uuid = a.invoice_uuid
LEFT JOIN tenant_finance.reception r ON r.reception_number = a.reception_number
WHERE i.fiscal_uuid = 'PON-UUID-FACTURA-RELACIONADA'
GROUP BY a.reception_number;







SELECT reception_number, COUNT(*) AS filas,
       COUNT(DISTINCT purchase_order_uuid) AS ocs
FROM tenant_finance.reception
GROUP BY reception_number
HAVING COUNT(*) > 1
ORDER BY reception_number;


BEGIN;

UPDATE tenant_finance.reception r
SET reception_number = r.reception_number || '_OLD_' || substr(r.reception_id::text,1,8)
FROM (
  SELECT reception_id,
         row_number() OVER (PARTITION BY reception_number ORDER BY created_at DESC) AS rn
  FROM tenant_finance.reception
  WHERE reception_number IN (
     SELECT reception_number
     FROM tenant_finance.reception
     GROUP BY reception_number
     HAVING COUNT(*) > 1
  )
) d
WHERE r.reception_id = d.reception_id
  AND d.rn > 1;

-- verificar en la MISMA transaccion antes de confirmar
SELECT reception_number, COUNT(*)
FROM tenant_finance.reception
GROUP BY reception_number HAVING COUNT(*) > 1;   -- debe dar 0 filas

COMMIT;


SELECT reception_number, COUNT(*)
FROM tenant_finance.reception
GROUP BY reception_number HAVING COUNT(*) > 1;   -- 0 filas = OK


UPDATE tenant_finance.reception
SET reception_number = split_part(reception_number, '_OLD_', 1)
WHERE reception_number LIKE '%\_OLD\_%';




UPDATE core_utils.cat_message
SET description = 'La factura será rechazada y las notas de crédito serán canceladas: el monto de la factura menos las notas de crédito ({0}) es menor al monto disponible de la recepción ({1}). ¿Desea continuar?',
    updated_by = 1, updated_at = CURRENT_TIMESTAMP
WHERE message_code = 'WRN7034';





SELECT cd.value, ch.status AS header_status, cd.status AS detail_status
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
WHERE ch.code = 'CatTipoRelacionFacturaNC';


SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_name ILIKE 'catalog_header';

-- y buscar el code en cada catalog_header que exista (repetir cambiando el esquema):
SELECT 'shared_catalogs' AS esquema, id, code, status FROM shared_catalogs.catalog_header WHERE code ILIKE '%TipoRelacionFacturaNC%'
UNION ALL
SELECT 'core_utils', id, code, status FROM core_utils.catalog_header WHERE code ILIKE '%TipoRelacionFacturaNC%';
-- (agrega UNION ALL por cada esquema que tenga catalog_header segun la 1a query)


SELECT ch.code, ch.status, cd.value, cd.status
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON cd.header_id = ch.id
WHERE ch.code IN ('CatTipoRelacionFacturaNC','CatFormaPagoValidoNc')
ORDER BY ch.code, cd.value;


SELECT id, code, name, status
FROM shared_catalogs.catalog_header
WHERE id = 104
   OR code ILIKE '%relacion%'
   OR code ILIKE '%TipoRelacion%'
   OR name ILIKE '%relaci%nota%'
   OR name ILIKE '%factura%nota%cr%';



UPDATE core_utils.cat_message
SET description = 'El tipo de relación de la Nota de Crédito no se encuentra permitido. Por favor, validar con el área financiera la relación permitida.',
    updated_by = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE message_code = 'BUS045';

-- verificar
SELECT message_code, description FROM core_utils.cat_message WHERE message_code = 'BUS045';
