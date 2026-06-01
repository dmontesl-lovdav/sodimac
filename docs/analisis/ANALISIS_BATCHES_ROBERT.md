# Análisis batches heredados de Robert — invoice-status-sync / rebate-agreements-sync

> Fecha: 2026-06-01 · Autor: David Montes · Contexto: Ivan reasignó a David ambos batches de Robert. Doc = revisión funcional + validación local + bugs.

## 1. Resumen ejecutivo

Dos batches Spring Boot 3.x / Java 17, arquitectura hexagonal, generados por Robert (probablemente con IA, sin handoff). Se clonaron al workspace con la nomenclatura del proyecto, se levantaron en local (Docker) y se validó su ejecución.

| Proyecto | Puerto local | Stack | Trigger | Estado validación |
|---|---|---|---|---|
| `APP03022-mrch.batch.somx.invoice-status-sync` | 8085 | SB 3.2 / Java 17 / hexagonal | `POST /api/v1/batch/sync-status/trigger` + cron 07:30 | ✅ green end-to-end |
| `APP03022-mrch.batch.somx.rebate-agreements-sync` | 8086 | SB 3.2 / Java 17 / hexagonal | `POST /api/sync/full` + cron 03:00 | ✅ SUCCESS (fuente Azure vacía) |

## 2. Qué hacen (nivel funcional)

### invoice-status-sync
Avanza facturas por la máquina de estados FBC, consultando sistemas legacy y re-sincronizando al portal.

```
estatus 6 → 7 → 8 → 9 → 10 → 11   (+13 rechazo, 16 no-enviada)
SAPITO → i213 → SAP contab → pago
```

- Lee facturas por estatus desde FBC (mock o BFF Fiscal real).
- Decide transición consultando: `Envios_Ap` (SAP/SAPITO) + SPs i213 (`i123_Valida_Documento_AP`, `i213_Valida_Documento_Pagado_AP`).
- Estatus finales (9,10,11,13) → push a FBC vía `PUT /invoices/{uuid}/status` (mapeo interno→FBC: 9→3, 10→7, 11→8, 13→11).
- Trazabilidad en SODIMAC_BATCH_DEV.

### rebate-agreements-sync
Descarga acuerdos comerciales (rebates) desde API externa Azure y los carga full-sync a BD local.

- Descarga contratos MX paginados + agreements por contrato (API `rebate-management-prd...azure.com`).
- Mapea Contract+Agreement → `RebateAcuerdosTemp`.
- Carga TRUNCATE+INSERT transaccional (la transacción gigante fue removida por bloqueos de 15-20 min; sólo el load queda en tx corta).
- Trazabilidad en tablas de control.

## 3. Relación con batch.fiscal-download (el de David)

**NO se reemplazan. Son etapas secuenciales del mismo flujo de factura.**

```
fiscal-download (David):    3 → 4 → 5      (descarga + desglose CFDI)   [SB 2.7 / Java 8]
                                  ↓ handoff
invoice-status-sync (Robert): 6 → 7 → 8 → 9 → 10 → 11   (SAPITO→i213→SAP→pago)
```

- Rangos de estatus disjuntos → no se pisan.
- Comparten máquina de estados FBC + bases SODIMAC_SAP_DEV / SODIMAC_BATCH_DEV.
- `rebate-agreements-sync` = dominio aparte (rebates), sólo comparte infra + patrón hexagonal.

**Analogía:** factura = paquete en aduana. fiscal-download = recepción (desempaca). invoice-status-sync = despacho/pago (lleva hasta pagado y avisa al mostrador FBC). rebate-sync = bodega aparte que actualiza el catálogo de convenios.

## 4. Validación local (evidencia)

Entorno: Docker `sodimac-mssql` (host port **1434**→1433), `sodimac-pg`. Perfil `local` en cada proyecto.

### invoice-status-sync — green end-to-end
- Mock FBC ON. Dependencias externas emuladas en SQL Server local (sin Oracle): tabla `Envios_Ap` sembrada + 2 SPs i213 (`CODE=1`).
- Resultado trigger: `10 processed, 8 success, 2 skipped (estatus 7), 0 errors`.
- Transiciones reales: 6→7, 8→9, 9→10, 10→11. Sync FBC mock: 9→10 (FBC 3→7), 10→11 (FBC 7→8).
- Persistencia: `CtrlEnlace`=1 ejecución, 10 transiciones, 2 cifras.

### rebate-agreements-sync — SUCCESS, fuente vacía
- API Azure alcanzable (HTTP 200) pero devuelve `{"data":[],"pagination":{"total":0}}` → 0 contratos MX.
- Run: `status=SUCCESS, contracts=0, agreements=0, loaded=0` (id 4005).
- `RebateAcuerdosTemp` auto-creada por Hibernate (ddl-auto:update). Trazabilidad escrita.

## 5. Bugs / hallazgos (pendientes)

| # | Proyecto | Hallazgo | Impacto |
|---|---|---|---|
| 1 | invoice-sync | Inventó esquema control `CtrlEnlace*` — NO usa convención `ctrlProceso*` | Divergencia de trazabilidad entre los 3 batches |
| 2 | invoice-sync | `captureCurrentCifras()` lee tabla `invoices` que ningún script crea | Cifras BEFORE/AFTER siempre en 0 |
| 3 | invoice-sync | `parseProveedorId("PROV001")` → NumberFormatException → manda `Provider: 0` a FBC | Sync FBC con proveedor inválido |
| 4 | rebate-sync | `fallbackExecuteFullSync` NPE (`rebateAgreementRepository is null`) | Manejador de error truena en vez de registrar fallo limpio |
| 5 | rebate-sync | Sin `PhysicalNamingStrategyStandardImpl` Hibernate crea `rebate_acuerdos_temp` snake; queries nativas usan `RebateAcuerdosTemp` → "Invalid object name" | dev.yml lo trae; sólo afecta perfiles sin la estrategia |
| 6 | rebate-sync | Adapter externo NO manda api-key a Azure | Posible causa de 0 datos (o MX vacío genuino) |

## 6. Pendientes / decisiones

- **Preguntar a Ivan:** ¿invoice-status-sync reemplaza o convive con fiscal-download a futuro? Define si se unifica la trazabilidad (`ctrlProceso*` vs `CtrlEnlace*`).
- **rebate green-path real:** api-key válida + data MX, o stub local que devuelva ≥1 contrato para validar el INSERT en `RebateAcuerdosTemp`.
- **Oracle SAPITO + i213 reales:** sólo si se quiere end-to-end no-mock (Fase 3).

## 7. Cómo levantarlos en local

```bash
# Docker arriba
docker start sodimac-mssql sodimac-pg

# Setup dependencias invoice-sync (una vez)
docker cp c:/tmp/invoice_sync_local_setup.sql sodimac-mssql:/tmp/setup.sql
docker exec sodimac-mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P 'Sodimac2026#Dev' -C -No -i /tmp/setup.sql

# invoice-status-sync
cd APP03022-mrch.batch.somx.invoice-status-sync
mvn -q -DskipTests clean package
java -jar target/invoice-status-sync-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
curl -X POST http://localhost:8085/api/v1/batch/sync-status/trigger

# rebate-agreements-sync
cd APP03022-mrch.batch.somx.rebate-agreements-sync
mvn -q -DskipTests clean package
java -jar target/rebate-agreements-sync-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
curl -X POST http://localhost:8086/api/sync/full
```

> Nota: perfil `local` apunta a host port 1434 (SQL Server Docker), credenciales SA. Oracle no se requiere (emulado en SQL Server).
