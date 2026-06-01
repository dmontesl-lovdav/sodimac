# Drift de contrato: invoice-status-sync (Robert) vs fiscal-api actual

> Fecha: 2026-06-01 · Autor: David Montes
> Contexto: invoice-status-sync (de Robert) lleva ~meses sin tocarse; fiscal-api ha estado en desarrollo activo (STM-410). El adapter de Robert fue escrito contra un contrato viejo. Este doc audita la divergencia ANTES de reescribir nada.

## Conclusión

**No reescribir el adapter de Robert todavía.** El contrato de fiscal-api derivó en 3 ejes (endpoints, modelo de búsqueda, validación de transición). Hay un conflicto de diseño de fondo, no solo paths. Requiere decisión con Ivan antes de codear.

## Eje 1 — Endpoint de búsqueda (fetch)

| | invoice-sync (Robert) | fiscal-api actual (STM-410) |
|---|---|---|
| Verbo/path | `GET /api/facturas?estatus={code}` | `POST /invoices/search/by-status` |
| Body | — (query param) | `InvoiceStatusSearchRequest` (JSON) |
| Respuesta | `List<FbcInvoiceDto>` plano | `Page<InvoiceSearchResponse>` (paginado) |

**Conflicto de diseño (grave):** el search actual exige campos **obligatorios**:
- `rfcEmisor` (NotBlank) — RFC de UN proveedor
- `fechaInicioRecepcion` / `fechaFinalRecepcion` (NotNull)
- `tipoDocumento` (NotBlank, I/E)
- `estatus` (NotNull)

El batch de Robert asume "dame TODAS las facturas en estatus X" (global, sin emisor). fiscal-api ya **no** ofrece eso: fuerza búsqueda **por-emisor + rango fechas**. Para barrer todos los proveedores, el batch tendría que iterar RFC por RFC, lo cual su diseño actual no contempla. → cambio de arquitectura, no solo de path.

## Eje 2 — Endpoint de actualización (PUT status)

| Campo | invoice-sync `FbcStatusUpdateRequest` | fiscal-api `InvoiceStatusUpdateRequest` | Match |
|---|---|---|---|
| numeroProveedor | int | BigDecimal | ✅ (JSON) |
| estatusOrigen | int | Integer | ✅ |
| estatusDestino | int | Integer | ✅ |
| idUsuarioActualizacion | int (=1) | Long (NotNull) | ✅ |
| fechaContabilizacion | — | LocalDate (req. si destino=7) | ⚠️ Robert no lo manda |
| comentario | sí | sí | ✅ |

Path `PUT /invoices/{uuid}/status` **coincide**. Payload casi alineado. Falta `fechaContabilizacion` cuando destino=7.

## Eje 3 — Validación de transición (el riesgo real)

fiscal-api STM-410 ahora **valida cada transición** contra el tren de estatus (catalogos/util-api). Rechaza con:
- `WRN7010` — estatus origen no catalogado
- `WRN7011` — transición no permitida

Robert genera la transición con `getFbcStatusCode()` (interno→FBC: 9→3, 10→7, 11→8, 13→11). Problema:

- Invoice interno 8→9: `getFbcStatusCode(8)=3`, `getFbcStatusCode(9)=3` → **PUT 3→3** (auto-transición). fiscal-api casi seguro la rechaza (WRN7011).
- Otras (3→7 interno 9→10) sí están en el ejemplo del DTO → probablemente válidas.

→ Aunque se arregle el fetch, **parte de los PUT fallarían** bajo la validación actual. El mapeo de estatus de Robert puede estar obsoleto respecto al tren vigente.

## Eje 4 — Respuesta (pendiente verificar)

`FbcStatusUpdateResponse` (Robert) usa `isSuccessful()` / `getResultMessage()`. fiscal-api `InvoiceStatusUpdateResponse` expone `success` / `code` / `message`. Posible mismatch de nombres (`resultMessage` vs `message`) → verificar deserialización.

## Recomendación / decisiones para Ivan

1. **¿El modelo de estatus (tren) sigue siendo el que asume Robert?** Si cambió, la lógica de transiciones 6→11 de invoice-sync hay que revalidarla, no solo el adapter.
2. **¿fiscal-api ofrecerá un fetch global-por-estatus** (sin rfcEmisor) para batch, o el batch debe iterar por proveedor?
3. **¿invoice-sync reemplaza o convive** con fiscal-download? Define si se reescribe o se jubila.
4. Hasta resolver: validar con **FBC mock + BD reales** (Oracle/i213/SAP) — ejercita la lógica de datos sin depender del contrato móvil. Ver [ANALISIS_BATCHES_ROBERT.md](ANALISIS_BATCHES_ROBERT.md).

## Estado fuentes (al 2026-06-01)

- fiscal-api: desarrollo activo (STM-410 ya integrado: search/by-status + validación transición).
- invoice-status-sync: congelado ~meses, contrato viejo.
- Drift confirmado en endpoints + búsqueda + validación.
