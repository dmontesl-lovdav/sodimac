# Fiscal API - Guia Tecnica para ETL

Documento tecnico para el desarrollo del proceso ETL que consume los endpoints de registro de fiscal-api.

**Audiencia**: Equipo de desarrollo ETL (Puga)
**Version**: 1.1
**Fecha**: 2026-03-10

---

## Archivos complementarios

| Archivo | Descripcion |
|---------|-------------|
| `fiscal-api-etl.postman_collection.json` | Coleccion Postman con todos los endpoints listos para importar |
| `xml-samples/factura-sample.xml` | XML de ejemplo para factura (tipo I) |
| `xml-samples/nota-credito-sample.xml` | XML de ejemplo para nota de credito (tipo E) |
| `xml-samples/complemento-pago-sample.xml` | XML de ejemplo para complemento de pago (tipo P) |

---

## 1. Contexto del Flujo

```
Detecno (PAC)           SQL Server              ETL (Puga)           BFF DEV
     |                      |                       |                     |
     |--- XML timbrado ---->|                       |                     |
     |                      |-- tabla comprobante -->|                     |
     |                      |   (xml_file)          |                     |
     |                      |                       |-- POST /register -->|
     |                      |                       |-- POST /registrar ->|
     |                      |                       |                     |-- BD PostgreSQL
```

1. Josue descarga XMLs timbrados de Detecno (PAC) y los almacena en SQL Server (tabla `comprobante`, columna `xml_file`)
2. El ETL lee los XMLs de la tabla `comprobante`
3. Segun el `TipoDeComprobante` del XML, el ETL invoca el endpoint correspondiente via BFF
4. El BFF enruta al servicio fiscal-api, que valida, parsea y dispersa los datos en PostgreSQL (esquema `tenant_fiscal`)

**Base URL BFF DEV**: `https://dev.fbusinesscenter.com/ppsomx/fiscal`

---

## 2. Endpoints de Registro

> Los endpoints con parametros y ejemplos de respuesta estan en la **coleccion Postman** adjunta.
> Importar el archivo `fiscal-api-etl.postman_collection.json` en Postman para tener todo listo.

### 2.1 Facturas y Notas de Credito

| Atributo | Valor |
|----------|-------|
| **Metodo** | POST |
| **URL** | `{{baseUrl}}/invoices/register` |
| **Content-Type** | multipart/form-data |
| **Tipos soportados** | Factura (I), Nota de Credito (E) |

**Parametro unico**: `file` (MultipartFile) — el archivo XML del CFDI

Este endpoint solo recibe el archivo XML. La informacion del proveedor, addenda y demas datos se extraen del propio XML.

**Codigos de respuesta**:
- `RES005` / `RES003` — Factura registrada (con/sin addenda)
- `RES007` / `RES006` — NC registrada (con/sin addenda)
- `BUS034` — UUID ya registrado (duplicado)
- `BUS043` — NC referencia factura no registrada

---

### 2.2 Complementos de Pago

| Atributo | Valor |
|----------|-------|
| **Metodo** | POST |
| **URL** | `{{baseUrl}}/fiscal/complementos-pago/registrar` |
| **Content-Type** | multipart/form-data |
| **Tipo soportado** | Complemento de Pago (P) |

**Parametros de entrada**:

| Campo | Tipo | Obligatorio | Descripcion |
|-------|------|-------------|-------------|
| xmlFile | MultipartFile | Si | Archivo XML del complemento de pago |
| idProveedor | Long | Si | Numero de proveedor |
| tipoAddenda | Integer | Si | Siempre debe ser **5** |
| tipoProveedor | String | Si | Tipo de proveedor (ej: "SLI", "TRA", "IND", "SOT") |
| idUsuario | Long | Si | ID del usuario que registra |

**Nota**: A diferencia de facturas/NC, este endpoint requiere campos adicionales ademas del XML.

**Codigos de respuesta**:
- `processingStatus=SUCCESS` — Complemento registrado
- `ERR026` — UUID ya registrado (duplicado)
- `ERR028` — tipoAddenda incorrecto (debe ser 5)

---

## 3. Endpoints de Consulta

### 3.1 Buscar Facturas/NC

| Atributo | Valor |
|----------|-------|
| **Metodo** | POST |
| **URL** | `{{baseUrl}}/invoices/search` |
| **Content-Type** | application/json |

**Campos obligatorios en el body JSON**:

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| tipoDocumento | String | "I" (facturas) o "E" (notas de credito) |
| fechaInicioRecepcion | String | Fecha inicio formato YYYY-MM-DD |
| fechaFinalRecepcion | String | Fecha fin formato YYYY-MM-DD |

**Campos opcionales**: rfcEmisor, rfcReceptor, idProveedor, serie, folio, uuid, estatus, noOrdenCompra, noRecepcion, page, size

**Restriccion**: El rango de fechas NO puede exceder 6 meses (error WRN7000).

### 3.2 Buscar Complementos de Pago

| Atributo | Valor |
|----------|-------|
| **Metodo** | GET |
| **URL** | `{{baseUrl}}/fiscal/complementos-pago/buscar` |

**Query params opcionales**: rfcEmisor, page, size

### 3.3 Documentos Relacionados por Complemento

| Atributo | Valor |
|----------|-------|
| **Metodo** | GET |
| **URL** | `{{baseUrl}}/related-documents/by-payment/{paymentsUuid}` |

Retorna las facturas pagadas por un complemento. **Usar el `paymentsUuid`** (UUID interno), no el fiscal_uuid del timbre.

---

## 4. Logica de Decision del ETL

El ETL debe determinar que endpoint invocar segun el atributo `TipoDeComprobante` del nodo raiz `<cfdi:Comprobante>` del XML:

| TipoDeComprobante | Tipo de documento | Endpoint |
|-------------------|-------------------|----------|
| **I** | Factura (Ingreso) | POST /invoices/register |
| **E** | Nota de Credito (Egreso) | POST /invoices/register |
| **P** | Complemento de Pago | POST /fiscal/complementos-pago/registrar |

**Importante para NC (tipo E)**: La factura relacionada debe estar registrada previamente. El ETL debe procesar primero todas las facturas (I) y luego las NCs (E).

---

## 5. Validaciones que Realiza la API

### 5.1 Validaciones comunes (Facturas, NC y Complementos)

| Validacion | Error | Descripcion |
|------------|-------|-------------|
| Archivo vacio | ERR001 | El archivo no puede estar vacio |
| Extension | ERR002 | El archivo debe tener extension .xml |
| Estructura XML | ERR004 | El XML debe ser parseable |
| XSD | ERR006/ERR007 | El XML debe cumplir con los esquemas XSD del SAT (CFDI 4.0, Pagos 2.0, TFD 1.1) |
| RFC receptor | ERR029/BUS008 | El RFC del receptor debe estar autorizado en el catalogo |
| Version CFDI | BUS021/BUS022 | Solo CFDI 4.0 es aceptado |
| UUID duplicado | BUS034/ERR026 | No se puede registrar dos veces el mismo UUID fiscal |

### 5.2 Validaciones especificas de Facturas/NC

| Validacion | Error | Descripcion |
|------------|-------|-------------|
| Tipo comprobante | BUS023 | Debe ser I (Factura) o E (Nota de Credito) |
| Serie y Folio duplicados | BUS035 | No puede haber dos documentos con misma serie+folio |
| NC sin factura | BUS043 | La NC debe referenciar una factura ya registrada |
| Tipo relacion NC | BUS045 | La relacion de NC debe ser tipo 01 |

### 5.3 Validaciones especificas de Complementos de Pago

| Validacion | Error | Descripcion |
|------------|-------|-------------|
| Tipo comprobante | ERR020 | Debe ser P (Pago) |
| Tipo addenda | ERR028 | tipoAddenda debe ser 5 |
| Nodo Pagos | ERR024 | Debe contener al menos un elemento Pago |
| Nodo Totales | ERR022/ERR023 | Debe especificar totales con monto |
| Complemento duplicado | ERR026 | UUID fiscal ya registrado |

### Recomendacion para manejo de errores en ETL

```
Para cada XML en la tabla comprobante:
  1. Leer xml_file
  2. Detectar TipoDeComprobante (I, E, P)
  3. Invocar endpoint correspondiente
  4. Si response.success == true → marcar como procesado
  5. Si error es BUS034/ERR026/BUS035 → marcar como duplicado (no reintentar)
  6. Si error es BUS043 → marcar como pendiente (reintentar despues de facturas)
  7. Otros errores → registrar en log para revision manual
```

---

## 6. Dispersion de Datos en Base de Datos

Esquema: `tenant_fiscal` (PostgreSQL)

### 6.1 Registro de Factura/NC — Tablas afectadas

```
XML CFDI (tipo I o E)
  |
  |-- issuer (emisor)
  |     issuer_uuid, name, rfc, tax_regime
  |
  |-- receiver (receptor)
  |     receiver_uuid, name, rfc, tax_regime
  |
  |-- invoice (documento principal)
  |     invoice_uuid (PK auto), fiscal_uuid (UUID SAT),
  |     document_type (I/E), series, folio, version,
  |     subtotal, total, discount, currency, exchange_rate,
  |     payment_method, payment_form, payment_conditions,
  |     place_of_issue, issue_date, certification_date,
  |     xml_content (XML completo), status (default 1),
  |     issuer_uuid (FK), receiver_uuid (FK)
  |
  |-- addendum (addenda del documento)
  |     addendum_uuid (PK auto), invoice_uuid (FK),
  |     supplier_number, supplier_type, addenda_type,
  |     purchase_order_number, reception_number,
  |     shipping_guide_number, addendum_content (XML addenda),
  |     user_id
  |
  |-- related_cfdi (solo para NC tipo E)
  |     related_cfdi_uuid (PK auto),
  |     invoice_uuid (FK → NC),
  |     related_invoice_uuid (FK → Factura),
  |     relation_type (ej: "01")
  |
  |-- tax (impuestos del documento)
  |     tax_uuid, invoice_uuid (FK), tax_type, total_transferred, total_withheld
  |
  |-- tax_transfer / tax_withholding (detalle impuestos)
```

### 6.2 Registro de Complemento de Pago — Tablas afectadas

```
XML CFDI (tipo P)
  |
  |-- issuer (emisor)
  |-- receiver (receptor)
  |
  |-- payments (complemento - cabecera)
  |     payments_uuid (PK auto), fiscal_uuid (UUID SAT),
  |     version, payment_date, certification_date,
  |     folio, series, xml_content, status (default 1),
  |     issuer_uuid (FK), receiver_uuid (FK)
  |
  |-- addendum (addenda del complemento)
  |     addendum_uuid (PK auto), payments_uuid (FK),
  |     supplier_number, supplier_type, addenda_type (=5), user_id
  |
  |-- payment (pagos individuales)
  |     payment_uuid (PK auto), payments_uuid (FK),
  |     payment_date, payment_method, currency, amount,
  |     exchange_rate, operation_number,
  |     payer_bank_rfc, payer_account,
  |     beneficiary_bank_rfc, beneficiary_account
  |
  |-- related_documents (facturas pagadas)
  |     related_document_uuid (PK auto),
  |     payment_uuid (FK → payment),
  |     document_uuid (FK → invoice),
  |     series, folio, currency, exchange_rate,
  |     amount_paid, previous_balance, remaining_balance,
  |     installment_number
```

### 6.3 Diagrama de relaciones

```
                     FACTURAS / NC                          COMPLEMENTOS DE PAGO

                    ┌────────────┐                         ┌────────────┐
                    │   issuer   │                         │   issuer   │
                    │  (emisor)  │                         │  (emisor)  │
                    └─────┬──────┘                         └─────┬──────┘
                          │                                      │
┌──────────┐        ┌─────┴──────┐        ┌──────────┐    ┌─────┴──────┐
│ receiver │────────│  invoice   │        │ receiver │────│  payments  │
│(receptor)│        │ (factura)  │        │(receptor)│    │(complemento│
└──────────┘        └─┬───┬───┬──┘        └──────────┘    └──┬──────┬──┘
                      │   │   │                              │      │
               ┌──────┘   │   └──────┐              ┌───────┘      └──────┐
               │          │          │              │                     │
        ┌──────┴──┐  ┌────┴────┐ ┌───┴────────┐  ┌─┴────────┐    ┌──────┴──┐
        │addendum │  │  tax    │ │related_cfdi│  │ payment  │    │addendum │
        │(addenda)│  │(impuest)│ │(NC→Factura)│  │(pago ind)│    │(addenda)│
        └─────────┘  └─────────┘ └────────────┘  └────┬─────┘    └─────────┘
                                                      │
                                               ┌──────┴──────────┐
                                               │related_documents│
                                               │(facturas pagadas│
                                               └─────────────────┘
```

---

## 7. Estructura del XML

Los XMLs de ejemplo estan en la carpeta `xml-samples/`. Aqui se resumen las diferencias clave:

| Atributo | Factura (I) / NC (E) | Complemento (P) |
|----------|---------------------|------------------|
| TipoDeComprobante | I o E | P |
| SubTotal | Monto real | 0 |
| Total | Monto real | 0 |
| Moneda | MXN, USD, etc. | XXX |
| UsoCFDI | G03, G01, etc. | CP01 |
| Nodo Pagos 2.0 | No tiene | Si |
| Nodo Addenda | Si (Addenda_Sodimac) | No obligatoria |
| CfdiRelacionados | Solo NC (tipo E) | No aplica |

### UUID del TimbreFiscalDigital

- Debe usar **solo caracteres hexadecimales** (0-9, a-f)
- Formato: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
- Es la clave unica del documento fiscal ante el SAT

---

## 8. RFCs Receptores Autorizados

El RFC del receptor en el XML debe estar autorizado. Los RFCs actuales en DEV son:

| RFC | Descripcion |
|-----|-------------|
| CGE990101GHI | Receptor autorizado |
| CSD161207R2A | Receptor autorizado |
| LAN7008173R5 | SODIMAC MEXICO S.A. DE C.V. |

Si el XML tiene un RFC receptor no autorizado, el registro sera rechazado con error ERR029 o BUS008.

---

## 9. Consideraciones Tecnicas

### Orden de procesamiento

1. **Primero**: Facturas (TipoDeComprobante=I)
2. **Segundo**: Notas de Credito (TipoDeComprobante=E) — requieren que la factura relacionada ya exista
3. **Tercero**: Complementos de Pago (TipoDeComprobante=P) — independientes

### Idempotencia

- Los endpoints **no son idempotentes**. Si se envia el mismo XML dos veces, el segundo intento sera rechazado con error de duplicado
- El ETL debe llevar control de cuales XMLs ya fueron procesados

### Encoding

- Los XMLs deben estar en UTF-8
- La declaracion `<?xml version="1.0" encoding="UTF-8"?>` debe estar presente

### Timeouts

- Configurar timeout del ETL en al menos 30 segundos por request
- La validacion XSD descarga esquemas remotos del SAT, lo que puede agregar latencia

---

## 10. Queries de Verificacion Post-Carga

Despues de que el ETL procese los XMLs, ejecutar estos selects para comprobar que los datos se dispersaron correctamente.

### 10.1 Verificar Facturas (tipo I)

```sql
SELECT i.invoice_uuid, i.fiscal_uuid, i.document_type, i.series, i.folio,
       i.subtotal, i.total, i.currency, i.status, i.issue_date,
       iss.rfc AS emisor_rfc, iss.name AS emisor_nombre,
       rec.rfc AS receptor_rfc, rec.name AS receptor_nombre
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
JOIN tenant_fiscal.receiver rec ON i.receiver_uuid = rec.receiver_uuid
WHERE i.document_type = 'I'
ORDER BY i.created_at DESC
LIMIT 20;
```

### 10.2 Verificar Notas de Credito (tipo E) con factura relacionada

```sql
SELECT i.invoice_uuid, i.fiscal_uuid, i.series, i.folio, i.subtotal, i.total, i.status,
       rc.relation_type,
       i_rel.series AS fac_serie, i_rel.folio AS fac_folio,
       i_rel.fiscal_uuid AS fac_fiscal_uuid, i_rel.total AS fac_total
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.related_cfdi rc ON rc.invoice_uuid = i.invoice_uuid
LEFT JOIN tenant_fiscal.invoice i_rel ON rc.related_invoice_uuid = i_rel.invoice_uuid
WHERE i.document_type = 'E'
ORDER BY i.created_at DESC;
```

### 10.3 Verificar Complementos de Pago con pagos y documentos relacionados

```sql
SELECT p.payments_uuid, p.fiscal_uuid, p.series, p.folio, p.status, p.payment_date,
       iss.rfc AS emisor_rfc, rec.rfc AS receptor_rfc,
       pay.payment_uuid, pay.amount, pay.currency, pay.payment_method, pay.operation_number,
       rd.series AS doc_serie, rd.folio AS doc_folio,
       rd.amount_paid, rd.previous_balance, rd.remaining_balance, rd.installment_number
FROM tenant_fiscal.payments p
JOIN tenant_fiscal.issuer iss ON p.issuer_uuid = iss.issuer_uuid
JOIN tenant_fiscal.receiver rec ON p.receiver_uuid = rec.receiver_uuid
LEFT JOIN tenant_fiscal.payment pay ON pay.payments_uuid = p.payments_uuid
LEFT JOIN tenant_fiscal.related_documents rd ON rd.payment_uuid = pay.payment_uuid
ORDER BY p.created_at DESC;
```

### 10.4 Verificar Addendas

```sql
SELECT a.addendum_uuid,
       CASE WHEN a.invoice_uuid IS NOT NULL THEN 'Factura/NC' ELSE 'Complemento' END AS tipo_doc,
       COALESCE(a.invoice_uuid::text, a.payments_uuid::text) AS doc_uuid,
       a.supplier_number, a.supplier_type, a.addenda_type,
       a.purchase_order_number, a.user_id
FROM tenant_fiscal.addendum a
ORDER BY a.addenda_type, a.created_at DESC
LIMIT 20;
```

### 10.5 Conteo rapido por tipo de documento

```sql
SELECT 'Facturas (I)' AS tipo, COUNT(*) AS total FROM tenant_fiscal.invoice WHERE document_type = 'I'
UNION ALL
SELECT 'Notas Credito (E)', COUNT(*) FROM tenant_fiscal.invoice WHERE document_type = 'E'
UNION ALL
SELECT 'Complementos Pago', COUNT(*) FROM tenant_fiscal.payments
UNION ALL
SELECT 'Pagos individuales', COUNT(*) FROM tenant_fiscal.payment
UNION ALL
SELECT 'Docs relacionados', COUNT(*) FROM tenant_fiscal.related_documents
UNION ALL
SELECT 'Addendas', COUNT(*) FROM tenant_fiscal.addendum
UNION ALL
SELECT 'Emisores', COUNT(*) FROM tenant_fiscal.issuer
UNION ALL
SELECT 'Receptores', COUNT(*) FROM tenant_fiscal.receiver;
```

---

## 11. Ambiente

| Ambiente | Base URL |
|----------|----------|
| DEV | https://dev.fbusinesscenter.com/ppsomx/fiscal |
