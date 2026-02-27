-- =============================================================
-- Fix: Poblar internal_status en catálogo de estatus EFA
-- Tabla: shared_catalogs.catalog_detail (header_id=15)
-- Contexto: Fer reportó que internalStatus viene null en la API
-- Mapeo: internal_status = sort_order - 1 (confirmado con enum InvoiceStatus de fiscal-api)
-- =============================================================

-- Antes del fix (verificar)
SELECT key, sort_order, internal_status, color
FROM shared_catalogs.catalog_detail
WHERE header_id = 15 AND key LIKE 'EFA%'
ORDER BY sort_order;

-- Aplicar fix
UPDATE shared_catalogs.catalog_detail
SET internal_status = sort_order - 1
WHERE header_id = 15
AND key LIKE 'EFA%'
AND internal_status IS NULL;

-- Después del fix (verificar)
SELECT key, sort_order, internal_status, color
FROM shared_catalogs.catalog_detail
WHERE header_id = 15 AND key LIKE 'EFA%'
ORDER BY sort_order;
