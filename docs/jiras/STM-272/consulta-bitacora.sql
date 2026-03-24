-- ============================================================
-- STM-272: Consultas de bitacora de auditoria - Complemento de Pago
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
WHERE trace_id = 'bb272001-0001-4000-a000-000000000001'
ORDER BY timestamp ASC;

-- 2. Ver logs de complementos de pago agrupados por transaccion (ultimas 24h)
SELECT
    trace_id,
    COUNT(*) AS total_pasos,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores,
    MIN(timestamp) AS inicio,
    MAX(timestamp) AS fin,
    MAX(duration_ms) AS duracion_total_ms
FROM core_audit.activity_logs
WHERE service_name = 'PaymentRegistrationService.registerPayment'
  AND timestamp >= NOW() - INTERVAL '24 hours'
GROUP BY trace_id
ORDER BY inicio DESC;

-- 3. Ver solo errores de registro de complementos de pago
SELECT
    trace_id,
    action,
    message,
    message_detail,
    details,
    timestamp
FROM core_audit.activity_logs
WHERE service_name = 'PaymentRegistrationService.registerPayment'
  AND is_error = true
ORDER BY timestamp DESC
LIMIT 50;

-- 4. Ver detalle de una accion especifica (ej: PAGO_PERSISTIR_BD)
SELECT *
FROM core_audit.activity_logs
WHERE action = 'PAGO_PERSISTIR_BD'
ORDER BY timestamp DESC
LIMIT 20;

-- 5. Contar registros por accion (distribucion de pasos)
SELECT
    action,
    COUNT(*) AS total,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores
FROM core_audit.activity_logs
WHERE service_name = 'PaymentRegistrationService.registerPayment'
GROUP BY action
ORDER BY action;

-- 6. Comparar volumenes entre facturas y pagos
SELECT
    service_name,
    COUNT(DISTINCT trace_id) AS transacciones,
    COUNT(*) AS total_logs,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS total_errores,
    AVG(duration_ms) AS duracion_promedio_ms
FROM core_audit.activity_logs
WHERE service_name IN (
    'InvoiceService.registerInvoice',
    'PaymentRegistrationService.registerPayment'
)
GROUP BY service_name;
