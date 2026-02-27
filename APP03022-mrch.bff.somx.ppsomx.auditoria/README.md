# BFF Auditoría

Backend For Frontend (BFF) que actúa como proxy transparente hacia el servicio `auditoria-api`.

## Descripción

Este BFF proporciona un punto de entrada para el frontend hacia los servicios de auditoría, manejando:
- Registro de logs de actividad
- Consulta de logs con filtros y paginación
- Generación de UUIDs para trazabilidad

## Arquitectura

```
Frontend → BFF Auditoría (NodeJS/Express) → auditoria-api (Backend)
                 ↓
           Proxy Transparente
```

## Requisitos

- Node.js 22.x
- npm

## Instalación

```bash
npm install
```

## Variables de Entorno

Copiar `.env.example` a `.env` y configurar:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `REMOTE_URL` | URL del backend auditoria-api | `http://localhost:8091` |
| `LOCAL_PORT` | Puerto del BFF | `3008` |
| `LOCAL_CONTEXT` | Contexto base | `/` |
| `HEALTH_PATH` | Path del health check | `/health` |
| `AUTH_PUBLIC_KEY` | Certificado público para JWT (base64) | `MIIClz...` |

## Ejecución

### Desarrollo
```bash
npm run start
```

### Docker
```bash
docker build -t bff-auditoria .
docker run -p 8080:8080 bff-auditoria
```

## Endpoints

El BFF actúa como proxy transparente. Los endpoints disponibles son:

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/health` | Health check del servicio |
| GET | `/api/activity-logs` | Listar logs de actividad |
| POST | `/api/activity-logs` | Crear log de actividad |
| GET | `/api/activity-logs/uuid` | Generar UUID |

## Estructura del Proyecto

```
├── src/
│   └── App.js              # Aplicación principal con proxy
├── cloud-endpoint/
│   └── openapi.yaml        # Especificación OpenAPI
├── kustomization/
│   ├── base/               # Configuración base de Kubernetes
│   ├── development/        # Configuración para desarrollo
│   ├── uat/                # Configuración para UAT
│   └── production/         # Configuración para producción
├── Dockerfile
├── package.json
├── .env.example
└── .gitlab-ci.yml
```

## Despliegue

El proyecto utiliza GitLab CI/CD con Kustomize para despliegue en Kubernetes.

### Ambientes

- **Development**: `vendor-dev.fbusinesscenter.com/ppsomx/auditoria/`
- **UAT**: `vendor-uat.fbusinesscenter.com/ppsomx/auditoria/`
- **Production**: `vendor.fbusinesscenter.com/ppsomx/auditoria/`

## Tecnologías

- Node.js 22.x
- Express 5.x
- express-http-proxy
- Pino (logging)
- dotenv

## Referencias

- Backend: `APP03022-mrch.backend.somx.auditoria-api`
- Arquitectura base: `APP03022-mrch.bff.somx.ppsomx.fiscal`
