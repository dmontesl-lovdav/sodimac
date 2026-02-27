-- ============================================================================
-- STM-393: Parametro MAX_SEARCH_MONTHS
-- Servicio: utils-api
-- Fecha: 2025-01-06
-- Descripcion: Maximo de meses permitidos en rango de busqueda de facturas
-- ============================================================================

-- ============================================================================
-- VERIFICACION PREVIA
-- ============================================================================

-- Verificar si el parametro ya existe
SELECT
    id_parameter,
    id_module,
    name,
    description,
    value,
    version,
    status
FROM core_utils.cat_parameter
WHERE name = 'MAX_SEARCH_MONTHS';

-- Verificar modulos disponibles
SELECT id_module, name, description
FROM core_utils.cat_module
WHERE status = 1
ORDER BY id_module;

-- ============================================================================
-- INSERT: Parametro MAX_SEARCH_MONTHS
-- ============================================================================

INSERT INTO core_utils.cat_parameter
(id_module, id_type, name, description, value, version, start_date, status, created_by, created_at)
VALUES
(1, 1, 'MAX_SEARCH_MONTHS', 'Maximo de meses permitidos en rango de busqueda de facturas', '6', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP)
ON CONFLICT (name, version) DO NOTHING;

-- ============================================================================
-- VERIFICACION POST-INSERT
-- ============================================================================

-- Verificar insercion
SELECT
    id_parameter,
    id_module,
    id_type,
    name,
    description,
    value,
    version,
    start_date,
    end_date,
    status,
    created_by,
    created_at
FROM core_utils.cat_parameter
WHERE name = 'MAX_SEARCH_MONTHS';

-- Listar todos los parametros del modulo FISCAL
SELECT
    p.id_parameter,
    m.name as modulo,
    p.name as parametro,
    p.value,
    p.version,
    p.status
FROM core_utils.cat_parameter p
INNER JOIN core_utils.cat_module m ON p.id_module = m.id_module
WHERE m.name = 'FISCAL'
  AND p.status = 1
ORDER BY p.name;

-- Probar endpoint de parametros (ejecutar desde aplicacion)
-- GET http://localhost:3712/api/parameters?name=MAX_SEARCH_MONTHS&status=1

-- ============================================================================
-- ROLLBACK (en caso de ser necesario)
-- ============================================================================

-- DELETE FROM core_utils.cat_parameter WHERE name = 'MAX_SEARCH_MONTHS';

-- ============================================================================
-- VERSIONADO: Si en el futuro se necesita cambiar el valor
-- ============================================================================

-- Opcion 1: Via API REST (recomendado)
-- POST http://localhost:3712/api/parameters/{id}/versions
-- Body: { "value": "12", "changeReason": "Ampliacion por requerimiento de negocio" }

-- Opcion 2: Via SQL (solo emergencias)
-- 1. Cerrar version actual
-- UPDATE core_utils.cat_parameter
-- SET status = 0, end_date = CURRENT_TIMESTAMP
-- WHERE name = 'MAX_SEARCH_MONTHS' AND version = 1.0;

-- 2. Crear nueva version
-- INSERT INTO core_utils.cat_parameter
-- (id_module, id_type, name, description, value, version, start_date, status, created_by, created_at)
-- VALUES
-- (1, 1, 'MAX_SEARCH_MONTHS', 'Maximo de meses permitidos en rango de busqueda de facturas', '12', 1.1, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
