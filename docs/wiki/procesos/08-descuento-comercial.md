# Proceso: Descuento comercial (Rebate)

> Cómo Sodimac registra y timbra descuentos comerciales a proveedores.

## Qué pasa (vista de negocio)

1. Sodimac acuerda un descuento con el proveedor (por volumen, exhibición, devolución, etc.).
2. **Alta operativa**: se registra el descuento en el sistema (monto, periodo, motivo).
3. Eventualmente el proveedor emite una **Nota de Crédito (NC)** que documenta fiscalmente el descuento.
4. **Timbrado**: el descuento se vincula con la NC (CFDI tipo E) en `tenant_fiscal.invoice`.

## Tablas involucradas

| Tabla | Schema | Rol |
|---|---|---|
| `rebate` | `tenant_finance` | Descuento operativo (pre-timbrado). |
| `stamped_rebate` | `tenant_finance` | Descuento ya timbrado fiscalmente con su `invoice_fiscal_uuid`. |
| `invoice` (`document_type='E'`) | `tenant_fiscal` | La Nota de Crédito CFDI que documenta el descuento. |
| `account_statement_discount` | `tenant_finance` | Aparece en el estado de cuenta mensual cuando aplica al período. |

## Endpoints

| Operación | Endpoint | Estado |
|---|---|---|
| Alta operativa pura | `POST /rebates` | ✅ (con fix 2026-05-15 — ver [análisis bug](../../analisis/MODULO_FINANZAS.md#bug-histórico-encontrado-en-rebate-2026-05-15)) |
| **Alta + timbrado con NC** (recomendado) | `POST /rebates/relate` (STM-973) | ✅ |
| Búsqueda con filtros | `POST /rebates/filter` (STM-875) | ✅ |
| Listar | `GET /rebates` | ✅ |
| Publicados (status=1) | `GET /rebates/published` | ✅ |
| Por proveedor | `GET /rebates/vendor/{vendorNumber}` | ✅ |
| Detalle | `GET /rebates/{uuid}` | ✅ |
| Export CSV | `GET /rebates/export/csv`, `GET /rebates/published/export/csv` | ✅ |

## Diagrama de flujo

```
[Sodimac] ↔ [Proveedor]

  1. Acuerdo comercial
  ───────────────────►

  2. Alta operativa
     POST /rebates  o  POST /rebates/relate
     → tenant_finance.rebate
     (si /relate: también tenant_finance.stamped_rebate)

  3. Proveedor emite NC
  ◄───────────────────
     → tenant_fiscal.invoice (document_type='E', fiscal_uuid del SAT)

  4. Vinculación NC ↔ Rebate
     stamped_rebate.invoice_fiscal_uuid ← invoice.fiscal_uuid

  5. Reflejo en estado de cuenta
     → tenant_finance.account_statement_discount
```

## Campos clave del payload (POST /rebates)

```
documentNumber   string  identificador del documento del descuento
referenceNumber  string  referencia comercial
sapDocument      string  documento SAP relacionado
vendorNumber     int     proveedor
amount           decimal monto del descuento
source           int     origen (catalogado, OriginCatalog)
periodId         int     periodo en formato YYYYMM
dueDate          date    fecha de vencimiento
postingDate      date    fecha contable
status           int     0=inactivo, 1=publicado, 2=procesado, 3=cancelado
```

## Campos clave (POST /rebates/relate)

```
rebateUuid           uuid  UUID del rebate previo (opcional, si ya existe)
invoiceFiscalUuid    uuid  UUID del CFDI NC del SAT
xmlContent           xml   XML de la NC para validación
numeroDocumento      string
referenciaDocumento  string
numeroProveedor      string (parseado a int)
usuario              string (parseado a int) — quien registra
```

Validaciones server-side:
- Tipo de documento debe ser **E** (NC/Egreso).
- Monto del rebate debe coincidir con monto de NC.
- RFC del proveedor debe coincidir con emisor de NC.

## Reglas de negocio

- **`POST /rebates` directo requiere que ya exista `stamped_rebate`** con el mismo `document_number` (FK lógica). Por eso el canal recomendado es `POST /rebates/relate` que crea ambos.
- El `source` (origen) viene de catálogo `origin_catalog` — valores numéricos representan tipo de origen del descuento.
- Un descuento timbrado **no debería ser modificable** — está atado a un CFDI con UUID fiscal.
- El descuento timbrado aparece en `account_statement_discount` cuando se genera el estado de cuenta del período.

## Riesgos / consideraciones

- **Bug histórico fixeado 2026-05-15**: el entity TypeORM tenía nombres distintos al contrato HTTP. Pre-fix, `POST /rebates` guardaba NULL silencioso en `vendor_number`, `reference_number`, `source` y fallaba con `NOT NULL` constraint.
- **FK lógica invertida**: el diseño obliga a `stamped_rebate` antes de `rebate`. Operativamente debería ser al revés. Probable error de modelo, pero **NO tocar sin alineación con equipo**.
- **`POST /rebates` no valida duplicados a nivel app** — confía en UNIQUE constraint de BD. Verificar que UAT tenga el constraint aplicado.

## JIRAs relacionados

- [STM-973](../../jiras/STM-973/) — endpoint `POST /rebates/relate` (relación con NC).
- [STM-875](../../jiras/STM-875/) — endpoint `POST /rebates/filter` (búsqueda con filtros).
- [STM-1421](../../jiras/STM-1421/) — filtro de seguridad por vendor en rebates (epic STM-1403).

## Cómo responder a Ivan / equipo

> *"¿Endpoint para alta de descuento comercial?"*
> → Depende del caso:
> - **Si el descuento aún NO tiene NC timbrada**: `POST /rebates` (alta operativa).
> - **Si ya hay NC del proveedor**: `POST /rebates/relate` (crea rebate + stamped_rebate y vincula NC). **Recomendado**.
>
> Para verificar: `GET /rebates/{uuid}` o consultar `tenant_finance.rebate` directo.
