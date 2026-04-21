# Documentación técnica – MFE Aclaraciones SPA

Proyecto: `APP03022-mrch.frontend.somx.aclaraciones-spa`  
Tipo: Micro Frontend SPA (single-spa + React)  
Dominio: Portal de aclaraciones, centro de ayuda y mantenedores asociados.

---

## 1. Descripción funcional de alto nivel

### 1.1 Propósito del MFE

Este micro frontend forma parte de un portal de aclaraciones y atención al cliente. Provee:

- **Centro de ayuda (Help Center)**: recursos, FAQs, detalle de FAQs, recursos de soporte.
- **Gestión de casos (Cases/Requests)**: alta, consulta y seguimiento de casos de aclaración.
- **Gestión de FAQs**: mantenimiento de preguntas frecuentes (alta, edición, carga masiva).
- **Gestión de categorías**: mantenimiento de categorías de FAQs (alta, edición, carga masiva).
- **Información relacionada**: contenidos asociados a los temas de aclaraciones.
- **SLAs**: mantenimiento de niveles de servicio.
- **Notices**: secciones informativas / comunicados.
- **Feedback**: captura y mantenimiento de feedback de los usuarios.
- **Module Resolver**: configuración de resolutores por módulo.
- **Mantenedor**: vista de administración general del dominio.
- **Playground UI**: vista interna para probar componentes de UI.
- **Herramientas de debug**: debug de roles y estado global Redux.

La lógica de negocio y persistencia está en servicios backend; el MFE solo orquesta UI, llamadas HTTP, roles y navegación.

### 1.2 Flujos principales (según App.jsx)

Las rutas definidas en `src\App.jsx` son:

- **Landing por rol (`/`)**
  - Aplica lógica de rol efectivo (`useEffectiveRole`).
  - Si el usuario es `VENDOR` → redirige a `/home` (Help Center).
  - Otros roles (`TECH_ADMIN`, `ADMIN`, `RESOLVER`) → redirigen a `/mantenedor`.

- **Help Center**
  - `/home` → `HelpCenterContainer`.
  - `/help-center/resources` → `HelpCenterSupportResources`.
  - `/help-center/faqs/category` → `HelpCenterFaqByCategory`.
  - `/help-center/faqs/detail` → `HelpCenterFaqDetail`.

- **Casos (Requests)**
  - `/cases` → `RequestContainer` con `initialState={3}` (p. ej. vista de consulta).
  - `/cases/new` → `RequestContainer` con `initialState={2}` (alta).
  - `/cases/:id` → `RequestContainer` con `initialState={4}` (detalle/seguimiento de un caso).

- **FAQs**
  - `/faqs` → `FaqGrid` (listado).
  - `/faq/new` → `AddEditFaqForm` (alta).
  - `/faq/:id/edit` → `AddEditFaqForm` (edición).
  - `/faq/bulk-upload` → `BulkFaqUpload` (carga masiva).

- **Categorías**
  - `/categories` → `CategoryGridContainer` (listado).
  - `/categories/new` → `AddEditCategoryForm` (alta).
  - `/categories/:id` → `AddEditCategoryForm` (edición).
  - `/categories/bulk-upload` → `BulkCategoryUpload` (carga masiva).

- **Información relacionada**
  - `/relatedInformation` → `RelatedInformationContainer`.
  - `/relatedInformation/new` → `AddEditRelatedInformationForm`.
  - `/relatedInformation/:id/edit` → `AddEditRelatedInformationForm`.

- **SLAs**
  - `/slas` → `SlaGridContainer`.
  - `/slas/new` → `SlaAddEditForm`.
  - `/slas/:id` → `SlaAddEditForm`.

- **Notices**
  - `/notices` → `NoticeGridContainer`.
  - `/notices/new` → `NoticeAddEditForm`.
  - `/notices/:id` → `NoticeAddEditForm`.

- **Feedback**
  - `/feedback` → `FeedbackContainer`.
  - `/feedback/new` → `AddEditFeedbackForm`.
  - `/feedback/:id` → `AddEditFeedbackForm`.

- **Module Resolver**
  - `/moduleResolver` → `ModuleResolverContainer`.
  - `/moduleResolver/new` → `AddEditModuleResolverForm`.
  - `/moduleResolver/:id/edit` → `AddEditModuleResolverForm`.
  - Todas estas rutas están protegidas por `RequireNonVendor` (bloquea role `VENDOR`).

- **Mantenedor**
  - `/mantenedor` → `MaintainersContainer`.
  - Ruta protegida por `RequireNonVendor`.

- **Playground y utilidades**
  - `/playground/ui` → `UiPlayground`.
  - `/debug/roles` → `RolesDebug`.
  - `DebugGlobalRedux` se renderiza dentro de `<Layout>` para inspeccionar Redux global.

- **Fallback**
  - `*` → redirige a `/`.

---

## 2. Stack tecnológico (según package.json)

### 2.1 Lenguajes y frameworks

- **React 18** (`react`, `react-dom`).
- **TypeScript 4.7** (`typescript`).
- **React Router DOM v6** (`react-router-dom`).
- **Redux Toolkit** (`@reduxjs/toolkit`, `react-redux`).
- **Micro frontend**:
  - `single-spa`, `single-spa-react`.
  - `redux-micro-frontend` (interacción entre stores de MFEs, aunque no se ve en App.jsx, sí en dependencias).
- **Estilos / UI**:
  - `tailwindcss` 4 + `@tailwindcss/postcss` + `autoprefixer`.
  - `@emotion/react`, `@emotion/styled`.
  - `react-datepicker`.

### 2.2 Build y bundling

- **Webpack 5** (`webpack`, `webpack-cli`, `webpack-dev-server`).
- **Babel**:
  - Core y loaders: `@babel/core`, `babel-loader`.
  - Presets: `@babel/preset-env`, `@babel/preset-react`, `@babel/preset-typescript`.
- **Carga de recursos**:
  - CSS: `style-loader`, `css-loader`, `postcss-loader`.
  - HTML: `html-webpack-plugin`.
  - Copia de assets: `copy-webpack-plugin`.
- **Entorno**:
  - `cross-env` para variables multiplataforma.
  - `dotenv` + `dotenv-webpack` para variables de entorno en build.
  - `prebuild-webpack-plugin` para tareas previas.

### 2.3 Testing, linting y calidad

- **Jest 29**:
  - `jest`, `jest-environment-jsdom`, `babel-jest`.
  - `@testing-library/react`, `@testing-library/jest-dom`.
- **ESLint 8**:
  - `eslint`, `@eslint/js`, `eslint-plugin-react-hooks`, `eslint-plugin-react-refresh`, `typescript-eslint`, `globals`.
- **SonarQube**:
  - Configurado en `sonar-project.properties`.

### 2.4 Autenticación y cliente HTTP

- **OIDC / JWT**:
  - `oidc-client-ts` para client OIDC.
  - `jwt-decode` para decodificar tokens y extraer roles (se usa en slices de `authentication`, consumidos por `useAppSelector` en `App.jsx`).
- **HTTP**:
  - `axios` como cliente HTTP.
- **Interfaces compartidas**:
  - `@rtl/mrch.frontend.cross.common-interfaces` para tipos comunes.

### 2.5 Scripts NPM

Según `package.json`:

- `npm start`  
  `cross-env NODE_ENV=development NODE_OPTIONS=--max-http-header-size=1048576 webpack serve --mode development --config config/webpack.config.js`
- `npm run build`  
  `cross-env NODE_ENV=production webpack --mode production --config config/webpack.config.js`
- `npm run lint`  
  `eslint src`
- `npm test`  
  `jest`

### 2.6 Requisitos de entorno

- Node.js `>= 20.18.1`.
- npm compatible con Node 20.
- Variables de entorno definidas vía `.env`/CI, consumidas por `dotenv-webpack` (URLs de APIs, parámetros OIDC, etc.).

---

## 3. Arquitectura técnica

### 3.1 Integración con shell y single-spa

En `src\App.jsx` se exportan los ciclos de vida del MFE:

```jsx
export const { bootstrap, mount, unmount } = singleSpaReact({
    React,
    ReactDOM,
    rootComponent: App,
    errorBoundary(err, errInfo, props) {
        // ...
    },
});
```

- El shell single-spa registra este MFE con estos métodos.
- El componente raíz `App`:
  - Se envuelve en `<HashRouter>`.
  - Monta opcionalmente un **parcel de autenticación**:

    ```jsx
    <Parcel
      config={() => import('authentication/App')}
      mountParcel={mountRootParcel}
      authConfig={AuthConfigDefault}
    />
    ```

    Esto acopla este MFE a otro MFE `authentication/App` que provee flujo OIDC centralizado.

  - Utiliza `<Provider store={localHomeStore}>` para su store local Redux.
  - Protege rutas con `<PrivateRoute>` (auth) y componentes `Gate`/`RequireNonVendor` (dependientes de token y rol).

### 3.2 Autenticación y roles (según App.jsx)

Helpers clave:

- `useAppSelector` lee del estado Redux `s.authentication?.tokenDecoded`.
- `useAuthReady`:
  - Retorna `true` solo cuando existe un `tokenDecoded` en el store.
  - `Gate` bloquea render de rutas mientras no se haya decodificado el token.

- `useEffectiveRole`:
  - Usa `realm_roles` (`realm_access.roles`) y `resource_roles` (`resource_access['fbc-aclaraciones'].roles`).
  - Prioridad de roles (primera coincidencia gana):
    1. Si en realm: `FBC_TECH_ADMIN_USER` → `TECH_ADMIN`.
    2. Si en resource: `ppsomx-admin` → `ADMIN`.
    3. Si en resource: `ppsomx-resolver` → `RESOLVER`.
    4. Si en resource: `ppsomx-vendor` → `VENDOR`.
    5. En otro caso → `ADMIN` (fallback).

- `Landing`:
  - Si `VENDOR` → `/home`.
  - Otro rol → `/mantenedor`.

- `RequireNonVendor`:
  - Bloquea acceso a rutas de admin (`moduleResolver`, `mantenedor`) si el rol es `VENDOR`.

### 3.3 Estado y global state

- `localHomeStore`:
  - Store Redux local a este MFE (slices: `authentication`, dominios: casos, FAQs, etc.).
- `DebugGlobalRedux`:
  - Componente de debug que muestra el estado global (interacción con `redux-micro-frontend` y el store global del portal).
- `handleSubscribeToGlobalAuthenticationChange` / `handleSubscribeToGlobalConfigurationChange`:
  - Se invocan con `useEffect` en `App` para suscribirse a cambios de autenticación y configuración globales (publicados por el shell/otros MFEs).

### 3.4 Layout y navegación

- `<Layout>`:
  - Componente contenedor común a todas las páginas (header, menú, layout de contenido).
- `<HashRouter>`:
  - Usa hash-based routing (`#/ruta`) para integrarse de forma más simple en shells single-spa.
- `<PrivateRoute>`:
  - Se asume que:
    - Verifica que el usuario está autenticado (token válido).
    - Redirige o bloquea si no lo está.

---

## 4. Endpoints (visión de alto nivel)

Los endpoints concretos (URLs, cuerpos, respuestas) se documentan en:

- `docs/endpoints-aclaraciones.csv`

Por ahora, y en función de los dominios que se ven en `App.jsx`, se espera al menos consumo de APIs para:

- **Casos / Requests** (`/cases` o nombre corporativo real).
- **FAQs**.
- **Categorías**.
- **Información relacionada**.
- **SLAs**.
- **Notices**.
- **Feedback**.
- **Config/resolvers/mantenedor**.

La fuente de verdad técnica de estos endpoints está en:

- `src/api/*` (en particular `src/api/aclaraciones.ts` y otros servicios).
- Swagger/OpenAPI del backend respectivo.

---

## 5. Runbook

### 5.1 Desarrollo local

```bash
git clone <URL_REPO>
cd APP03022-mrch.frontend.somx.aclaraciones-spa

npm install

# Configurar .env u otra mecánica de entorno:
# - URLs de APIs
# - Config de OIDC (authority, clientId, redirectUri)
# - Cualquier otra variable usada por dotenv-webpack

npm start
# Abre el puerto configurado en config/webpack.config.js (ej. http://localhost:8080)
```

### 5.2 Build

```bash
npm run build
# Genera el bundle de producción para integrarse con el shell single-spa
```

### 5.3 Tests y Lint

```bash
npm test      # Jest + React Testing Library
npm run lint  # ESLint sobre src
```

Estos comandos se usan también en CI/CD (`.gitlab-ci.yml`, `.github/workflows`).

---

## 6. Estructura del proyecto (según listado-archivos.txt)

Raíz:

```text
APP03022-mrch.frontend.somx.aclaraciones-spa/
  ├─ .github/                # Workflows de GitHub Actions
  ├─ .npm/                   # Config npm corporativa
  ├─ .vscode/                # Config de editor
  ├─ config/                 # webpack.config.js y relacionados
  ├─ Copilot-/               # Metadatos Copilot (no productivo)
  ├─ docs/                   # Documentación técnica y CSV de endpoints
  ├─ public/                 # Recursos estáticos
  ├─ src/                    # Código fuente del MFE
  ├─ index.html              # Plantilla HTML
  ├─ jest.config.js, jest.setup.js, jest.mock.css.js
  ├─ eslint.config.js, .eslintrc
  ├─ tsconfig*.json
  ├─ postcss.config.js
  ├─ sonar-project.properties
  ├─ package.json
  ├─ .gitlab-ci.yml
  └─ listado-archivos.txt    # Listado generado de archivos
```

Estructura esperada bajo `src/` (a partir de imports en `App.jsx`):

```text
src/
  ├─ App.jsx
  ├─ App.css
  ├─ api/
  │   └─ aclaraciones.ts      # y otros clientes HTTP
  ├─ configuration/
  │   └─ ConfigurationBuilder
  ├─ domain/
  │   └─ authConfig
  ├─ features/
  │   ├─ helpCenter/
  │   ├─ cases/
  │   ├─ faq/
  │   ├─ categories/
  │   ├─ relatedInformation/
  │   ├─ sla/
  │   ├─ notices/
  │   ├─ feedback/
  │   ├─ moduleResolver/
  │   └─ playground/
  ├─ services/
  │   └─ globalStateService
  ├─ shared/
  │   ├─ components/container/Layout
  │   └─ utils/rolesDebug
  ├─ store/
  │   ├─ localStore
  │   ├─ hooks/useAppSelector
  │   └─ DebugGlobalRedux
  ├─ PrivateRoute
  └─ index.tsx                # entry single-spa-react
```

---

## 7. CI/CD y parametrización

- **CI/CD**:
  - `.gitlab-ci.yml` y `.github/workflows/*` orquestan:
    - Instalación (`npm ci`/`npm install`).
    - `npm run lint`, `npm test`.
    - `npm run build`.
    - Análisis SonarQube.
    - Publicación en artefact repository o directo al entorno del shell.

- **Parámetros de entorno**:
  - Variables usadas por `dotenv-webpack`, típicamente:
    - URLs base de APIs (aclaraciones/casos, FAQs, catálogos, etc.).
    - Config de OIDC.
    - Flags (modo debug, entornos).

---

## 8. Mantenimiento de esta documentación

- Nuevos módulos en `features/*` → documentar ruta y propósito en sección 1.2.
- Nuevos endpoints HTTP en `src/api` → agregar fila en `docs/endpoints-aclaraciones.csv`.
- Cambios de roles y reglas de navegación → actualizar sección 3.2.
- Cambios de tooling/scripts → actualizar secciones 2 y 5.
