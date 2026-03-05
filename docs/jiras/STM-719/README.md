# STM-719: Proceso Batch de Descarga de Facturas y NC desde FBC

## Descripcion

Proceso batch standalone (Spring Boot) que descarga facturas y notas de credito desde el portal FBC,
desglosa el XML CFDI 4.0, almacena en SODIMAC_SAP_DEV y registra trazabilidad en SODIMAC_BATCH_DEV.

## Proyecto creado

`APP03022-mrch.backend.somx.batch-process` — Puerto 8090

## Arquitectura

```
[batch-process :8090]
    |
    +-- fiscal-api :8082 (buscar documentos + actualizar estatus)
    +-- SODIMAC_BATCH_DEV (SQL Server :1433 - trazabilidad)
    +-- SODIMAC_SAP_DEV (SQL Server :1433 - desglose CFDI)
```

## Endpoints

| Metodo | URL | Descripcion |
|--------|-----|-------------|
| POST | `/batch/invoices/download` | Ejecutar descarga de facturas (estatus 3) |
| POST | `/batch/credit-notes/download` | Ejecutar descarga de NC (estatus 3) |
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui/index.html` | Swagger UI |

## Tablas SODIMAC_SAP_DEV (creadas)

Comprobante, Emisor, Receptor, Concepto, Impuestos, Traslado, Retencion, DetalleImpuesto

DDL: `docs/db/sap_dev/sap-dev-dll.sql`

## Cambios en fiscal-api

- `InvoiceStatus.java`: Agregado estatus 14 "Error en el desglose de la factura"
- Transicion 4 -> [5, 11, **14**]

## Flujo de estatus (Factura)

```
3 (Pendiente Contabilizar) -> 4 (Proceso descarga)
    -> 5 (Desglose factura) [exito]
    -> 14 (Error desglose) [error]
    -> 1 (Pendiente Addenda) [sin addenda]
```

## Preguntas pendientes (Ivan)

1. NC: Filtrar con estatus 2 o 3? (nuestro enum NC(2)="Recibido Parcial", no "Pendiente Contabilizar")
2. NC error: Estatus 5 del JIRA = exito en nuestro enum. Usar 11 (Rechazo Contable)?
3. SODIMAC_SAP_DEV: DDL oficial o nuestro diseno?
4. XML: Desde fiscal-api (campo xmlContent) o directamente del portal FBC?

## Verificacion

```bash
# Compilar
cd APP03022-mrch.backend.somx.batch-process
mvn clean package -DskipTests -q

# Ejecutar
java -jar target/batch-process-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Health
curl http://localhost:8090/actuator/health

# Descarga facturas
curl -X POST http://localhost:8090/batch/invoices/download

# Descarga NC
curl -X POST http://localhost:8090/batch/credit-notes/download

# Verificar trazabilidad
MSYS_NO_PATHCONV=1 docker exec sodimac-mssql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U SA -P 'Sodimac2026#Dev' -C -d SODIMAC_BATCH_DEV \
  -Q "SELECT * FROM ctrlProcesoCab ORDER BY fecha_inicio DESC"
```
