-- ============================================================================
-- CATALOGOS API - Creación de Esquema y Tablas
-- Esquema: shared_catalogs
-- Descripción: Catálogos compartidos para el portal FBC (Falabella Business Center)
-- ============================================================================

-- Crear esquema si no existe
--CREATE SCHEMA IF NOT EXISTS shared_catalogs;

-- Establecer el esquema por defecto
--SET search_path TO shared_catalogs;

-- ============================================================================
-- Tabla: dictionary_lang
-- Descripción: Diccionario de traducciones para soporte multi-idioma.
--              Almacena las descripciones de los elementos en diferentes idiomas.
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.dictionary_lang (
    id SERIAL PRIMARY KEY,                              -- Identificador único autoincremental
    dict_id INTEGER NOT NULL,                           -- Identificador del diccionario (agrupa traducciones del mismo texto)
    lang_id INTEGER NOT NULL,                           -- Identificador del idioma (1=ES, 2=EN, 3=PT)
    description VARCHAR(512) NOT NULL,                  -- Texto traducido en el idioma especificado
    CONSTRAINT uk_dictionary_lang UNIQUE (dict_id, lang_id)
);

COMMENT ON TABLE shared_catalogs.dictionary_lang IS 'Diccionario de traducciones para soporte multi-idioma del sistema de catálogos';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.id IS 'Identificador único autoincremental';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.dict_id IS 'Identificador del diccionario que agrupa las traducciones del mismo texto';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.lang_id IS 'Identificador del idioma: 1=Español, 2=Inglés, 3=Portugués';
COMMENT ON COLUMN shared_catalogs.dictionary_lang.description IS 'Texto traducido en el idioma especificado';

CREATE INDEX IF NOT EXISTS idx_dictionary_dict_id ON shared_catalogs.dictionary_lang(dict_id);
CREATE INDEX IF NOT EXISTS idx_dictionary_lang_id ON shared_catalogs.dictionary_lang(lang_id);

-- ============================================================================
-- Tabla: catalog_header
-- Descripción: Encabezado de catálogos. Define los catálogos maestros disponibles
--              en el sistema (ej: Estatus de Factura, Tipos de Proveedor, etc.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.catalog_header (
    id SERIAL PRIMARY KEY,                              -- Identificador único autoincremental
    code VARCHAR(64) NOT NULL UNIQUE,                   -- Código único del catálogo (ej: CatEstatusFactura)
    prefix VARCHAR(4) NOT NULL,                         -- Prefijo para claves de detalle (ej: EFA, BUS, WRN)
    name VARCHAR(128) NOT NULL,                         -- Nombre descriptivo del catálogo
    description VARCHAR(512),                           -- Descripción detallada del propósito del catálogo
    module VARCHAR(32),                                 -- Módulo al que pertenece (fiscal, transporte, sistema, general)
    status INTEGER NOT NULL DEFAULT 1,                  -- Estado: 1=Activo, 0=Inactivo, -1=Pendiente revisión
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha de creación
    updated_at TIMESTAMP                                -- Fecha de última actualización
);

COMMENT ON TABLE shared_catalogs.catalog_header IS 'Encabezado de catálogos maestros del sistema FBC';
COMMENT ON COLUMN shared_catalogs.catalog_header.id IS 'Identificador único autoincremental';
COMMENT ON COLUMN shared_catalogs.catalog_header.code IS 'Código único del catálogo para referencia en código (ej: CatEstatusFactura)';
COMMENT ON COLUMN shared_catalogs.catalog_header.name IS 'Nombre descriptivo del catálogo para mostrar en UI';
COMMENT ON COLUMN shared_catalogs.catalog_header.description IS 'Descripción detallada del propósito y uso del catálogo';
COMMENT ON COLUMN shared_catalogs.catalog_header.module IS 'Módulo funcional: fiscal, transporte, sistema, general';
COMMENT ON COLUMN shared_catalogs.catalog_header.status IS 'Estado del registro: 1=Activo, 0=Inactivo, -1=Pendiente revisión funcional';
COMMENT ON COLUMN shared_catalogs.catalog_header.created_at IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN shared_catalogs.catalog_header.updated_at IS 'Fecha y hora de última modificación';

CREATE INDEX IF NOT EXISTS idx_catalog_header_code ON shared_catalogs.catalog_header(code);
CREATE INDEX IF NOT EXISTS idx_catalog_header_module ON shared_catalogs.catalog_header(module);
CREATE INDEX IF NOT EXISTS idx_catalog_header_status ON shared_catalogs.catalog_header(status);

-- ============================================================================
-- Tabla: catalog_detail
-- Descripción: Detalle de catálogos. Contiene los elementos individuales de cada
--              catálogo (ej: los diferentes estatus de una factura).
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.catalog_detail (
    id SERIAL PRIMARY KEY,                              -- Identificador único autoincremental
    header_id INTEGER NOT NULL,                         -- FK al catálogo padre (catalog_header)
    key VARCHAR(64) NOT NULL,                           -- Clave del elemento (ej: RES1001, BUS2001)
    dict_id INTEGER NOT NULL,                           -- FK al diccionario para la descripción traducida
    color VARCHAR(16),                                  -- Color asociado para UI (ej: Verde, Rojo, #28a745)
    sort_order INTEGER NOT NULL DEFAULT 0,              -- Orden de visualización
    status INTEGER NOT NULL DEFAULT 1,                  -- Estado: 1=Activo, 0=Inactivo, -1=Pendiente revisión
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha de creación
    updated_at TIMESTAMP,                               -- Fecha de última actualización
    CONSTRAINT uk_catalog_detail UNIQUE (header_id, key),
    CONSTRAINT fk_catalog_detail_header FOREIGN KEY (header_id) REFERENCES shared_catalogs.catalog_header(id)
    -- Nota: dict_id es una relación lógica con dictionary_lang(dict_id, lang_id)
    -- No se usa FK porque dict_id se repite por cada idioma en dictionary_lang
);

COMMENT ON TABLE shared_catalogs.catalog_detail IS 'Detalle de elementos de cada catálogo del sistema FBC';
COMMENT ON COLUMN shared_catalogs.catalog_detail.id IS 'Identificador único autoincremental';
COMMENT ON COLUMN shared_catalogs.catalog_detail.header_id IS 'Referencia al catálogo padre en catalog_header';
COMMENT ON COLUMN shared_catalogs.catalog_detail.key IS 'Clave única del elemento dentro del catálogo (ej: RES1001, 0, 1)';
COMMENT ON COLUMN shared_catalogs.catalog_detail.dict_id IS 'Referencia al diccionario para obtener la descripción traducida';
COMMENT ON COLUMN shared_catalogs.catalog_detail.color IS 'Color asociado para representación visual (Verde, Rojo, Amarillo, #hex)';
COMMENT ON COLUMN shared_catalogs.catalog_detail.sort_order IS 'Orden de visualización en listas y dropdowns';
COMMENT ON COLUMN shared_catalogs.catalog_detail.status IS 'Estado del registro: 1=Activo, 0=Inactivo, -1=Pendiente revisión funcional';
COMMENT ON COLUMN shared_catalogs.catalog_detail.created_at IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN shared_catalogs.catalog_detail.updated_at IS 'Fecha y hora de última modificación';

CREATE INDEX IF NOT EXISTS idx_catalog_detail_header ON shared_catalogs.catalog_detail(header_id);
CREATE INDEX IF NOT EXISTS idx_catalog_detail_key ON shared_catalogs.catalog_detail(key);
CREATE INDEX IF NOT EXISTS idx_catalog_detail_dict ON shared_catalogs.catalog_detail(dict_id);
CREATE INDEX IF NOT EXISTS idx_catalog_detail_status ON shared_catalogs.catalog_detail(status);

-- ============================================================================
-- FIN DEL SCRIPT DE CREACIÓN
-- ============================================================================
