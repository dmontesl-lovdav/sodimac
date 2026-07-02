# HANDOVER.md — `APP03022-mrch.frontend.somx.util.spa`

> Documento de traspaso basado en el `package.json`, configuración Webpack, rutas visibles en `App.tsx`, variables compartidas y el formato usado en el handover de Finanzas SPA.

**Proyecto:** APP03022-mrch.frontend.somx.util.spa  
**Tipo:** MFE / Micro frontend React  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.util.spa  
**Fecha:** 2026-06-30  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Carlos Rojas Burgos — cjrojasb@Falabella.cl  

---

## 1. ¿Qué hace este proyecto?

Este proyecto es un micro frontend React para el dominio de **Utilerías** dentro del portal **Falabella Business Center / PPSOMX**. Centraliza pantallas administrativas y operativas usadas por usuarios internos para configuración de parámetros, seguridad, perfiles, roles, permisos, catálogos, proveedores, bloqueos de proveedor y bitácoras de auditoría.

El frontend no contiene persistencia ni lógica de negocio principal; consume el backend de Utilerías mediante rutas `/api`, las cuales en desarrollo se proxyan hacia `BACKEND_URL` o hacia `http://localhost:3712` si no existe variable configurada.

El MFE se integra con el portal mediante **single-spa**, **Module Federation**, el remoto de autenticación `authentication`, y el estado global del portal mediante suscripciones a autenticación y configuración.

Los flujos visibles desde el código se agrupan en tres grandes módulos:

- **Mantenedores / Utilerías:** pantalla principal del módulo y configuración de parámetros.
- **Seguridad:** perfiles, roles, permisos, usuarios, aplicativos, eventos y atributos.
- **Catálogos / Auditoría:** proveedores, bloqueos, catálogos, elementos, conversiones y bitácora de actividades.

**Consumidores / usuarios:** usuarios internos del portal FBC con permisos administrativos, equipos de operación, soporte, seguridad y mantenimiento de catálogos.  
**Criticidad:** Alta. El MFE soporta configuración operativa, administración de permisos, seguridad funcional, catálogos base y auditoría, por lo que impacta indirectamente a otros dominios como Finanzas, Fiscal y otros módulos del portal.

**Documentación adicional:**

| Recurso | Link |
|---------|------|
| Repositorio | [APP03022-mrch.frontend.somx.util.spa](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.util.spa) |
| Confluence / documentación técnica | [Documentación Utilerías Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897549760/Documentacion+Utilerias+Dev) |
| Tablero Jira / backlog | No disponible |
| Figma / diseño UI | No disponible para Utilerías |
| Storybook | No disponible |
| README del repositorio | [README.md](README.md) |

---

## 2. Cómo levantar el proyecto

**Requisitos:** Node `>=20.18.1` · npm · acceso al repositorio · variables de entorno de desarrollo · backend Util disponible

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.util.spa
cd APP03022-mrch.frontend.somx.util.spa

# usar como base la rama develop
git checkout develop
git pull origin develop

# crear .env.development con las variables de DEV
# crear .env.production o inyectar variables desde GitHub Actions para UAT/producción

npm install --legacy-peer-deps
npm start
```

**Scripts disponibles:**

```bash
npm run build
npm run dev
npm start
npm run lint
npm test
npm run test:cov
```

| Script | Descripción |
|--------|-------------|
| `npm run build` | Ejecuta build productivo con Webpack: `cross-env NODE_ENV=production webpack --mode production --config config/webpack.config.js`. |
| `npm run dev` | Levanta `webpack serve` en modo development con `NODE_OPTIONS=--max-http-header-size=1048576`. |
| `npm start` | Alias de desarrollo, ejecuta `npm run dev`. |
| `npm run lint` | Ejecuta ESLint sobre `src`. |
| `npm test` | Ejecuta Jest con `--passWithNoTests`. |
| `npm run test:cov` | Ejecuta el script de cobertura `scripts/test-cov.sh`. |

**Notas de arranque local:**

- El entrypoint de Webpack es `./src/main.tsx`.
- El proyecto usa `webpack serve` para desarrollo local.
- El build productivo usa `webpack --mode production --config config/webpack.config.js`.
- El puerto local se toma desde `APP_PORT`; si no existe, `webpack.config.js` usa `3701` como fallback.
- El proyecto expone `./App` y `./Card` mediante `ModuleFederationPlugin`.
- El MFE usa `HashRouter`, por lo que las rutas internas viven bajo hash cuando se monta en el portal.
- El remoto de autenticación se toma desde `AUTHENTICATION_APP`; si no existe, cae a `authentication@http://localhost:3001/remoteEntry.js`.
- En desarrollo, Webpack proxya cualquier ruta `/api` hacia `BACKEND_URL` o `http://localhost:3712`.
- El archivo de variables cargado por Webpack se resuelve con `NODE_ENV`:
  - `NODE_ENV=development` → `.env.development`
  - `NODE_ENV=production` → `.env.production`
- En modo local/standalone, si existe un elemento `root` en el HTML, el MFE puede renderizarse fuera del shell; si no está standalone, exporta lifecycles de single-spa.

**Servicios externos necesarios para correr localmente:**

| Servicio / MFE / API | Uso | URL local / ambiente |
|----------------------|-----|----------------------|
| MFE `authentication` | Login / autenticación remota | `authentication@http://localhost:3001/remoteEntry.js` por default |
| Backend Utilerías | Parámetros, seguridad, catálogos, proveedores, auditoría | `BACKEND_URL` o `http://localhost:3712` |
| Shell / portal FBC | Host single-spa y estado global | Pendiente de confirmar |

---

## 3. Variables de entorno

### Variables esperadas por el SPA / Webpack

```env
APP_NAME=util
APP_PATH=/util
APP_PORT=3701
APP_URL=https://uat.fbusinesscenter.com/ppsomx/frontend/util/
APP_URL_PREFIX=https://uat.fbusinesscenter.com/ppsomx/frontend/
LOGIN_URL=https://uat.fbusinesscenter.com/login
AUTH_CONFIG_CLIENT=portal
AUTHENTICATION_APP=authentication@https://uat.fbusinesscenter.com/authentication/remoteEntry.js?v=${GITHUB_SHA}
STORE_DEBUG=false
APP_DEV=false
BACKEND_URL=https://uat.fbusinesscenter.com/ppsomx/backend-util
LOCAL_DEPLOYMENT=false
REACT_APP_LOCAL_DEPLOYMENT=false
USE_GCP_ARTIFACTS=true
```

> Nota: las variables anteriores son las mínimas esperadas para el frontend de acuerdo con `webpack.config.js` y el patrón de otros MFE del portal. Confirmar valores reales por ambiente en GitHub Actions, Cloud Storage, Kustomize o la configuración del shell.

### Variables compartidas del backend Utilerías

Estas variables corresponden al backend consumido por el SPA. Se documentan porque son necesarias para levantar el ecosistema completo, pero no deberían exponerse dentro del frontend si contienen datos internos o sensibles.

```env
DB_HOST=10.100.64.102
DB_PORT=5432
DB_NAME=b2b_portal
DB_SCHEMA=core_utils
DB_USER=wwwb2bportal
DB_SSL=true
PORT=8082
NODE_ENV=development
SECURITY_ENABLED=true
KEYCLOAK_GROUP_VENDOR=ppsomx-vendor
KEYCLOAK_GROUP_ADMIN=ppsomx-admin
```

### Tabla de variables SPA

| Variable | Descripción | Valor esperado / ejemplo | ¿Secreta? |
|----------|-------------|--------------------------|:---------:|
| `APP_NAME` | Nombre del micro frontend / remoto Module Federation. | `util` o valor real definido por shell | No |
| `APP_PATH` | Ruta base del MFE dentro del portal. | `/util` | No |
| `APP_PORT` | Puerto local usado por `webpack-dev-server`. | `3701` | No |
| `APP_URL` | URL pública del MFE. | `https://uat.fbusinesscenter.com/ppsomx/frontend/util/` | No |
| `APP_URL_PREFIX` | Prefijo base de frontends. | `https://uat.fbusinesscenter.com/ppsomx/frontend/` | No |
| `LOGIN_URL` | URL de login del portal. | `https://uat.fbusinesscenter.com/login` | No |
| `AUTH_CONFIG_CLIENT` | Cliente de autenticación usado por el portal. | `portal` | No |
| `AUTHENTICATION_APP` | Remote del MFE de autenticación. | `authentication@https://uat.fbusinesscenter.com/authentication/remoteEntry.js?v=${GITHUB_SHA}` | No |
| `STORE_DEBUG` | Habilita debug del store. | `false` | No |
| `APP_DEV` | Modo desarrollo / bypass local si aplica. | `false` | No |
| `BACKEND_URL` | URL del backend Utilerías usado por proxy local `/api`. | `https://uat.fbusinesscenter.com/ppsomx/backend-util` | No |
| `LOCAL_DEPLOYMENT` | Activa/desactiva modo local si el proyecto lo consume. | `false` | No |
| `REACT_APP_LOCAL_DEPLOYMENT` | Bandera React para modo local si el proyecto la consume. | `false` | No |
| `USE_GCP_ARTIFACTS` | Bandera usada por pipeline / despliegue si aplica. | `true` | No |

### Tabla de variables backend relacionadas

| Variable | Descripción | Valor compartido | ¿Secreta? |
|----------|-------------|------------------|:---------:|
| `DB_HOST` | Host PostgreSQL del backend Utilerías. | `10.100.64.102` | Sí / interno |
| `DB_PORT` | Puerto PostgreSQL. | `5432` | No |
| `DB_NAME` | Base de datos. | `b2b_portal` | No |
| `DB_SCHEMA` | Schema usado por Utilerías. | `core_utils` | No |
| `DB_USER` | Usuario de conexión a BD. | `wwwb2bportal` | Sí |
| `DB_SSL` | Indica uso de SSL hacia PostgreSQL. | `true` | No |
| `PORT` | Puerto del backend Utilerías. | `8082` | No |
| `NODE_ENV` | Modo de ejecución del backend. | `development` | No |
| `SECURITY_ENABLED` | Habilita seguridad en backend. | `true` | No |
| `KEYCLOAK_GROUP_VENDOR` | Grupo Keycloak para proveedores. | `ppsomx-vendor` | No |
| `KEYCLOAK_GROUP_ADMIN` | Grupo Keycloak para administradores. | `ppsomx-admin` | No |

**Archivo de referencia:** no se confirma existencia de `.env.example` / `.env.dist` actualizado para frontend.  
**Valores secretos:** no dejar credenciales de base de datos, tokens ni secretos de Keycloak en el repositorio. Usar GitHub Secrets, Secret Manager o el mecanismo definido por DevOps.

---

## 4. Arquitectura y dependencias

**Tipo:** MFE `single-spa` + `Module Federation`.

El proyecto se monta dentro del portal FBC como micro frontend remoto. Usa React 18, React Router con `HashRouter`, Redux Toolkit, `react-redux`, TanStack React Query, React Hook Form, Zod, single-spa-react y Webpack 5.

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

El proxy local de desarrollo apunta a backend Utilerías:

```js
proxy: [
  {
    context: ['/api'],
    target: BACKEND_URL || 'http://localhost:3712',
    changeOrigin: true,
    secure: false,
  },
]
```

**Stack:**

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje / Runtime | Node `>=20.18.1` · TypeScript · JavaScript |
| Framework | React 18 |
| Router | React Router DOM 6 con `HashRouter` |
| Bundler / Build | Webpack 5 + Babel + webpack-dev-server |
| Estado global/local | Redux Toolkit + react-redux + redux-micro-frontend |
| Server state | TanStack React Query |
| Formularios / validación | React Hook Form + Zod + `@hookform/resolvers` |
| Microfrontend | single-spa + single-spa-react + Module Federation |
| Auth | MFE remoto `authentication`, `oidc-client-ts`, `jwt-decode`, estado global del portal |
| HTTP Client | Axios |
| Archivos / Exportaciones | `xlsx`, `file-saver` |
| Testing | Jest + jsdom + Testing Library |
| DB / Storage | N/A en frontend |

**Dependencias relevantes:**

- `react`
- `react-dom`
- `react-router-dom`
- `single-spa`
- `single-spa-react`
- `@reduxjs/toolkit`
- `react-redux`
- `redux-micro-frontend`
- `@tanstack/react-query`
- `react-hook-form`
- `zod`
- `@hookform/resolvers`
- `axios`
- `jwt-decode`
- `oidc-client-ts`
- `xlsx`
- `file-saver`
- `dotenv`
- `dotenv-webpack`

---

## 5. Registro en el portal *(solo si es un MFE que vive dentro del portal)*

El proyecto es un MFE del portal y expone `./App` mediante Module Federation. Además expone `./Card`.

La ruta base visible en el código es `/util`. También existen rutas bajo `/seguridad`, por lo que se debe confirmar si el shell registra únicamente `/util` o también el prefijo `/seguridad` para permitir navegación directa.

### shell/apps.json sugerido / pendiente de confirmar

```json
{
  "appName": "util",
  "componentImport": "util/App",
  "routes": "['/util', '/seguridad']",
  "show": "showWhenPrefix",
  "appRemoteName": "util",
  "remote": "http://localhost:3701/remoteEntry.js"
}
```

**Puerto local (`remoteEntry.js`):** `3701`, si no se define `APP_PORT`.  
**Ruta base:** `/util`  
**Rutas adicionales visibles:** `/seguridad`  
**Exposes:** `./App`, `./Card`

**Nota:** confirmar el registro real en el shell/app-shell, porque este bloque está inferido desde `APP_NAME`, `webpack.config.js`, las rutas del `App.tsx` y el patrón del portal.

---

## 6. Vistas y flujos principales *(solo Frontend / MFE)*

| Vista / Flujo | Ruta | Descripción | Link / Screenshot |
|---------------|------|-------------|:-----------------:|
| Home Utilerías / Mantenedores | `/` y `/util` | Landing principal del módulo Utilerías / mantenedores. | Pendiente |
| Parámetros | `/util/parametros` | Configuración de parámetros del sistema. | Pendiente |
| Seguridad | `/seguridad` | Landing o contenedor principal del módulo de seguridad. | Pendiente |
| Perfil usuario | `/seguridad/perfil-usuario` | Administración de perfiles por usuario. | Pendiente |
| Perfil aplicativo | `/seguridad/perfil-aplicativo` | Administración de perfiles por aplicativo / módulo. | Pendiente |
| Perfil evento | `/seguridad/perfil-evento` | Administración de perfiles por evento o proceso. | Pendiente |
| Aplicativo evento | `/seguridad/aplicativo-evento` | Relación entre aplicativos y eventos. | Pendiente |
| Gestión de usuarios | `/seguridad/gestion-usuarios` | Consulta y administración de usuarios. | Pendiente |
| Detalle de usuario | `/seguridad/gestion-usuarios/:userId` | Detalle / edición de usuario. | Pendiente |
| Eventos por usuario y aplicativo | `/seguridad/gestion-usuarios/:userId/app/:moduleId/eventos` | Gestión de eventos/permisos asociados al usuario y aplicativo. | Pendiente |
| Rol usuario | `/seguridad/rol-usuario` | Relación de roles por usuario. | Pendiente |
| Rol permiso | `/seguridad/rol-permiso` | Administración de permisos por rol. | Pendiente |
| Usuario atributo | `/seguridad/usuario-atributo` | Administración de atributos asociados al usuario. | Pendiente |
| Hub Catálogos | `/util/catalogos` | Landing del módulo de catálogos. | Pendiente |
| Proveedores | `/util/catalogos/proveedores` | Consulta/listado de proveedores. | Pendiente |
| Crear proveedor | `/util/catalogos/proveedores/crear` | Alta de proveedor. | Pendiente |
| Editar proveedor | `/util/catalogos/proveedores/editar/:id` | Edición de proveedor existente. | Pendiente |
| Bloquear proveedor | `/util/catalogos/proveedores/:id/bloquear` | Registro de bloqueo asociado a proveedor. | Pendiente |
| Bloqueos de proveedor | `/util/catalogos/bloqueos` | Consulta/listado de bloqueos. | Pendiente |
| Crear bloqueo | `/util/catalogos/bloqueos/crear` | Alta de bloqueo. | Pendiente |
| Editar bloqueo | `/util/catalogos/bloqueos/editar/:id` | Edición de bloqueo existente. | Pendiente |
| Catálogos | `/util/catalogos/catalogs` | Consulta/listado de catálogos. | Pendiente |
| Crear catálogo | `/util/catalogos/catalogs/crear` | Alta de catálogo. | Pendiente |
| Editar catálogo | `/util/catalogos/catalogs/editar/:id` | Edición de catálogo. | Pendiente |
| Elementos de catálogo | `/util/catalogos/catalogs/:id/elementos` | Consulta de elementos asociados a un catálogo. | Pendiente |
| Importar elementos | `/util/catalogos/catalogs/:id/elementos/importar` | Importación masiva de elementos. | Pendiente |
| Nuevo elemento | `/util/catalogos/catalogs/:id/elementos/nuevo` | Alta de elemento de catálogo. | Pendiente |
| Editar elemento | `/util/catalogos/catalogs/:id/elementos/editar/:elementId` | Edición de elemento de catálogo. | Pendiente |
| Conversiones de elemento | `/util/catalogos/elementos/:elementId/conversiones` | Consulta de conversiones asociadas a un elemento. | Pendiente |
| Nueva conversión | `/util/catalogos/elementos/:elementId/conversiones/nueva` | Alta de conversión. | Pendiente |
| Editar conversión | `/util/catalogos/elementos/:elementId/conversiones/editar/:conversionId` | Edición de conversión existente. | Pendiente |
| Bitácora de actividades | `/util/auditoria/bitacora-actividades` | Consulta de logs/auditoría de actividad. | Pendiente |
| Tren de auditoría | `/util/auditoria/bitacora-actividades/tren/:traceId` | Consulta del tren de eventos de auditoría por `traceId`. | Pendiente |

**Figma / Storybook:** No disponible para Utilerías.

---

## 7. Seguridad y permisos

El MFE se integra con el estado global del portal mediante:

```ts
handleSubscribeToGlobalAuthenticationChange();
handleSubscribeToGlobalConfigurationChange();
```

La app usa `localHomeStore` para mantener estado local/global sincronizado y registra información de autenticación en consola durante el montaje. El código visible incluye logs de diagnóstico como:

```ts
logUtilAuthState('🔎 UTIL auth state BEFORE global subscriptions');
logUtilAuthState('🔐 UTIL Redux store changed');
```

**Importante:** aunque el token se muestra como preview parcial, se recomienda remover o condicionar estos logs para ambientes UAT/PRD.

El backend relacionado tiene seguridad habilitada según la variable compartida:

```env
SECURITY_ENABLED=true
KEYCLOAK_GROUP_VENDOR=ppsomx-vendor
KEYCLOAK_GROUP_ADMIN=ppsomx-admin
```

**Módulos de seguridad visibles en frontend:**

| Módulo | Ruta |
|--------|------|
| Perfil usuario | `/seguridad/perfil-usuario` |
| Perfil aplicativo | `/seguridad/perfil-aplicativo` |
| Perfil evento | `/seguridad/perfil-evento` |
| Aplicativo evento | `/seguridad/aplicativo-evento` |
| Gestión de usuarios | `/seguridad/gestion-usuarios` |
| Rol usuario | `/seguridad/rol-usuario` |
| Rol permiso | `/seguridad/rol-permiso` |
| Usuario atributo | `/seguridad/usuario-atributo` |

**Pendiente de confirmar:**

- Si existe gate de permisos por ruta/card similar a otros MFE.
- Si el shell debe registrar explícitamente `/seguridad` además de `/util`.
- Si `SECURITY_ENABLED=true` aplica a todos los ambientes o solo UAT/PRD.
- Si el frontend debe ocultar vistas según roles/permisos o si toda la validación queda en backend.

---

## 8. Estado actual

**Estado:** Pendiente de confirmar con ejecución real de pipeline y pruebas.  
**Cobertura de tests:** Pendiente de confirmar con `npm run test:cov`.  
**Backlog:** Pendiente de confirmar.  

**Bugs / riesgos conocidos:**

| Riesgo | Severidad | Contexto |
|--------|-----------|----------|
| Logs de token/auth en consola | Media / Alta | `App.tsx` imprime diagnóstico de autenticación y preview parcial del token. Se recomienda remover/condicionar para UAT/PRD. |
| Variables entregadas corresponden al backend | Media | `DB_HOST`, `DB_SCHEMA`, `DB_USER`, etc. son del backend Utilerías, no del SPA. Documentarlas sin exponerlas al frontend. |
| `APP_NAME` real pendiente | Media | Webpack usa `APP_NAME || 'util'`, pero el `package.json` se llama `utilerias`; confirmar nombre remoto usado por shell. |
| Rutas `/seguridad` fuera de `/util` | Media | Confirmar si el shell/app-shell registra también `/seguridad`, de lo contrario navegación directa puede no montar el MFE. |
| Instalación con `--legacy-peer-deps` | Baja / Media | El pipeline usa `npm install --legacy-peer-deps`; indica posibles conflictos de peer dependencies. |
| Cobertura real pendiente | Media | Existe `test:cov`, pero debe validarse si cumple quality gate. |

**Deuda técnica:**

- Confirmar `.env.example` / `.env.dist` con variables reales del frontend.
- Confirmar `APP_NAME` usado por el shell: `util`, `utilerias` u otro.
- Confirmar registro real en el shell/app-shell para `/util` y `/seguridad`.
- Remover o condicionar logs de token/autenticación en UAT/PRD.
- Documentar endpoints usados por cada pantalla.
- Confirmar cobertura mínima requerida por Sonar/Quality Gate.
- Agregar screenshots de pantallas principales.
- Documentar responsable del backend Utilerías.

---

## 9. Lo que no es obvio

- El proyecto usa `HashRouter`, por lo que las rutas internas viven bajo hash y la ruta real de montaje la decide el shell del portal.
- El `publicPath` de Webpack está configurado como `auto`, por lo que la carga de assets depende del contexto desde donde se cargue `remoteEntry.js`.
- El `uniqueName` de Webpack se toma desde `APP_NAME` o cae a `util`.
- El MFE expone `./App` y `./Card`; si el portal consume cards o accesos rápidos, confirmar si usa `util/Card`.
- El remoto de autenticación se carga desde `AUTHENTICATION_APP`; si la variable no existe, cae a `authentication@http://localhost:3001/remoteEntry.js`.
- En desarrollo, cualquier request `/api` se proxy a `BACKEND_URL` o `http://localhost:3712`.
- `npm run dev` aumenta `max-http-header-size` a `1048576`; esto probablemente responde a tokens o headers grandes del portal/gateway.
- La app inicializa TanStack Query con `refetchOnWindowFocus=false`, `refetchOnMount=false`, `refetchOnReconnect=false`, `retry=1` y `staleTime=30s`.
- El código visible registra en consola estado de autenticación y preview parcial de token; útil para debug, riesgoso si queda activo en ambientes compartidos.
- La ruta de auditoría `/util/auditoria/bitacora-actividades` aparece como módulo propio en Utilerías; en handovers anteriores se menciona que auditoría fue migrada desde Finanzas hacia Util SPA.
- Las variables `DB_*` compartidas son del backend y no deben empaquetarse en el bundle frontend.

---

## 10. CI/CD

El repositorio usa GitHub Actions con plantilla corporativa para frontend Node.

Según la configuración visible del pipeline:

```yaml
command_build: "npm run build"
command_dependency: "npm install --legacy-peer-deps"
command_test: "npm run test:cov"
runners: ${{ vars.RUNNER_MERCH }}
```

En deploy se observa configuración tipo:

```yaml
language: "node"
country: "CL"
business: "FA"
retail_type: "merch"
```

**Notas CI/CD:**

- El build productivo depende de `.env.production` o variables inyectadas por pipeline.
- Se usa `npm install --legacy-peer-deps`, no `npm ci`.
- El quality gate depende de `npm run test:cov` y del reporte generado por `scripts/test-cov.sh`.
- Confirmar si el artefacto final se publica en Cloud Storage / GCP Artifacts mediante plantilla FTI00382.
- Confirmar valores reales de `APP_URL`, `APP_NAME`, `AUTHENTICATION_APP` y `BACKEND_URL` por ambiente.

---

## 11. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Confirmar registro real del MFE en el shell/app-shell para `/util` y `/seguridad`. | Alta |
| Confirmar `APP_NAME` remoto real usado por Module Federation. | Alta |
| Agregar o actualizar `.env.example` con variables mínimas no sensibles del frontend. | Alta |
| Confirmar URL real de `BACKEND_URL` para DEV/UAT/PRD. | Alta |
| Remover o condicionar logs de token/autenticación para UAT/PRD. | Alta |
| Confirmar responsables de backend Utilerías y Authentication. | Alta |
| Documentar endpoints usados por parámetros, seguridad, catálogos y auditoría. | Media |
| Confirmar cobertura actual y política de quality gate. | Media |
| Agregar screenshots de pantallas principales. | Baja / Media |
| Confirmar si existe Figma, Storybook o documentación UX. | Baja / Media |

---

## 12. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | QA | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

## 13. Checklist de traspaso

| Ítem | Estado |
|------|--------|
| Repositorio confirmado | Completado |
| `package.json` revisado | Completado |
| Configuración Webpack revisada | Completado |
| Rutas principales identificadas | Completado |
| Variables frontend esperadas identificadas | Parcial |
| Variables backend relacionadas documentadas | Completado |
| Secretos removidos del handover | Completado |
| Scripts documentados | Completado |
| CI/CD documentado a nivel general | Parcial |
| Registro real en shell confirmado | Pendiente |
| Endpoints por pantalla documentados | Pendiente |
| Cobertura actual validada | Pendiente |
| Figma / Storybook confirmado | Pendiente |

---

*Completado por:* Oscar Bonelli, basado en código y configuración compartida · *Fecha:* 2026-06-30 · *Aceptado por:* Pendiente
