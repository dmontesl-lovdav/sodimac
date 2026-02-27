INSERT INTO shared_catalogs.catalog_header (code,prefix,"name",description,"module",status,created_at,updated_at,catalog_type,created_by,updated_by) VALUES
	 ('CatEstatusCartaPorte','ECP','Estatus Carta Porte','Catálogo de estados para guías de embarque/carta porte','transporte',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatEstatusCartaPorteFBC','ECF','Estatus Carta Porte FBC','Catálogo de estados para guías de embarque en portal FBC','transporte',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatEstatusRegistro','ERG','Estatus Registro','Catálogo de estados genéricos para registros','general',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatMsg','MSG','Categorías de Mensajes','Catálogo maestro de categorías de mensajes del sistema','sistema',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatMsgErrorInfra','INE','Mensajes Error Infraestructura','Catálogo de mensajes de error de infraestructura','sistema',-1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatMsgExcepcion','EXC','Mensajes Excepción','Catálogo de mensajes de excepciones','sistema',-1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatMsgInformativo','INF','Mensajes Informativos','Catálogo de mensajes informativos','sistema',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatPacRespuestasDetecno','PDT','Respuestas PAC Detecno','Catálogo de códigos de respuesta del PAC Detecno','sistema',-1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatPacRespuestas','PAC','Respuestas PAC','Catálogo maestro de tipos de respuesta PAC','sistema',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatMsgConfirmacion','CFM','Mensajes Confirmación','Catálogo de mensajes de confirmación','sistema',-1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL);
INSERT INTO shared_catalogs.catalog_header (code,prefix,"name",description,"module",status,created_at,updated_at,catalog_type,created_by,updated_by) VALUES
	 ('CatEstatusFactura','EFA','Estatus Factura','Catálogo de estados del ciclo de vida de facturas','fiscal',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatEstatusNotaCredito','ENC','Estatus Nota Crédito','Catálogo de estados para notas de crédito','fiscal',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatEstatusPago','EPA','Estatus Pago','Catálogo de estados para complementos de pago','fiscal',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatEstatusRecepcion','ERC','Estatus Recepción','Catálogo de estados para recepciones de mercancía','general',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatEstatusComplemento','ECM','Estatus Complemento','Catálogo de estados para complementos fiscales','fiscal',-1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatTipoOrigenRecepcion','TOR','Tipo Origen Recepción','Catálogo de orígenes de recepción por tipo de proveedor','general',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatTipoEntregaGuia','TEG','Tipo Entrega Guía','Catálogo de tipos de entrega para guías','transporte',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatTipoProveedor','TPR','Tipo Proveedor','Catálogo de clasificación de proveedores','general',1,'2025-12-04 01:13:42.378998',NULL,'SIMPLE',NULL,NULL),
	 ('CatMsgExitoso','RES','Mensajes Exitosos','Catálogo de mensajes de éxito del sistema','sistema',1,'2025-12-04 01:13:42.378998',NULL,'MESSAGE',NULL,NULL),
	 ('CatMsgErrorSistema','ERR','Mensajes Error Sistema','Catálogo de mensajes de error técnico','sistema',1,'2025-12-04 01:13:42.378998',NULL,'MESSAGE',NULL,NULL);
INSERT INTO shared_catalogs.catalog_header (code,prefix,"name",description,"module",status,created_at,updated_at,catalog_type,created_by,updated_by) VALUES
	 ('CatMsgNegocio','BUS','Mensajes Negocio','Catálogo de mensajes de validación de negocio','sistema',1,'2025-12-04 01:13:42.378998',NULL,'MESSAGE',NULL,NULL),
	 ('CatMsgAdvertencia','WRN','Mensajes Advertencia','Catálogo de mensajes de advertencia','sistema',1,'2025-12-04 01:13:42.378998',NULL,'MESSAGE',NULL,NULL),
	 ('c_RegimenFiscal','RGF','Régimen Fiscal SAT','Catálogo de regímenes fiscales del SAT para CFDI 4.0','fiscal',1,'2025-12-04 17:18:34.35779',NULL,'SAT_FISCAL',NULL,NULL),
	 ('c_UsoCFDI','UCF','Uso de CFDI SAT','Catálogo de usos de CFDI del SAT para CFDI 4.0','fiscal',1,'2025-12-04 17:18:34.668224',NULL,'SAT_FISCAL',NULL,NULL),
	 ('c_TasaOCuota','TOC','Tasa o Cuota IVA SAT','Catálogo de tasas o cuotas de IVA del SAT','fiscal',1,'2025-12-04 17:18:34.970416',NULL,'SAT_FISCAL',NULL,NULL),
	 ('c_FormaPago','FPG','Forma de Pago SAT','Catálogo de formas de pago del SAT para CFDI 4.0','fiscal',1,'2025-12-04 17:18:35.286062',NULL,'SAT_FISCAL',NULL,NULL),
	 ('c_MetodoPago','MPG','Método de Pago SAT','Catálogo de métodos de pago del SAT para CFDI 4.0','fiscal',1,'2025-12-04 17:18:35.583692',NULL,'SAT_FISCAL',NULL,NULL),
	 ('c_TipoDeComprobante','TDC','Tipo de Comprobante SAT','Catálogo de tipos de comprobante del SAT para CFDI 4.0','fiscal',1,'2025-12-04 17:18:35.901526',NULL,'SAT_FISCAL',NULL,NULL),
	 ('c_TipoRelacion','TRL','Tipo de Relación SAT','Catálogo de tipos de relación entre CFDIs del SAT para CFDI 4.0','fiscal',1,'2025-12-16 12:33:05.747072',NULL,'SAT_FISCAL',NULL,NULL),
	 ('TipoAddenda','TAD','Tipo de Addenda Sodimac','Catálogo de tipos de addenda según clasificación Sodimac','fiscal',1,'2025-12-16 12:33:06.053841',NULL,'SIMPLE',NULL,NULL);
INSERT INTO shared_catalogs.catalog_header (code,prefix,"name",description,"module",status,created_at,updated_at,catalog_type,created_by,updated_by) VALUES
	 ('CatOrigenCartaPorte','OCP','Origen Carta Porte','Catálogo de orígenes para carta porte','transporte',1,'2026-01-05 12:25:27.491813',NULL,'SIMPLE',NULL,NULL),
	 ('CAT_TEST','TES','test','test','general',0,'2026-01-19 14:19:13.46575',NULL,'PRIMARIO',NULL,NULL),
	 ('CAT_TEST_1','TES','test',NULL,'general',0,'2026-01-26 15:42:48.30283',NULL,'PRIMARIO',NULL,NULL),
	 ('CAT_TEST_2','TES','test','test','general',0,'2026-01-26 16:02:34.093781',NULL,'PRIMARIO',NULL,NULL),
	 ('CAT_TEST_3','TES','tes2','test2','general',0,'2026-01-26 16:03:25.041266','2026-01-26 16:13:43.383778','SECUNDARIO',NULL,NULL),
	 ('CAT_TESTTTT','TES','testttt','testttt','general',0,'2026-02-03 16:45:04.266524',NULL,'PRIMARIO',NULL,NULL),
	 ('CAT_TEST_4','TES','test',NULL,'general',0,'2026-02-06 14:55:06.109928',NULL,'PRIMARIO',NULL,NULL),
	 ('CatEstatusEstadoCuenta','EEC','Estatus Estado de Cuenta','Catalogo de estatus para el estado de cuenta de proveedores','finanzas',1,'2026-02-09 12:20:12.133375',NULL,'SIMPLE',NULL,NULL),
	 ('CAT_TES','TES','tes','tes','general',0,'2026-02-10 17:00:00.463246',NULL,'PRIMARIO',NULL,NULL),
	 ('CAT_TES_1','TES','tes','tes','general',0,'2026-02-10 17:00:26.674758',NULL,'PRIMARIO',NULL,NULL);
INSERT INTO shared_catalogs.catalog_header (code,prefix,"name",description,"module",status,created_at,updated_at,catalog_type,created_by,updated_by) VALUES
	 ('CAT_TES_2','TES','tes','tes','general',0,'2026-02-10 17:00:50.453757',NULL,'PRIMARIO',NULL,NULL),
	 ('CATPAISES','CAT','catalogo de paises',NULL,'general',0,'2026-02-13 10:47:40.724737',NULL,'PRIMARIO',NULL,NULL);
