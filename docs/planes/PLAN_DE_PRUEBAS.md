# PLAN DE PRUEBAS - SISTEMA DE FACTURACIÓN FISCAL

## INFORMACIÓN GENERAL

**Fecha**: 2025-11-11
**Proyectos**: fiscal-api (Java/Spring Boot) + finanzas-api (Node.js/Express)
**Objetivo**: Validar funcionalidad completa de JIRAs implementados

---

## 📋 RESUMEN DE JIRAS IMPLEMENTADOS

| JIRA | Descripción | Proyecto | Estado |
|------|-------------|----------|--------|
| STM-337 | Creación de microservicio para el registro de una factura o NC | fiscal-api | ✅ Implementado |
| STM-448 | Agregar método de consulta al microservicio de complemento de pago | fiscal-api | ✅ Implementado |
| STM-569 | Integración de servicios de facturación | fiscal-api | ✅ Implementado |
| STM-771 | Ajustar el método de consulta del servicio de facturas y NC | fiscal-api | ✅ Implementado |
| STM-875 | Búsqueda avanzada de descuentos comerciales con filtros y paginación | finanzas-api | ✅ Implementado |
| STM-972 | Creación de servicio para transformar un documento fiscal a JSON | fiscal-api | ✅ Implementado |
| STM-973 | Relacionar descuento comercial con Nota de Crédito mediante validación de XML | fiscal-api + finanzas-api | ✅ Implementado |
| STM-304 | Soporte de filtrado y exportación CSV para guías de envío | finanzas-api | ✅ Implementado |

---

## 🔧 CONFIGURACIÓN PREVIA

### 1. Servicios Requeridos

✅ **Base de datos PostgreSQL**
```bash
Host: 10.138.153.10:5432
Database: userapp
Schema fiscal-api: tenant_fiscal
Schema finanzas-api: public
```

✅ **fiscal-api** (Puerto 8082)
```bash
cd c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

✅ **finanzas-api** (Puerto 8091)
```bash
cd c:\workspace-fbc\backend\mrch.backend.somx.finanzas-api
npm run dev
```

### 2. Verificación de Servicios

**Comando de verificación:**
```bash
# Verificar fiscal-api
curl http://localhost:8082/actuator/health

# Verificar finanzas-api
curl http://localhost:8091/health
```

**Respuestas esperadas:**
- fiscal-api: `{"status":"UP"}`
- finanzas-api: `{"ok":true,"env":"development"}`

### 3. Herramientas de Prueba

- ✅ **Postman** (importar colección incluida)
- ✅ **cURL** (para pruebas manuales)
- ✅ **DBeaver/pgAdmin** (para verificación de datos)

---

## 📝 PLAN DE PRUEBAS DETALLADO

---

## JIRA STM-337: Registro de Factura o NC

### Objetivo
Validar el registro de facturas electrónicas (CFDI) desde archivos XML

### Pre-requisitos
1. ✅ Servicio fiscal-api levantado
2. ✅ Archivos XML de prueba en `src/main/resources/invoice/`
3. ✅ Emisor y receptor creados en BD

### Casos de Prueba

#### CP-337-01: Registrar Factura XML (Tipo I - Ingreso)

**Endpoint**: `POST /api/fiscal/xml/register`
**Content-Type**: `multipart/form-data`

**Datos de Entrada:**
```
file: factura_ejemplo.xml (CFDI Tipo I)
```

**Pasos:**
1. Abrir Postman
2. Crear request POST a `http://localhost:8082/api/fiscal/xml/register`
3. En Body → form-data:
   - Key: `file` (tipo File)
   - Value: Seleccionar archivo XML de factura
4. Click Send

**Resultado Esperado:**
```json
{
  "status": "success",
  "message": "Factura registrada exitosamente",
  "invoiceUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fiscalUuid": "12345678-1234-1234-1234-123456789012",
  "documentType": "I",
  "total": 1160.00,
  "subtotal": 1000.00,
  "taxes": {
    "totalTransferred": 160.00,
    "totalWithheld": 0.00
  }
}
```

**Validaciones:**
- ✅ Status HTTP 200 o 201
- ✅ `invoiceUuid` no nulo
- ✅ `fiscalUuid` coincide con UUID del XML
- ✅ Total calculado correctamente (subtotal - descuento + impuestos)
- ✅ Registro existe en BD: `SELECT * FROM tenant_fiscal.invoice WHERE invoice_uuid = '...'`
- ✅ Impuestos registrados: `SELECT * FROM tenant_fiscal.tax WHERE invoice_uuid = '...'`

---

#### CP-337-02: Registrar Nota de Crédito XML (Tipo E - Egreso)

**Endpoint**: `POST /api/fiscal/xml/register`
**Content-Type**: `multipart/form-data`

**Datos de Entrada:**
```
file: nota_credito_ejemplo.xml (CFDI Tipo E)
```

**Pasos:**
1. Seguir mismos pasos que CP-337-01
2. Usar archivo XML de Nota de Crédito

**Resultado Esperado:**
```json
{
  "status": "success",
  "message": "Nota de crédito registrada exitosamente",
  "invoiceUuid": "...",
  "documentType": "E",
  "relatedDocuments": [
    {
      "relatedUuid": "...",
      "relationType": "01"
    }
  ]
}
```

**Validaciones:**
- ✅ `documentType` = "E"
- ✅ CFDI relacionado existe: `SELECT * FROM tenant_fiscal.related_cfdi WHERE invoice_uuid = '...'`
- ✅ Tipo de relación correcto (01 = Nota de crédito)

---

#### CP-337-03: Validar Rechazo XML Duplicado

**Endpoint**: `POST /api/fiscal/xml/register`

**Datos de Entrada:**
```
file: mismo archivo XML del CP-337-01
```

**Resultado Esperado:**
```json
{
  "status": "error",
  "message": "El UUID fiscal ya existe en el sistema",
  "errorCode": "DUPLICATE_FISCAL_UUID"
}
```

**Validaciones:**
- ✅ Status HTTP 409 (Conflict) o 400 (Bad Request)
- ✅ Mensaje de error descriptivo
- ✅ No se crea registro duplicado en BD

---

#### CP-337-04: Validar Rechazo XML Inválido

**Endpoint**: `POST /api/fiscal/xml/register`

**Datos de Entrada:**
```
file: archivo_corrupto.xml (XML malformado)
```

**Resultado Esperado:**
```json
{
  "status": "error",
  "message": "El archivo XML no es válido",
  "errorCode": "INVALID_XML_FORMAT"
}
```

**Validaciones:**
- ✅ Status HTTP 400
- ✅ No se crea registro en BD

---

## JIRA STM-448: Consulta de Complementos de Pago

### Objetivo
Validar el endpoint de búsqueda de complementos de pago con filtros

### Pre-requisitos
1. ✅ Complementos de pago registrados en BD (tabla `tenant_fiscal.payments`)
2. ✅ Relaciones con facturas existentes

### Casos de Prueba

#### CP-448-01: Consultar Todos los Complementos de Pago (Sin Filtros)

**Endpoint**: `GET /api/payments?page=0&size=20`

**Pasos:**
1. GET request a `http://localhost:8082/api/payments?page=0&size=20`
2. Click Send

**Resultado Esperado:**
```json
{
  "content": [
    {
      "paymentsUuid": "...",
      "fiscalUuid": "...",
      "version": 2.0,
      "paymentDate": "2025-01-15",
      "status": 1,
      "issuer": { ... },
      "receiver": { ... },
      "payments": [...]
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

**Validaciones:**
- ✅ Status HTTP 200
- ✅ `content` es un array
- ✅ `totalElements` > 0
- ✅ Metadata de paginación correcta

---

#### CP-448-02: Buscar Complemento por UUID Fiscal

**Endpoint**: `GET /api/payments/fiscal/{fiscalUuid}`

**Datos de Entrada:**
```
fiscalUuid: "12345678-1234-1234-1234-123456789012"
```

**Pasos:**
1. GET request a `http://localhost:8082/api/payments/fiscal/12345678-1234-1234-1234-123456789012`

**Resultado Esperado:**
```json
{
  "paymentsUuid": "...",
  "fiscalUuid": "12345678-1234-1234-1234-123456789012",
  "version": 2.0,
  "paymentDate": "2025-01-15",
  "payments": [
    {
      "paymentUuid": "...",
      "amount": 5000.00,
      "currency": "MXN",
      "paymentMethod": "03",
      "relatedDocuments": [...]
    }
  ]
}
```

**Validaciones:**
- ✅ Status HTTP 200
- ✅ `fiscalUuid` coincide con el buscado
- ✅ Array `payments` contiene pagos individuales
- ✅ Cada pago tiene `relatedDocuments`

---

#### CP-448-03: Buscar Complementos por Receptor

**Endpoint**: `POST /api/payments/search`

**Datos de Entrada:**
```json
{
  "receiverRfc": "XAXX010101000",
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "page": 0,
  "size": 20
}
```

**Resultado Esperado:**
- Lista de complementos filtrados por RFC del receptor y rango de fechas

**Validaciones:**
- ✅ Todos los resultados tienen `receiver.rfc` = "XAXX010101000"
- ✅ Todas las fechas están en el rango especificado

---

## JIRA STM-771: Búsqueda Avanzada de Facturas y NC

### Objetivo
Validar filtros avanzados de búsqueda de facturas

### Casos de Prueba

#### CP-771-01: Buscar Facturas por RFC Emisor

**Endpoint**: `POST /api/invoices/search`

**Datos de Entrada:**
```json
{
  "issuerRfc": "SOD950101XYZ",
  "page": 0,
  "size": 20
}
```

**Resultado Esperado:**
```json
{
  "content": [...],
  "totalElements": 45,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

**Validaciones:**
- ✅ Todos los resultados tienen `issuer.rfc` = "SOD950101XYZ"
- ✅ Paginación funcional

---

#### CP-771-02: Buscar Facturas por Rango de Fechas y Tipo de Documento

**Endpoint**: `POST /api/invoices/search`

**Datos de Entrada:**
```json
{
  "documentType": "I",
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "page": 0,
  "size": 50
}
```

**Resultado Esperado:**
- Lista de facturas tipo "I" (Ingreso) emitidas en enero 2025

**Validaciones:**
- ✅ Todos los resultados tienen `documentType` = "I"
- ✅ Todas las fechas están en el rango
- ✅ Size correcto (50 items máximo)

---

#### CP-771-03: Buscar Facturas por UUID Fiscal

**Endpoint**: `GET /api/invoices/{fiscalUuid}`

**Datos de Entrada:**
```
fiscalUuid: "12345678-1234-1234-1234-123456789012"
```

**Resultado Esperado:**
```json
{
  "invoiceUuid": "...",
  "fiscalUuid": "12345678-1234-1234-1234-123456789012",
  "documentType": "I",
  "total": 1160.00,
  "subtotal": 1000.00,
  "issuer": { ... },
  "receiver": { ... },
  "taxes": { ... }
}
```

**Validaciones:**
- ✅ Status HTTP 200
- ✅ Factura encontrada con UUID correcto
- ✅ Datos completos de emisor, receptor e impuestos

---

## JIRA STM-972: Conversión XML a JSON

### Objetivo
Validar la conversión de archivos XML fiscales a JSON sin transformación

### Casos de Prueba

#### CP-972-01: Convertir XML Factura a JSON

**Endpoint**: `POST /api/xml-invoices/upload-convert`
**Content-Type**: `multipart/form-data`

**Datos de Entrada:**
```
file: factura_ejemplo.xml
```

**Pasos:**
1. POST request a `http://localhost:8082/api/xml-invoices/upload-convert`
2. Body → form-data → Key: `file` (tipo File)
3. Seleccionar archivo XML
4. Send

**Resultado Esperado:**
```json
{
  "comprobante": {
    "Version": "4.0",
    "Serie": "A",
    "Folio": "12345",
    "Fecha": "2025-01-15T10:30:00",
    "Subtotal": "1000.00",
    "Total": "1160.00",
    "Emisor": {
      "Rfc": "SOD950101XYZ",
      "Nombre": "SODIMAC SA DE CV"
    },
    "Receptor": {
      "Rfc": "XAXX010101000",
      "Nombre": "PUBLICO EN GENERAL"
    },
    "Impuestos": { ... }
  }
}
```

**Validaciones:**
- ✅ Status HTTP 200
- ✅ JSON preserva estructura exacta del XML
- ✅ Valores numéricos como strings (preservación)
- ✅ Atributos XML convertidos correctamente

---

#### CP-972-02: Detectar Tipo de Documento XML

**Endpoint**: `POST /api/xml-invoices/detect-document-type`

**Datos de Entrada:**
```
file: complemento_pago.xml
```

**Resultado Esperado:**
```json
{
  "documentType": "COMPLEMENTO_PAGO",
  "description": "Complemento de Pago CFDI 4.0",
  "version": "2.0"
}
```

**Validaciones:**
- ✅ Tipo de documento detectado correctamente
- ✅ Descripción informativa
- ✅ Versión del complemento identificada

---

## JIRA STM-973: Relación Descuento Comercial con NC

### Objetivo
Validar la relación entre descuentos comerciales (rebates) y Notas de Crédito

### Pre-requisitos
1. ✅ Descuentos comerciales en finanzas-api (tabla `rebate`)
2. ✅ Notas de crédito en fiscal-api (tabla `tenant_fiscal.invoice` con `document_type='E'`)

### Casos de Prueba

#### CP-973-01: Relacionar Descuento con NC

**Endpoint**: `POST /api/rebates/relate`
**Servicio**: finanzas-api (puerto 8091)

**Datos de Entrada:**
```json
{
  "rebateUuid": "550e8400-e29b-41d4-a716-446655440000",
  "invoiceFiscalUuid": "12345678-1234-1234-1234-123456789012",
  "xmlContent": "<cfdi:Comprobante...>...</cfdi:Comprobante>"
}
```

**Pasos:**
1. POST request a `http://localhost:8091/api/rebates/relate`
2. Body → raw → JSON
3. Enviar datos
4. Send

**Resultado Esperado:**
```json
{
  "status": "success",
  "message": "Descuento relacionado exitosamente con NC",
  "rebateUuid": "550e8400-e29b-41d4-a716-446655440000",
  "creditNoteUuid": "12345678-1234-1234-1234-123456789012",
  "validations": {
    "amountMatch": true,
    "vendorMatch": true,
    "documentType": "E"
  }
}
```

**Validaciones:**
- ✅ Status HTTP 200
- ✅ Relación creada en BD
- ✅ Validaciones ejecutadas correctamente:
  - Monto del descuento coincide con monto de NC
  - Proveedor del descuento coincide con emisor de NC
  - Documento es tipo "E" (Nota de Crédito)
- ✅ Campo `invoice_fiscal_uuid` actualizado en tabla `stamped_rebate`

**Consulta de Verificación:**
```sql
SELECT sr.*, r.amount, r.vendor_number
FROM stamped_rebate sr
JOIN rebate r ON r.document_number = sr.document_number
WHERE sr.stamped_rebate_uuid = '550e8400-e29b-41d4-a716-446655440000';
```

---

#### CP-973-02: Validar Rechazo por Tipo de Documento Incorrecto

**Endpoint**: `POST /api/rebates/relate`

**Datos de Entrada:**
```json
{
  "rebateUuid": "550e8400-e29b-41d4-a716-446655440000",
  "invoiceFiscalUuid": "87654321-4321-4321-4321-210987654321",
  "xmlContent": "... CFDI Tipo I (Factura) ..."
}
```

**Resultado Esperado:**
```json
{
  "status": "error",
  "message": "El documento debe ser una Nota de Crédito (Tipo E)",
  "errorCode": "INVALID_DOCUMENT_TYPE"
}
```

**Validaciones:**
- ✅ Status HTTP 400
- ✅ No se crea relación en BD
- ✅ Mensaje de error descriptivo

---

## JIRA STM-875: Búsqueda Avanzada de Descuentos Comerciales

### Objetivo
Validar filtros de búsqueda y paginación de descuentos comerciales

### Casos de Prueba

#### CP-875-01: Buscar Descuentos por Proveedor

**Endpoint**: `POST /api/rebates/filter`
**Servicio**: finanzas-api (puerto 8091)

**Datos de Entrada:**
```json
{
  "vendorNumber": 12345,
  "page": 0,
  "size": 20
}
```

**Resultado Esperado:**
```json
{
  "content": [
    {
      "rebateUuid": "...",
      "documentNumber": "DOC-2025-001",
      "vendorNumber": 12345,
      "amount": 5000.00,
      "status": 1,
      "dueDate": "2025-02-15",
      "postingDate": "2025-01-15"
    }
  ],
  "totalElements": 35,
  "totalPages": 2,
  "size": 20,
  "number": 0
}
```

**Validaciones:**
- ✅ Todos los resultados tienen `vendorNumber` = 12345
- ✅ Paginación funciona correctamente
- ✅ Datos completos en cada registro

---

#### CP-875-02: Buscar Descuentos por Rango de Fechas y Estado

**Endpoint**: `POST /api/rebates/filter`

**Datos de Entrada:**
```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "status": 1,
  "page": 0,
  "size": 50
}
```

**Resultado Esperado:**
- Lista de descuentos con estado 1 (activo) creados en enero 2025

**Validaciones:**
- ✅ Todos tienen `status` = 1
- ✅ Todas las fechas están en el rango
- ✅ Ordenamiento correcto (por fecha descendente)

---

#### CP-875-03: Exportar Descuentos a CSV

**Endpoint**: `GET /api/rebates/export/csv?vendorNumber=12345`

**Pasos:**
1. GET request a `http://localhost:8091/api/rebates/export/csv?vendorNumber=12345`
2. Send
3. Guardar archivo descargado

**Resultado Esperado:**
- Archivo CSV descargado con formato:
```csv
UUID,DocumentNumber,VendorNumber,Amount,Status,DueDate,PostingDate
550e8400-...,DOC-2025-001,12345,5000.00,1,2025-02-15,2025-01-15
...
```

**Validaciones:**
- ✅ Content-Type: `text/csv`
- ✅ Archivo descargable
- ✅ Headers correctos en primera línea
- ✅ Datos formateados correctamente
- ✅ Codificación UTF-8

---

## JIRA STM-304: Soporte de Filtrado y CSV para Guías de Envío

### Objetivo
Validar filtros y exportación CSV de guías de envío

### Casos de Prueba

#### CP-304-01: Buscar Guías de Envío por Proveedor

**Endpoint**: `GET /api/shipping-guides?vendorNumber=12345&page=0&size=20`
**Servicio**: finanzas-api (puerto 8091)

**Resultado Esperado:**
```json
{
  "content": [
    {
      "shippingGuideUuid": "...",
      "guideNumber": "GE-2025-001",
      "vendorNumber": 12345,
      "originWarehouse": "CDMX-01",
      "destinationWarehouse": "GDL-02",
      "status": 1,
      "purchaseOrders": [...]
    }
  ],
  "totalElements": 20,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Validaciones:**
- ✅ Filtro por `vendorNumber` funciona
- ✅ Relación con órdenes de compra cargada

---

#### CP-304-02: Exportar Guías de Envío a CSV

**Endpoint**: `GET /api/shipping-guides/export/csv?vendorNumber=12345`

**Resultado Esperado:**
- Archivo CSV con guías de envío

**Validaciones:**
- ✅ Archivo descargable
- ✅ Formato CSV correcto
- ✅ Datos completos exportados

---

## 🧪 PRUEBAS DE INTEGRACIÓN

### PI-01: Flujo Completo de Facturación

**Objetivo**: Validar el flujo end-to-end de registro y consulta de factura

**Pasos:**
1. Registrar factura XML → `POST /api/fiscal/xml/register`
2. Obtener `invoiceUuid` de la respuesta
3. Consultar factura registrada → `GET /api/invoices/{invoiceUuid}`
4. Verificar impuestos → `GET /api/invoices/{invoiceUuid}/taxes`
5. Buscar factura por RFC emisor → `POST /api/invoices/search`

**Resultado Esperado:**
- ✅ Factura registrada correctamente
- ✅ Impuestos calculados y almacenados
- ✅ Búsqueda retorna la factura
- ✅ Todos los datos consistentes entre endpoints

---

### PI-02: Flujo Completo de Relación Descuento-NC

**Objetivo**: Validar integración entre finanzas-api y fiscal-api

**Pasos:**
1. Crear descuento comercial en finanzas-api
2. Registrar NC en fiscal-api → `POST /api/fiscal/xml/register`
3. Relacionar descuento con NC → `POST /api/rebates/relate`
4. Consultar descuento actualizado → `GET /api/rebates/{rebateUuid}`
5. Verificar relación en BD

**Resultado Esperado:**
- ✅ Descuento y NC creados correctamente
- ✅ Relación establecida exitosamente
- ✅ Validaciones ejecutadas correctamente
- ✅ Campo `invoice_fiscal_uuid` actualizado en `stamped_rebate`

---

## 📊 CRITERIOS DE ACEPTACIÓN

### Funcionales
- ✅ Todos los endpoints responden correctamente
- ✅ Validaciones de negocio funcionan
- ✅ Paginación opera correctamente
- ✅ Filtros devuelven resultados esperados
- ✅ Exportación CSV funciona

### No Funcionales
- ✅ Tiempo de respuesta < 2 segundos (endpoints de consulta)
- ✅ Tiempo de respuesta < 5 segundos (endpoints de registro XML)
- ✅ Manejo correcto de errores (HTTP status codes)
- ✅ Logs informativos en consola
- ✅ Transacciones atómicas (rollback en caso de error)

### De Datos
- ✅ Datos persisten correctamente en BD
- ✅ Integridad referencial mantenida
- ✅ No hay duplicados de UUID fiscal
- ✅ Cálculos de impuestos correctos

---

## 🐛 REGISTRO DE DEFECTOS

| ID | Descripción | Severidad | Estado |
|----|-------------|-----------|--------|
| - | Sin defectos registrados | - | - |

---

## ✅ CHECKLIST FINAL

- [ ] Todos los endpoints de fiscal-api probados
- [ ] Todos los endpoints de finanzas-api probados
- [ ] Pruebas de integración ejecutadas
- [ ] Exportación CSV validada
- [ ] Manejo de errores verificado
- [ ] Paginación validada en todos los endpoints
- [ ] Validaciones de negocio confirmadas
- [ ] Datos en BD consistentes
- [ ] Logs revisados (sin errores críticos)
- [ ] Documentación de Postman actualizada

---

## 📌 NOTAS ADICIONALES

### Datos de Prueba Sugeridos

**Emisores de prueba:**
- RFC: SOD950101XYZ (Sodimac)
- RFC: FAL980101ABC (Falabella)

**Receptores de prueba:**
- RFC: XAXX010101000 (Público en general)
- RFC: PROV850101XYZ (Proveedor ejemplo)

**Proveedores (vendor_number):**
- 12345, 67890, 11111

### Archivos XML de Prueba

Ubicación: `c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api\src\main\resources\invoice\`

- `factura_ejemplo.xml` - CFDI Tipo I
- `nota_credito_ejemplo.xml` - CFDI Tipo E
- `complemento_pago_ejemplo.xml` - CFDI Tipo P

---

**Fin del Plan de Pruebas**
