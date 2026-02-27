INSERT INTO core_utils.cat_message (message_code,id_message_type,description,created_by,created_at,updated_by,updated_at) VALUES
	 ('ERR001',1,'Error de conexion con el servicio de timbrado',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR002',1,'Error al validar estructura del XML',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR003',1,'Error de autenticacion: credenciales invalidas',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR004',1,'Error al procesar la solicitud: datos incompletos',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR005',1,'Error de timeout: el servicio no responde',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR006',1,'Error al guardar en base de datos',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR007',1,'Error: RFC no valido',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR008',1,'Error: Certificado expirado',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR009',1,'Error: Sello digital invalido',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('ERR010',1,'Error: Folio fiscal duplicado',1,'2025-12-15 13:23:39.969991',NULL,NULL);
INSERT INTO core_utils.cat_message (message_code,id_message_type,description,created_by,created_at,updated_by,updated_at) VALUES
	 ('WRN001',2,'Advertencia: El certificado expira en menos de 30 dias',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('WRN002',2,'Advertencia: Limite de folios cercano al maximo',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('WRN003',2,'Advertencia: Proceso demorado, reintentando',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('WRN004',2,'Advertencia: Datos opcionales no proporcionados',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('WRN005',2,'Advertencia: Version de CFDI proxima a deprecar',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('INF001',3,'Proceso iniciado correctamente',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('INF002',3,'Validacion en progreso',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('INF003',3,'Documento en cola de procesamiento',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('INF004',3,'Sincronizacion programada',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('INF005',3,'Mantenimiento programado proximo',1,'2025-12-15 13:23:39.969991',NULL,NULL);
INSERT INTO core_utils.cat_message (message_code,id_message_type,description,created_by,created_at,updated_by,updated_at) VALUES
	 ('SUC001',4,'Documento timbrado exitosamente',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('SUC002',4,'Cancelacion procesada correctamente',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('SUC003',4,'Validacion completada sin errores',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('SUC004',4,'PDF generado correctamente',1,'2025-12-15 13:23:39.969991',NULL,NULL),
	 ('SUC005',4,'Notificacion enviada exitosamente',1,'2025-12-15 13:23:39.969991',NULL,NULL);
