-- ============================================================================
-- Rol Atributo: gestion de atributos por Rol (en sustitucion de Usuario Atributo)
-- Crea la tabla core_security.role_attribute replicando core_security.user_attribute
-- pero relacionando el atributo con un Rol (catalog_detail_role_id) en lugar de
-- un usuario. Idempotente.
-- ============================================================================
BEGIN;

CREATE TABLE IF NOT EXISTS core_security.role_attribute (
    role_attribute_id                 SERIAL PRIMARY KEY,
    catalog_detail_role_id            INTEGER      NOT NULL,
    catalog_detail_attribute_type_id  INTEGER      NOT NULL,
    catalog_detail_attribute_value_id INTEGER      NULL,
    status                            SMALLINT     NOT NULL DEFAULT 1,
    created_by                        VARCHAR(80)  NOT NULL DEFAULT 'SYSTEM',
    created_at                        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_by                        VARCHAR(80)  NULL,
    updated_at                        TIMESTAMP    NULL
);

CREATE INDEX IF NOT EXISTS idx_role_attribute_role
    ON core_security.role_attribute (catalog_detail_role_id);

CREATE INDEX IF NOT EXISTS idx_role_attribute_type
    ON core_security.role_attribute (catalog_detail_attribute_type_id);

COMMIT;
