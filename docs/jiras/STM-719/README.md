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

## Resumen ejecutivo para Jira Sodimac

**Analisis y correcciones realizadas — batch-fiscal-download**

**Problema inicial:** El batch encontraba 0 documentos al ejecutarse.

---

**Causas encontradas y fixes aplicados:**

**1. Fecha fuera de rango (datos)**
Las facturas de prueba tenian `created_at` anterior al rango de busqueda de 6 meses. Se actualizaron las fechas en BD.

**2. XML con BOM (codigo)**
`FacturaIngreso.xml` almacenado con UTF-8 BOM (`\uFEFF`) causaba `Content is not allowed in prolog` en el parser Java.
Fix: `CfdiDesgloseService.parseXml()` — strip BOM antes de parsear.

**3. Transiciones de estatus faltantes en `status_train` (datos BD)**
El batch intenta mover facturas/NCs por `3→4→5` (exito), `3→4→14` (error desglose), `3→4→1` (addenda invalida). Faltaban las transiciones `4→1` y `4→14` para `option_id=2` (NCs).
Fix: Script `fix-status-train.sql` — agregar las 4 transiciones faltantes.

**4. XML de addenda con formato incorrecto (datos)**
Los XML de prueba tenian addenda de otro proveedor (BovedaFiscal), no el formato Sodimac. El batch valida `IdProveedor`, `TipoProveedor`, `OrdenCompra`, `Recepcion` (facturas) y `TipoNC` (NCs).
Fix: Nuevos XMLs de prueba `test-factura-sodimac.xml` y `test-nc-sodimac.xml` con addenda Sodimac correcta.

**5. Columnas `fiscal_uuid` e `invoice_uuid` ausentes en `SODIMAC_SAP_DEV` (BD SQL Server — pendiente)**
El batch falla al intentar el desglose con `Invalid column name 'fiscal_uuid'`. La tabla `Comprobante` en `SODIMAC_SAP_DEV` no tiene estas columnas que el batch requiere para deduplicacion y trazabilidad.
Fix pendiente: Ejecutar `migration-sap-comprobante.sql` en `SODIMAC_SAP_DEV`:
```sql
ALTER TABLE Comprobante ADD fiscal_uuid VARCHAR(36) NOT NULL DEFAULT '';
ALTER TABLE Comprobante ADD invoice_uuid VARCHAR(36) NULL;
```
Script disponible en: `docs/jiras/STM-719/migration-sap-comprobante.sql`

---

**Resultado en local:** Batch ejecuta correctamente — Facturas 9/9 SUCCESS, NCs 3/3 SUCCESS — una vez aplicada la migracion en Sodimac el batch quedara funcional end-to-end.

## Preguntas pendientes (Ivan)

1. NC: Filtrar con estatus 2 o 3? (nuestro enum NC(2)="Recibido Parcial", no "Pendiente Contabilizar")
2. NC error: Estatus 5 del JIRA = exito en nuestro enum. Usar 11 (Rechazo Contable)?
3. **⚠️ BLOQUEANTE — SODIMAC_SAP_DEV: DDL oficial o nuestro diseno?** Ver seccion abajo "Hallazgo 2026-05-28".
4. XML: Desde fiscal-api (campo xmlContent) o directamente del portal FBC?

## Hallazgo 2026-05-28: mismatch entre codigo batch y SAP_DEV Sodimac

Al validar el batch contra Sodimac DEV SQL Server (`10.138.153.10:1433`) se confirma que:

### A) 3 tablas no existen en Sodimac

El codigo escribe a 8 tablas (`Comprobante`, `Emisor`, `Receptor`, `Concepto`, `Impuestos`, `Traslado`, `Retencion`, `DetalleImpuesto`). En `SODIMAC_SAP_DEV` real **no existen**:

- `Impuestos` (totales generales del comprobante)
- `Traslado` (traslados a nivel comprobante)
- `Retencion` (retenciones a nivel comprobante)

Si existen: `Comprobante`, `Emisor`, `Receptor`, `Concepto`, `DetalleImpuesto` (con estructura distinta — ver punto B).

### B) Tabla Comprobante con modelo viejo (autofacturador)

| Java entity espera (modelo nuevo) | Sodimac SAP_DEV real (autofacturador) |
|---|---|
| `id_comprobante INT IDENTITY` (PK) | `Uuid VARCHAR` (PK) |
| `lugar_expedicion` snake_case | `LugarExpedicion` PascalCase |
| `metodo_pago` | `MetodoPago` |
| `forma_pago` | `FormaPago` |
| `tipo_comprobante` | `TipoDeComprobante` |
| `condiciones_pago` | `CondicionDePago` |
| `xml_completo` | `Xml` |
| `estatus_proceso` VARCHAR | `Estatus` INT |
| `exportacion`, `no_certificado`, `sello`, `certificado`, `fecha_timbrado`, `rfc_prov_certif`, `no_certificado_sat`, `fecha_registro` | **No existen** |
| `fiscal_uuid`, `invoice_uuid` | ya agregadas via `migration-sap-comprobante.sql` |

### Workaround temporal aplicado

Para destrabar la prueba local (Docker tiene el modelo nuevo aplicado), se **comento en el codigo** todo lo relacionado con las 3 tablas faltantes:

Archivo: `APP03022-mrch.batch.somx.fiscal-download/src/main/java/com/sodimac/batch/fiscal/download/service/CfdiDesgloseService.java`

- Field declarations: `impuestosRepository`, `trasladoRepository`, `retencionRepository`
- Parametros del constructor + asignaciones
- Llamada a `extractImpuestos` dentro de `desglosar()` (paso 5)
- Metodos `extractImpuestos()`, `saveTraslado()`, `saveRetencion()`

Los datos NO se pierden:
- `DetalleImpuesto` (que si existe) guarda traslados/retenciones a nivel concepto
- `xml_completo` guarda el XML integro
- Totales a nivel comprobante se pueden derivar con `SUM()` o leyendo el XML

### Opciones para resolver el bloqueante (preguntar a Ivan/Bonelli)

| Opcion | DDL en Sodimac | Cambios codigo | Funcionalidad |
|---|---|---|---|
| 1. Aplicar `sap-dev-dll.sql` completo en DB nueva `SODIMAC_FBC_SAP_DEV` | DB nueva + 8 tablas | 0 | Completa |
| 2. Migracion hibrida: ALTER Comprobante existente + crear 3 faltantes en `SODIMAC_SAP_DEV` | ALTER + CREATE x3 | 0 | Completa pero modelo mixto |
| 3. Refactor batch a modelo viejo Sodimac (Uuid PK, PascalCase) | Solo ALTER ya aplicado | Mucho (5 entities + mappers + service) | Reducida (pierde sello/certificado/timbrado/etc.) |
| 4. Mantener workaround (sin las 3 tablas) + refactor de las otras 5 entities al modelo viejo | 0 | Moderado | Reducida pero sin tocar Sodimac |

**Recomendacion personal**: opcion 1 — DB nueva separa concerns, no toca modelo viejo del autofacturador, codigo funciona como esta.

### Estado actual

- **Docker local (modelo nuevo aplicado)**: batch funciona end-to-end. 9 facturas + 3 NCs procesadas con SUCCESS.
- **Sodimac DEV**: bloqueado. Necesita decision de Ivan/Bonelli antes de avanzar.
- **UAT**: bloqueado por dependencia de Sodimac DEV.

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
