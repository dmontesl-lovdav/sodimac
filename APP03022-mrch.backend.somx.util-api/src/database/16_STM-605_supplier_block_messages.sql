-- ============================================================================
-- STM-605: Mensajes para bloqueo de proveedores (BUS215, BUS216)
-- Fecha: 2026-03-03
-- Descripcion: Mensajes de error de negocio para validaciones de supplier_block
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- BUS215: Proveedor no registrado (dict_id: 7015)
-- ============================================================================

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(7015, 1, 'El proveedor con número {0} no se encuentra registrado.'),
(7015, 2, 'Supplier with number {0} is not registered.'),
(7015, 3, 'O fornecedor com número {0} não está registrado.');

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status)
VALUES (7, 'BUS215', 7015, NULL, 215, 1);

-- ============================================================================
-- BUS216: Bloqueo solapado (dict_id: 7016)
-- ============================================================================

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(7016, 1, 'Ya existe un bloqueo activo para el proveedor {0} que se solapa con las fechas especificadas.'),
(7016, 2, 'An active block for supplier {0} already overlaps with the specified date range.'),
(7016, 3, 'Já existe um bloqueio ativo para o fornecedor {0} que se sobrepõe às datas especificadas.');

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status)
VALUES (7, 'BUS216', 7016, NULL, 216, 1);

-- ============================================================================
-- VERIFICACION
-- ============================================================================

SELECT cd.key, dl.lang_id, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.key IN ('BUS215', 'BUS216')
ORDER BY cd.key, dl.lang_id;
