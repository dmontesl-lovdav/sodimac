-- =============================================================================
-- STM-335: Consultas de Validacion para relacion Factura-NC y cancelacion
-- Proyecto: fiscal-api
-- Fecha: 2026-03-11
-- BD: b2b_portal (PostgreSQL 16, puerto 5434)
-- =============================================================================

-- =============================================================================
-- 1. FACTURAS CON CONTEO DE NCs RELACIONADAS (Tarea 1)
-- Valida que creditNotesCount coincida con la API
-- =============================================================================
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    (SELECT COUNT(*)
     FROM tenant_fiscal.related_cfdi rc
     WHERE rc.related_invoice_uuid = i.invoice_uuid) AS credit_notes_count
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
WHERE i.document_type = 'I'
ORDER BY credit_notes_count DESC, i.created_at DESC;


-- =============================================================================
-- 2. NCs RELACIONADAS A UNA FACTURA ESPECIFICA (Tarea 2)
-- Simula el filtro relatedInvoiceUuid del search
-- =============================================================================
-- Ejemplo: NCs de la factura f0000010-0010-4010-a010-000000000010
SELECT
    nc.invoice_uuid AS nc_uuid,
    nc.fiscal_uuid AS nc_fiscal_uuid,
    nc.series AS nc_serie,
    nc.folio AS nc_folio,
    nc.total AS nc_total,
    nc.status AS nc_estatus,
    rc.relation_type,
    fac.fiscal_uuid AS factura_fiscal_uuid,
    fac.series AS factura_serie,
    fac.folio AS factura_folio,
    fac.total AS factura_total
FROM tenant_fiscal.related_cfdi rc
JOIN tenant_fiscal.invoice nc ON rc.invoice_uuid = nc.invoice_uuid
JOIN tenant_fiscal.invoice fac ON rc.related_invoice_uuid = fac.invoice_uuid
WHERE rc.related_invoice_uuid = 'f0000010-0010-4010-a010-000000000010'
ORDER BY nc.created_at DESC;


-- =============================================================================
-- 3. TODAS LAS RELACIONES FACTURA-NC EN EL SISTEMA
-- =============================================================================
SELECT
    fac.invoice_uuid AS factura_uuid,
    fac.fiscal_uuid AS factura_fiscal_uuid,
    fac.series || '/' || fac.folio AS factura,
    fac.total AS factura_total,
    nc.invoice_uuid AS nc_uuid,
    nc.fiscal_uuid AS nc_fiscal_uuid,
    nc.series || '/' || nc.folio AS nota_credito,
    nc.total AS nc_total,
    nc.status AS nc_estatus,
    rc.relation_type
FROM tenant_fiscal.related_cfdi rc
JOIN tenant_fiscal.invoice nc ON rc.invoice_uuid = nc.invoice_uuid
JOIN tenant_fiscal.invoice fac ON rc.related_invoice_uuid = fac.invoice_uuid
ORDER BY fac.invoice_uuid, nc.created_at;


-- =============================================================================
-- 4. NCs POR ESTATUS (Tarea 3 - Cancelacion)
-- Muestra distribucion de estatus para validar cancelacion
-- =============================================================================
SELECT
    i.status AS codigo_estatus,
    CASE i.status
        WHEN 0 THEN 'Rechazo Comercial'
        WHEN 1 THEN 'Pendiente Addenda'
        WHEN 2 THEN 'Recibido Parcial'
        WHEN 3 THEN 'Pendiente de Contabilizar'
        WHEN 4 THEN 'Proceso de descarga'
        WHEN 5 THEN 'Desglose de NC'
        WHEN 6 THEN 'Pendiente de envio a contabilizar'
        WHEN 7 THEN 'Aplicado'
        WHEN 8 THEN 'Completado'
        WHEN 10 THEN 'Cancelada'
        WHEN 11 THEN 'Rechazo Contable'
        WHEN 12 THEN 'No valido fiscal'
    END AS nombre_estatus,
    CASE
        WHEN i.status IN (3, 11) THEN 'SI - Puede cancelarse'
        ELSE 'NO - No puede cancelarse'
    END AS puede_cancelar,
    COUNT(*) AS cantidad
FROM tenant_fiscal.invoice i
WHERE i.document_type = 'E'
GROUP BY i.status
ORDER BY i.status;


-- =============================================================================
-- 5. NCs CANDIDATAS A CANCELACION (estatus 3 o 11)
-- =============================================================================
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    a.supplier_number AS numero_proveedor
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'E'
AND i.status IN (3, 11)
ORDER BY i.status, i.created_at DESC;


-- =============================================================================
-- 6. NCs QUE NO PUEDEN CANCELARSE (para probar WRN7023)
-- =============================================================================
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series AS serie,
    i.folio,
    i.total,
    i.status,
    CASE i.status
        WHEN 7 THEN 'Aplicado - ya tiene afectacion contable'
        WHEN 8 THEN 'Completado - ya tiene afectacion contable'
        WHEN 10 THEN 'Ya esta Cancelada'
        ELSE 'Estatus ' || i.status
    END AS razon_no_cancelable,
    iss.rfc AS emisor_rfc,
    a.supplier_number AS numero_proveedor
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'E'
AND i.status NOT IN (3, 11)
ORDER BY i.status;


-- =============================================================================
-- 7. TREN DE ESTATUS PARA NCs (option_id=2)
-- Verifica que las transiciones 3->10 y 11->10 existan
-- =============================================================================
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


-- =============================================================================
-- 8. VERIFICAR POST-CANCELACION
-- Ejecutar despues de cancelar una NC para confirmar el cambio
-- =============================================================================
-- Reemplazar el UUID con el de la NC cancelada
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.status,
    i.updated_at,
    i.updated_by
FROM tenant_fiscal.invoice i
WHERE i.invoice_uuid = 'e0000001-0001-4001-e001-000000000001';  -- <-- Modificar UUID
