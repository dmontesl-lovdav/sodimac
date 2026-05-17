# Wiki Sodimac B2B Portal — Negocio

> Punto único de entrada para consultas de negocio: "¿dónde guardo X?", "¿para qué sirve esta tabla?", "¿cómo fluye este proceso?".
> Última actualización: 2026-05-15.

## Cómo usar esta wiki

| Si tu pregunta es... | Ve a |
|---|---|
| "¿Cuál es la diferencia entre A y B?" / "¿Qué significa este término?" | [Conceptos clave](conceptos-clave.md) |
| "¿Para qué sirve la tabla X?" | [Glosario](glosario.md) |
| "¿Dónde guardo X dato / qué tabla uso?" | [Dónde vive qué](donde-vive-que.md) |
| "¿Cómo funciona el proceso Y?" | [Procesos](#procesos) |
| "¿Qué tablas hay en el módulo Z?" | [Módulos](#módulos) |
| "¿Cómo implementaron STM-XXX?" | [docs/jiras/](../jiras/) (técnico, fuera de wiki) |

## Módulos

| Schema BD | Doc | Tablas | Responsabilidad |
|---|---|---|---|
| `tenant_fiscal` | [Módulo Fiscal](modulos/fiscal.md) | 22 | CFDI ante SAT (factura, NC, REP, addenda, impuestos) |
| `tenant_finance` | [Módulo Finanzas](modulos/finanzas.md) | 30 | Operación con proveedor (OC, recepción, pago, descuento, 3-way-match, estado de cuenta) |

## Procesos (vista de negocio)

> Cada proceso describe qué pasa, qué tablas tocan, qué endpoints intervienen. Sin código.

| # | Proceso | Estado |
|---|---|---|
| 01 | [Pago al proveedor](procesos/06-pago-proveedor.md) | ✅ |
| 02 | [Descuento comercial (rebate)](procesos/08-descuento-comercial.md) | ✅ |
| 03 | [Facturación CFDI](procesos/04-facturacion-cfdi.md) | ✅ |
| 04 | [Three Way Match (conciliación)](procesos/05-three-way-match.md) | ✅ |
| — | Alta proveedor | 📝 pendiente |
| — | Orden de compra | 📝 pendiente |
| — | Recepción | 📝 pendiente |
| — | Complemento de pago (REP) | 📝 pendiente |
| — | Nota de crédito | 📝 pendiente |
| — | Estado de cuenta | 📝 pendiente |
| — | Bloqueo de proveedor | 📝 pendiente |
| — | Addenda | 📝 pendiente |

Procesos pendientes se llenan a demanda — cuando aparezca la pregunta.

## Convenciones

- **Una pieza de info vive en un solo sitio.** Glosario describe; procesos enlazan; módulos agrupan.
- **Cross-links explícitos**: `[nombre](ruta)` para que sean clickables en cualquier render.
- **Naming engañoso siempre marcado con ⚠** — ej. `fiscal_payments` vive en finanzas, no en fiscal.
- **Lo no-negocio NO va aquí**: bugs, refactors, decisiones técnicas → `docs/analisis/` o `docs/jiras/`.

## Reglas de oro para resolver dudas

1. **¿Pago a proveedor?** Es operativo → `tenant_finance.fiscal_payments` (legacy) o `payment_header`+`payment_detail` (nuevo). NO es CFDI.
2. **¿CFDI Complemento de Pago (REP)?** Es fiscal → `tenant_fiscal.payments` + `payment` + `totals` + `related_documents`.
3. **¿Descuento comercial?** Operativo en `tenant_finance.rebate`. Si está timbrado, también vive `stamped_rebate` y referencia CFDI de NC en fiscal.
4. **¿Una "factura"?** Siempre `tenant_fiscal.invoice` con `document_type`:
   - `I` = Ingreso (factura)
   - `E` = Egreso (nota de crédito)
   - `P` = Pago (complemento de pago)
5. **Ninguna tabla tiene FK declarada en BD** — la integridad la pone la app.

## Mantenimiento

Cuando alguien pregunte algo nuevo:

1. Si la respuesta no está en wiki → **agregar entrada al glosario o crear/extender proceso**
2. Linkear desde el lugar correcto (módulos / procesos / dónde-vive-qué)
3. Actualizar este README si se agregó proceso nuevo

La wiki **crece reactivamente** — solo se agrega lo que alguien preguntó. Evita sobre-documentar.

## Referencias técnicas (fuera de wiki)

- [docs/analisis/](../analisis/) — análisis técnicos, refactors, hallazgos de bugs
- [docs/arquitectura/](../arquitectura/) — diagramas, modelos ER, decisiones de arq
- [docs/jiras/](../jiras/) — documentación por ticket (implementaciones)
- [docs/BASE-DE-DATOS.md](../BASE-DE-DATOS.md) — conexión y credenciales
- [docs/ENTORNO-LOCAL.md](../ENTORNO-LOCAL.md) — levantamiento local
