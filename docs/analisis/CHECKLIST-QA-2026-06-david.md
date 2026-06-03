# Checklist QA 2026-06 — Módulos David (fiscal-api + util-api)

> Triaje de la ronda QA de Sodimac. Fuente: `sesiones/Pruebas_QA.docx`.
> Doc maestro de errores lo lleva Ivan (Word aparte) — este checklist es **solo el subconjunto que toca David**: `fiscal-api` y `util-api`.
> Estado: `[ ]` pendiente · `[~]` en revisión · `[x]` resuelto.
> Última actualización: 2026-06-02.

---

## fiscal-api

- [~] **F1 · Facturas: "Marca error al intentar cancelar la factura"** — DIAGNOSTICADO 2026-06-02
  - Cancelar (FE) = `PUT /invoices` con `estatus:0` (RECHAZO_COMERCIAL) → `updateInvoice` (NO el endpoint `/status`).
  - Reproducido local:
    - Sin addenda → **BUS048** (`validateSupplierOwnership` exige addenda, `InvoiceServiceImpl:942`).
    - Con addenda → **BUS051** "transición no permitida De: 2 a: 0" (`validateStatusTransition` → `InvoiceStatus.puedeTransicionarA`).
  - RAÍZ: enum `InvoiceStatus` no incluye `0` en ninguna lista `estatusSiguientesPermitidos` → cancelar imposible por diseño. NC (E) sí tiene cancelación (CreditNoteStatus.CANCELADA, STM-335); factura no.
  - Inconsistencia: `updateInvoice` valida con enum local; `updateInvoiceStatus` (/status) valida con tren de estatus (catálogo). Dos fuentes de verdad.
  - Catálogo `status_train` (UAT confirmado 2026-06-02): option_id=1 (Factura) NO tiene ninguna transición a target 0. Hueco en AMBAS capas (enum + catálogo). Factura UAT==local.
  - Patrón NC (option_id=2): cancela a estatus 10 "Cancelada" desde source 3 (Pendiente Contabilizar) y 11 (Rechazo Contable).
  - Estatus destino para factura = **"Rechazo Comercial"(0)** (el código 10 en factura ya es "Completado", no sirve). No hay que crear estatus nuevo, solo wire transiciones →0.
  - FIX 2 capas (al confirmar Ivan): (1) enum `InvoiceStatus` agregar 0 a `estatusSiguientesPermitidos` de estados origen elegidos; (2) `status_train` INSERT option_id=1 source→0 (local + UAT + seed repo).
  - ESCALADO 2026-06-02: Ivan respondió con tren de estatus oficial **v1.0** (renumera todo + elimina addenda) → rebasa F1, es un remodel cross-módulo. Ver [TREN-ESTATUS-v1.0-vs-codigo.md](TREN-ESTATUS-v1.0-vs-codigo.md).
  - ESTADO: EN ESPERA — David decidió alinear con Ivan antes de codear (no Path A ni B aún). Preguntas de alineación abajo en el doc del tren.

- [ ] **F2 · Notas de Crédito: "Publicar NC marca error al registrar el documento"**
  - Es `/invoices/register` con `TipoDeComprobante="E"`.
  - Hallazgo previo (2026-06-01): la NC exige factura padre registrada → da `400 ERR003 "registra primero la factura"`. Reproducido con XML `DMC160901KV5` (factura padre `49b6551d-…` no estaba).
  - Acción: confirmar con QA si cargó la factura padre antes. Si no → no es bug, es orden de carga. Si sí → investigar.
  - Relacionado: bug del part `file` vs `xmlFile` ya aclarado a Fer (no era del API).

- [ ] **F3 · Consulta de Complemento: "Pendiente de probar"**
  - Dominio fiscal-api (complemento de pago). En espera de QA.
  - Acción: vigilar. Sin defecto reportado aún.

- [~] **F4 · Filtro "Tipo Proveedor" en búsqueda de Facturas (verificación)**
  - Backend `InvoiceSearchResponse` YA devuelve `numeroProveedor`, `tipoProveedor`, `notasCreditoRelacionadas`.
  - Acción: confirmar que `/invoices/search` **acepta filtrar** por `tipoProveedor` cuando el FE lo manda. Si no filtra → ajuste fiscal-api. Si solo es mostrar → es FE.

---

## util-api

- [x] **U1 · Admin Proveedores: agregar 3 campos de correo** — RESUELTO 2026-06-02
  - `Email CXC` (oblig), `Email Comercial` (opc), `Email Principal` (oblig). Considerados en edición.
  - Ya entró por merge `feat/email-catalogos` a develop: `supplier.dto.ts` (Create/Update schemas), `Supplier.entity.ts`, `supplier.mapper.ts`, `supplier.service.ts`.
  - DB: UAT `shared_catalogs.supplier` YA tiene las 3 columnas (confirmado por SELECT). Local homologado (ALTER aplicado 2026-06-02, owner=postgres).
  - Drift menor: repo `util-api/src/database` solo trackea `15_STM-1376` (email_financial). Falta SQL versionado de principal/commercial — área Josue, opcional.

- [~] **U2 · Admin Proveedores: "listbox Tipo Proveedor no apunta a CatTipoProveedor"**
  - Backend YA expone el catálogo: `getAllSupplierTypes` → `findAllSupplierTypes()` (`supplier.controller.ts:87`).
  - Probable wiring de **frontend** (apunta a endpoint/clave equivocada). NO backend de David salvo que el endpoint esté roto.
  - Acción: confirmar con FE qué endpoint consume; verificar respuesta del catálogo.

- [~] **U3 · Catálogos de soporte a filtros (CatTipoProveedor / CatTipoRebate)**
  - Varios filtros del FE usan estos catálogos.
  - Acción: solo si el FE reporta que no cargan. Verificar que util-api los exponga correctamente. (CatTipoRebate ya marcado OK por QA 29/05.)

---

## NO son de David (descartados del triaje)

- **Frontend SPA** (cosmético/navegación): botón inicio→Home, alinear botones, renombrar columnas/etiquetas, orden de columnas, componentes listbox UI, Pagos "no me lleva a complemento de pago" / botón "Ver", Catálogo de Catálogos "no se ve botón buscar".
  - Nota: backend ya entrega los datos (numeroProveedor, tipoProveedor, NC relacionadas) — el ajuste de columnas/orden es solo visual.
- **Finanzas / otros equipos**: Guía de Embarque (completo), Lista de Recepciones, Estado de Cuenta, Three Way Match, Publicación de Recepción MIGO, Descuentos Comerciales.
- **Ya marcados (OK)** con fecha en el doc QA = resueltos.

---

## Dudas abiertas (confirmar con QA/Ivan)

1. F2 — ¿QA cargó la factura padre antes de publicar la NC? Pedir UUID usado.
2. F1 — UUID + transición de estatus que disparó el error al cancelar.
3. F4 — ¿el FE manda filtro `tipoProveedor` a `/invoices/search` y no filtra, o es solo display?
