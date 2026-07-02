# HANDOVER.md — `APP03022-mrch.frontend.somx.fiscal.spa`

> Documento de traspaso basado en los fuentes, configuración, rutas y variables de entorno compartidas para el repositorio.

**Proyecto:** APP03022-mrch.frontend.somx.fiscal.spa  
**Tipo:** MFE  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.fiscal.spa  
**Fecha:** 2026-06-29  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Carlos Rojas Burgos — cjrojasb@Falabella.cl  

---

## 1. ¿Qué hace este proyecto?

Este proyecto es un micro frontend React para el dominio de **Fiscal** dentro del portal **Falabella Business Center / PPSOMX**. Centraliza flujos fiscales utilizados por proveedores y usuarios internos, principalmente consulta de facturas, complementos de pago, publicación de complementos y notas de crédito.

El frontend no contiene persistencia ni lógica de negocio principal; consume servicios backend mediante `API_BASE_URL`, `REACT_APP_API_BASE_URL`, `CATALOGS_API_URL`, `FINANZAS_API_URL` y rutas auxiliares como `API_PROVIDERS`. Además, se integra con el MFE remoto de autenticación (`authentication/App`) y con el estado global del portal.

Los flujos visibles desde el código se agrupan en módulos fiscales. La pantalla principal `/fiscal` muestra el home del dominio Fiscal y permite navegar hacia facturas, complementos de pago y notas de crédito.

**Consumidores / usuarios:** proveedores del portal FBC y perfiles internos con permisos sobre módulos fiscales.  
**Criticidad:** Alta. El MFE soporta operación fiscal, consulta de documentos, publicación de complementos de pago y publicación de notas de crédito.

**Documentación adicional:**

| Recurso | Link |
|---------|------|
| Repositorio | [APP03022-mrch.frontend.somx.fiscal.spa](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.fiscal.spa) |
| Confluence / documentación técnica | [Documentación Fiscal Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897680752/Documentacion+Fiscal+Dev) |
| Tablero Jira / backlog | No disponible |
| Figma / diseño UI | No disponible para Fiscal |
| Storybook | No disponible |
| README del repositorio | [README.md](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.fiscal.spa/blob/develop/README.md) |

---

## 2. Cómo levantar el proyecto

**Requisitos:** Node `>=20.18.1` · npm · acceso al repositorio · variables de entorno de desarrollo

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.fiscal.spa
cd APP03022-mrch.frontend.somx.fiscal.spa

# usar como base la rama develop
git checkout develop
git pull origin develop

# crear .env.development con las variables de DEV
# crear .env.production o inyectar variables desde GitHub Actions para UAT/producción

npm install
npm start
```

**Scripts disponibles:**

```bash
npm start
npm run dev
npm run build
npm test
npm run test:cov
```

| Script | Descripción |
|--------|-------------|
| `npm start` | Levanta `webpack serve` en modo development. |
| `npm run dev` | Alias de desarrollo, equivalente a `npm start`. |
| `npm run build` | Ejecuta build productivo con Webpack. |
| `npm test` | Ejecuta Jest con `--passWithNoTests`. |
| `npm run test:cov` | Ejecuta el script de cobertura `scripts/test-cov.sh`. |

**Notas de arranque local:**

- El proyecto usa `webpack serve` para desarrollo local.
- El build productivo usa `webpack --mode production --config config/webpack.config.js`.
- El puerto local se toma desde `APP_PORT`; si no existe, `webpack.config.js` usa `3703` como fallback.
- El `publicPath` se toma desde `APP_URL`; si no existe, usa `http://localhost:3703/` como fallback.
- El proyecto expone `./App` y `./Card` mediante `ModuleFederationPlugin`.
- El MFE se monta con `single-spa-react` y exporta los lifecycle hooks `bootstrap`, `mount` y `unmount`.
- Las rutas internas usan `HashRouter`.
- En modo integrado al portal (`APP_DEV=false`), la app se suscribe al estado global de autenticación y configuración del shell.
- En modo desarrollo local (`APP_DEV=true`), se omite la suscripción al estado global del portal y el `ApiClient` no exige token.
- El archivo de variables cargado por Webpack se resuelve con `NODE_ENV`:
  - `NODE_ENV=development` → `.env.development`
  - `NODE_ENV=production` → `.env.production`

**Servicios externos necesarios para correr localmente:**

| Servicio / MFE / API | Uso | URL local / ambiente |
|----------------------|-----|----------------------|
| MFE `authentication` | Login / autenticación remota | `authentication@http://localhost:3001/remoteEntry.js` por default |
| Backend Fiscal | Facturas, complementos de pago, notas de crédito y descargas fiscales | `API_BASE_URL` / `REACT_APP_API_BASE_URL` |
| Backend Util / Catálogos | Catálogos y servicios auxiliares | `CATALOGS_API_URL` |
| Backend Finanzas | Integraciones con información financiera | `FINANZAS_API_URL` |
| Proveedores | Consulta de proveedores / suppliers | `API_PROVIDERS` |
| Shell / portal FBC | Host single-spa y estado global | Pendiente de confirmar |

---

## 3. Variables de entorno

### UAT

```env
APP_NAME=fiscal
APP_PATH=/fiscal
APP_URL=https://uat.fbusinesscenter.com/ppsomx/frontend/fiscal/
API_BASE_URL=https://uat.fbusinesscenter.com/ppsomx/fiscal
CATALOGS_API_URL=https://uat.fbusinesscenter.com/ppsomx/backend-util
FINANZAS_API_URL=https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/api
API_PROVIDERS=/suppliers

APP_URL_PREFIX=https://uat.fbusinesscenter.com/ppsomx/frontend/
LOGIN_URL=https://uat.fbusinesscenter.com/login
AUTH_CONFIG_CLIENT=portal
AUTHENTICATION_APP=authentication@https://uat.fbusinesscenter.com/authentication/remoteEntry.js?v=${CI_COMMIT_SHA}
STORE_DEBUG=false
APP_DEV=false
```

### Tabla de variables

| Variable | Descripción | UAT | ¿Secreta? |
|----------|-------------|-----|:---------:|
| `APP_NAME` | Nombre del micro frontend / remoto Module Federation | `fiscal` | No |
| `APP_PATH` | Ruta base del MFE dentro del portal | `/fiscal` | No |
| `APP_PORT` | Puerto local para `webpack-dev-server`; si no existe usa `3703` | No definida | No |
| `APP_URL` | URL pública del MFE / `publicPath` de Webpack | `https://uat.fbusinesscenter.com/ppsomx/frontend/fiscal/` | No |
| `APP_URL_PREFIX` | Prefijo base de frontends del portal | `https://uat.fbusinesscenter.com/ppsomx/frontend/` | No |
| `LOGIN_URL` | URL de login del portal | `https://uat.fbusinesscenter.com/login` | No |
| `AUTH_CONFIG_CLIENT` | Cliente de autenticación usado por el portal | `portal` | No |
| `AUTHENTICATION_APP` | Remote del MFE de autenticación | `authentication@https://uat.fbusinesscenter.com/authentication/remoteEntry.js?v=${CI_COMMIT_SHA}` | No |
| `STORE_DEBUG` | Habilita debug del store | `false` | No |
| `APP_DEV` | Modo desarrollo / bypass local de estado global y token | `false` | No |
| `API_BASE_URL` | Base URL del backend Fiscal | `https://uat.fbusinesscenter.com/ppsomx/fiscal` | No |
| `REACT_APP_API_BASE_URL` | Base URL alternativa leída por `ApiClient`; tiene prioridad sobre `API_BASE_URL` si existe | No definida | No |
| `CATALOGS_API_URL` | Base URL de catálogos / backend util | `https://uat.fbusinesscenter.com/ppsomx/backend-util` | No |
| `FINANZAS_API_URL` | Base URL del backend Finanzas usada para integraciones | `https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/api` | No |
| `API_PROVIDERS` | Ruta auxiliar para proveedores / suppliers | `/suppliers` | No |
| `REACT_APP_AUTH_DEFAULT_TOKEN` | Token local opcional leído por `ApiClient` si no existe token en store | No definida | Sí, si se usa |
| `AUTH_DEFAULT_TOKEN` | Token local opcional leído por `ApiClient` si no existe token en store | No definida | Sí, si se usa |

**Archivo de referencia:** no se confirma existencia de `.env.example` / `.env.dist`.  
**Valores secretos:** las variables compartidas no contienen secretos directos. Si se usan tokens locales (`AUTH_DEFAULT_TOKEN` / `REACT_APP_AUTH_DEFAULT_TOKEN`), credenciales de registry o secretos de CI/CD, deben manejarse por GitHub Secrets o el gestor correspondiente.

---

## 4. Arquitectura y dependencias

**Tipo:** MFE `single-spa` + `Module Federation`.

El proyecto se monta dentro del portal FBC como micro frontend remoto. Usa React 18, React Router con `HashRouter`, Redux Toolkit, `react-redux`, `single-spa-react` y Webpack 5.

El MFE expone:

```js
exposes: {
  './App': './src/App.tsx',
  './Card': './src/Card.tsx',
}
```

También consume el remoto de autenticación:

```js
remotes: {
  authentication:
    AUTHENTICATION_APP ||
    'authentication@http://localhost:3001/remoteEntry.js',
}
```

**Stack:**

|           |        |
|-----------|--------|
| Lenguaje / Runtime | Node `>=20.18.1` · TypeScript · JavaScript |
| Framework | React 18 |
| Router | React Router DOM 6 con `HashRouter` |
| Bundler / Build | Webpack 5 + Babel + webpack-dev-server |
| Estado | Redux Toolkit + react-redux |
| Microfrontend | single-spa + single-spa-react + Module Federation |
| HTTP client | Axios |
| Auth | MFE remoto `authentication/App`, `jwt-decode`, estado global |
| Archivos / Export | `file-saver`, `xlsx`, descarga binaria vía Blob |
| Testing | Jest + jsdom |
| DB / Storage | N/A en frontend |

**Dependencias relevantes:**

- `react`
- `react-dom`
- `react-router-dom`
- `single-spa`
- `single-spa-react`
- `@reduxjs/toolkit`
- `react-redux`
- `axios`
- `jwt-decode`
- `file-saver`
- `xlsx`
- `react-datepicker`
- `dotenv`
- `dotenv-webpack`

---

## 5. Registro en el portal *(solo si es un MFE que vive dentro del portal)*

El proyecto es un MFE del portal y expone `./App` mediante Module Federation. Además expone `./Card`.

La ruta base registrada para el MFE es `/fiscal`.

### shell/apps.json

```json
{
  "appName": "fiscal",
  "componentImport": "fiscal/App",
  "routes": "['/fiscal']",
  "show": "showWhenPrefix",
  "appRemoteName": "fiscal",
  "remote": "http://localhost:3703/remoteEntry.js"
}
```

**Puerto local (`remoteEntry.js`):** `3703`, si no se define `APP_PORT`.  
**Ruta base:** `/fiscal`  
**Exposes:** `./App`, `./Card`

**Nota:** confirmar el registro real en el shell/app-shell, porque este bloque está inferido desde `APP_NAME`, `APP_PATH`, `webpack.config.js` y el patrón del portal.

---

## 6. Vistas y flujos principales *(solo Frontend / MFE)*

| Vista / Flujo | Ruta | Descripción | Link / Screenshot |
|---------------|------|-------------|:-----------------:|
| Home Fiscal | `/` y `/fiscal` | Landing principal del módulo Fiscal. | Pendiente |
| Facturas | `/fiscal/facturas` | Consulta de facturas fiscales. | Pendiente |
| Consulta complemento de pago | `/fiscal/consulta-complemento-pago` | Consulta de complementos de pago. | Pendiente |
| Publicar complemento | `/fiscal/publicar-complemento` | Publicación / carga de complemento de pago. | Pendiente |
| Complemento relacionado | `/fiscal/complemento/:uuid` | Consulta de facturas relacionadas a un complemento por UUID. | Pendiente |
| Notas de crédito | `/fiscal/notas-credito` | Consulta de notas de crédito. | Pendiente |
| Publicar nota de crédito | `/fiscal/publicar-nota-credito` | Publicación / carga de nota de crédito. | Pendiente |
| Fallback Fiscal | `*` | Redirige/renderiza el home Fiscal cuando la ruta no coincide. | Pendiente |

**Figma / Storybook:** No disponible para Fiscal.

---

## 7. Seguridad y permisos

La integración de seguridad del MFE se apoya en el estado global del portal y en el token de autenticación utilizado por `ApiClient`.

En modo integrado al portal (`APP_DEV=false`):

- `GlobalStateBridge` suscribe la app a cambios globales de autenticación y configuración mediante:
  - `handleSubscribeToGlobalAuthenticationChange`
  - `handleSubscribeToGlobalConfigurationChange`
- `ApiClient` busca token en el store local:
  - `authentication.token`
  - `authentication.idToken`
- Si no encuentra token en el store, busca token local en variables:
  - `REACT_APP_AUTH_DEFAULT_TOKEN`
  - `AUTH_DEFAULT_TOKEN`
- Si `APP_DEV=false` y no existe token, `ApiClient` lanza el error `No token`.
- Cuando existe token, se envía en cada request como:

```http
Authorization: Bearer <token>
```

En modo local (`APP_DEV=true`):

- No se suscribe al estado global del portal.
- `ApiClient` no exige token.
- Las llamadas se ejecutan contra `API_BASE_URL` / `REACT_APP_API_BASE_URL` configurado.

**Permisos por módulo:** pendiente de confirmar. En el fragmento revisado de `App.tsx` no se observa un gate de permisos explícito por ruta.

---

## 8. Estado actual

**Estado:** Pendiente de confirmar.  
**Cobertura de tests:** Pendiente de confirmar con ejecución de `npm run test:cov`.  
**Backlog:** Pendiente de confirmar.

**Bugs conocidos / puntos de atención:**

| Punto | Severidad | Contexto |
|-------|-----------|----------|
| Variables DEV no compartidas | Media | Solo se cuenta con bloque UAT. Confirmar valores DEV antes de documentar como definitivos. |
| `REACT_APP_API_BASE_URL` no está en el bloque UAT compartido | Baja / Media | `ApiClient` lo lee con prioridad sobre `API_BASE_URL`. Si no existe, usa `API_BASE_URL`. |
| `AUTHENTICATION_APP` usa `${CI_COMMIT_SHA}` | Baja / Media | Confirmar si el pipeline de GitHub resuelve `CI_COMMIT_SHA` o debe homologarse a `${GITHUB_SHA}`. |
| Permisos por ruta pendientes | Media | En `App.tsx` no se observa un gate explícito por ruta; confirmar si el control vive en shell, backend o componentes internos. |
| `APP_DEV=false` requiere token | Media | Si se levanta fuera del portal con `APP_DEV=false`, las llamadas del `ApiClient` fallan con `No token`. |

**Deuda técnica:**

- Confirmar si existe `.env.example` / `.env.dist`; si no existe, agregarlo.
- Documentar variables DEV y producción.
- Confirmar responsables de backend Fiscal, Finanzas, Util/Catálogos y Authentication.
- Confirmar registro real del MFE en el shell/app-shell.
- Confirmar permisos reales por módulo y por ruta.
- Confirmar cobertura mínima requerida por Sonar/Quality Gate.
- Documentar endpoints usados por cada módulo fiscal.
- Agregar screenshots de pantallas principales.
- Confirmar link público de Swagger/OpenAPI del backend Fiscal si aplica.

---

## 9. Lo que no es obvio

- El proyecto usa `HashRouter`, por lo que las rutas internas viven bajo hash y la ruta real de montaje la decide el shell del portal.
- El `publicPath` de Webpack depende de `APP_URL`; si no se define, cae a `http://localhost:3703/`.
- El MFE expone `./App` y `./Card`; si el portal consume tarjetas o accesos rápidos, confirmar si está usando `fiscal/Card`.
- El remoto de autenticación se carga desde `AUTHENTICATION_APP`; si la variable no existe, cae a `authentication@http://localhost:3001/remoteEntry.js`.
- En modo no local (`APP_DEV=false`), la app se suscribe al estado global de autenticación y configuración del portal.
- En modo local (`APP_DEV=true`), se evita la suscripción global y el `ApiClient` no bloquea por ausencia de token.
- El `ApiClient` remueve slashes finales de `baseURL` y normaliza los paths para evitar doble slash.
- Las peticiones JSON envían `Accept: application/json` y `Content-Type: application/json` para métodos distintos de `GET`, excepto cuando el payload es `FormData`.
- `requestBinary` descarga archivos como Blob y crea un link temporal en el navegador para forzar la descarga.
- Webpack copia el contenido de `public` al build final, ignorando `index.html`.
- Los archivos `.xlsx` / `.xlsm` se publican como recursos en `static/templates/[name][ext]`.

---

## 10. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Confirmar registro real del MFE en el shell/app-shell | Alta |
| Agregar o actualizar `.env.example` con variables mínimas no sensibles | Alta |
| Completar variables DEV y producción | Alta |
| Homologar placeholder de versión en `AUTHENTICATION_APP` (`CI_COMMIT_SHA` vs `GITHUB_SHA`) | Alta |
| Confirmar responsables de backend Fiscal, Finanzas, Util/Catálogos y Authentication | Alta |
| Confirmar permisos por ruta y por módulo | Alta |
| Documentar endpoints usados por Facturas, Complementos y Notas de Crédito | Media |
| Documentar flujo de despliegue GitHub Actions / Cloud Storage / GCP Artifacts | Media |
| Confirmar cobertura actual y política de quality gate | Media |
| Agregar screenshots de pantallas principales | Baja / Media |
| Agregar link a Swagger o documentación de backend si existe | Media |

---

## 11. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | QA | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

*Completado por:* Oscar Bonelli, basado en código y configuración compartida · *Fecha:* 2026-06-29 · *Aceptado por:* Pendiente
