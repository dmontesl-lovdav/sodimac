-- ===============================
-- CONSULTAS DE IMPUESTOS
-- Consultas útiles para verificar los impuestos guardados
-- ===============================

-- ===============================
-- 1. Ver todos los impuestos de una factura (por fiscal_uuid)
-- ===============================
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.document_type,
    i.total AS invoice_total,
    t.total_transferred_taxes,
    t.total_withheld_taxes,
    (COALESCE(i.subtotal, 0) - COALESCE(i.discount, 0) + COALESCE(t.total_transferred_taxes, 0) - COALESCE(t.total_withheld_taxes, 0)) AS calculated_total
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
WHERE i.fiscal_uuid = 'a12b2040-d8f8-4fce-ab9d-37a636f8e59e';

-- ===============================
-- 2. Ver detalle de impuestos trasladados (IVA, IEPS)
-- ===============================
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    tt.tax_code,
    CASE tt.tax_code
        WHEN '001' THEN 'ISR'
        WHEN '002' THEN 'IVA'
        WHEN '003' THEN 'IEPS'
        ELSE 'Otro'
    END AS tax_name,
    tt.factor_type,
    tt.base,
    tt.rate_or_quota,
    tt.amount
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
JOIN tenant_fiscal.tax_transfer tt ON t.tax_uuid = tt.tax_uuid
WHERE i.fiscal_uuid = 'a12b2040-d8f8-4fce-ab9d-37a636f8e59e'
ORDER BY tt.tax_code;

-- ===============================
-- 3. Ver detalle de impuestos retenidos (ISR, IVA)
-- ===============================
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    tw.tax_code,
    CASE tw.tax_code
        WHEN '001' THEN 'ISR'
        WHEN '002' THEN 'IVA'
        ELSE 'Otro'
    END AS tax_name,
    tw.amount
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
JOIN tenant_fiscal.tax_withholding tw ON t.tax_uuid = tw.tax_uuid
WHERE i.fiscal_uuid = 'a12b2040-d8f8-4fce-ab9d-37a636f8e59e'
ORDER BY tw.tax_code;

-- ===============================
-- 4. Validar cálculo de total con impuestos
-- ===============================
-- Verifica que: total = subtotal - descuento + impuestos_trasladados - impuestos_retenidos
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.subtotal,
    i.discount,
    t.total_transferred_taxes,
    t.total_withheld_taxes,
    i.total AS total_invoice,
    (i.subtotal - COALESCE(i.discount, 0) + COALESCE(t.total_transferred_taxes, 0) - COALESCE(t.total_withheld_taxes, 0)) AS total_calculated,
    CASE
        WHEN ABS(i.total - (i.subtotal - COALESCE(i.discount, 0) + COALESCE(t.total_transferred_taxes, 0) - COALESCE(t.total_withheld_taxes, 0))) < 0.01
        THEN 'OK'
        ELSE 'ERROR - Totales no coinciden'
    END AS validation
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
WHERE i.fiscal_uuid = 'a12b2040-d8f8-4fce-ab9d-37a636f8e59e';

-- ===============================
-- 5. Resumen de impuestos por tipo de documento
-- ===============================
SELECT
    i.document_type,
    CASE i.document_type
        WHEN 'I' THEN 'Factura (Ingreso)'
        WHEN 'E' THEN 'Nota de Crédito (Egreso)'
        ELSE 'Otro'
    END AS document_type_name,
    COUNT(*) AS total_invoices,
    SUM(COALESCE(t.total_transferred_taxes, 0)) AS total_transferred,
    SUM(COALESCE(t.total_withheld_taxes, 0)) AS total_withheld
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
GROUP BY i.document_type
ORDER BY i.document_type;

-- ===============================
-- 6. Ver todos los impuestos de la última factura registrada
-- ===============================
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.document_type,
    i.total,
    i.created_at,
    t.total_transferred_taxes,
    t.total_withheld_taxes,

    -- Contar detalles
    (SELECT COUNT(*) FROM tenant_fiscal.tax_transfer tt WHERE tt.tax_uuid = t.tax_uuid) AS num_transfers,
    (SELECT COUNT(*) FROM tenant_fiscal.tax_withholding tw WHERE tw.tax_uuid = t.tax_uuid) AS num_withholdings
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
ORDER BY i.created_at DESC
LIMIT 1;

-- ===============================
-- 7. Desglose completo de impuestos para una factura específica
-- ===============================
-- Impuestos trasladados
SELECT
    'TRASLADO' AS tipo,
    tt.tax_code AS codigo,
    CASE tt.tax_code
        WHEN '001' THEN 'ISR'
        WHEN '002' THEN 'IVA'
        WHEN '003' THEN 'IEPS'
    END AS nombre,
    tt.factor_type AS tipo_factor,
    tt.base,
    tt.rate_or_quota AS tasa,
    tt.amount AS importe
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
JOIN tenant_fiscal.tax_transfer tt ON t.tax_uuid = tt.tax_uuid
WHERE i.fiscal_uuid = 'a12b2040-d8f8-4fce-ab9d-37a636f8e59e'

UNION ALL

-- Impuestos retenidos
SELECT
    'RETENCION' AS tipo,
    tw.tax_code AS codigo,
    CASE tw.tax_code
        WHEN '001' THEN 'ISR'
        WHEN '002' THEN 'IVA'
    END AS nombre,
    NULL AS tipo_factor,
    NULL AS base,
    NULL AS tasa,
    tw.amount AS importe
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
JOIN tenant_fiscal.tax_withholding tw ON t.tax_uuid = tw.tax_uuid
WHERE i.fiscal_uuid = 'a12b2040-d8f8-4fce-ab9d-37a636f8e59e'

ORDER BY tipo, codigo;



Consultas para validar en PostgreSQL:
-- 1. Verificar el registro en stamped_rebate:
SELECT 
    stamped_rebate_uuid,
    document_number,
    reference_number,
    invoice_fiscal_uuid,
    status,
    created_by,
    created_at
FROM tenant_finance.stamped_rebate
WHERE document_number = 'REB-FINAL-TEST-001';
-- 2. Verificar el registro en rebate:
SELECT 
    rebate_uuid,
    document_number,
    reference_number,
    sap_document,
    vendor_number,
    amount,
    source,
    period_id,
    status,
    created_by,
    created_at
FROM tenant_finance.rebate
WHERE document_number = 'REB-FINAL-TEST-001';
-- 3. Ver todos los rebates creados en esta sesión:
SELECT 
    sr.stamped_rebate_uuid,
    sr.document_number,
    sr.reference_number,
    sr.invoice_fiscal_uuid,
    r.rebate_uuid,
    r.vendor_number,
    r.amount,
    sr.created_at
FROM tenant_finance.stamped_rebate sr
LEFT JOIN tenant_finance.rebate r ON sr.document_number = r.document_number
WHERE sr.document_number LIKE 'REB-%' OR sr.document_number LIKE 'DOC-TEST-%'
ORDER BY sr.created_at DESC;

--4. Validar la relación entre stamped_rebate y rebate:
SELECT 
    sr.stamped_rebate_uuid,
    sr.document_number AS sr_document,
    sr.invoice_fiscal_uuid,
    r.rebate_uuid,
    r.document_number AS r_document,
    r.vendor_number
FROM tenant_finance.stamped_rebate sr
INNER JOIN tenant_finance.rebate r 
    ON sr.document_number = r.document_number
WHERE sr.stamped_rebate_uuid = '8addb625-a789-46c3-9223-bf5ee1c9b339';

--5. Verificar el último rebate creado:
SELECT 
    sr.stamped_rebate_uuid,
    sr.document_number,
    sr.reference_number,
    sr.invoice_fiscal_uuid,
    r.rebate_uuid,
    r.vendor_number,
    sr.created_at
FROM tenant_finance.stamped_rebate sr
LEFT JOIN tenant_finance.rebate r ON sr.document_number = r.document_number
ORDER BY sr.created_at desc;

--6. Contar todos los registros creados:
SELECT 
    (SELECT COUNT(*) FROM tenant_finance.stamped_rebate) AS total_stamped_rebates,
    (SELECT COUNT(*) FROM tenant_finance.rebate) AS total_rebates,
    (SELECT COUNT(*) 
     FROM tenant_finance.stamped_rebate sr
     INNER JOIN tenant_finance.rebate r ON sr.document_number = r.document_number
    ) AS total_relacionados;

-- 7. Verificar que la columna invoice_fiscal_uuid existe:
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'tenant_finance'
  AND table_name = 'stamped_rebate'
  AND column_name = 'invoice_fiscal_uuid';


SELECT DISTINCT 
    iss.rfc as rfc_emisor,
    iss.name as nombre_emisor,
    COUNT(i.invoice_uuid) as total_facturas
FROM tenant_fiscal.issuer iss
LEFT JOIN tenant_fiscal.invoice i ON iss.issuer_uuid = i.issuer_uuid
GROUP BY iss.rfc, iss.name
ORDER BY total_facturas DESC;

SELECT 
    iss.rfc,
    MIN(i.created_at) as fecha_minima,
    MAX(i.created_at) as fecha_maxima,
    COUNT(*) as total_documentos
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
WHERE iss.rfc = 'SOD970101ABC'  -- Cambia por tu RFC
GROUP BY iss.rfc;