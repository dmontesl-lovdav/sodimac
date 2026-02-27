# STM-395: Complemento de reglas de negocio en la pantalla de facturación

## Estado: IMPLEMENTADO

## Relación con otros JIRAs

| JIRA | Descripción | Relación |
|------|-------------|----------|
| **STM-397** | Validaciones para Notas de Crédito | Similar - misma implementación, diferente tipo de documento |

> **Nota:** STM-395 y STM-397 son funcionalmente idénticos pero aplican a diferentes tipos de documento. La implementación técnica es compartida, solo cambian los códigos de mensaje según el tipo (Factura vs Nota de Crédito).

---

## Descripción

Como proveedor usuario del Portal de Facturación quiero que el sistema valide correctamente la serie, folio y UUID de mis facturas para evitar duplicados, errores de carga y asegurar la integridad de los documentos registrados.

## Criterios de Aceptación

| ID | Criterio | Descripción | Estado |
|----|----------|-------------|--------|
| CA01 | Validación de Serie y Folio | Si la factura no tiene serie o no tiene folio, rechazar y mostrar WRN7012 | ✅ |
| CA02 | Duplicado por Serie + Folio | Si existe factura con misma serie+folio del mismo proveedor, rechazar y mostrar WRN7013 | ✅ |
| CA03 | Duplicado por UUID | Si existe factura con mismo UUID del mismo proveedor, rechazar y mostrar WRN7014 | ✅ |
| CA04 | Validación Exitosa | Si pasa todas las validaciones, permitir el registro sin errores | ✅ |

## Mensajes Implementados

| ID | Catálogo | Mensaje | Cuándo se activa |
|----|----------|---------|------------------|
| WRN7012 | CatMsgAdvertencia | La factura requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar. | Cuando la factura no contiene serie o folio |
| WRN7013 | CatMsgAdvertencia | La factura se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar. | Cuando se detecta una factura previa con la misma combinación serie + folio para el mismo proveedor |
| WRN7014 | CatMsgAdvertencia | La factura se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar. | Cuando se detecta una factura previa con el mismo UUID para el mismo proveedor |

---

## Implementación Realizada

### 1. Base de Datos - Catálogo de Mensajes

**Archivo:** `catalogos-api/src/main/resources/db/13_STM-395-397_validaciones_factura_nc.sql`

```sql
-- WRN7012: Validación Serie y Folio - Facturas (dict_id: 8016)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8016, 1, 'La factura requiere una serie y folio para publicar el documento...'),
(8016, 2, 'The invoice requires a series and folio to publish the document...'),
(8016, 3, 'A fatura requer uma série e folio para publicar o documento...');

-- WRN7013 y WRN7014 con estructura similar...

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN7012', 8016, NULL, 7012, 1),
(11, 'WRN7013', 8017, NULL, 7013, 1),
(11, 'WRN7014', 8018, NULL, 7014, 1);
```

### 2. Backend - fiscal-api

#### 2.1 FiscalMessageCode.java
```java
// ========== ADVERTENCIAS - VALIDACIÓN FACTURA (WRN7012-WRN7014) - STM-395 ==========
WRN7012("WRN7012", "La factura requiere una serie y folio para publicar el documento..."),
WRN7013("WRN7013", "La factura se encuentra previamente registrada con la misma serie y folio..."),
WRN7014("WRN7014", "La factura se encuentra previamente registrada con el mismo UUID...");
```

#### 2.2 InvoiceRepository.java
```java
boolean existsBySeriesAndFolioAndIssuerUuidAndDocumentType(
        String series, String folio, UUID issuerUuid, String documentType);

boolean existsByFiscalUuidAndIssuerUuidAndDocumentType(
        UUID fiscalUuid, UUID issuerUuid, String documentType);
```

#### 2.3 InvoiceServiceImpl.java
```java
private void validateSeriesAndFolio(InvoiceXmlDto invoiceDto, TipoDocumentoFiscal tipoDocumento) {
    // Valida que serie y folio no estén vacíos
    // Lanza WRN7012 para Factura, WRN7015 para NC
}

private void validateNoDuplicateBySeriesAndFolio(String serie, String folio,
        UUID issuerUuid, TipoDocumentoFiscal tipoDocumento) {
    // Valida que no exista duplicado por serie+folio+proveedor
    // Lanza WRN7013 para Factura, WRN7016 para NC
}

private void validateNoDuplicateByUuid(UUID fiscalUuid, UUID issuerUuid,
        TipoDocumentoFiscal tipoDocumento) {
    // Valida que no exista duplicado por UUID+proveedor
    // Lanza WRN7014 para Factura, WRN7017 para NC
}
```

---

## Pruebas

### Endpoint de prueba
```
POST http://localhost:8082/invoices/register
Content-Type: multipart/form-data
Body: file=@factura.xml
```

### Escenario 1: Factura sin serie o folio (WRN7012)
**Request:** XML de factura sin atributo `Folio` o `Serie`
**Response esperado:**
```json
{
  "code": "WRN7012",
  "message": "La factura requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar.",
  "success": false
}
```

### Escenario 2: Factura duplicada por serie+folio (WRN7013)
**Request:** XML de factura con serie y folio que ya existe para el mismo proveedor
**Response esperado:**
```json
{
  "code": "WRN7013",
  "message": "La factura se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.",
  "success": false
}
```

### Escenario 3: Factura duplicada por UUID (WRN7014)
**Request:** XML de factura con UUID fiscal que ya existe para el mismo proveedor
**Response esperado:**
```json
{
  "code": "WRN7014",
  "message": "La factura se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar.",
  "success": false
}
```

### Escenario 4: Registro exitoso
**Request:** XML de factura válido sin duplicados
**Response esperado:**
```json
{
  "code": "OK",
  "message": "Documento registrado exitosamente",
  "success": true,
  "invoiceUuid": "uuid-generado",
  "fiscalUuid": "uuid-del-timbre",
  "series": "SERIE",
  "folio": "12345",
  "documentType": "I"
}
```

---

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `catalogos-api/.../db/13_STM-395-397_validaciones_factura_nc.sql` | Script SQL con mensajes |
| `fiscal-api/.../FiscalMessageCode.java` | Códigos WRN7012-WRN7014 |
| `fiscal-api/.../InvoiceRepository.java` | Métodos de validación duplicados |
| `fiscal-api/.../InvoiceServiceImpl.java` | Lógica de validación integrada |

---

## Orden de Validación

1. **Parseo XML** - Validar estructura del CFDI
2. **CA01** - Validar serie y folio presentes → WRN7012
3. **Obtener Emisor** - Buscar o crear el proveedor
4. **CA02** - Validar no duplicado por serie+folio+proveedor → WRN7013
5. **CA03** - Validar no duplicado por UUID+proveedor → WRN7014
6. **CA04** - Si todo OK, continuar con el registro
