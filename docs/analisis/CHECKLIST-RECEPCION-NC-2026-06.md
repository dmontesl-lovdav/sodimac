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
| A1 | Recepción → Consumida cuando factura dentro tolerancia / 100% | 54, 55 | ✅ Implementado + probado local (0→1) |
| A2 | Recepción → Consumida cuando factura mayor (fuera tol) | 56 | ✅ Implementado + probado local (0→1) |
| A3 | Recepción NO se toca cuando factura menor (fuera tol) | 57 | ✅ Implementado + probado local (queda 0) |
| B | Rechazo Comercial (factura < recepción): NO subir PDF al bucket (XML sí en BD) | (32/48 contexto) | ✅ Implementado (skip GCS por status 1) |
| C | WRN7030: agregar "para dar inicio al proceso de pago" en la alerta de parcial | — | ✅ Implementado + probado local |
| D | #6 Tipo NC (1 Ajuste Recepción / 2 Descuento Comercial) en addenda | 16, 43 | ✅ Implementado + probado local (param→columna) |
| OK | Fecha del sistema en "Fecha Recepción" (= createdAt) | 48, 32 | ✅ Hecho (fix createdAt), en validación QA |

> **Implementado 2026-06-22 (local).** Columna `tenant_fiscal.addendum.tipo_nota_credito` aplicada
> en BD local; migración versionada en `migration/QA-2026-06_addendum_tipo_nota_credito.sql`
> (correr en UAT). `ReceptionEntity` ahora mapea `status`. Decisiones Ivan: XML sí se guarda en BD
> (solo se omite el bucket en rechazo comercial); columna nueva en addendum se crea (no contestó la
> pregunta, se procede). Pendiente: deploy UAT + correr migración + validar QA filas 16,42,43,54-57.
> Nota: la fila 42 (condición de pago NC) sigue pendiente — falta catálogo `CatCondicionPagoValidoNc`.

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
