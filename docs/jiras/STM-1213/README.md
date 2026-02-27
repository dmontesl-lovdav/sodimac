# STM-1213: API para Configuracion de Parametros (Sistema de Versionado)

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :green_circle: Completado |
| **Modulo** | Utils API (mrch.backend.somx.utils-api) |
| **Fecha Inicio** | 2024-12-12 |
| **Fecha Fin** | 2024-12-12 |

---

## Descripcion

Implementacion de sistema de versionado para parametros de configuracion. Cada cambio en el valor (`value`) de un parametro genera automaticamente una nueva version, preservando el historial completo de cambios.

### Caracteristicas Principales

1. **Sistema de Versionado con Atomicidad**
   - Al cambiar el `value`, se cierra la version actual (status=0, endDate=hoy) y se crea nueva version
   - Operacion transaccional: ambas operaciones son atomicas
   - Esquema de versiones: 1.0 -> 1.1 -> ... -> 1.9 -> 2.0 -> 2.1 ...

2. **Endpoints Especificos**
   - PATCH `/parameters/:id` - Actualiza metadatos (NO permite cambiar `value`)
   - POST `/parameters/:id/versions` - Crea nueva version (cuando cambia `value`)
   - PATCH `/parameters/:id/status` - Cambia estatus (solo ultima version)
   - GET `/parameters/:id/versions` - Obtiene historial de versiones

3. **Paginacion y Filtros**
   - Paginacion con `page`, `limit`, `sort`, `order`
   - Filtros por `idModule`, `idType`, `name`, `status`
   - Por defecto solo muestra ultima version de cada parametro
   - Parametro `includeHistory=true` para ver todas las versiones

---

## Checklist de Desarrollo

### Backend
- [x] Metodos de versionado en repository
- [x] Logica de versionado en service con transacciones
- [x] Endpoints nuevos en controller
- [x] Rutas actualizadas
- [x] Validaciones implementadas
- [x] Manejo de errores

### Testing
- [x] Coleccion Postman actualizada

### Documentacion
- [x] Documentacion tecnica

---

## Archivos Modificados

### Backend (mrch.backend.somx.utils-api)

| Archivo | Descripcion |
|---------|-------------|
| `src/repositories/parameter.repo.ts` | Nuevos metodos para versionado |
| `src/services/parameter.service.ts` | Logica de versionado con transacciones |
| `src/controllers/parameter.controller.ts` | Nuevos endpoints |
| `src/routes/parameter.routes.ts` | Nuevas rutas |

### Postman
| Archivo | Descripcion |
|---------|-------------|
| [STM-1213 - Versionado de Parametros.postman_collection.json](../../../postman/STM-1213%20-%20Versionado%20de%20Parametros.postman_collection.json) | Endpoints de versionado |

---

## Endpoints API

### CRUD Basico

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/parameters` | Listar parametros (paginado, solo ultima version) |
| GET | `/api/parameters/:id` | Obtener parametro por ID |
| POST | `/api/parameters` | Crear parametro (version inicial 1.0) |
| PATCH | `/api/parameters/:id` | Actualizar metadatos (NO value) |
| DELETE | `/api/parameters/:id` | Eliminar (soft delete) |

### Sistema de Versionado

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/parameters/:id/versions` | Obtener historial de versiones |
| POST | `/api/parameters/:id/versions` | Crear nueva version (cambio de value) |
| PATCH | `/api/parameters/:id/status` | Cambiar estatus (1=Activo, 0=Inactivo) |

---

## Ejemplos de Uso

### 1. Crear Parametro (version 1.0)

```http
POST /api/parameters
Content-Type: application/json

{
    "idModule": 1,
    "idType": 1,
    "name": "MAX_RETRIES",
    "description": "Numero maximo de reintentos",
    "value": "3"
}
```

**Respuesta:**
```json
{
    "success": true,
    "data": {
        "idParameter": 1,
        "name": "MAX_RETRIES",
        "value": "3",
        "version": 1.0,
        "status": 1,
        "startDate": "2024-12-12T10:00:00.000Z"
    }
}
```

### 2. Crear Nueva Version (cambio de value)

```http
POST /api/parameters/1/versions
Content-Type: application/json

{
    "value": "5",
    "changeReason": "Incremento por requerimiento de negocio"
}
```

**Resultado:**
- Version anterior (1.0): `status=0`, `endDate=hoy`
- Version nueva (1.1): `status=1`, `value="5"`

### 3. Obtener Historial de Versiones

```http
GET /api/parameters/1/versions
```

**Respuesta:**
```json
{
    "success": true,
    "data": [
        {"idParameter": 2, "name": "MAX_RETRIES", "value": "5", "version": 1.1, "status": 1},
        {"idParameter": 1, "name": "MAX_RETRIES", "value": "3", "version": 1.0, "status": 0}
    ],
    "count": 2
}
```

### 4. Cambiar Estatus

```http
PATCH /api/parameters/2/status
Content-Type: application/json

{
    "status": 0
}
```

### 5. Actualizar Metadatos (sin crear version)

```http
PATCH /api/parameters/2
Content-Type: application/json

{
    "description": "Nueva descripcion",
    "idModule": 2
}
```

---

## Logica de Versionado

### Calculo de Version

```typescript
function calculateNextVersion(currentVersion: number): number {
    const intPart = Math.floor(currentVersion);
    const decPart = Math.round((currentVersion - intPart) * 10);

    if (decPart >= 9) {
        return intPart + 1; // 1.9 -> 2.0
    }
    return Number((intPart + (decPart + 1) / 10).toFixed(1)); // 1.1 -> 1.2
}
```

### Secuencia de Versiones

```
1.0 -> 1.1 -> 1.2 -> ... -> 1.9 -> 2.0 -> 2.1 -> ... -> 2.9 -> 3.0
```

### Transaccion Atomica

```typescript
// 1. Cerrar version actual
await queryRunner.manager.update(CatParameter, { idParameter: id }, {
    status: 0,
    endDate: new Date()
});

// 2. Crear nueva version
const newParam = queryRunner.manager.create(CatParameter, {
    ...existingData,
    value: newValue,
    version: nextVersion,
    status: 1,
    startDate: new Date()
});

await queryRunner.manager.save(newParam);
await queryRunner.commitTransaction();
```

---

## Validaciones

| Validacion | Endpoint | Mensaje |
|------------|----------|---------|
| value no permitido | PATCH /:id | "No se permite modificar 'value' via PATCH. Use POST /:id/versions" |
| Solo ultima version | POST /:id/versions | "Solo se puede versionar la ultima version del parametro" |
| Solo ultima version | PATCH /:id/status | "Solo se puede cambiar el estatus de la ultima version" |
| Status invalido | PATCH /:id/status | "Estatus invalido. Debe ser 0 (Inactivo) o 1 (Activo)" |
| Nombre duplicado | POST / | "Ya existe un parametro con el nombre 'X'" |
| Version duplicada | POST /:id/versions | "Ya existe version X para este parametro" |

---

## Query Parameters para Listado

| Parametro | Tipo | Default | Descripcion |
|-----------|------|---------|-------------|
| idModule | number | - | Filtrar por modulo |
| idType | number | - | Filtrar por tipo |
| name | string | - | Filtrar por nombre (busqueda parcial) |
| status | number | - | Filtrar por estatus (1=Activo, 0=Inactivo) |
| includeHistory | boolean | false | Incluir todas las versiones |
| page | number | 1 | Numero de pagina |
| limit | number | 10 | Registros por pagina |
| sort | string | idParameter | Campo ordenamiento |
| order | string | desc | Orden (asc/desc) |
