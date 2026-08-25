# Tren de Estatus v1.0 (5) — Cambios y plan de pruebas

> Fuente: `Tren_Estatus_Portal_FBC_v1.0 (5).xlsx` (Ivan, 2026-08-24/25).
> Aplicado: local + UAT. Commits mirror `dmontes`: `97d9b8b`, `d1a5be6`, `66c51b3`, `7fbb13e`.
> Script BD: [`APP03022-mrch.backend.somx.fiscal-api/migration/QA-2026-08-24-tren-estatus-v1.0-5.sql`](../../APP03022-mrch.backend.somx.fiscal-api/migration/QA-2026-08-24-tren-estatus-v1.0-5.sql)

---

## 1. Qué es este cambio

**Remodel COMPLETO del catálogo de estatus de Factura y Nota de Crédito.** No es un ajuste incremental: renumera el significado de casi todos los códigos y rediseña el flujo. **Ivan confirmó que las facturas/NC previas quedan descartadas** → no hay migración de datos (reemplazo limpio de catálogo + tren).

Módulos afectados en `shared_catalogs.status_train`: `option_id = 1` (Factura) y `2` (NC). **Carta Porte (4) quedó idéntica** en v1.0(5) → no se tocó. Recepción (5) tampoco.

---

## 2. Renumeración de estatus — Factura (`CatEstatusFactura`)

| Código | Antes (v1.0 (2)/(4)) | Ahora (v1.0 (5)) |
|---|---|---|
| 1 | Rechazo Comercial | **No válido fiscal** |
| 2 | Recibido Parcial | Recibido Parcial *(igual)* |
| 3 | En proceso de envío | En proceso de envío *(igual)* |
| 4 | En proceso de descarga | **En proceso de desglose** |
| 5 | Desglose de factura | Desglose de factura *(igual)* |
| 6 | Error en el desglose | **Error en en el desglose xml** |
| 7 | Pendiente de registro FOSITO | **Pendiente Envío** |
| 8 | Pendiente envío i213 | **Enviada** |
| 9 | Factura enviada a la i213 | **Error registro contable** |
| 10 | Pendiente contabilizar | **Error de Envío** |
| 11 | Pendiente de Pago | **Pendiente Envío i213** |
| 12 | Pendiente de Complemento | **Enviada i213** |
| 13 | Completado | **Error i213** |
| 14 | Rechazo Contable | **Pendiente de contabilizar** |
| 15 | No válido fiscal | **Pendiente de Pago** |
| 16 | Error envío DMS | **Rechazo Contable** |
| 17 | Error envío i213 | **Pendiente de complemento** |
| 18 | Pago Manual | Pago Manual *(igual)* |
| 19 | Error envío SAPITO | **Completado** |
| 20 | Error contabilización | **Cancelada** |
| 21 | Pendiente movimiento contable | **eliminado** (huérfano, sin transición) |
| 22 | Error en el desglose contable | **eliminado** (huérfano, sin transición) |

Solo 2, 3, 5, 18 conservan significado.

## 3. Renumeración de estatus — Nota de Crédito (`CatEstatusNotaCredito`)

| Código | Antes (v1.0 (3)) | Ahora (v1.0 (5)) |
|---|---|---|
| 1 | Rechazo Comercial | **No válido fiscal** |
| 2 | Recibida Parcial | Recibida Parcial *(igual)* |
| 3 | En proceso de envío | En proceso de envío *(igual)* |
| 4 | Pendiente contabilizar | **En proceso de desglose** |
| 5 | En proceso de descarga | **Desglose de nota de crédito** |
| 6 | Desglose NC | **Error en en el desglose xml** |
| 7 | Error desglose NC | **Pendiente Envío** |
| 8 | Contabilizada | **Enviada** |
| 9 | Descontada | **Error registro contable** |
| 10 | Rechazo contable | **Error de Envío** |
| 11 | Cancelada | **Pendiente Envío i213** |
| 12 | Borrada | **Enviada i213** |
| 13 | *(no existía)* | **Error i213** (nuevo) |
| 14 | *(no existía)* | **Pendiente de contabilizar** (nuevo) |
| 15 | *(no existía)* | **Pendiente de descuento Pago** (nuevo) |
| 16 | *(no existía)* | **Rechazo Contable** (nuevo) |
| 17 | *(no existía)* | **Pendiente de complemento** (nuevo) |
| 18 | *(no existía)* | **N/A** (nuevo) |
| 19 | *(no existía)* | **Completado** (nuevo) |
| 20 | *(no existía)* | **Cancelada** (nuevo) |

La NC ahora llega hasta 20 (antes 12). **"Cancelada" pasó de 11 a 20.**

## 4. Transiciones (`status_train`)

Factura (option 1) y NC (option 2) comparten **el mismo set de 32 transiciones**:

```
2→3, 2→20, 3→4, 3→5, 3→6, 3→20, 4→5, 4→6, 5→7, 5→8, 5→9, 5→10,
6→3, 7→8, 7→10, 8→11, 8→12, 8→13, 9→3, 10→3, 11→12, 11→13,
12→14, 12→15, 12→16, 13→3, 14→15, 14→16, 15→18, 16→3, 17→19, 18→19
```

- Todos los estatus de error reintentan a **3** (En proceso de envío): 6, 9, 10, 13, 16.
- **`17→19`** (Pendiente de complemento → Completado): cuando llega el complemento de pago se cierra el ciclo, tanto factura como NC (Ivan 2026-08-25, "así sincronizamos los estatus").

## 5. Cambios de código (fiscal-api, requieren redeploy)

- **`NC_CANCELADA` 11 → 20** — detección de cancelación NC (WRN7023) + cascada de NCs al cancelar factura.
- **NC de descuento comercial (rebate) nace en 17** (Pendiente de complemento) — constante `NC_CONTABILIZADA(8)` renombrada a `NC_DESCUENTO_PENDIENTE_COMPLEMENTO(17)` en `resolveInitialDocumentStatus`. Antes nacía en 8.
- **Merge conflict resuelto** (`97d9b8b`) en `validateStatusTransition`: se mantuvo la validación **directo contra `status_train` (BD), sin enum** — el lado en conflicto reintroducía `InvoiceStatus.fromCodigo` y habría dado BUS049 al renumerar estatus.
- Los nombres de estatus salen del **catálogo BD** (`resolveStatusName`), no del enum.

## 6. Pendientes (fuera de fiscal-api)

1. **Batch `CreditNoteDownloadBatchService` (Robert):** hacía `3→9` para NC tipo 2; en v1.0(5) no existe esa transición (el 9 es "Error registro contable") y la NC de rebate ahora nace en 17 → el paso queda obsoleto, hay que quitarlo/realinearlo.
2. **Front (Fer):** labels/filtros/colores de estatus a la nueva numeración.
3. **Entrada al 17 para Factura:** el tren no tiene ninguna transición que lleve una factura a 17 (Pendiente de complemento). La NC de rebate nace ahí (register directo). Si una factura normal debe llegar a 17 vía portal, falta definir la transición de entrada (ej. `15→17`); si un batch lo setea directo, no requiere tren.

---

## 7. Plan de pruebas (UAT)

**Host:** `https://uat.fbusinesscenter.com/ppsomx/fiscal`
`idUsuarioActualizacion` = cualquier UUID válido de `core_security.user_data`.

### Paso previo — elegir sujetos reales en UAT
```sql
-- NC por estatus (para elegir orígenes válidos)
SELECT fiscal_uuid, status FROM tenant_fiscal.invoice
WHERE document_type = 'E' ORDER BY status;

-- Facturas por estatus
SELECT fiscal_uuid, status FROM tenant_fiscal.invoice
WHERE document_type = 'I' ORDER BY status;

-- Transiciones válidas del tren
SELECT option_id, source_status, target_status FROM shared_catalogs.status_train
WHERE option_id IN (1,2) ORDER BY option_id, source_status, target_status;
```

### Test 1 — Registrar NC de descuento comercial → nace en 17
`POST /invoices/register` (multipart):
```
file            = <XML de la NC>
tipoNotaCredito = 2
rebateId        = <id de rebate real>
idTransaccion   = TEST-V105-01
```
**Esperado:** HTTP 200. Verificar:
```sql
SELECT fiscal_uuid, status FROM tenant_fiscal.invoice WHERE fiscal_uuid = '<folio NC>';
-- status = 17  (Pendiente de complemento)
```

### Test 2 — Complemento cierra el ciclo: 17 → 19
`PUT /invoices/{fiscalUuid}/status`:
```json
{ "estatusOrigen": 17, "estatusDestino": 19,
  "numeroProveedor": "<num>", "idUsuarioActualizacion": "<uuid>" }
```
**Esperado:** HTTP 200, `estatusNuevoNombre: "Completado"`.
*(Antes de v1.0(5) esta transición no existía → WRN7011/BUS051.)*

### Test 3 — Cancelar NC → 20
Elegir NC en estatus 2 o 3.
`PUT /invoices/{fiscalUuid}/status`:
```json
{ "estatusOrigen": 2, "estatusDestino": 20,
  "numeroProveedor": "<num>", "idUsuarioActualizacion": "<uuid>" }
```
**Esperado:** HTTP 200, `estatusNuevoNombre: "Cancelada"`. Si la NC tiene afectación contable → **WRN7023**.

### Test 4 — Reintento de error → 3
Elegir factura en estatus 16 (Rechazo Contable).
`PUT /invoices/{fiscalUuid}/status`:
```json
{ "estatusOrigen": 16, "estatusDestino": 3,
  "numeroProveedor": "<num>", "idUsuarioActualizacion": "<uuid>" }
```
**Esperado:** HTTP 200, `estatusNuevoNombre: "En proceso de envio"`.

### Test 5 (negativo) — transición no permitida
`PUT /invoices/{fiscalUuid}/status` (Factura, 3→13 no existe):
```json
{ "estatusOrigen": 3, "estatusDestino": 13,
  "numeroProveedor": "<num>", "idUsuarioActualizacion": "<uuid>" }
```
**Esperado:** **BUS051** con nombres del catálogo: `De: 3 (En proceso de envio) a: 13 (Error i213)`.
Confirma que valida contra `status_train` (BD) y toma nombres del catálogo, sin enum.

---

## 8. Verificación post-aplicación (ya corrida en UAT ✓)

```sql
SELECT count(*) FROM shared_catalogs.status_train WHERE option_id = 1;  -- 32
SELECT count(*) FROM shared_catalogs.status_train WHERE option_id = 2;  -- 32
-- Catálogo NC 1..20 con nombres nuevos (ver §3)
```
