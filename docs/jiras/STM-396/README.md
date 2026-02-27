# STM-396 - Consulta de Notas de Credito y Descarga Masiva

## Descripcion

Implementacion del endpoint de consulta de Notas de Credito con datos de la factura relacionada, integracion con catalogos-api para descripciones de TipoRelacion y TipoAddenda, y endpoints de descarga masiva de documentos fiscales.

## Componentes Modificados

### 1. catalogos-api
- Script SQL `10_data_tipo_relacion_addenda.sql` con catalogos:
  - `c_TipoRelacion` (SAT): Tipos de relacion entre CFDIs
  - `TipoAddenda` (Sodimac): Tipos de addenda internos
- Soporte multiidioma: ES (1), EN (2), PT (3)

### 2. fiscal-api
- `SatCatalogService`: Servicio para consumir catalogos-api
- `InvoiceController`: Nuevos endpoints de descarga masiva
- `InvoiceServiceImpl`: Integracion con SatCatalogService y PdfRenderService
- `InvoiceSearchResponse`: Campos para factura relacionada (NC)
- `NotaCreditoRelacionadaDto`: DTO para NC relacionadas a facturas
- `PdfRenderService`: Generacion de PDFs reales desde XML usando Apache FOP

### 3. bff-fiscal
- Actualizacion de OpenAPI con nuevos endpoints

## Endpoints

### Consulta
```
POST /invoices/search
```
- Busqueda de Facturas (I) y Notas de Credito (E)
- Para NC incluye datos de factura relacionada
- Para Facturas incluye lista de NC relacionadas

### Descarga Masiva
```
POST /invoices/download/xml   # ZIP con XMLs originales
POST /invoices/download/pdf   # ZIP con PDFs generados en tiempo real
POST /invoices/export/csv     # Archivo CSV con datos de busqueda
```

#### Generacion de PDF
El endpoint `/invoices/download/pdf` genera PDFs reales en tiempo real:
- Usa Apache FOP con transformacion XSL (plantilla `Formato4.0.xsl`)
- Soporta CFDI version 4.0
- Incluye codigo QR con URL de verificacion SAT
- Requiere que el documento tenga contenido XML en la BD (`xml_content`)
- Los documentos sin XML son omitidos del ZIP

## Catalogos Nuevos

### c_TipoRelacion (SAT)
| Codigo | Descripcion |
|--------|-------------|
| 01 | Nota de credito de los documentos relacionados |
| 02 | Nota de debito de los documentos relacionados |
| 03 | Devolucion de mercancia sobre facturas o traslados previos |
| 04 | Sustitucion de los CFDI previos |
| 05 | Traslados de mercancias facturados previamente |
| 06 | Factura generada por los traslados previos |
| 07 | CFDI por aplicacion de anticipo |
| 08 | Factura generada por pagos en parcialidades |
| 09 | Factura generada por pagos diferidos |

### TipoAddenda (Sodimac)
| Codigo | Descripcion |
|--------|-------------|
| 1 | Addenda Estandar |
| 2 | Addenda con Carta Porte |
| 3 | Addenda Complemento de Pago |
| 4 | Addenda Internacional |
| 5 | Addenda Sodimac |

## Como Probar

### 1. Ejecutar script SQL en catalogos-api
```sql
-- Ejecutar en la base de datos de catalogos-api
\i 10_data_tipo_relacion_addenda.sql
```

### 2. Levantar servicios
```bash
# Terminal 1 - catalogos-api (puerto 8083)
cd backend/mrch.backend.somx.catalogos-api
mvn spring-boot:run

# Terminal 2 - fiscal-api (puerto 8082)
cd backend/mrch.backend.somx.fiscal-api
mvn spring-boot:run
```

### 3. Importar coleccion Postman
Importar `STM-396-NC-BulkDownload.postman_collection.json` en Postman.

### 4. Probar endpoints
1. Verificar catalogos en catalogos-api
2. Buscar NC con datos de factura relacionada
3. Probar descarga masiva con UUIDs reales

## Configuracion

fiscal-api debe tener configurado el URL de catalogos-api:
```yaml
# application.yml
sodimac:
  catalogos:
    api:
      url: http://localhost:8083
      enabled: true
```

## Archivos Relacionados

### Catalogos
- [10_data_tipo_relacion_addenda.sql](../../backend/mrch.backend.somx.catalogos-api/src/main/resources/db/10_data_tipo_relacion_addenda.sql)
- [SatCatalogService.java](../../backend/mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/SatCatalogService.java)
- [SatCatalogServiceImpl.java](../../backend/mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/SatCatalogServiceImpl.java)

### Generacion PDF
- [PdfRenderService.java](../../backend/mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/pdf/PdfRenderService.java)
- [PdfRenderServiceImpl.java](../../backend/mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/pdf/PdfRenderServiceImpl.java)
- [Formato4.0.xsl](../../backend/mrch.backend.somx.fiscal-api/src/main/resources/templates/xsl/Formato4.0.xsl)

### Descarga Masiva
- [InvoiceServiceImpl.java](../../backend/mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java) - Metodos `downloadXmlZip`, `downloadPdfZip`, `exportCsv`
- [InvoiceController.java](../../backend/mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/controller/InvoiceController.java)

### Validacion
- [queries-validacion.sql](queries-validacion.sql) - Consultas SQL para obtener datos de prueba
