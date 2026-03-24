-- ============================================================
-- STM-704: Consultas de bitacora de auditoria - Facturas/NC
-- Esquema: core_audit | Tabla: activity_logs
-- ============================================================

-- 1. Ver todos los logs de una transaccion especifica (por traceId)
SELECT
    activity_logs_uuid,
    trace_id,
    action,
    is_error,
    message,
    message_detail,
    service_name,
    user_id,
    duration_ms,
    timestamp,
    details
FROM core_audit.activity_logs
WHERE trace_id = 'aabb0704-0001-4000-a000-000000000001'
ORDER BY timestamp ASC;

-- 2. Ver logs de facturas/NC agrupados por transaccion (ultimas 24h)
SELECT
    trace_id,
    COUNT(*) AS total_pasos,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores,
    MIN(timestamp) AS inicio,
    MAX(timestamp) AS fin,
    MAX(duration_ms) AS duracion_total_ms
FROM core_audit.activity_logs
WHERE service_name = 'InvoiceService.registerInvoice'
  AND timestamp >= NOW() - INTERVAL '24 hours'
GROUP BY trace_id
ORDER BY inicio DESC;

-- 3. Ver solo errores de registro de facturas
SELECT
    trace_id,
    action,
    message,
    message_detail,
    details,
    timestamp
FROM core_audit.activity_logs
WHERE service_name = 'InvoiceService.registerInvoice'
  AND is_error = true
ORDER BY timestamp DESC
LIMIT 50;

-- 4. Ver detalle de una accion especifica (ej: PERSISTIR_DOCUMENTO)
SELECT *
FROM core_audit.activity_logs
WHERE action = 'PERSISTIR_DOCUMENTO'
ORDER BY timestamp DESC
LIMIT 20;

-- 5. Contar registros por accion (distribucion de pasos)
SELECT
    action,
    COUNT(*) AS total,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores
FROM core_audit.activity_logs
WHERE service_name = 'InvoiceService.registerInvoice'
GROUP BY action
ORDER BY action;

-- 6. Ver logs de actualizacion de facturas
SELECT
    trace_id,
    action,
    is_error,
    message,
    message_detail,
    duration_ms,
    timestamp
FROM core_audit.activity_logs
WHERE service_name = 'InvoiceService.updateInvoice'
ORDER BY timestamp DESC
LIMIT 50;
