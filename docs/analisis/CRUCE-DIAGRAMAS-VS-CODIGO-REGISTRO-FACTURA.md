# Cruce diagramas draw.io vs código — registro de factura

> Fuente: `sesiones/diagramas/` (Ivan, 2026-06-16): `DiagramaFlujoFactura`, `DiagramaFlujoFiscal`, `DiagramaFlujoDetalleFactura`.
> Contra: `fiscal-api` `InvoiceServiceImpl.registerInvoice` + servicios relacionados.
> Última actualización: 2026-06-16.

## ✅ Reflejado en código (diagrama ↔ register)

| Paso diagrama | Código |
|---|---|
| Valida tipo documento / ¿es factura? | PASO 2 — `BUS060` si no es I/E |
| Estructura XML válida | PASO 3 (`cfdiProcessor`) + `DiagramaFlujoDetalleFactura` |
| Versión vigente del documento | PASO 4 `validateCfdiVersion` (`BUS021`) |
| RFC receptor válido/autorizado | PASO 5 `validateAuthorizedReceiver` (`BUS008`) |
| ¿Existe serie+folio registrada? | PASO 3.1 `validateSeriesAndFolio` + 6.1 (`WRN7012/7013`) |
| ¿Existe UUID registrado? | PASO 6.2 `validateNoDuplicateByUuid` (`WRN7014`) |
| Rango tolerancia factura vs recepción | PASO 7 `validateImporteTolerance` |
| Estatus "3 - Recibida" | PASO 9 `invoice.setStatus(3)` |
| ¿Existe PDF? Guarda PDF | PASO 9.5 (GCS) |
| NC: relación con factura previa | `saveRelatedCfdis` (`BUS042/043/044`) |

## ⚠️ En el diagrama PERO NO en código (GAPS)

| # | Gap | Diagrama | Estado código | Acción |
|---|---|---|---|---|
| 1 | **Validación fiscal / PAC** | Todo `DiagramaFlujoFiscal`: estructura fiscal PAC, estatus timbrado, "¿Validación de timbre?", catalogar errores PAC | PASO 8 **COMENTADO** (`// validateWithSat`) | Definir si se integra PAC o el diagrama excede alcance actual |
| 2 | **Impuestos** | "Valida impuestos permitidos Sodimac" + "Valida monto impuestos registrados vs calculados" | Solo **guarda** impuestos (línea ~946); `BUS040/BUS041` en enum pero **no se llaman** | Implementar validación impuestos |
| 3 | **RFC factura vs Recepción** | "¿Valida RFC de la factura vs Recepción?" | Valida RFC receptor (catálogo), **no compara** RFC/proveedor factura vs recepción | Confirmar regla + implementar |
| 4 | ~~**Fuera de tolerancia (por dirección)**~~ | factura > recepción → 2 Parcial + NC; factura < recepción → 1 Rechazo Comercial | ✅ **RESUELTO** 2026-06-19 — `validateImporteTolerance` retorna `ToleranceResult{status,warning}`: >rec → status 2 + `WRN7030`; <rec → status 1 + `WRN7031`; dentro → 3. Diagrama actualizado Ivan 2026-06-18. Probado E2E local | — |
| 5 | **Recepción → "1 Consumida" automático** | Al registrar factura con addenda OK | fiscal-api **no toca** recepción (solo lee). Manual (finanzas) la deja en 2 | Definir: batch o implementar en registro |
| 6 | ~~**NC mayor a la factura**~~ | "¿La NC es mayor al monto factura? → Rechazo NC" | ✅ **RESUELTO** 2026-06-17 — `saveRelatedCfdis` compara total NC vs total factura relacionada; `BUS061` si NC > factura. Probado E2E local | — |
| 7 | **Perfil / proveedor asignado** | "¿Perfil de Proveedor? / proveedor asignado al perfil" | Filtro de seguridad (headers x-user) — vive en util-api/BFF, no en fiscal register | Verificar que esa capa lo cubra |

## ➕ En código pero NO en diagrama (QA junio-2026, posterior al diagrama)

- Bloqueo tipo/proveedor (`BUS2028/BUS2029`) — PASO 6.3/6.4
- NC forma de pago / uso CFDI (`BUS058/BUS059`) — PASO 6.5
- Monto NC ≤ factura (`BUS061`) — en `saveRelatedCfdis`

→ Agregar estos nodos al diagrama para que quede al día.

## 🐛 Fixes colaterales (2026-06-17)

- **Errores de negocio NC enmascarados como `ERR003`:** `saveInvoiceToDatabase` atrapaba toda excepción y la reenvolvía como `ERR003` técnico. Los códigos `BUS042/043/044/061` se perdían. Fix: el catch deja pasar `FiscalException` con su código original.
- **Partial-persist en errores post-save:** `registerInvoice` atrapa la excepción y retorna un response de error, por lo que Spring **commiteaba** la transacción dejando la NC persistida con relación inválida. Fix: helper `markRollbackOnly()` marca la transacción rollback-only en ambos catch. Verificado: NC fuera de regla ya no queda en BD.

## Prioridad sugerida (a confirmar con Ivan)

1. **Tolerancia → parcial (#4)** y **recepción → 1 (#5)** — ya en discusión, decisiones tomadas/pendientes.
2. **Impuestos (#2)** y **NC mayor a factura (#6)** — validaciones de negocio faltantes, alcance acotado.
3. **RFC factura vs recepción (#3)** — confirmar regla exacta.
4. **Validación fiscal/PAC (#1)** — la más grande; definir si entra en este alcance o es fase aparte.
5. **Perfil/proveedor (#7)** — verificar capa de seguridad (probablemente ya cubierto fuera de fiscal).

## Decisiones Ivan ya tomadas (2026-06-16)
- Estatus 3 = **"Recibida"** (corregir diagrama que decía "Proceso de envío"). Sin cambio de código.
- Fuera de tolerancia → **"2 - Recibido Parcial"** (implica cambio #4).
- Redacción estatus 2: "Recibido Parcial".

Ver detalle de trenes en [TRENES-ESTATUS-REGISTRO-FACTURA.md](TRENES-ESTATUS-REGISTRO-FACTURA.md).
