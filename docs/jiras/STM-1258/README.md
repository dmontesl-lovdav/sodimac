# STM-1258: Ajuste al Servicio de Facturacion y Nota de Credito para Visualizar y Actualizar Datos en el Front

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1258

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado (Documentacion) |
| **Modulo** | Fiscal API |
| **BFF** | https://dev.fbusinesscenter.com/ppsomx/fiscal |
| **API Backend** | mrch.backend.somx.fiscal-api (Puerto 8082) |
| **Fecha** | 2025-12-18 |

---

## Descripcion

Servicio de facturacion y nota de credito para visualizar y actualizar datos desde el frontend. La funcionalidad ya esta implementada en JIRAs anteriores.

### Funcionalidad Implementada

| JIRA | Funcionalidad | Estado |
|------|---------------|--------|
| STM-337 | Registro de facturas/NC desde XML | Completado |
| STM-338 | Busqueda y visualizacion de facturas/NC | Completado |
| STM-339 | Actualizacion de facturas/NC | Completado |
| STM-771 | XML completo en respuestas | Completado |
| STM-1168 | Notas de credito relacionadas | Completado |
| STM-1169 | Datos de addenda en respuestas | Completado |

---

## Endpoints Disponibles

### BFF URL Base
```
https://dev.fbusinesscenter.com/ppsomx/fiscal
```

### Visualizacion
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/invoices/search` | Buscar facturas y NC con filtros |

### Actualizacion
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| PUT | `/invoices` | Actualizar estatus y/o addenda |

---

## Como Probar

### 1. Buscar Facturas (POST /invoices/search)

```bash
curl -X POST https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/search \
  -H "Content-Type: application/json" \
  -d '{
    "rfcEmisor": "AAA010101AAA",
    "fechaInicioRecepcion": "2025-01-01",
    "fechaFinalRecepcion": "2025-12-31",
    "tipoDocumento": "I",
    "page": 0,
    "size": 20
  }'
```

**Filtros Obligatorios:**
- `rfcEmisor`: RFC del emisor/proveedor
- `fechaInicioRecepcion`: Fecha inicio (YYYY-MM-DD)
- `fechaFinalRecepcion`: Fecha fin (YYYY-MM-DD)
- `tipoDocumento`: I (Factura) o E (Nota de Credito)

**Filtros Opcionales:**
- `rfcReceptor`: RFC del receptor
- `idProveedor`: Numero de proveedor (Supplier Number)
- `serie`: Serie del documento
- `folio`: Folio del documento
- `uuid`: UUID fiscal del SAT
- `estatus`: Codigo de estatus

### 2. Actualizar Factura/NC (PUT /invoices)

```bash
curl -X PUT https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices \
  -H "Content-Type: application/json" \
  -d '{
    "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "numeroProveedor": 1234567890,
    "estatus": 2,
    "idUsuarioActualizacion": 12345,
    "addenda": {
      "noOc": "OC-12345",
      "noRecepcion": "REC-001"
    }
  }'
```

---

## Estructura de Respuesta (Visualizacion)

```json
{
  "content": [
    {
      "invoiceUuid": "uuid-interno-bd",
      "fiscalUuid": "uuid-sat-timbrado",
      "documentType": "I",
      "series": "A",
      "folio": "12345",
      "version": 4.0,
      "issueDate": "2025-01-15",
      "certificationDate": "2025-01-15T10:30:00",
      "total": 11600.00,
      "subtotal": 10000.00,
      "discount": 0.00,
      "currency": "MXN",
      "paymentMethod": "PUE",
      "paymentForm": "01",
      "status": 1,
      "statusName": "Pendiente Addenda",

      "emisorRfc": "AAA010101AAA",
      "emisorName": "PROVEEDOR SA DE CV",
      "emisorTaxRegime": "601",

      "receptorRfc": "CSD161207R2A",
      "receptorName": "COMERCIALIZADORA SDMHC",
      "receptorTaxRegime": "601",

      "hasAddenda": true,
      "addendaType": 5,
      "noOrdenCompra": "OC-12345",
      "noRecepcion": "REC-001",
      "numeroProveedor": 1234567890,

      "xmlContent": "<cfdi:Comprobante>...</cfdi:Comprobante>",

      "notasCreditoRelacionadas": [
        {
          "ncUuid": "nc-uuid",
          "ncFiscalUuid": "nc-fiscal-uuid",
          "ncTotal": 100.00
        }
      ]
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

## Codigos de Estatus

### Facturas (tipoDocumento = I)
| Codigo | Nombre | Transiciones Permitidas |
|--------|--------|-------------------------|
| 0 | Rechazo Comercial | - |
| 1 | Pendiente Addenda | 2, 3, 13 |
| 2 | Recibido Parcial | 3, 13 |
| 3 | Pendiente Contabilizar | 4 |
| 4 | Proceso Descarga | 5, 11 |
| 5 | Desglose Factura | 6, 11 |
| 6 | Pendiente Envio | 7, 11 |
| 7 | Aplicado | 8 |
| 8 | Completado | - |
| 11 | Rechazo Contable | 7 |
| 12 | No valido fiscal | - |
| 13 | Cerrado | - |

### Notas de Credito (tipoDocumento = E)
| Codigo | Nombre | Transiciones Permitidas |
|--------|--------|-------------------------|
| 0 | Rechazo Comercial | - |
| 1 | Pendiente Addenda | 2, 3 |
| 2 | Recibido Parcial | 3 |
| 3 | Pendiente Contabilizar | 4 |
| 4 | Proceso Descarga | 5, 11 |
| 5 | Desglose NC | 6, 11 |
| 6 | Pendiente Envio | 7, 11 |
| 7 | Aplicado | 8 |
| 8 | Completado | - |
| 11 | Rechazo Contable | 7 |
| 12 | No valido fiscal | - |

---

## Consultas SQL de Apoyo

Si los endpoints no devuelven resultados, usa estas consultas para obtener datos de prueba.

> **Nota**: Las relaciones entre tablas son por UUID, no por ID.

### Obtener RFCs de Emisores disponibles
```sql
-- Obtener RFCs de emisores con facturas
SELECT DISTINCT i.rfc, i.name, COUNT(*) as total_facturas
FROM tenant_fiscal.issuer i
INNER JOIN tenant_fiscal.invoice inv ON i.issuer_uuid = inv.issuer_uuid
GROUP BY i.rfc, i.name
ORDER BY total_facturas DESC
LIMIT 10;
```

### Obtener Facturas para probar busqueda
```sql
-- Facturas recientes con todos los datos
SELECT
    inv.invoice_uuid,
    inv.fiscal_uuid,
    inv.document_type,
    inv.series,
    inv.folio,
    inv.total,
    inv.status,
    inv.created_at,
    iss.rfc as emisor_rfc,
    iss.name as emisor_name,
    rec.rfc as receptor_rfc,
    add.supplier_number as numero_proveedor
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.receiver rec ON inv.receiver_uuid = rec.receiver_uuid
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.document_type = 'I'
ORDER BY inv.created_at DESC
LIMIT 10;
```

### Obtener Notas de Credito para probar busqueda
```sql
-- Notas de Credito recientes
SELECT
    inv.invoice_uuid,
    inv.fiscal_uuid,
    inv.document_type,
    inv.series,
    inv.folio,
    inv.total,
    inv.status,
    inv.created_at,
    iss.rfc as emisor_rfc,
    add.supplier_number as numero_proveedor
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.document_type = 'E'
ORDER BY inv.created_at DESC
LIMIT 10;
```

### Obtener datos para actualizar factura
```sql
-- Facturas que se pueden actualizar (no completadas/cerradas)
SELECT
    inv.fiscal_uuid,
    inv.document_type,
    inv.status,
    add.supplier_number as numero_proveedor,
    add.purchase_order_number as no_oc,
    add.reception_number as no_recepcion
FROM tenant_fiscal.invoice inv
LEFT JOIN tenant_fiscal.addendum add ON inv.invoice_uuid = add.invoice_uuid
WHERE inv.status NOT IN (8, 12, 13)  -- No completadas, no invalidas, no cerradas
  AND inv.document_type = 'I'
ORDER BY inv.created_at DESC
LIMIT 10;
```

### Verificar rango de fechas con datos
```sql
-- Ver rango de fechas con facturas disponibles
SELECT
    MIN(created_at) as fecha_mas_antigua,
    MAX(created_at) as fecha_mas_reciente,
    COUNT(*) as total_registros
FROM tenant_fiscal.invoice;
```

### Obtener facturas por RFC especifico
```sql
-- Facturas de un emisor especifico (reemplazar RFC)
SELECT
    inv.fiscal_uuid,
    inv.series,
    inv.folio,
    inv.total,
    inv.status,
    DATE(inv.created_at) as fecha
FROM tenant_fiscal.invoice inv
INNER JOIN tenant_fiscal.issuer iss ON inv.issuer_uuid = iss.issuer_uuid
WHERE iss.rfc = 'SOD970101ABC'
  AND inv.document_type = 'I'
ORDER BY inv.created_at DESC
LIMIT 20;
```

### Verificar estructura de addenda
```sql
-- Ver estructura de addendas disponibles
SELECT
    inv.fiscal_uuid,
    inv.document_type,
    add.addenda_type,
    add.supplier_number,
    add.purchase_order_number,
    add.reception_number,
    add.shipping_guide_number
FROM tenant_fiscal.addendum add
INNER JOIN tenant_fiscal.invoice inv ON add.invoice_uuid = inv.invoice_uuid
WHERE add.supplier_number IS NOT NULL
LIMIT 10;
```

---

## Archivos Relacionados

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-1258 - Ajuste Facturacion NC.postman_collection.json](./STM-1258%20-%20Ajuste%20Facturacion%20NC.postman_collection.json) | Coleccion Postman (apunta a BFF DEV) |

### Consultas SQL
| Archivo | Descripcion |
|---------|-------------|
| [queries-validacion.sql](./queries-validacion.sql) | Consultas para obtener datos de prueba |

### Variables Postman
| Variable | Valor | Descripcion |
|----------|-------|-------------|
| `bff_fiscal_url` | https://dev.fbusinesscenter.com/ppsomx/fiscal | URL del BFF |
| `rfc_emisor` | (obtener de BD) | RFC del emisor para busqueda |
| `id_proveedor` | (obtener de BD) | Numero de proveedor |
| `fiscal_uuid` | (obtener de BD) | UUID fiscal para actualizar |

### Documentacion Relacionada
| JIRA | Descripcion |
|------|-------------|
| [STM-337](../STM-337/README.md) | Registro de facturas/NC |
| [STM-338](../STM-338/README.md) | Busqueda de facturas/NC |
| [STM-339](../STM-339/README.md) | Actualizacion de facturas/NC |
| [STM-771](../STM-771/README.md) | XML completo en respuestas |
| [STM-1168](../STM-1168/README.md) | NC relacionadas |
| [STM-1169](../STM-1169/README.md) | Datos de addenda |

---

## Notas Tecnicas

- **BFF**: https://dev.fbusinesscenter.com/ppsomx/fiscal
- **API Backend**: fiscal-api (Puerto 8082)
- **Tecnologia**: Java 21, Spring Boot 3.x
- **Base de Datos**: PostgreSQL (schema: tenant_fiscal)

### Tablas Principales
| Tabla | Descripcion |
|-------|-------------|
| `invoice` | Facturas y Notas de Credito |
| `issuer` | Datos del emisor (proveedor) |
| `receiver` | Datos del receptor (Sodimac) |
| `addendum` | Datos de addenda comercial |
| `related_cfdi` | Relaciones entre documentos |

---

## Checklist de Validacion

- [ ] Endpoint de busqueda responde correctamente
- [ ] Endpoint de actualizacion responde correctamente
- [ ] Datos de addenda se muestran en respuesta
- [ ] XML completo se incluye en respuesta
- [ ] NC relacionadas se muestran para facturas
- [ ] Transiciones de estatus funcionan correctamente

