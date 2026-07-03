# Pendientes fiscal-api — julio 2026

> Puntos abiertos que compartió el usuario (2026-07-03) para retomar. Estado verificado en código.

## P1 — Nombres internos del ZIP (xml/PDF) con "-" cuando no hay folio
**Contexto**: la descarga masiva regresa un ZIP. Ya se corrigió (a) la generación del PDF desde XML
(XSLT, guión serie/folio) y (b) el **nombre del ZIP**. Falta confirmar los **nombres de los archivos
DENTRO del ZIP** (xml y PDF), que traerían un "-" colgante cuando falta folio.

**Estado en código**: **YA CORREGIDO** (`dbdf3de`). `buildXmlFileName`/`buildPdfFileName` delegan en
`buildDocumentFileName` ([InvoiceServiceImpl.java:2445](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/service/impl/InvoiceServiceImpl.java#L2445)),
que solo agrega "-" si hay **serie Y folio** (`Serie-Folio_UUID`; si falta uno → `Serie_UUID` /
`Folio_UUID`; si faltan ambos → `UUID`). Sin guión colgante.

**Acción**: **verificar que el jar desplegado en UAT tenga `dbdf3de`**. Si el "-" que vio el usuario
fue antes de ese deploy, ya está resuelto. Si persiste tras deploy → revisar si hay otro armado de
nombre (los ZIP usan `buildXmlFileName`/`buildPdfFileName` en `downloadXmlZip`/`downloadPdfZip`).
Prueba: descargar ZIP de una factura/NC **sin folio** y revisar los nombres internos.

## P2 — Search por factura relacionada
**Contexto**: en `POST /invoices/search` poder buscar por **factura relacionada** (para NC). El
usuario propone "otro parámetro para este escenario".

**Estado en código**: **YA EXISTE** el parámetro `relatedInvoiceUuid` en `InvoiceSearchRequest`
([:135](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/dto/InvoiceSearchRequest.java#L135)),
aplicado en `InvoiceSpecification` ([:196](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/repository/specification/InvoiceSpecification.java#L196))
contra `related_cfdi.related_invoice_uuid`. Es decir: pasando el `invoice_uuid` de una factura,
devuelve las NC relacionadas a esa factura.

**A definir con el usuario/Ivan** (antes de crear un parámetro nuevo):
- ¿El escenario es "dado una factura, traer sus NC" (ya cubierto por `relatedInvoiceUuid`)?
- ¿O es "dada una NC, filtrar/traer por el fiscalUuid de su factura relacionada"? En ese caso el
  filtro debería ser por **fiscalUuid** de la factura relacionada (hoy `relatedInvoiceUuid` es el
  `invoice_uuid` interno, no el fiscal). Ahí sí convendría un parámetro nuevo (ej.
  `relatedFiscalUuid`) que resuelva la factura por fiscalUuid y filtre las NC.
- Ojo consistencia con el fix del search por UUID (fila QA): si viene este filtro, ¿también se
  ignoran fechas? Definir.

**Acción**: confirmar el escenario exacto; si es por fiscalUuid de la relacionada, agregar
`relatedFiscalUuid` (resolver fiscal→invoice_uuid y reusar el predicate existente).

---
_Estado: ambos verificados en código, sin acción de código inmediata. P1 = verificar deploy; P2 =
definir escenario con Ivan/usuario._
