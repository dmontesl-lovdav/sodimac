-- tenant_fiscal.addendum definition

-- Drop table

-- DROP TABLE tenant_fiscal.addendum;

CREATE TABLE tenant_fiscal.addendum (
	addendum_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	invoice_uuid uuid NULL,
	supplier_number numeric(10) NULL,
	reception_number varchar(20) NULL,
	purchase_order_number varchar(50) NULL,
	shipping_guide_number varchar(30) NULL,
	addendum_content text NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	payments_uuid uuid NULL,
	supplier_type varchar(10) NULL,
	addenda_type int4 DEFAULT 1 NULL,
	user_id int8 NULL,
	update_date timestamp NULL,
	CONSTRAINT addendum_pkey PRIMARY KEY (addendum_uuid),
	CONSTRAINT chk_addendum_reference CHECK (((((invoice_uuid IS NOT NULL) AND (payments_uuid IS NULL)) OR ((invoice_uuid IS NULL) AND (payments_uuid IS NOT NULL)))))
);
CREATE INDEX idx_addendum_invoice_uuid ON tenant_fiscal.addendum (invoice_uuid);
CREATE INDEX idx_addendum_payments_uuid ON tenant_fiscal.addendum (payments_uuid);
CREATE INDEX idx_addendum_purchase_order ON tenant_fiscal.addendum (purchase_order_number);
CREATE INDEX idx_addendum_supplier_number ON tenant_fiscal.addendum (supplier_number);
CREATE INDEX idx_addendum_type ON tenant_fiscal.addendum (addenda_type);
CREATE INDEX idx_addendum_user_id ON tenant_fiscal.addendum (user_id);


-- tenant_fiscal.authorized_receiver_catalog definition

-- Drop table

-- DROP TABLE tenant_fiscal.authorized_receiver_catalog;

CREATE TABLE tenant_fiscal.authorized_receiver_catalog (
	authorized_receiver_id serial4 NOT NULL,
	receiver_uuid uuid NOT NULL,
	"name" varchar(100) NOT NULL,
	rfc varchar(13) NOT NULL,
	tax_regime varchar(3) NULL,
	valid_from timestamp NULL,
	valid_to timestamp NULL,
	status int4 DEFAULT 1 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT authorized_receiver_catalog_pkey PRIMARY KEY (authorized_receiver_id),
	CONSTRAINT chk_authorized_receiver_rfc_length CHECK ((((length((rfc)::text) >= 12) AND (length((rfc)::text) <= 13)))),
	CONSTRAINT chk_authorized_receiver_status CHECK (((status = ANY (ARRAY[0, 1])))),
	CONSTRAINT chk_authorized_receiver_valid_dates CHECK ((((valid_from IS NULL) OR (valid_to IS NULL) OR (valid_from <= valid_to))))
);
CREATE INDEX idx_authorized_receiver_receiver_uuid ON tenant_fiscal.authorized_receiver_catalog (receiver_uuid);
CREATE INDEX idx_authorized_receiver_rfc ON tenant_fiscal.authorized_receiver_catalog (rfc);
CREATE INDEX idx_authorized_receiver_status ON tenant_fiscal.authorized_receiver_catalog (status);


-- tenant_fiscal.equivalence_dr definition

-- Drop table

-- DROP TABLE tenant_fiscal.equivalence_dr;

CREATE TABLE tenant_fiscal.equivalence_dr (
	equivalence_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	related_document_uuid uuid NOT NULL,
	folio varchar(49) NULL,
	amount_paid numeric(16, 2) NOT NULL,
	previous_balance numeric(16, 2) NOT NULL,
	remaining_balance numeric(16, 2) NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NULL,
	installment_number numeric(3) NULL,
	tax_object varchar(10) NULL,
	series varchar(25) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_equivalence_dr_amounts CHECK ((((amount_paid >= (0)::numeric) AND (previous_balance >= (0)::numeric) AND (remaining_balance >= (0)::numeric)))),
	CONSTRAINT equivalence_dr_pkey PRIMARY KEY (equivalence_uuid)
);


-- tenant_fiscal.flyway_schema_history definition

-- Drop table

-- DROP TABLE tenant_fiscal.flyway_schema_history;

CREATE TABLE tenant_fiscal.flyway_schema_history (
	installed_rank int4 NOT NULL,
	"version" varchar(50) NULL,
	description varchar(200) NOT NULL,
	"type" varchar(20) NOT NULL,
	script varchar(1000) NOT NULL,
	checksum int4 NULL,
	installed_by varchar(100) NOT NULL,
	installed_on timestamp DEFAULT now() NOT NULL,
	execution_time int4 NOT NULL,
	success bool NOT NULL,
	CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);
CREATE INDEX flyway_schema_history_s_idx ON tenant_fiscal.flyway_schema_history (success);


-- tenant_fiscal.invoice definition

-- Drop table

-- DROP TABLE tenant_fiscal.invoice;

CREATE TABLE tenant_fiscal.invoice (
	invoice_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	fiscal_uuid uuid NULL,
	place_of_issue varchar(5) NULL,
	payment_method varchar(3) NULL,
	document_type bpchar(1) NOT NULL,
	total numeric(16, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NULL,
	discount numeric(16, 2) DEFAULT 0 NULL,
	subtotal numeric(16, 2) NOT NULL,
	payment_conditions varchar(255) NULL,
	payment_form varchar(10) NULL,
	issue_date date NOT NULL,
	certification_date timestamp NULL,
	folio varchar(49) NULL,
	series varchar(25) NULL,
	"version" numeric(6, 3) NOT NULL,
	xml_content text NULL,
	status int4 DEFAULT 1 NULL,
	issuer_uuid uuid NOT NULL,
	receiver_uuid uuid NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	accounting_sent_date timestamp NULL,
	accounting_date date NULL,
	CONSTRAINT chk_invoice_amounts CHECK ((((total >= (0)::numeric) AND (subtotal >= (0)::numeric) AND (discount >= (0)::numeric)))),
	CONSTRAINT chk_invoice_document_type CHECK (((document_type = ANY (ARRAY['I'::bpchar, 'E'::bpchar, 'T'::bpchar, 'N'::bpchar, 'P'::bpchar])))),
	CONSTRAINT chk_invoice_exchange_rate CHECK (((exchange_rate > (0)::numeric))),
	CONSTRAINT chk_invoice_status CHECK (((status = ANY (ARRAY[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13])))),
	CONSTRAINT invoice_pkey PRIMARY KEY (invoice_uuid),
	CONSTRAINT uq_invoice_fiscal_uuid UNIQUE (fiscal_uuid)
);
CREATE INDEX idx_invoice_accounting_date ON tenant_fiscal.invoice (accounting_date);
CREATE INDEX idx_invoice_document_type ON tenant_fiscal.invoice (document_type);
CREATE INDEX idx_invoice_issue_date ON tenant_fiscal.invoice (issue_date);
CREATE INDEX idx_invoice_issuer_uuid ON tenant_fiscal.invoice (issuer_uuid);
CREATE INDEX idx_invoice_receiver_uuid ON tenant_fiscal.invoice (receiver_uuid);
CREATE INDEX idx_invoice_sent_date ON tenant_fiscal.invoice (accounting_sent_date);
CREATE UNIQUE INDEX idx_invoice_series_folio ON tenant_fiscal.invoice (series,folio);
CREATE INDEX idx_invoice_status ON tenant_fiscal.invoice (status);
CREATE INDEX idx_invoice_total ON tenant_fiscal.invoice (total);


-- tenant_fiscal.invoice_status_history definition

-- Drop table

-- DROP TABLE tenant_fiscal.invoice_status_history;

CREATE TABLE tenant_fiscal.invoice_status_history (
	history_id uuid DEFAULT gen_random_uuid() NOT NULL,
	invoice_uuid uuid NOT NULL,
	fiscal_uuid uuid NOT NULL,
	status_from int4 NULL,
	status_to int4 NOT NULL,
	changed_by int4 NULL,
	changed_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	"comment" text NULL,
	CONSTRAINT invoice_status_history_pkey PRIMARY KEY (history_id)
);
CREATE INDEX idx_status_history_fiscal ON tenant_fiscal.invoice_status_history (fiscal_uuid);
CREATE INDEX idx_status_history_invoice ON tenant_fiscal.invoice_status_history (invoice_uuid);


-- tenant_fiscal.issuer definition

-- Drop table

-- DROP TABLE tenant_fiscal.issuer;

CREATE TABLE tenant_fiscal.issuer (
	issuer_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	"name" varchar(254) NOT NULL,
	rfc varchar(13) NOT NULL,
	tax_regime varchar(3) NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_issuer_rfc_format CHECK ((((rfc)::text ~ '^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$'::text))),
	CONSTRAINT chk_issuer_rfc_length CHECK ((((length((rfc)::text) >= 12) AND (length((rfc)::text) <= 13)))),
	CONSTRAINT issuer_pkey PRIMARY KEY (issuer_uuid)
);
CREATE INDEX idx_issuer_name ON tenant_fiscal.issuer ("name");
CREATE UNIQUE INDEX idx_issuer_rfc ON tenant_fiscal.issuer (rfc);


-- tenant_fiscal.log definition

-- Drop table

-- DROP TABLE tenant_fiscal.log;

CREATE TABLE tenant_fiscal.log (
	log_id uuid DEFAULT gen_random_uuid() NOT NULL,
	pac_id int4 NULL,
	version_id int4 NULL,
	cfdi_uuid uuid NULL,
	operation_type varchar(20) NOT NULL,
	transaction_date timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	record_start_date timestamp NULL,
	record_end_date timestamp NULL,
	status_code varchar(10) NULL,
	status_message text NULL,
	request_data text NULL,
	response_data text NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT log_pkey PRIMARY KEY (log_id)
);
CREATE INDEX idx_log_cfdi_uuid ON tenant_fiscal.log (cfdi_uuid);
CREATE INDEX idx_log_operation_type ON tenant_fiscal.log (operation_type);
CREATE INDEX idx_log_pac_id ON tenant_fiscal.log (pac_id);
CREATE INDEX idx_log_transaction_date ON tenant_fiscal.log (transaction_date);
CREATE INDEX idx_log_version_id ON tenant_fiscal.log (version_id);


-- tenant_fiscal.pac_catalog definition

-- Drop table

-- DROP TABLE tenant_fiscal.pac_catalog;

CREATE TABLE tenant_fiscal.pac_catalog (
	pac_id serial4 NOT NULL,
	"name" varchar(50) NOT NULL,
	description varchar(254) NULL,
	priority int4 DEFAULT 0 NULL,
	user_name varchar(100) NULL,
	"password" varchar(254) NULL,
	url varchar(254) NULL,
	license varchar(254) NULL,
	valid_from date NULL,
	valid_to date NULL,
	catalog_msg_id numeric(4) NULL,
	status int4 DEFAULT 1 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_pac_status CHECK (((status = ANY (ARRAY[0, 1])))),
	CONSTRAINT chk_pac_valid_dates CHECK ((((valid_from IS NULL) OR (valid_to IS NULL) OR (valid_from <= valid_to)))),
	CONSTRAINT pac_catalog_pkey PRIMARY KEY (pac_id)
);
CREATE UNIQUE INDEX idx_pac_catalog_name ON tenant_fiscal.pac_catalog ("name");
CREATE INDEX idx_pac_catalog_priority ON tenant_fiscal.pac_catalog (priority);
CREATE INDEX idx_pac_catalog_status ON tenant_fiscal.pac_catalog (status);
CREATE INDEX idx_pac_catalog_valid_dates ON tenant_fiscal.pac_catalog (valid_from,valid_to);


-- tenant_fiscal.payment definition

-- Drop table

-- DROP TABLE tenant_fiscal.payment;

CREATE TABLE tenant_fiscal.payment (
	payment_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	payments_uuid uuid NOT NULL,
	payment_date date NOT NULL,
	payment_method varchar(10) NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NULL,
	amount numeric(16, 2) NOT NULL,
	operation_number varchar(100) NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NULL,
	payer_bank_rfc varchar(13) NULL,
	payer_account varchar(50) NULL,
	beneficiary_bank_rfc varchar(13) NULL,
	beneficiary_account varchar(50) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_payment_amount CHECK (((amount > (0)::numeric))),
	CONSTRAINT chk_payment_date_not_future CHECK (((payment_date <= CURRENT_DATE))),
	CONSTRAINT chk_payment_exchange_rate CHECK (((exchange_rate > (0)::numeric))),
	CONSTRAINT payment_pkey PRIMARY KEY (payment_uuid)
);
CREATE INDEX idx_payment_date ON tenant_fiscal.payment (payment_date);
CREATE INDEX idx_payment_method ON tenant_fiscal.payment (payment_method);
CREATE INDEX idx_payment_payments_uuid ON tenant_fiscal.payment (payments_uuid);


-- tenant_fiscal.payments definition

-- Drop table

-- DROP TABLE tenant_fiscal.payments;

CREATE TABLE tenant_fiscal.payments (
	payments_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	fiscal_uuid uuid NULL,
	"version" numeric(6, 3) NOT NULL,
	payment_date date NOT NULL,
	certification_date timestamp NULL,
	issuer_uuid uuid NOT NULL,
	receiver_uuid uuid NOT NULL,
	folio varchar(49) NULL,
	series varchar(25) NULL,
	xml_content text NULL,
	status int4 DEFAULT 1 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_payments_date_not_future CHECK (((payment_date <= CURRENT_DATE))),
	CONSTRAINT chk_payments_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3])))),
	CONSTRAINT payments_pkey PRIMARY KEY (payments_uuid),
	CONSTRAINT uq_payments_fiscal_uuid UNIQUE (fiscal_uuid)
);
CREATE INDEX idx_payments_issuer_uuid ON tenant_fiscal.payments (issuer_uuid);
CREATE INDEX idx_payments_payment_date ON tenant_fiscal.payments (payment_date);
CREATE INDEX idx_payments_receiver_uuid ON tenant_fiscal.payments (receiver_uuid);
CREATE UNIQUE INDEX idx_payments_series_folio ON tenant_fiscal.payments (series,folio);
CREATE INDEX idx_payments_status ON tenant_fiscal.payments (status);


-- tenant_fiscal.payment_file_registry definition

-- Drop table

-- DROP TABLE tenant_fiscal.payment_file_registry;

CREATE TABLE tenant_fiscal.payment_file_registry (
	file_registry_id serial4 NOT NULL,
	file_name varchar(254) NOT NULL,
	payments_uuid uuid NULL,
	processing_status varchar(20) NOT NULL,
	error_code varchar(10) NULL,
	error_message text NULL,
	supplier_id int8 NULL,
	user_id int8 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_payment_file_status CHECK ((((processing_status)::text = ANY ((ARRAY['SUCCESS'::character varying, 'FAILED'::character varying, 'REJECTED'::character varying])::text[])))),
	CONSTRAINT payment_file_registry_pkey PRIMARY KEY (file_registry_id)
);
CREATE INDEX idx_payment_file_created_at ON tenant_fiscal.payment_file_registry (created_at);
CREATE INDEX idx_payment_file_name ON tenant_fiscal.payment_file_registry (file_name);
CREATE INDEX idx_payment_file_payments_uuid ON tenant_fiscal.payment_file_registry (payments_uuid);
CREATE INDEX idx_payment_file_status ON tenant_fiscal.payment_file_registry (processing_status);
CREATE INDEX idx_payment_file_supplier ON tenant_fiscal.payment_file_registry (supplier_id);
CREATE INDEX idx_payment_file_user ON tenant_fiscal.payment_file_registry (user_id);


-- tenant_fiscal.payment_response_catalog definition

-- Drop table

-- DROP TABLE tenant_fiscal.payment_response_catalog;

CREATE TABLE tenant_fiscal.payment_response_catalog (
	response_catalog_id serial4 NOT NULL,
	response_code varchar(10) NOT NULL,
	response_type varchar(20) NOT NULL,
	description varchar(254) NOT NULL,
	message_template text NULL,
	status int4 DEFAULT 1 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_payment_response_status CHECK (((status = ANY (ARRAY[0, 1])))),
	CONSTRAINT chk_payment_response_type CHECK ((((response_type)::text = ANY ((ARRAY['SUCCESS'::character varying, 'ERROR_SAT'::character varying, 'ERROR_STRUCTURE'::character varying, 'ERROR_VALIDATION'::character varying, 'ERROR_BUSINESS'::character varying])::text[])))),
	CONSTRAINT payment_response_catalog_pkey PRIMARY KEY (response_catalog_id)
);
CREATE INDEX idx_payment_response_code ON tenant_fiscal.payment_response_catalog (response_code);
CREATE INDEX idx_payment_response_status ON tenant_fiscal.payment_response_catalog (status);
CREATE INDEX idx_payment_response_type ON tenant_fiscal.payment_response_catalog (response_type);


-- tenant_fiscal.receiver definition

-- Drop table

-- DROP TABLE tenant_fiscal.receiver;

CREATE TABLE tenant_fiscal.receiver (
	receiver_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	"name" varchar(254) NOT NULL,
	rfc varchar(13) NOT NULL,
	tax_regime varchar(3) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_receiver_rfc_format CHECK ((((rfc)::text ~ '^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$'::text))),
	CONSTRAINT chk_receiver_rfc_length CHECK ((((length((rfc)::text) >= 12) AND (length((rfc)::text) <= 13)))),
	CONSTRAINT receiver_pkey PRIMARY KEY (receiver_uuid)
);
CREATE INDEX idx_receiver_name ON tenant_fiscal.receiver ("name");
CREATE UNIQUE INDEX idx_receiver_rfc ON tenant_fiscal.receiver (rfc);


-- tenant_fiscal.related_cfdi definition

-- Drop table

-- DROP TABLE tenant_fiscal.related_cfdi;

CREATE TABLE tenant_fiscal.related_cfdi (
	related_cfdi_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	invoice_uuid uuid NOT NULL,
	related_invoice_uuid uuid NOT NULL,
	relation_type varchar(3) NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_related_cfdi_not_self CHECK (((invoice_uuid <> related_invoice_uuid))),
	CONSTRAINT related_cfdi_pkey PRIMARY KEY (related_cfdi_uuid)
);
CREATE INDEX idx_related_cfdi_invoice_uuid ON tenant_fiscal.related_cfdi (invoice_uuid);
CREATE INDEX idx_related_cfdi_related_invoice ON tenant_fiscal.related_cfdi (related_invoice_uuid);
CREATE INDEX idx_related_cfdi_relation_type ON tenant_fiscal.related_cfdi (relation_type);


-- tenant_fiscal.related_documents definition

-- Drop table

-- DROP TABLE tenant_fiscal.related_documents;

CREATE TABLE tenant_fiscal.related_documents (
	related_document_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	payment_uuid uuid NOT NULL,
	document_uuid uuid NOT NULL,
	amount_paid numeric(16, 2) NOT NULL,
	previous_balance numeric(16, 2) NOT NULL,
	remaining_balance numeric(16, 2) NOT NULL,
	installment_number numeric(3) NULL,
	series varchar(25) NULL,
	folio varchar(49) NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_related_documents_amounts CHECK ((((amount_paid >= (0)::numeric) AND (previous_balance >= (0)::numeric) AND (remaining_balance >= (0)::numeric)))),
	CONSTRAINT chk_related_documents_balance CHECK (((remaining_balance = (previous_balance - amount_paid)))),
	CONSTRAINT chk_related_documents_exchange_rate CHECK (((exchange_rate > (0)::numeric))),
	CONSTRAINT related_documents_pkey PRIMARY KEY (related_document_uuid)
);
CREATE INDEX idx_related_documents_document_uuid ON tenant_fiscal.related_documents (document_uuid);
CREATE INDEX idx_related_documents_payment_uuid ON tenant_fiscal.related_documents (payment_uuid);


-- tenant_fiscal.tax definition

-- Drop table

-- DROP TABLE tenant_fiscal.tax;

CREATE TABLE tenant_fiscal.tax (
	tax_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	invoice_uuid uuid NOT NULL,
	total_transferred_taxes numeric(16, 2) NULL,
	total_withheld_taxes numeric(16, 2) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT tax_pkey PRIMARY KEY (tax_uuid)
);
CREATE INDEX idx_tax_invoice_uuid ON tenant_fiscal.tax (invoice_uuid);


-- tenant_fiscal.tax_detail definition

-- Drop table

-- DROP TABLE tenant_fiscal.tax_detail;

CREATE TABLE tenant_fiscal.tax_detail (
	tax_detail_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	tax_uuid uuid NOT NULL,
	product_service_code varchar(20) NULL,
	tax_type varchar(10) NOT NULL,
	tax_code varchar(3) NOT NULL,
	factor_type varchar(10) NULL,
	rate_or_quota numeric(6, 6) NULL,
	amount numeric(16, 2) NULL,
	base numeric(16, 2) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_tax_detail_code CHECK ((((tax_code)::text = ANY ((ARRAY['001'::character varying, '002'::character varying, '003'::character varying])::text[])))),
	CONSTRAINT chk_tax_detail_type CHECK ((((tax_type)::text = ANY ((ARRAY['Traslado'::character varying, 'Retencion'::character varying])::text[])))),
	CONSTRAINT tax_detail_pkey PRIMARY KEY (tax_detail_uuid)
);
CREATE INDEX idx_tax_detail_code ON tenant_fiscal.tax_detail (tax_code);
CREATE INDEX idx_tax_detail_tax_uuid ON tenant_fiscal.tax_detail (tax_uuid);
CREATE INDEX idx_tax_detail_type ON tenant_fiscal.tax_detail (tax_type);


-- tenant_fiscal.tax_transfer definition

-- Drop table

-- DROP TABLE tenant_fiscal.tax_transfer;

CREATE TABLE tenant_fiscal.tax_transfer (
	tax_transfer_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	tax_uuid uuid NOT NULL,
	tax_code varchar(3) NOT NULL,
	factor_type varchar(10) NOT NULL,
	rate_or_quota numeric(6, 6) NULL,
	amount numeric(16, 2) NULL,
	base numeric(16, 2) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_tax_transfer_code CHECK ((((tax_code)::text = ANY ((ARRAY['001'::character varying, '002'::character varying, '003'::character varying])::text[])))),
	CONSTRAINT chk_tax_transfer_factor_type CHECK ((((factor_type)::text = ANY ((ARRAY['Tasa'::character varying, 'Cuota'::character varying, 'Exento'::character varying])::text[])))),
	CONSTRAINT tax_transfer_pkey PRIMARY KEY (tax_transfer_uuid)
);
CREATE INDEX idx_tax_transfer_code ON tenant_fiscal.tax_transfer (tax_code);
CREATE INDEX idx_tax_transfer_tax_uuid ON tenant_fiscal.tax_transfer (tax_uuid);


-- tenant_fiscal.tax_withholding definition

-- Drop table

-- DROP TABLE tenant_fiscal.tax_withholding;

CREATE TABLE tenant_fiscal.tax_withholding (
	tax_withholding_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	tax_uuid uuid NOT NULL,
	tax_code varchar(3) NOT NULL,
	amount numeric(16, 2) NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_tax_withholding_code CHECK ((((tax_code)::text = ANY ((ARRAY['001'::character varying, '002'::character varying])::text[])))),
	CONSTRAINT tax_withholding_pkey PRIMARY KEY (tax_withholding_uuid)
);
CREATE INDEX idx_tax_withholding_code ON tenant_fiscal.tax_withholding (tax_code);
CREATE INDEX idx_tax_withholding_tax_uuid ON tenant_fiscal.tax_withholding (tax_uuid);


-- tenant_fiscal.totals definition

-- Drop table

-- DROP TABLE tenant_fiscal.totals;

CREATE TABLE tenant_fiscal.totals (
	totals_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	payments_uuid uuid NOT NULL,
	total_payments_amount numeric(16, 2) NOT NULL,
	total_base_iva_16 numeric(16, 2) DEFAULT 0 NULL,
	total_tax_iva_16 numeric(16, 2) DEFAULT 0 NULL,
	total_base_iva_8 numeric(16, 2) DEFAULT 0 NULL,
	total_tax_iva_8 numeric(16, 2) DEFAULT 0 NULL,
	total_base_iva_0 numeric(16, 2) DEFAULT 0 NULL,
	total_withholding_iva numeric(16, 2) DEFAULT 0 NULL,
	total_withholding_isr numeric(16, 2) DEFAULT 0 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_totals_amounts CHECK ((((total_payments_amount >= (0)::numeric) AND (total_base_iva_16 >= (0)::numeric) AND (total_tax_iva_16 >= (0)::numeric)))),
	CONSTRAINT totals_pkey PRIMARY KEY (totals_uuid)
);
CREATE INDEX idx_totals_payments_uuid ON tenant_fiscal.totals (payments_uuid);


-- tenant_fiscal.version_catalog definition

-- Drop table

-- DROP TABLE tenant_fiscal.version_catalog;

CREATE TABLE tenant_fiscal.version_catalog (
	version_id serial4 NOT NULL,
	"name" varchar(50) NOT NULL,
	description varchar(254) NULL,
	"version" numeric(7, 3) NOT NULL,
	document_type bpchar(1) NOT NULL,
	pac_id int4 NOT NULL,
	valid_from timestamp NULL,
	valid_to timestamp NULL,
	structure_url varchar(254) NULL,
	status int4 DEFAULT 1 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_version_document_type CHECK (((document_type = ANY (ARRAY['I'::bpchar, 'E'::bpchar, 'T'::bpchar, 'N'::bpchar, 'P'::bpchar])))),
	CONSTRAINT chk_version_status CHECK (((status = ANY (ARRAY[0, 1])))),
	CONSTRAINT chk_version_valid_dates CHECK ((((valid_from IS NULL) OR (valid_to IS NULL) OR (valid_from <= valid_to)))),
	CONSTRAINT version_catalog_pkey PRIMARY KEY (version_id)
);
CREATE INDEX idx_version_catalog_document_type ON tenant_fiscal.version_catalog (document_type);
CREATE INDEX idx_version_catalog_pac_id ON tenant_fiscal.version_catalog (pac_id);
CREATE INDEX idx_version_catalog_status ON tenant_fiscal.version_catalog (status);
CREATE INDEX idx_version_catalog_version ON tenant_fiscal.version_catalog ("version");


-- tenant_fiscal.addendum foreign keys

-- tenant_fiscal.authorized_receiver_catalog foreign keys

-- tenant_fiscal.equivalence_dr foreign keys

-- tenant_fiscal.flyway_schema_history foreign keys

-- tenant_fiscal.invoice foreign keys

-- tenant_fiscal.invoice_status_history foreign keys

-- tenant_fiscal.issuer foreign keys

-- tenant_fiscal.log foreign keys

-- tenant_fiscal.pac_catalog foreign keys

-- tenant_fiscal.payment foreign keys

-- tenant_fiscal.payments foreign keys

-- tenant_fiscal.payment_file_registry foreign keys

-- tenant_fiscal.payment_response_catalog foreign keys

-- tenant_fiscal.receiver foreign keys

-- tenant_fiscal.related_cfdi foreign keys

-- tenant_fiscal.related_documents foreign keys

-- tenant_fiscal.tax foreign keys

-- tenant_fiscal.tax_detail foreign keys

-- tenant_fiscal.tax_transfer foreign keys

-- tenant_fiscal.tax_withholding foreign keys

-- tenant_fiscal.totals foreign keys

-- tenant_fiscal.version_catalog foreign keys