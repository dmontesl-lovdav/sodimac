# Validación invoice-status-sync contra BD reales (sin mock de BD)

> Fecha: 2026-06-01 · Autor: David Montes
> Objetivo: des-mockear las dependencias de **base de datos** (SAP / SAPITO / i213) y ejercitar la lógica real del batch. FBC se deja en mock por drift de contrato con fiscal-api (ver [DRIFT_INVOICE_SYNC_VS_FISCAL_API.md](DRIFT_INVOICE_SYNC_VS_FISCAL_API.md)).

## Resultado

8 de 10 transiciones corren contra **BD reales**. Las 2 de SAPITO fallan **solo** por un artefacto de red Docker (NAT host↔contenedor), no por la lógica ni el código.

| Dependencia | Motor real | DB/Schema | Resultado |
|---|---|---|---|
| SAP `Envios_Ap` | SQL Server | `SODIMAC_SAP_DEV` | ✅ 6→7 con query real |
| SAPITO `Envios_Ap` | **Oracle** | `UODSRMX` | ✅ schema+data OK, query OK contenedor-a-contenedor; ❌ host→Oracle bloqueado por redirect NAT |
| i213 SPs | SQL Server | `AdmIF213ProdDB` | ✅ 9→10, 10→11 via SPs reales |
| Control `CtrlEnlace*` | SQL Server | `SODIMAC_BATCH_DEV` | ✅ persistencia |
| FBC (facturas) | — | — | mock (drift contrato) |

Transiciones observadas: `6→7` (SAP real), `8→9 / 9→10 / 10→11` (i213 real), `7→8` falla (SAPITO NAT).

## Hallazgo: redirect Oracle + NAT Docker

Oracle Free usa **dedicated server con redirect**: el listener acepta la conexión en :1521 y reenvía al cliente a `HOST=<host del listener>`. Al cruzar el NAT de Docker (app en host → Oracle en contenedor), el redirect rompe con `ORA-12514`.

Diagnóstico que lo confirma:
- Desde dentro del contenedor vía `//localhost:1521/freepdb1` → **funciona** (seed OK).
- Desde dentro del contenedor vía `//host.docker.internal:1521/freepdb1` → **ORA-12514** (mismo error que el host).
- `--hostname localhost` cambió el listener a `HOST=127.0.0.1` pero el redirect sigue rompiendo a través del NAT.

**Implicación clave:** es un artefacto de la topología local (app en host, Oracle en Docker). En el entorno real, batch y Oracle viven en la **misma red sin NAT** → el redirect resuelve bien. **No es bug del batch ni del código de Robert.**

### Cómo cerrar SAPITO al 100% en local
- **Dockerizar el batch** y correrlo en la misma red Docker que `sodimac-oracle` → redirect resuelve container-to-container. Es además lo más cercano a producción.
- (Descartado) homologar SAPITO en SQL Server: confunde, cada objeto debe vivir en su motor real.

## Setup reproducible (local)

Host port SQL Server Docker = **1434**→1433. Oracle = **1521**.

### 1. Oracle SAPITO
```bash
docker run -d --name sodimac-oracle --hostname localhost -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle -e APP_USER=UODSRMX -e APP_USER_PASSWORD=uodsrmx47q8 \
  gvenzl/oracle-free:23-slim
# esperar "DATABASE IS READY TO USE", luego (UODSRMX/uodsrmx47q8 @ //localhost:1521/FREEPDB1):
CREATE TABLE Envios_Ap (CODIGO_PROVEEDOR VARCHAR2(50), NUMERO_DOCUMENTO VARCHAR2(50), NUMERO_UUID VARCHAR2(100), FLAG_ENVIADO NUMBER);
INSERT INTO Envios_Ap VALUES ('PROV001','A12345','UUID-001-MOCK',1);
INSERT INTO Envios_Ap VALUES ('PROV002','B67890','UUID-002-MOCK',1);
COMMIT;
```

### 2. i213 — AdmIF213ProdDB (SQL Server, DB propia)
```sql
CREATE DATABASE AdmIF213ProdDB;
GO
USE AdmIF213ProdDB;
CREATE OR ALTER PROCEDURE dbo.i123_Valida_Documento_AP @idProveedor VARCHAR(50), @numeroDocumento VARCHAR(50)
AS BEGIN SET NOCOUNT ON; SELECT 1 AS CODE, 'Contabilizado OK' AS MSG; END;
GO
CREATE OR ALTER PROCEDURE dbo.i213_Valida_Documento_Pagado_AP @idProveedor VARCHAR(50), @numeroDocumento VARCHAR(50)
AS BEGIN SET NOCOUNT ON; SELECT 1 AS CODE, 'Pagado OK' AS MSG, 'REF-BANK-001' AS REF_BANK; END;
```

### 3. SAP `Envios_Ap` (SQL Server, SODIMAC_SAP_DEV)
```sql
USE SODIMAC_SAP_DEV;
CREATE TABLE dbo.Envios_Ap (CODIGO_PROVEEDOR VARCHAR(50), NUMERO_DOCUMENTO VARCHAR(50), NUMERO_UUID VARCHAR(100), FLAG_ENVIADO INT);
INSERT INTO dbo.Envios_Ap VALUES ('PROV001','A12345','UUID-001-MOCK',1),('PROV002','B67890','UUID-002-MOCK',1);
```
> Control `CtrlEnlace*` en SODIMAC_BATCH_DEV: ver [VALIDACION en ANALISIS_BATCHES_ROBERT.md](ANALISIS_BATCHES_ROBERT.md) / `c:\tmp\invoice_sync_local_setup.sql`.

### 4. Perfil local (`application-local.yml`)
```
datasource.sodimac-sap → jdbc:sqlserver://localhost:1434;databaseName=SODIMAC_SAP_DEV  (SA)
datasource.sapito      → jdbc:oracle:thin:@localhost:1521/FREEPDB1  (UODSRMX/uodsrmx47q8)
datasource.i213        → jdbc:sqlserver://localhost:1434;databaseName=AdmIF213ProdDB  (SA)
batch.datasource       → jdbc:sqlserver://localhost:1434;databaseName=SODIMAC_BATCH_DEV  (SA)
fbc.portal.mock-enabled: true   # FBC sigue mock por drift
```

### 5. Correr
```bash
mvn -q -DskipTests package
java -jar target/invoice-status-sync-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
curl -X POST http://localhost:8085/api/v1/batch/sync-status/trigger
```

## Limpieza aplicada (deuda corregida)

- Se eliminó la homologación de SAPITO sobre SQL Server (confundía). SAPITO ahora = Oracle real.
- Los SPs i213, que estaban mal puestos en `SODIMAC_SAP_DEV`, se movieron a su DB propia `AdmIF213ProdDB`.

## Pendientes

1. **SAPITO 100% verde**: dockerizar el batch (misma red que Oracle) — opcional, el código ya quedó validado contra Oracle real.
2. **FBC non-mock**: bloqueado por drift de contrato fiscal-api → decisión con Ivan (ver DRIFT doc).
3. **rebate-sync non-mock**: ya es non-mock; falta api-key/data real de Azure.
