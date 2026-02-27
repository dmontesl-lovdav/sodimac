# STM-1378: Creacion del MER Three Way Match

## Resumen

| Campo | Valor |
|-------|-------|
| **JIRA** | STM-1378 |
| **Titulo** | Creacion de MER Three Way Match y Vistas de Consulta |
| **Estado** | Analisis Completado - Pendiente Confirmacion Estatus |
| **Esquema DB** | `tenant_finance` |
| **Base de Datos** | PostgreSQL (b2b_portal) |

---

## Que es Three Way Match?

El **Three Way Match** (Conciliacion de Tres Vias) es un proceso de verificacion financiera que compara:

```
+-----------------+     +-----------------+     +-----------------+
|  ORDEN COMPRA   |     |   RECEPCION     |     |    FACTURA      |
|      (OC)       | <=> |   (Receipt)     | <=> |    (Invoice)    |
|                 |     |                 |     |                 |
| - Numero OC     |     | - Numero Recep  |     | - Serie/Folio   |
| - Fecha OC      |     | - Fecha Recep   |     | - UUID CFDI     |
| - Monto OC      |     | - Monto Recep   |     | - Monto Factura |
+-----------------+     +-----------------+     +-----------------+
         |                       |                       |
         +-----------------------+-----------------------+
                                 |
                                 v
                    +-------------------------+
                    |    THREE WAY MATCH      |
                    |      (Snapshot)         |
                    |                         |
                    | Consolida OC + Recep +  |
                    | Factura + NC + Pago     |
                    +-------------------------+
```

**Proposito**: Verificar que lo ordenado = lo recibido = lo facturado antes de autorizar el pago.

---

## Analisis de Tablas Origen

Se realizo un analisis de las tablas existentes en `tenant_finance` y `tenant_fiscal` para identificar el origen de los datos que alimentaran la tabla `three_way_match`.

### Tablas Origen Identificadas

| Concepto | Esquema | Tabla | Descripcion |
|----------|---------|-------|-------------|
| Orden de Compra | tenant_finance | `purchase_order` | Ordenes de compra del proveedor |
| Recepcion | tenant_finance | `reception` | Recepciones de mercancia (FK a purchase_order) |
| Factura | tenant_fiscal | `invoice` | CFDIs tipo Ingreso (document_type='I') |
| Nota de Credito | tenant_fiscal | `invoice` | CFDIs tipo Egreso (document_type='E') |
| Pago | tenant_finance | `fiscal_payments` | Pagos fiscales con referencia SAP |
| Documento SAP | tenant_finance | `sap_document` | Documentos contables SAP |

---

## Mapeo Origen → Destino (three_way_match)

### Tabla de Mapeo Completa

| # | JIRA (Espanol) | TWM (Destino) | Origen Tabla | Origen Columna | Tipo Origen | Tipo Destino |
|---|----------------|---------------|--------------|----------------|-------------|--------------|
| 1 | numeroProveedor | `vendor_number` | purchase_order | vendor_number | INTEGER | INTEGER |
| 2 | ordenCompra | `purchase_order_number` | purchase_order | order_number | VARCHAR(50) | VARCHAR(50) |
| 3 | fechaOrdenCompra | `purchase_order_date` | purchase_order | order_date | DATE | DATE |
| 4 | montoOrdenCompra | `purchase_order_amount` | purchase_order | total_amount | NUMERIC(15,2) | NUMERIC(15,2) |
| 5 | recepcion | `reception_number` | reception | reception_number | VARCHAR(50) | VARCHAR(50) |
| 6 | fechaRecepcion | `reception_date` | reception | reception_date | DATE | DATE |
| 7 | montoRecepcion | `reception_amount` | reception | amount | NUMERIC(16,2) | NUMERIC(16,2) |
| 8 | serie | `invoice_series` | invoice | series | VARCHAR(25) | VARCHAR(25) |
| 9 | folio | `invoice_folio` | invoice | folio | VARCHAR(49) | VARCHAR(49) |
| 10 | uuid | `invoice_uuid` | invoice | fiscal_uuid | UUID | UUID |
| 11 | fechaTimbrado | `invoice_stamp_date` | invoice | certification_date | TIMESTAMP | TIMESTAMP |
| 12 | montoFactura | `invoice_amount` | invoice | total | NUMERIC(16,2) | NUMERIC(16,2) |
| 13 | numeroNotaCredito | `credit_note_number` | invoice (type=E) | folio | VARCHAR(49) | VARCHAR(50) |
| 14 | montoNotaCredito | `credit_note_amount` | invoice (type=E) | total | NUMERIC(16,2) | NUMERIC(16,2) |
| 15 | numeroDocumento | `document_number` | fiscal_payments | document_number | VARCHAR(100) | VARCHAR(100) |
| 16 | documentoSap | `sap_document` | fiscal_payments | sap_document | VARCHAR(50) | VARCHAR(50) |
| 17 | fechaContable | `accounting_date` | invoice | accounting_date | DATE | DATE |
| 18 | montoContable | `accounting_amount` | sap_document | amount | NUMERIC(15,2) | NUMERIC(15,2) |
| 19 | referenciaPago | `payment_reference` | fiscal_payments | reference_payment | VARCHAR(100) | VARCHAR(100) |
| 20 | fechaPago | `payment_date` | fiscal_payments | payment_date | DATE | DATE |
| 21 | montoPago | `payment_amount` | fiscal_payments | amount | NUMERIC(15,2) | NUMERIC(15,2) |
| 22 | fechaRegistro | `created_at` | (generado) | CURRENT_TIMESTAMP | - | TIMESTAMP |
| 23 | estatus | `status` | (catalogo) | CatEstatusTWM | - | INTEGER |

### Campos Adicionales (Patron tenant_finance)

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| `three_way_match_uuid` | UUID | PK con gen_random_uuid() |
| `currency` | VARCHAR(3) | Moneda (MXN, USD) - de invoice.currency |
| `exchange_rate` | NUMERIC(18,6) | Tipo de cambio - de invoice.exchange_rate |
| `created_by` | BIGINT | Usuario que creo |
| `updated_by` | BIGINT | Usuario que actualizo |
| `updated_at` | TIMESTAMP | Fecha actualizacion |

---

## Estructura de Tablas Origen (Detalle)

### tenant_finance.purchase_order
```
purchase_order_uuid  UUID (PK)
order_number         VARCHAR(50)    -- ordenCompra
vendor_number        INTEGER        -- numeroProveedor
total_amount         NUMERIC(15,2)  -- montoOrdenCompra
currency             VARCHAR(3)
order_date           DATE           -- fechaOrdenCompra
delivery_date        DATE
status               INTEGER
```

### tenant_finance.reception
```
reception_id         UUID (PK)
purchase_order_uuid  UUID (FK)      -- Relacion con OC
reception_number     VARCHAR(50)    -- recepcion
amount               NUMERIC(16,2)  -- montoRecepcion
reception_date       DATE           -- fechaRecepcion
status               NUMERIC(2,0)
```

### tenant_fiscal.invoice
```
invoice_uuid         UUID (PK)
fiscal_uuid          UUID           -- uuid (CFDI)
series               VARCHAR(25)    -- serie
folio                VARCHAR(49)    -- folio
total                NUMERIC(16,2)  -- montoFactura
currency             VARCHAR(3)
exchange_rate        NUMERIC(18,6)
certification_date   TIMESTAMP      -- fechaTimbrado
accounting_date      DATE           -- fechaContable
document_type        CHAR(1)        -- 'I'=Factura, 'E'=NC
status               INTEGER
```

### tenant_finance.fiscal_payments
```
fiscal_payment_uuid  UUID (PK)
vendor_number        INTEGER
document_number      VARCHAR(100)   -- numeroDocumento
sap_document         VARCHAR(50)    -- documentoSap
reference_payment    VARCHAR(100)   -- referenciaPago
payment_date         DATE           -- fechaPago
amount               NUMERIC(15,2)  -- montoPago
currency             VARCHAR(3)
status               INTEGER
```

---

## Decisiones Confirmadas

| # | Pregunta | Respuesta | Fuente |
|---|----------|-----------|--------|
| 1 | Constraint UNIQUE | `(vendor_number, purchase_order_number, reception_number)` | JIRA |
| 2 | Granularidad | Un registro por OC + Recepcion | JIRA |
| 3 | Tipo de tabla | Snapshot (solo INSERT por ejecucion batch) | Contexto: Batch de Boneli |
| 4 | Esquema destino | `tenant_finance` | Analisis de tablas relacionadas |

---

## Pregunta Pendiente para Ivan

### Catalogo de Estatus (1-5)

> **Pregunta**: Cual es el significado de cada valor del estatus?

| Valor | Propuesta | Confirmado? |
|-------|-----------|-------------|
| 1 | Pendiente | ___ |
| 2 | Parcial | ___ |
| 3 | Conciliado | ___ |
| 4 | Discrepancia | ___ |
| 5 | Cerrado | ___ |

---

## Scripts Creados

| # | Script | Descripcion | Estado |
|---|--------|-------------|--------|
| 1 | `01_STM-1378_create_table.sql` | Tabla three_way_match con indices | Creado (pendiente ajustar tipos) |
| 2 | `02_STM-1378_create_catalog.sql` | Catalogo CatEstatusTWM | Creado |
| 3 | `03_STM-1378_create_views.sql` | 5 vistas de consulta | Creado |
| 4 | `04_STM-1378_test_data.sql` | Datos de prueba | Creado |
| 5 | `05_STM-1378_validate.sql` | Script de validacion | Creado |

---

## Vistas de Consulta

| Vista | Descripcion |
|-------|-------------|
| `vw_three_way_match` | Vista basica con descripcion de estatus |
| `vw_three_way_match_summary` | Totales por proveedor |

---

## Checklist de Implementacion

- [x] Analisis de requerimientos del JIRA
- [x] Exploracion de tablas origen en tenant_finance y tenant_fiscal
- [x] Mapeo de columnas origen → destino
- [x] Crear script DDL tabla
- [x] Crear script catalogo
- [x] Crear script vistas
- [x] Crear datos de prueba
- [x] Crear script validacion
- [ ] **Confirmar estatus con Ivan**
- [ ] Ajustar tipos de datos en DDL segun mapeo
- [ ] Ejecutar scripts en ambiente local
- [ ] Validar con script de validacion

---

## Referencias

- [JIRA STM-1378](https://jira.falabella.tech/browse/STM-1378)
- [STM-305 (modelo Estado de Cuenta)](../STM-305/README.md)
- [Catalogos API](../../APP03022-mrch.backend.somx.catalogos-api)
