-- =====================================================
-- STM-1212: Script de Creacion de Esquema y Tablas
-- Modulo: Herramientas y Utilerias
-- Base de datos: PostgreSQL
-- Esquema: core_utils
-- Fecha: 2025-12-15
-- =====================================================

-- =====================================================
-- CREAR ESQUEMA
-- =====================================================
CREATE SCHEMA IF NOT EXISTS core_utils;

SET search_path TO core_utils;

-- =====================================================
-- TABLA: cat_module (Catalogo de Modulos)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.cat_module (
    id_module SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP
);

COMMENT ON TABLE core_utils.cat_module IS 'Catalogo de modulos del sistema';
COMMENT ON COLUMN core_utils.cat_module.id_module IS 'Identificador unico del modulo';
COMMENT ON COLUMN core_utils.cat_module.name IS 'Nombre del modulo';
COMMENT ON COLUMN core_utils.cat_module.description IS 'Descripcion del modulo';

CREATE INDEX IF NOT EXISTS idx_cat_module_name ON core_utils.cat_module(name);

-- =====================================================
-- TABLA: cat_item_type (Catalogo de Tipos de Elemento)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.cat_item_type (
    id_item_type SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP
);

COMMENT ON TABLE core_utils.cat_item_type IS 'Catalogo de tipos de elemento';
COMMENT ON COLUMN core_utils.cat_item_type.id_item_type IS 'Identificador unico del tipo de elemento';
COMMENT ON COLUMN core_utils.cat_item_type.name IS 'Nombre del tipo de elemento';

CREATE INDEX IF NOT EXISTS idx_cat_item_type_name ON core_utils.cat_item_type(name);

-- =====================================================
-- TABLA: cat_item (Catalogo de Elementos)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.cat_item (
    id_item SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP
);

COMMENT ON TABLE core_utils.cat_item IS 'Catalogo de elementos';
COMMENT ON COLUMN core_utils.cat_item.id_item IS 'Identificador unico del elemento';
COMMENT ON COLUMN core_utils.cat_item.name IS 'Nombre del elemento';

CREATE INDEX IF NOT EXISTS idx_cat_item_name ON core_utils.cat_item(name);

-- =====================================================
-- TABLA: cat_process (Catalogo de Procesos)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.cat_process (
    id_process SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP
);

COMMENT ON TABLE core_utils.cat_process IS 'Catalogo de procesos del sistema';
COMMENT ON COLUMN core_utils.cat_process.id_process IS 'Identificador unico del proceso';
COMMENT ON COLUMN core_utils.cat_process.name IS 'Nombre del proceso';

CREATE INDEX IF NOT EXISTS idx_cat_process_name ON core_utils.cat_process(name);

-- =====================================================
-- TABLA: cat_message (Catalogo de Mensajes)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.cat_message (
    id_message SERIAL PRIMARY KEY,
    message_code VARCHAR(20) NOT NULL,
    id_message_type INTEGER NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP,
    CONSTRAINT uk_cat_message_code UNIQUE (message_code)
);

COMMENT ON TABLE core_utils.cat_message IS 'Catalogo de mensajes del sistema';
COMMENT ON COLUMN core_utils.cat_message.id_message IS 'Identificador unico del mensaje';
COMMENT ON COLUMN core_utils.cat_message.message_code IS 'Codigo unico del mensaje (ERR001, BUS001, etc.)';
COMMENT ON COLUMN core_utils.cat_message.id_message_type IS 'Tipo de mensaje: 1=Error, 2=Warning, 3=Info, 4=Success';
COMMENT ON COLUMN core_utils.cat_message.description IS 'Descripcion/texto del mensaje';

CREATE INDEX IF NOT EXISTS idx_cat_message_code ON core_utils.cat_message(message_code);
CREATE INDEX IF NOT EXISTS idx_cat_message_type ON core_utils.cat_message(id_message_type);

-- =====================================================
-- TABLA: application_msg (Mensajes por Aplicativo)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.application_msg (
    id_application_msg SERIAL PRIMARY KEY,
    id_message INTEGER NOT NULL,
    id_application INTEGER NOT NULL,
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP,
    CONSTRAINT uk_application_msg UNIQUE (id_message, id_application),
    CONSTRAINT fk_application_msg_message FOREIGN KEY (id_message)
        REFERENCES core_utils.cat_message(id_message) ON DELETE CASCADE
);

COMMENT ON TABLE core_utils.application_msg IS 'Relacion entre mensajes y aplicativos';
COMMENT ON COLUMN core_utils.application_msg.id_message IS 'FK al mensaje en cat_message';
COMMENT ON COLUMN core_utils.application_msg.id_application IS 'ID del aplicativo: 1=Portal Web, 2=API REST, 3=Batch, 4=Mobile';

CREATE INDEX IF NOT EXISTS idx_application_msg_app ON core_utils.application_msg(id_application);
CREATE INDEX IF NOT EXISTS idx_application_msg_message ON core_utils.application_msg(id_message);

-- =====================================================
-- TABLA: cat_parameter (Catalogo de Parametros)
-- =====================================================
CREATE TABLE IF NOT EXISTS core_utils.cat_parameter (
    id_parameter SERIAL PRIMARY KEY,
    id_module INTEGER,
    id_type INTEGER,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    value VARCHAR(1000),
    version DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 1,
    created_by INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER,
    updated_at TIMESTAMP,
    CONSTRAINT uk_cat_parameter_name_version UNIQUE (name, version),
    CONSTRAINT fk_cat_parameter_module FOREIGN KEY (id_module)
        REFERENCES core_utils.cat_module(id_module) ON DELETE SET NULL
);

COMMENT ON TABLE core_utils.cat_parameter IS 'Catalogo de parametros de configuracion del sistema';
COMMENT ON COLUMN core_utils.cat_parameter.id_parameter IS 'Identificador unico del parametro';
COMMENT ON COLUMN core_utils.cat_parameter.id_module IS 'FK al modulo al que pertenece';
COMMENT ON COLUMN core_utils.cat_parameter.id_type IS 'Tipo de parametro';
COMMENT ON COLUMN core_utils.cat_parameter.name IS 'Nombre del parametro';
COMMENT ON COLUMN core_utils.cat_parameter.value IS 'Valor del parametro';
COMMENT ON COLUMN core_utils.cat_parameter.version IS 'Version del parametro';
COMMENT ON COLUMN core_utils.cat_parameter.status IS 'Estado: 1=Activo, 0=Inactivo';

CREATE INDEX IF NOT EXISTS idx_cat_parameter_name ON core_utils.cat_parameter(name);
CREATE INDEX IF NOT EXISTS idx_cat_parameter_module ON core_utils.cat_parameter(id_module);
CREATE INDEX IF NOT EXISTS idx_cat_parameter_status ON core_utils.cat_parameter(status);

-- =====================================================
-- VERIFICACION DE TABLAS CREADAS
-- =====================================================
SELECT table_name, table_schema
FROM information_schema.tables
WHERE table_schema = 'core_utils'
ORDER BY table_name;

-- =====================================================
-- FIN DEL SCRIPT DE CREACION
-- =====================================================
