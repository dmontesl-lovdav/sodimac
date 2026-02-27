# 📚 EXPLICACIÓN FUNCIONAL DE TABLAS POR MICROSERVICIO
## finanzas-api y fiscal-api

**Fecha**: 2025-11-10
**Propósito**: Documentación funcional de cada tabla en ambos microservicios

---

## 🏢 ARQUITECTURA GENERAL

```
┌─────────────────────────────────────────────────┐
│          ECOSISTEMA DE FACTURACIÓN              │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────┐    ┌─────────────────┐  │
│  │   fiscal-api     │    │  finanzas-api   │  │
│  │  (Java/Spring)   │◄───┤  (Node/TypeORM) │  │
│  │                  │    │                 │  │
│  │ Schema:          │    │ Schema:         │  │
│  │ tenant_fiscal    │    │ tenant_finance  │  │
│  │                  │    │                 │  │
│  │ Función:         │    │ Función:        │  │
│  │ - Timbrado CFDI  │    │ - Operaciones   │  │
│  │ - Complementos   │    │ - Logística     │  │
│  │ - SAT Compliance │    │ - Proveedores   │  │
│  └──────────────────┘    └─────────────────┘  │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

# 🔵 FISCAL-API (tenant_fiscal)

## Propósito del Microservicio
**fiscal-api** es responsable de la **generación, timbrado y gestión de Comprobantes Fiscales Digitales por Internet (CFDI)** según las normas del SAT (Servicio de Administración Tributaria de México). Cumple con CFDI 4.0 y complementos de pago versión 2.0.

---

## 📊 TABLAS DE FISCAL-API

### 🔹 GRUPO 1: CATÁLOGOS BASE (Configuración)

#### 1. **pac_catalog**
**Propósito**: Gestión de Proveedores Autorizados de Certificación (PAC)

**Función de Negocio**:
- Almacena los PAC autorizados por el SAT para timbrar CFDIs
- Cada PAC tiene credenciales y URL de servicio web
- Sistema puede usar múltiples PAC con prioridad configurable
- Si un PAC falla, el sistema puede intentar con otro

**Datos Clave**:
- Nombre del PAC (ej: "Finkok", "SW Sapien", "PAC Integrador")
- URL del servicio web para timbrado
- Usuario y contraseña de autenticación
- Licencia otorgada por SAT
- Prioridad (1=primera opción, mayor=backup)
- Vigencia (valid_from/valid_to)

**Ejemplo de Uso**:
```
Cuando una factura necesita timbrarse:
1. Sistema busca PAC con status=1 y mayor prioridad
2. Envía XML a URL del PAC con credenciales
3. PAC devuelve UUID fiscal (TimbreFiscalDigital)
4. Si falla, intenta con siguiente PAC disponible
```

---

#### 2. **version_catalog**
**Propósito**: Versiones de CFDI soportadas por el sistema

**Función de Negocio**:
- Define qué versiones de CFDI puede generar el sistema
- Actualmente: CFDI 3.3 (legacy) y CFDI 4.0 (actual)
- Cada versión tiene estructura XML diferente
- PAC específico para cada versión

**Datos Clave**:
- Versión (ej: 4.0, 3.3)
- Tipo de documento (I=Ingreso, E=Egreso, P=Pago, N=Nómina, T=Traslado)
- PAC asociado
- URL del schema XSD para validación
- Vigencia

**Ejemplo de Uso**:
```
Al generar factura:
- Sistema valida qué versión CFDI usar
- Carga estructura XML según version_catalog
- Valida contra XSD correspondiente
- Envía a PAC configurado para esa versión
```

---

### 🔹 GRUPO 2: PARTES (Emisores y Receptores)

#### 3. **issuer**
**Propósito**: Emisores de comprobantes fiscales (la empresa que factura)

**Función de Negocio**:
- Almacena datos fiscales del emisor (tu empresa)
- Un sistema puede tener múltiples emisores (ej: diferentes sucursales)
- Datos requeridos por SAT en todo CFDI

**Datos Clave**:
- RFC del emisor (obligatorio, validado por SAT)
- Razón social
- Régimen fiscal (catálogo SAT ej: 601=General de Ley)

**Ejemplo de Uso**:
```xml
<cfdi:Emisor Rfc="SOD970101ABC"
             Nombre="Sodimac México SA de CV"
             RegimenFiscal="601" />
```

---

#### 4. **receiver**
**Propósito**: Receptores de comprobantes fiscales (clientes/proveedores)

**Función de Negocio**:
- Almacena datos fiscales del receptor
- Se crea un registro por cada cliente/proveedor
- Datos requeridos en CFDI

**Datos Clave**:
- RFC del receptor
- Razón social
- Régimen fiscal (opcional según tipo de CFDI)

**Ejemplo de Uso**:
```xml
<cfdi:Receptor Rfc="CGE990101GHI"
               Nombre="Cliente General Empresa SA"
               RegimenFiscal="612" />
```

---

#### 5. **authorized_receiver_catalog**
**Propósito**: Lista blanca de receptores autorizados

**Función de Negocio**:
- Control adicional: solo se puede facturar a receptores autorizados
- Útil para B2B donde solo ciertos clientes están aprobados
- Incluye vigencia temporal de autorización

**Datos Clave**:
- Receptor autorizado (FK a receiver)
- Fecha inicio/fin de autorización
- Régimen fiscal específico para esta relación

**Ejemplo de Uso**:
```
Antes de emitir factura:
1. Sistema verifica si receiver_uuid está en authorized_receiver_catalog
2. Valida que fecha actual esté entre valid_from y valid_to
3. Si no está autorizado → rechaza emisión
4. Si está autorizado → continúa proceso
```

---

### 🔹 GRUPO 3: COMPROBANTES FISCALES (CFDI Core)

#### 6. **invoice**
**Propósito**: Comprobantes fiscales digitales (Facturas, Notas de Crédito, etc.)

**Función de Negocio**:
- **TABLA CENTRAL** del sistema fiscal
- Almacena todos los comprobantes fiscales emitidos
- Cada registro = 1 CFDI timbrado por el SAT
- Contiene el XML completo del comprobante

**Datos Clave**:
- `fiscal_uuid`: UUID del SAT (TimbreFiscalDigital) - **folio fiscal oficial**
- `document_type`:
  - I = Ingreso (venta)
  - E = Egreso (nota de crédito/devolución)
  - T = Traslado (remisión sin valor fiscal)
  - N = Nómina
  - P = Pago (complemento de pago)
- `folio` + `series`: Numeración interna (ej: "A-00123")
- `payment_method`:
  - PUE = Pago en Una sola Exhibición
  - PPD = Pago en Parcialidades o Diferido
- `total`, `subtotal`, `discount`: Cálculos fiscales
- `xml_content`: XML completo del CFDI (con TimbreFiscalDigital)
- `status`:
  - 0 = Cancelado (anulado ante SAT)
  - 1 = Vigente (activo)
  - 2 = Pendiente (aún no timbrado)
  - 3 = Rechazado (PAC rechazó timbrado)

**Flujo de Vida**:
```
1. Creación:
   - invoice INSERT con status=2 (pendiente)
   - Se genera XML preliminar

2. Timbrado:
   - XML se envía a PAC
   - PAC devuelve fiscal_uuid + TimbreFiscalDigital
   - Se actualiza xml_content con timbre
   - status → 1 (vigente)

3. Cancelación (si aplica):
   - Solicitud de cancelación a SAT vía PAC
   - Si aprobada: status → 0 (cancelado)
```

**Ejemplo de Registro**:
```
invoice_uuid: 550e8400-e29b-41d4-a716-446655440000
fiscal_uuid: 12345678-1234-5678-1234-567812345678  ← UUID del SAT
document_type: I (Ingreso)
series: A
folio: 00123
payment_method: PPD
subtotal: 10000.00
discount: 0.00
total: 11600.00  (incluye IVA 16%)
status: 1 (vigente)
issuer_uuid: → SOD970101ABC
receiver_uuid: → CGE990101GHI
xml_content: <cfdi:Comprobante...>...</cfdi:Comprobante>
```

---

#### 7. **addendum**
**Propósito**: Addendas personalizadas de clientes

**Función de Negocio**:
- Algunos clientes corporativos requieren datos adicionales en CFDI
- Addenda = sección XML personalizada
- Walmart, Coppel, etc. tienen addendas específicas

**Datos Clave**:
- Invoice asociado (FK)
- Contenido XML de la addenda
- Tipo de addenda

**Ejemplo de Uso**:
```xml
<cfdi:Comprobante>
  ... datos normales ...
  <cfdi:Addenda>
    <walmart:DatosWalmart>
      <OrdenCompra>WM-123456</OrdenCompra>
      <Tienda>2505</Tienda>
    </walmart:DatosWalmart>
  </cfdi:Addenda>
</cfdi:Comprobante>
```

---

#### 8. **related_cfdi**
**Propósito**: Relaciones entre CFDIs

**Función de Negocio**:
- Un CFDI puede relacionarse con otro (ej: Nota de crédito vincula a factura original)
- Requerido por SAT para trazabilidad

**Datos Clave**:
- Invoice origen (FK)
- UUID fiscal del CFDI relacionado
- Tipo de relación (catálogo SAT ej: 01=Nota de crédito de factura)

**Ejemplo de Uso**:
```
Nota de Crédito NC-001 (por devolución de FAC-100):
- Se crea invoice con document_type=E (egreso)
- Se crea related_cfdi:
  - invoice_uuid → NC-001
  - related_fiscal_uuid → UUID fiscal de FAC-100
  - relation_type → 01 (Nota de crédito)
```

---

### 🔹 GRUPO 4: COMPLEMENTOS DE PAGO CFDI 4.0

#### 9. **payments**
**Propósito**: Complementos de pago (cuando pago es posterior a factura)

**Función de Negocio**:
- En México, si facturas con PPD (pago diferido), debes emitir complemento de pago al recibir el dinero
- Es un CFDI tipo "P" (Pago)
- Un complemento puede incluir múltiples pagos

**Datos Clave**:
- `fiscal_uuid`: UUID del SAT del complemento de pago
- Serie y folio (ej: "PP-00001")
- Emisor y receptor (mismos que factura original)
- XML del complemento
- `version`: 2.0 (versión actual del complemento de pago)

**Ejemplo de Uso**:
```
Cliente compró $100,000 facturado con PPD (pago diferido):
1. Se emitió invoice FAC-001 por $100,000
2. Cliente paga $50,000 el día 15
3. Se crea payments:
   - folio: PP-00001
   - payment_date: 2025-11-15
   - Se timbra ante SAT
   - Se obtiene fiscal_uuid del complemento
```

---

#### 10. **payment**
**Propósito**: Pagos individuales dentro de un complemento

**Función de Negocio**:
- Un complemento de pago puede tener múltiples pagos
- Cada payment es un pago específico (transferencia, efectivo, etc.)

**Datos Clave**:
- `payments_uuid`: FK al complemento padre
- Monto del pago
- Fecha del pago
- Método de pago (03=Transferencia, 01=Efectivo, etc.)
- Moneda y tipo de cambio
- Datos bancarios (cuenta origen/destino)

**Relación**: `payments 1 → N payment`

**Ejemplo**:
```
Complemento PP-00001:
  ├─ payment #1: $30,000 (Transferencia, cuenta XXXX1234)
  └─ payment #2: $20,000 (Transferencia, cuenta XXXX5678)

Total pagado: $50,000
```

---

#### 11. **related_documents**
**Propósito**: Vincula pagos con facturas que están siendo pagadas

**Función de Negocio**:
- SAT requiere especificar qué facturas se pagan con cada pago
- Permite amortizar facturas (pagar parcialmente)
- Lleva control de saldos

**Datos Clave**:
- `payment_uuid`: FK al pago que amortiza
- `document_uuid`: FK a invoice que se está pagando
- `amount_paid`: Cuánto se abonó de esta factura
- `previous_balance`: Saldo anterior de la factura
- `remaining_balance`: Saldo después del pago
- `installment_number`: Número de parcialidad

**Ejemplo**:
```
Factura FAC-001: $100,000
Pago 1 (PP-001): $30,000
  related_documents:
    - document_uuid: FAC-001
    - amount_paid: 30,000
    - previous_balance: 100,000
    - remaining_balance: 70,000
    - installment_number: 1

Pago 2 (PP-002): $70,000
  related_documents:
    - document_uuid: FAC-001
    - amount_paid: 70,000
    - previous_balance: 70,000
    - remaining_balance: 0.00
    - installment_number: 2
```

**NOTA IMPORTANTE**: Esta tabla tiene un CHECK constraint que valida:
```sql
remaining_balance = previous_balance - amount_paid
```
Problema conocido: errores de precisión decimal requieren ROUND().

---

#### 12. **equivalence_dr**
**Propósito**: Equivalencia en moneda nacional cuando pago es en moneda extranjera

**Función de Negocio**:
- Si factura es en USD pero pago es en MXN (o viceversa)
- SAT requiere calcular equivalencia en pesos mexicanos
- Usa tipo de cambio oficial de Banxico

**Datos Clave**:
- Related document asociado
- Monto equivalente en MXN
- Tipo de cambio aplicado

**Ejemplo**:
```
Factura: $1,000 USD
Pago: $20,000 MXN
Tipo de cambio: 20.00 MXN/USD

equivalence_dr:
  - equivalence_amount: 1,000 USD
  - exchange_rate: 20.00
  - equivalent_mxn: 20,000 MXN
```

---

#### 13. **totals**
**Propósito**: Totales de impuestos del complemento de pago

**Función de Negocio**:
- Resumen de impuestos trasladados y retenidos
- Requerido por SAT en complemento de pago v2.0

**Datos Clave**:
- Total de pagos
- Bases y montos por tasa de IVA (16%, 8%, 0%)
- Retenciones de IVA e ISR

**Ejemplo**:
```
Complemento PP-001 ($50,000 totales):
  - total_payments_amount: 50,000.00
  - total_base_iva_16: 43,103.45  (base gravable)
  - total_tax_iva_16: 6,896.55    (IVA 16%)
  - total_withholding_isr: 0.00
```

---

### 🔹 GRUPO 5: AUDITORÍA Y REGISTROS

#### 14. **log**
**Propósito**: Bitácora de todas las operaciones del sistema

**Función de Negocio**:
- Auditoría completa de acciones
- Trazabilidad de timbrados, cancelaciones, errores
- Requerido para debugging y compliance

**Datos Clave**:
- Tipo de operación (CREATE, STAMP, CANCEL, ERROR)
- Usuario que ejecutó
- CFDI afectado
- Mensaje descriptivo
- Timestamp

**Ejemplo**:
```
log_id: 12345
operation_type: STAMP
cfdi_uuid: FAC-001
user_id: 100
message: "CFDI timbrado exitosamente por PAC Finkok"
pac_id: 1
timestamp: 2025-11-10 10:30:00
```

---

#### 15. **payment_response_catalog**
**Propósito**: Catálogo de respuestas posibles del PAC

**Función de Negocio**:
- Almacena códigos de error/éxito del PAC
- Ayuda a interpretar respuestas de timbrado

**Datos Clave**:
- Código de respuesta
- Descripción
- Tipo (éxito/error/advertencia)

---

#### 16. **payment_file_registry**
**Propósito**: Registro de archivos de complementos de pago

**Función de Negocio**:
- Control de archivos XML y PDF generados
- Tracking de entregas a clientes

**Datos Clave**:
- Payment asociado
- Ruta del archivo
- Tipo (XML/PDF)
- Fecha de generación

---

# 🟢 FINANZAS-API (tenant_finance)

## Propósito del Microservicio
**finanzas-api** gestiona las **operaciones financieras y logísticas** de la empresa: órdenes de compra, recepciones de mercancía, cuentas por pagar, rebates, y guías de embarque. Se integra con SAP y consume servicios de fiscal-api para timbrado cuando es necesario.

---

## 📊 TABLAS DE FINANZAS-API

### 🔹 GRUPO 1: CATÁLOGOS (Configuración)

#### 1. **origin_catalog** ⚠️ (Entity sin tabla)
**Propósito**: Catálogo de orígenes/sucursales

**Función de Negocio**:
- Almacena sucursales, bodegas, centros de distribución
- Usado en recepciones y shipping guides

**Datos esperados**:
- ID de origen
- Nombre (ej: "CEDIS Monterrey")
- Código SAP
- Tipo (bodega/tienda/CEDIS)

---

#### 2. **status_catalog** ⚠️ (Entity sin tabla)
**Propósito**: Catálogo de estados de documentos

**Función de Negocio**:
- Estados posibles de purchase_order, receipt, etc.
- Estandariza códigos de status

---

#### 3. **pac_catalog** ❌ (NO debería estar aquí)
**Propósito**: Proveedores de Certificación

**PROBLEMA**: Tabla fiscal duplicada de fiscal-api. Debe eliminarse de finanzas-api.

---

#### 4. **version_catalog** ❌ (NO debería estar aquí)
**Propósito**: Versiones CFDI

**PROBLEMA**: Tabla fiscal duplicada de fiscal-api. Debe eliminarse de finanzas-api.

---

### 🔹 GRUPO 2: GESTIÓN DE COMPRAS

#### 5. **purchase_order**
**Propósito**: Órdenes de compra a proveedores

**Función de Negocio**:
- Registro de OC emitidas por empresa a proveedores
- Base para recepción de mercancía
- Integración con SAP

**Datos Clave**:
- `order_number`: Número de OC (ej: "OC-2024-001234")
- `vendor_number`: Proveedor (código SAP)
- `source_id`: Sucursal que ordena
- `total_amount`: Monto total de la OC
- `order_date`: Fecha de emisión
- `delivery_date`: Fecha compromiso de entrega
- `status`:
  - 0 = Cancelada
  - 1 = Disponible (pendiente de recibir)
  - 2 = Parcialmente recibida
  - 3 = Completamente recibida
  - 4 = Cerrada

**Flujo de Vida**:
```
1. Creación: INSERT con status=1 (disponible)
2. Recepción parcial: status=2, se crean reception records
3. Recepción completa: status=3
4. Cierre contable: status=4
```

---

#### 6. **receipt** 🟠 (OBSOLETA - migrar a reception)
**Propósito**: Recepciones de mercancía (versión antigua)

**Función de Negocio**:
- Registro de mercancía recibida físicamente
- Vincula con purchase_order
- Base para cuentas por pagar

**ESTADO**: Marcada para eliminación, usar `reception` en su lugar.

---

#### 7. **receipt_sku** 🟠 (OBSOLETA - migrar a reception_sku)
**Propósito**: Detalle de recepciones por SKU (versión antigua)

**Función de Negocio**:
- Detalle de productos recibidos
- Cantidad, costo unitario, costo total

**ESTADO**: Marcada para eliminación, usar `reception_sku` en su lugar.

---

#### 8. **reception** ⚠️ (Entity sin tabla - CRÍTICO)
**Propósito**: Recepciones de mercancía (versión nueva)

**Función de Negocio**:
- Reemplazo de `receipt`
- Registro de mercancía recibida con estado de consumo

**Datos Clave**:
- `purchase_order_uuid`: OC asociada
- `origin_id`: Sucursal de origen
- `destination_id`: Sucursal destino
- `amount`: Monto recibido
- `reception_date`: Fecha de recepción física
- `status`:
  - 0 = Disponible (recibida pero no procesada)
  - 1 = Consumida por sistema (automático)
  - 2 = Consumida manual
  - 3 = Cancelada
  - 4 = Borrado lógico

**Máquina de Estados**:
```
0 (Disponible) → 1 (Consumida sistema) ✅
0 (Disponible) → 2 (Consumida manual) ✅
1 (Consumida sistema) → NO PUEDE CAMBIAR ❌
2 (Consumida manual) → 3 (Cancelada) ✅
3 (Cancelada) → 4 (Borrado lógico) ✅
4 (Borrado lógico) → NO PUEDE CAMBIAR ❌
```

**PROBLEMA CRÍTICO**: Entity existe, service implementado, pero **NO hay CREATE TABLE en migration**.

---

#### 9. **reception_sku** ⚠️ (Entity sin tabla - CRÍTICO)
**Propósito**: Detalle de recepciones por producto

**Función de Negocio**:
- Detalle de SKUs en cada recepción
- Cantidad, costo unitario, costo total

**Datos Clave**:
- `reception_id`: FK a reception
- `sku`: Código del producto
- `description`: Descripción
- `quantity`: Cantidad recibida
- `unit_cost`: Costo unitario
- `total_cost`: quantity × unit_cost

**PROBLEMA CRÍTICO**: Entity existe pero **NO hay CREATE TABLE en migration**.

---

### 🔹 GRUPO 3: CUENTAS POR PAGAR

#### 10. **accounts_payable**
**Propósito**: Cuentas por pagar integradas desde ERP

**Función de Negocio**:
- Registro de documentos contables por pagar a proveedores
- Integración desde SAP u otro ERP
- Base para pagos y conciliaciones

**Datos Clave**:
- `company`: Código de empresa
- `document_number`: Número del documento AP
- `reference_number`: Referencia (puede ser OC o factura)
- `vendor_number`: Proveedor
- `amount`: Monto a pagar
- `gl_account`: Cuenta contable (GL Account)
- `due_date`: Fecha de vencimiento
- `payment_term`: Términos de pago (ej: "NET30")
- `hold_indicator`: Retenido (Y) o liberado (N)
- `sent_flag`: Ya enviado a sistema de pagos (0/1)
- `document_type`: Tipo de documento contable

**Flujo de Uso**:
```
1. ETL desde SAP → INSERT en accounts_payable
2. Sistema valida datos
3. Si ok → sent_flag=1, se integra con sistema de pagos
4. Si hay retención → hold_indicator='Y'
```

---

#### 11. **finanzas_payments** ⚠️ (Entity sin tabla - CRÍTICO)
**Propósito**: Pagos financieros operativos (NO fiscales)

**Función de Negocio**:
- Registro de pagos realizados a proveedores
- Diferente de complementos CFDI (fiscal)
- Integración con SAP

**Datos Clave**:
- `company`: Código de empresa
- `document_number`: Número de documento de pago
- `document_reference`: Referencia (puede ser AP o factura)
- `vendor_number`: Proveedor pagado
- `amount`: Monto pagado
- `sap_document`: Documento SAP resultante
- `payment_date`: Fecha del pago
- `document_type`: Tipo de documento

**Diferencia con fiscal_payments**:
- `finanzas_payments`: Pago operativo/contable (SAP)
- `fiscal_payments`: Complemento CFDI (SAT/timbrado)

**PROBLEMA CRÍTICO**: Controller, Service, Routes existen pero **NO hay CREATE TABLE en migration**.

---

#### 12. **fiscal_payments** ❌ (Ubicación incorrecta)
**Propósito**: Pagos fiscales con campos de timbrado

**PROBLEMA**: Esta tabla contiene campos fiscales (paymentMethod, bankAccount, referencePayment) que sugieren complementos CFDI. **Debería estar en fiscal-api**, no en finanzas-api.

---

### 🔹 GRUPO 4: REBATES Y DESCUENTOS

#### 13. **stamped_rebate**
**Propósito**: Rebates timbrados fiscalmente

**Función de Negocio**:
- Descuentos que ya fueron timbrados como CFDI
- Referencia a comprobante fiscal emitido

**Datos Clave**:
- `document_number`: Número del rebate
- `reference_number`: Referencia
- `status`: Estado del rebate

**Relación con fiscal**: Se timbra en fiscal-api, se registra aquí.

---

#### 14. **rebate**
**Propósito**: Descuentos y rebates aplicados a proveedores

**Función de Negocio**:
- Descuentos comerciales pactados con proveedores
- Pueden ser: rappels, bonificaciones, descuentos por volumen

**Datos Clave**:
- `document_number`: FK a stamped_rebate
- `sap_document`: Documento SAP
- `vendor_number`: Proveedor
- `amount`: Monto del rebate
- `source`: Origen del descuento
- `period_id`: Período aplicable
- `due_date`: Fecha de vencimiento
- `posting_date`: Fecha de contabilización

**Flujo**:
```
1. Se negocia rebate con proveedor
2. Se registra en rebate table
3. Se crea stamped_rebate
4. fiscal-api timbra como CFDI tipo E (egreso)
5. Se actualiza referencia
```

---

#### 15. **stamping_rebate** ⚠️ (Entity sin tabla)
**Propósito**: Control del proceso de timbrado de rebates

**Función esperada**:
- Tabla de trabajo para rebates en proceso de timbrado
- Estado del timbrado (pendiente/proceso/completado)

---

### 🔹 GRUPO 5: LOGÍSTICA Y EMBARQUES

#### 16. **shipping_guide**
**Propósito**: Guías de embarque para transporte de mercancía

**Función de Negocio**:
- Registro de embarques de proveedor a sucursales
- Control de transporte y arribo
- Base para Carta Porte (complemento CFDI de transporte)

**Datos Clave**:
- `guide_number`: Número de guía
- `vendor_number`: Proveedor que envía
- `truck_plate`: Placas del camión
- `trailer_plate`: Placas del remolque
- `driver_name`: Nombre del chofer
- `driver_license`: Licencia del conductor
- `source_id`: Origen (bodega/CEDIS)
- `destination_id`: Destino
- `delivery_type`: Tipo de entrega
- `delivery_date`: Fecha programada
- `estimated_arrival`: Hora estimada de arribo
- `actual_arrival`: Hora real de arribo

**Flujo de Uso**:
```
1. Proveedor crea envío → INSERT shipping_guide
2. Sistema genera Carta Porte (fiscal-api)
3. Chofer llega → actualiza actual_arrival
4. Recepción valida mercancía → crea reception
```

---

#### 17. **shipping_guide_document** ⚠️ (Entity sin tabla)
**Propósito**: Documentos asociados a guías de embarque

**Función esperada**:
- Almacena PDFs, XMLs, fotos de evidencia
- Carta Porte CFDI
- Factura del proveedor

---

#### 18. **shipping_guide_purchase_order** ⚠️ (Entity sin tabla)
**Propósito**: Relación M:N entre guías y órdenes de compra

**Función esperada**:
- Una guía puede incluir productos de múltiples OCs
- Una OC puede enviarse en múltiples guías (entregas parciales)

---

### 🔹 GRUPO 6: SAP INTEGRATION

#### 19. **sap_document**
**Propósito**: Documentos integrados desde SAP

**Función de Negocio**:
- Registro de documentos contables/financieros desde SAP
- Sincronización bidireccional
- Trazabilidad de integración

**Datos Clave**:
- `document_number`: Número del documento
- `vendor_number`: Proveedor
- `doc_sap`: Número de documento SAP resultante
- `amount`: Monto
- `source`: Sistema origen
- `document_type`: Tipo (factura, nota crédito, pago, etc.)
- `sap_status`: Estado en SAP
- `message`: Mensaje de respuesta de SAP

**Flujo**:
```
1. Sistema genera documento → INSERT sap_document (status=pendiente)
2. Se envía a SAP vía API/RFC
3. SAP responde con doc_sap y status
4. Se actualiza sap_status y doc_sap
```

---

### 🔹 GRUPO 7: BLOQUEOS DE PROVEEDORES

#### 20. **supplier_block** ⚠️ (Entity sin tabla)
**Propósito**: Bloqueo de proveedores por incumplimientos

**Función esperada**:
- Control de proveedores bloqueados para nuevas OCs
- Motivos: incumplimiento, calidad, pagos pendientes

---

#### 21. **vendor_block**
**Propósito**: Bloqueo de proveedores (similar a supplier_block)

**Función de Negocio**:
- Control de proveedores bloqueados
- Previene creación de nuevas OCs

**Datos Clave**:
- Vendor number
- Motivo de bloqueo
- Fecha inicio/fin del bloqueo
- Usuario que bloqueó

**NOTA**: Parece duplicar funcionalidad con supplier_block. Requiere análisis de cuál usar.

---

### 🔹 GRUPO 8: TABLAS FISCALES DUPLICADAS ❌

Las siguientes tablas **NO deberían estar** en finanzas-api:

- **invoice** (duplicada de fiscal-api)
- **issuer** (duplicada de fiscal-api)
- **receiver** (duplicada de fiscal-api)
- **payments** (duplicada de fiscal-api)
- **payment** (duplicada de fiscal-api)
- **related_documents** (duplicada de fiscal-api)
- **totals** (duplicada de fiscal-api)
- **addendum** (duplicada de fiscal-api)
- **related_cfdi** (duplicada de fiscal-api)
- **equivalence_dr** (duplicada de fiscal-api)
- **log** (duplicada de fiscal-api)

**Solución**: Eliminar de migrations de finanzas-api. Acceder a datos fiscales vía `fiscalApi.client.ts`.

---

# 🔄 FLUJOS DE NEGOCIO INTEGRADOS

## Flujo 1: Compra y Recepción de Mercancía

```
┌─────────────────────────────────────────────────────┐
│              FINANZAS-API                           │
├─────────────────────────────────────────────────────┤
│                                                     │
│  1. purchase_order (OC-123, vendor=1001)           │
│     status=1 (disponible)                          │
│                                                     │
│  2. shipping_guide (guía de transporte)            │
│     ├─ truck_plate: ABC-123                        │
│     └─ estimated_arrival: 2025-11-10 14:00        │
│                                                     │
│  3. reception (mercancía recibida)                 │
│     ├─ purchase_order_uuid → OC-123               │
│     ├─ amount: $50,000                             │
│     └─ status: 0 (disponible)                      │
│                                                     │
│  4. reception_sku (detalle)                        │
│     ├─ sku: PROD-001                               │
│     ├─ quantity: 100                               │
│     └─ unit_cost: $500                             │
│                                                     │
│  5. accounts_payable (AP generado)                 │
│     ├─ reference_number: OC-123                    │
│     └─ amount: $50,000                             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## Flujo 2: Facturación y Pago con Complemento CFDI

```
┌─────────────────────────────────────────────────────┐
│              FISCAL-API                             │
├─────────────────────────────────────────────────────┤
│                                                     │
│  1. invoice (FAC-001)                              │
│     ├─ document_type: I (ingreso)                  │
│     ├─ payment_method: PPD (pago diferido)         │
│     ├─ total: $100,000                             │
│     ├─ status: 1 (vigente)                         │
│     └─ fiscal_uuid: 12345678-SAT-UUID              │
│                                                     │
│  [Cliente paga $50,000 el día 15]                  │
│                                                     │
│  2. payments (PP-001 - complemento de pago)        │
│     ├─ payment_date: 2025-11-15                    │
│     └─ fiscal_uuid: 87654321-SAT-UUID              │
│                                                     │
│  3. payment (pago individual)                      │
│     ├─ payments_uuid → PP-001                      │
│     ├─ amount: $50,000                             │
│     └─ payment_method: 03 (transferencia)          │
│                                                     │
│  4. related_documents (amortización)               │
│     ├─ payment_uuid → payment #1                   │
│     ├─ document_uuid → FAC-001                     │
│     ├─ amount_paid: $50,000                        │
│     ├─ previous_balance: $100,000                  │
│     ├─ remaining_balance: $50,000                  │
│     └─ installment_number: 1                       │
│                                                     │
│  5. totals (resumen de impuestos del complemento)  │
│     ├─ total_payments_amount: $50,000              │
│     └─ total_tax_iva_16: $6,896.55                 │
│                                                     │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              FINANZAS-API                           │
├─────────────────────────────────────────────────────┤
│                                                     │
│  6. finanzas_payments (registro operativo)         │
│     ├─ document_reference: FAC-001                 │
│     ├─ amount: $50,000                             │
│     └─ sap_document: 4900123456                    │
│                                                     │
│  7. sap_document (integración SAP)                 │
│     ├─ document_number: PAY-001                    │
│     ├─ doc_sap: 4900123456                         │
│     └─ sap_status: 1 (procesado)                   │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

# 📋 RESUMEN DE PROBLEMAS DETECTADOS

## Críticos 🔴
1. **finanzas_payments**: Entity completa sin CREATE TABLE
2. **reception** + **reception_sku**: Entities sin CREATE TABLE (migración bloqueada)

## Arquitectura 🟠
3. **12 tablas CFDI duplicadas** en finanzas-api
4. **fiscal_payments** en ubicación incorrecta

## Menores 🟡
5. **6 entities sin tabla**: origin_catalog, status_catalog, supplier_block, stamping_rebate, shipping_guide_document, shipping_guide_purchase_order
6. **Entities obsoletas**: receipt, receipt_sku (aún exportadas)
7. **vendor_block vs supplier_block**: Posible duplicación

---

# 🎯 CONCLUSIÓN

## finanzas-api debe gestionar:
✅ Purchase Orders
✅ Recepciones (reception, NO receipt)
✅ Cuentas por pagar
✅ Pagos operativos (finanzas_payments)
✅ Rebates y descuentos
✅ Logística y embarques
✅ Integración SAP

## fiscal-api debe gestionar:
✅ Emisión de CFDIs (facturas)
✅ Complementos de pago CFDI
✅ Timbrado ante SAT
✅ Cancelaciones fiscales
✅ Compliance fiscal

## Integración:
- finanzas-api → fiscal-api (vía fiscalApi.client.ts)
- fiscal-api NO conoce finanzas-api
- Separación limpia de responsabilidades

---

**¿Quieres que profundice en algún flujo específico o tabla en particular?**
