-- ===============================
-- SCRIPT PARA GENERAR 1 CASO DE PRUEBA COMPLETO
-- Sistema de Facturacion Electronica - Modulo Fiscal
-- ===============================

-- NOTA: Este script genera 1 caso completo de prueba con integridad referencial
-- Incluye: 1 payment complement, 1 invoice, 1 payment, 1 related_document, 1 total

-- Configurar esquema
SET search_path TO tenant_fiscal;

-- ===============================
-- CASO DE PRUEBA COMPLETO
-- ===============================

DO $$
DECLARE
    v_issuer_uuid UUID;
    v_receiver_uuid UUID;
    v_payments_uuid UUID;
    v_payment_uuid UUID;
    v_invoice_uuid UUID;
    v_fiscal_uuid_payment UUID;
    v_fiscal_uuid_invoice UUID;
    v_totals_uuid UUID;
    v_related_doc_uuid UUID;
    v_folio_payment VARCHAR(49);
    v_folio_invoice VARCHAR(49);
    v_payment_date DATE;
    v_invoice_total NUMERIC(16,2);
    v_payment_amount NUMERIC(16,2);
BEGIN
    RAISE NOTICE '=== INICIANDO CREACION DE CASO DE PRUEBA ===';

    -- Obtener emisor y receptor existentes
    SELECT issuer_uuid INTO v_issuer_uuid FROM tenant_fiscal.issuer WHERE rfc = 'SOD970101ABC' LIMIT 1;
    SELECT receiver_uuid INTO v_receiver_uuid FROM tenant_fiscal.receiver WHERE rfc = 'CGE990101GHI' LIMIT 1;

    IF v_issuer_uuid IS NULL THEN
        RAISE EXCEPTION 'No se encontró emisor con RFC SOD970101ABC';
    END IF;

    IF v_receiver_uuid IS NULL THEN
        RAISE EXCEPTION 'No se encontró receptor con RFC CGE990101GHI';
    END IF;

    RAISE NOTICE 'Emisor UUID: %', v_issuer_uuid;
    RAISE NOTICE 'Receptor UUID: %', v_receiver_uuid;

    -- Generar UUIDs y folios únicos
    v_payments_uuid := gen_random_uuid();
    v_payment_uuid := gen_random_uuid();
    v_invoice_uuid := gen_random_uuid();
    v_fiscal_uuid_payment := gen_random_uuid();
    v_fiscal_uuid_invoice := gen_random_uuid();
    v_totals_uuid := gen_random_uuid();
    v_related_doc_uuid := gen_random_uuid();

    v_folio_payment := 'TEST-PAY-' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS');
    v_folio_invoice := 'TEST-INV-' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS');

    v_payment_date := CURRENT_DATE - INTERVAL '5 days';
    v_invoice_total := 11600.00;  -- Base: 10000 + IVA 16%: 1600
    v_payment_amount := 5000.00;  -- Pago parcial

    RAISE NOTICE '';
    RAISE NOTICE '[1/5] Creando INVOICE de prueba...';

    -- 1. Crear INVOICE (factura que será pagada)
    INSERT INTO tenant_fiscal.invoice (
        invoice_uuid,
        fiscal_uuid,
        place_of_issue,
        payment_method,
        document_type,
        total,
        exchange_rate,
        currency,
        discount,
        subtotal,
        payment_conditions,
        payment_form,
        issue_date,
        certification_date,
        folio,
        series,
        version,
        xml_content,
        status,
        issuer_uuid,
        receiver_uuid,
        created_by,
        created_at
    ) VALUES (
        v_invoice_uuid,
        v_fiscal_uuid_invoice,
        '01000',
        'PPD',  -- Pago en Parcialidades o Diferido
        'I',    -- Ingreso
        v_invoice_total,  -- total = subtotal (sin descuento para cumplir constraint)
        1.000000,
        'MXN',
        0.00,
        v_invoice_total,  -- subtotal = total (constraint: total = subtotal - discount)
        '30 dias',
        '99',
        v_payment_date - INTERVAL '30 days',
        NOW() - INTERVAL '30 days',
        v_folio_invoice,
        'TEST',
        4.000,
        '<cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4" Version="4.0" Folio="' || v_folio_invoice || '" Serie="TEST" TipoDeComprobante="I"></cfdi:Comprobante>',
        1,
        v_issuer_uuid,
        v_receiver_uuid,
        1,
        NOW()
    );

    RAISE NOTICE '    ✓ Invoice creada: %', v_invoice_uuid;
    RAISE NOTICE '    - Folio: %', v_folio_invoice;
    RAISE NOTICE '    - Total: $%', v_invoice_total;

    RAISE NOTICE '';
    RAISE NOTICE '[2/5] Creando PAYMENTS (complemento de pago)...';

    -- 2. Crear PAYMENTS (complemento de pago)
    INSERT INTO tenant_fiscal.payments (
        payments_uuid,
        fiscal_uuid,
        version,
        payment_date,
        certification_date,
        issuer_uuid,
        receiver_uuid,
        folio,
        series,
        xml_content,
        status,
        created_by,
        created_at
    ) VALUES (
        v_payments_uuid,
        v_fiscal_uuid_payment,
        2.000,
        v_payment_date,
        NOW(),
        v_issuer_uuid,
        v_receiver_uuid,
        v_folio_payment,
        'PP',
        '<cfdi:Comprobante xmlns:cfdi="http://www.sat.gob.mx/cfd/4" xmlns:pago20="http://www.sat.gob.mx/Pagos20" Version="4.0" Folio="' || v_folio_payment || '" Serie="PP" TipoDeComprobante="P"></cfdi:Comprobante>',
        1,
        1,
        NOW()
    );

    RAISE NOTICE '    ✓ Payments creado: %', v_payments_uuid;
    RAISE NOTICE '    - Folio: %', v_folio_payment;
    RAISE NOTICE '    - UUID Fiscal SAT: %', v_fiscal_uuid_payment;

    RAISE NOTICE '';
    RAISE NOTICE '[3/5] Creando PAYMENT (pago individual)...';

    -- 3. Crear PAYMENT (pago individual dentro del complemento)
    INSERT INTO tenant_fiscal.payment (
        payment_uuid,
        payments_uuid,
        payment_date,
        payment_method,
        currency,
        amount,
        operation_number,
        exchange_rate,
        payer_bank_rfc,
        payer_account,
        beneficiary_bank_rfc,
        beneficiary_account,
        created_by,
        created_at
    ) VALUES (
        v_payment_uuid,
        v_payments_uuid,
        v_payment_date,
        '03',  -- Transferencia electrónica
        'MXN',
        v_payment_amount,
        'OP-TEST-' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS'),
        1.000000,
        'BNM840515VB1',  -- Banco Nacional de Mexico
        '001234567890123456',
        'BBC940707IE1',  -- BBVA Bancomer
        '009876543210987654',
        1,
        NOW()
    );

    RAISE NOTICE '    ✓ Payment creado: %', v_payment_uuid;
    RAISE NOTICE '    - Monto: $%', v_payment_amount;
    RAISE NOTICE '    - Método: 03 (Transferencia)';

    RAISE NOTICE '';
    RAISE NOTICE '[4/5] Creando RELATED_DOCUMENTS (vinculación pago-factura)...';

    -- 4. Crear RELATED_DOCUMENTS (vincular pago con invoice)
    INSERT INTO tenant_fiscal.related_documents (
        related_document_uuid,
        payment_uuid,
        document_uuid,
        amount_paid,
        previous_balance,
        remaining_balance,
        installment_number,
        series,
        folio,
        currency,
        exchange_rate,
        created_by,
        created_at
    ) VALUES (
        v_related_doc_uuid,
        v_payment_uuid,
        v_invoice_uuid,
        v_payment_amount,
        v_invoice_total,
        v_invoice_total - v_payment_amount,  -- Saldo restante
        1,  -- Primera parcialidad
        'TEST',
        v_folio_invoice,
        'MXN',
        1.000000,
        1,
        NOW()
    );

    RAISE NOTICE '    ✓ Related document creado: %', v_related_doc_uuid;
    RAISE NOTICE '    - Saldo anterior: $%', v_invoice_total;
    RAISE NOTICE '    - Monto pagado: $%', v_payment_amount;
    RAISE NOTICE '    - Saldo restante: $%', v_invoice_total - v_payment_amount;

    RAISE NOTICE '';
    RAISE NOTICE '[5/5] Creando TOTALS (totales del complemento)...';

    -- 5. Crear TOTALS (totales del complemento de pago)
    INSERT INTO tenant_fiscal.totals (
        totals_uuid,
        payments_uuid,
        total_payments_amount,
        total_base_iva_16,
        total_tax_iva_16,
        total_base_iva_8,
        total_tax_iva_8,
        total_base_iva_0,
        total_withholding_iva,
        total_withholding_isr,
        created_by,
        created_at
    ) VALUES (
        v_totals_uuid,
        v_payments_uuid,
        v_payment_amount,
        (v_payment_amount / 1.16)::NUMERIC(16,2),  -- Base IVA 16%
        (v_payment_amount - (v_payment_amount / 1.16))::NUMERIC(16,2),  -- IVA 16%
        0.00,
        0.00,
        0.00,
        0.00,
        0.00,
        1,
        NOW()
    );

    RAISE NOTICE '    ✓ Totals creado: %', v_totals_uuid;
    RAISE NOTICE '    - Total pagos: $%', v_payment_amount;
    RAISE NOTICE '    - Base IVA 16%%: $%', (v_payment_amount / 1.16)::NUMERIC(16,2);
    RAISE NOTICE '    - IVA 16%%: $%', (v_payment_amount - (v_payment_amount / 1.16))::NUMERIC(16,2);

    RAISE NOTICE '';
    RAISE NOTICE '=== CASO DE PRUEBA CREADO EXITOSAMENTE ===';
    RAISE NOTICE '';
    RAISE NOTICE 'RESUMEN:';
    RAISE NOTICE '- 1 Invoice (factura)';
    RAISE NOTICE '- 1 Payments (complemento de pago)';
    RAISE NOTICE '- 1 Payment (pago individual)';
    RAISE NOTICE '- 1 Related document (vinculación)';
    RAISE NOTICE '- 1 Totals (totales)';

END $$;

-- ===============================
-- VERIFICACION
-- ===============================

SELECT
    'VERIFICACION CASO DE PRUEBA' as titulo,
    (SELECT COUNT(*) FROM tenant_fiscal.payments WHERE series = 'PP') as payments_count,
    (SELECT COUNT(*) FROM tenant_fiscal.payment WHERE payments_uuid IN (SELECT payments_uuid FROM tenant_fiscal.payments WHERE series = 'PP')) as payment_count,
    (SELECT COUNT(*) FROM tenant_fiscal.invoice WHERE series = 'TEST') as invoice_count,
    (SELECT COUNT(*) FROM tenant_fiscal.related_documents WHERE payment_uuid IN (SELECT payment_uuid FROM tenant_fiscal.payment WHERE payments_uuid IN (SELECT payments_uuid FROM tenant_fiscal.payments WHERE series = 'PP'))) as related_docs_count,
    (SELECT COUNT(*) FROM tenant_fiscal.totals WHERE payments_uuid IN (SELECT payments_uuid FROM tenant_fiscal.payments WHERE series = 'PP')) as totals_count;

-- Detalle del caso creado
SELECT
    p.payments_uuid,
    p.folio as complemento_folio,
    p.series as complemento_serie,
    pay.payment_uuid,
    pay.amount as monto_pago,
    i.folio as factura_folio,
    rd.amount_paid,
    rd.remaining_balance as saldo_restante,
    t.total_payments_amount as total_complemento
FROM tenant_fiscal.payments p
LEFT JOIN tenant_fiscal.payment pay ON pay.payments_uuid = p.payments_uuid
LEFT JOIN tenant_fiscal.related_documents rd ON rd.payment_uuid = pay.payment_uuid
LEFT JOIN tenant_fiscal.invoice i ON i.invoice_uuid = rd.document_uuid
LEFT JOIN tenant_fiscal.totals t ON t.payments_uuid = p.payments_uuid
WHERE p.series = 'PP'
ORDER BY p.created_at DESC
LIMIT 1;
