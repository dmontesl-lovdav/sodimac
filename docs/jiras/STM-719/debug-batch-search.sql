-- ============================================================
-- STM-719: Debug - Consultas para diagnosticar el batch de descarga
-- BD: b2b_portal (PostgreSQL) - tenant_fiscal.invoice
-- ============================================================


-- ============================================================
-- 1. Ver cuantas facturas estan disponibles para el batch
--    (status=3, document_type='I')
-- ============================================================
SELECT COUNT(*), document_type
FROM tenant_fiscal.invoice
WHERE status = 3
GROUP BY document_type;


-- ============================================================
-- 2. Verificar el created_at de esas facturas
--    El batch filtra por created_at >= hoy - 6 meses
--    Si estan fuera de ese rango, el batch no las encuentra
-- ============================================================
SELECT fiscal_uuid, created_at, document_type, status
FROM tenant_fiscal.invoice
WHERE status = 3 AND document_type = 'I'
ORDER BY created_at DESC
LIMIT 10;


-- ============================================================
-- 3. Replicar exactamente la consulta que hace el batch
--    (ajustar las fechas segun el dia de ejecucion)
--    Hoy: 2026-03-18 -> fechaInicio = hoy - 6 meses = 2025-09-18
-- ============================================================
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


-- ============================================================
-- 4. FIX (solo pruebas): Adelantar created_at 2 meses
--    para que queden dentro del rango de 6 meses
-- ============================================================
UPDATE tenant_fiscal.invoice
SET created_at = created_at + '2 months'::interval
WHERE status = 3
  AND document_type = 'I';


-- ============================================================
-- 5. Verificar parametro MAX_SEARCH_MONTHS en utils-api
--    Si el rango del batch supera este valor, la API rechaza
--    con WRN7000. Valor actual: 6 meses
-- ============================================================
SELECT id_parameter, name, value, description
FROM core_utils.cat_parameter
WHERE name = 'MAX_SEARCH_MONTHS';

-- Ampliar si se necesita mas rango (ej: 9 meses)
-- UPDATE core_utils.cat_parameter
-- SET value = '9', updated_at = NOW()
-- WHERE id_parameter = 212
--   AND name = 'MAX_SEARCH_MONTHS';
