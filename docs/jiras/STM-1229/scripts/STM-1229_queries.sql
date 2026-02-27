-- ============================================================================
-- STM-1229: Queries del Catalogo de Parametros
-- Esquema: core_utils
-- Fecha: 2025-12-22
-- ============================================================================

-- ============================================================================
-- 1. CONSULTAS DE PARAMETROS (cat_parameter)
-- ============================================================================

-- Listar todos los parametros activos
SELECT
    p.id_parameter,
    m.name AS module_name,
    p.id_type,
    p.name,
    p.description,
    p.value,
    p.version,
    p.start_date,
    p.end_date,
    p.status,
    p.created_at,
    p.created_by
FROM core_utils.cat_parameter p
LEFT JOIN core_utils.cat_module m ON p.id_module = m.id_module
WHERE p.status = 1
ORDER BY p.name, p.version DESC;

-- Buscar parametro por ID
SELECT * FROM core_utils.cat_parameter
WHERE id_parameter = 1;

-- Buscar parametro por nombre
SELECT * FROM core_utils.cat_parameter
WHERE name = 'MAX_RETRIES_TIMBRADO'
ORDER BY version DESC;

-- Obtener ultima version de un parametro
SELECT * FROM core_utils.cat_parameter
WHERE name = 'MAX_RETRIES_TIMBRADO'
  AND status = 1
ORDER BY version DESC
LIMIT 1;

-- Historial de versiones de un parametro
SELECT
    id_parameter,
    name,
    value,
    version,
    start_date,
    end_date,
    status,
    created_at
FROM core_utils.cat_parameter
WHERE name = 'MAX_RETRIES_TIMBRADO'
ORDER BY version DESC;

-- Parametros por modulo
SELECT
    p.id_parameter,
    p.name,
    p.value,
    p.version,
    p.status
FROM core_utils.cat_parameter p
WHERE p.id_module = 1  -- FISCAL
  AND p.status = 1
ORDER BY p.name;

-- Contar parametros por modulo
SELECT
    m.name AS modulo,
    COUNT(p.id_parameter) AS total_parametros
FROM core_utils.cat_module m
LEFT JOIN core_utils.cat_parameter p ON m.id_module = p.id_module AND p.status = 1
GROUP BY m.id_module, m.name
ORDER BY m.name;

-- Parametros vigentes (dentro de rango de fechas)
SELECT * FROM core_utils.cat_parameter
WHERE status = 1
  AND start_date <= CURRENT_TIMESTAMP
  AND (end_date IS NULL OR end_date >= CURRENT_TIMESTAMP)
ORDER BY name;

-- ============================================================================
-- 2. CONSULTAS DE MODULOS (cat_module)
-- ============================================================================

-- Listar todos los modulos
SELECT
    id_module,
    name,
    description,
    created_at
FROM core_utils.cat_module
ORDER BY name;

-- ============================================================================
-- 3. ESTADISTICAS
-- ============================================================================

-- Resumen de parametros
SELECT
    'Total Parametros' AS metric,
    COUNT(*) AS value
FROM core_utils.cat_parameter
UNION ALL
SELECT
    'Parametros Activos',
    COUNT(*)
FROM core_utils.cat_parameter WHERE status = 1
UNION ALL
SELECT
    'Parametros Inactivos',
    COUNT(*)
FROM core_utils.cat_parameter WHERE status = 0
UNION ALL
SELECT
    'Modulos con Parametros',
    COUNT(DISTINCT id_module)
FROM core_utils.cat_parameter WHERE status = 1;

-- Parametros por version
SELECT
    name,
    COUNT(*) AS total_versiones,
    MAX(version) AS ultima_version
FROM core_utils.cat_parameter
GROUP BY name
HAVING COUNT(*) > 1
ORDER BY total_versiones DESC;

-- ============================================================================
-- 4. VERIFICACION DE ESTRUCTURA
-- ============================================================================

-- Verificar estructura de tabla cat_parameter
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'core_utils'
  AND table_name = 'cat_parameter'
ORDER BY ordinal_position;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
