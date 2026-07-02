# HANDOVER.md — `APP03022-mrch.frontend.somx.finanzas-spa`

> Documento de traspaso basado en los fuentes, configuración, rutas y variables de entorno compartidas para el repositorio.

**Proyecto:** APP03022-mrch.frontend.somx.finanzas-spa  
**Tipo:** MFE  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.finanzas-spa  
**Fecha:** 2026-06-18  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Carlos Rojas Burgos — cjrojasb@Falabella.cl  

---

## 1. ¿Qué hace este proyecto?

Este proyecto es un micro frontend React para el dominio de **Finanzas** dentro del portal **Falabella Business Center**. Centraliza operaciones financieras utilizadas por proveedores y usuarios internos, incluyendo consulta de pagos, recepciones, guías de embarque, descuentos comerciales, rebates, estado de cuenta, Three Way Match y publicación de recepciones MIGO.

El frontend no contiene persistencia ni lógica de negocio principal; consume servicios backend mediante `API_BASE_URL`, `REACT_APP_API_BASE_URL`, `FISCAL_API_URL` y `CATALOGS_API_URL`. Además, se integra con el MFE remoto de autenticación (`authentication/App`) y con el estado global del portal.

Los flujos visibles desde el código se agrupan en módulos financieros. La pantalla principal `/finanzas` muestra cards de operación y cada card dirige a un flujo especializado.

**Consumidores / usuarios:** proveedores del portal FBC y perfiles internos con permisos sobre módulos financieros.  
**Criticidad:** Alta. El MFE soporta operación financiera, consulta de pagos, recepciones, documentos, descuentos, estado de cuenta y validaciones operativas.

**Documentación adicional:**

| Recurso | Link |
|---------|------|
| Confluence / documentación técnica | [Documentación Finanzas Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897680760/Documentacion+Finanzas+Dev) |
| Tablero Jira / backlog | No disponible |
| Figma / diseño UI | No disponible para Finanzas |
| Storybook | No disponible |
| Otro | `README.md` del repositorio, si aplica |

---

## 2. Cómo levantar el proyecto

**Requisitos:** Node `>=20.18.1` · npm · acceso al repositorio · variables de entorno de desarrollo

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.finanzas-spa
cd APP03022-mrch.frontend.somx.finanzas-spa

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
- El puerto local se toma desde `APP_PORT`; si no existe, `webpack.config.js` usa `3702` como fallback.
- El proyecto expone `./App` y `./Card` mediante `ModuleFederationPlugin`.
- El MFE se monta con `single-spa-react` y exporta los lifecycle hooks `bootstrap`, `mount` y `unmount`.
- En ejecución integrada al portal, monta el parcel remoto `authentication/App`.
- Si `ConfigurationBuilder.localDeployment` está activo, no monta el parcel remoto de autenticación y permite correr el MFE en modo local.
- Las rutas internas usan `HashRouter`.
- El archivo de variables cargado por Webpack se resuelve con `NODE_ENV`:
  - `NODE_ENV=development` → `.env.development`
  - `NODE_ENV=production` → `.env.production`

**Servicios externos necesarios para correr localmente:**

| Servicio / MFE / API | Uso | URL local / ambiente |
|----------------------|-----|----------------------|
| MFE `authentication` | Login / autenticación remota | `authentication@http://localhost:3001/remoteEntry.js` por default |
| Backend Finanzas | Pagos, recepciones, guías, Three Way Match, MIGO, etc. | `API_BASE_URL` / `REACT_APP_API_BASE_URL` |
| Backend Fiscal | Integración con Fiscal / facturación | `FISCAL_API_URL` |
| Backend Util / Catálogos | Catálogos y servicios auxiliares | `CATALOGS_API_URL` |
| Shell / portal FBC | Host single-spa y estado global | Pendiente de confirmar |

---

## 3. Variables de entorno

### DEV

```env
APP_NAME=finanzas
APP_PATH=/finanzas
APP_URL=https://dev.fbusinesscenter.com/ppsomx/frontend/finanzas/
APP_URL_PREFIX=https://dev.fbusinesscenter.com/ppsomx/frontend/
LOGIN_URL=https://dev.fbusinesscenter.com/login
AUTH_CONFIG_CLIENT=portal
AUTHENTICATION_APP=authentication@https://dev.fbusinesscenter.com/authentication/remoteEntry.js?v=${CI_COMMIT_SHA}
STORE_DEBUG=false
APP_DEV=false
API_BASE_URL=https://dev.fbusinesscenter.com/ppsomx/backend-finanzas
FISCAL_API_URL=https://dev.fbusinesscenter.com/ppsomx/fiscal/
CATALOGS_API_URL=https://mocki.io/v1/746a1b80-5b1f-4109-a7d4-f93531ece9a3
back=${GITHUB_SHA}
REACT_APP_API_BASE_URL=https://dev.fbusinesscenter.com/ppsomx/backend-finanzas
LOCAL_DEPLOYMENT=false
REACT_APP_LOCAL_DEPLOYMENT=false
FISCAL_SPA_URL=https://dev.fbusinesscenter.com
```

### UAT

```env
APP_NAME=finanzas
APP_PATH=/finanzas
FBC_HOME=https://uat.fbusinesscenter.com/

APP_URL=https://uat.fbusinesscenter.com/ppsomx/frontend/finanzas/
APP_URL_PREFIX=https://uat.fbusinesscenter.com/ppsomx/frontend/
LOGIN_URL=https://uat.fbusinesscenter.com/login
AUTH_CONFIG_CLIENT=portal
AUTHENTICATION_APP=authentication@https://uat.fbusinesscenter.com/authentication/remoteEntry.js?v=${GITHUB_SHA}
STORE_DEBUG=false
APP_DEV=false

API_BASE_URL=https://uat.fbusinesscenter.com/ppsomx/backend-finanzas
REACT_APP_API_BASE_URL=https://uat.fbusinesscenter.com/ppsomx/backend-finanzas

FISCAL_API_URL=https://uat.fbusinesscenter.com/ppsomx/fiscal/
CATALOGS_API_URL=https://uat.fbusinesscenter.com/ppsomx/backend-util

LOCAL_DEPLOYMENT=false
REACT_APP_LOCAL_DEPLOYMENT=false

FISCAL_SPA_URL=https://uat.fbusinesscenter.com/fiscal/#/fiscal/

back=${GITHUB_SHA}

USE_GCP_ARTIFACTS=true
```

### Tabla de variables

| Variable | Descripción | DEV | UAT | ¿Secreta? |
|----------|-------------|-----|-----|:---------:|
| `APP_NAME` | Nombre del micro frontend / remoto Module Federation | `finanzas` | `finanzas` | No |
| `APP_PATH` | Ruta base del MFE dentro del portal | `/finanzas` | `/finanzas` | No |
| `FBC_HOME` | URL home del portal FBC | No definida | `https://uat.fbusinesscenter.com/` | No |
| `APP_URL` | URL pública del MFE | `https://dev.fbusinesscenter.com/ppsomx/frontend/finanzas/` | `https://uat.fbusinesscenter.com/ppsomx/frontend/finanzas/` | No |
| `APP_URL_PREFIX` | Prefijo base de frontends | `https://dev.fbusinesscenter.com/ppsomx/frontend/` | `https://uat.fbusinesscenter.com/ppsomx/frontend/` | No |
| `LOGIN_URL` | URL de login del portal | `https://dev.fbusinesscenter.com/login` | `https://uat.fbusinesscenter.com/login` | No |
| `AUTH_CONFIG_CLIENT` | Cliente de autenticación usado por el portal | `portal` | `portal` | No |
| `AUTHENTICATION_APP` | Remote del MFE de autenticación | `authentication@https://dev.fbusinesscenter.com/authentication/remoteEntry.js?v=${CI_COMMIT_SHA}` | `authentication@https://uat.fbusinesscenter.com/authentication/remoteEntry.js?v=${GITHUB_SHA}` | No |
| `STORE_DEBUG` | Habilita debug del store | `false` | `false` | No |
| `APP_DEV` | Modo desarrollo / bypass local | `false` | `false` | No |
| `API_BASE_URL` | Base URL del backend Finanzas | `https://dev.fbusinesscenter.com/ppsomx/backend-finanzas` | `https://uat.fbusinesscenter.com/ppsomx/backend-finanzas` | No |
| `REACT_APP_API_BASE_URL` | Base URL expuesta a React para backend Finanzas | `https://dev.fbusinesscenter.com/ppsomx/backend-finanzas` | `https://uat.fbusinesscenter.com/ppsomx/backend-finanzas` | No |
| `FISCAL_API_URL` | Base URL del backend Fiscal | `https://dev.fbusinesscenter.com/ppsomx/fiscal/` | `https://uat.fbusinesscenter.com/ppsomx/fiscal/` | No |
| `CATALOGS_API_URL` | Base URL de catálogos / backend util | `https://mocki.io/v1/746a1b80-5b1f-4109-a7d4-f93531ece9a3` | `https://uat.fbusinesscenter.com/ppsomx/backend-util` | No |
| `LOCAL_DEPLOYMENT` | Activa/desactiva modo local | `false` | `false` | No |
| `REACT_APP_LOCAL_DEPLOYMENT` | Bandera React para modo local | `false` | `false` | No |
| `FISCAL_SPA_URL` | URL del SPA Fiscal | `https://dev.fbusinesscenter.com` | `https://uat.fbusinesscenter.com/fiscal/#/fiscal/` | No |
| `back` | Hash/versionado usado por pipeline | `${GITHUB_SHA}` | `${GITHUB_SHA}` | No |
| `USE_GCP_ARTIFACTS` | Bandera para uso de artefactos GCP | No definida | `true` | No |

**Archivo de referencia:** no se confirma existencia de `.env.example` / `.env.dist`.  
**Valores secretos:** no se identifican secretos directos en estas variables. Si se usan tokens locales, credenciales de registry o secretos de CI/CD, deben manejarse por GitHub Secrets o el gestor correspondiente.

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
| UI | MUI, CSS propio, componentes compartidos |
| Auth | MFE remoto `authentication/App`, `jwt-decode`, estado global |
| i18n | `i18next`, `react-i18next` |
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
- `@mui/material`
- `i18next`
- `react-i18next`
- `dotenv`
- `dotenv-webpack`

---

## 5. Registro en el portal *(solo si es un MFE que vive dentro del portal)*

El proyecto es un MFE del portal y expone `./App` mediante Module Federation. Además expone `./Card`.

La ruta base registrada para el MFE es `/finanzas`.

### shell/apps.json

```json
{
  "appName": "finanzas",
  "componentImport": "finanzas/App",
  "routes": "['/finanzas']",
  "show": "showWhenPrefix",
  "appRemoteName": "finanzas",
  "remote": "http://localhost:3702/remoteEntry.js"
}
```

**Puerto local (`remoteEntry.js`):** `3702`, si no se define `APP_PORT`.  
**Ruta base:** `/finanzas`  
**Exposes:** `./App`, `./Card`

**Nota:** confirmar el registro real en el shell/app-shell, porque este bloque está inferido desde `APP_NAME`, `APP_PATH`, `webpack.config.js` y el patrón del portal.

---

## 6. Vistas y flujos principales *(solo Frontend / MFE)*

| Vista / Flujo | Ruta | Descripción | Link / Screenshot |
|---------------|------|-------------|:-----------------:|
| Home Finanzas / Operaciones | `/` y `/finanzas` | Landing principal con cards de módulos financieros. | Pendiente |
| Pagos | `/finanzas/pagos` | Consulta y gestión de pagos de proveedores. | Pendiente |
| Detalle de pago | `/finanzas/pagos/detalle` | Vista de detalle de pago. | Pendiente |
| Lista de Recepciones | `/finanzas/recepciones` | Consulta de recepciones de órdenes de servicio y estatus. | Pendiente |
| Detalle de recepción | `/finanzas/recepciones/:uuid` | Consulta de detalle de recepción. | Pendiente |
| Edición de recepción | `/finanzas/recepciones/:uuid/editar` | Edición de recepción. | Pendiente |
| Factura de recepción | `/finanzas/recepciones/:uuid/factura` | Gestión/consulta de factura asociada a recepción. | Pendiente |
| Notas de crédito | `/finanzas/recepciones/:uuid/notas-credito` | Gestión/consulta de notas de crédito asociadas. | Pendiente |
| Guías de embarque | `/finanzas/guias` | Consulta de guías de embarque. | Pendiente |
| Detalle guía de embarque | `/finanzas/guias/:guideId` | Detalle de guía de embarque. | Pendiente |
| Actualización estatus guía | `/finanzas/guias/:guideId/estatus` | Actualización de estatus de guía. | Pendiente |
| Descuentos comerciales | `/finanzas/descuentos-comerciales` | Consulta y gestión de descuentos comerciales. | Pendiente |
| Detalle descuento comercial | `/finanzas/descuentos-comerciales/detalle` | Detalle de descuento comercial / rebate. | Pendiente |
| Rebates | `/finanzas/rebates` | Vista de rebates. | Pendiente |
| Estado de cuenta | `/finanzas/estado-cuenta` | Consulta de estados de cuenta y generación de PDF. | Pendiente |
| Three Way Match | `/finanzas/three-way-match` | Validación de orden de compra, recepción y factura pendientes de pago o pagadas. | Pendiente |
| MIGO | `/finanzas/migo` | Consulta/publicación/autorización/rechazo de recepciones MIGO. | Pendiente |
| Publicar MIGO | `/finanzas/migo/publicar` | Carga/publicación de recepción MIGO. | Pendiente |
| Recepciones MIGO | `/finanzas/migo/:id/recepciones` | Consulta de recepciones por MIGO. | Pendiente |
| Artículos MIGO | `/finanzas/migo/:id/recepciones/:nroOc/:nroRecepcion/articulos` | Consulta de artículos por OC y recepción. | Pendiente |
| Healthcheck | Card sin ruta | Valida estado del servicio Finanzas y conexión a base de datos. | Pendiente |

**Figma / Storybook:** No disponible para Finanzas.

---

## 7. Seguridad y permisos

La pantalla principal define cards con permisos por módulo usando `APP_KEYS` y `useSecurityContext`.

Permisos / apps usados por las cards:

| Card | Permiso requerido |
|------|-------------------|
| Guías de embarque | `APP_KEYS.CARTA_PORTE` |
| Lista de Recepciones | `APP_KEYS.RECEPTIONS` |
| Descuentos comerciales | `APP_KEYS.DISCOUNTS` |
| Pagos | Cualquiera de `APP_KEYS.INVOICES`, `APP_KEYS.CREDIT_NOTES`, `APP_KEYS.PAYMENT_COMPLEMENTS` |
| Estado de cuenta | `APP_KEYS.ACCOUNT_STATEMENT` |
| Three Way Match | `APP_KEYS.THREE_WAY_MATCH` |
| Publicación de recepción MIGO | `APP_KEYS.MIGO` |
| Healthcheck | Sin permiso explícito en la card |

**Importante:** actualmente el filtro de permisos está comentado y existe un TODO:

```ts
/* TODO: Implement the permission gate */
const finalCards = DEFAULT_CARDS;
```

Esto significa que, con el código actual mostrado, todas las cards se renderizan sin aplicar el gate de permisos en frontend.

La protección general del MFE se resuelve con:

- `PrivateRoute`
- `Gate`
- `useAuthReady`
- `localHomeStore`
- suscripción al estado global de autenticación y configuración mediante:
  - `handleSubscribeToGlobalAuthenticationChange`
  - `handleSubscribeToGlobalConfigurationChange`

---

## 8. Estado actual

**Estado:** Pendiente de confirmar.  
**Cobertura de tests:** Pendiente de confirmar con ejecución de `npm run test:cov`.  
**Backlog:** Pendiente de confirmar.  

**Bugs conocidos:**

| Bug | Severidad | Contexto |
|-----|-----------|----------|
| Gate de permisos comentado | Media / Alta | Las cards tienen permisos definidos, pero el filtrado está desactivado con `const finalCards = DEFAULT_CARDS`. |
| Diferencia entre DEV y UAT en `FISCAL_SPA_URL` | Media | DEV apunta a `https://dev.fbusinesscenter.com`, mientras UAT apunta a `https://uat.fbusinesscenter.com/fiscal/#/fiscal/`. Confirmar si DEV debería tener ruta completa. |
| `AUTHENTICATION_APP` usa `${CI_COMMIT_SHA}` en DEV y `${GITHUB_SHA}` en UAT | Baja / Media | El proyecto está en GitHub Actions; confirmar si DEV realmente resuelve `CI_COMMIT_SHA` o debe homologarse a `GITHUB_SHA`. |
| `CATALOGS_API_URL` usa Mocki en DEV y backend real en UAT | Media | Confirmar si DEV debe seguir con mock o apuntar a `backend-util` de DEV. |

**Deuda técnica:**

- Confirmar si existe `.env.example` / `.env.dist`; si no existe, agregarlo.
- Documentar responsables de backend Finanzas, Fiscal, Util/Catálogos y Authentication.
- Confirmar registro real del MFE en el shell/app-shell.
- Revisar y habilitar gate de permisos para cards.
- Confirmar cobertura mínima requerida por Sonar/Quality Gate.
- Homologar variables entre DEV y UAT cuando aplique.
- Documentar endpoints usados por cada módulo.
- Revisar si el proxy custom de `/api/finanzas-payment` sigue siendo necesario o puede resolverse solo con `devServer.proxy`.
- Revisar si el método definido como `POST /api/finanzas-payment` hacia backend `GET /api/finanzas-payment` es intencional.

---

## 9. Lo que no es obvio

- El proyecto usa `HashRouter`, por lo que las rutas internas viven bajo hash y la ruta real de montaje la decide el shell del portal.
- El `publicPath` de Webpack está configurado como `auto`, por lo que la carga de assets depende del contexto desde donde se cargue `remoteEntry.js`.
- El MFE expone `./App` y `./Card`; si el portal consume tarjetas o accesos rápidos, confirmar si está usando `finanzas/Card`.
- El remoto de autenticación se carga desde `AUTHENTICATION_APP`; si la variable no existe, cae a `authentication@http://localhost:3001/remoteEntry.js`.
- En modo no local, se monta el parcel remoto `authentication/App`.
- El `Gate` espera que exista `authentication.tokenDecoded` en el store local antes de renderizar las rutas.
- La app se suscribe al estado global de autenticación y configuración del portal mediante `globalStateService`.
- El filtrado real de cards por permisos está definido pero comentado; actualmente no está activo en la vista principal.
- Para desarrollo, Webpack incluye proxy hacia:
  - `http://localhost:8091` para `/api`
  - `http://localhost:8083` para catálogos y servicios auxiliares
- Existe un proxy específico para `POST /api/finanzas-payment` que reenvía a `GET /api/finanzas-payment` en `localhost:8091`.
- Auditoría fue migrada a `util.spa`, según comentario del código:
  - `/util/auditoria/bitacora-actividades`

---

## 10. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Confirmar registro real del MFE en el shell/app-shell | Alta |
| Agregar o actualizar `.env.example` con variables mínimas no sensibles | Alta |
| Confirmar URLs correctas de DEV para `FISCAL_SPA_URL` y `CATALOGS_API_URL` | Alta |
| Homologar placeholder de versión en `AUTHENTICATION_APP` (`CI_COMMIT_SHA` vs `GITHUB_SHA`) | Alta |
| Confirmar responsables de backend Finanzas, Fiscal, Util/Catálogos y Authentication | Alta |
| Habilitar o descartar formalmente el gate de permisos de cards | Alta |
| Documentar endpoints usados por módulo | Media |
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

*Completado por:* Oscar Bonelli, basado en código y configuración compartida · *Fecha:* 2026-06-18 · *Aceptado por:* Pendiente
