-- ============================================================================
-- UTILS-API: Mensajes para el modulo Herramientas y Utilerias
-- JIRA: STM-1212
-- Fecha: 2025-12-08
-- Descripcion: Agregar mensajes de error, exito e informativos para utils-api
-- Rango dict_id: 6000-6099 (reservado para utils-api)
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- DICTIONARY_LANG: Traducciones para mensajes de utils-api
-- Idiomas: 1=Espanol, 2=Ingles, 3=Portugues
-- ============================================================================

-- ----------------------------------------------------------------------------
-- ERRORES TECNICOS (ERR) - dict_id: 6000-6019
-- Para usar con header_id=6 (CatMsgErrorSistema)
-- ----------------------------------------------------------------------------

-- UTL_ERR001: Error de conexion a base de datos (dict_id: 6000)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6000, 1, 'Error de conexion con la base de datos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6000, 2, 'Database connection error');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6000, 3, 'Erro de conexao com o banco de dados');

-- UTL_ERR002: Error al procesar parametro (dict_id: 6001)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6001, 1, 'Error al procesar el parametro {0}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6001, 2, 'Error processing parameter {0}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6001, 3, 'Erro ao processar o parametro {0}');

-- UTL_ERR003: Error de validacion de datos (dict_id: 6002)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6002, 1, 'Error de validacion en los datos de entrada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6002, 2, 'Validation error in input data');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6002, 3, 'Erro de validacao nos dados de entrada');

-- UTL_ERR004: Recurso no encontrado (dict_id: 6003)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6003, 1, 'El recurso solicitado no fue encontrado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6003, 2, 'The requested resource was not found');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6003, 3, 'O recurso solicitado nao foi encontrado');

-- UTL_ERR005: Error interno del servidor (dict_id: 6004)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6004, 1, 'Error interno del servidor. Contacte al administrador');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6004, 2, 'Internal server error. Contact the administrator');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6004, 3, 'Erro interno do servidor. Contate o administrador');

-- UTL_ERR006: Timeout de operacion (dict_id: 6005)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6005, 1, 'Tiempo de espera agotado para la operacion');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6005, 2, 'Operation timeout exceeded');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6005, 3, 'Tempo limite excedido para a operacao');

-- UTL_ERR007: Error de serializacion (dict_id: 6006)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6006, 1, 'Error al serializar/deserializar datos');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6006, 2, 'Error serializing/deserializing data');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6006, 3, 'Erro ao serializar/deserializar dados');

-- UTL_ERR008: Error de configuracion (dict_id: 6007)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6007, 1, 'Error en la configuracion del sistema');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6007, 2, 'System configuration error');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6007, 3, 'Erro na configuracao do sistema');

-- ----------------------------------------------------------------------------
-- ERRORES DE NEGOCIO (BUS) - dict_id: 6020-6049
-- Para usar con header_id=7 (CatMsgNegocio)
-- ----------------------------------------------------------------------------

-- UTL_BUS001: Parametro duplicado (dict_id: 6020)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6020, 1, 'Ya existe un parametro con el nombre {0} y version {1}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6020, 2, 'A parameter with name {0} and version {1} already exists');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6020, 3, 'Ja existe um parametro com o nome {0} e versao {1}');

-- UTL_BUS002: Modulo no encontrado (dict_id: 6021)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6021, 1, 'El modulo con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6021, 2, 'Module with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6021, 3, 'O modulo com ID {0} nao existe');

-- UTL_BUS003: Parametro no encontrado (dict_id: 6022)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6022, 1, 'El parametro con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6022, 2, 'Parameter with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6022, 3, 'O parametro com ID {0} nao existe');

-- UTL_BUS004: Mensaje duplicado (dict_id: 6023)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6023, 1, 'Ya existe un mensaje con el codigo {0}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6023, 2, 'A message with code {0} already exists');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6023, 3, 'Ja existe uma mensagem com o codigo {0}');

-- UTL_BUS005: Mensaje no encontrado (dict_id: 6024)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6024, 1, 'El mensaje con codigo {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6024, 2, 'Message with code {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6024, 3, 'A mensagem com codigo {0} nao existe');

-- UTL_BUS006: Proceso no encontrado (dict_id: 6025)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6025, 1, 'El proceso con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6025, 2, 'Process with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6025, 3, 'O processo com ID {0} nao existe');

-- UTL_BUS007: Tipo de elemento no encontrado (dict_id: 6026)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6026, 1, 'El tipo de elemento con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6026, 2, 'Item type with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6026, 3, 'O tipo de elemento com ID {0} nao existe');

-- UTL_BUS008: Elemento no encontrado (dict_id: 6027)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6027, 1, 'El elemento con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6027, 2, 'Item with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6027, 3, 'O elemento com ID {0} nao existe');

-- UTL_BUS009: Parametro inactivo (dict_id: 6028)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6028, 1, 'El parametro {0} se encuentra inactivo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6028, 2, 'Parameter {0} is inactive');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6028, 3, 'O parametro {0} esta inativo');

-- UTL_BUS010: Valor de parametro invalido (dict_id: 6029)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6029, 1, 'El valor del parametro {0} no es valido: {1}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6029, 2, 'Parameter {0} value is not valid: {1}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6029, 3, 'O valor do parametro {0} nao e valido: {1}');

-- UTL_BUS011: Relacion mensaje-aplicativo duplicada (dict_id: 6030)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6030, 1, 'La relacion mensaje {0} - aplicativo {1} ya existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6030, 2, 'Message {0} - application {1} relationship already exists');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6030, 3, 'A relacao mensagem {0} - aplicativo {1} ja existe');

-- UTL_BUS012: Modulo en uso (dict_id: 6031)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6031, 1, 'El modulo {0} no puede eliminarse porque tiene parametros asociados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6031, 2, 'Module {0} cannot be deleted because it has associated parameters');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6031, 3, 'O modulo {0} nao pode ser excluido porque possui parametros associados');

-- ----------------------------------------------------------------------------
-- MENSAJES DE EXITO (RES) - dict_id: 6050-6069
-- Para usar con header_id=5 (CatMsgExitoso)
-- ----------------------------------------------------------------------------

-- UTL_RES001: Parametro creado (dict_id: 6050)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6050, 1, 'Parametro {0} creado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6050, 2, 'Parameter {0} created successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6050, 3, 'Parametro {0} criado com sucesso');

-- UTL_RES002: Parametro actualizado (dict_id: 6051)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6051, 1, 'Parametro {0} actualizado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6051, 2, 'Parameter {0} updated successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6051, 3, 'Parametro {0} atualizado com sucesso');

-- UTL_RES003: Parametro eliminado (dict_id: 6052)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6052, 1, 'Parametro eliminado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6052, 2, 'Parameter deleted successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6052, 3, 'Parametro excluido com sucesso');

-- UTL_RES004: Modulo creado (dict_id: 6053)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6053, 1, 'Modulo {0} creado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6053, 2, 'Module {0} created successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6053, 3, 'Modulo {0} criado com sucesso');

-- UTL_RES005: Configuracion guardada (dict_id: 6054)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6054, 1, 'Configuracion guardada exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6054, 2, 'Configuration saved successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6054, 3, 'Configuracao salva com sucesso');

-- ----------------------------------------------------------------------------
-- MENSAJES INFORMATIVOS (INF) - dict_id: 6070-6089
-- Para usar con header_id=10 (CatMsgInformativo)
-- ----------------------------------------------------------------------------

-- UTL_INF001: Sin resultados (dict_id: 6070)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6070, 1, 'No se encontraron resultados para la busqueda');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6070, 2, 'No results found for the search');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6070, 3, 'Nenhum resultado encontrado para a busca');

-- UTL_INF002: Operacion en proceso (dict_id: 6071)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6071, 1, 'La operacion se esta procesando');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6071, 2, 'The operation is being processed');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6071, 3, 'A operacao esta sendo processada');

-- UTL_INF003: Cache actualizado (dict_id: 6072)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6072, 1, 'Cache de parametros actualizado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6072, 2, 'Parameters cache updated');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6072, 3, 'Cache de parametros atualizado');

-- ----------------------------------------------------------------------------
-- MENSAJES DE ADVERTENCIA (WRN) - dict_id: 6090-6099
-- Para usar con header_id=11 (CatMsgAdvertencia)
-- ----------------------------------------------------------------------------

-- UTL_WRN001: Parametro proximo a expirar (dict_id: 6090)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6090, 1, 'El parametro {0} expira en {1} dias');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6090, 2, 'Parameter {0} expires in {1} days');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6090, 3, 'O parametro {0} expira em {1} dias');

-- UTL_WRN002: Version obsoleta (dict_id: 6091)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6091, 1, 'La version {0} del parametro {1} esta obsoleta');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6091, 2, 'Version {0} of parameter {1} is obsolete');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6091, 3, 'A versao {0} do parametro {1} esta obsoleta');

-- UTL_WRN003: Limite de registros (dict_id: 6092)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6092, 1, 'Se excedio el limite de {0} registros. Mostrando primeros resultados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6092, 2, 'Exceeded limit of {0} records. Showing first results');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (6092, 3, 'Excedido o limite de {0} registros. Mostrando primeiros resultados');

-- ============================================================================
-- CATALOG_DETAIL: Registros de mensajes para utils-api
-- ============================================================================

-- Errores tecnicos (header_id=6, CatMsgErrorSistema, prefijo ERR)
-- Nota: ERR001-ERR036 ya existen para fiscal-api, usamos ERR100+ para utils
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(6, 'ERR100', 6000, NULL, 100, 1),  -- Error conexion BD
(6, 'ERR101', 6001, NULL, 101, 1),  -- Error procesar parametro
(6, 'ERR102', 6002, NULL, 102, 1),  -- Error validacion datos
(6, 'ERR103', 6003, NULL, 103, 1),  -- Recurso no encontrado
(6, 'ERR104', 6004, NULL, 104, 1),  -- Error interno servidor
(6, 'ERR105', 6005, NULL, 105, 1),  -- Timeout operacion
(6, 'ERR106', 6006, NULL, 106, 1),  -- Error serializacion
(6, 'ERR107', 6007, NULL, 107, 1);  -- Error configuracion

-- Errores de negocio (header_id=7, CatMsgNegocio, prefijo BUS)
-- Nota: BUS001-BUS050 pueden estar en uso, usamos BUS100+ para utils
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(7, 'BUS100', 6020, NULL, 100, 1),  -- Parametro duplicado
(7, 'BUS101', 6021, NULL, 101, 1),  -- Modulo no encontrado
(7, 'BUS102', 6022, NULL, 102, 1),  -- Parametro no encontrado
(7, 'BUS103', 6023, NULL, 103, 1),  -- Mensaje duplicado
(7, 'BUS104', 6024, NULL, 104, 1),  -- Mensaje no encontrado
(7, 'BUS105', 6025, NULL, 105, 1),  -- Proceso no encontrado
(7, 'BUS106', 6026, NULL, 106, 1),  -- Tipo elemento no encontrado
(7, 'BUS107', 6027, NULL, 107, 1),  -- Elemento no encontrado
(7, 'BUS108', 6028, NULL, 108, 1),  -- Parametro inactivo
(7, 'BUS109', 6029, NULL, 109, 1),  -- Valor parametro invalido
(7, 'BUS110', 6030, NULL, 110, 1),  -- Relacion duplicada
(7, 'BUS111', 6031, NULL, 111, 1);  -- Modulo en uso

-- Mensajes de exito (header_id=5, CatMsgExitoso, prefijo RES)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(5, 'RES100', 6050, NULL, 100, 1),  -- Parametro creado
(5, 'RES101', 6051, NULL, 101, 1),  -- Parametro actualizado
(5, 'RES102', 6052, NULL, 102, 1),  -- Parametro eliminado
(5, 'RES103', 6053, NULL, 103, 1),  -- Modulo creado
(5, 'RES104', 6054, NULL, 104, 1);  -- Configuracion guardada

-- Mensajes informativos (header_id=10, CatMsgInformativo, prefijo INF)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(10, 'INF100', 6070, NULL, 100, 1),  -- Sin resultados
(10, 'INF101', 6071, NULL, 101, 1),  -- Operacion en proceso
(10, 'INF102', 6072, NULL, 102, 1);  -- Cache actualizado

-- Mensajes de advertencia (header_id=11, CatMsgAdvertencia, prefijo WRN)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN100', 6090, NULL, 100, 1),  -- Parametro proximo a expirar
(11, 'WRN101', 6091, NULL, 101, 1),  -- Version obsoleta
(11, 'WRN102', 6092, NULL, 102, 1);  -- Limite de registros

-- ============================================================================
-- VERIFICACION
-- ============================================================================
SELECT 'dictionary_lang (utils)' AS tabla, COUNT(*) AS registros
FROM shared_catalogs.dictionary_lang WHERE dict_id BETWEEN 6000 AND 6099
UNION ALL
SELECT 'catalog_detail (ERR100+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'ERR1%'
UNION ALL
SELECT 'catalog_detail (BUS100+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'BUS1%'
UNION ALL
SELECT 'catalog_detail (RES100+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'RES1%'
UNION ALL
SELECT 'catalog_detail (INF100+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'INF1%'
UNION ALL
SELECT 'catalog_detail (WRN100+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'WRN1%';

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
