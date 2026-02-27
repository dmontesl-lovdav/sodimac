# STM-1188: Registro de Facturas y Notas de Credito

## Descripcion

Documentacion completa del servicio de registro de facturas (I) y notas de credito (E) en el sistema fiscal.

## Endpoint

```
POST /api/invoices/register
Content-Type: multipart/form-data
```

**Parametro:**
- `file`: Archivo XML del CFDI (factura o nota de credito)

## JIRAs Relacionados

| JIRA | Descripcion | Estado |
|------|-------------|--------|
| STM-337 | Registro de Facturas y NC | Implementado |
| STM-338 | Consulta de Facturas y NC | Implementado |
| STM-339 | Actualizacion de Facturas y NC | Implementado |
| STM-1168 | NC Relacionadas en consulta | Implementado |
| STM-1169 | Datos OC/Recepcion en consulta | Implementado |
| STM-1188 | Ajuste historia de usuario registro | Documentacion |

## Flujo de Registro

```
1. Recibir archivo XML
         |
         v
2. Leer contenido XML
         |
         v
3. Detectar tipo documento (I/E)
         |
         v
4. Parsear y validar estructura CFDI
         |
         v
5. Validar version CFDI vigente (4.0)
         |
         v
6. Validar RFC receptor autorizado
         |
         v
7. Validar duplicidad UUID fiscal
         |
         v
8. Validar estructura addenda
         |
         v
9. Persistir en BD:
   - issuer (si no existe)
   - receiver (si no existe)
   - invoice
   - addendum (si tiene addenda)
   - tax + tax_transfer + tax_withholding
   - related_cfdi (solo NC)
         |
         v
10. Retornar respuesta con codigo BUS
```

## Tablas Afectadas

### Diagrama de Relaciones

```
+------------------+       +-------------------+       +------------------+
|     issuer       |       |      invoice      |       |    receiver      |
+------------------+       +-------------------+       +------------------+
| issuer_uuid   PK |<------| issuer_uuid    FK |------>| receiver_uuid PK |
| name             |       | invoice_uuid   PK |       | name             |
| rfc              |       | fiscal_uuid       |       | rfc              |
| tax_regime       |       | document_type     |       | tax_regime       |
+------------------+       | series            |       +------------------+
                           | folio             |
                           | total             |
                           | subtotal          |
                           | xml_content       |
                           | status            |
                           +-------------------+
                                    |
          +-------------------------+-------------------------+
          |                         |                         |
          v                         v                         v
+------------------+       +-------------------+       +------------------+
|    addendum      |       |       tax         |       |   related_cfdi   |
+------------------+       +-------------------+       +------------------+
| addendum_uuid PK |       | tax_uuid       PK |       | related_cfdi_uuid|
| invoice_uuid  FK |       | invoice_uuid   FK |       | invoice_uuid  FK |
| supplier_number  |       | total_transferred |       | related_inv_uuid |
| reception_number |       | total_withheld    |       | relation_type    |
| purchase_order   |       +-------------------+       +------------------+
| addenda_type     |                |
+------------------+       +--------+--------+
                           |                 |
                           v                 v
                  +----------------+  +------------------+
                  | tax_transfer   |  | tax_withholding  |
                  +----------------+  +------------------+
                  | tax_uuid    FK |  | tax_uuid      FK |
                  | tax_code       |  | tax_code         |
                  | factor_type    |  | amount           |
                  | rate_or_quota  |  +------------------+
                  | amount         |
                  | base           |
                  +----------------+
```

### Tabla: tenant_fiscal.invoice

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| invoice_uuid | UUID | PK - Identificador interno |
| fiscal_uuid | UUID | UUID del SAT (TimbreFiscalDigital) - UNIQUE |
| document_type | VARCHAR(1) | 'I' (Factura) o 'E' (NC) |
| series | VARCHAR(25) | Serie del comprobante |
| folio | VARCHAR(49) | Folio del comprobante |
| version | DECIMAL(6,3) | Version CFDI (4.0) |
| total | DECIMAL(16,2) | Monto total |
| subtotal | DECIMAL(16,2) | Subtotal |
| discount | DECIMAL(16,2) | Descuento |
| currency | VARCHAR(3) | Moneda (MXN) |
| exchange_rate | DECIMAL(18,6) | Tipo de cambio |
| payment_method | VARCHAR(3) | Metodo pago (PUE, PPD) |
| payment_form | VARCHAR(10) | Forma pago (01, 03, etc) |
| payment_conditions | VARCHAR(255) | Condiciones de pago |
| place_of_issue | VARCHAR(5) | CP lugar expedicion |
| issue_date | DATE | Fecha emision |
| certification_date | TIMESTAMP | Fecha certificacion SAT |
| xml_content | TEXT | XML completo del CFDI |
| status | INTEGER | Estatus del documento |
| issuer_uuid | UUID | FK a issuer |
| receiver_uuid | UUID | FK a receiver |
| created_at | TIMESTAMP | Fecha creacion |
| created_by | INTEGER | Usuario creador |

### Tabla: tenant_fiscal.issuer

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| issuer_uuid | UUID | PK |
| name | VARCHAR(254) | Nombre o razon social |
| rfc | VARCHAR(13) | RFC del emisor |
| tax_regime | VARCHAR(3) | Regimen fiscal |
| created_at | TIMESTAMP | Fecha creacion |

### Tabla: tenant_fiscal.receiver

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| receiver_uuid | UUID | PK |
| name | VARCHAR(254) | Nombre o razon social |
| rfc | VARCHAR(13) | RFC del receptor |
| tax_regime | VARCHAR(3) | Regimen fiscal |
| created_at | TIMESTAMP | Fecha creacion |

### Tabla: tenant_fiscal.addendum

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| addendum_uuid | UUID | PK |
| invoice_uuid | UUID | FK a invoice |
| supplier_number | DECIMAL(10) | Numero de proveedor |
| reception_number | VARCHAR(20) | Numero de recepcion |
| purchase_order_number | VARCHAR(50) | Numero de OC |
| shipping_guide_number | VARCHAR(30) | Numero de guia |
| supplier_type | VARCHAR(10) | Tipo de proveedor |
| addenda_type | INTEGER | Tipo de addenda |
| addendum_content | TEXT | Contenido XML de addenda |
| created_at | TIMESTAMP | Fecha creacion |

### Tabla: tenant_fiscal.related_cfdi

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| related_cfdi_uuid | UUID | PK |
| invoice_uuid | UUID | FK - UUID de la NC |
| related_invoice_uuid | UUID | FK - UUID de la Factura relacionada |
| relation_type | VARCHAR(3) | Tipo relacion ('01' = NC) |
| created_at | TIMESTAMP | Fecha creacion |

### Tabla: tenant_fiscal.tax

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| tax_uuid | UUID | PK |
| invoice_uuid | UUID | FK a invoice |
| total_transferred_taxes | DECIMAL(16,2) | Total impuestos trasladados |
| total_withheld_taxes | DECIMAL(16,2) | Total impuestos retenidos |
| created_at | TIMESTAMP | Fecha creacion |

### Tabla: tenant_fiscal.tax_transfer

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| tax_transfer_uuid | UUID | PK |
| tax_uuid | UUID | FK a tax |
| tax_code | VARCHAR(3) | Codigo (002=IVA, 003=IEPS) |
| factor_type | VARCHAR(10) | Tasa, Cuota, Exento |
| rate_or_quota | DECIMAL(6,6) | Tasa (0.160000 = 16%) |
| amount | DECIMAL(16,2) | Monto del impuesto |
| base | DECIMAL(16,2) | Base gravable |

### Tabla: tenant_fiscal.tax_withholding

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| tax_withholding_uuid | UUID | PK |
| tax_uuid | UUID | FK a tax |
| tax_code | VARCHAR(3) | Codigo (001=ISR, 002=IVA) |
| amount | DECIMAL(16,2) | Monto retenido |

### Catalogos de Validacion

**authorized_receiver_catalog** - RFC receptores autorizados (Sodimac)
**version_catalog** - Versiones CFDI vigentes

## Codigos de Respuesta

### Exito (BUS1xxx)

| Codigo | Mensaje |
|--------|---------|
| BUS1001 | Factura registrada exitosamente |
| BUS1002 | Factura registrada - Pendiente de Addenda |
| BUS1003 | Nota de Credito registrada exitosamente |
| BUS1004 | Nota de Credito registrada - Pendiente de Addenda |

### Error Validacion (BUS2xxx)

| Codigo | Mensaje |
|--------|---------|
| BUS2001 | XML invalido o mal formado |
| BUS2002 | RFC receptor no autorizado |
| BUS2202 | Version CFDI no vigente |
| BUS2301 | Tipo de documento no permitido |
| BUS2601 | Documento duplicado (UUID ya existe) |
| BUS2801 | NC sin CFDI relacionado |
| BUS2802 | Factura relacionada no existe |
| BUS2803 | Documento relacionado no es Factura |
| BUS2804 | Tipo de relacion invalido |

## Consultas SQL de Validacion

### 1. Verificar si una factura existe por UUID fiscal

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type,
    i.series,
    i.folio,
    i.total,
    i.status,
    i.created_at
FROM tenant_fiscal.invoice i
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E';
```

### 2. Verificar factura con emisor y receptor

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type,
    i.series,
    i.folio,
    i.total,
    i.status,
    iss.rfc AS emisor_rfc,
    iss.name AS emisor_nombre,
    rec.rfc AS receptor_rfc,
    rec.name AS receptor_nombre,
    i.created_at
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.receiver rec ON i.receiver_uuid = rec.receiver_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E';
```

### 3. Verificar factura con addenda

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    a.addendum_uuid,
    a.supplier_number AS numero_proveedor,
    a.purchase_order_number AS no_oc,
    a.reception_number AS no_recepcion,
    a.addenda_type
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E';
```

### 4. Verificar factura con impuestos

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.total,
    i.subtotal,
    t.total_transferred_taxes AS iva_trasladado,
    t.total_withheld_taxes AS impuestos_retenidos,
    tt.tax_code AS codigo_traslado,
    tt.rate_or_quota AS tasa,
    tt.amount AS monto_traslado,
    tw.tax_code AS codigo_retencion,
    tw.amount AS monto_retencion
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.tax t ON i.invoice_uuid = t.invoice_uuid
LEFT JOIN tenant_fiscal.tax_transfer tt ON t.tax_uuid = tt.tax_uuid
LEFT JOIN tenant_fiscal.tax_withholding tw ON t.tax_uuid = tw.tax_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E';
```

### 5. Verificar NC con CFDI relacionados

```sql
SELECT
    nc.invoice_uuid AS nc_uuid,
    nc.fiscal_uuid AS nc_fiscal_uuid,
    nc.series AS nc_serie,
    nc.folio AS nc_folio,
    nc.total AS nc_total,
    rc.relation_type,
    f.invoice_uuid AS factura_uuid,
    f.fiscal_uuid AS factura_fiscal_uuid,
    f.series AS factura_serie,
    f.folio AS factura_folio,
    f.total AS factura_total
FROM tenant_fiscal.invoice nc
INNER JOIN tenant_fiscal.related_cfdi rc ON nc.invoice_uuid = rc.invoice_uuid
INNER JOIN tenant_fiscal.invoice f ON rc.related_invoice_uuid = f.invoice_uuid
WHERE nc.document_type = 'E'
    AND nc.fiscal_uuid = 'UUID-DE-LA-NC';
```

### 6. Verificar todas las tablas de una factura

```sql
-- Query completo para validar registro
SELECT
    'invoice' AS tabla,
    COUNT(*) AS registros
FROM tenant_fiscal.invoice
WHERE fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E'
UNION ALL
SELECT
    'addendum' AS tabla,
    COUNT(*) AS registros
FROM tenant_fiscal.addendum a
INNER JOIN tenant_fiscal.invoice i ON a.invoice_uuid = i.invoice_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E'
UNION ALL
SELECT
    'tax' AS tabla,
    COUNT(*) AS registros
FROM tenant_fiscal.tax t
INNER JOIN tenant_fiscal.invoice i ON t.invoice_uuid = i.invoice_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E'
UNION ALL
SELECT
    'tax_transfer' AS tabla,
    COUNT(*) AS registros
FROM tenant_fiscal.tax_transfer tt
INNER JOIN tenant_fiscal.tax t ON tt.tax_uuid = t.tax_uuid
INNER JOIN tenant_fiscal.invoice i ON t.invoice_uuid = i.invoice_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E'
UNION ALL
SELECT
    'related_cfdi' AS tabla,
    COUNT(*) AS registros
FROM tenant_fiscal.related_cfdi rc
INNER JOIN tenant_fiscal.invoice i ON rc.invoice_uuid = i.invoice_uuid
WHERE i.fiscal_uuid = 'A12B2040-D8F8-4FCE-AB9D-37A636F8E59E';
```

### 7. Buscar facturas de un emisor en un periodo

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.document_type,
    i.series,
    i.folio,
    i.total,
    i.status,
    iss.rfc,
    i.issue_date,
    i.created_at
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
WHERE iss.rfc = 'AAA010101AAA'
    AND i.document_type = 'I'
    AND i.created_at BETWEEN '2025-01-01' AND '2025-12-31'
ORDER BY i.created_at DESC;
```

### 8. Verificar receptores autorizados

```sql
SELECT
    arc.authorized_receiver_id,
    arc.rfc,
    arc.name,
    arc.status,
    arc.valid_from,
    arc.valid_to
FROM tenant_fiscal.authorized_receiver_catalog arc
WHERE arc.status = 1
    AND CURRENT_DATE BETWEEN arc.valid_from AND arc.valid_to
ORDER BY arc.name;
```

### 9. Verificar versiones CFDI vigentes

```sql
SELECT
    vc.version_id,
    vc.name,
    vc.version,
    vc.document_type,
    vc.status
FROM tenant_fiscal.version_catalog vc
WHERE vc.status = 1
ORDER BY vc.document_type, vc.version DESC;
```

## Ejemplo de Request/Response

### Request

```bash
curl -X POST http://localhost:8082/api/invoices/register \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/factura.xml"
```

### Response Exitoso (BUS1001)

```json
{
  "code": "BUS1001",
  "message": "Factura registrada exitosamente",
  "success": true,
  "invoiceUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fiscalUuid": "50E9F895-F12E-4D28-8BA3-5D9B58FF10FF",
  "series": "A",
  "folio": "12345",
  "documentType": "I",
  "issuerRfc": "AAA010101AAA",
  "receiverRfc": "CSD161207R2A",
  "total": "11600.00",
  "issueDate": "2025-01-15T00:00:00",
  "hasAddenda": true,
  "pendingAddenda": false,
  "processedAt": "2025-01-15T10:30:00"
}
```

### Response Pendiente Addenda (BUS1002)

```json
{
  "code": "BUS1002",
  "message": "Factura registrada - Pendiente de Addenda",
  "success": true,
  "invoiceUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fiscalUuid": "50E9F895-F12E-4D28-8BA3-5D9B58FF10FF",
  "series": "A",
  "folio": "12345",
  "documentType": "I",
  "hasAddenda": false,
  "pendingAddenda": true
}
```

### Response Error Duplicado (BUS2601)

```json
{
  "code": "BUS2601",
  "message": "El documento con UUID 50E9F895-F12E-4D28-8BA3-5D9B58FF10FF ya se encuentra registrado en el sistema",
  "success": false
}
```

## Archivos de Implementacion

| Archivo | Descripcion |
|---------|-------------|
| InvoiceController.java | Controlador REST |
| InvoiceService.java | Interface del servicio |
| InvoiceServiceImpl.java | Implementacion del servicio |
| InvoiceRegistrationResponse.java | DTO de respuesta |
| InvoiceEntity.java | Entidad JPA |
| IssuerEntity.java | Entidad emisor |
| ReceiverEntity.java | Entidad receptor |
| AddendumEntity.java | Entidad addenda |
| TaxEntity.java | Entidad impuestos |
| RelatedCfdiEntity.java | Entidad CFDIs relacionados |
| FiscalErrorCode.java | Codigos de error |
| FiscalSuccessCode.java | Codigos de exito |

---

**JIRA:** STM-1188
**Fecha:** 2025-12-01
**Autor:** Sodimac Tech Team
**Estado:** Documentado
