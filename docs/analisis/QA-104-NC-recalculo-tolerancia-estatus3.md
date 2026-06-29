# Fila 104 — Recalcular tolerancia con (factura − NCs) y pasar factura a estatus 3

> Matriz xlsx v8, fila 104. Responsable David (Fiscal / Notas de crédito). Alto. Sin empezar.
> Análisis de diseño antes de codear. Hermano del tema tolerancia: [[project_tolerancia_recepcion_nc]].

## Requerimiento (texto literal)
> "Cambiar el estatus de la factura al estatus **3 (En proceso de envío)** cuando el valor de la
> recepción sea **igual o esté dentro del valor de la tolerancia** comparando la **(factura − NCs)**."

Lectura: al registrar la factura, si quedó **Recibido Parcial (2)** porque el subtotal excedía la
recepción fuera de tolerancia, al ir aplicando **notas de crédito** el monto efectivo baja. Cuando
`factura − Σ NCs` cae **dentro de tolerancia** vs la recepción, la factura debe pasar a **3**.

## Cómo funciona hoy (estado actual)

### Evaluación de tolerancia — al registrar la FACTURA
`validateImporteTolerance` ([InvoiceServiceImpl.java:767](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java#L767)):
- `subtotal` = del XML de la factura (`invoiceDto.getSubTotal()`).
- `receptionAmount` = `finanzasApiService.getReception(receptionId).getAmount()` → tabla
  `tenant_finance.reception` (por `reception_id` UUID que llega como parámetro de `/register`).
- `tolerance` = `cat_parameter` (id 3 monto / id 4 porcentaje / si ambos off = exacto 0).
- `diff = |subtotal − receptionAmount|`. Si `diff > tolerance`: factura>recepción → **2** (WRN7030);
  factura<recepción → **1** (WRN7031). Si no → **3 Recibida**.
- **Solo se evalúa para facturas** (documentType I), y **solo al registrar**. No se re-evalúa después.

### Vínculo NC → factura
- Al registrar una **NC** (documentType E), `saveRelatedCfdis`
  ([:1214](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java#L1214))
  lee `CfdiRelacionados` del XML, busca la factura padre por `fiscal_uuid`, valida que sea tipo I y
  que `NC.total ≤ factura.total` (BUS061), y guarda la relación en **`related_cfdi`**
  (`invoice_uuid`=NC, `related_invoice_uuid`=factura, `relation_type`).
- **La NC NO toca el estatus de la factura padre hoy.** La NC siempre queda status=1.

### ¿La resta se persiste? → **NO**
Punto central (responde la duda de David):
- **No existe** ninguna columna/tabla de neto/saldo/monto aplicado para la factura. `InvoiceEntity`
  tiene `total`, `subtotal`, `discount`, `status` — **nada de saldo acumulado**.
- Cada NC es un `InvoiceEntity` independiente con su propio `total`/`subtotal`. `related_cfdi` solo
  guarda el **vínculo UUID**, sin montos ni agregación.
- `reception.amount` **NO se modifica** al cargar una NC.
- Por tanto **la resta no queda en BD**. El neto `factura − Σ NCs` hay que **calcularlo en vivo**
  cada vez, sumando **todas** las NCs vinculadas (`related_cfdi.findByRelatedInvoiceUuid(facturaId)`).

## Decisión de diseño: recalcular vs persistir saldo

| | A — Recalcular en vivo (recomendado) | B — Persistir saldo acumulado |
|---|---|---|
| Cómo | Cada NC: sumar **todas** las NCs vinculadas, neto = factura − Σ NCs | Columna `saldo` en factura; restar **solo la última** NC |
| Migración | Ninguna | ALTER TABLE + backfill |
| Idempotencia | ✅ Reprocesar misma NC no doble-cuenta (related_cfdi es la fuente) | ❌ Riesgo doble resta si se reprocesa |
| Cancelar NC | ✅ Al recalcular ya no la suma | ❌ Hay que sumar de vuelta al saldo (frágil) |
| Complejidad | Baja, stateless | Media, maneja estado |

**Recomendación: Opción A.** Tu intuición ("solo restar la última") es la B, pero como la resta **no
se persiste**, B obliga a crear y mantener un saldo — y se rompe si una NC se cancela o se reprocesa.
A es stateless: sumar todas las NCs cada vez siempre da el neto correcto, sin migración.

## Dónde se implementa (Opción A)
En `saveRelatedCfdis`, después de guardar la relación
([~:1282](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java#L1282)):
1. `relatedCfdiRepository.findByRelatedInvoiceUuid(factura.invoiceUuid)` → todas las NCs.
2. Sumar montos de las NCs (ver duda #1: subtotal vs total).
3. `neto = factura.subtotal − Σ NC`.
4. Obtener `receptionAmount` de la factura (ver **gap** abajo).
5. Reusar la misma lógica de tolerancia (`cat_parameter`).
6. Si `factura.status == 2` (Recibido Parcial) **y** `|neto − receptionAmount| ≤ tolerance` →
   `factura.setStatus(3)` + `save`. (Transición 2→3 ya permitida en `InvoiceStatus`, sin cambio enum.)

### Gap técnico: el receptionId NO está en la factura
La tolerancia necesita `reception.amount`, que se obtiene por `reception_id` (UUID). Pero en la
factura **solo se guarda `addendum.reception_number`** (el número, no el UUID). Opciones:
- (a) Agregar `findByReceptionNumber` en el repo de recepción y resolver por número, o
- (b) Persistir `reception_id` (UUID) en `addendum`/`invoice` al registrar.
Recomiendo (a) si `reception_number` es único; si no, (b) con columna nueva (mini-migración).

## Dudas para Ivan (antes de codear)
1. **¿Qué monto se resta?** Hoy la tolerancia compara **subtotal** de factura vs recepción. La NC
   ¿se resta por **subtotal** o por **total** (con IVA)? (BUS061 usa total; F93 dice mostrar subtotal.)
   Lo lógico para que cuadre con la recepción es **subtotal − Σ subtotal NC**, pero confirmar.
2. **¿Suma de todas o solo la última?** Confirmado por código: como no se persiste saldo, hay que
   sumar **todas** las NCs vinculadas. (Salvo que se quiera persistir saldo — no recomendado.)
3. **¿Desde qué estatus se dispara?** Solo desde **Recibido Parcial (2) → 3**, ¿o también aplica a
   otros? (Rechazo Comercial (1) no tiene transición a 3 en el tren actual.)
4. **¿Y si la NC deja el neto POR DEBAJO de la tolerancia** (sobre-corrige, neto < recepción fuera de
   tolerancia)? ¿Se queda en 2, pasa a Rechazo Comercial (1), o no importa? El requerimiento solo
   habla del caso "entra en tolerancia → 3".
5. **¿El recálculo corre al registrar la NC** (mi propuesta), o también debería correr al **cancelar**
   una NC (revertir el estatus)? Hoy la NC se puede cancelar (CreditNoteStatus.CANCELADA).

## Resumen
- La resta **no se persiste** → recalcular en vivo sumando todas las NCs (Opción A, sin migración).
- Transición 2→3 ya existe en el enum; el trigger va en `saveRelatedCfdis` al registrar la NC.
- Gap: falta el `reception_id` en la factura para re-consultar el monto de recepción.
- 5 dudas para Ivan antes de codear (sobre todo subtotal-vs-total y qué pasa si sobre-corrige).
