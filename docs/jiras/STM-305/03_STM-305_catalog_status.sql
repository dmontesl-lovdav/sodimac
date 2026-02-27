-- ============================================================================
-- STM-305: Catalogo de Estatus para Estado de Cuenta
-- Esquema: shared_catalogs
-- Fecha: 2026-02-06
--
-- Crea el catalogo CatEstatusEstadoCuenta con los 5 estatus:
-- 1 = Generado
-- 2 = Publicado
-- 3 = Revisado
-- 4 = Rechazado
-- 5 = Reprocesado
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- 1. CREAR ENTRADA EN DICCIONARIO (traducciones)
-- ============================================================================

-- Obtener el siguiente dict_id disponible
DO $$
DECLARE
    v_dict_id_base INTEGER;
    v_header_id INTEGER;
BEGIN
    -- Obtener el proximo dict_id disponible
    SELECT COALESCE(MAX(dict_id), 0) + 1 INTO v_dict_id_base FROM shared_catalogs.dictionary_lang;

    -- ========================================================================
    -- PASO 1: Insertar traducciones para cada estatus
    -- ========================================================================

    -- EEC001: Generado
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 0, 1, 'Generado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 0, 2, 'Generated', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 0, 3, 'Gerado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;

    -- EEC002: Publicado
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 1, 1, 'Publicado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 1, 2, 'Published', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 1, 3, 'Publicado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;

    -- EEC003: Revisado
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 2, 1, 'Revisado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 2, 2, 'Reviewed', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 2, 3, 'Revisado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;

    -- EEC004: Rechazado
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 3, 1, 'Rechazado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 3, 2, 'Rejected', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 3, 3, 'Rejeitado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;

    -- EEC005: Reprocesado
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 4, 1, 'Reprocesado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 4, 2, 'Reprocessed', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;
    INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description, created_by, created_at)
    VALUES (v_dict_id_base + 4, 3, 'Reprocessado', 'STM-305', NOW())
    ON CONFLICT DO NOTHING;

    -- ========================================================================
    -- PASO 2: Crear el catalogo header
    -- ========================================================================

    INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status, created_at)
    VALUES (
        'CatEstatusEstadoCuenta',
        'EEC',
        'Estatus Estado de Cuenta',
        'Catalogo de estatus para el estado de cuenta de proveedores',
        'finanzas',
        'SIMPLE',
        1,
        NOW()
    )
    ON CONFLICT (code) DO NOTHING
    RETURNING id INTO v_header_id;

    -- Si ya existia, obtener el id
    IF v_header_id IS NULL THEN
        SELECT id INTO v_header_id FROM shared_catalogs.catalog_header WHERE code = 'CatEstatusEstadoCuenta';
    END IF;

    -- ========================================================================
    -- PASO 3: Crear los elementos del catalogo
    -- ========================================================================

    -- EEC001: Generado (internal_status = 1)
    INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, internal_status, sort_order, status, created_by, created_at)
    VALUES (v_header_id, 'EEC001', v_dict_id_base + 0, 1, 1, 1, 'STM-305', NOW())
    ON CONFLICT (header_id, key) DO NOTHING;

    -- EEC002: Publicado (internal_status = 2)
    INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, internal_status, sort_order, status, created_by, created_at)
    VALUES (v_header_id, 'EEC002', v_dict_id_base + 1, 2, 2, 1, 'STM-305', NOW())
    ON CONFLICT (header_id, key) DO NOTHING;

    -- EEC003: Revisado (internal_status = 3)
    INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, internal_status, sort_order, status, created_by, created_at)
    VALUES (v_header_id, 'EEC003', v_dict_id_base + 2, 3, 3, 1, 'STM-305', NOW())
    ON CONFLICT (header_id, key) DO NOTHING;

    -- EEC004: Rechazado (internal_status = 4)
    INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, internal_status, sort_order, status, created_by, created_at)
    VALUES (v_header_id, 'EEC004', v_dict_id_base + 3, 4, 4, 1, 'STM-305', NOW())
    ON CONFLICT (header_id, key) DO NOTHING;

    -- EEC005: Reprocesado (internal_status = 5)
    INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, internal_status, sort_order, status, created_by, created_at)
    VALUES (v_header_id, 'EEC005', v_dict_id_base + 4, 5, 5, 1, 'STM-305', NOW())
    ON CONFLICT (header_id, key) DO NOTHING;

    RAISE NOTICE 'Catalogo CatEstatusEstadoCuenta creado con header_id: %, dict_id_base: %', v_header_id, v_dict_id_base;

END $$;


-- ============================================================================
-- 2. VERIFICACION: Consultar el catalogo creado
-- ============================================================================

SELECT 'CATALOGO CREADO:' as info;

SELECT
    ch.id as header_id,
    ch.code,
    ch.name,
    ch.module,
    ch.catalog_type
FROM shared_catalogs.catalog_header ch
WHERE ch.code = 'CatEstatusEstadoCuenta';

SELECT 'ELEMENTOS DEL CATALOGO:' as info;

SELECT
    cd.key,
    cd.internal_status,
    cd.sort_order,
    dl.description as descripcion_es
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON ch.id = cd.header_id
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE ch.code = 'CatEstatusEstadoCuenta'
ORDER BY cd.sort_order;


-- ============================================================================
-- FIN DEL SCRIPT DE CATALOGO DE ESTATUS
-- ============================================================================
