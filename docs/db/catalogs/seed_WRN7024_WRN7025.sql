-- =============================================================
-- Seed: WRN7024 y WRN7025 en CatMsgAdvertencia
-- Solicitado por: Bonelli
-- Portable: usa subqueries, no IDs hardcodeados
-- Autor: dmontes
-- Fecha: 2026-03-03
--
-- Descripcion:
--   WRN7024 - "El valor del pago total no es igual al desglose
--              del pago, favor de validar."
--   WRN7025 - "El desglose del pago no contiene un ingreso,
--              favor de validar."
--
-- Prerequisito: CatMsgAdvertencia (header_id=11) debe existir
-- =============================================================

-- =============================================================
-- 1. Insertar traducciones en dictionary_lang (si no existen)
-- =============================================================
DO $$
DECLARE
    v_base_dict_id INTEGER;
    v_existing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_existing_count
    FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
      AND cd.key IN ('WRN7024', 'WRN7025');

    IF v_existing_count > 0 THEN
        RAISE NOTICE 'WRN7024/WRN7025 ya existen en CatMsgAdvertencia. Saltando.';
    ELSE
        SELECT COALESCE(MAX(dict_id), 0) + 1 INTO v_base_dict_id
        FROM shared_catalogs.dictionary_lang;

        RAISE NOTICE 'Insertando traducciones WRN7024/WRN7025 desde dict_id = %', v_base_dict_id;

        -- WRN7024
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 0, 1, 'El valor del pago total no es igual al desglose del pago, favor de validar.'),
            (v_base_dict_id + 0, 2, 'The total payment value does not match the payment breakdown, please validate.'),
            (v_base_dict_id + 0, 3, 'O valor do pagamento total não é igual ao detalhamento do pagamento, favor validar.');

        -- WRN7025
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 1, 1, 'El desglose del pago no contiene un ingreso, favor de validar.'),
            (v_base_dict_id + 1, 2, 'The payment breakdown does not contain an income entry, please validate.'),
            (v_base_dict_id + 1, 3, 'O detalhamento do pagamento não contém uma receita, favor validar.');
    END IF;
END $$;

-- =============================================================
-- 2. Insertar catalog_detail (si no existen)
-- =============================================================
INSERT INTO shared_catalogs.catalog_detail
    (header_id, key, dict_id, sort_order, status, created_at)
SELECT
    (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia'),
    v.key,
    (SELECT dl.dict_id FROM shared_catalogs.dictionary_lang dl WHERE dl.description = v.desc_es AND dl.lang_id = 1 LIMIT 1),
    v.sort_order,
    1,
    NOW()
FROM (VALUES
    ('WRN7024', 'El valor del pago total no es igual al desglose del pago, favor de validar.', 7024),
    ('WRN7025', 'El desglose del pago no contiene un ingreso, favor de validar.', 7025)
) AS v(key, desc_es, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
    AND cd.key = v.key
);

-- =============================================================
-- 3. Verificar resultado
-- =============================================================
SELECT cd.key, cd.sort_order, cd.status, dl.lang_id, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
  AND cd.key IN ('WRN7024', 'WRN7025')
ORDER BY cd.key, dl.lang_id;
