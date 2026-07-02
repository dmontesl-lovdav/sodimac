# HANDOVER.md — `APP03022-mrch.backend.somx.util-api`

> Documento de traspaso basado en el `package.json`, variables de entorno, estructura visible del repositorio y configuración compartida para el backend de Utilerías.

**Proyecto:** APP03022-mrch.backend.somx.util-api  
**Tipo:** Backend / Microservicio Node.js + TypeScript  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.util-api  
**Fecha:** 2026-06-30  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Carlos Rojas Burgos — cjrojasb@Falabella.cl  

---

## 1. ¿Qué hace este proyecto?

Este proyecto es el backend de **Utilerías** para el portal **Falabella Business Center / PPSOMX**. Expone servicios utilizados por el micro frontend `util.spa` y por otros backends del ecosistema para consultar y operar catálogos, parámetros del sistema, seguridad, auditoría, proveedores, bloqueos de proveedor, conversiones, elementos de catálogo y trazabilidad operativa.

De acuerdo con la estructura visible del código, el backend agrupa controladores para módulos como catálogos, elementos de catálogo, administración de catálogos, conversiones, parámetros, auditoría, seguridad, usuarios/perfiles/roles, layout validation, procesos, mensajes, módulos, proveedores, bloqueos de proveedor, tipos de ítem y tren de estados.

El servicio se conecta a PostgreSQL mediante TypeORM y utiliza el esquema `core_utils`. También genera documentación OpenAPI/Swagger desde scripts internos y expone endpoints transversales utilizados por otros dominios como Finanzas y Fiscal.

**Consumidores / usuarios:**

- Micro frontend `APP03022-mrch.frontend.somx.util.spa`.
- Micro frontends del portal FBC que consulten catálogos, parámetros o seguridad.
- Backends de dominio que consumen atributos de seguridad, catálogos o validaciones auxiliares.
- Proveedores del portal FBC.
- Usuarios internos con permisos de administración y operación.

**Criticidad:** Alta. El backend soporta servicios transversales de catálogos, parámetros, seguridad, auditoría y validaciones compartidas por otros módulos del portal.

**Documentación adicional:**

| Recurso | Link |
|---------|------|
| Repositorio | [APP03022-mrch.backend.somx.util-api](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.util-api) |
| Swagger / OpenAPI | Generado por `npm run openapi:gen`; confirmar ruta pública del ambiente |
| Postman collection | No disponible |
| Confluence / documentación técnica | [Documentación Utilerías Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897549760/Documentacion+Utilerias+Dev) |
| Tablero Jira / backlog | No disponible |
| README del repositorio | [README.md](README.md) |

---

## 2. Cómo levantar el proyecto

**Requisitos:**

- Node.js compatible con TypeScript moderno. Recomendado: Node 20+.
- npm.
- Acceso al repositorio.
- Acceso a PostgreSQL del ambiente correspondiente.
- Variables de entorno configuradas.
- Acceso a red corporativa / VPN / runner con visibilidad hacia la base de datos, si aplica.

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.util-api
cd APP03022-mrch.backend.somx.util-api

# usar como base la rama develop
git checkout develop
git pull origin develop

# crear archivo .env con las variables del ambiente
npm install
npm run dev
```

**Scripts disponibles:**

```bash
npm run dev
npm run start:dev
npm run build
npm start
npm run openapi:gen
npm run openapi:watch
npm run lint
npm run typeorm
npm run migration:generate
npm run migration:create
npm run migration:run
npm run migration:revert
npm run migration:show
npm test
npm run test:watch
npm run test:cov
```

| Script | Descripción |
|--------|-------------|
| `npm run dev` | Levanta el servicio con `nodemon`, observando `src` y ejecutando `tsx` sobre `src/server.ts`. |
| `npm run start:dev` | Levanta el servicio directamente con `tsx`. |
| `npm run build` | Limpia `dist`, compila TypeScript y resuelve alias con `tsc-alias`. |
| `npm start` | Ejecuta el build compilado desde `dist/server.js`. |
| `npm run openapi:gen` | Genera documentación OpenAPI desde `scripts/gen-openapi.ts`. |
| `npm run openapi:watch` | Regenera OpenAPI al detectar cambios en `src/docs`. |
| `npm run lint` | Ejecuta ESLint sobre archivos TS/JS dentro de `src`. |
| `npm run typeorm` | Ejecuta CLI de TypeORM usando `src/config/typeorm-datasource.ts`. |
| `npm run migration:*` | Comandos TypeORM para generar, crear, ejecutar, revertir y mostrar migraciones. |
| `npm test` | Ejecuta Jest con `--passWithNoTests`. |
| `npm run test:watch` | Ejecuta Jest en modo watch. |
| `npm run test:cov` | Ejecuta cobertura usando `scripts/test-cov.sh`. |

**Puerto local:**

```env
PORT=8082
```

**Notas de arranque local:**

- El entrypoint de desarrollo es `src/server.ts`.
- El proyecto usa TypeScript con `tsx` y `tsconfig-paths/register`.
- El build productivo genera salida en `dist`.
- El servicio se ejecuta en `PORT`; para el ambiente compartido se está usando `8082`.
- La conexión a base de datos usa PostgreSQL mediante `pg` y TypeORM.
- El esquema de base de datos configurado es `core_utils`.
- `SECURITY_ENABLED=true` indica que el middleware de seguridad debe ejecutarse.
- Para correr localmente puede requerirse ajustar temporalmente seguridad o usar tokens válidos del portal, según configuración del middleware.

---

## 3. Variables de entorno

> Importante: el valor literal de `DB_PASS` no se deja escrito en este handover. Debe mantenerse en GitHub Secrets, Secret Manager o el mecanismo oficial de secretos del ambiente.

### Ambiente compartido / UAT backend

```env
PORT=8082
NODE_ENV=development
SECURITY_ENABLED=true

DB_HOST=10.100.64.102
DB_PORT=5432
DB_USER=wwwb2bportal
DB_PASS=<SECRET_DB_PASS>
DB_NAME=b2b_portal
DB_SCHEMA=core_utils
DB_SSL=true

KEYCLOAK_GROUP_VENDOR=ppsomx-vendor
KEYCLOAK_GROUP_ADMIN=ppsomx-admin

PAGINATION_DEFAULT_SIZE=20
PAGINATION_MAX_SIZE=100
MAX_HEADER_SIZE=65536
```

### Tabla de variables

| Variable | Descripción | Valor ambiente compartido / UAT | ¿Secreta? |
|----------|-------------|----------------------------------|:---------:|
| `PORT` | Puerto donde escucha el backend. | `8082` | No |
| `NODE_ENV` | Modo de ejecución Node. | `development` | No |
| `SECURITY_ENABLED` | Habilita/deshabilita middleware de seguridad general. | `true` | No |
| `DB_HOST` | Host de PostgreSQL. | `10.100.64.102` | Sí / interno |
| `DB_PORT` | Puerto de PostgreSQL. | `5432` | No |
| `DB_USER` | Usuario de conexión a PostgreSQL. | `wwwb2bportal` | Sí |
| `DB_PASS` | Password de conexión a PostgreSQL. | Revisar secreto del ambiente | Sí |
| `DB_NAME` | Nombre de base de datos. | `b2b_portal` | No |
| `DB_SCHEMA` | Esquema PostgreSQL usado por Utilerías. | `core_utils` | No |
| `DB_SSL` | Habilita conexión SSL a PostgreSQL. | `true` | No |
| `KEYCLOAK_GROUP_VENDOR` | Grupo Keycloak para perfil proveedor. | `ppsomx-vendor` | No |
| `KEYCLOAK_GROUP_ADMIN` | Grupo Keycloak para perfil administrador. | `ppsomx-admin` | No |
| `PAGINATION_DEFAULT_SIZE` | Tamaño default de paginación. | `20` | No |
| `PAGINATION_MAX_SIZE` | Tamaño máximo permitido de paginación. | `100` | No |
| `MAX_HEADER_SIZE` | Tamaño máximo de headers aceptado por el servidor/proceso. | `65536` | No |

**Variables pendientes de confirmar:**

| Variable | Motivo |
|----------|--------|
| `NODE_ENV` | Para UAT normalmente debería validarse si corresponde `development` o `production`, según plantilla CI/CD. |
| `SECURITY_ENABLED` | Confirmar política final por ambiente. En la configuración compartida está en `true`. |
| `DB_SCHEMA` | Confirmar que `core_utils` sea el esquema definitivo en DEV/UAT/PRD. |
| `MAX_HEADER_SIZE` | Confirmar si se usa directamente por el servidor o si se requiere configurar también proxy/ingress/load balancer. |

---

## 4. Arquitectura y dependencias

**Tipo:** Backend / API Node.js + Express + TypeScript.

El proyecto está declarado como módulo ESM mediante:

```json
{
  "type": "module"
}
```

El backend usa Express como servidor HTTP, TypeORM para persistencia sobre PostgreSQL, validaciones con `zod`, `class-validator` y `class-transformer`, generación de documentación Swagger/OpenAPI, seguridad con JWT/Jose/Keycloak groups, logging con Pino y soporte para exportación/importación de archivos mediante ExcelJS y Multer.

**Stack:**

| Categoría | Tecnología |
|-----------|------------|
| Runtime | Node.js |
| Lenguaje | TypeScript |
| Framework HTTP | Express 5 |
| Módulos | ESM (`type: module`) |
| ORM / DB | TypeORM + PostgreSQL (`pg`) |
| Esquema DB | `core_utils` |
| Validación | Zod, class-validator, class-transformer |
| Auth / Seguridad | Jose, Helmet, Keycloak groups |
| HTTP Client | Axios |
| Logging | Pino, pino-pretty |
| OpenAPI / Swagger | swagger-jsdoc, swagger-ui-express, openapi-types |
| Archivos / Excel | Multer, ExcelJS |
| Transacciones | cls-hooked, typeorm-transactional-cls-hooked |
| Testing | Jest + ts-jest |
| Build TS | TypeScript, tsx, tsc-alias, tsconfig-paths |

**Dependencias principales:**

- `express`
- `typeorm`
- `pg`
- `axios`
- `zod`
- `class-validator`
- `class-transformer`
- `jose`
- `helmet`
- `cors`
- `swagger-jsdoc`
- `swagger-ui-express`
- `pino`
- `pino-pretty`
- `exceljs`
- `multer`
- `reflect-metadata`
- `cls-hooked`
- `typeorm-transactional-cls-hooked`
- `http-status-codes`

---

## 5. Módulos / controladores principales

Según la estructura visible del repositorio, existen controladores para los siguientes dominios:

| Controlador | Dominio funcional esperado |
|-------------|----------------------------|
| `applicationMsg.controller.ts` | Mensajes de aplicación / configuración de mensajes. |
| `auditLog.controller.ts` | Auditoría / bitácora de operaciones. |
| `catalog.controller.ts` | Catálogos generales. |
| `catalogElement.controller.ts` | Elementos de catálogo. |
| `catalogManagement.controller.ts` | Administración de catálogos. |
| `conversion.controller.ts` | Conversiones entre elementos/catálogos. |
| `health.controller.ts` | Health general del servicio. |
| `item.controller.ts` | Ítems / entidades de dominio utilitario. |
| `itemType.controller.ts` | Tipos de ítem. |
| `layoutValidation.controller.ts` | Validaciones de layout / estructura de datos. |
| `message.controller.ts` | Mensajes transversales. |
| `module.controller.ts` | Módulos / aplicativos. |
| `parameter.controller.ts` | Parámetros del sistema. |
| `process.controller.ts` | Procesos / eventos de aplicación. |
| `security.controller.ts` | Seguridad, roles, permisos, grupos o atributos de usuario. |
| `statusTrain.controller.ts` | Tren de estados / trazabilidad de estatus. |
| `supplier.controller.ts` | Proveedores. |
| `supplierBlock.controller.ts` | Bloqueos de proveedor. |

**Nota:** validar rutas exactas en `src/server.ts`, archivo de registro de routers o documentación OpenAPI generada.

---

## 6. Integraciones externas

| Servicio | Variable / Configuración | Uso esperado |
|----------|--------------------------|--------------|
| PostgreSQL | `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `DB_NAME`, `DB_SCHEMA`, `DB_SSL` | Persistencia del backend Utilerías. |
| Keycloak / Portal Auth | `KEYCLOAK_GROUP_VENDOR`, `KEYCLOAK_GROUP_ADMIN`, `SECURITY_ENABLED` | Validación de seguridad, perfiles y grupos. |
| Micro frontend Utilerías | `APP03022-mrch.frontend.somx.util.spa` | Consumidor principal de catálogos, parámetros, seguridad y auditoría. |
| Backends de dominio | Consumo vía URL pública/interna del backend util | Catálogos, parámetros, auditoría, atributos de usuario y validaciones auxiliares. |

---

## 7. Base de datos y migraciones

El proyecto usa TypeORM mediante el datasource:

```bash
src/config/typeorm-datasource.ts
```

Comandos disponibles:

```bash
npm run migration:generate
npm run migration:create
npm run migration:run
npm run migration:revert
npm run migration:show
```

**Datos de conexión compartidos:**

| Campo | Valor |
|-------|-------|
| Motor | PostgreSQL |
| Host | `10.100.64.102` |
| Puerto | `5432` |
| Base | `b2b_portal` |
| Esquema | `core_utils` |
| Usuario | `wwwb2bportal` |
| SSL | `true` |
| Password | Gestionar como secreto |

**Notas:**

- No subir credenciales a GitHub.
- Confirmar si el acceso a `10.100.64.102` requiere VPN, Cloud SQL Proxy, red corporativa o runner específico.
- Confirmar si las migraciones se ejecutan manualmente o como parte del pipeline.
- Confirmar si existe data base seed o carga inicial para catálogos/parámetros.

---

## 8. Seguridad y permisos

El backend tiene bandera de seguridad:

```env
SECURITY_ENABLED=true
```

También configura grupos Keycloak:

```env
KEYCLOAK_GROUP_VENDOR=ppsomx-vendor
KEYCLOAK_GROUP_ADMIN=ppsomx-admin
```

**Interpretación actual:**

- `SECURITY_ENABLED` controla seguridad general o middleware de protección global.
- Los grupos Keycloak separan permisos de proveedor y administrador.
- El servicio puede ser consultado por otros backends para atributos de usuario, catálogos o validaciones de permisos.

**Pendiente importante:**

Confirmar cómo deben quedar estas banderas por ambiente:

| Ambiente | `SECURITY_ENABLED` | Comentario |
|----------|--------------------|------------|
| Local | Pendiente | Puede requerir `false` para desarrollo local o tokens reales. |
| DEV | Pendiente | Confirmar con arquitectura/seguridad. |
| UAT | `true` compartido | Validar contra deployment real. |
| PRD | `true` esperado | Confirmar política real. |

---

## 9. CI/CD

El repositorio usa GitHub Actions. En la configuración visible se identifica un workflow tipo microservicio Node.

**Jobs visibles / esperados:**

| Job | Descripción |
|-----|-------------|
| `build` | Instala dependencias, ejecuta build y pruebas/cobertura. |
| `build_docker` | Construye imagen Docker usando plantilla corporativa. |
| `deploy` | Despliega artefacto/imagen según plantilla corporativa. |

**Comandos usados por el pipeline:**

```yaml
command_dependency: "npm install"
command_build: "npm run build"
command_test: "npm run test:cov"
runners: ${{ vars.RUNNER_MERCH }}
```

**Notas CI/CD:**

- Confirmar si `npm install` debe mantenerse o cambiarse a `npm ci` cuando exista `package-lock.json` estable.
- Confirmar si la política de Sonar exige cobertura mínima de 80%.
- `npm run test:cov` depende de `scripts/test-cov.sh`.
- El script de cobertura crea `coverage/lcov.info` si Jest no genera archivo.
- Confirmar variables reales inyectadas por ambiente desde GitHub Secrets/Variables.
- Confirmar que `DB_PASS` y credenciales de base no queden en YAML, Dockerfile ni archivos `.env` versionados.

---

## 10. Swagger / OpenAPI

El proyecto incluye generación de documentación OpenAPI:

```bash
npm run openapi:gen
npm run openapi:watch
```

El script ejecuta:

```bash
tsx -r tsconfig-paths/register scripts/gen-openapi.ts
```

**Pendiente de confirmar:**

- Ruta pública del Swagger en local.
- Ruta pública del Swagger en UAT.
- Si el archivo generado se commitea o se genera únicamente durante build.
- Si la documentación vive bajo `/docs`, `/swagger`, `/api-docs` u otra ruta.
- Si existe BFF/Cloud Endpoints separado para exponer la documentación pública.

---

## 11. Estado actual

**Estado:** Pendiente de confirmar con ejecución real de pipeline y pruebas.  
**Cobertura de tests:** Pendiente de confirmar con `npm run test:cov`.  
**Base de datos:** PostgreSQL `b2b_portal`.  
**Esquema:** `core_utils`.  
**Puerto compartido:** `8082`.  

**Bugs / riesgos conocidos:**

| Riesgo | Severidad | Contexto |
|--------|-----------|----------|
| `DB_PASS` compartida fuera de gestor de secretos | Alta | Debe mantenerse solo en Secret Manager / GitHub Secrets. |
| `SECURITY_ENABLED=true` puede complicar arranque local | Media | Para desarrollo local se requiere token/contexto válido o ajuste controlado. |
| `NODE_ENV=development` en variables compartidas | Media | Confirmar si para UAT debería ser `production`. |
| `DB_SCHEMA=core_utils` debe existir en el ambiente | Alta | Si el esquema no existe o el usuario no tiene permisos, el servicio no levanta o falla al consultar. |
| Quality gate sin pruebas suficientes | Media / Alta | Confirmar cobertura real y política de Sonar. |
| OpenAPI generado por script separado | Media | Si no se genera/actualiza, Swagger puede quedar desfasado. |
| Headers grandes | Media | `MAX_HEADER_SIZE=65536` debe estar soportado también por proxy/ingress/load balancer. |

**Deuda técnica:**

- Agregar `.env.example` sin secretos.
- Documentar rutas exactas generadas por cada controlador.
- Confirmar Swagger público por ambiente.
- Confirmar responsables de consumidores principales: util.spa, finanzas-api, fiscal-api y BFFs.
- Confirmar política de seguridad/JWT por ambiente.
- Confirmar si migraciones se ejecutan manualmente o vía pipeline.
- Homologar variables DEV/UAT/PRD.
- Revisar cobertura actual y quality gate.
- Documentar cargas iniciales de catálogos/parámetros si existen.

---

## 12. Lo que no es obvio

- El proyecto compila como ESM (`type: module`), por lo que imports/exports y ejecución con Node deben respetar este modo.
- `npm run dev` usa `nodemon` + `tsx`, no `ts-node` directamente.
- La base de datos usa SSL (`DB_SSL=true`) y esquema explícito `core_utils`.
- El backend Utilerías es transversal: no solo lo consume `util.spa`; otros backends pueden usarlo para catálogos, atributos de seguridad y validaciones.
- El script de cobertura crea `coverage/lcov.info` si no existe, para que el pipeline tenga salida de cobertura.
- El proyecto maneja transacciones con `typeorm-transactional-cls-hooked` y contexto con `cls-hooked`.
- Hay rutas de auditoría y tren de estados que pueden ser consumidas desde otros módulos, no solo desde Utilerías.
- Si `MAX_HEADER_SIZE=65536` es requerido por tokens o headers grandes, debe confirmarse que también esté soportado por proxy/ingress/load balancer.
- El build actual no ejecuta `openapi:gen` explícitamente según el `package.json` compartido; si se requiere OpenAPI actualizado en artefacto, confirmar si el pipeline lo ejecuta por separado.

---

## 13. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Crear `.env.example` sin secretos y con comentarios por variable. | Alta |
| Mover/validar `DB_PASS` en Secret Manager o GitHub Secrets. | Alta |
| Confirmar si `NODE_ENV=development` es correcto para UAT. | Alta |
| Confirmar política de `SECURITY_ENABLED` por ambiente. | Alta |
| Confirmar existencia y permisos del esquema `core_utils`. | Alta |
| Ejecutar `npm run build` y `npm run test:cov` para validar estado del proyecto. | Alta |
| Documentar rutas exactas por controlador desde OpenAPI generado. | Media |
| Agregar link de Swagger por ambiente. | Media |
| Confirmar flujo de migraciones. | Media |
| Confirmar si hay seeds/cargas iniciales de catálogos y parámetros. | Media |
| Confirmar cobertura mínima exigida por Sonar/Quality Gate. | Media |
| Documentar consumidores internos del endpoint de seguridad / atributos de usuario. | Media |

---

## 14. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | QA | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

## 15. Checklist de traspaso

| Ítem | Estado |
|------|--------|
| Repositorio confirmado | Completado |
| `package.json` revisado | Completado |
| Variables principales identificadas | Completado |
| Secretos removidos del handover | Completado |
| Scripts documentados | Completado |
| CI/CD documentado a nivel general | Parcial |
| Rutas exactas por endpoint | Pendiente |
| Swagger público confirmado | Pendiente |
| Seguridad por ambiente confirmada | Pendiente |
| Esquema `core_utils` validado | Pendiente |
| Cobertura actual validada | Pendiente |

---

*Completado por:* Oscar Bonelli, basado en código y configuración compartida · *Fecha:* 2026-06-30 · *Aceptado por:* Pendiente
