-- ============================================================================
-- Recrear schema shared_catalogs desde cero
-- DDL extraido de local (sodimac-pg / b2b_portal) el 2026-04-23
-- ============================================================================

-- ----------------------------------------------------------------------------
-- DROP (CASCADE resuelve dependencias)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS shared_catalogs.catalog_conversion       CASCADE;
DROP TABLE IF EXISTS shared_catalogs.catalog_detail_relation  CASCADE;
DROP TABLE IF EXISTS shared_catalogs.catalog_detail           CASCADE;
DROP TABLE IF EXISTS shared_catalogs.catalog_header           CASCADE;
DROP TABLE IF EXISTS shared_catalogs.dictionary_lang          CASCADE;
DROP TABLE IF EXISTS shared_catalogs.supplier                 CASCADE;
DROP TABLE IF EXISTS shared_catalogs.supplier_block           CASCADE;
DROP TABLE IF EXISTS shared_catalogs.supplier_type            CASCADE;
DROP TABLE IF EXISTS shared_catalogs.payment_condition        CASCADE;
DROP TABLE IF EXISTS shared_catalogs.status_train             CASCADE;

DROP SEQUENCE IF EXISTS shared_catalogs.catalog_conversion_id_seq     CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.catalog_detail_id_seq         CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.catalog_detail_relation_id_seq CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.catalog_header_id_seq         CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.dictionary_lang_id_seq        CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.payment_condition_id_seq      CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.status_train_id_seq           CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.supplier_id_seq               CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.supplier_block_id_seq         CASCADE;
DROP SEQUENCE IF EXISTS shared_catalogs.supplier_type_id_seq          CASCADE;

-- ----------------------------------------------------------------------------
-- SCHEMA
-- ----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS shared_catalogs;

SET default_table_access_method = heap;

-- ----------------------------------------------------------------------------
-- catalog_conversion
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.catalog_conversion (
    id                integer                      NOT NULL,
    source_element_id integer                      NOT NULL,
    target_element_id integer                      NOT NULL,
    valid_from        date,
    valid_to          date,
    status            integer  DEFAULT 1           NOT NULL,
    is_principal      boolean  DEFAULT false        NOT NULL,
    created_by        character varying(100),
    updated_by        character varying(100),
    created_at        timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at        timestamp without time zone
);

CREATE SEQUENCE shared_catalogs.catalog_conversion_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.catalog_conversion_id_seq OWNED BY shared_catalogs.catalog_conversion.id;
ALTER TABLE ONLY shared_catalogs.catalog_conversion ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.catalog_conversion_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- catalog_detail
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.catalog_detail (
    id                integer                      NOT NULL,
    header_id         integer                      NOT NULL,
    key               character varying(64)        NOT NULL,
    dict_id           integer                      NOT NULL,
    color             character varying(16),
    sort_order        integer  DEFAULT 0           NOT NULL,
    status            integer  DEFAULT 1           NOT NULL,
    created_at        timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at        timestamp without time zone,
    internal_status   integer,
    external_key      character varying(50),
    value             character varying(100),
    valid_from        date,
    valid_to          date,
    created_by        character varying(100),
    attributes        jsonb,
    parent_catalog_id integer,
    parent_element_id integer,
    updated_by        character varying(100)
);

CREATE SEQUENCE shared_catalogs.catalog_detail_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.catalog_detail_id_seq OWNED BY shared_catalogs.catalog_detail.id;
ALTER TABLE ONLY shared_catalogs.catalog_detail ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.catalog_detail_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- catalog_detail_relation
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.catalog_detail_relation (
    id               integer                      NOT NULL,
    source_detail_id integer                      NOT NULL,
    target_detail_id integer                      NOT NULL,
    relation_type    character varying(20) DEFAULT 'DEPENDS_ON'::character varying NOT NULL,
    status           integer  DEFAULT 1           NOT NULL,
    created_at       timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by       character varying(100),
    valid_from       date,
    valid_to         date,
    updated_by       character varying(100),
    updated_at       timestamp without time zone
);

CREATE SEQUENCE shared_catalogs.catalog_detail_relation_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.catalog_detail_relation_id_seq OWNED BY shared_catalogs.catalog_detail_relation.id;
ALTER TABLE ONLY shared_catalogs.catalog_detail_relation ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.catalog_detail_relation_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- catalog_header
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.catalog_header (
    id           integer                      NOT NULL,
    code         character varying(64)        NOT NULL,
    prefix       character varying(4)         NOT NULL,
    name         character varying(128)       NOT NULL,
    description  character varying(512),
    module       character varying(32),
    status       integer  DEFAULT 1           NOT NULL,
    created_at   timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   timestamp without time zone,
    catalog_type character varying(20) DEFAULT 'SIMPLE'::character varying NOT NULL,
    created_by   character varying(100),
    updated_by   character varying(100)
);

CREATE SEQUENCE shared_catalogs.catalog_header_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.catalog_header_id_seq OWNED BY shared_catalogs.catalog_header.id;
ALTER TABLE ONLY shared_catalogs.catalog_header ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.catalog_header_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- dictionary_lang
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.dictionary_lang (
    id          integer                NOT NULL,
    dict_id     integer                NOT NULL,
    lang_id     integer                NOT NULL,
    description character varying(512) NOT NULL
);

CREATE SEQUENCE shared_catalogs.dictionary_lang_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.dictionary_lang_id_seq OWNED BY shared_catalogs.dictionary_lang.id;
ALTER TABLE ONLY shared_catalogs.dictionary_lang ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.dictionary_lang_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- payment_condition
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.payment_condition (
    id              integer                      NOT NULL,
    supplier_number character varying(20),
    condition_name  character varying(100)       NOT NULL,
    days            integer  DEFAULT 0           NOT NULL,
    status          integer  DEFAULT 1           NOT NULL,
    created_at      timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by      character varying(100),
    updated_at      timestamp without time zone,
    updated_by      character varying(100)
);

CREATE SEQUENCE shared_catalogs.payment_condition_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.payment_condition_id_seq OWNED BY shared_catalogs.payment_condition.id;
ALTER TABLE ONLY shared_catalogs.payment_condition ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.payment_condition_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- status_train
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.status_train (
    id            integer                      NOT NULL,
    option_id     integer                      NOT NULL,
    source_status integer                      NOT NULL,
    target_status integer                      NOT NULL,
    created_by    bigint                       NOT NULL,
    created_at    timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by    bigint,
    updated_at    timestamp without time zone
);

CREATE SEQUENCE shared_catalogs.status_train_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.status_train_id_seq OWNED BY shared_catalogs.status_train.id;
ALTER TABLE ONLY shared_catalogs.status_train ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.status_train_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- supplier_type
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.supplier_type (
    id          integer                      NOT NULL,
    code        character varying(20)        NOT NULL,
    description character varying(100)       NOT NULL,
    status      integer  DEFAULT 1           NOT NULL,
    created_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by  character varying(100),
    updated_at  timestamp without time zone,
    updated_by  character varying(100)
);

CREATE SEQUENCE shared_catalogs.supplier_type_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.supplier_type_id_seq OWNED BY shared_catalogs.supplier_type.id;
ALTER TABLE ONLY shared_catalogs.supplier_type ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.supplier_type_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- supplier
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.supplier (
    id                   integer                      NOT NULL,
    supplier_number      character varying(20)        NOT NULL,
    rfc                  character varying(13)        NOT NULL,
    business_name        character varying(255)       NOT NULL,
    supplier_type_id     integer,
    logo                 character varying(500),
    payment_condition_id integer,
    status               integer  DEFAULT 1           NOT NULL,
    created_at           timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by           character varying(100),
    updated_at           timestamp without time zone,
    updated_by           character varying(100),
    email_financial      character varying(255)
);

CREATE SEQUENCE shared_catalogs.supplier_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.supplier_id_seq OWNED BY shared_catalogs.supplier.id;
ALTER TABLE ONLY shared_catalogs.supplier ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.supplier_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- supplier_block
-- ----------------------------------------------------------------------------
CREATE TABLE shared_catalogs.supplier_block (
    id              integer                      NOT NULL,
    supplier_number character varying(20)        NOT NULL,
    valid_from      date                         NOT NULL,
    valid_to        date                         NOT NULL,
    block_reason    character varying(255),
    status          integer  DEFAULT 1           NOT NULL,
    created_at      timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by      character varying(100),
    updated_at      timestamp without time zone,
    updated_by      character varying(100),
    CONSTRAINT chk_supplier_block_dates CHECK ((valid_to >= valid_from))
);

CREATE SEQUENCE shared_catalogs.supplier_block_id_seq
    AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE shared_catalogs.supplier_block_id_seq OWNED BY shared_catalogs.supplier_block.id;
ALTER TABLE ONLY shared_catalogs.supplier_block ALTER COLUMN id SET DEFAULT nextval('shared_catalogs.supplier_block_id_seq'::regclass);

-- ----------------------------------------------------------------------------
-- PRIMARY KEYS
-- ----------------------------------------------------------------------------
ALTER TABLE ONLY shared_catalogs.catalog_conversion     ADD CONSTRAINT catalog_conversion_pkey     PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.catalog_detail         ADD CONSTRAINT catalog_detail_pkey         PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.catalog_detail_relation ADD CONSTRAINT catalog_detail_relation_pkey PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.catalog_header         ADD CONSTRAINT catalog_header_pkey         PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.dictionary_lang        ADD CONSTRAINT dictionary_lang_pkey        PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.payment_condition      ADD CONSTRAINT payment_condition_pkey      PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.status_train           ADD CONSTRAINT status_train_pkey           PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.supplier               ADD CONSTRAINT supplier_pkey               PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.supplier_block         ADD CONSTRAINT supplier_block_pkey         PRIMARY KEY (id);
ALTER TABLE ONLY shared_catalogs.supplier_type          ADD CONSTRAINT supplier_type_pkey          PRIMARY KEY (id);

-- ----------------------------------------------------------------------------
-- UNIQUE CONSTRAINTS
-- ----------------------------------------------------------------------------
ALTER TABLE ONLY shared_catalogs.catalog_header          ADD CONSTRAINT catalog_header_code_key    UNIQUE (code);
ALTER TABLE ONLY shared_catalogs.catalog_detail          ADD CONSTRAINT uk_catalog_detail           UNIQUE (header_id, key);
ALTER TABLE ONLY shared_catalogs.catalog_detail_relation ADD CONSTRAINT uk_detail_relation          UNIQUE (source_detail_id, target_detail_id, relation_type);
ALTER TABLE ONLY shared_catalogs.dictionary_lang         ADD CONSTRAINT uk_dictionary_lang          UNIQUE (dict_id, lang_id);
ALTER TABLE ONLY shared_catalogs.status_train            ADD CONSTRAINT uk_status_train             UNIQUE (option_id, source_status, target_status);
ALTER TABLE ONLY shared_catalogs.supplier                ADD CONSTRAINT supplier_supplier_number_key UNIQUE (supplier_number);
ALTER TABLE ONLY shared_catalogs.supplier_type           ADD CONSTRAINT supplier_type_code_key      UNIQUE (code);

-- ----------------------------------------------------------------------------
-- FOREIGN KEYS
-- ----------------------------------------------------------------------------
ALTER TABLE ONLY shared_catalogs.catalog_detail
    ADD CONSTRAINT fk_catalog_detail_header FOREIGN KEY (header_id) REFERENCES shared_catalogs.catalog_header(id);

ALTER TABLE ONLY shared_catalogs.catalog_detail_relation
    ADD CONSTRAINT fk_relation_source FOREIGN KEY (source_detail_id) REFERENCES shared_catalogs.catalog_detail(id) ON DELETE CASCADE;

ALTER TABLE ONLY shared_catalogs.catalog_detail_relation
    ADD CONSTRAINT fk_relation_target FOREIGN KEY (target_detail_id) REFERENCES shared_catalogs.catalog_detail(id) ON DELETE CASCADE;

ALTER TABLE ONLY shared_catalogs.catalog_conversion
    ADD CONSTRAINT catalog_conversion_source_element_id_fkey FOREIGN KEY (source_element_id) REFERENCES shared_catalogs.catalog_detail(id);

ALTER TABLE ONLY shared_catalogs.catalog_conversion
    ADD CONSTRAINT catalog_conversion_target_element_id_fkey FOREIGN KEY (target_element_id) REFERENCES shared_catalogs.catalog_detail(id);

ALTER TABLE ONLY shared_catalogs.supplier
    ADD CONSTRAINT fk_supplier_type FOREIGN KEY (supplier_type_id) REFERENCES shared_catalogs.supplier_type(id);

ALTER TABLE ONLY shared_catalogs.supplier
    ADD CONSTRAINT fk_supplier_payment_condition FOREIGN KEY (payment_condition_id) REFERENCES shared_catalogs.payment_condition(id);

-- ----------------------------------------------------------------------------
-- INDEXES
-- ----------------------------------------------------------------------------
CREATE INDEX idx_catalog_header_code             ON shared_catalogs.catalog_header           USING btree (code);
CREATE INDEX idx_catalog_header_module           ON shared_catalogs.catalog_header           USING btree (module);
CREATE INDEX idx_catalog_header_status           ON shared_catalogs.catalog_header           USING btree (status);
CREATE INDEX idx_catalog_header_type             ON shared_catalogs.catalog_header           USING btree (catalog_type);

CREATE INDEX idx_catalog_detail_header           ON shared_catalogs.catalog_detail           USING btree (header_id);
CREATE INDEX idx_catalog_detail_key              ON shared_catalogs.catalog_detail           USING btree (key);
CREATE INDEX idx_catalog_detail_dict             ON shared_catalogs.catalog_detail           USING btree (dict_id);
CREATE INDEX idx_catalog_detail_status           ON shared_catalogs.catalog_detail           USING btree (status);
CREATE INDEX idx_catalog_detail_external_key     ON shared_catalogs.catalog_detail           USING btree (external_key);
CREATE INDEX idx_catalog_detail_valid_from       ON shared_catalogs.catalog_detail           USING btree (valid_from);
CREATE INDEX idx_catalog_detail_valid_to         ON shared_catalogs.catalog_detail           USING btree (valid_to);
CREATE INDEX idx_catalog_detail_parent_catalog_id ON shared_catalogs.catalog_detail          USING btree (parent_catalog_id);
CREATE INDEX idx_catalog_detail_parent_element_id ON shared_catalogs.catalog_detail          USING btree (parent_element_id);
CREATE INDEX idx_catalog_detail_attributes       ON shared_catalogs.catalog_detail           USING gin   (attributes);

CREATE INDEX idx_relation_source                 ON shared_catalogs.catalog_detail_relation  USING btree (source_detail_id);
CREATE INDEX idx_relation_target                 ON shared_catalogs.catalog_detail_relation  USING btree (target_detail_id);
CREATE INDEX idx_relation_type                   ON shared_catalogs.catalog_detail_relation  USING btree (relation_type);
CREATE INDEX idx_relation_status                 ON shared_catalogs.catalog_detail_relation  USING btree (status);
CREATE INDEX idx_relation_source_active          ON shared_catalogs.catalog_detail_relation  USING btree (source_detail_id, status) WHERE (status = 1);

CREATE INDEX idx_dictionary_dict_id              ON shared_catalogs.dictionary_lang          USING btree (dict_id);
CREATE INDEX idx_dictionary_lang_id              ON shared_catalogs.dictionary_lang          USING btree (lang_id);

CREATE INDEX idx_conv_source                     ON shared_catalogs.catalog_conversion       USING btree (source_element_id);
CREATE INDEX idx_conv_target                     ON shared_catalogs.catalog_conversion       USING btree (target_element_id);

CREATE INDEX idx_supplier_number                 ON shared_catalogs.supplier                 USING btree (supplier_number);
CREATE INDEX idx_supplier_rfc                    ON shared_catalogs.supplier                 USING btree (rfc);
CREATE INDEX idx_supplier_status                 ON shared_catalogs.supplier                 USING btree (status);
CREATE INDEX idx_supplier_type                   ON shared_catalogs.supplier                 USING btree (supplier_type_id);

CREATE INDEX idx_supplier_type_code              ON shared_catalogs.supplier_type            USING btree (code);
CREATE INDEX idx_supplier_type_status            ON shared_catalogs.supplier_type            USING btree (status);

CREATE INDEX idx_payment_condition_supplier      ON shared_catalogs.payment_condition        USING btree (supplier_number);
CREATE INDEX idx_payment_condition_status        ON shared_catalogs.payment_condition        USING btree (status);

CREATE INDEX idx_supplier_block_number           ON shared_catalogs.supplier_block           USING btree (supplier_number);
CREATE INDEX idx_supplier_block_status           ON shared_catalogs.supplier_block           USING btree (status);
CREATE INDEX idx_supplier_block_dates            ON shared_catalogs.supplier_block           USING btree (valid_from, valid_to);

CREATE INDEX idx_status_train_option             ON shared_catalogs.status_train             USING btree (option_id);
CREATE INDEX idx_status_train_source             ON shared_catalogs.status_train             USING btree (option_id, source_status);

-- ----------------------------------------------------------------------------
-- Verificacion
-- ----------------------------------------------------------------------------
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'shared_catalogs'
ORDER BY table_name;
