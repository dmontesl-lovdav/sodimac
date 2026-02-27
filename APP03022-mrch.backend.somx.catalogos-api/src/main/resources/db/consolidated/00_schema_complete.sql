-- ============================================================================
-- CATALOGOS API - Script Consolidado de Esquema y Tablas
-- Esquema: shared_catalogs
-- Descripcion: Script unificado para crear todas las tablas del modulo de catalogos
-- Fecha: 2025-12-22
--
-- NOTA: Este script maneja errores gracefully - si una tabla/columna ya existe,
--       continua con el siguiente elemento sin fallar.
-- ============================================================================

-- ============================================================================
-- CREAR ESQUEMA
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS shared_catalogs;

SET search_path TO shared_catalogs;

-- ============================================================================
-- TABLA: dictionary_lang
-- Descripcion: Diccionario de traducciones para soporte multi-idioma.
--              Almacena las descripciones de los elementos en diferentes idiomas.
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.dictionary_lang (
    id SERIAL PRIMARY KEY,
    dict_id INTEGER NOT NULL,
    lang_id INTEGER NOT NULL,
    description VARCHAR(512) NOT NULL,
    CONSTRAINT uk_dictionary_lang UNIQUE (dict_id, lang_id)
);

COMMENT ON TABLE shared_catalogs.dictionary_lang IS 'Diccionario de traducciones para soporte multi-idioma del sistema de catalogos';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.dict_id IS 'Identificador del diccionario que agrupa las traducciones del mismo texto';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.lang_id IS 'Identificador del idioma: 1=Espanol, 2=Ingles, 3=Portugues';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.description IS 'Texto traducido en el idioma especificado';

CREATE INDEX IF NOT EXISTS idx_dictionary_dict_id ON shared_catalogs.dictionary_lang(dict_id);
CREATE INDEX IF NOT EXISTS idx_dictionary_lang_id ON shared_catalogs.dictionary_lang(lang_id);

-- ============================================================================
-- TABLA: catalog_header
-- Descripcion: Encabezado de catalogos. Define los catalogos maestros disponibles
--              en el sistema (ej: Estatus de Factura, Tipos de Proveedor, etc.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.catalog_header (
    id SERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    prefix VARCHAR(4) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    module VARCHAR(32),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE shared_catalogs.catalog_header IS 'Encabezado de catalogos maestros del sistema FBC';
COMMENT ON COLUMN shared_catalogs.catalog_header.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.catalog_header.code IS 'Codigo unico del catalogo para referencia en codigo (ej: CatEstatusFactura)';
COMMENT ON COLUMN shared_catalogs.catalog_header.name IS 'Nombre descriptivo del catalogo para mostrar en UI';
COMMENT ON COLUMN shared_catalogs.catalog_header.description IS 'Descripcion detallada del proposito y uso del catalogo';
COMMENT ON COLUMN shared_catalogs.catalog_header.module IS 'Modulo funcional: fiscal, transporte, sistema, general';
COMMENT ON COLUMN shared_catalogs.catalog_header.status IS 'Estado del registro: 1=Activo, 0=Inactivo, -1=Pendiente revision funcional';
COMMENT ON COLUMN shared_catalogs.catalog_header.created_at IS 'Fecha y hora de creacion del registro';
COMMENT ON COLUMN shared_catalogs.catalog_header.updated_at IS 'Fecha y hora de ultima modificacion';

CREATE INDEX IF NOT EXISTS idx_catalog_header_code ON shared_catalogs.catalog_header(code);
CREATE INDEX IF NOT EXISTS idx_catalog_header_module ON shared_catalogs.catalog_header(module);
CREATE INDEX IF NOT EXISTS idx_catalog_header_status ON shared_catalogs.catalog_header(status);

-- ============================================================================
-- EVOLUCION: Agregar columna catalog_type a catalog_header
-- ============================================================================
ALTER TABLE shared_catalogs.catalog_header
ADD COLUMN IF NOT EXISTS catalog_type VARCHAR(20) NOT NULL DEFAULT 'SIMPLE';

COMMENT ON COLUMN shared_catalogs.catalog_header.catalog_type IS
'Tipo de catalogo que define su comportamiento:
- SIMPLE: Catalogo basico clave-valor (select, radio)
- MESSAGE: Mensajes de error/exito/advertencia del sistema
- SAT_FISCAL: Catalogo del SAT con atributos fiscales extendidos
- HIERARCHICAL: Catalogo jerarquico con niveles (tree select)
- BOOLEAN: Catalogo de dos opciones Si/No (checkbox)';

CREATE INDEX IF NOT EXISTS idx_catalog_header_type
ON shared_catalogs.catalog_header(catalog_type);

-- ============================================================================
-- TABLA: catalog_detail
-- Descripcion: Detalle de catalogos. Contiene los elementos individuales de cada
--              catalogo (ej: los diferentes estatus de una factura).
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.catalog_detail (
    id SERIAL PRIMARY KEY,
    header_id INTEGER NOT NULL,
    key VARCHAR(64) NOT NULL,
    dict_id INTEGER NOT NULL,
    color VARCHAR(16),
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_catalog_detail UNIQUE (header_id, key),
    CONSTRAINT fk_catalog_detail_header FOREIGN KEY (header_id) REFERENCES shared_catalogs.catalog_header(id)
);

COMMENT ON TABLE shared_catalogs.catalog_detail IS 'Detalle de elementos de cada catalogo del sistema FBC';
COMMENT ON COLUMN shared_catalogs.catalog_detail.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.catalog_detail.header_id IS 'Referencia al catalogo padre en catalog_header';
COMMENT ON COLUMN shared_catalogs.catalog_detail.key IS 'Clave unica del elemento dentro del catalogo (ej: RES1001, 0, 1)';
COMMENT ON COLUMN shared_catalogs.catalog_detail.dict_id IS 'Referencia al diccionario para obtener la descripcion traducida';
COMMENT ON COLUMN shared_catalogs.catalog_detail.color IS 'Color asociado para representacion visual (Verde, Rojo, Amarillo, #hex)';
COMMENT ON COLUMN shared_catalogs.catalog_detail.sort_order IS 'Orden de visualizacion en listas y dropdowns';
COMMENT ON COLUMN shared_catalogs.catalog_detail.status IS 'Estado del registro: 1=Activo, 0=Inactivo, -1=Pendiente revision funcional';
COMMENT ON COLUMN shared_catalogs.catalog_detail.created_at IS 'Fecha y hora de creacion del registro';
COMMENT ON COLUMN shared_catalogs.catalog_detail.updated_at IS 'Fecha y hora de ultima modificacion';

CREATE INDEX IF NOT EXISTS idx_catalog_detail_header ON shared_catalogs.catalog_detail(header_id);
CREATE INDEX IF NOT EXISTS idx_catalog_detail_key ON shared_catalogs.catalog_detail(key);
CREATE INDEX IF NOT EXISTS idx_catalog_detail_dict ON shared_catalogs.catalog_detail(dict_id);
CREATE INDEX IF NOT EXISTS idx_catalog_detail_status ON shared_catalogs.catalog_detail(status);

-- ============================================================================
-- EVOLUCION: Agregar columnas adicionales a catalog_detail
-- ============================================================================

-- internal_status: identificador interno del elemento
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS internal_status INTEGER;

COMMENT ON COLUMN shared_catalogs.catalog_detail.internal_status IS
'Identificador interno del elemento (idEstatus). Corresponde al ID numerico definido en el Excel del PM.';

-- external_key: clave real/externa del catalogo (ej: G01 del SAT)
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS external_key VARCHAR(50);

COMMENT ON COLUMN shared_catalogs.catalog_detail.external_key IS
'Clave externa o real del catalogo. Para catalogos del SAT es la clave oficial (G01, 601, 0.160000).';

-- value: valor asociado al elemento
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS value VARCHAR(100);

COMMENT ON COLUMN shared_catalogs.catalog_detail.value IS
'Valor asociado al elemento del catalogo. Para tasas de impuestos es el porcentaje.';

-- valid_from: fecha de inicio de vigencia
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS valid_from DATE;

COMMENT ON COLUMN shared_catalogs.catalog_detail.valid_from IS
'Fecha de inicio de vigencia del elemento. NULL significa vigente desde siempre.';

-- valid_to: fecha de fin de vigencia
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS valid_to DATE;

COMMENT ON COLUMN shared_catalogs.catalog_detail.valid_to IS
'Fecha de fin de vigencia del elemento. NULL significa sin fecha de expiracion.';

-- created_by: usuario que dio de alta el registro
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

COMMENT ON COLUMN shared_catalogs.catalog_detail.created_by IS
'Identificador del usuario o sistema que creo el registro.';

-- attributes: atributos adicionales en formato JSON
ALTER TABLE shared_catalogs.catalog_detail
ADD COLUMN IF NOT EXISTS attributes JSONB;

COMMENT ON COLUMN shared_catalogs.catalog_detail.attributes IS
'Atributos adicionales del elemento en formato JSON.';

-- Indices para nuevas columnas
CREATE INDEX IF NOT EXISTS idx_catalog_detail_external_key
ON shared_catalogs.catalog_detail(external_key);

CREATE INDEX IF NOT EXISTS idx_catalog_detail_valid_from
ON shared_catalogs.catalog_detail(valid_from);

CREATE INDEX IF NOT EXISTS idx_catalog_detail_valid_to
ON shared_catalogs.catalog_detail(valid_to);

CREATE INDEX IF NOT EXISTS idx_catalog_detail_attributes
ON shared_catalogs.catalog_detail USING GIN (attributes);

-- ============================================================================
-- TABLA: catalog_detail_relation
-- Descripcion: Relaciones/dependencias entre elementos de diferentes catalogos
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.catalog_detail_relation (
    id SERIAL PRIMARY KEY,
    source_detail_id INTEGER NOT NULL,
    target_detail_id INTEGER NOT NULL,
    relation_type VARCHAR(20) NOT NULL DEFAULT 'DEPENDS_ON',
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT fk_relation_source
        FOREIGN KEY (source_detail_id)
        REFERENCES shared_catalogs.catalog_detail(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_relation_target
        FOREIGN KEY (target_detail_id)
        REFERENCES shared_catalogs.catalog_detail(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_detail_relation
        UNIQUE (source_detail_id, target_detail_id, relation_type)
);

COMMENT ON TABLE shared_catalogs.catalog_detail_relation IS
'Tabla de relaciones entre elementos de catalogos.';

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.relation_type IS
'Tipo de relacion: DEPENDS_ON, REQUIRES, EXCLUDES, PARENT_OF';

CREATE INDEX IF NOT EXISTS idx_relation_source
ON shared_catalogs.catalog_detail_relation(source_detail_id);

CREATE INDEX IF NOT EXISTS idx_relation_target
ON shared_catalogs.catalog_detail_relation(target_detail_id);

CREATE INDEX IF NOT EXISTS idx_relation_type
ON shared_catalogs.catalog_detail_relation(relation_type);

CREATE INDEX IF NOT EXISTS idx_relation_status
ON shared_catalogs.catalog_detail_relation(status);

CREATE INDEX IF NOT EXISTS idx_relation_source_active
ON shared_catalogs.catalog_detail_relation(source_detail_id, status)
WHERE status = 1;

-- ============================================================================
-- TABLA: supplier_type
-- Descripcion: Catalogo de tipos de proveedor (Nacional, Internacional, etc.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.supplier_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.supplier_type IS 'Catalogo de tipos de proveedor del sistema';

CREATE INDEX IF NOT EXISTS idx_supplier_type_code ON shared_catalogs.supplier_type(code);
CREATE INDEX IF NOT EXISTS idx_supplier_type_status ON shared_catalogs.supplier_type(status);

-- ============================================================================
-- TABLA: payment_condition
-- Descripcion: Catalogo de condiciones de pago disponibles para proveedores
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.payment_condition (
    id SERIAL PRIMARY KEY,
    supplier_number VARCHAR(20),
    condition_name VARCHAR(100) NOT NULL,
    days INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.payment_condition IS 'Catalogo de condiciones de pago para proveedores';

CREATE INDEX IF NOT EXISTS idx_payment_condition_supplier ON shared_catalogs.payment_condition(supplier_number);
CREATE INDEX IF NOT EXISTS idx_payment_condition_status ON shared_catalogs.payment_condition(status);

-- ============================================================================
-- TABLA: supplier
-- Descripcion: Catalogo maestro de proveedores del sistema
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.supplier (
    id SERIAL PRIMARY KEY,
    supplier_number VARCHAR(20) NOT NULL UNIQUE,
    rfc VARCHAR(13) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    supplier_type_id INTEGER,
    logo VARCHAR(500),
    payment_condition_id INTEGER,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT fk_supplier_type
        FOREIGN KEY (supplier_type_id)
        REFERENCES shared_catalogs.supplier_type(id),
    CONSTRAINT fk_supplier_payment_condition
        FOREIGN KEY (payment_condition_id)
        REFERENCES shared_catalogs.payment_condition(id)
);

COMMENT ON TABLE shared_catalogs.supplier IS 'Catalogo maestro de proveedores del sistema FBC';

CREATE INDEX IF NOT EXISTS idx_supplier_number ON shared_catalogs.supplier(supplier_number);
CREATE INDEX IF NOT EXISTS idx_supplier_rfc ON shared_catalogs.supplier(rfc);
CREATE INDEX IF NOT EXISTS idx_supplier_type ON shared_catalogs.supplier(supplier_type_id);
CREATE INDEX IF NOT EXISTS idx_supplier_status ON shared_catalogs.supplier(status);

-- ============================================================================
-- TABLA: supplier_block
-- Descripcion: Bloqueos temporales de proveedores por rango de fechas
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.supplier_block (
    id SERIAL PRIMARY KEY,
    supplier_number VARCHAR(20) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    block_reason VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_supplier_block_dates CHECK (valid_to >= valid_from)
);

COMMENT ON TABLE shared_catalogs.supplier_block IS 'Bloqueos temporales de proveedores por rango de fechas';

CREATE INDEX IF NOT EXISTS idx_supplier_block_number ON shared_catalogs.supplier_block(supplier_number);
CREATE INDEX IF NOT EXISTS idx_supplier_block_dates ON shared_catalogs.supplier_block(valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_supplier_block_status ON shared_catalogs.supplier_block(status);

-- ============================================================================
-- TABLA: cost_center
-- Descripcion: Catalogo de centros de costo
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.cost_center (
    id SERIAL PRIMARY KEY,
    cost_center_code VARCHAR(20) NOT NULL UNIQUE,
    sap_branch VARCHAR(20),
    sodimac_branch VARCHAR(20),
    description VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.cost_center IS 'Catalogo de centros de costo';

CREATE INDEX IF NOT EXISTS idx_cost_center_code ON shared_catalogs.cost_center(cost_center_code);
CREATE INDEX IF NOT EXISTS idx_cost_center_sap_branch ON shared_catalogs.cost_center(sap_branch);
CREATE INDEX IF NOT EXISTS idx_cost_center_status ON shared_catalogs.cost_center(status);

-- ============================================================================
-- TABLA: benefit_center
-- Descripcion: Catalogo de centros de beneficio
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.benefit_center (
    id SERIAL PRIMARY KEY,
    benefit_center_code VARCHAR(20) NOT NULL UNIQUE,
    sap_branch VARCHAR(20),
    sodimac_branch VARCHAR(20),
    description VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.benefit_center IS 'Catalogo de centros de beneficio';

CREATE INDEX IF NOT EXISTS idx_benefit_center_code ON shared_catalogs.benefit_center(benefit_center_code);
CREATE INDEX IF NOT EXISTS idx_benefit_center_sap_branch ON shared_catalogs.benefit_center(sap_branch);
CREATE INDEX IF NOT EXISTS idx_benefit_center_status ON shared_catalogs.benefit_center(status);

-- ============================================================================
-- TABLA: accounting_account
-- Descripcion: Catalogo de cuentas contables
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.accounting_account (
    id SERIAL PRIMARY KEY,
    benefit_center_code VARCHAR(20) NOT NULL,
    sap_branch VARCHAR(20),
    sodimac_branch VARCHAR(20),
    description VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.accounting_account IS 'Catalogo de cuentas contables';

CREATE INDEX IF NOT EXISTS idx_accounting_account_benefit_center ON shared_catalogs.accounting_account(benefit_center_code);
CREATE INDEX IF NOT EXISTS idx_accounting_account_sap_branch ON shared_catalogs.accounting_account(sap_branch);
CREATE INDEX IF NOT EXISTS idx_accounting_account_status ON shared_catalogs.accounting_account(status);

-- ============================================================================
-- VISTAS UTILES
-- ============================================================================

-- Vista: Detalles de catalogo con informacion completa
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
    CASE
        WHEN cd.valid_from IS NOT NULL AND cd.valid_from > CURRENT_DATE THEN 'FUTURO'
        WHEN cd.valid_to IS NOT NULL AND cd.valid_to < CURRENT_DATE THEN 'EXPIRADO'
        ELSE 'VIGENTE'
    END AS vigencia_status
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id;

COMMENT ON VIEW shared_catalogs.v_catalog_detail_full IS
'Vista con informacion completa de detalles de catalogo incluyendo datos del header';

-- Vista: Relaciones entre catalogos con nombres legibles
CREATE OR REPLACE VIEW shared_catalogs.v_catalog_relations AS
SELECT
    r.id AS relation_id,
    r.relation_type,
    r.status AS relation_status,
    s.id AS source_id,
    s.key AS source_key,
    s.external_key AS source_external_key,
    sh.code AS source_catalog_code,
    sh.name AS source_catalog_name,
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
'Vista con informacion legible de las relaciones entre catalogos';

-- ============================================================================
-- VERIFICACION DE TABLAS CREADAS
-- ============================================================================
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'shared_catalogs'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- ============================================================================
-- FIN DEL SCRIPT DE ESQUEMA CONSOLIDADO
-- ============================================================================
