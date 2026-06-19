# Recepción de mercancía y fechas de la factura

> Vista de negocio: de dónde nace una recepción, cómo se liga a la factura y qué significan las
> distintas fechas que ve el usuario. Sin código.
> Última actualización: 2026-06-18.

---

## Qué es una recepción

Una **recepción** es el registro de que la mercancía/servicio de una orden de compra **llegó físicamente** a Sodimac. Es el documento contra el que se concilia la factura del proveedor (Three Way Match: OC ↔ recepción ↔ factura).

- Vive en `tenant_finance.reception` (módulo **finanzas**).
- Cada recepción tiene un **monto** (`amount`) y una **fecha de recepción** (`reception_date`).
- Estatus de recepción (`CatEstatusRecepcion`): `0` Disponible · `1` Consumida · `2` Consumida Manual · `3` En proceso Contable · `4` Rechazo Contable · `5` En proceso de Pago · `6` Pagada · `7` Cancelada · `8` Borrado lógico.

## De dónde nace la fecha de recepción

La `reception_date` **no la genera el portal** ni el registro de la factura. Es la fecha real en que se recibió la mercancía, **originada en SAP** (documento **MIGO** = goods receipt) y cargada a finanzas al dar de alta la orden de compra + recepción.

```
SAP (MIGO / goods receipt)  ──fecha_recepcion──►  finanzas-api (alta OC + recepción)
                                                   tenant_finance.reception.reception_date
```

- El valor viaja **en el payload** del alta (`receptionList[].receptionDate`); finanzas lo guarda tal cual (no lo autogenera con la fecha del sistema).
- Por eso la fecha de recepción puede ser **anterior** (días o semanas) al momento en que el proveedor sube su factura al portal.

## Cómo se liga la recepción con la factura

Al registrar la factura (XML con addenda Sodimac), se guarda una **addenda** que apunta a la recepción:

```
tenant_fiscal.invoice ──(invoice_uuid)──► tenant_fiscal.addendum
addendum.reception_number  ==  tenant_finance.reception.reception_id   (UUID en texto)
```

- `addendum.reception_number` guarda el **UUID** de la recepción (no el número legacy).
- Una recepción puede estar ligada a varias facturas (ej. varias FVS contra la misma recepción).

Ver también: [Addenda Sodimac](09-addenda-sodimac.md) · [Three Way Match](05-three-way-match.md).

## Las dos fechas que se confunden

| Fecha | Qué significa | Dónde nace |
|---|---|---|
| **Fecha de registro** (`invoice.created_at`) | Cuándo el proveedor **subió/cargó la factura** al portal | fiscal-api, al registrar el XML |
| **Fecha de recepción** (`reception.reception_date`) | Cuándo se **recibió la mercancía** (SAP MIGO) | finanzas, al alta de la recepción |

Pueden diferir bastante (caso real UAT: factura registrada 15-jun, recepción 04-may → 42 días). El usuario que busca facturas piensa en la **fecha de recepción**, no en la de registro.

## Búsqueda de facturas/NC por fecha (endpoint search)

Endpoint: `POST /invoices/search`, campos `fechaInicioRecepcion` / `fechaFinalRecepcion`.

- **El filtro trabaja sobre `invoice.created_at` (fecha de REGISTRO de la factura en el portal).** En este endpoint la `reception_date` de finanzas (orden de compra) **NO juega**.
- Los campos del request se llaman `fechaInicio/FinalRecepcion`, pero refieren a la fecha de **recepción/registro de la factura en el sistema**, no a la recepción de mercancía de SAP.
- El response **no** expone `fechaRecepcion`.

> **Historial de decisión:**
> - 2026-06-18: se cambió el filtro a `reception_date` (a pedido de Fer, que veía vacío el rango exacto).
> - **2026-06-19 (vigente):** negocio aclaró que la búsqueda debe ir por la **fecha de registro de la factura** (`created_at`). Se revirtió: el filtro vuelve a `created_at` y se quitó el campo `fechaRecepcion` del response. Detalle en [docs/soporte/fer.md](../../soporte/fer.md).

## Estatus de recepción al relacionar la factura

- **Automático** (factura con addenda Sodimac en el XML, registro OK): la recepción debería pasar a **1 Consumida**. ⚠️ Hoy es **gap** — fiscal-api solo lee la recepción, no la actualiza (pendiente: ¿batch o registro?).
- **Manual** (factura sin addenda, se relaciona desde finanzas): la recepción pasa a **2 Consumida Manual**.

Transiciones válidas del tren de recepción (`status_train`, option_id=5): `0→{1,2,7,8}` · `1→{3,4}` · `2→{3,4}` · `3→5` · `4→5` · `5→6`.

Ver tren completo: [docs/analisis/TRENES-ESTATUS-REGISTRO-FACTURA.md](../../analisis/TRENES-ESTATUS-REGISTRO-FACTURA.md).
