-- =============================================
-- Script: Stored Procedures para SODIMAC_BATCH_DEV
-- Propósito: Procedimientos almacenados para gestión de ejecuciones batch
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE SODIMAC_BATCH_DEV;
GO

-- =============================================
-- SP: Iniciar Ejecución de Proceso
-- =============================================
CREATE OR ALTER PROCEDURE sp_IniciarEjecucion
    @id_proceso INT,
    @id_ejecucion INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO ctrlProcesoCab (id_proceso, fecha_inicio, estatus)
    VALUES (@id_proceso, GETDATE(), 'IN_PROGRESS');

    SET @id_ejecucion = SCOPE_IDENTITY();

    -- Registrar log de inicio
    INSERT INTO ctrlLog (nombre, id_ejecucion, log, nivel, fase, fecha_registro)
    VALUES ('Inicio de Proceso', @id_ejecucion, 'Proceso iniciado', 'INFO', 'START', GETDATE());

    SELECT @id_ejecucion AS id_ejecucion, 'Ejecución iniciada exitosamente' AS mensaje;
END;
GO

PRINT 'SP sp_IniciarEjecucion creado.';
GO

-- =============================================
-- SP: Finalizar Ejecución de Proceso
-- =============================================
CREATE OR ALTER PROCEDURE sp_FinalizarEjecucion
    @id_ejecucion INT,
    @estatus VARCHAR(20),
    @registros_origen INT = 0,
    @registros_destino INT = 0,
    @mensaje_error VARCHAR(1000) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE ctrlProcesoCab
    SET
        fecha_final = GETDATE(),
        estatus = @estatus,
        registros_origen = @registros_origen,
        registros_destino = @registros_destino,
        mensaje_error = @mensaje_error
    WHERE id_ejecucion = @id_ejecucion;

    -- Registrar log de finalización
    DECLARE @log_mensaje VARCHAR(500);
    SET @log_mensaje = 'Proceso finalizado con estatus: ' + @estatus +
                       '. Registros origen: ' + CAST(@registros_origen AS VARCHAR(10)) +
                       ', Registros destino: ' + CAST(@registros_destino AS VARCHAR(10));

    INSERT INTO ctrlLog (nombre, id_ejecucion, log, nivel, fase, fecha_registro)
    VALUES ('Fin de Proceso', @id_ejecucion, @log_mensaje,
            CASE WHEN @estatus = 'SUCCESS' THEN 'INFO' ELSE 'ERROR' END,
            'END', GETDATE());

    SELECT 'Ejecución finalizada exitosamente' AS mensaje;
END;
GO

PRINT 'SP sp_FinalizarEjecucion creado.';
GO

-- =============================================
-- SP: Registrar Paso del Proceso
-- =============================================
CREATE OR ALTER PROCEDURE sp_RegistrarPaso
    @id_ejecucion INT,
    @nombre_paso VARCHAR(100),
    @secuencia INT,
    @parametros VARCHAR(255) = NULL,
    @detalle VARCHAR(255) = NULL,
    @registros_procesados INT = 0,
    @estatus VARCHAR(20) = 'SUCCESS'
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO ctrlProcesoDet
    (
        id_ejecucion,
        nombre_paso,
        secuencia,
        fecha_inicio_registro,
        fecha_final_registro,
        parametros_registro,
        detalle,
        registros_procesados,
        estatus
    )
    VALUES
    (
        @id_ejecucion,
        @nombre_paso,
        @secuencia,
        GETDATE(),
        GETDATE(),
        @parametros,
        @detalle,
        @registros_procesados,
        @estatus
    );

    SELECT 'Paso registrado exitosamente' AS mensaje;
END;
GO

PRINT 'SP sp_RegistrarPaso creado.';
GO

-- =============================================
-- SP: Registrar Log
-- =============================================
CREATE OR ALTER PROCEDURE sp_RegistrarLog
    @nombre VARCHAR(100),
    @id_ejecucion INT = NULL,
    @log TEXT,
    @nivel VARCHAR(20) = 'INFO',
    @fase VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO ctrlLog (nombre, id_ejecucion, log, nivel, fase, fecha_registro)
    VALUES (@nombre, @id_ejecucion, @log, @nivel, @fase, GETDATE());

    SELECT 'Log registrado exitosamente' AS mensaje;
END;
GO

PRINT 'SP sp_RegistrarLog creado.';
GO

-- =============================================
-- SP: Registrar Elemento Procesado
-- =============================================
CREATE OR ALTER PROCEDURE sp_RegistrarElemento
    @id_ejecucion INT,
    @valor VARCHAR(50),
    @valorAlterno VARCHAR(250) = NULL,
    @secuencia INT,
    @estatus VARCHAR(20) = 'PROCESSED',
    @detalle_error VARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO ctrlProcesoElemento
    (
        id_ejecucion,
        valor,
        valorAlterno,
        secuencia,
        estatus,
        fechaRegistro,
        detalle_error
    )
    VALUES
    (
        @id_ejecucion,
        @valor,
        @valorAlterno,
        @secuencia,
        @estatus,
        GETDATE(),
        @detalle_error
    );

    SELECT 'Elemento registrado exitosamente' AS mensaje;
END;
GO

PRINT 'SP sp_RegistrarElemento creado.';
GO

-- =============================================
-- SP: Obtener Última Ejecución de Proceso
-- =============================================
CREATE OR ALTER PROCEDURE sp_ObtenerUltimaEjecucion
    @id_proceso INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP 1
        cp.id_ejecucion,
        cp.id_proceso,
        ac.descripcion AS nombre_proceso,
        cp.registros_origen,
        cp.registros_destino,
        cp.fecha_inicio,
        cp.fecha_final,
        cp.duracion_segundos,
        cp.estatus,
        cp.mensaje_error
    FROM ctrlProcesoCab cp
    INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
    WHERE cp.id_proceso = @id_proceso
    ORDER BY cp.fecha_inicio DESC;
END;
GO

PRINT 'SP sp_ObtenerUltimaEjecucion creado.';
GO

-- =============================================
-- SP: Obtener Resumen de Ejecuciones
-- =============================================
CREATE OR ALTER PROCEDURE sp_ObtenerResumenEjecuciones
    @id_proceso INT = NULL,
    @fecha_inicio DATETIME = NULL,
    @fecha_fin DATETIME = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        cp.id_ejecucion,
        cp.id_proceso,
        ac.descripcion AS nombre_proceso,
        cp.registros_origen,
        cp.registros_destino,
        cp.fecha_inicio,
        cp.fecha_final,
        cp.duracion_segundos,
        cp.estatus,
        (SELECT COUNT(*) FROM ctrlProcesoDet WHERE id_ejecucion = cp.id_ejecucion) AS total_pasos,
        (SELECT COUNT(*) FROM ctrlLog WHERE id_ejecucion = cp.id_ejecucion AND nivel = 'ERROR') AS total_errores,
        (SELECT COUNT(*) FROM ctrlProcesoElemento WHERE id_ejecucion = cp.id_ejecucion) AS total_elementos
    FROM ctrlProcesoCab cp
    INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
    WHERE
        (@id_proceso IS NULL OR cp.id_proceso = @id_proceso)
        AND (@fecha_inicio IS NULL OR cp.fecha_inicio >= @fecha_inicio)
        AND (@fecha_fin IS NULL OR cp.fecha_inicio <= @fecha_fin)
    ORDER BY cp.fecha_inicio DESC;
END;
GO

PRINT 'SP sp_ObtenerResumenEjecuciones creado.';
GO

-- =============================================
-- SP: Obtener Detalle de Ejecución
-- =============================================
CREATE OR ALTER PROCEDURE sp_ObtenerDetalleEjecucion
    @id_ejecucion INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Datos de cabecera
    SELECT
        cp.id_ejecucion,
        cp.id_proceso,
        ac.descripcion AS nombre_proceso,
        cp.registros_origen,
        cp.registros_destino,
        cp.fecha_inicio,
        cp.fecha_final,
        cp.duracion_segundos,
        cp.estatus,
        cp.mensaje_error
    FROM ctrlProcesoCab cp
    INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
    WHERE cp.id_ejecucion = @id_ejecucion;

    -- Pasos del proceso
    SELECT
        nombre_paso,
        secuencia,
        fecha_inicio_registro,
        fecha_final_registro,
        DATEDIFF(SECOND, fecha_inicio_registro, fecha_final_registro) AS duracion_segundos,
        parametros_registro,
        detalle,
        registros_procesados,
        estatus
    FROM ctrlProcesoDet
    WHERE id_ejecucion = @id_ejecucion
    ORDER BY secuencia;

    -- Logs
    SELECT
        nombre,
        log,
        nivel,
        fase,
        fecha_registro
    FROM ctrlLog
    WHERE id_ejecucion = @id_ejecucion
    ORDER BY fecha_registro;

    -- Elementos procesados (resumen)
    SELECT
        estatus,
        COUNT(*) AS total
    FROM ctrlProcesoElemento
    WHERE id_ejecucion = @id_ejecucion
    GROUP BY estatus;
END;
GO

PRINT 'SP sp_ObtenerDetalleEjecucion creado.';
GO

-- =============================================
-- SP: Limpiar Datos Antiguos
-- =============================================
CREATE OR ALTER PROCEDURE sp_LimpiarDatosAntiguos
    @dias_retencion INT = 90
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @fecha_corte DATETIME;
    SET @fecha_corte = DATEADD(DAY, -@dias_retencion, GETDATE());

    -- Eliminar ejecuciones antiguas (cascada eliminará detalles, logs y elementos)
    DELETE FROM ctrlProcesoCab
    WHERE fecha_inicio < @fecha_corte;

    SELECT @@ROWCOUNT AS registros_eliminados, 'Datos antiguos eliminados' AS mensaje;
END;
GO

PRINT 'SP sp_LimpiarDatosAntiguos creado.';
GO

PRINT '===================================';
PRINT 'Stored Procedures creados exitosamente.';
PRINT '===================================';
GO
