# Retroalimentación para JIRA STM-932

## Resumen Ejecutivo

Se completó la **Fase 1** de implementación del BFF de Auditoría, configurando el proyecto como proxy transparente hacia el backend `auditoria-api`.

---

## Estado de Implementación

### ✅ Completado

| Componente | Descripción |
|------------|-------------|
| **BFF Proxy** | Implementado con express-http-proxy, siguiendo arquitectura de BFF Fiscal |
| **Kustomization** | Configuración K8s para development, UAT y production |
| **OpenAPI** | Especificación cloud-endpoint/openapi.yaml |
| **CI/CD** | Pipeline .gitlab-ci.yml configurado |
| **Documentación** | README.md y .env.example |

### ✅ Endpoints Disponibles

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/health` | Health check |
| GET | `/api/activity-logs/uuid` | Genera UUID para trazabilidad |
| GET | `/api/activity-logs` | Lista logs con filtros y paginación |
| POST | `/api/activity-logs` | Crea log de actividad (async) |

### ⚠️ Pendiente (Fase 2)

Los siguientes endpoints mencionados en el JIRA **no existen en el backend actual** y requieren implementación adicional:

| Operación | Estado |
|-----------|--------|
| Actualización Parcial (PATCH) | No implementado en backend |
| Versionado Histórico | No implementado en backend |
| Cambio de Estatus | No implementado en backend |

---

## Pruebas Realizadas

### Ambiente Local

```
Backend: http://localhost:8091 ✅
BFF:     http://localhost:3008 ✅
```

| Endpoint | Backend | BFF (Proxy) |
|----------|---------|-------------|
| GET /health | ✅ OK | ✅ OK |
| GET /api/activity-logs/uuid | ✅ OK | ✅ OK |
| POST /api/activity-logs | ✅ OK | ✅ OK |
| GET /api/activity-logs | ✅ OK (bug paginación) | ✅ OK |

---

## Bug Identificado

**Archivo:** `activityLogs.repo.ts` línea 29

**Descripción:** El cálculo de paginación usa `(pageNumber - 1) * pageSize`, lo cual genera un OFFSET negativo cuando `pageNumber = 0`.

**Error:** `QueryFailedError: OFFSET must not be negative`

**Impacto:** El endpoint GET /api/activity-logs falla con pageNumber=0

**Solución:** Cambiar a `pageNumber * pageSize` si es 0-indexed, o usar `Math.max(0, (pageNumber - 1) * pageSize)`.

---

## Artefactos Entregados

| Archivo | Ubicación |
|---------|-----------|
| Colección Postman | `docs/jiras/STM-932/BFF-Auditoria.postman_collection.json` |
| Queries SQL Validación | `docs/jiras/STM-932/validacion_auditoria.sql` |
| Documentación Técnica | `docs/jiras/STM-932/README.md` |

---

## URLs de Despliegue

| Ambiente | URL |
|----------|-----|
| Development | `https://vendor-dev.fbusinesscenter.com/ppsomx/auditoria/` |
| UAT | `https://vendor-uat.fbusinesscenter.com/ppsomx/auditoria/` |
| Production | `https://vendor.fbusinesscenter.com/ppsomx/auditoria/` |

---

## Próximos Pasos Sugeridos

1. **Corregir bug de paginación** en backend (`activityLogs.repo.ts`)
2. **Definir requerimientos** para Actualización, Versionado y Cambio de Estatus:
   - ¿Qué campos son editables?
   - ¿Cómo funciona el versionado?
   - ¿Cuáles son los estados válidos?
3. **Implementar endpoints faltantes** en backend según definición
4. **Actualizar BFF** para exponer nuevos endpoints
5. **Desplegar en Development** para pruebas de integración

---

## Código para Copiar a JIRA

```
h2. Estado: Fase 1 Completada

h3. Implementación BFF Auditoría

Se implementó el BFF como proxy transparente hacia auditoria-api:

* ✅ Estructura proyecto completa (kustomization, OpenAPI, CI/CD)
* ✅ Endpoints proxy: health, uuid, activity-logs (GET/POST)
* ✅ Pruebas locales exitosas

h3. Endpoints Disponibles

|| Método || Ruta || Descripción ||
| GET | /health | Health check |
| GET | /api/activity-logs/uuid | Genera UUID |
| GET | /api/activity-logs | Lista logs paginados |
| POST | /api/activity-logs | Crea log (async) |

h3. Pendiente (requiere backend)

* Actualización Parcial (PATCH)
* Versionado Histórico
* Cambio de Estatus

{color:red}*Bug identificado:*{color} Endpoint GET /api/activity-logs falla con pageNumber=0 (OFFSET negativo en activityLogs.repo.ts:29)

h3. Artefactos

* Colección Postman
* Queries SQL validación (esquema core_audit)
* Documentación técnica

h3. URLs

* DEV: https://vendor-dev.fbusinesscenter.com/ppsomx/auditoria/
* UAT: https://vendor-uat.fbusinesscenter.com/ppsomx/auditoria/
* PROD: https://vendor.fbusinesscenter.com/ppsomx/auditoria/
```
