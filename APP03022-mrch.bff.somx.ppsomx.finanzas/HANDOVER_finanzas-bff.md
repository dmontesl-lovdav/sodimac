# HANDOVER.md — `APP03022-mrch.bff.somx.ppsomx.finanzas`

> Documento de traspaso basado en el código, `package.json`, estructura `cloud-endpoint` y especificación OpenAPI compartida para el repositorio.

**Proyecto:** APP03022-mrch.bff.somx.ppsomx.finanzas  
**Tipo:** BFF / Gateway proxy / Expositor Swagger para Google Cloud Endpoints  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.finanzas  
**Fecha:** 2026-06-25  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Pendiente de confirmar

---

## 1. ¿Qué hace este proyecto?

Este proyecto es un **BFF intermedio para Finanzas** que cumple principalmente tres funciones:

1. **Exponer la documentación Swagger / OpenAPI** consumida por Google Cloud Endpoints.
2. **Publicar el archivo `openapi.yaml`** y los assets de Swagger UI bajo rutas públicas y rutas aliased del portal.
3. **Actuar como proxy hacia `finanzas-api`**, reenviando las rutas expuestas hacia el backend real con prefijo `/api`.

No contiene lógica de negocio financiera principal ni persistencia propia. La lógica de pagos, estados de cuenta, MIGO, Three Way Match, Carta Porte, rebates, órdenes de compra, guías, vendor blocks, auditoría, etc., vive en el backend **`APP03022-mrch.backend.somx.finanzas-api`**. Este BFF funciona como capa de exposición / gateway entre el consumidor, Google Cloud Endpoints y la API real.

**Uso principal:** publicar y versionar contratos OpenAPI 2.0 para Google Cloud Endpoints y enrutar tráfico hacia `finanzas-api`.

**Consumidores / usuarios:** Google Cloud Endpoints, portal FBC, micro frontend Finanzas, clientes internos que consumen las rutas documentadas.

**Criticidad:** Alta. Aunque no contiene negocio financiero, es parte del camino de exposición de las APIs de Finanzas y de la documentación utilizada por el gateway.

**Documentación adicional:**

| Recurso | Link / Estado |
|---------|---------------|
| Repositorio | `https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.finanzas` |
| Backend destino | `APP03022-mrch.backend.somx.finanzas-api` |
| Swagger UI | `/docs` y `/ppsomx/backend-finanzas/docs` |
| OpenAPI YAML | `/openapi.yaml` y `/ppsomx/backend-finanzas/openapi.yaml` |
| Google Cloud Endpoints | Configurado desde `cloud-endpoint/openapi.yaml` |
| Postman collection | No disponible |
| Confluence / documentación técnica | [Documentación Finanzas Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897680760/Documentacion+Finanzas+Dev) |
| Tablero Jira / backlog | No disponible |

---

## 2. Cómo levantar el proyecto

**Requisitos:** Node.js · npm · acceso al repositorio · variables de entorno del ambiente

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.finanzas
cd APP03022-mrch.bff.somx.ppsomx.finanzas

# usar como base la rama develop, si aplica
git checkout develop
git pull origin develop

npm install
npm run openapi:bundle
npm start
```

**Scripts disponibles:**

```bash
npm start
npm run openapi:bundle
npm test
```

| Script | Descripción |
|--------|-------------|
| `npm start` | Levanta el servidor Express con `node --max-http-header-size=1048576 ./src/App.js`. |
| `npm run openapi:bundle` | Genera `cloud-endpoint/openapi.yaml` a partir de `cloud-endpoint/src/root.yaml` usando `swagger-cli bundle`. |
| `npm test` | No ejecuta pruebas reales; genera un `coverage/lcov.info` dummy para que el pipeline tenga archivo de cobertura. |

**Notas de arranque local:**

- El entrypoint es `src/App.js`.
- El proyecto usa ES Modules (`"type": "module"`).
- El servidor Express toma configuración desde `.env` mediante `dotenv`.
- El puerto local se toma desde `LOCAL_PORT`; si no existe, usa `3000`.
- El backend destino se toma desde `REMOTE_URL`; si no existe, usa `http://localhost:3001`.
- El contexto local se toma desde `LOCAL_CONTEXT`; si no existe, usa `/`.
- El healthcheck se expone en `HEALTH_PATH`; si no existe, usa `/health`.
- Antes de publicar/deployar cambios en contratos, ejecutar `npm run openapi:bundle` para regenerar el archivo final `cloud-endpoint/openapi.yaml`.

**Ejemplo local mínimo:**

```env
REMOTE_URL=http://localhost:8082
LOCAL_PORT=3000
LOCAL_CONTEXT=/
HEALTH_PATH=/health
UTIL_API_URL=http://localhost:3712
DOMAIN_OPENAPI=localhost:3000
KEYCLOAK=
JWKS_URL=
AUTH_PUBLIC_KEY=
```

Con esa configuración local:

```bash
npm install
npm run openapi:bundle
npm start
```

Luego validar:

```bash
curl http://localhost:3000/health
curl http://localhost:3000/openapi.yaml
# Abrir en navegador:
# http://localhost:3000/docs
```

---

## 3. Variables de entorno

### Variables esperadas por `src/App.js`

```env
REMOTE_URL=<URL_BACKEND_FINANZAS_API>
LOCAL_PORT=3000
LOCAL_CONTEXT=/
HEALTH_PATH=/health
UTIL_API_URL=<URL_BACKEND_UTIL>
DOMAIN_OPENAPI=<HOST_PUBLICO_GOOGLE_ENDPOINTS>
KEYCLOAK=<URL_BASE_KEYCLOAK>
JWKS_URL=<URL_JWKS_KEYCLOAK>
AUTH_PUBLIC_KEY=<CERTIFICADO_X509_BASE64>
```

### UAT sugerido / pendiente de confirmar

> Valores de infraestructura como `REMOTE_URL`, `DOMAIN_OPENAPI`, `KEYCLOAK`, `JWKS_URL` y `AUTH_PUBLIC_KEY` deben confirmarse contra GitHub Actions, Kubernetes/Kustomize, Secret Manager o el entorno real de despliegue.

```env
REMOTE_URL=<URL_INTERNA_O_PUBLICA_DE_FINANZAS_API_UAT>
LOCAL_PORT=8080
LOCAL_CONTEXT=/
HEALTH_PATH=/health
UTIL_API_URL=https://uat.fbusinesscenter.com/ppsomx/backend-util
DOMAIN_OPENAPI=<HOST_PUBLICO_DEL_ENDPOINT_UAT>
KEYCLOAK=<URL_BASE_KEYCLOAK_UAT>
JWKS_URL=<URL_JWKS_KEYCLOAK_UAT>
AUTH_PUBLIC_KEY=<SECRET_AUTH_PUBLIC_KEY_BASE64>
```

### Tabla de variables

| Variable | Descripción | Default en código | UAT / valor esperado | ¿Secreta? |
|----------|-------------|-------------------|----------------------|:---------:|
| `REMOTE_URL` | URL base del backend real `finanzas-api` al que se proxyan las peticiones. | `http://localhost:3001` | Pendiente de confirmar | No / depende si es interna |
| `LOCAL_PORT` | Puerto donde escucha este BFF. | `3000` | `8080` o valor definido por deployment | No |
| `LOCAL_CONTEXT` | Contexto base sobre el que se monta el proxy. | `/` | Pendiente de confirmar; puede ser `/` o `/ppsomx/backend-finanzas` según gateway/deployment | No |
| `HEALTH_PATH` | Ruta local de healthcheck del BFF. | `/health` | `/health` | No |
| `UTIL_API_URL` | URL de backend util para consultar atributos de seguridad del usuario. | `http://localhost:3712` | `https://uat.fbusinesscenter.com/ppsomx/backend-util` | No |
| `DOMAIN_OPENAPI` | Host usado para resolver `${DOMAIN_OPENAPI}` dentro del OpenAPI final. | `x-forwarded-host` o `host` request | Pendiente de confirmar | No |
| `KEYCLOAK` | Base URL usada para resolver `${KEYCLOAK}` en `x-google-issuer`. | Vacío | Pendiente de confirmar | No |
| `JWKS_URL` | URL JWKS usada por Google Endpoints para validar JWT. | Vacío | Pendiente de confirmar | No |
| `AUTH_PUBLIC_KEY` | Certificado X509 en Base64 usado para habilitar validación/lectura de contexto. | Vacío | Secret / variable segura | Sí |

**Importante:** no dejar `AUTH_PUBLIC_KEY` ni URLs internas sensibles hardcodeadas en el repositorio. Usar GitHub Secrets, Kustomize Secret, Secret Manager o el mecanismo definido por DevOps.

---

## 4. Arquitectura y dependencias

**Tipo:** Node.js + Express + proxy HTTP + Swagger UI estático + OpenAPI para Google Cloud Endpoints.

```text
Cliente / Portal / Google Endpoints
        │
        ▼
Finanzas BFF
        │
        ├── /docs                         → Swagger UI HTML
        ├── /openapi.yaml                 → OpenAPI final resuelto
        ├── /swagger-ui*.js/css           → Assets Swagger UI
        ├── /health                       → Health local del BFF
        │
        └── Proxy dinámico
              request original: /three-way-match
              target remoto:   REMOTE_URL + /api/three-way-match
                            ▼
                    finanzas-api
```

### Stack

| Capa | Tecnología |
|------|------------|
| Runtime | Node.js |
| Servidor HTTP | Express 5 |
| Proxy | `express-http-proxy` |
| Configuración | `dotenv` |
| Logs | `pino` |
| Swagger UI | `swagger-ui-dist` |
| OpenAPI bundle | `@apidevtools/swagger-cli` |
| Auth / JWT | `jsonwebtoken`, certificado X509 opcional |
| Persistencia | No aplica |
| Tests | Dummy coverage, sin pruebas reales configuradas |

### `package.json`

```json
{
  "type": "module",
  "scripts": {
    "start": "node --max-http-header-size=1048576 ./src/App.js",
    "openapi:bundle": "npx swagger-cli bundle cloud-endpoint/src/root.yaml --outfile cloud-endpoint/openapi.yaml --type yaml",
    "test": "node -e \"const fs=require('fs'); fs.mkdirSync('coverage',{recursive:true}); fs.writeFileSync('coverage/lcov.info','TN:\\nSF:src/App.js\\nDA:1,0\\nLF:1\\nLH:0\\nend_of_record\\n'); console.log('No tests configured - dummy coverage generated');\""
  },
  "dependencies": {
    "body-parser": "^1.20.2",
    "dotenv": "^17.2.0",
    "express": "^5.1.0",
    "express-http-proxy": "^2.1.1",
    "jsonwebtoken": "^9.0.2",
    "pino": "^9.7.0",
    "swagger-ui-dist": "^5.32.4"
  },
  "devDependencies": {
    "@apidevtools/swagger-cli": "^4.0.4"
  }
}
```

---

## 5. Cloud Endpoints / OpenAPI

La definición raíz se encuentra en:

```text
cloud-endpoint/src/root.yaml
```

El archivo bundle final se genera en:

```text
cloud-endpoint/openapi.yaml
```

Comando de generación:

```bash
npm run openapi:bundle
```

El OpenAPI usa versión **Swagger / OpenAPI 2.0**:

```yaml
swagger: "2.0"
info:
  title: Finanzas BFF API
  version: 2.0.0
host: ${DOMAIN_OPENAPI}
schemes:
  - https
x-google-endpoints:
  - allowCors: true
    name: ${DOMAIN_OPENAPI}
```

Variables reemplazadas dinámicamente al servir `/openapi.yaml`:

| Placeholder | Fuente |
|-------------|--------|
| `${DOMAIN_OPENAPI}` | `DOMAIN_OPENAPI`, si existe; si no, `x-forwarded-host`; si no, `host` del request. |
| `${KEYCLOAK}` | Variable de entorno `KEYCLOAK`. |
| `${JWKS_URL}` | Variable de entorno `JWKS_URL`. |

**Rutas públicas de documentación:**

| Ruta | Descripción |
|------|-------------|
| `/docs` | Swagger UI del BFF. |
| `/openapi.yaml` | OpenAPI final servido como YAML. |
| `/swagger-ui.css` | CSS de Swagger UI. |
| `/swagger-ui-bundle.js` | Bundle JS de Swagger UI. |
| `/swagger-ui-standalone-preset.js` | Preset JS de Swagger UI. |
| `/ppsomx/backend-finanzas/docs` | Alias de Swagger UI para el path público del backend Finanzas. |
| `/ppsomx/backend-finanzas/openapi.yaml` | Alias del OpenAPI YAML para el path público del backend Finanzas. |
| `/ppsomx/backend-finanzas/swagger-ui.css` | Alias CSS. |
| `/ppsomx/backend-finanzas/swagger-ui-bundle.js` | Alias JS bundle. |
| `/ppsomx/backend-finanzas/swagger-ui-standalone-preset.js` | Alias JS preset. |

---

## 6. Rutas / dominios publicados

El OpenAPI documenta rutas para los siguientes dominios:

| Dominio | Rutas principales |
|---------|-------------------|
| Healthcheck | `/healthcheck` |
| Accounts Payable | `/accounts-payable`, `/accounts-payable/{uuid}` |
| Account Statement | `/account-statement`, `/account-statement/{uuid}`, `/account-statement/{uuid}/pdf`, `/account-statement/{uuid}/report-data`, `/confirm-review`, `/request-review` |
| Fiscal Payments | `/fiscal-payments`, `/fiscal-payments/{uuid}` |
| Finanzas Payment | `/finanzas-payment`, `/finanzas-payment/header-with-details`, `/finanzas-payment/header-with-details/{paymentHeaderUuid}` |
| Audit Logs | `/audit-logs`, `/audit-logs/export/csv`, `/audit-logs/transaction/{idTransaccion}`, `/audit-logs/{id}` |
| Rebates | `/rebates`, `/rebates/published`, `/rebates/search`, `/rebates/export/csv`, `/rebates/vendor/{vendorNumber}`, `/rebates/{uuid}` |
| Stamped Rebates | `/stamped-rebates`, `/stamped-rebates/export/csv`, `/stamped-rebates/{uuid}` |
| SAP Documents | `/sap-documents`, `/sap-documents/{uuid}` |
| Shipping Guide | `/shipping-guide`, `/shipping-guide/csv`, `/shipping-guide/cancel`, `/shipping-guide/status`, `/shipping-guide/{uuid}`, `/shipping-guide/guide/{idGuide}` |
| Three Way Match | `/three-way-match`, `/three-way-match/export/csv`, `/three-way-match/export/xlsx`, `/three-way-match/run` |
| MIGO | `/migo`, `/migo/upload`, `/migo/reject`, `/migo/{id}`, `/migo/{id}/receptions`, `/migo/{id}/authorize`, `/migo/{id}/export-csv` |
| Vendor Blocks | `/vendor-blocks`, `/vendor-blocks/{uuid}` |
| Purchase Orders | `/purchase-orders`, `/purchase-orders/{uuid}`, `/purchase-orders/listReception`, `/purchase-orders/updateReception`, `/purchase-orders/reception/{uuid}` |
| Carta Porte | `/carta-porte/guia-embarque`, `/carta-porte/oc`, `/carta-porte/all`, `/carta-porte/findAllGuia`, `/carta-porte/updateAllStatusGuia` |

**Nota:** Estas rutas se exponen/documentan en el BFF, pero la ejecución real se proxy a `finanzas-api` agregando el prefijo `/api`.

Ejemplo:

```text
Request al BFF:
GET /three-way-match?tipoFecha=...

Request hacia backend real:
GET ${REMOTE_URL}/api/three-way-match?tipoFecha=...
```

---

## 7. Seguridad y headers

El OpenAPI define mecanismos de seguridad:

```yaml
securityDefinitions:
  api_key:
    in: query
    name: key
    type: apiKey
  bearerAuth:
    in: header
    name: Authorization
    type: apiKey
  keycloak:
    authorizationUrl: ""
    flow: implicit
    scopes: {}
    type: oauth2
    x-google-audiences: vendor-backend
    x-google-issuer: ${KEYCLOAK}/auth/realms/corp
    x-google-jwks_uri: ${JWKS_URL}
    x-google-jwt-locations:
      - header: Authorization
        value_prefix: "Bearer "

security:
  - bearerAuth: []
```

### Flujo de contexto de seguridad en `src/App.js`

El BFF intenta construir contexto de usuario de tres formas:

1. Lee `x-endpoint-api-userinfo`, si viene desde Google Endpoints.
2. Si no viene, intenta decodificar el `Authorization: Bearer <token>`.
3. En modo sin certificado cargado, acepta `x-user-key` como fallback local/dev.

Con el `userKey`, consulta `UTIL_API_URL`:

```text
GET ${UTIL_API_URL}/api/security/user-attributes-by-key/{userKey}
```

El resultado se cachea por 5 minutos y se transforma en headers hacia `finanzas-api`:

| Header inyectado | Origen |
|------------------|--------|
| `x-user-key` | `sub` / `preferred_username` del token o header dev. |
| `x-user-vendors` | Atributos `ATR001` de Util API. |
| `x-user-types` | Atributos `ATR002` de Util API. |
| `x-user-groups` | Atributos `ATR004` de Util API. |

**Importante:** Si no se obtiene `userKey`, el proxy no bloquea la petición; solo omite la inyección de contexto y continúa hacia el backend.

---

## 8. Pipeline / CI-CD

**Archivo observado:** `.github/workflows/pipeline.yml` o `.gitlab-ci.yml` pendiente de confirmar según rama/repositorio real.

Consideraciones para pipeline:

- Debe ejecutar `npm install` o `npm ci`, según exista `package-lock.json`.
- Debe ejecutar `npm run openapi:bundle` antes de empaquetar/deployar para asegurar que `cloud-endpoint/openapi.yaml` esté actualizado.
- El script `npm test` genera cobertura dummy; no valida lógica real.
- Si hay quality gate de Sonar con cobertura mínima, este proyecto requiere excepción o pruebas reales.
- El deploy debe garantizar que `cloud-endpoint/openapi.yaml` exista dentro del artefacto.
- El arranque productivo usa `npm start`.

**Comandos esperados:**

```bash
npm install
npm run openapi:bundle
npm test
npm start
```

---

## 9. Estado actual

**Estado:** Funcional como BFF/proxy y expositor de Swagger, pendiente de validar en ambiente contra infraestructura real.

| Área | Estado |
|------|--------|
| Servidor Express | Implementado |
| Proxy hacia `finanzas-api` | Implementado |
| Swagger UI | Implementado |
| OpenAPI YAML | Implementado / bundle requerido |
| Variables OpenAPI dinámicas | Implementado para `DOMAIN_OPENAPI`, `KEYCLOAK`, `JWKS_URL` |
| Contexto de seguridad vía Util API | Implementado |
| Cache de atributos de usuario | Implementado con TTL 5 minutos |
| Tests reales | No implementados |
| Cobertura | Dummy coverage |
| Persistencia propia | No aplica |

**Bugs / riesgos conocidos:**

| Riesgo | Severidad | Contexto |
|--------|-----------|----------|
| `npm test` genera cobertura dummy | Media / Alta | No valida lógica real; puede ocultar errores del proxy o del OpenAPI. |
| `REMOTE_URL` debe apuntar al backend real y no al mismo endpoint público | Alta | Si apunta al mismo host/path del BFF puede generar loop o errores de routing. |
| `LOCAL_CONTEXT` debe coincidir con cómo el gateway entrega la ruta | Alta | Si Google Endpoints/Kubernetes no strippea igual que el código espera, el proxy puede construir rutas incorrectas. |
| `AUTH_PUBLIC_KEY` vacío desactiva validación de certificado | Media | En local es útil; en ambiente debe revisarse si se requiere validación estricta. |
| Si no hay `userKey`, el request continúa sin headers de contexto | Media | Puede depender de validaciones posteriores en `finanzas-api`; confirmar comportamiento esperado. |
| OpenAPI 2.0 limita documentación de uploads | Media | En MIGO y Carta Porte se documenta multipart sin declarar `type: file` por compatibilidad con Google Cloud Endpoints. |
| `swagger-cli` está deprecado en algunos ecosistemas | Baja / Media | Confirmar si la plantilla corporativa acepta seguir usando `@apidevtools/swagger-cli`. |

---

## 10. Lo que no es obvio

- Este repositorio **no es el backend financiero real**; es una capa intermedia para documentación, Google Endpoints y proxy.
- Las rutas documentadas como `/three-way-match`, `/migo`, `/account-statement`, etc., se reenvían al backend como `/api/three-way-match`, `/api/migo`, `/api/account-statement`, etc.
- El archivo `cloud-endpoint/src/root.yaml` usa `$ref` hacia archivos separados en `paths/` y `definitions/`.
- El archivo que consume Google Endpoints debe ser el bundle final `cloud-endpoint/openapi.yaml`.
- El BFF sirve Swagger UI sin usar `swagger-ui-express`; arma el HTML y sirve directamente assets desde `swagger-ui-dist`.
- Los alias `/ppsomx/backend-finanzas/docs` y `/ppsomx/backend-finanzas/openapi.yaml` existen para que la documentación funcione bajo el path público del portal/gateway.
- `DOMAIN_OPENAPI` puede resolverse dinámicamente desde headers del request si no está seteado.
- `KEYCLOAK` y `JWKS_URL` se reemplazan en runtime dentro del YAML servido.
- El proxy usa `parseReqBody: false`, importante para no romper payloads y multipart/form-data.
- El script de start aumenta `max-http-header-size` a `1048576`; esto apunta a soportar headers grandes, probablemente por tokens/contexto de gateway.
- `MIGO upload` y algunos endpoints Carta Porte con multipart no describen archivos con `type: file` por limitación de Google Cloud Endpoints/OpenAPI 2.0.

---

## 11. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Confirmar valores reales de `REMOTE_URL`, `LOCAL_CONTEXT`, `DOMAIN_OPENAPI`, `KEYCLOAK`, `JWKS_URL` y `AUTH_PUBLIC_KEY` en UAT/PRD. | Alta |
| Confirmar si el repo usa GitHub Actions, GitLab CI o ambos para deploy. | Alta |
| Validar que `npm run openapi:bundle` se ejecute siempre antes del deploy. | Alta |
| Agregar `.env.example` sin secretos con variables mínimas esperadas. | Alta |
| Confirmar con DevOps si `LOCAL_PORT` debe ser `8080`, `3000` u otro valor según GKE/Cloud Run. | Alta |
| Validar routing real: request público → Google Endpoints → BFF → `finanzas-api`. | Alta |
| Confirmar si el BFF debe bloquear cuando no exista `userKey` o seguir pasando la petición como hoy. | Alta |
| Reemplazar cobertura dummy por pruebas mínimas de health, docs, openapi y proxy path resolver. | Media |
| Agregar pruebas de generación OpenAPI / validación de YAML. | Media |
| Documentar el responsable de `backend-util` para el endpoint de atributos de seguridad. | Media |
| Revisar si `swagger-cli` debe migrarse a `@redocly/cli` u otra herramienta aprobada. | Baja / Media |

---

## 12. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

*Completado por:* Oscar Bonelli, basado en código, package y OpenAPI compartidos · *Fecha:* 2026-06-25 · *Aceptado por:* Pendiente
