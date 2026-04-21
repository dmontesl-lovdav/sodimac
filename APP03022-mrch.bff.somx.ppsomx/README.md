# Documentación técnica BFF APP03022 – Aclaraciones (MVP)

> Nota: Esta documentación se ha generado únicamente a partir del código disponible en el repositorio (principalmente `src/App.js`).

---

## 1. Descripción general del servicio / API

### 1.1. Propósito

Este proyecto implementa un **Backend For Frontend (BFF)** que actúa como **proxy HTTP** hacia un servicio remoto configurado mediante la variable de entorno `REMOTE_URL`.

Responsabilidades observadas:

- Exponer un punto de entrada HTTP local (puerto `LOCAL_PORT`, por defecto configurado en kustomize para cada ambiente).
- Reencaminar las peticiones bajo un contexto local (`LOCAL_CONTEXT`) hacia el servicio remoto (`REMOTE_URL`).
- Proveer un endpoint de salud (`HEALTH_PATH`).
- Ajustar el tamaño máximo del payload entrante (hasta `66mb`).
- Preparar la validación de autenticación basada en un certificado X.509 (aunque la verificación JWT está comentada en el código actual).

No se observan controladores de dominio propios ni lógica de negocio específica: el BFF actúa principalmente como **reverse proxy**.

### 1.2. Flujo funcional (alto nivel)

1. El cliente realiza una solicitud HTTP hacia el BFF (puerto `LOCAL_PORT`, contexto `LOCAL_CONTEXT`).
2. El BFF:
   - Aumenta el límite del tamaño del cuerpo de la petición (`body-parser`).
   - Registra logs de la solicitud (pino).
   - Si la ruta es de salud (`HEALTH_PATH`), responde directamente con `200` y `{"message": "healthy"}`.
   - Para el resto de rutas:
     - Reescribe el path removiendo `LOCAL_CONTEXT` y proxifica a `REMOTE_URL`.
     - (Preparado) puede verificar JWT usando `AUTH_PUBLIC_KEY` si se habilita la lógica de verificación.
3. La respuesta del servicio remoto es retornada al cliente por el BFF.

### 1.3. Casos de uso

- Healthcheck del BFF: `GET ${HEALTH_PATH}` → 200.
- Proxy de peticiones de frontend hacia backend de Aclaraciones: cualquier método bajo `LOCAL_CONTEXT`.

### 1.4. Usuarios o sistemas consumidores

- Frontends (SPA) que deben consumir el backend de Aclaraciones a través de un BFF local/central.
- Sistemas de infraestructura que realizan healthchecks (Kubernetes, Load Balancer).

---

## 2. Stack tecnológico

### 2.1. Lenguajes

- JavaScript (Node.js) — el proyecto utiliza ESM (`import`), por lo que Node >= 16 es recomendado; se sugiere Node 18 para compatibilidad.

### 2.2. Frameworks / runtime

- Express 5.x (según package.json) como servidor HTTP.
- Start script definido en package.json:
  - "start": "node --max-http-header-size=1048576 ./src/App.js"

### 2.3. Librerías clave

- express-http-proxy — proxy reverso.
- body-parser — parsing con límite de 66mb.
- pino — logging.
- dotenv — carga de variables de entorno.
- jsonwebtoken — presente (verificación JWT preparada, código comentado en App.js).

### 2.4. Dependencias externas

- Servicio remoto definido en `REMOTE_URL`. En este workspace, el overlay de `development` define:
  - REMOTE_URL=http://mrch-backend-somx-aclaraciones-api:8082
  - LOCAL_PORT=8080
  - LOCAL_CONTEXT=/
  - AUTH_PUBLIC_KEY (clave pública X.509 en base64) — definida en kustomization/development/env
  Esto indica que en desarrollo el BFF se despliega frente al servicio `mrch-backend-somx-aclaraciones-api:8082`.

### 2.5. Requerimientos mínimos de ejecución

- Node.js >= 16 (recomendado 18).
- npm (o yarn).
- Variables de entorno mínimas: REMOTE_URL, LOCAL_PORT, LOCAL_CONTEXT, HEALTH_PATH y opcionalmente AUTH_PUBLIC_KEY.

---

## 3. Arquitectura técnica

### 3.1. Diagrama de arquitectura (texto)

- Cliente (frontend) → BFF (Express) → Servicio remoto (REMOTE_URL).
- En Kubernetes (KGE) se despliega el BFF mediante los manifiestos en `kustomization/*` y el LB de GCP enruta tráfico según host/path.

### 3.2. Diagrama de secuencia – flujo principal (proxy)

1. Cliente → BFF (POST/GET/etc.).
2. BFF aplica body-parsers, logging, healthcheck shortcut.
3. BFF reescribe la ruta y proxifica a `REMOTE_URL` con express-http-proxy.
4. Servicio remoto responde; BFF reenvía respuesta al cliente.
5. En error de proxy, `proxyErrorHandler` registra y devuelve 500.

### 3.3. Integraciones con terceros

- Servicio remoto de Aclaraciones (especificado por `REMOTE_URL`).
- IdP / JWKS si se habilita verificación JWT (el OpenAPI y securityDefinitions en `cloud-endpoint/openapi.yaml` refieren a Keycloak/JWKS variables: ${KEYCLOAK}, ${JWKS_URL}).
- Observabilidad: el proyecto usa `pino` y puede integrarse con la plataforma de logs de la organización.

### 3.4. Componentes internos y responsabilidades

- `src/App.js` — módulo único que:
  - Carga configuración (dotenv).
  - Decodifica `AUTH_PUBLIC_KEY` a `X509Certificate` (actualmente sólo lectura).
  - Configura proxy con `express-http-proxy` (path rewrite, option decorator, error handler).
  - Define health endpoint y arranca servidor en `LOCAL_PORT`.

---

## 4. Inventario de endpoints expuestos por el BFF

El BFF no define endpoints de negocio propios salvo el `HEALTH_PATH`. Todas las demás rutas son proxificadas a `REMOTE_URL`.

- Healthcheck:
  - GET ${HEALTH_PATH} → 200 { "message": "healthy" }
- Proxy general:
  - Todas las rutas bajo `LOCAL_CONTEXT` son redirigidas a `REMOTE_URL` con reescritura de path.

Nota práctica: el repositorio incluye `cloud-endpoint/openapi.yaml` que describe la API del backend remoto (Requests, Attachments, Catalogs, etc.). Para generar el inventario completo de endpoints consumidos por clientes front-end, usar ese OpenAPI como fuente primaria y/o capturar tráfico real del frontend a través del BFF.

---

## 5. Runbook / Manual operativo (BFF específico)

### 5.1. Iniciar localmente (desarrollo)

- El overlay `kustomization/development/env` contiene valores útiles para pruebas y despliegue en entorno dev:
  - REMOTE_URL=http://mrch-backend-somx-aclaraciones-api:8082
  - LOCAL_PORT=8080
  - LOCAL_CONTEXT=/
  - AUTH_PUBLIC_KEY= (clave pública X.509 en base64, ya presente)
- Para pruebas locales rápidas:
  1. Crear un archivo `.env` en la raíz con los pares de variables anteriores (o exportarlas en PowerShell):
     - REMOTE_URL=http://mrch-backend-somx-aclaraciones-api:8082
     - LOCAL_PORT=8080
     - LOCAL_CONTEXT=/
     - HEALTH_PATH=/health
     - AUTH_PUBLIC_KEY=<valor-from-kustomization/development/env>
  2. Ejecutar:
     - `npm install`
     - `npm start` (usa el script: node --max-http-header-size=1048576 ./src/App.js)
  3. Verificar health: `curl http://localhost:8080/health` → 200 { "message":"healthy" }

### 5.2. Variables de entorno (resumen y valores detectados)

- REMOTE_URL: http://mrch-backend-somx-aclaraciones-api:8082 (development overlay)
- LOCAL_PORT: 8080 (development overlay)
- LOCAL_CONTEXT: / (development overlay)
- HEALTH_PATH: /health (default en App.js)
- AUTH_PUBLIC_KEY: clave pública X.509 (base64) provista en kustomization/development/env

### 5.3. Dependencias externas

- Servicio `mrch-backend-somx-aclaraciones-api:8082` en dev (REMOTE_URL).
- Keycloak / JWKS endpoints referenciados en `cloud-endpoint/openapi.yaml` variables (${KEYCLOAK}, ${JWKS_URL}) si se habilita seguridad en el backend.

### 5.4. Ejecutar pruebas

- No se incluyen tests automatizados en este repo. Recomendado:
  - Unit tests para proxyReqPathResolver, proxyReqOptDecorator.
  - E2E test que arranque el BFF apuntando a un mock del `REMOTE_URL` y valide proxificación y manejo de errores.

### 5.5. Troubleshooting

- Si el servidor no inicia:
  - Confirmar Node.js >= 16.
  - Revisar que `AUTH_PUBLIC_KEY` no provoque excepción durante decodificación (si está mal codificada, X509Certificate lanzará error).
- Si el BFF retorna 500 en proxificación:
  - Revisar logs del BFF (pino) y del backend (REMOTE_URL).
  - ProxyErrorHandler en `src/App.js` registra `ABORTING REQUEST WITH CODE ...`.
- Si requests largas fallan: revisar límite de `66mb` y memoria del contenedor.

### 5.6. Logs esperados

- " LOADING ENV "
- " CONFIGURING AUTH CERTS "
- " CONFIGURING PROXY "
- "LISTENING ON PORT: <LOCAL_PORT> CONTEXT: <LOCAL_CONTEXT> REMOTE: <REMOTE_URL>"
- "ACCEPTING NEW REQUEST <METHOD>: <originalUrl> - HEADERS: (...) - STATUS: <statusCode>"
- Warnings en `proxyErrorHandler` si ocurren fallos.

### 5.7. Comandos útiles

- `npm install`
- `npm start`  (arranca: node --max-http-header-size=1048576 ./src/App.js)
- `curl http://localhost:8080/health`

---

## 6. Estructura del proyecto (BFF workspace)

- package.json — contiene script `start` y dependencias (dotenv, express, express-http-proxy, jsonwebtoken, pino, body-parser).
- src/App.js — código fuente único con la lógica del proxy y healthcheck.
- cloud-endpoint/openapi.yaml — especificación OpenAPI (Swagger 2.0) del servicio remoto de Aclaraciones; sirve como fuente para inventario de endpoints.
- kustomization/* — manifiestos y archivos env por ambiente (development/uat/production) que inyectan `REMOTE_URL`, `LOCAL_PORT`, `AUTH_PUBLIC_KEY`, etc.

---

## 7. Configuraciones clave

### 7.1. Configuración del servidor

- Server arranca con `node --max-http-header-size=1048576 ./src/App.js`.
- Body parser límite: `66mb`.
- Health endpoint: `${HEALTH_PATH}` (por defecto `/health`).

### 7.2. Seguridad

- `AUTH_PUBLIC_KEY` está presente en el overlay de desarrollo; sin embargo la verificación JWT en `proxyReqOptDecorator` está comentada actualmente.
- Recomendación inmediata: activar verificación JWT en el BFF o asegurar que el backend remoto valide tokens y no confíe en encabezados provistos por clientes.
- Añadir `cors` y `helmet` según políticas frontend/backends.

### 7.3. Middlewares

- body-parser (json, raw, urlencoded) con límite 66mb.
- express-http-proxy con hooks: `proxyReqPathResolver`, `proxyReqOptDecorator`, `proxyErrorHandler`.

### 7.4. CI/CD

- El repositorio contiene `kustomization` por ambiente; se espera pipeline que:
  - Build image Node, instalar dependencias, empaquetar artefacto.
  - Push a registry y aplicar overlays de kustomize en GKE/KGE.

### 7.5. Parámetros por ambiente

- Valores por ambiente deben obtenerse de `kustomization/<env>/env` y secrets; development ya documentado en este repo.

---

## 8. Checklist técnico

### 8.1. Buenas prácticas aplicadas

- Uso de variables de entorno para parametrización.
- Healthcheck explícito.
- Uso de `pino` para logging de alto rendimiento.

### 8.2. Riesgos identificados

- Verificación JWT comentada: riesgo de confianza indebida en headers.
- Límite de payload elevado (66mb) incrementa riesgo de uso de memoria.
- Logging de headers completos puede exponer información sensible en logs.

### 8.3. Recomendaciones

- Activar y testear verificación JWT con `AUTH_PUBLIC_KEY` o delegar verificación al backend remoto.
- Añadir `helmet`, `cors` y `express-rate-limit`.
- Enmascarar headers sensibles en logs.
- Añadir pruebas automatizadas y dividir `App.js` en módulos para facilitar tests.

---

## 9. Anexos y pasos para completar documentación faltante

- `cloud-endpoint/openapi.yaml` es la fuente para completar inventario de endpoints y contratos. Usar `swagger-cli` o `swagger-editor` para validar y extraer ejemplos.
- Para completar tests y ejemplos reales:
  1. Arrancar BFF apuntando a un entorno staging del backend (`REMOTE_URL`).
  2. Ejecutar flujos en el frontend y capturar peticiones (Network tab) para poblar CSV de endpoints.

---

## 10. Entregables

- docs/BFF-APP03022-Documentacion.md (este documento).
- docs/BFF-APP03022-Endpoints.csv (inventario aproximado / placeholder).

---

Notas finales:
- Este documento incorpora información encontrada en el workspace: `package.json`, `src/App.js`, `kustomization/development/env` y `cloud-endpoint/openapi.yaml`. Las instrucciones y pasos para completar información PENDIENTE están incluidas en las secciones correspondientes.
