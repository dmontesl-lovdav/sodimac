-- =============================================================
-- Modelo Seguridad B2B Portal
-- Esquema: core_security
-- Basado en: ER "Modelo Seguridad" (sesiones/modelo ER.pdf)
-- Estándar: English snake_case, BIGSERIAL PKs, audit fields
--
-- Tablas moradas (cat_atributo, cat_perfil, cat_aplicativo,
-- cat_evento, perfil_aplicativo, perfil_aplicativo_evento,
-- aplicativo_evento) → pertenecen al módulo catalogos
-- (shared_catalogs o nuevo schema, pendiente decision)
--
-- Tablas azules → core_security
-- =============================================================

-- =============================================================
-- SCHEMA
-- =============================================================

CREATE SCHEMA IF NOT EXISTS core_security;

-- =============================================================
-- TABLAS CATÁLOGO (moradas) — módulo catalogos
-- Nota: si van en shared_catalogs reemplazar schema
-- =============================================================

-- Catálogo de atributos de seguridad
-- Ej: Proveedor, TipoProveedor, GrupoProveedor
CREATE TABLE IF NOT EXISTS core_security.cat_attribute (
    attribute_id    BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    status          INTEGER         NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    CONSTRAINT uq_cat_attribute_name UNIQUE (name)
);

-- Catálogo de perfiles de acceso
CREATE TABLE IF NOT EXISTS core_security.cat_profile (
    profile_id      BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    status          INTEGER         NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    CONSTRAINT uq_cat_profile_name UNIQUE (name)
);

-- Catálogo de aplicativos
CREATE TABLE IF NOT EXISTS core_security.cat_application (
    application_id  BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    status          INTEGER         NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP
);

-- Catálogo de eventos por aplicativo
CREATE TABLE IF NOT EXISTS core_security.cat_event (
    event_id        BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    status          INTEGER         NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP
);

-- Relación aplicativo → evento
CREATE TABLE IF NOT EXISTS core_security.application_event (
    application_event_id    BIGSERIAL   PRIMARY KEY,
    application_id          BIGINT      NOT NULL REFERENCES core_security.cat_application(application_id),
    event_id                BIGINT      NOT NULL REFERENCES core_security.cat_event(event_id),
    status                  INTEGER     NOT NULL DEFAULT 1,
    created_by              BIGINT,
    created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT,
    updated_at              TIMESTAMP,
    CONSTRAINT uq_application_event UNIQUE (application_id, event_id)
);

-- Perfil → Aplicativo
CREATE TABLE IF NOT EXISTS core_security.profile_application (
    profile_id      BIGINT      NOT NULL REFERENCES core_security.cat_profile(profile_id),
    application_id  BIGINT      NOT NULL REFERENCES core_security.cat_application(application_id),
    status          INTEGER     NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    PRIMARY KEY (profile_id, application_id)
);

-- Perfil → Aplicativo → Evento
CREATE TABLE IF NOT EXISTS core_security.profile_application_event (
    profile_id              BIGINT      NOT NULL REFERENCES core_security.cat_profile(profile_id),
    application_event_id    BIGINT      NOT NULL REFERENCES core_security.application_event(application_event_id),
    status                  INTEGER     NOT NULL DEFAULT 1,
    created_by              BIGINT,
    created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT,
    updated_at              TIMESTAMP,
    PRIMARY KEY (profile_id, application_event_id)
);

-- =============================================================
-- TABLAS PRINCIPALES (azules) — core_security
-- =============================================================

-- Catálogo de usuarios del sistema
-- user_id_external = sub/UUID de Keycloak
CREATE TABLE IF NOT EXISTS core_security.cat_user (
    user_id             BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(200)    NOT NULL,
    rfc                 VARCHAR(13),
    email               VARCHAR(255)    NOT NULL,
    user_id_external    VARCHAR(255),       -- sub de Keycloak
    status              INTEGER         NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT,
    updated_at          TIMESTAMP,
    CONSTRAINT uq_cat_user_email UNIQUE (email)
);

-- Catálogo de roles
CREATE TABLE IF NOT EXISTS core_security.cat_role (
    role_id     BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    status      INTEGER         NOT NULL DEFAULT 1,
    created_by  BIGINT,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT,
    updated_at  TIMESTAMP,
    CONSTRAINT uq_cat_role_name UNIQUE (name)
);

-- Catálogo de permisos
CREATE TABLE IF NOT EXISTS core_security.cat_permission (
    permission_id   BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(255),
    status          INTEGER         NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    CONSTRAINT uq_cat_permission_name UNIQUE (name)
);

-- Usuario → Rol (N:M)
CREATE TABLE IF NOT EXISTS core_security.user_role (
    user_id     BIGINT      NOT NULL REFERENCES core_security.cat_user(user_id),
    role_id     BIGINT      NOT NULL REFERENCES core_security.cat_role(role_id),
    status      INTEGER     NOT NULL DEFAULT 1,
    created_by  BIGINT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT,
    updated_at  TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- Rol → Permiso (N:M)
CREATE TABLE IF NOT EXISTS core_security.role_permission (
    role_id         BIGINT      NOT NULL REFERENCES core_security.cat_role(role_id),
    permission_id   BIGINT      NOT NULL REFERENCES core_security.cat_permission(permission_id),
    status          INTEGER     NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- Usuario → Perfil (N:M)
CREATE TABLE IF NOT EXISTS core_security.user_profile (
    user_id     BIGINT      NOT NULL REFERENCES core_security.cat_user(user_id),
    profile_id  BIGINT      NOT NULL REFERENCES core_security.cat_profile(profile_id),
    status      INTEGER     NOT NULL DEFAULT 1,
    created_by  BIGINT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT,
    updated_at  TIMESTAMP,
    PRIMARY KEY (user_id, profile_id)
);

-- Usuario → Atributo (cabecera: indica que el usuario tiene ese atributo)
CREATE TABLE IF NOT EXISTS core_security.user_attribute (
    user_id         BIGINT      NOT NULL REFERENCES core_security.cat_user(user_id),
    attribute_id    BIGINT      NOT NULL REFERENCES core_security.cat_attribute(attribute_id),
    value           VARCHAR(255) NOT NULL,
    status          INTEGER     NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    PRIMARY KEY (user_id, attribute_id, value)
);

-- Usuario → Atributo → Valores (múltiples valores por atributo)
-- Ej: user_id=5, attribute_id=1(Proveedor), value='1001'
--     user_id=5, attribute_id=1(Proveedor), value='1002'
--     user_id=5, attribute_id=2(TipoProveedor), value='-1' (todos)
CREATE TABLE IF NOT EXISTS core_security.user_attribute_value (
    user_id         BIGINT          NOT NULL REFERENCES core_security.cat_user(user_id),
    attribute_id    BIGINT          NOT NULL REFERENCES core_security.cat_attribute(attribute_id),
    value           VARCHAR(255)    NOT NULL,
    status          INTEGER         NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP,
    PRIMARY KEY (user_id, attribute_id, value)
);

-- =============================================================
-- ÍNDICES
-- =============================================================

CREATE INDEX IF NOT EXISTS idx_user_attribute_value_user    ON core_security.user_attribute_value (user_id);
CREATE INDEX IF NOT EXISTS idx_user_attribute_value_attr    ON core_security.user_attribute_value (attribute_id);
CREATE INDEX IF NOT EXISTS idx_user_attribute_value_status  ON core_security.user_attribute_value (status);
CREATE INDEX IF NOT EXISTS idx_cat_user_email               ON core_security.cat_user (email);
CREATE INDEX IF NOT EXISTS idx_cat_user_external            ON core_security.cat_user (user_id_external);

-- =============================================================
-- DATOS INICIALES — Atributos de seguridad para Jiras STM-1403
-- =============================================================

INSERT INTO core_security.cat_attribute (name, description, status, created_by)
VALUES
    ('Proveedor',      'Número de proveedor permitido para el usuario',       1, 1),
    ('TipoProveedor',  'Tipo de proveedor permitido (TPR001-TPR004 o -1)',    1, 1),
    ('GrupoProveedor', 'Grupo de proveedor permitido para el usuario',        1, 1)
ON CONFLICT (name) DO NOTHING;

-- =============================================================
-- VERIFICACIÓN POST-EJECUCIÓN
-- =============================================================
-- SELECT * FROM core_security.cat_attribute;
-- SELECT COUNT(*) FROM core_security.user_attribute_value;
--
-- Consulta tipo para filtrado de seguridad:
-- SELECT uav.value
-- FROM core_security.user_attribute_value uav
-- JOIN core_security.cat_attribute ca ON ca.attribute_id = uav.attribute_id
-- WHERE uav.user_id = :userId
--   AND ca.name = 'Proveedor'
--   AND uav.status = 1;
-- =============================================================
