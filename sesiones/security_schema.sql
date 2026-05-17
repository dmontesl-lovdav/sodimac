-- =============================================================================
-- Módulo seguridad: tablas core_security + relación a shared_catalogs
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) Tablas core_security
-- -----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS core_security;

-- Identidad de usuario.
-- Enlaza:
--   sub                -> identificador estable del proveedor de identidad
--   preferred_username -> nombre preferido / alias de login
--   given_name, family_name, email -> claims / perfil
CREATE TABLE IF NOT EXISTS core_security.user_data (
    user_data_id SERIAL PRIMARY KEY,
    sub VARCHAR(255) NOT NULL,
    preferred_username VARCHAR(255),
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    email VARCHAR(320),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_data_sub UNIQUE (sub),
    CONSTRAINT ck_user_data_status CHECK (status IN (0, 1))
);

CREATE INDEX IF NOT EXISTS idx_user_data_email
    ON core_security.user_data (email);
CREATE INDEX IF NOT EXISTS idx_user_data_preferred_username
    ON core_security.user_data (preferred_username);

-- Tabla puente de aplicativo/modulo y evento/proceso.
-- Enlaza dos elementos de shared_catalogs.catalog_detail:
--  - catalog_detail_module_id -> catalog_detail.id (tipo modulo/aplicativo)
--  - catalog_detail_process_id -> catalog_detail.id (tipo evento/proceso)
-- Se usa como base para asignar pares modulo-proceso a perfiles.
CREATE TABLE IF NOT EXISTS core_security.module_process (
    module_process_id SERIAL PRIMARY KEY,
    catalog_detail_module_id INTEGER NOT NULL,
    catalog_detail_process_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    CONSTRAINT ck_module_process_status CHECK (status IN (0, 1)),
    CONSTRAINT uq_module_process_module_process UNIQUE (catalog_detail_module_id, catalog_detail_process_id),
    CONSTRAINT fk_module_process_module
        FOREIGN KEY (catalog_detail_module_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_module_process_process
        FOREIGN KEY (catalog_detail_process_id) REFERENCES shared_catalogs.catalog_detail (id)
);

-- Asignacion de modulos/aplicativos a perfiles.
-- Enlaza:
--  - catalog_detail_profile_id -> shared_catalogs.catalog_detail.id (perfil)
--  - catalog_detail_module_id  -> shared_catalogs.catalog_detail.id (modulo/aplicativo)
-- La PK compuesta evita asignaciones duplicadas perfil-modulo.
CREATE TABLE IF NOT EXISTS core_security.profile_module (
    catalog_detail_profile_id INTEGER NOT NULL,
    catalog_detail_module_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    PRIMARY KEY (catalog_detail_profile_id, catalog_detail_module_id),
    CONSTRAINT ck_profile_module_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_profile_module_profile
        FOREIGN KEY (catalog_detail_profile_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_profile_module_module
        FOREIGN KEY (catalog_detail_module_id) REFERENCES shared_catalogs.catalog_detail (id)
);

-- Asignacion de usuarios a perfiles.
-- Enlaza:
--  - catalog_detail_profile_id -> shared_catalogs.catalog_detail.id (perfil)
--  - user_data_id              -> core_security.user_data.user_data_id
CREATE TABLE IF NOT EXISTS core_security.profile_user (
    catalog_detail_profile_id INTEGER NOT NULL,
    user_data_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    PRIMARY KEY (catalog_detail_profile_id, user_data_id),
    CONSTRAINT ck_profile_user_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_profile_user_profile
        FOREIGN KEY (catalog_detail_profile_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_profile_user_user_data
        FOREIGN KEY (user_data_id) REFERENCES core_security.user_data (user_data_id)
);

-- Asignacion de pares modulo-proceso a perfiles.
-- Enlaza:
--  - catalog_detail_profile_id -> shared_catalogs.catalog_detail.id (perfil)
--  - module_process_id         -> core_security.module_process.module_process_id
-- Permite granularidad por eventos dentro de cada aplicativo para un perfil.
CREATE TABLE IF NOT EXISTS core_security.profile_module_process (
    catalog_detail_profile_id INTEGER NOT NULL,
    module_process_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    PRIMARY KEY (catalog_detail_profile_id, module_process_id),
    CONSTRAINT ck_profile_module_process_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_profile_module_process_profile
        FOREIGN KEY (catalog_detail_profile_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_profile_module_process_module_process
        FOREIGN KEY (module_process_id) REFERENCES core_security.module_process (module_process_id)
);

-- Asignacion de usuarios a roles.
-- Enlaza:
--  - catalog_detail_role_id -> shared_catalogs.catalog_detail.id (rol)
--  - user_data_id           -> core_security.user_data.user_data_id
CREATE TABLE IF NOT EXISTS core_security.role_user (
    catalog_detail_role_id INTEGER NOT NULL,
    user_data_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    PRIMARY KEY (catalog_detail_role_id, user_data_id),
    CONSTRAINT ck_role_user_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_role_user_role
        FOREIGN KEY (catalog_detail_role_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_role_user_user_data
        FOREIGN KEY (user_data_id) REFERENCES core_security.user_data (user_data_id)
);

-- Asignacion de permisos a roles.
-- Enlaza:
--  - catalog_detail_role_id       -> shared_catalogs.catalog_detail.id (rol)
--  - catalog_detail_permission_id -> shared_catalogs.catalog_detail.id (permiso)
-- Define las acciones permitidas por cada rol.
CREATE TABLE IF NOT EXISTS core_security.role_permission (
    catalog_detail_role_id INTEGER NOT NULL,
    catalog_detail_permission_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    PRIMARY KEY (catalog_detail_role_id, catalog_detail_permission_id),
    CONSTRAINT ck_role_permission_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (catalog_detail_role_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (catalog_detail_permission_id) REFERENCES shared_catalogs.catalog_detail (id)
);

-- Asignacion de proveedores a roles.
-- Enlaza:
--  - catalog_detail_role_id     -> shared_catalogs.catalog_detail.id (rol)
--  - catalog_detail_provider_id -> shared_catalogs.catalog_detail.id (proveedor)
-- Permite segmentar alcance de un rol por proveedor.
CREATE TABLE IF NOT EXISTS core_security.role_provider (
    catalog_detail_role_id INTEGER NOT NULL,
    catalog_detail_provider_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    PRIMARY KEY (catalog_detail_role_id, catalog_detail_provider_id),
    CONSTRAINT ck_role_provider_status CHECK (status IN (0, 1)),
    CONSTRAINT fk_role_provider_role
        FOREIGN KEY (catalog_detail_role_id) REFERENCES shared_catalogs.catalog_detail (id),
    CONSTRAINT fk_role_provider_provider
        FOREIGN KEY (catalog_detail_provider_id) REFERENCES shared_catalogs.catalog_detail (id)
);

-- Asignacion de atributos a usuarios.
-- Enlaza:
--  - user_data_id                     -> core_security.user_data.user_data_id
--  - catalog_detail_attribute_type_id -> shared_catalogs.catalog_detail.id (tipo de atributo)
-- Evita repetir el mismo tipo de atributo para un usuario.
CREATE TABLE IF NOT EXISTS core_security.user_attribute (
    user_attribute_id SERIAL PRIMARY KEY,
    user_data_id INTEGER NOT NULL,
    catalog_detail_attribute_type_id INTEGER NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    created_by VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(80),
    updated_at TIMESTAMP,
    CONSTRAINT ck_user_attribute_status CHECK (status IN (0, 1)),
    CONSTRAINT uq_user_attribute_user_attribute_type UNIQUE (user_data_id, catalog_detail_attribute_type_id),
    CONSTRAINT fk_user_attribute_user_data
        FOREIGN KEY (user_data_id) REFERENCES core_security.user_data (user_data_id),
    CONSTRAINT fk_user_attribute_attribute_type
        FOREIGN KEY (catalog_detail_attribute_type_id) REFERENCES shared_catalogs.catalog_detail (id)
);

CREATE INDEX IF NOT EXISTS idx_module_process_module
    ON core_security.module_process (catalog_detail_module_id);
CREATE INDEX IF NOT EXISTS idx_module_process_process
    ON core_security.module_process (catalog_detail_process_id);
CREATE INDEX IF NOT EXISTS idx_profile_module_profile
    ON core_security.profile_module (catalog_detail_profile_id);
CREATE INDEX IF NOT EXISTS idx_profile_module_module
    ON core_security.profile_module (catalog_detail_module_id);
CREATE INDEX IF NOT EXISTS idx_profile_user_user
    ON core_security.profile_user (user_data_id);
CREATE INDEX IF NOT EXISTS idx_profile_module_process_module_process
    ON core_security.profile_module_process (module_process_id);
CREATE INDEX IF NOT EXISTS idx_role_user_user
    ON core_security.role_user (user_data_id);
CREATE INDEX IF NOT EXISTS idx_role_permission_permission
    ON core_security.role_permission (catalog_detail_permission_id);
CREATE INDEX IF NOT EXISTS idx_role_provider_provider
    ON core_security.role_provider (catalog_detail_provider_id);
CREATE INDEX IF NOT EXISTS idx_user_attribute_user
    ON core_security.user_attribute (user_data_id);
CREATE INDEX IF NOT EXISTS idx_user_attribute_attribute_type
    ON core_security.user_attribute (catalog_detail_attribute_type_id);

-- -----------------------------------------------------------------------------
-- Datos de ejemplo: usuarios
-- -----------------------------------------------------------------------------
INSERT INTO core_security.user_data (user_data_id, sub, preferred_username, given_name, family_name, email, status)
VALUES
    (1, 'sb000001', 'USR_FERNANDO', 'Fernando', 'Perez', 'fernando.perez@example.com', 1),
    (2, 'sb000002', 'USR_ANA', 'Ana', 'Martinez', 'ana.martinez@example.com', 1),
    (3, 'sb000003', 'USR_JOSE', 'Jose', 'Ramirez', 'jose.ramirez@example.com', 1),
    (4, 'sb000004', 'USR_INACTIVO', 'Usuario', 'Inactivo', 'inactivo@example.com', 0),
    (5, 'sb000005', 'zedlav.sd18@gmail.com', 'Iván', 'Cortés', 'ivan@gmail.com', 1)
ON CONFLICT (user_data_id) DO NOTHING;

-- Ajustar secuencia SERIAL
SELECT setval(
    pg_get_serial_sequence('core_security.user_data', 'user_data_id'),
    (SELECT COALESCE(MAX(user_data_id), 1) FROM core_security.user_data)
);
