# Fernando (Fer) - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-05-28 | BUS048 al cambiar estatus de factura recién subida

**Contexto**: Fer subió XML Truper (`0118413484.xml`) vía POST `/ppsomx/fiscal/invoices/register` → respuesta `RES005` "Pendiente de Addenda". Luego PUT a estatus 2 → recibió `BUS048` "La addenda del documento no se encuentra registrada en el sistema".
**Pregunta**: ¿Es normal este error?
**Respuesta**: Sí, comportamiento esperado. El XML de Truper no trae nodo `<cfdi:Addenda>`. Sodimac acepta 2 escenarios:
1. **XML CON addenda Sodimac** (`Addenda_Sodimac` local o `Addenda_Sodimac_CartaPorte` foráneo) → respuesta `RES004`, estatus normal.
2. **XML SIN addenda** → respuesta `RES005`, estatus 1 (Pendiente Addenda). Se guarda OK pero el PUT a estatus 2 falla con `BUS048` hasta completar la addenda manual desde finanzas-api (vinculación OC + recepción).

**Diagnóstico técnico**:
- `AddendaValidationServiceImpl.java:60-66` → sin addenda = `return false`, NO error
- `InvoiceServiceImpl.java:934-967` (`validateSupplierOwnership`) → exige addenda registrada para validar propiedad del proveedor en PUT
- Comentario en código: "Si el documento NO tiene addenda, es un ERROR (no debería ocurrir)" — regla confirmada por Ivan
- Vinculación manual: `purchaseOrder.service.ts:119-131` crea `AddendumManual` cuando se asocia recepción a factura con `status=2` + `uuid`

**Pendiente confirmar con Ivan**: si addendum_manual de finanzas alimenta tenant_fiscal.addendum (que es de donde fiscal-api lee).
**Documentación generada**: [docs/wiki/procesos/09-addenda-sodimac.md](../wiki/procesos/09-addenda-sodimac.md)
**Estado**: Resuelto (información entregada, doc de wiki creado)

---

## 2026-03-25 | Relacionar descuento comercial con nota de crédito

**Contexto**: Fer tiene un ticket para pantalla de relacionar descuento comercial con NC. Ya sabe que el endpoint de subir NC es `/api/fiscal/xml/process/file` pero pregunta cómo relacionar el descuento con la NC.
**Respuesta**: El endpoint no está en fiscal-api sino en finanzas-api: `POST /rebates/relate` (multipart: uuid, numeroDocumento, referenciaDocumento, numeroProveedor, usuario, xmlFile). Internamente valida la NC contra fiscal-api y crea registros StampedRebate y Rebate.
**Referencia**: Se le sugirió acercarse con Josué para más contexto del flujo completo.
**Estado**: Resuelto (información entregada)

---

## 2026-03-11 | Dispersión de pagos en complementos - IMPLEMENTADA

**Contexto**: Fer logro registrar un complemento de pago y pregunta si hay un endpoint para relacionarlo con la factura o si el registro ya lo hace.
**Pregunta**: Hay un endpoint donde pueda relacionar el complemento con la factura, o el mismo endpoint de registro ya lo hace?
**Respuesta**: No hay endpoint separado. El registro lo hace automaticamente. Se implemento la dispersion completa:
- `parsePagos()` en `PaymentXmlParserServiceImpl.java` ahora parsea el nodo `<Pagos>` con cada `<Pago>` y sus `<DoctoRelacionado>`
- `savePaymentsAndRelatedDocuments()` en `PaymentRegistrationServiceImpl.java` persiste en tablas `payment` y `related_documents`
- Flujo: `payments` (cabecera) → `payment` (pago individual) → `related_documents` (factura pagada)
**Verificacion**: Probado con XML de complemento. Datos dispersados correctamente: monto, forma de pago, cuentas bancarias, saldos, parcialidad
**Endpoint para consultar**: `GET /related-documents/by-payment/{paymentsUuid}?page=0`
**Jira**: -
**Estado**: Resuelto — dispersion implementada y probada, pendiente deploy

---

## 2026-03-09 | Facturas de un complemento de pago + XML valido para registro

**Contexto**: Fer pregunta como ver las facturas (array) de un complemento de pago y pide un XML valido para probar el happy path de registro.
**Pregunta 1**: El endpoint GET /fiscal/complementos-pago/buscar devuelve `relatedDocumentsCount` pero no el array de facturas
**Respuesta 1**: Existe endpoint aparte: `GET /related-documents/by-payment/{paymentsUuid}?page=0` (sin prefijo /api). Devuelve Page con documentos relacionados (documentUuid, series, folio, amountPaid, previousBalance, remainingBalance, installmentNumber, currency).
**Pregunta 2**: XML valido para complemento de pago
**Respuesta 2**: Se genero XML de test (`docs/test/complemento-pago-test.xml`). Puntos clave: TipoDeComprobante="P", SubTotal=0, Total=0, Moneda="XXX", UsoCFDI="CP01", Version CFDI 4.0 + Pagos 2.0. Endpoint de registro: `POST /fiscal/complementos-pago/registrar` (multipart: xmlFile, tipoAddenda=5, idProveedor, tipoProveedor, idUsuario). RFC receptor debe estar autorizado en catalogo. UUID en el XML debe usar solo caracteres hex validos (0-9, a-f).
**Pruebas validadas**: Todos los endpoints probados en localhost:8082 con datos reales. Busqueda, documentos relacionados, registro y re-busqueda — todos OK. Curls documentados en `docs/conversacion/fer.txt`.
**Jira**: -
**Estado**: Resuelto (informado a Fer, pruebas validadas)

---

## 2026-03-05 | Facturas con NCs relacionadas + validación de estatus en search

**Contexto**: Fer necesita ver facturas con NCs relacionadas y sigue teniendo problemas con estatus.
**Pregunta 1**: Como ver facturas con sus NCs relacionadas?
**Respuesta 1**: No se necesita endpoint adicional. `POST /invoices/search` con `tipoDocumento=I` ya incluye `notasCreditoRelacionadas[]` en cada factura. En DEV, factura Serie A / Folio 001 (emisor SOD970101ABC) tiene una NC relacionada.
**Pregunta 2**: El estatus sigue dando problemas, a veces mando un valor y regresa vacio
**Causa raiz**: El API no validaba que el estatus existiera — simplemente filtraba y regresaba `[]`. No habia forma de saber si el estatus era invalido o si no habia datos.
**Solucion 2**: Se agrego validacion de estatus contra el catalogo local (enum InvoiceStatus/CreditNoteStatus). Ahora si mandas un estatus que no existe (ej: 99) o uno que no corresponde al tipo de documento (ej: 13 con tipoDocumento=E), regresa error BUS049 con mensaje claro.
**Cambios en codigo**:
- `InvoiceServiceImpl.java`: nuevo metodo `validateStatusExists()` que se ejecuta antes del search
- `StatusTrainApiServiceImpl.java`: cuando `status-train.api.enabled=false`, ahora valida transiciones con enums locales en vez de permitir todo
**Jira**: -
**Estado**: Resuelto (cambios en fiscal-api, pendiente deploy)

---

## 2026-03-05 | Campo "status" incorrecto en search + endpoint registro complementos

**Contexto**: Fer reporta que `POST /invoices/search` no regresa resultados al pasar estatus, y pregunta si el endpoint de registro de facturas sirve para complementos.
**Pregunta 1**: Paso el estatus y no regresa resultados
**Causa raiz**: Dos errores en el payload: (a) el campo se llama `estatus` no `status`, (b) lo manda como String "12" en vez de Integer 12. Tambien tenia `rfcEmisor:" "` con espacio en blanco.
**Solucion 1**: Corregir payload: `"estatus":12` (nombre correcto + tipo Integer). Quitar campos vacios innecesarios.
**Pregunta 2**: El mismo endpoint de registro de facturas sirve para complementos?
**Respuesta 2**: No, son endpoints distintos. Facturas/NC: `POST /invoices/register`. Complementos de pago: `POST /fiscal/complementos-pago/registrar`. Ambos reciben multipart XML pero con validaciones diferentes (TipoDeComprobante I/E vs P).
**Jira**: -
**Estado**: Resuelto (informado a Fer)

---

## 2026-03-05 | Fechas obligatorias con UUID, UUID fiscal vs interno, subtotal en complementos

**Contexto**: Fer hizo 3 preguntas sobre el endpoint `POST /invoices/search` y complementos de pago.
**Pregunta 1**: Si mando UUID, las fechas dejan de ser obligatorias?
**Respuesta 1**: No. `fechaInicioRecepcion` y `fechaFinalRecepcion` tienen `@NotNull` en `InvoiceSearchRequest.java`. Son siempre obligatorias.
**Pregunta 2**: Que UUID mando para ver detalle de facturas relacionadas?
**Respuesta 2**: No necesita endpoint aparte. `POST /invoices/search` con `tipoDocumento=E` (Notas de Credito) ya incluye los datos de la factura relacionada: `relatedInvoiceUuid`, `relatedInvoiceSeries`, `relatedInvoiceFolio`, `relatedInvoiceSubtotal`, `relatedInvoiceTotal`. El campo `uuid` del request mapea a `fiscal_uuid` (TimbreFiscalDigital del SAT), no al UUID interno.
**Pregunta 3**: En complementos de pago falta el subtotal
**Respuesta 3**: Correcto, no existe campo subtotal en BD ni en DTO para complementos de pago. Por especificacion SAT CFDI 4.0, el nodo Comprobante de un complemento de pago tiene SubTotal=0 y Total=0. El `totalAmount` que devolvemos es la suma de los pagos individuales.
**Nota adicional**: Los UUIDs no son duplicados - `invoiceUuid` (PK interno) y `fiscalUuid` (SAT) son campos distintos. Si se ven iguales en el grid, revisar binding en el front.
**Jira**: -
**Estado**: Resuelto (informado a Fer)

---

## 2026-03-02 | internalStatus null + endpoint regresa [] (estatus EFA)

**Contexto**: Fer consume la API de catalogos para obtener estatus de facturas. El catalogo devuelve `internalStatus: null` y en DEV el endpoint regresa `[]` (array vacio).
**Causa raiz**: Fer consultaba `CatEstatusComplemento` (id=19, prefix=ECM, status=-1) que esta vacio y sin configurar. Los estatus EFA en realidad pertenecen a `CatEstatusFactura` (id=15, prefix=EFA, status=1).
**Problema adicional**: En local los datos estaban con IDs diferentes porque los exports de DBeaver no incluian PKs. Se re-exporto con "Include generated columns" activado para tener datos identicos.
**Solucion**: Fer debe cambiar su consulta de `CatEstatusComplemento` a `CatEstatusFactura`. El `internalStatus` ya viene poblado (1-14) y es el valor que debe mandar en `status` del `POST /invoices/search`.
**Mapeo**: EFA001=1 (Rechazo Comercial), EFA002=2 (Pendiente Addenda), ..., EFA014=14 (Pago Manual)
**Archivos**: `docs/db/catalogs/` — exports con PKs desde Sodimac
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
