# STM-1528 — Análisis técnico (interno)

> ⚠ **Jira incompleto en XML** (placeholder). Reglas de negocio NO documentadas. Análisis basado en patrón del epic STM-1403.

## Resumen del jira

- **Módulo**: Bitácora de actividades (audit logs)
- **Pide**: filtrar la información de bitácora por atributos del usuario
- **Prioridad**: Alta
- **Epic**: STM-1403 (asumido)

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.util-api` (auditoria-api fue **deprecado**, su funcionalidad ahora vive en util-api)

**Endpoints existentes** ([`activityLogs.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/activityLogs.routes.ts)):
- `GET /api/activity-logs/uuid` — obtener por UUID
- `GET /api/activity-logs` — listar con filtros
- `POST /api/activity-logs` — crear (registro nuevo en bitácora, normalmente lo invocan otros servicios)

**Entity** ([`ActivityLogs.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/ActivityLogs.entity.ts)):
- Tabla: `core_audit.activity_logs`
- Campos: `activity_logs_uuid`, `trace_id`, `trace_front_id`, `duration_ms`, `is_error`, `modulo`, `service_name`, `action`, `message`, `message_detail`, **`user_id`**, `timestamp`, `details` (JSON)
- **NO tiene `vendor_number` ni `supplier_number`**.
- **Sí tiene `user_id`** (puede ser el `sub` del JWT del usuario que ejecutó la acción).

## Hallazgos

### 1. XML placeholder, sin reglas
198 líneas, sin sección de reglas. Mismo problema que STM-1527.

### 2. Modelo de datos: filtrado posible por `user_id`
A diferencia de STM-1527 (parámetros), `activity_logs` SÍ tiene un campo que mapea al usuario: `user_id`. Esto facilita una interpretación natural:

> "Cada usuario ve solo sus propias entradas de bitácora, salvo admin que ve todo."

Esa interpretación NO encaja con el patrón vendor del epic, pero ES la interpretación más natural para audit logs.

### 3. Pero el patrón epic dice "Proveedor/TipoProveedor/GrupoProveedor"
Si Ivan quiere filtrar por vendor, no hay columna directa. Posibilidades:
- Filtrar por `service_name` o `modulo` (qué servicio invocó el log) — interpretación débil.
- Hacer JOIN con `core_security.user_data` por `user_id` → cruzar con atributos del usuario que generó el log → filtrar logs cuyo originador comparte vendors con el usuario consultante. **Complejo.**
- Filtrar por `details.vendor_number` (si los servicios consumidores incluyen vendor en el campo JSON `details`). **Requiere convención en el JSON.**

### 4. Interpretación más probable
Combinación:
- **Admin** (`vendors=null`) → ve TODOS los logs.
- **Usuario con `user_id` propio** → ve solo logs donde `activity_logs.user_id = req.user.sub` (lo más natural para audit).
- **Filtro por vendors** → solo si `details.vendor_number` está en la lista.

Pero esto NO está en el ticket. Es interpretación.

## Propuesta

### Camino 1 (recomendado): Pedir clarificación a Ivan

> STM-1528 indica filtrar bitácora por atributo de usuario. La tabla `activity_logs` tiene `user_id` pero no `vendor_number`. Necesito:
> - ¿El filtro es "cada usuario ve sus propios logs" (por `user_id`)?
> - O ¿"filtrar por vendor del log"? Si sí, ¿de dónde sale el vendor (campo `details` JSON)?
> - ¿Reglas iguales al epic (5 reglas + WRN7029)?

### Camino 2 (asumido por defecto del epic)
Implementar middleware `attachSecurityContext` en util-api (si no existe ya) y filtrar por:
```ts
if (req.security.vendors === null) {
    // admin → sin filtro
} else if (req.security.vendors.length === 0) {
    // WRN7029
} else {
    // filtrar: logs donde details.vendor_number IN req.security.vendors
    // (PostgreSQL JSON query: details->>'vendor_number' IN (...))
}
```

Esto solo aplica si los servicios consumidores escriben `vendor_number` en `details`. Convención a definir.

### Camino 3 (más simple, "cada user ve sus logs")
Filtrar siempre por `user_id = req.user.sub`. Ignora atributos del epic. Ivan tendría que justificar por qué se aparta del patrón.

## Archivos a tocar (si se implementa)

| Archivo | Cambio |
|---------|--------|
| `src/controllers/activityLogs.controller.ts` | Leer `req.security`, validar WRN7029, propagar filtro |
| `src/services/activityLogs.service.ts` | Aceptar `userId` o `allowedVendors` según interpretación |
| `src/repositories/activityLogs.repo.ts` | Filtros según interpretación (WHERE `user_id =` o `details->>'vendor_number' IN ...`) |
| `src/middlewares/security.middleware.ts` (util-api) | Crear si no existe; gemelo del de finanzas-api |

## Dudas para Ivan (BLOQUEANTES)

1. **¿Qué se considera "filtrar bitácora por atributo"?** Sus options principales:
   - (a) cada user ve solo sus logs (`user_id = sub`)
   - (b) filtrar por vendor inferido (campo `details` JSON)
   - (c) admin ve todo, resto solo "sus" eventos
2. **¿Aplica WRN7029 a un user normal viendo bitácora?** Si user no tiene atributos pero solo quiere ver SUS logs, no aplicaría.
3. **¿Hay convención para incluir `vendor_number` en `details`?** Si no, esa vía no es viable.

## Estimación

- **Sin clarificación, no estimable.**
- Camino simple ("cada user ve sus logs"): 1-2 SP.
- Camino vendor (JSON query): 3 SP (modelado + tests).

## Referencias

- Entity: [`ActivityLogs.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/ActivityLogs.entity.ts)
- Routes: [`activityLogs.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/activityLogs.routes.ts)
- Memoria: auditoria-api deprecado, migrado a util-api → ver MEMORY.md
