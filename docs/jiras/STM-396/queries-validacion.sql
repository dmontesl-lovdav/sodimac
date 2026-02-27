-- ============================================================================
-- STM-396: Consultas de validacion para endpoints de Notas de Credito
-- Base de datos: fiscal-api (PostgreSQL)
-- ============================================================================

-- ============================================================================
-- 1. OBTENER DATOS PARA PROBAR ENDPOINTS
-- ============================================================================

-- 1.1 Obtener facturas con XML para probar descarga ZIP
-- Retorna invoice_uuid que se pueden usar en /invoices/download/xml
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type,
    i.series,
    i.folio,
    i.issue_date,
    e.rfc AS rfc_emisor,
    e.name AS nombre_emisor,
    i.total,
    i.status,
    CASE WHEN i.xml_content IS NOT NULL THEN 'SI' ELSE 'NO' END AS tiene_xml,
    LENGTH(i.xml_content) AS tamano_xml
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer e ON i.issuer_uuid = e.issuer_uuid
WHERE i.xml_content IS NOT NULL
  AND i.document_type = 'I'
ORDER BY i.created_at DESC
LIMIT 10;

-- 1.2 Obtener Notas de Credito con datos de factura relacionada
-- Para probar el endpoint /invoices/search con tipoDocumento='E'
SELECT
    nc.invoice_uuid AS nc_uuid,
    nc.fiscal_uuid AS nc_fiscal_uuid,
    nc.series AS nc_serie,
    nc.folio AS nc_folio,
    nc.total AS nc_total,
    nc.status AS nc_status,
    -- Factura relacionada
    rel.relation_type AS tipo_relacion,
    fac.fiscal_uuid AS factura_uuid,
    fac.series AS factura_serie,
    fac.folio AS factura_folio,
    fac.subtotal AS factura_subtotal,
    fac.total AS factura_total,
    -- Emisor
    e.rfc AS rfc_emisor,
    e.name AS nombre_emisor
FROM tenant_fiscal.invoice nc
JOIN tenant_fiscal.issuer e ON nc.issuer_uuid = e.issuer_uuid
LEFT JOIN tenant_fiscal.related_cfdi rel ON nc.invoice_uuid = rel.invoice_uuid
LEFT JOIN tenant_fiscal.invoice fac ON rel.related_invoice_uuid = fac.invoice_uuid
WHERE nc.document_type = 'E'
ORDER BY nc.created_at DESC
LIMIT 20;

-- 1.3 Obtener facturas con NC relacionadas
-- Para probar el endpoint /invoices/search con tipoDocumento='I' y ver notasCreditoRelacionadas
SELECT
    fac.invoice_uuid AS factura_uuid,
    fac.fiscal_uuid AS factura_fiscal_uuid,
    fac.series AS factura_serie,
    fac.folio AS factura_folio,
    fac.total AS factura_total,
    fac.status AS factura_status,
    -- NCs relacionadas
    COUNT(nc.invoice_uuid) AS cantidad_nc,
    STRING_AGG(nc.fiscal_uuid::text, ', ') AS nc_uuids,
    SUM(nc.total) AS total_nc
FROM tenant_fiscal.invoice fac
JOIN tenant_fiscal.issuer e ON fac.issuer_uuid = e.issuer_uuid
LEFT JOIN tenant_fiscal.related_cfdi rel ON fac.invoice_uuid = rel.related_invoice_uuid
LEFT JOIN tenant_fiscal.invoice nc ON rel.invoice_uuid = nc.invoice_uuid AND nc.document_type = 'E'
WHERE fac.document_type = 'I'
GROUP BY fac.invoice_uuid, fac.fiscal_uuid, fac.series, fac.folio, fac.total, fac.status
HAVING COUNT(nc.invoice_uuid) > 0
ORDER BY COUNT(nc.invoice_uuid) DESC
LIMIT 10;

-- ============================================================================
-- 2. DATOS PARA VARIABLES DE POSTMAN
-- ============================================================================

-- 2.1 Obtener RFCs de emisores disponibles
SELECT DISTINCT
    e.rfc AS rfc_emisor,
    e.name AS nombre_emisor,
    COUNT(i.invoice_uuid) AS total_documentos,
    MIN(i.created_at)::date AS fecha_inicio,
    MAX(i.created_at)::date AS fecha_final
FROM tenant_fiscal.issuer e
JOIN tenant_fiscal.invoice i ON e.issuer_uuid = i.issuer_uuid
GROUP BY e.rfc, e.name
ORDER BY total_documentos DESC
LIMIT 10;

-- 2.2 Obtener rango de fechas con datos
SELECT
    MIN(created_at)::date AS fecha_minima,
    MAX(created_at)::date AS fecha_maxima,
    COUNT(*) AS total_registros
FROM tenant_fiscal.invoice;

-- ============================================================================
-- 3. CONSULTAS ESPECIFICAS PARA CADA ENDPOINT
-- ============================================================================

-- 3.1 Para POST /invoices/download/xml
-- Obtener UUIDs con XML para el request
SELECT
    invoice_uuid,
    fiscal_uuid,
    series || '-' || folio AS serie_folio,
    document_type
FROM tenant_fiscal.invoice
WHERE xml_content IS NOT NULL
ORDER BY created_at DESC
LIMIT 5;

-- Ejemplo de request body:
-- {
--   "invoiceUuids": [
--     "uuid-1-aqui",
--     "uuid-2-aqui",
--     "uuid-3-aqui"
--   ]
-- }

-- 3.2 Para POST /invoices/search (Notas de Credito)
-- Obtener filtros validos para buscar NC
SELECT
    e.rfc AS "rfcEmisor",
    MIN(nc.created_at)::date AS "fechaInicioRecepcion",
    MAX(nc.created_at)::date AS "fechaFinalRecepcion",
    'E' AS "tipoDocumento",
    COUNT(*) AS total_nc
FROM tenant_fiscal.invoice nc
JOIN tenant_fiscal.issuer e ON nc.issuer_uuid = e.issuer_uuid
WHERE nc.document_type = 'E'
GROUP BY e.rfc
HAVING COUNT(*) > 0
ORDER BY total_nc DESC
LIMIT 5;

-- 3.3 Para POST /invoices/export/csv
-- Verificar datos que se exportaran
SELECT
    i.fiscal_uuid,
    i.document_type,
    i.series,
    i.folio,
    i.issue_date,
    e.rfc AS emisor_rfc,
    e.name AS emisor_nombre,
    r.rfc AS receptor_rfc,
    i.subtotal,
    i.total,
    i.currency,
    i.status,
    a.purchase_order_number AS orden_compra,
    a.reception_number AS recepcion
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer e ON i.issuer_uuid = e.issuer_uuid
LEFT JOIN tenant_fiscal.receiver r ON i.receiver_uuid = r.receiver_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'E'
ORDER BY i.created_at DESC
LIMIT 10;

-- ============================================================================
-- 4. VALIDACION DE CATALOGOS (catalogos-api)
-- ============================================================================

-- 4.1 Verificar TipoRelacion en la base de datos de catalogos
-- Ejecutar en la BD de catalogos-api
SELECT
    ch.code AS catalogo,
    cd.key,
    cd.external_key AS codigo_sat,
    dl.description AS descripcion_es
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON ch.id = cd.header_id
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE ch.code = 'c_TipoRelacion'
ORDER BY cd.sort_order;

-- 4.2 Verificar TipoAddenda
SELECT
    ch.code AS catalogo,
    cd.key,
    cd.external_key AS codigo,
    dl.description AS descripcion_es
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON ch.id = cd.header_id
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE ch.code = 'TipoAddenda'
ORDER BY cd.sort_order;

-- ============================================================================
-- 5. ESTADISTICAS GENERALES
-- ============================================================================

-- 5.1 Resumen de documentos por tipo
SELECT
    document_type,
    CASE document_type
        WHEN 'I' THEN 'Factura'
        WHEN 'E' THEN 'Nota de Credito'
        ELSE 'Otro'
    END AS tipo_nombre,
    COUNT(*) AS cantidad,
    SUM(CASE WHEN xml_content IS NOT NULL THEN 1 ELSE 0 END) AS con_xml,
    SUM(total) AS monto_total
FROM tenant_fiscal.invoice
GROUP BY document_type
ORDER BY cantidad DESC;

-- 5.2 Documentos por estatus
SELECT
    document_type,
    status,
    COUNT(*) AS cantidad
FROM tenant_fiscal.invoice
GROUP BY document_type, status
ORDER BY document_type, status;

-- 5.3 Relaciones NC-Factura
SELECT
    rel.relation_type,
    COUNT(*) AS cantidad_relaciones
FROM tenant_fiscal.related_cfdi rel
JOIN tenant_fiscal.invoice nc ON rel.invoice_uuid = nc.invoice_uuid
WHERE nc.document_type = 'E'
GROUP BY rel.relation_type
ORDER BY cantidad_relaciones DESC;
