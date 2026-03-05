-- SODIMAC_BATCH_DEV.dbo.catCatalogo definition

-- Drop table

-- DROP TABLE SODIMAC_BATCH_DEV.dbo.catCatalogo;

CREATE TABLE SODIMAC_BATCH_DEV.dbo.catCatalogo (
	idCatalogo int IDENTITY(1,1) NOT NULL,
	nombre varchar(100) COLLATE Modern_Spanish_CI_AS NOT NULL,
	descripcion varchar(255) COLLATE Modern_Spanish_CI_AS NULL,
	estatus int DEFAULT 1 NOT NULL,
	usuarioCreacion int NULL,
	fechaCreacion datetime DEFAULT getdate() NOT NULL,
	usuarioActualizacion int NULL,
	fechaActualizacion datetime NULL,
	CONSTRAINT PK_catCatalogo PRIMARY KEY (idCatalogo),
	CONSTRAINT UQ_catCatalogo_nombre UNIQUE (nombre)
);
 CREATE NONCLUSTERED INDEX IX_catCatalogo_estatus ON SODIMAC_BATCH_DEV.dbo.catCatalogo (  estatus ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab definition

-- Drop table

-- DROP TABLE SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab;

CREATE TABLE SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab (
	id_ejecucion int IDENTITY(1,1) NOT NULL,
	id_proceso int NOT NULL,
	registros_origen int DEFAULT 0 NULL,
	registros_destino int DEFAULT 0 NULL,
	fecha_inicio datetime DEFAULT getdate() NOT NULL,
	fecha_final datetime NULL,
	estatus varchar(20) COLLATE Modern_Spanish_CI_AS DEFAULT 'IN_PROGRESS' NOT NULL,
	duracion_segundos AS (datediff(second,[fecha_inicio],[fecha_final])) PERSISTED,
	mensaje_error varchar(1000) COLLATE Modern_Spanish_CI_AS NULL,
	CONSTRAINT PK_ctrlProcesoCab PRIMARY KEY (id_ejecucion)
);
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoCab_estatus ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab (  estatus ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoCab_fecha_inicio ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab (  fecha_inicio DESC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoCab_fecha_inicio_estatus ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab (  fecha_inicio DESC  , estatus ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoCab_id_proceso ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab (  id_proceso ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- SODIMAC_BATCH_DEV.dbo.adminCatalogo definition

-- Drop table

-- DROP TABLE SODIMAC_BATCH_DEV.dbo.adminCatalogo;

CREATE TABLE SODIMAC_BATCH_DEV.dbo.adminCatalogo (
	idCatalogo int NOT NULL,
	idElemento int NOT NULL,
	descripcion varchar(50) COLLATE Modern_Spanish_CI_AS NULL,
	estatus int DEFAULT 1 NOT NULL,
	usuarioCreacion int NULL,
	fechaCreacion datetime DEFAULT getdate() NOT NULL,
	usuarioActualizacion int NULL,
	fechaActualizacion datetime NULL,
	elementoConversion varchar(50) COLLATE Modern_Spanish_CI_AS NULL,
	CONSTRAINT PK_adminCatalogo PRIMARY KEY (idCatalogo,idElemento),
	CONSTRAINT FK_adminCatalogo_catCatalogo FOREIGN KEY (idCatalogo) REFERENCES SODIMAC_BATCH_DEV.dbo.catCatalogo(idCatalogo)
);
 CREATE NONCLUSTERED INDEX IX_adminCatalogo_estatus ON SODIMAC_BATCH_DEV.dbo.adminCatalogo (  estatus ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_adminCatalogo_idCatalogo ON SODIMAC_BATCH_DEV.dbo.adminCatalogo (  idCatalogo ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- SODIMAC_BATCH_DEV.dbo.ctrlLog definition

-- Drop table

-- DROP TABLE SODIMAC_BATCH_DEV.dbo.ctrlLog;

CREATE TABLE SODIMAC_BATCH_DEV.dbo.ctrlLog (
	id_log int IDENTITY(1,1) NOT NULL,
	nombre varchar(100) COLLATE Modern_Spanish_CI_AS NOT NULL,
	id_ejecucion int NULL,
	log text COLLATE Modern_Spanish_CI_AS NULL,
	nivel varchar(20) COLLATE Modern_Spanish_CI_AS DEFAULT 'INFO' NOT NULL,
	fase varchar(50) COLLATE Modern_Spanish_CI_AS NULL,
	fecha_registro datetime DEFAULT getdate() NOT NULL,
	CONSTRAINT PK_ctrlLog PRIMARY KEY (id_log),
	CONSTRAINT FK_ctrlLog_ctrlProcesoCab FOREIGN KEY (id_ejecucion) REFERENCES SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab(id_ejecucion) ON DELETE CASCADE
);
 CREATE NONCLUSTERED INDEX IX_ctrlLog_fecha_nivel ON SODIMAC_BATCH_DEV.dbo.ctrlLog (  fecha_registro DESC  , nivel ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlLog_fecha_registro ON SODIMAC_BATCH_DEV.dbo.ctrlLog (  fecha_registro DESC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlLog_id_ejecucion ON SODIMAC_BATCH_DEV.dbo.ctrlLog (  id_ejecucion ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlLog_nivel ON SODIMAC_BATCH_DEV.dbo.ctrlLog (  nivel ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet definition

-- Drop table

-- DROP TABLE SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet;

CREATE TABLE SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet (
	id_flujo int IDENTITY(1,1) NOT NULL,
	id_ejecucion int NOT NULL,
	nombre_paso varchar(100) COLLATE Modern_Spanish_CI_AS NOT NULL,
	secuencia int NOT NULL,
	fecha_inicio_registro datetime DEFAULT getdate() NOT NULL,
	fecha_final_registro datetime NULL,
	parametros_registro varchar(255) COLLATE Modern_Spanish_CI_AS NULL,
	detalle varchar(255) COLLATE Modern_Spanish_CI_AS NULL,
	registros_procesados int DEFAULT 0 NULL,
	estatus varchar(20) COLLATE Modern_Spanish_CI_AS DEFAULT 'IN_PROGRESS' NOT NULL,
	CONSTRAINT PK_ctrlProcesoDet PRIMARY KEY (id_flujo),
	CONSTRAINT FK_ctrlProcesoDet_ctrlProcesoCab FOREIGN KEY (id_ejecucion) REFERENCES SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab(id_ejecucion) ON DELETE CASCADE
);
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoDet_id_ejecucion ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet (  id_ejecucion ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoDet_secuencia ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet (  id_ejecucion ASC  , secuencia ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- SODIMAC_BATCH_DEV.dbo.ctrlProcesoElemento definition

-- Drop table

-- DROP TABLE SODIMAC_BATCH_DEV.dbo.ctrlProcesoElemento;

CREATE TABLE SODIMAC_BATCH_DEV.dbo.ctrlProcesoElemento (
	idElemento int IDENTITY(1,1) NOT NULL,
	id_ejecucion int NOT NULL,
	valor varchar(50) COLLATE Modern_Spanish_CI_AS NOT NULL,
	valorAlterno varchar(250) COLLATE Modern_Spanish_CI_AS NULL,
	secuencia int NOT NULL,
	estatus varchar(20) COLLATE Modern_Spanish_CI_AS DEFAULT 'PROCESSED' NOT NULL,
	fechaRegistro datetime DEFAULT getdate() NOT NULL,
	detalle_error varchar(500) COLLATE Modern_Spanish_CI_AS NULL,
	CONSTRAINT PK_ctrlProcesoElemento PRIMARY KEY (idElemento),
	CONSTRAINT FK_ctrlProcesoElemento_ctrlProcesoCab FOREIGN KEY (id_ejecucion) REFERENCES SODIMAC_BATCH_DEV.dbo.ctrlProcesoCab(id_ejecucion) ON DELETE CASCADE
);
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoElemento_estatus ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoElemento (  estatus ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoElemento_id_ejecucion ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoElemento (  id_ejecucion ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX IX_ctrlProcesoElemento_valor ON SODIMAC_BATCH_DEV.dbo.ctrlProcesoElemento (  valor ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;