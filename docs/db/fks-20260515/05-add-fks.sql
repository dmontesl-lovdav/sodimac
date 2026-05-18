-- Script idempotente: solo agrega FKs que faltan.
-- Verifica existencia por nombre antes de crear.

DO $$
DECLARE
  rec record;
  fks text[][] := ARRAY[
    -- tenant_fiscal
    ['fk_invoice_issuer',                  'tenant_fiscal',  'invoice',                     'issuer_uuid',          'tenant_fiscal',  'issuer',            'issuer_uuid'],
    ['fk_invoice_receiver',                'tenant_fiscal',  'invoice',                     'receiver_uuid',        'tenant_fiscal',  'receiver',          'receiver_uuid'],
    ['fk_tax_invoice',                     'tenant_fiscal',  'tax',                         'invoice_uuid',         'tenant_fiscal',  'invoice',           'invoice_uuid'],
    ['fk_tax_detail_tax',                  'tenant_fiscal',  'tax_detail',                  'tax_uuid',             'tenant_fiscal',  'tax',               'tax_uuid'],
    ['fk_tax_transfer_tax',                'tenant_fiscal',  'tax_transfer',                'tax_uuid',             'tenant_fiscal',  'tax',               'tax_uuid'],
    ['fk_tax_withholding_tax',             'tenant_fiscal',  'tax_withholding',             'tax_uuid',             'tenant_fiscal',  'tax',               'tax_uuid'],
    ['fk_related_cfdi_invoice',            'tenant_fiscal',  'related_cfdi',                'invoice_uuid',         'tenant_fiscal',  'invoice',           'invoice_uuid'],
    ['fk_related_cfdi_related_invoice',    'tenant_fiscal',  'related_cfdi',                'related_invoice_uuid', 'tenant_fiscal',  'invoice',           'invoice_uuid'],
    ['fk_addendum_invoice',                'tenant_fiscal',  'addendum',                    'invoice_uuid',         'tenant_fiscal',  'invoice',           'invoice_uuid'],
    ['fk_addendum_payments',               'tenant_fiscal',  'addendum',                    'payments_uuid',        'tenant_fiscal',  'payments',          'payments_uuid'],
    ['fk_invoice_status_history_invoice',  'tenant_fiscal',  'invoice_status_history',      'invoice_uuid',         'tenant_fiscal',  'invoice',           'invoice_uuid'],
    ['fk_payments_issuer',                 'tenant_fiscal',  'payments',                    'issuer_uuid',          'tenant_fiscal',  'issuer',            'issuer_uuid'],
    ['fk_payments_receiver',               'tenant_fiscal',  'payments',                    'receiver_uuid',        'tenant_fiscal',  'receiver',          'receiver_uuid'],
    ['fk_payment_payments',                'tenant_fiscal',  'payment',                     'payments_uuid',        'tenant_fiscal',  'payments',          'payments_uuid'],
    ['fk_totals_payments',                 'tenant_fiscal',  'totals',                      'payments_uuid',        'tenant_fiscal',  'payments',          'payments_uuid'],
    ['fk_related_documents_payment',       'tenant_fiscal',  'related_documents',           'payment_uuid',         'tenant_fiscal',  'payment',           'payment_uuid'],
    ['fk_related_documents_invoice',       'tenant_fiscal',  'related_documents',           'document_uuid',        'tenant_fiscal',  'invoice',           'invoice_uuid'],
    ['fk_equivalence_dr_related_documents','tenant_fiscal',  'equivalence_dr',              'related_document_uuid','tenant_fiscal',  'related_documents', 'related_document_uuid'],
    ['fk_authorized_receiver_receiver',    'tenant_fiscal',  'authorized_receiver_catalog', 'receiver_uuid',        'tenant_fiscal',  'receiver',          'receiver_uuid'],
    ['fk_version_catalog_pac_fis',         'tenant_fiscal',  'version_catalog',             'pac_id',               'tenant_fiscal',  'pac_catalog',       'pac_id'],
    ['fk_payment_file_registry_payments',  'tenant_fiscal',  'payment_file_registry',       'payments_uuid',        'tenant_fiscal',  'payments',          'payments_uuid'],
    -- tenant_finance faltantes
    ['fk_twm_logs_ejecucion',              'tenant_finance', 'twm_logs',                    'id_ejecucion',         'tenant_finance', 'twm_ejecucion',     'id'],
    ['fk_twm_cifras_ejecucion',            'tenant_finance', 'twm_cifras_control',          'id_ejecucion',         'tenant_finance', 'twm_ejecucion',     'id'],
    ['fk_stamped_rebate_invoice_fiscal',   'tenant_finance', 'stamped_rebate',              'invoice_fiscal_uuid',  'tenant_fiscal',  'invoice',           'fiscal_uuid'],
    ['fk_addendum_manual_invoice',         'tenant_finance', 'addendum_manual',             'invoice_uuid',         'tenant_fiscal',  'invoice',           'invoice_uuid']
  ];
  i int;
  fk_name text; src_sch text; src_tbl text; src_col text; tgt_sch text; tgt_tbl text; tgt_col text;
BEGIN
  FOR i IN 1..array_length(fks, 1) LOOP
    fk_name := fks[i][1]; src_sch := fks[i][2]; src_tbl := fks[i][3]; src_col := fks[i][4];
    tgt_sch := fks[i][5]; tgt_tbl := fks[i][6]; tgt_col := fks[i][7];
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = fk_name) THEN
      EXECUTE format('ALTER TABLE %I.%I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES %I.%I(%I)',
                     src_sch, src_tbl, fk_name, src_col, tgt_sch, tgt_tbl, tgt_col);
      RAISE NOTICE 'CREADO: %', fk_name;
    ELSE
      RAISE NOTICE 'YA EXISTE: %', fk_name;
    END IF;
  END LOOP;
END $$;

SELECT n.nspname AS schema, COUNT(*) AS fks
FROM pg_constraint c JOIN pg_class cl ON c.conrelid = cl.oid JOIN pg_namespace n ON cl.relnamespace = n.oid
WHERE c.contype = 'f' AND n.nspname IN ('tenant_fiscal','tenant_finance')
GROUP BY n.nspname ORDER BY n.nspname;
