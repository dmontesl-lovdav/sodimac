-- ============================================================================
-- STM-395 / STM-397: Validaciones de Serie, Folio y UUID
-- Descripcion: Mensajes de advertencia para validaciones de duplicidad
--              en facturas (STM-395) y notas de credito (STM-397)
-- ============================================================================

-- ============================================================================
-- DICTIONARY_LANG: Traducciones (dict_id: 8016-8021)
-- Idiomas: 1=Español, 2=Inglés, 3=Portugués
-- ============================================================================

-- WRN7012: Validacion Serie y Folio - Facturas (dict_id: 8016)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8016, 1, 'La factura requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8016, 2, 'The invoice requires a series and folio to publish the document. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8016, 3, 'A fatura requer uma série e folio para publicar o documento. Por favor, valide as informações antes de continuar.');

-- WRN7013: Duplicado Serie + Folio - Facturas (dict_id: 8017)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8017, 1, 'La factura se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8017, 2, 'The invoice is already registered with the same series and folio. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8017, 3, 'A fatura já está registrada com a mesma série e folio. Por favor, valide as informações antes de continuar.');

-- WRN7014: Duplicado UUID - Facturas (dict_id: 8018)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8018, 1, 'La factura se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8018, 2, 'The invoice is already registered with the same UUID. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8018, 3, 'A fatura já está registrada com o mesmo UUID. Por favor, valide as informações antes de continuar.');

-- WRN7015: Validacion Serie y Folio - Notas de Credito (dict_id: 8019)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8019, 1, 'La nota de crédito requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8019, 2, 'The credit note requires a series and folio to publish the document. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8019, 3, 'A nota de crédito requer uma série e folio para publicar o documento. Por favor, valide as informações antes de continuar.');

-- WRN7016: Duplicado Serie + Folio - Notas de Credito (dict_id: 8020)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8020, 1, 'La nota de crédito se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8020, 2, 'The credit note is already registered with the same series and folio. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8020, 3, 'A nota de crédito já está registrada com a mesma série e folio. Por favor, valide as informações antes de continuar.');

-- WRN7017: Duplicado UUID - Notas de Credito (dict_id: 8021)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8021, 1, 'La nota de crédito se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8021, 2, 'The credit note is already registered with the same UUID. Please validate the information before continuing.');
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES (8021, 3, 'A nota de crédito já está registrada com o mesmo UUID. Por favor, valide as informações antes de continuar.');

-- ============================================================================
-- CATALOG_DETAIL: Registros para CatMsgAdvertencia (header_id=11)
-- ============================================================================

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN7012', 8016, NULL, 7012, 1),
(11, 'WRN7013', 8017, NULL, 7013, 1),
(11, 'WRN7014', 8018, NULL, 7014, 1),
(11, 'WRN7015', 8019, NULL, 7015, 1),
(11, 'WRN7016', 8020, NULL, 7016, 1),
(11, 'WRN7017', 8021, NULL, 7017, 1);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
