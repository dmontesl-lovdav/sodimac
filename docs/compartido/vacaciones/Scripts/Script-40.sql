-- 1. Verificar si existe el catálogo CatMsgAdvertencia
SELECT * --id, code, name, status 
FROM shared_catalogs.catalog_header 
WHERE code = 'CatMsgAdvertencia';

-- 2. Verificar si existe el mensaje WRN7009 en ese catálogo
SELECT cd.id, cd.key, cd.value, dl.description
FROM shared_catalogs.catalog_detail cd
LEFT JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.key = 'WRN7009';

-- 3. Ver todos los mensajes existentes en CatMsgAdvertencia (si existe)
SELECT cd.id, cd.key, cd.value, dl.description, cd.status, cd
FROM shared_catalogs.catalog_detail cd
LEFT JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
ORDER BY cd.key;


SELECT key, status, valid_from, valid_to
FROM shared_catalogs.catalog_detail
WHERE header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatEstatusComplemento')
ORDER BY sort_order;

select *
FROM shared_catalogs.catalog_detail
order by header_id;

select *
from shared_catalogs.catalog_detail
where key = 'WRN103';

-- 4. Ver todos los catálogos de mensajes disponibles
SELECT id, code, name, status 
FROM shared_catalogs.catalog_header 
WHERE code LIKE '%Msg%' OR name LIKE '%mensaje%'
ORDER BY code;


SELECT COALESCE(MAX(dict_id), 0) + 1 as next_dict_id FROM shared_catalogs.dictionary_lang;

-- 1. INSERT en dictionary_lang (3 idiomas) - WRN102
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7073, 1, 'El estatus de la recepción no permite realizar esta actualización. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7073, 2, 'The reception status does not allow this update. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7073, 3, 'O status da recepção não permite esta atualização. Por favor, valide as informações antes de continuar.');

-- 2. INSERT en catalog_detail (CatMsgAdvertencia = header_id 11)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES (11, 'WRN103', 7073, NULL, 102, 1);


-- Renombrar columna sent_date a accounting_sent_date
ALTER TABLE tenant_fiscal.invoice 
RENAME COLUMN sent_date TO accounting_sent_date;
 
select *
from tenant_fiscal.invoice ;

SELECT DISTINCT i.rfc, i.name 
FROM tenant_fiscal.issuer i
INNER JOIN tenant_fiscal.invoice inv ON i.issuer_uuid = inv.issuer_uuid;


-- Ver RFC emisores disponibles con fechas de recepción
SELECT DISTINCT 
    i.rfc AS rfc_emisor, 
    i.name AS nombre_emisor,
    inv.created_at AS fecha_recepcion,
    inv.document_type
FROM tenant_fiscal.issuer i
INNER JOIN tenant_fiscal.invoice inv ON i.issuer_uuid = inv.issuer_uuid
ORDER BY inv.created_at DESC;

-- Resumen de RFC disponibles con rangos de fechas
SELECT 
    i.rfc AS rfc_emisor,
    i.name AS nombre_emisor,
    MIN(inv.created_at) AS primera_recepcion,
    MAX(inv.created_at) AS ultima_recepcion,
    COUNT(*) AS total_documentos
FROM tenant_fiscal.issuer i
INNER JOIN tenant_fiscal.invoice inv ON i.issuer_uuid = inv.issuer_uuid
GROUP BY i.rfc, i.name;


SELECT i.document_type, i.status, COUNT(*) as total
FROM tenant_fiscal.invoice i
GROUP BY i.document_type, i.status
ORDER BY total DESC
LIMIT 10;

SELECT iss.rfc, i.document_type, i.status, COUNT(*) as total
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
GROUP BY iss.rfc, i.document_type, i.status
ORDER BY total DESC
LIMIT 10;

SELECT i.invoice_uuid, iss.rfc, i.document_type, i.status, i.issue_date
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
WHERE i.status = 3
LIMIT 5;



-- ============================================================================
-- STM-410: Actualizar constraint de estatus para soportar tren de estatus
-- ============================================================================
-- Problema: El constraint chk_invoice_status solo permite status 1, 2, 3
-- Solucion: Actualizar para permitir todos los estatus del tren (1-13)
-- ============================================================================

-- ============================================================================
-- PASO 1: VALIDACION - Ejecutar para ver el estado actual
-- ============================================================================

-- Ver el constraint actual
SELECT
    tc.constraint_name,
    tc.table_schema,
    tc.table_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_schema = 'tenant_fiscal'
  AND tc.table_name = 'invoice'
  AND tc.constraint_type = 'CHECK';

-- Ver valores de estatus actualmente en uso
SELECT DISTINCT status, COUNT(*) as count
FROM tenant_fiscal.invoice
GROUP BY status
ORDER BY status;

-- ============================================================================
-- PASO 2: CORRECCION - Ejecutar para actualizar el constraint
-- ============================================================================

-- Eliminar el constraint existente
ALTER TABLE tenant_fiscal.invoice
DROP CONSTRAINT IF EXISTS chk_invoice_status;

-- Crear nuevo constraint que permite todos los estatus del tren de estatus
-- Segun STM-1166, los estatus son:
-- 1  = Pendiente Addenda
-- 2  = Validada
-- 3  = Pendiente de Contabilizar
-- 4  = Proceso de descarga
-- 5  = Desglose de factura
-- 6  = Pendiente de envio
-- 7  = Pendiente de pago
-- 8  = Pagado
-- 9  = Complemento de pago
-- 10 = Completado
-- 11 = Rechazo contable
-- 12 = Rechazo de pago
-- 13 = Rechazada

ALTER TABLE tenant_fiscal.invoice
ADD CONSTRAINT chk_invoice_status
CHECK (status IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));

-- ============================================================================
-- PASO 3: VERIFICACION - Ejecutar para confirmar el cambio
-- ============================================================================

-- Verificar que el constraint se actualizo correctamente
SELECT
    tc.constraint_name,
    tc.table_schema,
    tc.table_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_schema = 'tenant_fiscal'
  AND tc.table_name = 'invoice'
  AND tc.constraint_type = 'CHECK';

-- ============================================================================
-- NOTAS:
-- - Este script debe ejecutarse en la base de datos tenant_fiscal
-- - El constraint original probablemente era: CHECK (status IN (1, 2, 3))
-- - El nuevo constraint permite todos los estatus definidos en el tren de estatus
-- - Si hay otros constraints similares, revisar y actualizar tambien
-- ============================================================================



-- ============================================================================
-- VALIDACION: Ver el estado actual de supplier_type en addendum
-- ============================================================================

-- Ver addendas con sus campos
SELECT 
    a.addendum_uuid,
    a.invoice_uuid,
    a.supplier_number,
    a.supplier_type,
    a.purchase_order_number,
    a.reception_number,
    i.document_type,
    i.series,
    i.folio
FROM tenant_fiscal.addendum a
JOIN tenant_fiscal.invoice i ON a.invoice_uuid = i.invoice_uuid
ORDER BY a.created_at DESC
LIMIT 10;

-- ============================================================================
-- UPDATE: Poner datos de prueba para supplier_type
-- ============================================================================

-- Actualizar supplier_type para las addendas existentes
UPDATE tenant_fiscal.addendum 
SET supplier_type = 'NACIONAL'
WHERE supplier_type IS NULL 
  AND supplier_number IS NOT NULL;

-- O para un registro específico (reemplazar UUID)
UPDATE tenant_fiscal.addendum 
SET supplier_type = 'NACIONAL'
WHERE invoice_uuid = '9db51140-3fec-45a9-89ee-7e8b35dda53f';

-- ============================================================================
-- VERIFICACION: Confirmar el cambio
-- ============================================================================

SELECT 
    a.invoice_uuid,
    a.supplier_number,
    a.supplier_type,
    i.series,
    i.folio,
    a.supplier_type
FROM tenant_fiscal.addendum a
JOIN tenant_fiscal.invoice i ON a.invoice_uuid = i.invoice_uuid
WHERE a.supplier_type IS NOT NULL;

select *
 FROM tenant_fiscal.addendum a;

UPDATE tenant_fiscal.addendum 
SET supplier_type = 'SODIMAC'
WHERE invoice_uuid IN (
    '66666666-6666-6666-6666-666666666666',
    
    
    
    
    
    
    '9db51140-3fec-45a9-89ee-7e8b35dda53f'
);


SELECT id, code, prefix, name, description, module, status 
FROM shared_catalogs.catalog_header 
WHERE code LIKE '%Estatus%' 
ORDER BY id;


SELECT id, code, name FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia';

SELECT key, dict_id FROM shared_catalogs.catalog_detail 
WHERE key IN ('WRN7012', 'WRN7013', 'WRN7014', 'WRN7015', 'WRN7016', 'WRN7017');


SELECT dl.dict_id, dl.description 
FROM shared_catalogs.dictionary_lang dl
WHERE dl.lang_id = 1 
AND (dl.description ILIKE '%serie y folio%' 
     OR dl.description ILIKE '%misma serie%' 
     OR dl.description ILIKE '%mismo UUID%'
     OR dl.description ILIKE '%previamente registrada%');

SELECT MAX(dict_id) as max_dict_id FROM shared_catalogs.dictionary_lang;

SELECT * FROM shared_catalogs.dictionary_lang;

-- ============================================================================
-- STM-395 / STM-397: Validaciones de Serie, Folio y UUID
-- Descripcion: Mensajes de advertencia para validaciones de duplicidad
--              en facturas (STM-395) y notas de credito (STM-397)
-- ============================================================================

-- ============================================================================
-- DICTIONARY_LANG: Traducciones (dict_id: 8016-8021)
-- Idiomas: 1=Español, 2=Inglés, 3=Portugués
-- ============================================================================

-- WRN7012: Validacion Serie y Folio - Facturas (dict_id: 8016)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8016, 1, 'La factura requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8016, 2, 'The invoice requires a series and folio to publish the document. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8016, 3, 'A fatura requer uma série e folio para publicar o documento. Por favor, valide as informações antes de continuar.');

-- WRN7013: Duplicado Serie + Folio - Facturas (dict_id: 8017)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8017, 1, 'La factura se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8017, 2, 'The invoice is already registered with the same series and folio. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8017, 3, 'A fatura já está registrada com a mesma série e folio. Por favor, valide as informações antes de continuar.');

-- WRN7014: Duplicado UUID - Facturas (dict_id: 8018)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8018, 1, 'La factura se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8018, 2, 'The invoice is already registered with the same UUID. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8018, 3, 'A fatura já está registrada com o mesmo UUID. Por favor, valide as informações antes de continuar.');

-- WRN7015: Validacion Serie y Folio - Notas de Credito (dict_id: 8019)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8019, 1, 'La nota de crédito requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8019, 2, 'The credit note requires a series and folio to publish the document. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8019, 3, 'A nota de crédito requer uma série e folio para publicar o documento. Por favor, valide as informações antes de continuar.');

-- WRN7016: Duplicado Serie + Folio - Notas de Credito (dict_id: 8020)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8020, 1, 'La nota de crédito se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8020, 2, 'The credit note is already registered with the same series and folio. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8020, 3, 'A nota de crédito já está registrada com a mesma série e folio. Por favor, valide as informações antes de continuar.');

-- WRN7017: Duplicado UUID - Notas de Credito (dict_id: 8021)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8021, 1, 'La nota de crédito se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8021, 2, 'The credit note is already registered with the same UUID. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8021, 3, 'A nota de crédito já está registrada com o mesmo UUID. Por favor, valide as informações antes de continuar.');

-- ============================================================================
-- CATALOG_DETAIL: Registros para CatMsgAdvertencia (header_id=11)
-- ============================================================================

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN7012', 8016, NULL, 7012, 1),
(11, 'WRN7013', 8017, NULL, 7013, 1),
(11, 'WRN7014', 8018, NULL, 7014, 1),
(11, 'WRN7015', 8019, NULL, 7015, 1),
(11, 'WRN7016', 8020, NULL, 7016, 1),
(11, 'WRN7017', 8021, NULL, 7017, 1);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================



select *
from tenant_finance.three_way_match;

select *
from tenant_finance.vw_three_way_match;

select *
from tenant_finance.vw_three_way_match_summary



select *
from shared_catalogs.catalog_detail
where parent_element_id is not null;


SELECT
    ch.code AS catalogo,
    cd.key AS clave_interna,
    cd.external_key AS clave_sat,
    cd.internal_status AS id_negocio,
    dl.description AS descripcion,
    cd.attributes AS atributos_extra
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE ch.code = 'c_RegimenFiscal'
ORDER BY cd.sort_order;




-- =============================================================
-- JIRA: STM-399 - Correccion de naming sobre lo desplegado por Bonelli
-- Descripcion: Homologar nombres de tablas, columnas, constraints
--              e indices al estandar ingles del proyecto
-- Esquema: tenant_finance
-- Autor: dmontes
-- Fecha: 2026-03-02
--
-- Prerequisito: v3 de Bonelli ya aplicada (finanzas_payment_headers
--               y payment_detail ya existen)
--
-- Cambios:
--   1. Renombrar tabla finanzas_payment_headers → payment_header
--   2. Renombrar columnas en español → ingles
--   3. FK se mantiene nullable (hay 3 registros sin cabecera en prod)
--   4. Renombrar constraints e indices obsoletos
-- =============================================================

BEGIN;

-- =============================================================
-- 1. RENOMBRAR TABLA CABECERA
-- =============================================================

ALTER TABLE tenant_finance.finanzas_payment_headers
    RENAME TO payment_header;

-- =============================================================
-- 2. RENOMBRAR COLUMNAS EN CABECERA (payment_header)
-- =============================================================

-- id_referencia_pago → payment_header_uuid
ALTER TABLE tenant_finance.payment_header
    RENAME COLUMN id_referencia_pago TO payment_header_uuid;

-- importe → total_amount
ALTER TABLE tenant_finance.payment_header
    RENAME COLUMN importe TO total_amount;

-- Renombrar PK
ALTER TABLE tenant_finance.payment_header
    RENAME CONSTRAINT finanzas_payment_headers_pkey TO pk_payment_header;

-- Renombrar UNIQUE
ALTER TABLE tenant_finance.payment_header
    RENAME CONSTRAINT uq_finanzas_payment_headers_group TO uq_payment_header_group;

-- Renombrar indices
ALTER INDEX tenant_finance.idx_finanzas_payment_headers_company_anio
    RENAME TO idx_payment_header_company_anio;

ALTER INDEX tenant_finance.idx_finanzas_payment_headers_vendor_payment_date
    RENAME TO idx_payment_header_vendor_payment_date;

-- =============================================================
-- 3. RENOMBRAR COLUMNA FK EN DETALLE (payment_detail)
-- =============================================================

-- Primero eliminar la FK existente (referencia nombre viejo)
ALTER TABLE tenant_finance.payment_detail
    DROP CONSTRAINT fk_finanzas_payments_header;

-- Renombrar columna id_referencia_pago → payment_header_uuid
ALTER TABLE tenant_finance.payment_detail
    RENAME COLUMN id_referencia_pago TO payment_header_uuid;

-- NOTA: FK se mantiene nullable porque hay 3 registros existentes
-- sin cabecera asignada. Se hara NOT NULL cuando la app los vincule.

-- Recrear FK con nombre correcto
ALTER TABLE tenant_finance.payment_detail
    ADD CONSTRAINT fk_payment_detail_header
    FOREIGN KEY (payment_header_uuid)
    REFERENCES tenant_finance.payment_header (payment_header_uuid);

-- =============================================================
-- 4. RENOMBRAR CONSTRAINTS E INDICES EN DETALLE (payment_detail)
-- =============================================================

-- PK
ALTER TABLE tenant_finance.payment_detail
    RENAME CONSTRAINT finanzas_payments_pkey TO pk_payment_detail;

-- Check constraints
ALTER TABLE tenant_finance.payment_detail
    RENAME CONSTRAINT chk_finanzas_payments_amount TO chk_payment_detail_amount;

ALTER TABLE tenant_finance.payment_detail
    RENAME CONSTRAINT chk_finanzas_payments_status TO chk_payment_detail_status;

-- Indices
ALTER INDEX tenant_finance.idx_finanzas_payments_date
    RENAME TO idx_payment_detail_date;

ALTER INDEX tenant_finance.idx_finanzas_payments_document
    RENAME TO idx_payment_detail_document;

ALTER INDEX tenant_finance.idx_finanzas_payments_id_referencia_pago
    RENAME TO idx_payment_detail_header;

ALTER INDEX tenant_finance.idx_finanzas_payments_sap_doc
    RENAME TO idx_payment_detail_sap_doc;

ALTER INDEX tenant_finance.idx_finanzas_payments_status
    RENAME TO idx_payment_detail_status;

ALTER INDEX tenant_finance.idx_finanzas_payments_vendor
    RENAME TO idx_payment_detail_vendor;

COMMIT;

-- =============================================================
-- VERIFICACION POST-EJECUCION
-- =============================================================
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'tenant_finance'
--   AND table_name IN ('payment_header', 'payment_detail')
-- ORDER BY table_name;
--
-- Resultado esperado:
--   payment_detail
--   payment_header
--
SELECT column_name FROM information_schema.columns
 WHERE table_schema = 'tenant_finance'
   AND table_name = 'payment_header'
 ORDER BY ordinal_position;
--
-- Resultado esperado:
--   payment_header_uuid, company, anio, vendor_number, currency,
--   total_amount, payment_date, status, created_by, created_at,
--   updated_by, updated_at
-- =============================================================



select *
    FROM tenant_fiscal.related_cfdi rc
    JOIN tenant_fiscal.invoice i_nc ON rc.invoice_uuid = i_nc.invoice_uuid
    JOIN tenant_fiscal.invoice i_rel ON rc.related_invoice_uuid = i_rel.invoice_uuid
    ORDER BY i_nc.folio;




SELECT * -- source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 2
ORDER BY source_status, target_status;

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES
    (2, 3, 10, 1),   -- Pendiente de Contabilizar -> Cancelada
    (2, 11, 10, 1)   -- Rechazo Contable -> Cancelada
ON CONFLICT (option_id, source_status, target_status) DO NOTHING;


SELECT
    st.source_status,
    CASE st.source_status
        WHEN 1 THEN 'Pendiente Addenda'
        WHEN 2 THEN 'Recibido Parcial'
        WHEN 3 THEN 'Pend. Contabilizar'
        WHEN 4 THEN 'Proceso descarga'
        WHEN 5 THEN 'Desglose NC'
        WHEN 6 THEN 'Pend. envio contab.'
        WHEN 7 THEN 'Aplicado'
        WHEN 11 THEN 'Rechazo Contable'
    END AS source_nombre,
    st.target_status,
    CASE st.target_status
        WHEN 2 THEN 'Recibido Parcial'
        WHEN 3 THEN 'Pend. Contabilizar'
        WHEN 4 THEN 'Proceso descarga'
        WHEN 5 THEN 'Desglose NC'
        WHEN 6 THEN 'Pend. envio contab.'
        WHEN 7 THEN 'Aplicado'
        WHEN 8 THEN 'Completado'
        WHEN 10 THEN 'Cancelada'
        WHEN 11 THEN 'Rechazo Contable'
    END AS target_nombre
FROM shared_catalogs.status_train st
WHERE st.option_id = 2
ORDER BY st.source_status, st.target_status;


INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by, created_at)
SELECT 1, 4, 14, 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE option_id = 1
      AND source_status = 4
      AND target_status = 14
);



SELECT id, option_id, source_status, target_status
FROM shared_catalogs.status_train
WHERE source_status = 4
ORDER BY option_id, target_status;

update tenant_fiscal.invoice 
set status = 3
where status = 4
and   document_type = 'I';

select *
from tenant_fiscal.invoice
where status = 4
and   document_type = 'I';


-- Verificar constraint actual
SELECT pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conname = 'chk_invoice_status';

-- Ampliar constraint para incluir estatus 14
ALTER TABLE tenant_fiscal.invoice DROP CONSTRAINT chk_invoice_status;
ALTER TABLE tenant_fiscal.invoice ADD CONSTRAINT chk_invoice_status
    CHECK (status = ANY (ARRAY[1,2,3,4,5,6,7,8,9,10,11,12,13,14]));


SELECT COUNT(*), document_type
FROM tenant_fiscal.invoice
WHERE status = 4
GROUP BY document_type;

select fiscal_uuid, status, document_type, created_at, issue_date 
from tenant_fiscal.invoice
where status = 3
AND document_type = 'I'
order by created_at DESC;


-- uuid=f0000082-a082-4b82-c082-000000000082
-- uuid=f0000060-a060-4b60-c060-000000000060
-- uuid=f0000047-a047-4b47-c047-000000000047

UPDATE tenant_fiscal.invoice
SET status = 3
WHERE status = 4
  AND document_type = 'I';

update tenant_fiscal.invoice
set created_at = created_at + '2 months'::interval

SELECT COUNT(*)
FROM tenant_fiscal.invoice
WHERE status = 3
  AND document_type = 'E';

SELECT COUNT(*), document_type
FROM tenant_fiscal.invoice
WHERE status = 3
GROUP BY document_type;

SELECT i.fiscal_uuid, i.created_at, i.status, i.document_type
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON iss.issuer_uuid = i.issuer_uuid
LEFT  JOIN tenant_fiscal.receiver rec ON rec.receiver_uuid = i.receiver_uuid
WHERE i.created_at >= '2025-09-18 00:00:00'
  AND i.created_at <= '2026-03-18 23:59:59'
  AND i.document_type = 'I'
  AND i.status = 3
ORDER BY i.created_at ASC
LIMIT 100 OFFSET 0;

SELECT id_parameter, name, value, description
FROM core_utils.cat_parameter
WHERE name = 'MAX_SEARCH_MONTHS';



UPDATE tenant_fiscal.invoice
SET created_at = created_at + '2 months'::interval
WHERE status = 3
  AND document_type = 'I';

select *
from core_audit.activity_logs
order by 1 desc;