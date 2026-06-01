# Proceso: Addenda Sodimac

> Cómo Sodimac maneja la información comercial (OC, recepción, guía) asociada a un CFDI: cuándo viene en el XML, cuándo se captura manualmente, y por qué algunos flujos quedan "Pendiente de Addenda".

## Qué es una addenda (vista de negocio)

La **addenda es información comercial NO fiscal** que el receptor (Sodimac) necesita para amarrar una factura con su cadena operativa: Orden de Compra, Recepción de mercancía, guía de embarque, identificador interno del proveedor.

El SAT **no exige addenda** — es un espacio libre dentro del nodo `<cfdi:Addenda>` donde cada receptor define su propio schema. Sodimac, Walmart, Liverpool, Bimbo, etc. cada uno tiene el suyo.

## Por qué unos XML traen addenda y otros no

| Caso | Por qué |
|---|---|
| **XML CON addenda Sodimac** | Proveedor conoce el formato Sodimac, tiene los datos comerciales en su ERP, y los incrusta antes de timbrar. |
| **XML SIN addenda** | Proveedor timbra factura genérica — no conoce formato Sodimac, no integra los datos, o factura inicialmente sin asociación operativa. La addenda se captura **después** del lado Sodimac. |

Ambos casos son válidos. Sodimac acepta los dos.

## Tipos de addenda Sodimac soportados

Definidos en [AddendaValidationServiceImpl.java:83-87](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/AddendaValidationServiceImpl.java#L83-L87):

| Tipo | Cuándo aplica |
|---|---|
| `Addenda_Sodimac` | Mercancía, servicios, transporte local |
| `Addenda_Sodimac_CartaPorte` | Transporte foráneo (con complemento Carta Porte) |

Tipos legacy también parseados (ver [XmlToJsonConverter.java:432-438](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/util/XmlToJsonConverter.java#L432-L438)):

| Tipo legacy | Origen |
|---|---|
| `AddendaK` | Formato anterior, aún soportado para retro-compat |
| `Addenda_Sodimac_Detecno` | Variante con datos Detecno |

## Campos obligatorios

Ambos `Addenda_Sodimac` y `Addenda_Sodimac_CartaPorte` exigen los mismos 5 atributos:

| Atributo | Validación |
|---|---|
| `RFC` | Formato RFC válido: `^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$` |
| `UUID` | Formato UUID + **debe coincidir** con UUID del TimbreFiscalDigital |
| `Folio` | No vacío |
| `NoOC` | Número de Orden de Compra, no vacío |
| `Proveedor` | Identificador del proveedor, no vacío |

Validación completa en [AddendaValidationServiceImpl.java:128-191](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/AddendaValidationServiceImpl.java#L128-L191).

## Flujo al registrar factura

### Camino A — XML con addenda válida

```
POST /ppsomx/fiscal/invoices/register (XML con <Addenda_Sodimac>)
  → AddendaValidationService.validateAddenda() → true
  → saveInvoice() + saveAddenda() en tenant_fiscal.addendum
  → Response: RES004 "Factura registrada exitosamente"
  → invoice.status = (estatus normal, NO 1)
  → hasAddenda: true, pendingAddenda: false
```

### Camino B — XML sin addenda

```
POST /ppsomx/fiscal/invoices/register (XML sin nodo <cfdi:Addenda>)
  → AddendaValidationService.validateAddenda() → false (NO es error)
  → saveInvoice() SIN addenda
  → Response: RES005 "Factura registrada exitosamente - Pendiente de Addenda"
  → invoice.status = 1 (PENDIENTE_ADDENDA)
  → hasAddenda: false, pendingAddenda: true
```

Decisión sin-addenda en [AddendaValidationServiceImpl.java:60-66](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/AddendaValidationServiceImpl.java#L60-L66):

```java
if (addendaNodes.getLength() == 0) {
    log.info("No se encontró nodo Addenda...Será marcado como pendiente de addenda");
    return false; // No tiene addenda, pero no es error
}
```

### Camino C — XML con `<cfdi:Addenda>` pero NO Sodimac

```
POST /register (XML con addenda de otro receptor, ej. AddendaWalmart)
  → findAddendaSodimacNode() → null
  → throw BUS001 "La addenda no contiene el nodo requerido"
  → factura NO se registra
```

## Códigos de respuesta

Definidos en [FiscalSuccessCode.java:22-25](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/enums/FiscalSuccessCode.java#L22-L25):

| Código | Significado |
|---|---|
| `RES004` | Factura registrada exitosamente (con addenda) |
| `RES005` | Factura registrada exitosamente - Pendiente de Addenda |
| `RES006` | NC registrada exitosamente (con addenda) |
| `RES007` | NC registrada exitosamente - Pendiente de Addenda |

## Vinculación posterior (addenda manual)

Cuando la factura quedó en estatus 1 (Pendiente Addenda), la addenda se completa **desde finanzas-api** al asociar la factura con su OC + Recepción.

**Tabla destino:** `tenant_finance.addendum_manual` (vive en finanzas, no en fiscal).

**Trigger:** [purchaseOrder.service.ts:119-131](../../../APP03022-mrch.backend.somx.finanzas-api/src/services/purchaseOrder.service.ts#L119-L131) — cuando se procesa una recepción con `dto.uuid != null && dto.status == 2`, se crea el `AddendumManual`:

```typescript
if(dto.uuid != null && dto.uuid != undefined && dto.status == 2){ // ES ADDENDA MANUAL
    let addendaManual = new AddendumManual();
    addendaManual.supplierNumber = dto.supplierNumber;
    addendaManual.orderNumber = dto.orderNumber;
    addendaManual.invoiceId = dto.uuid;
    addendaManual.receptionId = recep.receptionId;
    addendaManual.supplierTypeId = supplier.supplierType.id;
}
```

> ⚠ **Pendiente confirmar con Ivan:** si addendum_manual de finanzas alimenta también `tenant_fiscal.addendum` (que es de donde fiscal-api lee al validar transiciones de estatus), o si quedan en tablas separadas. Si solo vive en finanzas, los PUT de estatus seguirán fallando con `BUS048`.

## Regla BUS048 — por qué falla el PUT de estatus sin addenda

[InvoiceServiceImpl.validateSupplierOwnership():934-967](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java#L934-L967):

```
PUT /ppsomx/fiscal/invoices (estatus=X)
  → validateSupplierOwnership(invoice, numeroProveedor)
  → findByInvoiceUuid → addendum == null?
  → throw BUS048 "La addenda del documento no se encuentra registrada"
```

Comentario textual del método (regla **confirmada por Ivan**):
> "Si el documento NO tiene addenda, es un ERROR (no debería ocurrir)"

La addenda es el ancla para validar propiedad del proveedor (`Invoice → Addendum → supplierNumber`). Sin addenda no hay forma de validar que el `numeroProveedor` del request sea dueño de la factura.

## Tablas

| Tabla | Schema | Cuándo se llena |
|---|---|---|
| `addendum` | `tenant_fiscal` | XML llegó con `<Addenda_Sodimac>` válida — se persiste en POST `/register` |
| `addendum_manual` | `tenant_finance` | Captura posterior al asociar OC + recepción a factura sin addenda |

Detalle de columnas:
- `addendum`: `addendum_uuid`, `invoice_uuid` (lógico), `supplier_number`, `reception_number`, `purchase_order_number`, `shipping_guide_number`, `addendum_content`, `addenda_type`, `supplier_type`
- `addendum_manual`: `addendum_manual_uuid`, `invoice_uuid` (lógico → tenant_fiscal.invoice), `supplier_number`, `reception_id`, `purchase_order_number`, `supplier_type_id`, `user_id`

## Estatus relacionados

| Estatus | Nombre | Transiciones permitidas |
|---|---|---|
| 1 | PENDIENTE_ADDENDA | [2, 3, 13] |
| 2 | RECIBIDO_PARCIAL | [3, 13] |

Definidos en [InvoiceStatus.java:22-28](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/enums/InvoiceStatus.java#L22-L28) y [CreditNoteStatus.java:22-28](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/enums/CreditNoteStatus.java#L22-L28).

La transición 1→2 está permitida en `status_train`, pero requiere addenda registrada (regla BUS048).

## Errores de addenda

| Código | Mensaje | Cuándo |
|---|---|---|
| `BUS001` | Addenda no contiene nodo Sodimac requerido | XML con `<cfdi:Addenda>` pero sin `Addenda_Sodimac*` |
| `BUS013` | Campo RFC faltante | RFC vacío en addenda |
| `BUS014` | Campo UUID faltante | UUID vacío en addenda |
| `BUS015` | Campo Folio faltante | Folio vacío |
| `BUS016` | Campo NoOC faltante | OC vacía |
| `BUS017` | Campo Proveedor faltante | Proveedor vacío |
| `BUS018` | RFC con formato inválido | No cumple regex SAT |
| `BUS019` | UUID addenda no coincide con TFD | UUID interno ≠ UUID TimbreFiscalDigital |
| `BUS048` | Addenda no registrada | PUT estatus sin addenda previa |

## Cómo responder al equipo

> *"Mi XML no tiene addenda, ¿es error?"*
> → No. Se guarda con código `RES005` y queda en estatus 1 "Pendiente Addenda". La addenda se vincula después desde finanzas al asociar OC + recepción.

> *"Por qué BUS048 al cambiar estatus de una factura recién subida?"*
> → Porque el XML llegó sin addenda. El PUT exige addenda registrada para validar propiedad del proveedor. Primero hay que completar la addenda (manual o re-cargar XML con addenda Sodimac).

> *"Qué tipo de addenda Sodimac me toca usar?"*
> → `Addenda_Sodimac` para mercancía/servicios/transporte local. `Addenda_Sodimac_CartaPorte` para transporte foráneo con complemento Carta Porte.

> *"Qué campos lleva la addenda Sodimac?"*
> → RFC, UUID (debe coincidir con TFD), Folio, NoOC, Proveedor. Todos obligatorios.

## JIRAs relacionados

- [STM-337](../../jiras/STM-337/) — registro factura/NC (POST `/register`)
- [STM-339](../../jiras/STM-339/) — update factura/NC con addenda opcional en body (PUT)
- [STM-704](../../jiras/STM-704/) — bitácora del registro
- [STM-719](../../jiras/STM-719/) — batch descarga, valida addenda en factura/NC, rechaza a "Pendiente Addenda" si falta

## Referencias técnicas

- [AddendaValidationService.java](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/AddendaValidationService.java) — interface
- [AddendaValidationServiceImpl.java](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/AddendaValidationServiceImpl.java) — validación XML
- [XmlToJsonConverter.java:420-545](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/util/XmlToJsonConverter.java#L420-L545) — extracción nodos addenda
- [InvoiceServiceImpl.java:934-967](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java#L934-L967) — `validateSupplierOwnership` (BUS048)
- [AddendumEntity.java](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/entity/AddendumEntity.java) — entity tenant_fiscal.addendum
- [AddendumManual.entity.ts](../../../APP03022-mrch.backend.somx.finanzas-api/src/entities/AddendumManual.entity.ts) — entity tenant_finance.addendum_manual
