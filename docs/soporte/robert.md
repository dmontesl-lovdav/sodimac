# Robert - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-06-23 | Handoff: Robert retoma los 3 batches del flujo fiscal

**Contexto**: Robert (autor original, `g_dop02`) retoma los batches que David tuvo reasignados.
Se le pasó por Teams el handoff + zip limpio (`C:\Users\dmont\Downloads\batches-robert-clean.zip`,
3 proyectos sin target/.md/claude).

### Los 3 proyectos
| Proyecto | Jira | Tech | Puerto |
|---|---|---|---|
| `APP03022-mrch.batch.somx.fiscal-download` | STM-719 | Java 8 / SB 2.7.18 / SQL Server | — |
| `APP03022-mrch.batch.somx.invoice-status-sync` | STM-1309 (epic STM-336) | Java 17 / SB 3.2 / hexagonal | 8085 |
| `APP03022-mrch.batch.somx.rebate-agreements-sync` | sin jira en código (epic STM-336) | Java 17 / SB 3.2 / hexagonal | 8086 |

### Flujo funcional (tren de estatus de factura, catálogo CatEstatusFactura)
**fiscal-download** (descarga + desglose CFDI): 3 (En proceso de envío) → 4 (En proceso de descarga)
→ 5 (Desglose de factura). Error desglose → 16 (Estructura inválida); reintento 16→5. XML faltante
se queda en 4. Solo path Factura (NC modelo viejo a propósito).
**invoice-status-sync** (post-desglose: SAPITO→i213→SAP→pago): 7 (Pendiente registro FOSITO) → 8
(Pendiente envío i213) → 9 (Factura enviada a la i213) → 10 (Pendiente contabilizar) → 11 (Pendiente
de Pago) → 12 (Pendiente de Complemento). Re-sync estatus a FBC. Trigger `POST /api/v1/batch/sync-status/trigger`.
**rebate-agreements-sync**: acuerdos rebate desde Azure → `RebateAcuerdosTemp`. Trigger `POST /api/sync/full`.
Handoff fiscal-download → invoice-sync alrededor del 5→7.

### Estado (lo hecho mientras David los tuvo)
- Los 3 recalibrados al tren v1.0 (códigos 1-18).
- invoice-sync: control migrado a `ctrlProcesoCab/Det/Elemento/ctrlLog` (antes `CtrlEnlace*`
  inventado); SIN MOCK, ligado a fiscal-api real (`POST /invoices/search` + `PUT /invoices/{uuid}/status`).
  Validado end-to-end: 30 procesadas, 15 transiciones reales, 0 errores.
- fiscal-download: error desglose recalibrado a 16 (antes 14→6); validado 3→4, 4→5, 5→16, 16→5.
- rebate-sync: corregido NPE fallback (`@Retry` sobre proxy CGLIB) + `RebateDataLoadService`
  (TRUNCATE+INSERT transaccional real).

### Ambientación local
- invoice-sync/rebate: perfil `local`, SQL Server Docker host port 1434→1433. Setup
  `invoice_sync_local_setup.sql` (crea `Envios_Ap` + SPs i213). fiscal-download: Java 8 / SB 2.7.
- Build `mvn clean package`, correr perfil `local`.
- SAPITO 100% verde necesita Oracle real en red Docker; si no, falla solo por NAT (ORA-12514) =
  artefacto local, NO bug. `Dockerfile`/`application-docker.yml` = SOLO testing.

### Pendientes / validar con Ivan
1. invoice-sync ¿reemplaza o convive con fiscal-download?
2. Fuente real de cifras por estatus (hoy cuenta vía FBC; antes apuntaba a tabla `invoices` inexistente).
3. rebate-sync: green-path data real Azure (api-key al adapter).
4. Deploy UAT tren v1.0 (`status_train` + `chk_invoice_status` 1-18).

**Docs**: `docs/analisis/ANALISIS_BATCHES_ROBERT.md`, `VALIDACION_NONMOCK_INVOICE_SYNC.md`,
`DRIFT_INVOICE_SYNC_VS_FISCAL_API.md`, `docs/jiras/STM-1309/`, `docs/jiras/STM-336/`, `docs/jiras/STM-719/`.
Memoria: [[project_robert_batches_adopted]]. Sesión: `sesiones/robert/20260623-robert.txt`.

### Dudas de Robert (se van agregando)
_(pendiente — David irá pasando las preguntas)_

---
