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

## VALIDADO EN UAT 2026-06-23
- **Fila 47 / WRN7032:** registro con folio fiscal en `addendum_manual` → `WRN7032` (HTTP 400). OK.
- **Receptor CATRFCRECEPTOR:** receptor autorizado (CSD161207R2A) pasa la validación; ya no aparece
  el error de `authorized_receiver_catalog` (tabla dropeada + código nuevo vivo). Negativo (BUS008)
  validado en local.
- **Mensajes WRN7030/7031/7032** registrados en `CatMsgAdvertencia` (inserts directos en UAT, no versionados).
- **Fila 42:** cerrada (cubierta por BUS058 / `CatFormaPagoValidoNc`).

## Plan de implementación (fiscal-api)
1. ✅ **Fila 47 — HECHO** (`65e4632`). `WRN7032` nuevo + `/register` valida el `fiscalUuid` contra
   `addendum_manual.invoice_uuid`; si existe → WRN7032. Probado local (folio en manual → WRN7032,
   folio nuevo → RES004).
2. ✅ **CatRfcReceptor — HECHO** (`9d1a975`). Validación de receptor migrada a `CatRfcReceptor`
   (shared_catalogs) en los **3 flujos** (factura InvoiceServiceImpl + InvoiceRegistrationServiceImpl,
   complemento PaymentValidationServiceImpl). Seed util-api 19. Tabla `authorized_receiver_catalog`
   dropeada en local (ddl-auto=none, boot OK). Probado: autorizado → RES004, ausente → BUS008.
3. ✅ **Fila 42 — cerrada** (ya cubierta por BUS058, sin cambio).

## CATRFCRECEPTOR — estructura real (Ivan ya lo pobló en UAT)
El catálogo lo creó y pobló **Ivan** (UAT id 100, code `CATRFCRECEPTOR` en MAYÚSCULAS). Modelo:
- `catalog_detail.value` = id secuencial (1, 2...), **NO el RFC**.
- `catalog_detail.key` = `CRR000x`. `catalog_detail.status` = 1 activo / 0 inactivo.
- **El RFC vive en `dictionary_lang.description`** (lang_id 1).

La validación (`existsRfcReceptorAutorizado`) matchea `dl.description = :rfc` con `cd.status=1` y
`ch.status=1`. **No corremos seed** (Ivan administra el catálogo desde pantalla). El seed 19 se
eliminó. Probado local (datos sincronizados de UAT): CSD161207R2A (status 1) → autorizado;
COZI841029TE2 (status 0) → rechazado; ausente → rechazado.

## Pendiente para deploy UAT
- **Deploy del jar fiscal-api** (la validación nueva). El catálogo ya está poblado en UAT.
- **DROP** `tenant_fiscal.authorized_receiver_catalog` en UAT (ya no se usa).
- **Cleanup pendiente (code):** el CRUD `AuthorizedReceiverCatalog*` (entity/repo/service/controller/
  mapper/dto) queda huérfano tras dropear la tabla. Su endpoint GET fallaría si se llama. Quitar en
  un commit aparte.
