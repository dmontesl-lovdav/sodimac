# STM-314 — Retro para pegar en Jira

## Resumen

**El filtro de seguridad por atributos de usuario sobre notas de crédito YA está implementado** en `fiscal-api` desde STM-323 (filtro de facturas). Las Notas de Crédito (CFDI tipo Egreso) viven en la misma tabla `invoice` que las facturas, discriminadas por `document_type='E'`. El filtro existente cubre ambos casos sin cambios adicionales.

Este ticket queda cubierto por el código existente.

## Evidencia técnica

**Repositorio**: `APP03022-mrch.backend.somx.fiscal-api`

**Modelo de datos**:
- Tabla `invoice` con campo `document_type` (1 char):
  - `'I'` = Factura
  - `'E'` = Nota de Crédito (Egreso)
  - `'T'` = Traslado

El `InvoiceController.search` documenta este comportamiento explícitamente:

```
* - Tipo de Documento (tipoDocumento): I=Factura, E=Nota de Crédito
```

**Flujo de filtrado**:

1. `InvoiceController.searchInvoices(searchRequest, xUserVendors)` recibe header `x-user-vendors` inyectado por el BFF.
2. `parseVendorHeader` interpreta el header (valor `-1` → admin, vacío → WRN7029, lista → restringido).
3. Si vendors está vacío → 400 con `WRN7029`.
4. `InvoiceService.searchInvoices(searchRequest, allowedVendors)` construye la `InvoiceSpecification`:
   - Aplica filtro `documentType = searchRequest.tipoDocumento.toUpperCase()` (cuando viene).
   - Aplica filtro `allowedVendors` via subquery a `AddendumEntity.supplierNumber`.
   - Ambos predicados se combinan con AND.

**Cumplimiento de reglas del jira**:

| Regla | Cobertura |
|-------|-----------|
| 1. Filtrado por atributos del usuario | ✓ vía `allowedVendors` propagado del header al `InvoiceSpecification` |
| 2. Seguridad mediante token | ✓ JWT decodificado en BFF, headers anti-spoof |
| 3. Atributos para filtrado (Proveedor / TipoProveedor / GrupoProveedor) | ✓ Proveedor via `supplierNumber` |
| 4. Uso del catálogo de catálogos | ✓ util-api consulta `core_security.user_attribute` |
| 5. Valor `-1` (wildcard admin) | ✓ `parseVendorHeader` retorna `null` → no aplica filtro |
| 6. Múltiples atributos (OR lógico) | ✓ `vendor_number IN (...)` SQL |

## Cómo validar (QA)

Llamar al endpoint de búsqueda de invoices con `tipoDocumento=E` y distintos headers `x-user-vendors`:

| Caso | Body | Header | Resultado esperado |
|------|------|--------|--------------------|
| Admin ve todas las NC | `{"tipoDocumento":"E", ...}` | `x-user-vendors: -1` | Lista completa de NC |
| Restringido a 1 vendor | `{"tipoDocumento":"E", ...}` | `x-user-vendors: 11111` | Solo NC del vendor 11111 |
| OR multi-vendor | `{"tipoDocumento":"E", ...}` | `x-user-vendors: 11111,22222` | NC de vendors 11111 o 22222 |
| Sin atributos | `{"tipoDocumento":"E", ...}` | `x-user-vendors:` (vacío) | 400 con `code=WRN7029` |
| Confirmar que factura sigue funcionando | `{"tipoDocumento":"I", ...}` | header válido | Facturas filtradas |

## Acción solicitada

- [ ] QA valida los casos arriba en UAT con `tipoDocumento=E`.
- [ ] Si OK, mover ticket a `Done` con esta justificación.
- [ ] Si UI o backend requieren un endpoint dedicado `/notas-credito` distinto del genérico `/invoices?tipoDocumento=E`, indicar para abrir trabajo nuevo (sería un wrapper, no lógica nueva).

## Referencias

- Filtro implementado en: `InvoiceSpecification.java` ([repository/specification/](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/repository/specification/InvoiceSpecification.java))
- Controller: `InvoiceController.java`
- Jira hermano que implementó este filtro: STM-323 (filtro de facturas)
- Epic STM-1403 (patrón de seguridad por atributo de usuario)
- DTOs relacionados a NC (sin lógica de seguridad, solo modelado): `NotaCreditoRelacionadaDto`, `NotaCreditoXlsxDto`
