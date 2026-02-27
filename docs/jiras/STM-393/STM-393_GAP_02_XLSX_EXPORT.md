# STM-393 GAP 02: Endpoint Exportacion XLSX con 2 Hojas

## Descripcion del GAP

Se requiere un nuevo endpoint para exportar los resultados de busqueda en formato XLSX con dos hojas:
- **Hoja 1**: Facturas
- **Hoja 2**: Notas de Credito relacionadas

Actualmente solo existe exportacion CSV simple (`POST /invoices/export/csv`).

## Impacto

- **Severidad**: Alta
- **Servicios afectados**: fiscal-api
- **Dependencias**: Apache POI (ya existe en el proyecto)

---

## Implementacion Requerida

### 1. Controller

**Archivo:** `src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java`

```java
/**
 * Exportar facturas y NC relacionadas a XLSX con 2 hojas
 * STM-393: Pantalla consulta de facturas
 */
@PostMapping("/export/xlsx")
@Operation(summary = "Exportar busqueda a XLSX",
           description = "Genera archivo XLSX con 2 hojas: Facturas y Notas de Credito relacionadas")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Archivo XLSX generado"),
    @ApiResponse(responseCode = "400", description = "Parametros invalidos"),
    @ApiResponse(responseCode = "404", description = "Sin resultados")
})
public ResponseEntity<byte[]> exportToXlsx(
        @RequestBody @Valid InvoiceSearchRequest request,
        @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

    byte[] xlsxContent = invoiceService.exportToXlsx(request, lang);

    String filename = String.format("facturas_%s.xlsx",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(xlsxContent.length);

    return new ResponseEntity<>(xlsxContent, headers, HttpStatus.OK);
}
```

---

### 2. Service Interface

**Archivo:** `src/main/java/com/sodimac/fiscal/api/service/InvoiceService.java`

```java
/**
 * Exportar busqueda a XLSX con 2 hojas (Facturas + NC)
 * @param request Criterios de busqueda
 * @param lang Idioma para headers
 * @return Contenido del archivo XLSX
 */
byte[] exportToXlsx(InvoiceSearchRequest request, String lang);
```

---

### 3. Service Implementation

**Archivo:** `src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java`

```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Override
public byte[] exportToXlsx(InvoiceSearchRequest request, String lang) {
    // Obtener todas las facturas sin paginacion
    request.setPage(0);
    request.setSize(Integer.MAX_VALUE);

    Page<InvoiceSearchResponse> invoices = searchInvoices(request);

    if (invoices.isEmpty()) {
        throw new FiscalException(FiscalErrorCode.NO_RESULTS_FOUND);
    }

    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

        // Crear estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle dateTimeStyle = createDateTimeStyle(workbook);

        // Hoja 1: Facturas
        Sheet facturasSheet = workbook.createSheet("Facturas");
        createFacturasSheet(facturasSheet, invoices.getContent(), headerStyle, dateStyle, dateTimeStyle);

        // Hoja 2: Notas de Credito
        Sheet ncSheet = workbook.createSheet("Notas de Credito");
        List<NotaCreditoXlsxDto> notasCredito = extractNotasCredito(invoices.getContent());
        createNotasCreditoSheet(ncSheet, notasCredito, headerStyle, dateStyle, dateTimeStyle);

        // Auto-size columnas
        autoSizeColumns(facturasSheet, 14);
        autoSizeColumns(ncSheet, 12);

        workbook.write(outputStream);
        return outputStream.toByteArray();

    } catch (IOException e) {
        log.error("Error generando XLSX: {}", e.getMessage());
        throw new FiscalException(FiscalErrorCode.EXPORT_ERROR, e.getMessage());
    }
}

private void createFacturasSheet(Sheet sheet, List<InvoiceSearchResponse> facturas,
        CellStyle headerStyle, CellStyle dateStyle, CellStyle dateTimeStyle) {

    // Headers
    String[] headers = {
        "Serie", "Folio", "Subtotal", "Total", "Orden de Compra", "Recepcion",
        "UUID", "# NC Relacionadas", "ID Proveedor", "Nombre Proveedor",
        "Fecha Emision", "Fecha Recepcion", "Fecha Envio"
    };

    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }

    // Datos
    int rowNum = 1;
    for (InvoiceSearchResponse factura : facturas) {
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(nullSafe(factura.getSeries()));
        row.createCell(1).setCellValue(nullSafe(factura.getFolio()));
        row.createCell(2).setCellValue(factura.getSubtotal() != null ?
            factura.getSubtotal().doubleValue() : 0);
        row.createCell(3).setCellValue(factura.getTotal() != null ?
            factura.getTotal().doubleValue() : 0);
        row.createCell(4).setCellValue(nullSafe(factura.getNoOrdenCompra()));
        row.createCell(5).setCellValue(nullSafe(factura.getNoRecepcion()));
        row.createCell(6).setCellValue(nullSafe(factura.getFiscalUuid()));
        row.createCell(7).setCellValue(factura.getNotasCreditoRelacionadas() != null ?
            factura.getNotasCreditoRelacionadas().size() : 0);
        row.createCell(8).setCellValue(factura.getNumeroProveedor() != null ?
            factura.getNumeroProveedor().toString() : "");
        row.createCell(9).setCellValue(nullSafe(factura.getEmisorName()));

        // Fechas
        Cell cellFechaEmision = row.createCell(10);
        if (factura.getIssueDate() != null) {
            cellFechaEmision.setCellValue(factura.getIssueDate());
            cellFechaEmision.setCellStyle(dateStyle);
        }

        Cell cellFechaRecepcion = row.createCell(11);
        if (factura.getCreatedAt() != null) {
            cellFechaRecepcion.setCellValue(Date.from(
                factura.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
            cellFechaRecepcion.setCellStyle(dateTimeStyle);
        }

        Cell cellFechaEnvio = row.createCell(12);
        if (factura.getSentDate() != null) {
            cellFechaEnvio.setCellValue(Date.from(
                factura.getSentDate().atZone(ZoneId.systemDefault()).toInstant()));
            cellFechaEnvio.setCellStyle(dateTimeStyle);
        }
    }
}

private void createNotasCreditoSheet(Sheet sheet, List<NotaCreditoXlsxDto> notasCredito,
        CellStyle headerStyle, CellStyle dateStyle, CellStyle dateTimeStyle) {

    // Headers
    String[] headers = {
        "Serie", "Folio", "Subtotal", "Total", "Motivo", "UUID",
        "Fecha Emision", "Fecha Recepcion", "Fecha Envio",
        "Serie Factura", "Folio Factura", "UUID Factura"
    };

    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }

    // Datos
    int rowNum = 1;
    for (NotaCreditoXlsxDto nc : notasCredito) {
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(nullSafe(nc.getSerie()));
        row.createCell(1).setCellValue(nullSafe(nc.getFolio()));
        row.createCell(2).setCellValue(nc.getSubtotal() != null ?
            nc.getSubtotal().doubleValue() : 0);
        row.createCell(3).setCellValue(nc.getTotal() != null ?
            nc.getTotal().doubleValue() : 0);
        row.createCell(4).setCellValue(nullSafe(nc.getTipoRelacionNombre())); // Motivo
        row.createCell(5).setCellValue(nullSafe(nc.getFiscalUuid()));

        // Fechas NC
        Cell cellFechaEmision = row.createCell(6);
        if (nc.getFechaEmision() != null) {
            cellFechaEmision.setCellValue(nc.getFechaEmision());
            cellFechaEmision.setCellStyle(dateStyle);
        }

        Cell cellFechaRecepcion = row.createCell(7);
        if (nc.getFechaRecepcion() != null) {
            cellFechaRecepcion.setCellValue(Date.from(
                nc.getFechaRecepcion().atZone(ZoneId.systemDefault()).toInstant()));
            cellFechaRecepcion.setCellStyle(dateTimeStyle);
        }

        Cell cellFechaEnvio = row.createCell(8);
        if (nc.getSentDate() != null) {
            cellFechaEnvio.setCellValue(Date.from(
                nc.getSentDate().atZone(ZoneId.systemDefault()).toInstant()));
            cellFechaEnvio.setCellStyle(dateTimeStyle);
        }

        // Datos Factura relacionada
        row.createCell(9).setCellValue(nullSafe(nc.getFacturaSerie()));
        row.createCell(10).setCellValue(nullSafe(nc.getFacturaFolio()));
        row.createCell(11).setCellValue(nullSafe(nc.getFacturaUuid()));
    }
}

private List<NotaCreditoXlsxDto> extractNotasCredito(List<InvoiceSearchResponse> facturas) {
    List<NotaCreditoXlsxDto> result = new ArrayList<>();

    for (InvoiceSearchResponse factura : facturas) {
        if (factura.getNotasCreditoRelacionadas() != null) {
            for (NotaCreditoRelacionadaDto nc : factura.getNotasCreditoRelacionadas()) {
                NotaCreditoXlsxDto dto = NotaCreditoXlsxDto.builder()
                    .serie(nc.getSerie())
                    .folio(nc.getFolio())
                    .subtotal(nc.getSubtotal())
                    .total(nc.getTotal())
                    .tipoRelacionNombre(nc.getTipoRelacionNombre())
                    .fiscalUuid(nc.getFiscalUuid() != null ? nc.getFiscalUuid().toString() : null)
                    .fechaEmision(nc.getFechaEmision())
                    .fechaRecepcion(nc.getFechaRecepcion())
                    .sentDate(nc.getSentDate()) // Requiere agregar campo en NotaCreditoRelacionadaDto
                    .facturaSerie(factura.getSeries())
                    .facturaFolio(factura.getFolio())
                    .facturaUuid(factura.getFiscalUuid() != null ? factura.getFiscalUuid().toString() : null)
                    .build();
                result.add(dto);
            }
        }
    }

    return result;
}

private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
}

private CellStyle createDateStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
    return style;
}

private CellStyle createDateTimeStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
    return style;
}

private void autoSizeColumns(Sheet sheet, int numColumns) {
    for (int i = 0; i < numColumns; i++) {
        sheet.autoSizeColumn(i);
    }
}

private String nullSafe(String value) {
    return value != null ? value : "";
}
```

---

### 4. DTO para NC en XLSX

**Archivo:** `src/main/java/com/sodimac/fiscal/api/model/dto/NotaCreditoXlsxDto.java`

```java
package com.sodimac.fiscal.api.model.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class NotaCreditoXlsxDto {
    private String serie;
    private String folio;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String tipoRelacionNombre; // Motivo
    private String fiscalUuid;
    private LocalDate fechaEmision;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime sentDate;

    // Datos de la Factura relacionada
    private String facturaSerie;
    private String facturaFolio;
    private String facturaUuid;
}
```

---

### 5. Actualizar NotaCreditoRelacionadaDto

**Archivo:** `src/main/java/com/sodimac/fiscal/api/model/dto/NotaCreditoRelacionadaDto.java`

**Agregar campo subtotal y sentDate:**

```java
@Schema(description = "Subtotal de la NC")
private BigDecimal subtotal;

@Schema(description = "Fecha de envio de la NC")
private LocalDateTime sentDate;
```

---

### 6. OpenAPI

```yaml
/invoices/export/xlsx:
  post:
    summary: Exportar busqueda a XLSX
    description: Genera archivo XLSX con 2 hojas (Facturas y Notas de Credito)
    operationId: exportToXlsx
    tags:
      - Invoices
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/InvoiceSearchRequest'
    responses:
      '200':
        description: Archivo XLSX generado
        content:
          application/vnd.openxmlformats-officedocument.spreadsheetml.sheet:
            schema:
              type: string
              format: binary
      '400':
        description: Parametros invalidos
      '404':
        description: Sin resultados
```

---

## Estructura del Archivo XLSX

### Hoja "Facturas"

| Columna | Tipo | Formato |
|---------|------|---------|
| A | Serie | Texto |
| B | Folio | Texto |
| C | Subtotal | Numero |
| D | Total | Numero |
| E | Orden de Compra | Texto |
| F | Recepcion | Texto |
| G | UUID | Texto |
| H | # NC Relacionadas | Numero |
| I | ID Proveedor | Texto |
| J | Nombre Proveedor | Texto |
| K | Fecha Emision | dd/mm/yyyy |
| L | Fecha Recepcion | dd/mm/yyyy hh:mm:ss |
| M | Fecha Envio | dd/mm/yyyy hh:mm:ss |

### Hoja "Notas de Credito"

| Columna | Tipo | Formato |
|---------|------|---------|
| A | Serie | Texto |
| B | Folio | Texto |
| C | Subtotal | Numero |
| D | Total | Numero |
| E | Motivo | Texto |
| F | UUID | Texto |
| G | Fecha Emision | dd/mm/yyyy |
| H | Fecha Recepcion | dd/mm/yyyy hh:mm:ss |
| I | Fecha Envio | dd/mm/yyyy hh:mm:ss |
| J | Serie Factura | Texto |
| K | Folio Factura | Texto |
| L | UUID Factura | Texto |

---

## Checklist de Implementacion

- [ ] DTO NotaCreditoXlsxDto creado
- [ ] NotaCreditoRelacionadaDto actualizado con subtotal y sentDate
- [ ] Metodo exportToXlsx implementado en InvoiceServiceImpl
- [ ] Endpoint POST /invoices/export/xlsx creado
- [ ] OpenAPI actualizado
- [ ] Pruebas unitarias creadas
- [ ] Pruebas de integracion completadas

---

## Archivos a Crear/Modificar

| Archivo | Tipo | Accion |
|---------|------|--------|
| `NotaCreditoXlsxDto.java` | Java | Crear |
| `NotaCreditoRelacionadaDto.java` | Java | Modificar |
| `InvoiceService.java` | Java | Agregar metodo |
| `InvoiceServiceImpl.java` | Java | Implementar metodo |
| `InvoiceController.java` | Java | Agregar endpoint |
| `api.yml` | YAML | Agregar operacion |

---

**Esfuerzo estimado:** 8 horas
**Dependencias:** GAP 01 (campo sentDate)
