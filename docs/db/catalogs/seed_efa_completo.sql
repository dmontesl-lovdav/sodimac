-- =============================================================
-- Seed completo: Estatus EFA en CatEstatusComplemento
-- Portable: funciona en cualquier ambiente (no usa IDs hardcodeados)
-- Incluye: dictionary_lang + catalog_detail + internal_status
-- =============================================================

-- =============================================================
-- 1. Verificar que el header exista
-- =============================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM shared_catalogs.catalog_header WHERE code = 'CatEstatusComplemento'
    ) THEN
        RAISE EXCEPTION 'El catalogo CatEstatusComplemento no existe en catalog_header. Abortando.';
    END IF;
END $$;

-- =============================================================
-- 2. Insertar traducciones en dictionary_lang (si no existen)
--    Usa dict_id secuencial a partir del max actual + 1
--    Mapeo: EFA001=offset+0, EFA002=offset+1, ..., EFA014=offset+13
-- =============================================================
DO $$
DECLARE
    v_base_dict_id INTEGER;
    v_existing_count INTEGER;
BEGIN
    -- Verificar si ya existen traducciones EFA (por descripcion en español)
    SELECT COUNT(*) INTO v_existing_count
    FROM shared_catalogs.dictionary_lang
    WHERE description = 'Rechazo Comercial' AND lang_id = 1;

    IF v_existing_count > 0 THEN
        RAISE NOTICE 'Las traducciones EFA ya existen en dictionary_lang. Saltando inserts de diccionario.';
    ELSE
        -- Obtener siguiente dict_id disponible
        SELECT COALESCE(MAX(dict_id), 0) + 1 INTO v_base_dict_id
        FROM shared_catalogs.dictionary_lang;

        RAISE NOTICE 'Insertando traducciones EFA desde dict_id = %', v_base_dict_id;

        -- EFA001: Rechazo Comercial
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 0, 1, 'Rechazo Comercial'),
            (v_base_dict_id + 0, 2, 'Commercial Rejection'),
            (v_base_dict_id + 0, 3, 'Rejeição Comercial');
        -- EFA002: Pendiente Addenda
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 1, 1, 'Pendiente Addenda'),
            (v_base_dict_id + 1, 2, 'Pending Addenda'),
            (v_base_dict_id + 1, 3, 'Adendo Pendente');
        -- EFA003: Recibido Parcial
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 2, 1, 'Recibido Parcial'),
            (v_base_dict_id + 2, 2, 'Partial Received'),
            (v_base_dict_id + 2, 3, 'Recebido Parcial');
        -- EFA004: Pendiente de Contabilizar
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 3, 1, 'Pendiente de Contabilizar'),
            (v_base_dict_id + 3, 2, 'Pending Accounting'),
            (v_base_dict_id + 3, 3, 'Pendente de Contabilização');
        -- EFA005: En proceso de descarga
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 4, 1, 'En proceso de descarga'),
            (v_base_dict_id + 4, 2, 'Download in progress'),
            (v_base_dict_id + 4, 3, 'Em processo de download');
        -- EFA006: Desglose de factura
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 5, 1, 'Desglose de factura'),
            (v_base_dict_id + 5, 2, 'Invoice breakdown'),
            (v_base_dict_id + 5, 3, 'Detalhamento de fatura');
        -- EFA007: Pendiente de envio a contabilizar
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 6, 1, 'Pendiente de envío a contabilizar'),
            (v_base_dict_id + 6, 2, 'Pending submission for accounting'),
            (v_base_dict_id + 6, 3, 'Pendente de envio para contabilização');
        -- EFA008: Pendiente de Pago
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 7, 1, 'Pendiente de Pago'),
            (v_base_dict_id + 7, 2, 'Pending Payment'),
            (v_base_dict_id + 7, 3, 'Pendente de Pagamento');
        -- EFA009: Pagado
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 8, 1, 'Pagado'),
            (v_base_dict_id + 8, 2, 'Paid'),
            (v_base_dict_id + 8, 3, 'Pago');
        -- EFA010: Pendiente de complemento
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 9, 1, 'Pendiente de complemento'),
            (v_base_dict_id + 9, 2, 'Pending supplement'),
            (v_base_dict_id + 9, 3, 'Pendente de complemento');
        -- EFA011: Completado
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 10, 1, 'Completado'),
            (v_base_dict_id + 10, 2, 'Completed'),
            (v_base_dict_id + 10, 3, 'Completado');
        -- EFA012: Rechazo Contable
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 11, 1, 'Rechazo Contable'),
            (v_base_dict_id + 11, 2, 'Accounting Rejection'),
            (v_base_dict_id + 11, 3, 'Rejeição Contábil');
        -- EFA013: No valido fiscal
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 12, 1, 'No válido fiscal'),
            (v_base_dict_id + 12, 2, 'Not fiscally valid'),
            (v_base_dict_id + 12, 3, 'Não válido fiscalmente');
        -- EFA014: Pago Manual
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 13, 1, 'Pago Manual'),
            (v_base_dict_id + 13, 2, 'Manual Payment'),
            (v_base_dict_id + 13, 3, 'Pagamento Manual');
    END IF;
END $$;

-- =============================================================
-- 3. Insertar catalog_detail para EFA (si no existen)
--    Usa subquery para header_id y busca dict_id por descripcion
-- =============================================================
INSERT INTO shared_catalogs.catalog_detail
    (header_id, key, dict_id, color, sort_order, status, created_at, internal_status)
SELECT
    (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatEstatusComplemento'),
    v.key,
    (SELECT dl.dict_id FROM shared_catalogs.dictionary_lang dl WHERE dl.description = v.desc_es AND dl.lang_id = 1 LIMIT 1),
    v.color,
    v.sort_order,
    1,
    NOW(),
    v.sort_order - 1
FROM (VALUES
    ('EFA001', 'Rechazo Comercial',                   'Rojo',     1),
    ('EFA002', 'Pendiente Addenda',                   'Amarillo', 2),
    ('EFA003', 'Recibido Parcial',                    'Amarillo', 3),
    ('EFA004', 'Pendiente de Contabilizar',           'Amarillo', 4),
    ('EFA005', 'En proceso de descarga',              'Amarillo', 5),
    ('EFA006', 'Desglose de factura',                 'Amarillo', 6),
    ('EFA007', 'Pendiente de envío a contabilizar',   'Amarillo', 7),
    ('EFA008', 'Pendiente de Pago',                   'Amarillo', 8),
    ('EFA009', 'Pagado',                              'Verde',    9),
    ('EFA010', 'Pendiente de complemento',            'Amarillo', 10),
    ('EFA011', 'Completado',                          'Verde',    11),
    ('EFA012', 'Rechazo Contable',                    'Rojo',     12),
    ('EFA013', 'No válido fiscal',                    'Rojo',     13),
    ('EFA014', 'Pago Manual',                         'Verde',    14)
) AS v(key, desc_es, color, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatEstatusComplemento')
    AND cd.key = v.key
);

-- =============================================================
-- 4. Verificar resultado
-- =============================================================
SELECT cd.key, cd.internal_status, dl.description, cd.color, cd.sort_order, cd.status
FROM shared_catalogs.catalog_detail cd
LEFT JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatEstatusComplemento')
ORDER BY cd.sort_order;
