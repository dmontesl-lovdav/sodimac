-- ============================================================================
-- STM-1258: Consultas de validacion para Facturacion y Notas de Credito
-- Base de datos: fiscal-api (PostgreSQL)
-- Schema: tenant_fiscal
-- ============================================================================

-- ============================================================================
-- 1. OBTENER DATOS PARA PROBAR ENDPOINTS
-- ============================================================================

-- 1.1 Obtener RFCs de Emisores disponibles
-- Usar el RFC en el campo "rfcEmisor" del request
SELECT DISTINCT i.rfc, i.name, COUNT(*) as total_facturas
FROM tenant_fiscal.issuer i
INNER JOIN tenant_fiscal.invoice inv ON i.issuer_uuid = inv.issuer_uuid
GROUP BY i.rfc, i.name
ORDER BY total_facturas DESC
LIMIT 10;

-- 1.2 Obtener Facturas para probar busqueda
-- Usar estos datos para construir el request de POST /invoices/search
SELECT
    inv.invoice_uuid,
    inv.fiscal_uuid,
    inv.document_type,
    inv.series,
    inv.folio,
    inv.total,
    inv.status,
    inv.created_at,
    iss.rfc as emisor_rfc,
    iss.name as emisor_name,
    rec.rfc as receptor_rfc,
    add.supplier_number as numero_proveedor
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.receiver rec ON inv.receiver_uuid = rec.receiver_uuid
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.document_type = 'I'
ORDER BY inv.created_at DESC
LIMIT 10;

-- 1.3 Obtener Notas de Credito para probar busqueda
SELECT
    inv.invoice_uuid,
    inv.fiscal_uuid,
    inv.document_type,
    inv.series,
    inv.folio,
    inv.total,
    inv.status,
    inv.created_at,
    iss.rfc as emisor_rfc,
    add.supplier_number as numero_proveedor
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.document_type = 'E'
ORDER BY inv.created_at DESC
LIMIT 10;

-- ============================================================================
-- 2. DATOS PARA ACTUALIZAR FACTURAS (PUT /invoices)
-- ============================================================================

-- 2.1 Facturas que se pueden actualizar (no completadas/cerradas)
-- Usar fiscal_uuid y supplier_number en el request de actualizacion
SELECT
    inv.fiscal_uuid,
    inv.document_type,
    inv.series,
    inv.folio,
    inv.status,
    add.supplier_number as numero_proveedor,
    add.purchase_order_number as no_oc,
    add.reception_number as no_recepcion
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.status NOT IN (8, 12, 13)  -- No completadas, no invalidas, no cerradas
  AND inv.document_type = 'I'
ORDER BY inv.created_at DESC
LIMIT 10;

-- 2.2 Notas de Credito que se pueden actualizar
SELECT
    inv.fiscal_uuid,
    inv.document_type,
    inv.series,
    inv.folio,
    inv.status,
    add.supplier_number as numero_proveedor
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.status NOT IN (8, 12, 13)
  AND inv.document_type = 'E'
ORDER BY inv.created_at DESC
LIMIT 10;

-- ============================================================================
-- 3. VERIFICAR RANGO DE FECHAS
-- ============================================================================

-- 3.1 Ver rango de fechas con facturas disponibles
SELECT
    MIN(created_at) as fecha_mas_antigua,
    MAX(created_at) as fecha_mas_reciente,
    COUNT(*) as total_registros
FROM tenant_fiscal.invoice;

-- 3.2 Conteo por tipo de documento
SELECT
    document_type,
    CASE document_type
        WHEN 'I' THEN 'Factura'
        WHEN 'E' THEN 'Nota de Credito'
        ELSE 'Otro'
    END AS tipo_nombre,
    COUNT(*) AS cantidad
FROM tenant_fiscal.invoice
GROUP BY document_type
ORDER BY cantidad DESC;

-- ============================================================================
-- 4. OBTENER FACTURAS POR RFC ESPECIFICO
-- ============================================================================

-- 4.1 Facturas de un emisor especifico
-- Reemplazar 'SOD970101ABC' con el RFC real
SELECT
    inv.fiscal_uuid,
    inv.series,
    inv.folio,
    inv.total,
    inv.status,
    DATE(inv.created_at) as fecha
FROM tenant_fiscal.invoice inv
INNER JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
WHERE iss.rfc = 'SOD970101ABC'
  AND inv.document_type = 'I'
ORDER BY inv.created_at DESC
LIMIT 20;

-- ============================================================================
-- 5. VERIFICAR ESTRUCTURA DE ADDENDA
-- ============================================================================

-- 5.1 Ver estructura de addendas disponibles
SELECT
    inv.fiscal_uuid,
    inv.document_type,
    add.addenda_type,
    add.supplier_number,
    add.purchase_order_number,
    add.reception_number,
    add.shipping_guide_number
FROM tenant_fiscal.addendum add
INNER JOIN tenant_fiscal.invoice inv ON add.invoice_uuid = inv.invoice_uuid
WHERE add.supplier_number IS NOT NULL
LIMIT 10;

-- ============================================================================
-- 6. FACTURAS CON NC RELACIONADAS
-- ============================================================================

-- 6.1 Facturas que tienen Notas de Credito relacionadas
SELECT
    fac.invoice_uuid AS factura_uuid,
    fac.fiscal_uuid AS factura_fiscal_uuid,
    fac.series AS factura_serie,
    fac.folio AS factura_folio,
    fac.total AS factura_total,
    fac.status AS factura_status,
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

-- 6.2 Notas de Credito con datos de factura relacionada
SELECT
    nc.invoice_uuid AS nc_uuid,
    nc.fiscal_uuid AS nc_fiscal_uuid,
    nc.series AS nc_serie,
    nc.folio AS nc_folio,
    nc.total AS nc_total,
    nc.status AS nc_status,
    rel.relation_type AS tipo_relacion,
    fac.fiscal_uuid AS factura_uuid,
    fac.series AS factura_serie,
    fac.folio AS factura_folio,
    fac.total AS factura_total,
    e.rfc AS rfc_emisor
FROM tenant_fiscal.invoice nc
JOIN tenant_fiscal.issuer e ON nc.issuer_uuid = e.issuer_uuid
LEFT JOIN tenant_fiscal.related_cfdi rel ON nc.invoice_uuid = rel.invoice_uuid
LEFT JOIN tenant_fiscal.invoice fac ON rel.related_invoice_uuid = fac.invoice_uuid
WHERE nc.document_type = 'E'
ORDER BY nc.created_at DESC
LIMIT 20;

-- ============================================================================
-- 7. ESTADISTICAS POR ESTATUS
-- ============================================================================

-- 7.1 Documentos por estatus (para saber que estatus actualizar)
SELECT
    document_type,
    status,
    CASE status
        WHEN 0 THEN 'Rechazo Comercial'
        WHEN 1 THEN 'Pendiente Addenda'
        WHEN 2 THEN 'Recibido Parcial'
        WHEN 3 THEN 'Pendiente Contabilizar'
        WHEN 4 THEN 'Proceso Descarga'
        WHEN 5 THEN 'Desglose'
        WHEN 6 THEN 'Pendiente Envio'
        WHEN 7 THEN 'Aplicado'
        WHEN 8 THEN 'Completado'
        WHEN 11 THEN 'Rechazo Contable'
        WHEN 12 THEN 'No valido fiscal'
        WHEN 13 THEN 'Cerrado'
        ELSE 'Desconocido'
    END AS status_nombre,
    COUNT(*) AS cantidad
FROM tenant_fiscal.invoice
GROUP BY document_type, status
ORDER BY document_type, status;

-- ============================================================================
-- 8. DATOS PARA VARIABLES DE POSTMAN
-- ============================================================================

-- 8.1 Obtener todos los datos necesarios para configurar Postman
SELECT
    iss.rfc AS rfc_emisor,
    inv.fiscal_uuid,
    add.supplier_number AS id_proveedor,
    inv.status,
    inv.document_type,
    MIN(inv.created_at)::date AS fecha_inicio,
    MAX(inv.created_at)::date AS fecha_final
FROM tenant_fiscal.invoice inv
JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE add.supplier_number IS NOT NULL
GROUP BY iss.rfc, inv.fiscal_uuid, add.supplier_number, inv.status, inv.document_type
ORDER BY MAX(inv.created_at) DESC
LIMIT 5;
