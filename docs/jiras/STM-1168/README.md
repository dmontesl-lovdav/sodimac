# STM-1168: Presentar Notas de Credito Relacionadas a Facturas

## Descripcion

Este documento describe la funcionalidad para mostrar las Notas de Credito (NC) relacionadas cuando se consulta una Factura.

## Objetivo

Cuando se realiza una busqueda de Facturas (tipoDocumento = "I") mediante el endpoint `POST /api/invoices/search`, el sistema debe incluir en la respuesta la lista de Notas de Credito que estan relacionadas a cada Factura.

Esto permite al usuario visualizar de manera inmediata:
- Cuantas NC afectan a una Factura
- El monto total acreditado
- El estatus de cada NC relacionada
- El tipo de relacion (devolucion, descuento, etc.)

## Contexto de Negocio

En el proceso de facturacion mexicana (CFDI 4.0), cuando se emite una Nota de Credito, esta debe referenciar a la Factura original mediante el nodo `CfdiRelacionados`. Este nodo contiene:
- **TipoRelacion**: Codigo que indica el tipo de relacion (01 = Nota de credito)
- **CfdiRelacionado**: UUID de la factura original

### Ejemplo de XML CfdiRelacionados
```xml
<cfdi:CfdiRelacionados TipoRelacion="01">
    <cfdi:CfdiRelacionado>50E9F895-F12E-4D28-8BA3-5D9B58FF10FF</cfdi:CfdiRelacionado>
</cfdi:CfdiRelacionados>
```

## Arquitectura de Tablas

### Diagrama de Relaciones

```
+------------------+       +-------------------+       +------------------+
|     invoice      |       |   related_cfdi    |       |     invoice      |
|   (Factura I)    |       |                   |       |   (NC tipo E)    |
+------------------+       +-------------------+       +------------------+
| invoice_uuid  PK |<------| related_inv_uuid  |       | invoice_uuid  PK |
| fiscal_uuid      |       | invoice_uuid   FK |------>| fiscal_uuid      |
| document_type='I'|       | relation_type     |       | document_type='E'|
| series           |       | created_at        |       | series           |
| folio            |       +-------------------+       | folio            |
| total            |                                   | total            |
| status           |                                   | status           |
+------------------+                                   +------------------+
```

### Tabla: invoice
Almacena tanto Facturas (I) como Notas de Credito (E).

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| invoice_uuid | UUID | PK - Identificador interno |
| fiscal_uuid | UUID | UUID fiscal del SAT (TimbreFiscalDigital) |
| document_type | VARCHAR(5) | Tipo: 'I' (Factura) o 'E' (NC) |
| series | VARCHAR(25) | Serie del comprobante |
| folio | VARCHAR(40) | Folio del comprobante |
| total | DECIMAL(18,6) | Monto total |
| status | INTEGER | Codigo de estatus |
| issue_date | DATE | Fecha de emision |
| created_at | TIMESTAMP | Fecha de registro en el sistema |

### Tabla: related_cfdi
Almacena las relaciones entre documentos fiscales.

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| related_cfdi_uuid | UUID | PK - Identificador de la relacion |
| invoice_uuid | UUID | FK - UUID de la NC que referencia |
| related_invoice_uuid | UUID | FK - UUID de la Factura referenciada |
| relation_type | VARCHAR(2) | Tipo de relacion ('01', '02', etc.) |
| created_at | TIMESTAMP | Fecha de creacion del registro |

## Tipos de Relacion (TipoRelacion SAT)

| Codigo | Nombre | Descripcion |
|--------|--------|-------------|
| 01 | Nota de credito | NC que aplica descuento o devolucion |
| 02 | Nota de debito | ND que incrementa el monto |
| 03 | Devolucion de mercancia | Devolucion de productos |
| 04 | Sustitucion de CFDI previo | Reemplazo de documento |
| 05 | Traslados de mercancias | Para carta porte |
| 06 | Factura por traslados previos | Complemento de traslado |
| 07 | CFDI por aplicacion de anticipo | Anticipo aplicado |

## Implementacion Tecnica

### Entidad: RelatedCfdiEntity

```java
@Entity
@Table(name = "related_cfdi")
public class RelatedCfdiEntity {
    @Id
    private UUID relatedCfdiUuid;

    @Column(name = "invoice_uuid")
    private UUID invoiceUuid;  // UUID de la NC

    @Column(name = "related_invoice_uuid")
    private UUID relatedInvoiceUuid;  // UUID de la Factura

    @Column(name = "relation_type")
    private String relationType;  // "01", "02", etc.
}
```

### Repositorio: RelatedCfdiRepository

```java
public interface RelatedCfdiRepository extends JpaRepository<RelatedCfdiEntity, UUID> {

    // Buscar NC que referencian a una Factura especifica
    List<RelatedCfdiEntity> findByRelatedInvoiceUuid(UUID relatedInvoiceUuid);

    // Buscar las relaciones de una NC especifica
    List<RelatedCfdiEntity> findByInvoiceUuid(UUID invoiceUuid);
}
```

### DTO: NotaCreditoRelacionadaDto

```java
@Data
@Builder
public class NotaCreditoRelacionadaDto {
    private UUID invoiceUuid;      // UUID interno de la NC
    private UUID fiscalUuid;       // UUID fiscal de la NC
    private String serie;          // Serie de la NC
    private String folio;          // Folio de la NC
    private BigDecimal total;      // Monto de la NC
    private String tipoRelacion;   // Codigo: "01", "02", etc.
    private String tipoRelacionNombre;  // Nombre: "Nota de credito"
    private Integer status;        // Estatus de la NC
    private String statusNombre;   // Nombre del estatus
    private LocalDate fechaEmision;      // Fecha emision NC
    private LocalDateTime fechaRecepcion; // Fecha registro NC
}
```

### Response: InvoiceSearchResponse

Campo agregado para STM-1168:

```java
@Schema(description = "Lista de Notas de Credito relacionadas (solo para tipoDocumento='I')")
private List<NotaCreditoRelacionadaDto> notasCreditoRelacionadas;
```

### Servicio: InvoiceServiceImpl

Metodo para obtener NC relacionadas:

```java
private List<NotaCreditoRelacionadaDto> findNotasCreditoRelacionadas(UUID facturaUuid) {
    // 1. Buscar relaciones donde la Factura es la referenciada
    List<RelatedCfdiEntity> relaciones = relatedCfdiRepository
        .findByRelatedInvoiceUuid(facturaUuid);

    if (relaciones.isEmpty()) {
        return Collections.emptyList();
    }

    // 2. Obtener UUIDs de las NC
    List<UUID> ncUuids = relaciones.stream()
        .map(RelatedCfdiEntity::getInvoiceUuid)
        .collect(Collectors.toList());

    // 3. Buscar datos de las NC
    List<InvoiceEntity> notasCredito = invoiceRepository.findAllById(ncUuids);

    // 4. Crear mapa UUID -> TipoRelacion
    Map<UUID, String> tipoRelacionMap = relaciones.stream()
        .collect(Collectors.toMap(
            RelatedCfdiEntity::getInvoiceUuid,
            RelatedCfdiEntity::getRelationType
        ));

    // 5. Mapear a DTO
    return notasCredito.stream()
        .map(nc -> NotaCreditoRelacionadaDto.builder()
            .invoiceUuid(nc.getInvoiceUuid())
            .fiscalUuid(nc.getFiscalUuid())
            .serie(nc.getSeries())
            .folio(nc.getFolio())
            .total(nc.getTotal())
            .tipoRelacion(tipoRelacionMap.get(nc.getInvoiceUuid()))
            .tipoRelacionNombre(NotaCreditoRelacionadaDto
                .getTipoRelacionNombre(tipoRelacionMap.get(nc.getInvoiceUuid())))
            .status(nc.getStatus())
            .statusNombre(InvoiceSearchResponse.getStatusName("E", nc.getStatus()))
            .fechaEmision(nc.getIssueDate())
            .fechaRecepcion(nc.getCreatedAt())
            .build())
        .collect(Collectors.toList());
}
```

## Validaciones al Registrar NC (Modo Estricto)

Cuando se registra una Nota de Credito, el sistema valida:

1. **NC debe tener CfdiRelacionados**: Error BUS2801
2. **Factura referenciada debe existir**: Error BUS2802
3. **Documento referenciado debe ser Factura (tipo I)**: Error BUS2803
4. **Tipo de relacion debe ser '01' para NC**: Error BUS2804

### Codigos de Error

| Codigo | Mensaje |
|--------|---------|
| BUS2801 | La Nota de Credito debe incluir al menos un CFDI relacionado |
| BUS2802 | La Factura relacionada con UUID {uuid} no se encuentra registrada |
| BUS2803 | El CFDI relacionado con UUID {uuid} no es una Factura (tipo I) |
| BUS2804 | El tipo de relacion '{tipo}' no es valido. Para NC debe ser '01' |

## Consultas SQL de Validacion

### Ver NC relacionadas a una Factura

```sql
SELECT
    nc.invoice_uuid,
    nc.fiscal_uuid,
    nc.series,
    nc.folio,
    nc.total,
    nc.status,
    rc.relation_type,
    nc.issue_date,
    nc.created_at
FROM tenant_fiscal.related_cfdi rc
INNER JOIN tenant_fiscal.invoice nc
    ON rc.invoice_uuid = nc.invoice_uuid
WHERE rc.related_invoice_uuid = '50E9F895-F12E-4D28-8BA3-5D9B58FF10FF'
ORDER BY nc.created_at DESC;
```

### Ver todas las relaciones NC-Factura de un emisor

```sql
SELECT
    f.fiscal_uuid as factura_uuid,
    f.series as factura_serie,
    f.folio as factura_folio,
    f.total as factura_total,
    nc.fiscal_uuid as nc_uuid,
    nc.series as nc_serie,
    nc.folio as nc_folio,
    nc.total as nc_total,
    rc.relation_type
FROM tenant_fiscal.related_cfdi rc
INNER JOIN tenant_fiscal.invoice f
    ON rc.related_invoice_uuid = f.invoice_uuid
INNER JOIN tenant_fiscal.invoice nc
    ON rc.invoice_uuid = nc.invoice_uuid
INNER JOIN tenant_fiscal.issuer iss
    ON nc.issuer_uuid = iss.issuer_uuid
WHERE iss.rfc = 'AAA010101AAA'
ORDER BY f.created_at DESC, nc.created_at DESC;
```

### Contar NC por Factura

```sql
SELECT
    f.fiscal_uuid,
    f.series,
    f.folio,
    f.total as total_factura,
    COUNT(rc.related_cfdi_uuid) as cantidad_nc,
    COALESCE(SUM(nc.total), 0) as total_nc
FROM tenant_fiscal.invoice f
LEFT JOIN tenant_fiscal.related_cfdi rc
    ON f.invoice_uuid = rc.related_invoice_uuid
LEFT JOIN tenant_fiscal.invoice nc
    ON rc.invoice_uuid = nc.invoice_uuid
WHERE f.document_type = 'I'
GROUP BY f.invoice_uuid, f.fiscal_uuid, f.series, f.folio, f.total
HAVING COUNT(rc.related_cfdi_uuid) > 0
ORDER BY cantidad_nc DESC;
```

### Verificar integridad de relaciones

```sql
-- Buscar NC huerfanas (sin Factura relacionada en el sistema)
SELECT
    nc.invoice_uuid,
    nc.fiscal_uuid,
    nc.series,
    nc.folio,
    rc.relation_type,
    rc.related_invoice_uuid as factura_no_encontrada
FROM tenant_fiscal.related_cfdi rc
INNER JOIN tenant_fiscal.invoice nc
    ON rc.invoice_uuid = nc.invoice_uuid
LEFT JOIN tenant_fiscal.invoice f
    ON rc.related_invoice_uuid = f.invoice_uuid
WHERE f.invoice_uuid IS NULL;
```

## Flujo de Datos

### Al Consultar Facturas (POST /api/invoices/search)

```
1. Usuario solicita busqueda con tipoDocumento = "I"
                    |
                    v
2. Sistema busca Facturas segun filtros
                    |
                    v
3. Para cada Factura encontrada:
   a. Buscar en related_cfdi donde related_invoice_uuid = factura.uuid
   b. Obtener invoice_uuid de las NC relacionadas
   c. Buscar datos completos de cada NC en invoice
   d. Mapear a NotaCreditoRelacionadaDto
                    |
                    v
4. Agregar lista de NC al campo notasCreditoRelacionadas
                    |
                    v
5. Retornar respuesta con NC incluidas
```

### Al Registrar NC (POST /api/invoices/register)

```
1. Recibir XML de NC
                    |
                    v
2. Parsear nodo CfdiRelacionados
                    |
                    v
3. Para cada CfdiRelacionado:
   a. Validar que existe Factura con ese UUID fiscal
   b. Validar que el documento es tipo "I"
   c. Validar que TipoRelacion = "01"
   d. Crear registro en related_cfdi
                    |
                    v
4. Si alguna validacion falla -> Rechazar NC
                    |
                    v
5. Si todo OK -> Guardar NC y relaciones
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

### Response (con NC relacionadas)
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
      "notasCreditoRelacionadas": [
        {
          "invoiceUuid": "b2c3d4e5-f6a7-8901-bcde-f23456789012",
          "fiscalUuid": "60F0A906-G23F-5E39-9CB4-6E0C69GG21GG",
          "serie": "NC",
          "folio": "001",
          "total": 1160.00,
          "tipoRelacion": "01",
          "tipoRelacionNombre": "Nota de credito de los documentos relacionados",
          "status": 7,
          "statusNombre": "Aplicado",
          "fechaEmision": "2025-01-20",
          "fechaRecepcion": "2025-01-20T14:30:00"
        },
        {
          "invoiceUuid": "c3d4e5f6-a7b8-9012-cdef-345678901234",
          "fiscalUuid": "70G1B017-H34G-6F40-0DC5-7F1D70HH32HH",
          "serie": "NC",
          "folio": "002",
          "total": 580.00,
          "tipoRelacion": "01",
          "tipoRelacionNombre": "Nota de credito de los documentos relacionados",
          "status": 3,
          "statusNombre": "Pendiente de Contabilizar",
          "fechaEmision": "2025-01-25",
          "fechaRecepcion": "2025-01-25T09:15:00"
        }
      ]
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

## Casos de Prueba

### Caso 1: Factura con NC relacionadas
- Buscar Factura que tenga NC registradas
- Verificar que `notasCreditoRelacionadas` contiene las NC
- Validar que los montos y estatus son correctos

### Caso 2: Factura sin NC relacionadas
- Buscar Factura que no tenga NC
- Verificar que `notasCreditoRelacionadas` es null o array vacio

### Caso 3: Busqueda de NC (tipoDocumento = "E")
- Buscar Notas de Credito
- Verificar que `notasCreditoRelacionadas` no aplica (null o vacio)

### Caso 4: Registrar NC con Factura inexistente
- Intentar registrar NC que referencia UUID de Factura no existente
- Verificar que retorna error BUS2802

### Caso 5: Registrar NC que referencia otra NC
- Intentar registrar NC que referencia documento tipo "E"
- Verificar que retorna error BUS2803

## Checklist de Validacion

- [ ] Endpoint POST /api/invoices/search retorna notasCreditoRelacionadas
- [ ] Solo aplica para tipoDocumento = "I"
- [ ] Cada NC incluye: invoiceUuid, fiscalUuid, serie, folio, total
- [ ] Cada NC incluye: tipoRelacion, tipoRelacionNombre
- [ ] Cada NC incluye: status, statusNombre
- [ ] Cada NC incluye: fechaEmision, fechaRecepcion
- [ ] Registro de NC valida existencia de Factura referenciada
- [ ] Registro de NC valida que documento referenciado es tipo "I"
- [ ] Registro de NC valida TipoRelacion = "01"
- [ ] Errores BUS2801-BUS2804 funcionan correctamente

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| RelatedCfdiRepository.java | Agregados metodos findByRelatedInvoiceUuid y findByInvoiceUuid |
| NotaCreditoRelacionadaDto.java | Nuevo DTO creado |
| InvoiceSearchResponse.java | Agregado campo notasCreditoRelacionadas |
| FiscalErrorCode.java | Agregados codigos BUS2801-BUS2804 |
| InvoiceServiceImpl.java | Implementado saveRelatedCfdis y findNotasCreditoRelacionadas |

---

**JIRA:** STM-1168
**Fecha:** 2025-12-01
**Autor:** Sodimac Tech Team
**Estado:** Implementado
