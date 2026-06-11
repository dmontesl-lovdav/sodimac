# Analogía: batches ↔ fiscal-api ↔ Tren de Estatus

> Explicador funcional. Para entender de un vistazo qué hacen los 3 batches, qué hace fiscal-api, y por qué hoy están desalineados.
> Fecha: 2026-06-11 · Relacionado: [ANALISIS_BATCHES_ROBERT.md](ANALISIS_BATCHES_ROBERT.md), [DRIFT_INVOICE_SYNC_VS_FISCAL_API.md](DRIFT_INVOICE_SYNC_VS_FISCAL_API.md).

## La idea en una frase

**fiscal-api es el rastreo + reglamento; los batches son los escáneres que reportan avances; el Tren de Estatus v1.0 es la ruta oficial. Recalibrar los escáneres al tren nuevo = el trabajo pendiente.**

## La analogía: rastreo de un paquete (tipo DHL)

Una **factura = un paquete** que el proveedor manda a Sodimac para cobrar. Viaja por estaciones hasta "pagado".

### 🧠 fiscal-api = sistema central de rastreo (+ reglamento)
El cerebro. Guarda **cada factura y su estatus actual**, y tiene el **mapa oficial de hitos** (el Tren de Estatus v1.0, que vive en BD `shared_catalogs.status_train`). Nadie mueve una factura a un estatus que el mapa no permita — si se intenta, fiscal-api **rechaza** (`WRN7011 Transición no permitida` / `WRN7010 origen no catalogado`). Es la **única fuente de verdad**.

### 🗺️ Tren de Estatus v1.0 = la ruta oficial
La lista de checkpoints y qué saltos son legales entre ellos. Definido por Ivan en Excel (`Tren_Estatus_Portal_FBC_v1.0.xlsx`, 2026-06-02) → cargado a la BD (`status_train` + enum `InvoiceStatus`). Es el reglamento; lo que está en BD manda.

### 🤖 Los batches = los escáneres en cada estación
No son el cerebro. Brazos automáticos que **revisan sistemas externos** y luego le dicen a fiscal-api "avanza esta factura". fiscal-api valida contra el mapa y registra.

| Batch | Rol (analogía) | Mueve estatus | Sistemas que consulta |
|---|---|---|---|
| **fiscal-download** (David) | escáner de **RECEPCIÓN**: abre la caja (descarga XML) y desempaca (desglosa CFDI) | 3 Recibida → 4 → 5 Desglose | fiscal-api |
| **invoice-status-sync** (Robert) | escáneres de **DESPACHO/PAGO**: preguntan ¿registrado? ¿contabilizado? ¿pagado? | 7 → 8 → 9 → 10 → 11 | SAP, SAPITO (Oracle), i213 |
| **rebate-agreements-sync** (Robert) | **bodega APARTE**: actualiza catálogo de descuentos/convenios | — (otro dominio) | API Azure rebate-management |

## Diagrama

```
                          ┌──────────────────────────────────────┐
                          │            fiscal-api                 │
                          │  (rastreo central + valida tren v1.0) │
                          │   BD: status_train + InvoiceStatus    │
                          └──────────────────────────────────────┘
                              ▲            ▲             ▲
                 "avanza"     │            │ "avanza"    │ (rebate NO toca facturas)
                              │            │             │
        ┌─────────────────────┐   ┌──────────────────────┐   ┌────────────────────────┐
        │  fiscal-download    │   │ invoice-status-sync  │   │ rebate-agreements-sync │
        │  RECEPCIÓN          │   │ DESPACHO / PAGO      │   │ catálogo descuentos    │
        │  3→4→5              │   │ 7→8→9→10→11          │   │ (Azure → RebateTemp)   │
        └─────────────────────┘   └──────────────────────┘   └────────────────────────┘
                                       │      │      │
                                  consulta:   │   i213 (SQL Server)
                                   SAP ───┘  SAPITO (Oracle)

Flujo factura (tren v1.0):  3 → 4 → 5 → 7 → 8 → 9 → 10 → 11 → 12 → 13
                            └ fiscal-download ┘ └──── invoice-status-sync ────┘
```

## Cómo influyen en fiscal-api

Los batches **empujan**, fiscal-api **valida y registra**. Una factura avanza solo si:
1. El batch confirma el hecho real en el sistema externo (ej. i213 dice "pagado").
2. fiscal-api acepta el salto porque está en el mapa v1.0.

## Por qué hoy están desalineados

Los 3 escáneres se programaron con un **mapa viejo** (numeraciones previas a v1.0). fiscal-api ya tiene el **mapa v1.0**. Cuando un batch dice "mueve a 14" pensando "error desglose", v1.0 dice "14 = Rechazo Contable" → factura mal etiquetada o salto rechazado.

**No es que los batches sean malos — el mapa cambió después de fabricarlos.** Pendiente: **recalibrar los 3 batches al tren v1.0** (que ya está en BD).

| Batch | Drift vs v1.0 |
|---|---|
| fiscal-download | `ERROR_DESGLOSE=14` (debe ser 6/16); `PENDIENTE_ADDENDA=1` obsoleto (addenda eliminada). 3/4/5 ok |
| invoice-status-sync | rango 6-16 corrido −1; `getFbcStatusCode` (3/7/8/11) obsoleto → eliminar; entrada 6 → debe ser 7 |
| rebate-agreements-sync | sin estatus de factura (no aplica) |

> Fuente de verdad = Tren v1.0 en BD. STM-1309 y numeraciones previas = históricas, NO usar como referencia de estatus.
