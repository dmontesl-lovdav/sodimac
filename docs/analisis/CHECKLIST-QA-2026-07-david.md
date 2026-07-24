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
| 118 | Sí | A | Estatus NC = "Recibida parcial" (2) hasta que (factura−NCs) cumpla tolerancia; luego ambos a "En proceso de envío" | Lógica de estatus en cascada NC↔factura. **Autónomo.** |
| 121 | Sí | M | Agregar al API de consulta de NC el filtro por **uuid de la factura relacionada** | Filtro `relatedInvoiceUuid` ya existe en el spec (reproducido OK local). Validar/cerrar. **Autónomo.** |
| 122 | Sí | M | Consulta por uuid en filtro de NC no retorna (y da **500**, ver Y01) | El 500 no reprodujo local (posible env UAT). **Depende de trace UAT.** |
| 111 | Sí | B | Nombre archivo PDF sin serie/folio: no poner el guión medio | Cosmético. **Autónomo.** |
| 112 | Sí | B | Ídem para NC | Cosmético. **Autónomo.** |

---

## D. Nuevas en v13 (sin revisión) — por analizar/implementar

| Fila | Excel | Niv | Descripción | Nota |
|---|---|---|---|---|
| **196** | Sí | A | Permitir agregar una NC **con o sin factura**, solo para descuentos comerciales (tipoNC=2) | Cambio de fondo: hoy `saveRelatedCfdis` exige factura (BUS042/043). Permitir sin factura cuando es descuento comercial. **Autónomo.** |
| **197** | Sí | A | (ver sección B — ya resuelto `7c4b5b9`, **validado UAT 24/07**) | — |
| **198** | Sí | M | Filtrar consulta de NC por **Tipo de Nota de Crédito** (listbox junto al estatus) | Continuación de `c7a07e0` (ya devuelvo `tipoNotaCreditoDescripcion`); agregar filtro en el search. **Autónomo.** |

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

Nota: `docs/` NO viaja al repo real de Sodimac (solo mirror). En UAT **no** requiere seed del catálogo (Ivan ya lo creó por portal); el `UPPER()` cubre el casing.
