# Ivan - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-03-02 | Estructura de tablas de pagos (payment_header / payment_detail)

**Contexto**: Ivan pidio la estructura y nombre de las tablas de pagos del esquema `tenant_finance`
**Problema**: Necesitaba conocer el modelo de datos de pagos para su desarrollo
**Solucion**: Se le compartio la estructura completa. Detalle a continuacion:
**Jira**: STM-399
**Estado**: Resuelto (informacion entregada)

### Estructura cabecera-detalle

```
payment_header (1) ──── (N) payment_detail
```

### `payment_header` — Cabecera (deposito total al proveedor)

| Columna | Tipo | Null | Default | Descripcion |
|---------|------|------|---------|-------------|
| `payment_header_uuid` | UUID | NO | gen_random_uuid() | **PK** |
| `company` | INTEGER | NO | | Empresa SAP |
| `vendor_number` | INTEGER | NO | | Numero de proveedor |
| `currency` | VARCHAR(3) | NO | 'MXN' | Moneda |
| `total_amount` | NUMERIC(15,2) | NO | | Monto total del deposito |
| `payment_date` | DATE | NO | | Fecha de pago |
| `status` | INTEGER | NO | 1 | Estado |
| `created_by` | BIGINT | SI | | Usuario creador |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | Fecha creacion |
| `updated_by` | BIGINT | SI | | Usuario actualizador |
| `updated_at` | TIMESTAMP | SI | | Fecha actualizacion |

Indices: `vendor_number`, `company`, `payment_date`, `status`

### `payment_detail` — Detalle (lineas de pago a facturas)

> Antes se llamaba `finanzas_payments`, renombrada por STM-399

| Columna | Tipo | Null | Default | Descripcion |
|---------|------|------|---------|-------------|
| `finanzas_payment_uuid` | UUID | NO | gen_random_uuid() | **PK** |
| `payment_header_uuid` | UUID | NO | | **FK → payment_header** |
| `company` | INTEGER | NO | | Empresa SAP |
| `document_number` | VARCHAR(100) | NO | | Numero de documento |
| `document_reference` | VARCHAR(100) | NO | | Referencia del documento |
| `vendor_number` | INTEGER | NO | | Numero de proveedor |
| `amount` | NUMERIC(15,2) | NO | | Monto del pago |
| `currency` | VARCHAR(3) | NO | 'MXN' | Moneda |
| `document_type` | VARCHAR(5) | NO | | Tipo de documento |
| `sap_document` | VARCHAR(50) | NO | | Documento SAP |
| `payment_date` | DATE | NO | | Fecha de pago |
| `status` | INTEGER | NO | 1 | Estado (0-3) |
| `created_by` | BIGINT | SI | | Usuario creador |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | Fecha creacion |
| `updated_by` | BIGINT | SI | | Usuario actualizador |
| `updated_at` | TIMESTAMP | SI | | Fecha actualizacion |

Indices: `document_number`, `vendor_number`, `sap_document`, `payment_date`, `status`, `payment_header_uuid`
Check constraints: `amount >= 0`, `status IN (0,1,2,3)`

---
