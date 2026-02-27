-- ============================================================================
-- FISCAL-API: Nuevos mensajes ERR032-ERR036
-- Fecha: 2025-12-04
-- Descripcion: Agregar codigos de error faltantes identificados en analisis
-- ============================================================================

-- ============================================================================
-- DICTIONARY_LANG: Traducciones para nuevos errores (dict_id: 2090-2094)
-- ============================================================================

-- ERR032: PAC no disponible (dict_id: 2090)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2090, 1, 'PAC no disponible para el nivel de prioridad solicitado');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2090, 2, 'PAC not available for the requested priority level');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2090, 3, 'PAC não disponível para o nível de prioridade solicitado');

-- ERR033: Error generando PDF (dict_id: 2091)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2091, 1, 'Error generando PDF desde XML');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2091, 2, 'Error generating PDF from XML');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2091, 3, 'Erro ao gerar PDF a partir do XML');

-- ERR034: Error generando PDF Complemento Pago (dict_id: 2092)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2092, 1, 'Error generando PDF del Complemento de Pago');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2092, 2, 'Error generating Payment Complement PDF');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2092, 3, 'Erro ao gerar PDF do Complemento de Pagamento');

-- ERR035: Error validacion campos request (dict_id: 2093)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2093, 1, 'Error de validación en los campos del request');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2093, 2, 'Validation error in request fields');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2093, 3, 'Erro de validação nos campos do request');

-- ERR036: Error inesperado del sistema (dict_id: 2094)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2094, 1, 'Error inesperado del sistema. Contacte al administrador');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2094, 2, 'Unexpected system error. Contact the administrator');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (2094, 3, 'Erro inesperado do sistema. Contate o administrador');

-- ============================================================================
-- CATALOG_DETAIL: Nuevos registros para CatMsgErrorSistema (header_id=6)
-- ============================================================================

-- Nuevos errores sistema (ERR032-ERR036)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(6, 'ERR032', 2090, NULL, 32, 1),
(6, 'ERR033', 2091, NULL, 33, 1),
(6, 'ERR034', 2092, NULL, 34, 1),
(6, 'ERR035', 2093, NULL, 35, 1),
(6, 'ERR036', 2094, NULL, 36, 1);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
