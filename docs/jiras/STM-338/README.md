# STM-338: Consulta de Facturas y Notas de Credito

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-338

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Proyecto** | fiscal-api |
| **Sprint** | Sprint 2025 |

---

## Descripcion

Endpoint para busqueda de facturas y notas de credito con multiples filtros opcionales.

**Endpoint:** `POST /invoices/search`

**URL DEV:** `https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/search`

---

## Filtros Disponibles

### Obligatorios

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| rfcEmisor | String | RFC del emisor (proveedor) |
| fechaInicioRecepcion | Date | Fecha inicio (YYYY-MM-DD) |
| fechaFinalRecepcion | Date | Fecha fin (YYYY-MM-DD) |
| tipoDocumento | String | I (Factura) o E (NC) |

### Opcionales

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| rfcReceptor | String | RFC del receptor (Sodimac) |
| idProveedor | BigDecimal | Numero de proveedor |
| serie | String | Serie del comprobante |
| folio | String | Folio del comprobante |
| uuid | UUID | UUID fiscal del SAT |
| estatus | Integer | Codigo de estatus |
| noOrdenCompra | String | Numero de Orden de Compra |
| noRecepcion | String | Numero de Recepcion |

### Paginacion

| Campo | Tipo | Default |
|-------|------|---------|
| page | Integer | 0 |
| size | Integer | 20 |
| sortBy | String | createdAt |
| sortDirection | String | DESC |

---

## Filtros OC y Recepcion (Nuevo)

Los filtros `noOrdenCompra` y `noRecepcion` buscan en la tabla `addendum`:

```sql
-- Query generado para noOrdenCompra
SELECT i.* FROM invoice i
WHERE i.invoice_uuid IN (
    SELECT a.invoice_uuid FROM addendum a
    WHERE a.purchase_order_number = 'OC-2025-001234'
)
AND ... otros filtros ...

-- Query generado para noRecepcion
SELECT i.* FROM invoice i
WHERE i.invoice_uuid IN (
    SELECT a.invoice_uuid FROM addendum a
    WHERE a.reception_number = 'REC-2025-005678'
)
AND ... otros filtros ...
```

---

## Ejemplos de Uso

### Buscar Facturas por OC

```json
POST /invoices/search
{
  "rfcEmisor": "AAA010101AAA",
  "noOrdenCompra": "OC-2025-001234",
  "fechaInicioRecepcion": "2025-01-01",
  "fechaFinalRecepcion": "2025-12-31",
  "tipoDocumento": "I",
  "page": 0,
  "size": 20
}
```

### Buscar NC por Recepcion

```json
POST /invoices/search
{
  "rfcEmisor": "AAA010101AAA",
  "noRecepcion": "REC-2025-005678",
  "fechaInicioRecepcion": "2025-01-01",
  "fechaFinalRecepcion": "2025-12-31",
  "tipoDocumento": "E",
  "page": 0,
  "size": 20
}
```

### Buscar por OC y Recepcion

```json
POST /invoices/search
{
  "rfcEmisor": "AAA010101AAA",
  "noOrdenCompra": "OC-2025-001234",
  "noRecepcion": "REC-2025-005678",
  "fechaInicioRecepcion": "2025-01-01",
  "fechaFinalRecepcion": "2025-12-31",
  "tipoDocumento": "E",
  "page": 0,
  "size": 20
}
```

---

## Response

```json
{
  "content": [
    {
      "invoiceUuid": "a1b2c3d4-...",
      "fiscalUuid": "50E9F895-...",
      "documentType": "I",
      "series": "A",
      "folio": "12345",
      "total": 11600.00,
      "status": 8,
      "statusName": "Completado",
      "emisorRfc": "AAA010101AAA",
      "emisorName": "PROVEEDOR SA DE CV",
      "receptorRfc": "CSD161207R2A",
      "receptorName": "COMERCIALIZADORA SDMHC",
      "noOrdenCompra": "OC-2025-001234",
      "noRecepcion": "REC-2025-005678",
      "numeroProveedor": 1001,
      "notasCreditoRelacionadas": [...]
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

---

## Archivos Modificados

| Archivo | Descripcion |
|---------|-------------|
| InvoiceSearchRequest.java | Campos noOrdenCompra, noRecepcion |
| InvoiceSpecification.java | Subqueries para filtrar por addendum |
| InvoiceServiceImpl.java | Logs de nuevos filtros |

---

## Coleccion Postman

**Archivo:** `STM-338-Invoice-Search.postman_collection.json`

| # | Request | Descripcion |
|---|---------|-------------|
| 1 | Buscar Facturas (Basico) | Filtros obligatorios |
| 2 | Buscar NC (Basico) | Filtros obligatorios tipo E |
| 3 | Buscar por RFC Receptor | Filtro adicional |
| 4 | Buscar por ID Proveedor | Filtro supplier_number |
| 5 | Buscar por Serie y Folio | Filtros especificos |
| 6 | Buscar por UUID Fiscal | Busqueda exacta |
| 7 | Buscar por Estatus | Filtrar por status |
| 8 | Busqueda Combinada | Todos los filtros |
| 9 | Paginacion - Pagina 2 | Test paginacion |
| 10 | Error - Sin RFC Emisor | Validacion |
| 11 | Error - Sin Tipo Documento | Validacion |
| 12 | Buscar por Orden de Compra | **NUEVO** |
| 13 | Buscar por Recepcion | **NUEVO** |
| 14 | Buscar NC por OC y Recepcion | **NUEVO** |

---

## Consultas SQL de Validacion

### Buscar facturas por OC

```sql
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    a.purchase_order_number,
    a.reception_number
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE a.purchase_order_number = 'OC-2025-001234'
ORDER BY i.created_at DESC;
```

### Buscar NC por recepcion

```sql
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    a.purchase_order_number,
    a.reception_number
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'E'
    AND a.reception_number = 'REC-2025-005678'
ORDER BY i.created_at DESC;
```

---

**Fecha:** 2025-12-10
**Autor:** Sodimac Tech Team
