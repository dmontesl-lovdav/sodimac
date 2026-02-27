-- ============================================================================
-- STM-829 - Datos de Prueba para Pantalla de Pagos
-- ============================================================================
-- Base de datos: PostgreSQL (localhost:5434 / b2b_portal)
-- Schema: tenant_fiscal
-- Ejecutar en: DBeaver, pgAdmin o cualquier cliente SQL
-- ============================================================================

SET search_path TO tenant_fiscal;

-- ============================================================================
-- 1. EMISORES (Proveedores) - Agregar variedad para filtro CA-03
-- ============================================================================
-- Ya existe: 11111111-1111-1111-1111-111111111111 (SOD970101ABC)

INSERT INTO tenant_fiscal.issuer (issuer_uuid, name, rfc, tax_regime, created_by, created_at)
VALUES
    ('22222222-2222-2222-2222-222222222222', 'MATERIALES CONSTRUCCION DEL NORTE S.A. DE C.V.', 'MCN120315AB1', '601', 1, NOW()),
    ('33333333-3333-3333-3333-333333333333', 'FERRETERIA INDUSTRIAL LOPEZ S.A. DE C.V.', 'FIL980712CD3', '601', 1, NOW()),
    ('55555555-5555-5555-5555-555555555555', 'DISTRIBUIDORA ELECTRICA NACIONAL S.A. DE C.V.', 'DEN050228EF5', '601', 1, NOW()),
    ('66666666-6666-6666-6666-666666666666', 'PINTURAS Y ACABADOS PREMIUM S.A. DE C.V.', 'PAP110901GH7', '601', 1, NOW())
ON CONFLICT (issuer_uuid) DO NOTHING;

-- ============================================================================
-- 2. COMPLEMENTOS DE PAGO (payments) - Documentos padre CFDI
-- ============================================================================
-- Ya existe: aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee (folio 001, serie P)

INSERT INTO tenant_fiscal.payments (payments_uuid, fiscal_uuid, version, payment_date, issuer_uuid, receiver_uuid, folio, series, status, created_by, created_at)
VALUES
    -- Proveedor MCN (Materiales Construccion) - 3 complementos
    ('a0000001-0001-4000-8000-000000000001', 'f0000001-0001-4000-8000-000000000001', 2.000, '2025-01-15', '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', '100', 'PA', 1, 1, NOW()),
    ('a0000001-0001-4000-8000-000000000002', 'f0000001-0001-4000-8000-000000000002', 2.000, '2025-02-20', '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', '101', 'PA', 1, 1, NOW()),
    ('a0000001-0001-4000-8000-000000000003', 'f0000001-0001-4000-8000-000000000003', 2.000, '2025-03-10', '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', '102', 'PA', 0, 1, NOW()),

    -- Proveedor FIL (Ferreteria Industrial) - 3 complementos
    ('a0000002-0002-4000-8000-000000000001', 'f0000002-0002-4000-8000-000000000001', 2.000, '2025-01-25', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', '200', 'PB', 1, 1, NOW()),
    ('a0000002-0002-4000-8000-000000000002', 'f0000002-0002-4000-8000-000000000002', 2.000, '2025-04-05', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', '201', 'PB', 2, 1, NOW()),
    ('a0000002-0002-4000-8000-000000000003', 'f0000002-0002-4000-8000-000000000003', 2.000, '2025-05-18', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', '202', 'PB', 1, 1, NOW()),

    -- Proveedor DEN (Distribuidora Electrica) - 3 complementos
    ('a0000003-0003-4000-8000-000000000001', 'f0000003-0003-4000-8000-000000000001', 2.000, '2025-06-12', '55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', '300', 'PC', 1, 1, NOW()),
    ('a0000003-0003-4000-8000-000000000002', 'f0000003-0003-4000-8000-000000000002', 2.000, '2025-07-22', '55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', '301', 'PC', 3, 1, NOW()),
    ('a0000003-0003-4000-8000-000000000003', 'f0000003-0003-4000-8000-000000000003', 2.000, '2025-08-30', '55555555-5555-5555-5555-555555555555', '44444444-4444-4444-4444-444444444444', '302', 'PC', 1, 1, NOW()),

    -- Proveedor PAP (Pinturas y Acabados) - 3 complementos
    ('a0000004-0004-4000-8000-000000000001', 'f0000004-0004-4000-8000-000000000001', 2.000, '2025-09-14', '66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', '400', 'PD', 1, 1, NOW()),
    ('a0000004-0004-4000-8000-000000000002', 'f0000004-0004-4000-8000-000000000002', 2.000, '2025-10-28', '66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', '401', 'PD', 0, 1, NOW()),
    ('a0000004-0004-4000-8000-000000000003', 'f0000004-0004-4000-8000-000000000003', 2.000, '2025-12-05', '66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', '402', 'PD', 1, 1, NOW()),

    -- Proveedor SOD (existente) - 2 complementos adicionales
    ('a0000005-0005-4000-8000-000000000001', 'f0000005-0005-4000-8000-000000000001', 2.000, '2025-11-10', '11111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', '002', 'P', 2, 1, NOW()),
    ('a0000005-0005-4000-8000-000000000002', 'f0000005-0005-4000-8000-000000000002', 2.000, '2026-01-15', '11111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', '003', 'P', 1, 1, NOW())
ON CONFLICT (payments_uuid) DO NOTHING;

-- ============================================================================
-- 3. PAGOS INDIVIDUALES (payment) - Lo que ve el usuario en el grid
-- ============================================================================
-- Ya existe: bbbbbbbb-cccc-dddd-eeee-ffffffffffff (complemento aaaaaaaa)

INSERT INTO tenant_fiscal.payment (payment_uuid, payments_uuid, payment_date, payment_method, currency, amount, operation_number, exchange_rate, payer_bank_rfc, payer_account, beneficiary_bank_rfc, beneficiary_account, created_by, created_at)
VALUES
    -- Pagos del complemento 100 (MCN - Ene 2025)
    ('b0000001-0001-4000-8000-000000000001', 'a0000001-0001-4000-8000-000000000001', '2025-01-15', '03', 'MXN', 45000.00, 'SAP-2025-001', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),
    ('b0000001-0001-4000-8000-000000000002', 'a0000001-0001-4000-8000-000000000001', '2025-01-15', '03', 'MXN', 23500.50, 'SAP-2025-002', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 101 (MCN - Feb 2025)
    ('b0000001-0001-4000-8000-000000000003', 'a0000001-0001-4000-8000-000000000002', '2025-02-20', '03', 'MXN', 78200.00, 'SAP-2025-003', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 102 (MCN - Mar 2025 - CANCELADO)
    ('b0000001-0001-4000-8000-000000000004', 'a0000001-0001-4000-8000-000000000003', '2025-03-10', '03', 'MXN', 15800.75, 'SAP-2025-004', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 200 (FIL - Ene 2025)
    ('b0000002-0002-4000-8000-000000000001', 'a0000002-0002-4000-8000-000000000001', '2025-01-25', '03', 'MXN', 32100.00, 'SAP-2025-005', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 201 (FIL - Abr 2025 - PENDIENTE)
    ('b0000002-0002-4000-8000-000000000002', 'a0000002-0002-4000-8000-000000000002', '2025-04-05', '03', 'MXN', 91500.00, 'SAP-2025-006', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 202 (FIL - May 2025)
    ('b0000002-0002-4000-8000-000000000003', 'a0000002-0002-4000-8000-000000000003', '2025-05-18', '03', 'MXN', 12750.25, 'SAP-2025-007', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),
    ('b0000002-0002-4000-8000-000000000004', 'a0000002-0002-4000-8000-000000000003', '2025-05-18', '03', 'MXN', 8430.00, 'SAP-2025-008', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 300 (DEN - Jun 2025)
    ('b0000003-0003-4000-8000-000000000001', 'a0000003-0003-4000-8000-000000000001', '2025-06-12', '03', 'MXN', 156000.00, 'SAP-2025-009', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 301 (DEN - Jul 2025 - RECHAZADO)
    ('b0000003-0003-4000-8000-000000000002', 'a0000003-0003-4000-8000-000000000002', '2025-07-22', '03', 'MXN', 43200.50, 'SAP-2025-010', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 302 (DEN - Ago 2025)
    ('b0000003-0003-4000-8000-000000000003', 'a0000003-0003-4000-8000-000000000003', '2025-08-30', '03', 'USD', 5200.00, 'SAP-2025-011', 17.250000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 400 (PAP - Sep 2025)
    ('b0000004-0004-4000-8000-000000000001', 'a0000004-0004-4000-8000-000000000001', '2025-09-14', '03', 'MXN', 27800.00, 'SAP-2025-012', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),
    ('b0000004-0004-4000-8000-000000000002', 'a0000004-0004-4000-8000-000000000001', '2025-09-14', '03', 'MXN', 14350.75, 'SAP-2025-013', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 401 (PAP - Oct 2025 - CANCELADO)
    ('b0000004-0004-4000-8000-000000000003', 'a0000004-0004-4000-8000-000000000002', '2025-10-28', '03', 'MXN', 65000.00, 'SAP-2025-014', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 402 (PAP - Dic 2025)
    ('b0000004-0004-4000-8000-000000000004', 'a0000004-0004-4000-8000-000000000003', '2025-12-05', '03', 'MXN', 38900.00, 'SAP-2025-015', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),
    ('b0000004-0004-4000-8000-000000000005', 'a0000004-0004-4000-8000-000000000003', '2025-12-05', '02', 'MXN', 11200.50, 'CHQ-2025-001', 1.000000, NULL, NULL, 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 002 (SOD - Nov 2025 - PENDIENTE)
    ('b0000005-0005-4000-8000-000000000001', 'a0000005-0005-4000-8000-000000000001', '2025-11-10', '03', 'MXN', 52400.00, 'SAP-2025-016', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),

    -- Pagos del complemento 003 (SOD - Ene 2026)
    ('b0000005-0005-4000-8000-000000000002', 'a0000005-0005-4000-8000-000000000002', '2026-01-15', '03', 'MXN', 89750.00, 'SAP-2026-001', 1.000000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW()),
    ('b0000005-0005-4000-8000-000000000003', 'a0000005-0005-4000-8000-000000000002', '2026-01-15', '03', 'USD', 3500.00, 'SAP-2026-002', 17.450000, 'BBA011001ABC', '012345678901234567', 'BME100101DEF', '987654321098765432', 1, NOW())
ON CONFLICT (payment_uuid) DO NOTHING;

-- ============================================================================
-- 4. VERIFICACION - Ejecutar despues de los INSERTs
-- ============================================================================

-- Total de registros por tabla
SELECT 'issuer' AS tabla, COUNT(*) AS total FROM tenant_fiscal.issuer
UNION ALL
SELECT 'receiver', COUNT(*) FROM tenant_fiscal.receiver
UNION ALL
SELECT 'payments', COUNT(*) FROM tenant_fiscal.payments
UNION ALL
SELECT 'payment', COUNT(*) FROM tenant_fiscal.payment;

-- Pagos por proveedor
SELECT
    i.rfc AS proveedor_rfc,
    i.name AS proveedor_nombre,
    COUNT(DISTINCT ps.payments_uuid) AS complementos,
    COUNT(p.payment_uuid) AS pagos,
    SUM(p.amount) AS monto_total
FROM tenant_fiscal.payment p
INNER JOIN tenant_fiscal.payments ps ON p.payments_uuid = ps.payments_uuid
INNER JOIN tenant_fiscal.issuer i ON ps.issuer_uuid = i.issuer_uuid
GROUP BY i.rfc, i.name
ORDER BY monto_total DESC;

-- Pagos por estatus
SELECT
    ps.status,
    CASE ps.status
        WHEN 0 THEN 'Cancelado'
        WHEN 1 THEN 'Vigente'
        WHEN 2 THEN 'Pendiente'
        WHEN 3 THEN 'Rechazado'
    END AS estatus,
    COUNT(DISTINCT ps.payments_uuid) AS complementos,
    COUNT(p.payment_uuid) AS pagos
FROM tenant_fiscal.payments ps
LEFT JOIN tenant_fiscal.payment p ON ps.payments_uuid = p.payments_uuid
GROUP BY ps.status
ORDER BY ps.status;

-- Rango de fechas disponibles
SELECT
    MIN(payment_date) AS fecha_inicio,
    MAX(payment_date) AS fecha_fin
FROM tenant_fiscal.payment;

-- ============================================================================
-- RESUMEN DE DATOS INSERTADOS
-- ============================================================================
-- 5 Proveedores (issuers):
--   SOD970101ABC - SODIMAC MEXICO (existente)
--   MCN120315AB1 - MATERIALES CONSTRUCCION DEL NORTE
--   FIL980712CD3 - FERRETERIA INDUSTRIAL LOPEZ
--   DEN050228EF5 - DISTRIBUIDORA ELECTRICA NACIONAL
--   PAP110901GH7 - PINTURAS Y ACABADOS PREMIUM
--
-- 16 Complementos de pago (payments):
--   Status 0 (Cancelado):  2 complementos
--   Status 1 (Vigente):    10 complementos
--   Status 2 (Pendiente):  3 complementos
--   Status 3 (Rechazado):  1 complemento
--
-- 21 Pagos individuales (payment):
--   Fechas: Enero 2025 - Enero 2026
--   Monedas: MXN (19), USD (2)
--   Metodos: 03-Transferencia (20), 02-Cheque (1)
--   Montos: $3,500 USD a $156,000 MXN
--
-- FILTROS QUE SE PUEDEN PROBAR:
--   CA-02: Fecha inicio/fin (ej: 2025-01-01 a 2025-06-30)
--   CA-03: Numero proveedor / RFC (5 proveedores distintos)
--   CA-04: Paginacion (21 pagos > 2 paginas de 10)
-- ============================================================================
