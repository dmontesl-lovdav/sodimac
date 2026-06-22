# Checklist — puntos nuevos recepción / NC (Ivan + QA matriz) 2026-06-22

> Puntos finos que pidió Ivan tras confirmar el flujo de tolerancia, cruzados con la hoja
> "Listado Issue Abiertos" del `QA_Matriz_Pruebas_Portal_FBC_Mexico_v1.0.xlsx`.
> Estrategia: igual que los issues de Fer — ir revisando uno por uno.
> Hermano: [QA-FER-2026-06-issues-search.md](QA-FER-2026-06-issues-search.md).

## Contexto: qué YA está hecho vs qué es nuevo

Lo desplegado (tolerancia) cambia el estatus de la **FACTURA** (1 Rechazo Comercial / 2 Recibido
Parcial / 3 Recibida) + alerta WRN7030/7031. **NO** toca el estatus de la **RECEPCIÓN** (finanzas).
fiscal-api solo **lee** la recepción (`ReceptionEntity` sin columna status, `ReceptionRepository`
vacío, `FinanzasApiService.getReception`). Los puntos nuevos son sobre la **recepción** y la **NC**.

## Regla de estatus de recepción (confirmada por xlsx filas 54-57)

| Caso factura vs recepción | Status factura | Estatus recepción |
|---|---|---|
| Dentro tolerancia (incl 100%) | 3 Recibida | → **Consumida** |
| Mayor, fuera tolerancia | 2 Recibido Parcial (pide NC) | → **Consumida** |
| Menor, fuera tolerancia | 1 Rechazo Comercial | **NO se toca** (queda Disponible) |

Regla corta: recepción → **Consumida** siempre que factura ≥ recepción; se queda Disponible solo
si factura < recepción fuera de tolerancia.

## Checklist

| # | Punto | Fila xlsx | Estado |
|---|---|---|---|
| A1 | Recepción → Consumida cuando factura dentro tolerancia / 100% | 54, 55 | ✅ VALIDADO UAT (0→1) |
| A2 | Recepción → Consumida cuando factura mayor (fuera tol) | 56 | ✅ VALIDADO UAT (0→1) |
| A3 | Recepción NO se toca cuando factura menor (fuera tol) | 57 | ✅ VALIDADO UAT (queda 0) |
| B | Rechazo Comercial (factura < recepción): NO subir PDF al bucket **ni guardar XML** | 57 | ✅ VALIDADO UAT (xml_content y addendum_content NULL) |
| C | WRN7030: agregar "para dar inicio al proceso de pago" en la alerta de parcial | 56 | ✅ VALIDADO UAT |
| D | #6 Tipo NC (1 Ajuste Recepción / 2 Descuento Comercial) en addenda; 0 en facturas | 16, 43 | ✅ VALIDADO UAT (NC=2, factura=0) |
| OK | Fecha del sistema en "Fecha Recepción" (= createdAt) | 48, 32 | ✅ VALIDADO UAT |

> **VALIDADO EN UAT 2026-06-22.** Pruebas con CFDIs reales (sesiones/test): factura_exacto
> (recepción 0→1), factura_mayor (WRN7030 + 0→1), factura_menor (Rechazo Comercial: recepción
> queda 0, xml_content y addendum_content NULL, tipo 0), NC DMC (tipoNotaCredito=2 persistido).
> Commits: `23898c0` (features) + `65e8562` (retro: XML no se persiste en rechazo + default 0).
> Migración `migration/QA-2026-06_addendum_tipo_nota_credito.sql` corrida en UAT (columna + DEFAULT
> '0' + backfill facturas).
>
> **Decisiones Ivan (daily 2026-06-22):**
> - Rechazo Comercial: el XML **no** se guarda (ni en BD ni bucket); el desglose en tablas basta.
> - `tipo_nota_credito`: la addenda aplica a facturas y NC → en facturas queda **0** (no null).
> - Columna nueva en addendum: se crea (no contestó la pregunta, se procedió).
>
> **Único pendiente:** fila **42** (filtrar condición de pago para alta de NC) — falta catálogo
> `CatCondicionPagoValidoNc` (definición Ivan).

## Bloqueantes / dudas a Ivan

1. **A1-A3 — ¿cómo cambia fiscal el estatus de la recepción?** fiscal-api no es dueño de
   `tenant_finance.reception` y la entidad no tiene columna status. ¿finanzas-api expone endpoint
   para marcar Consumida? ¿Catálogo de estatus de recepción tiene "Consumida" y qué valor?
   (Nota: xlsx fila 68 dice recepción se registra "0-Disponible" / "1-Consumida".)
2. **B — Rechazo Comercial + XML:** ¿el XML tampoco se guarda en la columna `xml_content` de BD,
   o solo no va a GCS? Si no hay XML, ¿con qué datos queda la factura registrada en BD?
3. **D pregunta 2 — columna nueva `tipo_nota_credito` en `addendum`** (junto a id proveedor +
   número documento que ya existen). ¿OK agregar el campo? (Pregunta 1 ya respondida: el front
   manda el tipo NC como parámetro en la addenda.)

## Notas QA matriz (verificación de estatus marcados por David)

Revisión 2026-06-22 de filas con responsable David marcadas "En Validación":
- Fila 48 ✅ correcta (fix createdAt).
- Filas 54, 55, 56, 16 ❌ marcadas "En Validación" pero **sin nada construido** → deberían ser
  Pendiente / En desarrollo. Son las features nuevas (A, D).
- Fila 57 ⚠️ coincide por accidente (no se toca la recepción), parte de la misma feature A.
