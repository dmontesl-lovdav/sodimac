# Fernando (Fer) - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-02-27 | internalStatus null en API de catalogos (estatus EFA)

**Contexto**: Fer consume la API de catalogos para obtener estatus de facturas. Necesita el ID numerico para mandarlo al endpoint `POST /invoices/search` en el campo `status`, pero el catalogo devuelve `internalStatus: null`.
**Problema**: La columna `internal_status` en `shared_catalogs.catalog_detail` no estaba poblada para las entradas EFA (header_id=15, catalogo `CatEstatusComplemento`). El codigo (Entity -> Mapper -> DTO) estaba correcto, el problema era de datos.
**Causa**: Al poblar el catalogo de estatus EFA nunca se lleno la columna `internal_status`. Otros catalogos como `CatEstatusCartaPorteFBC` si la tenian poblada.
**Solucion**: Se poblo `internal_status = sort_order - 1` para las 14 entradas EFA. Mapeo: EFA001=0, EFA002=1, ..., EFA014=13. Fer debe usar el campo `internalStatus` de la respuesta del catalogo como valor para `status` en fiscal.
**Archivos**: `docs/db/catalogs/fix_efa_internal_status.sql`
**Jira**: -
**Estado**: Resuelto en local. Pendiente aplicar fix en DEV cuando levanten la VM.

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
