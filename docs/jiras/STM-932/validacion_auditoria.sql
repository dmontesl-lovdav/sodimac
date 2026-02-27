-- ============================================================================
-- STM-932: Validación de Base de Datos - Auditoría API
-- Esquema: core_audit
-- Tabla: activity_logs
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. VERIFICAR ESTRUCTURA DE LA TABLA
-- ----------------------------------------------------------------------------

-- Ver estructura de la tabla activity_logs
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'core_audit'
  AND table_name = 'activity_logs'
ORDER BY ordinal_position;

-- ----------------------------------------------------------------------------
-- 2. CONSULTAS DE VALIDACIÓN DE DATOS
-- ----------------------------------------------------------------------------

-- Contar total de registros en activity_logs
SELECT COUNT(*) AS total_registros
FROM core_audit.activity_logs;

-- Ver los últimos 10 registros insertados
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    service_name,
    user_id,
    is_error,
    message,
    timestamp
FROM core_audit.activity_logs
ORDER BY timestamp DESC
LIMIT 10;

-- Ver registros de hoy
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    service_name,
    user_id,
    is_error,
    message,
    message_detail,
    duration_ms,
    timestamp
FROM core_audit.activity_logs
WHERE timestamp >= CURRENT_DATE
ORDER BY timestamp DESC;

-- ----------------------------------------------------------------------------
-- 3. CONSULTAS POR FILTROS (como usa el endpoint GET /api/activity-logs)
-- ----------------------------------------------------------------------------

-- Buscar por rango de fechas (ejemplo: últimos 30 días)
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    service_name,
    user_id,
    is_error,
    message,
    timestamp
FROM core_audit.activity_logs
WHERE timestamp BETWEEN '2024-01-01 00:00:00' AND '2024-01-31 23:59:59'
ORDER BY timestamp ASC
LIMIT 20 OFFSET 0;

-- Buscar por módulo
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    service_name,
    is_error,
    message,
    timestamp
FROM core_audit.activity_logs
WHERE modulo = 'fiscal'
ORDER BY timestamp DESC
LIMIT 20;

-- Buscar por service_name
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    service_name,
    is_error,
    message,
    timestamp
FROM core_audit.activity_logs
WHERE service_name = 'fiscal-api'
ORDER BY timestamp DESC
LIMIT 20;

-- Buscar por trace_id específico (para trazabilidad)
SELECT
    activity_logs_uuid,
    trace_id,
    trace_front_id,
    modulo,
    action,
    service_name,
    user_id,
    is_error,
    message,
    message_detail,
    details,
    duration_ms,
    timestamp
FROM core_audit.activity_logs
WHERE trace_id = '550e8400-e29b-41d4-a716-446655440000'
ORDER BY timestamp ASC;

-- ----------------------------------------------------------------------------
-- 4. CONSULTAS DE ANÁLISIS
-- ----------------------------------------------------------------------------

-- Contar logs por módulo
SELECT
    modulo,
    COUNT(*) AS total_logs,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores,
    SUM(CASE WHEN NOT is_error THEN 1 ELSE 0 END) AS exitosos
FROM core_audit.activity_logs
GROUP BY modulo
ORDER BY total_logs DESC;

-- Contar logs por servicio
SELECT
    service_name,
    COUNT(*) AS total_logs,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores
FROM core_audit.activity_logs
GROUP BY service_name
ORDER BY total_logs DESC;

-- Contar logs por acción
SELECT
    action,
    COUNT(*) AS total,
    AVG(duration_ms) AS duracion_promedio_ms
FROM core_audit.activity_logs
GROUP BY action
ORDER BY total DESC
LIMIT 20;

-- Ver solo errores
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    service_name,
    user_id,
    message,
    message_detail,
    details,
    timestamp
FROM core_audit.activity_logs
WHERE is_error = TRUE
ORDER BY timestamp DESC
LIMIT 50;

-- Estadísticas por día (últimos 7 días)
SELECT
    DATE(timestamp) AS fecha,
    COUNT(*) AS total_logs,
    SUM(CASE WHEN is_error THEN 1 ELSE 0 END) AS errores,
    SUM(CASE WHEN NOT is_error THEN 1 ELSE 0 END) AS exitosos,
    ROUND(AVG(duration_ms)::numeric, 2) AS duracion_promedio_ms
FROM core_audit.activity_logs
WHERE timestamp >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(timestamp)
ORDER BY fecha DESC;

-- ----------------------------------------------------------------------------
-- 5. CONSULTAS DE USUARIO
-- ----------------------------------------------------------------------------

-- Actividad por usuario
SELECT
    user_id,
    COUNT(*) AS total_acciones,
    MAX(timestamp) AS ultima_actividad
FROM core_audit.activity_logs
GROUP BY user_id
ORDER BY total_acciones DESC
LIMIT 20;

-- Ver actividad de un usuario específico
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    is_error,
    message,
    timestamp
FROM core_audit.activity_logs
WHERE user_id = 'user123'
ORDER BY timestamp DESC
LIMIT 50;

-- ----------------------------------------------------------------------------
-- 6. VERIFICAR REGISTRO INSERTADO VIA BFF
-- ----------------------------------------------------------------------------

-- Buscar el registro de prueba insertado via BFF
SELECT *
FROM core_audit.activity_logs
WHERE trace_id = '550e8400-e29b-41d4-a716-446655440000'
   OR trace_id = '660e8400-e29b-41d4-a716-446655440001';

-- Ver detalles JSON de un registro
SELECT
    activity_logs_uuid,
    trace_id,
    modulo,
    action,
    details::text AS details_json,
    timestamp
FROM core_audit.activity_logs
WHERE details IS NOT NULL
  AND details::text != '{}'
ORDER BY timestamp DESC
LIMIT 10;

-- ----------------------------------------------------------------------------
-- 7. LIMPIEZA DE DATOS DE PRUEBA (EJECUTAR SOLO SI ES NECESARIO)
-- ----------------------------------------------------------------------------

-- PRECAUCIÓN: Descomentar solo para limpiar datos de prueba
-- DELETE FROM core_audit.activity_logs
-- WHERE trace_id IN (
--     '550e8400-e29b-41d4-a716-446655440000',
--     '660e8400-e29b-41d4-a716-446655440001'
-- );

-- ============================================================================
-- FIN DEL SCRIPT DE VALIDACIÓN
-- ============================================================================
