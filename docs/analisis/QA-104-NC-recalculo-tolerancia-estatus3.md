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

## Reglas confirmadas por Ivan (2026-06-29)

1. **Monto = SUBTOTAL.** El portal maneja todo sin impuestos. `neto = subtotal_factura −
   Σ(subtotal de TODAS las NCs vinculadas)`. (No total/IVA.)
2. **Sumatoria al momento, todas las NCs.** Confirmado: no hay monto fijo/saldo, se recalcula en vivo
   cada vez (Opción A).
3. **Solo cambia a 3 cuando se cumple la regla** `recepción dentro de tolerancia vs (factura − Σ NC)`.
   Origen: Recibido Parcial (2) → 3. No otros estatus.
4. **Si el neto sobre-corrige** (queda por debajo de la recepción fuera de tolerancia) → **cascada de
   rechazo**:
   - Factura → **1 Rechazo Comercial**, guardando en el **log el motivo**.
   - Las NCs vinculadas → **Canceladas** (Ivan dijo "9"; el código tiene `CreditNoteStatus.CANCELADA`
     = **10** → ⚠️ ver choque #B).
   - La recepción/OC → **Disponible (0)** para que el proveedor vuelva a subir su factura.
   - **Front** muestra confirmación ANTES de ejecutar: `"La factura será rechazada y las notas de
     crédito serán canceladas, ya que el monto total de la factura menos las notas de crédito son
     menor al monto disponible de la recepción, ¿Desea continuar?"` (Ivan lo etiquetó `WRN7032` →
     ⚠️ ese código YA existe para otra cosa, ver choque #A).
5. **El recálculo corre cada vez que el proveedor sube una NC** (al registrar). No al cancelar.

### Lógica final (al registrar cada NC, en `saveRelatedCfdis`)
```
neto = subtotal_factura − Σ(subtotal_NC vinculadas)
diff = |neto − receptionAmount|   (misma tolerancia cat_parameter)
si diff ≤ tolerance        → factura = 3 (En proceso de envío)   [desde 2]
si neto > recepción y fuera → factura se queda 2 (Recibido Parcial)  [aún falta NC]
si neto < recepción y fuera → CASCADA RECHAZO:
        factura = 1 (Rechazo Comercial) + log motivo
        NCs vinculadas = Cancelada (9? / 10?)
        recepción = 0 (Disponible)
        (front confirma antes con el msg WRN7032/nuevo)
```

## Choques detectados (confirmar antes/durante)

**#A — El código WRN7032 ya está usado.** `WRN7032` = "La factura se encuentra previamente
registrada manualmente..." (addenda manual, validado UAT 2026-06-23, [[project_addenda_manual_wrn7032]]).
NO se puede reusar para el mensaje de confirmación de rechazo. Hay que crear un **código nuevo**
(ej. `WRN7034`). Además ese mensaje es un **confirm de front** ("¿Desea continuar?"): el front lo
muestra ANTES; si el usuario acepta, recién ahí el back ejecuta la cascada. Definir si el back lo
devuelve como warning o es 100% front.

**#B — Estatus de cancelación de NC: 9 (RESUELTO).** Ivan tenía razón: el catálogo real
`CatEstatusNotaCredito` dice **9 = Cancelada**, 10 = Borrada. El enum Java `CreditNoteStatus` está
**desactualizado** (tiene 10=Cancelada, sin 9; toda la numeración difiere del catálogo). Igual que la
trampa de factura (enum dice Recibida(3), catálogo dice "En proceso de envió"): **manda el catálogo de
BD, no el enum**. → Para la cascada uso **9 = Cancelada**. Nota: el enum `CreditNoteStatus` debería
limpiarse/alinearse al catálogo en otra tarea (riesgo en `validateStatusTransition` para NC).
Catálogo completo en [[reference_fiscal_status_catalogs]].

## Gap técnico (sigue)
El `reception_id` (UUID) NO está en la factura, solo `addendum.reception_number`. Para re-consultar
el monto de recepción Y para poder ponerla en Disponible (0) en la cascada, se necesita el UUID:
resolver por `reception_number` (si es único) o persistir el UUID al registrar.

## Alcance real (creció)
Ya no es "solo pasar a 3". Es **re-evaluar tolerancia con el neto en cada alta de NC** + **cascada de
rechazo cross-módulo** (factura→1, NCs→cancelada, recepción→disponible) + **mensaje nuevo de front**.
Transiciones a validar en el tren: 2→3 (ya existe) y 2→1 (Recibido Parcial → Rechazo Comercial, ya
existe: `RECIBIDO_PARCIAL → {1,3,18}`).

## Implementación (HECHO, `7600fca` + fixes)
- **PASO 9.7** en `registerInvoice`: al registrar NC → `reevaluarFacturaTrasNc`. Solo actúa si la
  factura está en 2 (Recibido Parcial). neto = subtotal factura − Σ subtotal de TODAS las NCs
  vinculadas (`related_cfdi`). 3 desenlaces: dentro tol → 3; neto<recepción fuera tol → cascada;
  neto>recepción fuera tol → sigue 2.
- **Cascada** `ejecutarCascadaRechazoNc`: factura → 1, NCs → 9 (Cancelada), recepción → 0
  (Disponible) + motivo en bitácora. Gateada por `confirmarCancelacionNc`: si false y aplica →
  `WRN7034` (throw → rollback de la NC, no se registra); si true → ejecuta.
- **Nuevo param** `confirmarCancelacionNc` (boolean, default false) en `POST /invoices/register`
  (controller + service + interface). **Nuevo** `ReceptionRepository.findByReceptionNumber`.
  **Nuevo** `WRN7034`. Tolerancia reusa `cat_parameter` (helper `resolveTolerance`).
- **Gap resuelto**: el receptionId no está en la factura → se resuelve la recepción por
  `addendum.reception_number` (`resolveReceptionDeFactura`).

## Pruebas (VALIDADO LOCAL + UAT 2026-06-29, PAC omitido)
**UAT 2026-06-29** (factura_mayor real `fiscal 31343515`, recepción 999056 amount=5000, tol=40):
NC subtotal 4000 → neto 5000 → factura **2→3** ✔. NC subtotal 4200 → neto 4800: sin confirmar →
**WRN7034** HTTP 400 + rollback (factura sigue 2, NC no registra) ✔; con `confirmarCancelacionNc=true`
→ factura **1**, NC **9**, recepción **0** ✔. Mensaje WRN7034 sembrado en catálogo UAT.

### Detalle local (mismo resultado)
Escenario: recepción=6000, factura_mayor subtotal=9000 → status 2 (WRN7030). Tolerancia=40 (monto).
| Caso | NC subtotal | neto | Resultado | ✓ |
|---|---|---|---|---|
| Dentro de tolerancia | 2980 | 6020 (diff 20≤40) | factura 2 → **3** | ✔ |
| Sobre-corrige, sin confirmar | 3100 | 5900 (<6000, diff 100) | **WRN7034** HTTP 400 + rollback (factura sigue 2, NC no registra) | ✔ |
| Sobre-corrige, confirmando | 3100 | 5900 | factura → **1**, NC → **9**, recepción → **0** | ✔ |

**Nota de entorno (solo local)**: `SatCatalogService.getActiveCatalogValues` lee de **util-api** (HTTP),
no del DB. Para validar NC en local hay que levantar util-api (:3712) y apuntar fiscal con
`--utils.api.url=http://localhost:3712/api` (el `/api` lo agrega el bff en UAT; directo al util-api
hace falta el prefijo, si no → 404 → BUS058). En UAT no aplica.

## Resumen
- Neto = subtotal factura − Σ subtotal NC, recalculado en vivo en cada alta de NC.
- 3 desenlaces: dentro tol → 3; sigue faltando → 2; sobre-corrige → cascada rechazo (1 + NC cancel +
  recepción disponible + confirm front).
- Pendiente confirmar: código del msg (WRN7032 chocado → nuevo) y estatus cancel NC (9 vs 10).
- Gap: receptionId no persistido en la factura.
