# Exposicion de Endpoints a traves del BFF

> **PRIORIDAD: ALTA**
> Este documento es critico para cualquier tarea que involucre crear o exponer nuevos endpoints en el sistema fiscal.

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Diagrama de Arquitectura](#diagrama-de-arquitectura)
3. [Flujo de una Peticion](#flujo-de-una-peticion)
4. [Archivos Clave](#archivos-clave)
5. [Checklist para Nuevos Endpoints](#checklist-para-nuevos-endpoints)
6. [Errores Comunes](#errores-comunes)
7. [Ejemplos Practicos](#ejemplos-practicos)

---

## Resumen Ejecutivo

**Problema Comun:** Un endpoint implementado en `fiscal-api` retorna `405 Not Allowed` cuando se llama desde la URL publica.

**Causa Raiz:** El endpoint NO esta definido en el `api.yml` del BFF, que actua como contrato del API Gateway.

**Solucion:** Agregar la definicion del endpoint en `api.yml` del BFF y desplegar.

---

## Diagrama de Arquitectura

```
+-------------------+     +-------------------+     +-------------------+     +-------------------+
|                   |     |                   |     |                   |     |                   |
|  Cliente/Usuario  |---->|  nginx (Gateway)  |---->|   BFF (Node.js)   |---->|   fiscal-api      |
|                   |     |                   |     |                   |     |   (Spring Boot)   |
+-------------------+     +-------------------+     +-------------------+     +-------------------+
                                   |                        |                         |
                                   |                        |                         |
                                   v                        v                         v
                          +----------------+       +----------------+        +------------------+
                          | Valida que el  |       | Proxy simple   |        | Implementacion   |
                          | endpoint exista|       | (App.js)       |        | real del         |
                          | en api.yml     |       | Redirige todo  |        | endpoint         |
                          +----------------+       | a REMOTE_URL   |        +------------------+
                                                   +----------------+
```

### Flujo Detallado

```
Usuario hace request:
https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/search (POST)
                    |
                    v
            +---------------+
            |    nginx      |
            | (API Gateway) |
            +---------------+
                    |
                    | Verifica contra api.yml del BFF
                    | Si endpoint NO existe -> 405 Not Allowed
                    | Si endpoint SI existe -> Continua
                    |
                    v
            +---------------+
            |     BFF       |
            |   (Node.js)   |
            | Puerto: 3000  |
            +---------------+
                    |
                    | express-http-proxy
                    | Quita contexto local
                    | Redirige a REMOTE_URL
                    |
                    v
            +---------------+
            |  fiscal-api   |
            | (Spring Boot) |
            | Puerto: 8082  |
            +---------------+
                    |
                    v
            +---------------+
            |   Response    |
            +---------------+
```

---

## Flujo de una Peticion

### URL Publica vs URL Interna

| Componente | URL/Puerto |
|------------|------------|
| **URL Publica (DEV)** | `https://dev.fbusinesscenter.com/ppsomx/fiscal/` |
| **BFF (interno)** | `http://localhost:3000/` |
| **fiscal-api (interno)** | `http://localhost:8082/` |

### Transformacion de URLs

```
ENTRADA (publica):
https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/search

TRANSFORMACION en BFF:
- LOCAL_CONTEXT = /
- REMOTE_URL = http://localhost:8082
- Path resultante = /invoices/search

SALIDA (a fiscal-api):
http://localhost:8082/invoices/search
```

---

## Archivos Clave

### 1. BFF - api.yml (CONTRATO DEL API GATEWAY)

**Ubicacion:** `backend/mrch.bff.somx.ppsomx.fiscal/api.yml`

**Funcion:** Define TODOS los endpoints que el API Gateway permite. Si un endpoint NO esta aqui, retorna 405.

```yaml
# Ejemplo de endpoint existente
/payments:
  get:
    tags:
      - payment-controller
    summary: Obtener pagos paginados
    operationId: getAllPayments
    ...

# Ejemplo de endpoint agregado (STM-338)
/invoices/search:
  post:
    tags:
      - invoice-controller
    summary: Buscar facturas y notas de credito
    operationId: searchInvoices
    consumes:
      - application/json
    produces:
      - application/json
    parameters:
      - name: Authorization
        in: header
        required: true
        type: string
      - in: body
        name: body
        required: true
        schema:
          $ref: '#/definitions/InvoiceSearchRequest'
    responses:
      "200":
        description: Resultados paginados
        schema:
          $ref: '#/definitions/PageInvoiceSearchResponse'
    security:
      - bearerAuth: []
```

### 2. BFF - App.js (PROXY)

**Ubicacion:** `backend/mrch.bff.somx.ppsomx.fiscal/src/App.js`

**Funcion:** Proxy simple que redirige peticiones al fiscal-api. NO define rutas especificas.

```javascript
// Configuracion clave
const remoteUrl = process.env.REMOTE_URL || 'http://localhost:3001';
const localContext = process.env.LOCAL_CONTEXT || '/';

// Proxy que redirige todo
const remoteResolver = proxy(remoteUrl, {
    proxyReqPathResolver: (request) => {
        const targetPath = request.originalUrl.replace(localContext, "");
        return targetPath.startsWith("/") ? targetPath : "/" + targetPath;
    }
});
```

### 3. BFF - .env

**Ubicacion:** `backend/mrch.bff.somx.ppsomx.fiscal/.env`

```properties
REMOTE_URL=http://localhost:8082
LOCAL_PORT=3000
LOCAL_CONTEXT=/
```

### 4. fiscal-api - Controller

**Ubicacion:** `backend/mrch.backend.somx.fiscal-api/src/main/java/.../controller/`

**Funcion:** Implementacion real del endpoint en Spring Boot.

```java
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @PostMapping("/search")
    public ResponseEntity<Page<InvoiceSearchResponse>> searchInvoices(
            @Valid @RequestBody InvoiceSearchRequest request) {
        // Implementacion
    }
}
```

---

## Checklist para Nuevos Endpoints

### Paso 1: Implementar en fiscal-api

- [ ] Crear/modificar Controller con `@RequestMapping` y `@PostMapping/@GetMapping`
- [ ] Crear DTOs de Request/Response
- [ ] Implementar logica en Service
- [ ] Compilar y probar localmente

### Paso 2: Exponer en BFF (api.yml)

- [ ] Agregar path en seccion `paths:`
- [ ] Definir metodo HTTP (get/post/put/delete)
- [ ] Agregar parametros (headers, body, query)
- [ ] Definir responses (200, 400, 401, 403, 500)
- [ ] Agregar definiciones en seccion `definitions:` si hay nuevos DTOs
- [ ] Agregar security si requiere autenticacion

### Paso 3: Desplegar

- [ ] Commit cambios de fiscal-api
- [ ] Commit cambios de BFF (api.yml)
- [ ] Desplegar fiscal-api en ambiente
- [ ] Desplegar BFF en ambiente
- [ ] Probar con curl/Postman usando URL publica

### Paso 4: Documentar

- [ ] Actualizar coleccion Postman
- [ ] Documentar en carpeta `docs/jiras/STM-XXX/`

---

## Errores Comunes

### Error 405 Not Allowed

```
curl -X POST https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/search
<html>
<head><title>405 Not Allowed</title></head>
...
</html>
```

**Causa:** Endpoint NO definido en `api.yml` del BFF.

**Solucion:** Agregar endpoint al `api.yml` y desplegar BFF.

### Error 404 Not Found

**Causa:** Endpoint definido en BFF pero NO implementado en fiscal-api, o path incorrecto.

**Solucion:** Verificar que el path en `api.yml` coincida con `@RequestMapping` en Controller.

### Error 500 Internal Server Error

**Causa:** fiscal-api tiene error interno (NPE, DB, etc).

**Solucion:** Revisar logs de fiscal-api.

---

## Ejemplos Practicos

### Ejemplo 1: Endpoint GET Simple

**fiscal-api Controller:**
```java
@GetMapping("/payments")
public ResponseEntity<Page<PaymentDto>> getAllPayments(@RequestParam int page) {
    return ResponseEntity.ok(paymentService.findAll(page));
}
```

**BFF api.yml:**
```yaml
/payments:
  get:
    summary: Obtener pagos paginados
    operationId: getAllPayments
    parameters:
      - name: page
        in: query
        type: integer
        default: 0
    responses:
      "200":
        schema:
          $ref: '#/definitions/PagePaymentDto'
```

### Ejemplo 2: Endpoint POST con Body (STM-338)

**fiscal-api Controller:**
```java
@PostMapping("/search")
public ResponseEntity<Page<InvoiceSearchResponse>> searchInvoices(
        @Valid @RequestBody InvoiceSearchRequest request) {
    return ResponseEntity.ok(invoiceService.search(request));
}
```

**BFF api.yml:**
```yaml
/invoices/search:
  post:
    summary: Buscar facturas
    consumes:
      - application/json
    parameters:
      - in: body
        name: body
        schema:
          $ref: '#/definitions/InvoiceSearchRequest'
    responses:
      "200":
        schema:
          $ref: '#/definitions/PageInvoiceSearchResponse'
```

---

## Referencias Rapidas

### URLs por Ambiente

| Ambiente | URL Base |
|----------|----------|
| DEV | `https://dev.fbusinesscenter.com/ppsomx/fiscal/` |
| QA | `https://qa.fbusinesscenter.com/ppsomx/fiscal/` |
| PROD | `https://fbusinesscenter.com/ppsomx/fiscal/` |

### Proyectos Relacionados

| Proyecto | Ubicacion | Tecnologia |
|----------|-----------|------------|
| fiscal-api | `backend/mrch.backend.somx.fiscal-api` | Spring Boot (Java) |
| BFF | `backend/mrch.bff.somx.ppsomx.fiscal` | Node.js + Express |

### Comandos Utiles

```bash
# Probar endpoint publico
curl -X POST https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"rfcEmisor": "AAA010101AAA", ...}'

# Probar fiscal-api directo (solo local)
curl -X POST http://localhost:8082/invoices/search \
  -H "Content-Type: application/json" \
  -d '{"rfcEmisor": "AAA010101AAA", ...}'
```

---

**Fecha de creacion:** 2025-12-10
**Ultima actualizacion:** 2025-12-10
**Autor:** Sodimac Tech Team
**Ticket de referencia:** STM-338
