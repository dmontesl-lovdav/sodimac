# Ivan - Historial de Consultas

_Consultas mas recientes primero_

---

## 2026-07-02 | Fila 122: estatus de la NC acompaña a la factura (extensión 104)

**Pedido (matriz v9, fila 122, Alto, David, Fiscal/NC)**: hoy (fila 104) la NC se registra en status 1
y solo se re-evalúa la factura. Fila 122 quiere que la **NC acompañe**: **2 (Recibido Parcial)**
mientras (Factura − NC) no cuadra con la recepción, y **3 (En proceso de envío)** —factura Y NC—
cuando cuadra/entra en tolerancia.

**Conflicto detectado (bloqueante)**: el requerimiento usa nombres de estatus de FACTURA, pero la NC
tiene catálogo propio distinto (`CatEstatusNotaCredito`): ahí "Recibido Parcial" NO existe, y "En
proceso de envío" = **1** (no 3). Además la cascada de fila 104 manda la NC a **9 (Cancelada)** usando
el catálogo de NC → mezclar numeraciones en la misma columna `invoice.status` es inconsistente.

**4 dudas enviadas a Ivan**: (1) NC usa CatEstatusNotaCredito o CatEstatusFactura; (2) estatus exacto
de alta de NC con neto sin cuadrar ("Recibido Parcial" no existe en NC); (3) al cuadrar la NC va a 1 o
a 3; (4) ¿reemplaza el status 1 actual de alta?

**Detalle**: [docs/analisis/QA-104-NC-recalculo-tolerancia-estatus3.md](../analisis/QA-104-NC-recalculo-tolerancia-estatus3.md) (sección Fila 122).
**Estado**: análisis hecho, sin codear, esperando confirmación de Ivan (numeración de estatus NC).

---

## 2026-06-29 | Fila 104: recalcular tolerancia (factura − NCs) → estatus 3

**Pedido (matriz v8, fila 104, Alto, David, Fiscal/NC)**: cambiar la factura a **estatus 3 (En
proceso de envío)** cuando `(factura − NCs)` entre en tolerancia vs la recepción. (Fila 33 "cancelar
factura" se reasignó a Fernando/front — ya no es de David.)

**Hallazgo clave**: la resta **NO se persiste** en BD. No hay columna saldo/neto; cada NC es un
registro aparte, `related_cfdi` solo guarda el vínculo UUID, `reception.amount` no se modifica al
cargar NC. → El neto se recalcula en vivo sumando **todas** las NCs vinculadas (Opción A, sin
migración). "Solo restar la última" obligaría a crear un saldo (frágil, no recomendado).

**Dónde**: trigger en `saveRelatedCfdis` al registrar la NC; si factura en 2 (Recibido Parcial) y
neto en tolerancia → status 3. Transición 2→3 ya existe (sin cambio enum). **Gap**: el `reception_id`
no se guarda en la factura (solo `addendum.reception_number`) → resolver monto recepción por número
o persistir UUID.

**5 dudas enviadas a Ivan**: (1) restar por subtotal o total; (2) todas las NCs vs última
(confirmado: todas); (3) solo desde Recibido Parcial(2)→3; (4) qué pasa si sobre-corrige (neto bajo
tolerancia); (5) recalcular también al cancelar NC (revertir 3→2).

**Detalle**: [docs/analisis/QA-104-NC-recalculo-tolerancia-estatus3.md](../analisis/QA-104-NC-recalculo-tolerancia-estatus3.md)
**Estado**: análisis hecho, sin codear, esperando confirmación de Ivan (5 dudas).

---

## 2026-06-26 | PDF: fallback genera desde XML si no está en el bucket

**Pedido Ivan**: al bajar el PDF, si no se subió al bucket, generarlo a partir del XML (usar la
funcionalidad XSLT de Robert). Inicialmente pidió subir el XML al bucket; se resolvió mejor leyendo
el XML de BD.

**Hecho** (`e7a2ccb`, desplegado UAT 2026-06-26): `GET /invoices/{uuid}/pdf` ahora:
1. Si `pdf_gcs_object` existe → descarga el PDF del bucket (igual que antes).
2. Si NO existe (o el bucket falla) → **genera el PDF desde `invoice.xml_content`** con
   `pdfRenderService.renderFromXml` (XSLT `Formato4.0.xsl` + QR, la funcionalidad de Robert).
3. Solo da ERR001 si no hay ni PDF ni XML.

**Decisión**: el fallback lee `xml_content` de **BD**, no del bucket. El XML ya se persiste siempre
en BD al registrar (PASO 9.5), así que NO hace falta subir el XML al bucket (un artefacto menos) ni
migración/columna nueva. Logra lo que Ivan pidió de forma más robusta (la BD siempre tiene el XML
aunque el bucket falle).

**Validado local**: factura con `pdf_gcs_object` vacío + `xml_content` → `GET .../pdf` HTTP 200, PDF
válido generado desde XML (antes ERR001). Solo jar, sin SQL. Relacionado: WRN7033 (`375f545`) sigue
avisando en `warnings[]` cuando el PDF no se sube.

**Detalle**: [[project_fiscal_pdf_gcs_flow]] en memoria.

---

## 2026-06-23 | Llamada NC: serie/folio, uuid relacionado, F94/F93 (front)

**Contexto**: puntos de una llamada haciendo el flujo de NC (matriz xlsx v6, filas 93-98).

**Back (David) — HECHO + validado UAT**:
- **F98** (`2e36ddd`): factura sin serie no se podía subir. Regla nueva: **folio requerido, serie
  opcional** (CFDI 4.0 SAT). Folio sin serie → RES004; sin folio → WRN7012. Mensajes WRN7012/7015
  ajustados a "requiere un folio".
- **F97** (`edd6ee2`): `xml/process/file` ahora regresa `uuidRelacionado` + `tipoRelacion` desde
  `CfdiRelacionados`. Validado con NC real: `uuidRelacionado=A9651E62...`, `tipoRelacion=01`,
  `formaPago=99` (F95 ya existía).

**Front (finanzas-spa) — NO es back, confirmado en código** (repos sincronizados 2026-06-23):
- **F94**: "éxito sin alerta NC". `ReceptionInvoiceControl.tsx` L73-106 duplica tolerancia local
  hardcodeada (`difference > 40`) e **ignora `response.warnings[]`** (back sí manda WRN7030). En
  éxito pinta fijo "Tu factura se procesó correctamente". Fix front: quitar tolerancia local +
  mostrar warnings.
- **F93**: monto de factura relacionada usa **total**, debe **subtotal**. `ReceptionCredits.tsx` L46
  usa `r.invoice.total` → cambiar a `r.invoice.subtotal`. Back ya expone ambos.

**Para Ivan**: F97/F98 listos en UAT (marcar Excel). F94/F93/F95/F96 son **front** → equipo
finanzas-spa.

**Detalle**: [docs/analisis/QA-IVAN-2026-06-23b-nc-serie-folio-uuid-relacionado.md](../analisis/QA-IVAN-2026-06-23b-nc-serie-folio-uuid-relacionado.md)
**Estado**: back cerrado y validado; front pendiente (otro equipo).

---

## 2026-06-23 | Nuevos puntos: addenda manual, CatRfcReceptor, condición de pago NC

**Contexto**: Ivan pasó comentarios para `/register` (carga XML): validar receptor contra
`CatRfcReceptor`; al cargar XML validar que no se haya cargado ya la addenda manual (UUID no exista
en `addendum_manual` ni `addendum`); quitar la FK de `tenant_finance.addendum_manual`; validar
condición de pago de la NC (= `FormaPago` del XML, ej. 99) contra `CATCONDICIONPAGOVALIDONC`.

**Hallazgos**: el flujo "Consumida manual" lo dispara **finanzas (Josue)** —
`purchaseOrder.service.ts` crea `addendum_manual` + pone `reception.status=2`; NO escribe en
`tenant_fiscal.invoice` (por eso la FK estorba). `CatRfcReceptor` no existe; `CATCONDICIONPAGOVALIDONC`
existe vacío y valida el mismo `FormaPago` que `CatFormaPagoValidoNc` (posible redundancia).

**Dudas pendientes**: (1) ¿`CatRfcReceptor` nuevo o es BUS008 ya existente? (2) ¿qué es `dto.uuid`
guardado en `addendum_manual.invoice_uuid` (folio fiscal vs invoice_uuid interno)? (3)
¿`CatCondicionPagoValidoNc` reemplaza o se suma a `CatFormaPagoValidoNc`? + poblar catálogo.

**Listo para codear sin dudas**: quitar FK `fk_addendum_manual_invoice`.

**Detalle**: [docs/analisis/QA-IVAN-2026-06-23-addenda-manual-rfc-condicionpago.md](../analisis/QA-IVAN-2026-06-23-addenda-manual-rfc-condicionpago.md)
**Estado**: Sin codear, pendiente confirmar dudas con Ivan/Josue.

---

## 2026-06-22 | Retro: tipoProveedor en /search + nombre de estatus + bloqueo Transporte

**Contexto**: Ivan dio retro de 3 puntos tras validar UAT.
1. `/search`: tipoProveedor no reflejaba cambios en `CatTipoProveedor` (PARMEX Mercancía→Transporte seguía dando Mercancía).
2. `/register`: estatus salía "Recibida", esperaba "En proceso de envío" (notó que las descripciones no deben venir de código fijo).
3. Proveedor 308550 bloqueado por tipo Transporte (no sabía si era bug).

**Veredicto**: P1 y P2a válidos, P2b no es bug.
- **P1**: tipoProveedor se congelaba al registrar. Fix: resolver en vivo desde `supplier_number` (display en /search y /complementos-pago). Filtro sigue en valor guardado (pendiente).
- **P2a**: el nombre del estatus salía del enum `InvoiceStatus` hardcodeado. El catálogo `CatEstatusFactura` dice 3 = "En proceso de envió". Fix: leer descripción de la BD, no del enum.
- **P2b**: `CatBloqueoTipoProveedor` tiene Transporte con `status=1` = bloqueado. El bloqueo funciona bien; si no quieren que bloquee, ajustar catálogo.

**Commit**: `81deffa` (rama `dmontes`)
**Detalle**: [docs/analisis/RETRO-IVAN-2026-06-22-tipoproveedor-estatus.md](../analisis/RETRO-IVAN-2026-06-22-tipoproveedor-estatus.md)
**Estado**: Implementado, probado local, pendiente deploy UAT.

---

## 2026-05-05 | Reproceso puntos CES por tickets puntuales

**Contexto**: Ivan compartio 126 tickets de tipo asignacion (`TRANSACTIONTYPE='sale'`) que requieren replicarse manualmente al modelo fiscal (`AdminPuntosCes` + `VentaCab` + `VentaDetImpuesto`) sin depender del rango de fechas del job.
**Solucion**: Modo exclusivo nuevo en `ReplicarPuntosJob` controlado por dos props (`puntos.ces.tickets.activo`, `puntos.ces.tickets.lista`). Nuevo metodo de repo `findPuntosCesByTickets` filtra por `NUM_TRX IN (...)` y `TRANSACTIONTYPE='sale'`, sin filtro de fecha. Control CES con clave `MANUAL-yyyyMMdd`.
**Proyecto**: `soporte/bctfacturacion`
**Commit**: `3e568d8` (rama `dmontes`)
**Detalle**: [docs/soporte/ivan/puntos-ces-tickets-puntuales.md](ivan/puntos-ces-tickets-puntuales.md)
**Proceso completo**: [docs/wiki/procesos/10-puntos-ces.md](../wiki/procesos/10-puntos-ces.md)
**Tickets**: `sesiones/soporte/20260505-puntos-ces.txt`
**Estado**: Implementado y pusheado. Validacion en ejecucion — query devuelve 0 filas en algun ambiente, queries de diagnostico documentadas.

---

## 2026-03-18 | Candado 60 días autofacturador no bloquea timbrado

**Contexto**: Iván reporta que al timbrar desde el autofacturador, el sistema deja pasar tickets que deberían estar bloqueados por el candado de 60 días.
**Análisis**:
- El parámetro `Aplicacion.DiasPermitidosFacturar` está correctamente configurado en 60 (consultado vía WS de parámetros)
- La validación está en `autofacturador/TicketsServiceImpl.validarTicketWS()` líneas 138-157
- **Posible causa**: si el ticket no existe en BD Oracle BCT (`ticketRepository.findByTicket()` retorna `null`), el método devuelve "OK" en línea 143 y se salta el candado
- El WSFT (`/timbrarVersion`) NO tiene candado de días propio, solo `/retimbrarTicket` lo tiene
- Iván va a debuggear línea 142 de `TicketsServiceImpl.java` para verificar si `ticketBctHdrVal` llega como `null`
**Proyectos involucrados**: `soporte/autofacturador`, `soporte/sodimacfinanzaswsft`
**Estado**: En investigación por Iván

---

## 2026-03-02 | Nuevo catalogo CatTipoOrigenRecepcionSodimac

**Contexto**: Ivan y Josue solicitaron un nuevo catalogo de tipos de origen de recepcion especifico para Sodimac, con 5 entradas y external keys.
**Problema**: No existia el catalogo. Ya existia `CatTipoOrigenRecepcion` (id=20) pero necesitaban uno con datos especificos.
**Solucion**: Se creo script portable `seed_CatTipoOrigenRecepcionSodimac.sql`. Probado en local.
**Datos**: TOS001=SLI(Mercancia), TOS002=TRA(Transporte), TOS003=IND(Indirectos), TOS004=SOT(Servicios), TOS005=Blanco(Mercancia/ODMBS)
**Endpoint**: `GET /CatTipoOrigenRecepcionSodimac/details?lang=1`
**Archivos**: `docs/db/catalogs/seed_CatTipoOrigenRecepcionSodimac.sql`
**Jira**: -
**Estado**: Resuelto en local. Pendiente ejecutar en Sodimac DEV.

---

## 2026-03-02 | Estructura de tablas de pagos (payment_header / payment_detail)

**Contexto**: Ivan pidio la estructura y nombre de las tablas de pagos del esquema `tenant_finance`
**Problema**: Necesitaba conocer el modelo de datos de pagos para su desarrollo
**Solucion**: Se le compartio la estructura completa. Detalle a continuacion:
**Jira**: STM-399
**Estado**: Resuelto (informacion entregada)

### Estructura cabecera-detalle

```
payment_header (1) ──── (N) payment_detail
```

### `payment_header` — Cabecera (deposito total al proveedor)

| Columna | Tipo | Null | Default | Descripcion |
|---------|------|------|---------|-------------|
| `payment_header_uuid` | UUID | NO | gen_random_uuid() | **PK** |
| `company` | INTEGER | NO | | Empresa SAP |
| `vendor_number` | INTEGER | NO | | Numero de proveedor |
| `currency` | VARCHAR(3) | NO | 'MXN' | Moneda |
| `total_amount` | NUMERIC(15,2) | NO | | Monto total del deposito |
| `payment_date` | DATE | NO | | Fecha de pago |
| `status` | INTEGER | NO | 1 | Estado |
| `created_by` | BIGINT | SI | | Usuario creador |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | Fecha creacion |
| `updated_by` | BIGINT | SI | | Usuario actualizador |
| `updated_at` | TIMESTAMP | SI | | Fecha actualizacion |

Indices: `vendor_number`, `company`, `payment_date`, `status`

### `payment_detail` — Detalle (lineas de pago a facturas)

> Antes se llamaba `finanzas_payments`, renombrada por STM-399

| Columna | Tipo | Null | Default | Descripcion |
|---------|------|------|---------|-------------|
| `finanzas_payment_uuid` | UUID | NO | gen_random_uuid() | **PK** |
| `payment_header_uuid` | UUID | NO | | **FK → payment_header** |
| `company` | INTEGER | NO | | Empresa SAP |
| `document_number` | VARCHAR(100) | NO | | Numero de documento |
| `document_reference` | VARCHAR(100) | NO | | Referencia del documento |
| `vendor_number` | INTEGER | NO | | Numero de proveedor |
| `amount` | NUMERIC(15,2) | NO | | Monto del pago |
| `currency` | VARCHAR(3) | NO | 'MXN' | Moneda |
| `document_type` | VARCHAR(5) | NO | | Tipo de documento |
| `sap_document` | VARCHAR(50) | NO | | Documento SAP |
| `payment_date` | DATE | NO | | Fecha de pago |
| `status` | INTEGER | NO | 1 | Estado (0-3) |
| `created_by` | BIGINT | SI | | Usuario creador |
| `created_at` | TIMESTAMP | NO | CURRENT_TIMESTAMP | Fecha creacion |
| `updated_by` | BIGINT | SI | | Usuario actualizador |
| `updated_at` | TIMESTAMP | SI | | Fecha actualizacion |

Indices: `document_number`, `vendor_number`, `sap_document`, `payment_date`, `status`, `payment_header_uuid`
Check constraints: `amount >= 0`, `status IN (0,1,2,3)`

---
