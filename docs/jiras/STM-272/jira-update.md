# STM-272: Resumen de implementacion para actualizar JIRA

## Que se hizo

Se integro el servicio auditoria-api en el flujo de registro de complemento de pago (`registerPayment`) para registrar trazabilidad paso a paso.

## Cambios realizados

### Backend (fiscal-api)
1. **PaymentRegistrationController**: nuevo query param `idTransaccion` obligatorio con validacion FISCAL-ERR-102
2. **PaymentRegistrationServiceImpl**: inyeccion de `AuditoriaApiService`, 13 puntos de auditoria en el flujo
3. **AuditAction enum**: 14 nuevas constantes `PAGO_*` para acciones de complemento de pago
4. **Swagger**: anotacion `@Parameter` para documentar idTransaccion

### BFF fiscal
5. **OpenAPI**: parametro `idTransaccion` agregado en `POST /fiscal/complementos-pago/registrar` (yaml, json, bundled)

## Puntos de auditoria registrados (13 en flujo exitoso)

| # | Accion | Descripcion |
|---|--------|-------------|
| 1 | PAGO_REGISTRO_REQUEST | Inicio del registro |
| 2 | PAGO_LEER_ARCHIVO_XML | Lectura del archivo XML |
| 3 | PAGO_VALIDAR_TIPO_ADDENDA | Validacion tipo addenda |
| 4 | PAGO_VALIDAR_ESTRUCTURA_XSD | Validacion estructura XSD |
| 5 | PAGO_PARSEAR_XML | Parseo del XML Pagos 2.0 |
| 6 | PAGO_VALIDAR_TIPO_COMPROBANTE | Validacion tipo comprobante (P) |
| 7 | PAGO_VALIDAR_DUPLICADO | Validacion UUID no duplicado |
| 8 | PAGO_VALIDAR_RECEPTOR | Validacion receptor autorizado |
| 9 | PAGO_VALIDAR_VERSION | Validacion version Pagos 2.0 |
| 10 | PAGO_VALIDAR_SAT | Validacion SAT (omitida, pendiente multipac) |
| 11 | PAGO_PERSISTIR_BD | Persistencia en BD |
| 12 | PAGO_REGISTRO_ARCHIVO | Registro de archivo procesado |
| 13 | PAGO_REGISTRO_RESPONSE | Response exitoso con duracion |

En caso de error se agrega: `PAGO_REGISTRO_ERROR`

## Variables de entorno

Reutiliza las mismas de STM-704:
- `AUDITORIA_API_ENABLED` (default: false en dev, true en prod)
- `AUDITORIA_API_URL` (default: http://localhost:8091 en dev)

## Impacto

- **Backend**: fiscal-api (4 archivos modificados)
- **BFF**: fiscal (3 archivos OpenAPI)
- **Frontend**: debe enviar `idTransaccion` como query param al BFF
- **Base de datos**: sin cambios (usa tabla existente `core_audit.activity_logs`)

## Pruebas realizadas

| Test | Resultado | Logs auditoria |
|------|-----------|----------------|
| Registro exitoso | 200, SUCCESS | 13 logs |
| Sin idTransaccion | 400, FISCAL-ERR-102 | 0 |
| idTransaccion vacio | 400, FISCAL-ERR-102 | 0 |
| Duplicado | 422, error | 7 logs (6 OK + 1 error) |
