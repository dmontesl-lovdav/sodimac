-- ============================================================================
-- CATALOGOS API - Inserción de Datos
-- Esquema: shared_catalogs
-- Idiomas: 1=Español, 2=Inglés, 3=Portugués
-- Status: 1=Activo, 0=Inactivo, -1=Pendiente revisión (sin descripción)
--
-- PATRON DE CLAVES (key):
--   {PREFIJO}{NUMERO_SECUENCIAL}
--   Ejemplo: EFA001 = Estatus Factura 001
-- ============================================================================

SET search_path TO shared_catalogs;

-- Limpiar datos existentes (en orden por dependencias)
TRUNCATE TABLE shared_catalogs.catalog_detail CASCADE;
TRUNCATE TABLE shared_catalogs.catalog_header CASCADE;
TRUNCATE TABLE shared_catalogs.dictionary_lang CASCADE;

-- ============================================================================
-- DICTIONARY_LANG: Traducciones en 3 idiomas
-- ============================================================================

-- ----------------------------------------------------------------------------
-- CatEstatusCartaPorte (dict_id: 1000-1011) - Prefijo: ECP
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1000, 1, 'Guía de embarque pendiente de enviar al portal FBC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1000, 2, 'Shipping guide pending to send to FBC portal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1000, 3, 'Guia de embarque pendente de envio ao portal FBC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1001, 1, 'Guía de embarque enviada al portal de proveedores FBC sin OC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1001, 2, 'Shipping guide sent to FBC supplier portal without PO');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1001, 3, 'Guia de embarque enviada ao portal de fornecedores FBC sem OC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1002, 1, 'Guía de embarque enviada al portal de proveedores FBC con OC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1002, 2, 'Shipping guide sent to FBC supplier portal with PO');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1002, 3, 'Guia de embarque enviada ao portal de fornecedores FBC com OC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1003, 1, 'Guía de embarque sin OC recibida en el portal FBC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1003, 2, 'Shipping guide without PO received in FBC portal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1003, 3, 'Guia de embarque sem OC recebida no portal FBC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1004, 1, 'Guía de embarque con OC recibida en el portal de FBC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1004, 2, 'Shipping guide with PO received in FBC portal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1004, 3, 'Guia de embarque com OC recebida no portal FBC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1005, 1, 'Guía de embarque relacionado con una factura');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1005, 2, 'Shipping guide related to an invoice');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1005, 3, 'Guia de embarque relacionada com uma fatura');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1006, 1, 'Guía de embarque enviada a contabilizar');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1006, 2, 'Shipping guide sent for accounting');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1006, 3, 'Guia de embarque enviada para contabilização');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1007, 1, 'Guía de embarque contabilizada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1007, 2, 'Shipping guide accounted');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1007, 3, 'Guia de embarque contabilizada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1008, 1, 'Guía de embarque rechazada en la contabilización');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1008, 2, 'Shipping guide rejected in accounting');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1008, 3, 'Guia de embarque rejeitada na contabilização');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1009, 1, 'Guía de embarque pagada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1009, 2, 'Shipping guide paid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1009, 3, 'Guia de embarque paga');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1010, 1, 'Guía de embarque rechazada en el portal de proveedores FBC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1010, 2, 'Shipping guide rejected in FBC supplier portal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1010, 3, 'Guia de embarque rejeitada no portal de fornecedores FBC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1011, 1, 'Guía de embarque cancelada en el portal de proveedores FBC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1011, 2, 'Shipping guide cancelled in FBC supplier portal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1011, 3, 'Guia de embarque cancelada no portal de fornecedores FBC');

-- ----------------------------------------------------------------------------
-- CatEstatusCartaPorteFBC (dict_id: 1012-1020) - Prefijo: ECF
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1012, 1, 'Guía de embarque registrada en el portal de proveedores FBC sin OC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1012, 2, 'Shipping guide registered in FBC supplier portal without PO');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1012, 3, 'Guia de embarque registrada no portal de fornecedores FBC sem OC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1013, 1, 'Guía de embarque registrada en el portal de proveedores FBC con OC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1013, 2, 'Shipping guide registered in FBC supplier portal with PO');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1013, 3, 'Guia de embarque registrada no portal de fornecedores FBC com OC');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1014, 1, 'Guía de embarque relacionada con una OC y factura');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1014, 2, 'Shipping guide related to a PO and invoice');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1014, 3, 'Guia de embarque relacionada com uma OC e fatura');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1015, 1, 'Guía de embarque con OC enviada a contabilizar');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1015, 2, 'Shipping guide with PO sent for accounting');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1015, 3, 'Guia de embarque com OC enviada para contabilização');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1016, 1, 'Guía de embarque con OC contabilizada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1016, 2, 'Shipping guide with PO accounted');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1016, 3, 'Guia de embarque com OC contabilizada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1017, 1, 'Guía de embarque con OC rechazada en la contabilización');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1017, 2, 'Shipping guide with PO rejected in accounting');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1017, 3, 'Guia de embarque com OC rejeitada na contabilização');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1018, 1, 'Guía de embarque con OC pagada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1018, 2, 'Shipping guide with PO paid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1018, 3, 'Guia de embarque com OC paga');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1019, 1, 'Rechazo en la recepción de la guía de embarque');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1019, 2, 'Rejection in shipping guide reception');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1019, 3, 'Rejeição na recepção da guia de embarque');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1020, 1, 'Guía de embarque cancelada en el portal de proveedores FBC');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1020, 2, 'Shipping guide cancelled in FBC supplier portal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1020, 3, 'Guia de embarque cancelada no portal de fornecedores FBC');

-- ----------------------------------------------------------------------------
-- CatEstatusRegistro (dict_id: 1021-1023) - Prefijo: ERG
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1021, 1, 'Inactivo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1021, 2, 'Inactive');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1021, 3, 'Inativo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1022, 1, 'Activo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1022, 2, 'Active');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1022, 3, 'Ativo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1023, 1, 'Borrado Lógico');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1023, 2, 'Soft Delete');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1023, 3, 'Exclusão Lógica');

-- ----------------------------------------------------------------------------
-- CatMsg - Categorías de Mensajes (dict_id: 1024-1029) - Prefijo: MSG
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1024, 1, 'Catálogo para gestionar los mensajes de éxito en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1024, 2, 'Catalog to manage success messages in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1024, 3, 'Catálogo para gerenciar mensagens de sucesso no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1025, 1, 'Catálogo para gestionar los mensajes de negocio del aplicativo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1025, 2, 'Catalog to manage business messages of the application');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1025, 3, 'Catálogo para gerenciar mensagens de negócio do aplicativo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1026, 1, 'Catálogo de los mensajes de error a nivel técnico del sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1026, 2, 'Catalog of technical error messages of the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1026, 3, 'Catálogo de mensagens de erro técnico do sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1027, 1, 'Catálogo de mensajes de errores de infraestructura');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1027, 2, 'Catalog of infrastructure error messages');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1027, 3, 'Catálogo de mensagens de erros de infraestrutura');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1028, 1, 'Catálogo de excepciones que genera el aplicativo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1028, 2, 'Catalog of exceptions generated by the application');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1028, 3, 'Catálogo de exceções geradas pelo aplicativo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1029, 1, 'Catálogo de los mensajes informativos que retorna el aplicativo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1029, 2, 'Catalog of informational messages returned by the application');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1029, 3, 'Catálogo de mensagens informativas retornadas pelo aplicativo');

-- ----------------------------------------------------------------------------
-- CatMsgExitoso (dict_id: 1030-1032) - Prefijo: RES
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1030, 1, 'El registro del proveedor {0} se realizó exitosamente.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1030, 2, 'Supplier {0} registration was successful.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1030, 3, 'O registro do fornecedor {0} foi realizado com sucesso.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1031, 1, 'El reporte se ha descargado exitosamente.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1031, 2, 'The report was downloaded successfully.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1031, 3, 'O relatório foi baixado com sucesso.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1032, 1, 'El registro de la guía de embarque se realizó exitosamente.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1032, 2, 'Shipping guide registration was successful.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1032, 3, 'O registro da guia de embarque foi realizado com sucesso.');

-- ----------------------------------------------------------------------------
-- CatMsgNegocio (dict_id: 1033-1039) - Prefijo: BUS
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1033, 1, 'La addenda de la factura no cumple con la estructura requerida. Por favor, verifica el formato y el nombre de los elementos.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1033, 2, 'The invoice addenda does not meet the required structure. Please verify the format and element names.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1033, 3, 'O adendo da fatura não atende à estrutura exigida. Por favor, verifique o formato e os nomes dos elementos.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1034, 1, 'El RFC del receptor {0} no acepta documentos fiscales. Verifica que el RFC esté autorizado para recibir comprobantes fiscales.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1034, 2, 'The receiver RFC {0} does not accept fiscal documents. Verify that the RFC is authorized to receive tax receipts.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1034, 3, 'O RFC do receptor {0} não aceita documentos fiscais. Verifique se o RFC está autorizado a receber comprovantes fiscais.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1035, 1, 'La guía carta porte no contiene un archivo xml y csv asociado, favor de validar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1035, 2, 'The bill of lading does not contain an associated xml and csv file, please validate.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1035, 3, 'A carta porte não contém um arquivo xml e csv associado, favor validar.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1036, 1, 'No fue posible registrar la guía carta porte, falta información por completar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1036, 2, 'It was not possible to register the bill of lading, missing information to complete.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1036, 3, 'Não foi possível registrar a carta porte, faltam informações para completar.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1037, 1, 'La guía de embarque se encuentra previamente registrada, favor de validar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1037, 2, 'The shipping guide is already registered, please validate.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1037, 3, 'A guia de embarque já está registrada, favor validar.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1038, 1, 'Las facturas no corresponden al complemento que desea publicar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1038, 2, 'The invoices do not correspond to the supplement you want to publish.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1038, 3, 'As faturas não correspondem ao complemento que deseja publicar.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1039, 1, 'Las notas de crédito no corresponden al complemento que desea publicar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1039, 2, 'The credit notes do not correspond to the supplement you want to publish.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1039, 3, 'As notas de crédito não correspondem ao complemento que deseja publicar.');

-- ----------------------------------------------------------------------------
-- CatMsgInformativo (dict_id: 1040) - Prefijo: INF
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1040, 1, 'No existe información con los criterios establecidos.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1040, 2, 'No information exists with the established criteria.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1040, 3, 'Não existe informação com os critérios estabelecidos.');

-- ----------------------------------------------------------------------------
-- CatMsgAdvertencia (dict_id: 1041-1048) - Prefijo: WRN
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1041, 1, 'La fecha inicio no puede ser superior a la fecha final.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1041, 2, 'The start date cannot be greater than the end date.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1041, 3, 'A data de início não pode ser maior que a data final.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1042, 1, 'Se requiere capturar por lo menos un filtro de búsqueda.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1042, 2, 'At least one search filter is required.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1042, 3, 'É necessário inserir pelo menos um filtro de busca.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1043, 1, 'No existe información con los filtros de búsqueda capturados.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1043, 2, 'No information exists with the captured search filters.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1043, 3, 'Não existe informação com os filtros de busca inseridos.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1044, 1, 'No es posible publicar el complemento de pago, faltan documentos fiscales por publicar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1044, 2, 'Cannot publish payment supplement, fiscal documents are missing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1044, 3, 'Não é possível publicar o complemento de pagamento, faltam documentos fiscais para publicar.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1045, 1, 'Se requiere publicar el complemento de pago en formato XML.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1045, 2, 'The payment supplement must be published in XML format.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1045, 3, 'É necessário publicar o complemento de pagamento em formato XML.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1046, 1, 'El periodo de búsqueda no puede ser superior a 6 meses, favor de validar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1046, 2, 'The search period cannot exceed 6 months, please validate.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1046, 3, 'O período de busca não pode ser superior a 6 meses, favor validar.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1047, 1, 'La fecha final no puede ser menor a la fecha inicio.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1047, 2, 'The end date cannot be less than the start date.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1047, 3, 'A data final não pode ser menor que a data de início.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1048, 1, 'Se requiere publicar factura en formato XML.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1048, 2, 'Invoice must be published in XML format.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1048, 3, 'É necessário publicar a fatura em formato XML.');

-- ----------------------------------------------------------------------------
-- CatPacRespuestas (dict_id: 1049-1050) - Prefijo: PAC
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1049, 1, 'Catálogo de respuestas del PAC Detecno de todas las validaciones que realiza en cada documento.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1049, 2, 'Detecno PAC response catalog for all validations performed on each document.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1049, 3, 'Catálogo de respostas do PAC Detecno de todas as validações realizadas em cada documento.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1050, 1, 'Catálogo de respuestas de todas las validaciones que realiza en cada documento.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1050, 2, 'Response catalog for all validations performed on each document.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1050, 3, 'Catálogo de respostas de todas as validações realizadas em cada documento.');

-- ----------------------------------------------------------------------------
-- CatEstatusFactura (dict_id: 1051-1064) - Prefijo: EFA
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1051, 1, 'Rechazo Comercial');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1051, 2, 'Commercial Rejection');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1051, 3, 'Rejeição Comercial');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1052, 1, 'Pendiente Addenda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1052, 2, 'Pending Addenda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1052, 3, 'Adendo Pendente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1053, 1, 'Recibido Parcial');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1053, 2, 'Partial Received');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1053, 3, 'Recebido Parcial');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1054, 1, 'Pendiente de Contabilizar');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1054, 2, 'Pending Accounting');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1054, 3, 'Pendente de Contabilização');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1055, 1, 'En proceso de descarga');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1055, 2, 'Download in progress');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1055, 3, 'Em processo de download');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1056, 1, 'Desglose de factura');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1056, 2, 'Invoice breakdown');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1056, 3, 'Detalhamento de fatura');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1057, 1, 'Pendiente de envío a contabilizar');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1057, 2, 'Pending submission for accounting');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1057, 3, 'Pendente de envio para contabilização');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1058, 1, 'Pendiente de Pago');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1058, 2, 'Pending Payment');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1058, 3, 'Pendente de Pagamento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1059, 1, 'Pagado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1059, 2, 'Paid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1059, 3, 'Pago');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1060, 1, 'Pendiente de complemento');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1060, 2, 'Pending supplement');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1060, 3, 'Pendente de complemento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1061, 1, 'Completado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1061, 2, 'Completed');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1061, 3, 'Completado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1062, 1, 'Rechazo Contable');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1062, 2, 'Accounting Rejection');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1062, 3, 'Rejeição Contábil');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1063, 1, 'No válido fiscal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1063, 2, 'Not fiscally valid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1063, 3, 'Não válido fiscalmente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1064, 1, 'Pago Manual');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1064, 2, 'Manual Payment');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1064, 3, 'Pagamento Manual');

-- ----------------------------------------------------------------------------
-- CatEstatusNotaCredito (dict_id: 1065-1068) - Prefijo: ENC
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1065, 1, 'Nota de crédito pendiente de relacionar');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1065, 2, 'Credit note pending to relate');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1065, 3, 'Nota de crédito pendente de relacionar');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1066, 1, 'Nota de crédito relacionada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1066, 2, 'Credit note related');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1066, 3, 'Nota de crédito relacionada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1067, 1, 'Nota de crédito cancelada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1067, 2, 'Credit note cancelled');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1067, 3, 'Nota de crédito cancelada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1068, 1, 'Nota de crédito borrada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1068, 2, 'Credit note deleted');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1068, 3, 'Nota de crédito excluída');

-- ----------------------------------------------------------------------------
-- CatEstatusPago (dict_id: 1069-1072) - Prefijo: EPA
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1069, 1, 'Pendiente de complemento');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1069, 2, 'Pending supplement');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1069, 3, 'Pendente de complemento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1070, 1, 'Complemento relacionado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1070, 2, 'Related supplement');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1070, 3, 'Complemento relacionado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1071, 1, 'Pago cancelado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1071, 2, 'Payment cancelled');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1071, 3, 'Pagamento cancelado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1072, 1, 'Pago borrado lógico');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1072, 2, 'Payment soft deleted');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1072, 3, 'Pagamento excluído logicamente');

-- ----------------------------------------------------------------------------
-- CatEstatusRecepcion (dict_id: 1073-1081) - Prefijo: ERC
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1073, 1, 'Recepción disponible');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1073, 2, 'Reception available');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1073, 3, 'Recepção disponível');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1074, 1, 'Recepción consumida (Relación con factura)');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1074, 2, 'Reception consumed (Invoice relationship)');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1074, 3, 'Recepção consumida (Relação com fatura)');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1075, 1, 'Recepción consumida manual');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1075, 2, 'Manual consumed reception');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1075, 3, 'Recepção consumida manual');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1076, 1, 'En proceso de contabilizar factura');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1076, 2, 'Invoice accounting in progress');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1076, 3, 'Em processo de contabilização de fatura');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1077, 1, 'Factura rechazada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1077, 2, 'Invoice rejected');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1077, 3, 'Fatura rejeitada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1078, 1, 'Factura en proceso de pago');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1078, 2, 'Invoice payment in progress');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1078, 3, 'Fatura em processo de pagamento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1079, 1, 'Recepción pagada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1079, 2, 'Reception paid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1079, 3, 'Recepção paga');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1080, 1, 'Recepción cancelada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1080, 2, 'Reception cancelled');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1080, 3, 'Recepção cancelada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1081, 1, 'Recepción borrada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1081, 2, 'Reception deleted');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1081, 3, 'Recepção excluída');

-- ----------------------------------------------------------------------------
-- CatTipoOrigenRecepcion (dict_id: 1082-1085) - Prefijo: TOR
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1082, 1, 'Proveedores de mercancía ODBMS');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1082, 2, 'ODBMS merchandise suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1082, 3, 'Fornecedores de mercadoria ODBMS');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1083, 1, 'Proveedores de transporte Carta Porte');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1083, 2, 'Bill of Lading transport suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1083, 3, 'Fornecedores de transporte Carta Porte');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1084, 1, 'Proveedores de insumos SAP');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1084, 2, 'SAP supplies suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1084, 3, 'Fornecedores de insumos SAP');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1085, 1, 'Proveedores de servicios');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1085, 2, 'Service suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1085, 3, 'Fornecedores de serviços');

-- ----------------------------------------------------------------------------
-- CatTipoEntregaGuia (dict_id: 1086-1091) - Prefijo: TEG
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1086, 1, 'Despacho a domicilio');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1086, 2, 'Home delivery');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1086, 3, 'Entrega a domicílio');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1087, 1, 'Envío a tiendas desde CEDIS');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1087, 2, 'Shipment to stores from CEDIS');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1087, 3, 'Envio para lojas a partir do CEDIS');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1088, 1, 'Envío entre tiendas y CEDIS');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1088, 2, 'Shipment between stores and CEDIS');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1088, 3, 'Envio entre lojas e CEDIS');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1089, 1, 'Mercancía');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1089, 2, 'Merchandise');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1089, 3, 'Mercadoria');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1090, 1, 'Servicios profesionales');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1090, 2, 'Professional services');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1090, 3, 'Serviços profissionais');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1091, 1, 'Insumos para tienda u ODA');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1091, 2, 'Supplies for store or ODA');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1091, 3, 'Insumos para loja ou ODA');

-- ----------------------------------------------------------------------------
-- CatTipoProveedor (dict_id: 1092-1095) - Prefijo: TPR
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1092, 1, 'Proveedores de mercancía');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1092, 2, 'Merchandise suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1092, 3, 'Fornecedores de mercadoria');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1093, 1, 'Proveedores de transporte');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1093, 2, 'Transport suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1093, 3, 'Fornecedores de transporte');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1094, 1, 'Proveedores indirectos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1094, 2, 'Indirect suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1094, 3, 'Fornecedores indiretos');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1095, 1, 'Proveedores de servicios');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1095, 2, 'Service suppliers');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1095, 3, 'Fornecedores de serviços');

-- CatOrigenCartaPorte (dict_id: 1096-1099) - Prefijo: OCP
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1096, 1, 'Origen pendiente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1096, 2, 'Pending origin');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1096, 3, 'Origem pendente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1097, 1, 'Origen local');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1097, 2, 'Local origin');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1097, 3, 'Origem local');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1098, 1, 'Origen foráneo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1098, 2, 'Foreign origin');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1098, 3, 'Origem estrangeira');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1099, 1, 'Origen mixto');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1099, 2, 'Mixed origin');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (1099, 3, 'Origem mista');

-- ============================================================================
-- MENSAJES DE FISCAL-API - Errores Sistema (ERR)
-- dict_id: 2000-2099 - Errores de Archivo XML
-- ============================================================================
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2000, 1, 'El archivo está vacío');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2000, 2, 'The file is empty');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2000, 3, 'O arquivo está vazio');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2001, 1, 'El archivo debe tener extensión .xml');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2001, 2, 'The file must have .xml extension');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2001, 3, 'O arquivo deve ter extensão .xml');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2002, 1, 'Error procesando archivo XML fiscal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2002, 2, 'Error processing fiscal XML file');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2002, 3, 'Erro ao processar arquivo XML fiscal');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2003, 1, 'El XML no tiene una estructura válida');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2003, 2, 'The XML does not have a valid structure');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2003, 3, 'O XML não tem uma estrutura válida');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2004, 1, 'Error leyendo archivo XML');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2004, 2, 'Error reading XML file');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2004, 3, 'Erro ao ler arquivo XML');

-- dict_id: 2010-2019 - Errores XSD
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2010, 1, 'El XML no cumple con la estructura XSD');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2010, 2, 'The XML does not comply with XSD structure');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2010, 3, 'O XML não cumpre com a estrutura XSD');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2011, 1, 'El XML no cumple con la estructura XSD Pagos 2.0');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2011, 2, 'The XML does not comply with XSD Pagos 2.0 structure');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2011, 3, 'O XML não cumpre com a estrutura XSD Pagos 2.0');

-- dict_id: 2020-2039 - Errores JAXB/Parseo
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2020, 1, 'Error configurando procesador CartaPorte');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2020, 2, 'Error configuring CartaPorte processor');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2020, 3, 'Erro ao configurar processador CartaPorte');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2021, 1, 'Error configurando procesador CFDI');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2021, 2, 'Error configuring CFDI processor');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2021, 3, 'Erro ao configurar processador CFDI');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2022, 1, 'Error configurando procesador Pagos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2022, 2, 'Error configuring Pagos processor');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2022, 3, 'Erro ao configurar processador Pagos');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2023, 1, 'Error procesando CartaPorte');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2023, 2, 'Error processing CartaPorte');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2023, 3, 'Erro ao processar CartaPorte');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2024, 1, 'Error procesando CFDI');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2024, 2, 'Error processing CFDI');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2024, 3, 'Erro ao processar CFDI');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2025, 1, 'Error procesando Pagos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2025, 2, 'Error processing Pagos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2025, 3, 'Erro ao processar Pagos');

-- dict_id: 2040-2059 - Errores Tipo Documento
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2040, 1, 'Error detectando tipo de documento XML');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2040, 2, 'Error detecting XML document type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2040, 3, 'Erro ao detectar tipo de documento XML');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2041, 1, 'Tipo de documento fiscal no válido');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2041, 2, 'Invalid fiscal document type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2041, 3, 'Tipo de documento fiscal não válido');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2042, 1, 'Tipo de CFDI no soportado en este procesador');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2042, 2, 'CFDI type not supported in this processor');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2042, 3, 'Tipo de CFDI não suportado neste processador');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2043, 1, 'Se esperaba Factura (I) pero se encontró tipo diferente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2043, 2, 'Expected Invoice (I) but found different type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2043, 3, 'Esperava-se Fatura (I) mas encontrou tipo diferente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2044, 1, 'Se esperaba Nota de Crédito (E) pero se encontró tipo diferente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2044, 2, 'Expected Credit Note (E) but found different type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2044, 3, 'Esperava-se Nota de Crédito (E) mas encontrou tipo diferente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2045, 1, 'Tipo de comprobante no válido');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2045, 2, 'Invalid voucher type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2045, 3, 'Tipo de comprovante não válido');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2046, 1, 'El tipo de comprobante debe ser P (Pago)');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2046, 2, 'The voucher type must be P (Payment)');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2046, 3, 'O tipo de comprovante deve ser P (Pagamento)');

-- dict_id: 2060-2079 - Errores Complemento de Pago
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2060, 1, 'Datos de Pagos no pueden ser nulos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2060, 2, 'Payment data cannot be null');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2060, 3, 'Dados de Pagos não podem ser nulos');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2061, 1, 'Complemento de Pago debe incluir nodo Totales');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2061, 2, 'Payment Complement must include Totales node');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2061, 3, 'Complemento de Pagamento deve incluir nó Totales');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2062, 1, 'Nodo Totales debe especificar el monto total de pagos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2062, 2, 'Totales node must specify total payment amount');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2062, 3, 'Nó Totales deve especificar o valor total de pagamentos');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2063, 1, 'Complemento de Pago debe contener al menos un elemento Pago');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2063, 2, 'Payment Complement must contain at least one Pago element');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2063, 3, 'Complemento de Pagamento deve conter ao menos um elemento Pago');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2064, 1, 'Complemento de Pago debe especificar la versión');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2064, 2, 'Payment Complement must specify version');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2064, 3, 'Complemento de Pagamento deve especificar a versão');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2065, 1, 'El complemento de pago ya se encuentra registrado en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2065, 2, 'The payment complement is already registered in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2065, 3, 'O complemento de pagamento já está registrado no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2066, 1, 'Error convirtiendo XML a Document');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2066, 2, 'Error converting XML to Document');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2066, 3, 'Erro ao converter XML para Document');

-- dict_id: 2080-2099 - Errores Validación Negocio General
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2080, 1, 'El tipo de addenda debe ser 5 para complementos de pago');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2080, 2, 'Addenda type must be 5 for payment complements');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2080, 3, 'O tipo de addenda deve ser 5 para complementos de pagamento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2081, 1, 'El RFC receptor no está autorizado en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2081, 2, 'The receiver RFC is not authorized in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2081, 3, 'O RFC receptor não está autorizado no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2082, 1, 'La versión del complemento de pago no es vigente en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2082, 2, 'The payment complement version is not current in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2082, 3, 'A versão do complemento de pagamento não está vigente no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2083, 1, 'El documento relacionado no se encuentra registrado en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2083, 2, 'The related document is not registered in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2083, 3, 'O documento relacionado não está registrado no sistema');

-- ============================================================================
-- MENSAJES DE NEGOCIO FISCAL-API (BUS) - dict_id: 3000-3999
-- Agregando los que faltan en catálogo
-- ============================================================================

-- BUS008-BUS020: Nuevos mensajes de negocio de fiscal-api
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3000, 1, 'El RFC del receptor no acepta documentos fiscales. Verifica que el RFC esté autorizado para recibir comprobantes fiscales.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3000, 2, 'The receiver RFC does not accept fiscal documents. Verify that the RFC is authorized to receive tax receipts.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3000, 3, 'O RFC do receptor não aceita documentos fiscais. Verifique se o RFC está autorizado a receber comprovantes fiscais.');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3001, 1, 'El RFC del receptor no está activo en el período de vigencia especificado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3001, 2, 'The receiver RFC is not active in the specified validity period');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3001, 3, 'O RFC do receptor não está ativo no período de vigência especificado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3002, 1, 'El RFC del receptor no se encuentra registrado en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3002, 2, 'The receiver RFC is not registered in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3002, 3, 'O RFC do receptor não está registrado no sistema');

-- Addenda BUS
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3003, 1, 'La addenda debe contener el nodo Addenda_Sodimac para documentos de mercancía, servicios y transporte local');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3003, 2, 'The addenda must contain the Addenda_Sodimac node for merchandise, services and local transport documents');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3003, 3, 'A addenda deve conter o nó Addenda_Sodimac para documentos de mercadoria, serviços e transporte local');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3004, 1, 'La addenda debe contener el nodo Addenda_Sodimac_CartaPorte para transporte foráneo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3004, 2, 'The addenda must contain the Addenda_Sodimac_CartaPorte node for external transport');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3004, 3, 'A addenda deve conter o nó Addenda_Sodimac_CartaPorte para transporte externo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3005, 1, 'El campo RFC en la addenda es obligatorio');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3005, 2, 'The RFC field in the addenda is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3005, 3, 'O campo RFC na addenda é obrigatório');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3006, 1, 'El campo UUID en la addenda es obligatorio');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3006, 2, 'The UUID field in the addenda is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3006, 3, 'O campo UUID na addenda é obrigatório');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3007, 1, 'El campo Folio en la addenda es obligatorio');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3007, 2, 'The Folio field in the addenda is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3007, 3, 'O campo Folio na addenda é obrigatório');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3008, 1, 'El campo NoOC (Número de Orden de Compra) en la addenda es obligatorio');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3008, 2, 'The NoOC (Purchase Order Number) field in the addenda is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3008, 3, 'O campo NoOC (Número de Ordem de Compra) na addenda é obrigatório');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3009, 1, 'El campo Proveedor en la addenda es obligatorio');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3009, 2, 'The Provider field in the addenda is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3009, 3, 'O campo Fornecedor na addenda é obrigatório');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3010, 1, 'El formato del RFC en la addenda no es válido');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3010, 2, 'The RFC format in the addenda is not valid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3010, 3, 'O formato do RFC na addenda não é válido');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3011, 1, 'El UUID en la addenda no coincide con el UUID del TimbreFiscalDigital');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3011, 2, 'The UUID in the addenda does not match the TimbreFiscalDigital UUID');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3011, 3, 'O UUID na addenda não coincide com o UUID do TimbreFiscalDigital');

-- Versión CFDI
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3012, 1, 'La versión del documento no se encuentra configurada en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3012, 2, 'The document version is not configured in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3012, 3, 'A versão do documento não está configurada no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3013, 1, 'La versión del documento no está vigente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3013, 2, 'The document version is not current');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3013, 3, 'A versão do documento não está vigente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3014, 1, 'El documento debe ser versión CFDI 4.0');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3014, 2, 'The document must be CFDI version 4.0');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3014, 3, 'O documento deve ser versão CFDI 4.0');

-- Tipo Documento
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3015, 1, 'El tipo de documento debe ser I (Factura) o E (Nota de Crédito)');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3015, 2, 'The document type must be I (Invoice) or E (Credit Note)');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3015, 3, 'O tipo de documento deve ser I (Fatura) ou E (Nota de Crédito)');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3016, 1, 'El tipo de comprobante no está permitido para este proceso');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3016, 2, 'The voucher type is not allowed for this process');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3016, 3, 'O tipo de comprovante não é permitido para este processo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3017, 1, 'Las Notas de Crédito deben incluir CFDIs relacionados con tipo de relación 01');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3017, 2, 'Credit Notes must include related CFDIs with relation type 01');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3017, 3, 'As Notas de Crédito devem incluir CFDIs relacionados com tipo de relação 01');

-- Validación SAT
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3018, 1, 'El documento fiscal no se encuentra vigente en el SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3018, 2, 'The fiscal document is not current in SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3018, 3, 'O documento fiscal não está vigente no SAT');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3019, 1, 'El documento fiscal se encuentra cancelado en el SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3019, 2, 'The fiscal document is cancelled in SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3019, 3, 'O documento fiscal está cancelado no SAT');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3020, 1, 'El UUID no se encuentra registrado en el SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3020, 2, 'The UUID is not registered in SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3020, 3, 'O UUID não está registrado no SAT');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3021, 1, 'El sello digital del documento no es válido según el SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3021, 2, 'The document digital seal is not valid according to SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3021, 3, 'O selo digital do documento não é válido segundo o SAT');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3022, 1, 'No fue posible validar el estatus del documento en el SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3022, 2, 'It was not possible to validate the document status in SAT');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3022, 3, 'Não foi possível validar o status do documento no SAT');

-- Emisor
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3023, 1, 'El RFC del emisor no se encuentra registrado como proveedor autorizado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3023, 2, 'The issuer RFC is not registered as an authorized supplier');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3023, 3, 'O RFC do emissor não está registrado como fornecedor autorizado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3024, 1, 'El emisor se encuentra inactivo en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3024, 2, 'The issuer is inactive in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3024, 3, 'O emissor está inativo no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3025, 1, 'El RFC del emisor no es válido');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3025, 2, 'The issuer RFC is not valid');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3025, 3, 'O RFC do emissor não é válido');

-- Duplicidad
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3026, 1, 'El documento con UUID ya se encuentra registrado en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3026, 2, 'The document with UUID is already registered in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3026, 3, 'O documento com UUID já está registrado no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3027, 1, 'El documento con Serie y Folio ya se encuentra registrado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3027, 2, 'The document with Series and Folio is already registered');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3027, 3, 'O documento com Série e Folio já está registrado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3028, 1, 'El archivo ya fue procesado anteriormente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3028, 2, 'The file was already processed previously');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3028, 3, 'O arquivo já foi processado anteriormente');

-- Validación Contenido
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3029, 1, 'El documento debe contener al menos un concepto');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3029, 2, 'The document must contain at least one concept');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3029, 3, 'O documento deve conter ao menos um conceito');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3030, 1, 'El total del documento no puede ser cero');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3030, 2, 'The document total cannot be zero');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3030, 3, 'O total do documento não pode ser zero');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3031, 1, 'Los montos de los conceptos no coinciden con el total del documento');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3031, 2, 'The concept amounts do not match the document total');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3031, 3, 'Os valores dos conceitos não coincidem com o total do documento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3032, 1, 'Los impuestos calculados no coinciden con los declarados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3032, 2, 'The calculated taxes do not match the declared ones');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3032, 3, 'Os impostos calculados não coincidem com os declarados');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3033, 1, 'El documento contiene impuestos no permitidos por Sodimac');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3033, 2, 'The document contains taxes not allowed by Sodimac');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3033, 3, 'O documento contém impostos não permitidos pela Sodimac');

-- CFDIs Relacionados NC
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3034, 1, 'La Nota de Crédito debe incluir al menos un CFDI relacionado en el nodo CfdiRelacionados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3034, 2, 'The Credit Note must include at least one related CFDI in the CfdiRelacionados node');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3034, 3, 'A Nota de Crédito deve incluir ao menos um CFDI relacionado no nó CfdiRelacionados');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3035, 1, 'La Factura relacionada no se encuentra registrada en el sistema. Debe registrar primero la Factura antes de cargar la Nota de Crédito');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3035, 2, 'The related Invoice is not registered in the system. You must register the Invoice first before loading the Credit Note');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3035, 3, 'A Fatura relacionada não está registrada no sistema. Deve registrar primeiro a Fatura antes de carregar a Nota de Crédito');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3036, 1, 'El CFDI relacionado no es una Factura (tipo I). Solo se pueden relacionar NC con Facturas');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3036, 2, 'The related CFDI is not an Invoice (type I). Only Credit Notes can be related to Invoices');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3036, 3, 'O CFDI relacionado não é uma Fatura (tipo I). Só podem ser relacionadas NC com Faturas');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3037, 1, 'El tipo de relación no es válido. Para Notas de Crédito debe ser 01');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3037, 2, 'The relation type is not valid. For Credit Notes it must be 01');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3037, 3, 'O tipo de relação não é válido. Para Notas de Crédito deve ser 01');

-- Actualización Facturas/NC
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3038, 1, 'El documento con UUID no se encuentra registrado en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3038, 2, 'The document with UUID is not registered in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3038, 3, 'O documento com UUID não está registrado no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3039, 1, 'El documento con UUID no pertenece al proveedor especificado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3039, 2, 'The document with UUID does not belong to the specified supplier');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3039, 3, 'O documento com UUID não pertence ao fornecedor especificado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3040, 1, 'La addenda del documento no se encuentra registrada en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3040, 2, 'The document addenda is not registered in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3040, 3, 'A addenda do documento não está registrada no sistema');

-- Transición de estatus
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3041, 1, 'El estatus no es válido para el tipo de documento');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3041, 2, 'The status is not valid for the document type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3041, 3, 'O status não é válido para o tipo de documento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3042, 1, 'El estatus no existe en el catálogo de estatus');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3042, 2, 'The status does not exist in the status catalog');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3042, 3, 'O status não existe no catálogo de status');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3043, 1, 'La transición de estatus no está permitida');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3043, 2, 'The status transition is not allowed');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3043, 3, 'A transição de status não é permitida');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3044, 1, 'No se puede actualizar un documento en estatus final alcanzado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3044, 2, 'Cannot update a document in final status');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3044, 3, 'Não é possível atualizar um documento em status final');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3045, 1, 'El estatus del documento ya es el indicado. No se requiere actualización');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3045, 2, 'The document status is already the indicated one. No update required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3045, 3, 'O status do documento já é o indicado. Não é necessária atualização');

-- Permisos
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3046, 1, 'El usuario no tiene permisos para actualizar documentos fiscales');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3046, 2, 'The user does not have permissions to update fiscal documents');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3046, 3, 'O usuário não tem permissões para atualizar documentos fiscais');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3047, 1, 'Solo el proveedor propietario puede actualizar este documento');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3047, 2, 'Only the owner supplier can update this document');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3047, 3, 'Somente o fornecedor proprietário pode atualizar este documento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3048, 1, 'El documento está en proceso automático y no puede ser modificado manualmente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3048, 2, 'The document is in automatic process and cannot be modified manually');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (3048, 3, 'O documento está em processo automático e não pode ser modificado manualmente');

-- ============================================================================
-- MENSAJES DE ÉXITO FISCAL (RES) - dict_id: 4000-4099
-- Mensajes de FiscalSuccessCode
-- ============================================================================
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4000, 1, 'Factura registrada exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4000, 2, 'Invoice registered successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4000, 3, 'Fatura registrada com sucesso');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4001, 1, 'Factura registrada exitosamente - Pendiente de Addenda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4001, 2, 'Invoice registered successfully - Pending Addenda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4001, 3, 'Fatura registrada com sucesso - Pendente de Addenda');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4002, 1, 'Nota de Crédito registrada exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4002, 2, 'Credit Note registered successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4002, 3, 'Nota de Crédito registrada com sucesso');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4003, 1, 'Nota de Crédito registrada exitosamente - Pendiente de Addenda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4003, 2, 'Credit Note registered successfully - Pending Addenda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4003, 3, 'Nota de Crédito registrada com sucesso - Pendente de Addenda');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4004, 1, 'Complemento de Pago registrado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4004, 2, 'Payment Complement registered successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4004, 3, 'Complemento de Pagamento registrado com sucesso');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4005, 1, 'Factura actualizada exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4005, 2, 'Invoice updated successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4005, 3, 'Fatura atualizada com sucesso');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4006, 1, 'Factura actualizada exitosamente - Addenda actualizada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4006, 2, 'Invoice updated successfully - Addenda updated');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4006, 3, 'Fatura atualizada com sucesso - Addenda atualizada');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4007, 1, 'Nota de Crédito actualizada exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4007, 2, 'Credit Note updated successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4007, 3, 'Nota de Crédito atualizada com sucesso');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4008, 1, 'Nota de Crédito actualizada exitosamente - Addenda actualizada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4008, 2, 'Credit Note updated successfully - Addenda updated');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4008, 3, 'Nota de Crédito atualizada com sucesso - Addenda atualizada');

-- ============================================================================
-- ADVERTENCIAS FISCAL (WRN) - dict_id: 4100-4199
-- Mensajes de FiscalWarningCode que faltan
-- ============================================================================
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4100, 1, 'El total calculado no coincide con el total declarado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4100, 2, 'The calculated total does not match the declared total');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4100, 3, 'O total calculado não coincide com o total declarado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4101, 1, 'Tipo de cambio no especificado, usando valor por defecto');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4101, 2, 'Exchange rate not specified, using default value');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4101, 3, 'Taxa de câmbio não especificada, usando valor padrão');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4102, 1, 'Moneda no especificada, usando MXN por defecto');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4102, 2, 'Currency not specified, using MXN by default');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4102, 3, 'Moeda não especificada, usando MXN por padrão');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4103, 1, 'El pago es parcial, saldo pendiente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4103, 2, 'The payment is partial, pending balance');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4103, 3, 'O pagamento é parcial, saldo pendente');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4104, 1, 'Número de operación no especificado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4104, 2, 'Operation number not specified');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4104, 3, 'Número de operação não especificado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4105, 1, 'Información bancaria incompleta');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4105, 2, 'Incomplete bank information');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4105, 3, 'Informação bancária incompleta');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4106, 1, 'Documento relacionado con saldo cero');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4106, 2, 'Related document with zero balance');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4106, 3, 'Documento relacionado com saldo zero');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4107, 1, 'El monto pagado excede el saldo del documento');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4107, 2, 'The paid amount exceeds the document balance');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4107, 3, 'O valor pago excede o saldo do documento');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4108, 1, 'Datos opcionales de CartaPorte no especificados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4108, 2, 'Optional CartaPorte data not specified');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4108, 3, 'Dados opcionais de CartaPorte não especificados');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4109, 1, 'Figuras de transporte no especificadas');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4109, 2, 'Transport figures not specified');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4109, 3, 'Figuras de transporte não especificadas');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4110, 1, 'Addenda sin contenido');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4110, 2, 'Addenda without content');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4110, 3, 'Addenda sem conteúdo');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4111, 1, 'Campos opcionales de addenda vacíos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4111, 2, 'Optional addenda fields empty');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4111, 3, 'Campos opcionais de addenda vazios');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4112, 1, 'Emisor no existía, se creó nuevo registro');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4112, 2, 'Issuer did not exist, new record created');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4112, 3, 'Emissor não existia, novo registro criado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4113, 1, 'Receptor no existía, se creó nuevo registro');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4113, 2, 'Receiver did not exist, new record created');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4113, 3, 'Receptor não existia, novo registro criado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4114, 1, 'Archivo con nombre similar ya fue procesado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4114, 2, 'File with similar name was already processed');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4114, 3, 'Arquivo com nome similar já foi processado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4115, 1, 'Validación SAT tardó más de lo esperado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4115, 2, 'SAT validation took longer than expected');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4115, 3, 'Validação SAT demorou mais do que o esperado');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4116, 1, 'Respuesta SAT incompleta pero válida');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4116, 2, 'Incomplete but valid SAT response');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4116, 3, 'Resposta SAT incompleta mas válida');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4117, 1, 'Versión del documento será obsoleta próximamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4117, 2, 'Document version will become obsolete soon');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4117, 3, 'Versão do documento será obsoleta em breve');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4118, 1, 'Datos opcionales no especificados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4118, 2, 'Optional data not specified');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4118, 3, 'Dados opcionais não especificados');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4119, 1, 'Documento registrado sin número de recepción. Requiere vinculación manual');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4119, 2, 'Document registered without reception number. Requires manual linking');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4119, 3, 'Documento registrado sem número de recepção. Requer vinculação manual');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4120, 1, 'La recepción no se encuentra disponible en el sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4120, 2, 'The reception is not available in the system');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4120, 3, 'A recepção não está disponível no sistema');

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4121, 1, 'La recepción ya se encuentra vinculada a otro documento fiscal');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4121, 2, 'The reception is already linked to another fiscal document');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (4121, 3, 'A recepção já está vinculada a outro documento fiscal');

-- ============================================================================
-- CATALOG_HEADER: Definición de catálogos
-- Columna PREFIX indica el prefijo a usar para nuevos registros en catalog_detail
-- ============================================================================

INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, status) VALUES
(1, 'CatEstatusCartaPorte', 'ECP', 'Estatus Carta Porte', 'Catálogo de estados para guías de embarque/carta porte', 'transporte', 1),
(2, 'CatEstatusCartaPorteFBC', 'ECF', 'Estatus Carta Porte FBC', 'Catálogo de estados para guías de embarque en portal FBC', 'transporte', 1),
(3, 'CatEstatusRegistro', 'ERG', 'Estatus Registro', 'Catálogo de estados genéricos para registros', 'general', 1),
(4, 'CatMsg', 'MSG', 'Categorías de Mensajes', 'Catálogo maestro de categorías de mensajes del sistema', 'sistema', 1),
(5, 'CatMsgExitoso', 'RES', 'Mensajes Exitosos', 'Catálogo de mensajes de éxito del sistema', 'sistema', 1),
(6, 'CatMsgErrorSistema', 'ERR', 'Mensajes Error Sistema', 'Catálogo de mensajes de error técnico', 'sistema', 1),
(7, 'CatMsgNegocio', 'BUS', 'Mensajes Negocio', 'Catálogo de mensajes de validación de negocio', 'sistema', 1),
(8, 'CatMsgErrorInfra', 'INE', 'Mensajes Error Infraestructura', 'Catálogo de mensajes de error de infraestructura', 'sistema', -1),
(9, 'CatMsgExcepcion', 'EXC', 'Mensajes Excepción', 'Catálogo de mensajes de excepciones', 'sistema', -1),
(10, 'CatMsgInformativo', 'INF', 'Mensajes Informativos', 'Catálogo de mensajes informativos', 'sistema', 1),
(11, 'CatMsgAdvertencia', 'WRN', 'Mensajes Advertencia', 'Catálogo de mensajes de advertencia', 'sistema', 1),
(12, 'CatPacRespuestasDetecno', 'PDT', 'Respuestas PAC Detecno', 'Catálogo de códigos de respuesta del PAC Detecno', 'sistema', -1),
(13, 'CatPacRespuestas', 'PAC', 'Respuestas PAC', 'Catálogo maestro de tipos de respuesta PAC', 'sistema', 1),
(14, 'CatMsgConfirmacion', 'CFM', 'Mensajes Confirmación', 'Catálogo de mensajes de confirmación', 'sistema', -1),
(15, 'CatEstatusFactura', 'EFA', 'Estatus Factura', 'Catálogo de estados del ciclo de vida de facturas', 'fiscal', 1),
(16, 'CatEstatusNotaCredito', 'ENC', 'Estatus Nota Crédito', 'Catálogo de estados para notas de crédito', 'fiscal', 1),
(17, 'CatEstatusPago', 'EPA', 'Estatus Pago', 'Catálogo de estados para complementos de pago', 'fiscal', 1),
(18, 'CatEstatusRecepcion', 'ERC', 'Estatus Recepción', 'Catálogo de estados para recepciones de mercancía', 'general', 1),
(19, 'CatEstatusComplemento', 'ECM', 'Estatus Complemento', 'Catálogo de estados para complementos fiscales', 'fiscal', -1),
(20, 'CatTipoOrigenRecepcion', 'TOR', 'Tipo Origen Recepción', 'Catálogo de orígenes de recepción por tipo de proveedor', 'general', 1),
(21, 'CatTipoEntregaGuia', 'TEG', 'Tipo Entrega Guía', 'Catálogo de tipos de entrega para guías', 'transporte', 1),
(22, 'CatTipoProveedor', 'TPR', 'Tipo Proveedor', 'Catálogo de clasificación de proveedores', 'general', 1),
(23, 'CatOrigenCartaPorte', 'OCP', 'Origen Carta Porte', 'Catálogo de orígenes para carta porte', 'transporte', 1);

SELECT setval('shared_catalogs.catalog_header_id_seq', 23, true);

-- ============================================================================
-- CATALOG_DETAIL: Elementos con claves estandarizadas
-- Patrón: {PREFIJO}{NUMERO} donde PREFIJO identifica el catálogo
-- ============================================================================

-- CatEstatusCartaPorte (header_id=1) - Prefijo: ECP
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(1, 'ECP001', 1000, NULL, 1, 1),
(1, 'ECP002', 1001, NULL, 2, 1),
(1, 'ECP003', 1002, NULL, 3, 1),
(1, 'ECP004', 1003, NULL, 4, 1),
(1, 'ECP005', 1004, NULL, 5, 1),
(1, 'ECP006', 1005, NULL, 6, 1),
(1, 'ECP007', 1006, NULL, 7, 1),
(1, 'ECP008', 1007, NULL, 8, 1),
(1, 'ECP009', 1008, NULL, 9, 1),
(1, 'ECP010', 1009, NULL, 10, 1),
(1, 'ECP011', 1010, NULL, 11, 1),
(1, 'ECP012', 1011, NULL, 12, 1);

-- CatEstatusCartaPorteFBC (header_id=2) - Prefijo: ECF
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(2, 'ECF001', 1012, 'Amarillo', 1, 1),
(2, 'ECF002', 1013, 'Amarillo', 2, 1),
(2, 'ECF003', 1014, 'Amarillo', 3, 1),
(2, 'ECF004', 1015, 'Amarillo', 4, 1),
(2, 'ECF005', 1016, 'Amarillo', 5, 1),
(2, 'ECF006', 1017, 'Rojo', 6, 1),
(2, 'ECF007', 1018, 'Verde', 7, 1),
(2, 'ECF008', 1019, 'Rojo', 8, 1),
(2, 'ECF009', 1020, 'Rojo', 9, 1);

-- CatEstatusRegistro (header_id=3) - Prefijo: ERG
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(3, 'ERG001', 1021, NULL, 1, 1),
(3, 'ERG002', 1022, NULL, 2, 1),
(3, 'ERG003', 1023, NULL, 3, 1);

-- CatMsg (header_id=4) - Prefijo: MSG
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(4, 'MSG001', 1024, NULL, 1, 1),
(4, 'MSG002', 1025, NULL, 2, 1),
(4, 'MSG003', 1026, NULL, 3, 1),
(4, 'MSG004', 1027, NULL, 4, 1),
(4, 'MSG005', 1028, NULL, 5, 1),
(4, 'MSG006', 1029, NULL, 6, 1);

-- CatMsgExitoso (header_id=5) - Prefijo: RES
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(5, 'RES001', 1030, NULL, 1, 1),
(5, 'RES002', 1031, NULL, 2, 1),
(5, 'RES003', 1032, NULL, 3, 1);

-- CatMsgNegocio (header_id=7) - Prefijo: BUS
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(7, 'BUS001', 1033, NULL, 1, 1),
(7, 'BUS002', 1034, NULL, 2, 1),
(7, 'BUS003', 1035, NULL, 3, 1),
(7, 'BUS004', 1036, NULL, 4, 1),
(7, 'BUS005', 1037, NULL, 5, 1),
(7, 'BUS006', 1038, NULL, 6, 1),
(7, 'BUS007', 1039, NULL, 7, 1);

-- CatMsgInformativo (header_id=10) - Prefijo: INF
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(10, 'INF001', 1040, NULL, 1, 1);

-- CatMsgAdvertencia (header_id=11) - Prefijo: WRN
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN001', 1041, NULL, 1, 1),
(11, 'WRN002', 1042, NULL, 2, 1),
(11, 'WRN003', 1043, NULL, 3, 1),
(11, 'WRN004', 1044, NULL, 4, 1),
(11, 'WRN005', 1045, NULL, 5, 1),
(11, 'WRN006', 1046, NULL, 6, 1),
(11, 'WRN007', 1047, NULL, 7, 1),
(11, 'WRN008', 1048, NULL, 8, 1);

-- CatPacRespuestas (header_id=13) - Prefijo: PAC
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(13, 'PAC001', 1049, NULL, 1, 1),
(13, 'PAC002', 1050, NULL, 2, 1);

-- CatEstatusFactura (header_id=15) - Prefijo: EFA
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(15, 'EFA001', 1051, 'Rojo', 1, 1),
(15, 'EFA002', 1052, 'Amarillo', 2, 1),
(15, 'EFA003', 1053, 'Amarillo', 3, 1),
(15, 'EFA004', 1054, 'Amarillo', 4, 1),
(15, 'EFA005', 1055, 'Amarillo', 5, 1),
(15, 'EFA006', 1056, 'Amarillo', 6, 1),
(15, 'EFA007', 1057, 'Amarillo', 7, 1),
(15, 'EFA008', 1058, 'Amarillo', 8, 1),
(15, 'EFA009', 1059, 'Verde', 9, 1),
(15, 'EFA010', 1060, 'Amarillo', 10, 1),
(15, 'EFA011', 1061, 'Verde', 11, 1),
(15, 'EFA012', 1062, 'Rojo', 12, 1),
(15, 'EFA013', 1063, 'Rojo', 13, 1),
(15, 'EFA014', 1064, 'Verde', 14, 1);

-- CatEstatusNotaCredito (header_id=16) - Prefijo: ENC
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(16, 'ENC001', 1065, 'Amarillo', 1, 1),
(16, 'ENC002', 1066, 'Verde', 2, 1),
(16, 'ENC003', 1067, 'Rojo', 3, 1),
(16, 'ENC004', 1068, 'Rojo', 4, 1);

-- CatEstatusPago (header_id=17) - Prefijo: EPA
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(17, 'EPA001', 1069, 'Amarillo', 1, 1),
(17, 'EPA002', 1070, 'Verde', 2, 1),
(17, 'EPA003', 1071, 'Rojo', 3, 1),
(17, 'EPA004', 1072, 'Rojo', 4, 1);

-- CatEstatusRecepcion (header_id=18) - Prefijo: ERC
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(18, 'ERC001', 1073, 'Amarillo', 1, 1),
(18, 'ERC002', 1074, 'Verde', 2, 1),
(18, 'ERC003', 1075, 'Verde', 3, 1),
(18, 'ERC004', 1076, 'Amarillo', 4, 1),
(18, 'ERC005', 1077, 'Rojo', 5, 1),
(18, 'ERC006', 1078, 'Amarillo', 6, 1),
(18, 'ERC007', 1079, 'Verde', 7, 1),
(18, 'ERC008', 1080, 'Amarillo', 8, 1),
(18, 'ERC009', 1081, 'Rojo', 9, 1);

-- CatTipoOrigenRecepcion (header_id=20) - Prefijo: TOR
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(20, 'TOR001', 1082, NULL, 1, 1),
(20, 'TOR002', 1083, NULL, 2, 1),
(20, 'TOR003', 1084, NULL, 3, 1),
(20, 'TOR004', 1085, NULL, 4, 1);

-- CatTipoEntregaGuia (header_id=21) - Prefijo: TEG
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(21, 'TEG001', 1086, NULL, 1, 1),
(21, 'TEG002', 1087, NULL, 2, 1),
(21, 'TEG003', 1088, NULL, 3, 1),
(21, 'TEG004', 1089, NULL, 4, 1),
(21, 'TEG005', 1090, NULL, 5, 1),
(21, 'TEG006', 1091, NULL, 6, 1);

-- CatTipoProveedor (header_id=22) - Prefijo: TPR
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(22, 'TPR001', 1092, NULL, 1, 1),
(22, 'TPR002', 1093, NULL, 2, 1),
(22, 'TPR003', 1094, NULL, 3, 1),
(22, 'TPR004', 1095, NULL, 4, 1);

-- CatOrigenCartaPorte (header_id=23) - Prefijo: OCP
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(23, 'OCP001', 1096, 'Gris', 1, 1),
(23, 'OCP002', 1097, 'Azul', 2, 1),
(23, 'OCP003', 1098, 'Verde', 3, 1),
(23, 'OCP004', 1099, 'Naranja', 4, 1);

-- ============================================================================
-- MENSAJES DE FISCAL-API - catalog_detail
-- ============================================================================

-- CatMsgErrorSistema (header_id=6) - Prefijo: ERR - Errores técnicos de fiscal-api
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
-- Errores XML (ERR001-ERR005)
(6, 'ERR001', 2000, NULL, 1, 1),
(6, 'ERR002', 2001, NULL, 2, 1),
(6, 'ERR003', 2002, NULL, 3, 1),
(6, 'ERR004', 2003, NULL, 4, 1),
(6, 'ERR005', 2004, NULL, 5, 1),
-- Errores XSD (ERR006-ERR007)
(6, 'ERR006', 2010, NULL, 6, 1),
(6, 'ERR007', 2011, NULL, 7, 1),
-- Errores JAXB (ERR008-ERR013)
(6, 'ERR008', 2020, NULL, 8, 1),
(6, 'ERR009', 2021, NULL, 9, 1),
(6, 'ERR010', 2022, NULL, 10, 1),
(6, 'ERR011', 2023, NULL, 11, 1),
(6, 'ERR012', 2024, NULL, 12, 1),
(6, 'ERR013', 2025, NULL, 13, 1),
-- Errores Tipo Documento (ERR014-ERR020)
(6, 'ERR014', 2040, NULL, 14, 1),
(6, 'ERR015', 2041, NULL, 15, 1),
(6, 'ERR016', 2042, NULL, 16, 1),
(6, 'ERR017', 2043, NULL, 17, 1),
(6, 'ERR018', 2044, NULL, 18, 1),
(6, 'ERR019', 2045, NULL, 19, 1),
(6, 'ERR020', 2046, NULL, 20, 1),
-- Errores Complemento Pago (ERR021-ERR027)
(6, 'ERR021', 2060, NULL, 21, 1),
(6, 'ERR022', 2061, NULL, 22, 1),
(6, 'ERR023', 2062, NULL, 23, 1),
(6, 'ERR024', 2063, NULL, 24, 1),
(6, 'ERR025', 2064, NULL, 25, 1),
(6, 'ERR026', 2065, NULL, 26, 1),
(6, 'ERR027', 2066, NULL, 27, 1),
-- Errores Validación Negocio General (ERR028-ERR031)
(6, 'ERR028', 2080, NULL, 28, 1),
(6, 'ERR029', 2081, NULL, 29, 1),
(6, 'ERR030', 2082, NULL, 30, 1),
(6, 'ERR031', 2083, NULL, 31, 1);

-- CatMsgNegocio (header_id=7) - Prefijo: BUS - Mensajes adicionales de fiscal-api
-- Continuando desde BUS008 (BUS001-BUS007 ya existen)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
-- RFC Receptor (BUS008-BUS010)
(7, 'BUS008', 3000, NULL, 8, 1),
(7, 'BUS009', 3001, NULL, 9, 1),
(7, 'BUS010', 3002, NULL, 10, 1),
-- Addenda (BUS011-BUS019)
(7, 'BUS011', 3003, NULL, 11, 1),
(7, 'BUS012', 3004, NULL, 12, 1),
(7, 'BUS013', 3005, NULL, 13, 1),
(7, 'BUS014', 3006, NULL, 14, 1),
(7, 'BUS015', 3007, NULL, 15, 1),
(7, 'BUS016', 3008, NULL, 16, 1),
(7, 'BUS017', 3009, NULL, 17, 1),
(7, 'BUS018', 3010, NULL, 18, 1),
(7, 'BUS019', 3011, NULL, 19, 1),
-- Versión CFDI (BUS020-BUS022)
(7, 'BUS020', 3012, NULL, 20, 1),
(7, 'BUS021', 3013, NULL, 21, 1),
(7, 'BUS022', 3014, NULL, 22, 1),
-- Tipo Documento (BUS023-BUS025)
(7, 'BUS023', 3015, NULL, 23, 1),
(7, 'BUS024', 3016, NULL, 24, 1),
(7, 'BUS025', 3017, NULL, 25, 1),
-- Validación SAT (BUS026-BUS030)
(7, 'BUS026', 3018, NULL, 26, 1),
(7, 'BUS027', 3019, NULL, 27, 1),
(7, 'BUS028', 3020, NULL, 28, 1),
(7, 'BUS029', 3021, NULL, 29, 1),
(7, 'BUS030', 3022, NULL, 30, 1),
-- Emisor (BUS031-BUS033)
(7, 'BUS031', 3023, NULL, 31, 1),
(7, 'BUS032', 3024, NULL, 32, 1),
(7, 'BUS033', 3025, NULL, 33, 1),
-- Duplicidad (BUS034-BUS036)
(7, 'BUS034', 3026, NULL, 34, 1),
(7, 'BUS035', 3027, NULL, 35, 1),
(7, 'BUS036', 3028, NULL, 36, 1),
-- Validación Contenido (BUS037-BUS041)
(7, 'BUS037', 3029, NULL, 37, 1),
(7, 'BUS038', 3030, NULL, 38, 1),
(7, 'BUS039', 3031, NULL, 39, 1),
(7, 'BUS040', 3032, NULL, 40, 1),
(7, 'BUS041', 3033, NULL, 41, 1),
-- CFDIs Relacionados NC (BUS042-BUS045)
(7, 'BUS042', 3034, NULL, 42, 1),
(7, 'BUS043', 3035, NULL, 43, 1),
(7, 'BUS044', 3036, NULL, 44, 1),
(7, 'BUS045', 3037, NULL, 45, 1),
-- Actualización Facturas/NC (BUS046-BUS048)
(7, 'BUS046', 3038, NULL, 46, 1),
(7, 'BUS047', 3039, NULL, 47, 1),
(7, 'BUS048', 3040, NULL, 48, 1),
-- Transición de estatus (BUS049-BUS053)
(7, 'BUS049', 3041, NULL, 49, 1),
(7, 'BUS050', 3042, NULL, 50, 1),
(7, 'BUS051', 3043, NULL, 51, 1),
(7, 'BUS052', 3044, NULL, 52, 1),
(7, 'BUS053', 3045, NULL, 53, 1),
-- Permisos (BUS054-BUS056)
(7, 'BUS054', 3046, NULL, 54, 1),
(7, 'BUS055', 3047, NULL, 55, 1),
(7, 'BUS056', 3048, NULL, 56, 1);

-- CatMsgExitoso (header_id=5) - Prefijo: RES - Mensajes éxito fiscal-api
-- Continuando desde RES004 (RES001-RES003 ya existen)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(5, 'RES004', 4000, NULL, 4, 1),
(5, 'RES005', 4001, NULL, 5, 1),
(5, 'RES006', 4002, NULL, 6, 1),
(5, 'RES007', 4003, NULL, 7, 1),
(5, 'RES008', 4004, NULL, 8, 1),
(5, 'RES009', 4005, NULL, 9, 1),
(5, 'RES010', 4006, NULL, 10, 1),
(5, 'RES011', 4007, NULL, 11, 1),
(5, 'RES012', 4008, NULL, 12, 1);

-- CatMsgAdvertencia (header_id=11) - Prefijo: WRN - Advertencias fiscal-api
-- Continuando desde WRN009 (WRN001-WRN008 ya existen)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN009', 4100, NULL, 9, 1),
(11, 'WRN010', 4101, NULL, 10, 1),
(11, 'WRN011', 4102, NULL, 11, 1),
(11, 'WRN012', 4103, NULL, 12, 1),
(11, 'WRN013', 4104, NULL, 13, 1),
(11, 'WRN014', 4105, NULL, 14, 1),
(11, 'WRN015', 4106, NULL, 15, 1),
(11, 'WRN016', 4107, NULL, 16, 1),
(11, 'WRN017', 4108, NULL, 17, 1),
(11, 'WRN018', 4109, NULL, 18, 1),
(11, 'WRN019', 4110, NULL, 19, 1),
(11, 'WRN020', 4111, NULL, 20, 1),
(11, 'WRN021', 4112, NULL, 21, 1),
(11, 'WRN022', 4113, NULL, 22, 1),
(11, 'WRN023', 4114, NULL, 23, 1),
(11, 'WRN024', 4115, NULL, 24, 1),
(11, 'WRN025', 4116, NULL, 25, 1),
(11, 'WRN026', 4117, NULL, 26, 1),
(11, 'WRN027', 4118, NULL, 27, 1),
(11, 'WRN028', 4119, NULL, 28, 1),
(11, 'WRN029', 4120, NULL, 29, 1),
(11, 'WRN030', 4121, NULL, 30, 1);

-- ============================================================================
-- FIN DEL SCRIPT DE DATOS
-- ============================================================================
