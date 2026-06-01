-- =============================================
-- Script: Verificación y Tests de SODIMAC_BATCH_DEV
-- Propósito: Verificar que todo funcione correctamente
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE SODIMAC_BATCH_DEV;
GO

PRINT '===================================';
PRINT 'INICIANDO VERIFICACIÓN Y TESTS';
PRINT '===================================';
PRINT '';

-- =============================================
-- Test 1: Verificar que todas las tablas existen
-- =============================================
PRINT '--- Test 1: Verificar tablas ---';

DECLARE @expected_tables INT = 6;
DECLARE @actual_tables INT;

SELECT @actual_tables = COUNT(*)
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE';

IF @actual_tables = @expected_tables
    PRINT '✓ PASS: Todas las tablas creadas correctamente (' + CAST(@actual_tables AS VARCHAR(2)) + ' tablas)';
ELSE
    PRINT '✗ FAIL: Se esperaban ' + CAST(@expected_tables AS VARCHAR(2)) + ' tablas, se encontraron ' + CAST(@actual_tables AS VARCHAR(2));

PRINT '';

-- =============================================
-- Test 2: Verificar stored procedures
-- =============================================
PRINT '--- Test 2: Verificar stored procedures ---';

DECLARE @expected_sps INT = 8;
DECLARE @actual_sps INT;

SELECT @actual_sps = COUNT(*)
FROM sys.procedures;

IF @actual_sps = @expected_sps
    PRINT '✓ PASS: Todos los stored procedures creados correctamente (' + CAST(@actual_sps AS VARCHAR(2)) + ' SPs)';
ELSE
    PRINT '✗ FAIL: Se esperaban ' + CAST(@expected_sps AS VARCHAR(2)) + ' SPs, se encontraron ' + CAST(@actual_sps AS VARCHAR(2));

PRINT '';

-- =============================================
-- Test 3: Verificar vistas
-- =============================================
PRINT '--- Test 3: Verificar vistas ---';

DECLARE @expected_views INT = 7;
DECLARE @actual_views INT;

SELECT @actual_views = COUNT(*)
FROM INFORMATION_SCHEMA.VIEWS;

IF @actual_views = @expected_views
    PRINT '✓ PASS: Todas las vistas creadas correctamente (' + CAST(@actual_views AS VARCHAR(2)) + ' vistas)';
ELSE
    PRINT '✗ FAIL: Se esperaban ' + CAST(@expected_views AS VARCHAR(2)) + ' vistas, se encontraron ' + CAST(@actual_views AS VARCHAR(2));

PRINT '';

-- =============================================
-- Test 4: Verificar datos de catálogos
-- =============================================
PRINT '--- Test 4: Verificar datos de catálogos ---';

DECLARE @total_catalogos INT;
DECLARE @total_elementos INT;

SELECT @total_catalogos = COUNT(*) FROM catCatalogo;
SELECT @total_elementos = COUNT(*) FROM adminCatalogo;

IF @total_catalogos >= 5 AND @total_elementos >= 20
    PRINT '✓ PASS: Datos de catálogos insertados correctamente (' + CAST(@total_catalogos AS VARCHAR(2)) + ' catálogos, ' + CAST(@total_elementos AS VARCHAR(3)) + ' elementos)';
ELSE
    PRINT '✗ FAIL: Faltan datos en catálogos';

PRINT '';

-- =============================================
-- Test 5: Verificar foreign keys
-- =============================================
PRINT '--- Test 5: Verificar foreign keys ---';

DECLARE @expected_fks INT = 5;
DECLARE @actual_fks INT;

SELECT @actual_fks = COUNT(*)
FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS;

IF @actual_fks = @expected_fks
    PRINT '✓ PASS: Todas las foreign keys creadas correctamente (' + CAST(@actual_fks AS VARCHAR(2)) + ' FKs)';
ELSE
    PRINT '✗ FAIL: Se esperaban ' + CAST(@expected_fks AS VARCHAR(2)) + ' FKs, se encontraron ' + CAST(@actual_fks AS VARCHAR(2));

PRINT '';

-- =============================================
-- Test 6: Verificar índices
-- =============================================
PRINT '--- Test 6: Verificar índices ---';

DECLARE @total_indices INT;

SELECT @total_indices = COUNT(*)
FROM sys.indexes
WHERE object_id IN (
    SELECT object_id
    FROM sys.tables
    WHERE schema_id = SCHEMA_ID('dbo')
)
AND is_primary_key = 0
AND is_unique_constraint = 0
AND type > 0; -- Excluir heaps

IF @total_indices >= 15
    PRINT '✓ PASS: Índices creados correctamente (' + CAST(@total_indices AS VARCHAR(3)) + ' índices)';
ELSE
    PRINT '✗ FAIL: Se esperaban al menos 15 índices, se encontraron ' + CAST(@total_indices AS VARCHAR(3));

PRINT '';

-- =============================================
-- Test 7: Test de inserción en ctrlProcesoCab
-- =============================================
PRINT '--- Test 7: Test de inserción en ctrlProcesoCab ---';

BEGIN TRY
    DECLARE @test_id_ejecucion INT;

    INSERT INTO ctrlProcesoCab (id_proceso, fecha_inicio, estatus)
    VALUES (1, GETDATE(), 'IN_PROGRESS');

    SET @test_id_ejecucion = SCOPE_IDENTITY();

    -- Verificar que se insertó
    IF EXISTS (SELECT 1 FROM ctrlProcesoCab WHERE id_ejecucion = @test_id_ejecucion)
    BEGIN
        PRINT '✓ PASS: Inserción en ctrlProcesoCab exitosa';

        -- Limpiar test
        DELETE FROM ctrlProcesoCab WHERE id_ejecucion = @test_id_ejecucion;
    END
    ELSE
        PRINT '✗ FAIL: No se pudo insertar en ctrlProcesoCab';
END TRY
BEGIN CATCH
    PRINT '✗ FAIL: Error al insertar en ctrlProcesoCab - ' + ERROR_MESSAGE();
END CATCH

PRINT '';

-- =============================================
-- Test 8: Test de stored procedure sp_IniciarEjecucion
-- =============================================
PRINT '--- Test 8: Test de sp_IniciarEjecucion ---';

BEGIN TRY
    DECLARE @sp_test_id_ejecucion INT;

    EXEC sp_IniciarEjecucion
        @id_proceso = 1,
        @id_ejecucion = @sp_test_id_ejecucion OUTPUT;

    IF @sp_test_id_ejecucion IS NOT NULL AND @sp_test_id_ejecucion > 0
    BEGIN
        PRINT '✓ PASS: sp_IniciarEjecucion funciona correctamente (ID: ' + CAST(@sp_test_id_ejecucion AS VARCHAR(10)) + ')';

        -- Limpiar test
        DELETE FROM ctrlProcesoCab WHERE id_ejecucion = @sp_test_id_ejecucion;
    END
    ELSE
        PRINT '✗ FAIL: sp_IniciarEjecucion no retornó ID válido';
END TRY
BEGIN CATCH
    PRINT '✗ FAIL: Error en sp_IniciarEjecucion - ' + ERROR_MESSAGE();
END CATCH

PRINT '';

-- =============================================
-- Test 9: Test de relaciones CASCADE DELETE
-- =============================================
PRINT '--- Test 9: Test de CASCADE DELETE ---';

BEGIN TRY
    -- Crear ejecución de prueba
    DECLARE @cascade_test_id INT;

    INSERT INTO ctrlProcesoCab (id_proceso, fecha_inicio, estatus)
    VALUES (1, GETDATE(), 'IN_PROGRESS');

    SET @cascade_test_id = SCOPE_IDENTITY();

    -- Insertar registros relacionados
    INSERT INTO ctrlProcesoDet (id_ejecucion, nombre_paso, secuencia)
    VALUES (@cascade_test_id, 'Test Paso 1', 1);

    INSERT INTO ctrlLog (nombre, id_ejecucion, log, nivel)
    VALUES ('Test Log', @cascade_test_id, 'Test mensaje', 'INFO');

    INSERT INTO ctrlProcesoElemento (id_ejecucion, valor, secuencia)
    VALUES (@cascade_test_id, 'TEST_001', 1);

    -- Eliminar ejecución (debe eliminar en cascada)
    DELETE FROM ctrlProcesoCab WHERE id_ejecucion = @cascade_test_id;

    -- Verificar que se eliminaron los registros relacionados
    DECLARE @remaining_det INT, @remaining_log INT, @remaining_elem INT;

    SELECT @remaining_det = COUNT(*) FROM ctrlProcesoDet WHERE id_ejecucion = @cascade_test_id;
    SELECT @remaining_log = COUNT(*) FROM ctrlLog WHERE id_ejecucion = @cascade_test_id;
    SELECT @remaining_elem = COUNT(*) FROM ctrlProcesoElemento WHERE id_ejecucion = @cascade_test_id;

    IF @remaining_det = 0 AND @remaining_log = 0 AND @remaining_elem = 0
        PRINT '✓ PASS: CASCADE DELETE funciona correctamente';
    ELSE
        PRINT '✗ FAIL: CASCADE DELETE no eliminó todos los registros relacionados';
END TRY
BEGIN CATCH
    PRINT '✗ FAIL: Error en test de CASCADE DELETE - ' + ERROR_MESSAGE();
END CATCH

PRINT '';

-- =============================================
-- Test 10: Test de vistas
-- =============================================
PRINT '--- Test 10: Test de vistas ---';

BEGIN TRY
    DECLARE @view_test_count INT = 0;

    -- Probar cada vista
    SELECT @view_test_count = COUNT(*) FROM vw_EjecucionesRecientes;
    SELECT @view_test_count = COUNT(*) FROM vw_EstadisticasPorProceso;
    SELECT @view_test_count = COUNT(*) FROM vw_LogsErrores;
    SELECT @view_test_count = COUNT(*) FROM vw_EjecucionesEnCurso;
    SELECT @view_test_count = COUNT(*) FROM vw_RendimientoDiario;
    SELECT @view_test_count = COUNT(*) FROM vw_ElementosFallidos;
    SELECT @view_test_count = COUNT(*) FROM vw_ResumenHoy;

    PRINT '✓ PASS: Todas las vistas funcionan correctamente';
END TRY
BEGIN CATCH
    PRINT '✗ FAIL: Error al consultar vistas - ' + ERROR_MESSAGE();
END CATCH

PRINT '';

-- =============================================
-- Test 11: Test de columna computed (duracion_segundos)
-- =============================================
PRINT '--- Test 11: Test de columna computed ---';

BEGIN TRY
    DECLARE @computed_test_id INT;

    INSERT INTO ctrlProcesoCab (id_proceso, fecha_inicio, fecha_final, estatus)
    VALUES (1, '2026-01-20 10:00:00', '2026-01-20 10:05:30', 'SUCCESS');

    SET @computed_test_id = SCOPE_IDENTITY();

    DECLARE @duracion INT;
    SELECT @duracion = duracion_segundos FROM ctrlProcesoCab WHERE id_ejecucion = @computed_test_id;

    IF @duracion = 330 -- 5 minutos 30 segundos = 330 segundos
    BEGIN
        PRINT '✓ PASS: Columna computed duracion_segundos funciona correctamente (' + CAST(@duracion AS VARCHAR(10)) + ' segundos)';
        DELETE FROM ctrlProcesoCab WHERE id_ejecucion = @computed_test_id;
    END
    ELSE
        PRINT '✗ FAIL: Columna computed no calcula correctamente (esperado: 330, obtenido: ' + CAST(@duracion AS VARCHAR(10)) + ')';
END TRY
BEGIN CATCH
    PRINT '✗ FAIL: Error en test de columna computed - ' + ERROR_MESSAGE();
END CATCH

PRINT '';

-- =============================================
-- Resumen de Tests
-- =============================================
PRINT '===================================';
PRINT 'VERIFICACIÓN COMPLETADA';
PRINT '===================================';
PRINT '';

-- Mostrar resumen de objetos en la BD
PRINT 'RESUMEN DE OBJETOS:';
PRINT '-----------------------------------';

SELECT
    'Tablas' AS TipoObjeto,
    COUNT(*) AS Total
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
UNION ALL
SELECT
    'Vistas',
    COUNT(*)
FROM INFORMATION_SCHEMA.VIEWS
UNION ALL
SELECT
    'Stored Procedures',
    COUNT(*)
FROM sys.procedures
UNION ALL
SELECT
    'Foreign Keys',
    COUNT(*)
FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
UNION ALL
SELECT
    'Índices (no PK)',
    COUNT(*)
FROM sys.indexes
WHERE object_id IN (SELECT object_id FROM sys.tables WHERE schema_id = SCHEMA_ID('dbo'))
AND is_primary_key = 0
AND is_unique_constraint = 0
AND type > 0;

PRINT '';
PRINT '===================================';
PRINT 'Base de datos lista para usar';
PRINT '===================================';
GO
