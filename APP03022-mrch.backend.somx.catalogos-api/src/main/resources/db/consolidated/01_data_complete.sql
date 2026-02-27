-- ============================================================================
-- CATALOGOS API - Script Consolidado de Datos (Inserts)
-- Esquema: shared_catalogs
-- Descripcion: Script unificado para insertar todos los datos del modulo de catalogos
-- Fecha: 2025-12-22
--
-- NOTA: Este script maneja errores gracefully - si un registro ya existe,
--       continua con el siguiente sin fallar (ON CONFLICT DO NOTHING).
--
-- IDIOMAS: 1=Espanol, 2=Ingles, 3=Portugues
-- STATUS:  1=Activo, 0=Inactivo, -1=Pendiente revision
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- PARTE 1: CATALOG_HEADER - Encabezados de catalogos
-- ============================================================================

INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
-- Catalogos de estatus
(1, 'CatEstatusCartaPorte', 'ECP', 'Estatus Carta Porte', 'Catalogo de estatus para guias de embarque', 'transporte', 'SIMPLE', 1),
(2, 'CatEstatusCartaPorteFBC', 'ECF', 'Estatus Carta Porte FBC', 'Catalogo de estatus para guias de embarque en portal FBC', 'transporte', 'SIMPLE', 1),
(3, 'CatEstatusRegistro', 'ERG', 'Estatus Registro', 'Catalogo de estatus genericos de registro', 'sistema', 'SIMPLE', 1),
(4, 'CatEstatusFactura', 'EFA', 'Estatus Factura', 'Catalogo de estatus para facturas', 'fiscal', 'SIMPLE', 1),
-- Catalogos de mensajes
(5, 'CatMsgExitoso', 'RES', 'Mensajes Exitosos', 'Catalogo de mensajes de exito del sistema', 'sistema', 'MESSAGE', 1),
(6, 'CatMsgErrorSistema', 'ERR', 'Mensajes Error Sistema', 'Catalogo de mensajes de error tecnico', 'sistema', 'MESSAGE', 1),
(7, 'CatMsgNegocio', 'BUS', 'Mensajes Negocio', 'Catalogo de mensajes de error de negocio', 'sistema', 'MESSAGE', 1),
(8, 'CatMsgInfraestructura', 'INF', 'Mensajes Infraestructura', 'Catalogo de mensajes de infraestructura', 'sistema', 'MESSAGE', 1),
(9, 'CatMsgExcepcion', 'EXC', 'Mensajes Excepcion', 'Catalogo de excepciones del sistema', 'sistema', 'MESSAGE', 1),
(10, 'CatMsgInformativo', 'INF', 'Mensajes Informativos', 'Catalogo de mensajes informativos', 'sistema', 'MESSAGE', 1),
(11, 'CatMsgAdvertencia', 'WRN', 'Mensajes Advertencia', 'Catalogo de mensajes de advertencia', 'sistema', 'MESSAGE', 1),
-- Otros catalogos
(12, 'CatPacRespuestas', 'PAC', 'Respuestas PAC', 'Catalogo de respuestas del PAC Detecno', 'fiscal', 'SIMPLE', 1),
(13, 'CatEstatusNotaCredito', 'ENC', 'Estatus Nota Credito', 'Catalogo de estatus para notas de credito', 'fiscal', 'SIMPLE', 1),
(14, 'CatEstatusPago', 'EPA', 'Estatus Pago', 'Catalogo de estatus para pagos', 'fiscal', 'SIMPLE', 1),
(15, 'CatEstatusRecepcion', 'ERC', 'Estatus Recepcion', 'Catalogo de estatus para recepciones', 'fiscal', 'SIMPLE', 1),
(16, 'CatTipoOrigenRecepcion', 'TOR', 'Tipo Origen Recepcion', 'Catalogo de tipos de origen de recepcion', 'fiscal', 'SIMPLE', 1),
(17, 'CatTipoEntregaGuia', 'TEG', 'Tipo Entrega Guia', 'Catalogo de tipos de entrega de guia', 'transporte', 'SIMPLE', 1),
(18, 'CatTipoProveedor', 'TPR', 'Tipo Proveedor', 'Catalogo de tipos de proveedor', 'proveedor', 'SIMPLE', 1),
(19, 'CatOrigenCartaPorte', 'OCP', 'Origen Carta Porte', 'Catalogo de origenes de carta porte', 'transporte', 'SIMPLE', 1),
-- Catalogos SAT
(23, 'c_RegimenFiscal', 'RGF', 'Regimen Fiscal SAT', 'Catalogo de regimenes fiscales del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1),
(24, 'c_UsoCFDI', 'UCF', 'Uso de CFDI SAT', 'Catalogo de usos de CFDI del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1),
(25, 'c_TasaOCuota', 'TOC', 'Tasa o Cuota IVA SAT', 'Catalogo de tasas o cuotas de IVA del SAT', 'fiscal', 'SAT_FISCAL', 1),
(26, 'c_FormaPago', 'FPG', 'Forma de Pago SAT', 'Catalogo de formas de pago del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1),
(27, 'c_MetodoPago', 'MPG', 'Metodo de Pago SAT', 'Catalogo de metodos de pago del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1),
(28, 'c_TipoDeComprobante', 'TDC', 'Tipo de Comprobante SAT', 'Catalogo de tipos de comprobante del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1),
(29, 'c_TipoRelacion', 'TRL', 'Tipo de Relacion SAT', 'Catalogo de tipos de relacion entre CFDIs del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1),
(30, 'TipoAddenda', 'TAD', 'Tipo de Addenda Sodimac', 'Catalogo de tipos de addenda segun clasificacion Sodimac', 'fiscal', 'SIMPLE', 1)
ON CONFLICT (id) DO NOTHING;

-- Actualizar secuencia
SELECT setval('shared_catalogs.catalog_header_id_seq', COALESCE((SELECT MAX(id) FROM shared_catalogs.catalog_header), 0) + 1, false);

-- ============================================================================
-- PARTE 2: DICTIONARY_LANG - Traducciones
-- ============================================================================

-- CatEstatusCartaPorte (dict_id: 1000-1011)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(1000, 1, 'Guia de embarque pendiente de enviar al portal FBC'),
(1000, 2, 'Shipping guide pending to send to FBC portal'),
(1000, 3, 'Guia de embarque pendente de envio ao portal FBC'),
(1001, 1, 'Guia de embarque enviada al portal de proveedores FBC sin OC'),
(1001, 2, 'Shipping guide sent to FBC supplier portal without PO'),
(1001, 3, 'Guia de embarque enviada ao portal de fornecedores FBC sem OC'),
(1002, 1, 'Guia de embarque enviada al portal de proveedores FBC con OC'),
(1002, 2, 'Shipping guide sent to FBC supplier portal with PO'),
(1002, 3, 'Guia de embarque enviada ao portal de fornecedores FBC com OC'),
(1003, 1, 'Guia de embarque sin OC recibida en el portal FBC'),
(1003, 2, 'Shipping guide without PO received in FBC portal'),
(1003, 3, 'Guia de embarque sem OC recebida no portal FBC'),
(1004, 1, 'Guia de embarque con OC recibida en el portal de FBC'),
(1004, 2, 'Shipping guide with PO received in FBC portal'),
(1004, 3, 'Guia de embarque com OC recebida no portal FBC'),
(1005, 1, 'Guia de embarque relacionado con una factura'),
(1005, 2, 'Shipping guide related to an invoice'),
(1005, 3, 'Guia de embarque relacionada com uma fatura'),
(1006, 1, 'Guia de embarque enviada a contabilizar'),
(1006, 2, 'Shipping guide sent for accounting'),
(1006, 3, 'Guia de embarque enviada para contabilizacao'),
(1007, 1, 'Guia de embarque contabilizada'),
(1007, 2, 'Shipping guide accounted'),
(1007, 3, 'Guia de embarque contabilizada'),
(1008, 1, 'Guia de embarque rechazada en la contabilizacion'),
(1008, 2, 'Shipping guide rejected in accounting'),
(1008, 3, 'Guia de embarque rejeitada na contabilizacao'),
(1009, 1, 'Guia de embarque pagada'),
(1009, 2, 'Shipping guide paid'),
(1009, 3, 'Guia de embarque paga'),
(1010, 1, 'Guia de embarque rechazada en el portal de proveedores FBC'),
(1010, 2, 'Shipping guide rejected in FBC supplier portal'),
(1010, 3, 'Guia de embarque rejeitada no portal de fornecedores FBC'),
(1011, 1, 'Guia de embarque cancelada en el portal de proveedores FBC'),
(1011, 2, 'Shipping guide cancelled in FBC supplier portal'),
(1011, 3, 'Guia de embarque cancelada no portal de fornecedores FBC')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- CatEstatusCartaPorteFBC (dict_id: 1012-1020)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(1012, 1, 'Guia de embarque registrada en el portal de proveedores FBC sin OC'),
(1012, 2, 'Shipping guide registered in FBC supplier portal without PO'),
(1012, 3, 'Guia de embarque registrada no portal de fornecedores FBC sem OC'),
(1013, 1, 'Guia de embarque registrada en el portal de proveedores FBC con OC'),
(1013, 2, 'Shipping guide registered in FBC supplier portal with PO'),
(1013, 3, 'Guia de embarque registrada no portal de fornecedores FBC com OC'),
(1014, 1, 'Guia de embarque relacionada con una OC y factura'),
(1014, 2, 'Shipping guide related to a PO and invoice'),
(1014, 3, 'Guia de embarque relacionada com uma OC e fatura'),
(1015, 1, 'Guia de embarque con OC enviada a contabilizar'),
(1015, 2, 'Shipping guide with PO sent for accounting'),
(1015, 3, 'Guia de embarque com OC enviada para contabilizacao'),
(1016, 1, 'Guia de embarque con OC contabilizada'),
(1016, 2, 'Shipping guide with PO accounted'),
(1016, 3, 'Guia de embarque com OC contabilizada'),
(1017, 1, 'Guia de embarque con OC rechazada en la contabilizacion'),
(1017, 2, 'Shipping guide with PO rejected in accounting'),
(1017, 3, 'Guia de embarque com OC rejeitada na contabilizacao'),
(1018, 1, 'Guia de embarque con OC pagada'),
(1018, 2, 'Shipping guide with PO paid'),
(1018, 3, 'Guia de embarque com OC paga'),
(1019, 1, 'Rechazo en la recepcion de la guia de embarque'),
(1019, 2, 'Rejection in shipping guide reception'),
(1019, 3, 'Rejeicao na recepcao da guia de embarque'),
(1020, 1, 'Guia de embarque cancelada en el portal de proveedores FBC'),
(1020, 2, 'Shipping guide cancelled in FBC supplier portal'),
(1020, 3, 'Guia de embarque cancelada no portal de fornecedores FBC')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- CatEstatusRegistro (dict_id: 1021-1023)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(1021, 1, 'Inactivo'),
(1021, 2, 'Inactive'),
(1021, 3, 'Inativo'),
(1022, 1, 'Activo'),
(1022, 2, 'Active'),
(1022, 3, 'Ativo'),
(1023, 1, 'Borrado Logico'),
(1023, 2, 'Soft Delete'),
(1023, 3, 'Exclusao Logica')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- CatEstatusFactura (dict_id: 1051-1064)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(1051, 1, 'Rechazo Comercial'),
(1051, 2, 'Commercial Rejection'),
(1051, 3, 'Rejeicao Comercial'),
(1052, 1, 'Pendiente Addenda'),
(1052, 2, 'Pending Addenda'),
(1052, 3, 'Adendo Pendente'),
(1053, 1, 'Recibido Parcial'),
(1053, 2, 'Partial Received'),
(1053, 3, 'Recebido Parcial'),
(1054, 1, 'Pendiente de Contabilizar'),
(1054, 2, 'Pending Accounting'),
(1054, 3, 'Pendente de Contabilizacao'),
(1055, 1, 'En proceso de descarga'),
(1055, 2, 'Download in progress'),
(1055, 3, 'Em processo de download'),
(1056, 1, 'Desglose de factura'),
(1056, 2, 'Invoice breakdown'),
(1056, 3, 'Detalhamento de fatura'),
(1057, 1, 'Pendiente de envio a contabilizar'),
(1057, 2, 'Pending submission for accounting'),
(1057, 3, 'Pendente de envio para contabilizacao'),
(1058, 1, 'Pendiente de Pago'),
(1058, 2, 'Pending Payment'),
(1058, 3, 'Pendente de Pagamento'),
(1059, 1, 'Pagado'),
(1059, 2, 'Paid'),
(1059, 3, 'Pago'),
(1060, 1, 'Pendiente de complemento'),
(1060, 2, 'Pending supplement'),
(1060, 3, 'Pendente de complemento'),
(1061, 1, 'Completado'),
(1061, 2, 'Completed'),
(1061, 3, 'Completado'),
(1062, 1, 'Rechazo Contable'),
(1062, 2, 'Accounting Rejection'),
(1062, 3, 'Rejeicao Contabil'),
(1063, 1, 'No valido fiscal'),
(1063, 2, 'Not fiscally valid'),
(1063, 3, 'Nao valido fiscalmente'),
(1064, 1, 'Pago Manual'),
(1064, 2, 'Manual Payment'),
(1064, 3, 'Pagamento Manual')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- CatEstatusNotaCredito (dict_id: 1065-1068)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(1065, 1, 'Nota de credito pendiente de relacionar'),
(1065, 2, 'Credit note pending to relate'),
(1065, 3, 'Nota de credito pendente de relacionar'),
(1066, 1, 'Nota de credito relacionada'),
(1066, 2, 'Credit note related'),
(1066, 3, 'Nota de credito relacionada'),
(1067, 1, 'Nota de credito cancelada'),
(1067, 2, 'Credit note cancelled'),
(1067, 3, 'Nota de credito cancelada'),
(1068, 1, 'Nota de credito borrada'),
(1068, 2, 'Credit note deleted'),
(1068, 3, 'Nota de credito excluida')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- CatEstatusPago (dict_id: 1069-1072)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(1069, 1, 'Pendiente de complemento'),
(1069, 2, 'Pending supplement'),
(1069, 3, 'Pendente de complemento'),
(1070, 1, 'Complemento relacionado'),
(1070, 2, 'Related supplement'),
(1070, 3, 'Complemento relacionado'),
(1071, 1, 'Pago cancelado'),
(1071, 2, 'Payment cancelled'),
(1071, 3, 'Pagamento cancelado'),
(1072, 1, 'Pago borrado logico'),
(1072, 2, 'Payment soft deleted'),
(1072, 3, 'Pagamento excluido logicamente')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- ============================================================================
-- MENSAJES FISCAL-API - Errores Sistema (ERR) dict_id: 2000-2099
-- ============================================================================
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
-- Errores de archivo XML (2000-2004)
(2000, 1, 'El archivo esta vacio'),
(2000, 2, 'The file is empty'),
(2000, 3, 'O arquivo esta vazio'),
(2001, 1, 'El archivo debe tener extension .xml'),
(2001, 2, 'The file must have .xml extension'),
(2001, 3, 'O arquivo deve ter extensao .xml'),
(2002, 1, 'Error procesando archivo XML fiscal'),
(2002, 2, 'Error processing fiscal XML file'),
(2002, 3, 'Erro ao processar arquivo XML fiscal'),
(2003, 1, 'El XML no tiene una estructura valida'),
(2003, 2, 'The XML does not have a valid structure'),
(2003, 3, 'O XML nao tem uma estrutura valida'),
(2004, 1, 'Error leyendo archivo XML'),
(2004, 2, 'Error reading XML file'),
(2004, 3, 'Erro ao ler arquivo XML'),
-- Errores XSD (2010-2011)
(2010, 1, 'El XML no cumple con la estructura XSD'),
(2010, 2, 'The XML does not comply with XSD structure'),
(2010, 3, 'O XML nao cumpre com a estrutura XSD'),
(2011, 1, 'El XML no cumple con la estructura XSD Pagos 2.0'),
(2011, 2, 'The XML does not comply with XSD Pagos 2.0 structure'),
(2011, 3, 'O XML nao cumpre com a estrutura XSD Pagos 2.0'),
-- Errores JAXB (2020-2025)
(2020, 1, 'Error configurando procesador CartaPorte'),
(2020, 2, 'Error configuring CartaPorte processor'),
(2020, 3, 'Erro ao configurar processador CartaPorte'),
(2021, 1, 'Error configurando procesador CFDI'),
(2021, 2, 'Error configuring CFDI processor'),
(2021, 3, 'Erro ao configurar processador CFDI'),
(2022, 1, 'Error configurando procesador Pagos'),
(2022, 2, 'Error configuring Pagos processor'),
(2022, 3, 'Erro ao configurar processador Pagos'),
(2023, 1, 'Error procesando CartaPorte'),
(2023, 2, 'Error processing CartaPorte'),
(2023, 3, 'Erro ao processar CartaPorte'),
(2024, 1, 'Error procesando CFDI'),
(2024, 2, 'Error processing CFDI'),
(2024, 3, 'Erro ao processar CFDI'),
(2025, 1, 'Error procesando Pagos'),
(2025, 2, 'Error processing Pagos'),
(2025, 3, 'Erro ao processar Pagos'),
-- Errores Tipo Documento (2040-2046)
(2040, 1, 'Error detectando tipo de documento XML'),
(2040, 2, 'Error detecting XML document type'),
(2040, 3, 'Erro ao detectar tipo de documento XML'),
(2041, 1, 'Tipo de documento fiscal no valido'),
(2041, 2, 'Invalid fiscal document type'),
(2041, 3, 'Tipo de documento fiscal nao valido'),
(2042, 1, 'Tipo de CFDI no soportado en este procesador'),
(2042, 2, 'CFDI type not supported in this processor'),
(2042, 3, 'Tipo de CFDI nao suportado neste processador'),
(2043, 1, 'Se esperaba Factura (I) pero se encontro tipo diferente'),
(2043, 2, 'Expected Invoice (I) but found different type'),
(2043, 3, 'Esperava-se Fatura (I) mas encontrou tipo diferente'),
(2044, 1, 'Se esperaba Nota de Credito (E) pero se encontro tipo diferente'),
(2044, 2, 'Expected Credit Note (E) but found different type'),
(2044, 3, 'Esperava-se Nota de Credito (E) mas encontrou tipo diferente'),
(2045, 1, 'Tipo de comprobante no valido'),
(2045, 2, 'Invalid voucher type'),
(2045, 3, 'Tipo de comprovante nao valido'),
(2046, 1, 'El tipo de comprobante debe ser P (Pago)'),
(2046, 2, 'The voucher type must be P (Payment)'),
(2046, 3, 'O tipo de comprovante deve ser P (Pagamento)'),
-- ERR032-ERR036 Nuevos errores
(2090, 1, 'PAC no disponible para el nivel de prioridad solicitado'),
(2090, 2, 'PAC not available for the requested priority level'),
(2090, 3, 'PAC nao disponivel para o nivel de prioridade solicitado'),
(2091, 1, 'Error generando PDF desde XML'),
(2091, 2, 'Error generating PDF from XML'),
(2091, 3, 'Erro ao gerar PDF a partir do XML'),
(2092, 1, 'Error generando PDF del Complemento de Pago'),
(2092, 2, 'Error generating Payment Complement PDF'),
(2092, 3, 'Erro ao gerar PDF do Complemento de Pagamento'),
(2093, 1, 'Error de validacion en los campos del request'),
(2093, 2, 'Validation error in request fields'),
(2093, 3, 'Erro de validacao nos campos do request'),
(2094, 1, 'Error inesperado del sistema. Contacte al administrador'),
(2094, 2, 'Unexpected system error. Contact the administrator'),
(2094, 3, 'Erro inesperado do sistema. Contate o administrador')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- ============================================================================
-- CATALOGOS SAT - Diccionarios (dict_id: 5000+)
-- ============================================================================

-- c_RegimenFiscal (dict_id: 5000-5018)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5000, 1, 'General de Ley Personas Morales'),
(5000, 2, 'General Law Legal Entities'),
(5001, 1, 'Personas Morales con Fines no Lucrativos'),
(5001, 2, 'Non-Profit Legal Entities'),
(5002, 1, 'Sueldos y Salarios e Ingresos Asimilados a Salarios'),
(5002, 2, 'Wages, Salaries and Assimilated Income'),
(5003, 1, 'Arrendamiento'),
(5003, 2, 'Leasing'),
(5004, 1, 'Regimen de Enajenacion o Adquisicion de Bienes'),
(5004, 2, 'Asset Disposal or Acquisition Regime'),
(5005, 1, 'Demas ingresos'),
(5005, 2, 'Other Income'),
(5006, 1, 'Residentes en el Extranjero sin Establecimiento Permanente en Mexico'),
(5006, 2, 'Foreign Residents without Permanent Establishment in Mexico'),
(5007, 1, 'Ingresos por Dividendos (socios y accionistas)'),
(5007, 2, 'Dividend Income (partners and shareholders)'),
(5008, 1, 'Personas Fisicas con Actividades Empresariales y Profesionales'),
(5008, 2, 'Individuals with Business and Professional Activities'),
(5009, 1, 'Ingresos por intereses'),
(5009, 2, 'Interest Income'),
(5010, 1, 'Regimen de los ingresos por obtencion de premios'),
(5010, 2, 'Prize Income Regime'),
(5011, 1, 'Sin obligaciones fiscales'),
(5011, 2, 'Without Tax Obligations'),
(5012, 1, 'Sociedades Cooperativas de Produccion que optan por diferir sus ingresos'),
(5012, 2, 'Production Cooperative Societies opting to defer income'),
(5013, 1, 'Incorporacion Fiscal'),
(5013, 2, 'Fiscal Incorporation'),
(5014, 1, 'Actividades Agricolas, Ganaderas, Silvicolas y Pesqueras'),
(5014, 2, 'Agricultural, Livestock, Forestry and Fishing Activities'),
(5015, 1, 'Opcional para Grupos de Sociedades'),
(5015, 2, 'Optional for Corporate Groups'),
(5016, 1, 'Coordinados'),
(5016, 2, 'Coordinated'),
(5017, 1, 'Regimen de las Actividades Empresariales con ingresos a traves de Plataformas Tecnologicas'),
(5017, 2, 'Business Activities through Technology Platforms Regime'),
(5018, 1, 'Regimen Simplificado de Confianza'),
(5018, 2, 'Simplified Trust Regime')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- c_TipoRelacion (dict_id: 5600-5608)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5600, 1, 'Nota de credito de los documentos relacionados'),
(5600, 2, 'Credit note for related documents'),
(5600, 3, 'Nota de credito dos documentos relacionados'),
(5601, 1, 'Nota de debito de los documentos relacionados'),
(5601, 2, 'Debit note for related documents'),
(5601, 3, 'Nota de debito dos documentos relacionados'),
(5602, 1, 'Devolucion de mercancia sobre facturas o traslados previos'),
(5602, 2, 'Merchandise return on previous invoices or transfers'),
(5602, 3, 'Devolucao de mercadoria sobre faturas ou transferencias anteriores'),
(5603, 1, 'Sustitucion de los CFDI previos'),
(5603, 2, 'Substitution of previous CFDIs'),
(5603, 3, 'Substituicao dos CFDIs anteriores'),
(5604, 1, 'Traslados de mercancias facturados previamente'),
(5604, 2, 'Previously invoiced merchandise transfers'),
(5604, 3, 'Transferencias de mercadorias faturadas anteriormente'),
(5605, 1, 'Factura generada por los traslados previos'),
(5605, 2, 'Invoice generated for previous transfers'),
(5605, 3, 'Fatura gerada pelas transferencias anteriores'),
(5606, 1, 'CFDI por aplicacion de anticipo'),
(5606, 2, 'CFDI for advance payment application'),
(5606, 3, 'CFDI por aplicacao de adiantamento'),
(5607, 1, 'Factura generada por pagos en parcialidades'),
(5607, 2, 'Invoice generated for installment payments'),
(5607, 3, 'Fatura gerada por pagamentos parcelados'),
(5608, 1, 'Factura generada por pagos diferidos'),
(5608, 2, 'Invoice generated for deferred payments'),
(5608, 3, 'Fatura gerada por pagamentos diferidos')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- TipoAddenda (dict_id: 5700-5704)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5700, 1, 'Addenda Estandar'),
(5700, 2, 'Standard Addenda'),
(5700, 3, 'Addenda Padrao'),
(5701, 1, 'Addenda con Carta Porte'),
(5701, 2, 'Addenda with Bill of Lading'),
(5701, 3, 'Addenda com Carta de Porte'),
(5702, 1, 'Addenda Complemento de Pago'),
(5702, 2, 'Payment Complement Addenda'),
(5702, 3, 'Addenda Complemento de Pagamento'),
(5703, 1, 'Addenda Internacional'),
(5703, 2, 'International Addenda'),
(5703, 3, 'Addenda Internacional'),
(5704, 1, 'Addenda Sodimac'),
(5704, 2, 'Sodimac Addenda'),
(5704, 3, 'Addenda Sodimac')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- ============================================================================
-- PARTE 3: CATALOG_DETAIL - Detalles de catalogos
-- ============================================================================

-- c_TipoRelacion (header_id: 29)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(29, 'TRL001', '01', 5600, 1, 1, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL002', '02', 5601, 2, 2, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL003', '03', 5602, 3, 3, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL004', '04', 5603, 4, 4, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL005', '05', 5604, 5, 5, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL006', '06', 5605, 6, 6, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL007', '07', 5606, 7, 7, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL008', '08', 5607, 8, 8, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL009', '09', 5608, 9, 9, 1, '2000-01-01', '2030-12-31')
ON CONFLICT (header_id, key) DO NOTHING;

-- TipoAddenda (header_id: 30)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(30, 'TAD001', '1', 5700, 1, 1, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD002', '2', 5701, 2, 2, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD003', '3', 5702, 3, 3, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD004', '4', 5703, 4, 4, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD005', '5', 5704, 5, 5, 1, '2000-01-01', '2030-12-31')
ON CONFLICT (header_id, key) DO NOTHING;

-- Errores sistema (header_id: 6)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(6, 'ERR001', 2000, NULL, 1, 1),
(6, 'ERR002', 2001, NULL, 2, 1),
(6, 'ERR003', 2002, NULL, 3, 1),
(6, 'ERR004', 2003, NULL, 4, 1),
(6, 'ERR005', 2004, NULL, 5, 1),
(6, 'ERR006', 2010, NULL, 6, 1),
(6, 'ERR007', 2011, NULL, 7, 1),
(6, 'ERR008', 2020, NULL, 8, 1),
(6, 'ERR009', 2021, NULL, 9, 1),
(6, 'ERR010', 2022, NULL, 10, 1),
(6, 'ERR011', 2023, NULL, 11, 1),
(6, 'ERR012', 2024, NULL, 12, 1),
(6, 'ERR013', 2025, NULL, 13, 1),
(6, 'ERR014', 2040, NULL, 14, 1),
(6, 'ERR015', 2041, NULL, 15, 1),
(6, 'ERR016', 2042, NULL, 16, 1),
(6, 'ERR017', 2043, NULL, 17, 1),
(6, 'ERR018', 2044, NULL, 18, 1),
(6, 'ERR019', 2045, NULL, 19, 1),
(6, 'ERR020', 2046, NULL, 20, 1),
(6, 'ERR032', 2090, NULL, 32, 1),
(6, 'ERR033', 2091, NULL, 33, 1),
(6, 'ERR034', 2092, NULL, 34, 1),
(6, 'ERR035', 2093, NULL, 35, 1),
(6, 'ERR036', 2094, NULL, 36, 1)
ON CONFLICT (header_id, key) DO NOTHING;

-- ============================================================================
-- PARTE 4: DATOS DE PROVEEDORES
-- ============================================================================

-- Tipos de proveedor
INSERT INTO shared_catalogs.supplier_type (code, description, created_by, status) VALUES
('NAC', 'Proveedor Nacional', 'system', 1),
('INT', 'Proveedor Internacional', 'system', 1),
('MIX', 'Proveedor Mixto', 'system', 1)
ON CONFLICT (code) DO NOTHING;

-- Condiciones de pago
INSERT INTO shared_catalogs.payment_condition (condition_name, days, created_by, status) VALUES
('Contado', 0, 'system', 1),
('15 dias', 15, 'system', 1),
('30 dias', 30, 'system', 1),
('45 dias', 45, 'system', 1),
('60 dias', 60, 'system', 1),
('90 dias', 90, 'system', 1)
ON CONFLICT DO NOTHING;

-- Centros de costo
INSERT INTO shared_catalogs.cost_center (cost_center_code, sap_branch, sodimac_branch, description, created_by, status) VALUES
('CC001', 'SAP001', 'SOD001', 'Centro de Costo Principal CDMX', 'system', 1),
('CC002', 'SAP002', 'SOD002', 'Centro de Costo Guadalajara', 'system', 1),
('CC003', 'SAP003', 'SOD003', 'Centro de Costo Monterrey', 'system', 1),
('CC004', 'SAP004', 'SOD004', 'Centro de Costo Puebla', 'system', 1),
('CC005', 'SAP005', 'SOD005', 'Centro de Costo Queretaro', 'system', 1)
ON CONFLICT (cost_center_code) DO NOTHING;

-- Centros de beneficio
INSERT INTO shared_catalogs.benefit_center (benefit_center_code, sap_branch, sodimac_branch, description, created_by, status) VALUES
('CB001', 'SAP001', 'SOD001', 'Centro de Beneficio Retail', 'system', 1),
('CB002', 'SAP002', 'SOD002', 'Centro de Beneficio Mayoreo', 'system', 1),
('CB003', 'SAP003', 'SOD003', 'Centro de Beneficio E-Commerce', 'system', 1),
('CB004', 'SAP004', 'SOD004', 'Centro de Beneficio Servicios', 'system', 1),
('CB005', 'SAP005', 'SOD005', 'Centro de Beneficio Corporativo', 'system', 1)
ON CONFLICT (benefit_center_code) DO NOTHING;

-- ============================================================================
-- VERIFICACION
-- ============================================================================
SELECT 'catalog_header' AS tabla, COUNT(*) AS registros FROM shared_catalogs.catalog_header
UNION ALL SELECT 'dictionary_lang', COUNT(*) FROM shared_catalogs.dictionary_lang
UNION ALL SELECT 'catalog_detail', COUNT(*) FROM shared_catalogs.catalog_detail
UNION ALL SELECT 'supplier_type', COUNT(*) FROM shared_catalogs.supplier_type
UNION ALL SELECT 'payment_condition', COUNT(*) FROM shared_catalogs.payment_condition
UNION ALL SELECT 'cost_center', COUNT(*) FROM shared_catalogs.cost_center
UNION ALL SELECT 'benefit_center', COUNT(*) FROM shared_catalogs.benefit_center;

-- ============================================================================
-- FIN DEL SCRIPT DE DATOS CONSOLIDADO
-- ============================================================================
