# STM-1309 — Análisis técnico

> Análisis aparte de la transcripción ([MXSTM-1309.md](MXSTM-1309.md)). Fecha: 2026-06-01 · David Montes.
> Contexto: STM-1309 está `Done`, fue implementado por g_dop02 (Robert) en el proyecto `APP03022-mrch.batch.somx.invoice-status-sync`. David lo heredó. Este análisis valida implementación vs spec y detecta el conflicto con fiscal-api actual.

## 1. Qué pedía el jira

Proceso batch (07:30 AM, retry 2×30min, ≤15 min) que:
1. Consulta facturas por estatus en FBC (servicio del back fiscal — URL dejada como `XXX` en el jira).
2. Por cada factura, consulta el estatus real en BD locales (SAP / SAPITO Oracle / i213) según el "Panel de Queries por Escenario".
3. Avanza el estatus en FBC: `6→7→8→9→10→11` (+13 rechazo, +16 no enviada).
4. Registra trazabilidad en SODIMAC_BATCH_DEV (`CtrlProcesoCab/Det/Elemento/ctrlLog` + `catCatalogo`/`adminCatalogo`).

## 2. Implementación de Robert vs spec

### ✅ Fiel al spec
- **Máquina de estados**: el enum `InvoiceFlowStatus` copia EXACTO el Panel de Queries (6=registro SAPITO, 7=envío i213, 8=enviada i213, 9=contabilizar SAP, 10=pago, 11=pagada, 13=rechazo, 16=no enviada). Robert NO inventó la numeración — viene del jira.
- **Queries**: `Envios_Ap` (CODIGO_PROVEEDOR/NUMERO_UUID/FLAG_ENVIADO), SPs `i123_Valida_Documento_AP` / `i213_Valida_Documento_Pagado_AP` — calcan el jira.
- Scheduler 07:30, retry 2×30min, ≤15 min, cifras control before/after.

### ❌ Desviaciones del spec
- **Tablas de control**: el jira pide `CtrlProcesoCab/Det/Elemento/ctrlLog`. Robert escribió en tablas **inventadas** `CtrlEnlace / CtrlEnlace_CifrasControl / CtrlEnlace_Comprobante_ControlDocumento / CtrlEnlaceLog`. Desviación directa del spec.
- **Contrato del servicio fiscal**: el jira dejó las URLs como `URL: XXX` ("Ajustar el servicio en el back fiscal para consultar por estatus"). Robert **adivinó** el contrato (`GET /api/facturas?estatus=`) que NO coincide con fiscal-api real.

### 🐛 Bugs detectados (ver VALIDACION_NONMOCK / ANALISIS_BATCHES_ROBERT)
- `captureCurrentCifras()` lee tabla `invoices` inexistente → cifras siempre 0.
- `parseProveedorId()` revienta con proveedores no numéricos (en data real es numérico, se mitiga).

## 3. EL CONFLICTO: STM-1309 vs tren de estatus actual de fiscal-api

El modelo de estatus de STM-1309 (dic-2025, reporter Ivan) **NO empata** con el enum `InvoiceStatus` vigente de fiscal-api (STM-410/719):

| Código | STM-1309 | fiscal-api HOY |
|---|---|---|
| 7 | Pendiente envío a i213 | **Pendiente de Pago** |
| 8 | Factura enviada a i213 | **Pagado** |
| 9 | Pendiente contabilizar SAP | Pendiente de complemento |
| 10 | Pendiente Pago | Completado |
| 11 | Pagada | Rechazo Contable |
| 13 | Rechazo contable | Pago Manual |
| 16 | No enviada i213 | (no existe) |

Consecuencia: el PUT de fiscal-api **valida transiciones** (STM-410). Las que genera invoice-sync en términos FBC (ej. `3→3` interno 8→9, `3→7` interno 9→10) **serían rechazadas con WRN7011** salvo `7→8`. Ver [DRIFT_INVOICE_SYNC_VS_FISCAL_API.md](../../analisis/DRIFT_INVOICE_SYNC_VS_FISCAL_API.md).

## 4. Pregunta para Ivan (reporter de STM-1309)

> STM-1309 (que tú reportaste, `Done`) define estatus 7=Pendiente envío i213, 8=Enviada i213, … pero el tren de estatus actual de fiscal-api usa 7=Pendiente de Pago, 8=Pagado. Son dos modelos oficiales que no empatan. **¿Cuál es el vigente y contra cuál debe quedar invoice-sync?**

## 5. Recomendación

`invoice-status-sync` = **mantener como base, re-trabajar**:
- Alinear el modelo de estatus al tren vigente (definir con Ivan cuál).
- Fetch → `POST /invoices/search` de fiscal-api (rfcEmisor opcional = todos los proveedores; ya probado por batch.fiscal-download).
- Mapeo de transiciones respetando el tren (evitar 3→3 / 3→7 si no están catalogadas).
- Control → migrar de `CtrlEnlace*` a `CtrlProcesoCab/Det/Elemento/ctrlLog` (cumplir spec).
- Reusable sin tocar: estructura hexagonal, adapters SAP/SAPITO(Oracle)/i213 (validados reales), scheduler, retry.

Estado validación local: 8/10 transiciones contra BD reales (SAP + i213). SAPITO host→Oracle bloqueado solo por NAT Docker (no es bug). Ver [VALIDACION_NONMOCK_INVOICE_SYNC.md](../../analisis/VALIDACION_NONMOCK_INVOICE_SYNC.md).
