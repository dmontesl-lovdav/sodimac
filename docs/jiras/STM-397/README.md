# STM-397: Complemento de reglas de negocio en la pantalla de notas de crédito

## Estado: IMPLEMENTADO

## Relación con otros JIRAs

| JIRA | Descripción | Relación |
|------|-------------|----------|
| **STM-395** | Validaciones para Facturas | Similar - misma implementación, diferente tipo de documento |

> **Nota:** STM-395 y STM-397 son funcionalmente idénticos pero aplican a diferentes tipos de documento. La implementación técnica es compartida, solo cambian los códigos de mensaje según el tipo (Factura vs Nota de Crédito).

---

## Descripción

Como proveedor usuario del Portal de Facturación quiero que el sistema valide correctamente la serie, folio y UUID de mis notas de crédito para evitar duplicados, errores de carga y asegurar la integridad de los documentos registrados.

## Criterios de Aceptación

| ID | Criterio | Descripción | Estado |
|----|----------|-------------|--------|
| CA01 | Validación de Serie y Folio | Si la nota de crédito no tiene serie o no tiene folio, rechazar y mostrar WRN7015 | ✅ |
| CA02 | Duplicado por Serie + Folio | Si existe NC con misma serie+folio del mismo proveedor, rechazar y mostrar WRN7016 | ✅ |
| CA03 | Duplicado por UUID | Si existe NC con mismo UUID del mismo proveedor, rechazar y mostrar WRN7017 | ✅ |
| CA04 | Validación Exitosa | Si pasa todas las validaciones, permitir el registro sin errores | ✅ |

## Mensajes Implementados

> **Nota:** Los mensajes originales del JIRA decían "factura" pero se corrigieron a "nota de crédito" para mantener consistencia con el tipo de documento.

| ID | Catálogo | Mensaje | Cuándo se activa |
|----|----------|---------|------------------|
| WRN7015 | CatMsgAdvertencia | La nota de crédito requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar. | Cuando la NC no contiene serie o folio |
| WRN7016 | CatMsgAdvertencia | La nota de crédito se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar. | Cuando se detecta una NC previa con la misma combinación serie + folio para el mismo proveedor |
| WRN7017 | CatMsgAdvertencia | La nota de crédito se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar. | Cuando se detecta una NC previa con el mismo UUID para el mismo proveedor |

---

## Implementación Realizada

### 1. Base de Datos - Catálogo de Mensajes

**Archivo:** `catalogos-api/src/main/resources/db/13_STM-395-397_validaciones_factura_nc.sql`

```sql
-- WRN7015: Validación Serie y Folio - Notas de Crédito (dict_id: 8019)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(8019, 1, 'La nota de crédito requiere una serie y folio para publicar el documento...'),
(8019, 2, 'The credit note requires a series and folio to publish the document...'),
(8019, 3, 'A nota de crédito requer uma série e folio para publicar o documento...');

-- WRN7016 y WRN7017 con estructura similar...

INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES
(11, 'WRN7015', 8019, NULL, 7015, 1),
(11, 'WRN7016', 8020, NULL, 7016, 1),
(11, 'WRN7017', 8021, NULL, 7017, 1);
```

### 2. Backend - fiscal-api

#### 2.1 FiscalMessageCode.java
```java
// ========== ADVERTENCIAS - VALIDACIÓN NOTA DE CRÉDITO (WRN7015-WRN7017) - STM-397 ==========
WRN7015("WRN7015", "La nota de crédito requiere una serie y folio para publicar el documento..."),
WRN7016("WRN7016", "La nota de crédito se encuentra previamente registrada con la misma serie y folio..."),
WRN7017("WRN7017", "La nota de crédito se encuentra previamente registrada con el mismo UUID...");
```

#### 2.2 InvoiceRepository.java (compartido con STM-395)
```java
boolean existsBySeriesAndFolioAndIssuerUuidAndDocumentType(
        String series, String folio, UUID issuerUuid, String documentType);

boolean existsByFiscalUuidAndIssuerUuidAndDocumentType(
        UUID fiscalUuid, UUID issuerUuid, String documentType);
```

#### 2.3 InvoiceServiceImpl.java (compartido con STM-395)
Las validaciones detectan automáticamente el tipo de documento y usan el código correspondiente:
- `TipoDocumentoFiscal.FACTURA` → WRN7012, WRN7013, WRN7014
- `TipoDocumentoFiscal.NOTA_CREDITO` → WRN7015, WRN7016, WRN7017

---

## Pruebas

### Endpoint de prueba
```
POST http://localhost:8082/invoices/register
Content-Type: multipart/form-data
Body: file=@nota_credito.xml
```

### Escenario 1: NC sin serie o folio (WRN7015)
**Request:** XML de nota de crédito sin atributo `Folio` o `Serie`
**Response esperado:**
```json
{
  "code": "WRN7015",
  "message": "La nota de crédito requiere una serie y folio para publicar el documento. Por favor, valide la información antes de continuar.",
  "success": false
}
```

### Escenario 2: NC duplicada por serie+folio (WRN7016)
**Request:** XML de nota de crédito con serie y folio que ya existe para el mismo proveedor
**Response esperado:**
```json
{
  "code": "WRN7016",
  "message": "La nota de crédito se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.",
  "success": false
}
```

### Escenario 3: NC duplicada por UUID (WRN7017)
**Request:** XML de nota de crédito con UUID fiscal que ya existe para el mismo proveedor
**Response esperado:**
```json
{
  "code": "WRN7017",
  "message": "La nota de crédito se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar.",
  "success": false
}
```

### Escenario 4: Registro exitoso
**Request:** XML de nota de crédito válido sin duplicados
**Response esperado:**
```json
{
  "code": "OK",
  "message": "Documento registrado exitosamente",
  "success": true,
  "invoiceUuid": "uuid-generado",
  "fiscalUuid": "uuid-del-timbre",
  "series": "NC",
  "folio": "00444871",
  "documentType": "E"
}
```

---

## Evidencia de Pruebas

### WRN7016 - NC duplicada por serie+folio
**Comando:**
```bash
curl -X POST "http://localhost:8082/invoices/register" -F "file=@NotaCredito.xml"
```

**Resultado:**
```json
{
  "code": "WRN7016",
  "message": "La nota de crédito se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar.",
  "success": false
}
```

---

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `catalogos-api/.../db/13_STM-395-397_validaciones_factura_nc.sql` | Script SQL con mensajes |
| `fiscal-api/.../FiscalMessageCode.java` | Códigos WRN7015-WRN7017 |
| `fiscal-api/.../InvoiceRepository.java` | Métodos de validación (compartidos) |
| `fiscal-api/.../InvoiceServiceImpl.java` | Lógica de validación (compartida) |

---

## Diferencia con STM-395

| Aspecto | STM-395 (Facturas) | STM-397 (Notas de Crédito) |
|---------|-------------------|---------------------------|
| Tipo documento | `I` | `E` |
| Msg Serie/Folio | WRN7012 | WRN7015 |
| Msg Duplicado Serie+Folio | WRN7013 | WRN7016 |
| Msg Duplicado UUID | WRN7014 | WRN7017 |
