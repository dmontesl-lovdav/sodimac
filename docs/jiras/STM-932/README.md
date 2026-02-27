# STM-932: Creación del BFF de Auditoría

## Estado: ✅ IMPLEMENTADO (Fase 1)

---

## Descripción

Como Desarrollador Backend, quiero implementar los endpoints del BFF (NodeJS) con una estrategia de persistencia híbrida, para permitir la gestión del ciclo de vida del servicio de auditoría (creación, edición simple y versionado histórico) asegurando la integridad de los datos.

## Criterios de Aceptación

| ID | Criterio | Descripción | Estado |
|----|----------|-------------|--------|
| CA01 | Cobertura de Endpoints | Exponer métodos HTTP para: Consulta, Creación | ✅ Implementado |
| CA01 | Cobertura de Endpoints | Actualización Parcial, Versionado, Cambio de Estatus | ⚠️ Pendiente Backend |
| CA02 | Manejo de Respuestas | Retornar códigos HTTP estándar (200, 201, 400, 409, 422, 500) | ✅ Implementado |
| CA03 | Integridad de Datos | Versionado debe garantizar atomicidad | ⚠️ Pendiente Backend |
| CA04 | Validaciones de Entrada | Rechazar peticiones inválidas | ✅ Implementado (Zod) |

---

## Implementación Completada

### 1. Estructura del Proyecto BFF

```
APP03022-mrch.bff.somx.ppsomx.auditoria/
├── src/
│   └── App.js                      ✅ Proxy transparente completo
├── cloud-endpoint/
│   └── openapi.yaml                ✅ Especificación OpenAPI
├── kustomization/
│   ├── base/                       ✅ Configuración base K8s
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── kustomization.yaml
│   ├── development/                ✅ 9 archivos
│   ├── uat/                        ✅ 9 archivos
│   └── production/                 ✅ 9 archivos
├── Dockerfile                      ✅ Node 22 Alpine
├── package.json                    ✅ Dependencias completas
├── .env.example                    ✅ Variables documentadas
├── .gitlab-ci.yml                  ✅ Pipeline CI/CD
└── README.md                       ✅ Documentación
```

### 2. Endpoints Expuestos (via Proxy)

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| GET | `/health` | Health check del servicio | ✅ Probado |
| GET | `/api/activity-logs/uuid` | Generar UUID para trazabilidad | ✅ Probado |
| GET | `/api/activity-logs` | Listar logs con filtros y paginación | ✅ Probado |
| POST | `/api/activity-logs` | Crear log de actividad | ✅ Probado |

### 3. URLs por Ambiente

| Ambiente | URL |
|----------|-----|
| Local | `http://localhost:3008` |
| Development | `https://vendor-dev.fbusinesscenter.com/ppsomx/auditoria/` |
| UAT | `https://vendor-uat.fbusinesscenter.com/ppsomx/auditoria/` |
| Production | `https://vendor.fbusinesscenter.com/ppsomx/auditoria/` |

### 4. Pruebas Realizadas

#### Backend Directo (localhost:8091)
```bash
# Health Check
curl http://localhost:8091/health
# Respuesta: {"ok":true,"env":"development"}

# Generar UUID
curl http://localhost:8091/api/activity-logs/uuid
# Respuesta: "ba741f62-7694-4be0-add6-bf2234b2df39"

# Crear Log
curl -X POST http://localhost:8091/api/activity-logs \
  -H "Content-Type: application/json" \
  -d '{"traceId":"550e8400-e29b-41d4-a716-446655440000","modulo":"fiscal","action":"TEST","serviceName":"test","userId":"user1","isError":false,"message":"Test","messageDetail":"Detail","timestamp":"2024-01-15T10:30:00Z"}'
# Respuesta: {"message":"Data received, processing in background."}
```

#### BFF Proxy (localhost:3008)
```bash
# Health Check (proxied)
curl http://localhost:3008/health
# Respuesta: {"ok":true,"env":"development"}

# Generar UUID (proxied)
curl http://localhost:3008/api/activity-logs/uuid
# Respuesta: "41f0663b-1999-4678-af58-ace79df41f14"

# Crear Log (proxied)
curl -X POST http://localhost:3008/api/activity-logs \
  -H "Content-Type: application/json" \
  -d '{"traceId":"660e8400-e29b-41d4-a716-446655440001","modulo":"auditoria","action":"BFF_TEST","serviceName":"bff","userId":"user2","isError":false,"message":"Via BFF","messageDetail":"Proxy test","timestamp":"2024-01-15T11:00:00Z"}'
# Respuesta: {"message":"Data received, processing in background."}
```

---

## Artefactos Generados

| Archivo | Descripción |
|---------|-------------|
| `BFF-Auditoria.postman_collection.json` | Colección Postman con todos los endpoints |
| `validacion_auditoria.sql` | Queries SQL para validar en BD (esquema core_audit) |

---

## Observaciones Técnicas

### Bug Identificado en Backend

**Archivo:** `activityLogs.repo.ts:29`
```typescript
const skip = (pageNumber - 1) * pageSize; // BUG: pageNumber 0 causa OFFSET negativo
```

**Problema:** Cuando `pageNumber = 0`, el cálculo resulta en `skip = -pageSize`, causando error PostgreSQL: `OFFSET must not be negative`.

**Solución Sugerida:**
```typescript
const skip = pageNumber * pageSize; // Si pageNumber es 0-indexed
// O
const skip = Math.max(0, (pageNumber - 1) * pageSize); // Si pageNumber es 1-indexed
```

---

## Pendientes (Fase 2 - Backend)

Los siguientes endpoints requieren implementación en el backend `auditoria-api`:

| Endpoint | Método | Descripción | Dependencia |
|----------|--------|-------------|-------------|
| `/api/activity-logs/:id` | GET | Obtener log por ID | Backend |
| `/api/activity-logs/:id` | PATCH | Actualizar metadatos | Backend + definir campos editables |
| `/api/activity-logs/:id/version` | POST | Crear nueva versión | Backend + definir lógica versionado |
| `/api/activity-logs/:id/status` | PATCH | Cambiar estatus | Backend + definir estados válidos |

### Preguntas Pendientes para Fase 2

1. **Actualización Parcial:** ¿Qué campos son editables vs bloqueados?
2. **Versionado:** ¿Cómo se relacionan las versiones? ¿Tabla separada?
3. **Cambio de Estatus:** ¿Cuáles son los estados válidos y transiciones permitidas?

---

## Conclusión

✅ **Fase 1 Completada:** El BFF de Auditoría está implementado como proxy transparente hacia el backend `auditoria-api`, siguiendo la arquitectura de referencia del BFF Fiscal.

⚠️ **Fase 2 Pendiente:** Los endpoints de Actualización, Versionado y Cambio de Estatus requieren implementación adicional en el backend antes de poder exponerlos en el BFF.

---

## Comandos Útiles

```bash
# Levantar Backend
cd APP03022-mrch.backend.somx.auditoria-api
npm run start:dev

# Levantar BFF
cd APP03022-mrch.bff.somx.ppsomx.auditoria
npm run start

# Validar en PostgreSQL
psql -h localhost -p 5434 -U wwwb2bportal -d b2b_portal
\c b2b_portal
SET search_path TO core_audit;
SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 10;
```
