# Conceptos clave de negocio

> Explicaciones funcionales para entender el dominio. Aquí van pares/grupos de conceptos que se confunden o requieren contexto de negocio.

## Pago a proveedor vs Descuento comercial (Rebate)

### Pago a proveedor

**Qué es**: la **erogación monetaria** que Sodimac hace a un proveedor para liquidar facturas pendientes (cuentas por pagar).

**Características**:
- Es **dinero que sale** de Sodimac hacia el proveedor.
- Liquida una o varias facturas previamente recibidas y conciliadas (3-way-match).
- Se ejecuta vía transferencia bancaria, SPEI, cheque, etc.
- Tiene calendario según `payment_term` del proveedor.
- Posteriormente el proveedor emite un **CFDI Complemento de Pago (REP)** que documenta fiscalmente el pago ante SAT.

**Sinónimos**: pago operativo, liquidación, payment.

**Tabla**: `tenant_finance.fiscal_payments` ⚠ naming engañoso, vive en finanzas.

### Descuento comercial (rebate)

**Qué es**: una **reducción del adeudo** que Sodimac aplica al proveedor por acuerdos comerciales pactados.

**Características**:
- NO es dinero que sale — es un **ajuste contra lo que Sodimac le debe** al proveedor.
- Se origina por **acuerdos comerciales**: volumen de compra, exhibición, devoluciones, bonificaciones, publicidad cooperativa, etc.
- El proveedor lo **acepta** y eventualmente emite una **Nota de Crédito (NC)** que documenta fiscalmente el descuento ante SAT.
- **Reduce el monto a pagar** en la próxima liquidación.

**Sinónimos**: rebate, bonificación, ajuste comercial, descuento por volumen.

**Tabla**: `tenant_finance.rebate` (+ `tenant_finance.stamped_rebate` cuando se timbra).

### ¿Qué significa "rebate"?

Término en inglés del retail/CPG. **No tiene traducción exacta de una palabra al español** — por eso el código usa rebate y el ER en español dice "Descuento Comercial".

Tres acepciones según contexto:
- **Retail/B2B (este caso)**: descuento condicional que el proveedor da al comprador (Sodimac) por cumplir un acuerdo (volumen, plazo, etc.). Se aplica **después** de la facturación original.
- **Consumer**: reembolso al consumidor final (cashback).
- **Tax**: devolución fiscal.

En Sodimac aplica la primera. Equivalente más cercano: **"bonificación comercial"** o **"descuento por acuerdo"**.

### ¿Por qué se guardan por separado?

Son **dos hechos contables y fiscales distintos**:

| Aspecto | Pago | Descuento comercial |
|---|---|---|
| **Naturaleza** | Erogación (dinero sale) | Ajuste de adeudo (sin movimiento de dinero directo) |
| **Documento fiscal del proveedor** | CFDI Complemento de Pago (`document_type='P'`) | Nota de Crédito (`document_type='E'`) |
| **Cuándo ocurre** | Al liquidar facturas vencidas | Cuando se cumple el acuerdo comercial |
| **Impacto SAP** | Disminuye saldo bancario + aplica a facturas | Reduce cuenta por pagar |
| **Origen** | Tesorería ejecuta | Comercial pacta + Finanzas valida |
| **Vendor mete dato?** | El proveedor emite REP después | El proveedor emite NC |

Si los pusieras en la misma tabla:
- Mezclarías eventos contables diferentes.
- Confundirías auditoría.
- Romperías la trazabilidad CFDI (REP ≠ NC para SAT).
- Distintos campos críticos (banco/cuenta para pago, periodo/origen para rebate).

### ¿Hay endpoint que guarde ambos?

**No, y no debería haberlo** en el caso típico.

Razones:
1. **No se pactan al mismo tiempo**: el descuento se acuerda meses antes del pago.
2. **No se ejecutan al mismo tiempo**: el descuento se aplica al saldo, el pago se ejecuta en su fecha.
3. **Documentos fiscales distintos**: cada uno disparará un CFDI separado del proveedor.
4. **Responsables organizacionales distintos**: comercial maneja descuentos, tesorería maneja pagos.

**Cuándo sí se relacionan**:
- En el **estado de cuenta** del proveedor — ambos aparecen como movimientos del período en tablas separadas (`account_statement_payment` y `account_statement_discount`).
- En el **Three Way Match** — un descuento puede compensar una diferencia OC↔Factura sin requerir devolución física.

### Ejemplo concreto

```
Día 01: Sodimac emite OC de $100,000 al proveedor X
Día 05: Proveedor X entrega mercancía + factura ($100,000)
Día 10: 3-way-match: OK
Día 30: Por acuerdo de volumen, proveedor X otorga 5% bonificación
        → Proveedor X emite NC de $5,000
        → POST /rebates/relate  (registra el descuento + NC)
        → tenant_finance.rebate ($5,000) + stamped_rebate vinculado a NC
Día 45: Llega fecha de pago
        → Sodimac paga $95,000 ($100,000 factura − $5,000 NC)
        → POST /fiscal-payments  (registra el pago)
        → tenant_finance.fiscal_payments ($95,000)
Día 50: Proveedor X emite CFDI REP por los $95,000
        → tenant_fiscal.payments + payment + related_documents (vincula factura original)
```

Tres hechos, tres tablas, tres CFDIs. Por eso son endpoints separados.

### Referencias

- [Proceso: Pago al proveedor](procesos/06-pago-proveedor.md)
- [Proceso: Descuento comercial](procesos/08-descuento-comercial.md)
- [Glosario completo](glosario.md)
