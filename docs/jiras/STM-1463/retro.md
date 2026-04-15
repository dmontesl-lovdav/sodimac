# Retroalimentacion STM-1463

**Historia:** Migrar el modulo de auditoria a utilerias BackEnd
**Epic:** STM-1403
**Story Points:** 3
**Estado propuesto:** Listo para code review / QA

---

## Trabajo realizado

Se fusiono el codigo BackEnd de `auditoria-api` dentro de `util-api`. Se conserva la base de datos sin cambios (la tabla `activity_logs` se mantiene en el esquema `core_audit`) y se mantiene el puerto de `util-api` (3712) como punto de entrada unico.

**Archivos incorporados a util-api:**

- `src/entities/ActivityLogs.entity.ts` - Entity con `@Entity({ schema: 'core_audit', name: 'activity_logs' })` para que TypeORM apunte al esquema correcto sin mover la tabla.
- `src/schemas/activityLog.schema.ts` - Validacion Zod (CreateActivityLogSchema, ListActivityLogQuerySchema).
- `src/repositories/activityLogs.repo.ts` - INSERT directo + findAllPaginated.
- `src/services/activityLogs.service.ts` - Logica de negocio.
- `src/controllers/activityLogs.controller.ts` - Endpoints create/list/uuid.
- `src/routes/activityLogs.routes.ts` - Router registrado en `src/routes/index.ts` con prefijo `/activity-logs`.
- `src/middlewares/logger.ts` - Middleware de trazabilidad con AsyncLocalStorage. Se agrego soporte de `trace_front_id` (se lee del header `TraceFrontId`) tomando la mejora que tenia la variante de finanzas-api.
- `src/middlewares/validate.ts` - Helpers `validateBody`, `validateQuery`, `validateParams`.
- `src/utils/logger.ts` - Se unifico el logger a **pino** (util-api ya lo usaba). auditoria-api usaba winston, se descarto para evitar duplicar dependencias.

**Archivos modificados:**

- `src/config/typeorm-datasource.ts` - Agregada entidad `ActivityLogs` al arreglo `ENTITIES`.
- `src/routes/index.ts` - Registrada ruta `/activity-logs`.
- `src/middlewares/index.ts` - Re-exporta `validate` y `logger`.
- `.env.example` - Agregado bloque `# Variables de Auditoria (STM-1463)` con `DB_AUDIT_SCHEMA` y `LOG_LEVEL`.

**Decisiones tecnicas:**

- Se descarto el paquete `uuid` (dependencia de auditoria-api): se reemplazo por `crypto.randomUUID()` nativo de Node 18+. Menos dependencias, mismo resultado.
- Se descarto `typeorm-transactional-cls-hooked` para auditoria: el INSERT se mantiene por `datasource.query` crudo (igual que la version original) y no requiere el contexto transaccional.
- Se conservo el comportamiento de **fire-and-forget** en el POST: el controller responde 201 inmediato y el insert corre en background (patron original de auditoria-api).

**Verificacion local:**

- `npm run build` compila sin errores.
- `npm run dev` arranca en puerto 3712, inicializa datasource con 8 entidades incluyendo `ActivityLogs`.
- Prueba `GET /api/activity-logs/uuid` devuelve UUID valido.
- Prueba `POST /api/activity-logs/` con payload valido responde 201 y el registro queda insertado en `core_audit.activity_logs`.

---

## Consumidores impactados

**fiscal-api (Java):** consume `AUDITORIA_API_URL` por HTTP. Requiere cambiar la URL:
- Dev: `http://localhost:8091` -> `http://localhost:3712`
- Prod: coordinar con DevOps el cambio del service `auditoria-api:8080` al service de `util-api`.

**finanzas-api (Node):** inserta directo en `core_audit.activity_logs` con SQL crudo (no consume HTTP). Como la tabla se mantiene en el mismo esquema, **no requiere cambios**.

**Otros servicios (catalogos-api, aclaraciones-api, util-api mismo):** no se encontraron consumidores adicionales de auditoria.

---

## Observacion sobre los Criterios de Aceptacion

Los CA cargados en la historia (CA-01 a CA-06) describen un sistema de cache de permisos con TTL, invalidacion, degradacion graceful y endpoint de metricas del modulo `authorization`. Mencionan HUs AUTH-008, AUTH-015, AUTH-017 y roles ROLE_ADMIN_FINANCIERO / ROLE_SYSADMIN.

El trabajo real de esta historia (conforme a la descripcion) es **fusionar el codigo BackEnd del modulo de auditoria dentro de util-api** para evitar el desfase de los DevOps corporativos al crear un repo nuevo. El scope de los CAs corresponde a un trabajo de autenticacion/autorizacion diferente (presumiblemente otra historia del mismo Epic STM-1403).

Se sugiere:
1. Mover los CA actuales a la historia que realmente los demanda.
2. Reemplazarlos por CAs alineados con la fusion (ej: "el endpoint POST /api/activity-logs responde 201 desde util-api", "la tabla sigue en core_audit", "fiscal-api apunta al nuevo host", etc.).

---

## Pendientes para cerrar

1. Apuntar `AUDITORIA_API_URL` de fiscal-api al nuevo host.
2. Descontinuar el repo `auditoria-api` (archivar, README con redirect).
3. Descontinuar el despliegue DevOps de `auditoria-api`.
4. Confirmar con finanzas-api si conviene migrar su escritura directa al endpoint de util-api o mantenerla (fuera de scope de esta historia).
