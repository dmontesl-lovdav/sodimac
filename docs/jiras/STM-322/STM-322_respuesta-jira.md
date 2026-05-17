# STM-322 — Retro para pegar en Jira

## Resumen

**El filtro de seguridad por atributos de usuario sobre complementos de pago YA está implementado** en `fiscal-api` desde el commit `a311b59` (etiquetado como STM-1474 pero el código corresponde a complementos de pago). No requiere desarrollo nuevo.

Este ticket queda cubierto por el código existente y las reglas del epic STM-1403.

## Evidencia técnica

**Repositorio**: `APP03022-mrch.backend.somx.fiscal-api`

**Tabla involucrada**: `payments` (entity `PaymentsEntity`), con campo `fiscal_uuid` correspondiente al TimbreFiscalDigital del SAT. Es la tabla de complementos de pago CFDI tipo P.

**Endpoint expuesto**: el flujo de búsqueda de complementos de pago en `PaymentRegistrationController` (o el controller equivalente) ya:

1. **Lee el header anti-spoof `x-user-vendors`** inyectado por el BFF tras consultar `util-api /api/security/user-attributes-by-key/{sub}`.
2. **Maneja `WRN7029`** cuando el usuario no tiene atributos configurados.
3. **Pasa `allowedVendors` al service `PaymentQueryServiceImpl.searchPayments`**.
4. **El service propaga a `PaymentsRepositoryCustomImpl.searchPayments`** que aplica `addVendorPredicate` con `vendor_number IN (:allowedVendors)`.

**Cumplimiento de reglas del jira**:

| Regla | Cobertura |
|-------|-----------|
| 1. Filtrado por atributos del usuario | ✓ vía `allowedVendors` |
| 2. Seguridad mediante token | ✓ JWT decodificado en BFF + headers anti-spoof inyectados |
| 3. Atributos para filtrado (Proveedor / TipoProveedor / GrupoProveedor) | ✓ Proveedor via `vendor_number`. Tipo/Grupo se manejan en el BFF al construir headers (ATR001/ATR002/ATR004). |
| 4. Uso del catálogo de catálogos | ✓ util-api consulta `core_security.user_attribute` y resuelve los catálogos |
| 5. Valor `-1` (wildcard admin) | ✓ `parseVendorHeader` retorna `null` → no aplica filtro |
| 6. Múltiples atributos (OR lógico) | ✓ cláusula SQL `IN (...)` |

## Cómo validar (QA)

Llamar al endpoint de búsqueda de complementos de pago con distintos headers `x-user-vendors`:

| Caso | Header | Resultado esperado |
|------|--------|--------------------|
| Admin | `x-user-vendors: -1` o ausente | Lista completa de complementos |
| Restringido a 1 vendor | `x-user-vendors: 11111` | Solo complementos del vendor 11111 |
| Restringido OR | `x-user-vendors: 11111,22222` | Complementos de vendor 11111 o 22222 |
| Usuario sin atributos | `x-user-vendors:` (vacío) | 400 con `code=WRN7029` |

## Acción solicitada

- [ ] QA valida los casos arriba en UAT.
- [ ] Si OK, mover ticket a `Done` con esta justificación.
- [ ] Si surge un endpoint adicional no cubierto, reabrir y especificar.

## Referencias

- Commit que implementó el filtro: `a311b59 fix: STM-1474 convertir allowedVendors a BigDecimal para comparacion con supplier_number numeric`
- Epic STM-1403 (patrón de seguridad por atributo de usuario)
- Jira hermano STM-1474 (recepciones) — comparte la misma infraestructura
