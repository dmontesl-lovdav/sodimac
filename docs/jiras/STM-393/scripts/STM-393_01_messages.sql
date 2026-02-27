-- ============================================================================
-- STM-393: Mensajes para Pantalla Consulta de Facturas
-- Servicio: catalogos-api
-- Fecha: 2025-01-06
-- Descripcion: Mensajes informativos y advertencias para validaciones de busqueda
-- ============================================================================

-- ============================================================================
-- VERIFICACION PREVIA
-- ============================================================================

-- Verificar el ultimo dict_id usado
SELECT MAX(dict_id) as ultimo_dict_id FROM shared_catalogs.dictionary_lang;

-- Verificar que no existan los mensajes
SELECT cd.key, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.key IN ('INF6000', 'WRN7000', 'WRN7005', 'WRN7006');

-- ============================================================================
-- DICTIONARY_LANG: Traducciones (dict_id: 8000-8003)
-- ============================================================================

-- INF6000: Sin resultados (dict_id: 8000)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8000, 1, 'No existe informacion con los criterios establecidos'),
(8000, 2, 'No information found with the established criteria'),
(8000, 3, 'Nao existem informacoes com os criterios estabelecidos');

-- WRN7000: Fecha inicio > fecha final (dict_id: 8001)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8001, 1, 'La fecha inicio no puede ser superior a la fecha final'),
(8001, 2, 'Start date cannot be greater than end date'),
(8001, 3, 'A data de inicio nao pode ser posterior a data final');

-- WRN7005: Rango > 6 meses (dict_id: 8002)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8002, 1, 'El periodo de busqueda no puede ser superior a 6 meses, favor de validar'),
(8002, 2, 'Search period cannot exceed 6 months, please validate'),
(8002, 3, 'O periodo de busca nao pode exceder 6 meses, por favor valide');

-- WRN7006: Fecha final < fecha inicio (dict_id: 8003)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8003, 1, 'La fecha final no puede ser menor a la fecha inicio'),
(8003, 2, 'End date cannot be less than start date'),
(8003, 3, 'A data final nao pode ser anterior a data de inicio');

-- ============================================================================
-- CATALOG_DETAIL: Registros en catalogos de mensajes
-- ============================================================================

-- Mensaje Informativo (header_id=10: CatMsgInformativo)
INSERT INTO shared_catalogs.catalog_detail
(header_id, key, dict_id, color, sort_order, status, internal_status)
VALUES
(10, 'INF6000', 8000, '#17a2b8', 6000, 1, 6000);

-- Mensajes de Advertencia (header_id=11: CatMsgAdvertencia)
INSERT INTO shared_catalogs.catalog_detail
(header_id, key, dict_id, color, sort_order, status, internal_status)
VALUES
(11, 'WRN7000', 8001, '#ffc107', 7000, 1, 7000),
(11, 'WRN7005', 8002, '#ffc107', 7005, 1, 7005),
(11, 'WRN7006', 8003, '#ffc107', 7006, 1, 7006);

-- ============================================================================
-- VERIFICACION POST-INSERT
-- ============================================================================

-- Verificar traducciones insertadas
SELECT
    dl.dict_id,
    dl.lang_id,
    CASE dl.lang_id WHEN 1 THEN 'ES' WHEN 2 THEN 'EN' WHEN 3 THEN 'PT' END as idioma,
    dl.description
FROM shared_catalogs.dictionary_lang dl
WHERE dl.dict_id BETWEEN 8000 AND 8003
ORDER BY dl.dict_id, dl.lang_id;

-- Verificar detalles de catalogo insertados
SELECT
    ch.code as catalogo,
    ch.name as catalogo_nombre,
    cd.key as mensaje_key,
    dl.description as mensaje_es,
    cd.color,
    cd.status
FROM shared_catalogs.catalog_detail cd
INNER JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id
INNER JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.key IN ('INF6000', 'WRN7000', 'WRN7005', 'WRN7006')
ORDER BY cd.key;

-- Probar endpoint de mensajes (ejecutar desde aplicacion)
-- GET http://localhost:8083/message/INF6000?lang=1
-- GET http://localhost:8083/message/WRN7000?lang=1
-- GET http://localhost:8083/message/WRN7005?lang=1
-- GET http://localhost:8083/message/WRN7006?lang=1

-- ============================================================================
-- ROLLBACK (en caso de ser necesario)
-- ============================================================================

-- DELETE FROM shared_catalogs.catalog_detail WHERE key IN ('INF6000', 'WRN7000', 'WRN7005', 'WRN7006');
-- DELETE FROM shared_catalogs.dictionary_lang WHERE dict_id BETWEEN 8000 AND 8003;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
