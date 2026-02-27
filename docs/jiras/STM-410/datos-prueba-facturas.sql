-- ============================================================================
-- DATOS DE PRUEBA - FACTURAS CON ADDENDAS Y RELACIONES (STM-410)
-- ============================================================================
-- Genera facturas (I), notas de credito (E), addendas y relaciones CFDI
-- para pruebas de la grilla de facturas y el tren de estatus.
--
-- Ejecucion: mediante jshell (no hay psql en la maquina)
--   jshell --class-path "<maven>/postgresql-42.7.7.jar" run-inserts.jsh
--   jshell --class-path "<maven>/postgresql-42.7.7.jar" run-inserts-100.jsh
--
-- Fecha: 2026-02-18
-- ============================================================================

SET search_path TO tenant_fiscal;

-- ============================================================================
-- PARTE 1: DATOS BASE (17 documentos manuales)
-- Ejecutado con: run-inserts.jsh
-- ============================================================================

-- RECEIVERS ADICIONALES
INSERT INTO receiver (receiver_uuid, name, rfc, tax_regime, created_by, created_at)
VALUES
    ('aaaa0001-0001-0001-0001-000000000001', 'SODIMAC HOMECENTER MEXICO S.A. DE C.V.', 'SHM130515QR8', '601', 1, NOW()),
    ('aaaa0002-0002-0002-0002-000000000002', 'FALABELLA RETAIL MEXICO S.A. DE C.V.', 'FRM140820LP3', '601', 1, NOW())
ON CONFLICT DO NOTHING;

-- Estatus 12: No valido fiscal (2 facturas) - Nota: estatus 0 no permitido por check constraint
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at)
VALUES
    ('f0000001-0001-4001-a001-000000000001', 'f0000001-a001-4001-b001-000000000001', 'I', 25000.00, 21551.72, 'MXN', 1, 'FA', '10001', 4.0, 12, '2025-06-15', '2025-06-15 10:30:00', 'PPD', '99', 'NET 30', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '88888888-8888-8888-8888-888888888888', 1, '2025-06-15 10:30:00'),
    ('f0000002-0002-4002-a002-000000000002', 'f0000002-a002-4002-b002-000000000002', 'I', 18500.50, 15948.71, 'MXN', 1, 'A', '20001', 4.0, 12, '2025-07-20', '2025-07-20 14:00:00', 'PUE', '01', 'CONTADO', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '55555555-5555-5555-5555-555555555555', 1, '2025-07-20 14:00:00');

-- Estatus 1: Pendiente Addenda (2 facturas)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at)
VALUES
    ('f0000003-0003-4003-a003-000000000003', 'f0000003-a003-4003-b003-000000000003', 'I', 45000.00, 38793.10, 'MXN', 1, 'B', '30001', 4.0, 1, '2025-08-10', '2025-08-10 09:15:00', 'PPD', '99', 'NET 30', '06600', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 1, '2025-08-10 09:15:00'),
    ('f0000004-0004-4004-a004-000000000004', 'f0000004-a004-4004-b004-000000000004', 'I', 5200.00, 4482.76, 'USD', 17.50, 'INV', '40001', 4.0, 1, '2025-09-05', '2025-09-05 16:45:00', 'PPD', '99', 'NET 60', '06600', '55555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 1, '2025-09-05 16:45:00');

-- Estatus 2: Recibido Parcial (1 factura)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at)
VALUES
    ('f0000005-0005-4005-a005-000000000005', 'f0000005-a005-4005-b005-000000000005', 'I', 150000.00, 129310.34, 'MXN', 1, 'FA', '50001', 4.0, 2, '2025-10-01', '2025-10-01 08:00:00', 'PPD', '99', 'NET 30', '06600', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, '2025-10-01 08:00:00');

-- Estatus 3: Pendiente de Contabilizar (2 facturas)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at)
VALUES
    ('f0000006-0006-4006-a006-000000000006', 'f0000006-a006-4006-b006-000000000006', 'I', 32000.00, 27586.21, 'MXN', 1, 'MC', '60001', 4.0, 3, '2025-10-15', '2025-10-15 11:20:00', 'PPD', '99', 'NET 30', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '55555555-5555-5555-5555-555555555555', 1, '2025-10-15 11:20:00'),
    ('f0000007-0007-4007-a007-000000000007', 'f0000007-a007-4007-b007-000000000007', 'I', 78500.00, 67672.41, 'MXN', 1, 'FA', '70001', 4.0, 3, '2025-11-01', '2025-11-01 13:30:00', 'PPD', '99', 'NET 45', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '88888888-8888-8888-8888-888888888888', 1, '2025-11-01 13:30:00');

-- Estatus 7: Pendiente de Pago (2 facturas)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date)
VALUES
    ('f0000008-0008-4008-a008-000000000008', 'f0000008-a008-4008-b008-000000000008', 'I', 55000.00, 47413.79, 'MXN', 1, 'B', '80001', 4.0, 7, '2025-11-10', '2025-11-10 09:00:00', 'PPD', '99', 'NET 30', '06600', '33333333-3333-3333-3333-333333333333', '55555555-5555-5555-5555-555555555555', 1, '2025-11-10 09:00:00', '2025-12-01'),
    ('f0000009-0009-4009-a009-000000000009', 'f0000009-a009-4009-b009-000000000009', 'I', 12800.00, 11034.48, 'USD', 17.25, 'INV', '90001', 4.0, 7, '2025-11-20', '2025-11-20 15:00:00', 'PPD', '99', 'NET 60', '06600', '55555555-5555-5555-5555-555555555555', '88888888-8888-8888-8888-888888888888', 1, '2025-11-20 15:00:00', '2025-12-15');

-- Estatus 8: Pagado (2 facturas)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date)
VALUES
    ('f0000010-0010-4010-a010-000000000010', 'f0000010-a010-4010-b010-000000000010', 'I', 95000.00, 81896.55, 'MXN', 1, 'FA', '10002', 4.0, 8, '2025-09-01', '2025-09-01 10:00:00', 'PPD', '99', 'NET 30', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '55555555-5555-5555-5555-555555555555', 1, '2025-09-01 10:00:00', '2025-09-20'),
    ('f0000011-0011-4011-a011-000000000011', 'f0000011-a011-4011-b011-000000000011', 'I', 42000.00, 36206.90, 'MXN', 1, 'MC', '10003', 4.0, 8, '2025-08-15', '2025-08-15 12:00:00', 'PUE', '01', 'CONTADO', '06600', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, '2025-08-15 12:00:00', '2025-09-01');

-- Estatus 10: Completado (1 factura)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at, accounting_date)
VALUES
    ('f0000012-0012-4012-a012-000000000012', 'f0000012-a012-4012-b012-000000000012', 'I', 200000.00, 172413.79, 'MXN', 1, 'FA', '10004', 4.0, 10, '2025-06-01', '2025-06-01 08:00:00', 'PPD', '99', 'NET 30', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '55555555-5555-5555-5555-555555555555', 1, '2025-06-01 08:00:00', '2025-07-01');

-- Estatus 11: Rechazo Contable (1 factura)
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, payment_conditions, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at)
VALUES
    ('f0000013-0013-4013-a013-000000000013', 'f0000013-a013-4013-b013-000000000013', 'I', 67000.00, 57758.62, 'MXN', 1, 'B', '10005', 4.0, 11, '2025-12-01', '2025-12-01 10:00:00', 'PPD', '99', 'NET 30', '06600', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 1, '2025-12-01 10:00:00');

-- NOTAS DE CREDITO (Tipo E) - 4 notas de credito
INSERT INTO invoice (invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate, series, folio, version, status, issue_date, certification_date, payment_method, payment_form, place_of_issue, issuer_uuid, receiver_uuid, created_by, created_at)
VALUES
    ('e0000001-0001-4001-e001-000000000001', 'e0000001-a001-4001-e001-000000000001', 'E', 15000.00, 12931.03, 'MXN', 1, 'NC', '50001', 4.0, 3, '2025-09-15', '2025-09-15 11:00:00', 'PUE', '01', '06600', '50f67897-f28f-492c-a80c-0f8523f1837d', '55555555-5555-5555-5555-555555555555', 1, '2025-09-15 11:00:00'),
    ('e0000002-0002-4002-e002-000000000002', 'e0000002-a002-4002-e002-000000000002', 'E', 8500.00, 7327.59, 'MXN', 1, 'NC', '50002', 4.0, 7, '2025-09-01', '2025-09-01 14:00:00', 'PUE', '01', '06600', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, '2025-09-01 14:00:00'),
    ('e0000003-0003-4003-e003-000000000003', 'e0000003-a003-4003-e003-000000000003', 'E', 50000.00, 43103.45, 'MXN', 1, 'NC', '50003', 4.0, 10, '2025-07-01', '2025-07-01 09:00:00', 'PUE', '01', '06600', 'e5eebf2e-2bdd-4061-aa66-21403b849bbe', '55555555-5555-5555-5555-555555555555', 1, '2025-07-01 09:00:00'),
    ('e0000004-0004-4004-e004-000000000004', 'e0000004-a004-4004-e004-000000000004', 'E', 3200.00, 2758.62, 'MXN', 1, 'NC', '50004', 4.0, 1, '2025-12-10', '2025-12-10 16:00:00', 'PUE', '01', '06600', '33333333-3333-3333-3333-333333333333', '88888888-8888-8888-8888-888888888888', 1, '2025-12-10 16:00:00');

-- ADDENDAS para facturas con estatus >= 2 y NCs
INSERT INTO addendum (addendum_uuid, invoice_uuid, supplier_number, reception_number, purchase_order_number, shipping_guide_number, supplier_type, addenda_type, created_by, created_at)
VALUES
    ('add00005-0005-4005-a005-000000000005', 'f0000005-0005-4005-a005-000000000005', 67890, 'REC-2025-050', 'OC-2025-1001', 'GE-050', 'SODIMAC', 1, 1, '2025-10-02 08:00:00'),
    ('add00006-0006-4006-a006-000000000006', 'f0000006-0006-4006-a006-000000000006', 12345, 'REC-2025-060', 'OC-2025-1002', 'GE-060', 'SODIMAC', 1, 1, '2025-10-16 08:00:00'),
    ('add00007-0007-4007-a007-000000000007', 'f0000007-0007-4007-a007-000000000007', 54321, 'REC-2025-070', 'OC-2025-1003', 'GE-070', 'SODIMAC', 1, 1, '2025-11-02 08:00:00'),
    ('add00008-0008-4008-a008-000000000008', 'f0000008-0008-4008-a008-000000000008', 11111, 'REC-2025-080', 'OC-2025-1004', 'GE-080', 'SODIMAC', 1, 1, '2025-11-11 08:00:00'),
    ('add00009-0009-4009-a009-000000000009', 'f0000009-0009-4009-a009-000000000009', 22222, 'REC-2025-090', 'OC-2025-1005', NULL, 'TOTTUS', 2, 1, '2025-11-21 08:00:00'),
    ('add00010-0010-4010-a010-000000000010', 'f0000010-0010-4010-a010-000000000010', 12345, 'REC-2025-100', 'OC-2025-1006', 'GE-100', 'SODIMAC', 1, 1, '2025-09-02 08:00:00'),
    ('add00011-0011-4011-a011-000000000011', 'f0000011-0011-4011-a011-000000000011', 67890, 'REC-2025-110', 'OC-2025-1007', 'GE-110', 'SODIMAC', 1, 1, '2025-08-16 08:00:00'),
    ('add00012-0012-4012-a012-000000000012', 'f0000012-0012-4012-a012-000000000012', 54321, 'REC-2025-120', 'OC-2025-1008', 'GE-120', 'SODIMAC', 1, 1, '2025-06-02 08:00:00'),
    ('add00013-0013-4013-a013-000000000013', 'f0000013-0013-4013-a013-000000000013', 11111, 'REC-2025-130', 'OC-2025-1009', 'GE-130', 'SODIMAC', 1, 1, '2025-12-02 08:00:00'),
    ('add0e001-e001-4001-e001-000000000001', 'e0000001-0001-4001-e001-000000000001', 12345, 'REC-2025-NC1', 'OC-2025-1006', NULL, 'SODIMAC', 1, 1, '2025-09-16 08:00:00'),
    ('add0e002-e002-4002-e002-000000000002', 'e0000002-0002-4002-e002-000000000002', 67890, 'REC-2025-NC2', 'OC-2025-1007', NULL, 'SODIMAC', 1, 1, '2025-09-02 08:00:00'),
    ('add0e003-e003-4003-e003-000000000003', 'e0000003-0003-4003-e003-000000000003', 54321, 'REC-2025-NC3', 'OC-2025-1008', NULL, 'SODIMAC', 1, 1, '2025-07-02 08:00:00');

-- RELACIONES NC <-> Factura
INSERT INTO related_cfdi (related_cfdi_uuid, invoice_uuid, related_invoice_uuid, relation_type, created_by, created_at)
VALUES
    ('ce100001-0001-4001-a001-000000000001', 'e0000001-0001-4001-e001-000000000001', 'f0000010-0010-4010-a010-000000000010', '01', 1, '2025-09-15 11:00:00'),
    ('ce200002-0002-4002-a002-000000000002', 'e0000002-0002-4002-e002-000000000002', 'f0000011-0011-4011-a011-000000000011', '01', 1, '2025-09-01 14:00:00'),
    ('ce300003-0003-4003-a003-000000000003', 'e0000003-0003-4003-e003-000000000003', 'f0000012-0012-4012-a012-000000000012', '01', 1, '2025-07-01 09:00:00');

-- ============================================================================
-- PARTE 2: CARGA MASIVA (100 facturas generadas)
-- Ejecutado con: run-inserts-100.jsh
-- ============================================================================
-- Este bloque genera 100 facturas tipo I con datos aleatorios usando PL/pgSQL.
-- Distribucion de estatus: 1,1,2,2,3,3,3,7,7,8,8,10,11,12,13
-- 6 emisores, 4 receptores, 8 series, MXN/USD, montos $5K-$500K
-- Addendas automaticas para facturas con estatus >= 2

DO $$
DECLARE
    v_issuer_uuids UUID[] := ARRAY[
        '50f67897-f28f-492c-a80c-0f8523f1837d',
        'e5eebf2e-2bdd-4061-aa66-21403b849bbe',
        '33333333-3333-3333-3333-333333333333',
        '55555555-5555-5555-5555-555555555555',
        '66666666-6666-6666-6666-666666666666',
        '11111111-1111-1111-1111-111111111111'
    ];
    v_receiver_uuids UUID[] := ARRAY[
        '88888888-8888-8888-8888-888888888888',
        '55555555-5555-5555-5555-555555555555',
        '33333333-3333-3333-3333-333333333333',
        'aaaa0001-0001-0001-0001-000000000001'
    ];
    v_series TEXT[] := ARRAY['FA','MC','B','INV','A','FC','FE','SER'];
    v_statuses INT[] := ARRAY[1,1,2,2,3,3,3,7,7,8,8,10,11,12,13];
    v_supplier_nums INT[] := ARRAY[12345,54321,67890,11111,22222,33333,44444,55555];
    v_supplier_types TEXT[] := ARRAY['SODIMAC','SODIMAC','SODIMAC','TOTTUS'];

    v_inv_uuid UUID;
    v_fisc_uuid UUID;
    v_add_uuid UUID;
    v_status INT;
    v_subtotal NUMERIC;
    v_total NUMERIC;
    v_currency TEXT;
    v_exchange_rate NUMERIC;
    v_issue_date DATE;
    v_cert_date TIMESTAMP;
    v_payment_method TEXT;
    v_payment_form TEXT;
    v_payment_cond TEXT;
    v_accounting_date DATE;
BEGIN
    FOR i IN 1..100 LOOP
        v_inv_uuid  := uuid_generate_v4();
        v_fisc_uuid := uuid_generate_v4();
        v_add_uuid  := uuid_generate_v4();
        v_status    := v_statuses[1 + floor(random() * array_length(v_statuses, 1))::int];
        v_subtotal  := round((5000 + random() * 495000)::numeric, 2);
        v_total     := round(v_subtotal * 1.16, 2);
        v_currency  := CASE WHEN random() < 0.2 THEN 'USD' ELSE 'MXN' END;
        v_exchange_rate := CASE WHEN v_currency = 'USD' THEN round((17 + random() * 3)::numeric, 6) ELSE 1 END;
        v_issue_date := '2025-01-01'::date + (floor(random() * 396))::int;
        v_cert_date  := v_issue_date + ((8 + floor(random() * 10))::int || ' hours')::interval
                        + (floor(random() * 60)::int || ' minutes')::interval;
        v_payment_method := CASE WHEN random() < 0.5 THEN 'PPD' ELSE 'PUE' END;
        v_payment_form   := CASE WHEN v_payment_method = 'PPD' THEN '99' ELSE '01' END;
        v_payment_cond   := CASE WHEN v_payment_method = 'PPD'
                                 THEN CASE WHEN random() < 0.5 THEN 'NET 30' ELSE 'NET 60' END
                                 ELSE 'CONTADO' END;
        v_accounting_date := CASE WHEN v_status >= 7 THEN v_issue_date + (15 + floor(random() * 30))::int ELSE NULL END;

        INSERT INTO invoice (
            invoice_uuid, fiscal_uuid, document_type, total, subtotal, currency, exchange_rate,
            series, folio, version, status, issue_date, certification_date,
            payment_method, payment_form, payment_conditions, place_of_issue,
            issuer_uuid, receiver_uuid, created_by, created_at, accounting_date
        ) VALUES (
            v_inv_uuid, v_fisc_uuid, 'I', v_total, v_subtotal, v_currency, v_exchange_rate,
            v_series[1 + floor(random() * array_length(v_series, 1))::int],
            (100000 + i)::text, 4.0, v_status, v_issue_date, v_cert_date,
            v_payment_method, v_payment_form, v_payment_cond, '06600',
            v_issuer_uuids[1 + floor(random() * array_length(v_issuer_uuids, 1))::int],
            v_receiver_uuids[1 + floor(random() * array_length(v_receiver_uuids, 1))::int],
            1, v_cert_date + (floor(random() * 60)::int || ' minutes')::interval,
            v_accounting_date
        );

        -- Addenda para facturas con estatus >= 2
        IF v_status >= 2 THEN
            INSERT INTO addendum (
                addendum_uuid, invoice_uuid, supplier_number, reception_number,
                purchase_order_number, shipping_guide_number, supplier_type, addenda_type,
                created_by, created_at
            ) VALUES (
                v_add_uuid, v_inv_uuid,
                v_supplier_nums[1 + floor(random() * array_length(v_supplier_nums, 1))::int],
                'REC-2025-' || lpad(i::text, 5, '0'),
                'OC-2025-' || lpad((2000 + i)::text, 5, '0'),
                CASE WHEN random() < 0.5 THEN 'GE-' || lpad(i::text, 5, '0') ELSE NULL END,
                v_supplier_types[1 + floor(random() * array_length(v_supplier_types, 1))::int],
                1, 1, v_cert_date + '1 hour'::interval
            );
        END IF;
    END LOOP;
END $$;

-- ============================================================================
-- CONSULTAS DE VALIDACION
-- ============================================================================

-- 1. Resumen por tipo de documento y estatus
SELECT
    document_type AS tipo,
    status AS estatus,
    COUNT(*) AS cantidad,
    TO_CHAR(SUM(total), 'FM$999,999,999.00') AS monto_total
FROM tenant_fiscal.invoice
GROUP BY document_type, status
ORDER BY document_type, status;

-- 2. Total general
SELECT
    COUNT(*) AS total_documentos,
    COUNT(*) FILTER (WHERE document_type = 'I') AS facturas,
    COUNT(*) FILTER (WHERE document_type = 'E') AS notas_credito,
    TO_CHAR(SUM(total), 'FM$999,999,999.00') AS monto_total
FROM tenant_fiscal.invoice;

-- 3. Facturas con sus addendas
SELECT
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.status,
    i.series || '-' || i.folio AS serie_folio,
    i.total,
    i.currency,
    iss.rfc AS rfc_emisor,
    a.supplier_number AS num_proveedor,
    a.purchase_order_number AS oc,
    a.reception_number AS recepcion
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
ORDER BY i.status, i.created_at DESC
LIMIT 20;

-- 4. Notas de credito con sus relaciones
SELECT
    nc.fiscal_uuid AS nc_uuid,
    nc.series || '-' || nc.folio AS nc_serie_folio,
    nc.total AS nc_total,
    nc.status AS nc_estatus,
    fac.fiscal_uuid AS factura_uuid,
    fac.series || '-' || fac.folio AS fac_serie_folio,
    fac.total AS fac_total,
    rc.relation_type
FROM tenant_fiscal.invoice nc
JOIN tenant_fiscal.related_cfdi rc ON nc.invoice_uuid = rc.invoice_uuid
JOIN tenant_fiscal.invoice fac ON rc.related_invoice_uuid = fac.invoice_uuid
WHERE nc.document_type = 'E';

-- 5. Conteo de addendas y relaciones
SELECT
    (SELECT COUNT(*) FROM tenant_fiscal.addendum) AS total_addendas,
    (SELECT COUNT(*) FROM tenant_fiscal.related_cfdi) AS total_relaciones;

-- 6. Distribucion por emisor
SELECT
    iss.rfc,
    iss.name AS emisor,
    COUNT(*) AS facturas
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
GROUP BY iss.rfc, iss.name
ORDER BY facturas DESC;
