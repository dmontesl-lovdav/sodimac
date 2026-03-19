# STM-704: Analisis Tecnico

## Situacion Actual

El metodo `registerInvoice()` en `InvoiceServiceImpl` (linea 112) ejecuta ~10 pasos de validacion pero **NO registra nada en la bitacora**. Solo el metodo `updateInvoice()` tiene algo de logging con `ActivityLogService` (tabla `tenant_fiscal.log`).

### Validaciones actuales del registro

| Paso | Validacion | Metodo/Servicio |
|------|-----------|-----------------|
| 1 | Leer contenido del archivo XML | `readXmlFile()` |
| 2 | Detectar tipo de documento (I/E) | `XmlDocumentTypeDetector.detectDocumentType()` |
| 3 | Procesar y parsear XML CFDI | `CfdiXmlProcessorService.processCfdi()` |
| 3.1 | Validar serie y folio (STM-395/397) | `validateSeriesAndFolio()` |
| 4 | Validar version CFDI vigente | `validateCfdiVersion()` |
| 5 | Validar RFC receptor autorizado | `validateAuthorizedReceiver()` |
| 6 | Obtener emisor (proveedor) | `IssuerService.getOrCreate()` |
| 6.1 | Validar duplicado por serie+folio | `validateNoDuplicateBySeriesAndFolio()` |
| 6.2 | Extraer UUID fiscal, validar duplicado | `extractFiscalUuid()`, `validateNoDuplicateByUuid()` |
| 7 | Validar addenda | `AddendaValidationService.validateAddenda()` |
| 8 | Persistir en base de datos | `saveInvoiceToDatabase()` |
| 9 | Construir respuesta | `buildRegistrationSuccessResponse()` |

---

## Servicio de bitacora: auditoria-api

La bitacora se implementa mediante el proyecto `APP03022-mrch.backend.somx.auditoria-api` (Node.js/TypeScript).

### Datos del servicio

| Campo | Valor |
|-------|-------|
| **Proyecto** | `APP03022-mrch.backend.somx.auditoria-api` |
| **Stack** | Node.js 20 + Express + TypeScript + TypeORM |
| **Puerto (dev)** | 8091 (default 3711) |
| **Esquema BD** | `core_audit` |
| **Tabla** | `core_audit.activity_logs` |

### Endpoint principal

**POST `/api/activity-logs/`** (fire-and-forget, retorna 201 inmediato)

Request:
```json
{
  "traceId": "uuid (requerido) - idTransaccion",
  "traceFrontId": "uuid (opcional) - id desde el front",
  "modulo": "string (requerido) - ej: fiscal-api",
  "action": "string (requerido) - ej: REGISTRO_FACTURA",
  "serviceName": "string (requerido) - ej: InvoiceService.registerInvoice",
  "userId": "string (requerido)",
  "isError": "boolean (requerido)",
  "message": "string (requerido) - mensaje funcional",
  "messageDetail": "string (requerido) - mensaje tecnico",
  "details": "object (opcional, JSONB) - datos adicionales",
  "durationms": "number (opcional) - duracion en ms",
  "timestamp": "date (requerido)"
}
```

### Endpoint consulta

**GET `/api/activity-logs/`** - Consulta con paginacion

Query params: `dateAtInitial`, `dateAtEnd`, `traceId`, `modulo`, `serviceName`, `pageNumber`, `pageSize`

### Endpoint UUID

**GET `/api/activity-logs/uuid`** - Genera un UUID (puede usarse para obtener el traceId)

### Tabla `core_audit.activity_logs`

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| activity_logs_uuid | UUID (PK) | Auto-generado |
| trace_id | UUID | ID de correlacion/transaccion |
| trace_front_id | UUID | ID del frontend |
| duration_ms | Decimal(18,6) | Duracion en ms |
| is_error | boolean | Flag de error |
| modulo | varchar | Modulo (ej: fiscal-api) |
| service_name | varchar | Servicio |
| action | varchar | Accion |
| message | varchar | Mensaje funcional |
| message_detail | text | Mensaje tecnico detallado |
| user_id | varchar | Usuario |
| timestamp | timestamp | Fecha/hora |
| details | JSONB | Datos adicionales (flexible) |

---

## Plan de implementacion

### 1. Crear cliente HTTP para auditoria-api en fiscal-api

Nuevo servicio `AuditoriaApiService` (similar a `UtilsApiServiceImpl`):
- Property: `auditoria.api.enabled` (default: false)
- URL: `auditoria.api.url` (default: `http://localhost:8091`)
- Metodo `logActivity(traceId, action, isError, message, messageDetail, details, userId)`
- Fire-and-forget (async, no bloquea el flujo de registro)
- Si falla, solo loguear warning (no afectar operacion principal)

### 2. Modificar `registerInvoice()` para recibir idTransaccion

El `traceId` (idTransaccion) llega como parametro al metodo. Se debe:
- Agregar parametro al controller (header o query param o en el multipart)
- Pasarlo al metodo de servicio
- Usarlo como `traceId` en cada llamada a auditoria-api

### 3. Registrar cada paso de validacion

Por cada paso del flujo, llamar a `auditoriaApiService.logActivity()` con:
- `traceId` = idTransaccion recibido
- `action` = nombre del paso (ej: "VALIDAR_ESTRUCTURA_XML", "VALIDAR_XSD", etc.)
- `isError` = true/false segun resultado
- `message` = mensaje funcional
- `messageDetail` = detalle tecnico
- `details` = JSON con datos del documento (serie, folio, RFC, etc.)

### 4. Guardar request y response

- Al inicio: registrar log con `details` = request completo (nombre archivo, tamaño)
- Al final: registrar log con `details` = response (codigo, uuid, etc.)

---

## Integracion actual

Actualmente fiscal-api **NO tiene ningun cliente hacia auditoria-api**. No hay referencias a "auditoria" en el proyecto. La integracion es nueva.

---

## Archivos a crear/modificar

| Archivo | Accion | Descripcion |
|---------|--------|-------------|
| `AuditoriaApiService.java` | Crear | Interface del cliente |
| `AuditoriaApiServiceImpl.java` | Crear | Implementacion con RestTemplate |
| `InvoiceServiceImpl.java` | Modificar | Agregar llamadas a auditoria en `registerInvoice()` |
| `InvoiceController.java` | Modificar | Recibir idTransaccion como parametro |
| `application-dev.yml` | Modificar | Agregar config de auditoria-api |

---

## Checklist de Desarrollo

### Analisis
- [x] Requerimientos entendidos (transcripcion en README.md)
- [x] Analisis tecnico completado
- [x] Servicio de bitacora identificado (auditoria-api)
- [x] Definir como llega el idTransaccion → query param `@RequestParam`

### Backend
- [x] Crear `AuditoriaApiService` + `AuditoriaApiServiceImpl`
- [x] Configurar URL y enabled en application-dev.properties
- [x] Modificar controller para recibir idTransaccion (con validacion FISCAL-ERR-102)
- [x] Modificar `registerInvoice()` para registrar cada paso (13 puntos de auditoria)
- [x] Guardar request y response completos (con durationMs)
- [x] Manejo de errores (auditoria no afecta registro, solo loguea warning)
- [x] Migrar `updateInvoice()` de activityLogService a auditoriaApiService
- [x] Anotacion @Parameter de Swagger para idTransaccion

### Testing
- [x] Pruebas con registro exitoso (13 logs en auditoria)
- [x] Pruebas con error en validacion (9 logs: 8 OK + 1 error)
- [x] Pruebas sin idTransaccion (HTTP 400, FISCAL-ERR-102, 0 logs)
- [x] Postman collection creada (STM-704-Register-Invoice-Auditoria.postman_collection.json)

### Documentacion
- [x] Variables de entorno documentadas (AUDITORIA_API_ENABLED, AUDITORIA_API_URL)
- [x] MD para actualizar JIRA (jira-update.md)
- [x] OpenAPI BFF actualizado (openapi.yaml, openapi.json, openapi-bundled.yaml)
- [x] Impacto en frontend identificado (InvoiceClient.ts necesita agregar idTransaccion)
