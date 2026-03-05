# STM-719: Creación de proceso para descargas las facturas y notas de crédito registrados en el portal de proveedor FBC

| Campo | Valor |
|-------|-------|
| **Tipo** | Story |
| **Estado** | In Progress |
| **Asignado** | g_dco018 |
| **Prioridad** | Alta |
| **Creado** | Wed, 16 Jul 2025 14:06:04 -0300 |
| **Labels** | Proyecto |

---

### ‍ Como usuario

Requiero un proceso que descargue las facturas y notas de crédito desde el portal utilizando los servicios web de FBC, para iniciar su desglose, contabilización y posterior pago en SAP.

---

## ⚙️ Reglas de Negocio

 - Validar previamente el funcionamiento del WS STM-338 (Consulta de Factura y NC).

 - Manejar la descarga de una factura y una nota de crédito por separado o en proceso independientes

 - Tomar como base el listado de todas las facturas con estatus 3 consumiendo el servicio de FBC

 - Tomar como base el listado de todas las notas de crédito con estatus 2 consumiendo el servicio de FBC

 - Para actualizar el estatus de una factura y nota de crédito utilizar los siguientes valores para cambiar uno a uno
 

 - Id Proveedor

 - Uuid

 

 

 - Crear un proceso dedicado a la versión vigente de CFDI 4.0.

 - Solo se descargan los siguientes documentos:
 

 - Facturas con estatus **"3 - Pendiente de contabilizar"**

 - Notas de crédito con estatus **"2- Pendiente de contabilizar****"**

 

 

 - Procesamiento transaccional por documento. Si falla un documento, se revierte completamente.

 - Actualizar estatus en origen al momento de iniciar el proceso:
 

 - Factura → "**4 - En proceso de descarga**"

 - NC → "**3 - En proceso de descarga**"

 

 

 - Actualización de estatus origen al terminar el procesamiento de manera correcta
 

 - Factura → "5 **- Desglose de factura**"

 - NC → "4 **- Desglose de nota de crédito**"

 

 

 - En caso de existir un error al intentar registrar la información en el desglose colocar el siguiente estatus
 

 - Factura → "14 **- Error en el desglose de la factura**"

 - NC → "5 **- Error en el desglose de la nota de crédito**"

 

 

 - Registrar actividad del proceso por documento y consolidado:
 

 - ctrlProcesoCab

 - ctrlProcesoDet

 - ctrlProcesoElemento
 

 - Guardar en esta tabla cada factura y nota de crédito considerando el uuid como valor

 

 

 - ctrlLog

 - Revisar la historia para conocer el MER para almacenar la información del proceso [STM-1167](https://jira.falabella.tech/browse/STM-1167) Monitoreo, trazabilidad y auditoría de ejecuciones batch de extracción y envío de información - Jira Falabella

 

 

 - Validar Addenda:
 

 - Factura:
 

 - IdProveedor

 -  TipoProveedor

 - OrdenCompra

 -  Recepcion

 

 

 - NC:
 

 - IdProveedor,

 - TipoProveedor

 - TipoNC

 

 

 - Si no tiene Addenda rechazar la descargar y cambiar de estatus la factura o nota de crédito a pendiente de Addenda

 

 

---

## ️ Datos de Conexión para almacenar la información de proceso

| Parámetro | Valor |
|---|---|
| IP | 10.138.150.124 |
| Puerto | 5319 |
| Base de datos | **SODIMAC_BATCH_DEV** |

## ️ Datos de Conexión para registrar la información de las facturas y notas de crédito

| Parámetro | Valor |
|---|---|
| IP | 10.138.150.124 |
| Puerto | 5319 |
| Base de datos | **SODIMAC_SAP_DEV** |

---

## Nodos Requeridos para almacenar en la BD SODIMAC_SAP_DEV

| Nodo XML | Requerido | Observaciones | Tabla |
|---|---|---|---|
| Comprobante | ✅ | Principal del documento | Comprobante |
| Emisor | ✅ | Datos del proveedor | Emisor |
| Receptor | ✅ | Datos de la empresa | Receptor |
| Concepto | ✅ | Detalle de productos/servicios | Concepto |
| DetalleImpuesto | ✅ | Desglose de impuestos | DetalleImpuesto |
| Impuestos | ✅ | Totales de impuestos | Impuestos |
| Traslado | ✅ | IVA u otros | Traslado |
| Retención | Opcional | Solo si aplica | Retención |

---

## Servicios Web Utilizados

| Servicio | Descripción | Historia relacionada |
|---|---|---|
| STM-338 | Consulta de Factura y NC | Validación previa obligatoria |
| STM-339 | Actualización de estatus | Cambiar estatus a "En proceso de contabilizar" |

---

## Cambios de Estatus en el origen portal FBC

| Documento | Estatus Inicial | Estatus Final | Descripción |
|---|---|---|---|
| Factura | Pendiente de Pago | Pendiente por contabilizar | Tras descarga exitosa |
| NC | Pendiente por descontar | Pendiente por contabilizar | Tras descarga exitosa |
| Factura | Sin Addenda válida | Pendiente de Addenda | Rechazo por falta de Addenda |
| NC | Sin Addenda valida | Pendiente de Addenda | Rechazo por falta de Addenda |
| Factura/NC | Pendiente por contabilizar | En proceso de contabilizar | Usando WS STM-339 |