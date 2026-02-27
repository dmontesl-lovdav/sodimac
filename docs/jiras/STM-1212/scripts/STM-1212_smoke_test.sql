-- ============================================================================
-- JIRA: STM-1212
-- Descripcion: Prueba de humo para tabla cat_parameter
-- Proposito: Verificar que la tabla permite INSERT/SELECT correctamente
-- Fecha: 2025-12-08
-- ============================================================================

-- ============================================================================
-- PRUEBA 1: INSERT basico con valores minimos requeridos
-- ============================================================================
INSERT INTO cat_parameter (
    id_module,
    id_type,
    name,
    value,
    created_by
)
VALUES (
    1,                          -- id_module (ejemplo: Fiscal)
    1,                          -- id_type (ejemplo: Configuracion)
    'TASA_IVA',                 -- name
    '16',                       -- value
    1                           -- created_by
);

-- ============================================================================
-- PRUEBA 2: INSERT con todos los campos
-- ============================================================================
INSERT INTO cat_parameter (
    id_module,
    id_type,
    name,
    description,
    value,
    version,
    start_date,
    end_date,
    status,
    created_by
)
VALUES (
    1,                                          -- id_module
    2,                                          -- id_type
    'LIMITE_FACTURACION',                       -- name
    'Limite maximo de facturacion diaria',      -- description
    '1000000',                                  -- value
    1.0,                                        -- version
    CURRENT_TIMESTAMP,                          -- start_date
    NULL,                                       -- end_date (indefinido)
    1,                                          -- status (activo)
    1                                           -- created_by
);

-- ============================================================================
-- PRUEBA 3: INSERT de nueva version del mismo parametro
-- ============================================================================
INSERT INTO cat_parameter (
    id_module,
    id_type,
    name,
    description,
    value,
    version,
    created_by
)
VALUES (
    1,                                          -- id_module
    1,                                          -- id_type
    'TASA_IVA',                                 -- mismo nombre
    'Tasa IVA actualizada 2025',                -- description
    '16',                                       -- value
    2.0,                                        -- NUEVA VERSION
    1                                           -- created_by
);

-- ============================================================================
-- PRUEBA 4: Verificar que UNIQUE constraint funciona
-- ============================================================================
-- Este INSERT debe FALLAR (mismo nombre + misma version)
-- INSERT INTO cat_parameter (id_module, id_type, name, value, version, created_by)
-- VALUES (1, 1, 'TASA_IVA', '18', 1.0, 1);
-- ERROR: duplicate key value violates unique constraint "uk_cat_parameter_name_version"

-- ============================================================================
-- VERIFICACION: Mostrar registros insertados
-- ============================================================================
SELECT
    id_parameter,
    id_module,
    id_type,
    name,
    value,
    version,
    status,
    created_at,
    created_by
FROM cat_parameter
ORDER BY name, version;

-- ============================================================================
-- LIMPIEZA (opcional - descomentar para eliminar datos de prueba)
-- ============================================================================
-- DELETE FROM cat_parameter WHERE created_by = 1;
