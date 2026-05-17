-- =============================================================================
-- Seed seguridad para Sodimac BD — version paso a paso (sin transaccion)
-- DB: b2b_portal (PostgreSQL)
-- IMPORTANTE: Ejecuta bloque por bloque, no todo de un tiron.
-- Si un paso falla, lee el mensaje real (no el 25P02 cascade).
-- =============================================================================

ROLLBACK;  -- limpia estado abortado de intento previo


-- -----------------------------------------------------------------------------
-- PASO 0: diagnostico previo (ejecuta y revisa)
-- -----------------------------------------------------------------------------

-- 0.1 ¿Existe UNIQUE en catalog_header.code?
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'shared_catalogs.catalog_header'::regclass
  AND contype IN ('p','u');

-- 0.2 ¿Existe UNIQUE en catalog_detail (header_id, key)?
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'shared_catalogs.catalog_detail'::regclass
  AND contype IN ('p','u');

-- 0.3 ¿UNIQUE actual en user_attribute?
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'core_security.user_attribute'::regclass
  AND contype IN ('p','u');

-- 0.4 dict_ids ya ocupados cerca del rango 10093-10096
SELECT id, header_id, key, dict_id
FROM shared_catalogs.catalog_detail
WHERE dict_id BETWEEN 10090 AND 10100
ORDER BY dict_id;


-- -----------------------------------------------------------------------------
-- PASO 1: crear header CatProveedor (solo si NO existe)
-- -----------------------------------------------------------------------------
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type)
SELECT 'CatProveedor', 'PRV', 'Proveedor', 'Catalogo de proveedores', 'seguridad', 'SIMPLE'
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_header WHERE code = 'CatProveedor'
);

SELECT id, code, name FROM shared_catalogs.catalog_header WHERE code='CatProveedor';


-- -----------------------------------------------------------------------------
-- PASO 2: insertar valores -1, 11111, 22222, 33333 (solo los que no existan)
-- -----------------------------------------------------------------------------
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, status)
SELECT h.id, v.key, v.dict_id, v.sort_order, 1
FROM shared_catalogs.catalog_header h
CROSS JOIN (VALUES
    ('-1',    10093, 99),
    ('11111', 10094,  5),
    ('22222', 10095,  6),
    ('33333', 10096,  7)
) AS v(key, dict_id, sort_order)
WHERE h.code = 'CatProveedor'
  AND NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_detail d
    WHERE d.header_id = h.id AND d.key = v.key
  );

SELECT id, header_id, key, dict_id, sort_order
FROM shared_catalogs.catalog_detail
WHERE key IN ('-1','11111','22222','33333')
ORDER BY sort_order;


-- -----------------------------------------------------------------------------
-- PASO 3: limpiar user_attribute previos de tipo ATR001
-- -----------------------------------------------------------------------------
DELETE FROM core_security.user_attribute
WHERE catalog_detail_attribute_type_id = (
    SELECT id FROM shared_catalogs.catalog_detail WHERE key='ATR001'
);


-- -----------------------------------------------------------------------------
-- PASO 4a: FERNANDO -> 11111
-- -----------------------------------------------------------------------------
INSERT INTO core_security.user_attribute (user_data_id, catalog_detail_attribute_type_id, catalog_detail_attribute_value_id, status)
SELECT ud.user_data_id,
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='ATR001'),
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='11111'),
       1
FROM core_security.user_data ud
WHERE ud.preferred_username = 'USR_FERNANDO';


-- -----------------------------------------------------------------------------
-- PASO 4b: JOSE -> 11111
-- -----------------------------------------------------------------------------
INSERT INTO core_security.user_attribute (user_data_id, catalog_detail_attribute_type_id, catalog_detail_attribute_value_id, status)
SELECT ud.user_data_id,
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='ATR001'),
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='11111'),
       1
FROM core_security.user_data ud
WHERE ud.preferred_username = 'USR_JOSE';


-- -----------------------------------------------------------------------------
-- PASO 4c: JOSE -> 22222 (puede fallar si UNIQUE (user_data_id, type_id) activo)
-- Si falla aqui, el modelo Sodimac aun tiene UNIQUE viejo. Avisar para corregir.
-- -----------------------------------------------------------------------------
INSERT INTO core_security.user_attribute (user_data_id, catalog_detail_attribute_type_id, catalog_detail_attribute_value_id, status)
SELECT ud.user_data_id,
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='ATR001'),
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='22222'),
       1
FROM core_security.user_data ud
WHERE ud.preferred_username = 'USR_JOSE';


-- -----------------------------------------------------------------------------
-- PASO 4d: Ivan (zedlav.sd18@gmail.com) -> -1
-- -----------------------------------------------------------------------------
INSERT INTO core_security.user_attribute (user_data_id, catalog_detail_attribute_type_id, catalog_detail_attribute_value_id, status)
SELECT ud.user_data_id,
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='ATR001'),
       (SELECT id FROM shared_catalogs.catalog_detail WHERE key='-1'),
       1
FROM core_security.user_data ud
WHERE ud.preferred_username = 'zedlav.sd18@gmail.com';


-- =============================================================================
-- VERIFICACION final
-- =============================================================================
SELECT
    ud.preferred_username,
    t.key AS tipo,
    v.key AS valor
FROM core_security.user_attribute ua
JOIN core_security.user_data ud       ON ud.user_data_id = ua.user_data_id
JOIN shared_catalogs.catalog_detail t ON t.id = ua.catalog_detail_attribute_type_id
JOIN shared_catalogs.catalog_detail v ON v.id = ua.catalog_detail_attribute_value_id
WHERE t.key = 'ATR001'
ORDER BY ud.preferred_username, v.key;

-- Esperado:
-- USR_FERNANDO          ATR001  11111
-- USR_JOSE              ATR001  11111
-- USR_JOSE              ATR001  22222
-- zedlav.sd18@gmail.com ATR001  -1
