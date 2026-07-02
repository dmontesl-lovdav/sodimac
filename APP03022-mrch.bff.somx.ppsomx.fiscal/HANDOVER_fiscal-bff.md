# HANDOVER.md — `APP03022-mrch.bff.somx.ppsomx.fiscal`

> Documento de traspaso basado en el `package.json`, variables de entorno, estructura visible del repositorio y el handover de referencia del BFF de Finanzas.

**Proyecto:** APP03022-mrch.bff.somx.ppsomx.fiscal  
**Tipo:** BFF / Gateway proxy / Expositor de contrato OpenAPI para Google Cloud Endpoints  
**Repositorio:** https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.fiscal  
**Fecha:** 2026-06-29  
**Entrega:** Oscar Bonelli — g_dti26@sodimac.com.mx  
**Recepción:** Pendiente de confirmar  

---

## 1. ¿Qué hace este proyecto?

Este proyecto es un **BFF / reverse proxy intermedio para Fiscal** dentro del ecosistema **Falabella Business Center / PPSOMX**.

Su función principal es servir como capa de exposición entre el consumidor externo / gateway y el backend real **`APP03022-mrch.backend.somx.fiscal-api`**, que corre en Java Spring Boot. El BFF no contiene lógica fiscal principal ni persistencia propia; actúa como proxy hacia el backend Fiscal y como punto de publicación de contrato para infraestructura GCP / Google Cloud Endpoints, según la estructura visible del repositorio.

El backend destino es el servicio interno:

```env
REMOTE_URL=http://mrch-backend-somx-fiscal-api:8082
```

**Uso principal:**

- Exponer el acceso público/gateway hacia `fiscal-api`.
- Publicar o versionar contratos OpenAPI relacionados con Fiscal.
- Proveer healthcheck del BFF.
- Aceptar headers grandes mediante `--max-http-header-size=1048576`, útil para tokens o contexto de gateway.
- Mantener una capa simple de proxy sin duplicar lógica del backend Java.

**Consumidores / usuarios:**

- Google Cloud Endpoints / gateway.
- Micro frontend `APP03022-mrch.frontend.somx.fiscal.spa`.
- Portal Falabella Business Center.
- Clientes internos que consuman las rutas públicas de Fiscal.

**Criticidad:** Alta. Aunque este BFF no implementa negocio fiscal, forma parte del camino de exposición hacia APIs fiscales usadas por el portal.

**Documentación adicional:**

| Recurso | Link / Estado |
|---------|---------------|
| Repositorio | [APP03022-mrch.bff.somx.ppsomx.fiscal](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.fiscal) |
| Backend destino | [APP03022-mrch.backend.somx.fiscal-api](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.backend.somx.fiscal-api) |
| Micro frontend consumidor | [APP03022-mrch.frontend.somx.fiscal.spa](https://github.com/falabella-stores-and-merchandise/APP03022-mrch.frontend.somx.fiscal.spa) |
| Confluence / documentación técnica | [Documentación Fiscal Dev](https://falabella.atlassian.net/wiki/spaces/SMI/pages/897680752/Documentacion+Fiscal+Dev) |
| Swagger / OpenAPI | Archivos visibles: `api.yml`, `openapi.yml`, `openapi.json`, `cloud-endpoint/openapi-bundled.yaml`; confirmar ruta pública final |
| Postman collection | No disponible |
| Tablero Jira / backlog | No disponible |
| README del repositorio | [README.md](README.md) |

---

## 2. Cómo levantar el proyecto

**Requisitos:**

- Node.js.
- npm.
- Acceso al repositorio.
- Variables de entorno del ambiente.
- Acceso al backend Fiscal destino, local o por red interna.

```bash
git clone https://github.com/falabella-stores-and-merchandise/APP03022-mrch.bff.somx.ppsomx.fiscal
cd APP03022-mrch.bff.somx.ppsomx.fiscal

# usar como base la rama develop, si aplica
git checkout develop
git pull origin develop

npm install
npm start
```

**Scripts disponibles:**

```bash
npm start
npm test
```

| Script | Descripción |
|--------|-------------|
| `npm start` | Levanta el servidor Express con `node --max-http-header-size=1048576 ./src/App.js`. |
| `npm test` | No ejecuta pruebas reales; genera un `coverage/lcov.info` dummy para que el pipeline tenga archivo de cobertura. |

**Nota importante sobre OpenAPI:**

El `package.json` compartido **no contiene script `openapi:bundle`**, aunque el repositorio sí muestra archivos relacionados con contrato OpenAPI / Cloud Endpoints (`api.yml`, `openapi.yml`, `openapi.json`, `cloud-endpoint/openapi-bundled.yaml`). Confirmar si el bundle se genera manualmente, desde pipeline, o si el archivo versionado se usa directamente.

**Notas de arranque local:**

- El entrypoint es `src/App.js`.
- El proyecto usa ES Modules (`"type": "module"`).
- La configuración se carga desde `.env` mediante `dotenv`.
- El puerto local se toma desde `LOCAL_PORT`.
- El backend destino se toma desde `REMOTE_URL`.
- El contexto local se toma desde `LOCAL_CONTEXT`.
- El healthcheck se expone en `HEALTH_PATH`.
- El servicio está diseñado para correr en GCP con parsers limitados y headers grandes.

**Ejemplo local mínimo:**

```env
REMOTE_URL=http://localhost:8082
LOCAL_PORT=8080
LOCAL_CONTEXT=/
HEALTH_PATH=/health
AUTH_PUBLIC_KEY=
```

Con esa configuración local:

```bash
npm install
npm start
```

Luego validar:

```bash
curl http://localhost:8080/health
```

---

## 3. Variables de entorno

### Variables compartidas para Fiscal BFF

```env
REMOTE_URL=http://mrch-backend-somx-fiscal-api:8082
LOCAL_PORT=8080
LOCAL_CONTEXT=/
HEALTH_PATH=/health
AUTH_PUBLIC_KEY=
```

### Tabla de variables

| Variable | Descripción | Valor compartido | ¿Secreta? |
|----------|-------------|------------------|:---------:|
| `REMOTE_URL` | URL base del backend real `fiscal-api` al que se proxyan las peticiones. | `http://mrch-backend-somx-fiscal-api:8082` | No / interna |
| `LOCAL_PORT` | Puerto donde escucha el BFF. | `8080` | No |
| `LOCAL_CONTEXT` | Contexto base sobre el que se monta el proxy. | `/` | No |
| `HEALTH_PATH` | Ruta local de healthcheck del BFF. | `/health` | No |
| `AUTH_PUBLIC_KEY` | Llave/certificado público para validación de JWT o contexto de autenticación, si aplica. | Vacío en la configuración compartida | Sí, si se usa |

**Importante:** no dejar `AUTH_PUBLIC_KEY` real hardcodeado en el repositorio. Si se requiere en UAT/PRD, debe manejarse mediante Secret Manager, GitHub Secrets, Kustomize Secret o el mecanismo oficial de secretos.

### Variables pendientes de confirmar

| Variable | Motivo |
|----------|--------|
| `AUTH_PUBLIC_KEY` | Se compartió vacío. Confirmar si la validación JWT se delega a Google Endpoints o si el BFF debe validar token. |
| `LOCAL_CONTEXT` | Está como `/`. Confirmar si el gateway público entra por `/ppsomx/fiscal` y si Kubernetes/Endpoints strippea el prefijo antes de llegar al BFF. |
| `REMOTE_URL` | Confirmar que el service name `mrch-backend-somx-fiscal-api` sea correcto para todos los namespaces/ambientes. |
| Ruta pública OpenAPI | Confirmar si se sirve desde `/openapi.yml`, `/openapi.json`, `/api.yml`, `/cloud-endpoint/openapi-bundled.yaml` o path del gateway. |

---

## 4. Arquitectura y dependencias

**Tipo:** Node.js + Express + proxy HTTP + contrato OpenAPI para GCP.

```text
Cliente / Portal / Google Endpoints
        │
        ▼
Fiscal BFF
        │
        ├── /health              → Health local del BFF
        ├── OpenAPI / api.yml    → Contrato usado por gateway, pendiente de confirmar ruta pública
        │
        └── Proxy
              request público
                    │
                    ▼
              REMOTE_URL
                    │
                    ▼
              fiscal-api Java Spring Boot
```

### Stack

| Capa | Tecnología |
|------|------------|
| Runtime | Node.js |
| Servidor HTTP | Express 4 |
| Proxy | `express-http-proxy` |
| Configuración | `dotenv` |
| Logs | `pino` |
| Auth / JWT | `jsonwebtoken`, `AUTH_PUBLIC_KEY` opcional |
| OpenAPI bundle / validación | `@apidevtools/swagger-cli` como dependencia dev |
| Persistencia | No aplica |
| Tests | Dummy coverage, sin pruebas reales configuradas |

### `package.json`

```json
{
  "name": "reverse-proxy-bff",
  "version": "1.0.0",
  "description": "Express proxy (BFF) with healthcheck and size-limited parsers for GCP",
  "license": "MIT",
  "type": "module",
  "main": "src/App.js",
  "scripts": {
    "start": "node --max-http-header-size=1048576 ./src/App.js",
    "test": "node -e \"const fs=require('fs'); fs.mkdirSync('coverage',{recursive:true}); fs.writeFileSync('coverage/lcov.info','TN:\\nSF:src/App.js\\nDA:1,0\\nLF:1\\nLH:0\\nend_of_record\\n'); console.log('No tests configured - dummy coverage generated');\""
  },
  "dependencies": {
    "dotenv": "16.4.5",
    "express": "4.19.2",
    "express-http-proxy": "1.6.3",
    "jsonwebtoken": "9.0.2",
    "pino": "9.7.0"
  },
  "devDependencies": {
    "nodemon": "3.0.2",
    "pino-pretty": "11.2.2",
    "@apidevtools/swagger-cli": "4.0.4"
  }
}
```

---

## 5. Cloud Endpoints / OpenAPI

En la estructura visible del repositorio se identifican archivos de especificación:

```text
api.yml
openapi.yml
openapi.json
cloud-endpoint/openapi-bundled.yaml
```

**Pendiente de confirmar:**

- Cuál de estos archivos es el contrato final consumido por Google Cloud Endpoints.
- Si `cloud-endpoint/openapi-bundled.yaml` se genera manualmente o por pipeline.
- Si `openapi.yml` / `openapi.json` son fuentes, salidas generadas o respaldos.
- Ruta pública final del contrato en UAT/PRD.
- Si el BFF sirve directamente alguno de estos archivos o si solo se usan como artefactos de despliegue.

**Recomendación operativa:**

Antes de desplegar cambios de rutas/contrato:

```bash
# Confirmar comando real usado por el proyecto.
# El package actual no trae script openapi:bundle.
npx swagger-cli validate openapi.yml
npx swagger-cli validate cloud-endpoint/openapi-bundled.yaml
```

Si se decide homologar con el BFF de Finanzas, se puede agregar un script similar a:

```json
{
  "scripts": {
    "openapi:bundle": "npx swagger-cli bundle cloud-endpoint/src/root.yaml --outfile cloud-endpoint/openapi.yaml --type yaml"
  }
}
```

Solo agregarlo si la estructura real del repo tiene `cloud-endpoint/src/root.yaml`; en Fiscal, según la estructura visible compartida, el archivo observado es `cloud-endpoint/openapi-bundled.yaml`.

---

## 6. Rutas / dominios publicados

El BFF no implementa lógica de negocio por ruta; delega hacia `fiscal-api`.

El backend Fiscal Java contiene controladores para dominios como:

| Dominio / controlador backend | Uso esperado |
|-------------------------------|--------------|
| `InvoiceController` | Consulta/operación de facturas. |
| `PaymentController` | Pagos / complementos de pago. |
| `PaymentPdfController` | Generación o descarga de PDFs de pago. |
| `PaymentRegistrationController` | Registro de pagos. |
| `AddendumController` | Addendas. |
| `IssuerController` | Emisores. |
| `ReceiverController` | Receptores. |
| `RelatedCfdiController` | CFDI relacionados. |
| `RelatedDocumentsController` | Documentos relacionados. |
| `FiscalXmlProcessorController` | Procesamiento de XML fiscal. |
| `XmlInvoiceController` | Factura XML. |
| `PdfController` | Generación/consulta de PDF. |
| `TotalsController` | Totales / agregados. |
| `FiscalValidationController` | Validaciones fiscales. |
| `HealthController` | Health del backend Fiscal. |
| `LogController` | Logs / trazabilidad. |
| `VersionCatalogController` | Catálogo de versiones. |
| `AuthorizedReceiverCatalogController` | Catálogo de receptores autorizados. |
| `PacCatalogController` | Catálogo PAC. |
| `EquivalenceDrController` | Equivalencias. |

**Nota:** validar rutas exactas contra `api.yml`, `openapi.yml` o el Swagger generado por `fiscal-api` (`/swagger-ui` y `/api-docs` del backend Java).

### Ejemplo conceptual de proxy

```text
Request público:
GET /invoice/...

Destino backend:
GET ${REMOTE_URL}/invoice/...
```

Si el BFF agrega o remueve prefijos como `/api`, `/ppsomx/fiscal` o `/`, debe confirmarse directamente en `src/App.js` y en el gateway. Con la configuración compartida `LOCAL_CONTEXT=/`, no se debe asumir strip de prefijo sin validar.

---

## 7. Seguridad y headers

El proyecto incluye `jsonwebtoken` y `AUTH_PUBLIC_KEY`, por lo que puede existir lógica de lectura/validación de JWT en `src/App.js`. La variable compartida está vacía:

```env
AUTH_PUBLIC_KEY=
```

**Interpretación actual:**

- La validación de seguridad podría estar delegada a Google Cloud Endpoints / gateway.
- El BFF podría actuar solamente como proxy si `AUTH_PUBLIC_KEY` está vacío.
- Si se configura `AUTH_PUBLIC_KEY`, debe revisarse si el BFF valida JWT, decodifica claims o inyecta headers hacia `fiscal-api`.

**Pendientes importantes:**

| Punto | Acción |
|-------|--------|
| Validación JWT | Confirmar si la hace Google Endpoints, el BFF o `fiscal-api`. |
| `AUTH_PUBLIC_KEY` | Confirmar si debe existir en UAT/PRD. |
| Headers hacia backend | Confirmar si el BFF pasa `Authorization` intacto o si agrega headers adicionales. |
| Bloqueo por token inválido | Confirmar si el BFF bloquea o solo reenvía. |
| Tamaño de headers | El start usa `--max-http-header-size=1048576`; validar que ingress/proxy también permita headers grandes. |

---

## 8. Pipeline / CI-CD

**Estructura visible:** `.github/workflows/pipeline.yml`, `Dockerfile`, `.gitlab-ci.yml`.

El pipeline debe validar como mínimo:

```bash
npm install
npm test
npm start
```

**Consideraciones:**

- El proyecto trae `package-lock.json`; se recomienda confirmar si el pipeline usa `npm ci` para builds reproducibles.
- El script `npm test` genera cobertura dummy y no valida lógica real.
- Si el pipeline necesita publicar OpenAPI, confirmar si usa `api.yml`, `openapi.yml`, `openapi.json` o `cloud-endpoint/openapi-bundled.yaml`.
- Si se requiere validar el contrato, agregar paso explícito con `swagger-cli validate`.
- Confirmar si el deployment usa GitHub Actions, GitLab CI o ambos. La estructura muestra ambos archivos.
- Confirmar variables inyectadas por ambiente desde Secrets/Variables/Kustomize.

**Comandos esperados:**

```bash
npm ci
npm test
npm start
```

Si el pipeline mantiene `npm install`, confirmar que sea decisión de la plantilla corporativa.

---

## 9. Estado actual

**Estado:** Funcional como BFF/proxy simple, pendiente de validar contra infraestructura real y contrato OpenAPI final.

| Área | Estado |
|------|--------|
| Servidor Express | Implementado |
| Proxy hacia `fiscal-api` | Implementado / pendiente validar path exacto |
| Healthcheck local | Implementado por `HEALTH_PATH` |
| OpenAPI / Cloud Endpoints | Archivos presentes, ruta/flujo de generación pendiente de confirmar |
| Seguridad JWT | Pendiente de confirmar por `AUTH_PUBLIC_KEY` vacío |
| Tests reales | No implementados |
| Cobertura | Dummy coverage |
| Persistencia propia | No aplica |

**Bugs / riesgos conocidos:**

| Riesgo | Severidad | Contexto |
|--------|-----------|----------|
| `npm test` genera cobertura dummy | Media / Alta | No valida health, proxy ni contrato OpenAPI. |
| `AUTH_PUBLIC_KEY` vacío | Media / Alta | Confirmar si es esperado porque la validación se hace en Google Endpoints o backend. |
| `REMOTE_URL` interno depende de DNS/namespace | Alta | Si el service name no resuelve en el runtime, todo el BFF falla. |
| `LOCAL_CONTEXT=/` | Media / Alta | Confirmar cómo se maneja el prefijo público `/ppsomx/fiscal`. |
| OpenAPI con múltiples archivos posibles | Media | No está claro cuál es la fuente de verdad: `api.yml`, `openapi.yml`, `openapi.json` o `openapi-bundled.yaml`. |
| Sin script `openapi:bundle` | Media | Si el bundle debe regenerarse, actualmente no está documentado en scripts npm. |
| Header size alto solo en Node | Media | El ingress/gateway también debe soportar headers grandes para que sirva de algo. |

---

## 10. Lo que no es obvio

- Este repositorio **no es el backend Fiscal real**; es una capa intermedia hacia `APP03022-mrch.backend.somx.fiscal-api`.
- El backend real corre en Java Spring Boot en el puerto `8082`.
- El BFF corre en Node/Express y escucha en `LOCAL_PORT=8080`.
- `REMOTE_URL` apunta a un service name interno: `mrch-backend-somx-fiscal-api`.
- El `package.json` no trae script de bundle OpenAPI, aunque el repositorio tiene archivos OpenAPI.
- El start aumenta `max-http-header-size` a `1048576`, probablemente por tokens grandes o headers del gateway.
- `AUTH_PUBLIC_KEY` está vacío en la configuración compartida; no asumir que seguridad está desactivada sin revisar `src/App.js` y Google Endpoints.
- `LOCAL_CONTEXT=/` puede significar que el prefijo público se resuelve antes de llegar al contenedor.
- El BFF no debe duplicar reglas fiscales; cualquier cambio de negocio debe ir en `fiscal-api`.

---

## 11. Próximos pasos

| Tarea | Prioridad |
|-------|-----------|
| Confirmar en `src/App.js` si el proxy agrega, elimina o conserva prefijos de ruta. | Alta |
| Confirmar ruta pública real del BFF en UAT: probablemente bajo `/ppsomx/fiscal`. | Alta |
| Confirmar si `AUTH_PUBLIC_KEY` debe configurarse en UAT/PRD o si la seguridad se delega al gateway. | Alta |
| Confirmar cuál OpenAPI es la fuente de verdad: `api.yml`, `openapi.yml`, `openapi.json` o `cloud-endpoint/openapi-bundled.yaml`. | Alta |
| Agregar o documentar script de validación/bundle OpenAPI si aplica. | Alta |
| Validar DNS interno de `mrch-backend-somx-fiscal-api:8082` desde el runtime. | Alta |
| Agregar `.env.example` actualizado sin secretos. | Alta |
| Reemplazar cobertura dummy por pruebas mínimas de health y proxy. | Media |
| Agregar validación de OpenAPI en pipeline con `swagger-cli validate`. | Media |
| Confirmar si el repo usa GitHub Actions, GitLab CI o ambos. | Media |
| Confirmar owner técnico del backend Fiscal y responsable de gateway/Cloud Endpoints. | Media |

---

## 12. Equipo anterior

| Nombre | Rol | Email | Disponible hasta |
|--------|-----|-------|:----------------:|
| Oscar Bonelli | Responsable de entrega / Tech Lead | g_dti26@sodimac.com.mx | Activo |
| Oscar Bonelli | Dev senior | g_dti26@sodimac.com.mx | Activo |
| Ivan Saul Cortez | PO / PM | iscortesz@sodimac.com.mx | Activo |

**Período de soporte acordado:** Pendiente de completar.

---

## 13. Checklist de traspaso

| Ítem | Estado |
|------|--------|
| Repositorio confirmado | Completado |
| `package.json` revisado | Completado |
| Variables principales identificadas | Completado |
| Backend destino identificado | Completado |
| Confluence vinculada | Completado |
| Scripts documentados | Completado |
| Secretos evitados en el handover | Completado |
| Ruta exacta pública del BFF | Pendiente |
| Fuente de verdad OpenAPI confirmada | Pendiente |
| Flujo de generación/bundle OpenAPI confirmado | Pendiente |
| Seguridad/JWT por ambiente confirmada | Pendiente |
| Tests reales implementados | Pendiente |
| Pipeline validado | Pendiente |

---

*Completado por:* Oscar Bonelli, basado en código, package, variables y estructura compartida · *Fecha:* 2026-06-29 · *Aceptado por:* Pendiente
