-- ============================================================================
-- STM-1378: Creacion de Catalogo de Estatus Three Way Match
-- Esquema: shared_catalogs
-- Fecha: 2026-02-11
--
-- Catalogo: CatEstatusTWM
-- Prefijo: ETW
-- dict_id: 1200-1204
--
-- Estatus de conciliacion:
--   1 = Pendiente    (registro inicial, sin match completo)
--   2 = Parcial      (match parcial, falta informacion)
--   3 = Conciliado   (match completo OC = Recepcion = Factura)
--   4 = Discrepancia (diferencias detectadas entre documentos)
--   5 = Cerrado      (cerrado manualmente o por proceso)
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- 1. CREAR DICCIONARIO (traducciones)
-- ============================================================================

-- Diccionario para CatEstatusTWM (dict_id: 1200-1204)
-- Idiomas: 1=Espanol, 2=Ingles, 3=Portugues

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description)
VALUES
    -- ETW001: Pendiente
    (1200, 1, 'Pendiente'),
    (1200, 2, 'Pending'),
    (1200, 3, 'Pendente'),
    -- ETW002: Parcial
    (1201, 1, 'Parcial'),
    (1201, 2, 'Partial'),
    (1201, 3, 'Parcial'),
    -- ETW003: Conciliado
    (1202, 1, 'Conciliado'),
    (1202, 2, 'Reconciled'),
    (1202, 3, 'Conciliado'),
    -- ETW004: Discrepancia
    (1203, 1, 'Discrepancia'),
    (1203, 2, 'Discrepancy'),
    (1203, 3, 'Discrepancia'),
    -- ETW005: Cerrado
    (1204, 1, 'Cerrado'),
    (1204, 2, 'Closed'),
    (1204, 3, 'Fechado')
ON CONFLICT (dict_id, lang_id) DO UPDATE
SET description = EXCLUDED.description;


-- ============================================================================
-- 2. CREAR HEADER DEL CATALOGO
-- ============================================================================

INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type, status)
VALUES (
    'CatEstatusTWM',
    'ETW',
    'Estatus Three Way Match',
    'Catalogo de estatus para la conciliacion de tres vias (OC + Recepcion + Factura)',
    'finanzas',
    'SIMPLE',
    1
)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    module = EXCLUDED.module;


-- ============================================================================
-- 3. CREAR DETALLE DEL CATALOGO
-- ============================================================================

-- Obtener el header_id del catalogo recien creado
DO $$
DECLARE
    v_header_id INTEGER;
BEGIN
    SELECT id INTO v_header_id
    FROM shared_catalogs.catalog_header
    WHERE code = 'CatEstatusTWM';

    -- Insertar los elementos del catalogo
    INSERT INTO shared_catalogs.catalog_detail
        (header_id, key, dict_id, color, sort_order, status, internal_status)
    VALUES
        (v_header_id, 'ETW001', 1200, 'Amarillo', 1, 1, 1),  -- Pendiente
        (v_header_id, 'ETW002', 1201, 'Amarillo', 2, 1, 2),  -- Parcial
        (v_header_id, 'ETW003', 1202, 'Verde',    3, 1, 3),  -- Conciliado
        (v_header_id, 'ETW004', 1203, 'Rojo',     4, 1, 4),  -- Discrepancia
        (v_header_id, 'ETW005', 1204, 'Gris',     5, 1, 5)   -- Cerrado
    ON CONFLICT (header_id, key) DO UPDATE
    SET
        dict_id = EXCLUDED.dict_id,
        color = EXCLUDED.color,
        sort_order = EXCLUDED.sort_order,
        internal_status = EXCLUDED.internal_status;

    RAISE NOTICE 'Catalogo CatEstatusTWM creado con header_id: %', v_header_id;
END $$;


-- ============================================================================
-- 4. VERIFICACION
-- ============================================================================

-- Mostrar el catalogo creado
SELECT
    ch.code AS catalogo,
    ch.prefix,
    cd.key AS codigo,
    cd.internal_status AS valor,
    dl.description AS descripcion_es,
    cd.color,
    cd.sort_order
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON ch.id = cd.header_id
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE ch.code = 'CatEstatusTWM'
ORDER BY cd.sort_order;


-- ============================================================================
-- FIN DEL SCRIPT DE CATALOGO
-- ============================================================================
