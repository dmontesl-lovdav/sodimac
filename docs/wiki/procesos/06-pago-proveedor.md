# Proceso: Pago al proveedor

> Cómo Sodimac registra el pago a un proveedor.
> Este proceso es **operativo**, NO fiscal. El CFDI Complemento de Pago (REP) que documenta el pago ante SAT es un proceso aparte: ver [Complemento de Pago (REP)](07-complemento-pago-rep.md) (pendiente).

## Qué pasa (vista de negocio)

1. El proveedor emitió factura(s) que Sodimac validó (3-way-match OK).
2. Llega la fecha de pago según el calendario / `payment_term`.
3. Tesorería ejecuta el pago vía transferencia bancaria / SPEI.
4. El sistema **registra el pago en el portal** → tabla operativa de pagos.
5. Posteriormente el proveedor emite un **CFDI Complemento de Pago (REP)** que confirma fiscalmente el pago — entra por flujo aparte y vive en `tenant_fiscal`.

## Tablas involucradas

### Pago operativo

| Tabla | Schema | Rol |
|---|---|---|
| `fiscal_payments` ⚠ legacy | `tenant_finance` | Registro single-row del pago. Naming engañoso. |
| `payment_header` (refactor en curso) | `tenant_finance` | Cabecera del pago (vendor, año, total, fecha). |
| `payment_detail` (refactor en curso) | `tenant_finance` | Detalle por documento pagado (1 header → N details). |

### Contexto que se consulta al pagar

| Tabla | Schema | Para qué |
|---|---|---|
| `vendor_block` | `tenant_finance` | Validar que el proveedor NO esté bloqueado. |
| `accounts_payable` | `tenant_finance` | Verificar la cuenta por pagar contable. |
| `three_way_match` | `tenant_finance` | Verificar que pasó la conciliación. |
| `purchase_order` | `tenant_finance` | OC origen del adeudo. |

## Endpoints

| Operación | Endpoint | Estado |
|---|---|---|
| Alta de pago (legacy) | `POST /fiscal-payments` (BFF `/ppsomx/backend-finanzas/fiscal-payments`) | ✅ funcional UAT |
| Alta de pago (refactor) | `POST /finanzas-payment` | Verificar disponibilidad en UAT |
| Consulta de pago | `GET /fiscal-payments/{uuid}` o `GET /fiscal-payments` | ✅ |
| Listar por filtros | `GET /fiscal-payments?vendorNumber=X&from=...&to=...` | ✅ |

## Campos clave del payload (POST /fiscal-payments)

```
paymentNumber    string  identificador único de negocio
company          int     empresa Sodimac que paga
documentNumber   string  documento referenciado
referenceNumber  string  referencia adicional
vendorNumber     int     proveedor
amount           decimal monto del pago
currency         string  moneda (MXN, USD, etc.)
documentType     string  tipo de documento (PP, etc.)
paymentDate      date    fecha del pago
status           int     0=inactivo, 1=activo, 2=procesado, 3=cancelado
```

## Diagrama de flujo

```
[Proveedor]                                          [Sodimac]
    │                                                    │
    │  emite factura                                     │
    │ ────────────────────────────────────────────────►  │
    │                                                    │
    │                              [Recepción / OC]      │
    │                              [Three Way Match]     │
    │                              ✓ Conciliación        │
    │                                                    │
    │                              [Vendor Block?]       │
    │                              ✓ No bloqueado        │
    │                                                    │
    │                              [Tesorería paga]      │
    │                              POST /fiscal-payments │
    │                              → fiscal_payments     │
    │                                                    │
    │  emite CFDI REP                                    │
    │ ────────────────────────────────────────────────►  │
    │                              [fiscal-api recibe]   │
    │                              → tenant_fiscal.payments
    │                              → payment, totals,    │
    │                                related_documents   │
```

## Reglas de negocio

- **Un `payment_number` debe ser único** por empresa+vendor. Constraint UNIQUE en local; ⚠ falta en UAT.
- El pago operativo NO requiere CFDI REP previo. El REP puede llegar después.
- Si el proveedor está en `vendor_block` activo, el pago **no debería procesarse** (validación a nivel app, no BD).
- `payment_date` se guarda como `date` (sin zona). Discrepancias de timezone entre cliente y BD pueden producir drift de 1 día — validado: en UAT con `America/Mexico_City` no hay drift; en local con container UTC sí.

## Riesgos / consideraciones

- **Naming confuso**: `fiscal_payments` no es fiscal. Renombrar amerita migración mayor.
- **Refactor coexistiendo**: dos modelos (`fiscal_payments` vs `payment_header`+`detail`) viven en paralelo. Decidir cuál es el oficial por endpoint.
- **Sin FK declarada** a `vendor_block`, `accounts_payable` ni `purchase_order` — la validación es solo en app.

## JIRAs relacionados

- [STM-1460](../../jiras/STM-1460/) — filtro de seguridad por vendor en `finanzasPayment.*`.
- [STM-1403](../../jiras/STM-1403/) — epic de seguridad por atributo de usuario.

## Cómo responder a Ivan / equipo

> *"¿Endpoint para alta de pago?"*
> → `POST /fiscal-payments` (legacy, hoy funcional UAT). Pendiente confirmar si `/finanzas-payment` (refactor) ya está activo.
> Para verificar el registro: `GET /fiscal-payments/{uuid}` o consultar `tenant_finance.fiscal_payments`.
