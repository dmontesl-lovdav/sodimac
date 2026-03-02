# Fernando (Fer) - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-03-02 | internalStatus null + endpoint regresa [] (estatus EFA)

**Contexto**: Fer consume la API de catalogos para obtener estatus de facturas. El catalogo devuelve `internalStatus: null` y en DEV el endpoint regresa `[]` (array vacio).
**Causa raiz**: Fer consultaba `CatEstatusComplemento` (id=19, prefix=ECM, status=-1) que esta vacio y sin configurar. Los estatus EFA en realidad pertenecen a `CatEstatusFactura` (id=15, prefix=EFA, status=1).
**Problema adicional**: En local los datos estaban con IDs diferentes porque los exports de DBeaver no incluian PKs. Se re-exporto con "Include generated columns" activado para tener datos identicos.
**Solucion**: Fer debe cambiar su consulta de `CatEstatusComplemento` a `CatEstatusFactura`. El `internalStatus` ya viene poblado (1-14) y es el valor que debe mandar en `status` del `POST /invoices/search`.
**Mapeo**: EFA001=1 (Rechazo Comercial), EFA002=2 (Pendiente Addenda), ..., EFA014=14 (Pago Manual)
**Archivos**: `docs/db/catalogs/v2/` — exports con PKs desde Sodimac
**Jira**: -
**Estado**: Resuelto. Fer debe usar `CatEstatusFactura` en lugar de `CatEstatusComplemento`.

---

## 2026-02-27 | Generar datos de prueba (facturas con addendas y relaciones)

**Contexto**: Fer necesita mas datos para probar listados, ordenamiento y busquedas en el front
**Problema**: Solo habia un registro valido en las tablas
**Solucion**: Se poblaron las tablas con inserts adicionales (ya entregado)
**Jira**: -
**Estado**: Resuelto

---

## 2026-02-27 | Error al cambiar estatus de factura - endpoint incorrecto

**Contexto**: Fer usaba `PUT /invoices` con payload `{uuid, estatus}` para cancelar y recibia error "UUID no encontrado"
**Problema**: Estaba usando el endpoint equivocado. `PUT /invoices` es para actualizar addenda, no para cambiar estatus.
**Solucion**: El endpoint correcto es `PUT /invoices/{uuid}/status` con el UUID en la URL
**Ejemplo**: `PUT /invoices/a12b2040-d8f8-4fce-ab9d-37a636f8e59e/status`
**Payload**: `{"numeroProveedor": 12345, "estatusOrigen": 2, "estatusDestino": 3, "idUsuarioActualizacion": 1}`
**Nota**: Este endpoint depende del servicio de catalogos (status-train) que no estaba levantado en DEV
**Jira**: STM-1166
**Estado**: Resuelto (informado a Fer)

---

## 2026-02-27 | Como cancelar una factura

**Contexto**: Fer pregunto si existe un endpoint para cancelar facturas
**Problema**: No hay endpoint directo de cancelacion
**Solucion**: Se usa el tren de estatus (status-train). No se puede cancelar directamente, solo si el estatus actual lo permite (ej: pago parcial). Referencia: STM-1166
**Jira**: STM-1166
**Estado**: Resuelto (informado a Fer)

---

## 2026-02-27 | Bitacora de actividades - cancelacion

**Contexto**: Fer tiene un ticket para registrar en bitacora quien y cuando cancelo una factura
**Problema**: Pregunto si fiscal ya maneja bitacora
**Solucion**: Si existe tabla de bitacora interna (`invoice_status_history`), se actualiza automaticamente al cambiar estatus. No hay API que la liste, solo consulta SQL.
**Jira**: -
**Estado**: Resuelto (informado a Fer, bitacora es interna)
