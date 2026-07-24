# Checklist QA — puntos de David (matriz v13 + comentarios Ivan/Fer)

**Fecha:** 2026-07-23 · **Fuente:** `QA_Matriz_Pruebas_Portal_FBC_Mexico_v1.0 (13).xlsx` (hoja "Listado Issue Abiertos") + comentarios Ivan/Fer 2026-07-23.

Leyenda estado matriz: `rev`=Revisión DEV, `aj`=Ajuste DEV, `QA`=Validado QA.

---

## A. Cerradas — validadas por QA (solo confirmar que sigan marcadas)

| Fila | Niv | Descripción | Commit / nota |
|---|---|---|---|
| 28 | M | Tipo proveedor no se ve en grid consulta factura | validado QA 25/06 |
| 29 | A | Valor recepción se presenta guionizado | validado QA 25/06 |
| 30 | A | Filtro búsqueda facturas retorna info de otros | validado QA 25/06 |
| 40 | M | Tipo proveedor no se ve en grid NC | validado QA |
| 41 | A | Filtro búsqueda NC retorna info de otros | validado QA |
| 42 | A | Filtrar condición de pago válida para NC | validado QA |
| 47 | A | Validar factura registrada al subir | validado QA |
| 48 | A | Fecha del sistema en fecha recepción | validado QA 29/06 |
| 55/56/57 | A | Estatus recepción a Consumida al publicar | validado QA |
| 64 | A | Permitir NC con condición de pago válida | validado QA |
| 95 | M | Agregar uuid en consulta de documento | validado QA |
| 96 | M | No subir factura sin serie pero con folio | validado QA |
| 119 | A | Reordenar estatus catálogo NC | validado QA |
| 153 | A | NC "return unique key" (ERR003 no único) | `34b0b2a` — validado QA |
| 159 | M | Máscara $ en msg tolerancia factura vs recepción | `c7a07e0` — validado QA |
| 160 | M | Máscara $ en msg confirmación tolerancia (WRN7034) | `c7a07e0` — validado QA |

---

## B. Hechas por David — falta pase/validación QA (marcar Ajuste=x, pedir QA)

| Fila | Niv | Descripción | Estado / commit |
|---|---|---|---|
| 16 | A | Columna addenda NC: descuento comercial (2) | ajuste hecho, falta QA |
| 43 | A | Columna addenda NC: ajuste por recepción (1) | ajuste hecho, falta QA |
| 101 | A | Estatus factura 3 "En proceso de envío" al cumplir tolerancia (factura−NCs) | ajuste hecho, falta QA |
| 179 | A | Validar TipoRelacion de la NC (era "01", ahora por catálogo `CatTipoRelacionFacturaNC` = 01 y 03) | `ef4229c` — validado e2e local, falta deploy+QA |
| **197** | A | **NC no muestra uuid relacionado (null)** — root cause: portal guardó code del catálogo en MAYÚSCULAS, back comparaba camelCase | `7c4b5b9` — **ya resuelto** (query case-insensitive), falta deploy+QA |

> Nota f179/f197: dependen del catálogo `CatTipoRelacionFacturaNC`. Ya está creado en UAT (por portal, code UPPERCASE). El fix `7c4b5b9` lo lee case-insensitive.

---

## C. Pendientes de David (rev=x, sin ajuste) — trabajo por hacer

Columna **Excel** = está en la matriz v13 (fila real). Todos aquí son del Excel.

| Fila | Excel | Niv | Descripción | Qué implica |
|---|---|---|---|---|
| 113 | Sí | A | Cancelar **factura** → error 500 | Reproducir + fix. **Depende de trace UAT** (posible inestabilidad de ambiente). |
| 114 | Sí | A | Cancelar **NC** → error 500 | Idem. (La duda de Fer del "no existe" era uuid PK vs fiscal; el 500 es aparte.) |
| 118 | Sí | A | Estatus NC = "Recibida parcial" (2) hasta que (factura−NCs) cumpla tolerancia; luego ambos a "En proceso de envío" | **YA CUBIERTO** por `reevaluarFacturaTrasNc` (InvoiceServiceImpl L1232): neto en tolerancia → factura+NCs a 3; fuera y >recepción → NCs a 2; <recepción → cascada. Mismo trabajo fila 104/122 (validado UAT 22/06). Marcar hecho; QA revalida las 2 ramas. |
| 121 | Sí | M | Agregar al API de consulta de NC el filtro por **uuid de la factura relacionada** | **YA CUBIERTO** — el filtro `relatedInvoiceUuid` ya existe en el spec (reproducido OK local, 200). Marcar hecho. |
| 122 | Sí | M | Consulta por uuid en filtro de NC no retorna (y da **500**, ver Y01) | **"No retorna" RESUELTO `719a027`**: el filtro solo aceptaba invoice_uuid interno; el usuario usa el folio fiscal → 0 resultados. Ahora acepta ambos. El **500** sigue aparte (depende de trace UAT). |
| 111 | Sí | B | Nombre archivo PDF sin serie/folio: no poner el guión medio | **YA CUBIERTO** — `buildDocumentFileName` (L2471) agrega "-" solo si hay serie Y folio. Marcar hecho; QA revalida. |
| 112 | Sí | B | Ídem para NC | **YA CUBIERTO** (mismo método, aplica a XML y PDF). Marcar hecho. |

---

## D. Nuevas en v13 (sin revisión) — por analizar/implementar

| Fila | Excel | Niv | Descripción | Nota |
|---|---|---|---|---|
| **196** | Sí | A | Permitir agregar una NC **con o sin factura**, solo para descuentos comerciales (tipoNC=2) | **HECHO `8643cff`** — NC tipo 2 sin CfdiRelacionados pasa; tipo 1 sigue exigiendo factura. Validado e2e local. Falta pase+QA. |
| **197** | Sí | A | (ver sección B — ya resuelto `7c4b5b9`, **validado UAT 24/07**) | — |
| **198** | Sí | M | Filtrar consulta de NC por **Tipo de Nota de Crédito** (listbox junto al estatus) | **HECHO `bb8f79b`** — filtro `tipoNotaCredito` en search (subquery addendum). Validado e2e local. Front: agregar el listbox. Falta pase+QA. |

---

## E. Observaciones Ivan/Fer 2026-07-23 — CONVERSACIÓN (no Excel)

Estas NO están en la matriz; salieron del chat. Los 500 no reprodujeron local → posible **inestabilidad de UAT** (Ivan/Fer: "de repente UAT deja de responder"). Se deprioriza hasta tener trace.

| Reporte | Imagen | Excel | Issue | Estado |
|---|---|---|---|---|
| Buscar NC por uuid factura → 500 | Y01 | No (conversación) | correlaciona f121/f122 | No reprodujo local; env UAT |
| Publicar NC → 500 | Y02 | No (conversación) | register 500 | No reprodujo local; env UAT |
| "UUID Factura" vacío al publicar NC | Y03 | No (conversación) | = f197 | **Resuelto `7c4b5b9`, validado UAT 24/07** |
| Recepción manual con uuid duplicado | Y04 | No (conversación) | finanzas/back | Pasar a finanzas/back |

---

## Foco recomendado (solo Excel, autónomos, sin depender de trace UAT)

Orden sugerido:
1. **f198** (M) — filtro por Tipo de NC en el search. Continúa `c7a07e0`, contrato ya listo. Rápido.
2. **f196** (A) — NC con/sin factura para descuento comercial. Toca `saveRelatedCfdis`.
3. **f118** (A) — estatus NC en cascada por tolerancia.
4. **f121** (M) — filtro por uuid factura relacionada (validar; el spec ya lo trae).
5. **f111/f112** (B) — cosmético nombre archivo.

**Bloqueados por trace UAT** (no local): f113, f114, f122 (los 500).

---

## Retro Ivan 2026-07-24 (no es fila directa)

- **Mensaje tipo relación no permitido** — cuando la NC trae un `TipoRelacion` no permitido, salía BUS042 (engañoso). Ahora → **BUS045** con texto claro ("El tipo de relación de la NC no se encuentra permitido. Por favor, validar con el área financiera..."). Commit `62512b7` + seed `migration/QA-2026-07-24-BUS045-...sql`. **Validado UAT 24/07.** Va con el punto de tipo relación (179/197).
- **Tipo de NC en blanco** (`tipoNotaCredito`=0 por defecto) → es del **front (Fer)**: debe enviar el `tipoNotaCredito` al publicar. Alternativa back (no elegida): derivar del `<TipoNC>` del addenda.

## Estado final (2026-07-24): todo lo de fiscal-api al alcance de David, cerrado

| Punto | Estado |
|---|---|
| 196, 197, 198, 122 | ✓ hechos + validados UAT |
| BUS045 (retro Ivan) | ✓ hecho + validado UAT |
| 118, 121, 111, 112, 179 | ✓ ya cubiertos / validados |
| **Abierto (no depende de David):** 113, 114, 500 de 122 | ⏳ bloqueados por trace UAT (posible inestabilidad ambiente) |
| **Front (Fer):** listbox f198, enviar tipoNotaCredito | ⏳ |

## Resumen de acciones

1. **Marcar en la matriz**: sección A (confirmar cerradas); sección B → Ajuste=x + solicitar QA (16, 43, 101, 179, 197).
2. **Desplegar a UAT**: `ef4229c` (catálogo NC) + `7c4b5b9` (casing) → cierra 179 y 197.
3. **Por implementar (prioridad)**: 121/122 (filtro uuid + 500 search NC), 114/113 (cancelar 500), 196 (NC sin factura descuento), 198 (filtro tipoNC), 118 (estatus cascada), 111/112 (cosmético).
4. **Fuera de David**: Y04 (recepción manual uuid duplicado) → finanzas/back.
5. **Causa raíz catálogos (informar, no toca David)**: util-api `createCatalog` uppercasea el code → catálogos creados por portal quedan en MAYÚSCULAS. Mitigado en fiscal-api con query case-insensitive.

---

## F. Clases a modificar en el pase completo a Sodimac (fiscal-api)

Pendiente de pasar a Sodimac (develop→uat). Commits mirror: `76380dc`, `aa4ed2a`, `ef4229c`, `7c4b5b9`.

| Clase | Cambio | Commit(s) |
|---|---|---|
| `model/dto/invoicexml/InvoiceXmlDto.java` | `cfdiRelacionados` de objeto único → `List` (captura todos los bloques) | 76380dc |
| `repository/AddendumRepository.java` | `findActiveCatalogValues` (query directa a shared_catalogs) + `UPPER()` case-insensitive | ef4229c, 7c4b5b9 |
| `service/impl/FiscalXmlTransformerServiceImpl.java` | consulta filtra bloque por catálogo `CatTipoRelacionFacturaNC` (inyecta AddendumRepository) | ef4229c |
| `service/impl/InvoiceServiceImpl.java` | `saveRelatedCfdis` filtra bloques por catálogo (register) | 76380dc, ef4229c |
| `util/XmlSecureFactory.java` | fix PDF cortesía (XXE Xalan best-effort, sin FEATURE_SECURE_PROCESSING) + `@SuppressWarnings` S2755 | aa4ed2a |
| `test/.../service/impl/CfdiRelacionadosTipo01Test.java` | test 5 casos (catálogo NC, JAXB) | 76380dc, ef4229c, 7c4b5b9 |
| `model/dto/InvoiceSearchRequest.java` | campo `tipoNotaCredito` (filtro f198) | bb8f79b |
| `repository/specification/InvoiceSpecification.java` | predicado filtro por `tipoNotaCredito` (f198) | bb8f79b |
| `service/impl/InvoiceServiceImpl.java` | NC Descuento Comercial (tipo 2) sin factura relacionada (f196) | 8643cff |
| `repository/specification/InvoiceSpecification.java` | filtro NC por factura relacionada acepta fiscal_uuid o interno (f122) | 719a027 |

Nota: `docs/` NO viaja al repo real de Sodimac (solo mirror). En UAT **no** requiere seed del catálogo (Ivan ya lo creó por portal); el `UPPER()` cubre el casing.
