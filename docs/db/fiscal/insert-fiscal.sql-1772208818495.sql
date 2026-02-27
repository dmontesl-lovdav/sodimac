INSERT INTO tenant_fiscal.payment_response_catalog (response_code,response_type,description,message_template,status,created_by,created_at,updated_by,updated_at) VALUES
	 ('200','SUCCESS','Complemento de pago validado exitosamente','El complemento de pago fue validado correctamente ante el SAT',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('0','SUCCESS','Validación exitosa SAT','El documento es válido ante el SAT',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('301','ERROR_SAT','CFDI no encontrado en SAT','El UUID del complemento de pago no se encuentra registrado en el SAT',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('302','ERROR_SAT','CFDI cancelado','El complemento de pago se encuentra cancelado en el SAT',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('303','ERROR_SAT','RFC emisor no válido','El RFC del emisor no es válido según el SAT',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('304','ERROR_SAT','RFC receptor no válido','El RFC del receptor no es válido según el SAT',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('305','ERROR_SAT','Sello digital inválido','El sello digital del complemento de pago no es válido',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('401','ERROR_STRUCTURE','XML mal formado','El archivo XML no tiene una estructura válida',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('402','ERROR_STRUCTURE','XSD no cumplido','El XML no cumple con el esquema XSD Pagos 2.0',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('403','ERROR_STRUCTURE','Nodo requerido faltante','Falta un nodo requerido en la estructura del complemento de pago',1,1,'2025-11-20 15:44:36.165578',NULL,NULL);
INSERT INTO tenant_fiscal.payment_response_catalog (response_code,response_type,description,message_template,status,created_by,created_at,updated_by,updated_at) VALUES
	 ('404','ERROR_STRUCTURE','Atributo requerido faltante','Falta un atributo requerido en el XML',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('501','ERROR_VALIDATION','Receptor no autorizado','El RFC receptor no está autorizado en el sistema',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('502','ERROR_VALIDATION','Versión no vigente','La versión del complemento de pago no es vigente en el sistema',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('503','ERROR_VALIDATION','Documento relacionado no existe','El documento relacionado no se encuentra registrado en el sistema',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('504','ERROR_VALIDATION','Documento relacionado no pagado','El documento relacionado no tiene estatus de pagado',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('505','ERROR_VALIDATION','Complemento duplicado','El complemento de pago ya se encuentra registrado en el sistema',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('601','ERROR_BUSINESS','Proveedor no registrado','El ID de proveedor no se encuentra registrado',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('602','ERROR_BUSINESS','Usuario no autorizado','El usuario no tiene permisos para registrar complementos de pago',1,1,'2025-11-20 15:44:36.165578',NULL,NULL),
	 ('700','ERROR_BUSINESS','Error general','Ocurrió un error inesperado al procesar el complemento de pago',1,1,'2025-11-20 15:44:36.165578',NULL,NULL);
