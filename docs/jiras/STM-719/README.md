# STM-719: Proceso Batch de Descarga de Facturas y NC desde FBC

## Descripcion

Proceso batch standalone (Spring Boot) que descarga facturas y notas de credito desde el portal FBC,
desglosa el XML CFDI 4.0, almacena en SODIMAC_SAP_DEV y registra trazabilidad en SODIMAC_BATCH_DEV.

## Proyecto creado

`APP03022-mrch.batch.somx.fiscal-download` — Puerto 8090

## Arquitectura

```
[fiscal-download :8090]
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

## Flujo de estatus (NC)

```
3 (Pendiente Contabilizar) -> 4 (Proceso descarga)
    -> 5 (Desglose NC) [exito]
    -> 14 (Error desglose) [error]
    -> 1 (Pendiente Addenda) [sin addenda valida]
```

## Que hace el batch (explicacion simple)

El batch es un **robot que trabaja en segundo plano** sin interfaz. Su funcion:

1. **Busca facturas y NC pendientes** en el portal B2B (fiscal-api) — las que tienen estatus 3 "Pendiente Contabilizar"
2. **Descarga el XML CFDI** de cada una y lo desglosa: extrae emisor, receptor, conceptos, impuestos
3. **Guarda el desglose en SAP** (SODIMAC_SAP_DEV) para que los sistemas contables de Sodimac puedan consumirlo
4. **Valida la addenda** — verifica que el XML traiga los datos Sodimac requeridos (IdProveedor, TipoProveedor, OrdenCompra, Recepcion para facturas; TipoNC para NC)
5. **Actualiza el estatus** de cada documento segun el resultado: exito, error de desglose, o addenda invalida
6. **Registra trazabilidad** en SODIMAC_BATCH_DEV: cuantos proceso, cuantos fallaron, duracion

En resumen: **lee facturas del portal → las convierte al formato SAP → las deja listas para contabilidad**.

## Hallazgos y correcciones (2026-03-18)

### 1. BOM en XML (codigo)
**Causa:** Algunos XML almacenados tenian BOM UTF-8 (`\uFEFF`) al inicio, lo que rompia el parser Java.
**Fix:** `CfdiDesgloseService.parseXml()` — eliminar BOM antes de parsear.
**Archivo:** `APP03022-mrch.batch.somx.fiscal-download/.../service/CfdiDesgloseService.java`

### 2. Transiciones de estatus faltantes (BD PostgreSQL)
**Causa:** `shared_catalogs.status_train` no tenia las transiciones `4→1` y `4→14` para NCs (option_id=2).
**Fix:** Insertar las 4 transiciones faltantes.
**Script:** `docs/jiras/STM-719/fix-status-train.sql`

### 3. Columnas faltantes en SODIMAC_SAP_DEV (BD SQL Server — pendiente aplicar en Sodimac)
**Causa:** La tabla `Comprobante` en `SODIMAC_SAP_DEV` no tiene las columnas `fiscal_uuid` ni `invoice_uuid` que el batch necesita para deduplicacion y trazabilidad.
**Sintoma:** `ERROR: Invalid column name 'fiscal_uuid'` al intentar el desglose.
**Fix pendiente:** Ejecutar el siguiente DDL en `SODIMAC_SAP_DEV`:
```sql
ALTER TABLE Comprobante ADD fiscal_uuid VARCHAR(36) NOT NULL DEFAULT '';
ALTER TABLE Comprobante ADD invoice_uuid VARCHAR(36) NULL;
```
**Script completo:** `docs/jiras/STM-719/migration-sap-comprobante.sql`

### Resultado en local
Batch ejecuta correctamente una vez aplicados los fixes:
- Facturas: `origen=9 destino=9 errores=0 — SUCCESS`
- NC: `origen=3 destino=3 errores=0 — SUCCESS`

## Preguntas pendientes (Ivan)

1. NC: Filtrar con estatus 2 o 3? (nuestro enum NC(2)="Recibido Parcial", no "Pendiente Contabilizar")
2. NC error: Estatus 5 del JIRA = exito en nuestro enum. Usar 11 (Rechazo Contable)?
3. SODIMAC_SAP_DEV: DDL oficial o nuestro diseno?
4. XML: Desde fiscal-api (campo xmlContent) o directamente del portal FBC?

## Verificacion

```bash
# Compilar
cd APP03022-mrch.batch.somx.fiscal-download
mvn clean package -DskipTests -q

# Ejecutar
java -jar target/fiscal-download-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

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
