# Verificacion de Endpoints: Todos los Proyectos Backend vs BFFs

> **Fecha:** 2025-12-10
> **Proposito:** Verificar consistencia entre backends y BFFs
> **IMPORTANTE:** Este documento incluye el BFF principal (ppsomx) que expone multiples servicios

---

## Inventario de Proyectos

| Proyecto | Backend | BFF | Tecnologia Backend | api.yml |
|----------|---------|-----|-------------------|---------|
| catalogos | mrch.backend.somx.catalogos-api | mrch.bff.somx.ppsomx.catalogos | Spring Boot (Java) | SI |
| utils | mrch.backend.somx.utils-api | mrch.bff.somx.ppsomx.utils | Node.js (TypeScript) | NO |
| finanzas | mrch.backend.somx.finanzas-api | **mrch.bff.somx.ppsomx.finanzas** | Node.js (TypeScript) | SI |
| fiscal | mrch.backend.somx.fiscal-api | mrch.bff.somx.ppsomx.fiscal | Spring Boot (Java) | SI |

### Ubicaciones de BFFs

| BFF | Ubicacion | api.yml | Descripcion |
|-----|-----------|---------|-------------|
| catalogos | `backend/mrch.bff.somx.ppsomx.catalogos/` | `api.yml` | BFF exclusivo para catalogos |
| utils | `backend/mrch.bff.somx.ppsomx.utils/` | **NO TIENE** | BFF proxy simple |
| fiscal | `backend/mrch.bff.somx.ppsomx.fiscal/` | `api.yml` | BFF exclusivo para fiscal |
| **finanzas** | `backend/mrch.bff.somx.ppsomx.finanzas/` | `cloud-endpoint/openapi.yaml` | BFF exclusivo para finanzas |
| ppsomx | `configuration/mrch.bff.somx.ppsomx/` | `cloud-endpoint/openapi.yaml` | BFF compartido - Otros servicios |

---

## 1. CATALOGOS-API vs BFF CATALOGOS

### Arquitectura

| Componente | Tecnologia | Ubicacion |
|------------|------------|-----------|
| **Backend** | Spring Boot (Java) | `mrch.backend.somx.catalogos-api` |
| **BFF** | Node.js + api.yml | `mrch.bff.somx.ppsomx.catalogos` |

### Comparativa de Endpoints

| catalogos-api | BFF api.yml | Estado | Metodo |
|---------------|-------------|--------|--------|
| `/` (GET all) | `/` | OK | GET |
| `/module/{module}` | `/module/{module}` | OK | GET |
| `/{code}` | `/{code}` | OK | GET |
| `/{code}/details` | `/{code}/details` | OK | GET |
| `/{code}/details/{key}` | `/{code}/details/{key}` | OK | GET |
| `/message/{key}` | `/message/{key}` | OK | GET |
| `/message/{key}/format` | `/message/{key}/format` | OK | GET |
| `/id/{id}` | `/id/{id}` | OK | GET |
| `/prefix/{prefix}` | `/prefix/{prefix}` | OK | GET |
| `/health` | `/health` | OK | GET |

### Resultado: CATALOGOS OK

Todos los endpoints de catalogos-api estan correctamente expuestos en el BFF.

---

## 2. UTILS-API vs BFF UTILS

### Arquitectura

| Componente | Tecnologia | Ubicacion |
|------------|------------|-----------|
| **Backend** | Node.js (TypeScript/Express) | `mrch.backend.somx.utils-api` |
| **BFF** | Node.js (Proxy simple) | `mrch.bff.somx.ppsomx.utils` |

### IMPORTANTE: BFF utils NO tiene api.yml

El BFF de utils es un **proxy simple sin API Gateway**:
- NO tiene archivo `api.yml`
- Solo tiene `App.js` que hace proxy a `REMOTE_URL`
- Context: `/api/utils`
- Remote: `http://localhost:3712`

Esto significa que **todos los endpoints del backend pasan directamente** sin validacion de API Gateway.

### Endpoints de utils-api

#### /parameters
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/parameters` | GET | Listar parametros |
| `/parameters/:id` | GET | Obtener por ID |
| `/parameters` | POST | Crear parametro |
| `/parameters/:id` | PATCH | Actualizar parametro |
| `/parameters/:id` | DELETE | Eliminar parametro |

#### /modules
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/modules` | GET | Listar modulos |
| `/modules/:id` | GET | Obtener por ID |
| `/modules` | POST | Crear modulo |
| `/modules/:id` | PATCH | Actualizar modulo |
| `/modules/:id` | DELETE | Eliminar modulo |

#### /messages
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/messages` | GET | Listar mensajes |
| `/messages/code/:code` | GET | Obtener por codigo |
| `/messages/:id` | GET | Obtener por ID |
| `/messages` | POST | Crear mensaje |
| `/messages/:id` | PATCH | Actualizar mensaje |
| `/messages/:id` | DELETE | Eliminar mensaje |

#### /application-messages
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/application-messages` | GET | Listar mensajes de app |
| `/application-messages/:id` | GET | Obtener por ID |
| `/application-messages` | POST | Crear mensaje |
| `/application-messages/:id` | DELETE | Eliminar mensaje |

#### /processes
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/processes` | GET | Listar procesos |
| `/processes/:id` | GET | Obtener por ID |
| `/processes` | POST | Crear proceso |
| `/processes/:id` | PATCH | Actualizar proceso |
| `/processes/:id` | DELETE | Eliminar proceso |

#### /item-types
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/item-types` | GET | Listar tipos de item |
| `/item-types/:id` | GET | Obtener por ID |
| `/item-types` | POST | Crear tipo |
| `/item-types/:id` | PATCH | Actualizar tipo |
| `/item-types/:id` | DELETE | Eliminar tipo |

#### /items
| Path | Metodo | Descripcion |
|------|--------|-------------|
| `/items` | GET | Listar items |
| `/items/:id` | GET | Obtener por ID |
| `/items` | POST | Crear item |
| `/items/:id` | PATCH | Actualizar item |
| `/items/:id` | DELETE | Eliminar item |

### Resultado: UTILS - SIN API GATEWAY

El BFF de utils NO valida endpoints porque no tiene api.yml.
Todos los endpoints del backend son accesibles directamente a traves del proxy.

---

## 3. FINANZAS-API vs BFF FINANZAS

### Arquitectura

| Componente | Tecnologia | Ubicacion |
|------------|------------|-----------|
| **Backend** | Node.js (TypeScript/Express) | `mrch.backend.somx.finanzas-api` |
| **BFF** | Node.js + openapi.yaml | `mrch.bff.somx.ppsomx.finanzas` |
| **OpenAPI** | `cloud-endpoint/openapi.yaml` | 2126 lineas |

### Comparativa Detallada de Endpoints

#### /accounts-payable (Cuentas por Pagar)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/accounts-payable` | `/accounts-payable` | OK | GET |
| `/accounts-payable` | `/accounts-payable` | OK | POST |
| `/accounts-payable/:uuid` | `/accounts-payable/{uuid}` | OK | GET |
| `/accounts-payable/:uuid` | `/accounts-payable/{uuid}` | OK | PATCH |
| `/accounts-payable/:uuid` | `/accounts-payable/{uuid}` | OK | DELETE |

#### /fiscal-payments (Pagos Fiscales)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/fiscal-payments` | `/fiscal-payments` | OK | GET |
| `/fiscal-payments` | `/fiscal-payments` | OK | POST |
| `/fiscal-payments/:uuid` | `/fiscal-payments/{uuid}` | OK | GET |
| `/fiscal-payments/:uuid` | `/fiscal-payments/{uuid}` | OK | PUT |
| `/fiscal-payments/:uuid` | `/fiscal-payments/{uuid}` | OK | DELETE |

#### /rebates (Descuentos)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/rebates` | `/rebates` | OK | GET |
| `/rebates` | `/rebates` | OK | POST |
| `/rebates/published` | `/rebates/published` | OK | GET |
| `/rebates/published/export/csv` | `/rebates/published/export/csv` | OK | GET |
| `/rebates/search` | `/rebates/search` | OK | GET |
| `/rebates/export/csv` | `/rebates/export/csv` | OK | GET |
| `/rebates/vendor/:vendorNumber` | `/rebates/vendor/{vendorNumber}` | OK | GET |
| `/rebates/:uuid` | `/rebates/{uuid}` | OK | GET |
| `/rebates/:uuid` | `/rebates/{uuid}` | OK | PUT |
| `/rebates/:uuid` | `/rebates/{uuid}` | OK | DELETE |
| `/rebates/relate` | - | **FALTA BFF** | POST |
| `/rebates/filter` | - | **FALTA BFF** | POST |

#### /stamped-rebates (Descuentos Timbrados)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/stamped-rebates` | `/stamped-rebates` | OK | GET |
| `/stamped-rebates` | `/stamped-rebates` | OK | POST |
| `/stamped-rebates/export/csv` | `/stamped-rebates/export/csv` | OK | GET |
| `/stamped-rebates/:uuid` | `/stamped-rebates/{uuid}` | OK | GET |
| `/stamped-rebates/:uuid` | `/stamped-rebates/{uuid}` | OK | PUT |
| `/stamped-rebates/:uuid` | `/stamped-rebates/{uuid}` | OK | DELETE |

#### /sap-documents (Documentos SAP)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/sap-documents` | `/sap-documents` | OK | GET |
| `/sap-documents` | `/sap-documents` | OK | POST |
| `/sap-documents/:uuid` | `/sap-documents/{uuid}` | OK | GET |
| `/sap-documents/:uuid` | `/sap-documents/{uuid}` | OK | PUT |
| `/sap-documents/:uuid` | `/sap-documents/{uuid}` | OK | DELETE |

#### /shipping-guide (Guias de Embarque)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/shipping-guide` | `/shipping-guides` | OK | GET |
| `/shipping-guide/csv` | `/shipping-guides/csv` | OK | GET |
| `/shipping-guide/:uuid` | `/shipping-guides/{uuid}` | OK | GET |
| `/shipping-guide/:uuid` | `/shipping-guides/{uuid}` | OK | PUT |
| `/shipping-guide/:uuid` | `/shipping-guides/{uuid}` | OK | DELETE |
| - | `/shipping-guides` | **SOLO BFF** | POST |

> **Nota:** El endpoint POST `/shipping-guide` esta comentado en el backend.

#### /vendor-blocks (Bloqueos de Proveedor)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/vendor-blocks` | `/vendor-blocks` | OK | GET |
| `/vendor-blocks` | `/vendor-blocks` | OK | POST |
| `/vendor-blocks/:uuid` | `/vendor-blocks/{uuid}` | OK | GET |
| `/vendor-blocks/:uuid` | `/vendor-blocks/{uuid}` | OK | PUT |
| `/vendor-blocks/:uuid` | `/vendor-blocks/{uuid}` | OK | DELETE |

#### /purchase-orders (Ordenes de Compra)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/purchase-orders` | `/purchase-orders` | OK | GET |
| `/purchase-orders` | - | **FALTA BFF** | POST |
| `/purchase-orders/:uuid` | `/purchase-orders/{uuid}` | OK | GET |
| `/purchase-orders/:uuid` | `/purchase-orders/{uuid}` | OK | PATCH |
| `/purchase-orders/listReception` | - | **FALTA BFF** | GET |
| `/purchase-orders/updateReception` | - | **FALTA BFF** | PATCH |

#### /finanzas-payment (Pagos Finanzas)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/finanzas-payment` | - | **FALTA BFF** | GET |
| `/finanzas-payment` | - | **FALTA BFF** | POST |
| `/finanzas-payment` | - | **FALTA BFF** | PATCH |

#### /carta-porte (Carta Porte)

| finanzas-api | BFF openapi.yaml | Estado | Metodo |
|--------------|------------------|--------|--------|
| `/carta-porte/guia-embarque` | - | **FALTA BFF** | POST |
| `/carta-porte/oc` | - | **FALTA BFF** | POST |
| `/carta-porte/all` | - | **FALTA BFF** | POST |

### Resumen Finanzas

| Modulo | Total Backend | En BFF | Faltantes |
|--------|---------------|--------|-----------|
| accounts-payable | 5 | 5 | 0 |
| fiscal-payments | 5 | 5 | 0 |
| rebates | 12 | 10 | 2 (relate, filter) |
| stamped-rebates | 6 | 6 | 0 |
| sap-documents | 5 | 5 | 0 |
| shipping-guide | 5 | 5 | 0 |
| vendor-blocks | 5 | 5 | 0 |
| purchase-orders | 6 | 4 | 2 (POST, listReception, updateReception) |
| finanzas-payment | 3 | 0 | 3 |
| carta-porte | 3 | 0 | 3 |
| **TOTAL** | **55** | **45** | **10** |

### Resultado: FINANZAS 82% EXPUESTO

- **45 de 55 endpoints** estan correctamente expuestos en el BFF
- **10 endpoints** del backend NO estan expuestos en el BFF:
  - `/rebates/relate` (POST) - STM-973 nuevo
  - `/rebates/filter` (POST) - STM-875 nuevo
  - `/purchase-orders` (POST)
  - `/purchase-orders/listReception` (GET)
  - `/purchase-orders/updateReception` (PATCH)
  - `/finanzas-payment` (GET, POST, PATCH) - 3 endpoints
  - `/carta-porte/*` (guia-embarque, oc, all) - 3 endpoints

---

## Resumen General

| Proyecto | Backend | BFF | api.yml | Estado |
|----------|---------|-----|---------|--------|
| **catalogos** | Spring Boot | Node.js | SI | OK - Todos expuestos |
| **utils** | Node.js | Node.js | NO | Sin validacion API Gateway (Proxy) |
| **finanzas** | Node.js | Node.js | SI | 82% - 45/55 endpoints expuestos |
| **fiscal** | Spring Boot | Node.js | SI | OK - Ver documento VERIFICACION_ENDPOINTS.md |

---

## Patrones de Arquitectura Identificados

### Patron 1: BFF Exclusivo con API Gateway (fiscal, catalogos, finanzas)
```
Usuario -> nginx -> API Gateway (api.yml/openapi.yaml) -> BFF exclusivo -> Backend
```
- Un BFF por cada backend
- Requiere definir endpoints en api.yml o openapi.yaml
- Error 405 si el endpoint no esta definido

### Patron 2: Proxy Simple sin API Gateway (utils)
```
Usuario -> nginx -> BFF (proxy) -> Backend
```
- NO tiene api.yml/openapi.yaml
- Todos los endpoints del backend son accesibles
- Sin validacion de API Gateway

---

## Acciones Recomendadas

### Para utils-api:
1. Crear `api.yml` para consistencia con otros BFFs
2. O mantener como proxy simple si no se requiere validacion

### Para finanzas-api:
Si se necesitan exponer los 10 endpoints faltantes:
1. Agregarlos a `backend/mrch.bff.somx.ppsomx.finanzas/cloud-endpoint/openapi.yaml`
2. Agregar las definiciones de Request/Response
3. Desplegar el BFF finanzas

---

**Fecha de verificacion:** 2025-12-10
