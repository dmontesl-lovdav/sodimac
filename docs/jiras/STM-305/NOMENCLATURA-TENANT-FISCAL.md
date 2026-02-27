# STM-305: Analisis de Nomenclatura

## Fecha de Revision
2026-02-06

## Conexion Utilizada
```
Host: localhost:5434
Database: b2b_portal
Esquemas revisados: tenant_fiscal, tenant_finance
```

> **IMPORTANTE:** Las tablas de Estado de Cuenta deben crearse en `tenant_finance` (no `tenant_fiscal`).
> El analisis de nomenclatura aplica a ambos esquemas ya que usan las mismas convenciones.

---

## 1. Tablas Existentes (22 total)

```
addendum                    log                         receiver
authorized_receiver_catalog pac_catalog                 related_cfdi
equivalence_dr              payment                     related_documents
flyway_schema_history       payment_file_registry       tax
invoice                     payment_response_catalog    tax_detail
invoice_status_history      payments                    tax_transfer
issuer                                                  tax_withholding
                                                        totals
                                                        version_catalog
```

---

## 2. Convenciones Detectadas

### 2.1 Nombres de Tablas

| Regla | Ejemplo |
|-------|---------|
| snake_case | `invoice`, `tax_detail`, `payment_file_registry` |
| Singular | `invoice` (no `invoices`), `payment` (no `payments`*) |
| Prefijo comun para relacionadas | `tax`, `tax_detail`, `tax_transfer`, `tax_withholding` |

> *Nota: Existe `payment` y `payments` (plural). `payments` parece ser un encabezado de complemento de pago CFDI.

### 2.2 Nombres de Columnas

| Regla | Ejemplo |
|-------|---------|
| snake_case | `invoice_uuid`, `fiscal_uuid`, `exchange_rate` |
| Sufijo `_uuid` para IDs | `invoice_uuid`, `issuer_uuid`, `receiver_uuid` |
| Sufijo `_date` para fechas | `issue_date`, `payment_date`, `certification_date` |
| Prefijo `_at` para timestamps | `created_at`, `updated_at` |
| Prefijo `_by` para usuarios | `created_by`, `updated_by` |

### 2.3 Tipos de Datos

| PostgreSQL | Uso |
|------------|-----|
| `UUID` | Identificadores primarios y foraneos |
| `BIGINT` | IDs numericos (created_by, updated_by) |
| `NUMERIC(18,2)` | Montos monetarios |
| `VARCHAR` | Textos variables (moneda, serie, folio) |
| `INTEGER` | Status, versiones |
| `DATE` | Fechas sin hora |
| `TIMESTAMP` | Fechas con hora (created_at, certification_date) |
| `TEXT` | Contenido largo (xml_content) |

### 2.4 Campos de Auditoria (Estandar)

Todas las tablas incluyen:

```sql
created_by   BIGINT,
created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_by   BIGINT,
updated_at   TIMESTAMP
```

### 2.5 Defaults Comunes

| Campo | Default |
|-------|---------|
| UUID PK | `gen_random_uuid()` |
| created_at | `CURRENT_TIMESTAMP` |
| status | `1` |
| currency | `'MXN'` |
| exchange_rate | `1` |
| discount | `0` |

---

## 3. Estructura de Tabla Ejemplo: invoice

```sql
CREATE TABLE tenant_fiscal.invoice (
    invoice_uuid        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiscal_uuid         UUID,
    place_of_issue      VARCHAR,
    payment_method      VARCHAR,
    document_type       CHAR NOT NULL,
    total               NUMERIC NOT NULL,
    exchange_rate       NUMERIC DEFAULT 1,
    currency            VARCHAR DEFAULT 'MXN',
    discount            NUMERIC DEFAULT 0,
    subtotal            NUMERIC NOT NULL,
    payment_conditions  VARCHAR,
    payment_form        VARCHAR,
    issue_date          DATE NOT NULL,
    certification_date  TIMESTAMP,
    folio               VARCHAR,
    series              VARCHAR,
    version             NUMERIC NOT NULL,
    xml_content         TEXT,
    status              INTEGER DEFAULT 1,
    issuer_uuid         UUID NOT NULL,
    receiver_uuid       UUID NOT NULL,
    created_by          BIGINT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by          BIGINT,
    updated_at          TIMESTAMP,
    accounting_sent_date TIMESTAMP,
    accounting_date     DATE
);
```

---

## 4. Mapeo para STM-305

### 4.1 Nombres de Tablas

| JIRA Original | PostgreSQL Final |
|---------------|------------------|
| EstadoCuenta | `account_statement` |
| EstadoCuentaFacturas | `account_statement_invoice` |
| EstadoCuentaDescuentos | `account_statement_discount` |
| EstadoCuentaNotasCredito | `account_statement_credit_note` |
| EstadoCuentaPagos | `account_statement_payment` |
| EstadoCuentaOrdenesCompra | `account_statement_purchase_order` |
| EstadoCuentaRecepciones | `account_statement_reception` |

### 4.2 Nombres de Columnas (tabla principal)

| JIRA Original | PostgreSQL Final | Tipo |
|---------------|------------------|------|
| IdEstadoCuenta | `account_statement_uuid` | UUID |
| NumeroProveedor | `vendor_number` | BIGINT |
| Anio | `year` | INTEGER |
| Mes | `month` | SMALLINT |
| Version | `version` | INTEGER |
| Estatus | `status` | INTEGER |
| MontoInicial | `initial_balance` | NUMERIC(18,2) |
| MontoFinal | `final_balance` | NUMERIC(18,2) |
| FechaProceso | `process_date` | TIMESTAMP |
| FechaRevision | `review_date` | TIMESTAMP |
| FechaEmision | `issue_date` | TIMESTAMP |
| FechaInicioPeriodo | `period_start_date` | DATE |
| FechaFinPeriodo | `period_end_date` | DATE |
| IdEstadoCuentaAnterior | `previous_statement_uuid` | UUID |
| - | `created_by` | BIGINT |
| - | `created_at` | TIMESTAMP |
| - | `updated_by` | BIGINT |
| - | `updated_at` | TIMESTAMP |

### 4.3 Decision: UUID (aplicada)

Se usa **UUID** para mantener consistencia con las tablas existentes en `tenant_finance`:
- `purchase_order_uuid`
- `reception_id` (UUID)
- `accounts_payable_uuid`
- `rebate_uuid`

---

## 5. Conclusion

Las tablas de STM-305 siguen estas convenciones:

1. **Tablas:** Ingles, snake_case, singular (`account_statement`, no `EstadoCuenta`)
2. **Columnas:** Ingles, snake_case (`initial_balance`, no `MontoInicial`)
3. **PK:** `{tabla}_uuid` UUID con `gen_random_uuid()`
4. **FK:** `{tabla_ref}_uuid` (ej: `account_statement_uuid`)
5. **Auditoria:** 4 campos estandar (created_by, created_at, updated_by, updated_at)
6. **Moneda:** Patron existente (currency, amount, exchange_rate, base_currency, base_amount)
7. **Comentarios:** En espanol

