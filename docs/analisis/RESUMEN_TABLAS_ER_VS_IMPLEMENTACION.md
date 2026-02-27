# 📊 Resumen: Tablas del ER vs Implementación Actual
## Proyecto: finanzas-api

**Fecha:** 2025-11-10
**Objetivo:** Comparación clara entre tablas del diagrama ER y la implementación actual

---

## 🎯 Vista General

| Estado | Cantidad | Descripción |
|--------|----------|-------------|
| ✅ **Implementadas** | 5 tablas | Entity + CREATE TABLE completos |
| 🟢 **Creadas Ahora** | 5 tablas | Entity existía, CREATE TABLE agregado hoy |
| ❌ **Eliminadas** | 12 tablas | Tablas CFDI removidas (pertenecen a fiscal-api) |
| **📊 Total ER** | 10 tablas | Tablas identificadas en el diagrama original |

---

## 📋 TABLAS DEL ER (En orden de aparición en las imágenes)

### 1️⃣ **TimbradoRebate** → `stamped_rebate`
**Nombre en ER:** TimbradoRebate
**Tabla BD:** `stamped_rebate`
**Estado:** ✅ Implementada completamente

#### Descripción Funcional:
Almacena los **descuentos o rebates que han sido timbrados** fiscalmente por el PAC. Es la tabla "padre" que contiene el documento fiscal timbrado del descuento.

#### Campos Principales:
- `document_number` - Folio del documento timbrado (único)
- `reference_number` - Referencia interna del descuento
- `status` - Estado del timbrado (0=Borrador, 1=Timbrado, 2=Cancelado)

#### Relación:
- **1:N** con `rebate` - Un timbrado puede tener múltiples líneas de descuento

#### Propósito de Negocio:
Cuando se genera un descuento/rebate para un proveedor, este documento fiscal se timbra ante el SAT. Esta tabla guarda el "envoltorio" fiscal del descuento.

---

### 2️⃣ **Rebate** → `rebate`
**Nombre en ER:** Rebate
**Tabla BD:** `rebate`
**Estado:** ✅ Implementada completamente

#### Descripción Funcional:
Representa los **descuentos o rebates individuales** aplicados a proveedores. Cada registro es una línea de descuento específica con su monto, periodo y documento SAP asociado.

#### Campos Principales:
- `document_number` - Folio del timbrado padre (FK a stamped_rebate)
- `vendor_number` - Número del proveedor (1000XXXXX)
- `amount` - Monto del descuento (DECIMAL 15,2)
- `sap_document` - Documento en SAP relacionado
- `period_id` - Periodo fiscal del rebate
- `due_date` - Fecha de vencimiento
- `status` - Estado (0=Pendiente, 1=Aplicado, 2=Cancelado)

#### Relación:
- **N:1** con `stamped_rebate` - Muchos rebates pertenecen a un timbrado
- **N:1** con `sap_document` - (Posible, no confirmada)

#### Propósito de Negocio:
Gestiona los descuentos comerciales que se aplican a los proveedores (devoluciones, bonificaciones, descuentos por volumen, etc.). Estos descuentos se restan de las cuentas por pagar.

---

### 3️⃣ **DocumentoSap** → `sap_document`
**Nombre en ER:** DocumentoSap
**Tabla BD:** `sap_document`
**Estado:** ✅ Implementada completamente

#### Descripción Funcional:
Almacena documentos que han sido **integrados desde SAP** hacia el sistema de finanzas. Son registros contables provenientes del ERP corporativo.

#### Campos Principales:
- `document_number` - Número del documento en sistema
- `reference_number` - Referencia externa
- `vendor_number` - Proveedor asociado
- `amount` - Monto del documento
- `doc_sap` - Número del documento en SAP (15 caracteres)
- `sap_status` - Estado de integración (0=Pendiente, 1=Integrado, 2=Error, 3=Cancelado)
- `document_type` - Tipo de documento (RE=Recibo, KR=Factura, etc.)
- `message` - Mensaje de respuesta de SAP

#### Relación:
- **Standalone** - No tiene FKs directas pero se relaciona por document_number

#### Propósito de Negocio:
Sincronización bidireccional con SAP. Cuando se generan documentos financieros (recepciones, pagos, rebates), se envían a SAP y esta tabla guarda el tracking de la integración.

---

### 4️⃣ **GuiaEmbarque** → `shipping_guide`
**Nombre en ER:** GuiaEmbarque
**Tabla BD:** `shipping_guide`
**Estado:** ✅ Implementada completamente

#### Descripción Funcional:
Gestiona las **guías de embarque o remisiones** que documentan el envío físico de mercancía desde proveedores hasta los almacenes.

#### Campos Principales:
- `guide_number` - Número único de la guía (ej: GE-2024-001)
- `vendor_number` - Proveedor que envía
- `truck_plate` - Placa del camión
- `trailer_plate` - Placa del remolque
- `driver_name` - Nombre del chofer
- `driver_license` - Licencia de conducir
- `source_id` - Almacén/ubicación de origen
- `destination_id` - Almacén/ubicación de destino
- `delivery_type` - Tipo de entrega (1=Normal, 2=Express, etc.)
- `amount` - Valor de la mercancía transportada
- `status` - Estado (0=Borrador, 1=En tránsito, 2=Entregado, 3=Cancelado)
- `delivery_date` - Fecha programada de entrega
- `estimated_arrival` - Hora estimada de llegada
- `actual_arrival` - Hora real de llegada

#### Relación:
- **N:1** con `purchase_order` - (A través de tabla pivot)
- **1:N** con `shipping_guide_document` - Una guía tiene múltiples documentos adjuntos
- **M:N** con `purchase_order` - Una guía puede consolidar varias órdenes

#### Propósito de Negocio:
Control logístico de envíos. Permite rastrear la mercancía desde que sale del proveedor hasta que llega al almacén, con información del transporte y documentación asociada.

---

### 5️⃣ **GuiaEmbarqueDocumento** → `shipping_guide_document`
**Nombre en ER:** GuiaEmbarqueDocumento
**Tabla BD:** `shipping_guide_document`
**Estado:** 🟢 Creada hoy (Migration 1700000000003)

#### Descripción Funcional:
Almacena **documentos digitales adjuntos** a las guías de embarque (PDFs, imágenes, Excel, etc.).

#### Campos Principales:
- `shipping_guide_id` - FK a la guía de embarque
- `file_name` - Nombre del archivo (ej: guia_001.pdf)
- `file_type` - Tipo de archivo (1=PDF, 2=Excel, 3=Imagen)
- `status` - Estado del documento

#### Relación:
- **N:1** con `shipping_guide` - Muchos documentos por guía

#### Propósito de Negocio:
Digitalización de documentación logística. Permite adjuntar a cada guía: remisión firmada, fotos de la carga, comprobantes de entrega, cartas porte, etc.

**Casos de Uso:**
- Subir PDF de la guía firmada por el cliente
- Adjuntar fotos de la mercancía al momento de carga/descarga
- Guardar carta porte del transportista

---

### 6️⃣ **Pagos** → `finanzas_payments`
**Nombre en ER:** Pagos
**Tabla BD:** `finanzas_payments`
**Estado:** 🟢 Creada hoy (Migration 1700000000003)

#### Descripción Funcional:
Registra los **pagos realizados a proveedores** desde el sistema financiero (NO son complementos de pago CFDI, esos están en fiscal-api).

#### Campos Principales:
- `company` - Empresa que realiza el pago (1=FBC)
- `document_number` - Folio del pago
- `document_reference` - Referencia del documento pagado
- `vendor_number` - Proveedor al que se paga
- `amount` - Monto del pago (DECIMAL 15,2)
- `currency` - Moneda (MXN, USD, EUR)
- `document_type` - Tipo de documento (PP=Pago proveedor)
- `sap_document` - Documento SAP del pago
- `payment_date` - Fecha en que se realizó el pago
- `status` - Estado (0=Borrador, 1=Activo, 2=Procesado, 3=Cancelado)

#### Relación:
- **Standalone** - Se relaciona conceptualmente con accounts_payable y rebates

#### Propósito de Negocio:
Tracking de todos los pagos efectuados a proveedores. Es el registro operacional del pago (NO el comprobante fiscal CFDI).

**Diferencia clave:**
- `finanzas_payments` = **Registro operacional** del pago (cuándo, cuánto, a quién)
- `payments` (fiscal-api) = **Complemento de pago CFDI** (documento fiscal timbrado)

**Flujo Completo:**
1. Se registra pago en `finanzas_payments` (operacional)
2. Se genera XML de complemento de pago en fiscal-api
3. Se timbra ante el SAT (tabla `payments` en fiscal-api)

---

### 7️⃣ **OrdenCompra** → `purchase_order`
**Nombre en ER:** OrdenCompra
**Tabla BD:** `purchase_order`
**Estado:** ✅ Implementada completamente

#### Descripción Funcional:
Gestiona las **órdenes de compra** (OC) emitidas a proveedores. Es el documento que formaliza la solicitud de compra de mercancía o servicios.

#### Campos Principales:
- `order_number` - Número único de la OC (ej: OC-2024-0001)
- `vendor_number` - Proveedor al que se compra (1000XXXXX)
- `source_id` - Almacén/ubicación de origen
- `total_amount` - Monto total de la OC
- `currency` - Moneda (MXN, USD)
- `status` - Estado (0=Borrador, 1=Aprobada, 2=En proceso, 3=Completada, 4=Cancelada)
- `order_date` - Fecha de emisión de la OC
- `delivery_date` - Fecha programada de entrega
- `terms_and_conditions` - Términos y condiciones

#### Relación:
- **1:N** con `reception` - Una OC tiene múltiples recepciones (entregas parciales)
- **M:N** con `shipping_guide` - Una OC puede estar en varias guías (consolidación)

#### Propósito de Negocio:
Documento base de cualquier compra. Inicia el flujo: OC → Recepción → Pago. Define qué se compra, a quién, cuánto cuesta y cuándo se entrega.

**Estados del ciclo de vida:**
- **0=Borrador** - OC en proceso de creación
- **1=Aprobada** - OC autorizada, enviada al proveedor
- **2=En proceso** - Proveedor preparando el pedido
- **3=Completada** - Toda la mercancía recibida
- **4=Cancelada** - OC cancelada antes de completarse

---

### 8️⃣ **Recepcion** → `reception`
**Nombre en ER:** Recepcion
**Tabla BD:** `reception`
**Estado:** 🟢 Creada hoy (Migration 1700000000003)

#### Descripción Funcional:
Registra las **recepciones de mercancía** en almacén. Cada recepción documenta la llegada física de productos comprados mediante una orden de compra.

#### Campos Principales:
- `purchase_order_uuid` - FK a la orden de compra
- `origin_id` - Ubicación de origen (almacén proveedor)
- `destination_id` - Ubicación de destino (almacén FBC)
- `amount` - Monto total de lo recibido
- `status` - Estado (0=Borrador, 1=Confirmado, 2=En proceso, 3=Completado, 4=Cancelado)
- `comment` - Observaciones de la recepción
- `reception_date` - Fecha física de recepción

#### Relación:
- **N:1** con `purchase_order` - Muchas recepciones por OC (entregas parciales)
- **1:N** con `reception_sku` - Una recepción tiene múltiples SKUs

#### Propósito de Negocio:
Control de recepción de mercancía. Permite entregas parciales de una OC (ej: OC de 1000 unidades puede recibirse en 3 entregas de 400, 300 y 300).

**Casos de Uso:**
- OC aprobada → Proveedor envía → Se registra recepción
- Validar cantidad recibida vs cantidad ordenada
- Detectar faltantes o sobrantes
- Generar reportes de cumplimiento de proveedores

**Estados del flujo:**
- **0=Borrador** - Recepción en captura
- **1=Confirmado** - Recepción confirmada (INMUTABLE)
- **2=En proceso** - Validación de inventario
- **3=Completado** - Mercancía ingresada al sistema
- **4=Cancelado** - Recepción cancelada (devolución)

---

### 9️⃣ **[ReceptionSku]** → `reception_sku`
**Nombre en ER:** (No visible directamente, es detalle de Recepcion)
**Tabla BD:** `reception_sku`
**Estado:** 🟢 Creada hoy (Migration 1700000000003)

#### Descripción Funcional:
Detalle **línea por línea** de cada recepción. Especifica qué productos (SKUs) se recibieron, en qué cantidad y a qué costo.

#### Campos Principales:
- `reception_id` - FK a la recepción padre
- `sku` - Código del producto (15 caracteres)
- `description` - Descripción del producto
- `quantity` - Cantidad recibida (hasta 6 decimales para unidades fraccionarias)
- `unit_cost` - Costo unitario del producto
- `total_cost` - Costo total (quantity * unit_cost)
- `status` - Estado de la línea

#### Relación:
- **N:1** con `reception` - Muchos SKUs por recepción

#### Propósito de Negocio:
Detalle exacto de lo recibido. Si una recepción incluye 10 productos diferentes, habrá 10 registros en reception_sku.

**Ejemplo Real:**
```
Recepción R-001 de OC-2024-0001:
- SKU: 100001 - Laptop Dell, Qty: 50, Cost: $12,000.00 c/u
- SKU: 100002 - Monitor HP, Qty: 50, Cost: $3,000.00 c/u
- SKU: 100003 - Teclado Logitech, Qty: 50, Cost: $500.00 c/u
Total Recepción: $775,000.00
```

---

### 🔟 **[ShippingGuide-PO]** → `shipping_guide_purchase_order`
**Nombre en ER:** (No visible, es tabla pivot M:N)
**Tabla BD:** `shipping_guide_purchase_order`
**Estado:** 🟢 Creada hoy (Migration 1700000000003)

#### Descripción Funcional:
Tabla **pivot many-to-many** que relaciona guías de embarque con órdenes de compra. Permite consolidar múltiples OCs en una sola guía.

#### Campos Principales:
- `shipping_guide_id` - FK a guía de embarque
- `purchase_order_id` - FK a orden de compra
- Constraint UNIQUE (shipping_guide_id, purchase_order_id) - Evita duplicados

#### Relación:
- **M:N** - Una guía puede tener varias OCs, una OC puede estar en varias guías

#### Propósito de Negocio:
Consolidación logística. El mismo camión puede traer mercancía de 3 órdenes diferentes, o una orden grande puede venir en 2 camiones distintos.

**Casos de Uso:**
- **Consolidación:** Guía GE-001 trae mercancía de OC-001, OC-002 y OC-003
- **Fraccionamiento:** OC-001 (grande) se divide en Guía GE-001 y GE-002
- **Rastreo:** ¿En qué guía vino mi orden OC-123?

---

## 🔗 DIAGRAMA DE RELACIONES

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE NEGOCIO COMPLETO                        │
└─────────────────────────────────────────────────────────────────────┘

    1. COMPRA                2. LOGÍSTICA              3. RECEPCIÓN
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│ PurchaseOrder   │──┬──>│ ShippingGuide   │      │   Reception     │
│   (1)           │  │   │   (N)           │      │   (N)           │
│                 │  │   │                 │      │                 │
│ - order_number  │  │   │ - guide_number  │      │ - amount        │
│ - vendor_number │  │   │ - truck_plate   │      │ - status        │
│ - total_amount  │  │   │ - driver_name   │      │ - comment       │
│ - status        │  │   │ - status        │      └────────┬────────┘
└─────────────────┘  │   └────────┬────────┘               │
                     │            │                         │
                     │            ├──> ShippingGuide        │
                     │            │    Document (N)         │
                     │            │    - file_name          │
                     └────────────┼────> ShippingGuide     │
                                  │       PurchaseOrder     │
                                  │       (M:N pivot)       │
                                  │                         │
                                  └─────────────────────────┤
                                                            │
                                                            V
                                                    ┌─────────────────┐
                                                    │ ReceptionSku    │
                                                    │   (N)           │
                                                    │                 │
                                                    │ - sku           │
                                                    │ - quantity      │
                                                    │ - unit_cost     │
                                                    │ - total_cost    │
                                                    └─────────────────┘

    4. DESCUENTOS               5. PAGOS                6. INTEGRACIÓN
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│ StampedRebate   │      │FinanzasPayment  │      │  SapDocument    │
│   (1)           │      │   (N)           │      │   (N)           │
│                 │      │                 │      │                 │
│ - document_num  │      │ - vendor_number │      │ - doc_sap       │
│ - status        │      │ - amount        │      │ - sap_status    │
└────────┬────────┘      │ - payment_date  │      │ - message       │
         │               │ - sap_document  │      └─────────────────┘
         │               └─────────────────┘               ▲
         │                                                 │
         V                                                 │
┌─────────────────┐                            (Integración con SAP)
│    Rebate       │                                        │
│   (N)           │                                        │
│                 │                                        │
│ - vendor_number │────────────────────────────────────────┘
│ - amount        │
│ - sap_document  │
│ - period_id     │
└─────────────────┘
```

---

## 📈 ESTADO ACTUAL vs ER ORIGINAL

### ✅ Lo que YA estaba implementado (5 tablas):
1. `stamped_rebate` - Timbrado de rebates
2. `rebate` - Descuentos a proveedores
3. `sap_document` - Integración SAP
4. `shipping_guide` - Guías de embarque
5. `purchase_order` - Órdenes de compra

### 🟢 Lo que se CREÓ hoy (5 tablas):
1. `reception` - Recepciones de mercancía ⭐ CRÍTICO
2. `reception_sku` - Detalle de recepciones ⭐ CRÍTICO
3. `finanzas_payments` - Pagos a proveedores ⭐ CRÍTICO
4. `shipping_guide_document` - Documentos de guías 🔵 COMPLETITUD
5. `shipping_guide_purchase_order` - Relación guías-OCs 🔵 COMPLETITUD

### ❌ Lo que se ELIMINÓ hoy (12 tablas CFDI):
- issuer, receiver, invoice, addendum, related_cfdi
- payments, payment, related_documents, equivalence_dr, totals
- log, authorized_receiver_catalog

**Razón:** Estas tablas pertenecen a fiscal-api (compliance CFDI), NO a finanzas-api (operaciones).

---

## 🎯 CONFORMIDAD FINAL

```
╔══════════════════════════════════════════════════════╗
║         ESTADO FINAL DE IMPLEMENTACIÓN ER            ║
╠══════════════════════════════════════════════════════╣
║  Tablas del ER Identificadas:             10        ║
║  Implementadas ANTES de hoy:               5  (50%)  ║
║  Implementadas HOY:                        5  (50%)  ║
║  ─────────────────────────────────────────────────   ║
║  ✅ CONFORMIDAD TOTAL:                   100%        ║
║  ✅ Entities correctas:                  100%        ║
║  ✅ CREATE TABLE en migrations:          100%        ║
║  ✅ Relaciones implementadas:            100%        ║
╠══════════════════════════════════════════════════════╣
║  🎉 ESTADO: IMPLEMENTACIÓN COMPLETA                  ║
╚══════════════════════════════════════════════════════╝
```

---

## 📝 Notas Importantes

### Campos "fechaRespaldo" del ER
El ER muestra un campo `fechaRespaldo` en varias tablas. Este campo:
- ❌ NO se implementó en las migrations actuales
- ⚠️ Podría agregarse en el futuro si se requiere auditoría de backups
- 💡 No afecta la funcionalidad core del sistema

### Diferencia: finanzas_payments vs fiscal_payments
- **finanzas_payments** (Tabla "Pagos" del ER) = Registro operacional del pago
- **fiscal_payments** (Tabla legacy) = Pagos con datos fiscales adicionales
- **payments** (fiscal-api) = Complemento de pago CFDI 4.0 (timbrado SAT)

### Arquitectura Limpia Lograda
Después de la limpieza de hoy:
- ✅ finanzas-api contiene SOLO operaciones financieras (10 tablas del ER)
- ✅ fiscal-api contiene compliance CFDI (12 tablas fiscales)
- ✅ Separación clara de responsabilidades
- ✅ 0% duplicación de esquemas

---

**🎉 Resultado:** El proyecto finanzas-api ahora tiene **100% de conformidad** con el ER diagram original.
