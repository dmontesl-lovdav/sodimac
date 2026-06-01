# Troubleshooting Envoy / Cloud Endpoint — Sodimac UAT

Guía para diagnosticar errores 404 / 500 / 502 / 504 que vienen del gateway Envoy (ESPv2) de GCP Cloud Endpoint en UAT.

---

## Cómo identificar que el error viene de Envoy

Mira el header de respuesta `x-envoy-decorator-operation`:

```
x-envoy-decorator-operation: ingress <NombreOperación>
```

- `ingress <NombreOperación>` → Envoy enrutó al backend, error viene del backend
- `ingress UnknownOperationName` → **Envoy NO conoce el path**, rechaza antes de llegar al backend

Headers complementarios:
- `via: 1.1 google` → pasó por GCP
- `server: cloudflare` → pasó por Cloudflare (siempre presente en UAT público)
- `x-envoy-upstream-service-time: <ms>` → tiempo que tardó el backend en responder (si hay valor, llegó al backend)

---

## Catálogo de errores por status code

### 200 + HTML body (no JSON) en `/ppsomx/backend-fiscal/*`

**Síntoma**: curl al path devuelve 200 OK pero body es HTML del frontend FBC.

**Causa**: Ese path NO existe en gateway. Cloudflare hace catchall a la SPA (`https://uat.fbusinesscenter.com/`).

**Ejemplo**:
```bash
curl -i "https://uat.fbusinesscenter.com/ppsomx/backend-fiscal/health"
# → 200 OK + <!DOCTYPE html>...
```

**Lectura**: `/ppsomx/backend-fiscal/*` **NO está enrutado**. fiscal-api no es accesible directo desde gateway público.

**Rutas reales para llegar a fiscal-api**:
- Vía BFF: `/ppsomx/fiscal/*` → bff.fiscal → fiscal-api
- NO hay path público directo backend

---

### 404 `UnknownOperationName`

```
HTTP/1.1 404 Not Found
x-envoy-decorator-operation: ingress UnknownOperationName
{"message":"The current request is not defined by this API.","code":404}
```

**Causa**: el path NO está declarado en el OpenAPI spec registrado en Cloud Endpoint.

**Sub-causas**:

| Sub-causa | Cómo identificar | Fix |
|-----------|------------------|-----|
| OpenAPI viejo en gateway (deploy Cloud Endpoint nunca corrió) | Path existe en código fuente del BFF, falta en gateway | Aprobar Manual Approval Cloud Endpoint en Pipeline CI |
| Path nunca declarado | Path no existe en `openapi.yaml` del BFF | Agregar al `openapi.yaml` del BFF + push + aprobar deploy |
| Doble `/api` por cliente Spring mal configurado | Path tipo `/api/api/messages/...` | Quitar `/api` del cliente (ver `bff_api_prefix_trap`) |
| Path typo | Path con error: `/invoices/seach` en vez de `/invoices/search` | Corregir typo en cliente |

---

### 404 `not found`

```
HTTP/1.1 404 Not Found
{"message":"not found"}
```

(Sin header `UnknownOperationName`.)

**Causa**: backend (BFF o pod) recibió el request y respondió 404. Endpoint definido en gateway pero NO existe en pod.

**Sub-causas**:

| Sub-causa | Fix |
|-----------|-----|
| Pod K8S con código viejo (deploy GKE no corrió o falló) | Re-run pipeline, verificar Deploy GKE en Actions |
| Path correcto pero recurso no existe (ej: user no en DB) | Body de respuesta clarifica (`"User not found"` vs path 404) |

---

### 500 Internal Server Error

```
HTTP/1.1 500
{"message":"Internal Server Error","code":500}
x-envoy-decorator-operation: ingress <Operation>
```

**Causa**: backend procesó pero crasheó.

**Sub-causas observadas en Sodimac**:

| Sub-causa | Síntoma adicional | Fix |
|-----------|-------------------|-----|
| BFF parseReqBody:false + body parsers globales | Backend log: "Required request body is missing" | Quitar body parsers (json/raw/urlencoded) del BFF (ver `project_bff_proxy_body_pattern`) |
| Doble `/api` en path util-api | 500 desde backend Java tras llamar util-api | Quitar `/api` del cliente Spring |
| NPE en middleware security cuando headers vacíos | x-envoy-upstream-service-time: 2000+ms | Validar headers presentes antes desreferenciar |
| DB caída o credenciales malas | `org.postgresql.util.PSQLException` en pod log | Verificar `DATASOURCE_URL` / `DB_HOST` en kustomization |
| util-api timeout | x-envoy-upstream-service-time > 5000ms | Aumentar `UTILS_API_TIMEOUT` o verificar util-api salud |

**Diagnóstico**: necesitas logs del cluster. Pide a DevOps:
```bash
kubectl -n vendor-portal logs deployment/bff-fiscal --tail=100
kubectl -n vendor-portal logs deployment/fiscal-api --tail=100
```

Filtrar por timestamp del error (usa `Date:` header de la respuesta curl).

---

### 502 Bad Gateway

```
HTTP/1.1 502 Bad Gateway
```

**Causa**: Envoy no pudo contactar al backend.

**Sub-causas**:
- Pod backend no levantó (readiness probe falla)
- Pod backend crashloop (revisar `kubectl describe pod`)
- Service K8S desincronizado (raro)

**Fix**: revisar estado del deployment + logs del pod.

---

### 504 Gateway Timeout

```
HTTP/1.1 504 Gateway Timeout
```

**Causa**: backend tardó más del timeout configurado en responder.

**Sub-causas observadas**:

| Sub-causa | Síntoma | Fix |
|-----------|---------|-----|
| BFF stream del body consumido por parsers, proxy espera infinito | x-envoy-upstream-service-time > 60000ms | Quitar body parsers globales del BFF (ver patrón documentado) |
| DB query lenta | logs muestran query > 30s | Optimizar query, agregar índice |
| External API lenta (Detecno, Carbajal) | Backend log esperando integración externa | Aumentar timeout o circuit-breaker |

---

### 401 / 403 Unauthorized / Forbidden

```
HTTP/1.1 401 Unauthorized
```

**Causa**: gateway o backend rechazó por auth.

**Sub-causas**:

| Sub-causa | Fix |
|-----------|-----|
| JWT requerido y no enviado | Mandar `Authorization: Bearer <JWT>` válido UAT |
| JWT firma inválida (alg=none rechazado) | Conseguir JWT real de Keycloak UAT, no usar el local `alg=none` |
| JWT expirado | Renovar token |
| Path requiere scope no presente en JWT | Verificar `scope` claim del JWT |

---

## Workflow de diagnóstico rápido

Cuando un cliente reporta error en UAT:

1. **Reproducir con curl** desde tu PC Sodimac con `-i` para ver headers.
2. **Leer status code y body** primero.
3. **Mirar `x-envoy-decorator-operation`** — te dice si llegó al backend o no.
4. **Mirar `x-envoy-upstream-service-time`** — te dice si backend respondió.
5. **Si llegó al backend (header con nombre operación)**: pedir logs del pod, filtrar por timestamp.
6. **Si NO llegó (UnknownOperationName)**: revisar OpenAPI del BFF, verificar deploy Cloud Endpoint reciente.
7. **Si responde HTML SPA**: path no existe en gateway, está pegando a Cloudflare catchall.

---

## Headers útiles para guardar en debug

Cuando reportas un bug, captura estos headers de la respuesta:

```
Date:                              # timestamp exacto del error (para buscar en logs)
CF-RAY:                            # ID de la request en Cloudflare
x-envoy-decorator-operation:       # operación que matched (o UnknownOperationName)
x-envoy-upstream-service-time:     # ms que tardó el backend
x-powered-by:                      # Express = BFF Node, ausente = backend Java
content-type:                      # JSON = backend, HTML = catchall SPA
```

Estos 5 headers reducen el diagnóstico a 30 segundos en lugar de 30 minutos.

---

## Cluster GKE UAT

- **Proyecto**: `fal-corp-mrch-foundational-uat`
- **Cluster**: `gke-foundational`
- **Zona**: `us-east4`
- **Namespace**: `vendor-portal`

Comandos típicos (necesitas `gcloud auth login` + `gcloud container clusters get-credentials`):

```bash
kubectl -n vendor-portal get pods | grep <servicio>
kubectl -n vendor-portal logs <pod> --tail=200
kubectl -n vendor-portal logs <pod> --previous --tail=200   # si crasheó
kubectl -n vendor-portal describe pod <pod>
kubectl -n vendor-portal exec -it <pod> -- /bin/sh           # entrar al contenedor
```

Si no tienes acceso kubectl, pide a Bonelli o líder DevOps.
