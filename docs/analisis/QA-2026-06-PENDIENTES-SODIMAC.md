# QA junio 2026 — Pendientes y checklist (resolver local → pasar a Sodimac lunes 2026-06-15)

> Fuente: `sesiones/batch/Pruebas_QA (2).docx`
> Estrategia: **resolver + documentar de este lado** (PC personal / mirror), el **lunes 15-jun** se pasa a Sodimac (develop → uat + data/config UAT).
> Última actualización: 2026-06-13.

---

## Estado deploy UAT — verificado 2026-06-15

Código en develop + uat (fiscal-api `77df2fc`/`7f01246`, util-api `c0ad742`/`eded68f`). Verificación BD UAT:

| Item | UAT |
|---|---|
| Columna `pdf_gcs_object` | ✅ existe |
| `status_train` v1.0 (18 filas, 2→1 cancelar) | ✅ |
| Constraint `chk_invoice_status` 1..18 | ✅ |
| `CatFormaPagoValidoNc` (99) / `CatUsoCfdiValidoNc` (G02) / `CatBloqueoTipoProveedor` | ✅ **seedeados 2026-06-15** (`17_QA-2026-06_catalogos_nc_bloqueo.sql`) — UAT no los tenía |
| `cat_parameter` id3 monto / id4 % | ⚠️ **status=0 (inactivos)** → tolerancia = EXACTA. Confirmar con Ivan/finanzas si activar id3 (40) |
| Mensajes BUS058/059/060/2028/2029 | ⬜ no seedeados (enum tiene fallback, opcional) |
| Env vars GCS (`GCS_BUCKET`, `GCS_PREFIX_SOMX`) | ⬜ pendiente Bonelli |

> ⚠️ Sin los catálogos NC, fiscal-api rechazaba TODA NC (BUS058/059). Ya seedeados. Falta decidir tolerancia (exacta vs monto 40).

---

## A. Acciones que SOLO se hacen del lado Sodimac (UAT)

> No se pueden ejecutar desde PC personal (sin VPN/cert/cluster). Lista para correr el lunes.

| # | Acción | Dónde | Script / Detalle | Depende de |
|---|---|---|---|---|
| A1 | Aplicar tren de estatus v1.0 Factura | Postgres UAT `shared_catalogs.status_train` | `sesiones/sql/sync-status-train-v1.0.sql` | — |
| A2 | Alta env vars GCS para PDF | k8s deployment `fiscal-api` UAT | `GCS_BUCKET=<bucket>`, `GCS_PREFIX_SOMX=fiscal/invoices` | Saber nombre bucket (Ivan/infra) |
| A3 | Permisos SA del pod sobre bucket GCS | GCP IAM / Workload Identity | Storage Object Admin (o Creator+Viewer) | A2 |
| A4 | Migración columna PDF | Postgres UAT `tenant_fiscal.invoice` | `ADD COLUMN IF NOT EXISTS pdf_gcs_object VARCHAR(500)` (o RENAME si quedó `pdf_path`) | — |
| A5 | Deploy fiscal-api (PDF + tren v1.0) | pipeline `uat` | merge develop→uat | A1, A4 |
| A6 | Deploy util-api (seed v1.0 alineado) | pipeline `uat` | seed `12_status_train_data.sql` solo afecta DB nueva; UAT existente se arregla con A1 | — |
| A7 | Deploy fiscal-api + util-api (bloqueo proveedores) | pipeline `uat` | nuevo endpoint util-api `GET /suppliers/number/{n}/type-blocked` + BUS2028/2029 en fiscal-api. Catálogo `CatBloqueoTipoProveedor` (id 82) YA existe en UAT (dump 2026-06-12). Mensajes BUS2028/2029: fiscal-api tiene fallback en enum, opcional seedearlos en `CatMsgNegocio` | — |
| A8 | Deploy fiscal-api (validaciones NC) | pipeline `uat` | BUS058/BUS059 en fiscal-api (fallback en enum). Catálogos `CatFormaPagoValidoNc` (id 89, valor 99) y `CatUsoCfdiValidoNc` (id 87, valor G02) YA existen en UAT (dump 2026-06-12). Reusa endpoint existente `GET /catalog/{code}/details` de util-api | — |

**Responsables sugeridos:** Bonelli (env vars + deploy), Ivan (bucket GCS + decisiones tren NC).

---

## B. Checklist por punto QA

Leyenda: ✅ resuelto local · 🔧 en progreso · ⬜ pendiente · 🚫 no es backend (frontend/otro) · ❓ requiere decisión

### B1. Facturas

- [x] ✅ **Cancelar factura "marca error"** — NO era bug código. Falta data UAT (tren v1.0). Validado local 2→1=Rechazo Comercial. → **A1**
- [ ] ⬜ Filtro búsqueda por fecha de recepción no retorna (26-30 mayo sin resultado, sí hay data)
- [ ] ⬜ Ordenar estatus de la factura conforme catálogo `CatEstatusFactura`
- [ ] 🔧 Error al visualizar PDF de la factura — feature PDF recién hecho; revisar si es por falta de config GCS (A2/A3) o bug
- [ ] ❓ PDF opcional/obligatorio por parámetro — *implementado upload opcional; falta el PARÁMETRO de catálogo que lo vuelve obligatorio.* Ver C1
- [x] ✅ Validación XML no-factura → mensaje claro. Implementado + probado local (XML tipo P → mensaje "no corresponde a una factura válida"). **OJO numeración:** Ivan pidió `BUS057` pero ese id ya estaba en uso (tolerancia de importe) → se asignó **`BUS060`**. Ver nota C4.
- [x] ✅ Bloqueo publicación por **tipo proveedor** (`CatBloqueoTipoProveedor` activo) → `BUS2028` — implementado + probado local (register supplier MERCANCIA bloqueado → BUS2028)
- [x] ✅ Bloqueo publicación por **proveedor** (posterior a tipo) → `BUS2029` — implementado + probado local (supplier_block vigente → BUS2029). Orden tipo→proveedor confirmado.
- [x] ✅ Validar **recepción vs factura** con tolerancia (BUS057). Mejorado + probado local los 3 modos: monto activo (id3)→por monto; monto off + % activo (id4)→por porcentaje (base=importe recepción); ambos off→exacto. Orden RFC→es factura→bloqueo tipo→bloqueo proveedor→tolerancia confirmado. Ver nota C5 (interpretación del valor %).
- [x] ✅ Estatus factura según catálogo (Ok Dev 12/06) → **A5 deploy**
- [x] ✅ Grid usa UUID documento, no guid BD (Ok Dev 12/06) → **A5 deploy**
- [ ] 🚫 Fecha default actual en filtros inicio/fin (frontend)
- [ ] 🚫 Grid vacío al inicio igual a Carta Porte (frontend)

### B2. Notas de Crédito (backend)

- [x] ✅ Validar **forma de pago** vs `CatFormaPagoValidoNc` al registrar → `BUS058`. Implementado + probado local (forma no válida → BUS058). Elemento XML: `Comprobante/@FormaPago`.
- [x] ✅ Validar **uso CFDI** vs `CatUsoCfdiValidoNc` (posterior a forma de pago) → `BUS059`. Implementado + probado local. Elemento XML: `Receptor/@UsoCFDI` (QA decía Emisor; en CFDI 4.0 va en Receptor). Orden forma→uso confirmado.
- [ ] ❓ PDF opcional/obligatorio NC por parámetro (mismo patrón factura). Ver C1
- [ ] ⬜ Error al subir NC ("Publicar NC") — reproducir (¿mismo patrón tren/data que cancel factura?)
- [ ] ❓ Cancelar NC — seed `option_id=2` sigue tren viejo. ¿Alinear NC ahora o esperar? (decisión Ivan)

### B3. Publicación recepción MIGO

- [ ] ⬜ Filtro fecha de publicación incorrecto (busca 13/06 retorna 12/06)
- [ ] ⬜ Al autorizar recepción → registrar estatus **0 Disponible** (hoy registra 1 Consumida = incorrecto)
- [ ] ⬜ Fecha de registro de recepción incorrecta
- [ ] ⬜ Layout recepción: agregar `Numero_Proveedor` (tras `Nro_Recepcion`)
- [ ] ⬜ Error al autorizar la recepción (reproducir)

### B4. Guía de embarque (backend)

- [ ] ⬜ Cancelar guía → no cambia estatus recepción a "7 Cancelada"
- [ ] ⬜ Filtro por fecha desfasado un día (pide hoy, trae ayer)

### B5. Descuentos comerciales (backend)

- [ ] ⬜ Filtro por fecha de aplicación trae días anteriores (espera solo 17/05)
- [ ] ⬜ Relacionar NC ↔ descuento comercial vía tabla addenda de la NC

### B6. Catálogo de catálogos

- [ ] ⬜ STM-1590 — validación formato/contenido muestra error genérico (no detalla celda/causa)
- [ ] ⬜ STM-1595 — filtro "Elemento" sin búsqueda parcial LIKE / case-insensitive / TRIM
- [ ] ⬜ Detalle de catálogos no presenta info al buscar

### B7. Admin proveedores (backend)

- [ ] ⬜ Proveedor inactivo desaparece del grid (debe verse con estatus Inactivo)

### B8. Seguridad / usuarios (revisar si backend)

- [ ] ⬜ Eventos duplicados en grid (perfil evento / aplicativo evento) — filtrar por pantalla
- [ ] 🚫 Paginación grid usuarios (frontend)

### B9. Pura UI (no backend — frontend SPA)

> ⚠️ Copias locales de SPA NO actualizadas — no tocar desde acá, anotar para equipo front.

- [ ] 🚫 Renombrar columnas grids (recepciones, NC, exportes CSV)
- [ ] 🚫 Reubicar/renombrar botones (Guardar, Exportar CSV primario, Volver, etc.)
- [ ] 🚫 Diseños prototipo (subir factura, NC, complemento, guías de embarque)
- [ ] 🚫 Paginadores, listbox catálogos, estilos filtros
- [ ] 🚫 No permitir fechas futuras en pickers

---

## C. Decisiones pendientes (preguntar antes de codear)

- **C1. Parámetro PDF obligatorio/opcional** — QA pide un parámetro de configuración:
  - inactivo/no existe → "Subir PDF (Opcional)"
  - activo → "Subir PDF (Requerido)"
  - Aplica a Factura, NC y Complemento de pago.
  - ❓ ¿Dónde vive el parámetro? (`CatParameter` en util-api / catálogo de catálogos). Confirmar key + alcance con Ivan.
- **C2. Tren NC v1.0** — ¿alinear `option_id=2` ahora o mantener viejo?
- **C3. Tolerancia recepción vs factura** — ✅ resuelto. Fuente = `core_utils.cat_parameter` id3 (monto) e id4 (porcentaje), cada uno con `status`. Monto tiene prioridad; conviven (monto gana si ambos activos).
- **C5. ⚠️ Interpretación del valor de "Tolerancia por porcentaje" (cat_parameter id4 = 0.01).** Implementado como **fracción**: tolerancia = importe_recepción × valor (0.01 → 1% del importe → ej 270 sobre 27000). Si Ivan esperaba que 0.01 sea "0.01%" o que el valor se ingrese como "1" para 1%, ajustar. Confirmar convención del campo en la pantalla de parámetros.
- **C4. ⚠️ AVISAR A IVAN — reasignación de código de mensaje.** QA pidió `BUS057` = "El archivo XML no corresponde a una factura válida". Pero `BUS057` ya estaba asignado a la validación de tolerancia de importe (implementado 2026-06-05). El mensaje de XML-no-factura quedó como **`BUS060`** con el texto exacto de Ivan. En el dump de Sodimac (2026-06-12) BUS057/058/059/060 estaban libres en el catálogo de mensajes, así que no hay conflicto en BD. Resumen de códigos nuevos fiscal-api: `BUS058`=forma pago NC, `BUS059`=uso CFDI NC, `BUS060`=XML no factura, `BUS2028`=bloqueo tipo, `BUS2029`=bloqueo proveedor.

---

## D. Estado de despliegue (lo ya hecho local, listo para subir)

| Item | Local | Mirror (dmontes) | UAT |
|---|---|---|---|
| Feature PDF GCS (fiscal-api) | ✅ | ✅ pusheado | ⬜ deploy + A2/A3/A4 |
| Tren estatus v1.0 (fiscal-api) | ✅ | ✅ | ⬜ A1 + deploy |
| Seed v1.0 alineado (util-api `12_status_train_data.sql`) | ✅ editado | ⬜ commit | ⬜ A1 (data) |

---

## E. Plan lunes 2026-06-15

1. Correr A1 (tren v1.0) + A4 (columna PDF) en Postgres UAT.
2. A2/A3 con Ivan/Bonelli (bucket + permisos GCS).
3. Merge develop→uat (A5) → deploy fiscal-api + util-api.
4. Re-test QA: cancelar factura, ver PDF, registrar con PDF.
5. Avanzar puntos backend ⬜ pendientes (priorizar bloqueos proveedor + tolerancia + validaciones NC).
