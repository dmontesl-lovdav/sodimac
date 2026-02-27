# STM-1229: Servicio de Parametros en BFF de Utilerias

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1229

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | Implementado y Probado |
| **Modulo** | BFF Utilerias |
| **BFF** | mrch.bff.somx.ppsomx.utils (Puerto 3800) |
| **Backend** | mrch.backend.somx.utils-api (Puerto 3712) |
| **Fecha** | 2025-12-22 |
| **Probado** | SI - 8 endpoints verificados |

---

## Descripcion

Implementar el servicio de parametros en el BFF de Utilerias para tener comunicacion con el Front.

> **Nota**: El backend fue implementado en el JIRA **STM-1213**. Ver documentacion en [STM-1213](../STM-1213/README.md).

---

## Endpoints Disponibles (8 endpoints)

| Metodo | Endpoint | Descripcion | Estado |
|--------|----------|-------------|--------|
| GET | `/api/parameters` | Listar parametros con filtros y paginacion | OK |
| GET | `/api/parameters/:id` | Obtener parametro por ID | OK |
| POST | `/api/parameters` | Crear nuevo parametro | OK |
| PATCH | `/api/parameters/:id` | Actualizar metadatos (NO value) | OK |
| DELETE | `/api/parameters/:id` | Eliminar parametro | OK |
| GET | `/api/parameters/:id/versions` | Historial de versiones | OK |
| POST | `/api/parameters/:id/versions` | Crear nueva version | OK |
| PATCH | `/api/parameters/:id/status` | Cambiar estatus | OK |

---

## Resultados de Pruebas (Backend - Puerto 3712)

### GET /api/parameters - Listar parametros
```bash
curl http://localhost:3712/api/parameters
```
**Resultado**: 21 parametros retornados exitosamente

### GET /api/parameters/:id - Obtener por ID
```bash
curl http://localhost:3712/api/parameters/1
```
**Resultado**:
```json
{
  "success": true,
  "data": {
    "idParameter": 1,
    "name": "MAX_RETRIES_TIMBRADO",
    "value": "3",
    "version": "1.00"
  }
}
```

### POST /api/parameters - Crear parametro
```bash
curl -X POST http://localhost:3712/api/parameters \
  -H "Content-Type: application/json" \
  -d '{"idModule":1,"idType":1,"name":"TEST_PARAM","description":"Parametro de prueba","value":"test_value"}'
```
**Resultado**:
```json
{
  "success": true,
  "data": {"idParameter": 43, "version": "1.00"},
  "message": "Parameter created successfully"
}
```

### PATCH /api/parameters/:id - Actualizar metadatos
```bash
curl -X PATCH http://localhost:3712/api/parameters/43 \
  -H "Content-Type: application/json" \
  -d '{"description":"Parametro de prueba actualizado"}'
```
**Resultado**:
```json
{
  "success": true,
  "message": "Parameter updated successfully"
}
```

### POST /api/parameters/:id/versions - Crear nueva version
```bash
curl -X POST http://localhost:3712/api/parameters/43/versions \
  -H "Content-Type: application/json" \
  -d '{"value":"new_test_value"}'
```
**Resultado**:
```json
{
  "success": true,
  "data": {"idParameter": 44, "version": "1.10"},
  "message": "Nueva version 1.10 creada exitosamente"
}
```

### GET /api/parameters/:id/versions - Historial de versiones
```bash
curl http://localhost:3712/api/parameters/44/versions
```
**Resultado**:
```json
{
  "success": true,
  "data": [
    {"idParameter": 44, "version": "1.10"},
    {"idParameter": 43, "version": "1.00"}
  ],
  "count": 2
}
```

### PATCH /api/parameters/:id/status - Cambiar estatus
```bash
curl -X PATCH http://localhost:3712/api/parameters/44/status \
  -H "Content-Type: application/json" \
  -d '{"status":0}'
```
**Resultado**:
```json
{
  "success": true,
  "message": "Estatus actualizado a Inactivo"
}
```

> **Nota**: Solo se puede cambiar el estatus de la ultima version del parametro.

### DELETE /api/parameters/:id - Eliminar parametro
```bash
curl -X DELETE http://localhost:3712/api/parameters/44
```
**Resultado**:
```json
{
  "success": true,
  "message": "Parameter with id 44 deleted successfully"
}
```

---

## Modelo de Datos

### ParameterDto (Respuesta)
```json
{
  "idParameter": 1,
  "idModule": 1,
  "idType": 1,
  "name": "MAX_RETRIES_TIMBRADO",
  "description": "Numero maximo de reintentos para timbrado",
  "value": "3",
  "version": "1.00",
  "startDate": "2025-12-15T19:23:40.443Z",
  "endDate": null,
  "status": 1,
  "createdBy": 1,
  "createdAt": "2025-12-15T19:23:40.443Z",
  "updatedBy": null,
  "updatedAt": null
}
```

### CreateParameterDto (Request POST)
```json
{
  "idModule": 1,
  "idType": 1,
  "name": "NOMBRE_PARAMETRO",
  "description": "Descripcion opcional",
  "value": "valor_requerido"
}
```

### CreateVersionDto (Request POST /versions)
```json
{
  "value": "nuevo_valor"
}
```

---

## Filtros Disponibles (GET /parameters)

| Parametro | Tipo | Descripcion |
|-----------|------|-------------|
| idModule | number | Filtrar por modulo |
| idType | number | Filtrar por tipo |
| name | string | Filtrar por nombre |
| status | number | 1=Activo, 0=Inactivo |
| page | number | Pagina (default: 1) |
| limit | number | Registros por pagina (default: 10) |
| sort | string | Campo de ordenamiento |
| order | string | ASC/DESC |

---

## Coleccion Postman

Archivo: `STM-1229 - Parameters BFF.postman_collection.json`

Variable: `base_url = http://localhost:3712/api`

---

## Script SQL

Archivo: `scripts/STM-1229_queries.sql`

---

## Tabla Involucrada

| Tabla | Esquema | Descripcion |
|-------|---------|-------------|
| `cat_parameter` | core_utils | Catalogo de parametros de configuracion |

---

## Documentacion Relacionada

| JIRA | Descripcion |
|------|-------------|
| [STM-1212](../STM-1212/README.md) | Modelo ER de CatParametro |
| [STM-1213](../STM-1213/README.md) | Implementacion backend de parametros |

---

## Archivos del Proyecto

**Backend (utils-api):**
- Controller: `src/controllers/parameter.controller.ts`
- Service: `src/services/parameter.service.ts`
- Repository: `src/repositories/parameter.repo.ts`
- Entity: `src/entities/CatParameter.entity.ts`
- Routes: `src/routes/parameter.routes.ts`

---

## Correcciones Realizadas

Se corrigieron errores de TypeScript relacionados con `exactOptionalPropertyTypes`:

### Archivos corregidos:
1. `src/controllers/applicationMsg.controller.ts`
2. `src/controllers/item.controller.ts`
3. `src/controllers/itemType.controller.ts`
4. `src/controllers/message.controller.ts`
5. `src/controllers/module.controller.ts`
6. `src/controllers/process.controller.ts`
7. `src/exceptions/UtilsException.ts`
8. `src/repositories/parameter.repo.ts` (subquery fix)

### Patron de correccion:
```typescript
// Antes (error con exactOptionalPropertyTypes)
const filters = {
    name: req.query.name ? req.query.name as string : undefined
};

// Despues (correcto)
const filters: { name?: string } = {};
if (req.query.name) filters.name = req.query.name as string;
```

---

## Como Ejecutar

```bash
cd backend/mrch.backend.somx.utils-api
npm run build
npm start
```

El servicio inicia en el puerto 3712.

---

## Notas Tecnicas

- Backend: utils-api (Puerto 3712)
- Sistema de versionado de parametros implementado
- PATCH no permite cambiar 'value' (usar POST /versions)
- Solo la ultima version puede cambiar de estatus
- Soft-delete con campo status (1=Activo, 0=Inactivo)
- Version inicial: 1.00, incrementos de 0.10
