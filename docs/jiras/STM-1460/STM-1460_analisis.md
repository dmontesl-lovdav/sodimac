# STM-1460 — Análisis técnico (interno)

> Documento interno para implementación. No es respuesta de Jira.

## Resumen del jira

- **Módulo**: Pagos
- **Pide**: filtrar la información de pagos por atributos del usuario
- **Prioridad**: Alta | **Sprint**: 8-2027
- **Epic**: STM-1403

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.finanzas-api`

**¿Qué tabla/entity corresponde a "pagos" en este jira?**

`finanzas-api` tiene varios módulos que podrían interpretarse como "pagos":
| Módulo | Tabla | Entity | Jira hermano |
|--------|-------|--------|--------------|
| `finanzas-payment` (header+details) | `finanzas_payment_*` | `FinanzasPaymentHeader`, `FinanzasPayment` | candidato principal STM-1460 |
| `fiscal-payments` | `fiscal_payments` | `FiscalPayment` | sin jira aún (genérico) |
| `account-statement` | (estado de cuenta) | — | STM-321 ✓ cubierto |

**Asunción**: STM-1460 cubre **`finanzas-payment`** (header + details). El módulo `fiscal-payments` lo deja sin filtro o requiere otro jira aparte.

**Endpoints actuales** ([`finanzasPayments.routes.ts`](../../../APP03022-mrch.backend.somx.finanzas-api/src/routes/finanzasPayments.routes.ts)):
- `GET /finanzas-payment` — list paginado (controller.list)
- `POST /finanzas-payment/header-with-details` — alta combinada
- `POST /finanzas-payment` — alta simple
- `PATCH /finanzas-payment` — actualización
- `GET /finanzas-payment/header-with-details` — listar con detalles
- `GET /finanzas-payment/header-with-details/{paymentHeaderUuid}` — detalle

**Entities** (campo vendor confirmado):
- `FinanzasPayment.entities.ts` → columna `vendor_number INT NOT NULL`
- `FinanzasPaymentHeader.entity.ts` → columna `vendor_number INT NOT NULL`

**Estado del filtro**:
- `finanzasPayment.controller.ts` → **NO usa `req.security`**
- `finanzasPayment.service.ts` → **NO usa `allowedVendors`**
- `FinanzasPayment.repo.ts`, `FinanzasPaymentHeader.repo.ts` → **NO filtran por vendor a partir de headers**

✗ **Trabajo nuevo confirmado.**

## Hallazgos

### Patrón aplicable
Idéntico a STM-1421 (rebates). Ver [`STM-1421_analisis.md`](../STM-1421/STM-1421_analisis.md).

### Archivos a modificar

| Archivo | Cambio |
|---------|--------|
| `src/controllers/finanzasPayment.controller.ts` | Leer `req.security.vendors`, WRN7029 si vacío, pasar `allowedVendors` al service en TODOS los métodos `list`, `listHeaderWithDetails`, `getHeaderWithDetailsByUuid` |
| `src/services/finanzasPayment.service.ts` | Aceptar `allowedVendors` y propagar a ambos repos |
| `src/repositories/FinanzasPayment.repo.ts` | Filtros por `vendor_number IN (...)` en queries de lectura |
| `src/repositories/FinanzasPaymentHeader.repo.ts` | Mismo filtro en queries de lectura |

### Endpoints afectados (lectura)
- `GET /finanzas-payment` (controller.list)
- `GET /finanzas-payment/header-with-details`
- `GET /finanzas-payment/header-with-details/{paymentHeaderUuid}` — caso especial: si la cabecera tiene `vendor_number` NO permitido → 404 (no exponer existencia).

Endpoints de escritura (POST/PATCH): el jira dice "filtrado de información". Default: no aplicar restricción en escritura. Confirmar con Ivan si requieren validación adicional ("no permitir crear pago para vendor X si user no tiene X").

### Riesgo: `header-with-details` con detalles
Si el header tiene vendor permitido pero algún detail referencia otro vendor (poco probable por modelo pero verificar), decidir si:
- (a) Filtrar también detalles por vendor.
- (b) Solo filtrar a nivel header (que es el dueño del vendor).

Recomiendo (b) — la lógica de "ver el pago" se decide por el header.

## Propuesta

### Implementación
1. Patrón clásico — extraer `sec = req.security` en controller, WRN7029 si vacío, propagar a repo.
2. Repo: `andWhere('header.vendor_number IN (:...allowedVendors)', { allowedVendors })`.
3. Detalle por UUID: cargar header, validar que `header.vendor_number IN allowedVendors`. Si no → 404.
4. Mantener middleware `activityLogger("FinanzasPayment")` para no romper bitácora.

### Tests
- Admin (null) → ve todos los pagos.
- Single vendor → ve solo los suyos.
- Multi-vendor (OR) → ve unión.
- Vacío → WRN7029.
- GET detalle de pago de otro vendor → 404.

## Dudas para Ivan

- **¿Filtrar POST/PATCH/DELETE?** O solo lectura (default).
- **¿"Pagos" en el jira incluye `fiscal-payments` aparte?** Si sí, agregar filtro al otro módulo o levantar jira hermano.
- **¿Caso especial endpoints `/header-with-details` con detalles cruzados?** Confirmar si pueden tener vendors mixtos.

## Estimación
- **Implementación**: 2-3 horas (patrón idéntico a STM-1421, pero 2 repos).
- **Tests + QA**: 1-2 horas.
- Total: ~4-5 horas. Si jira reporta SP, ~3 SP consistente.

## Referencias
- Patrón: STM-1421 y STM-321 (ambos en finanzas-api).
- Endpoints: [`finanzasPayments.routes.ts`](../../../APP03022-mrch.backend.somx.finanzas-api/src/routes/finanzasPayments.routes.ts)
- Memoria epic: [[project_stm1403_epic]] + [[project_security_headers_semantics]]
