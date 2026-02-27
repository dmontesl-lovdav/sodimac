-- core_audit.activity_logs definition

-- Drop table

-- DROP TABLE core_audit.activity_logs;

CREATE TABLE core_audit.activity_logs (
	activity_logs_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	trace_front_id uuid NULL,
	trace_id uuid NOT NULL,
	duration_ms numeric(18, 6) NULL,
	is_error bool NOT NULL,
	modulo varchar(100) NULL,
	service_name varchar(100) NOT NULL,
	"action" varchar(100) NOT NULL,
	message varchar(100) NOT NULL,
	message_detail varchar NULL,
	user_id varchar(50) NULL,
	"timestamp" timestamp NOT NULL,
	details jsonb NULL,
	tipo_evento varchar(10) DEFAULT 'INFO'::character varying NOT NULL,
	codigo_error varchar(64) NULL,
	id_mensaje varchar(64) NULL,
	paso varchar(128) NULL,
	log text NULL,
	CONSTRAINT activity_logs_pkey PRIMARY KEY (activity_logs_uuid)
);
CREATE INDEX idx_activity_logs_codigo_error ON core_audit.activity_logs USING btree (codigo_error);
CREATE INDEX idx_activity_logs_modulo ON core_audit.activity_logs USING btree (modulo);
CREATE INDEX idx_activity_logs_timestamp ON core_audit.activity_logs USING btree ("timestamp" DESC);
CREATE INDEX idx_activity_logs_tipo_evento ON core_audit.activity_logs USING btree (tipo_evento);
CREATE INDEX idx_activity_logs_trace_id ON core_audit.activity_logs USING btree (trace_id);


-- core_audit.insert_log definition

-- Drop table

-- DROP TABLE core_audit.insert_log;

CREATE TABLE core_audit.insert_log (
	id uuid DEFAULT gen_random_uuid() NULL,
	schema_name text NULL,
	table_name text NULL,
	user_name text NULL,
	inserted_at timestamp DEFAULT CURRENT_TIMESTAMP NULL
);