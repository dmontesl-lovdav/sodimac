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

## Resoluciones Ivan (2026-06-23) — todas confirmadas

**1. CatRfcReceptor — SÍ cambiar.** Migrar la validación de receptor de la tabla
`tenant_fiscal.authorized_receiver_catalog` al catálogo `CatRfcReceptor` (shared_catalogs). Razón:
la tabla no tiene pantalla de mantenimiento; el catálogo sí. Además **borrar** la tabla
`authorized_receiver_catalog`. Hoy tiene 3 RFCs (CGE990101GHI, CSD161207R2A, LAN7008173R5) → migrar
a `CatRfcReceptor`. Validación sigue rechazando si el RFC no está (mismo mensaje BUS008).

**2. CatCondicionPagoValidoNc — NO usar.** Ivan confirma que el catálogo correcto es
`CatFormaPagoValidoNc` (ya implementado, BUS058). `CATCONDICIONPAGOVALIDONC` queda **sin uso**
(vacío). → **Fila 42 = ya cubierta** por la validación BUS058 (= fila 65). Sin cambio de código.

**3. Addenda manual (fila 47) — RESUELTO.** `addendum_manual.invoice_uuid` = folio fiscal (Josue).
Al cargar el XML, comparar el `fiscalUuid` contra `addendum_manual.invoice_uuid`; si existe →
**WRN7032**: *"La factura se encuentra previamente registrada manualmente, Por favor, validar con el
área de finanzas."* (dar de alta el mensaje).

## Hecho por otros / no es nuestra chamba
- **Quitar FK (punto 4): HECHO por Josue** — dropeó `fk_addendum_manual_invoice` en UAT (es dueño de
  `tenant_finance.addendum_manual`). En **local lo dropeamos nosotros** 2026-06-23 para igualar.
- **Consumida manual (punto 2): HECHO por Josue** en finanzas-api.

## Plan de implementación (fiscal-api)
1. **Fila 47** — `WRN7032` nuevo + en `/register` validar el `fiscalUuid` contra
   `tenant_finance.addendum_manual.invoice_uuid`; si existe → WRN7032. (El duplicado contra
   `addendum`/`invoice` ya lo cubren WRN7013/7014.)
2. **CatRfcReceptor** — seed del catálogo (header + 3 RFCs actuales), cambiar la validación de
   receptor para leer `CatRfcReceptor` en vez de `authorized_receiver_catalog`, y drop de la tabla.
3. **Fila 42** — cerrar, ya cubierta por BUS058 (sin cambio).
