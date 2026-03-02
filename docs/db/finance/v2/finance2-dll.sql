-- tenant_finance.accounts_payable definition

-- Drop table

-- DROP TABLE tenant_finance.accounts_payable;

CREATE TABLE tenant_finance.accounts_payable (
	accounts_payable_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	company int4 NOT NULL,
	document_date date NOT NULL,
	reference_number varchar(100) NOT NULL,
	document_number varchar(100) NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	debit_credit varchar(1) NOT NULL,
	gl_account varchar(20) NOT NULL,
	vendor_number int4 NOT NULL,
	amount numeric(15, 2) NOT NULL,
	branch int4 NOT NULL,
	payment_term varchar(5) NOT NULL,
	due_date date NOT NULL,
	hold_indicator varchar(1) DEFAULT 'N'::character varying NOT NULL,
	source_system varchar(20) NOT NULL,
	sent_at timestamp NULL,
	posting_date date NOT NULL,
	document_class varchar(5) NOT NULL,
	reference_id varchar(40) NOT NULL,
	cost_center varchar(15) NOT NULL,
	profit_center varchar(15) NOT NULL,
	sent_flag int4 DEFAULT 0 NOT NULL,
	receipt_date date NOT NULL,
	document_type varchar(15) NOT NULL,
	etl_source varchar(20) NOT NULL,
	period_id int4 NOT NULL,
	resent_by int8 NULL,
	resent_at timestamp NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT accounts_payable_pkey PRIMARY KEY (accounts_payable_uuid),
	CONSTRAINT chk_accounts_payable_amount CHECK (((amount >= (0)::numeric))),
	CONSTRAINT chk_accounts_payable_debit_credit CHECK ((((debit_credit)::text = ANY ((ARRAY['D'::character varying, 'C'::character varying])::text[])))),
	CONSTRAINT chk_accounts_payable_exchange_rate CHECK (((exchange_rate > (0)::numeric))),
	CONSTRAINT chk_accounts_payable_hold_indicator CHECK ((((hold_indicator)::text = ANY ((ARRAY['Y'::character varying, 'N'::character varying])::text[])))),
	CONSTRAINT chk_accounts_payable_sent_flag CHECK (((sent_flag = ANY (ARRAY[0, 1]))))
);
CREATE INDEX idx_accounts_payable_vendor_number ON tenant_finance.accounts_payable (vendor_number);


-- tenant_finance.account_statement definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement;

CREATE TABLE tenant_finance.account_statement (
	account_statement_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	vendor_number int8 NOT NULL,
	"year" int4 NOT NULL,
	"month" int2 NOT NULL,
	"version" int4 DEFAULT 1 NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	initial_balance numeric(18, 2) DEFAULT 0 NOT NULL,
	final_balance numeric(18, 2) DEFAULT 0 NOT NULL,
	process_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	review_date timestamp NULL,
	issue_date timestamp NULL,
	period_start_date date NOT NULL,
	period_end_date date NOT NULL,
	previous_statement_uuid uuid NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_month_check CHECK ((((month >= 1) AND (month <= 12)))),
	CONSTRAINT account_statement_pkey PRIMARY KEY (account_statement_uuid),
	CONSTRAINT uq_account_statement_vendor_period UNIQUE (vendor_number,"year","month","version")
);
CREATE INDEX ix_account_statement_previous ON tenant_finance.account_statement (previous_statement_uuid);
CREATE INDEX ix_account_statement_published ON tenant_finance.account_statement (vendor_number,"year","month");
CREATE INDEX ix_account_statement_status ON tenant_finance.account_statement (status);
CREATE INDEX ix_account_statement_vendor_period ON tenant_finance.account_statement (vendor_number,"year","month","version");


-- tenant_finance.account_statement_credit_note definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement_credit_note;

CREATE TABLE tenant_finance.account_statement_credit_note (
	account_statement_credit_note_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_statement_uuid uuid NOT NULL,
	document_number varchar(50) NULL,
	series varchar(50) NULL,
	folio varchar(50) NULL,
	"uuid" varchar(64) NOT NULL,
	issue_date timestamp NULL,
	accounting_date timestamp NULL,
	currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	amount numeric(18, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	base_currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	base_amount numeric(18, 2) NOT NULL,
	status varchar(50) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_credit_note_pkey PRIMARY KEY (account_statement_credit_note_uuid)
);
CREATE INDEX ix_account_statement_credit_note_statement ON tenant_finance.account_statement_credit_note (account_statement_uuid);
CREATE INDEX ix_account_statement_credit_note_uuid ON tenant_finance.account_statement_credit_note ("uuid");


-- tenant_finance.account_statement_discount definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement_discount;

CREATE TABLE tenant_finance.account_statement_discount (
	account_statement_discount_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_statement_uuid uuid NOT NULL,
	document_number varchar(50) NULL,
	reference_number varchar(50) NULL,
	series varchar(50) NULL,
	folio varchar(50) NULL,
	"uuid" varchar(64) NULL,
	discount_date timestamp NULL,
	accounting_date timestamp NULL,
	currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	amount numeric(18, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	base_currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	base_amount numeric(18, 2) NOT NULL,
	status varchar(50) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_discount_pkey PRIMARY KEY (account_statement_discount_uuid)
);
CREATE INDEX ix_account_statement_discount_statement ON tenant_finance.account_statement_discount (account_statement_uuid);


-- tenant_finance.account_statement_invoice definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement_invoice;

CREATE TABLE tenant_finance.account_statement_invoice (
	account_statement_invoice_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_statement_uuid uuid NOT NULL,
	invoice_type varchar(20) NOT NULL,
	series varchar(50) NULL,
	folio varchar(50) NULL,
	"uuid" varchar(64) NULL,
	stamp_date timestamp NULL,
	accounting_date timestamp NULL,
	payment_date timestamp NULL,
	currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	amount numeric(18, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	base_currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	base_amount numeric(18, 2) NOT NULL,
	invoice_status varchar(50) NULL,
	payment_id int8 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_invoice_invoice_type_check CHECK ((((invoice_type)::text = ANY ((ARRAY['PENDING'::character varying, 'PAID'::character varying])::text[])))),
	CONSTRAINT account_statement_invoice_pkey PRIMARY KEY (account_statement_invoice_uuid)
);
CREATE INDEX ix_account_statement_invoice_statement ON tenant_finance.account_statement_invoice (account_statement_uuid);
CREATE INDEX ix_account_statement_invoice_type ON tenant_finance.account_statement_invoice (account_statement_uuid,invoice_type);
CREATE INDEX ix_account_statement_invoice_uuid ON tenant_finance.account_statement_invoice ("uuid");


-- tenant_finance.account_statement_payment definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement_payment;

CREATE TABLE tenant_finance.account_statement_payment (
	account_statement_payment_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_statement_uuid uuid NOT NULL,
	payment_id int8 NULL,
	document_number varchar(50) NULL,
	reference_number varchar(50) NULL,
	payment_date timestamp NULL,
	currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	amount numeric(18, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	base_currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	base_amount numeric(18, 2) NOT NULL,
	status varchar(50) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_payment_pkey PRIMARY KEY (account_statement_payment_uuid)
);
CREATE INDEX ix_account_statement_payment_ref ON tenant_finance.account_statement_payment (payment_id);
CREATE INDEX ix_account_statement_payment_statement ON tenant_finance.account_statement_payment (account_statement_uuid);


-- tenant_finance.account_statement_purchase_order definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement_purchase_order;

CREATE TABLE tenant_finance.account_statement_purchase_order (
	account_statement_purchase_order_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_statement_uuid uuid NOT NULL,
	order_number int8 NOT NULL,
	document_date date NULL,
	due_date date NULL,
	currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	amount numeric(18, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	base_currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	base_amount numeric(18, 2) NOT NULL,
	status varchar(50) NULL,
	source_id int8 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_purchase_order_pkey PRIMARY KEY (account_statement_purchase_order_uuid)
);
CREATE INDEX ix_account_statement_po_number ON tenant_finance.account_statement_purchase_order (order_number);
CREATE INDEX ix_account_statement_po_statement ON tenant_finance.account_statement_purchase_order (account_statement_uuid);


-- tenant_finance.account_statement_reception definition

-- Drop table

-- DROP TABLE tenant_finance.account_statement_reception;

CREATE TABLE tenant_finance.account_statement_reception (
	account_statement_reception_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	account_statement_uuid uuid NOT NULL,
	reception_number int8 NOT NULL,
	order_number int8 NOT NULL,
	document_date date NULL,
	reception_date date NULL,
	due_date date NULL,
	currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	amount numeric(18, 2) NOT NULL,
	exchange_rate numeric(18, 6) DEFAULT 1 NOT NULL,
	base_currency varchar(10) DEFAULT 'MXN'::character varying NOT NULL,
	base_amount numeric(18, 2) NOT NULL,
	status varchar(50) NULL,
	source_id int8 NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT account_statement_reception_pkey PRIMARY KEY (account_statement_reception_uuid)
);
CREATE INDEX ix_account_statement_reception_number ON tenant_finance.account_statement_reception (reception_number);
CREATE INDEX ix_account_statement_reception_order ON tenant_finance.account_statement_reception (order_number);
CREATE INDEX ix_account_statement_reception_statement ON tenant_finance.account_statement_reception (account_statement_uuid);


-- tenant_finance.activity_logs definition

-- Drop table

-- DROP TABLE tenant_finance.activity_logs;

CREATE TABLE tenant_finance.activity_logs (
	activity_logs_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
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
	CONSTRAINT activity_logs_pkey PRIMARY KEY (activity_logs_uuid)
);


-- tenant_finance.addendum_manual definition

-- Drop table

-- DROP TABLE tenant_finance.addendum_manual;

CREATE TABLE tenant_finance.addendum_manual (
	addendum_manual_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	invoice_uuid uuid NULL,
	supplier_number numeric(10) NULL,
	reception_id uuid NOT NULL,
	purchase_order_number varchar(50) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	supplier_type_id int4 NULL,
	user_id int8 NULL,
	CONSTRAINT addendum_manual_pkey PRIMARY KEY (addendum_manual_uuid),
	CONSTRAINT chk_addendum_manual_reference CHECK (((invoice_uuid IS NOT NULL)))
);
CREATE INDEX idx_addendum_manual_invoice_uuid ON tenant_finance.addendum_manual (invoice_uuid);
CREATE INDEX idx_addendum_manual_purchase_order ON tenant_finance.addendum_manual (purchase_order_number);
CREATE INDEX idx_addendum_manual_supplier_number ON tenant_finance.addendum_manual (supplier_number);
CREATE INDEX idx_addendum_manual_user_id ON tenant_finance.addendum_manual (user_id);


-- tenant_finance.finanzas_payments definition

-- Drop table

-- DROP TABLE tenant_finance.finanzas_payments;

CREATE TABLE tenant_finance.finanzas_payments (
	finanzas_payment_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	company int4 NOT NULL,
	document_number varchar(100) NOT NULL,
	document_reference varchar(100) NOT NULL,
	vendor_number int4 NOT NULL,
	amount numeric(15, 2) NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NOT NULL,
	document_type varchar(5) NOT NULL,
	sap_document varchar(50) NOT NULL,
	payment_date date NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	id_referencia_pago uuid NULL,
	CONSTRAINT chk_finanzas_payments_amount CHECK (((amount >= (0)::numeric))),
	CONSTRAINT chk_finanzas_payments_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3])))),
	CONSTRAINT finanzas_payments_pkey PRIMARY KEY (finanzas_payment_uuid)
);
CREATE INDEX idx_finanzas_payments_date ON tenant_finance.finanzas_payments (payment_date);
CREATE INDEX idx_finanzas_payments_document ON tenant_finance.finanzas_payments (document_number);
CREATE INDEX idx_finanzas_payments_id_referencia_pago ON tenant_finance.finanzas_payments (id_referencia_pago);
CREATE INDEX idx_finanzas_payments_sap_doc ON tenant_finance.finanzas_payments (sap_document);
CREATE INDEX idx_finanzas_payments_status ON tenant_finance.finanzas_payments (status);
CREATE INDEX idx_finanzas_payments_vendor ON tenant_finance.finanzas_payments (vendor_number);


-- tenant_finance.finanzas_payment_headers definition

-- Drop table

-- DROP TABLE tenant_finance.finanzas_payment_headers;

CREATE TABLE tenant_finance.finanzas_payment_headers (
	id_referencia_pago uuid DEFAULT gen_random_uuid() NOT NULL,
	company int4 NOT NULL,
	anio int4 NOT NULL,
	vendor_number int4 NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NOT NULL,
	importe numeric(15, 2) NOT NULL,
	payment_date date NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT now() NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT finanzas_payment_headers_pkey PRIMARY KEY (id_referencia_pago)
);
CREATE INDEX idx_finanzas_payment_headers_company_anio ON tenant_finance.finanzas_payment_headers (company,anio);
CREATE INDEX idx_finanzas_payment_headers_vendor_payment_date ON tenant_finance.finanzas_payment_headers (vendor_number,payment_date);


-- tenant_finance.fiscal_payments definition

-- Drop table

-- DROP TABLE tenant_finance.fiscal_payments;

CREATE TABLE tenant_finance.fiscal_payments (
	fiscal_payment_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	payment_number varchar(50) NOT NULL,
	company int4 NOT NULL,
	document_number varchar(100) NOT NULL,
	reference_number varchar(100) NOT NULL,
	vendor_number int4 NOT NULL,
	amount numeric(15, 2) NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NOT NULL,
	document_type varchar(5) NOT NULL,
	sap_document varchar(50) NULL,
	payment_date date NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	payment_method varchar(10) NULL,
	bank_account varchar(50) NULL,
	reference_payment varchar(100) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_fiscal_payments_amount CHECK (((amount >= (0)::numeric))),
	CONSTRAINT chk_fiscal_payments_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3])))),
	CONSTRAINT fiscal_payments_pkey PRIMARY KEY (fiscal_payment_uuid),
	CONSTRAINT uq_fiscal_payments_number UNIQUE (payment_number)
);
CREATE INDEX idx_fiscal_payments_vendor_number ON tenant_finance.fiscal_payments (vendor_number);


-- tenant_finance.migrations definition

-- Drop table

-- DROP TABLE tenant_finance.migrations;

CREATE TABLE tenant_finance.migrations (
	id serial4 NOT NULL,
	"timestamp" int8 NOT NULL,
	"name" varchar NOT NULL,
	CONSTRAINT "PK_8c82d7f526340ab734260ea46be" PRIMARY KEY (id)
);


-- tenant_finance.pac_catalog definition

-- Drop table

-- DROP TABLE tenant_finance.pac_catalog;

CREATE TABLE tenant_finance.pac_catalog (
	pac_id serial4 NOT NULL,
	"name" varchar(50) NOT NULL,
	description varchar(254) NULL,
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
CREATE UNIQUE INDEX idx_pac_catalog_name ON tenant_finance.pac_catalog ("name");
CREATE INDEX idx_pac_catalog_status ON tenant_finance.pac_catalog (status);


-- tenant_finance.purchase_order definition

-- Drop table

-- DROP TABLE tenant_finance.purchase_order;

CREATE TABLE tenant_finance.purchase_order (
	purchase_order_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	order_number varchar(50) NOT NULL,
	vendor_number int4 NOT NULL,
	source_id int4 NOT NULL,
	total_amount numeric(15, 2) NOT NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	order_date date NOT NULL,
	delivery_date date NULL,
	terms_and_conditions text NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_purchase_order_amount CHECK (((total_amount >= (0)::numeric))),
	CONSTRAINT chk_purchase_order_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3, 4])))),
	CONSTRAINT purchase_order_pkey PRIMARY KEY (purchase_order_uuid),
	CONSTRAINT uq_purchase_order_number UNIQUE (order_number)
);
CREATE INDEX idx_purchase_order_vendor_number ON tenant_finance.purchase_order (vendor_number);


-- tenant_finance.rebate definition

-- Drop table

-- DROP TABLE tenant_finance.rebate;

CREATE TABLE tenant_finance.rebate (
	rebate_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	document_number varchar(100) NOT NULL,
	reference_number varchar(100) NOT NULL,
	sap_document varchar(50) NOT NULL,
	vendor_number int4 NOT NULL,
	amount numeric(15, 2) NOT NULL,
	"source" int4 NOT NULL,
	period_id int4 NOT NULL,
	due_date date NOT NULL,
	posting_date date NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_rebate_amount CHECK (((amount >= (0)::numeric))),
	CONSTRAINT chk_rebate_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3])))),
	CONSTRAINT rebate_pkey PRIMARY KEY (rebate_uuid)
);


-- tenant_finance.reception definition

-- Drop table

-- DROP TABLE tenant_finance.reception;

CREATE TABLE tenant_finance.reception (
	reception_id uuid DEFAULT gen_random_uuid() NOT NULL,
	purchase_order_uuid uuid NULL,
	origin_id numeric(3) NULL,
	destination_id numeric(3) NULL,
	amount numeric(16, 2) NULL,
	status numeric(2) NULL,
	"comment" varchar(256) NULL,
	reception_date date NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	reception_number varchar(50) NULL,
	guide_number varchar(50) NULL,
	CONSTRAINT reception_pkey PRIMARY KEY (reception_id)
);
CREATE INDEX idx_reception_created_at ON tenant_finance.reception (created_at);
CREATE INDEX idx_reception_date ON tenant_finance.reception (reception_date);
CREATE INDEX idx_reception_purchase_order ON tenant_finance.reception (purchase_order_uuid);
CREATE INDEX idx_reception_status ON tenant_finance.reception (status);


-- tenant_finance.reception_sku definition

-- Drop table

-- DROP TABLE tenant_finance.reception_sku;

CREATE TABLE tenant_finance.reception_sku (
	reception_sku_id uuid DEFAULT gen_random_uuid() NOT NULL,
	reception_id uuid NOT NULL,
	sku varchar(15) NOT NULL,
	description varchar(256) NOT NULL,
	quantity numeric(20, 6) NULL,
	unit_cost numeric(16, 2) NULL,
	total_cost numeric(26, 2) NULL,
	status numeric(2) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT reception_sku_pkey PRIMARY KEY (reception_sku_id)
);
CREATE INDEX idx_reception_sku_reception_id ON tenant_finance.reception_sku (reception_id);
CREATE INDEX idx_reception_sku_sku ON tenant_finance.reception_sku (sku);


-- tenant_finance.sap_document definition

-- Drop table

-- DROP TABLE tenant_finance.sap_document;

CREATE TABLE tenant_finance.sap_document (
	sap_document_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	document_number varchar(100) NOT NULL,
	reference_number varchar(100) NOT NULL,
	vendor_number int4 NOT NULL,
	amount numeric(15, 2) NOT NULL,
	"source" int4 NOT NULL,
	doc_sap varchar(15) NOT NULL,
	message varchar(254) NULL,
	sap_status int4 DEFAULT 1 NOT NULL,
	document_type varchar(5) NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_sap_document_amount CHECK (((amount >= (0)::numeric))),
	CONSTRAINT chk_sap_document_status CHECK (((sap_status = ANY (ARRAY[0, 1, 2, 3])))),
	CONSTRAINT sap_document_pkey PRIMARY KEY (sap_document_uuid),
	CONSTRAINT uq_sap_document_ref UNIQUE (document_number,reference_number)
);


-- tenant_finance.shipping_guide definition

-- Drop table

-- DROP TABLE tenant_finance.shipping_guide;

CREATE TABLE tenant_finance.shipping_guide (
	shipping_guide_id uuid DEFAULT uuid_generate_v4() NOT NULL,
	guide_number varchar(50) NOT NULL,
	vendor_number int4 NOT NULL,
	truck_plate varchar(20) NULL,
	trailer_plate varchar(20) NULL,
	driver_name varchar(100) NULL,
	driver_license varchar(50) NULL,
	source_id int4 NOT NULL,
	destination_id int4 NULL,
	delivery_type int4 NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	"comments" text NULL,
	delivery_date date NOT NULL,
	estimated_arrival timestamp NULL,
	actual_arrival timestamp NULL,
	sent_at timestamp NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	is_status_updated bool NULL,
	CONSTRAINT chk_shipping_guide_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3, 4, 5, 6, 7, 8, 9])))),
	CONSTRAINT shipping_guide_pkey PRIMARY KEY (shipping_guide_id),
	CONSTRAINT uq_shipping_guide_number UNIQUE (guide_number)
);
CREATE INDEX idx01_is_status_updated ON tenant_finance.shipping_guide (is_status_updated);
CREATE INDEX idx_shipping_guide_vendor_number ON tenant_finance.shipping_guide (vendor_number);


-- tenant_finance.shipping_guide_document definition

-- Drop table

-- DROP TABLE tenant_finance.shipping_guide_document;

CREATE TABLE tenant_finance.shipping_guide_document (
	shipping_guide_document_id uuid DEFAULT gen_random_uuid() NOT NULL,
	shipping_guide_id uuid NULL,
	file_name varchar(100) NULL,
	file_type int2 NULL,
	status numeric(2) NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT shipping_guide_document_pkey PRIMARY KEY (shipping_guide_document_id)
);
CREATE INDEX idx_shipping_guide_document_guide_id ON tenant_finance.shipping_guide_document (shipping_guide_id);
CREATE INDEX idx_shipping_guide_document_status ON tenant_finance.shipping_guide_document (status);


-- tenant_finance.shipping_guide_purchase_order definition

-- Drop table

-- DROP TABLE tenant_finance.shipping_guide_purchase_order;

CREATE TABLE tenant_finance.shipping_guide_purchase_order (
	shipping_guide_purchase_order_id uuid DEFAULT gen_random_uuid() NOT NULL,
	shipping_guide_id uuid NOT NULL,
	purchase_order_uuid uuid NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT shipping_guide_purchase_order_pkey PRIMARY KEY (shipping_guide_purchase_order_id),
	CONSTRAINT uq_shipping_guide_purchase_order UNIQUE (shipping_guide_id,purchase_order_uuid)
);
CREATE INDEX idx_shipping_guide_po_guide_id ON tenant_finance.shipping_guide_purchase_order (shipping_guide_id);
CREATE INDEX idx_shipping_guide_po_order_id ON tenant_finance.shipping_guide_purchase_order (purchase_order_uuid);


-- tenant_finance.stamped_rebate definition

-- Drop table

-- DROP TABLE tenant_finance.stamped_rebate;

CREATE TABLE tenant_finance.stamped_rebate (
	stamped_rebate_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	document_number varchar(100) NOT NULL,
	reference_number varchar(100) NOT NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	invoice_fiscal_uuid uuid NULL,
	CONSTRAINT chk_stamped_rebate_status CHECK (((status = ANY (ARRAY[0, 1, 2, 3])))),
	CONSTRAINT stamped_rebate_pkey PRIMARY KEY (stamped_rebate_uuid),
	CONSTRAINT uq_stamped_rebate_doc UNIQUE (document_number)
);


-- tenant_finance.three_way_match definition

-- Drop table

-- DROP TABLE tenant_finance.three_way_match;

CREATE TABLE tenant_finance.three_way_match (
	three_way_match_uuid uuid DEFAULT gen_random_uuid() NOT NULL,
	vendor_number int4 NOT NULL,
	purchase_order_number varchar(50) NOT NULL,
	purchase_order_date date NULL,
	purchase_order_amount numeric(15, 2) NULL,
	reception_number varchar(50) NULL,
	reception_date date NULL,
	reception_amount numeric(16, 2) NULL,
	invoice_series varchar(25) NULL,
	invoice_folio varchar(49) NULL,
	invoice_uuid uuid NULL,
	invoice_stamp_date timestamp NULL,
	invoice_amount numeric(16, 2) NULL,
	credit_note_number varchar(50) NULL,
	credit_note_amount numeric(16, 2) NULL,
	document_number varchar(100) NULL,
	sap_document varchar(50) NULL,
	accounting_date date NULL,
	accounting_amount numeric(15, 2) NULL,
	payment_reference varchar(100) NULL,
	payment_date date NULL,
	payment_amount numeric(15, 2) NULL,
	currency varchar(3) DEFAULT 'MXN'::character varying NULL,
	exchange_rate numeric(18, 6) DEFAULT 1.000000 NULL,
	status int4 DEFAULT 1 NOT NULL,
	created_by int8 NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT three_way_match_pkey PRIMARY KEY (three_way_match_uuid),
	CONSTRAINT uq_twm_vendor_po_reception UNIQUE (vendor_number,purchase_order_number,reception_number)
);
CREATE INDEX ix_twm_created_at ON tenant_finance.three_way_match (created_at DESC);
CREATE INDEX ix_twm_invoice_uuid ON tenant_finance.three_way_match (invoice_uuid);
CREATE INDEX ix_twm_purchase_order_number ON tenant_finance.three_way_match (purchase_order_number);
CREATE INDEX ix_twm_reception_number ON tenant_finance.three_way_match (reception_number);
CREATE INDEX ix_twm_sap_document ON tenant_finance.three_way_match (sap_document);
CREATE INDEX ix_twm_status ON tenant_finance.three_way_match (status);
CREATE INDEX ix_twm_vendor_number ON tenant_finance.three_way_match (vendor_number);
CREATE INDEX ix_twm_vendor_status ON tenant_finance.three_way_match (vendor_number,status);


-- tenant_finance.twm_cifras_control definition

-- Drop table

-- DROP TABLE tenant_finance.twm_cifras_control;

CREATE TABLE tenant_finance.twm_cifras_control (
	id uuid DEFAULT gen_random_uuid() NOT NULL,
	id_ejecucion uuid NOT NULL,
	paso varchar(10) NOT NULL,
	total_registros int4 NOT NULL,
	total_monto numeric(18, 2) NULL,
	detalle_json jsonb NULL,
	fecha_registro timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT twm_cifras_control_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_twm_cifras_ejecucion ON tenant_finance.twm_cifras_control (id_ejecucion);


-- tenant_finance.twm_ejecucion definition

-- Drop table

-- DROP TABLE tenant_finance.twm_ejecucion;

CREATE TABLE tenant_finance.twm_ejecucion (
	id uuid DEFAULT gen_random_uuid() NOT NULL,
	estado varchar(20) NOT NULL,
	fechainicio timestamp NOT NULL,
	fechafin timestamp NULL,
	intento int4 NOT NULL,
	fechabase date NOT NULL,
	CONSTRAINT twm_ejecucion_pkey PRIMARY KEY (id)
);


-- tenant_finance.twm_logs definition

-- Drop table

-- DROP TABLE tenant_finance.twm_logs;

CREATE TABLE tenant_finance.twm_logs (
	id uuid DEFAULT gen_random_uuid() NOT NULL,
	id_ejecucion uuid NOT NULL,
	severidad varchar(20) NOT NULL,
	codigo_mensaje varchar(100) NOT NULL,
	mensaje_params jsonb NULL,
	stack_trace text NULL,
	fecha_hora timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT twm_logs_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_twm_logs_ejecucion ON tenant_finance.twm_logs (id_ejecucion);


-- tenant_finance.vendor_block definition

-- Drop table

-- DROP TABLE tenant_finance.vendor_block;

CREATE TABLE tenant_finance.vendor_block (
	vendor_block_uuid uuid DEFAULT uuid_generate_v4() NOT NULL,
	vendor_number int4 NOT NULL,
	block_reason varchar(10) NOT NULL,
	block_description text NULL,
	start_date date NOT NULL,
	end_date date NULL,
	status int4 DEFAULT 1 NOT NULL,
	auto_unblock bool DEFAULT false NOT NULL,
	block_type varchar(5) NOT NULL,
	created_by int8 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_by int8 NULL,
	updated_at timestamp NULL,
	CONSTRAINT chk_vendor_block_dates CHECK ((((end_date IS NULL) OR (start_date <= end_date)))),
	CONSTRAINT chk_vendor_block_status CHECK (((status = ANY (ARRAY[0, 1, 2])))),
	CONSTRAINT chk_vendor_block_type CHECK ((((block_type)::text = ANY ((ARRAY['TEMP'::character varying, 'PERM'::character varying, 'PART'::character varying])::text[])))),
	CONSTRAINT vendor_block_pkey PRIMARY KEY (vendor_block_uuid)
);
CREATE INDEX idx_vendor_block_vendor_number ON tenant_finance.vendor_block (vendor_number);


-- tenant_finance.version_catalog definition

-- Drop table

-- DROP TABLE tenant_finance.version_catalog;

CREATE TABLE tenant_finance.version_catalog (
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
CREATE INDEX idx_version_catalog_pac_id ON tenant_finance.version_catalog (pac_id);
CREATE INDEX idx_version_catalog_status ON tenant_finance.version_catalog (status);


-- tenant_finance.accounts_payable foreign keys

-- tenant_finance.account_statement foreign keys

-- tenant_finance.account_statement_credit_note foreign keys

-- tenant_finance.account_statement_discount foreign keys

-- tenant_finance.account_statement_invoice foreign keys

-- tenant_finance.account_statement_payment foreign keys

-- tenant_finance.account_statement_purchase_order foreign keys

-- tenant_finance.account_statement_reception foreign keys

-- tenant_finance.activity_logs foreign keys

-- tenant_finance.addendum_manual foreign keys

-- tenant_finance.finanzas_payments foreign keys

-- tenant_finance.finanzas_payment_headers foreign keys

-- tenant_finance.fiscal_payments foreign keys

-- tenant_finance.migrations foreign keys

-- tenant_finance.pac_catalog foreign keys

-- tenant_finance.purchase_order foreign keys

-- tenant_finance.rebate foreign keys

-- tenant_finance.reception foreign keys

-- tenant_finance.reception_sku foreign keys

-- tenant_finance.sap_document foreign keys

-- tenant_finance.shipping_guide foreign keys

-- tenant_finance.shipping_guide_document foreign keys

-- tenant_finance.shipping_guide_purchase_order foreign keys

-- tenant_finance.stamped_rebate foreign keys

-- tenant_finance.three_way_match foreign keys

-- tenant_finance.twm_cifras_control foreign keys

-- tenant_finance.twm_ejecucion foreign keys

-- tenant_finance.twm_logs foreign keys

-- tenant_finance.vendor_block foreign keys

-- tenant_finance.version_catalog foreign keys