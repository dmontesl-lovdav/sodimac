# Retroalimentacion STM-1487

**Historia:** Migrar el modulo de auditoria a utilerias BFF
**Epic:** STM-1403
**Story Points:** 3
**Estado propuesto:** Listo para code review / QA

---

## Trabajo realizado

Al inicio no existia un BFF de utilerias en el workspace. Se creo el proyecto `APP03022-mrch-bff-somx-ppsomx-util` siguiendo el patron de los BFFs existentes (bff.fiscal, bff.catalogos) y se integraron las rutas de auditoria apuntando al backend de util-api (puerto 3712).

**Arquitectura del BFF:**

- Proxy transparente con `express-http-proxy`.
- Puerto local: 3800.
- Contexto local: `/api/utils`.
- Backend remoto: `http://localhost:3712`.
- Request `/api/utils/activity-logs/*` se traduce a `/api/activity-logs/*` en util-api.

**Integracion de auditoria en el OpenAPI de Cloud Endpoints:**

- `cloud-endpoint/src/definitions/activity-logs.yaml` - DTOs: CreateActivityLogDto, ListActivityLogQueryDto, ActivityLogDto, ActivityLogListResponse, ActivityLogCreateResponse, ActivityLogUuidResponse.
- `cloud-endpoint/src/paths/activity-logs.yaml` - GET y POST `/activity-logs`, GET `/activity-logs/uuid`.
- `cloud-endpoint/src/root.yaml` - Registradas rutas y definitions. Actualizada la descripcion para incluir "activity logs".
- `cloud-endpoint/openapi.yaml` - Regenerado con `npm run openapi:bundle`.

**Bug detectado y corregido en src/App.js:**

El `App.js` inicial del BFF registraba los body-parsers (`bodyParser.json`, `bodyParser.raw`, `bodyParser.urlencoded`) **antes** del middleware de proxy. Esto provocaba que el stream del request fuera consumido por los parsers y el proxy reenviaba un cuerpo vacio, dejando al backend colgado hasta agotar el timeout del request.

**Cambio aplicado:** se movieron los body-parsers a una posicion posterior al `localService.use(localContext, remoteResolver)`, siguiendo el patron ya probado en `bff.ppsomx.fiscal` (mismo equipo, mismo stack). Adicionalmente se cambio `parseReqBody: false` a `parseReqBody: true` en la config del proxy para permitir reenvio explicito del cuerpo JSON.

**Verificacion local:**

- BFF arranca en 3800 y proxyea a util-api 3712.
- `GET /api/utils/activity-logs/uuid` -> 201 con UUID generado.
- `POST /api/utils/activity-logs/` -> 201 y registro insertado en `core_audit.activity_logs` con `service_name = "bff-util"`.
- `GET /api/utils/activity-logs/` con filtros de fecha -> 200 con pagina de resultados.

---

## Observacion sobre los Criterios de Aceptacion

Los CA cargados en la historia (CA-01 a CA-03) describen:

- CA-01: Endpoint `GET /api/auth/health` que responde con el estado de la BD y del servicio FBC con estructura propia de un sistema de autorizacion.
- CA-02: Endpoint `GET /api/admin/auth/metrics` con metricas operativas (totalVerifications, deniedRate, cache.hitRate) accesible solo para ROLE_SYSADMIN.
- CA-03: Logs estructurados JSON del modulo `authorization` con niveles INFO/WARN/ERROR segun resultado ALLOWED/DENIED/ERROR.

El scope real de esta historia (segun la descripcion) es **fusionar el codigo BFF de auditoria dentro del BFF de utilerias**, que por naturaleza es un proxy transparente. Los CAs cargados corresponden a funcionalidad de un modulo de autorizacion diferente (mismo Epic STM-1403, presumiblemente otra historia).

Se sugiere:
1. Mover los CA actuales a la historia que realmente los demanda.
2. Reemplazarlos por CAs alineados al proxy (ej: "el BFF responde 201 al POST /api/utils/activity-logs con el payload correcto", "el header X-Trace-Id se propaga", "el health local del BFF responde 200", etc.).

---

## Pendientes para cerrar

1. Descontinuar el BFF `bff.ppsomx.auditoria` (puerto 3008): ya no tiene backend propio, queda reemplazado por `bff-util` + util-api.
2. Actualizar los frontends que consumian `bff.ppsomx.auditoria` para apuntar al nuevo contexto `/api/utils/activity-logs`.
3. Revisar con DevOps la configuracion de Cloud Endpoints de produccion (kustomization/production/ingress.yaml) para dar de alta las rutas nuevas del dominio.

---

## Comentario final

Fue valioso armar el BFF desde cero tomando como base `bff.fiscal` y `bff.catalogos` porque permitio detectar que la variante inicial tenia un orden de middlewares invertido que habria impedido que funcionara en produccion. La correccion es de una sola linea pero bloquea todos los POST/PUT/PATCH si no se detecta.
