INSERT INTO SODIMAC_BATCH_DEV.dbo.adminCatalogo (idCatalogo,idElemento,descripcion,estatus,usuarioCreacion,fechaCreacion,usuarioActualizacion,fechaActualizacion,elementoConversion) VALUES
	 (1,1,N'Sincronización de Acuerdos Comerciales (Rebates)',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'REBATE_AGREEMENTS_SYNC'),
	 (1,2,N'Sincronización de Estado de Facturas',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'INVOICE_STATUS_SYNC'),
	 (1,3,N'Sincronización de Proveedores',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'VENDOR_SYNC'),
	 (1,4,N'Carga de Catálogo de Productos',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'PRODUCT_CATALOG_LOAD'),
	 (2,1,N'En Progreso',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'IN_PROGRESS'),
	 (2,2,N'Completado Exitosamente',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'SUCCESS'),
	 (2,3,N'Fallido',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'FAILED'),
	 (2,4,N'Completado Parcialmente',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'PARTIAL'),
	 (2,5,N'Cancelado',1,1,'2026-01-21 12:05:02.487',NULL,NULL,N'CANCELLED'),
	 (3,1,N'Información',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'INFO');
INSERT INTO SODIMAC_BATCH_DEV.dbo.adminCatalogo (idCatalogo,idElemento,descripcion,estatus,usuarioCreacion,fechaCreacion,usuarioActualizacion,fechaActualizacion,elementoConversion) VALUES
	 (3,2,N'Advertencia',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'WARN'),
	 (3,3,N'Error',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'ERROR'),
	 (3,4,N'Debug',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'DEBUG'),
	 (3,5,N'Fatal',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'FATAL'),
	 (4,1,N'Extracción de Datos',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'EXTRACT'),
	 (4,2,N'Transformación de Datos',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'TRANSFORM'),
	 (4,3,N'Carga de Datos',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'LOAD'),
	 (4,4,N'Validación',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'VALIDATION'),
	 (4,5,N'Inicio de Proceso',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'START'),
	 (4,6,N'Fin de Proceso',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'END');
INSERT INTO SODIMAC_BATCH_DEV.dbo.adminCatalogo (idCatalogo,idElemento,descripcion,estatus,usuarioCreacion,fechaCreacion,usuarioActualizacion,fechaActualizacion,elementoConversion) VALUES
	 (4,7,N'Limpieza de Datos',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'CLEANUP'),
	 (4,8,N'Notificación',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'NOTIFICATION'),
	 (5,1,N'Contrato Comercial',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'CONTRACT'),
	 (5,2,N'Acuerdo de Rebate',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'AGREEMENT'),
	 (5,3,N'Factura',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'INVOICE'),
	 (5,4,N'Proveedor',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'VENDOR'),
	 (5,5,N'Producto',1,1,'2026-01-21 12:05:02.49',NULL,NULL,N'PRODUCT');
