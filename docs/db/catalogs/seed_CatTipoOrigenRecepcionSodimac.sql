-- =============================================================
-- Seed: CatTipoOrigenRecepcionSodimac
-- Solicitado por: Ivan y Josue
-- Portable: usa subqueries, no IDs hardcodeados
-- =============================================================

-- =============================================================
-- 1. Insertar header (si no existe)
-- =============================================================
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, status, created_at, catalog_type)
SELECT 'CatTipoOrigenRecepcionSodimac', 'TOS', 'Tipo Origen Recepcion Sodimac',
       'Catalogo de tipos de origen de recepcion para Sodimac', 'general', 1, NOW(), 'SIMPLE'
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_header WHERE code = 'CatTipoOrigenRecepcionSodimac'
);

-- =============================================================
-- 2. Insertar traducciones en dictionary_lang (si no existen)
-- =============================================================
DO $$
DECLARE
    v_base_dict_id INTEGER;
    v_existing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_existing_count
    FROM shared_catalogs.dictionary_lang
    WHERE description = 'Proveedor SLI' AND lang_id = 1;

    IF v_existing_count > 0 THEN
        RAISE NOTICE 'Las traducciones TOS ya existen. Saltando.';
    ELSE
        SELECT COALESCE(MAX(dict_id), 0) + 1 INTO v_base_dict_id
        FROM shared_catalogs.dictionary_lang;

        RAISE NOTICE 'Insertando traducciones TOS desde dict_id = %', v_base_dict_id;

        -- TOS001: Proveedor SLI
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 0, 1, 'Proveedor SLI'),
            (v_base_dict_id + 0, 2, 'SLI Supplier'),
            (v_base_dict_id + 0, 3, 'Fornecedor SLI');
        -- TOS002: Proveedor TRA
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 1, 1, 'Proveedor TRA'),
            (v_base_dict_id + 1, 2, 'TRA Supplier'),
            (v_base_dict_id + 1, 3, 'Fornecedor TRA');
        -- TOS003: Proveedor IND
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 2, 1, 'Proveedor IND'),
            (v_base_dict_id + 2, 2, 'IND Supplier'),
            (v_base_dict_id + 2, 3, 'Fornecedor IND');
        -- TOS004: Proveedor SOT
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 3, 1, 'Proveedor SOT'),
            (v_base_dict_id + 3, 2, 'SOT Supplier'),
            (v_base_dict_id + 3, 3, 'Fornecedor SOT');
        -- TOS005: Proveedor ODMBS
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 4, 1, 'Proveedor ODMBS'),
            (v_base_dict_id + 4, 2, 'ODMBS Supplier'),
            (v_base_dict_id + 4, 3, 'Fornecedor ODMBS');
    END IF;
END $$;

-- =============================================================
-- 3. Insertar catalog_detail (si no existen)
-- =============================================================
INSERT INTO shared_catalogs.catalog_detail
    (header_id, key, dict_id, color, sort_order, status, created_at, internal_status, external_key, value)
SELECT
    (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatTipoOrigenRecepcionSodimac'),
    v.key,
    (SELECT dl.dict_id FROM shared_catalogs.dictionary_lang dl WHERE dl.description = v.desc_es AND dl.lang_id = 1 LIMIT 1),
    NULL,
    v.sort_order,
    1,
    NOW(),
    v.internal_status,
    v.external_key,
    v.valor
FROM (VALUES
    ('TOS001', 'Proveedor SLI',   1, 'SLI',    'Mercancia',  1),
    ('TOS002', 'Proveedor TRA',   2, 'TRA',    'Transporte', 2),
    ('TOS003', 'Proveedor IND',   3, 'IND',    'Indirectos', 3),
    ('TOS004', 'Proveedor SOT',   4, 'SOT',    'Servicios',  4),
    ('TOS005', 'Proveedor ODMBS', 1, 'Blanco', 'Mercancia',  5)
) AS v(key, desc_es, internal_status, external_key, valor, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatTipoOrigenRecepcionSodimac')
    AND cd.key = v.key
);

-- =============================================================
-- 4. Verificar resultado
-- =============================================================
SELECT cd.key, cd.internal_status, cd.external_key, cd.value, dl.description, cd.sort_order, cd.status
FROM shared_catalogs.catalog_detail cd
LEFT JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatTipoOrigenRecepcionSodimac')
ORDER BY cd.sort_order;
