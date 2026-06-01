# Entorno UAT — Sodimac FBC

## URLs de los BFFs

| Servicio | URL base UAT | Llega a |
|---|---|---|
| fiscal (BFF) | `https://uat.fbusinesscenter.com/ppsomx/fiscal/` | bff.fiscal → fiscal-api |
| util (BFF) | `https://uat.fbusinesscenter.com/ppsomx/backend-util/` | bff-util → util-api |
| finanzas (BFF) | `https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/` | bff.finanzas → finanzas-api |

> `uat.vendor.fbusinesscenter.com` existe en el ingress k8s pero requiere VPN/cert — usar siempre `uat.fbusinesscenter.com`.

### ⚠ Trampa — paths inexistentes responden HTML 200 (SPA catchall)

Cualquier path que NO esté en la lista anterior cae en el catchall del frontend FBC. Cloudflare devuelve 200 OK pero con HTML de la SPA en lugar de error.

Ejemplos de paths que **NO existen** pero parecen funcionar:
```bash
curl -i "https://uat.fbusinesscenter.com/ppsomx/backend-fiscal/health"
# → 200 OK + <!DOCTYPE html>...   ⚠ NO es respuesta del backend
```

Lectura rápida:
- `Content-Type: text/html` → SPA catchall (path no enrutado)
- `Content-Type: application/json` → respuesta real del BFF / backend

Para diagnóstico completo de errores gateway: [docs/wiki/troubleshooting-envoy.md](wiki/troubleshooting-envoy.md).

---

## Variables de ambiente — fiscal-api en k8s UAT

El pipeline inyecta estas variables desde el Secret/ConfigMap de k8s:

| Variable k8s | Descripción |
|---|---|
| `ENV_DEV` | `uat` → activa `application-uat.properties` en Spring Boot |
| `DATASOURCE_URL` | JDBC URL completa: `jdbc:postgresql://10.100.64.102:5432/b2b_portal?currentSchema=tenant_fiscal&useSSL=true` |
| `DATASOURCE_USERNAME` | `wwwb2bportal` |
| `DATASOURCE_PASSWORD` | `b8@qU0YM1HU>` |
| `UTILS_API_URL` | `https://uat.fbusinesscenter.com/ppsomx/backend-util` (sin `/api` al final — el BFF lo agrega) |
| `FINANZAS_JWT_ENABLED` | `false` por ahora (JWT deshabilitado) |
| `SECURITY_ENABLED` | `false` por ahora |
| `UTILS_API_ENABLED` | `true` (presente en k8s pero ya no lo lee el código — se eliminó) |
| `UTILS_API_TIMEOUT` | `5000` (presente en k8s pero ya no lo lee el código — se eliminó) |

> **Nota importante**: k8s usa `DATASOURCE_*` y `UTILS_API_URL` (con S).
> El código desde 2026-05-27 soporta ambas convenciones via fallback encadenado en `application.properties`.

---

## Comportamiento del BFF fiscal (proxy)

El BFF fiscal **NO agrega prefijo `/api`** — pasa las rutas tal cual al backend.

```
curl → Cloudflare → Envoy (ESP) → BFF fiscal → fiscal-api :8082
```

Solo las rutas registradas en `cloud-endpoint/openapi-bundled.yaml` pasan por Envoy.
`/actuator/health` **no está registrado** → Envoy devuelve 404.

## Comportamiento del BFF util (proxy)

El BFF util **SÍ agrega `/api`** automáticamente a todas las rutas:

```js
return "/api" + normalizedPath;  // App.js del BFF util
```

Por eso fiscal-api llama `{UTILS_API_URL}/messages/code/BUS001` (sin `/api`) y el BFF lo convierte a `/api/messages/code/BUS001` hacia util-api.

---

## Curls de validación UAT

### BFF fiscal

```bash
# Health del BFF (resuelto por el BFF, no llega a fiscal-api)
curl -i "https://uat.fbusinesscenter.com/ppsomx/fiscal/health"
# → 200 {"message":"healthy"}

# Validar que fiscal-api está arriba (pasa por Envoy + BFF)
curl -i "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices"
# → 200 con paginación (aunque vacío) si fiscal-api está arriba
# → 500 body vacío si fiscal-api está caído

# Buscar facturas
curl -i -X POST "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/search" \
  -H "Content-Type: application/json" \
  -H "x-user-rfc: *" \
  -H "x-user-vendors: *" \
  -d '{"tipoDocumento":"I","fechaInicioRecepcion":"2026-01-01","fechaFinalRecepcion":"2026-05-26","page":0,"size":3}'

# Procesar XML (multipart)
curl -i -X POST "https://uat.fbusinesscenter.com/ppsomx/fiscal/fiscal/xml/process/file" \
  -F "file=@ruta/al/archivo.xml"
```

### BFF util

```bash
# Mensaje por código
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-util/messages/code/BUS001"
# → {"success":true,"data":{"description":"La addenda de la factura..."}}

# Parámetro por nombre
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-util/parameters?name=MAX_SEARCH_MONTHS"
# → {"success":true,"data":[{"name":"MAX_SEARCH_MONTHS","value":"6",...}]}
```

### BFF finanzas

```bash
# Health
curl -i "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/healthcheck"
# → 200 {"alive":true,...}

# Pagos
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/finanzas-payment?createdAtInitial=2026-01-01&createdAtEnd=2026-05-26&pageNumber=1&pageSize=5"
```

---

## Diagnóstico rápido si fiscal-api da 500

| Síntoma | Causa | Acción |
|---|---|---|
| 500 body vacío, `x-powered-by: Express`, <50ms | fiscal-api pod caído | revisar pod en k8s |
| 404 `"not defined by this API"` | Ruta no registrada en Envoy | agregar al `openapi-bundled.yaml` |
| 405 desde nginx | Método HTTP no permitido o path incorrecto | verificar host y path del ingress |
| 200 en `/health` pero 500 en otros | BFF arriba, fiscal-api caído | igual — pod caído |

### Verificar pod en k8s

```bash
kubectl get pods -n vendor-portal | grep fiscal
kubectl logs <pod-name> --tail=100
# o si crasheó:
kubectl logs <pod-name> --previous --tail=100
```

Cluster UAT: `gke-foundational`, proyecto `fal-corp-mrch-foundational-uat`, zona `us-east4`, namespace `vendor-portal`.
