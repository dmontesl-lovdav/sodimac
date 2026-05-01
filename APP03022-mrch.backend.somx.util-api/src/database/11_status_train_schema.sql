-- ============================================================================
-- STM-1166: Tren de Estatus - Creación de Tabla
-- Esquema: shared_catalogs
-- Descripción: Configuración de transiciones de estatus permitidas por opción.
--              Define qué cambios de estatus son válidos para cada tipo de documento.
-- ============================================================================

-- ============================================================================
-- Tabla: status_train
-- Descripción: Almacena las reglas de transición de estatus permitidas.
--              Cada registro define un par origen->destino válido para una opción.
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.status_train (
    id SERIAL PRIMARY KEY,                              -- Identificador único autoincremental
    option_id INTEGER NOT NULL,                         -- Identificador de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)
    source_status INTEGER NOT NULL,                     -- Estatus origen de la transición
    target_status INTEGER NOT NULL,                     -- Estatus destino permitido
    created_by BIGINT NOT NULL,                         -- Usuario que registró la regla
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha de creación
    updated_by BIGINT,                                  -- Usuario de última actualización
    updated_at TIMESTAMP,                               -- Fecha de última actualización
    CONSTRAINT uk_status_train UNIQUE (option_id, source_status, target_status)
);

-- Comentarios de tabla y columnas
COMMENT ON TABLE shared_catalogs.status_train IS 'Configuración del tren de estatus: transiciones permitidas por opción/tipo de documento';
COMMENT ON COLUMN shared_catalogs.status_train.id IS 'Identificador único autoincremental';
COMMENT ON COLUMN shared_catalogs.status_train.option_id IS 'Opción/funcionalidad: 1=Factura(I), 2=NotaCredito(E), 3=ComplementoPago(P), 4=CartaPorte(T)';
COMMENT ON COLUMN shared_catalogs.status_train.source_status IS 'Estatus origen desde el cual se permite la transición';
COMMENT ON COLUMN shared_catalogs.status_train.target_status IS 'Estatus destino al cual se permite transicionar';
COMMENT ON COLUMN shared_catalogs.status_train.created_by IS 'ID del usuario que creó el registro';
COMMENT ON COLUMN shared_catalogs.status_train.created_at IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN shared_catalogs.status_train.updated_by IS 'ID del usuario que realizó la última actualización';
COMMENT ON COLUMN shared_catalogs.status_train.updated_at IS 'Fecha y hora de última modificación';

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_status_train_option ON shared_catalogs.status_train(option_id);
CREATE INDEX IF NOT EXISTS idx_status_train_source ON shared_catalogs.status_train(option_id, source_status);

-- ============================================================================
-- FIN DEL SCRIPT DE CREACIÓN
-- ============================================================================
