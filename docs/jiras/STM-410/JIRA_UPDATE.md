# STM-410: Ajuste de Servicio de Facturacion

## Estado: COMPLETADO

---

## Resumen de Implementacion

Se implemento la funcionalidad de actualizacion de estatus de facturas con validacion de transiciones mediante integracion con el servicio de tren de estatus (STM-1166) en **catalogos-api**.

---

## Componentes Implementados

### Backend (fiscal-api)

| Componente | Archivo | Descripcion |
|------------|---------|-------------|
| Controller | `InvoiceController.java` | Endpoints de busqueda y actualizacion de estatus |
| Service | `InvoiceServiceImpl.java` | Logica de negocio para actualizacion de estatus |
| DTO Request | `StatusUpdateRequestDto.java` | DTO para solicitud de actualizacion |
| DTO Response | `StatusUpdateResponseDto.java` | DTO para respuesta de actualizacion |
| Client Service | `StatusTrainApiServiceImpl.java` | Cliente para consumir servicio de tren de estatus |
| Migration | `STM-410_add_accounting_date.sql` | Script para agregar columna accounting_date |

### Integracion con STM-1166

| Servicio | URL | Descripcion |
|----------|-----|-------------|
| catalogos-api | `http://localhost:8083/status-train/validate` | Validacion de transiciones |

---

## Endpoints Implementados

### 1. Busqueda por Estatus
```
POST /invoices/search/by-status
```
Busca facturas/NC filtrando por estatus (campo obligatorio).

**Request Body:**
```json
{
  "rfcEmisor": "SOD970101ABC",
  "fechaInicioRecepcion": "2025-08-01",
  "fechaFinalRecepcion": "2026-01-31",
  "tipoDocumento": "I",
  "estatus": 1,
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

### 2. Actualizacion de Estatus
```
PUT /invoices/{uuid}/status
```
Actualiza el estatus de una factura validando la transicion con el tren de estatus.

**Request Body:**
```json
{
  "numeroProveedor": 1234567890,
  "estatusOrigen": 3,
  "estatusDestino": 4,
  "idUsuarioActualizacion": 12345,
  "comentario": "Transicion de Pendiente Contabilizar a Proceso de Descarga",
  "fechaContabilizacion": "2026-01-16"
}
```

**Nota:** `fechaContabilizacion` es requerido cuando `estatusDestino = 7` (Pendiente de Pago).

---

## Codigos de Respuesta

### Exito
| Codigo | Mensaje | Descripcion |
|--------|---------|-------------|
| BUS3010 | Estatus actualizado exitosamente | Actualizacion completada |

### Errores de Negocio
| Codigo | HTTP | Mensaje | Descripcion |
|--------|------|---------|-------------|
| BUS3101 | 404 | Documento no encontrado | UUID no existe en la base de datos |
| BUS3102 | 400 | El estatus actual no coincide | El estatus actual del documento no coincide con estatusOrigen |

### Errores de Validacion (desde STM-1166)
| Codigo | HTTP | Mensaje | Descripcion |
|--------|------|---------|-------------|
| WRN7010 | 400 | Estatus origen no catalogado | El estatus origen no existe en el tren |
| WRN7011 | 400 | Transicion no permitida | La transicion solicitada no esta permitida |

---

## Flujo de Estatus (Facturas - option_id=1)

```
1 (Pendiente Addenda)
    |
    +---> 2 (Validada)
    +---> 3 (Pendiente de Contabilizar) ---> 4 (Proceso de descarga)
    |                                              |
    +---> 13 (Rechazada)                          +---> 5 (Desglose de factura)
                                                   |         |
                                                   |         +---> 6 (Pendiente de envio)
                                                   |                    |
                                                   |                    +---> 7 (Pendiente de Pago)
                                                   |                              |
                                                   |                              +---> 8 (Pagado)
                                                   |                                       |
                                                   |                                       +---> 9 (Complemento)
                                                   |                                                  |
                                                   |                                                  +---> 10 (Completado)
                                                   |
                                                   +---> 11 (Rechazo contable) ---> 7 (Pendiente de Pago)
```

---

## Pruebas Realizadas

### Fecha de Pruebas: 2026-01-16

### 1. Busqueda por Estatus
| Prueba | Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|--------|---------|-------------------|-------------------|--------|
| 1.1 Buscar facturas status=1 | RFC, tipo=I, estatus=1 | Lista de facturas | 3 facturas | PASS |
| 1.2 Buscar NC status=2 | RFC, tipo=E, estatus=2 | Lista de NC | 1 NC | PASS |
| 1.3 Sin campo estatus | RFC, tipo=I | HTTP 400 | HTTP 400 | PASS |

### 2. Actualizacion de Estatus - Casos Exitosos
| Prueba | Transicion | Resultado Esperado | Resultado Obtenido | Estado |
|--------|------------|-------------------|-------------------|--------|
| 2.1 Addenda -> Contabilizar | 1 -> 3 | BUS3010 | BUS3010 | PASS |
| 2.2 Contabilizar -> Descarga | 3 -> 4 | BUS3010 | BUS3010 | PASS |
| 2.3 Descarga -> Desglose | 4 -> 5 | BUS3010 | BUS3010 | PASS |
| 2.4 Desglose -> Envio | 5 -> 6 | BUS3010 | BUS3010 | PASS |
| 2.5 Envio -> Pago (con fecha) | 6 -> 7 | BUS3010 | BUS3010 | PASS |

### 3. Actualizacion de Estatus - Casos de Error
| Prueba | Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|--------|---------|-------------------|-------------------|--------|
| 3.1 Transicion no permitida | 3 -> 8 | WRN7011 | WRN7011 | PASS |
| 3.2 Estatus no coincide | origen=5 (actual=3) | BUS3102 | BUS3102 | PASS |
| 3.3 UUID no existe | UUID invalido | BUS3101 | BUS3101 | PASS |
| 3.4 Estatus origen invalido | origen=999 | BUS3102 | BUS3102 | PASS |

---

## Dependencias

### Este JIRA depende de:
- **STM-1166**: Servicio de Gestion del Tren de Estatus (catalogos-api)

### Servicios Requeridos
| Servicio | Puerto | Descripcion |
|----------|--------|-------------|
| fiscal-api | 8082 | API de facturacion |
| catalogos-api | 8083 | API de catalogos (tren de estatus) |
| PostgreSQL | 5434 | Base de datos |

---

## Configuracion Requerida

### Variables de Entorno (fiscal-api)
```properties
SERVER_PORT=8082
DATASOURCE_URL=jdbc:postgresql://localhost:5434/b2b_portal
DATASOURCE_USERNAME=wwwb2bportal
DATASOURCE_PASSWORD=***
CATALOGOS_API_URL=http://localhost:8083
```

### Base de Datos
- Schema: `tenant_fiscal`
- Tabla: `invoice`
- Columna nueva: `accounting_date` (DATE, nullable)
- Constraint actualizado: `chk_invoice_status` permite valores 1-13

---

## Archivos de Prueba

- Coleccion Postman: `docs/jiras/STM-410/STM-410_Invoice_Status_Update.postman_collection.json`

---

## Commits Relacionados

- feat(STM-410): script migracion columna accounting_date

---

## Notas Adicionales

1. El campo `estatus` es **obligatorio** en el endpoint de busqueda por estatus
2. La `fechaContabilizacion` solo se guarda cuando `estatusDestino = 7`
3. El servicio valida la transicion con catalogos-api antes de actualizar
4. Si catalogos-api no esta disponible, la actualizacion falla con error tecnico
5. El constraint `chk_invoice_status` debe permitir valores 1-13 para soportar todos los estatus del tren
