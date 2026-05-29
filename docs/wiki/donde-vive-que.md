# Dónde vive qué

> Q&A rápido para "¿dónde guardo X dato?" o "¿qué endpoint uso para Y?". Sin código, solo orientación de negocio.

## Pagos

### "Necesito registrar que se le pagó al proveedor"
→ **Pago operativo**.
- Tabla legacy: `tenant_finance.fiscal_payments` (1 row por pago)
- Tabla nueva (refactor): `tenant_finance.payment_header` + `payment_detail` (1:N)
- Endpoint legacy: `POST /fiscal-payments`
- Endpoint nuevo: `POST /finanzas-payment` (verificar disponibilidad en UAT)
- ⚠ La tabla se llama `fiscal_payments` pero **NO es CFDI** — vive en finanzas.

### "Necesito guardar el complemento de pago que el proveedor emitió (CFDI REP)"
→ **CFDI REP**, fiscal.
- Tablas: `tenant_fiscal.payments` (cabecera + XML) + `payment` (pagos individuales) + `totals` (sumatorias) + `related_documents` (facturas que paga) + `equivalence_dr` (cambio divisa si aplica)
- API: fiscal-api, endpoints bajo `/payments`

### "¿Cómo sé qué facturas paga un REP?"
→ `tenant_fiscal.related_documents.payment_uuid` → `payment`, y `related_documents.document_uuid` → `invoice`.

## Descuentos comerciales (Rebates)

### "Necesito registrar un descuento operativo (sin factura aún)"
→ `tenant_finance.rebate`. Endpoint: `POST /rebates`.

### "Necesito relacionar un descuento con su Nota de Crédito timbrada"
→ Endpoint: `POST /rebates/relate` (STM-973). Crea tanto `stamped_rebate` como `rebate` y guarda `invoice_fiscal_uuid`.

### "¿Dónde veo qué descuentos tiene timbrados un proveedor?"
→ `tenant_finance.stamped_rebate` filtrado por vendor (via FK lógica con `rebate.vendor_number`).
- O: endpoint `POST /rebates/filter` (STM-875).

## Facturas y Notas de Crédito (CFDI)

### "Necesito buscar facturas por proveedor"
→ `tenant_fiscal.invoice` con `document_type='I'`, filtrar por `issuer_uuid` → `issuer.rfc`.
- Endpoint: `GET /invoices` con filtros.

### "Necesito buscar Notas de Crédito"
→ `tenant_fiscal.invoice` con `document_type='E'`.

### "¿Cuál es el UUID fiscal SAT de una factura?"
→ `tenant_fiscal.invoice.fiscal_uuid` (UUID del TimbreFiscalDigital, no el `invoice_uuid` interno).

## Órdenes de compra / Recepción / Logística

### "Necesito guardar una OC"
→ `tenant_finance.purchase_order`. Endpoint: `POST /purchase-orders`.

### "Necesito registrar la recepción de mercancía"
→ `tenant_finance.reception` (cabecera) + `reception_sku` (SKUs). Endpoint relacionado: `/api/migo`.

### "Necesito guardar la guía de embarque (Carta Porte) del proveedor"
→ `tenant_finance.shipping_guide` + `shipping_guide_document` (documentos) + `shipping_guide_purchase_order` (OCs cubiertas). Endpoint: `/api/shipping-guide`.

## Three Way Match

### "¿Cuál es el resultado de la conciliación de OC vs Recepción vs Factura?"
→ `tenant_finance.three_way_match`. Endpoint: `GET /three-way-match`.

### "¿Quién ejecutó el TWM y cuándo?"
→ `tenant_finance.twm_ejecucion` (corridas) + `twm_logs` (logs) + `twm_cifras_control` (totales por paso).

## Cuentas por pagar / SAP

### "¿Qué cuentas por pagar tiene un proveedor?"
→ `tenant_finance.accounts_payable` filtrado por `vendor_number`. Endpoint: `/api/accounts-payable`.

### "¿Qué se le envió a SAP?"
→ `tenant_finance.sap_document`. Endpoint: `/api/sap-documents`.

## Estado de cuenta

### "Necesito el estado de cuenta mensual de un proveedor"
→ `tenant_finance.account_statement` (cabecera por vendor + año + mes + versión) + 6 sub-tablas de detalle.
- Endpoint: `/api/account-statement`.

### "¿Por qué hay versiones?"
→ Cada vez que se regenera el estado de cuenta, se incrementa `version` y `previous_statement_uuid` apunta al anterior. Historial inmutable.

## Bloqueos y Addenda

### "¿Está bloqueado este proveedor para pago?"
→ `tenant_finance.vendor_block` con `status` activo. Endpoint: `/api/vendor-blocks`.

### "Necesito capturar una addenda que el proveedor no incluyó"
→ `tenant_finance.addendum_manual`. Se llena automático desde finanzas-api al asociar OC + recepción a la factura (`purchaseOrder.service.ts:119-131`). Ver [proceso addenda](procesos/09-addenda-sodimac.md).

### "La addenda viene dentro del CFDI"
→ `tenant_fiscal.addendum`. Se persiste al cargar la factura. Tipos soportados: `Addenda_Sodimac` (local) y `Addenda_Sodimac_CartaPorte` (foráneo). Ver [proceso addenda](procesos/09-addenda-sodimac.md).

### "Mi factura quedó en estatus Pendiente Addenda — ¿por qué?"
→ El XML llegó sin nodo `<cfdi:Addenda>`. Respuesta `RES005`, estatus 1. Hay que completar la addenda antes de mover estatus (PUT da BUS048). Ver [proceso addenda](procesos/09-addenda-sodimac.md).

## Catálogos / Maestros

### "¿Qué PAC firma este CFDI?"
→ `tenant_fiscal.pac_catalog`. (Ojo: `tenant_finance.pac_catalog` existe duplicado.)

### "¿Qué versión de XML está vigente?"
→ `tenant_fiscal.version_catalog` con `pac_id` y `document_type`.

### "¿Cuáles son los RFCs de Sodimac autorizados a recibir CFDI?"
→ `tenant_fiscal.authorized_receiver_catalog`.

## Bitácora / Logs

### "Necesito auditar quién hizo qué"
→ `tenant_finance.activity_logs` (bitácora del módulo, vive aquí desde la deprecación de auditoria-api). Endpoint: `/api/audit-logs`.

### "¿Cómo registrar un cambio de estado de factura?"
→ `tenant_fiscal.invoice_status_history` (se llena automático en cada transición).

## Datos que NO viven en estos schemas

| Quieres | Schema |
|---|---|
| Usuarios / autenticación | `core_security` (no documentado aquí) |
| Catálogos compartidos SAT (regímenes, formas de pago, etc.) | `shared_catalogs` |
| Utilerías (atributos de usuario, parámetros) | `core_utils` |
| Bitácora cross-módulo legacy | `core_audit` (deprecada, migrada a `tenant_finance.activity_logs`) |

## Rutas BFF en UAT (ppsomx)

Dos prefijos diferentes en `uat.fbusinesscenter.com` — NO son equivalentes:

| Prefijo | Apunta a | Acepta multipart POST |
|---|---|---|
| `/ppsomx/fiscal/` | **bff.fiscal** (proxy) → fiscal-api:8082 | Sí (`parseReqBody:false`) |
| `/ppsomx/backend-fiscal/` | fiscal-api directo (sin BFF) | No: nginx devuelve `405 Not Allowed` para POST con body grande |

**Ejemplo correcto** para subir XML:
```
POST https://uat.fbusinesscenter.com/ppsomx/fiscal/fiscal/xml/process/file
```
(doble `fiscal/` porque el BFF transparente conserva el path completo del backend)

**No funciona**:
```
POST https://uat.fbusinesscenter.com/ppsomx/backend-fiscal/fiscal/xml/process/file  ← 405
```

Si el JWT (~8KB) causa `Request Header Or Cookie Too Large` en alguna ruta, el BFF tiene `--max-http-header-size=1048576` en su start script, pero nginx puede necesitar `proxy-buffer-size: "32k"` en el ingress K8s (pendiente Bonelli).

## Reglas mnemotécnicas

1. **¿Es para SAT?** → `tenant_fiscal`.
2. **¿Es para flujo interno con proveedor?** → `tenant_finance`.
3. **¿Dice "fiscal" pero es interno?** → Probablemente naming heredado. Mirar dos veces. (`fiscal_payments` es el caso clásico.)
4. **¿Es pago?** Pregunta: ¿operativo o CFDI? Si operativo → finanzas. Si CFDI REP → fiscal.
5. **¿Es factura?** Siempre `tenant_fiscal.invoice` con `document_type` correspondiente.
