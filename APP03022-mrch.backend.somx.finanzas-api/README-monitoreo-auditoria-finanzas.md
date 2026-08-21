# Inventario de eventos monitoreados — Finanzas API

## 1. Objetivo

Este documento presenta el inventario consolidado de los eventos que actualmente son registrados por `finanzas-api` en la tabla:

```text
core_audit.activity_logs
```

El inventario fue obtenido mediante la revisión de los middlewares, rutas, controladores y servicios del proyecto, además de registros observados directamente en la Bitácora de Actividades.

## 2. Información general almacenada

Los registros generados por el mecanismo actual contienen, entre otros, los siguientes datos:

| Campo | Valor o procedencia actual |
|---|---|
| Módulo | `API_FINANZAS` |
| Aplicativo | Nombre entregado a `activityLogger()` o `UNKNOWN` |
| Usuario | `system` |
| ID de transacción | UUID generado por el backend |
| ID de transacción del frontend | Header `TraceFrontId`, cuando está disponible |
| Acción | Método o proceso almacenado en el contexto |
| Fecha | Fecha y hora generada por el backend |
| Tipo de evento | `INFO` o `ERROR` |
| Detalles | Método, URL, IP, body, query, respuesta y duración, según el evento |

El sistema soporta el tipo `ALERTA`, pero no se encontraron llamadas que actualmente lo utilicen.

## 3. Eventos automáticos generales

Las rutas que utilizan `activityLogger()` generan automáticamente:

```text
START_{MÉTODO_HTTP}
END_{MÉTODO_HTTP}
```

Cuando una ruta también utiliza `logBeforeMethod()`, se agrega:

```text
INIT_METHOD
```

Un flujo exitoso instrumentado normalmente produce:

```text
START_GET / START_POST / START_PUT / START_PATCH / START_DELETE
INIT_METHOD
END_GET / END_POST / END_PUT / END_PATCH / END_DELETE
```

## 4. Carta Porte

Aplicativo almacenado:

```text
CartaPorte
```

| Operación | Método | Proceso | Eventos automáticos |
|---|---|---|---|
| Registrar guía de embarque | POST | `guia-embarque` | `START_POST`, `INIT_METHOD`, `END_POST` |
| Registrar orden de compra | POST | `oc` | `START_POST`, `INIT_METHOD`, `END_POST` |
| Registrar OC, guía y documentos | POST | `all` | `START_POST`, `INIT_METHOD`, `END_POST` |
| Consultar guías | POST | `findAllGuia` | `START_POST`, `INIT_METHOD`, `END_POST` |
| Actualizar estatus de guías | POST | `updateAllStatusGuia` | `START_POST`, `INIT_METHOD`, `END_POST` |

Eventos de error específicos:

```text
ERROR : No fue posible registrar la GUIA DE carta porte, Favor de validar
ERROR : No fue posible registrar la OC DE carta porte, Favor de validar
ERROR : No fue posible registrar la OC Y LA DOCUMENTACION DE carta porte, Favor de validar
ERROR: No fue posible encontrar las guias
ERROR: AL ACTULIAZAR LOS ESTATUS DE LAS GUIAS
```

Un error dentro del controlador puede producir:

```text
START_POST       INFO
INIT_METHOD      INFO
ERROR específico ERROR
END_POST         INFO
```

Actualmente `END_POST` continúa almacenándose como `INFO` aunque la respuesta haya terminado con un código de error.

## 5. Órdenes de compra y recepciones

Aplicativo almacenado:

```text
PurchaseOrders
```

| Método | Proceso monitoreado | Eventos automáticos |
|---|---|---|
| GET | `listReception` | `START_GET`, `INIT_METHOD`, `END_GET` |
| GET | `listReceptionV2` | `START_GET`, `INIT_METHOD`, `END_GET` |
| PATCH | `updateReception` | `START_PATCH`, `INIT_METHOD`, `END_PATCH` |
| GET | `getReceptionById` | `START_GET`, `INIT_METHOD`, `END_GET` |
| PATCH | `updateReceptionStatusByUuid` | `START_PATCH`, `INIT_METHOD`, `END_PATCH` |
| POST | `save` | `START_POST`, `INIT_METHOD`, `END_POST` |
| GET | `list` | `START_GET`, `INIT_METHOD`, `END_GET` |
| GET | `getById` | `START_GET`, `INIT_METHOD`, `END_GET` |
| PATCH | `updateById` | `START_PATCH`, `INIT_METHOD`, `END_PATCH` |

Eventos de error encontrados:

```text
ERROR:
ERROR
ERROR: NO FUE POSIBLE LISTAR LAS RECEPCIONES
```

## 6. Guías de embarque

Aplicativo almacenado:

```text
ShippingGuide
```

| Método | Proceso monitoreado | Eventos automáticos |
|---|---|---|
| GET | `downloadFile` | `START_GET`, `INIT_METHOD`, `END_GET` |
| GET | `list` | `START_GET`, `INIT_METHOD`, `END_GET` |
| GET | `csvExport` | `START_GET`, `INIT_METHOD`, `END_GET` |
| POST | `cancel` | `START_POST`, `INIT_METHOD`, `END_POST` |
| Por confirmar | `updateStatus` | `START_*`, `INIT_METHOD`, `END_*` |
| GET | `getById` | `START_GET`, `INIT_METHOD`, `END_GET` |
| PUT | `updateByUuid` | `START_PUT`, `INIT_METHOD`, `END_PUT` |
| PATCH | `updateByGuide` | `START_PATCH`, `INIT_METHOD`, `END_PATCH` |
| DELETE | `remove` | `START_DELETE`, `INIT_METHOD`, `END_DELETE` |

Eventos de error específicos:

```text
ERROR  : No fue posible exportar a csv, Favor de validar
ERROR  : No fue posible listar las guias de enbarque, Favor de validar
ERROR update shipping guide status
ERROR cancel shipping guides
ERROR  :
```

También se almacenan mensajes dinámicos provenientes de:

```ts
CatMsgExc.description
```

## 7. Pagos de Finanzas

Aplicativo almacenado:

```text
FinanzasPayment
```

| Operación | Método | Eventos almacenados |
|---|---|---|
| Listar pagos | GET | `START_GET`, `END_GET` |
| Crear encabezado con detalles | POST | `START_POST`, `END_POST` |
| Crear pago | POST | `START_POST`, `END_POST` |
| Actualizar pago | PATCH | `START_PATCH`, `END_PATCH` |
| Consultar encabezado con detalles | GET | `START_GET`, `END_GET` |

Las rutas de FinanzasPayment no utilizan `logBeforeMethod()`. Por lo tanto, no almacenan `INIT_METHOD` ni identifican un proceso funcional como `list`, `create` o `update`.

## 8. Almacenamiento Google Cloud

Aplicativo almacenado:

```text
StorageGCP
```

| Operación | Método | Proceso | Eventos automáticos |
|---|---|---|---|
| Cargar archivos | POST | `uploadMultiple` | `START_POST`, `INIT_METHOD`, `END_POST` |
| Descargar archivo | GET | `downloadFile` | `START_GET`, `INIT_METHOD`, `END_GET` |

Eventos específicos:

```text
Archivos enviados exitosamente a google cloud
ERROR : No fue posible registrar los archivos en google cloud
GCS upload FAILED → bucket={bucketName}
GCS download FAILED → bucket={bucketName} object={objectName} cause={error}
GCS upload FAILED → bucket. NameFiles: {nameFiles}
GCS upload SUCCESS → bucket. NameFiles: {nameFiles}
```

## 9. Rebates

Se observaron directamente en la base de datos eventos con la siguiente estructura:

```text
Módulo: API_FINANZAS
Aplicativo: UNKNOWN
Acción: GET
Proceso: rebates?source=...&from=...&to=...&limit=...&page=...
Usuario: system
```

Situación identificada:

- Existen registros reales de consultas de Rebates en la Bitácora de Actividades.
- El aplicativo se almacena como `UNKNOWN`.
- En la versión actual del repositorio, las rutas de Rebates no montan `activityLogger()`.
- El controlador de Rebates tampoco llama directamente a `logActivity()`.
- La instrumentación observada puede corresponder a otra versión desplegada, un middleware anterior o una capa no presente en la versión local revisada.

En el código actual:

```text
Rebates exitoso → no existe instrumentación propia localizada
Rebates con error → ERROR_HANDLER
```

## 10. Generación de Transaction ID

Evento exitoso:

```text
Mensaje: Transaction ID generated successfully
Tipo: INFO
Paso: CREATE_TRANSACTION_ID
IdMensaje: TRANSACTION_ID_CREATED
```

Evento de error:

```text
Mensaje: Error creating transaction ID
Tipo: ERROR
Paso: CREATE_TRANSACTION_ID
```

Posibles identificadores de error:

```text
INVALID_TRANSACTION_ORIGIN
TRANSACTION_ID_NOT_FOUND
CREATE_TRANSACTION_ID_ERROR
UNIQUE_VIOLATION
```

Además del registro en `activity_logs`, los errores de Transaction ID se almacenan mediante:

```ts
repo.createErrorLog(...)
```

Por lo tanto, esta funcionalidad utiliza dos mecanismos de persistencia para los errores.

## 11. Errores globales y validaciones

Eventos generales encontrados:

```text
ValidationError
ERROR_HANDLER
TERMINA REQUEST CON ERROR
GlobalErrorHandler
```

El evento `ERROR_HANDLER` guarda:

```text
Tipo: ERROR
Código: status HTTP resuelto
Paso: método HTTP + URL
Log: stack o error serializado
```

Clasificación utilizada:

| Error | Código HTTP | IdMensaje |
|---|---:|---|
| Validación Zod | 400 | `ValidationError` |
| Consulta TypeORM | 400 | `DatabaseQueryFailed` |
| JSON inválido | 400 | `InvalidJson` |
| Servicio no disponible | 503 | `ECONNREFUSED` o `ETIMEDOUT` |
| Error no identificado | 500 | `UnhandledError` |

Si un controlador ya envió la respuesta, `errorHandler` no registra `ERROR_HANDLER` debido a la validación:

```ts
if (res.headersSent) {
    return next(err);
}
```

## 12. Comunicación HTTP/Axios

Eventos encontrados:

```text
ERROR: EN AXIOS GET. URL:{url}
ERROR: EN AXIOS POST. URL:{url}
```

Estos eventos heredan el aplicativo y el ID de transacción del contexto activo. Si se ejecutan fuera de un contexto creado por `activityLogger()`, pueden almacenarse con aplicativo `UNKNOWN`.

## 13. Tipos de evento utilizados

| Tipo | Estado actual |
|---|---|
| `INFO` | Utilizado |
| `ERROR` | Utilizado |
| `ALERTA` | Soportado, pero sin usos localizados |

## 14. Resumen ejecutivo

Actualmente Finanzas API monitorea:

- Inicio y finalización de solicitudes HTTP.
- Inicio de métodos funcionales instrumentados.
- Operaciones de Carta Porte.
- Órdenes de compra y recepciones.
- Guías de embarque.
- Pagos de Finanzas.
- Carga y descarga de archivos en Google Cloud.
- Generación de identificadores de transacción.
- Validaciones y errores globales.
- Errores de comunicación HTTP/Axios.
- Consultas de Rebates observadas directamente en la base de datos.

La implementación actual no está completamente centralizada:

1. `logger.ts` realiza inserts directos en `core_audit.activity_logs`.
2. Algunos controladores y servicios llaman directamente a `logActivity()`.
3. Existe adicionalmente el endpoint `POST /audit-logs` con controller, service y repository propios.
4. Algunos errores utilizan tablas adicionales, como Transaction ID.

Esta distribución genera inconsistencias como:

- Aplicativos almacenados como `UNKNOWN`.
- Usuario fijo `system`.
- Eventos técnicos sin nombre funcional de proceso.
- Finalizaciones registradas como `INFO` aunque la respuesta termine con error.
- Posibles duplicados.
- Diferencias entre el código local y el comportamiento observado en ambientes desplegados.

## 15. Recomendación arquitectónica

La responsabilidad debería distribuirse de la siguiente manera:

```text
Frontend Finanzas
    → solicita la operación

Finanzas API
    → ejecuta la operación
    → genera el evento de auditoría con contexto confiable

Utilerías API / Servicio central de auditoría
    → valida y normaliza el evento
    → realiza el único INSERT autorizado

core_audit.activity_logs
    → persistencia centralizada
```

Finanzas debe producir el evento, pero Utilerías debería ser la única propietaria de la validación, repositorio, tabla e inserción de auditoría.

