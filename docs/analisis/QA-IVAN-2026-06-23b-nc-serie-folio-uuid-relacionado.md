# Puntos llamada David+Ivan 2026-06-23 (NC, serie/folio, uuid relacionado)

> Puntos detectados en una llamada haciendo el flujo de NC, + escenarios de Fer/Ivan. Agregados a
> la matriz (xlsx v6) como filas 93-98. Interpretación, hallazgos en código y qué es back vs front.
> **Aún sin codear** (F98 pendiente confirmar regla con Ivan). Hermano:
> [QA-IVAN-2026-06-23-addenda-manual-rfc-condicionpago.md](QA-IVAN-2026-06-23-addenda-manual-rfc-condicionpago.md).

## Artefactos
- `sesiones/fiscal-api/Factura_CNO120829961_1094838 (1).xml`: Tipo I, Folio 9101041839, **sin Serie**,
  SubTotal 303,610.97 / Total 352,188.72. (imágenes imagenX02/X03)
- `sesiones/fiscal-api/Nota_Credito_AIR130902MN1_1076333.xml`: Tipo E, Serie DCBAIR, Folio 540138,
  TipoRelacion 01, **CfdiRelacionado UUID A9651E62-836A-4DB5-8005-9B9D2C51F478** (factura relacionada).

## Puntos

### F98 — Factura sin serie (con folio) no se puede subir — BACK (David)
Hoy `validateSeriesAndFolio` ([InvoiceServiceImpl] línea ~873) rechaza si falta serie **O** folio
(exige ambos): `(serie blank) || (folio blank)` → WRN7012 (factura) / WRN7015 (NC) "requiere serie
y folio". El CFDI CNO no trae Serie (válido en SAT) → rechazado.
**Regla definida (David, confirmada con el equipo):** el **FOLIO es requerido** (identifica el
documento, dedup serie+folio); la **SERIE es OPCIONAL** (en CFDI 4.0 SAT ambos son opcionales).
**HECHO** (`2e36ddd`): `validateSeriesAndFolio` rechaza solo si falta el folio; mensajes WRN7012/
WRN7015 ajustados a "requiere un folio" (enum + texto en BD `CatMsgAdvertencia`). Probado local:
factura con folio sin serie → RES004; sin folio → WRN7012. Deploy UAT: jar + UPDATE de los 2
mensajes en el catálogo. Mensaje a Ivan enviado (antes/después).

### F97 — Servicio de consulta de documento debe regresar el uuid relacionado — BACK (David) ✅ HECHO
`POST /fiscal/xml/process/file` devolvía `FiscalXmlResponse` sin el UUID de la factura relacionada.
**HECHO** (`edd6ee2`): se agregó `ComprobanteResponse.uuidRelacionado` y se puebla (+ `tipoRelacion`)
desde el nodo CfdiRelacionados. Probado con NC real → `uuidRelacionado=A9651E62...`, `tipoRelacion=01`.
Alimenta la pantalla de NC (F96, front).
**F95 (FormaPago): YA EXISTÍA** — `ComprobanteResponse.formaPago` ya se exponía y poblaba (=99 en la
prueba). Sin cambio de back; el front solo debe leerlo.

### F94 — Factura monto > recepción: sale "éxito" sin alerta de NC — **FRONT (finanzas-spa)** ✅ CONFIRMADO
El back **sí** devuelve `WRN7030` en `response.warnings[]` (validado fila 56). Bug en el front:
`finanzas-spa/src/features/orders/components/parts/ReceptionInvoiceControl.tsx` (L73-106):
1. El front **duplica la lógica de tolerancia** hardcodeada (`difference > 40`) en vez de confiar en
   el back. Solo llama `client.create()` si `difference > 40`.
2. En éxito (L78-90) **ignora `response.warnings[]`** y pinta fijo `"Tu factura se procesó
   correctamente"` → nunca muestra WRN7030 (alerta de NC).
3. La rama `else` (diff ≤ 40, L104-106) muestra el mensaje **al revés**: `"Hay una diferencia mayor a
   $40..."`.
**Fix front:** quitar la tolerancia local, registrar siempre y mostrar `response.warnings[]`
(WRN7030) tras el éxito. La tolerancia ya la evalúa el back. **No es back.**

### F93 — Monto de factura relacionada = subtotal, no total — **FRONT (finanzas-spa)** ✅ CONFIRMADO
`finanzas-spa/src/features/orders/components/ReceptionCredits.tsx` (L46): la columna **"Importe"**
usa `r.invoice.total` (con impuestos); debe usar `r.invoice.subtotal`. El back ya expone ambos
(`InvoiceSearchResponse` → `subtotal` y `total` separados; `Invoice` interface L199/L203 tiene los
dos). **Fix front:** cambiar `r.invoice.total` → `r.invoice.subtotal` en accessor y exportAccessor.
**No es back.**

### F96 — Pantalla NC: uuid relacionado en su campo — FRONT
Consume F97. Hoy la pantalla toma el UUID de la propia NC; debe mostrar el de la factura relacionada.

## Resumen back (David)
| Fila | Punto | Estado |
|---|---|---|
| F98 | serie/folio: folio requerido, serie opcional | ✅ HECHO + validado UAT (`2e36ddd`). Folio sin serie → RES004; sin folio → WRN7012 |
| F97 | exponer uuid relacionado en `xml/process/file` (+ FormaPago F95) | ✅ HECHO + validado UAT (`edd6ee2`). `uuidRelacionado=A9651E62...`, `tipoRelacion=01`, `formaPago=99` |
| F94 | warning NC en pantalla | 🔵 **FRONT** confirmado — `ReceptionInvoiceControl.tsx` L73-106 ignora `warnings[]` + tolerancia local hardcodeada. Back OK |
| F93 | monto relacionado subtotal vs total | 🔵 **FRONT** confirmado — `ReceptionCredits.tsx` L46 usa `total`, debe `subtotal`. Back OK |

## Excel (xlsx v6)
- F97 → Rev DEV ✔ / Ajuste DEV ✔ (validado UAT 2026-06-23)
- F98 → Rev DEV ✔ / Ajuste DEV ✔ (validado UAT 2026-06-23)
- F94, F93 → pendientes (front/finanzas)
- F95 (FormaPago) → front (back ya lo expone)
- F96 (uuid relacionado en pantalla NC) → front (consume F97)
