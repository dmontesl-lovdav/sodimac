-- ============================================================================
-- UTILS-API - Script Consolidado de Esquema y Tablas
-- Esquema: core_utils
-- Descripcion: Script unificado para crear todas las tablas del modulo utils
-- Fecha: 2025-12-22
--
-- NOTA: Este script maneja errores gracefully - si una tabla/columna ya existe,
--       continua con el siguiente elemento sin fallar.
-- ============================================================================

-- ============================================================================
-- CREAR ESQUEMA
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS core_utils;

SET search_path TO core_utils;

-- ============================================================================
-- TABLA: cat_module (Catalogo de Modulos)
-- Descripcion: Catalogo de modulos del sistema
-- ============================================================================
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

CREATE INDEX IF NOT EXISTS idx_cat_module_name ON core_utils.cat_module(name);

-- ============================================================================
-- TABLA: cat_item_type (Catalogo de Tipos de Elemento)
-- Descripcion: Catalogo de tipos de elemento
-- ============================================================================
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

CREATE INDEX IF NOT EXISTS idx_cat_item_type_name ON core_utils.cat_item_type(name);

-- ============================================================================
-- TABLA: cat_item (Catalogo de Elementos)
-- Descripcion: Catalogo de elementos
-- ============================================================================
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

CREATE INDEX IF NOT EXISTS idx_cat_item_name ON core_utils.cat_item(name);

-- ============================================================================
-- TABLA: cat_process (Catalogo de Procesos)
-- Descripcion: Catalogo de procesos del sistema
-- ============================================================================
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

CREATE INDEX IF NOT EXISTS idx_cat_process_name ON core_utils.cat_process(name);

-- ============================================================================
-- TABLA: cat_message (Catalogo de Mensajes)
-- Descripcion: Catalogo de mensajes del sistema
-- ============================================================================
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

CREATE INDEX IF NOT EXISTS idx_cat_message_code ON core_utils.cat_message(message_code);
CREATE INDEX IF NOT EXISTS idx_cat_message_type ON core_utils.cat_message(id_message_type);

-- ============================================================================
-- TABLA: application_msg (Mensajes por Aplicativo)
-- Descripcion: Relacion entre mensajes y aplicativos
-- ============================================================================
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

CREATE INDEX IF NOT EXISTS idx_application_msg_app ON core_utils.application_msg(id_application);
CREATE INDEX IF NOT EXISTS idx_application_msg_message ON core_utils.application_msg(id_message);

-- ============================================================================
-- TABLA: cat_parameter (Catalogo de Parametros)
-- Descripcion: Catalogo de parametros de configuracion
-- ============================================================================
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

COMMENT ON TABLE core_utils.cat_parameter IS 'Catalogo de parametros de configuracion';

CREATE INDEX IF NOT EXISTS idx_cat_parameter_name ON core_utils.cat_parameter(name);
CREATE INDEX IF NOT EXISTS idx_cat_parameter_module ON core_utils.cat_parameter(id_module);
CREATE INDEX IF NOT EXISTS idx_cat_parameter_status ON core_utils.cat_parameter(status);

-- ============================================================================
-- VERIFICACION DE TABLAS CREADAS
-- ============================================================================
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'core_utils'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- ============================================================================
-- FIN DEL SCRIPT DE ESQUEMA CONSOLIDADO
-- ============================================================================
