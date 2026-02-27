# STM-393: Pantalla para Consulta de Facturas Registradas

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-393

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :yellow_circle: En Analisis |
| **Modulo** | Fiscal |
| **Tipo** | Frontend + Backend |
| **APIs Involucradas** | fiscal-api, catalogos-api, utils-api |
| **Fecha Analisis** | 2025-01-06 |

---

## Descripcion

Como proveedor, se requiere tener una pantalla que permita consultar las facturas publicadas, relacionadas con sus ordenes de compra y recepciones, para visualizar, descargar o gestionar los documentos fiscales de forma eficiente.

---

## Criterios de Aceptacion

- [ ] La pantalla no muestra resultados sin parametros de busqueda
- [ ] Las fechas se validan correctamente (inicio <= final)
- [ ] El periodo de busqueda no excede 6 meses
- [ ] El grid muestra los campos segun el tipo de usuario
- [ ] Los botones de accion funcionan segun reglas de negocio
- [ ] Se muestran mensajes informativos y advertencias segun flujo
- [ ] El reporte XLSX se genera correctamente segun seleccion
- [ ] Los servicios web se consumen correctamente y manejan excepciones

---

## Analisis de Servicios Existentes

### Servicios Requeridos vs Implementados

| Servicio | Endpoint | API | Estado | JIRA Relacionado |
|----------|----------|-----|--------|------------------|
| Busqueda de facturas | `POST /invoices/search` | fiscal-api | :green_circle: Implementado | STM-338 |
| Catalogo de estatus | `GET /CatEstatusFactura/details` | catalogos-api | :green_circle: Implementado | - |
| Descarga XML masivo | `POST /invoices/download/xml` | fiscal-api | :green_circle: Implementado | STM-396 |
| Descarga PDF masivo | `POST /invoices/download/pdf` | fiscal-api | :green_circle: Implementado | STM-396 |
| Exportar CSV | `POST /invoices/export/csv` | fiscal-api | :green_circle: Implementado | STM-396 |
| Catalogo de mensajes | `GET /message/{key}` | catalogos-api | :green_circle: Implementado | - |
| Parametros de config | `GET /api/parameters` | utils-api | :green_circle: Implementado | STM-1213 |
| **Exportar XLSX 2 hojas** | `POST /invoices/export/xlsx` | fiscal-api | :red_circle: **NO EXISTE** | **Pendiente** |

---

## Parametros de Busqueda

| Campo | Obligatorio | Tipo | Longitud | Default | Validaciones | idMsg | Existe en API |
|-------|-------------|------|----------|---------|--------------|-------|---------------|
| Fecha inicio recepcion | Si | Fecha | - | Fecha actual | No mayor a fecha final | WRN7000 | :green_circle: `fechaInicioRecepcion` |
| Fecha final recepcion | Si | Fecha | - | Fecha actual | No menor a fecha inicio | WRN7006 | :green_circle: `fechaFinalRecepcion` |
| ID de proveedor | Condicional | Numerico | 10 | Auto si es proveedor | Solo numeros | - | :green_circle: `idProveedor` |
| Serie | No | Texto | 30 | - | Validar longitud | - | :green_circle: `serie` |
| Folio | No | Texto | 30 | - | Validar longitud | - | :green_circle: `folio` |
| Estatus factura | No | Numerico | - | - | - | - | :green_circle: `estatus` |

### Endpoint de Busqueda

```
POST /invoices/search
Content-Type: application/json
```

**Request Body:**
```json
{
  "rfcEmisor": "AAA010101AAA",
  "fechaInicioRecepcion": "2025-01-01",
  "fechaFinalRecepcion": "2025-06-30",
  "tipoDocumento": "I",
  "idProveedor": 1234567890,
  "serie": "A",
  "folio": "12345",
  "estatus": 1,
  "page": 0,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

---

## Grid de Resultados

### Campos Mostrados

| # | Campo STM-393 | Campo API | Tabla BD | Estado | Visibilidad |
|---|---------------|-----------|----------|--------|-------------|
| 1 | Checkbox | N/A | - | Frontend | Todos |
| 2 | Serie | `series` | invoice.series | :green_circle: OK | Todos |
| 3 | Folio | `folio` | invoice.folio | :green_circle: OK | Todos |
| 4 | Subtotal | `subtotal` | invoice.subtotal | :green_circle: OK | Todos |
| 5 | Total | `total` | invoice.total | :green_circle: OK | Todos |
| 6 | Orden de compra | `noOrdenCompra` | addendum.purchase_order_number | :green_circle: OK | Todos |
| 7 | Recepcion | `noRecepcion` | addendum.reception_number | :green_circle: OK | Todos |
| 8 | UUID | `fiscalUuid` | invoice.fiscal_uuid | :green_circle: OK | Todos |
| 9 | # NC relacionadas | `notasCreditoRelacionadas.length` | related_cfdi | :green_circle: OK | Todos |
| 10 | ID proveedor | `numeroProveedor` | addendum.supplier_number | :green_circle: OK | Solo admin |
| 11 | Nombre proveedor | `emisorName` | issuer.name | :green_circle: OK | Solo admin |
| 12 | Fecha emision | `issueDate` | invoice.issue_date | :green_circle: OK | Todos |
| 13 | Fecha recepcion | `createdAt` | invoice.created_at | :green_circle: OK | Todos |
| 14 | **Fecha de envio** | **`sentDate`** | **invoice.sent_date** | :red_circle: **GAP** | Todos |

### Paginacion

- Registros por pagina: 10 (default)
- Parametros: `page`, `size`

---

## Catalogo de Estatus de Factura

**Endpoint:** `GET /CatEstatusFactura/details?lang=1`

| Codigo | ID Interno | Descripcion (ES) |
|--------|------------|------------------|
| EFA001 | 0 | Rechazo Comercial |
| EFA002 | 1 | Pendiente Addenda |
| EFA003 | 2 | Recibido Parcial |
| EFA004 | 3 | Pendiente de Contabilizar |
| EFA005 | 4 | En proceso de descarga |
| EFA006 | 5 | Desglose de factura |
| EFA007 | 6 | Pendiente de envio a contabilizar |
| EFA008 | 7 | Pendiente de Pago |
| EFA009 | 8 | Pagado |
| EFA010 | 9 | Pendiente de complemento |
| EFA011 | 10 | Completado |
| EFA012 | 11 | Rechazo Contable |
| EFA013 | 12 | No valido fiscal |
| EFA014 | 13 | Pago Manual |

---

## Botones y Acciones

### Botones por Registro

| Boton | Accion | Endpoint | Reglas | Visibilidad |
|-------|--------|----------|--------|-------------|
| Descargar XML | ZIP desde repositorio | `POST /invoices/download/xml` | Validar existencia | Todos |
| Descargar PDF | ZIP o generar si no existe | `POST /invoices/download/pdf` | Validar existencia | Todos |
| Ver NC | Redirige a pantalla NC | N/A (navegacion) | Requiere ID factura | Todos |
| Cancelar factura | Redirige a cancelacion | N/A (navegacion) | Solo estatus 1 o 2 | Solo admin |

### Botones Generales

| Boton | Accion | Endpoint | Visibilidad |
|-------|--------|----------|-------------|
| Buscar | Ejecuta busqueda | `POST /invoices/search` | Todos |
| Limpiar filtros | Restablece campos | N/A (frontend) | Todos |
| Descargar XLSX | Genera reporte 2 hojas | `POST /invoices/export/xlsx` | Todos |
| Descargar XML (masivo) | ZIP con XMLs | `POST /invoices/download/xml` | Todos |
| Descargar PDF (masivo) | ZIP con PDFs | `POST /invoices/download/pdf` | Todos |
| Cancelar (masivo) | Redirige a cancelacion | N/A (navegacion) | Solo admin |

---

## Reporte XLSX (2 Hojas)

### Hoja 1: Facturas

| Campo | Formato |
|-------|---------|
| Serie | Texto |
| Folio | Texto |
| Subtotal | Numero |
| Total | Numero |
| Orden de compra | Texto |
| Recepcion | Texto |
| UUID | Texto |
| # NC relacionadas | Numero |
| ID proveedor (admin) | Numero |
| Nombre proveedor (admin) | Texto |
| Fecha emision | dd/mm/yyyy |
| Fecha recepcion | dd/mm/yyyy hh24:mi:ss |
| Fecha de envio | dd/mm/yyyy hh24:mi:ss |

### Hoja 2: Notas de Credito

| Campo | Formato |
|-------|---------|
| Serie | Texto |
| Folio | Texto |
| Subtotal | Numero |
| Total | Numero |
| Motivo | Texto |
| UUID | Texto |
| Fecha emision | dd/mm/yyyy |
| Fecha recepcion | dd/mm/yyyy hh24:mi:ss |
| Fecha de envio | dd/mm/yyyy hh24:mi:ss |
| Serie Factura (relacionada) | Texto |
| Folio Factura (relacionada) | Texto |
| UUID Factura (relacionada) | Texto |

---

## Mensajes por Flujo

| idMsg | Tipo | Mensaje | Estado |
|-------|------|---------|--------|
| INF6000 | Informativo | No existe informacion con los criterios establecidos | :red_circle: **Por crear** |
| WRN7000 | Advertencia | La fecha inicio no puede ser superior a la fecha final | :red_circle: **Por crear** |
| WRN7005 | Advertencia | El periodo de busqueda no puede ser superior a 6 meses | :red_circle: **Por crear** |
| WRN7006 | Advertencia | La fecha final no puede ser menor a la fecha inicio | :red_circle: **Por crear** |

**Endpoint para obtener mensajes:** `GET /message/{idMsg}?lang=1`

---

## Validaciones

### Frontend

| Validacion | Campo | Mensaje |
|------------|-------|---------|
| Fecha inicio <= fecha final | fechaInicioRecepcion, fechaFinalRecepcion | WRN7000/WRN7006 |
| Rango <= 6 meses | fechaInicioRecepcion, fechaFinalRecepcion | WRN7005 |
| Solo numeros | idProveedor | Validacion local |
| Longitud maxima 30 | serie, folio | Validacion local |

### Backend (Por implementar)

| Validacion | Servicio | Estado |
|------------|----------|--------|
| Rango de fechas <= 6 meses | fiscal-api | :red_circle: **Pendiente** |
| Consultar parametro MAX_SEARCH_MONTHS | utils-api | :red_circle: **Pendiente** |

---

## Resumen de GAPS

| # | Gap | Descripcion | Prioridad | Archivo de Implementacion |
|---|-----|-------------|-----------|---------------------------|
| 1 | Campo `sentDate` | Agregar fecha de envio en response | :red_circle: Alta | [STM-393_GAP_01_SENT_DATE.md](./STM-393_GAP_01_SENT_DATE.md) |
| 2 | Endpoint XLSX | Crear exportacion con 2 hojas | :red_circle: Alta | [STM-393_GAP_02_XLSX_EXPORT.md](./STM-393_GAP_02_XLSX_EXPORT.md) |
| 3 | Mensajes INF/WRN | Insertar mensajes en catalogos-api | :yellow_circle: Media | [STM-393_GAP_03_MESSAGES.md](./STM-393_GAP_03_MESSAGES.md) |
| 4 | Parametro rango | Insertar MAX_SEARCH_MONTHS en utils-api | :yellow_circle: Media | [STM-393_GAP_04_PARAMETER.md](./STM-393_GAP_04_PARAMETER.md) |
| 5 | Validacion backend | Validar rango 6 meses en fiscal-api | :yellow_circle: Media | [STM-393_GAP_05_VALIDATION.md](./STM-393_GAP_05_VALIDATION.md) |

---

## Archivos Relacionados

### Scripts SQL

| Archivo | Descripcion |
|---------|-------------|
| [STM-393_01_messages.sql](./scripts/STM-393_01_messages.sql) | Mensajes INF6000, WRN7000, WRN7005, WRN7006 |
| [STM-393_02_parameter.sql](./scripts/STM-393_02_parameter.sql) | Parametro MAX_SEARCH_MONTHS |
| [STM-393_03_sent_date_column.sql](./scripts/STM-393_03_sent_date_column.sql) | Columna sent_date en invoice |

### Documentacion de GAPS

| Archivo | Descripcion |
|---------|-------------|
| [STM-393_GAP_01_SENT_DATE.md](./STM-393_GAP_01_SENT_DATE.md) | Implementacion campo fecha de envio |
| [STM-393_GAP_02_XLSX_EXPORT.md](./STM-393_GAP_02_XLSX_EXPORT.md) | Implementacion exportacion XLSX |
| [STM-393_GAP_03_MESSAGES.md](./STM-393_GAP_03_MESSAGES.md) | Implementacion mensajes |
| [STM-393_GAP_04_PARAMETER.md](./STM-393_GAP_04_PARAMETER.md) | Implementacion parametro |
| [STM-393_GAP_05_VALIDATION.md](./STM-393_GAP_05_VALIDATION.md) | Implementacion validacion rango |

### Postman

| Archivo | Descripcion |
|---------|-------------|
| [STM-393_Consulta_Facturas.postman_collection.json](./STM-393_Consulta_Facturas.postman_collection.json) | Coleccion de pruebas |

---

## JIRAs Relacionados

| JIRA | Descripcion | Relacion |
|------|-------------|----------|
| STM-337 | Registro de Facturas y NC | Origen de datos |
| STM-338 | Consulta de Facturas y NC | Endpoint de busqueda |
| STM-396 | Descarga masiva XML/PDF/CSV | Descargas existentes |
| STM-1168 | NC relacionadas en consulta | Campo notasCreditoRelacionadas |
| STM-1169 | Datos OC/Recepcion en consulta | Campos noOrdenCompra, noRecepcion |
| STM-1213 | Sistema de parametros | Parametro MAX_SEARCH_MONTHS |

---

## Notas Tecnicas

- **fiscal-api**: Puerto 8082
- **catalogos-api**: Puerto 8083
- **utils-api**: Puerto 3712
- **BFF fiscal**: https://dev.fbusinesscenter.com/ppsomx/fiscal

---

**Fecha:** 2025-01-06
**Autor:** Sodimac Tech Team
**Estado:** Analisis Completado - Pendiente Implementacion de GAPS
