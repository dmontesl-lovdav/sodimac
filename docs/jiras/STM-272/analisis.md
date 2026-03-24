# STM-272: Analisis Tecnico

## Relacion con STM-704

Este JIRA es el equivalente del STM-704 pero para **complemento de pago** en lugar de factura/NC. La infraestructura de auditoria (AuditoriaApiService, AuditoriaApiServiceImpl, configuracion) ya fue creada en STM-704 y se reutiliza directamente.

---

## Situacion Actual

El metodo `registerPayment()` en `PaymentRegistrationServiceImpl` (linea 114) ejecuta 11 pasos de validacion pero **NO registra nada en la bitacora de auditoria**. Solo tiene logs de consola (`log.info`/`log.debug`).

### Validaciones actuales del registro

| Paso | Validacion | Metodo/Servicio |
|------|-----------|-----------------|
| 1 | Leer archivo XML | `readXmlFile()` |
| 2 | Validar tipo de addenda | `validationService.validateAddendaType()` |
| 3 | Validar estructura XML contra XSD | `xmlParserService.validateXmlStructure()` |
| 4 | Parsear XML | `xmlParserService.parsePaymentXml()` |
| 5 | Validar tipo de comprobante (P) | `validationService.validateComprobanteType()` |
| 6 | Validar no duplicado (UUID fiscal) | `validationService.validateNoDuplicate()` |
| 7 | Validar receptor autorizado | `validationService.validateAuthorizedReceiver()` |
| 8 | Validar version vigente (Pagos 2.0) | `validationService.validatePaymentVersion()` |
| 9 | Validar con SAT via multipac | **Comentado** (igual que en facturas) |
| 10 | Registrar en base de datos | `savePaymentToDatabase()` |
| 11 | Registrar archivo procesado | `saveFileRegistry()` |

### Diferencias con el flujo de facturas (STM-704)

| Aspecto | Facturas (STM-704) | Complemento de pago (STM-272) |
|---------|-------------------|-------------------------------|
| Controller | `InvoiceController` | `PaymentRegistrationController` |
| Service | `InvoiceServiceImpl.registerInvoice()` | `PaymentRegistrationServiceImpl.registerPayment()` |
| Endpoint | `POST /invoices/register` | `POST /fiscal/complementos-pago/registrar` |
| Entrada | `MultipartFile` + `idTransaccion` (query param) | `PaymentRegistrationRequest` (ModelAttribute) |
| idTransaccion | Ya implementado como query param | **No existe, hay que agregarlo** |
| Tipo doc | I (Factura) o E (NC) | P (Pago) |
| Pasos | 10 pasos | 11 pasos |
| AuditoriaApiService | Ya inyectado | **No inyectado** |
| Constructor | `@RequiredArgsConstructor` (Lombok) | Constructor manual (por @Qualifier) |

---

## Servicio de bitacora: auditoria-api

Ya implementado en STM-704. Reutilizar:
- `AuditoriaApiService` (interface)
- `AuditoriaApiServiceImpl` (RestTemplate, fire-and-forget, @Async)
- Configuracion en `application-dev.properties` y `application-prod.properties`

---

## Plan de implementacion

### 1. Agregar idTransaccion al controller

**Opcion elegida**: Query param (consistente con STM-704 en InvoiceController)

En `PaymentRegistrationController.registrarComplementoPago()`:
- Agregar `@RequestParam(value = "idTransaccion", required = false) String idTransaccion`
- Validar que no sea null/vacio (retornar 400 con FISCAL-ERR-102)
- Pasar al service

### 2. Inyectar AuditoriaApiService en PaymentRegistrationServiceImpl

El constructor es manual (no usa Lombok) por el `@Qualifier` en PacService. Agregar el parametro:

```java
private final AuditoriaApiService auditoriaApiService;
// ... en constructor:
this.auditoriaApiService = auditoriaApiService;
```

### 3. Modificar firma de `registerPayment()`

En interface `PaymentRegistrationService`:
```java
PaymentRegistrationResponse registerPayment(PaymentRegistrationRequest request, String idTransaccion);
```

### 4. Agregar puntos de auditoria

Agregar `auditoriaApiService.logActivity()` en cada paso:

| # | Action (AuditAction) | Punto de insercion |
|---|---------------------|--------------------|
| 1 | `PAGO_REGISTRO_REQUEST` | Inicio del metodo |
| 2 | `PAGO_LEER_ARCHIVO_XML` | Despues de readXmlFile() |
| 3 | `PAGO_VALIDAR_TIPO_ADDENDA` | Despues de validateAddendaType() |
| 4 | `PAGO_VALIDAR_ESTRUCTURA_XSD` | Despues de validateXmlStructure() |
| 5 | `PAGO_PARSEAR_XML` | Despues de parsePaymentXml() |
| 6 | `PAGO_VALIDAR_TIPO_COMPROBANTE` | Despues de validateComprobanteType() |
| 7 | `PAGO_VALIDAR_DUPLICADO` | Despues de validateNoDuplicate() |
| 8 | `PAGO_VALIDAR_RECEPTOR` | Despues de validateAuthorizedReceiver() |
| 9 | `PAGO_VALIDAR_VERSION` | Despues de validatePaymentVersion() |
| 10 | `PAGO_VALIDAR_SAT` | Despues del paso SAT (comentado) |
| 11 | `PAGO_PERSISTIR_BD` | Despues de savePaymentToDatabase() |
| 12 | `PAGO_REGISTRO_ARCHIVO` | Despues de saveFileRegistry() |
| 13 | `PAGO_REGISTRO_RESPONSE` | Response exitoso (con durationMs) |
| 14 | `PAGO_REGISTRO_ERROR` | Catch de errores |

Total: **14 puntos de auditoria** (12 flujo normal + 1 response + 1 error)

### 5. Agregar constantes al enum AuditAction

Agregar seccion `// ========== REGISTRO COMPLEMENTO DE PAGO (registerPayment) ==========` con las 14 constantes.

### 6. Actualizar OpenAPI del BFF

Agregar parametro `idTransaccion` en `POST /fiscal/complementos-pago/registrar` (igual que se hizo para `/invoices/register`).

---

## Archivos a crear/modificar

| Archivo | Accion | Descripcion |
|---------|--------|-------------|
| `PaymentRegistrationController.java` | Modificar | Agregar idTransaccion como query param + @Parameter Swagger |
| `PaymentRegistrationService.java` | Modificar | Actualizar firma: `registerPayment(request, idTransaccion)` |
| `PaymentRegistrationServiceImpl.java` | Modificar | Inyectar AuditoriaApiService, agregar 14 puntos de auditoria |
| `AuditAction.java` | Modificar | Agregar 14 constantes para flujo de pago |
| `openapi.yaml` (BFF) | Modificar | Agregar idTransaccion en /fiscal/complementos-pago/registrar |
| `openapi.json` (BFF) | Modificar | Idem en formato JSON |
| `openapi-bundled.yaml` (BFF) | Modificar | Idem en version bundled |

**No se crean archivos nuevos** - toda la infraestructura de auditoria ya existe (STM-704).

---

## Checklist de Desarrollo

### Analisis
- [x] Requerimientos entendidos (transcripcion en README.md)
- [x] Analisis tecnico completado
- [x] Diferencias con STM-704 identificadas
- [x] Definir idTransaccion como query param (consistente con STM-704)

### Backend
- [x] Modificar controller para recibir idTransaccion (con validacion FISCAL-ERR-102)
- [x] Actualizar firma de `registerPayment()` en interface y impl
- [x] Inyectar AuditoriaApiService en PaymentRegistrationServiceImpl (constructor manual)
- [x] Agregar 14 constantes al enum AuditAction
- [x] Agregar 14 puntos de auditoria en registerPayment()
- [x] Log del paso SAT (comentado, igual que STM-704)
- [x] Anotacion @Parameter de Swagger para idTransaccion

### Testing
- [x] Prueba registro exitoso (13 logs en auditoria)
- [x] Prueba sin idTransaccion (HTTP 400, FISCAL-ERR-102)
- [x] Prueba con error duplicado (7 logs: 6 OK + 1 error)
- [x] Crear XMLs de prueba para complemento de pago
- [x] Postman collection

### Documentacion
- [x] MD para actualizar JIRA (jira-update.md)
- [x] OpenAPI BFF actualizado (yaml, json, bundled)
- [x] Guia de ramas y PR
- [x] Scripts SQL de consulta de bitacora
