-- ============================================================================
-- JIRA: STM-1212
-- Tabla: cat_parameter
-- Modulo: Herramientas y Utilerias
-- Descripcion: Catalogo de parametros de configuracion con control de versiones
-- Fecha: 2025-12-08
-- Stack: PostgreSQL
-- Basado en: mrch.backend.somx.finanzas-api
-- ============================================================================

-- ============================================================================
-- PASO 1: Crear tabla cat_parameter
-- ============================================================================
CREATE TABLE cat_parameter (
    -- Llave primaria
    id_parameter        SERIAL PRIMARY KEY,

    -- Campos principales
    id_module           INTEGER NOT NULL,                                   -- Referencia a catalogo de modulos
    id_type             INTEGER NOT NULL,                                   -- Referencia a catalogo de tipos de parametro
    name                VARCHAR(50) NOT NULL,                               -- Nombre tecnico del parametro
    description         VARCHAR(250),                                       -- Descripcion funcional (nullable)
    value               VARCHAR(150) NOT NULL,                              -- Valor de configuracion
    version             DECIMAL(4,1) NOT NULL DEFAULT 1.0,                  -- Control de versiones (ej. 1.0, 1.1, 2.0)
    start_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,       -- Inicio de vigencia
    end_date            TIMESTAMP,                                          -- Fin de vigencia (NULL = indefinido)
    status              INTEGER NOT NULL DEFAULT 1,                         -- 1=Activo, 0=Inactivo

    -- Campos de auditoria (patron finanzas-api)
    created_by          BIGINT,                                             -- ID del usuario que registro
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,       -- Fecha de creacion
    updated_by          BIGINT,                                             -- ID del usuario que actualizo
    updated_at          TIMESTAMP                                           -- Fecha de actualizacion
);

-- ============================================================================
-- PASO 2: Comentarios en espanol
-- ============================================================================
COMMENT ON TABLE cat_parameter IS 'Catalogo de parametros de configuracion del sistema';
COMMENT ON COLUMN cat_parameter.id_parameter IS 'Identificador unico autoincremental';
COMMENT ON COLUMN cat_parameter.id_module IS 'Referencia al catalogo de modulos';
COMMENT ON COLUMN cat_parameter.id_type IS 'Referencia al catalogo de tipos de parametro';
COMMENT ON COLUMN cat_parameter.name IS 'Nombre tecnico del parametro';
COMMENT ON COLUMN cat_parameter.description IS 'Descripcion funcional del parametro';
COMMENT ON COLUMN cat_parameter.value IS 'Valor de configuracion';
COMMENT ON COLUMN cat_parameter.version IS 'Version del parametro (ej. 1.0, 1.1, 2.0)';
COMMENT ON COLUMN cat_parameter.start_date IS 'Fecha de inicio de vigencia';
COMMENT ON COLUMN cat_parameter.end_date IS 'Fecha fin de vigencia (NULL = indefinido)';
COMMENT ON COLUMN cat_parameter.status IS 'Estado: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN cat_parameter.created_by IS 'ID del usuario que registro';
COMMENT ON COLUMN cat_parameter.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN cat_parameter.updated_by IS 'ID del usuario que actualizo';
COMMENT ON COLUMN cat_parameter.updated_at IS 'Fecha de ultima actualizacion';

-- ============================================================================
-- PASO 3: Indices (segun JIRA STM-1212)
-- ============================================================================
CREATE INDEX idx_cat_parameter_name ON cat_parameter(name);
CREATE INDEX idx_cat_parameter_id_module ON cat_parameter(id_module);

-- ============================================================================
-- PASO 4: Constraint UNIQUE (segun JIRA STM-1212)
-- ============================================================================
ALTER TABLE cat_parameter
    ADD CONSTRAINT uk_cat_parameter_name_version UNIQUE (name, version);

-- ============================================================================
-- VERIFICACION: Mostrar estructura de la tabla
-- ============================================================================
-- SELECT column_name, data_type, character_maximum_length, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'cat_parameter'
-- ORDER BY ordinal_position;
