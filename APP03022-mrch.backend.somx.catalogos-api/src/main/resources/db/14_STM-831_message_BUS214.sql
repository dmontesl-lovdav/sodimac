-- ============================================================================
-- STM-831: Agregar mensaje de error BUS214
-- Fecha: 2026-02-05
-- Descripcion: Mensaje de error para tipo de proveedor invalido
-- dict_id: 7014
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- DICTIONARY_LANG: Traducciones para mensaje BUS214
-- Idiomas: 1=Espanol, 2=Ingles, 3=Portugues
-- ============================================================================

-- SUP_BUS015: Tipo de proveedor no valido (dict_id: 7014)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(7014, 1, 'No existe el tipo de proveedor [{0}] solicitado.'),
(7014, 2, 'Supplier type [{0}] does not exist.'),
(7014, 3, 'O tipo de fornecedor [{0}] nao existe.');

-- ============================================================================
-- CATALOG_DETAIL: Registro de mensaje BUS214
-- header_id=7 corresponde a CatMsgNegocio (errores de negocio)
-- ============================================================================

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status)
VALUES (7, 'BUS214', 7014, NULL, 214, 1);

-- ============================================================================
-- VERIFICACION
-- ============================================================================

SELECT 'Mensaje BUS214 creado:' AS info;

SELECT cd.key, dl.lang_id, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.key = 'BUS214'
ORDER BY dl.lang_id;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
