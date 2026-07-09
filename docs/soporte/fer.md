# Fernando (Fer) - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-07-09 | Tipo NC en search + máscara $ en msg tolerancia (matriz v11)

**Pregunta Fer**: "al consultar las NC, ¿puedo saber desde el endpoint de search qué tipo de NC es?"
(para columna "Tipo NC" = Ajuste por Recepción / Descuento Comercial + CSV). Y 2 issues de la
matriz reasignados a David (mensaje del back): usar máscara `$000,000.00` en los montos de los
mensajes de tolerancia (diferencia y confirmación).

**Respuesta / fix (back, fiscal-api, commit `c7a07e0`):**
- **Search NC**: el response ya traía `tipoNotaCredito` (código 1/2). Se agregó
  **`tipoNotaCreditoDescripcion`** (catálogo `CatTipoNotaCredito`, ES). Fer arma la columna + CSV
  con ese campo. No cambia nada más del contrato.
- **Máscara `$#,##0.00`** (helper `maskMoney`, es-MX): WRN7030/7031 ahora mandan los montos
  formateados (`$41,098.18`); WRN7034 pasó de texto estático a mostrar montos (neto factura-NCs y
  monto de la recepción) con máscara. WRN7030/7031 **no** cambian de texto (placeholders iguales);
  WRN7034 sí → seed `core_utils.cat_message` en UAT (`migration/QA-mascara-tolerancia-WRN7034.sql`).

Validado local: search NC (tipo 1 y 2) y WRN7034 con `$6,340.00` / `$11,214.00`. Ver [[project_reception_number_no_unico]] (mismo día, otro fix).

---

## 2026-07-09 | ERR003 al publicar NC — resuelto en back, sin cambio de front

**Reporte QA**: publicar NC daba **ERR003 "Query did not return a unique result"**.

**Causa (back)**: `reception_number` duplicado en UAT; el recálculo de tolerancia resolvía la
recepción solo por número → 2 filas → error.

**Fix**: 100% back (fiscal-api) — resuelve la recepción por número + OC. **No cambia el contrato**:
mismo endpoint, misma request/response. **Fer no ajusta nada.** Ya en develop/uat (`34b0b2a`).

---

## 2026-07-03 | PUT /invoices 500: idUsuarioActualizacion debe ser numérico (no UUID)

**Reporte**: `PUT https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices` (cancelar NC) daba **500**
con body `{"uuid":"cf5a9aa1-...","numeroProveedor":250117,"estatus":0,"idUsuarioActualizacion":"9f7affd6-fc0f-4f76-bcaf-65b1af36a47d"}`.

**Causa**: `idUsuarioActualizacion` en el back es **Long**; el front manda un **UUID**. `getUserIdFromStore()`
(fiscal.spa `src/utils/getUserIdFromStore.ts`) regresa el **`sub` del JWT (UUID)**. Jackson no convierte
UUID→Long → `InvalidFormatException` → el ControllerAdvisor lo mapea a 500. (El fallback `?? "1"` sí
funciona porque "1" parsea a Long.)

**Decisión (David)**: **NO adaptar el back al UUID.** El `idUsuarioActualizacion` se guarda en
`updated_by`/`created_by` (bigint) + bitácora, que son **numéricos**. El front debe mandar el **id
numérico** del usuario, no el `sub`. Ajustar `getUserIdFromStore()` para priorizar un claim numérico
(`idUsuario`/`userId`) en vez de `sub`. **Mismo caso** en `InvoiceClient.ts:34/:42` y
`complement/AddComplement.tsx:75` (todos mandan `getUserIdFromStore()`).

**Origen back**: `InvoiceUpdateRequest.idUsuarioActualizacion` (Long) → `invoice.setUpdatedBy(...)` +
`.intValue()`. Confirmado reproduciendo local (copia UAT): 500 con UUID, OK con numérico.

**Pendiente back (no bloqueante)**: agregar handler de `HttpMessageNotReadableException` en
`ControllerAdvisor` para que un body con tipo malo dé **400** legible en vez de 500.

---

## 2026-06-25 | Estructura de respuesta /register (éxito, PDF fallido, NC) + manejo en front

Fer pidió: (1) confirmar que el aviso de NC (monto mayor) siempre sale igual, (2) si `message`
siempre viene, (3) si el punto F96 es UUID mal referenciado, y (4) ejemplos de response para tipar
el front.

**Reglas confirmadas en código** (`InvoiceRegistrationResponse` + `GlobalExceptionHandler`):
- **Monto mayor a recepción fuera de tolerancia (±40)** → `HTTP 200`, `success:true`, factura
  **Recibido Parcial**, aviso de NC en **`warnings[]`** (WRN7030), **NO** en `message`. Dentro de
  tolerancia → `warnings:[]`. Menor fuera de tolerancia → Rechazo Comercial (WRN7031).
- **`message` siempre presente** en éxito y error, pero la **forma del objeto cambia**: éxito trae
  `success`/`warnings`/`invoiceUuid`; error es `{timestamp,status,error,code,message,path}` (sin
  `success` ni `warnings`). **Ramificar por HTTP status** (200 vs 400/500), no solo por `success`.
- **F96 (pantalla NC): sí, UUID mal referenciado.** Hoy pone el UUID de la propia NC en el campo
  "uuid relacionado"; debe poner el de la factura relacionada = `uuidRelacionado` que ya expone
  `POST /fiscal/xml/process/file` (F97). Ver
  [docs/analisis/QA-IVAN-2026-06-23b-nc-serie-folio-uuid-relacionado.md](../analisis/QA-IVAN-2026-06-23b-nc-serie-folio-uuid-relacionado.md).

**Ejemplos de response (todos HTTP 200, success:true; diferencia en `warnings[]`):**

1. Factura OK → `warnings: []`.
2. Factura OK pero PDF no subió al bucket → `warnings: ["La factura se registró correctamente, pero
   el PDF no se pudo almacenar..."]` (WRN7033).
3. Factura mayor a recepción → Recibido Parcial → `warnings: ["La factura se registró como Recibido
   Parcial: ... Se requiere una nota de crédito..."]` (WRN7030).

Campos del éxito (`InvoiceRegistrationResponse`): `code` (RES004), `message`, `success`,
`invoiceUuid` (usar para `GET /invoices/{invoiceUuid}/pdf`), `fiscalUuid`, `series`, `folio`,
`documentType` (I=factura, E=NC), `issuerRfc`, `receiverRfc`, `total`, `issueDate`, `hasAddenda`,
`pendingAddenda`, `warnings[]`, `processedAt`.

**Respuesta de error** (`GlobalExceptionHandler`). Campos garantizados en todo error: `timestamp,
status, error, code, message, path`. Variantes:
- **Negocio** (`FiscalException`) → `HTTP 400`, `error:"Business Error"`, `code` = WRN/BUS (WRN7012
  folio, WRN7013 duplicado, WRN7032 addenda manual, BUS008 receptor, etc.).
- **Validación `@Valid`** → `HTTP 400`, `error:"Validation Error"`, `code:"ERR035"`, + mapa `errors{}`.
- **Argumento inválido** (`IllegalArgumentException`) → `HTTP 400`, `error:"Invalid Argument"`,
  `code:"ERR003"`.
- **Interno no controlado** → `HTTP 500`, `error:"Internal Server Error"`, `code:"ERR036"`.

```json
// Negocio (400)
{ "timestamp":"2026-06-25T10:34:12.184", "status":400, "error":"Business Error",
  "code":"WRN7012", "message":"La factura requiere un folio...", "path":"/invoices/register" }
// Validación (400) — agrega errors{}
{ "timestamp":"...", "status":400, "error":"Validation Error", "code":"ERR035",
  "message":"Error de validación...", "errors":{"xml":"El archivo XML es obligatorio"}, "path":"..." }
// Interno (500)
{ "timestamp":"...", "status":500, "error":"Internal Server Error", "code":"ERR036",
  "message":"Ocurrió un error interno...", "path":"/invoices/register" }
```

Type TS error: `{ timestamp, status, error, code, message, path, errors?: Record<string,string> }`
(`errors` solo en Validation Error). Fuente:
[GlobalExceptionHandler.java](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/exception/GlobalExceptionHandler.java).

**Resumen para el front**: 200 → tipar como `RegisterResponse` y **revisar `warnings[]`** (vacío = OK;
WRN7030 = pedir NC; WRN7033 = avisar PDF no disponible). 400/500 → tipar como `ErrorResponse` y
mostrar `message`.

---

## 2026-06-24 | Descarga de PDF baja "vacío" + warnings[] del /register

**Reporte**: `GET /ppsomx/fiscal/invoices/{uuid}/pdf` baja el PDF vacío. URL reportada usaba
`61442df7-...` que es el **fiscalUuid**.

**Diagnóstico (2 cosas)**:
1. **UUID invertido (front)**: el endpoint hace `findById` = PK **`invoiceUuid`** (interno), no el
   `fiscalUuid` (folio fiscal). Con el fiscalUuid no encuentra → ERR001 → el front guarda el JSON de
   error como `.pdf` (de ahí el "vacío"). Fix front: usar `response.invoiceUuid` del /register.
2. **Esa factura no tenía PDF en bucket**: `pdf_gcs_object` vacío (18 de 23 facturas igual). El front
   casi nunca adjunta el `pdfFile` al registrar, o el upload falló silencioso. La feature **sí
   funciona** (5 facturas con objeto válido en `bk-fiscal-uat/fiscal-somx/{invoiceUuid}.pdf`).

**Cambio back (HECHO, desplegado UAT)** — `375f545`: si el PDF se manda pero el bucket falla, el
/register ahora regresa éxito **con `WRN7033` en `warnings[]`** (antes era silencioso). La factura
igual se registra (no crítico). **Fer debe leer `warnings[]` y mostrarlo** tras registrar.

**Mensaje enviado a Fer**: (1) usar `invoiceUuid` no `fiscalUuid` en la URL de descarga + revisar
HTTP code; (2) mostrar `response.warnings[]` (incluye WRN7033 si falla el PDF).

**Detalle GCS**: subida en PASO 9.5 de `InvoiceServiceImpl` → `GcsStorageServiceImpl.uploadPdf` →
guarda path en `invoice.pdf_gcs_object`. Descarga: `getPdfByInvoiceUuid` → `downloadPdf`. Config:
`fiscal.storage.gcs.bucket` (`GCS_BUCKET`) / `fiscal.storage.gcs.prefix` (`GCS_PREFIX_SOMX`).

---

## 2026-06-19 | Lista de 6 issues (search + complementos + NC descuento comercial)

Fer reportó 6 puntos sobre `/invoices/search`, `complementos-pago/buscar` y NC. Detalle y análisis en [docs/analisis/QA-FER-2026-06-issues-search.md](../analisis/QA-FER-2026-06-issues-search.md).

Estado (todos desplegados en UAT salvo #6):
1. **`noRecepcion` mostraba GUID + `tipoProveedor` null** → ✅ noRecepcion ahora = `reception_number` de finanzas (numérico); `tipoProveedor` (id 1-4) se puebla al registrar leyendo directo de `shared_catalogs.supplier`. Commits `5f5f4b8`, `961513d`.
2. **Filtro por fecha** → cerrado: filtra por fecha de **registro** (`created_at`), decisión Ivan. Su reporte fue pre-reunión.
3. **`tipoProveedor` null en NC** → ✅ mismo fix que #1.
4. **Filtro por `tipoProveedor` en search** → ✅ `5fe715b`. Además se devuelve `tipoProveedor` (id) + `tipoProveedorDescripcion` (texto) en campos separados.
5. **Filtro por `tipoProveedor` en complementos-pago/buscar** → ✅ mismo patrón + id/desc en response.
6. **NC de descuento comercial** (PDF en NC, UUID factura relacionada, tipo NC en addenda) → 🟡 pendiente, feature grande. Catálogo `CatTipoNotaCredito` (1 Ajuste Recepción, 2 Descuento Comercial) ya creado (seed 18 util-api).

**Backfill UAT:** se rellenaron las addendas viejas (supplier_type + reception_number numérico) con script manual. Validado: PARKMEX 252523 → noRecepcion 846919, tipoProveedor 1 "Mercancía". Addendas sin numeroProveedor quedan en null.

---

## 2026-06-18 | Búsqueda NC/facturas no filtra por fecha de recepción

**Contexto**: Fer reportó que en POST `/invoices/search` (UAT), al mandar un rango exacto de fechas de recepción (`fechaInicioRecepcion=2026-05-26`, `fechaFinalRecepcion=2026-05-30`, `tipoDocumento=I`) NO salían registros, pero con un rango amplio sí aparecían 3 facturas con fecha de recepción en ese rango.

**Causa raíz**: el filtro `fechaInicioRecepcion`/`fechaFinalRecepcion` se aplicaba sobre `invoice.created_at` (fecha de **registro** en el sistema) en `InvoiceSpecification`, no sobre la **fecha real de recepción** (`tenant_finance.reception.reception_date`). Ambas difieren hasta semanas. Datos reales (dump UAT): factura `A/7957` registrada 2026-06-15 pero recepción 2026-05-04 (**42 días** de diferencia); `FVS/202500870-871` registradas 06-15/06-18, recepción 06-04. Por eso un rango amplio las pescaba por casualidad y el rango exacto fallaba.

**Fix 2026-06-18** (rama `dmontes`):
- `ReceptionEntity`: se mapeó la columna `reception_date`.
- `ReceptionRepository.findReceptionIdsByDateRange` + `AddendumRepository.findInvoiceUuidsByReceptionNumbers`: resuelven los invoice_uuid cuya recepción cae en el rango (vínculo `addendum.reception_number = reception.reception_id`).
- `InvoiceServiceImpl.resolveReceptionInvoiceUuids`: precomputa esa lista y la pasa a la Specification (evita join cross-tipo varchar=uuid que rompía en Postgres con `operator does not exist: character varying = uuid`).
- `InvoiceSpecification`: nuevo overload 3-arg; filtra por `invoice_uuid IN (lista)`. Lista vacía = 0 resultados; null = sin filtro.
- **Probado local sobre dump UAT**: rango por recepción 06-01..06-10 → FVS x2; 05-01..05-10 → A/7957; **06-04 exacto → FVS x2** (el caso que fallaba). Todas con created_at fuera del rango → el código viejo las habría perdido.

**Display fecha recepción (resuelto 2026-06-18)**: se agregó el campo `fechaRecepcion` (LocalDate) al response de `/invoices/search`, poblado desde `tenant_finance.reception.reception_date` vía `addendum.reception_number`. Cambio additive. Probado local: FVS/870-871 → 2026-06-04, A/7957 → 2026-05-04 (coincide con el filtro).

**VERIFICADO EN UAT 2026-06-18**: deploy vivo (response trae `fechaRecepcion`). Búsqueda por recepción `2026-06-04` exacto → 4 facturas FVS (`totalElements:4`). Rango `26-30 may` vacío = correcto (no hay recepciones reales ahí; lo que Fer veía era createdAt). Mensaje formal enviado a Fer explicando filtro real vs registro + cómo validar.

### REVERTIDO 2026-06-19 — la búsqueda va por fecha de REGISTRO, no por recepción

Negocio aclaró: en `/invoices/search` las fechas deben comparar contra la **fecha de registro de la factura** (`invoice.created_at`). El campo de fecha de recepción de finanzas (orden de compra) **NO juega** en este endpoint.

Se revirtió el cambio del 2026-06-18:
- `InvoiceSpecification`: filtro vuelve a `created_at` (atStartOfDay / atTime 23:59:59).
- Se eliminó el plumbing de recepción: `resolveReceptionInvoiceUuids`, `resolveReceptionDate`, overload 3-arg de `buildSpecification`, métodos `ReceptionRepository.findReceptionIdsByDateRange` y `AddendumRepository.findInvoiceUuidsByReceptionNumbers`, campo `ReceptionEntity.receptionDate`, inyección `receptionRepository` en el service.
- Se quitó el campo `fechaRecepcion` del response `InvoiceSearchResponse`.
- Probado local: `created_at` 2026-06-15 → 2 facturas; `2026-06-04` (reception_date) → 0; response sin `fechaRecepcion`.

Nota: los campos del request siguen llamándose `fechaInicio/FinalRecepcion` pero refieren a la fecha de registro de la factura.

---

## 2026-06-15 | Error 500 en /register con XML addenda Detecno (PARKMEX)

**Contexto**: Fer reportó `Error 500` en POST `/register` de fiscal-api con el XML `AUGL750630GE4_1089513_MontoExacto.xml`.
Request: `idTransaccion=f25797e1-...`, `receptionId=5ef71932-...`, `supplierNumber=252523`, `purchaseOrderNumber=843754` (sin pdfFile).

**Reproducción local (rama `dmontes`, último build)**: **NO reproduce** → responde `RES004` (registrada OK), incluso creando la recepción `5ef71932` con monto exacto (20045). El 500 es específico del entorno/código de Fer.

**Hallazgos**:
1. El `/register` vivo usa `InvoiceServiceImpl`, que **ya no valida estructura de addenda** (removido en tren v1.0, 2026-06-05) → por eso local registra sin importar la addenda.
2. Existe servicio legacy `InvoiceRegistrationServiceImpl` (PASO 7 valida addenda con `addendaValidator.validateAddenda`). Si el entorno de Fer corre ese o un build previo, valida y se comporta distinto.
3. **Pista principal**: el XML trae `<Addenda_Sodimac_Detecno>` con `OrdenCompra/IdRecepcion/IdProveedor`. El validador `AddendaValidationServiceImpl` SOLO reconoce `Addenda_Sodimac`, `Addenda_Sodimac_CartaPorte`, `Addenda_Transportistas_Sodimac_Detecno`. **`Addenda_Sodimac_Detecno` no está** → en nuestro código actual daría `BUS001` (manejado), no 500. Un 500 saldría en una versión con match laxo por "Detecno" que intente leer campos inexistentes (IdGuiaEntrega/IdViaje).
4. Inconsistencias del XML: `SubTotal=20045.00` vs `Total=4088.00` (Total<SubTotal); addenda `IdProveedor=252202` ≠ form `supplierNumber=252523`; `OrdenCompra/IdRecepcion=846131` ≠ form (`843754`/`5ef71932`).

**RESUELTO 2026-06-15** (commit en `dmontes`):
- **Causa real** (log pod UAT): `UNHANDLED ERROR: duplicate key value violates unique constraint "uq_invoice_fiscal_uuid"`. La factura (fiscalUuid `940946EB-9F31-4C88-A56A-8D1F5FBAC069`) **ya estaba registrada en UAT**. No era la addenda.
- **Bug**: los checks de duplicado en `registerInvoice` (PASO 6.1 serie+folio y 6.2 UUID) usan clave **compuesta con `issuerUuid`**. Si el emisor del registro existente difiere del que arma `getOrCreate`, ambos checks fallan → el documento llega a persistir → revienta el constraint único (que es solo por `fiscal_uuid`) → `ControllerAdvisor` devuelve 500 genérico (`{"message":"Internal Server Error","code":500}`).
- **Fix**: `validateNoDuplicateByUuid` ahora valida **por `fiscal_uuid` solo** (`invoiceRepository.findByFiscalUuid`) además del check compuesto. El folio fiscal del SAT es único global. Ahora responde `WRN7014` ("previamente registrada con el mismo UUID") en vez de 500.
- **Probado local**: simulando que 6.1 falla (folio distinto en BD) con fiscalUuid ya existente → antes 500, ahora `WRN7014`.
- **VERIFICADO EN UAT 2026-06-15**: mismo curl de Fer → `HTTP 400 WRN7014` (ya no 500). Fix desplegado (uat `7f01246`).

**Nota descartada**: la addenda `Addenda_Sodimac_Detecno` NO era la causa del 500 (el `/register` vivo usa `InvoiceServiceImpl`, que no valida estructura de addenda). Queda como observación menor por si en otro flujo se valida.

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
