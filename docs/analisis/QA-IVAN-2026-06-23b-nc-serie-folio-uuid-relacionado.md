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

### F97 — Servicio de consulta de documento debe regresar el uuid relacionado — BACK (David)
`POST /fiscal/xml/process/file` devuelve `FiscalXmlResponse`, que **no** expone el UUID de la
factura relacionada (CfdiRelacionados). Para NC hay que **agregar el campo + poblarlo** del XML
(ej. NC AIR → A9651E62...). Alimenta la pantalla de NC (F96, front).
**F95 (Fernando, mismo DTO):** agregar también **`FormaPago`** en el response (la pantalla NC lo
necesita al publicar).

### F94 — Factura monto > recepción: sale "éxito" sin alerta de NC — probable FRONT
El back **sí** devuelve `WRN7030` en `response.warnings[]` (validado en fila 56). Si sale "éxito"
sin la alerta: o (a) el caso quedó **dentro de tolerancia** (±$40) → Recibida, sin warning
(correcto), o (b) el **front finanzas/recepción no muestra `warnings[]`**. Pantalla Finanzas.
**Confirmar:** montos usados + si el front lee `warnings[]`.

### F93 — Monto de factura relacionada = subtotal, no total — probable FRONT (finanzas)
En el detalle de la recepción, el monto de la factura relacionada muestra el **total** (con
impuestos); debe mostrar **subtotal**. `InvoiceSearchResponse` ya expone `subtotal` y `total`
separados. **Confirmar de dónde toma el monto** (fiscal `/invoices/search` vs endpoint finanzas);
si es del search, el front debe usar `subtotal`.

### F96 — Pantalla NC: uuid relacionado en su campo — FRONT
Consume F97. Hoy la pantalla toma el UUID de la propia NC; debe mostrar el de la factura relacionada.

## Resumen back (David)
| Fila | Punto | Estado |
|---|---|---|
| F98 | serie/folio: rechazar solo si faltan ambos | ⏳ confirmar regla con Ivan, luego `||`→`&&` |
| F97 | exponer uuid relacionado en `xml/process/file` (+ FormaPago F95) | ⬜ codear (FiscalXmlResponse) |
| F94 | warning NC en pantalla | ⚠️ back ya manda warnings[]; confirmar front/montos |
| F93 | monto relacionado subtotal vs total | ⚠️ confirmar origen; probable front finanzas |
