-- core_utils.cat_item definition

-- Drop table

-- DROP TABLE core_utils.cat_item;

CREATE TABLE core_utils.cat_item (
	id_item serial4 NOT NULL,
	"name" varchar(100) NOT NULL,
	description varchar(500) NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT cat_item_pkey PRIMARY KEY (id_item)
);
CREATE INDEX idx_cat_item_name ON core_utils.cat_item USING btree (name);


-- core_utils.cat_item_type definition

-- Drop table

-- DROP TABLE core_utils.cat_item_type;

CREATE TABLE core_utils.cat_item_type (
	id_item_type serial4 NOT NULL,
	"name" varchar(100) NOT NULL,
	description varchar(500) NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT cat_item_type_pkey PRIMARY KEY (id_item_type)
);
CREATE INDEX idx_cat_item_type_name ON core_utils.cat_item_type USING btree (name);


-- core_utils.cat_message definition

-- Drop table

-- DROP TABLE core_utils.cat_message;

CREATE TABLE core_utils.cat_message (
	id_message serial4 NOT NULL,
	message_code varchar(20) NOT NULL,
	id_message_type int4 NOT NULL,
	description varchar(500) NOT NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT cat_message_pkey PRIMARY KEY (id_message),
	CONSTRAINT uk_cat_message_code UNIQUE (message_code)
);
CREATE INDEX idx_cat_message_code ON core_utils.cat_message USING btree (message_code);
CREATE INDEX idx_cat_message_type ON core_utils.cat_message USING btree (id_message_type);


-- core_utils.cat_module definition

-- Drop table

-- DROP TABLE core_utils.cat_module;

CREATE TABLE core_utils.cat_module (
	id_module serial4 NOT NULL,
	"name" varchar(100) NOT NULL,
	description varchar(500) NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT cat_module_pkey PRIMARY KEY (id_module)
);
CREATE INDEX idx_cat_module_name ON core_utils.cat_module USING btree (name);


-- core_utils.cat_process definition

-- Drop table

-- DROP TABLE core_utils.cat_process;

CREATE TABLE core_utils.cat_process (
	id_process serial4 NOT NULL,
	"name" varchar(100) NOT NULL,
	description varchar(500) NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT cat_process_pkey PRIMARY KEY (id_process)
);
CREATE INDEX idx_cat_process_name ON core_utils.cat_process USING btree (name);


-- core_utils.application_msg definition

-- Drop table

-- DROP TABLE core_utils.application_msg;

CREATE TABLE core_utils.application_msg (
	id_application_msg serial4 NOT NULL,
	id_message int4 NOT NULL,
	id_application int4 NOT NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT application_msg_pkey PRIMARY KEY (id_application_msg),
	CONSTRAINT uk_application_msg UNIQUE (id_message, id_application),
	CONSTRAINT fk_application_msg_message FOREIGN KEY (id_message) REFERENCES core_utils.cat_message(id_message) ON DELETE CASCADE
);
CREATE INDEX idx_application_msg_app ON core_utils.application_msg USING btree (id_application);
CREATE INDEX idx_application_msg_message ON core_utils.application_msg USING btree (id_message);


-- core_utils.cat_parameter definition

-- Drop table

-- DROP TABLE core_utils.cat_parameter;

CREATE TABLE core_utils.cat_parameter (
	id_parameter serial4 NOT NULL,
	id_module int4 NULL,
	id_type int4 NULL,
	"name" varchar(100) NOT NULL,
	description varchar(500) NULL,
	value varchar(1000) NULL,
	"version" numeric(5, 2) DEFAULT 1.0 NOT NULL,
	start_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	end_date timestamp NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_by int4 DEFAULT 1 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int4 NULL,
	updated_at timestamp NULL,
	CONSTRAINT cat_parameter_pkey PRIMARY KEY (id_parameter),
	CONSTRAINT uk_cat_parameter_name_version UNIQUE (name, version),
	CONSTRAINT fk_cat_parameter_module FOREIGN KEY (id_module) REFERENCES core_utils.cat_module(id_module) ON DELETE SET NULL
);
CREATE INDEX idx_cat_parameter_module ON core_utils.cat_parameter USING btree (id_module);
CREATE INDEX idx_cat_parameter_name ON core_utils.cat_parameter USING btree (name);
CREATE INDEX idx_cat_parameter_status ON core_utils.cat_parameter USING btree (status);