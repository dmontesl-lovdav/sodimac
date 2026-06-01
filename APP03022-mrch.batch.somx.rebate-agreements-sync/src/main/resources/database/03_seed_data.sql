-- =============================================
-- Script: Datos Iniciales (Seed Data) para SODIMAC_BATCH_DEV
-- Propósito: Insertar catálogos y configuraciones iniciales
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE SODIMAC_BATCH_DEV;
GO

-- =============================================
-- Insertar Catálogos Principales
-- =============================================

-- Catálogo de Procesos Batch
INSERT INTO catCatalogo (nombre, descripcion, estatus, usuarioCreacion, fechaCreacion)
VALUES
    ('PROCESOS_BATCH', 'Catálogo de procesos batch del sistema', 1, 1, GETDATE()),
    ('ESTATUS_EJECUCION', 'Estados de ejecución de procesos', 1, 1, GETDATE()),
    ('NIVEL_LOG', 'Niveles de severidad de logs', 1, 1, GETDATE()),
    ('FASE_PROCESO', 'Fases de ejecución de procesos', 1, 1, GETDATE()),
    ('TIPO_ELEMENTO', 'Tipos de elementos procesados', 1, 1, GETDATE());
GO

PRINT 'Catálogos principales insertados.';
GO

-- =============================================
-- Insertar Procesos Batch
-- =============================================

DECLARE @idCatalogoProcesos INT;
SELECT @idCatalogoProcesos = idCatalogo FROM catCatalogo WHERE nombre = 'PROCESOS_BATCH';

INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
VALUES
    (@idCatalogoProcesos, 1, 'Sincronización de Acuerdos Comerciales (Rebates)', 1, 1, GETDATE(), 'REBATE_AGREEMENTS_SYNC'),
    (@idCatalogoProcesos, 2, 'Sincronización de Estado de Facturas', 1, 1, GETDATE(), 'INVOICE_STATUS_SYNC'),
    (@idCatalogoProcesos, 3, 'Sincronización de Proveedores', 1, 1, GETDATE(), 'VENDOR_SYNC'),
    (@idCatalogoProcesos, 4, 'Carga de Catálogo de Productos', 1, 1, GETDATE(), 'PRODUCT_CATALOG_LOAD');
GO

PRINT 'Procesos batch insertados.';
GO

-- =============================================
-- Insertar Estatus de Ejecución
-- =============================================

DECLARE @idCatalogoEstatus INT;
SELECT @idCatalogoEstatus = idCatalogo FROM catCatalogo WHERE nombre = 'ESTATUS_EJECUCION';

INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
VALUES
    (@idCatalogoEstatus, 1, 'En Progreso', 1, 1, GETDATE(), 'IN_PROGRESS'),
    (@idCatalogoEstatus, 2, 'Completado Exitosamente', 1, 1, GETDATE(), 'SUCCESS'),
    (@idCatalogoEstatus, 3, 'Fallido', 1, 1, GETDATE(), 'FAILED'),
    (@idCatalogoEstatus, 4, 'Completado Parcialmente', 1, 1, GETDATE(), 'PARTIAL'),
    (@idCatalogoEstatus, 5, 'Cancelado', 1, 1, GETDATE(), 'CANCELLED');
GO

PRINT 'Estatus de ejecución insertados.';
GO

-- =============================================
-- Insertar Niveles de Log
-- =============================================

DECLARE @idCatalogoNivel INT;
SELECT @idCatalogoNivel = idCatalogo FROM catCatalogo WHERE nombre = 'NIVEL_LOG';

INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
VALUES
    (@idCatalogoNivel, 1, 'Información', 1, 1, GETDATE(), 'INFO'),
    (@idCatalogoNivel, 2, 'Advertencia', 1, 1, GETDATE(), 'WARN'),
    (@idCatalogoNivel, 3, 'Error', 1, 1, GETDATE(), 'ERROR'),
    (@idCatalogoNivel, 4, 'Debug', 1, 1, GETDATE(), 'DEBUG'),
    (@idCatalogoNivel, 5, 'Fatal', 1, 1, GETDATE(), 'FATAL');
GO

PRINT 'Niveles de log insertados.';
GO

-- =============================================
-- Insertar Fases de Proceso
-- =============================================

DECLARE @idCatalogoFase INT;
SELECT @idCatalogoFase = idCatalogo FROM catCatalogo WHERE nombre = 'FASE_PROCESO';

INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
VALUES
    (@idCatalogoFase, 1, 'Extracción de Datos', 1, 1, GETDATE(), 'EXTRACT'),
    (@idCatalogoFase, 2, 'Transformación de Datos', 1, 1, GETDATE(), 'TRANSFORM'),
    (@idCatalogoFase, 3, 'Carga de Datos', 1, 1, GETDATE(), 'LOAD'),
    (@idCatalogoFase, 4, 'Validación', 1, 1, GETDATE(), 'VALIDATION'),
    (@idCatalogoFase, 5, 'Inicio de Proceso', 1, 1, GETDATE(), 'START'),
    (@idCatalogoFase, 6, 'Fin de Proceso', 1, 1, GETDATE(), 'END'),
    (@idCatalogoFase, 7, 'Limpieza de Datos', 1, 1, GETDATE(), 'CLEANUP'),
    (@idCatalogoFase, 8, 'Notificación', 1, 1, GETDATE(), 'NOTIFICATION');
GO

PRINT 'Fases de proceso insertadas.';
GO

-- =============================================
-- Insertar Tipos de Elemento
-- =============================================

DECLARE @idCatalogoTipo INT;
SELECT @idCatalogoTipo = idCatalogo FROM catCatalogo WHERE nombre = 'TIPO_ELEMENTO';

INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
VALUES
    (@idCatalogoTipo, 1, 'Contrato Comercial', 1, 1, GETDATE(), 'CONTRACT'),
    (@idCatalogoTipo, 2, 'Acuerdo de Rebate', 1, 1, GETDATE(), 'AGREEMENT'),
    (@idCatalogoTipo, 3, 'Factura', 1, 1, GETDATE(), 'INVOICE'),
    (@idCatalogoTipo, 4, 'Proveedor', 1, 1, GETDATE(), 'VENDOR'),
    (@idCatalogoTipo, 5, 'Producto', 1, 1, GETDATE(), 'PRODUCT');
GO

PRINT 'Tipos de elemento insertados.';
GO

-- =============================================
-- Verificar datos insertados
-- =============================================

PRINT '===================================';
PRINT 'Resumen de datos insertados:';
PRINT '===================================';

SELECT
    c.nombre AS Catalogo,
    COUNT(a.idElemento) AS TotalElementos
FROM catCatalogo c
LEFT JOIN adminCatalogo a ON c.idCatalogo = a.idCatalogo
GROUP BY c.nombre
ORDER BY c.nombre;

PRINT '===================================';
PRINT 'Datos iniciales insertados exitosamente.';
PRINT '===================================';
GO
