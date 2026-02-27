# STM-1169: Mostrar Datos de OC y Recepcion en Consulta de Facturas

## Descripcion

Este documento describe la funcionalidad para mostrar los datos de negocio de la Addenda (Orden de Compra, Recepcion, Proveedor) cuando se consultan Facturas o Notas de Credito.

## Objetivo

Cuando se realiza una busqueda de documentos fiscales mediante el endpoint `POST /api/invoices/search`, el sistema debe incluir en la respuesta los datos de negocio extraidos de la Addenda:

- Numero de Orden de Compra (noOrdenCompra)
- Numero de Recepcion (noRecepcion)
- Numero de Proveedor (numeroProveedor)
- Tipo de Proveedor (tipoProveedor)
- Guia de Entrega (guiaEntrega)

Esto permite al usuario:
- Ver la OC asociada a una Factura sin consultas adicionales
- Identificar la Recepcion que ampara la Factura
- Conocer el numero de proveedor en el sistema Sodimac
- Filtrar y relacionar documentos con procesos de compra

## Contexto de Negocio

Las Facturas recibidas de proveedores incluyen una Addenda con informacion de negocio especifica de Sodimac. Esta informacion es crucial para:

1. **Conciliacion contable**: Relacionar Facturas con OC y Recepciones
2. **Trazabilidad**: Seguimiento del ciclo de compra completo
3. **Validacion**: Verificar que la Factura corresponde a mercancia recibida
4. **Reporteria**: Generar reportes por proveedor, OC o periodo

### Datos de Addenda Sodimac

| Campo | Descripcion | Ejemplo |
|-------|-------------|---------|
| noOrdenCompra | Numero de Orden de Compra | "OC-2025-001234" |
| noRecepcion | Numero de Recepcion de mercancia | "REC-2025-005678" |
| numeroProveedor | ID del proveedor en sistema Sodimac | 1001 |
| tipoProveedor | Clasificacion del proveedor | "NACIONAL" |
| guiaEntrega | Numero de guia de transporte | "FEDEX-123456789" |

## Arquitectura de Tablas

### Diagrama de Relaciones

```
+------------------+       +-------------------+
|     invoice      |       |     addendum      |
+------------------+       +-------------------+
| invoice_uuid  PK |<------| invoice_uuid   FK |
| fiscal_uuid      |       | addendum_uuid  PK |
| document_type    |       | purchase_order_number |
| series           |       | reception_number  |
| folio            |       | supplier_number   |
| total            |       | supplier_type     |
| status           |       | shipping_guide_number |
+------------------+       | addenda_type      |
                           +-------------------+
```

### Tabla: invoice
Documento fiscal (Factura o NC).

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| invoice_uuid | UUID | PK - Identificador interno |
| fiscal_uuid | UUID | UUID fiscal del SAT |
| document_type | VARCHAR(5) | 'I' o 'E' |
| series | VARCHAR(25) | Serie |
| folio | VARCHAR(40) | Folio |
| total | DECIMAL(18,6) | Monto total |
| status | INTEGER | Estatus |

### Tabla: addendum
Datos de negocio de la Addenda.

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| addendum_uuid | UUID | PK - Identificador de addenda |
| invoice_uuid | UUID | FK - Referencia a invoice |
| purchase_order_number | VARCHAR(50) | Numero de OC |
| reception_number | VARCHAR(20) | Numero de recepcion |
| supplier_number | DECIMAL(10) | ID del proveedor |
| supplier_type | VARCHAR(10) | Tipo de proveedor |
| shipping_guide_number | VARCHAR(30) | Numero de guia |
| addenda_type | INTEGER | Tipo de addenda |
| addendum_content | TEXT | Contenido XML de addenda |

## Implementacion Tecnica

### Entidad: AddendumEntity

```java
@Entity
@Table(name = "addendum")
public class AddendumEntity {
    @Id
    private UUID addendumUuid;

    @Column(name = "invoice_uuid")
    private UUID invoiceUuid;

    @Column(name = "purchase_order_number")
    private String purchaseOrderNumber;  // noOrdenCompra

    @Column(name = "reception_number")
    private String receptionNumber;  // noRecepcion

    @Column(name = "supplier_number")
    private BigDecimal supplierNumber;  // numeroProveedor

    @Column(name = "supplier_type")
    private String supplierType;  // tipoProveedor

    @Column(name = "shipping_guide_number")
    private String shippingGuideNumber;  // guiaEntrega

    @Column(name = "addenda_type")
    private Integer addendaType;
}
```

### Response: InvoiceSearchResponse

Campos agregados para STM-1169:

```java
// ========== DATOS DE NEGOCIO ADDENDA (STM-1169) ==========

@Schema(description = "Numero de Orden de Compra asociada a la factura")
private String noOrdenCompra;

@Schema(description = "Numero de Recepcion asociada a la factura")
private String noRecepcion;

@Schema(description = "Numero de proveedor")
private BigDecimal numeroProveedor;

@Schema(description = "Tipo de proveedor")
private String tipoProveedor;

@Schema(description = "Numero de guia de entrega")
private String guiaEntrega;
```

### Servicio: InvoiceServiceImpl

Mapeo en el metodo mapToSearchResponse:

```java
private InvoiceSearchResponse mapToSearchResponse(InvoiceEntity invoice,
        IssuerEntity issuer, ReceiverEntity receiver, AddendumEntity addenda) {

    InvoiceSearchResponse.InvoiceSearchResponseBuilder builder =
        InvoiceSearchResponse.builder()
            // ... otros campos ...

            // STM-1169: Datos de negocio de addenda
            .noOrdenCompra(addenda != null ? addenda.getPurchaseOrderNumber() : null)
            .noRecepcion(addenda != null ? addenda.getReceptionNumber() : null)
            .numeroProveedor(addenda != null ? addenda.getSupplierNumber() : null)
            .tipoProveedor(addenda != null ? addenda.getSupplierType() : null)
            .guiaEntrega(addenda != null ? addenda.getShippingGuideNumber() : null);

    return builder.build();
}
```

## Consultas SQL de Validacion

### Ver Facturas con datos de OC y Recepcion

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    a.purchase_order_number as no_orden_compra,
    a.reception_number as no_recepcion,
    a.supplier_number as numero_proveedor,
    a.supplier_type as tipo_proveedor,
    a.shipping_guide_number as guia_entrega
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.addendum a
    ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'I'
ORDER BY i.created_at DESC;
```

### Buscar Facturas por Orden de Compra

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    iss.rfc as emisor_rfc,
    iss.name as emisor_nombre,
    a.purchase_order_number,
    a.reception_number
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss
    ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.addendum a
    ON i.invoice_uuid = a.invoice_uuid
WHERE a.purchase_order_number = 'OC-2025-001234'
    AND i.document_type = 'I'
ORDER BY i.created_at DESC;
```

### Buscar Facturas por Numero de Recepcion

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    iss.rfc as emisor_rfc,
    a.purchase_order_number,
    a.reception_number
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.issuer iss
    ON i.issuer_uuid = iss.issuer_uuid
INNER JOIN tenant_fiscal.addendum a
    ON i.invoice_uuid = a.invoice_uuid
WHERE a.reception_number = 'REC-2025-005678'
    AND i.document_type = 'I';
```

### Listar Facturas por Proveedor con OC

```sql
SELECT
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    a.supplier_number,
    a.supplier_type,
    a.purchase_order_number,
    a.reception_number,
    i.created_at
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.addendum a
    ON i.invoice_uuid = a.invoice_uuid
WHERE a.supplier_number = 1001
    AND i.document_type = 'I'
ORDER BY i.created_at DESC;
```

### Resumen de Facturas por OC

```sql
SELECT
    a.purchase_order_number,
    COUNT(i.invoice_uuid) as cantidad_facturas,
    SUM(i.total) as total_facturado,
    MIN(i.created_at) as primera_factura,
    MAX(i.created_at) as ultima_factura
FROM tenant_fiscal.invoice i
INNER JOIN tenant_fiscal.addendum a
    ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'I'
    AND a.purchase_order_number IS NOT NULL
GROUP BY a.purchase_order_number
ORDER BY total_facturado DESC;
```

### Verificar Facturas sin datos de Addenda

```sql
SELECT
    i.invoice_uuid,
    i.fiscal_uuid,
    i.series,
    i.folio,
    i.total,
    i.status,
    CASE
        WHEN a.addendum_uuid IS NULL THEN 'SIN ADDENDA'
        WHEN a.purchase_order_number IS NULL THEN 'SIN OC'
        ELSE 'OK'
    END as estado_addenda
FROM tenant_fiscal.invoice i
LEFT JOIN tenant_fiscal.addendum a
    ON i.invoice_uuid = a.invoice_uuid
WHERE i.document_type = 'I'
    AND (a.addendum_uuid IS NULL OR a.purchase_order_number IS NULL)
ORDER BY i.created_at DESC;
```

## Flujo de Datos

### Registro de Factura con Addenda (PUT /api/invoices)

```
1. Usuario envia datos de addenda en request
   {
     "addenda": {
       "noOc": "OC-2025-001234",
       "noRecepcion": "REC-2025-005678",
       "idProveedor": 1001,
       "tipoProveedor": "NACIONAL",
       "idGuiaEntrega": "FEDEX-123456789"
     }
   }
                    |
                    v
2. Sistema actualiza registro en tabla addendum
   - purchase_order_number = noOc
   - reception_number = noRecepcion
   - supplier_number = idProveedor
   - supplier_type = tipoProveedor
   - shipping_guide_number = idGuiaEntrega
                    |
                    v
3. Datos disponibles para consulta
```

### Consulta de Facturas (POST /api/invoices/search)

```
1. Usuario solicita busqueda
                    |
                    v
2. Sistema busca en invoice + issuer + receiver + addendum
                    |
                    v
3. Mapeo de resultados incluye datos de addenda:
   - noOrdenCompra <- addendum.purchase_order_number
   - noRecepcion <- addendum.reception_number
   - numeroProveedor <- addendum.supplier_number
   - tipoProveedor <- addendum.supplier_type
   - guiaEntrega <- addendum.shipping_guide_number
                    |
                    v
4. Response incluye todos los datos
```

## Ejemplo de Respuesta

### Request
```json
POST /api/invoices/search
{
  "rfcEmisor": "AAA010101AAA",
  "fechaInicioRecepcion": "2025-01-01",
  "fechaFinalRecepcion": "2025-12-31",
  "tipoDocumento": "I",
  "page": 0,
  "size": 20
}
```

### Response (con datos de OC/Recepcion)
```json
{
  "content": [
    {
      "invoiceUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "fiscalUuid": "50E9F895-F12E-4D28-8BA3-5D9B58FF10FF",
      "documentType": "I",
      "series": "A",
      "folio": "12345",
      "total": 11600.00,
      "status": 8,
      "statusName": "Completado",
      "emisorRfc": "AAA010101AAA",
      "emisorName": "PROVEEDOR EJEMPLO SA DE CV",

      "hasAddenda": true,
      "addendaType": 5,
      "addendaTypeName": "Addenda Sodimac",

      "noOrdenCompra": "OC-2025-001234",
      "noRecepcion": "REC-2025-005678",
      "numeroProveedor": 1001,
      "tipoProveedor": "NACIONAL",
      "guiaEntrega": "FEDEX-123456789",

      "notasCreditoRelacionadas": []
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

## Mapeo de Campos

| Campo Response | Campo Addendum Entity | Campo BD addendum |
|----------------|----------------------|-------------------|
| noOrdenCompra | purchaseOrderNumber | purchase_order_number |
| noRecepcion | receptionNumber | reception_number |
| numeroProveedor | supplierNumber | supplier_number |
| tipoProveedor | supplierType | supplier_type |
| guiaEntrega | shippingGuideNumber | shipping_guide_number |

## Casos de Prueba

### Caso 1: Factura con Addenda completa
- Buscar Factura que tenga todos los datos de addenda
- Verificar que response incluye noOrdenCompra, noRecepcion, etc.

### Caso 2: Factura sin Addenda
- Buscar Factura que no tenga addenda registrada
- Verificar que campos de negocio son null

### Caso 3: Factura con Addenda parcial
- Buscar Factura con addenda pero sin OC o Recepcion
- Verificar que campos disponibles aparecen, otros son null

### Caso 4: Nota de Credito con Addenda
- Buscar NC que tenga addenda
- Verificar que datos de addenda aparecen correctamente

### Caso 5: Actualizar Addenda de Factura
- Usar PUT /api/invoices para actualizar noOc y noRecepcion
- Consultar Factura y verificar nuevos valores

## Relacion con Otros JIRAs

| JIRA | Descripcion | Relacion |
|------|-------------|----------|
| STM-337 | Registro de Factura | Guarda datos iniciales de addenda |
| STM-339 | Actualizacion de Addenda | Permite modificar OC, Recepcion |
| STM-338 | Consulta de Facturas | Endpoint donde se muestran los datos |
| STM-1168 | NC Relacionadas | Complementa la respuesta de busqueda |

## Checklist de Validacion

- [ ] Response incluye noOrdenCompra cuando addenda tiene OC
- [ ] Response incluye noRecepcion cuando addenda tiene recepcion
- [ ] Response incluye numeroProveedor cuando addenda tiene supplier
- [ ] Response incluye tipoProveedor cuando addenda tiene tipo
- [ ] Response incluye guiaEntrega cuando addenda tiene guia
- [ ] Campos son null cuando Factura no tiene addenda
- [ ] Funciona para Facturas (tipoDocumento = "I")
- [ ] Funciona para NC (tipoDocumento = "E")
- [ ] PUT /api/invoices actualiza correctamente los campos

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| InvoiceSearchResponse.java | Agregados campos noOrdenCompra, noRecepcion, numeroProveedor, tipoProveedor, guiaEntrega |
| InvoiceServiceImpl.java | Modificado mapToSearchResponse para incluir datos de addenda |

---

**JIRA:** STM-1169
**Fecha:** 2025-12-01
**Autor:** Sodimac Tech Team
**Estado:** Implementado
