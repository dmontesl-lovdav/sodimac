-- Insertar Catálogos Principales
IF NOT EXISTS (SELECT 1 FROM catCatalogo WHERE nombre = 'PROCESOS_BATCH')
BEGIN
    INSERT INTO catCatalogo (nombre, descripcion, estatus, usuarioCreacion, fechaCreacion)
    VALUES
        ('PROCESOS_BATCH', 'Catálogo de procesos batch del sistema', 1, 1, GETDATE()),
        ('ESTATUS_EJECUCION', 'Estados de ejecución de procesos', 1, 1, GETDATE()),
        ('NIVEL_LOG', 'Niveles de severidad de logs', 1, 1, GETDATE()),
        ('FASE_PROCESO', 'Fases de ejecución de procesos', 1, 1, GETDATE()),
        ('TIPO_ELEMENTO', 'Tipos de elementos procesados', 1, 1, GETDATE());
END

-- Insertar Procesos Batch
IF NOT EXISTS (SELECT 1 FROM adminCatalogo WHERE idCatalogo = (SELECT idCatalogo FROM catCatalogo WHERE nombre = 'PROCESOS_BATCH') AND idElemento = 1)
BEGIN
    DECLARE @idCatalogoProcesos INT;
    SELECT @idCatalogoProcesos = idCatalogo FROM catCatalogo WHERE nombre = 'PROCESOS_BATCH';

    INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
    VALUES
        (@idCatalogoProcesos, 1, 'Sincronización de Acuerdos Comerciales (Rebates)', 1, 1, GETDATE(), 'REBATE_AGREEMENTS_SYNC'),
        (@idCatalogoProcesos, 2, 'Sincronización de Estado de Facturas', 1, 1, GETDATE(), 'INVOICE_STATUS_SYNC'),
        (@idCatalogoProcesos, 3, 'Sincronización de Proveedores', 1, 1, GETDATE(), 'VENDOR_SYNC'),
        (@idCatalogoProcesos, 4, 'Carga de Catálogo de Productos', 1, 1, GETDATE(), 'PRODUCT_CATALOG_LOAD');
END

-- Insertar Estatus de Ejecución
IF NOT EXISTS (SELECT 1 FROM adminCatalogo WHERE idCatalogo = (SELECT idCatalogo FROM catCatalogo WHERE nombre = 'ESTATUS_EJECUCION') AND idElemento = 1)
BEGIN
    DECLARE @idCatalogoEstatus INT;
    SELECT @idCatalogoEstatus = idCatalogo FROM catCatalogo WHERE nombre = 'ESTATUS_EJECUCION';

    INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
    VALUES
        (@idCatalogoEstatus, 1, 'En Progreso', 1, 1, GETDATE(), 'IN_PROGRESS'),
        (@idCatalogoEstatus, 2, 'Completado Exitosamente', 1, 1, GETDATE(), 'SUCCESS'),
        (@idCatalogoEstatus, 3, 'Fallido', 1, 1, GETDATE(), 'FAILED'),
        (@idCatalogoEstatus, 4, 'Completado Parcialmente', 1, 1, GETDATE(), 'PARTIAL'),
        (@idCatalogoEstatus, 5, 'Cancelado', 1, 1, GETDATE(), 'CANCELLED');
END

-- Insertar Niveles de Log
IF NOT EXISTS (SELECT 1 FROM adminCatalogo WHERE idCatalogo = (SELECT idCatalogo FROM catCatalogo WHERE nombre = 'NIVEL_LOG') AND idElemento = 1)
BEGIN
    DECLARE @idCatalogoNivel INT;
    SELECT @idCatalogoNivel = idCatalogo FROM catCatalogo WHERE nombre = 'NIVEL_LOG';

    INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
    VALUES
        (@idCatalogoNivel, 1, 'Información', 1, 1, GETDATE(), 'INFO'),
        (@idCatalogoNivel, 2, 'Advertencia', 1, 1, GETDATE(), 'WARN'),
        (@idCatalogoNivel, 3, 'Error', 1, 1, GETDATE(), 'ERROR'),
        (@idCatalogoNivel, 4, 'Debug', 1, 1, GETDATE(), 'DEBUG'),
        (@idCatalogoNivel, 5, 'Fatal', 1, 1, GETDATE(), 'FATAL');
END

-- Insertar Fases de Proceso
IF NOT EXISTS (SELECT 1 FROM adminCatalogo WHERE idCatalogo = (SELECT idCatalogo FROM catCatalogo WHERE nombre = 'FASE_PROCESO') AND idElemento = 1)
BEGIN
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
END

-- Insertar Tipos de Elemento
IF NOT EXISTS (SELECT 1 FROM adminCatalogo WHERE idCatalogo = (SELECT idCatalogo FROM catCatalogo WHERE nombre = 'TIPO_ELEMENTO') AND idElemento = 1)
BEGIN
    DECLARE @idCatalogoTipo INT;
    SELECT @idCatalogoTipo = idCatalogo FROM catCatalogo WHERE nombre = 'TIPO_ELEMENTO';

    INSERT INTO adminCatalogo (idCatalogo, idElemento, descripcion, estatus, usuarioCreacion, fechaCreacion, elementoConversion)
    VALUES
        (@idCatalogoTipo, 1, 'Contrato Comercial', 1, 1, GETDATE(), 'CONTRACT'),
        (@idCatalogoTipo, 2, 'Acuerdo de Rebate', 1, 1, GETDATE(), 'AGREEMENT'),
        (@idCatalogoTipo, 3, 'Factura', 1, 1, GETDATE(), 'INVOICE'),
        (@idCatalogoTipo, 4, 'Proveedor', 1, 1, GETDATE(), 'VENDOR'),
        (@idCatalogoTipo, 5, 'Producto', 1, 1, GETDATE(), 'PRODUCT');
END
