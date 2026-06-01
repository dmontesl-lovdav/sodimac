# [STM-1309] Creación de proceso para sincronizar los estatus de una factura

> Transcripción fiel del XML oficial de Jira. No analizar aquí — el análisis va en `STM-1309_analisis.md`.
> Enlace: https://jira.falabella.tech/browse/STM-1309

## Metadatos

| Campo | Valor |
|---|---|
| Key | STM-1309 |
| Tipo | Story |
| Prioridad | Alta |
| Estado | Done |
| Resolución | Done |
| Asignado | g_dop02 (g_dop02@sodimac.com.mx) |
| Reporter | IVAN SAUL CORTES ZAMORA (iscortesz@sodimac.com.mx) |
| Epic Link | STM-336 |
| Componente | Portal de Proveedores FBC |
| Sprints | FBC - Sprint 1/2/3/4 - 2026 |
| Story Points | 5.0 |
| Área de Negocio | Transversal |
| Automatizado | No |
| Creado | Mon, 29 Dec 2025 12:52:50 -0400 |
| Actualizado | Mon, 2 Mar 2026 19:18:31 -0400 |
| Resuelto | Mon, 2 Mar 2026 19:18:31 -0400 |
| Labels | Proyecto |

---

## ✅ Historia de Usuario

**Como** usuario administrador de finanzas y proveedor
**Quiero** crear un proceso batch que sincronice los estatus de las facturas en el portal de proveedores FBC
**Para** dar visibilidad clara del flujo en que se encuentra cada factura y facilitar el seguimiento para el pago.

---

## ✅ Reglas de Negocio

- Crear un **proceso batch** que:
  - Descargar la información de las facturas del portal de FBC considerando la consulta de los servicios como base de información para sincronizar los estatus.
  - Configurar en la BD SODIMAC_BATCH_DEV las cadenas de conexión creando el catálogo de conexión en la tabla catCatalogo.
  - Dar de alta las cadenas de conexión en la tabla adminCatalogo en valor encriptado.
  - Conecte a las **BD locales de Sodimac México** para consultar el estatus real de cada factura.
  - Actualice los estatus en el portal de FBC para mantener sincronización de información de cada factura.

- **Datos de conexión**:

| Sistema | Manejador | IP | Usuario | BD/SID | Puerto |
|---|---|---|---|---|---|
| Sodimac SAP | SQL Server | 10.138.153.10 | SodimacETLUSR | SODIMAC_SAP_DEV | 1433 |
| SAPITO | Oracle | ensenada | UODSRMX | odsrmxts | 1541 |
| ~~interfase i213~~ | ~~SQL Server~~ | ~~98.200.28.19~~ | ~~I_SODIMAC_IF213~~ | ~~AdmIF213ProdDB~~ | ~~ ~~ |
| interfase i213 | SQL Server | 10.138.153.10 | SodimacETLUSR | AdmIF213ProdDB | 1433 |
| Sodimac SAP | SQL Server | 10.138.153.10 | SodimacETLUSR | SODIMAC_BATCH_DEV | 1433 |

- **Ejecución programada**:
  - Todos los días a las **07:30 AM**.
  - Dos reintentos automáticos cada 30 minutos en caso de error.

- **Control y auditoría**:
  - Generar **logs detallados**.
  - Tomar **fotografía/cifras control** antes de actualizar datos.
  - Registrar las cifras control en la BD SODIMAC_BATCH_DEV.
  - Registrar en la tabla de control CtrlProcesoCab el número de registros totales y finales que fueron procesados.
  - Registrar paso a paso la ejecución del proceso CtrlProcesoDet.
  - Registrar la factura que cambio de estatus en la tabla CtrlProcesoElemento.
  - En caso de generar un error al intentar actualizar la factura guardar el detalle en la tabla ctrlLog.
  - Insertar en la tabla adminCatalogo el nombre del proceso que se va a crear.

- **Servicios involucrados**:
  - **Consulta de facturas**: `URL: XXX`
    - Ajustar el servicio en el back fiscal para consultar por estatus cada factura.
  - **Actualización de factura**: `URL: XXX`

- **Datos clave para actualización**:
  - `idProveedor`
  - `uuid`
  - `serie + folio = (Número de documento)`

---

## ✅ Panel de Queries por Escenario

- Realizar una consulta por estatus utilizando el servicio de FBC, en caso de no contar con registro almacenar en la tabla de cifras control el dato para tener rastro de fecha y hora de cada consulta.

| Escenario | Descripción | Query Ejemplo | Sistema Local | Acción en Portal FBC |
|---|---|---|---|---|
| Estatus 6 → Pendiente registro en SAPITO | Validar si fueron registradas las facturas a BD SODIMAC_SAP, en caso de no existir registro no cambiar el estatus | `SELECT COUNT(1) FROM Envios_Ap WHERE CODIGO_PROVEEDOR = XX AND NUMERO_UUID = XX` | Sodimac SAP | Cambiar estatus a **7** |
| Estatus 7 → Pendiente envío a i213 | Validar si están pendientes de enviar de Sapito a i213. En caso de existir registros cambiar el estatus | `SELECT COUNT(1) FROM Envios_Ap WHERE CODIGO_PROVEEDOR = XX AND NUMERO_UUID = XX AND FLAG_ENVIADO IN (0)` | SAPITO | Cambiar estatus a **8** |
| Estatus 8 - Factura enviada a la i213 | Validar el envío de Sapito a i213. En caso de existir registros cambiar el estatus a 9; en caso de no existir registro cambiar el flag_enviado igual a 2, esto indica que no fue enviado a la interfase i213 | `SELECT COUNT(1) FROM Envios_Ap WHERE CODIGO_PROVEEDOR = XX AND NUMERO_UUID = XX AND FLAG_ENVIADO IN (1)` | SAPITO | Cambiar estatus a **9** (si flag=2 → estatus **16**) |
| Estatus 9 → Pendiente contabilizar en SAP | Factura registrada en i213 sin contabilizar en SAP. Validar el estatus de la factura: en caso de ser 0 cambiar el estatus a 13 - Factura con rechazo contable; en caso de ser 1 cambiar el estatus a 10 indicando que la factura fue contabilizada | Ejecutar procedimiento `i123_Valida_Documento_AP` pasando como parámetro el id del proveedor y número de documento | i213 | Cambiar estatus a 10 (si flag=0 → estatus 13) |
| Estatus 10 → Pendiente Pago | Pendiente de Pago: si el estatus es 0 no cambiar el estatus; en caso de regresar el estatus en 1 cambiarlo | Ejecutar procedimiento `i213_Valida_Documento_Pagado_AP` pasando como parámetro el id del proveedor y número de documento | i213 | Cambiar estatus a **11** |

---

## Acceptance Criteria

- El proceso debe ejecutarse automáticamente a las 07:30 AM y reintentar hasta 2 veces si falla.
- Debe conectarse correctamente a las BD locales y al portal FBC.
- Debe actualizar los estatus según las reglas definidas para cada flujo.
- Generar **log de ejecución** con: Hora de inicio y fin; Cantidad de facturas procesadas; Cantidad de errores.
- Tomar **cifras control** antes y después de la actualización.
- Validar que los estatus en el portal reflejen el flujo real del documento.
- Tiempo máximo de ejecución: **≤ 15 minutos** para todo el lote.
- En caso de error, registrar detalle y enviar alerta al equipo de soporte.
