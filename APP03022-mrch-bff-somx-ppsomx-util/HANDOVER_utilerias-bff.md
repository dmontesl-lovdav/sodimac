# HANDOVER.md — `APP03022-mrch-bff-somx-ppsomx-util`

> Documento de traspaso basado en el código, `package.json`, estructura `cloud-endpoint`, variables de entorno compartidas y handover base del BFF de Finanzas.

**Proyecto:** APP03022-mrch-bff-somx-ppsomx-util  
**Tipo:** BFF / Gateway proxy / Expositor OpenAPI para Google Cloud Endpoints  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch-bff-somx-ppsomx-util  
**Fecha:** 2026-06-30  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Pendiente de confirmar

---

## 1. ¿Qué hace este proyecto?

Este proyecto es un **BFF intermedio para Utilerías / Utils API** dentro del ecosistema **Falabella Business Center / PPSOMX**.

Su función principal es servir como capa de exposición y proxy hacia el backend real `APP03022-mrch.backend.somx.util-api`, reenviando las rutas recibidas hacia la API de Utilerías con el prefijo `/api`.

Además, el repositorio contiene configuración `cloud-endpoint` para generar el contrato OpenAPI que será consumido por Google Cloud Endpoints o por el mecanismo de publicación definido en infraestructura.

No contiene persistencia propia ni lógica de negocio principal. La lógica de catálogos, parámetros, seguridad, usuarios, permisos, auditoría, procesos, proveedores y bloqueos vive en el backend **`APP03022-mrch.backend.somx.util-api`**.

**Uso principal:** publicar contratos OpenAPI 2.0 para Google Cloud Endpoints y enrutar tráfico hacia `util-api`.

**Consumidores / usuarios:** Google Cloud Endpoints, portal FBC, micro frontend Utilerías, micro frontends que consumen catálogos/parámetros/seguridad y clientes internos autorizados.

**Criticidad:** Alta. Aunque no contiene lógica de negocio principal, es parte del camino de exposición de APIs compartidas de Utilerías, catálogos, parámetros y seguridad.

**Documentación adicional:**

| Recurso | Link / Estado |
|---------|---------------|
| Repositorio | [APP03022-mrch-bff-somx-ppsomx-util](https://github.com/falabella-stores-and-merchandise/APP03022-mrch-bff-somx-ppsomx-util) |
| Backend destino | `APP03022-mrch.backend.somx.util-api` |
| OpenAPI YAML | Generado en `cloud-endpoint/openapi.yaml` mediante `npm run openapi:bundle` |
| Google Cloud Endpoints | Configurado desde `cloud-endpoint/openapi.yaml` |
| Postman collection | No disponible |
| Confluence / documentación técnica | [Documentación Utilerías Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897549760/Documentacion+Utilerias+Dev) |
| Tablero Jira / backlog | No disponible |
| README del repositorio | [README.md](README.md) |

---

## 2. Cómo levantar el proyecto

**Requisitos:** Node.js · npm · acceso al repositorio · variables de entorno del ambiente

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch-bff-somx-ppsomx-util
cd APP03022-mrch-bff-somx-ppsomx-util

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
```

---

## 3. Variables de entorno

### Variables esperadas por `src/App.js`

```env
REMOTE_URL=<URL_BACKEND_UTIL_API>
LOCAL_PORT=3000
LOCAL_CONTEXT=/
HEALTH_PATH=/health
AUTH_PUBLIC_KEY=<CERTIFICADO_X509_BASE64>
```

### UAT / ambiente compartido

```env
REMOTE_URL=http://mrch-backend-somx-util-api:8080
LOCAL_PORT=8080
LOCAL_CONTEXT=/
HEALTH_PATH=/health
AUTH_PUBLIC_KEY=
```

### Tabla de variables

| Variable | Descripción | Default en código | UAT / valor esperado | ¿Secreta? |
|----------|-------------|-------------------|----------------------|:---------:|
| `REMOTE_URL` | URL base del backend real `util-api` al que se proxyan las peticiones. | `http://localhost:3001` | `http://mrch-backend-somx-util-api:8080` | No / interna |
| `LOCAL_PORT` | Puerto donde escucha este BFF. | `3000` | `8080` | No |
| `LOCAL_CONTEXT` | Contexto base sobre el que se monta el proxy. | `/` | `/` | No |
| `HEALTH_PATH` | Ruta local de healthcheck del BFF. | `/health` | `/health` | No |
| `AUTH_PUBLIC_KEY` | Certificado X509 en Base64 usado para habilitar validación/lectura de contexto si aplica. | Vacío | Vacío / pendiente de confirmar | Sí, si se configura |

**Importante:** no dejar `AUTH_PUBLIC_KEY` hardcodeado en el repositorio. Si se usa en ambiente, debe vivir en GitHub Secrets, Kustomize Secret, Secret Manager o el mecanismo definido por DevOps.

---

## 4. Arquitectura y dependencias

**Tipo:** Node.js + Express + proxy HTTP + OpenAPI para Google Cloud Endpoints.

```text
Cliente / Portal / Google Endpoints
        │
        ▼
Utilerías BFF
        │
        ├── /health                       → Health local del BFF
        │
        └── Proxy dinámico
              request original: /catalogs
              target remoto:   REMOTE_URL + /api/catalogs
                            ▼
                       util-api
```

### Comportamiento del proxy

El proxy recibe una petición bajo `LOCAL_CONTEXT`, elimina ese contexto de la URL original y reenvía al backend destino agregando el prefijo `/api`.

Ejemplo:

```text
Request al BFF:
GET /security/user-attributes-by-key/{userKey}

Request hacia backend real:
GET ${REMOTE_URL}/api/security/user-attributes-by-key/{userKey}
```

La lógica relevante en `src/App.js` es:

```js
const targetPath = request.originalUrl.replace(localContext, "");
const normalizedPath = targetPath.startsWith("/") ? targetPath : "/" + targetPath;
return "/api" + normalizedPath;
```

### Stack

| Capa | Tecnología |
|------|------------|
| Runtime | Node.js |
| Servidor HTTP | Express 5 |
| Proxy | `express-http-proxy` |
| Body parser | `body-parser` |
| Configuración | `dotenv` |
| Logs | `pino` |
| OpenAPI bundle | `@apidevtools/swagger-cli` |
| Auth / JWT | `jsonwebtoken`, certificado X509 opcional |
| Persistencia | No aplica |
| Tests | Dummy coverage, sin pruebas reales configuradas |

### `package.json`

```json
{
  "name": "mrch.bff.somx.ppsomx.util",
  "version": "1.0.0",
  "description": "BFF para Utils API - Herramientas y Utilerias",
  "type": "module",
  "scripts": {
    "start": "node --max-http-header-size=1048576 ./src/App.js",
    "openapi:bundle": "npx swagger-cli bundle cloud-endpoint/src/root.yaml --outfile cloud-endpoint/openapi.yaml --type yaml",
    "test": "node -e \"const fs=require('fs'); fs.mkdirSync('coverage',{recursive:true}); fs.writeFileSync('coverage/lcov.info','TN:\\nSF:src/App.js\\nDA:1,0\\nLF:1\\nLH:0\\nend_of_record\\n'); console.log('No tests configured - dummy coverage generated');\""
  },
  "dependencies": {
    "dotenv": "^17.2.0",
    "express": "^5.1.0",
    "express-http-proxy": "^2.1.1",
    "body-parser": "^1.20.2",
    "jsonwebtoken": "^9.0.2",
    "pino": "^9.7.0"
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

El repositorio contiene archivos separados bajo `cloud-endpoint/src/paths` y componentes comunes/definiciones referenciadas desde el root YAML.

**Nota:** en el `src/App.js` compartido solo se observa healthcheck y proxy. Confirmar si la publicación de `/docs` o `/openapi.yaml` se sirve desde este mismo BFF en otra versión del archivo, desde infraestructura, o si el YAML se consume directamente como artefacto por Google Cloud Endpoints.

---

## 6. Rutas / dominios publicados

Según la estructura visible del OpenAPI y el backend util, el BFF expone/documenta rutas asociadas a los siguientes dominios:

| Dominio | Uso esperado |
|---------|--------------|
| Health | Validación de vida del BFF. |
| Security | Usuarios, roles, permisos, perfiles, atributos y resolución de contexto de seguridad. |
| Catalog | Catálogos generales usados por micro frontends y backends. |
| Catalog Element | Elementos de catálogos y operaciones relacionadas. |
| Catalog Management | Administración de catálogos. |
| Parameter | Parámetros del sistema. |
| Module | Aplicativos / módulos del portal. |
| Process | Procesos / eventos relacionados con módulos. |
| Conversion | Conversiones entre elementos o catálogos. |
| Audit Log | Bitácora de actividades y trazabilidad. |
| Status Train | Tren de estados / consulta de trazabilidad de estatus. |
| Supplier | Proveedores. |
| Supplier Block | Bloqueos de proveedor. |
| Layout Validation | Validaciones de layout / cargas. |
| Messages | Mensajes o catálogos de mensajes operativos. |
| Item / Item Type | Ítems y tipos de ítem. |

**Nota:** estas rutas se exponen/documentan en el BFF, pero la ejecución real se proxy a `util-api` agregando el prefijo `/api`.

---

## 7. Seguridad y headers

El archivo `src/App.js` carga `AUTH_PUBLIC_KEY` y, si existe, intenta convertirlo desde Base64 a certificado X509:

```js
const authPublicKey = process.env.AUTH_PUBLIC_KEY || '';
let decodedAuthKey = '';

if (authPublicKey) {
  decodedAuthKey = new X509Certificate(Buffer.from(authPublicKey, "base64")).toString();
}
```

Con el código compartido, el certificado se carga y se deja disponible en memoria, pero no se observa una validación JWT activa dentro del proxy. Si `AUTH_PUBLIC_KEY` no está definido, el servicio registra:

```text
AUTH_PUBLIC_KEY not set - JWT validation disabled
```

**Pendiente importante:** confirmar si la validación de JWT queda a cargo de Google Cloud Endpoints, del backend `util-api`, de este BFF en otra versión del archivo, o de una capa previa de infraestructura.

---

## 8. Payload y límites

El BFF incrementa el tamaño máximo de payload a `66mb` usando `body-parser`:

```js
const maximumPayloadSize = '66mb';
localService.use(bodyParser.json({ limit: maximumPayloadSize }));
localService.use(bodyParser.raw({ limit: maximumPayloadSize }));
localService.use(bodyParser.urlencoded({ limit: maximumPayloadSize, extended: true }));
```

También arranca Node con:

```bash
node --max-http-header-size=1048576 ./src/App.js
```

Esto sugiere que el servicio debe soportar headers grandes, posiblemente por tokens, claims o contexto inyectado por gateway.

**Nota técnica:** en el código compartido, el proxy se registra antes de los `bodyParser`. Confirmar si esto es intencional; para rutas proxyeadas, el body parsing puede depender principalmente de `express-http-proxy` y su opción `parseReqBody: true`.

---

## 9. Pipeline / CI-CD

El repositorio contiene configuración de pipeline y kustomization por ambiente:

```text
.github/workflows/pipeline.yml
kustomization/development
kustomization/uat
kustomization/production
```

Consideraciones para pipeline:

- Debe ejecutar `npm install` o `npm ci`, según exista `package-lock.json` estable.
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

## 10. Estado actual

**Estado:** Funcional como BFF/proxy y generador de OpenAPI, pendiente de validar en ambiente contra infraestructura real.

| Área | Estado |
|------|--------|
| Servidor Express | Implementado |
| Proxy hacia `util-api` | Implementado |
| Healthcheck | Implementado |
| OpenAPI YAML | Implementado / bundle requerido |
| Certificado X509 opcional | Cargado si existe `AUTH_PUBLIC_KEY` |
| Tests reales | No implementados |
| Cobertura | Dummy coverage |
| Persistencia propia | No aplica |

**Bugs / riesgos conocidos:**

| Riesgo | Severidad | Contexto |
|--------|-----------|----------|
| `npm test` genera cobertura dummy | Media / Alta | No valida lógica real; puede ocultar errores del proxy o del OpenAPI. |
| `REMOTE_URL` debe apuntar al backend real y no al mismo endpoint público | Alta | Si apunta al mismo host/path del BFF puede generar loop o errores de routing. |
| `LOCAL_CONTEXT` debe coincidir con cómo el gateway entrega la ruta | Alta | Si Google Endpoints/Kubernetes no strippea igual que el código espera, el proxy puede construir rutas incorrectas. |
| `AUTH_PUBLIC_KEY` vacío desactiva validación de certificado | Media | En local puede ser útil; en ambiente debe confirmarse si se requiere validación estricta. |
| Certificado cargado pero no usado explícitamente | Media | En el código compartido se carga `decodedAuthKey`, pero no se observa validación JWT. |
| Body parsers registrados después del proxy | Media | Puede ser intencional, pero conviene validar cargas grandes o multipart. |
| `swagger-cli` puede considerarse legacy/deprecated | Baja / Media | Confirmar si la plantilla corporativa permite seguir usando `@apidevtools/swagger-cli`. |

**Deuda técnica:**

- Agregar `.env.example` sin secretos y con comentarios por variable.
- Confirmar publicación real del OpenAPI / Swagger por ambiente.
- Confirmar si el BFF debe servir `/docs` y `/openapi.yaml` directamente o solo generar el artefacto.
- Confirmar si `AUTH_PUBLIC_KEY` debe activar validación JWT real en el BFF.
- Reemplazar cobertura dummy por pruebas mínimas.
- Documentar rutas exactas por dominio desde `cloud-endpoint/openapi.yaml`.
- Confirmar integración con Google Cloud Endpoints y routing público.

---

## 11. Lo que no es obvio

- Este repositorio **no es el backend real de Utilerías**; es una capa intermedia para documentación/OpenAPI, Google Endpoints y proxy.
- Las rutas recibidas se reenvían al backend agregando `/api` antes del path original.
- `REMOTE_URL` en UAT apunta al servicio interno `mrch-backend-somx-util-api:8080`.
- El archivo que debe consumir Google Endpoints es el bundle final `cloud-endpoint/openapi.yaml`.
- El script de start aumenta `max-http-header-size` a `1048576`, probablemente para soportar tokens o headers grandes.
- El código carga `AUTH_PUBLIC_KEY` como certificado X509, pero con el fragmento compartido no se ve una validación JWT aplicada al proxy.
- `parseReqBody` está en `true`, distinto a otros BFF donde puede estar en `false` para no tocar multipart/payloads grandes.
- Los `bodyParser` se registran después del proxy; conviene validar comportamiento real con cargas grandes.

---

## 12. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Confirmar valores reales de `REMOTE_URL`, `LOCAL_CONTEXT`, `LOCAL_PORT`, `HEALTH_PATH` y `AUTH_PUBLIC_KEY` en DEV/UAT/PRD. | Alta |
| Confirmar si el BFF debe servir Swagger UI o solo generar OpenAPI para Cloud Endpoints. | Alta |
| Validar que `npm run openapi:bundle` se ejecute siempre antes del deploy. | Alta |
| Agregar `.env.example` sin secretos con variables mínimas esperadas. | Alta |
| Validar routing real: request público → Google Endpoints → BFF → `util-api`. | Alta |
| Confirmar si la validación JWT debe vivir en el BFF, Google Endpoints o `util-api`. | Alta |
| Probar requests con payload grande y multipart, si aplica. | Media |
| Reemplazar cobertura dummy por pruebas mínimas de health, openapi y proxy path resolver. | Media |
| Agregar pruebas de generación OpenAPI / validación de YAML. | Media |
| Revisar si `swagger-cli` debe migrarse a `@redocly/cli` u otra herramienta aprobada. | Baja / Media |

---

## 13. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

## 14. Checklist de traspaso

| Ítem | Estado |
|------|--------|
| Repositorio confirmado | Completado |
| `package.json` revisado | Completado |
| Variables principales identificadas | Completado |
| Secretos removidos del handover | Completado |
| Scripts documentados | Completado |
| CI/CD documentado a nivel general | Parcial |
| OpenAPI bundle identificado | Completado |
| Rutas exactas por endpoint | Pendiente |
| Swagger público confirmado | Pendiente |
| Seguridad/JWT por ambiente confirmada | Pendiente |
| Routing público confirmado | Pendiente |
| Cobertura actual validada | Pendiente |

---

*Completado por:* Oscar Bonelli, basado en código, package, variables y OpenAPI compartidos · *Fecha:* 2026-06-30 · *Aceptado por:* Pendiente
