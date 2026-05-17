# Proceso: Facturación CFDI

> Cómo Sodimac recibe y almacena los CFDIs (factura, NC, complemento de pago) emitidos por proveedores.

## Qué pasa (vista de negocio)

1. El proveedor entrega mercancía o servicios a Sodimac (precede una OC y una recepción).
2. El proveedor **emite y timbra** un CFDI ante el SAT vía su PAC.
3. El proveedor **envía el CFDI a Sodimac** (XML + PDF, típicamente por portal o email).
4. Sodimac **recibe el XML**, lo valida, extrae datos y lo almacena.
5. Sodimac valida la **addenda** Sodimac dentro del CFDI (datos comerciales: OC, recepción, guía).
6. El CFDI queda disponible para conciliación (3-way-match) y posterior pago.

## Tipos de CFDI relevantes

| `document_type` | Significado | Caso de uso |
|---|---|---|
| `I` | Ingreso | **Factura** del proveedor a Sodimac (cobra mercancía/servicio) |
| `E` | Egreso | **Nota de Crédito** (descuento, devolución) |
| `P` | Pago | **Complemento de Pago (REP)** que documenta el pago ya realizado |

## Tablas involucradas

### Núcleo

| Tabla | Schema | Rol |
|---|---|---|
| `invoice` | `tenant_fiscal` | CFDI completo (XML + datos extraídos). `document_type` indica I/E/P. |
| `issuer` | `tenant_fiscal` | Datos del emisor (RFC, nombre, régimen). |
| `receiver` | `tenant_fiscal` | Datos del receptor. |
| `authorized_receiver_catalog` | `tenant_fiscal` | Receptores Sodimac válidos. |
| `invoice_status_history` | `tenant_fiscal` | Trazabilidad de cambios de status. |

### Impuestos (cuando aplica IVA/ISR)

| Tabla | Schema | Rol |
|---|---|---|
| `tax` | `tenant_fiscal` | Encabezado de impuestos por factura. |
| `tax_detail` | `tenant_fiscal` | Detalle por producto/servicio. |
| `tax_transfer` | `tenant_fiscal` | IVA/IEPS trasladados. |
| `tax_withholding` | `tenant_fiscal` | Retenciones (IVA, ISR). |

### Relaciones entre CFDIs

| Tabla | Rol |
|---|---|
| `related_cfdi` | NC referencia/sustituye factura, REP relaciona facturas, etc. |

### Addenda

| Tabla | Schema | Rol |
|---|---|---|
| `addendum` | `tenant_fiscal` | Addenda dentro del XML del CFDI. |
| `addendum_manual` | `tenant_finance` | Addenda capturada manualmente cuando el CFDI no la trae. |

### Catálogos

| Tabla | Rol |
|---|---|
| `pac_catalog` | PACs autorizados que timbran. |
| `version_catalog` | Versiones de XML aceptadas. |

## Endpoints (fiscal-api :8082)

| Operación | Endpoint |
|---|---|
| Búsqueda CFDI por filtros | `GET /invoices` o `GET /invoices/search` |
| Detalle | `GET /invoices/{uuid}` |
| Carga CFDI | `POST /invoices` |
| Update status | `PUT /invoices/{uuid}/status` |
| Listar emisores | `GET /issuers` |
| Listar receptores | `GET /receivers` |
| Receptores autorizados Sodimac | `GET /authorized-receivers` |
| Addenda | `GET/POST /addendum` |
| PAC catalog | `GET /pac-catalog` |

## Diagrama de flujo

```
[Proveedor]                                    [Sodimac]
   │                                              │
   │ Emite CFDI ──► PAC ──► timbra ──► SAT        │
   │                                              │
   │ Envía XML + PDF                              │
   │ ───────────────────────────────────────────► │
   │                                              │
   │                              [fiscal-api]    │
   │                              ├─ Valida XML  │
   │                              ├─ Extrae datos│
   │                              │              │
   │                              ├──► invoice    │
   │                              ├──► issuer     │
   │                              ├──► receiver   │
   │                              ├──► tax + detail + transfer + withholding
   │                              ├──► addendum (si trae)
   │                              └──► invoice_status_history (created)
   │                                              │
   │                              [Conciliación]  │
   │                              3-Way-Match con │
   │                              OC + Recepción  │
   │                                              │
   │                              [Listo para pagar]
```

## Reglas de negocio

- El CFDI llega ya timbrado por el proveedor — Sodimac **no timbra entrante**, solo recibe.
- Sodimac sí **valida**: estructura XML contra versión vigente (`version_catalog`), RFC receptor en `authorized_receiver_catalog`, integridad del UUID fiscal contra el SAT.
- El XML crudo se almacena en `invoice.xml_content` por si hace falta re-procesar.
- Cualquier cambio de status (validado, rechazado, anulado, pagado) **debe registrarse en `invoice_status_history`**.
- Una NC (`document_type='E'`) **debe referenciar** la factura original via `related_cfdi`.
- Un REP (`document_type='P'`) referencia las facturas que paga via `related_documents` (en tablas separadas: `payments`, `payment`, `related_documents` — ver [proceso REP](07-complemento-pago-rep.md) pendiente).

## Riesgos / consideraciones

- **Sin FK declaradas**: la integridad relacional la pone la app. Un seed mal hecho puede dejar `invoice.issuer_uuid` apuntando a nada.
- **Catálogos PAC/version duplicados** en `tenant_fiscal` y `tenant_finance` — usar el de fiscal por convención.
- **`addendum`** vive en fiscal cuando viene en el CFDI; en finanzas (`addendum_manual`) cuando se captura manualmente.

## JIRAs relacionados

- [STM-314](../../jiras/STM-314/) — filtro seguridad NC.
- [STM-322](../../jiras/STM-322/) — filtro seguridad complementos de pago.
- [STM-323](../../jiras/STM-323/) — filtro seguridad facturas.
- [STM-1474](../../jiras/STM-1474/) — filtro seguridad complementos.
- [STM-393](../../jiras/STM-393/) — múltiples gaps de validación CFDI.

## Cómo responder al equipo

> *"¿Dónde se almacena una factura del proveedor?"*
> → `tenant_fiscal.invoice` con `document_type='I'`. Datos extraídos en tablas relacionales + XML crudo en `xml_content`.

> *"¿Y la NC?"*
> → Misma tabla, `document_type='E'`. Si referencia una factura previa, está vinculada en `related_cfdi`.

> *"¿Qué impuestos trae el CFDI?"*
> → `tax` (encabezado) + `tax_detail`/`tax_transfer`/`tax_withholding` (desglose).
