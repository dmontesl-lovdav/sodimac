-- =============================================
-- Script: Vistas para SODIMAC_BATCH_DEV
-- Propósito: Vistas para monitoreo y reportes
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE SODIMAC_BATCH_DEV;
GO

-- =============================================
-- Vista: vw_EjecucionesRecientes
-- Descripción: Muestra las últimas 100 ejecuciones
-- =============================================
CREATE OR ALTER VIEW vw_EjecucionesRecientes
AS
SELECT TOP 100
    cp.id_ejecucion,
    cp.id_proceso,
    ac.descripcion AS nombre_proceso,
    cp.registros_origen,
    cp.registros_destino,
    CASE
        WHEN cp.registros_origen > 0
        THEN CAST((cp.registros_destino * 100.0 / cp.registros_origen) AS DECIMAL(5,2))
        ELSE 0
    END AS porcentaje_carga,
    cp.fecha_inicio,
    cp.fecha_final,
    cp.duracion_segundos,
    cp.estatus,
    CASE cp.estatus
        WHEN 'SUCCESS' THEN 'Exitoso'
        WHEN 'FAILED' THEN 'Fallido'
        WHEN 'IN_PROGRESS' THEN 'En Progreso'
        WHEN 'PARTIAL' THEN 'Parcial'
        ELSE 'Desconocido'
    END AS estatus_descripcion,
    cp.mensaje_error
FROM ctrlProcesoCab cp
INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
ORDER BY cp.fecha_inicio DESC;
GO

PRINT 'Vista vw_EjecucionesRecientes creada.';
GO

-- =============================================
-- Vista: vw_EstadisticasPorProceso
-- Descripción: Estadísticas agrupadas por proceso
-- =============================================
CREATE OR ALTER VIEW vw_EstadisticasPorProceso
AS
SELECT
    cp.id_proceso,
    ac.descripcion AS nombre_proceso,
    COUNT(*) AS total_ejecuciones,
    SUM(CASE WHEN cp.estatus = 'SUCCESS' THEN 1 ELSE 0 END) AS ejecuciones_exitosas,
    SUM(CASE WHEN cp.estatus = 'FAILED' THEN 1 ELSE 0 END) AS ejecuciones_fallidas,
    SUM(CASE WHEN cp.estatus = 'PARTIAL' THEN 1 ELSE 0 END) AS ejecuciones_parciales,
    AVG(cp.duracion_segundos) AS duracion_promedio_segundos,
    MIN(cp.fecha_inicio) AS primera_ejecucion,
    MAX(cp.fecha_inicio) AS ultima_ejecucion,
    SUM(cp.registros_origen) AS total_registros_origen,
    SUM(cp.registros_destino) AS total_registros_destino
FROM ctrlProcesoCab cp
INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
WHERE cp.estatus <> 'IN_PROGRESS'
GROUP BY cp.id_proceso, ac.descripcion;
GO

PRINT 'Vista vw_EstadisticasPorProceso creada.';
GO

-- =============================================
-- Vista: vw_LogsErrores
-- Descripción: Muestra todos los logs de error
-- =============================================
CREATE OR ALTER VIEW vw_LogsErrores
AS
SELECT
    cl.id_log,
    cl.nombre,
    cl.id_ejecucion,
    ac.descripcion AS nombre_proceso,
    cl.log,
    cl.nivel,
    cl.fase,
    cl.fecha_registro
FROM ctrlLog cl
LEFT JOIN ctrlProcesoCab cp ON cl.id_ejecucion = cp.id_ejecucion
LEFT JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
WHERE cl.nivel IN ('ERROR', 'FATAL');
GO

PRINT 'Vista vw_LogsErrores creada.';
GO

-- =============================================
-- Vista: vw_EjecucionesEnCurso
-- Descripción: Muestra ejecuciones actualmente en progreso
-- =============================================
CREATE OR ALTER VIEW vw_EjecucionesEnCurso
AS
SELECT
    cp.id_ejecucion,
    cp.id_proceso,
    ac.descripcion AS nombre_proceso,
    cp.fecha_inicio,
    DATEDIFF(MINUTE, cp.fecha_inicio, GETDATE()) AS minutos_transcurridos,
    cp.registros_origen,
    cp.registros_destino,
    (SELECT COUNT(*) FROM ctrlProcesoDet WHERE id_ejecucion = cp.id_ejecucion) AS pasos_completados,
    (SELECT COUNT(*) FROM ctrlLog WHERE id_ejecucion = cp.id_ejecucion AND nivel = 'ERROR') AS errores_detectados
FROM ctrlProcesoCab cp
INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
WHERE cp.estatus = 'IN_PROGRESS';
GO

PRINT 'Vista vw_EjecucionesEnCurso creada.';
GO

-- =============================================
-- Vista: vw_RendimientoDiario
-- Descripción: Rendimiento de procesos por día
-- =============================================
CREATE OR ALTER VIEW vw_RendimientoDiario
AS
SELECT
    CAST(cp.fecha_inicio AS DATE) AS fecha,
    cp.id_proceso,
    ac.descripcion AS nombre_proceso,
    COUNT(*) AS total_ejecuciones,
    SUM(CASE WHEN cp.estatus = 'SUCCESS' THEN 1 ELSE 0 END) AS exitosas,
    SUM(CASE WHEN cp.estatus = 'FAILED' THEN 1 ELSE 0 END) AS fallidas,
    AVG(cp.duracion_segundos) AS duracion_promedio_segundos,
    SUM(cp.registros_origen) AS total_registros_origen,
    SUM(cp.registros_destino) AS total_registros_destino
FROM ctrlProcesoCab cp
INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
WHERE cp.estatus <> 'IN_PROGRESS'
GROUP BY CAST(cp.fecha_inicio AS DATE), cp.id_proceso, ac.descripcion;
GO

PRINT 'Vista vw_RendimientoDiario creada.';
GO

-- =============================================
-- Vista: vw_ElementosFallidos
-- Descripción: Elementos que fallaron en el procesamiento
-- =============================================
CREATE OR ALTER VIEW vw_ElementosFallidos
AS
SELECT
    cpe.idElemento,
    cpe.id_ejecucion,
    ac.descripcion AS nombre_proceso,
    cp.fecha_inicio AS fecha_ejecucion,
    cpe.valor,
    cpe.valorAlterno,
    cpe.secuencia,
    cpe.detalle_error,
    cpe.fechaRegistro
FROM ctrlProcesoElemento cpe
INNER JOIN ctrlProcesoCab cp ON cpe.id_ejecucion = cp.id_ejecucion
INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
WHERE cpe.estatus = 'FAILED';
GO

PRINT 'Vista vw_ElementosFallidos creada.';
GO

-- =============================================
-- Vista: vw_ResumenHoy
-- Descripción: Resumen de ejecuciones del día actual
-- =============================================
CREATE OR ALTER VIEW vw_ResumenHoy
AS
SELECT
    cp.id_proceso,
    ac.descripcion AS nombre_proceso,
    COUNT(*) AS total_ejecuciones,
    SUM(CASE WHEN cp.estatus = 'SUCCESS' THEN 1 ELSE 0 END) AS exitosas,
    SUM(CASE WHEN cp.estatus = 'FAILED' THEN 1 ELSE 0 END) AS fallidas,
    SUM(CASE WHEN cp.estatus = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS en_progreso,
    AVG(cp.duracion_segundos) AS duracion_promedio_segundos,
    MAX(cp.fecha_inicio) AS ultima_ejecucion
FROM ctrlProcesoCab cp
INNER JOIN adminCatalogo ac ON cp.id_proceso = ac.idElemento
WHERE CAST(cp.fecha_inicio AS DATE) = CAST(GETDATE() AS DATE)
GROUP BY cp.id_proceso, ac.descripcion;
GO

PRINT 'Vista vw_ResumenHoy creada.';
GO

PRINT '===================================';
PRINT 'Vistas creadas exitosamente.';
PRINT '===================================';
GO
