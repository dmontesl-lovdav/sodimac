-- =============================================
-- Script: Creación de Tablas en SODIMAC_BATCH_DEV
-- Propósito: Tablas para monitoreo, trazabilidad y auditoría
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE SODIMAC_BATCH_DEV;
GO

-- =============================================
-- Tabla: catCatalogo
-- Descripción: Almacena o registra todos los catálogos que utiliza el sistema
-- =============================================
CREATE TABLE catCatalogo
(
    idCatalogo INT IDENTITY(1,1) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    estatus INT NOT NULL DEFAULT 1, -- 1=Activo, 0=Inactivo
    usuarioCreacion INT NULL,
    fechaCreacion DATETIME NOT NULL DEFAULT GETDATE(),
    usuarioActualizacion INT NULL,
    fechaActualizacion DATETIME NULL,
    CONSTRAINT PK_catCatalogo PRIMARY KEY CLUSTERED (idCatalogo),
    CONSTRAINT UQ_catCatalogo_nombre UNIQUE (nombre)
);
GO

CREATE INDEX IX_catCatalogo_estatus ON catCatalogo(estatus);
GO

PRINT 'Tabla catCatalogo creada exitosamente.';
GO

-- =============================================
-- Tabla: adminCatalogo
-- Descripción: Almacena todos los valores utilizados en los catálogos configurados
-- Relación: catCatalogo(1) -> adminCatalogo(N)
-- =============================================
CREATE TABLE adminCatalogo
(
    idCatalogo INT NOT NULL,
    idElemento INT NOT NULL,
    descripcion VARCHAR(50) NULL,
    estatus INT NOT NULL DEFAULT 1, -- 1=Activo, 0=Inactivo
    usuarioCreacion INT NULL,
    fechaCreacion DATETIME NOT NULL DEFAULT GETDATE(),
    usuarioActualizacion INT NULL,
    fechaActualizacion DATETIME NULL,
    elementoConversion VARCHAR(50) NULL,
    CONSTRAINT PK_adminCatalogo PRIMARY KEY CLUSTERED (idCatalogo, idElemento),
    CONSTRAINT FK_adminCatalogo_catCatalogo
        FOREIGN KEY (idCatalogo) REFERENCES catCatalogo(idCatalogo)
);
GO

CREATE INDEX IX_adminCatalogo_estatus ON adminCatalogo(estatus);
CREATE INDEX IX_adminCatalogo_idCatalogo ON adminCatalogo(idCatalogo);
GO

PRINT 'Tabla adminCatalogo creada exitosamente.';
GO

-- =============================================
-- Tabla: ctrlProcesoCab
-- Descripción: Control de ejecución y cifras origen-destino
-- =============================================
CREATE TABLE ctrlProcesoCab
(
    id_ejecucion INT IDENTITY(1,1) NOT NULL,
    id_proceso INT NOT NULL, -- Relación con adminCatalogo
    registros_origen INT NULL DEFAULT 0,
    registros_destino INT NULL DEFAULT 0,
    fecha_inicio DATETIME NOT NULL DEFAULT GETDATE(),
    fecha_final DATETIME NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, SUCCESS, FAILED, PARTIAL
    duracion_segundos AS DATEDIFF(SECOND, fecha_inicio, fecha_final) PERSISTED,
    mensaje_error VARCHAR(1000) NULL,
    CONSTRAINT PK_ctrlProcesoCab PRIMARY KEY CLUSTERED (id_ejecucion)
);
GO

CREATE INDEX IX_ctrlProcesoCab_id_proceso ON ctrlProcesoCab(id_proceso);
CREATE INDEX IX_ctrlProcesoCab_fecha_inicio ON ctrlProcesoCab(fecha_inicio DESC);
CREATE INDEX IX_ctrlProcesoCab_estatus ON ctrlProcesoCab(estatus);
CREATE INDEX IX_ctrlProcesoCab_fecha_inicio_estatus ON ctrlProcesoCab(fecha_inicio DESC, estatus);
GO

PRINT 'Tabla ctrlProcesoCab creada exitosamente.';
GO

-- =============================================
-- Tabla: ctrlProcesoDet
-- Descripción: Secuencia de pasos del proceso
-- Relación: ctrlProcesoCab(1) -> ctrlProcesoDet(N)
-- =============================================
CREATE TABLE ctrlProcesoDet
(
    id_flujo INT IDENTITY(1,1) NOT NULL,
    id_ejecucion INT NOT NULL,
    nombre_paso VARCHAR(100) NOT NULL,
    secuencia INT NOT NULL,
    fecha_inicio_registro DATETIME NOT NULL DEFAULT GETDATE(),
    fecha_final_registro DATETIME NULL,
    parametros_registro VARCHAR(255) NULL,
    detalle VARCHAR(255) NULL,
    registros_procesados INT NULL DEFAULT 0,
    estatus VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, SUCCESS, FAILED
    CONSTRAINT PK_ctrlProcesoDet PRIMARY KEY CLUSTERED (id_flujo),
    CONSTRAINT FK_ctrlProcesoDet_ctrlProcesoCab
        FOREIGN KEY (id_ejecucion) REFERENCES ctrlProcesoCab(id_ejecucion)
        ON DELETE CASCADE
);
GO

CREATE INDEX IX_ctrlProcesoDet_id_ejecucion ON ctrlProcesoDet(id_ejecucion);
CREATE INDEX IX_ctrlProcesoDet_secuencia ON ctrlProcesoDet(id_ejecucion, secuencia);
GO

PRINT 'Tabla ctrlProcesoDet creada exitosamente.';
GO

-- =============================================
-- Tabla: ctrlLog
-- Descripción: Logs técnicos y funcionales
-- Relación: ctrlProcesoCab(1) -> ctrlLog(N)
-- =============================================
CREATE TABLE ctrlLog
(
    id_log INT IDENTITY(1,1) NOT NULL,
    nombre VARCHAR(100) NOT NULL, -- Tipo o nombre del evento
    id_ejecucion INT NULL, -- Proceso relacionado (puede ser NULL para logs globales)
    log TEXT NULL,
    nivel VARCHAR(20) NOT NULL DEFAULT 'INFO', -- INFO, WARN, ERROR, DEBUG
    fase VARCHAR(50) NULL, -- EXTRACT, TRANSFORM, LOAD, VALIDATION, etc.
    fecha_registro DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_ctrlLog PRIMARY KEY CLUSTERED (id_log),
    CONSTRAINT FK_ctrlLog_ctrlProcesoCab
        FOREIGN KEY (id_ejecucion) REFERENCES ctrlProcesoCab(id_ejecucion)
        ON DELETE CASCADE
);
GO

CREATE INDEX IX_ctrlLog_id_ejecucion ON ctrlLog(id_ejecucion);
CREATE INDEX IX_ctrlLog_fecha_registro ON ctrlLog(fecha_registro DESC);
CREATE INDEX IX_ctrlLog_nivel ON ctrlLog(nivel);
CREATE INDEX IX_ctrlLog_fecha_nivel ON ctrlLog(fecha_registro DESC, nivel);
GO

PRINT 'Tabla ctrlLog creada exitosamente.';
GO

-- =============================================
-- Tabla: ctrlProcesoElemento
-- Descripción: Llevar el control de todos los documentos o valores procesados
-- Relación: ctrlProcesoCab(1) -> ctrlProcesoElemento(N)
-- =============================================
CREATE TABLE ctrlProcesoElemento
(
    idElemento INT IDENTITY(1,1) NOT NULL,
    id_ejecucion INT NOT NULL,
    valor VARCHAR(50) NOT NULL, -- Valor único del proceso (ej: contract_id, agreement_id)
    valorAlterno VARCHAR(250) NULL, -- Valor alterno o de conversión
    secuencia INT NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'PROCESSED', -- PROCESSED, FAILED, SKIPPED
    fechaRegistro DATETIME NOT NULL DEFAULT GETDATE(),
    detalle_error VARCHAR(500) NULL,
    CONSTRAINT PK_ctrlProcesoElemento PRIMARY KEY CLUSTERED (idElemento),
    CONSTRAINT FK_ctrlProcesoElemento_ctrlProcesoCab
        FOREIGN KEY (id_ejecucion) REFERENCES ctrlProcesoCab(id_ejecucion)
        ON DELETE CASCADE
);
GO

CREATE INDEX IX_ctrlProcesoElemento_id_ejecucion ON ctrlProcesoElemento(id_ejecucion);
CREATE INDEX IX_ctrlProcesoElemento_valor ON ctrlProcesoElemento(valor);
CREATE INDEX IX_ctrlProcesoElemento_estatus ON ctrlProcesoElemento(estatus);
GO

PRINT 'Tabla ctrlProcesoElemento creada exitosamente.';
GO

PRINT 'Todas las tablas han sido creadas exitosamente.';
GO
