-- =============================================
-- Script: Ejemplos de Uso de SODIMAC_BATCH_DEV
-- Propósito: Demostrar cómo usar los SPs y consultar datos
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE SODIMAC_BATCH_DEV;
GO

PRINT '===================================';
PRINT 'EJEMPLOS DE USO';
PRINT '===================================';
PRINT '';

-- =============================================
-- Ejemplo 1: Iniciar una nueva ejecución
-- =============================================
PRINT '--- Ejemplo 1: Iniciar nueva ejecución ---';

DECLARE @id_ejecucion INT;
DECLARE @id_proceso INT = 1; -- Sincronización de Acuerdos Comerciales (Rebates)

-- Iniciar ejecución
EXEC sp_IniciarEjecucion
    @id_proceso = @id_proceso,
    @id_ejecucion = @id_ejecucion OUTPUT;

PRINT 'ID de ejecución creada: ' + CAST(@id_ejecucion AS VARCHAR(10));
PRINT '';

-- =============================================
-- Ejemplo 2: Registrar pasos del proceso
-- =============================================
PRINT '--- Ejemplo 2: Registrar pasos del proceso ---';

-- Paso 1: Descarga de contratos
EXEC sp_RegistrarPaso
    @id_ejecucion = @id_ejecucion,
    @nombre_paso = 'Descarga de contratos desde API externa',
    @secuencia = 1,
    @parametros = 'page=1, pageSize=100',
    @detalle = 'Descargados 50 contratos',
    @registros_procesados = 50,
    @estatus = 'SUCCESS';

-- Paso 2: Descarga de acuerdos
EXEC sp_RegistrarPaso
    @id_ejecucion = @id_ejecucion,
    @nombre_paso = 'Descarga de acuerdos comerciales',
    @secuencia = 2,
    @parametros = 'contracts=50',
    @detalle = 'Descargados 150 acuerdos',
    @registros_procesados = 150,
    @estatus = 'SUCCESS';

-- Paso 3: Eliminación de datos existentes
EXEC sp_RegistrarPaso
    @id_ejecucion = @id_ejecucion,
    @nombre_paso = 'Eliminación de datos existentes (full-sync)',
    @secuencia = 3,
    @parametros = NULL,
    @detalle = 'Eliminados 140 registros previos',
    @registros_procesados = 140,
    @estatus = 'SUCCESS';

-- Paso 4: Carga de nuevos datos
EXEC sp_RegistrarPaso
    @id_ejecucion = @id_ejecucion,
    @nombre_paso = 'Carga de nuevos acuerdos a BD',
    @secuencia = 4,
    @parametros = NULL,
    @detalle = 'Cargados 150 acuerdos',
    @registros_procesados = 150,
    @estatus = 'SUCCESS';

PRINT 'Pasos registrados exitosamente';
PRINT '';

-- =============================================
-- Ejemplo 3: Registrar logs durante el proceso
-- =============================================
PRINT '--- Ejemplo 3: Registrar logs ---';

-- Log de información
EXEC sp_RegistrarLog
    @nombre = 'Conexión API',
    @id_ejecucion = @id_ejecucion,
    @log = 'Conexión exitosa con API externa de Rebates',
    @nivel = 'INFO',
    @fase = 'EXTRACT';

-- Log de advertencia
EXEC sp_RegistrarLog
    @nombre = 'Contrato sin acuerdos',
    @id_ejecucion = @id_ejecucion,
    @log = 'Contrato ID 1234 no tiene acuerdos asociados',
    @nivel = 'WARN',
    @fase = 'TRANSFORM';

PRINT 'Logs registrados exitosamente';
PRINT '';

-- =============================================
-- Ejemplo 4: Registrar elementos procesados
-- =============================================
PRINT '--- Ejemplo 4: Registrar elementos procesados ---';

-- Registrar varios contratos procesados
DECLARE @i INT = 1;
WHILE @i <= 5
BEGIN
    EXEC sp_RegistrarElemento
        @id_ejecucion = @id_ejecucion,
        @valor = CONCAT('CONTRACT_', @i),
        @valorAlterno = CONCAT('Contrato Comercial ', @i),
        @secuencia = @i,
        @estatus = 'PROCESSED',
        @detalle_error = NULL;

    SET @i = @i + 1;
END

PRINT 'Elementos registrados exitosamente';
PRINT '';

-- =============================================
-- Ejemplo 5: Finalizar ejecución
-- =============================================
PRINT '--- Ejemplo 5: Finalizar ejecución ---';

-- Simular un pequeño delay para que haya duración
WAITFOR DELAY '00:00:02';

EXEC sp_FinalizarEjecucion
    @id_ejecucion = @id_ejecucion,
    @estatus = 'SUCCESS',
    @registros_origen = 150,
    @registros_destino = 150,
    @mensaje_error = NULL;

PRINT 'Ejecución finalizada exitosamente';
PRINT '';

-- =============================================
-- Ejemplo 6: Consultar resultados
-- =============================================
PRINT '--- Ejemplo 6: Consultar resultados ---';
PRINT '';

-- Obtener última ejecución
PRINT 'Última ejecución del proceso:';
EXEC sp_ObtenerUltimaEjecucion @id_proceso = 1;
PRINT '';

-- Obtener detalle de la ejecución
PRINT 'Detalle completo de la ejecución:';
EXEC sp_ObtenerDetalleEjecucion @id_ejecucion = @id_ejecucion;
PRINT '';

-- =============================================
-- Ejemplo 7: Consultar vistas
-- =============================================
PRINT '--- Ejemplo 7: Consultar vistas ---';
PRINT '';

-- Ejecuciones recientes
PRINT 'Ejecuciones recientes:';
SELECT TOP 5
    id_ejecucion,
    nombre_proceso,
    estatus_descripcion,
    registros_origen,
    registros_destino,
    porcentaje_carga,
    duracion_segundos,
    fecha_inicio
FROM vw_EjecucionesRecientes
ORDER BY fecha_inicio DESC;
PRINT '';

-- Estadísticas por proceso
PRINT 'Estadísticas por proceso:';
SELECT * FROM vw_EstadisticasPorProceso;
PRINT '';

-- Resumen del día
PRINT 'Resumen del día:';
SELECT * FROM vw_ResumenHoy;
PRINT '';

-- =============================================
-- Ejemplo 8: Simular ejecución fallida
-- =============================================
PRINT '--- Ejemplo 8: Simular ejecución fallida ---';

DECLARE @id_ejecucion_fallida INT;

-- Iniciar ejecución
EXEC sp_IniciarEjecucion
    @id_proceso = 1,
    @id_ejecucion = @id_ejecucion_fallida OUTPUT;

-- Registrar log de error
EXEC sp_RegistrarLog
    @nombre = 'Error de conexión',
    @id_ejecucion = @id_ejecucion_fallida,
    @log = 'No se pudo conectar con el servicio externo después de 3 reintentos',
    @nivel = 'ERROR',
    @fase = 'EXTRACT';

-- Finalizar con error
EXEC sp_FinalizarEjecucion
    @id_ejecucion = @id_ejecucion_fallida,
    @estatus = 'FAILED',
    @registros_origen = 0,
    @registros_destino = 0,
    @mensaje_error = 'Timeout al conectar con API externa después de 3 reintentos';

PRINT 'Ejecución fallida registrada';
PRINT '';

-- Consultar logs de errores
PRINT 'Logs de errores:';
SELECT
    nombre,
    nombre_proceso,
    log,
    nivel,
    fase,
    fecha_registro
FROM vw_LogsErrores
ORDER BY fecha_registro DESC;
PRINT '';

PRINT '===================================';
PRINT 'EJEMPLOS COMPLETADOS';
PRINT '===================================';
GO
