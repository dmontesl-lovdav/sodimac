# Issues Fer 2026-06-19 — endpoint search + NC descuento comercial

> Reporte de Fer (g_dco018) sobre `/invoices/search`, `complementos-pago/buscar` y NC de
> descuento comercial. Interpretación, estado y plan de ataque.
> Endpoints UAT:
> - `POST /ppsomx/fiscal/invoices/search`
> - `GET /ppsomx/fiscal/fiscal/complementos-pago/buscar`

---

## Resumen / estado

| # | Issue | Endpoint | Estado |
|---|---|---|---|
| 1 | `noRecepcion` muestra GUID en vez del número; `tipoProveedor` null | search | ✅ resuelto 2026-06-19 (`5f5f4b8` noRecepcion + `961513d` tipoProveedor) |
| 2 | Filtro fecha "no opera" (26-30 may vacío) | search | ✅ cerrado (era pre-reunión Ivan) |
| 3 | `tipoProveedor` null también en NC | search | ✅ resuelto con #1 (`961513d`) |
| 4 | Agregar filtro por `tipoProveedor` (id numérico) | search | ✅ resuelto 2026-06-19 (`5fe715b`) — filtro + id/descripción en response |
| 5 | Agregar filtro por `tipoProveedor` | complementos-pago/buscar | ✅ resuelto 2026-06-19 — filtro + id/descripción en response |
| 6 | NC de descuento comercial (PDF, tipo NC, addenda) | register NC | ✅ resuelto 2026-06-22 (`23898c0`+`65e8562`) — tipo NC en addenda, validado UAT. Ver [CHECKLIST-RECEPCION-NC-2026-06.md](CHECKLIST-RECEPCION-NC-2026-06.md) |

**Backfill datos viejos (UAT, 2026-06-19):** el fix #1/#3 puebla al registrar, así que las addendas previas quedaban con `supplier_type` null y `reception_number` = UUID. Se corrió un script de backfill **manual en UAT** (no versionado) que rellena `addendum.supplier_type` (id 1-4 por proveedor) y reemplaza `reception_number` UUID → número de finanzas. Idempotente. Validado: PARKMEX (252523) → `noRecepcion` 846919, `tipoProveedor` 1, "Mercancía". Excepción: addendas sin `supplier_number` quedan en null (no hay con qué resolver).

**VALIDADO EN UAT 2026-06-19:** deploy vivo, campos presentes y poblados en datos reales tras backfill. #1/#3/#4/#5 OK.

**Decisiones tomadas:**
- #1 `noRecepcion` = `tenant_finance.reception.reception_number` (numérico) resuelto por receptionId (UUID).
- #3 `tipoProveedor` = id de CatTipoProveedor (1-4), leído **directo de shared_catalogs** (sin util-api), guardado en `addendum.supplier_type` al registrar.

---

## 1. `noRecepcion` y `tipoProveedor` mal en el response

**Síntoma (imagen Fer):** columna "Recepción" muestra el UUID `e39ef97b-…`; columna "Tipo Proveedor" sale `--`. En el response: `noRecepcion: "e39ef97b-…"`, `tipoProveedor: null`.

**Causa:**
- `noRecepcion` = `addendum.reception_number`. En el **registro** se guarda el **UUID** de la recepción (`receptionId`), no el número. Fer espera el número numérico (tipo `846919`).
- `tipoProveedor` = `addendum.supplier_type`. En el **registro NO se setea** `supplier_type` (solo en el flujo de *actualización*, `InvoiceServiceImpl` ~1380). Por eso null en el listado.

**Plan:**
- Setear `addendum.supplier_type` al registrar (origen: tipo del proveedor del XML/addenda o vía supplierNumber → CatTipoProveedor).
- Definir qué debe llevar `noRecepcion`: el número de recepción (no el UUID). Origen posible: addenda XML `IdRecepcion`, o `reception.reception_number` de finanzas. **Confirmar con Fer/Ivan** cuál es el "número" esperado.

## 2. Filtro de fecha — CERRADO

El mensaje de Fer fue **antes** de la reunión con Ivan. Ivan definió que la búsqueda va por la
**fecha de registro de la factura** (`invoice.created_at`), no por `reception_date` de finanzas.
Ya está así (revert 2026-06-19, commit `eeb0d4f`). Ver [docs/soporte/fer.md](../soporte/fer.md) y
[wiki/procesos/11-recepcion-y-fechas.md](../wiki/procesos/11-recepcion-y-fechas.md). **Sin acción.**

## 3. `tipoProveedor` en NC

Mismo problema que #1 (mismo endpoint y campo). Se resuelve junto con #1.

## 4. Filtro `tipoProveedor` en search

Agregar parámetro al request de `/invoices/search`: id numérico del tipo de proveedor →
devolver solo facturas/NC de ese tipo. Hoy `InvoiceSearchRequest` **no** filtra por tipo
(el front lo manda en el payload pero se ignora). Filtra vía `addendum.supplier_type`.

## 5. Filtro `tipoProveedor` en complementos-pago/buscar

Igual que #4 pero en `GET /fiscal/complementos-pago/buscar`. Mismo patrón.

## 6. NC de descuento comercial (feature)

En descuentos comerciales se puede publicar una NC. Ajustes al back:
- Permitir **PDF en la NC** (hoy solo factura sube PDF a GCS).
- Guardar el **UUID de la factura relacionada** en la NC.
- Marcar que la NC **pertenece a un descuento comercial**.
- Guardar en la **addenda** el **tipo de NC**: `1 = Ajuste por Recepción`, `2 = Descuento Comercial`.
- Pasar en la addenda de la NC: **Número de documento, Id Proveedor, Tipo NC**.

**Tipo de NC ≠ tren/estatus.** Es clasificación de origen. Catálogo nuevo **`CatTipoNotaCredito`**
ya creado (seed 18 util-api, commit `9b6e305`): 1 Ajuste Recepción, 2 Descuento Comercial.

### Validación de código (2026-06-19) — qué ya existe vs qué falta

**Ya cubierto en código (sin acción):**
- `register` **sí guarda NC**, no solo facturas: `InvoiceServiceImpl` línea 170 permite
  `NOTA_CREDITO`, bloque NC línea 277, `saveRelatedCfdis` 1006-1008.
- **Tipo de documento sale del XML**: `detectDocumentType` lee `TipoDeComprobante`
  (`XmlDocumentTypeDetectorServiceImpl` línea 65): `I`=Factura, `E`=NC.
- **PDF en NC**: el upload a GCS es genérico (no limitado a factura). Solo falta que el front
  lo mande.
- **Relación NC↔factura**: `saveRelatedCfdis` ya guarda en `related_cfdi` el UUID de la factura
  + `relation_type` (`TipoRelacion` SAT).

**Bloqueante — el Tipo NC (1/2) NO viene en el CFDI:**
El XML solo trae `TipoDeComprobante` (I/E) y `TipoRelacion` (SAT `c_TipoRelacion` 01-07, "01 =
Nota de crédito de docs relacionados"). Ninguno distingue **Ajuste Recepción vs Descuento
Comercial** — es clasificación de negocio. Confirmado: `InvoiceServiceImpl` línea 1444
`tipoNotaCredito - No existe campo equivalente en AddendumEntity`. Existe DTO
`AddendaUpdateDto.tipoNotaCredito` (línea 40) pero **se recibe y se descarta**.

**Por qué va en `addendum`:** es donde viven los campos de negocio fuera del CFDI por documento
(`supplier_number`=Id Proveedor, `reception_number`, `supplier_type`, `addenda_type`). Fer pidió
Tipo NC junto a Id Proveedor + Número documento, que ya son campos de addendum → un solo payload.

**2 preguntas pendientes a Ivan (enviadas 2026-06-19):**
1. ¿De dónde sale el Tipo NC (1/2)? No está en CFDI. ¿Front lo manda como parámetro en la
   addenda, o se infiere del módulo de descuentos?
2. Confirmar **columna nueva en `addendum`** (`tipo_nota_credito`). ¿OK agregar el campo?

---

## Orden de ataque

1. **#1 + #3** — poblar `tipoProveedor` en registro + corregir `noRecepcion`. Concreto, bajo riesgo.
2. **#4 + #5** — filtro por tipo de proveedor (patrón repetible en 2 endpoints).
3. **#6** — feature NC descuento comercial (su propio alcance; catálogo ya listo).

## Dudas a confirmar
- #1: ¿qué valor exacto debe llevar `noRecepcion` (IdRecepcion de la addenda vs reception_number de finanzas)?
- #6: confirmar a Ivan el catálogo `CatTipoNotaCredito` (ya creado) + alcance addenda NC.
