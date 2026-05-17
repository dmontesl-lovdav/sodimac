# STM-314 — Análisis técnico (interno)

> Documento interno para implementación. No es respuesta de Jira.

## Resumen del jira

- **Módulo**: Notas de Crédito (CFDI tipo Egreso)
- **Pide**: filtrar la información de notas de crédito por atributos del usuario autenticado
- **Prioridad**: Media
- **Epic**: STM-1403

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.fiscal-api`

**Modelo de datos clave**: las Notas de Crédito **NO tienen tabla aparte**. Viven en la tabla `invoice` (entity `InvoiceEntity`) discriminadas por el campo `document_type`:
- `document_type='I'` → Ingreso (factura)
- `document_type='E'` → Egreso (Nota de Crédito) ← lo que pide STM-314
- `document_type='P'` → Pago (complemento) — manejado en otra tabla `payments`
- `document_type='T'` → Traslado

**Filtro ya implementado**:
- `InvoiceSpecification.buildSpecification(searchRequest, allowedVendors)` aplica:
  1. Filtro `documentType` cuando viene en `searchRequest.getTipoDocumento()` (uppercase, ej. `"E"`).
  2. Filtro `allowedVendors` via subquery a `AddendumEntity.supplierNumber` (STM-323 ya merged).
  3. Ambos filtros se combinan con AND.

**Servicio**:
- `InvoiceServiceImpl` consume `InvoiceSpecification` y recibe `allowedVendors` desde el controller (que lo obtiene del `SecurityContextFilter`).

**Headers**:
- `SecurityContextFilter` + `JwtTokenInterceptor` + `UtilApiSecurityClient` ya inyectan `x-user-vendors` desde util-api.

## Hallazgos

### ¿Funcionalidad ya existe?
**Sí.** Mismo `InvoiceSpecification` que cubre STM-323 (facturas) cubre STM-314 (NC) sin cambios adicionales:
- Cliente llama `GET /invoices?tipoDocumento=E` (o el parámetro equivalente).
- `InvoiceSpecification` filtra por `documentType='E'` AND `vendor IN allowedVendors`.
- Mismo flujo, mismo entity, mismo repo.

### ¿STM-314 es duplicado o redundante?
**Funcionalmente sí.** El epic STM-1403 fue particionado por "módulo del negocio" (NC, facturas, pagos, complemento...) pero el código backend los maneja en la misma tabla. Es posible que la partición vino desde requirements del producto/UI sin conocimiento del modelo de datos.

### Trabajo real pendiente
1. **Verificar QA** — correr regresión con `tipoDocumento=E` y usuario con vendors específicos, confirmar que solo retorna NC permitidas.
2. **Documentar** explícitamente en respuesta-jira que STM-314 está cubierto por STM-323 a nivel código.
3. **Confirmar con Ivan** si hay endpoint dedicado "/notas-credito" que él imagina. Hoy no existe (no se encontró en controllers). Si insiste, sería un wrapper/alias del endpoint genérico — discutir si vale el esfuerzo.

## Propuesta

### Camino corto (recomendado)
1. Marcar STM-314 como cubierto por STM-323 a nivel código.
2. QA valida con casos `tipoDocumento=E` + distintos `x-user-vendors`.
3. Cerrar como Done con justificación documentada.

### Camino alternativo (si Ivan exige endpoint dedicado)
- Crear `/notas-credito` controller que invoca internamente `InvoiceService` con `tipoDocumento=E` forzado. **Solo wrapper, sin lógica nueva.**
- Esto agrega valor solo si la UI necesita un endpoint semánticamente separado. Si la UI ya usa `/invoices?tipoDocumento=E`, **no agrega valor**.

## Dudas para Ivan

- **¿La UI consume un endpoint específico de NC o usa el genérico?** Si genérico, no hay trabajo. Si específico, ¿cuál es la ruta esperada?
- **¿Hay diferencia de visualización/exportación entre factura y NC?** Si sí, es trabajo de frontend, no backend.
- **¿Algún campo adicional que NC deba retornar y factura no?** Ya hay `NotaCreditoRelacionadaDto` y `NotaCreditoXlsxDto` en DTOs → revisar si cubren el caso.

## Estimación

- **Si ya cubierto** (esperado): 0 SP — solo QA + cierre.
- **Si requiere endpoint wrapper**: 0.5-1 SP — copia de controller existente.

## Referencias

- DTOs NC: [`NotaCreditoRelacionadaDto.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/dto/NotaCreditoRelacionadaDto.java), [`NotaCreditoXlsxDto.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/dto/NotaCreditoXlsxDto.java)
- Spec: [`InvoiceSpecification.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/repository/specification/InvoiceSpecification.java)
- Jira hermano STM-323 (mismo entity): [STM-323/](../STM-323/)
- Epic STM-1403: [STM-1403/](../STM-1403/)
