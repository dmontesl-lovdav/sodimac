-- ============================================================================
-- CATALOGOS API - Evolución de Esquema v2.0
-- Esquema: shared_catalogs
-- Descripción: Mejoras para soportar catálogos del SAT, atributos extendidos,
--              dependencias entre catálogos y consumo desde componentes frontend.
-- Fecha: 2025-12-04
-- ============================================================================

-- ============================================================================
-- PARTE 1: Modificaciones a catalog_header
-- Agregar columna catalog_type para definir el comportamiento del catálogo
-- ============================================================================

-- Agregar columna catalog_type
ALTER TABLE shared_catalogs.catalog_header
ADD COLUMN IF NOT EXISTS catalog_type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE';

COMMENT ON COLUMN shared_catalogs.catalog_header.catalog_type IS
'Tipo de catálogo que define su comportamiento:
- SIMPLE: Catálogo básico clave-valor (select, radio)
- MESSAGE: Mensajes de error/éxito/advertencia del sistema
- SAT_FISCAL: Catálogo del SAT con atributos fiscales extendidos
- HIERARCHICAL: Catálogo jerárquico con niveles (tree select)
- BOOLEAN: Catálogo de dos opciones Sí/No (checkbox)';

-- Índice para filtrar por tipo
CREATE INDEX IF NOT EXISTS idx_catalog_header_type
ON shared_catalogs.catalog_header(catalog_type);

-- ============================================================================
-- PARTE 2: Modificaciones a catalog_detail
-- Agregar columnas para clave externa, valor, vigencia y atributos JSON
-- ============================================================================

-- 2.1 Columna internal_status: identificador interno del elemento (idEstatus del Excel/negocio)
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS internal_status INTEGER;

COMMENT ON COLUMN shared_catalogs.catalog_detail.internal_status IS
'Identificador interno del elemento (idEstatus). Corresponde al ID numérico definido en el Excel del PM.
Para catálogos de estatus: 0=Pendiente, 1=Activo, 2=Procesado, etc.
Si no se especifica, se asigna incrementalmente dentro de cada catálogo (empezando en 1).';

-- 2.2 Columna external_key: clave real/externa del catálogo (ej: G01 del SAT)
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS external_key VARCHAR(50);

COMMENT ON COLUMN shared_catalogs.catalog_detail.external_key IS
'Clave externa o real del catálogo. Para catálogos del SAT es la clave oficial (G01, 601, 0.160000).
Para catálogos internos puede ser null o igual a key.';

-- 2.3 Columna value: valor asociado al elemento (ej: 0.16 para IVA 16%)
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS value VARCHAR(100);

COMMENT ON COLUMN shared_catalogs.catalog_detail.value IS
'Valor asociado al elemento del catálogo. Para tasas de impuestos es el porcentaje (0.16),
para otros catálogos puede ser un código, monto, o cualquier valor de negocio.';

-- 2.4 Columna valid_from: fecha de inicio de vigencia
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS valid_from DATE;

COMMENT ON COLUMN shared_catalogs.catalog_detail.valid_from IS
'Fecha de inicio de vigencia del elemento. NULL significa vigente desde siempre.
Útil para catálogos del SAT que cambian en fechas específicas.';

-- 2.5 Columna valid_to: fecha de fin de vigencia
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS valid_to DATE;

COMMENT ON COLUMN shared_catalogs.catalog_detail.valid_to IS
'Fecha de fin de vigencia del elemento. NULL significa sin fecha de expiración.
Útil para catálogos del SAT que dejan de ser válidos en fechas específicas.';

-- 2.6 Columna created_by: usuario que dio de alta el registro
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

COMMENT ON COLUMN shared_catalogs.catalog_detail.created_by IS
'Identificador del usuario o sistema que creó el registro.
Útil para auditoría y trazabilidad de cambios.';

-- 2.7 Columna attributes: atributos adicionales en formato JSON
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS attributes JSONB;

COMMENT ON COLUMN shared_catalogs.catalog_detail.attributes IS
'Atributos adicionales del elemento en formato JSON. Ejemplos:
- Catálogo UsoCFDI: {"aplica_pf": true, "aplica_pm": true}
- Catálogo TasaIVA: {"traslado": true, "retencion": true, "impuesto": "002"}
- Catálogo Régimen: {"persona_fisica": true, "persona_moral": false}
Permite extender el catálogo sin modificar el esquema.';

-- Índices para búsquedas
CREATE INDEX IF NOT EXISTS idx_catalog_detail_external_key
ON shared_catalogs.catalog_detail(external_key);

CREATE INDEX IF NOT EXISTS idx_catalog_detail_valid_from
ON shared_catalogs.catalog_detail(valid_from);

CREATE INDEX IF NOT EXISTS idx_catalog_detail_valid_to
ON shared_catalogs.catalog_detail(valid_to);

-- Índice GIN para búsquedas en JSON (PostgreSQL)
CREATE INDEX IF NOT EXISTS idx_catalog_detail_attributes
ON shared_catalogs.catalog_detail USING GIN (attributes);

-- ============================================================================
-- PARTE 3: Nueva tabla catalog_detail_relation
-- Relaciones/dependencias entre elementos de diferentes catálogos
-- ============================================================================

CREATE TABLE IF NOT EXISTS shared_catalogs.catalog_detail_relation (
    id SERIAL PRIMARY KEY,                              -- Identificador único
    source_detail_id INTEGER NOT NULL,                  -- FK al detalle origen (el que depende)
    target_detail_id INTEGER NOT NULL,                  -- FK al detalle destino (del que depende)
    relation_type VARCHAR(20) NOT NULL DEFAULT 'DEPENDS_ON', -- Tipo de relación
    status INTEGER NOT NULL DEFAULT 1,                  -- Estado: 1=Activo, 0=Inactivo
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_relation_source
        FOREIGN KEY (source_detail_id)
        REFERENCES shared_catalogs.catalog_detail(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_relation_target
        FOREIGN KEY (target_detail_id)
        REFERENCES shared_catalogs.catalog_detail(id)
        ON DELETE CASCADE,

    -- Evitar duplicados
    CONSTRAINT uk_detail_relation
        UNIQUE (source_detail_id, target_detail_id, relation_type)
);

COMMENT ON TABLE shared_catalogs.catalog_detail_relation IS
'Tabla de relaciones entre elementos de catálogos. Permite definir dependencias
entre catálogos (ej: UsoCFDI G01 depende de RegimenFiscal 601, 603, 606...).
Si está vacía, los catálogos funcionan de forma independiente.';

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.id IS
'Identificador único autoincremental';

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.source_detail_id IS
'ID del elemento que tiene la dependencia (ej: G01 de UsoCFDI)';

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.target_detail_id IS
'ID del elemento del que depende (ej: 601 de RegimenFiscal)';

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.relation_type IS
'Tipo de relación:
- DEPENDS_ON: El source solo es válido si target está seleccionado
- REQUIRES: El source requiere que target exista/esté activo
- EXCLUDES: El source es incompatible con target
- PARENT_OF: Relación jerárquica padre-hijo';

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.status IS
'Estado de la relación: 1=Activa, 0=Inactiva';

-- Índices para consultas eficientes
CREATE INDEX IF NOT EXISTS idx_relation_source
ON shared_catalogs.catalog_detail_relation(source_detail_id);

CREATE INDEX IF NOT EXISTS idx_relation_target
ON shared_catalogs.catalog_detail_relation(target_detail_id);

CREATE INDEX IF NOT EXISTS idx_relation_type
ON shared_catalogs.catalog_detail_relation(relation_type);

CREATE INDEX IF NOT EXISTS idx_relation_status
ON shared_catalogs.catalog_detail_relation(status);

-- Índice compuesto para la consulta más común: obtener dependencias activas de un source
CREATE INDEX IF NOT EXISTS idx_relation_source_active
ON shared_catalogs.catalog_detail_relation(source_detail_id, status)
WHERE status = 1;

-- ============================================================================
-- PARTE 4: Actualizar catálogos existentes con catalog_type
-- ============================================================================

-- Catálogos de mensajes
UPDATE shared_catalogs.catalog_header
SET catalog_type = 'MESSAGE'
WHERE prefix IN ('BUS', 'ERR', 'WRN', 'RES');

-- Catálogos simples existentes
UPDATE shared_catalogs.catalog_header
SET catalog_type = 'SIMPLE'
WHERE catalog_type IS NULL OR catalog_type = '';

-- ============================================================================
-- PARTE 5: Vistas útiles para consultas
-- ============================================================================

-- Vista: Detalles de catálogo con información completa
CREATE OR REPLACE VIEW shared_catalogs.v_catalog_detail_full AS
SELECT
    cd.id,
    cd.header_id,
    ch.code AS catalog_code,
    ch.prefix AS catalog_prefix,
    ch.name AS catalog_name,
    ch.catalog_type,
    cd.key,
    cd.external_key,
    cd.value,
    cd.dict_id,
    cd.color,
    cd.sort_order,
    cd.internal_status,
    cd.status,
    cd.valid_from,
    cd.valid_to,
    cd.attributes,
    cd.created_at,
    cd.created_by,
    -- Indicador de vigencia
    CASE
        WHEN cd.valid_from IS NOT NULL AND cd.valid_from > CURRENT_DATE THEN 'FUTURO'
        WHEN cd.valid_to IS NOT NULL AND cd.valid_to < CURRENT_DATE THEN 'EXPIRADO'
        ELSE 'VIGENTE'
    END AS vigencia_status
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id;

COMMENT ON VIEW shared_catalogs.v_catalog_detail_full IS
'Vista con información completa de detalles de catálogo incluyendo datos del header';

-- Vista: Relaciones entre catálogos con nombres legibles
CREATE OR REPLACE VIEW shared_catalogs.v_catalog_relations AS
SELECT
    r.id AS relation_id,
    r.relation_type,
    r.status AS relation_status,
    -- Source (el que depende)
    s.id AS source_id,
    s.key AS source_key,
    s.external_key AS source_external_key,
    sh.code AS source_catalog_code,
    sh.name AS source_catalog_name,
    -- Target (del que depende)
    t.id AS target_id,
    t.key AS target_key,
    t.external_key AS target_external_key,
    th.code AS target_catalog_code,
    th.name AS target_catalog_name
FROM shared_catalogs.catalog_detail_relation r
JOIN shared_catalogs.catalog_detail s ON r.source_detail_id = s.id
JOIN shared_catalogs.catalog_header sh ON s.header_id = sh.id
JOIN shared_catalogs.catalog_detail t ON r.target_detail_id = t.id
JOIN shared_catalogs.catalog_header th ON t.header_id = th.id;

COMMENT ON VIEW shared_catalogs.v_catalog_relations IS
'Vista con información legible de las relaciones entre catálogos';

-- ============================================================================
-- FIN DEL SCRIPT DE EVOLUCIÓN v2.0
-- ============================================================================

/*
EJEMPLOS DE USO:

1. Obtener detalles de catálogo filtrados por atributo JSON:
   SELECT * FROM shared_catalogs.catalog_detail
   WHERE attributes->>'aplica_pf' = 'true';

2. Obtener catálogos con dependencias:
   SELECT source_key, target_key, relation_type
   FROM shared_catalogs.v_catalog_relations
   WHERE source_catalog_code = 'USO_CFDI';

3. Obtener detalles válidos a una fecha:
   SELECT * FROM shared_catalogs.catalog_detail
   WHERE (valid_from IS NULL OR valid_from <= CURRENT_DATE)
     AND (valid_to IS NULL OR valid_to >= CURRENT_DATE);

4. Filtrar por dependencia (ej: UsoCFDI que aplican para régimen 605):
   SELECT DISTINCT cd.*
   FROM shared_catalogs.catalog_detail cd
   JOIN shared_catalogs.catalog_detail_relation r ON cd.id = r.source_detail_id
   JOIN shared_catalogs.catalog_detail target ON r.target_detail_id = target.id
   WHERE target.external_key = '605'
     AND r.relation_type = 'DEPENDS_ON'
     AND r.status = 1;
*/
