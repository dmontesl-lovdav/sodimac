# Proceso: Three Way Match (Conciliación 3-way)

> Validación cruzada **OC ↔ Recepción ↔ Factura** que autoriza el pago al proveedor.

## Qué pasa (vista de negocio)

Sodimac no paga una factura hasta confirmar que:
1. **Existe una OC** previamente emitida.
2. **Hubo recepción** de la mercancía contra esa OC.
3. **La factura coincide** con OC y recepción en cantidades, montos y fechas.

Si las 3 piezas concuerdan → autorizar pago.
Si hay discrepancias → bloquear, generar notificación, o requerir NC del proveedor.

Este proceso corre como un **job periódico** que cruza datos y deja resultados en `three_way_match`.

## Tablas involucradas

### Origen de datos (lo que se cruza)

| Tabla | Schema | Rol |
|---|---|---|
| `purchase_order` | `tenant_finance` | Orden de compra Sodimac. |
| `reception` | `tenant_finance` | Recepción de mercancía. |
| `reception_sku` | `tenant_finance` | SKUs individuales recibidos. |
| `invoice` (`document_type='I'`) | `tenant_fiscal` | Factura del proveedor. |
| `invoice` (`document_type='E'`) | `tenant_fiscal` | NC si aplica. |
| `accounts_payable` | `tenant_finance` | Cuenta por pagar contable (SAP). |
| `sap_document` | `tenant_finance` | Documento contable SAP. |
| `fiscal_payments` | `tenant_finance` | Pago realizado (si ya se pagó). |

### Resultado y ejecución

| Tabla | Schema | Rol |
|---|---|---|
| `three_way_match` | `tenant_finance` | Resultado consolidado por movimiento. |
| `twm_ejecucion` | `tenant_finance` | Cada corrida del job (estado, fechas, intento). |
| `twm_logs` | `tenant_finance` | Logs por ejecución (severidad, mensaje, stack). |
| `twm_cifras_control` | `tenant_finance` | Totales por paso para auditoría. |

## Endpoints

| Operación | Endpoint |
|---|---|
| Listar resultados TWM | `GET /three-way-match` |
| Detalle | `GET /three-way-match/{uuid}` |
| Ejecutar manual / forzar | Cronjob (no expuesto vía API directamente) |
| Ver ejecuciones | (consulta directa a `twm_ejecucion`) |

> El job principal corre vía cronjob en Kubernetes — ver kustomization de finanzas-api.

## Diagrama de flujo

```
                     ┌───────────────────────────┐
                     │   twm_ejecucion (corrida) │
                     └───────────┬───────────────┘
                                 │
                                 ▼
        ┌────────────────────────────────────────────┐
        │     Paso 1: Cargar candidatos              │
        │   - facturas pendientes (tenant_fiscal.invoice)
        │   - OCs (tenant_finance.purchase_order)    │
        │   - recepciones (tenant_finance.reception) │
        └─────────────────────┬──────────────────────┘
                              │
                              ▼
        ┌────────────────────────────────────────────┐
        │     Paso 2: Match OC ↔ Recepción ↔ Factura │
        │   - por vendor_number + order_number       │
        │   - por monto + cantidad                   │
        │   - por fechas                             │
        └─────────────────────┬──────────────────────┘
                              │
                              ▼
        ┌────────────────────────────────────────────┐
        │     Paso 3: Aplicar tolerancias            │
        │   - ¿Hay NC que compense diferencia?       │
        │   - ¿Margen de tolerancia configurado?     │
        └─────────────────────┬──────────────────────┘
                              │
            ┌─────────────────┴─────────────────┐
            ▼                                   ▼
   ✅ MATCH OK                       ❌ DISCREPANCIA
   → three_way_match.status=OK      → three_way_match.status=ERROR
   → autorizar pago                  → log en twm_logs
                                     → notificación / bloqueo
                              │
                              ▼
        ┌────────────────────────────────────────────┐
        │     Paso 4: Cifras de control              │
        │   - registros totales por paso             │
        │   - montos por paso                        │
        │   → twm_cifras_control                     │
        └────────────────────────────────────────────┘
```

## Reglas de negocio

- **OC + Recepción + Factura deben coincidir** en `vendor_number`, `order_number`, montos (con tolerancia configurable), fechas.
- Si la **factura excede** la OC: posible discrepancia → puede requerir NC para compensar.
- Si **falta recepción**: imposible pagar — la mercancía no llegó.
- Si **falta OC**: factura sin sustento — rechazar o tratar caso especial.
- El status del `three_way_match` determina si SAP recibe la cuenta por pagar (`accounts_payable.sent_flag`).

## Riesgos / consideraciones

- **Sin FK declaradas**: el cruce depende de campos de negocio (`vendor_number`, `order_number`) — un cambio de nomenclatura entre sistemas rompería el match.
- **Múltiples ejecuciones** del job pueden generar registros duplicados en `three_way_match`. Validar idempotencia.
- **`twm_logs` con severidad alta** debería disparar alertas (no validado si existe).
- El job vive en finanzas-api como módulo aparte ([STM-1524](../../jiras/STM-1524/)).

## JIRAs relacionados

- [STM-1524](../../jiras/STM-1524/) — filtro seguridad por vendor en Three Way Match.
- [STM-333](../../jiras/STM-333/) — modelo entidad relación administración financiera.

## Cómo responder al equipo

> *"¿Dónde veo si la factura pasó conciliación?"*
> → `tenant_finance.three_way_match` filtrado por `vendor_number` + `invoice_uuid`. O endpoint `GET /three-way-match?invoiceUuid=...`.

> *"¿Cuándo corrió por última vez el TWM?"*
> → `SELECT MAX(fechainicio) FROM tenant_finance.twm_ejecucion WHERE estado='OK';`

> *"¿Por qué esta factura no pasó match?"*
> → Buscar en `twm_logs` por `id_ejecucion` + `vendor_number` con severidad ERROR.
