# STM-704: Resumen de Implementacion para JIRA

## Descripcion del cambio

Se implemento la bitacora de auditoria para el registro de facturas y notas de credito (`registerInvoice`) y se estandarizo la auditoria en la actualizacion de facturas (`updateInvoice`), migrando de `ActivityLogService` (tabla `tenant_fiscal.log`) a `AuditoriaApiService` (servicio externo `auditoria-api`).

## Archivos creados

| Archivo | Descripcion |
|---------|-------------|
| `AuditoriaApiService.java` | Interface del cliente HTTP para auditoria-api |
| `AuditoriaApiServiceImpl.java` | Implementacion con RestTemplate, fire-and-forget, truncamiento a 100 chars |

## Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `InvoiceController.java` | Nuevo parametro `idTransaccion` (query param, obligatorio). Validacion con error `FISCAL-ERR-102` si no viene |
| `InvoiceService.java` | Firma actualizada: `registerInvoice(MultipartFile, String idTransaccion)` |
| `InvoiceServiceImpl.java` | 13 puntos de auditoria en `registerInvoice()` + migracion de `updateInvoice()` a `auditoriaApiService` |
| `FiscalApiApplication.java` | Agregado `@EnableAsync` (preparado para uso futuro) |
| `application-dev.properties` | Nuevas propiedades `auditoria.api.enabled` y `auditoria.api.url` |

## Parametro nuevo en API

**Endpoint:** `POST /invoices/register`
**Parametro:** `idTransaccion` (query param)
**Obligatorio:** Si
**Tipo:** String (UUID)
**Error si falta:** HTTP 400, codigo `FISCAL-ERR-102`, mensaje "El idTransaccion es obligatorio para el registro"

## Variables de entorno nuevas

| Variable | Default (dev) | Descripcion |
|----------|---------------|-------------|
| `AUDITORIA_API_ENABLED` | `true` | Habilita/deshabilita envio de logs |
| `AUDITORIA_API_URL` | `http://localhost:8091` | URL base de auditoria-api |

## Acciones de auditoria registradas

### registerInvoice (13 puntos)

| # | Action | Descripcion |
|---|--------|-------------|
| 1 | REGISTRO_REQUEST | Inicio del registro (datos del archivo) |
| 2 | LEER_ARCHIVO_XML | Lectura del contenido XML |
| 3 | DETECTAR_TIPO_DOCUMENTO | Deteccion factura (I) o NC (E) |
| 4 | PROCESAR_XML_CFDI | Parseo JAXB del XML CFDI 4.0 |
| 5 | VALIDAR_SERIE_FOLIO | Validacion de serie y folio |
| 6 | VALIDAR_VERSION_CFDI | Validacion version CFDI vigente |
| 7 | VALIDAR_RFC_RECEPTOR | RFC receptor autorizado |
| 8 | OBTENER_EMISOR | Obtener/crear emisor (proveedor) |
| 9 | VALIDAR_DUPLICADO_SERIE_FOLIO | Verificar no duplicado por serie+folio |
| 10 | VALIDAR_DUPLICADO_UUID | Verificar no duplicado por UUID fiscal |
| 11 | VALIDAR_ADDENDA | Validacion de addenda Sodimac |
| 12 | PERSISTIR_DOCUMENTO | Guardado en BD |
| 13 | REGISTRO_RESPONSE | Response final (incluye durationMs) |
| E1 | REGISTRO_ERROR_NEGOCIO | Error de validacion de negocio |
| E2 | REGISTRO_ERROR_TECNICO | Error tecnico inesperado |

### updateInvoice (migrado de ActivityLogService)

| # | Action | Descripcion |
|---|--------|-------------|
| 1 | ACTUALIZACION_FACTURA | Actualizacion exitosa |
| E1 | ACTUALIZACION_ERROR_NEGOCIO | Error de negocio |
| E2 | ACTUALIZACION_ERROR_TECNICO | Error tecnico |

## Pruebas realizadas

| Escenario | Resultado | Logs auditoria |
|-----------|-----------|----------------|
| Registro exitoso | HTTP 200, RES005 | 13 registros (todos isError=false) |
| Sin idTransaccion | HTTP 400, FISCAL-ERR-102 | 0 registros |
| Factura duplicada | HTTP 400, WRN7013 | 9 registros (8 OK + 1 error) |

## Impacto en otros componentes

### BFF (APP03022-mrch.bff.somx.ppsomx.fiscal)
- **openapi.yaml** debe actualizarse para agregar `idTransaccion` como query param en `/invoices/register`
- **El BFF es proxy transparente**, no requiere cambios en codigo (solo OpenAPI)

### Frontend (APP03022-mrch.frontend.somx.finanzas-spa)
- **InvoiceClient.ts** linea 21: `create(invoice: File)` debe agregar `idTransaccion` como parametro
- Cambiar a: `create(invoice: File, idTransaccion: string)` y pasarlo como query param en la URL

### auditoria-api
- Sin cambios requeridos. El servicio ya esta funcionando y acepta los campos que enviamos.

## Dependencia para deploy

Para que la auditoria funcione en ambientes:
1. `auditoria-api` debe estar desplegado y accesible
2. Configurar `AUDITORIA_API_URL` con la URL del servicio en el ambiente
3. Configurar `AUDITORIA_API_ENABLED=true`
4. Si auditoria-api no esta disponible, fiscal-api funciona normalmente (solo loguea warning)
