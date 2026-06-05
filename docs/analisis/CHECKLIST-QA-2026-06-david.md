# Checklist QA 2026-06 — Módulos David (fiscal-api + util-api)

> Triaje de la ronda QA de Sodimac. Fuente: `sesiones/Pruebas_QA.docx`.
> Doc maestro de errores lo lleva Ivan (Word aparte) — este checklist es **solo el subconjunto que toca David**: `fiscal-api` y `util-api`.
> Estado: `[ ]` pendiente · `[~]` en revisión · `[x]` resuelto.
> Última actualización: 2026-06-02.

---

## fiscal-api

- [x] **F1 · Facturas: "Marca error al intentar cancelar la factura"** — IMPLEMENTADO 2026-06-05
  - Resuelto vía adopción completa del **Tren de Estatus v1.0** (Ivan, 2026-06-02).
  - Cancelar = estatus **1 (Rechazo Comercial)** desde **2 (Recibido Parcial)**. Habilitado en `status_train` y en `InvoiceStatus.java`.
  - BUS048 eliminado de `validateSupplierOwnership` — addenda ya no bloquea actualización.
  - Ver detalle completo en [TREN-ESTATUS-v1.0-vs-codigo.md](TREN-ESTATUS-v1.0-vs-codigo.md).

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
2. F4 — ¿el FE manda filtro `tipoProveedor` a `/invoices/search` y no filtra, o es solo display?

## Pendiente antes de deploy UAT (tren v1.0)

- Avisar Ivan: tren v1.0 implementado, factura registrada queda en estatus **3 (Recibida)**.
- Aplicar `sesiones/sql/sync-status-train-v1.0.sql` en Sodimac UAT.
- Revisar si NC inicial status=1 sigue siendo correcto o también cambia (alineación futura con Ivan).
