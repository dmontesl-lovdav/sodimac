-- =============================================================================
-- STM-338: Consultas de Validacion para busqueda por OC y Recepcion
-- Proyecto: fiscal-api
-- Fecha: 2025-12-10
-- =============================================================================

-- -----------------------------------------------------------------------------
-- CONFIGURACION: Modificar estos valores segun los datos a buscar
-- -----------------------------------------------------------------------------
-- @NO_OC = 'OC-2025-001234'
-- @NO_RECEPCION = 'REC-2025-005678'
-- @RFC_EMISOR = 'AAA010101AAA'

-- =============================================================================
-- 1. BUSCAR FACTURAS POR ORDEN DE COMPRA
-- =============================================================================
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    iss.name AS emisor_nombre,
    a.purchase_order_number AS no_orden_compra,
    a.reception_number AS no_recepcion,
    a.supplier_number AS numero_proveedor,
    i.created_at AS fecha_recepcion
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE a.purchase_order_number = 'OC-2025-001234'  -- <-- Modificar OC aqui
    AND i.document_type = 'I'
ORDER BY i.created_at DESC;


-- =============================================================================
-- 2. BUSCAR FACTURAS POR NUMERO DE RECEPCION
-- =============================================================================
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    a.purchase_order_number AS no_orden_compra,
    a.reception_number AS no_recepcion,
    i.created_at AS fecha_recepcion
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE a.reception_number = 'REC-2025-005678'  -- <-- Modificar Recepcion aqui
    AND i.document_type = 'I'
ORDER BY i.created_at DESC;


-- =============================================================================
-- 3. BUSCAR NOTAS DE CREDITO POR OC Y RECEPCION
-- =============================================================================
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    a.purchase_order_number AS no_orden_compra,
    a.reception_number AS no_recepcion,
    i.created_at AS fecha_recepcion
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE a.purchase_order_number = 'OC-2025-001234'  -- <-- Modificar OC aqui
    AND a.reception_number = 'REC-2025-005678'    -- <-- Modificar Recepcion aqui
    AND i.document_type = 'E'
ORDER BY i.created_at DESC;


-- =============================================================================
-- 4. VER TODAS LAS OC REGISTRADAS EN EL SISTEMA
-- =============================================================================
SELECT DISTINCT
    a.purchase_order_number AS no_orden_compra,
    COUNT(i.invoice_uuid) AS cantidad_documentos,
    SUM(CASE WHEN i.document_type = 'I' THEN 1 ELSE 0 END) AS facturas,
    SUM(CASE WHEN i.document_type = 'E' THEN 1 ELSE 0 END) AS notas_credito
FROM tenant_fiscal.addendum a
INNER JOIN tenant_fiscal.invoice i ON a.invoice_uuid = i.invoice_uuid
WHERE a.purchase_order_number IS NOT NULL
GROUP BY a.purchase_order_number
ORDER BY cantidad_documentos DESC
LIMIT 50;


-- =============================================================================
-- 5. VER TODAS LAS RECEPCIONES REGISTRADAS EN EL SISTEMA
-- =============================================================================
SELECT DISTINCT
    a.reception_number AS no_recepcion,
    COUNT(i.invoice_uuid) AS cantidad_documentos,
    SUM(CASE WHEN i.document_type = 'I' THEN 1 ELSE 0 END) AS facturas,
    SUM(CASE WHEN i.document_type = 'E' THEN 1 ELSE 0 END) AS notas_credito
FROM tenant_fiscal.addendum a
INNER JOIN tenant_fiscal.invoice i ON a.invoice_uuid = i.invoice_uuid
WHERE a.reception_number IS NOT NULL
GROUP BY a.reception_number
ORDER BY cantidad_documentos DESC
LIMIT 50;


-- =============================================================================
-- 6. BUSCAR DOCUMENTOS DE UN EMISOR CON DATOS DE ADDENDA
-- =============================================================================
SELECT
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    a.purchase_order_number AS no_oc,
    a.reception_number AS no_recepcion,
    a.supplier_number AS id_proveedor,
    a.supplier_type AS tipo_proveedor,
    i.created_at AS fecha_recepcion
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE iss.rfc = 'AAA010101AAA'  -- <-- Modificar RFC aqui
    AND i.created_at >= '2025-01-01'
    AND i.created_at <= '2025-12-31 23:59:59'
ORDER BY i.created_at DESC
LIMIT 100;


-- =============================================================================
-- 7. VERIFICAR DOCUMENTOS SIN ADDENDA (no tendran OC ni Recepcion)
-- =============================================================================
SELECT
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    i.created_at AS fecha_recepcion
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE a.addendum_uuid IS NULL
ORDER BY i.created_at DESC
LIMIT 50;


-- =============================================================================
-- 8. RESUMEN DE OC POR EMISOR
-- =============================================================================
SELECT
    iss.rfc AS emisor_rfc,
    iss.name AS emisor_nombre,
    a.purchase_order_number AS no_oc,
    COUNT(i.invoice_uuid) AS documentos,
    SUM(i.total) AS total_facturado
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE a.purchase_order_number IS NOT NULL
GROUP BY iss.rfc, iss.name, a.purchase_order_number
ORDER BY total_facturado DESC
LIMIT 50;
