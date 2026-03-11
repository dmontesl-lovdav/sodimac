# Fiscal API - Guia de Endpoints para ETL

**Base URL**: `https://dev.fbusinesscenter.com/ppsomx/fiscal`

---

## 1. Registrar Factura o Nota de Credito

**POST** `/invoices/register`

### Entrada (multipart/form-data)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| file | File | Si | Archivo XML del CFDI |

- Factura: XML con `TipoDeComprobante="I"`
- Nota de Credito: XML con `TipoDeComprobante="E"` (la factura relacionada debe existir previamente)

### Salida (200 OK)

```json
{
  "code": "RES005",
  "message": "Factura registrada exitosamente - Pendiente de Addenda",
  "success": true,
  "invoiceUuid": "41c5d355-4089-41d1-896a-1f9f9da73798",
  "fiscalUuid": "aabb0001-ae01-4000-a000-000000000001",
  "series": "FA",
  "folio": "ETL001",
  "documentType": "I",
  "issuerRfc": "EKU9003173C9",
  "receiverRfc": "LAN7008173R5",
  "total": "11600.00",
  "issueDate": "2025-07-10T00:00:00",
  "hasAddenda": false,
  "pendingAddenda": true,
  "warnings": [],
  "processedAt": "2026-03-10T16:05:23.035496"
}
```

### Codigos de respuesta

| Codigo | Significado |
|--------|-------------|
| RES003 | Factura registrada exitosamente |
| RES005 | Factura registrada - Pendiente de Addenda |
| RES006 | Nota de Credito registrada exitosamente |
| RES007 | Nota de Credito registrada - Pendiente de Addenda |

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| ERR001 | El archivo esta vacio | Verificar que xml_file tiene contenido |
| ERR002 | El archivo debe tener extension .xml | Nombre del archivo debe terminar en .xml |
| ERR004 | El XML no tiene una estructura valida | XML corrupto o mal formado |
| ERR006 | El XML no cumple con el esquema XSD | Estructura no cumple con CFDI 4.0 |
| ERR029 | El RFC receptor no esta autorizado | RFC no existe en catalogo de receptores |
| BUS022 | El documento debe ser version CFDI 4.0 | Version incorrecta |
| BUS023 | Tipo debe ser I o E | Se envio un complemento (P) a este endpoint |
| BUS034 | UUID ya registrado | Duplicado, no reintentar |
| BUS035 | Serie y Folio ya registrados | Duplicado, no reintentar |
| BUS043 | Factura relacionada no registrada | Registrar la factura primero, luego la NC |
| BUS045 | Tipo de relacion debe ser 01 | NC con tipo de relacion incorrecto |

---

## 2. Registrar Complemento de Pago

**POST** `/fiscal/complementos-pago/registrar`

### Entrada (multipart/form-data)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| xmlFile | File | Si | Archivo XML del complemento (TipoDeComprobante="P") |
| idProveedor | Long | Si | Numero de proveedor |
| tipoAddenda | Integer | Si | Siempre **5** |
| tipoProveedor | String | Si | Tipo: "SLI", "TRA", "IND", "SOT" |
| idUsuario | Long | Si | ID del usuario que registra |

### Salida (200 OK)

```json
{
  "paymentsUuid": "abcd13a4-5c57-474e-9dc8-7e2e1d97d7f6",
  "fileName": "complemento-pago.xml",
  "processingStatus": "SUCCESS",
  "responseCode": "200",
  "message": "Complemento de pago registrado exitosamente",
  "folio": "TEST001",
  "serie": "P",
  "rfcEmisor": "EKU9003173C9",
  "rfcReceptor": "LAN7008173R5",
  "montoTotalPagos": "13920.00",
  "fechaRegistro": "2026-03-09T13:32:31",
  "logUuid": null
}
```

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| ERR001 | El archivo esta vacio | Verificar contenido del XML |
| ERR002 | Extension debe ser .xml | Nombre del archivo debe terminar en .xml |
| ERR004 | XML no tiene estructura valida | XML corrupto o mal formado |
| ERR020 | Tipo de comprobante debe ser P | Se envio factura/NC a este endpoint |
| ERR022 | Debe especificar totales | Falta nodo Totales en Pagos 2.0 |
| ERR024 | Debe contener al menos un Pago | Falta nodo Pago dentro de Pagos |
| ERR026 | Complemento ya registrado | UUID duplicado, no reintentar |
| ERR028 | Tipo de addenda debe ser 5 | Campo tipoAddenda incorrecto |
| ERR029 | RFC receptor no autorizado | RFC no existe en catalogo |

---

## 3. Buscar Facturas / Notas de Credito

**POST** `/invoices/search`

### Entrada (application/json)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| tipoDocumento | String | Si | "I" (facturas) o "E" (notas de credito) |
| fechaInicioRecepcion | String | Si | Fecha inicio YYYY-MM-DD |
| fechaFinalRecepcion | String | Si | Fecha fin YYYY-MM-DD |
| rfcEmisor | String | No | RFC del emisor (vacio = todos) |
| rfcReceptor | String | No | RFC del receptor |
| idProveedor | Number | No | Numero de proveedor |
| serie | String | No | Serie del comprobante |
| folio | String | No | Folio del comprobante |
| uuid | String | No | UUID fiscal del timbre |
| estatus | Integer | No | Estatus del documento |
| noOrdenCompra | String | No | Numero de orden de compra |
| noRecepcion | String | No | Numero de recepcion |
| page | Integer | No | Pagina (default 0) |
| size | Integer | No | Tamaño pagina (default 20) |

**Restriccion**: Rango de fechas maximo 6 meses.

### Salida (200 OK)

```json
{
  "content": [
    {
      "invoiceUuid": "a0000032-b032-4c32-d032-000000000032",
      "fiscalUuid": "f0000032-a032-4b32-c032-000000000032",
      "documentType": "I",
      "series": "FA",
      "folio": "100032",
      "total": 488405.69,
      "subtotal": 421039.39,
      "currency": "MXN",
      "paymentMethod": "PPD",
      "status": 1,
      "statusName": "Pendiente Addenda",
      "issueDate": "2025-12-30",
      "emisorRfc": "DEN050228EF5",
      "emisorName": "DISTRIBUIDORA ELECTRICA NACIONAL S.A. DE C.V.",
      "receptorRfc": "CGE990101GHI",
      "receptorName": "CLIENTE GENERICO S.A. DE C.V.",
      "hasAddenda": false,
      "noOrdenCompra": null,
      "noRecepcion": null
    }
  ],
  "totalElements": 118,
  "totalPages": 6,
  "number": 0,
  "size": 20
}
```

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | Campos obligatorios faltantes | Enviar tipoDocumento, fechaInicioRecepcion, fechaFinalRecepcion |
| WRN7000 | Rango de fechas excede 6 meses | Reducir el rango de fechas |

---

## 4. Buscar Complementos de Pago

**GET** `/fiscal/complementos-pago/buscar`

### Entrada (query params)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| rfcEmisor | String | No | RFC del emisor |
| page | Integer | No | Pagina (default 0) |
| size | Integer | No | Tamaño pagina (default 20) |

### Salida (200 OK)

```json
{
  "content": [
    {
      "paymentsUuid": "abcd13a4-5c57-474e-9dc8-7e2e1d97d7f6",
      "fiscalUuid": "99990001-ae01-4000-a000-000000000001",
      "version": 2.0,
      "paymentDate": "2025-06-15",
      "folio": "TEST001",
      "series": "P",
      "status": 1,
      "statusDescription": "Vigente",
      "issuerRfc": "EKU9003173C9",
      "issuerName": "Empresa Pagadora Test S.A. de C.V.",
      "receiverRfc": "LAN7008173R5",
      "receiverName": "SODIMAC MEXICO S.A. DE C.V.",
      "totalAmount": 0,
      "relatedDocumentsCount": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

## 5. Documentos Relacionados por Complemento

**GET** `/related-documents/by-payment/{paymentsUuid}`

### Entrada (path param)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| paymentsUuid | UUID | Si | UUID interno del complemento (NO el fiscal_uuid del timbre) |

### Salida (200 OK)

```json
{
  "content": [
    {
      "relatedDocumentUuid": "rd000001-0001-4001-a001-000000000001",
      "paymentUuid": "pay00001-0001-4001-a001-000000000001",
      "documentUuid": "a0000011-b011-4c11-d011-000000000011",
      "series": "FA",
      "folio": "10011",
      "currency": "MXN",
      "exchangeRate": 1.0,
      "amountPaid": 13920.00,
      "previousBalance": 13920.00,
      "remainingBalance": 0.00,
      "installmentNumber": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```
