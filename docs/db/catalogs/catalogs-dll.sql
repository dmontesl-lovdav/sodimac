-- shared_catalogs.catalog_header definition

-- Drop table

-- DROP TABLE shared_catalogs.catalog_header;

CREATE TABLE shared_catalogs.catalog_header (
	id serial4 NOT NULL,
	code varchar(64) NOT NULL,
	prefix varchar(4) NOT NULL,
	"name" varchar(128) NOT NULL,
	description varchar(512) NULL,
	"module" varchar(32) NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp NULL,
	catalog_type varchar(20) DEFAULT 'SIMPLE'::character varying NOT NULL,
	created_by varchar(100) NULL,
	updated_by varchar(100) NULL,
	CONSTRAINT catalog_header_code_key UNIQUE (code),
	CONSTRAINT catalog_header_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_catalog_header_code ON shared_catalogs.catalog_header USING btree (code);
CREATE INDEX idx_catalog_header_module ON shared_catalogs.catalog_header USING btree (module);
CREATE INDEX idx_catalog_header_status ON shared_catalogs.catalog_header USING btree (status);
CREATE INDEX idx_catalog_header_type ON shared_catalogs.catalog_header USING btree (catalog_type);


-- shared_catalogs.dictionary_lang definition

-- Drop table

-- DROP TABLE shared_catalogs.dictionary_lang;

CREATE TABLE shared_catalogs.dictionary_lang (
	id serial4 NOT NULL,
	dict_id int4 NOT NULL,
	lang_id int4 NOT NULL,
	description varchar(512) NOT NULL,
	CONSTRAINT dictionary_lang_pkey PRIMARY KEY (id),
	CONSTRAINT uk_dictionary_lang UNIQUE (dict_id, lang_id)
);
CREATE INDEX idx_dictionary_dict_id ON shared_catalogs.dictionary_lang USING btree (dict_id);
CREATE INDEX idx_dictionary_lang_id ON shared_catalogs.dictionary_lang USING btree (lang_id);


-- shared_catalogs.payment_condition definition

-- Drop table

-- DROP TABLE shared_catalogs.payment_condition;

CREATE TABLE shared_catalogs.payment_condition (
	id serial4 NOT NULL,
	supplier_number varchar(20) NULL,
	condition_name varchar(100) NOT NULL,
	days int4 DEFAULT 0 NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	created_by varchar(100) NULL,
	updated_at timestamp NULL,
	updated_by varchar(100) NULL,
	CONSTRAINT payment_condition_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_payment_condition_status ON shared_catalogs.payment_condition USING btree (status);
CREATE INDEX idx_payment_condition_supplier ON shared_catalogs.payment_condition USING btree (supplier_number);


-- shared_catalogs.status_train definition

-- Drop table

-- DROP TABLE shared_catalogs.status_train;

CREATE TABLE shared_catalogs.status_train (
	id serial4 NOT NULL,
	option_id int4 NOT NULL,
	source_status int4 NOT NULL,
	target_status int4 NOT NULL,
	created_by int8 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT status_train_pkey PRIMARY KEY (id),
	CONSTRAINT uk_status_train UNIQUE (option_id, source_status, target_status)
);
CREATE INDEX idx_status_train_option ON shared_catalogs.status_train USING btree (option_id);
CREATE INDEX idx_status_train_source ON shared_catalogs.status_train USING btree (option_id, source_status);


-- shared_catalogs.supplier_block definition

-- Drop table

-- DROP TABLE shared_catalogs.supplier_block;

CREATE TABLE shared_catalogs.supplier_block (
	id serial4 NOT NULL,
	supplier_number varchar(20) NOT NULL,
	valid_from date NOT NULL,
	valid_to date NOT NULL,
	block_reason varchar(255) NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	created_by varchar(100) NULL,
	updated_at timestamp NULL,
	updated_by varchar(100) NULL,
	CONSTRAINT chk_supplier_block_dates CHECK ((valid_to >= valid_from)),
	CONSTRAINT supplier_block_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_supplier_block_dates ON shared_catalogs.supplier_block USING btree (valid_from, valid_to);
CREATE INDEX idx_supplier_block_number ON shared_catalogs.supplier_block USING btree (supplier_number);
CREATE INDEX idx_supplier_block_status ON shared_catalogs.supplier_block USING btree (status);


-- shared_catalogs.supplier_type definition

-- Drop table

-- DROP TABLE shared_catalogs.supplier_type;

CREATE TABLE shared_catalogs.supplier_type (
	id serial4 NOT NULL,
	code varchar(20) NOT NULL,
	description varchar(100) NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	created_by varchar(100) NULL,
	updated_at timestamp NULL,
	updated_by varchar(100) NULL,
	CONSTRAINT supplier_type_code_key UNIQUE (code),
	CONSTRAINT supplier_type_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_supplier_type_code ON shared_catalogs.supplier_type USING btree (code);
CREATE INDEX idx_supplier_type_status ON shared_catalogs.supplier_type USING btree (status);


-- shared_catalogs.catalog_detail definition

-- Drop table

-- DROP TABLE shared_catalogs.catalog_detail;

CREATE TABLE shared_catalogs.catalog_detail (
	id serial4 NOT NULL,
	header_id int4 NOT NULL,
	"key" varchar(64) NOT NULL,
	dict_id int4 NOT NULL,
	color varchar(16) NULL,
	sort_order int4 DEFAULT 0 NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp NULL,
	internal_status int4 NULL,
	external_key varchar(50) NULL,
	value varchar(100) NULL,
	valid_from date NULL,
	valid_to date NULL,
	created_by varchar(100) NULL,
	"attributes" jsonb NULL,
	parent_catalog_id int4 NULL,
	parent_element_id int4 NULL,
	updated_by varchar(100) NULL,
	CONSTRAINT catalog_detail_pkey PRIMARY KEY (id),
	CONSTRAINT uk_catalog_detail UNIQUE (header_id, key),
	CONSTRAINT fk_catalog_detail_header FOREIGN KEY (header_id) REFERENCES shared_catalogs.catalog_header(id)
);
CREATE INDEX idx_catalog_detail_attributes ON shared_catalogs.catalog_detail USING gin (attributes);
CREATE INDEX idx_catalog_detail_dict ON shared_catalogs.catalog_detail USING btree (dict_id);
CREATE INDEX idx_catalog_detail_external_key ON shared_catalogs.catalog_detail USING btree (external_key);
CREATE INDEX idx_catalog_detail_header ON shared_catalogs.catalog_detail USING btree (header_id);
CREATE INDEX idx_catalog_detail_key ON shared_catalogs.catalog_detail USING btree (key);
CREATE INDEX idx_catalog_detail_parent_catalog_id ON shared_catalogs.catalog_detail USING btree (parent_catalog_id);
CREATE INDEX idx_catalog_detail_parent_element_id ON shared_catalogs.catalog_detail USING btree (parent_element_id);
CREATE INDEX idx_catalog_detail_status ON shared_catalogs.catalog_detail USING btree (status);
CREATE INDEX idx_catalog_detail_valid_from ON shared_catalogs.catalog_detail USING btree (valid_from);
CREATE INDEX idx_catalog_detail_valid_to ON shared_catalogs.catalog_detail USING btree (valid_to);


-- shared_catalogs.catalog_detail_relation definition

-- Drop table

-- DROP TABLE shared_catalogs.catalog_detail_relation;

CREATE TABLE shared_catalogs.catalog_detail_relation (
	id serial4 NOT NULL,
	source_detail_id int4 NOT NULL,
	target_detail_id int4 NOT NULL,
	relation_type varchar(20) DEFAULT 'DEPENDS_ON'::character varying NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	created_by varchar(100) NULL,
	valid_from date NULL,
	valid_to date NULL,
	updated_by varchar(100) NULL,
	updated_at timestamp NULL,
	CONSTRAINT catalog_detail_relation_pkey PRIMARY KEY (id),
	CONSTRAINT uk_detail_relation UNIQUE (source_detail_id, target_detail_id, relation_type),
	CONSTRAINT fk_relation_source FOREIGN KEY (source_detail_id) REFERENCES shared_catalogs.catalog_detail(id) ON DELETE CASCADE,
	CONSTRAINT fk_relation_target FOREIGN KEY (target_detail_id) REFERENCES shared_catalogs.catalog_detail(id) ON DELETE CASCADE
);
CREATE INDEX idx_relation_source ON shared_catalogs.catalog_detail_relation USING btree (source_detail_id);
CREATE INDEX idx_relation_source_active ON shared_catalogs.catalog_detail_relation USING btree (source_detail_id, status) WHERE (status = 1);
CREATE INDEX idx_relation_status ON shared_catalogs.catalog_detail_relation USING btree (status);
CREATE INDEX idx_relation_target ON shared_catalogs.catalog_detail_relation USING btree (target_detail_id);
CREATE INDEX idx_relation_type ON shared_catalogs.catalog_detail_relation USING btree (relation_type);


-- shared_catalogs.supplier definition

-- Drop table

-- DROP TABLE shared_catalogs.supplier;

CREATE TABLE shared_catalogs.supplier (
	id serial4 NOT NULL,
	supplier_number varchar(20) NOT NULL,
	rfc varchar(13) NOT NULL,
	business_name varchar(255) NOT NULL,
	supplier_type_id int4 NULL,
	logo varchar(500) NULL,
	payment_condition_id int4 NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	created_by varchar(100) NULL,
	updated_at timestamp NULL,
	updated_by varchar(100) NULL,
	email_financial varchar(255) NULL,
	CONSTRAINT supplier_pkey PRIMARY KEY (id),
	CONSTRAINT supplier_supplier_number_key UNIQUE (supplier_number),
	CONSTRAINT fk_supplier_payment_condition FOREIGN KEY (payment_condition_id) REFERENCES shared_catalogs.payment_condition(id),
	CONSTRAINT fk_supplier_type FOREIGN KEY (supplier_type_id) REFERENCES shared_catalogs.supplier_type(id)
);
CREATE INDEX idx_supplier_number ON shared_catalogs.supplier USING btree (supplier_number);
CREATE INDEX idx_supplier_rfc ON shared_catalogs.supplier USING btree (rfc);
CREATE INDEX idx_supplier_status ON shared_catalogs.supplier USING btree (status);
CREATE INDEX idx_supplier_type ON shared_catalogs.supplier USING btree (supplier_type_id);


-- shared_catalogs.catalog_conversion definition

-- Drop table

-- DROP TABLE shared_catalogs.catalog_conversion;

CREATE TABLE shared_catalogs.catalog_conversion (
	id serial4 NOT NULL,
	source_element_id int4 NOT NULL,
	target_element_id int4 NOT NULL,
	valid_from date NULL,
	valid_to date NULL,
	status int4 DEFAULT 1 NOT NULL,
	is_principal bool DEFAULT false NOT NULL,
	created_by varchar(100) NULL,
	updated_by varchar(100) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp NULL,
	CONSTRAINT catalog_conversion_pkey PRIMARY KEY (id),
	CONSTRAINT catalog_conversion_source_element_id_fkey FOREIGN KEY (source_element_id) REFERENCES shared_catalogs.catalog_detail(id),
	CONSTRAINT catalog_conversion_target_element_id_fkey FOREIGN KEY (target_element_id) REFERENCES shared_catalogs.catalog_detail(id)
);
CREATE INDEX idx_conv_source ON shared_catalogs.catalog_conversion USING btree (source_element_id);
CREATE INDEX idx_conv_target ON shared_catalogs.catalog_conversion USING btree (target_element_id);