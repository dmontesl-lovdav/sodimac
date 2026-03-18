-- ============================================================
-- STM-719: Setup datos de prueba para batch de descarga
-- BD: b2b_portal (PostgreSQL)
-- NOTA: Ejecutar solo en ambiente de pruebas, NO en produccion
-- ============================================================


-- ============================================================
-- PRE: Ver estado actual
-- ============================================================
SELECT document_type, status, COUNT(*),
       SUM(CASE WHEN xml_content IS NOT NULL THEN 1 ELSE 0 END) AS con_xml,
       MIN(created_at) AS mas_antigua,
       MAX(created_at) AS mas_reciente
FROM tenant_fiscal.invoice
WHERE status IN (1, 3)
  AND document_type IN ('I', 'E')
GROUP BY document_type, status
ORDER BY document_type, status;


-- ============================================================
-- PASO 1: Facturas (tipo=I) - Adelantar created_at +2 meses
--         Solo las que esten fuera del rango de 6 meses
-- ============================================================
UPDATE tenant_fiscal.invoice
SET created_at = created_at + '2 months'::interval
WHERE status = 3
  AND document_type = 'I'
  AND created_at < NOW() - INTERVAL '6 months';

SELECT COUNT(*), MIN(created_at), MAX(created_at)
FROM tenant_fiscal.invoice
WHERE status = 3 AND document_type = 'I';


-- ============================================================
-- PASO 2: Facturas (tipo=I) - Asignar XML Sodimac con addenda
--         Fuerza actualizacion en TODAS las de status=3
-- ============================================================
UPDATE tenant_fiscal.invoice
SET xml_content = $XML_I$<cfdi:Comprobante xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:cfdi="http://www.sat.gob.mx/cfd/4" xsi:schemaLocation="http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd" Version="4.0" Serie="TS" Folio="000001" Fecha="2025-11-01T10:00:00" Sello="TEST" FormaPago="99" NoCertificado="00001000000707767690" SubTotal="10000.00" Moneda="MXN" TipoCambio="1" Exportacion="01" Total="11600.00" TipoDeComprobante="I" MetodoPago="PPD" LugarExpedicion="06500" Certificado="TEST"><cfdi:Emisor Rfc="HTR060210FJ9" Nombre="H.H. TRANSPORTES" RegimenFiscal="624"/><cfdi:Receptor Rfc="CSD161207R2A" Nombre="COMERCIALIZADORA SDMHC" DomicilioFiscalReceptor="53150" RegimenFiscalReceptor="601" UsoCFDI="G03"/><cfdi:Conceptos><cfdi:Concepto ClaveProdServ="78101802" NoIdentificacion="TEST-001" Cantidad="1.0000" ClaveUnidad="E48" Unidad="Servicio" Descripcion="SERVICIO DE PRUEBA" ValorUnitario="10000.00" Importe="10000.00" ObjetoImp="02"><cfdi:Impuestos><cfdi:Traslados><cfdi:Traslado Base="10000.00" Impuesto="002" TipoFactor="Tasa" TasaOCuota="0.160000" Importe="1600.00"/></cfdi:Traslados></cfdi:Impuestos></cfdi:Concepto></cfdi:Conceptos><cfdi:Impuestos TotalImpuestosTrasladados="1600.00"><cfdi:Traslados><cfdi:Traslado Base="10000.00" Impuesto="002" TipoFactor="Tasa" TasaOCuota="0.160000" Importe="1600.00"/></cfdi:Traslados></cfdi:Impuestos><cfdi:Complemento><tfd:TimbreFiscalDigital xmlns:tfd="http://www.sat.gob.mx/TimbreFiscalDigital" Version="1.1" UUID="F0000001-TEST-4B01-C001-000000000001" FechaTimbrado="2025-11-01T10:00:06" RfcProvCertif="SCD110105654" SelloCFD="TEST" NoCertificadoSAT="00001000000702501858" SelloSAT="TEST"/></cfdi:Complemento><cfdi:Addenda><s:SodimacAddenda xmlns:s="urn:sodimac:addenda"><s:IdProveedor>12345</s:IdProveedor><s:TipoProveedor>DIR</s:TipoProveedor><s:OrdenCompra>OC-TEST-2025-001</s:OrdenCompra><s:Recepcion>REC-TEST-2025-001</s:Recepcion></s:SodimacAddenda></cfdi:Addenda></cfdi:Comprobante>$XML_I$
WHERE status = 3
  AND document_type = 'I';

SELECT COUNT(*) AS total, SUM(CASE WHEN xml_content IS NOT NULL THEN 1 ELSE 0 END) AS con_xml
FROM tenant_fiscal.invoice
WHERE status = 3 AND document_type = 'I';


-- ============================================================
-- PASO 3: Facturas (tipo=I) - Agregar addendum si no existe
--         fiscal-api usa supplier_number para numeroProveedor
-- ============================================================
INSERT INTO tenant_fiscal.addendum (invoice_uuid, supplier_number, purchase_order_number, reception_number, supplier_type, addenda_type, created_by)
SELECT i.invoice_uuid, 12345, 'OC-TEST-2025-001', 'REC-TEST-2025-001', 'DIR', 1, 1
FROM tenant_fiscal.invoice i
WHERE i.status = 3
  AND i.document_type = 'I'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_fiscal.addendum a WHERE a.invoice_uuid = i.invoice_uuid
  );

SELECT COUNT(*) AS addenda_insertadas
FROM tenant_fiscal.addendum a
JOIN tenant_fiscal.invoice i ON i.invoice_uuid = a.invoice_uuid
WHERE i.status = 3 AND i.document_type = 'I';


-- ============================================================
-- PASO 4: Notas de Credito (tipo=E)
--         Resetear status=1 a status=3, asignar XML y created_at
--         Fuerza actualizacion en TODAS (status 1 o 3)
-- ============================================================
UPDATE tenant_fiscal.invoice
SET status      = 3,
    xml_content = $XML_E$<cfdi:Comprobante xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:cfdi="http://www.sat.gob.mx/cfd/4" xsi:schemaLocation="http://www.sat.gob.mx/cfd/4 http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd" Version="4.0" Serie="NC" Folio="000001" Fecha="2025-11-01T10:00:00" Sello="TEST" NoCertificado="00001000000707767690" SubTotal="1000.00" Moneda="MXN" Total="1160.00" TipoDeComprobante="E" MetodoPago="PUE" LugarExpedicion="06500" Exportacion="01" Certificado="TEST"><cfdi:CfdiRelacionados TipoRelacion="03"><cfdi:CfdiRelacionado UUID="F0000001-TEST-4B01-C001-000000000001"/></cfdi:CfdiRelacionados><cfdi:Emisor Rfc="HTR060210FJ9" Nombre="H.H. TRANSPORTES" RegimenFiscal="624"/><cfdi:Receptor Rfc="CSD161207R2A" Nombre="COMERCIALIZADORA SDMHC" UsoCFDI="G02" DomicilioFiscalReceptor="53150" RegimenFiscalReceptor="601"/><cfdi:Conceptos><cfdi:Concepto ClaveProdServ="78101802" NoIdentificacion="TEST-NC-001" Cantidad="1.000" ClaveUnidad="E48" Unidad="Servicio" Descripcion="NOTA DE CREDITO PRUEBA" ValorUnitario="1000.00" Importe="1000.00" ObjetoImp="02"><cfdi:Impuestos><cfdi:Traslados><cfdi:Traslado Base="1000.00" Impuesto="002" TipoFactor="Tasa" TasaOCuota="0.160000" Importe="160.00"/></cfdi:Traslados></cfdi:Impuestos></cfdi:Concepto></cfdi:Conceptos><cfdi:Impuestos TotalImpuestosTrasladados="160.00"><cfdi:Traslados><cfdi:Traslado Impuesto="002" TipoFactor="Tasa" TasaOCuota="0.160000" Importe="160.00" Base="1000.00"/></cfdi:Traslados></cfdi:Impuestos><cfdi:Complemento><tfd:TimbreFiscalDigital xmlns:tfd="http://www.sat.gob.mx/TimbreFiscalDigital" Version="1.1" UUID="E0000001-TEST-4E01-D001-000000000001" FechaTimbrado="2025-11-01T10:00:06" RfcProvCertif="SCD110105654" SelloCFD="TEST" NoCertificadoSAT="00001000000702501858" SelloSAT="TEST"/></cfdi:Complemento><cfdi:Addenda><s:SodimacAddenda xmlns:s="urn:sodimac:addenda"><s:IdProveedor>12345</s:IdProveedor><s:TipoProveedor>DIR</s:TipoProveedor><s:TipoNC>DEV</s:TipoNC></s:SodimacAddenda></cfdi:Addenda></cfdi:Comprobante>$XML_E$,
    created_at  = NOW()
WHERE document_type = 'E'
  AND status IN (1, 3, 4);

SELECT COUNT(*), MIN(created_at), MAX(created_at)
FROM tenant_fiscal.invoice
WHERE status = 3 AND document_type = 'E';


-- ============================================================
-- PASO 5: Notas de Credito (tipo=E) - Agregar addendum si no existe
-- ============================================================
INSERT INTO tenant_fiscal.addendum (invoice_uuid, supplier_number, supplier_type, addenda_type, created_by)
SELECT i.invoice_uuid, 12345, 'DIR', 1, 1
FROM tenant_fiscal.invoice i
WHERE i.status = 3
  AND i.document_type = 'E'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_fiscal.addendum a WHERE a.invoice_uuid = i.invoice_uuid
  );

SELECT COUNT(*) AS addenda_nc
FROM tenant_fiscal.addendum a
JOIN tenant_fiscal.invoice i ON i.invoice_uuid = a.invoice_uuid
WHERE i.status = 3 AND i.document_type = 'E';


-- ============================================================
-- POST: Estado final
-- ============================================================
SELECT document_type, status, COUNT(*),
       SUM(CASE WHEN xml_content IS NOT NULL THEN 1 ELSE 0 END) AS con_xml,
       MIN(created_at) AS mas_antigua,
       MAX(created_at) AS mas_reciente
FROM tenant_fiscal.invoice
WHERE status = 3
  AND document_type IN ('I', 'E')
GROUP BY document_type, status
ORDER BY document_type;
