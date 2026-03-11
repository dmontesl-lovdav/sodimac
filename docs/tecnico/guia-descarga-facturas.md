# Fiscal API - Guia de Descarga de Facturas (PDF y XML)

**Base URL**: `https://dev.fbusinesscenter.com/ppsomx/fiscal`

---

## 1. Descargar XML individual

**GET** `/invoices/{fiscalUuid}/xml`

### Entrada (path param + query param)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| fiscalUuid | String (UUID) | Si | UUID fiscal del TimbreFiscalDigital |
| download | boolean | No | `true`: descarga como archivo, `false` (default): visualiza en navegador |

### Salida (200 OK)

Contenido XML del documento (Content-Type: application/xml).

Si `download=true`, incluye header `Content-Disposition: attachment; filename="{uuid}.xml"`.

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| ERR001 | El archivo esta vacio | El documento no tiene xml_content en BD |
| 404 | Documento no encontrado | El UUID fiscal no existe |

---

## 2. Generar PDF de Factura/NC individual

**GET** `/api/fiscal/pdf/from-fiscal-uuid/{invoiceUuid}`

### Entrada (path param + query param)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| invoiceUuid | UUID | Si | UUID fiscal del TimbreFiscalDigital |
| inline | boolean | No | `true` (default): visualiza en navegador, `false`: descarga como archivo |

### Salida (200 OK)

Archivo PDF binario (Content-Type: application/pdf).

Nombre del archivo: `cfdi-{uuid}.pdf`

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| ERR001 | El archivo esta vacio | El documento no tiene xml_content en BD |
| 404 | UUID no encontrado | El UUID fiscal no existe en tabla invoice |
| 500 | Error interno | Error al generar el PDF (XML corrupto o incompleto) |

---

## 3. Generar PDF de Factura/NC desde archivo XML

**POST** `/api/fiscal/pdf/from-file`

### Entrada (multipart/form-data)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| file | File | Si | Archivo XML de factura CFDI 4.0 |
| inline | boolean | No | `true` (default): visualiza, `false`: descarga |

### Salida (200 OK)

Archivo PDF binario generado a partir del XML subido.

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | XML invalido o version no soportada | Solo soporta CFDI 4.0 |
| 500 | Error interno al generar el PDF | XML corrupto |

---

## 4. Descargar XMLs masivo (ZIP)

**POST** `/invoices/download/xml`

### Entrada (application/json)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| invoiceUuids | List\<UUID\> | Si | Lista de `invoice_uuid` (UUID interno, NO el fiscal) |
| documentType | String | No | "I" (Factura) o "E" (Nota de Credito) — para nombre del ZIP |

### Salida (200 OK)

Archivo ZIP (Content-Type: application/zip) con los XMLs.
Nombre de cada archivo dentro del ZIP: `Serie-Folio_UUID.xml`

### Ejemplo body

```json
{
  "invoiceUuids": [
    "a0000011-b011-4c11-d011-000000000011",
    "a0000032-b032-4c32-d032-000000000032"
  ],
  "documentType": "I"
}
```

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | Lista de UUIDs vacia | Enviar al menos un UUID |
| 500 | Error interno | Algun documento no tiene xml_content |

---

## 5. Descargar PDFs masivo (ZIP)

**POST** `/invoices/download/pdf`

### Entrada (application/json)

Mismo formato que descarga masiva de XMLs (ver seccion 4).

```json
{
  "invoiceUuids": [
    "a0000011-b011-4c11-d011-000000000011"
  ],
  "documentType": "I"
}
```

### Salida (200 OK)

Archivo ZIP con los PDFs generados.

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | Lista de UUIDs vacia | Enviar al menos un UUID |
| 500 | Error interno | Error al generar PDF de algun documento |

---

## 6. Exportar a CSV

**POST** `/invoices/export/csv`

### Entrada (application/json)

Mismos campos que el endpoint de busqueda `/invoices/search`:

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| tipoDocumento | String | Si | "I" (facturas) o "E" (notas de credito) |
| fechaInicioRecepcion | String | Si | Fecha inicio YYYY-MM-DD |
| fechaFinalRecepcion | String | Si | Fecha fin YYYY-MM-DD |
| rfcEmisor | String | No | RFC del emisor (vacio = todos) |
| estatus | Integer | No | Filtrar por estatus |

### Salida (200 OK)

Archivo CSV (Content-Type: text/csv; charset=UTF-8).

Exporta hasta 10,000 registros.

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | Campos obligatorios faltantes | Enviar tipoDocumento, fechas |
| WRN7000 | Rango de fechas excede 6 meses | Reducir rango |

---

## 7. Exportar a XLSX (Excel)

**POST** `/invoices/export/xlsx`

### Entrada (application/json + header)

Mismos campos que CSV (ver seccion 6).

Header opcional: `Accept-Language: es` (default) o `en`

### Salida (200 OK)

Archivo XLSX con 2 hojas:
- **Hoja 1 "Facturas"**: Lista de facturas encontradas
- **Hoja 2 "Notas de Credito"**: NCs relacionadas a las facturas

Exporta hasta 10,000 registros.

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | Campos obligatorios faltantes | Enviar tipoDocumento, fechas |
| 404 | Sin resultados para exportar | No hay datos en el rango |
| WRN7000 | Rango de fechas excede 6 meses | Reducir rango |

---

## 8. Generar PDF de Complemento de Pago desde archivo XML

**POST** `/api/payment/pdf/from-file`

### Entrada (multipart/form-data)

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| file | File | Si | Archivo XML del complemento de pago |
| inline | boolean | No | `true` (default): visualiza, `false`: descarga |

### Salida (200 OK)

Archivo PDF binario generado a partir del XML de complemento.

### Codigos de error

| Codigo | Mensaje | Que hacer |
|--------|---------|-----------|
| 400 | XML invalido | XML corrupto o no es complemento de pago |
| 500 | Error interno | Error al generar el PDF |

---

## Notas importantes

### UUID interno vs UUID fiscal

- **invoiceUuid** (UUID interno): PK auto-generado de la tabla `invoice`. Se usa en descarga masiva (ZIP).
- **fiscalUuid** (UUID fiscal): UUID del TimbreFiscalDigital del SAT. Se usa en descarga individual (XML y PDF).

El endpoint `/invoices/search` devuelve ambos UUIDs. Usar el correcto segun el endpoint.

### xml_content en BD

Los endpoints de descarga por UUID dependen de que el documento tenga el campo `xml_content` poblado en la tabla `invoice` o `payments`. Si el XML no se almaceno (documentos migrados o de prueba sin XML), los endpoints daran error ERR001.

### PDF de complemento de pago por UUID

El endpoint `GET /api/payment/pdf/from-uuid/{uuid}` tiene un **bug conocido**: busca en la tabla `invoice` en vez de `payments`. Por ahora usar la opcion `POST /api/payment/pdf/from-file` subiendo el XML directamente.
