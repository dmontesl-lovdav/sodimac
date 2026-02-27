-- ============================================================================
-- SUPPLIERS-API: Mensajes para el modulo de Proveedores
-- JIRA: STM-1225
-- Fecha: 2025-12-11
-- Descripcion: Agregar mensajes de error, exito e informativos para suppliers-api
-- Rango dict_id: 7000-7099 (reservado para suppliers-api)
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- DICTIONARY_LANG: Traducciones para mensajes de suppliers-api
-- Idiomas: 1=Espanol, 2=Ingles, 3=Portugues
-- ============================================================================

-- ----------------------------------------------------------------------------
-- ERRORES DE NEGOCIO (BUS) - dict_id: 7000-7029
-- Para usar con header_id=7 (CatMsgNegocio)
-- ----------------------------------------------------------------------------

-- SUP_BUS001: Proveedor duplicado por numero (dict_id: 7000)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7000, 1, 'Ya existe un proveedor con el numero: {0}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7000, 2, 'A supplier with number {0} already exists');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7000, 3, 'Ja existe um fornecedor com o numero: {0}');

-- SUP_BUS002: Proveedor no encontrado por ID (dict_id: 7001)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7001, 1, 'El proveedor con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7001, 2, 'Supplier with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7001, 3, 'O fornecedor com ID {0} nao existe');

-- SUP_BUS003: Proveedor no encontrado por numero (dict_id: 7002)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7002, 1, 'El proveedor con numero {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7002, 2, 'Supplier with number {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7002, 3, 'O fornecedor com numero {0} nao existe');

-- SUP_BUS004: Proveedor no encontrado por RFC (dict_id: 7003)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7003, 1, 'El proveedor con RFC {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7003, 2, 'Supplier with RFC {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7003, 3, 'O fornecedor com RFC {0} nao existe');

-- SUP_BUS005: Tipo de proveedor no encontrado (dict_id: 7004)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7004, 1, 'El tipo de proveedor con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7004, 2, 'Supplier type with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7004, 3, 'O tipo de fornecedor com ID {0} nao existe');

-- SUP_BUS006: Condicion de pago no encontrada (dict_id: 7005)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7005, 1, 'La condicion de pago con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7005, 2, 'Payment condition with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7005, 3, 'A condicao de pagamento com ID {0} nao existe');

-- SUP_BUS007: Centro de costo no encontrado (dict_id: 7006)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7006, 1, 'El centro de costo con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7006, 2, 'Cost center with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7006, 3, 'O centro de custo com ID {0} nao existe');

-- SUP_BUS008: Centro de beneficio no encontrado (dict_id: 7007)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7007, 1, 'El centro de beneficio con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7007, 2, 'Benefit center with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7007, 3, 'O centro de beneficio com ID {0} nao existe');

-- SUP_BUS009: Cuenta contable no encontrada (dict_id: 7008)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7008, 1, 'La cuenta contable con ID {0} no existe');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7008, 2, 'Accounting account with ID {0} does not exist');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7008, 3, 'A conta contabil com ID {0} nao existe');

-- SUP_BUS010: RFC duplicado (dict_id: 7009)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7009, 1, 'Ya existe un proveedor con el RFC: {0}');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7009, 2, 'A supplier with RFC {0} already exists');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7009, 3, 'Ja existe um fornecedor com o RFC: {0}');

-- SUP_BUS011: Proveedor inactivo (dict_id: 7010)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7010, 1, 'El proveedor {0} se encuentra inactivo');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7010, 2, 'Supplier {0} is inactive');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7010, 3, 'O fornecedor {0} esta inativo');

-- SUP_BUS012: Proveedor con roles asociados (dict_id: 7011)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7011, 1, 'El proveedor {0} no puede eliminarse porque tiene roles asociados');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7011, 2, 'Supplier {0} cannot be deleted because it has associated roles');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7011, 3, 'O fornecedor {0} nao pode ser excluido porque possui funcoes associadas');

-- SUP_BUS013: Numero de proveedor requerido (dict_id: 7012)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7012, 1, 'El numero de proveedor es requerido');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7012, 2, 'Supplier number is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7012, 3, 'O numero do fornecedor e obrigatorio');

-- SUP_BUS014: Razon social requerida (dict_id: 7013)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7013, 1, 'La razon social del proveedor es requerida');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7013, 2, 'Supplier business name is required');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7013, 3, 'A razao social do fornecedor e obrigatoria');

-- ----------------------------------------------------------------------------
-- MENSAJES DE EXITO (RES) - dict_id: 7030-7049
-- Para usar con header_id=5 (CatMsgExitoso)
-- ----------------------------------------------------------------------------

-- SUP_RES001: Proveedor creado (dict_id: 7030)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7030, 1, 'Proveedor {0} creado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7030, 2, 'Supplier {0} created successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7030, 3, 'Fornecedor {0} criado com sucesso');

-- SUP_RES002: Proveedor actualizado (dict_id: 7031)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7031, 1, 'Proveedor {0} actualizado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7031, 2, 'Supplier {0} updated successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7031, 3, 'Fornecedor {0} atualizado com sucesso');

-- SUP_RES003: Proveedor eliminado (dict_id: 7032)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7032, 1, 'Proveedor {0} eliminado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7032, 2, 'Supplier {0} deleted successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7032, 3, 'Fornecedor {0} excluido com sucesso');

-- SUP_RES004: Proveedor activado (dict_id: 7033)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7033, 1, 'Proveedor {0} activado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7033, 2, 'Supplier {0} activated successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7033, 3, 'Fornecedor {0} ativado com sucesso');

-- SUP_RES005: Proveedor desactivado (dict_id: 7034)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7034, 1, 'Proveedor {0} desactivado exitosamente');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7034, 2, 'Supplier {0} deactivated successfully');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7034, 3, 'Fornecedor {0} desativado com sucesso');

-- ----------------------------------------------------------------------------
-- MENSAJES INFORMATIVOS (INF) - dict_id: 7050-7069
-- Para usar con header_id=10 (CatMsgInformativo)
-- ----------------------------------------------------------------------------

-- SUP_INF001: Sin proveedores (dict_id: 7050)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7050, 1, 'No se encontraron proveedores');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7050, 2, 'No suppliers found');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7050, 3, 'Nenhum fornecedor encontrado');

-- SUP_INF002: Proveedores listados (dict_id: 7051)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7051, 1, 'Se encontraron {0} proveedores');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7051, 2, '{0} suppliers found');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7051, 3, '{0} fornecedores encontrados');

-- ----------------------------------------------------------------------------
-- MENSAJES DE ADVERTENCIA (WRN) - dict_id: 7070-7089
-- Para usar con header_id=11 (CatMsgAdvertencia)
-- ----------------------------------------------------------------------------

-- SUP_WRN001: Proveedor sin tipo asignado (dict_id: 7070)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7070, 1, 'El proveedor {0} no tiene tipo de proveedor asignado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7070, 2, 'Supplier {0} does not have an assigned supplier type');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7070, 3, 'O fornecedor {0} nao possui tipo de fornecedor atribuido');

-- SUP_WRN002: Proveedor sin condicion de pago (dict_id: 7071)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7071, 1, 'El proveedor {0} no tiene condicion de pago asignada');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7071, 2, 'Supplier {0} does not have an assigned payment condition');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (7071, 3, 'O fornecedor {0} nao possui condicao de pagamento atribuida');

-- ============================================================================
-- CATALOG_DETAIL: Registros de mensajes para suppliers-api
-- ============================================================================

-- Errores de negocio (header_id=7, CatMsgNegocio, prefijo BUS)
-- Usamos BUS200+ para suppliers-api
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(7, 'BUS200', 7000, NULL, 200, 1),  -- Proveedor duplicado por numero
(7, 'BUS201', 7001, NULL, 201, 1),  -- Proveedor no encontrado por ID
(7, 'BUS202', 7002, NULL, 202, 1),  -- Proveedor no encontrado por numero
(7, 'BUS203', 7003, NULL, 203, 1),  -- Proveedor no encontrado por RFC
(7, 'BUS204', 7004, NULL, 204, 1),  -- Tipo de proveedor no encontrado
(7, 'BUS205', 7005, NULL, 205, 1),  -- Condicion de pago no encontrada
(7, 'BUS206', 7006, NULL, 206, 1),  -- Centro de costo no encontrado
(7, 'BUS207', 7007, NULL, 207, 1),  -- Centro de beneficio no encontrado
(7, 'BUS208', 7008, NULL, 208, 1),  -- Cuenta contable no encontrada
(7, 'BUS209', 7009, NULL, 209, 1),  -- RFC duplicado
(7, 'BUS210', 7010, NULL, 210, 1),  -- Proveedor inactivo
(7, 'BUS211', 7011, NULL, 211, 1),  -- Proveedor con roles asociados
(7, 'BUS212', 7012, NULL, 212, 1),  -- Numero de proveedor requerido
(7, 'BUS213', 7013, NULL, 213, 1);  -- Razon social requerida

-- Mensajes de exito (header_id=5, CatMsgExitoso, prefijo RES)
-- Usamos RES200+ para suppliers-api
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(5, 'RES200', 7030, NULL, 200, 1),  -- Proveedor creado
(5, 'RES201', 7031, NULL, 201, 1),  -- Proveedor actualizado
(5, 'RES202', 7032, NULL, 202, 1),  -- Proveedor eliminado
(5, 'RES203', 7033, NULL, 203, 1),  -- Proveedor activado
(5, 'RES204', 7034, NULL, 204, 1);  -- Proveedor desactivado

-- Mensajes informativos (header_id=10, CatMsgInformativo, prefijo INF)
-- Usamos INF200+ para suppliers-api
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(10, 'INF200', 7050, NULL, 200, 1),  -- Sin proveedores
(10, 'INF201', 7051, NULL, 201, 1);  -- Proveedores listados

-- Mensajes de advertencia (header_id=11, CatMsgAdvertencia, prefijo WRN)
-- Usamos WRN200+ para suppliers-api
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN200', 7070, NULL, 200, 1),  -- Proveedor sin tipo asignado
(11, 'WRN201', 7071, NULL, 201, 1);  -- Proveedor sin condicion de pago

-- ============================================================================
-- VERIFICACION
-- ============================================================================
SELECT 'dictionary_lang (suppliers)' AS tabla, COUNT(*) AS registros
FROM shared_catalogs.dictionary_lang WHERE dict_id BETWEEN 7000 AND 7099
UNION ALL
SELECT 'catalog_detail (BUS200+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'BUS2%'
UNION ALL
SELECT 'catalog_detail (RES200+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'RES2%'
UNION ALL
SELECT 'catalog_detail (INF200+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'INF2%'
UNION ALL
SELECT 'catalog_detail (WRN200+)', COUNT(*)
FROM shared_catalogs.catalog_detail WHERE key LIKE 'WRN2%';

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
