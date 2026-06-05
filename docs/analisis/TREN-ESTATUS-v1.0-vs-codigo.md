# Tren de Estatus Portal FBC v1.0 (Ivan) vs código actual

> Fuente: `sesiones/tren-estatus/Tren_Estatus_Portal_FBC_v1.0.xlsx` (hoja `Tren_Factura`) + respuesta de Ivan 2026-06-02.
> Contexto: a raíz del punto QA "marca error al cancelar la factura", Ivan entregó el tren de estatus oficial v1.0, que **renumera y remodela** el catálogo actual y **elimina el estatus de Addenda**.
> Última actualización: 2026-06-02.

## Decisión de negocio de Ivan (cancelación)

- Cancelar factura = estatus **"Rechazo Comercial"**.
- Permitido **solo desde "Recibido Parcial"** (source 2 → Rechazo Comercial).
- Rechazo para re-contabilizar = "Rechazo Contable", desde "Pendiente de contabilizar".
- **Addenda ya no se maneja**: "pendiente de Addenda ya no se considera porque se relaciona directo con la recepción". → elimina estatus Pendiente Addenda + validación BUS048.

## Comparación Factura — código actual (`InvoiceStatus.java`) vs Tren v1.0

| Concepto | Código HOY | siguientes HOY | Tren v1.0 | siguientes v1.0 |
|---|---|---|---|---|
| Rechazo Comercial | 0 | {} | **1** | N/A |
| Pendiente Addenda | 1 | {2,3,13} | **ELIMINADO** | — |
| Recibido Parcial | 2 | {3,13} | 2 | {1,3,17} |
| En proceso de envio (≈Pend. Contab. viejo) | 3 | {4} | 3 | {4} |
| En proceso de descarga | 4 | {5,11,14} | 4 | {5} |
| Desglose de factura | 5 | {6,11} | 5 | {7,16} |
| Error en el desglose | 14 | {3} | 6 | (sin siguiente) |
| Pendiente registro en SAPITO | — | — | 7 | {8} |
| Pendiente envío a i213 | 6 | {7,11} | 8 | {9} |
| Factura enviada a i213 | — | — | 9 | {10,17} |
| Pendiente de contabilizar | — | — | 10 | {11,14} |
| Pendiente de Pago | 7 | {8} | 11 | {12} |
| Pendiente de complemento | 9 | {10} | 12 | {13} |
| Completado | 10 | {} | 13 | N/A |
| Rechazo Contable | 11 | {7} | 14 | {8} |
| No valido fiscal | 12 | {} | 15 | N/A |
| Estructura invalida | — | — | 16 | {5} |
| Error envio i213 | — | — | 17 | {8} |
| Pago Manual | 13 | {} | 18 | N/A |

Nota: el Excel trae otras hojas/módulos (Recepción, Carta Porte, Pagos, Descuento comercial) con su propia numeración — fuera de alcance de fiscal-api salvo Factura.

## Inconsistencias a confirmar con Ivan

- Fila "Recibido Parcial" (2) lista siguiente "**17 - Pago Manual**", pero en el catálogo código 17 = "Error envio i213" y 18 = "Pago Manual". Las etiquetas de "estatus siguiente" no cuadran con la columna de código.

## Impacto técnico (si se adopta v1.0 completo)

1. `InvoiceStatus.java` (fiscal-api) — renumerar + agregar estatus SAP nuevos (7,8,9,16,17) + quitar Pendiente Addenda.
2. Catálogo `shared_catalogs.status_train` — reemplazar filas option_id=1 (local + UAT + seed repo).
3. **Migración de datos**: facturas existentes con códigos viejos → mapear a nuevos. Sin esto, el significado de cada status cambia.
4. **batch** (`SincronizacionEstatus`) — usa estos códigos; alinear.
5. finanzas-api + frontend (labels/colores de estatus) — alinear.
6. Registro: eliminar marca "RES005 pendiente addenda" + validación BUS048 en updateInvoice.

## Caminos

- **Path A (mínimo, desbloquea QA ya)**: en numeración ACTUAL, habilitar cancel Recibido Parcial(2) → Rechazo Comercial(0) en enum + status_train; relajar BUS048. Quirúrgico.
- **Path B (remodel completo)**: adoptar Tren v1.0 — nuevo JIRA/epic, cross-módulo, con migración de datos. Coordinar Ivan + finanzas + batch.

## IMPLEMENTADO 2026-06-05

Todas las preguntas resueltas y código desplegado a `dmontes`.

| Punto | Resolución |
|---|---|
| Estatus inicial registro | **3 (Recibida)** — confirmado con David. NC mantiene 1 hasta alineación futura. |
| Inconsistencia Excel Recibido Parcial(2) → 17/18 | Ivan corrigió Excel: es **18 (Pago Manual)** |
| Addenda eliminada como requisito | BUS048 removido, `validateAddenda` eliminado del registro |
| Estatus SAP (7,8,9,16,17) | Los setea el batch — no tocar en backend manual |
| Migración data existente | Script pendiente antes de deploy UAT (`sesiones/sql/sync-status-train-v1.0.sql` solo toca `status_train`, no facturas existentes) |
| Cross-módulo (batch, FE, finanzas) | Pendiente alinear — fiscal-api implementado, aviso a Ivan necesario |

### Archivos modificados

- `InvoiceStatus.java` — renumerado completo 1-18, sin Pendiente Addenda
- `FiscalMessageCode.java` — BUS057 agregado (tolerancia importe)
- `InvoiceController.java` — `/register` + params `receptionId`, `supplierNumber`, `purchaseOrderNumber`
- `InvoiceServiceImpl.java` — lógica tolerancia + addenda estructurada + status inicial 3 + BUS048 eliminado
- `ReceptionEntity.java` + `ReceptionRepository.java` — acceso directo a `tenant_finance.reception`
- `FinanzasApiService` + `Impl` — abstracción sobre el repositorio
- `sesiones/sql/sync-status-train-v1.0.sql` — script para actualizar catálogo en UAT
