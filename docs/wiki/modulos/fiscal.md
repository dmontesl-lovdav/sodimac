# Módulo Fiscal — tenant_fiscal

> Análisis funcional + estructural del schema `tenant_fiscal` de `b2b_portal` (PostgreSQL).
> Fuente: BD local sincronizada con UAT al 2026-05-15. ER de referencia provisto por equipo.

## Resumen

`tenant_fiscal` es la **fuente de verdad fiscal**: gestiona el ciclo de vida del CFDI (Comprobante Fiscal Digital por Internet) ante el SAT. 22 tablas, sin FKs declaradas (relaciones por convención de aplicación), agrupadas en 4 dominios:

1. **Núcleo CFDI** (factura, NC, partes, relaciones entre CFDIs)
2. **Complemento de Pago / REP** (CFDI tipo P y sus dependientes)
3. **Impuestos** (desglose IVA/IEPS, retenciones)
4. **Soporte** (addenda, historial, catálogos PAC/versiones, control, logs)

**Lo que NO hace fiscal**: operación interna pre-fiscal (cuentas por pagar, descuentos comerciales operativos, OC, recepciones, ThreeWayMatch, bloqueos). Esos viven en `tenant_finance`.

## Inventario de tablas

### Grupo 1 — Núcleo CFDI

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `invoice` | `invoice_uuid` (PK), `fiscal_uuid` (UUID SAT TimbreFiscalDigital), `document_type` (I=Factura, E=NC, P=Pago), `total`, `subtotal`, `xml_content`, `issuer_uuid`, `receiver_uuid`, `status`, `accounting_date`, `accounting_sent_date` | CFDI completo. Factura/NC/REP con XML crudo + datos extraídos para query. |
| `issuer` | `issuer_uuid`, `rfc`, `name`, `tax_regime` | Emisor del CFDI (típicamente el proveedor). |
| `receiver` | `receiver_uuid`, `rfc`, `name`, `tax_regime` | Receptor (típicamente Sodimac). |
| `authorized_receiver_catalog` | `authorized_receiver_id`, `receiver_uuid`, `rfc`, `valid_from/to`, `status` | RFCs Sodimac autorizados para recibir CFDI. |
| `related_cfdi` | `related_cfdi_uuid`, `invoice_uuid`, `related_invoice_uuid`, `relation_type` | CFDIs vinculados (NC sustituye factura, NC referencia factura, etc.). Auto-referencia a `invoice`. |

### Grupo 2 — Complemento de Pago (REP)

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `payments` | `payments_uuid` (PK), `fiscal_uuid`, `version`, `payment_date`, `issuer_uuid`, `receiver_uuid`, `xml_content`, `status` | **CFDI Complemento de Pago completo** (1 XML timbrado). |
| `payment` | `payment_uuid`, `payments_uuid` (lógico), `payment_date`, `amount`, `payment_method`, `currency`, `operation_number`, `payer_bank_rfc`, `payer_account`, `beneficiary_bank_rfc`, `beneficiary_account` | Cada pago individual dentro del REP. 1 REP puede tener N pagos. |
| `totals` | `totals_uuid`, `payments_uuid` (lógico), `total_payments_amount`, `total_base_iva_16`, `total_tax_iva_16`, `total_base_iva_8`, `total_tax_iva_8`, `total_base_iva_0`, `total_withholding_iva`, `total_withholding_isr` | Totales agregados del REP (sumatorias y desgloses IVA/ISR). |
| `related_documents` | `related_document_uuid`, `payment_uuid` (lógico), `document_uuid` (→ `invoice`), `amount_paid`, `previous_balance`, `remaining_balance`, `installment_number`, `series`, `folio`, `currency`, `exchange_rate` | Facturas (DoctoRelacionados) pagadas por ese pago. |
| `equivalence_dr` | `equivalence_uuid`, `related_document_uuid` (lógico), `amount_paid`, `previous_balance`, `remaining_balance`, `currency`, `installment_number` | Datos de equivalencia DR cuando hay cambio de divisa. |

### Grupo 3 — Impuestos

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `tax` | `tax_uuid`, `invoice_uuid` (lógico), `total_transferred_taxes`, `total_withheld_taxes` | Encabezado de impuestos por invoice. |
| `tax_detail` | `tax_detail_uuid`, `tax_uuid`, `product_service_code`, `tax_type`, `tax_code`, `factor_type`, `rate_or_quota`, `base`, `amount` | Detalle por concepto/producto-servicio. |
| `tax_transfer` | `tax_transfer_uuid`, `tax_uuid`, `tax_code`, `factor_type`, `rate_or_quota`, `base`, `amount` | IVA/IEPS trasladados. |
| `tax_withholding` | `tax_withholding_uuid`, `tax_uuid`, `tax_code`, `amount` | Retenciones IVA/ISR. |

### Grupo 4 — Soporte / Catálogos / Control

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `addendum` | `addendum_uuid`, `invoice_uuid` o `payments_uuid` (lógico, alternativos), `supplier_number`, `reception_number`, `purchase_order_number`, `shipping_guide_number`, `addendum_content`, `addenda_type`, `supplier_type` | Addenda Sodimac (datos comerciales no-CFDI agregados al XML: OC, recepción, guía). |
| `invoice_status_history` | `history_id`, `invoice_uuid`, `fiscal_uuid`, `status_from`, `status_to`, `changed_by`, `changed_at`, `comment` | Trazabilidad cambios de status invoice. |
| `pac_catalog` | `pac_id`, `name`, `url`, `user_name`, `password`, `license`, `valid_from/to`, `priority` | Catálogo de PACs (proveedores autorizados de timbrado). |
| `version_catalog` | `version_id`, `pac_id` (lógico), `document_type`, `version`, `structure_url`, `valid_from/to` | Versiones de XML aceptadas por PAC + tipo de documento. |
| `payment_file_registry` | `file_registry_id`, `payments_uuid` (lógico), `file_name`, `processing_status`, `error_code`, `error_message`, `supplier_id` | Registro de archivos REP procesados (carga masiva). |
| `payment_response_catalog` | `response_catalog_id`, `response_code`, `response_type`, `description`, `message_template` | Códigos de respuesta del flujo de pago (mensajes WRN/ERR/INFO). |
| `flyway_schema_history` | — | Control de migraciones Flyway. |
| `log` | — | Auditoría/log de aplicación. |

## Relaciones lógicas (sin FK declarada en BD)

```
issuer ──┐
         ├──< invoice ──┐
receiver ┘              ├──< related_cfdi (auto-referencia: related_invoice_uuid → invoice)
                        ├──< addendum (opcional)
                        ├──< invoice_status_history
                        └──< tax ──< tax_detail
                                 ├──< tax_transfer
                                 └──< tax_withholding

issuer ──┐
         ├──< payments ──< payment ──< related_documents ──< equivalence_dr
receiver ┘    │                            └─→ invoice (document_uuid)
              ├──< totals
              ├──< addendum (opcional, addenda de REP)
              └──< payment_file_registry

pac_catalog ──< version_catalog

receiver ──< authorized_receiver_catalog
```

~~Sin FK declarada significa: la integridad referencial **depende del código de aplicación**, no de la BD. Inserts ad-hoc pueden violar invariantes sin que PostgreSQL lo detecte.~~

**Actualización 2026-05-15**: 21 FKs agregadas en local. Script idempotente en [docs/db/fks-20260515/](../../db/fks-20260515/) listo para aplicar en UAT (pendiente autorización).

## Objetivo del módulo fiscal

Responsabilidad única: **gestionar el ciclo CFDI ante el SAT como única fuente de verdad fiscal**.

Concretamente:

1. **Recepción/almacenamiento** de CFDIs que llegan de proveedores: Factura (I), Nota de Crédito (E), Complemento de Pago (P).
2. **Persistencia dual** — XML crudo en `xml_content` + datos extraídos a tablas relacionales para queries y reportes.
3. **Modelado de partes** (Emisor, Receptor) y catálogo de receptores autorizados Sodimac.
4. **Desglose de impuestos** (IVA trasladado, retenciones IVA/ISR) para reportes fiscales y conciliación.
5. **Relaciones entre CFDIs** (NC sustituye factura, REP paga facturas, DR con balances/equivalencias).
6. **Addenda Sodimac** — extensión comercial dentro del XML que apunta a OC/recepción/guía operativa.
7. **Auditoría de status** del CFDI vía `invoice_status_history`.
8. **Timbrado** soportado con catálogo de PACs (`pac_catalog`) y versiones de XML aceptadas (`version_catalog`).
9. **Carga masiva** de complementos de pago (`payment_file_registry`) con códigos de respuesta normalizados.

## Conceptos críticos para evitar confusiones

### "Pago" tiene dos significados según el módulo

| Módulo | Tabla | Significado |
|---|---|---|
| **fiscal** (`tenant_fiscal`) | `payments` + `payment` + `totals` + `related_documents` | **CFDI Complemento de Pago (REP)** — comprobante fiscal que documenta el pago ante SAT |
| **finanzas** (`tenant_finance`) | `fiscal_payments` y/o `finanzas_payment` | **Registro operativo del pago al proveedor** (calendario, monto, banco) |

Son **dos pasos del mismo proceso de negocio**:

1. **Operativo** (finanzas) — registra que hay/hubo pago al proveedor.
2. **Fiscal** (fiscal) — registra el CFDI REP que el proveedor emite por ese pago.

### Naming engañoso

- La tabla `tenant_finance.fiscal_payments` **vive en finanzas** pese a su nombre. Es **operativa**, no fiscal. El endpoint `POST /fiscal-payments` toca esa tabla.
- El "pago fiscal" real (CFDI Complemento de Pago) vive en `tenant_fiscal.payments`.

### Sin FKs declaradas

Toda la integridad referencial del módulo se sostiene por convención de aplicación (TypeORM/JPA en el código). Implicación: cualquier ingestión que omita la app (SQL directo, batches, ETL) puede dejar la BD inconsistente sin error inmediato.

## Flujo de registro de Factura (v1.0 — 2026-06-05)

### Cambios respecto a versión anterior

- Addenda ya **no** se valida desde el XML. Los datos de addenda vienen del FE en el mismo call.
- Validación de tolerancia de importe integrada en `/register`.
- Estatus inicial = **3 (Recibida)**. Eliminado "Pendiente Addenda".

### Endpoints involucrados

| Endpoint | Actor | Qué hace |
|---|---|---|
| `POST /fiscal/xml/process/file` | FE → fiscal-api | Parsea XML, retorna preview (serie, folio, subtotal). NO registra. |
| `GET /purchase-orders/reception/{uuid}` | FE → finanzas-api | Obtiene datos de recepción (amount, OC, proveedor). NO registra. |
| `POST /invoices/register` | FE → fiscal-api | **Registro completo** — toda la lógica vive aquí. |

### Params de `POST /invoices/register`

```
file              MultipartFile  XML del CFDI
idTransaccion     String         UUID trazabilidad bitácora
receptionId       String         UUID de tenant_finance.reception
supplierNumber    String         Número proveedor Sodimac
purchaseOrderNumber String       Número OC
```

### Lógica interna del registro (pasos clave)

1. Lee XML → obtiene `subtotal`
2. Lee `tenant_finance.reception` via JPA (`ReceptionRepository`) → obtiene `amount`
3. Valida `|subtotal - amount| ≤ tolerancia` (parámetro `"Tolerancia por importe"`, `core_utils.cat_parameter` id=3, valor=40) → **BUS057** si supera
4. Guarda `invoice` con status **3 (Recibida)**
5. Guarda `addendum` con `supplier_number`, `purchase_order_number`, `reception_number` poblados

### Tren de Estatus v1.0 (Factura)

18 estatus (1-18), sin Pendiente Addenda. Cancelar = status 1 (Rechazo Comercial) desde status 2 (Recibido Parcial).
Ver detalle completo: [TREN-ESTATUS-v1.0-vs-codigo.md](../../analisis/TREN-ESTATUS-v1.0-vs-codigo.md).

Catálogo `shared_catalogs.status_train` option_id=1 sincronizado con v1.0 (local + UAT pendiente).

---

## Endpoints relevantes (fiscal-api)

> Puerto 8082 local. Detalle en Swagger: http://localhost:8082/swagger-ui/index.html

Principales recursos:
- `/api/invoices` — CRUD + búsqueda CFDI (factura/NC)
- `/api/payments` — Complemento de Pago (REP)
- `/api/addendum` — gestión de addendas Sodimac
- `/api/issuers`, `/api/receivers`, `/api/authorized-receivers` — partes
- `/api/pac-catalog`, `/api/version-catalog` — administración PAC

## Referencias

- ER fuente: provisto por equipo (Modelo Fiscal + Modelo Financiero, diagrama PNG).
- Memoria proyecto: `tenant_fiscal` = 22 tablas, fiscal-api proyecto principal.
- Hermano: [Módulo Finanzas](finanzas.md).
- Documentos relacionados:
  - [ANALISIS_ER_VS_IMPLEMENTACION.md](../../analisis/ANALISIS_ER_VS_IMPLEMENTACION.md)
  - [EXPLICACION_FUNCIONAL_TABLAS.md](../../analisis/EXPLICACION_FUNCIONAL_TABLAS.md)
  - [ANALISIS_TABLAS_CFDI_DUPLICADAS.md](../../analisis/ANALISIS_TABLAS_CFDI_DUPLICADAS.md)
