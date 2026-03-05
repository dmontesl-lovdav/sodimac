INSERT INTO SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet (id_flujo,id_ejecucion,nombre_paso,secuencia,fecha_inicio_registro,fecha_final_registro,parametros_registro,detalle,registros_procesados,estatus) VALUES
	 (1,2,N'Descargar contrato 1527',1,'2026-01-21 18:28:36.987','2026-01-21 18:28:37.647',NULL,NULL,1,N'SUCCESS'),
	 (2,2,N'Descargar agreements del contrato',2,'2026-01-21 18:28:37.653','2026-01-21 18:28:39.61',NULL,NULL,6,N'SUCCESS'),
	 (3,2,N'Guardar agreements en base de datos',3,'2026-01-21 18:28:39.737','2026-01-21 18:28:40.73',NULL,NULL,6,N'SUCCESS'),
	 (4,4,N'Descargar contratos desde API externa',1,'2026-01-21 18:31:34.21','2026-01-21 18:31:47.473',NULL,NULL,883,N'SUCCESS'),
	 (5,4,N'Descargar agreements de cada contrato',2,'2026-01-21 18:31:47.563','2026-01-21 18:34:36.337',NULL,NULL,5849,N'SUCCESS'),
	 (6,4,N'Eliminar datos existentes',3,'2026-01-21 18:50:40.877','2026-01-21 18:50:42.593',NULL,NULL,0,N'SUCCESS'),
	 (7,4,N'Cargar nuevos datos en base de datos',4,'2026-01-21 18:50:42.713','2026-01-21 19:06:39.577',NULL,NULL,5849,N'SUCCESS'),
	 (8,6,N'Descargar contrato 1527',1,'2026-01-21 19:41:46.093','2026-01-21 19:41:46.747',NULL,NULL,1,N'SUCCESS'),
	 (9,6,N'Descargar agreements del contrato',2,'2026-01-21 19:41:46.753','2026-01-21 19:41:48.83',NULL,NULL,6,N'SUCCESS'),
	 (10,6,N'Guardar agreements en base de datos',3,'2026-01-21 19:41:48.963','2026-01-21 19:41:49.98',NULL,NULL,6,N'SUCCESS');
INSERT INTO SODIMAC_BATCH_DEV.dbo.ctrlProcesoDet (id_flujo,id_ejecucion,nombre_paso,secuencia,fecha_inicio_registro,fecha_final_registro,parametros_registro,detalle,registros_procesados,estatus) VALUES
	 (11,8,N'Descargar contratos desde API externa',1,'2026-01-21 19:45:26.747','2026-01-21 19:45:39.79',NULL,NULL,883,N'SUCCESS'),
	 (12,8,N'Descargar agreements de cada contrato',2,'2026-01-21 19:45:39.92','2026-01-21 19:48:24.19',NULL,NULL,5849,N'SUCCESS'),
	 (13,8,N'Eliminar datos existentes',3,'2026-01-21 20:03:46.223','2026-01-21 20:03:46.743',NULL,NULL,0,N'SUCCESS'),
	 (14,8,N'Cargar nuevos datos en base de datos',4,'2026-01-21 20:03:46.883','2026-01-21 20:18:28.453',NULL,NULL,5849,N'SUCCESS'),
	 (23,21,N'Descargar contratos desde API externa',1,'2026-01-22 11:47:23.147','2026-01-22 11:47:37.237',NULL,NULL,883,N'SUCCESS'),
	 (24,21,N'Descargar agreements de cada contrato',2,'2026-01-22 11:47:38.303','2026-01-22 11:50:32.9',NULL,NULL,5849,N'SUCCESS'),
	 (25,21,N'Eliminar datos existentes',3,'2026-01-22 12:05:30.533','2026-01-22 12:20:33.93',NULL,NULL,0,N'SUCCESS'),
	 (26,21,N'Cargar nuevos datos en base de datos',4,'2026-01-22 12:05:31.083','2026-01-22 12:20:34.51',NULL,NULL,5849,N'SUCCESS');
