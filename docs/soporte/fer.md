# Fernando (Fer) - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-03-02 | internalStatus null + catalog_detail vacio en DEV (estatus EFA)

**Contexto**: Fer consume la API de catalogos para obtener estatus de facturas. Necesita el ID numerico (`internalStatus`) para mandarlo al endpoint `POST /invoices/search` en el campo `status`, pero el catalogo devuelve `internalStatus: null`. Ademas, en DEV el endpoint regresa `[]` (array vacio).
**Problema dual**:
1. En local: `internal_status` no estaba poblado en `catalog_detail` para las entradas EFA → se corrigio con UPDATE
2. En DEV: el header `CatEstatusComplemento` existe (id=19) pero NO tiene registros en `catalog_detail` → los 14 estatus EFA nunca se insertaron
**Causa raiz**: Los scripts de insert exportados con DBeaver tenian `header_id=15` hardcodeado (id en local). En DEV el autoincremento asigno `id=19` al header, entonces los inserts de detail apuntaban al header equivocado.
**Solucion**: Se creo script portable `seed_efa_completo.sql` que usa subqueries en lugar de IDs hardcodeados. Inserta dictionary_lang (42 traducciones) + catalog_detail (14 entradas) con `internal_status = sort_order - 1`. Mapeo: EFA001=0, EFA002=1, ..., EFA014=13.
**Archivos**:
- `docs/db/catalogs/fix_efa_internal_status.sql` — fix para local (UPDATE internal_status)
- `docs/db/catalogs/seed_efa_completo.sql` — seed portable para DEV (inserts completos)
**Diagnostico adicional**: Si el endpoint regresa `[]`, verificar: `status=1` (activos), `valid_from`/`valid_to` (vigencia). Query de repositorio filtra por las 3 condiciones.
**Jira**: -
**Estado**: Resuelto en local. Fer debe ejecutar `seed_efa_completo.sql` en DEV via DBeaver.

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
