# Comentarios Ivan 2026-06-23 — addenda manual, CatRfcReceptor, condición de pago NC

> Nuevos puntos de Ivan sobre `/register` (carga XML), addenda manual y catálogos. Interpretación,
> hallazgos en BD/código y dudas. **Aún sin codear** — pendiente confirmar dudas.
> Relacionado: [CHECKLIST-RECEPCION-NC-2026-06.md](CHECKLIST-RECEPCION-NC-2026-06.md),
> [QA-FER-2026-06-issues-search.md](QA-FER-2026-06-issues-search.md).

## Flujo actual de Josue (finanzas-api) — Consumida manual

`finanzas-api/src/services/purchaseOrder.service.ts` (`updateReceptionAndAddendaManual`), líneas ~183-205.

Cuando llega `dto.status == 2` (Consumida manual) con `dto.uuid` (el usuario lo captura **en pantalla**):
1. Crea `AddendumManual` (`entities/AddendumManual.entity.ts`, tabla `tenant_finance.addendum_manual`) con:
   `invoiceId = dto.uuid`, `receptionId`, `supplierNumber`, `orderNumber`, `supplierTypeId`.
2. Pone `reception.status = 2` (CatEstatusRecepcion = "Consumida manual").
3. Guarda por cascade (reception → addendum_manual).

**Clave:** **NO escribe en `tenant_fiscal.invoice`.** Solo `addendum_manual` + `reception`. El
`invoice_uuid` que guarda = `dto.uuid` (folio fiscal capturado en pantalla), que **aún no existe**
como fila en `tenant_fiscal.invoice` (la addenda manual se crea ANTES/independiente del registro
fiscal). Por eso la FK `addendum_manual.invoice_uuid → tenant_fiscal.invoice` estorba.

## Hallazgos en BD
- `tenant_finance.addendum_manual`: `addendum_manual_uuid, invoice_uuid, supplier_number, reception_id,
  purchase_order_number, supplier_type_id, user_id` + auditoría.
- FK única: **`fk_addendum_manual_invoice`** (`invoice_uuid → tenant_fiscal.invoice`) ← la que pide quitar.
- `CatRfcReceptor`: **NO existe** en shared_catalogs.
- `CATCONDICIONPAGOVALIDONC`: existe pero **vacío** (sin valores). Casing en MAYÚSCULAS (los hermanos
  `CatFormaPagoValidoNc`/`CatUsoCfdiValidoNc` van camelCase).

## Detalles nuevos a solucionar (interpretación)

| # | Punto Ivan | Interpretación | Dónde |
|---|---|---|---|
| 1 | CatRfcReceptor | Receptor fuera del catálogo → factura inválida, se rechaza. **Ya existe** validación de receptor (BUS008, usa tabla `receiver`/AuthorizedReceiverCatalogService). | fiscal `/register` |
| 2 | Consumida manual | Lo dispara **finanzas (Josue)**: addenda manual + recepción estatus 2. Ver flujo arriba. Fila 46 (Josue). | finanzas-api (hecho) |
| 3 | UUID no exista en addenda manual ni addenda | Al cargar XML en fiscal, validar que el fiscalUuid no esté ya en `addendum_manual` **ni** en `addendum` (que no se haya cargado ya la addenda manual). Fila 47. | fiscal `/register` |
| 4 | Quitar FK | Drop `fk_addendum_manual_invoice`. La addenda manual se crea antes del registro fiscal → la FK revienta. | BD (migración) |
| 5 | CatCondicionPagoValidoNc | Al registrar NC, validar el `FormaPago` del XML (ej. 99) contra `CATCONDICIONPAGOVALIDONC`. Fila 42. | fiscal `/register` NC |

**Extra (visto en xlsx, no en comentarios):** Fila 33 — botón cancelar factura activo en estatus 3
(En proceso envío), 6 (Error desglose), 16 (Estructura inválida). Mayormente front; el back debe
permitir la transición de cancelación desde esos estatus.

## Dudas a confirmar con Ivan (antes de codear)
1. **CatRfcReceptor**: ¿catálogo nuevo que reemplaza la validación actual de receptor (BUS008/tabla
   `receiver`), o es la que ya existe? Si es nuevo, Ivan lo crea + puebla.
2. ~~Check UUID duplicado~~ **RESUELTO (Josue 2026-06-23):** `addendum_manual.invoice_uuid` = el
   **folio fiscal** de la factura manual. En `/register` se compara el `fiscalUuid` del XML contra
   `addendum_manual.invoice_uuid`. Falta confirmar el **comportamiento esperado** si ya existe
   (¿rechazo? ¿con qué código?).
3. **CatCondicionPagoValidoNc vs CatFormaPagoValidoNc**: ambos validan el `FormaPago` del XML.
   ¿`CatCondicionPagoValidoNc` es **adicional** o **reemplaza** a `CatFormaPagoValidoNc` (BUS058, fila 65)?
   Y poblar el catálogo (sigue vacío).

## Resuelto / no es nuestra chamba
- **Quitar FK (punto 4): HECHO por Josue (2026-06-23)** — él dropeó la FK
  `fk_addendum_manual_invoice` del lado finanzas (es dueño de `tenant_finance.addendum_manual`).
  No es tarea nuestra. (Verificar que el cambio llegue al dump local si se re-restaura.)
- **Punto 2 (Consumida manual): HECHO por Josue** en finanzas-api.
