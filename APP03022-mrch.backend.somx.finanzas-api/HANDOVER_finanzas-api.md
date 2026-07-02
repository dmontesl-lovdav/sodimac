# HANDOVER.md — `APP03022-mrch.backend.somx.finanzas-api`

> Documento de traspaso basado en el `package.json`, variables de entorno, estructura visible del repositorio y configuración compartida para el backend de Finanzas.

**Proyecto:** APP03022-mrch.backend.somx.finanzas-api  
**Tipo:** Backend / Microservicio Node.js + TypeScript  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.finanzas-api  
**Fecha:** 2026-06-24  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Carlos Rojas Burgos — cjrojasb@Falabella.cl  

---

## 1. ¿Qué hace este proyecto?

Este proyecto es el backend de **Finanzas** para el portal **Falabella Business Center / PPSOMX**. Expone servicios utilizados por el micro frontend `finanzas-spa` para consultar y operar información financiera de proveedores y usuarios internos.

De acuerdo con la estructura visible del código, el backend agrupa controladores para módulos como pagos, estado de cuenta, cuentas por pagar, carta porte, guías de embarque, MIGO, Three Way Match, rebates, documentos SAP, auditoría, órdenes de compra, bloqueo de proveedor, healthchecks y operaciones transaccionales.

El servicio se conecta a PostgreSQL, consume servicios externos de Util / Catálogos y Fiscal, y utiliza Google Cloud Storage para almacenamiento de archivos relacionados con Finanzas.

**Consumidores / usuarios:**

- Micro frontend `APP03022-mrch.frontend.somx.finanzas-spa`.
- Proveedores del portal FBC.
- Usuarios internos con permisos sobre módulos financieros.
- Otros backends BFF / utilitarios que consulten información financiera.

**Criticidad:** Alta. El backend soporta flujos operativos financieros, consulta de pagos, documentos, estados, recepciones, integraciones externas y almacenamiento de archivos.

**Documentación adicional:**

| Recurso | Link |
|---------|------|
| Repositorio | [APP03022-mrch.backend.somx.finanzas-api](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.finanzas-api) |
| Swagger / OpenAPI | Generado por `npm run openapi:gen`; confirmar ruta pública del ambiente |
| Postman collection | No disponible |
| Confluence / documentación técnica | [Documentación Finanzas Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897680760/Documentacion+Finanzas+Dev) |
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
- Acceso a servicios externos configurados: Util / Catálogos, Fiscal y GCS.

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.finanzas-api
cd APP03022-mrch.backend.somx.finanzas-api

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
npm run lint:nb
npm run test
npm run test:cov
npm run seed
npm run seed:reset
npm run migration:generate
npm run migration:create
npm run migration:run
npm run migration:revert
npm run migration:show
```

| Script | Descripción |
|--------|-------------|
| `npm run dev` | Levanta el servicio con `nodemon`, observando `src` y ejecutando `tsx` sobre `src/server.ts`. |
| `npm run start:dev` | Levanta el servicio directamente con `tsx`. |
| `npm run build` | Limpia `dist`, compila TypeScript, resuelve alias con `tsc-alias` y genera OpenAPI. |
| `npm start` | Ejecuta el build compilado desde `dist/server.js`. |
| `npm run openapi:gen` | Genera documentación OpenAPI desde `scripts/gen-openapi.ts`. |
| `npm run openapi:watch` | Regenera OpenAPI al detectar cambios en `src/docs`. |
| `npm run lint` | Ejecuta ESLint sobre archivos TS/JS dentro de `src`. |
| `npm run lint:nb` | Ejecuta lint sin bloquear el proceso por errores. |
| `npm run test` | Ejecuta Jest con `--passWithNoTests`. |
| `npm run test:cov` | Ejecuta cobertura usando `scripts/test-cov.sh`. |
| `npm run seed` | Ejecuta semillas desde `src/seeds/index.ts`. |
| `npm run seed:reset` | Revierte migración, ejecuta migración y vuelve a correr seeds. |
| `npm run migration:*` | Comandos TypeORM para generar, crear, ejecutar, revertir y mostrar migraciones. |

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
- Si `SECURITY_ENABLED=false` y `FINANZAS_JWT_ENABLED=false`, el servicio corre sin validación estricta de seguridad/JWT local.
- Para ambientes reales debe confirmarse que la seguridad quede habilitada según política del portal.

---

## 3. Variables de entorno

> Importante: el valor literal de `DB_PASS` no se deja escrito en este handover. Debe mantenerse en GitHub Secrets, Secret Manager o el mecanismo oficial de secretos del ambiente.

### Ambiente compartido / UAT backend

```env
PORT=8082
NODE_ENV=development
SECURITY_ENABLED=false

DB_HOST=10.100.64.102
DB_PORT=5432
DB_USER=wwwb2bportal
DB_PASS=<SECRET_DB_PASS>
DB_NAME=b2b_portal
DB_SSL=true

KEYCLOAK_GROUP_VENDOR=ppsomx-vendor
KEYCLOAK_GROUP_ADMIN=ppsomx-admin

CATALOGS_API_URL_BFF=https://uat.fbusinesscenter.com/ppsomx/backend-util

GCS_BUCKET=fs-sod-mx-mrch-aclaraciones-uat
GCS_PREFIX=bk-finanzas-uat/finanzas-somx/

FINANZAS_JWT_ENABLED=false

PAGINATION_DEFAULT_SIZE=20
PAGINATION_MAX_SIZE=100
MAX_HEADER_SIZE=65536

FISCAL_API_URL=https://fiscal-api.tu-dominio
CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN=/ruta/que/valida/modulo-y-pantalla

UTIL_API_URL=https://uat.fbusinesscenter.com/ppsomx/backend-util
```

### Tabla de variables

| Variable | Descripción | Valor ambiente compartido / UAT | ¿Secreta? |
|----------|-------------|----------------------------------|:---------:|
| `PORT` | Puerto donde escucha el backend. | `8082` | No |
| `NODE_ENV` | Modo de ejecución Node. | `development` | No |
| `SECURITY_ENABLED` | Habilita/deshabilita middleware de seguridad general. | `false` | No |
| `DB_HOST` | Host de PostgreSQL. | `10.100.64.102` | Sí / interno |
| `DB_PORT` | Puerto de PostgreSQL. | `5432` | No |
| `DB_USER` | Usuario de conexión a PostgreSQL. | `wwwb2bportal` | Sí |
| `DB_PASS` | Password de conexión a PostgreSQL. | Revisar secreto del ambiente | Sí |
| `DB_NAME` | Nombre de base de datos. | `b2b_portal` | No |
| `DB_SSL` | Habilita conexión SSL a PostgreSQL. | `true` | No |
| `KEYCLOAK_GROUP_VENDOR` | Grupo Keycloak para perfil proveedor. | `ppsomx-vendor` | No |
| `KEYCLOAK_GROUP_ADMIN` | Grupo Keycloak para perfil administrador. | `ppsomx-admin` | No |
| `CATALOGS_API_URL_BFF` | Base URL del backend Util / Catálogos consumido vía BFF. | `https://uat.fbusinesscenter.com/ppsomx/backend-util` | No |
| `GCS_BUCKET` | Bucket de Google Cloud Storage usado por el servicio. | `fs-sod-mx-mrch-aclaraciones-uat` | No |
| `GCS_PREFIX` | Prefijo/carpeta lógica dentro del bucket para archivos de Finanzas. | `bk-finanzas-uat/finanzas-somx/` | No |
| `FINANZAS_JWT_ENABLED` | Habilita/deshabilita validación JWT propia del backend Finanzas. | `false` | No |
| `PAGINATION_DEFAULT_SIZE` | Tamaño default de paginación. | `20` | No |
| `PAGINATION_MAX_SIZE` | Tamaño máximo permitido de paginación. | `100` | No |
| `MAX_HEADER_SIZE` | Tamaño máximo de headers aceptado por el servidor. | `65536` | No |
| `FISCAL_API_URL` | URL base del backend Fiscal. | `https://fiscal-api.tu-dominio` | No / pendiente |
| `CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN` | Path para validar origen de transacción por módulo/pantalla. | `/ruta/que/valida/modulo-y-pantalla` | No / pendiente |
| `UTIL_API_URL` | URL base del backend Util. | `https://uat.fbusinesscenter.com/ppsomx/backend-util` | No |

**Variables pendientes de confirmar:**

| Variable | Motivo |
|----------|--------|
| `FISCAL_API_URL` | El valor compartido parece placeholder: `https://fiscal-api.tu-dominio`. Confirmar URL real de UAT/DEV/PRD. |
| `CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN` | El valor compartido parece placeholder: `/ruta/que/valida/modulo-y-pantalla`. Confirmar path real. |
| `NODE_ENV` | Para UAT normalmente debería validarse si corresponde `development` o `production`, según plantilla CI/CD. |
| `SECURITY_ENABLED` / `FINANZAS_JWT_ENABLED` | Confirmar política de seguridad para cada ambiente. En UAT/PRD normalmente no deberían quedar deshabilitadas sin aprobación. |
| `GCS_BUCKET` | El nombre contiene `aclaraciones-uat`; confirmar si es intencional reutilizar bucket de aclaraciones para Finanzas o si se requiere bucket propio. |

---

## 4. Arquitectura y dependencias

**Tipo:** Backend / API Node.js + Express + TypeScript.

El proyecto está declarado como módulo ESM mediante:

```json
{
  "type": "module"
}
```

El backend usa Express como servidor HTTP, TypeORM para persistencia sobre PostgreSQL, librerías de validación con `zod`, `class-validator` y `class-transformer`, generación de documentación Swagger/OpenAPI, seguridad con JWT/Jose/Keycloak, y almacenamiento externo en Google Cloud Storage.

**Stack:**

| Categoría | Tecnología |
|-----------|------------|
| Runtime | Node.js |
| Lenguaje | TypeScript |
| Framework HTTP | Express 5 |
| Módulos | ESM (`type: module`) |
| ORM / DB | TypeORM + PostgreSQL (`pg`) |
| Validación | Zod, zod-form-data, class-validator, class-transformer |
| Auth / Seguridad | JWT, Jose, jsonwebtoken, Helmet, Keycloak groups |
| Storage | Google Cloud Storage (`@google-cloud/storage`) |
| HTTP Client | Axios + axios-retry |
| Logging | Pino, pino-pretty, Winston |
| OpenAPI / Swagger | swagger-jsdoc, swagger-ui-express, openapi-types |
| Archivos / XML / PDF | multer, archiver, xml2js, fast-xml-parser, pdfkit, puppeteer, exceljs |
| Testing | Jest + ts-jest |
| Build TS | TypeScript, tsx, tsc-alias, tsconfig-paths |

**Dependencias principales:**

- `express`
- `typeorm`
- `pg`
- `axios`
- `axios-retry`
- `zod`
- `zod-form-data`
- `class-validator`
- `class-transformer`
- `jsonwebtoken`
- `jose`
- `helmet`
- `cors`
- `@google-cloud/storage`
- `swagger-jsdoc`
- `swagger-ui-express`
- `pino`
- `winston`
- `exceljs`
- `pdfkit`
- `puppeteer`
- `multer`
- `xml2js`
- `fast-xml-parser`

---

## 5. Módulos / controladores principales

Según la estructura visible del repositorio, existen controladores para los siguientes dominios:

| Controlador | Dominio funcional esperado |
|-------------|----------------------------|
| `accountsPayable.controller.ts` | Cuentas por pagar. |
| `accountStatement.controller.ts` | Estado de cuenta. |
| `auditLog.controller.ts` | Auditoría / bitácora de operaciones. |
| `cartaPorte.controller.ts` | Carta Porte. |
| `finanzasPayment.controller.ts` | Pagos de Finanzas. |
| `fiscalPayment.controller.ts` | Pagos / integración Fiscal. |
| `health.controller.ts` | Health general del servicio. |
| `healthcheck.controller.ts` | Healthcheck técnico / monitoreo. |
| `migo.controller.ts` | Recepciones MIGO. |
| `purchaseOrder.controller.ts` | Órdenes de compra. |
| `rebate.controller.ts` | Rebates / descuentos. |
| `rebateFiscal.controller.ts` | Integración fiscal de rebates. |
| `sapDocument.controller.ts` | Documentos SAP. |
| `shippingGuide.controller.ts` | Guías de embarque. |
| `stampedRebate.controller.ts` | Rebates timbrados. |
| `storageGCP.controller.ts` | Operaciones con Google Cloud Storage. |
| `threeWayMatch.controller.ts` | Three Way Match. |
| `transactional.controller.ts` | Operaciones transaccionales / validaciones. |
| `vendorBlock.controller.ts` | Bloqueo de proveedor. |

**Nota:** validar rutas exactas en `src/server.ts`, archivo de registro de routers o documentación OpenAPI generada.

---

## 6. Integraciones externas

| Servicio | Variable / Configuración | Uso esperado |
|----------|--------------------------|--------------|
| PostgreSQL | `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `DB_NAME`, `DB_SSL` | Persistencia del backend Finanzas. |
| Backend Util / Catálogos | `UTIL_API_URL`, `CATALOGS_API_URL_BFF` | Catálogos, validaciones auxiliares y servicios compartidos. |
| Validador origen transacción | `CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN` | Validación por módulo y pantalla. |
| Backend Fiscal | `FISCAL_API_URL` | Integración con servicios fiscales. |
| Google Cloud Storage | `GCS_BUCKET`, `GCS_PREFIX` | Almacenamiento/descarga de archivos bajo prefijo de Finanzas. |
| Keycloak / Portal Auth | `KEYCLOAK_GROUP_VENDOR`, `KEYCLOAK_GROUP_ADMIN` | Validación de grupos/perfiles. |

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

Comandos de seeds:

```bash
npm run seed
npm run seed:reset
```

**Datos de conexión compartidos:**

| Campo | Valor |
|-------|-------|
| Motor | PostgreSQL |
| Host | `10.100.64.102` |
| Puerto | `5432` |
| Base | `b2b_portal` |
| Usuario | `wwwb2bportal` |
| SSL | `true` |
| Password | Gestionar como secreto |

**Notas:**

- No subir credenciales a GitHub.
- Confirmar si el acceso a `10.100.64.102` requiere VPN, Cloud SQL Proxy, red corporativa o runner específico.
- Confirmar si las migraciones se ejecutan manualmente o como parte del pipeline.
- Confirmar si los seeds aplican para DEV/UAT o solo local.

---

## 8. Seguridad y permisos

El backend tiene banderas de seguridad:

```env
SECURITY_ENABLED=false
FINANZAS_JWT_ENABLED=false
```

También configura grupos Keycloak:

```env
KEYCLOAK_GROUP_VENDOR=ppsomx-vendor
KEYCLOAK_GROUP_ADMIN=ppsomx-admin
```

**Interpretación actual:**

- `SECURITY_ENABLED` controla seguridad general o middleware de protección global.
- `FINANZAS_JWT_ENABLED` controla validación JWT específica del backend Finanzas.
- Los grupos Keycloak separan permisos de proveedor y administrador.

**Pendiente importante:**

Confirmar cómo deben quedar estas banderas por ambiente:

| Ambiente | `SECURITY_ENABLED` | `FINANZAS_JWT_ENABLED` | Comentario |
|----------|--------------------|-------------------------|------------|
| Local | `false` permitido | `false` permitido | Útil para desarrollo local. |
| DEV | Pendiente | Pendiente | Confirmar con arquitectura/seguridad. |
| UAT | Pendiente | Pendiente | No debería quedar deshabilitado sin aprobación. |
| PRD | `true` esperado | `true` esperado | Confirmar política real. |

---

## 9. CI/CD

El repositorio usa GitHub Actions. En la configuración visible se identifica un workflow tipo:

```yaml
name: Pipeline CI Microservice
```

Ramas configuradas:

```yaml
on:
  push:
    branches:
      - develop
      - feature/**
      - uat
      - release/**
      - main
      - master
      - hotfix/**
    tags:
      - "v*.*.*"
  workflow_dispatch:
```

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
- El build ejecuta `npm run openapi:gen`, por lo que fallas en generación OpenAPI pueden romper pipeline.
- Validar si Puppeteer requiere dependencias adicionales en la imagen Docker/base runner.
- Confirmar variables reales inyectadas por ambiente desde GitHub Secrets/Variables.

---

## 10. Storage / Google Cloud Storage

El servicio utiliza Google Cloud Storage mediante:

```env
GCS_BUCKET=fs-sod-mx-mrch-aclaraciones-uat
GCS_PREFIX=bk-finanzas-uat/finanzas-somx/
```

**Uso esperado:** almacenamiento, consulta, descarga o gestión de archivos asociados al dominio Finanzas.

**Pendientes:**

- Confirmar si el bucket `fs-sod-mx-mrch-aclaraciones-uat` es correcto para Finanzas.
- Confirmar permisos IAM del service account usado por el runtime.
- Confirmar si `GCS_PREFIX` debe separarse por ambiente, país o módulo.
- Confirmar política de retención / limpieza de archivos.

---

## 11. Swagger / OpenAPI

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

---

## 12. Estado actual

**Estado:** Pendiente de confirmar con ejecución real de pipeline y pruebas.  
**Cobertura de tests:** Pendiente de confirmar con `npm run test:cov`.  
**Base de datos:** PostgreSQL `b2b_portal`.  
**Puerto compartido:** `8082`.  

**Bugs / riesgos conocidos:**

| Riesgo | Severidad | Contexto |
|--------|-----------|----------|
| `DB_PASS` compartida fuera de gestor de secretos | Alta | Debe mantenerse solo en Secret Manager / GitHub Secrets. |
| `SECURITY_ENABLED=false` en ambiente no local | Alta | Confirmar si UAT requiere seguridad activa. |
| `FINANZAS_JWT_ENABLED=false` en ambiente no local | Alta | Confirmar si UAT requiere validación JWT. |
| `FISCAL_API_URL` parece placeholder | Alta | `https://fiscal-api.tu-dominio` debe reemplazarse por URL real. |
| `CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN` parece placeholder | Media / Alta | Debe confirmarse endpoint real. |
| Bucket GCS con nombre `aclaraciones` | Media | Confirmar si es intencional o deuda histórica. |
| `NODE_ENV=development` en variables compartidas | Media | Confirmar si para UAT debería ser `production`. |
| Puppeteer en backend | Media | Puede requerir configuración especial en Docker/Linux. |
| OpenAPI generado durante build | Media | Si falla documentación, falla build. |

**Deuda técnica:**

- Agregar `.env.example` sin secretos.
- Documentar rutas exactas generadas por cada controlador.
- Confirmar Swagger público por ambiente.
- Confirmar responsables de backend Util, Fiscal y Auth.
- Confirmar política de seguridad/JWT por ambiente.
- Confirmar si migraciones/seeds se ejecutan manualmente o vía pipeline.
- Confirmar bucket/prefix definitivo para GCS Finanzas.
- Homologar variables DEV/UAT/PRD.
- Revisar cobertura actual y quality gate.

---

## 13. Lo que no es obvio

- El proyecto compila como ESM (`type: module`), por lo que imports/exports y ejecución con Node deben respetar este modo.
- El build no solo compila TypeScript: también ejecuta `tsc-alias` y genera OpenAPI.
- `npm run dev` usa `nodemon` + `tsx`, no `ts-node` directamente.
- La base de datos usa SSL (`DB_SSL=true`).
- Hay dos banderas relacionadas con seguridad: `SECURITY_ENABLED` y `FINANZAS_JWT_ENABLED`; no asumir que una reemplaza a la otra sin revisar middleware.
- Existen dos variables para Util/Catálogos: `CATALOGS_API_URL_BFF` y `UTIL_API_URL`; confirmar si ambas siguen activas o si una quedó por compatibilidad.
- `FISCAL_API_URL` y `CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN` quedaron con valores placeholder en la información compartida.
- El bucket GCS contiene `aclaraciones` en el nombre, aunque el prefijo apunta a Finanzas.
- El backend parece tener controladores tanto de operación financiera como de healthcheck, storage y auditoría.
- Si `MAX_HEADER_SIZE=65536` es requerido por tokens o headers grandes, debe confirmarse que también esté soportado por proxy/ingress/load balancer.

---

## 14. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Crear `.env.example` sin secretos y con comentarios por variable. | Alta |
| Mover/validar `DB_PASS` en Secret Manager o GitHub Secrets. | Alta |
| Confirmar URL real de `FISCAL_API_URL`. | Alta |
| Confirmar path real de `CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN`. | Alta |
| Confirmar si `SECURITY_ENABLED` y `FINANZAS_JWT_ENABLED` deben estar activos en UAT/PRD. | Alta |
| Confirmar si `NODE_ENV=development` es correcto para UAT. | Alta |
| Confirmar bucket GCS definitivo para Finanzas. | Alta |
| Ejecutar `npm run build` y `npm run test:cov` para validar estado del proyecto. | Alta |
| Documentar rutas exactas por controlador desde OpenAPI generado. | Media |
| Agregar link de Swagger por ambiente. | Media |
| Confirmar flujo de migraciones y seeds. | Media |
| Revisar dependencias requeridas por Puppeteer en Docker. | Media |
| Confirmar cobertura mínima exigida por Sonar/Quality Gate. | Media |

---

## 15. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | QA | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

## 16. Checklist de traspaso

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
| URL real de Fiscal confirmada | Pendiente |
| Endpoint real de validación de origen confirmado | Pendiente |
| Seguridad por ambiente confirmada | Pendiente |
| Bucket GCS validado | Pendiente |
| Cobertura actual validada | Pendiente |

---

*Completado por:* Oscar Bonelli, basado en código y configuración compartida · *Fecha:* 2026-06-24 · *Aceptado por:* Pendiente
