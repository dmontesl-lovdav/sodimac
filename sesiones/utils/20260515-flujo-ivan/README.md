# Flujo demo para Ivan — Pago + Descuento Comercial

> Ejecutado y validado en local 2026-05-15. Listo para reproducir en UAT (PC Sodimac).

## Escenario de negocio

- **Proveedor**: vendor 34786 (Distribuidora Mexicana)
- **OC**: PO-2026-001234 por $116,000 MXN
- **Factura**: FAC-SOD-A-12345
- **Acuerdo Q2-2026**: 5% bonificación = $5,800 descuento
- **Pago neto**: $110,200 ($116,000 − $5,800)

## Archivos

| Archivo | Propósito |
|---|---|
| `body-pago.json` | Payload POST /fiscal-payments |
| `body-stamped-rebate.json` | Payload POST /stamped-rebates (prerequisito FK) |
| `body-rebate.json` | Payload POST /rebates |
| `curls.cmd` | Script Windows con los 4 pasos (3 POSTs + GET) |
| `queries-validacion.sql` | Queries BD para validar inserts |

## Pasos

1. **Pago**: `POST /fiscal-payments` con `body-pago.json` → 201 + `fiscalPaymentUuid`
2. **Stamped rebate**: `POST /stamped-rebates` con `body-stamped-rebate.json` → 201 + `stampedRebateUuid`. Necesario por FK lógica `rebate.document_number → stamped_rebate.document_number`.
3. **Descuento**: `POST /rebates` con `body-rebate.json` → 201 + `rebateId`
4. **Verificación**: `GET /rebates?pageNumber=1&pageSize=10` → 200 + array que incluye el rebate creado

## Resultado local (validación previa)

| Paso | HTTP | UUID generado |
|---|---|---|
| POST /fiscal-payments | 201 | `87f62e3c-f02e-4055-836f-f5ec3a87a298` |
| POST /stamped-rebates | 201 | `424ce174-4a61-4c93-ab9d-ee3461e9ff34` |
| POST /rebates | 201 | `d72977b1-c191-47c5-8d0d-8d38aa79233d` |
| GET /rebates | 200 | retorna 8 rebates incluyendo `REB-2026-Q2-VOL-001` ✅ |

## ⚠ Atención sobre `/rebates` en UAT

Tiene bug de mapeo entity/schema (props TS `supplierNumber`/`documentReference`/`originId` no matchean BD `vendor_number`/`reference_number`/`source`). El fix está en local sin deployar a UAT.

**Si en UAT el paso 3 retorna**:
```
HTTP 400
{"code":"23502","driver":"Failing row contains ... null ..."}
```

→ Es el bug. Opciones:
1. Esperar push del fix a develop + deploy UAT
2. Usar `POST /rebates/relate` (requiere XML de NC, también tiene el bug)
3. Insertar rebate vía SQL directo en UAT (no recomendado)

## Cómo correr

Desde PC Sodimac:
```cmd
cd <ruta-a-este-folder>
curls.cmd
```

Para validar BD UAT:
```
psql -h <host-uat> -U <user> -d <db> -f queries-validacion.sql
```

(O abre las queries en DBeaver/pgAdmin y córrelas manualmente.)

## Cleanup opcional

Si quieres dejar BD limpia tras la demo, descomenta los DELETEs al final de `queries-validacion.sql`.
