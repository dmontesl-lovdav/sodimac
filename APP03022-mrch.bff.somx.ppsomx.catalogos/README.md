# BFF (Backend For Frontend) para Catalogos API

Este BFF actúa como proxy transparente entre el frontend y la Catalogos API backend.
Proporciona acceso a las funcionalidades de catálogos y mensajes del sistema para el Portal de Proveedores de Sodimac México.

## Funcionalidades principales:
- Consulta de catálogos del sistema por módulo o código
- Consulta de detalles de catálogos con soporte multi-idioma
- Obtención de mensajes de error/éxito por código e idioma
- Health check del servicio

Este proxy funciona de manera transparente sin validación de autenticación.

## Usage
### Installing:
```
npm i
```

### Configuring:
Crea un archivo `.env` o declara las siguientes variables de entorno:
```
CATALOGOS_API_PORT=8083
REMOTE_URL=http://localhost:${CATALOGOS_API_PORT}
LOCAL_PORT=3000
LOCAL_CONTEXT=/
HEALTH_PATH=/health
```
Donde:
- `CATALOGOS_API_PORT` es el puerto donde corre Catalogos API backend (por defecto: 8083)
- `REMOTE_URL` es la URL del servicio Catalogos API backend (usa ${CATALOGOS_API_PORT})
- `LOCAL_PORT` es el puerto local donde este proxy escuchará conexiones (por defecto: 3000)
- `LOCAL_CONTEXT` es el contexto base para este proxy (por defecto: /)
- `HEALTH_PATH` define la ruta de health check (por defecto: /health)

**Nota:** Para cambiar el puerto de Catalogos API en diferentes ambientes, solo modifica `CATALOGOS_API_PORT` en el `.env`

### Execution:
```
npm start
```

El BFF estará escuchando en `http://localhost:3000`

---

## Endpoints principales

### Health Check
```bash
# Health check del BFF
curl http://localhost:3000/health
```

### Catálogos
```bash
# Obtener todos los catálogos
curl http://localhost:3000/api/v1/catalogs

# Obtener catálogos por módulo
curl http://localhost:3000/api/v1/catalogs/module/FISCAL

# Obtener catálogo por código
curl http://localhost:3000/api/v1/catalogs/code/CAT_001

# Obtener detalles de un catálogo
curl http://localhost:3000/api/v1/catalogs/CAT_001/details

# Obtener detalle específico por key e idioma
curl "http://localhost:3000/api/v1/catalogs/CAT_001/details/KEY_001?language=es"
```

### Mensajes
```bash
# Obtener mensaje por código e idioma
curl "http://localhost:3000/api/v1/messages/MSG_001?language=es"
```

### Mapeo de URLs

El BFF mapea las rutas de la siguiente manera:
- `http://localhost:3000/*` → `http://localhost:8083/*`

Ejemplos:
- `/health` → `/health`
- `/api/v1/catalogs` → `/api/v1/catalogs`
- `/api/v1/messages/MSG_001` → `/api/v1/messages/MSG_001`

## Documentación OpenAPI

La especificación OpenAPI completa está disponible en:
- Archivo: `cloud-endpoint/openapi.yaml`

## Arquitectura

```
Frontend/Cliente
    ↓
BFF Catalogos (puerto 3000)
    ↓ proxy /* → /*
Catalogos API (puerto 8083)
    ↓
PostgreSQL
```

## Notas

- El BFF tiene un tamaño máximo de payload de **10MB**
- No requiere autenticación
