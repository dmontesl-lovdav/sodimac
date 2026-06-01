-- Tabla: catCatalogo
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'catCatalogo')
BEGIN
    CREATE TABLE catCatalogo
    (
        idCatalogo INT IDENTITY(1,1) NOT NULL,
        nombre VARCHAR(100) NOT NULL,
        descripcion VARCHAR(255) NULL,
        estatus INT NOT NULL DEFAULT 1,
        usuarioCreacion INT NULL,
        fechaCreacion DATETIME NOT NULL DEFAULT GETDATE(),
        usuarioActualizacion INT NULL,
        fechaActualizacion DATETIME NULL,
        CONSTRAINT PK_catCatalogo PRIMARY KEY CLUSTERED (idCatalogo),
        CONSTRAINT UQ_catCatalogo_nombre UNIQUE (nombre)
    );

    CREATE INDEX IX_catCatalogo_estatus ON catCatalogo(estatus);
END

-- Tabla: adminCatalogo
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'adminCatalogo')
BEGIN
    CREATE TABLE adminCatalogo
    (
        idCatalogo INT NOT NULL,
        idElemento INT NOT NULL,
        descripcion VARCHAR(50) NULL,
        estatus INT NOT NULL DEFAULT 1,
        usuarioCreacion INT NULL,
        fechaCreacion DATETIME NOT NULL DEFAULT GETDATE(),
        usuarioActualizacion INT NULL,
        fechaActualizacion DATETIME NULL,
        elementoConversion VARCHAR(50) NULL,
        CONSTRAINT PK_adminCatalogo PRIMARY KEY CLUSTERED (idCatalogo, idElemento),
        CONSTRAINT FK_adminCatalogo_catCatalogo
            FOREIGN KEY (idCatalogo) REFERENCES catCatalogo(idCatalogo)
    );

    CREATE INDEX IX_adminCatalogo_estatus ON adminCatalogo(estatus);
    CREATE INDEX IX_adminCatalogo_idCatalogo ON adminCatalogo(idCatalogo);
END

-- Tabla: ctrlProcesoCab
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ctrlProcesoCab')
BEGIN
    CREATE TABLE ctrlProcesoCab
    (
        id_ejecucion INT IDENTITY(1,1) NOT NULL,
        id_proceso INT NOT NULL,
        registros_origen INT NULL DEFAULT 0,
        registros_destino INT NULL DEFAULT 0,
        fecha_inicio DATETIME NOT NULL DEFAULT GETDATE(),
        fecha_final DATETIME NULL,
        estatus VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
        duracion_segundos AS DATEDIFF(SECOND, fecha_inicio, fecha_final) PERSISTED,
        mensaje_error VARCHAR(1000) NULL,
        CONSTRAINT PK_ctrlProcesoCab PRIMARY KEY CLUSTERED (id_ejecucion)
    );

    CREATE INDEX IX_ctrlProcesoCab_id_proceso ON ctrlProcesoCab(id_proceso);
    CREATE INDEX IX_ctrlProcesoCab_fecha_inicio ON ctrlProcesoCab(fecha_inicio DESC);
    CREATE INDEX IX_ctrlProcesoCab_estatus ON ctrlProcesoCab(estatus);
    CREATE INDEX IX_ctrlProcesoCab_fecha_inicio_estatus ON ctrlProcesoCab(fecha_inicio DESC, estatus);
END

-- Tabla: ctrlProcesoDet
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ctrlProcesoDet')
BEGIN
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
        estatus VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
        CONSTRAINT PK_ctrlProcesoDet PRIMARY KEY CLUSTERED (id_flujo),
        CONSTRAINT FK_ctrlProcesoDet_ctrlProcesoCab
            FOREIGN KEY (id_ejecucion) REFERENCES ctrlProcesoCab(id_ejecucion)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_ctrlProcesoDet_id_ejecucion ON ctrlProcesoDet(id_ejecucion);
    CREATE INDEX IX_ctrlProcesoDet_secuencia ON ctrlProcesoDet(id_ejecucion, secuencia);
END

-- Tabla: ctrlLog
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ctrlLog')
BEGIN
    CREATE TABLE ctrlLog
    (
        id_log INT IDENTITY(1,1) NOT NULL,
        nombre VARCHAR(100) NOT NULL,
        id_ejecucion INT NULL,
        log TEXT NULL,
        nivel VARCHAR(20) NOT NULL DEFAULT 'INFO',
        fase VARCHAR(50) NULL,
        fecha_registro DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_ctrlLog PRIMARY KEY CLUSTERED (id_log),
        CONSTRAINT FK_ctrlLog_ctrlProcesoCab
            FOREIGN KEY (id_ejecucion) REFERENCES ctrlProcesoCab(id_ejecucion)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_ctrlLog_id_ejecucion ON ctrlLog(id_ejecucion);
    CREATE INDEX IX_ctrlLog_fecha_registro ON ctrlLog(fecha_registro DESC);
    CREATE INDEX IX_ctrlLog_nivel ON ctrlLog(nivel);
    CREATE INDEX IX_ctrlLog_fecha_nivel ON ctrlLog(fecha_registro DESC, nivel);
END

-- Tabla: ctrlProcesoElemento
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ctrlProcesoElemento')
BEGIN
    CREATE TABLE ctrlProcesoElemento
    (
        idElemento INT IDENTITY(1,1) NOT NULL,
        id_ejecucion INT NOT NULL,
        valor VARCHAR(50) NOT NULL,
        valorAlterno VARCHAR(250) NULL,
        secuencia INT NOT NULL,
        estatus VARCHAR(20) NOT NULL DEFAULT 'PROCESSED',
        fechaRegistro DATETIME NOT NULL DEFAULT GETDATE(),
        detalle_error VARCHAR(500) NULL,
        CONSTRAINT PK_ctrlProcesoElemento PRIMARY KEY CLUSTERED (idElemento),
        CONSTRAINT FK_ctrlProcesoElemento_ctrlProcesoCab
            FOREIGN KEY (id_ejecucion) REFERENCES ctrlProcesoCab(id_ejecucion)
            ON DELETE CASCADE
    );

    CREATE INDEX IX_ctrlProcesoElemento_id_ejecucion ON ctrlProcesoElemento(id_ejecucion);
    CREATE INDEX IX_ctrlProcesoElemento_valor ON ctrlProcesoElemento(valor);
    CREATE INDEX IX_ctrlProcesoElemento_estatus ON ctrlProcesoElemento(estatus);
END
