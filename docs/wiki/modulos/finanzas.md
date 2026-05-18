# Módulo Finanzas — tenant_finance

> Análisis funcional + estructural del schema `tenant_finance` de `b2b_portal` (PostgreSQL).
> Fuente: BD local sincronizada con UAT al 2026-05-15. ER de referencia provisto por equipo.

## Resumen

`tenant_finance` es el **dominio operativo pre-fiscal** del portal B2B. Gestiona toda la cadena de negocio con proveedores: OC, recepción, conciliación, descuentos, pagos, contabilidad SAP, estado de cuenta y bloqueos. 30 tablas, sin FKs declaradas (relaciones por convención de aplicación), agrupadas en 9 dominios:

1. **Pagos operativos** (registro del pago al proveedor, refactor en curso)
2. **Descuentos comerciales / rebates** (operativo + timbrado)
3. **Cuentas por pagar** (registro contable SAP)
4. **Estado de cuenta** (snapshot mensual del proveedor)
5. **Cadena logística** (OC, recepciones, guías de embarque)
6. **Three Way Match** (conciliación OC vs recepción vs factura)
7. **Bloqueos y addenda manual**
8. **Catálogos** (PAC, versiones — duplicados de tenant_fiscal)
9. **Bitácora y control**

**Lo que NO hace finanzas**: emisión, almacenamiento o validación de CFDI ante SAT. Eso vive en `tenant_fiscal`.

## Inventario de tablas

### Grupo 1 — Pagos operativos

> Refactor en curso: `fiscal_payments` (single-row) → `payment_header` + `payment_detail` (1:N).

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `fiscal_payments` | `fiscal_payment_uuid` (PK), `payment_number` (UNIQUE en local; en UAT **falta el constraint** → permite duplicados), `company`, `document_number`, `reference_number`, `vendor_number`, `amount`, `currency`, `document_type`, `sap_document`, `payment_date`, `status`, `payment_method`, `bank_account`, `reference_payment` | **Pago operativo** al proveedor. ⚠ Nombre engañoso: vive en finanzas, no en fiscal. Tabla legacy en proceso de reemplazo. |
| `payment_header` | `payment_header_uuid`, `company`, `anio`, `vendor_number`, `currency`, `total_amount`, `payment_date`, `status` | Encabezado del pago refactorizado (agrupa N detalles). |
| `payment_detail` | `finanzas_payment_uuid` (PK), `payment_header_uuid` (lógico), `company`, `document_number`, `document_reference`, `vendor_number`, `amount`, `currency`, `document_type`, `sap_document`, `payment_date`, `status` | Detalle del pago refactorizado. 1 header → N details. |

### Grupo 2 — Descuentos comerciales (rebates)

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `rebate` | `rebate_uuid` (PK), `document_number`, `reference_number`, `sap_document`, `vendor_number`, `amount`, `source`, `period_id`, `due_date`, `posting_date`, `status` | Descuento comercial operativo (pre-timbrado). FK lógica `document_number → stamped_rebate.document_number`. |
| `stamped_rebate` | `stamped_rebate_uuid`, `document_number`, `reference_number`, `status`, `invoice_fiscal_uuid` (lógico → `tenant_fiscal.invoice.fiscal_uuid`) | Descuento ya timbrado fiscalmente. Vincula al CFDI de NC en `tenant_fiscal`. |

### Grupo 3 — Cuentas por pagar

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `accounts_payable` | `accounts_payable_uuid`, `company`, `document_date`, `reference_number`, `document_number`, `currency`, `exchange_rate`, `debit_credit`, `gl_account`, `vendor_number`, `amount`, `branch`, `payment_term`, `due_date`, `hold_indicator`, `source_system`, `posting_date`, `document_class`, `reference_id`, `cost_center`, `profit_center`, `sent_flag`, `period_id`, `etl_source` | Registro contable SAP de cuenta por pagar. Origen ETL del ERP. |
| `sap_document` | `sap_document_uuid`, `document_number`, `reference_number`, `vendor_number`, `amount`, `source`, `doc_sap`, `message`, `sap_status`, `document_type` | Documento contable enviado a SAP con su respuesta/status. |

### Grupo 4 — Estado de cuenta (snapshot mensual proveedor)

> Cabecera + 6 sub-tablas: 1 `account_statement` agrupa todos los movimientos del proveedor en el período.

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `account_statement` | `account_statement_uuid`, `vendor_number`, `year`, `month`, `version`, `status`, `initial_balance`, `final_balance`, `process_date`, `period_start_date`, `period_end_date`, `previous_statement_uuid` (lógico, encadena versiones) | Cabecera del estado de cuenta mensual. |
| `account_statement_invoice` | `account_statement_uuid` (lógico), `invoice_type`, `series`, `folio`, `uuid`, `stamp_date`, `accounting_date`, `payment_date`, `amount`, `payment_id`, `invoice_status` | Facturas del período. |
| `account_statement_credit_note` | `account_statement_uuid` (lógico), `document_number`, `series`, `folio`, `uuid`, `issue_date`, `accounting_date`, `amount` | Notas de crédito del período. |
| `account_statement_discount` | `account_statement_uuid` (lógico), `document_number`, `reference_number`, `series`, `folio`, `uuid`, `discount_date`, `accounting_date`, `amount` | Descuentos del período. |
| `account_statement_payment` | `account_statement_uuid` (lógico), `payment_id`, `document_number`, `reference_number`, `payment_date`, `amount` | Pagos del período. |
| `account_statement_purchase_order` | `account_statement_uuid` (lógico), `order_number`, `document_date`, `due_date`, `amount`, `source_id` | OCs del período. |
| `account_statement_reception` | `account_statement_uuid` (lógico), `reception_number`, `order_number`, `document_date`, `reception_date`, `due_date`, `amount`, `source_id` | Recepciones del período. |

### Grupo 5 — Cadena logística (OC y entrega)

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `purchase_order` | `purchase_order_uuid`, `order_number`, `vendor_number`, `source_id`, `total_amount`, `currency`, `status`, `order_date`, `delivery_date`, `terms_and_conditions` | Orden de compra. |
| `reception` | `reception_id`, `purchase_order_uuid` (lógico), `origin_id`, `destination_id`, `amount`, `status`, `reception_date`, `reception_number`, `guide_number` | Recepción de mercancía contra OC. |
| `reception_sku` | `reception_sku_id`, `reception_id` (lógico), `sku`, `description`, `quantity`, `unit_cost`, `total_cost`, `status` | Detalle SKU por recepción. |
| `shipping_guide` | `shipping_guide_id`, `guide_number`, `vendor_number`, `truck_plate`, `trailer_plate`, `driver_name`, `driver_license`, `source_id`, `destination_id`, `delivery_type`, `status`, `delivery_date`, `estimated_arrival`, `actual_arrival`, `is_status_updated` | Guía de embarque del proveedor (Carta Porte). |
| `shipping_guide_document` | `shipping_guide_document_id`, `shipping_guide_id` (lógico), `file_name`, `file_type`, `status` | Documentos asociados a la guía (XML, CSV, etc.). |
| `shipping_guide_purchase_order` | `shipping_guide_purchase_order_id`, `shipping_guide_id` (lógico), `purchase_order_uuid` (lógico) | Tabla puente N:M entre guía de embarque y OCs. |

### Grupo 6 — Three Way Match (conciliación)

> Match OC ↔ Recepción ↔ Factura: detecta diferencias en cantidades, montos y fechas para autorizar o bloquear pago.

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `three_way_match` | `three_way_match_uuid`, `vendor_number`, `purchase_order_number`, `purchase_order_amount`, `reception_number`, `reception_amount`, `invoice_series/folio/uuid`, `invoice_stamp_date`, `invoice_amount`, `credit_note_number`, `credit_note_amount`, `document_number`, `sap_document`, `accounting_date`, `accounting_amount`, `payment_reference`, `payment_date`, `payment_amount`, `currency`, `exchange_rate`, `status` | Resultado consolidado de la conciliación de un movimiento. |
| `twm_ejecucion` | `id`, `estado`, `fechainicio`, `fechafin`, `intento`, `fechabase` | Cada corrida del job TWM. |
| `twm_logs` | `id`, `id_ejecucion` (lógico), `severidad`, `codigo_mensaje`, `mensaje_params`, `stack_trace`, `fecha_hora` | Logs por ejecución. |
| `twm_cifras_control` | `id`, `id_ejecucion` (lógico), `paso`, `total_registros`, `total_monto`, `detalle_json`, `fecha_registro` | Totales por paso del proceso para auditoría. |

### Grupo 7 — Bloqueos y addenda manual

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `vendor_block` | `vendor_block_uuid`, `vendor_number`, `block_reason`, `block_description`, `start_date`, `end_date`, `status`, `auto_unblock`, `block_type` | Bloqueo del proveedor (impide pago). |
| `addendum_manual` | `addendum_manual_uuid`, `invoice_uuid` (lógico → `tenant_fiscal.invoice`), `supplier_number`, `reception_id` (lógico → `reception`), `purchase_order_number`, `supplier_type_id`, `user_id` | Addenda capturada manualmente cuando el proveedor no la envía. Complementa la del CFDI. |

### Grupo 8 — Catálogos (¡duplicados con tenant_fiscal!)

> Ambos schemas tienen `pac_catalog` y `version_catalog` con estructura similar. Posible deuda de diseño (debería vivir en `shared_catalogs`).

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `pac_catalog` | `pac_id`, `name`, `description`, `url`, `license`, `valid_from/to`, `catalog_msg_id`, `status` | Catálogo PAC duplicado. |
| `version_catalog` | `version_id`, `name`, `version`, `document_type`, `pac_id` (lógico), `valid_from/to`, `structure_url`, `status` | Versiones XML duplicado. |

### Grupo 9 — Bitácora y control

| Tabla | Columnas clave | Propósito |
|---|---|---|
| `activity_logs` | `activity_logs_uuid`, `trace_id`, `duration_ms`, `is_error`, `modulo`, `service_name`, `action`, `message`, `message_detail`, `user_id`, `timestamp`, `details` | Bitácora de actividades del módulo (STM-1528). Sin `vendor_number` → filtro por seguridad pendiente de definir. |
| `migrations` | — | Control TypeORM migrations. |

## Relaciones lógicas (sin FK declarada)

```
purchase_order ──< reception ──< reception_sku
                        │
                        └─→ (referenciado por shipping_guide_purchase_order N:M)

shipping_guide ──< shipping_guide_document
              ├──< shipping_guide_purchase_order ─→ purchase_order

vendor_block ─→ vendor (por vendor_number)

payment_header ──< payment_detail

rebate ─→ stamped_rebate (por document_number)
stamped_rebate ─→ tenant_fiscal.invoice (por invoice_fiscal_uuid)

addendum_manual ─→ tenant_fiscal.invoice
              ├─→ reception
              └─→ purchase_order (por número)

account_statement ──┬──< account_statement_invoice
                    ├──< account_statement_credit_note
                    ├──< account_statement_discount
                    ├──< account_statement_payment
                    ├──< account_statement_purchase_order
                    └──< account_statement_reception

account_statement ─→ account_statement (self por previous_statement_uuid, versionado)

twm_ejecucion ──< twm_logs
            └──< twm_cifras_control

three_way_match ─→ purchase_order, reception, tenant_fiscal.invoice, sap_document, fiscal_payments (por números/uuids)

accounts_payable, sap_document — entran vía ETL, referencian vendor_number + document_number
```

## Objetivo del módulo finanzas

Responsabilidad única: **registrar y operar la cadena de negocio del proveedor antes y después del CFDI**, sin emitir comprobantes fiscales.

Concretamente:

1. **Cadena logística** — OC, recepción, guía de embarque, conciliación 3-way.
2. **Pago operativo al proveedor** — calendario, monto, banco, método (≠ CFDI REP).
3. **Descuentos comerciales** — registro operativo del rebate antes y después de su timbrado fiscal.
4. **Cuentas por pagar** — registro contable SAP (origen ETL).
5. **Estado de cuenta del proveedor** — snapshot mensual versionado de todos sus movimientos.
6. **Bloqueos de proveedor** — control de habilitación para pago.
7. **Addenda manual** — captura de addenda cuando el proveedor no la envía con su CFDI.
8. **Bitácora operativa** — `activity_logs` para auditoría de acciones del módulo.

**Lo que NO hace tenant_finance**:
- NO emite, valida ni almacena CFDIs ante SAT (eso es `tenant_fiscal`).
- NO timbra fiscalmente (delega a fiscal-api / PACs).
- NO maneja seguridad/autenticación (eso vive en `core_security`).

## Puntos críticos

### Confusiones de naming

| Tabla | Naming | Realidad |
|---|---|---|
| `fiscal_payments` | sugiere CFDI | **Es pago operativo**, no fiscal. Vive en finanzas. |
| `stamped_rebate` | sugiere algo "estampado" | Es el **rebate timbrado** (con UUID CFDI de NC en fiscal). |
| `pac_catalog`, `version_catalog` | aparecen aquí Y en fiscal | Duplicación entre schemas. Probable deuda. |

### Refactor pagos en curso

Hay 2 modelos coexistiendo:
- **Legacy**: `fiscal_payments` (single-row por pago, lo que toca POST `/fiscal-payments`).
- **Nuevo**: `payment_header` + `payment_detail` (header 1:N detail, lo que toca POST `/finanzas-payment/header-with-details`).

**Pantalla FBC "Finanzas > Pagos"** (`uat.fbusinesscenter.com/finanzas#/finanzas/pagos`) consume `GET /finanzas-payment` → tabla `payment_detail`. En UAT **está vacía** al 2026-05-18 — la pantalla muestra "No se encontraron pagos" sin ser un error. Para poblarla: `POST /finanzas-payment/header-with-details`.

Endpoint correcto para "alta de pago" depende de si el refactor está activo o no — confirmar con Bonelli.

### Sin FKs declaradas

Igual que en `tenant_fiscal`, integridad referencial depende del código de aplicación. Cualquier ingestión SQL directa (ETL, seeds) puede dejar inconsistencias silenciosas.

**Actualización 2026-05-15**: 4 FKs adicionales identificadas (`twm_logs→twm_ejecucion`, `twm_cifras_control→twm_ejecucion`, `stamped_rebate→fiscal.invoice`, `addendum_manual→fiscal.invoice`).

**Actualización 2026-05-18 (UAT)**: Scripts aplicados en UAT. Se agregaron 8 PKs faltantes (1 en `tenant_fiscal.tax`, 7 en `tenant_finance`) y 25 FKs nuevas (21 en `tenant_fiscal`, 4 en `tenant_finance`). Scripts en [docs/db/fks-20260515/](../../db/fks-20260515/).

### Bug histórico encontrado en `rebate` (2026-05-15)

Entity TypeORM tenía props (`supplierNumber`, `documentReference`, `originId`) distintas al contrato HTTP (Zod) y a BD (`vendor_number`, `reference_number`, `source`). POST `/rebates` quedaba con NULLs silenciosos hasta golpear `NOT NULL` constraint. Fix aplicado: alinear props TS con BD/contrato.

### FK lógica invertida `rebate → stamped_rebate`

Diseño actual exige `stamped_rebate` exista ANTES que `rebate` (FK lógica de `rebate.document_number → stamped_rebate.document_number`). Inverso al flujo natural (operativo primero, timbrado después). Probable error de modelo; canal correcto para alta es `POST /rebates/relate` que crea ambos.

## Endpoints relevantes (finanzas-api)

> Detalle en código: [routes/index.ts](../../../APP03022-mrch.backend.somx.finanzas-api/src/routes/index.ts)

Principales recursos:
- `/api/fiscal-payments` — pagos operativos (legacy)
- `/api/finanzas-payment` — pagos operativos (refactor header/detail)
- `/api/rebates` — descuentos comerciales (alta operativa pura)
- `/api/rebates/relate` — alta de rebate + timbrado (STM-973)
- `/api/stamped-rebates` — gestión rebates timbrados
- `/api/accounts-payable` — cuentas por pagar SAP
- `/api/purchase-orders` — OC
- `/api/shipping-guide` — guías embarque
- `/api/three-way-match` — conciliación 3-way
- `/api/vendor-blocks` — bloqueos proveedor
- `/api/account-statement` — estado de cuenta mensual
- `/api/sap-documents` — documentos SAP
- `/api/audit-logs` — bitácora (activity_logs)
- `/api/migo` — documentos MIGO (recepciones SAP)

## Referencias

- ER fuente: Modelo Financiero (lado derecho del diagrama provisto por equipo).
- Hermano: [Módulo Fiscal](fiscal.md).
- Análisis JIRA relacionado:
  - [STM-1421 (filtro seguridad rebates)](../../jiras/STM-1421/STM-1421_analisis.md)
  - [STM-1460 (filtro seguridad finanzas-payment)](../../jiras/STM-1460/STM-1460_analisis.md)
  - [STM-321 (filtro seguridad estado de cuenta)](../../jiras/STM-321/)
  - [STM-1524 (Three Way Match)](../../jiras/STM-1524/)
  - [STM-1461 (Carta Porte / shipping guide)](../../jiras/STM-1461/)
  - [STM-333 (modelo entidad relación administración financiera)](../../jiras/STM-333/)
  - [STM-973 (relación descuento con NC)](../../jiras/STM-973/)
  - [STM-875 (filtro descuentos)](../../jiras/STM-875/)
