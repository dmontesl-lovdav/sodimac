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
| 6 | NC de descuento comercial (PDF, tipo NC, addenda) | register NC | 🟡 feature grande (catálogo CatTipoNotaCredito ya creado) |

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

---

## Orden de ataque

1. **#1 + #3** — poblar `tipoProveedor` en registro + corregir `noRecepcion`. Concreto, bajo riesgo.
2. **#4 + #5** — filtro por tipo de proveedor (patrón repetible en 2 endpoints).
3. **#6** — feature NC descuento comercial (su propio alcance; catálogo ya listo).

## Dudas a confirmar
- #1: ¿qué valor exacto debe llevar `noRecepcion` (IdRecepcion de la addenda vs reception_number de finanzas)?
- #6: confirmar a Ivan el catálogo `CatTipoNotaCredito` (ya creado) + alcance addenda NC.
