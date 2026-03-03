-- =============================================================
-- Seed: BUS215 y BUS216 en CatMsgNegocio
-- Jira: STM-605
-- Portable: verifica existencia antes de insertar
-- Autor: dmontes
-- Fecha: 2026-03-03
--
-- BUS215 - "El proveedor con número {0} no se encuentra registrado."
-- BUS216 - "Ya existe un bloqueo activo para el proveedor {0} que
--           se solapa con las fechas especificadas."
-- =============================================================

DO $$
DECLARE
    v_base_dict_id INTEGER;
    v_existing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_existing_count
    FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgNegocio')
      AND cd.key IN ('BUS215', 'BUS216');

    IF v_existing_count > 0 THEN
        RAISE NOTICE 'BUS215/BUS216 ya existen en CatMsgNegocio. Saltando.';
    ELSE
        -- BUS215: Proveedor no registrado
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (7015, 1, 'El proveedor con número {0} no se encuentra registrado.'),
            (7015, 2, 'Supplier with number {0} is not registered.'),
            (7015, 3, 'O fornecedor com número {0} não está registrado.');

        INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, created_at)
        VALUES (
            (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgNegocio'),
            'BUS215', 7015, 215, 1, NOW()
        );

        -- BUS216: Bloqueo solapado
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (7016, 1, 'Ya existe un bloqueo activo para el proveedor {0} que se solapa con las fechas especificadas.'),
            (7016, 2, 'An active block for supplier {0} already overlaps with the specified date range.'),
            (7016, 3, 'Já existe um bloqueio ativo para o fornecedor {0} que se sobrepõe às datas especificadas.');

        INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status, created_at)
        VALUES (
            (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgNegocio'),
            'BUS216', 7016, 216, 1, NOW()
        );

        RAISE NOTICE 'BUS215 y BUS216 creados exitosamente.';
    END IF;
END $$;

-- Verificacion
SELECT cd.key, cd.sort_order, dl.lang_id, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.key IN ('BUS215', 'BUS216')
ORDER BY cd.key, dl.lang_id;
