# BFF (Backend For Frontend) para Fiscal API

Este BFF actúa como proxy transparente entre el frontend y la Fiscal API backend.
Proporciona acceso a las funcionalidades de procesamiento de documentos fiscales del SAT (Sistema de Administración Tributaria) de México.

## Funcionalidades principales:
- Procesamiento de XML fiscales (CFDI 4.0, Complementos de Pago 2.0, Carta Porte 3.1)
- Validación con PAC (Proveedores Autorizados de Certificación)
- Registro y consulta de complementos de pago
- Gestión de catálogos fiscales (PACs, emisores, receptores)
- Consultas de facturas, pagos y documentos relacionados

Este proxy funciona de manera transparente y puede validar conexiones mediante JWT contra la firma pública de Keycloak (opcional en desarrollo).

## Usage
### Installing:
```
npm i
```

### Configuring:
Crea un archivo `.env` o declara las siguientes variables de entorno:
```
FISCAL_API_PORT=8082
REMOTE_URL=http://localhost:${FISCAL_API_PORT}
LOCAL_PORT=3000
LOCAL_CONTEXT=/api/v1/fiscal
HEALTH_PATH=/health
AUTH_PUBLIC_KEY=
```
Donde:
- `FISCAL_API_PORT` es el puerto donde corre Fiscal API backend (por defecto: 8082)
- `REMOTE_URL` es la URL del servicio Fiscal API backend (usa ${FISCAL_API_PORT})
- `LOCAL_PORT` es el puerto local donde este proxy escuchará conexiones (por defecto: 3000)
- `LOCAL_CONTEXT` es el contexto base para este proxy (por defecto: /api/v1/fiscal)
- `HEALTH_PATH` define la ruta de health check (por defecto: /health)
- `AUTH_PUBLIC_KEY` es el certificado público RSA X509 de Keycloak en formato PEM o DER (opcional para desarrollo local)

**Nota:** Para cambiar el puerto de Fiscal API en diferentes ambientes, solo modifica `FISCAL_API_PORT` en el `.env`

### Execution:
```
npm start
```

El BFF estará escuchando en `http://localhost:3000`

## Scripts de Prueba

Para facilitar las pruebas, se incluyen scripts batch que puedes ejecutar:

```bash
# Menu interactivo con todas las opciones
test-bff.bat

# Pruebas completas automaticas
test-endpoints.bat

# Busquedas avanzadas
test-search-advanced.bat

# Subir y procesar XML
test-upload-xml.bat
```

---

## Endpoints principales

### Health Check
```bash
# Health check del BFF (no requiere autenticación)
curl http://localhost:3000/health

# Health check del procesador XML (requiere header Authorization)
curl -H "Authorization: Bearer dummy-token" \
  http://localhost:3000/api/v1/fiscal/api/fiscal/xml/health
```

### Catálogos Fiscales
```bash
# Obtener catálogo de PACs
curl -H "Authorization: Bearer dummy-token" \
  http://localhost:3000/api/v1/fiscal/api/pac-catalog

# Obtener emisores
curl -H "Authorization: Bearer dummy-token" \
  http://localhost:3000/api/v1/fiscal/api/issuers

# Obtener receptores
curl -H "Authorization: Bearer dummy-token" \
  http://localhost:3000/api/v1/fiscal/api/receivers
```

**Nota importante**: Aunque la validación JWT está deshabilitada (comentada en App.js), el BFF requiere que el header `Authorization` esté presente. Para desarrollo local, puedes usar cualquier valor como `Bearer dummy-token`.

### Procesamiento XML
```bash
# Procesar archivo XML fiscal
curl -X POST \
  -H "Authorization: Bearer dummy-token" \
  -F "file=@complemento_pago.xml" \
  http://localhost:3000/api/v1/fiscal/api/fiscal/xml/process/file
```

### Complementos de Pago
```bash
# Registrar complemento de pago
curl -X POST \
  -H "Authorization: Bearer dummy-token" \
  -F "xmlFile=@complemento.xml" \
  -F "idProveedor=123" \
  -F "tipoAddenda=5" \
  -F "tipoProveedor=PROVEEDOR" \
  -F "idUsuario=456" \
  http://localhost:3000/api/v1/fiscal/api/fiscal/complementos-pago/registrar

# Buscar complementos de pago (búsqueda simple)
curl -H "Authorization: Bearer dummy-token" \
  "http://localhost:3000/api/v1/fiscal/api/fiscal/complementos-pago/buscar?rfcEmisor=XAXX010101000&page=0&size=20"

# Buscar complementos de pago (búsqueda avanzada con múltiples filtros)
curl -H "Authorization: Bearer dummy-token" \
  "http://localhost:3000/api/v1/fiscal/api/fiscal/complementos-pago/buscar?rfcEmisor=XAXX010101000&status=1&fechaPagoInicio=2025-01-01&fechaPagoFin=2025-12-31&montoMinimo=1000&montoMaximo=50000&sortBy=paymentDate&sortDirection=DESC&page=0&size=20"
```

**Parámetros de búsqueda disponibles:**
- `paymentsUuid`: UUID del complemento (folio fiscal)
- `rfcEmisor`: RFC del emisor (proveedor)
- `rfcReceptor`: RFC del receptor (Sodimac/Falabella)
- `folio`: Número de folio
- `serie`: Serie del complemento
- `numeroProveedor`: Número de proveedor
- `fechaPagoInicio`, `fechaPagoFin`: Rango de fechas (YYYY-MM-DD)
- `status`: Estado (0=Cancelado, 1=Vigente, 2=Pendiente, 3=Rechazado)
- `montoMinimo`, `montoMaximo`: Rango de montos
- `sortBy`: Campo de ordenamiento (paymentDate, folio, serie, createdAt)
- `sortDirection`: Dirección (ASC, DESC)
- `page`, `size`: Paginación
```

### Mapeo de URLs

El BFF mapea las rutas de la siguiente manera:
- `http://localhost:3000/api/v1/fiscal/*` → `http://localhost:8082/*`

Ejemplos:
- `/api/v1/fiscal/pac-catalog` → `/pac-catalog`
- `/api/v1/fiscal/fiscal/xml/health` → `/fiscal/xml/health`
- `/api/v1/fiscal/validacion/upload` → `/validacion/upload`

## Documentación OpenAPI

La especificación OpenAPI completa está disponible en:
- Archivo: `cloud-endpoint/openapi.yaml`
- Archivo JSON: `cloud-endpoint/openapi.json` (generado desde fiscal-api)

## Arquitectura

```
Frontend/Cliente
    ↓
BFF Fiscal (puerto 3000)
    ↓ proxy /api/v1/fiscal/* → /*
Fiscal API (puerto 8082)
    ↓
PostgreSQL (10.138.153.10:5432)
```

## Notas

- El BFF tiene un tamaño máximo de payload de **66MB** para soportar XMLs grandes
- La validación JWT está **deshabilitada por defecto** para facilitar desarrollo local (ver líneas 47-51 en `src/App.js`)
