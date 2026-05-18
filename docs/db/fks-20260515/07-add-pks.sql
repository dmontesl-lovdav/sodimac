-- ============================================================================
-- Agrega PRIMARY KEYs faltantes detectadas en UAT (script 06 confirma viabilidad).
-- Idempotente: solo agrega si la tabla NO tiene ya una PK.
-- Bloque PL/pgSQL con catch — si una falla, las demás siguen.
-- ============================================================================

DO $$
DECLARE
  pks text[][] := ARRAY[
    ['tenant_fiscal','tax','tax_uuid'],
    ['tenant_finance','account_statement','account_statement_uuid'],
    ['tenant_finance','pac_catalog','pac_id'],
    ['tenant_finance','payment_header','payment_header_uuid'],
    ['tenant_finance','purchase_order','purchase_order_uuid'],
    ['tenant_finance','reception','reception_id'],
    ['tenant_finance','shipping_guide','shipping_guide_id'],
    ['tenant_finance','twm_ejecucion','id']
  ];
  i int;
  sch text; tbl text; col text;
  has_pk boolean;
BEGIN
  FOR i IN 1..array_length(pks, 1) LOOP
    sch := pks[i][1]; tbl := pks[i][2]; col := pks[i][3];

    SELECT EXISTS(
      SELECT 1 FROM pg_constraint
      WHERE conrelid = (sch||'.'||tbl)::regclass
        AND contype = 'p'
    ) INTO has_pk;

    IF has_pk THEN
      RAISE NOTICE 'YA TIENE PK: %.%', sch, tbl;
    ELSE
      BEGIN
        -- Asegurar NOT NULL antes de la PK
        EXECUTE format('ALTER TABLE %I.%I ALTER COLUMN %I SET NOT NULL', sch, tbl, col);
        EXECUTE format('ALTER TABLE %I.%I ADD PRIMARY KEY (%I)', sch, tbl, col);
        RAISE NOTICE 'PK CREADA: %.%(%)', sch, tbl, col;
      EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'FALLO PK %.%(%): %', sch, tbl, col, SQLERRM;
      END;
    END IF;
  END LOOP;
END $$;

-- Verificación
SELECT n.nspname AS schema, cl.relname AS tabla, c.conname AS pk
FROM pg_constraint c
JOIN pg_class cl ON c.conrelid = cl.oid
JOIN pg_namespace n ON cl.relnamespace = n.oid
WHERE c.contype = 'p'
  AND n.nspname IN ('tenant_fiscal','tenant_finance')
  AND cl.relname IN ('tax','account_statement','pac_catalog','payment_header','purchase_order','reception','shipping_guide','twm_ejecucion')
ORDER BY schema, tabla;
