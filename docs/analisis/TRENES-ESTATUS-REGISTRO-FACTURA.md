# Trenes de estatus en el registro de factura (XML) + decisiones Ivan

> Fuente: diagramas `sesiones/diagramas/DiagramaFlujoFactura.drawio.xml` (Ivan, 2026-06-16) + revisión de código fiscal-api / finanzas-api.
> Última actualización: 2026-06-16.

## Trenes que intervienen

Los trenes viven en `shared_catalogs.status_train` por `option_id`:

| option_id | Tren |
|---|---|
| 1 | Factura |
| 2 | Nota de Crédito |
| 3 | Pagos / Complemento |
| 4 | Carta Porte |
| 5 | Recepción |

Al registrar una **factura por XML** intervienen **2 trenes**: **Factura (1)** y **Recepción (5)**.
(Si fuera NC, sería el **2** + **5**.) La **addenda NO tiene tren propio** — es el vínculo factura↔recepción que dispara el tren de Recepción.

### 1. Tren Factura (option_id = 1)
- Define el estatus de la factura: **3 Recibida** (o **2 Recibido Parcial**, ver decisión b).
- En el **registro**, fiscal-api **setea el estatus inicial directo** (`InvoiceServiceImpl.registerInvoice` → `invoice.setStatus(...)`, ~línea 922). **No valida transición** contra el tren — es la creación.
- La validación del tren factura corre en los **cambios posteriores**: `PUT /invoices/{uuid}/status` (`updateInvoiceStatus` → `statusTrainApiService.validateTransition`, option_id por tipo de doc). Esos cambios los hace el **batch SAP** (3→4→5→...).

### 2. Tren Recepción (option_id = 5)
- Al **relacionar factura ↔ recepción** (vía addenda), la recepción cambia de estatus.
- Lo maneja **finanzas-api** (`purchaseOrder.service.ts` → `validarStatus(reception, newStatus, 5, token)` → valida contra el tren de recepción).
- Estatus recepción (`CatEstatusRecepcion`): `0` Disponible (nace) · `1` Consumida · `2` Consumida Manual · `3` En proceso Contable · `4` Rechazo Contable · `5` En proceso de Pago · `6` Pagada · `7` Cancelada · `8` Borrado lógico.
- Transiciones tren recepción (option_id=5): `0→{1,2,7,8}` · `1→{3,4}` · `2→{3,4}`.

| Camino | Estatus recepción | Dónde |
|---|---|---|
| Automático (factura con addenda Sodimac en XML, registro OK) | **1 Consumida** | ⚠️ **GAP** — no implementado en fiscal-api (solo lee la recepción, no la actualiza). ¿batch o pendiente? |
| Manual (factura sin addenda, se relaciona desde finanzas) | **2 Consumida Manual** | finanzas-api (crea `AddendumManual`), `purchaseOrder.service.ts:218-242` |

## Decisiones de Ivan (2026-06-16)

- **(a) Estatus 3 = "Recibida".** El diagrama decía "Proceso de envío"; se corrige el diagrama. Código + Tren v1.0 ya están en "Recibida". **Sin cambio de código.**
- **(b) Fuera de tolerancia → "2 - Recibido Parcial".** El flujo indica que la factura se registre en parcial, NO que se rechace.
  - **Cambio de código pendiente:** hoy `validateImporteTolerance` lanza `BUS057` (rechaza) cuando `diff > tolerancia`. Debe cambiar a **registrar la factura con `status = 2` (Recibido Parcial)** y continuar (pedir NC).
- **Redacción:** unificar "Recibida Parcial" → **"Recibido Parcial"** (estatus 2).

## Pendientes / dudas abiertas a Ivan

1. **(b) caso factura < recepción:** el diagrama muestra "mensaje de alerta, monto menor a la recepción". ¿También queda en parcial (2), o se rechaza/alerta sin registrar? (factura > recepción ya quedó claro: parcial + NC).
2. **Recepción → 1 (Consumida) automático:** ¿lo hace un batch o se implementa en el registro? Hoy es gap.

## Flujo (diagrama) resumido

1. Consulta tolerancia factura vs recepción.
2. ¿Monto factura == recepción al 100%? → **registra factura (3 Recibida)** inmediato.
3. Si hay diferencia → compara con tolerancia.
   - Dentro de tolerancia → registra (3 Recibida).
   - Fuera de tolerancia, factura > recepción → **2 Recibido Parcial** + requiere NC.
   - Fuera de tolerancia, factura < recepción → alerta "monto menor" (definir, punto 1).
4. Al relacionar (addenda) → recepción a **1 Consumida** (automático, gap) / **2 Consumida Manual** (manual).
