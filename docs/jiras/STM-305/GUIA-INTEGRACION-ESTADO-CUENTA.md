# STM-305: Guía de Integración - Estado de Cuenta

> **Documento para el equipo de integración**
> Fecha: 2026-02-09
> Esquema: `tenant_finance`
> Base de datos: `b2b_portal`

---

## 1. Resumen Ejecutivo

El Estado de Cuenta es un documento mensual que consolida la posición financiera de un proveedor. Contiene:

- **Saldo inicial**: Heredado del mes anterior
- **Facturas**: Pendientes y pagadas del período
- **Descuentos**: Descuentos comerciales aplicados
- **Notas de crédito**: Ajustes a favor del proveedor
- **Pagos**: Pagos realizados en el período
- **Órdenes de compra**: OCs del período (informativo)
- **Recepciones**: Recepciones de mercancía (informativo)

### Fórmula del Saldo Final

```
SaldoFinal = SaldoInicial + Facturas - (Pagos + NotasCredito + Descuentos)
```

---

## 2. Diagrama de Relaciones

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              account_statement                               │
│  (Tabla Principal - Control y Versionado)                                   │
│                                                                              │
│  PK: account_statement_uuid                                                  │
│  FK: previous_statement_uuid → account_statement (auto-referencia)          │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
                                   │ FK: account_statement_uuid
                                   │
       ┌───────────────┬───────────┼───────────┬───────────────┬──────────────┐
       │               │           │           │               │              │
       ▼               ▼           ▼           ▼               ▼              ▼
┌─────────────┐ ┌─────────────┐ ┌─────────┐ ┌─────────┐ ┌───────────┐ ┌───────────┐
│  _invoice   │ │  _discount  │ │ _credit │ │_payment │ │_purchase  │ │_reception │
│             │ │             │ │  _note  │ │         │ │  _order   │ │           │
│ Facturas    │ │ Descuentos  │ │ Notas   │ │ Pagos   │ │ Órdenes   │ │Recepciones│
│ pendientes  │ │ comerciales │ │ crédito │ │         │ │ de compra │ │           │
│ y pagadas   │ │             │ │         │ │         │ │           │ │           │
└─────────────┘ └─────────────┘ └─────────┘ └─────────┘ └───────────┘ └───────────┘
     (+)             (-)            (-)         (-)         (info)        (info)
   Aumenta         Reduce         Reduce      Reduce     Informativo   Informativo
    saldo          saldo          saldo       saldo
```

---

## 3. Catálogo de Estatus

El campo `status` en `account_statement` utiliza el catálogo **CatEstatusEstadoCuenta**:

| Código | Valor | Descripción | Uso |
|--------|-------|-------------|-----|
| EEC001 | 1 | Generado | Estado inicial al crear el EdC |
| EEC002 | 2 | Publicado | EdC visible para el proveedor |
| EEC003 | 3 | Revisado | Proveedor confirmó el EdC |
| EEC004 | 4 | Rechazado | Proveedor rechazó el EdC |
| EEC005 | 5 | Reprocesado | Versión anterior, reemplazada por nueva |

### Flujo de Estados

```
[1] Generado ──────► [2] Publicado ──────► [3] Revisado
                           │
                           ├──────────────► [4] Rechazado
                           │
                           └──────────────► [5] Reprocesado
                                                   │
                                                   ▼
                                            Nueva versión
                                            [1] Generado → [2] Publicado
```

---

## 4. Tablas - Descripción Funcional

### 4.1 `account_statement` (Tabla Principal)

**Propósito**: Controla el estado de cuenta mensual por proveedor con soporte para versionamiento.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_uuid` | UUID | **PK** - Identificador único |
| `vendor_number` | BIGINT | Número del proveedor (referencia lógica, sin FK) |
| `year` | INTEGER | Año del período (ej: 2026) |
| `month` | SMALLINT | Mes del período (1-12) |
| `version` | INTEGER | Versión para reprocesos (1, 2, 3...) |
| `status` | INTEGER | Estado del EdC (ver catálogo arriba) |
| `initial_balance` | NUMERIC(18,2) | Saldo inicial (= final del mes anterior) |
| `final_balance` | NUMERIC(18,2) | Saldo final calculado |
| `process_date` | TIMESTAMP | Fecha/hora de generación |
| `review_date` | TIMESTAMP | Fecha/hora de revisión por proveedor |
| `issue_date` | TIMESTAMP | Fecha/hora de publicación |
| `period_start_date` | DATE | Inicio del período (ej: 2026-01-01) |
| `period_end_date` | DATE | Fin del período (ej: 2026-01-31) |
| `previous_statement_uuid` | UUID | **FK** - Referencia a versión anterior (reprocesos) |
| `created_by` | BIGINT | Usuario que creó el registro |
| `created_at` | TIMESTAMP | Fecha de creación |
| `updated_by` | BIGINT | Usuario que actualizó |
| `updated_at` | TIMESTAMP | Fecha de actualización |

**Constraint único**: `(vendor_number, year, month, version)`

#### Datos de Ejemplo

```sql
SELECT account_statement_uuid, vendor_number, year, month, version, status,
       initial_balance, final_balance, period_start_date, period_end_date
FROM tenant_finance.account_statement
ORDER BY vendor_number, version;
```

| vendor | año | mes | versión | status | saldo_inicial | saldo_final | período |
|--------|-----|-----|---------|--------|---------------|-------------|---------|
| 1001 | 2026 | 1 | 1 | 2 (Publicado) | 50,000.00 | 65,500.00 | Ene 2026 |
| 1002 | 2026 | 1 | 1 | 1 (Generado) | 0.00 | 25,000.00 | Ene 2026 |
| 1003 | 2026 | 1 | 1 | 5 (Reprocesado) | 10,000.00 | 35,000.00 | Ene 2026 |
| 1003 | 2026 | 1 | 2 | 2 (Publicado) | 10,000.00 | 32,000.00 | Ene 2026 |

> **Nota**: El proveedor 1003 tiene 2 versiones. La v1 fue reprocesada (status=5) y la v2 es la versión actual publicada. La v2 tiene `previous_statement_uuid` apuntando a la v1.

---

### 4.2 `account_statement_invoice` (Facturas)

**Propósito**: Registra las facturas del período, separadas en pendientes y pagadas.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_invoice_uuid` | UUID | **PK** - Identificador único |
| `account_statement_uuid` | UUID | **FK** - Referencia al EdC |
| `invoice_type` | VARCHAR(20) | `PENDING` o `PAID` |
| `series` | VARCHAR(50) | Serie del CFDI |
| `folio` | VARCHAR(50) | Folio del CFDI |
| `uuid` | VARCHAR(64) | UUID del CFDI (timbrado SAT) |
| `stamp_date` | TIMESTAMP | Fecha de timbrado |
| `accounting_date` | TIMESTAMP | Fecha contable |
| `payment_date` | TIMESTAMP | Fecha de pago (solo si `PAID`) |
| `currency` | VARCHAR(10) | Moneda origen (MXN, USD, etc.) |
| `amount` | NUMERIC(18,2) | Monto en moneda origen |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio aplicado |
| `base_currency` | VARCHAR(10) | Moneda base (siempre MXN) |
| `base_amount` | NUMERIC(18,2) | Monto convertido a MXN |
| `invoice_status` | VARCHAR(50) | Estatus descriptivo |
| `payment_id` | BIGINT | ID del pago relacionado (si aplica) |

#### Datos de Ejemplo

```sql
SELECT invoice_type, series, folio, uuid, currency, amount, base_amount, invoice_status
FROM tenant_finance.account_statement_invoice
WHERE account_statement_uuid = '<uuid_proveedor_1001>';
```

| tipo | serie | folio | uuid_cfdi | moneda | monto | monto_mxn | estatus |
|------|-------|-------|-----------|--------|-------|-----------|---------|
| PAID | A | 0998 | UUID-FAC-003-2026 | MXN | 8,000.00 | 8,000.00 | Pagado |
| PENDING | A | 1001 | UUID-FAC-001-2026 | MXN | 15,000.00 | 15,000.00 | Pendiente de Pago |
| PENDING | B | 2001 | UUID-FAC-002-2026 | USD | 1,000.00 | 17,500.00 | Pendiente de Pago |

> **Nota sobre conversión de moneda**: La factura en USD se convierte a MXN usando `exchange_rate=17.50`, resultando en `base_amount=17,500.00`.

---

### 4.3 `account_statement_discount` (Descuentos)

**Propósito**: Registra los descuentos comerciales aplicados en el período.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_discount_uuid` | UUID | **PK** - Identificador único |
| `account_statement_uuid` | UUID | **FK** - Referencia al EdC |
| `document_number` | VARCHAR(50) | Número de documento del descuento |
| `reference_number` | VARCHAR(50) | Referencia (ej: código de promoción) |
| `series` | VARCHAR(50) | Serie (si aplica) |
| `folio` | VARCHAR(50) | Folio (si aplica) |
| `uuid` | VARCHAR(64) | UUID del CFDI (si aplica) |
| `discount_date` | TIMESTAMP | Fecha del descuento |
| `accounting_date` | TIMESTAMP | Fecha contable |
| `currency` | VARCHAR(10) | Moneda origen |
| `amount` | NUMERIC(18,2) | Monto en moneda origen |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio |
| `base_currency` | VARCHAR(10) | Moneda base (MXN) |
| `base_amount` | NUMERIC(18,2) | Monto convertido a MXN |
| `status` | VARCHAR(50) | Estatus del descuento |

#### Datos de Ejemplo

```sql
SELECT document_number, reference_number, currency, amount, base_amount, status
FROM tenant_finance.account_statement_discount
WHERE account_statement_uuid = '<uuid_proveedor_1001>';
```

| documento | referencia | moneda | monto | monto_mxn | estatus |
|-----------|------------|--------|-------|-----------|---------|
| DESC-001 | PROMO-ENE-2026 | MXN | 2,000.00 | 2,000.00 | Aplicado |

---

### 4.4 `account_statement_credit_note` (Notas de Crédito)

**Propósito**: Registra las notas de crédito emitidas en el período.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_credit_note_uuid` | UUID | **PK** - Identificador único |
| `account_statement_uuid` | UUID | **FK** - Referencia al EdC |
| `document_number` | VARCHAR(50) | Número de documento |
| `series` | VARCHAR(50) | Serie del CFDI |
| `folio` | VARCHAR(50) | Folio del CFDI |
| `uuid` | VARCHAR(64) | **Requerido** - UUID del CFDI |
| `issue_date` | TIMESTAMP | Fecha de emisión |
| `accounting_date` | TIMESTAMP | Fecha contable |
| `currency` | VARCHAR(10) | Moneda origen |
| `amount` | NUMERIC(18,2) | Monto en moneda origen |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio |
| `base_currency` | VARCHAR(10) | Moneda base (MXN) |
| `base_amount` | NUMERIC(18,2) | Monto convertido a MXN |
| `status` | VARCHAR(50) | Estatus de la NC |

#### Datos de Ejemplo

```sql
SELECT document_number, series, folio, uuid, currency, amount, base_amount, status
FROM tenant_finance.account_statement_credit_note
WHERE account_statement_uuid = '<uuid_proveedor_1001>';
```

| documento | serie | folio | uuid_cfdi | moneda | monto | monto_mxn | estatus |
|-----------|-------|-------|-----------|--------|-------|-----------|---------|
| NC-001 | NC | 0001 | UUID-NC-001-2026 | MXN | 3,000.00 | 3,000.00 | Aplicada |

---

### 4.5 `account_statement_payment` (Pagos)

**Propósito**: Registra los pagos realizados al proveedor en el período.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_payment_uuid` | UUID | **PK** - Identificador único |
| `account_statement_uuid` | UUID | **FK** - Referencia al EdC |
| `payment_id` | BIGINT | ID del pago en sistema origen |
| `document_number` | VARCHAR(50) | Número de documento de pago |
| `reference_number` | VARCHAR(50) | Referencia bancaria/transferencia |
| `payment_date` | TIMESTAMP | Fecha del pago |
| `currency` | VARCHAR(10) | Moneda origen |
| `amount` | NUMERIC(18,2) | Monto en moneda origen |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio |
| `base_currency` | VARCHAR(10) | Moneda base (MXN) |
| `base_amount` | NUMERIC(18,2) | Monto convertido a MXN |
| `status` | VARCHAR(50) | Estatus del pago |

#### Datos de Ejemplo

```sql
SELECT payment_id, document_number, reference_number, currency, amount, base_amount, status
FROM tenant_finance.account_statement_payment
WHERE account_statement_uuid = '<uuid_proveedor_1001>';
```

| payment_id | documento | referencia | moneda | monto | monto_mxn | estatus |
|------------|-----------|------------|--------|-------|-----------|---------|
| 5001 | PAG-001 | TRANSF-001 | MXN | 8,000.00 | 8,000.00 | Aplicado |
| 5002 | PAG-002 | TRANSF-002 | MXN | 2,000.00 | 2,000.00 | Aplicado |

---

### 4.6 `account_statement_purchase_order` (Órdenes de Compra)

**Propósito**: Registra las órdenes de compra del período (informativo, no afecta saldo).

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_purchase_order_uuid` | UUID | **PK** - Identificador único |
| `account_statement_uuid` | UUID | **FK** - Referencia al EdC |
| `order_number` | BIGINT | Número de OC |
| `document_date` | DATE | Fecha del documento |
| `due_date` | DATE | Fecha de vencimiento |
| `currency` | VARCHAR(10) | Moneda origen |
| `amount` | NUMERIC(18,2) | Monto en moneda origen |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio |
| `base_currency` | VARCHAR(10) | Moneda base (MXN) |
| `base_amount` | NUMERIC(18,2) | Monto convertido a MXN |
| `status` | VARCHAR(50) | Estatus de la OC |
| `source_id` | BIGINT | ID en tabla origen (trazabilidad) |

#### Datos de Ejemplo

```sql
SELECT order_number, document_date, due_date, currency, amount, base_amount, status
FROM tenant_finance.account_statement_purchase_order
WHERE account_statement_uuid = '<uuid_proveedor_1001>';
```

| orden | fecha_doc | vencimiento | moneda | monto | monto_mxn | estatus |
|-------|-----------|-------------|--------|-------|-----------|---------|
| 900001 | 2026-01-10 | 2026-02-10 | MXN | 25,000.00 | 25,000.00 | Recibida |
| 900002 | 2026-01-18 | 2026-02-18 | USD | 500.00 | 8,750.00 | Pendiente |

---

### 4.7 `account_statement_reception` (Recepciones)

**Propósito**: Registra las recepciones de mercancía del período (informativo, no afecta saldo).

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `account_statement_reception_uuid` | UUID | **PK** - Identificador único |
| `account_statement_uuid` | UUID | **FK** - Referencia al EdC |
| `reception_number` | BIGINT | Número de recepción |
| `order_number` | BIGINT | Número de OC relacionada |
| `document_date` | DATE | Fecha del documento |
| `reception_date` | DATE | Fecha de recepción física |
| `due_date` | DATE | Fecha de vencimiento para pago |
| `currency` | VARCHAR(10) | Moneda origen |
| `amount` | NUMERIC(18,2) | Monto en moneda origen |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio |
| `base_currency` | VARCHAR(10) | Moneda base (MXN) |
| `base_amount` | NUMERIC(18,2) | Monto convertido a MXN |
| `status` | VARCHAR(50) | Estatus de la recepción |
| `source_id` | BIGINT | ID en tabla origen (trazabilidad) |

#### Datos de Ejemplo

```sql
SELECT reception_number, order_number, reception_date, due_date, currency, amount, base_amount, status
FROM tenant_finance.account_statement_reception
WHERE account_statement_uuid = '<uuid_proveedor_1001>';
```

| recepción | orden | fecha_recepción | vencimiento | moneda | monto | monto_mxn | estatus |
|-----------|-------|-----------------|-------------|--------|-------|-----------|---------|
| 800001 | 900001 | 2026-01-12 | 2026-02-12 | MXN | 25,000.00 | 25,000.00 | Completa |

---

## 5. Queries Útiles para Integración

### 5.1 Obtener Estado de Cuenta Actual de un Proveedor

```sql
-- Última versión publicada de un proveedor para un período
SELECT *
FROM tenant_finance.account_statement
WHERE vendor_number = 1001
  AND year = 2026
  AND month = 1
  AND status = 2  -- Publicado
ORDER BY version DESC
LIMIT 1;
```

### 5.2 Obtener Todos los Detalles de un Estado de Cuenta

```sql
-- Resumen por tipo de documento
SELECT
    'Facturas Pendientes' as tipo,
    COUNT(*) as cantidad,
    SUM(base_amount) as total_mxn
FROM tenant_finance.account_statement_invoice
WHERE account_statement_uuid = '<uuid>'
  AND invoice_type = 'PENDING'

UNION ALL

SELECT
    'Facturas Pagadas',
    COUNT(*),
    SUM(base_amount)
FROM tenant_finance.account_statement_invoice
WHERE account_statement_uuid = '<uuid>'
  AND invoice_type = 'PAID'

UNION ALL

SELECT 'Descuentos', COUNT(*), SUM(base_amount)
FROM tenant_finance.account_statement_discount
WHERE account_statement_uuid = '<uuid>'

UNION ALL

SELECT 'Notas de Crédito', COUNT(*), SUM(base_amount)
FROM tenant_finance.account_statement_credit_note
WHERE account_statement_uuid = '<uuid>'

UNION ALL

SELECT 'Pagos', COUNT(*), SUM(base_amount)
FROM tenant_finance.account_statement_payment
WHERE account_statement_uuid = '<uuid>';
```

### 5.3 Verificar Versionamiento (Reprocesos)

```sql
-- Ver historial de versiones de un proveedor
SELECT
    version,
    status,
    CASE status
        WHEN 1 THEN 'Generado'
        WHEN 2 THEN 'Publicado'
        WHEN 5 THEN 'Reprocesado'
    END as status_desc,
    initial_balance,
    final_balance,
    previous_statement_uuid
FROM tenant_finance.account_statement
WHERE vendor_number = 1003
  AND year = 2026
  AND month = 1
ORDER BY version;
```

### 5.4 Obtener EdC con Detalles (JOIN)

```sql
-- Estado de cuenta con totales por categoría
SELECT
    s.account_statement_uuid,
    s.vendor_number,
    s.year,
    s.month,
    s.version,
    s.initial_balance,
    s.final_balance,
    COALESCE(inv.total_invoices, 0) as total_facturas,
    COALESCE(dis.total_discounts, 0) as total_descuentos,
    COALESCE(cn.total_credit_notes, 0) as total_notas_credito,
    COALESCE(pay.total_payments, 0) as total_pagos
FROM tenant_finance.account_statement s
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total_invoices
    FROM tenant_finance.account_statement_invoice
    GROUP BY account_statement_uuid
) inv ON s.account_statement_uuid = inv.account_statement_uuid
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total_discounts
    FROM tenant_finance.account_statement_discount
    GROUP BY account_statement_uuid
) dis ON s.account_statement_uuid = dis.account_statement_uuid
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total_credit_notes
    FROM tenant_finance.account_statement_credit_note
    GROUP BY account_statement_uuid
) cn ON s.account_statement_uuid = cn.account_statement_uuid
LEFT JOIN (
    SELECT account_statement_uuid, SUM(base_amount) as total_payments
    FROM tenant_finance.account_statement_payment
    GROUP BY account_statement_uuid
) pay ON s.account_statement_uuid = pay.account_statement_uuid
WHERE s.status = 2;  -- Solo publicados
```

---

## 6. Patrón de Conversión de Moneda

Todas las tablas de detalle siguen el mismo patrón para manejar múltiples monedas:

| Campo | Descripción | Ejemplo USD | Ejemplo MXN |
|-------|-------------|-------------|-------------|
| `currency` | Moneda origen | USD | MXN |
| `amount` | Monto en moneda origen | 1,000.00 | 15,000.00 |
| `exchange_rate` | Tipo de cambio | 17.50 | 1.00 |
| `base_currency` | Moneda base (siempre MXN) | MXN | MXN |
| `base_amount` | `amount * exchange_rate` | 17,500.00 | 15,000.00 |

> **Importante**: Para cálculos de saldos siempre usar `base_amount` (MXN).

---

## 7. Índices Disponibles

Para optimizar las consultas, se crearon los siguientes índices:

| Índice | Tabla | Columnas | Uso |
|--------|-------|----------|-----|
| `ix_account_statement_vendor_period` | account_statement | vendor_number, year, month, version | Búsqueda por proveedor y período |
| `ix_account_statement_status` | account_statement | status | Filtrar por estatus |
| `ix_account_statement_published` | account_statement | vendor_number, year, month WHERE status=2 | Solo publicados |
| `ix_account_statement_invoice_statement` | account_statement_invoice | account_statement_uuid | JOIN con facturas |
| `ix_account_statement_invoice_type` | account_statement_invoice | account_statement_uuid, invoice_type | Filtrar por tipo |

---

## 8. Vistas de Consulta (SQL Views)

Se crearon **10 vistas** para facilitar las consultas más comunes:

### Vistas Principales

| Vista | Descripción | Uso Principal |
|-------|-------------|---------------|
| `vw_account_statement` | EdC con descripción de estatus | Consultas generales |
| `vw_account_statement_current` | Última versión publicada por proveedor/período | **Mostrar al proveedor** |
| `vw_account_statement_summary` | EdC con totales de cada tipo de documento | Dashboards, listados |
| `vw_account_statement_balance` | Verificación del cálculo del saldo | Auditoría, validación |

### Vistas de Detalle

| Vista | Descripción |
|-------|-------------|
| `vw_account_statement_invoices` | Facturas con datos del EdC padre |
| `vw_account_statement_payments` | Pagos con datos del EdC padre |
| `vw_account_statement_credit_notes` | Notas de crédito con datos del EdC padre |
| `vw_account_statement_discounts` | Descuentos con datos del EdC padre |
| `vw_account_statement_purchase_orders` | OCs con datos del EdC padre |
| `vw_account_statement_receptions` | Recepciones con datos del EdC padre |

### Ejemplos de Uso

```sql
-- Obtener el EdC vigente para mostrar al proveedor
SELECT * FROM tenant_finance.vw_account_statement_current
WHERE vendor_number = 1001 AND year = 2026 AND month = 1;

-- Dashboard con totales
SELECT vendor_number, year, month, status_description,
       initial_balance, final_balance,
       pending_invoices_count, pending_invoices_total,
       payments_count, payments_total
FROM tenant_finance.vw_account_statement_summary
WHERE status = 2;  -- Solo publicados

-- Validar que los saldos estén correctos
SELECT vendor_number, year, month, version,
       final_balance_stored, final_balance_calculated, difference, is_balanced
FROM tenant_finance.vw_account_statement_balance
WHERE is_balanced = FALSE;  -- Mostrar errores

-- Facturas de un proveedor en un período
SELECT series, folio, cfdi_uuid, invoice_type, currency, amount, base_amount
FROM tenant_finance.vw_account_statement_invoices
WHERE vendor_number = 1001 AND year = 2026 AND month = 1;
```

---

## 9. Consideraciones para la API

### 9.1 Endpoints Sugeridos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/account-statements/{vendorNumber}` | Lista EdC de un proveedor |
| GET | `/account-statements/{vendorNumber}/{year}/{month}` | EdC específico (última versión publicada) |
| GET | `/account-statements/{uuid}` | EdC por UUID con todos sus detalles |
| GET | `/account-statements/{uuid}/invoices` | Facturas del EdC |
| GET | `/account-statements/{uuid}/payments` | Pagos del EdC |
| POST | `/account-statements` | Crear nuevo EdC |
| PATCH | `/account-statements/{uuid}/status` | Cambiar estatus |

### 9.2 Respuesta JSON Sugerida

```json
{
  "accountStatementUuid": "3dd32c11-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "vendorNumber": 1001,
  "year": 2026,
  "month": 1,
  "version": 1,
  "status": {
    "code": "EEC002",
    "value": 2,
    "description": "Publicado"
  },
  "initialBalance": 50000.00,
  "finalBalance": 65500.00,
  "periodStartDate": "2026-01-01",
  "periodEndDate": "2026-01-31",
  "summary": {
    "totalInvoices": 40500.00,
    "totalDiscounts": 2000.00,
    "totalCreditNotes": 3000.00,
    "totalPayments": 10000.00
  },
  "invoices": [...],
  "discounts": [...],
  "creditNotes": [...],
  "payments": [...],
  "purchaseOrders": [...],
  "receptions": [...]
}
```

---

## 10. Preguntas Frecuentes (FAQ)

### ¿Por qué `vendor_number` no tiene FK?

Es una **referencia lógica** a un sistema externo (SAP/ERP). No existe una tabla de proveedores en `tenant_finance`.

### ¿Cómo se maneja el reproceso?

1. El EdC original cambia a status=5 (Reprocesado)
2. Se crea una nueva versión (version+1)
3. La nueva versión tiene `previous_statement_uuid` apuntando a la anterior

### ¿Las OCs y Recepciones afectan el saldo?

No. Son **solo informativas** para que el proveedor vea el contexto completo.

### ¿Qué moneda usar para cálculos?

Siempre usar `base_amount` (MXN). Los campos `amount` y `currency` son solo para mostrar el detalle original.

### ¿Cómo obtener el saldo inicial del mes actual?

```sql
SELECT final_balance
FROM tenant_finance.account_statement
WHERE vendor_number = <vendor>
  AND (year < <current_year> OR (year = <current_year> AND month < <current_month>))
  AND status = 2  -- Publicado
ORDER BY year DESC, month DESC
LIMIT 1;
```

---

## 11. Contacto

Para dudas sobre este modelo contactar al equipo de desarrollo backend.

- **JIRA**: STM-305
- **Esquema**: `tenant_finance`
- **Fecha de implementación**: 2026-02-09
