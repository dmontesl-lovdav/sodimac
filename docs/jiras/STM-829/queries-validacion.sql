-- ============================================================================
-- STM-829 - Consultas de Validacion para Pantalla de Pagos
-- ============================================================================
-- Base de datos: PostgreSQL
-- Schema: tenant_fiscal
-- ============================================================================

-- Configurar schema
SET search_path TO tenant_fiscal;

-- ============================================================================
-- 0. VERIFICAR QUE LAS TABLAS EXISTEN
-- ============================================================================

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'tenant_fiscal'
  AND table_name IN ('payment', 'payments', 'issuer', 'receiver')
ORDER BY table_name;

-- ============================================================================
-- 1. LISTA DE PAGOS (valida GET /payments)
-- ============================================================================
-- Tabla: payment (pagos individuales)
-- Campos que retorna el API: paymentUuid, paymentsUuid, paymentDate,
--                            paymentMethod, currency, amount, operationNumber,
--                            exchangeRate, payerBankRfc, payerAccount,
--                            beneficiaryBankRfc, beneficiaryAccount

SELECT
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
    created_at,
    updated_at
FROM tenant_fiscal.payment
ORDER BY payment_date DESC
LIMIT 10;

-- ============================================================================
-- 2. DOCUMENTOS DE PAGO / COMPLEMENTOS (valida GET /payment-documents)
-- ============================================================================
-- Tabla: payments (complementos de pago CFDI)
-- Campos: paymentsUuid, fiscalUuid, version, paymentDate, folio, series, status

SELECT
    ps.payments_uuid,
    ps.fiscal_uuid,
    ps.version,
    ps.payment_date,
    ps.folio,
    ps.series,
    ps.status,
    CASE ps.status
        WHEN 0 THEN 'Cancelado'
        WHEN 1 THEN 'Vigente'
        WHEN 2 THEN 'Pendiente'
        WHEN 3 THEN 'Rechazado'
        ELSE 'Desconocido'
    END AS status_description,
    ps.issuer_uuid,
    ps.receiver_uuid,
    ps.created_at
FROM tenant_fiscal.payments ps
ORDER BY ps.payment_date DESC
LIMIT 10;

-- ============================================================================
-- 3. DOCUMENTOS DE PAGO CON DATOS DE EMISOR Y RECEPTOR
-- ============================================================================

SELECT
    ps.payments_uuid,
    ps.fiscal_uuid,
    ps.version,
    ps.payment_date,
    ps.folio,
    ps.series,
    ps.status,
    i.rfc AS issuer_rfc,
    i.name AS issuer_name,
    r.rfc AS receiver_rfc,
    r.name AS receiver_name,
    ps.created_at
FROM tenant_fiscal.payments ps
LEFT JOIN tenant_fiscal.issuer i ON ps.issuer_uuid = i.issuer_uuid
LEFT JOIN tenant_fiscal.receiver r ON ps.receiver_uuid = r.receiver_uuid
ORDER BY ps.payment_date DESC
LIMIT 10;

-- ============================================================================
-- 4. VISTA COMPLETA PARA GRID (campos del requerimiento STM-829)
-- ============================================================================
-- Campos requeridos: idPago, Numero documento, Referencia documento,
--                    Numero proveedor, Nombre proveedor, Moneda, Importe,
--                    Tipo de documento, Documento SAP, Fecha de pago,
--                    Estatus, Fecha de registro, Fecha de actualizacion

SELECT
    p.payment_uuid AS "idPago",
    ps.folio AS "Numero documento",
    ps.series AS "Referencia documento",
    i.rfc AS "Numero proveedor",
    i.name AS "Nombre proveedor",
    p.currency AS "Moneda",
    p.amount AS "Importe",
    'Complemento de Pago' AS "Tipo de documento",
    p.operation_number AS "Documento SAP",
    p.payment_date AS "Fecha de pago",
    CASE ps.status
        WHEN 0 THEN 'Cancelado'
        WHEN 1 THEN 'Vigente'
        WHEN 2 THEN 'Pendiente'
        WHEN 3 THEN 'Rechazado'
        ELSE 'Desconocido'
    END AS "Estatus",
    p.created_at AS "Fecha de registro",
    p.updated_at AS "Fecha de actualizacion"
FROM tenant_fiscal.payment p
INNER JOIN tenant_fiscal.payments ps ON p.payments_uuid = ps.payments_uuid
LEFT JOIN tenant_fiscal.issuer i ON ps.issuer_uuid = i.issuer_uuid
ORDER BY p.payment_date DESC
LIMIT 10;

-- ============================================================================
-- 5. FILTRO POR RANGO DE FECHAS (CA-02)
-- ============================================================================
-- Modificar fechas segun necesidad

SELECT
    p.payment_uuid,
    p.payment_date,
    p.currency,
    p.amount,
    p.operation_number,
    ps.folio,
    i.name AS proveedor
FROM tenant_fiscal.payment p
INNER JOIN tenant_fiscal.payments ps ON p.payments_uuid = ps.payments_uuid
LEFT JOIN tenant_fiscal.issuer i ON ps.issuer_uuid = i.issuer_uuid
WHERE p.payment_date BETWEEN '2024-01-01' AND '2026-12-31'
ORDER BY p.payment_date DESC
LIMIT 10;

-- ============================================================================
-- 6. FILTRO POR PROVEEDOR / RFC (CA-03 - Solo Admin)
-- ============================================================================
-- Primero obtener los RFCs disponibles, luego filtrar

-- Ver RFCs disponibles:
SELECT DISTINCT i.rfc, i.name
FROM tenant_fiscal.issuer i
INNER JOIN tenant_fiscal.payments ps ON i.issuer_uuid = ps.issuer_uuid
LIMIT 10;

-- Filtrar por RFC especifico (cambiar 'XXX' por RFC real):
SELECT
    p.payment_uuid,
    p.payment_date,
    p.currency,
    p.amount,
    i.rfc AS proveedor_rfc,
    i.name AS proveedor_nombre
FROM tenant_fiscal.payment p
INNER JOIN tenant_fiscal.payments ps ON p.payments_uuid = ps.payments_uuid
INNER JOIN tenant_fiscal.issuer i ON ps.issuer_uuid = i.issuer_uuid
WHERE i.rfc = 'SOD970101ABC'  -- Cambiar por RFC real
ORDER BY p.payment_date DESC
LIMIT 10;

-- ============================================================================
-- 7. CONTAR TOTAL DE REGISTROS (validar paginacion CA-04)
-- ============================================================================

SELECT 'payment' AS tabla, COUNT(*) AS total_registros
FROM tenant_fiscal.payment
UNION ALL
SELECT 'payments' AS tabla, COUNT(*) AS total_registros
FROM tenant_fiscal.payments
UNION ALL
SELECT 'issuer' AS tabla, COUNT(*) AS total_registros
FROM tenant_fiscal.issuer
UNION ALL
SELECT 'receiver' AS tabla, COUNT(*) AS total_registros
FROM tenant_fiscal.receiver;

-- ============================================================================
-- 8. SUMA TOTAL POR COMPLEMENTO
-- ============================================================================

SELECT
    ps.payments_uuid,
    ps.folio,
    ps.series,
    COUNT(p.payment_uuid) AS num_pagos,
    COALESCE(SUM(p.amount), 0) AS total_amount,
    ps.status
FROM tenant_fiscal.payments ps
LEFT JOIN tenant_fiscal.payment p ON ps.payments_uuid = p.payments_uuid
GROUP BY ps.payments_uuid, ps.folio, ps.series, ps.status, ps.payment_date
ORDER BY ps.payment_date DESC;

-- ============================================================================
-- 9. PAGOS POR ESTATUS
-- ============================================================================

SELECT
    status,
    CASE status
        WHEN 0 THEN 'Cancelado'
        WHEN 1 THEN 'Vigente'
        WHEN 2 THEN 'Pendiente'
        WHEN 3 THEN 'Rechazado'
        ELSE 'Desconocido'
    END AS estatus_descripcion,
    COUNT(*) AS cantidad
FROM tenant_fiscal.payments
GROUP BY status
ORDER BY status;

-- ============================================================================
-- 10. BUSCAR PAGO ESPECIFICO POR UUID
-- ============================================================================
-- UUIDs reales del ambiente DEV (obtenidos del API):

-- Pago individual (GET /payments/{uuid})
SELECT *
FROM tenant_fiscal.payment
WHERE payment_uuid = 'bbbbbbbb-cccc-dddd-eeee-ffffffffffff';

-- Complemento de pago (GET /payment-documents/{uuid})
SELECT *
FROM tenant_fiscal.payments
WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee';

-- ============================================================================
-- 11. VERIFICAR RANGO DE FECHAS CON DATOS
-- ============================================================================

SELECT
    MIN(payment_date) AS fecha_mas_antigua,
    MAX(payment_date) AS fecha_mas_reciente,
    COUNT(*) AS total_pagos
FROM tenant_fiscal.payment;

SELECT
    MIN(payment_date) AS fecha_mas_antigua,
    MAX(payment_date) AS fecha_mas_reciente,
    COUNT(*) AS total_complementos
FROM tenant_fiscal.payments;

-- ============================================================================
-- 12. DETALLE COMPLETO DE UN PAGO (validar respuesta del API)
-- ============================================================================

SELECT
    p.payment_uuid,
    p.payments_uuid,
    p.payment_date,
    p.payment_method,
    p.currency,
    p.amount,
    p.operation_number,
    p.exchange_rate,
    p.payer_bank_rfc,
    p.payer_account,
    p.beneficiary_bank_rfc,
    p.beneficiary_account,
    p.created_at,
    p.updated_at,
    ps.folio,
    ps.series,
    ps.fiscal_uuid,
    ps.status,
    i.rfc AS issuer_rfc,
    i.name AS issuer_name,
    r.rfc AS receiver_rfc,
    r.name AS receiver_name
FROM tenant_fiscal.payment p
INNER JOIN tenant_fiscal.payments ps ON p.payments_uuid = ps.payments_uuid
LEFT JOIN tenant_fiscal.issuer i ON ps.issuer_uuid = i.issuer_uuid
LEFT JOIN tenant_fiscal.receiver r ON ps.receiver_uuid = r.receiver_uuid
ORDER BY p.payment_date DESC
LIMIT 1;

-- ============================================================================
-- 13. ESTRUCTURA DE TABLAS (para referencia)
-- ============================================================================

-- Ver columnas de la tabla payment
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'tenant_fiscal' AND table_name = 'payment'
ORDER BY ordinal_position;

-- Ver columnas de la tabla payments
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'tenant_fiscal' AND table_name = 'payments'
ORDER BY ordinal_position;

-- ============================================================================
-- DATOS DE PRUEBA EN DEV (obtenidos del API)
-- ============================================================================
--
-- paymentUuid:     bbbbbbbb-cccc-dddd-eeee-ffffffffffff
-- paymentsUuid:    aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
-- paymentDate:     2025-11-20
-- paymentMethod:   03
-- currency:        MXN
-- amount:          1800.00
-- operationNumber: OP-001-2024
-- exchangeRate:    1.0
--
-- ============================================================================
-- NOTAS
-- ============================================================================
--
-- Schema: tenant_fiscal
--
-- Tablas principales:
--   - payment:  Pagos individuales (lo que ve el usuario en el grid)
--   - payments: Complementos de pago (documento CFDI padre)
--   - issuer:   Emisores (proveedores)
--   - receiver: Receptores (Sodimac/Falabella)
--
-- Relaciones:
--   - payment.payments_uuid -> payments.payments_uuid (FK)
--   - payments.issuer_uuid -> issuer.issuer_uuid (FK)
--   - payments.receiver_uuid -> receiver.receiver_uuid (FK)
--
-- ============================================================================
