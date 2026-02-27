# STM-1166: Servicio de Gestion del Tren de Estatus

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1166

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :yellow_circle: Pendiente |
| **Asignado** | Por asignar |
| **Sprint** | Por definir |
| **Prioridad** | Alta (Dependencia de STM-410) |
| **Proyecto** | catalogos-api |

---

## Descripcion

Crear un servicio en el modulo de catalogos que administre el **tren de estatus permitido por opcion** (origen -> destino), con consultas y actualizaciones estandarizadas para validar las transiciones de estatus de manera consistente desde cualquier modulo del sistema.

---

## Modelo de Datos

### Tabla: status_train

**Esquema:** shared_catalogs

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| `id` | SERIAL PK | Identificador unico de la regla |
| `option_id` | INTEGER NOT NULL | Identificador de la opcion/funcionalidad (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte) |
| `source_status` | INTEGER NOT NULL | Estatus inicial de la transicion |
| `target_status` | INTEGER NOT NULL | Estatus final permitido |
| `created_by` | BIGINT NOT NULL | Usuario que creo la regla |
| `created_at` | TIMESTAMP NOT NULL | Fecha de creacion |
| `updated_by` | BIGINT | Usuario ultima actualizacion |
| `updated_at` | TIMESTAMP | Fecha ultima actualizacion |

**DDL:**
```sql
-- ============================================================================
-- Tabla: status_train
-- Descripción: Configuración de transiciones de estatus permitidas por opción.
--              Define qué cambios de estatus son válidos para cada tipo de documento.
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.status_train (
    id SERIAL PRIMARY KEY,                              -- Identificador único autoincremental
    option_id INTEGER NOT NULL,                         -- Identificador de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)
    source_status INTEGER NOT NULL,                     -- Estatus origen de la transición
    target_status INTEGER NOT NULL,                     -- Estatus destino permitido
    created_by BIGINT NOT NULL,                         -- Usuario que registró la regla
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Fecha de creación
    updated_by BIGINT,                                  -- Usuario de última actualización
    updated_at TIMESTAMP,                               -- Fecha de última actualización
    CONSTRAINT uk_status_train UNIQUE (option_id, source_status, target_status)
);

COMMENT ON TABLE shared_catalogs.status_train IS 'Configuración del tren de estatus: transiciones permitidas por opción/tipo de documento';
COMMENT ON COLUMN shared_catalogs.status_train.id IS 'Identificador único autoincremental';
COMMENT ON COLUMN shared_catalogs.status_train.option_id IS 'Opción/funcionalidad: 1=Factura(I), 2=NotaCredito(E), 3=ComplementoPago(P), 4=CartaPorte(T)';
COMMENT ON COLUMN shared_catalogs.status_train.source_status IS 'Estatus origen desde el cual se permite la transición';
COMMENT ON COLUMN shared_catalogs.status_train.target_status IS 'Estatus destino al cual se permite transicionar';
COMMENT ON COLUMN shared_catalogs.status_train.created_by IS 'ID del usuario que creó el registro';
COMMENT ON COLUMN shared_catalogs.status_train.created_at IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN shared_catalogs.status_train.updated_by IS 'ID del usuario que realizó la última actualización';
COMMENT ON COLUMN shared_catalogs.status_train.updated_at IS 'Fecha y hora de última modificación';

CREATE INDEX IF NOT EXISTS idx_status_train_option ON shared_catalogs.status_train(option_id);
CREATE INDEX IF NOT EXISTS idx_status_train_source ON shared_catalogs.status_train(option_id, source_status);
```

---

## Endpoints a Implementar

### 1. Validar Transicion de Estatus

```
GET /api/status-train/validate
```

**Parametros:**
| Parametro | Tipo | Obligatorio | Descripcion |
|-----------|------|-------------|-------------|
| `optionId` | Integer | Si | ID de la opcion/funcionalidad |
| `sourceStatus` | Integer | Si | Estatus actual |
| `targetStatus` | Integer | Si | Estatus destino deseado |

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "valid": true,
  "data": {
    "id": 45,
    "optionId": 1,
    "sourceStatus": 3,
    "targetStatus": 4,
    "createdBy": 1001,
    "createdAt": "2025-01-01T10:00:00",
    "updatedBy": 1002,
    "updatedAt": "2025-01-10T15:30:00"
  }
}
```

**Respuesta - Estatus origen no catalogado (400):**
```json
{
  "success": false,
  "valid": false,
  "code": "WRN7010",
  "message": "El estatus origen no existe catalogado. Por favor, valide la informacion antes de continuar."
}
```

**Respuesta - Transicion no permitida (400):**
```json
{
  "success": false,
  "valid": false,
  "code": "WRN7011",
  "message": "El estatus destino no existe catalogado. Por favor, valide la informacion antes de continuar."
}
```

---

### 2. Consultar Estatus Destino Permitidos

```
GET /api/status-train/allowed-destinations
```

**Parametros:**
| Parametro | Tipo | Obligatorio | Descripcion |
|-----------|------|-------------|-------------|
| `optionId` | Integer | Si | ID de la opcion/funcionalidad |
| `sourceStatus` | Integer | Si | Estatus actual |

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "id": 45,
      "optionId": 1,
      "sourceStatus": 3,
      "targetStatus": 4,
      "createdBy": 1001,
      "createdAt": "2025-01-01T10:00:00",
      "updatedBy": null,
      "updatedAt": null
    },
    {
      "id": 46,
      "optionId": 1,
      "sourceStatus": 3,
      "targetStatus": 11,
      "createdBy": 1001,
      "createdAt": "2025-01-01T10:00:00",
      "updatedBy": null,
      "updatedAt": null
    }
  ]
}
```

**Respuesta - Estatus origen no catalogado (400):**
```json
{
  "success": false,
  "code": "WRN7010",
  "message": "El estatus origen no existe catalogado. Por favor, valide la informacion antes de continuar."
}
```

---

### 3. Registrar Nueva Regla

```
POST /api/status-train
```

**Request Body:**
```json
{
  "optionId": 1,
  "sourceStatus": 3,
  "targetStatus": 4,
  "createdBy": 1001
}
```

**Respuesta exitosa (201):**
```json
{
  "success": true,
  "message": "Regla de tren de estatus registrada exitosamente",
  "data": {
    "id": 47,
    "optionId": 1,
    "sourceStatus": 3,
    "targetStatus": 4,
    "createdBy": 1001,
    "createdAt": "2025-01-12T10:00:00"
  }
}
```

---

### 4. Actualizar Regla Existente

```
PUT /api/status-train/{id}
```

**Parametros de ruta:**
- `id`: ID de la configuracion a actualizar

**Query Parameters:**
- `optionId`: ID de la opcion (para validacion adicional)

**Request Body:**
```json
{
  "sourceStatus": 3,
  "targetStatus": 5,
  "updatedBy": 1002
}
```

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "message": "Regla de tren de estatus actualizada exitosamente",
  "data": {
    "id": 47,
    "optionId": 1,
    "sourceStatus": 3,
    "targetStatus": 5,
    "createdBy": 1001,
    "createdAt": "2025-01-01T10:00:00",
    "updatedBy": 1002,
    "updatedAt": "2025-01-12T15:30:00"
  }
}
```

---

## Mensajes de Catalogo Requeridos

| Codigo | Tipo | Mensaje |
|--------|------|---------|
| WRN7010 | Advertencia | El estatus origen no existe catalogado. Por favor, valide la informacion antes de continuar. |
| WRN7011 | Advertencia | El estatus destino no existe catalogado. Por favor, valide la informacion antes de continuar. |

---

## Opciones/Funcionalidades (option_id)

| ID | Opcion | Codigo CFDI | Descripcion |
|----|--------|-------------|-------------|
| 1 | INVOICE | I | Tren de estatus para Facturas (CFDI v4.0) |
| 2 | CREDIT_NOTE | E | Tren de estatus para Notas de Credito (CFDI v4.0) |
| 3 | PAYMENT_COMPLEMENT | P | Tren de estatus para Complementos de Pago (Pagos v2.0) |
| 4 | CARTA_PORTE | T | Tren de estatus para Factura con CartaPorte (CartaPorte v3.1) |

---

## Referencia de Estatus por Opcion

### Facturas (option_id = 1) - InvoiceStatus.java

| Codigo | Nombre | Destinos Permitidos |
|--------|--------|---------------------|
| 0 | Rechazo Comercial | - |
| 1 | Pendiente Addenda | 2, 3, 13 |
| 2 | Recibido Parcial | 3, 13 |
| 3 | Pendiente de Contabilizar | 4 |
| 4 | Proceso de descarga | 5, 11 |
| 5 | Desglose de factura | 6, 11 |
| 6 | Pendiente de envio a contabilizar | 7, 11 |
| 7 | **Pendiente de Pago** | 8 |
| 8 | Pagado | 9 |
| 9 | Pendiente de complemento | 10 |
| 10 | Completado | - |
| 11 | Rechazo Contable | 7 |
| 12 | No valido fiscal | - |
| 13 | Pago Manual | - |

### Notas de Credito (option_id = 2) - CreditNoteStatus.java

| Codigo | Nombre | Destinos Permitidos |
|--------|--------|---------------------|
| 0 | Rechazo Comercial | - |
| 1 | Pendiente Addenda | 2, 3 |
| 2 | Recibido Parcial | 3 |
| 3 | Pendiente de Contabilizar | 4 |
| 4 | Proceso de descarga | 5, 11 |
| 5 | Desglose de NC | 6, 11 |
| 6 | Pendiente de envio a contabilizar | 7, 11 |
| 7 | Aplicado | 8 |
| 8 | Completado | - |
| 11 | Rechazo Contable | 7 |
| 12 | No valido fiscal | - |

### Complementos de Pago (option_id = 3)

> **Nota:** Actualmente `PaymentsEntity.java` no tiene un enum de estatus definido. Se usa un campo `status` INTEGER con valor por defecto 1. Pendiente definir el tren de estatus para este tipo de documento.

### Factura con CartaPorte (option_id = 4)

> **Nota:** Pendiente definir el tren de estatus. Evaluar si aplican los mismos estatus que Factura (option_id = 1) o si requiere un flujo diferente.

---

## Checklist de Desarrollo

### Analisis
- [x] Requerimientos entendidos
- [x] Analisis tecnico completado
- [ ] Diseno aprobado por el equipo

### Backend (catalogos-api)
- [x] Script DDL para tabla status_train (11_status_train_schema.sql)
- [x] Script DML para datos iniciales (12_status_train_data.sql)
- [x] Entidad JPA StatusTrain.java
- [x] Repository StatusTrainRepository.java
- [x] DTOs: StatusTrainDto, StatusTrainCreateDto, StatusTrainUpdateDto, StatusTrainValidationResponse, StatusTrainListResponse
- [x] Mapper StatusTrainMapper.java
- [x] Service StatusTrainService.java (interface + impl)
- [x] Controller StatusTrainController.java
- [x] Validaciones de negocio
- [x] Manejo de errores (WRN7010, WRN7011)

### Testing
- [ ] Pruebas unitarias
- [ ] Pruebas de integracion
- [ ] Coleccion Postman

### Documentacion
- [ ] README actualizado
- [ ] OpenAPI/Swagger actualizado

---

## Dependencias

### Este JIRA es dependencia de:
- **STM-410**: Ajuste de servicio de facturacion (requiere consumir este servicio)

### Este JIRA depende de:
- Ninguno (puede implementarse de forma independiente)

---

## Notas Tecnicas

### Migracion desde Enums Actuales

Actualmente el tren de estatus esta hardcodeado en enums:
- `InvoiceStatus.java` - 13 estados con transiciones en array
- `CreditNoteStatus.java` - 10 estados con transiciones en array

Este servicio centraliza las reglas en base de datos, permitiendo:
1. Modificar transiciones sin redespliegue
2. Auditoria de cambios
3. Consistencia entre modulos
4. Configuracion por opcion/funcionalidad

### Script de Migracion Inicial (03_status_train_data.sql)

Se debe crear un script que migre las transiciones actuales de los enums a la tabla:

```sql
-- ============================================================================
-- Datos iniciales para tabla status_train
-- Descripción: Transiciones de estatus migradas desde enums de fiscal-api
-- ============================================================================

-- Facturas (option_id = 1) - Desde InvoiceStatus.java
-- PENDIENTE_ADDENDA (1) -> RECIBIDO_PARCIAL (2), PENDIENTE_CONTABILIZAR (3), PAGO_MANUAL (13)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES
(1, 1, 2, 1), (1, 1, 3, 1), (1, 1, 13, 1),
-- RECIBIDO_PARCIAL (2) -> PENDIENTE_CONTABILIZAR (3), PAGO_MANUAL (13)
(1, 2, 3, 1), (1, 2, 13, 1),
-- PENDIENTE_CONTABILIZAR (3) -> PROCESO_DESCARGA (4)
(1, 3, 4, 1),
-- PROCESO_DESCARGA (4) -> DESGLOSE_FACTURA (5), RECHAZO_CONTABLE (11)
(1, 4, 5, 1), (1, 4, 11, 1),
-- DESGLOSE_FACTURA (5) -> PENDIENTE_ENVIO_CONTABILIZAR (6), RECHAZO_CONTABLE (11)
(1, 5, 6, 1), (1, 5, 11, 1),
-- PENDIENTE_ENVIO_CONTABILIZAR (6) -> PENDIENTE_PAGO (7), RECHAZO_CONTABLE (11)
(1, 6, 7, 1), (1, 6, 11, 1),
-- PENDIENTE_PAGO (7) -> PAGADO (8)
(1, 7, 8, 1),
-- PAGADO (8) -> PENDIENTE_COMPLEMENTO (9)
(1, 8, 9, 1),
-- PENDIENTE_COMPLEMENTO (9) -> COMPLETADO (10)
(1, 9, 10, 1),
-- RECHAZO_CONTABLE (11) -> PENDIENTE_PAGO (7)
(1, 11, 7, 1);

-- Notas de Credito (option_id = 2) - Desde CreditNoteStatus.java
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES
(2, 1, 2, 1), (2, 1, 3, 1),
(2, 2, 3, 1),
(2, 3, 4, 1),
(2, 4, 5, 1), (2, 4, 11, 1),
(2, 5, 6, 1), (2, 5, 11, 1),
(2, 6, 7, 1), (2, 6, 11, 1),
(2, 7, 8, 1),
(2, 11, 7, 1);

-- Complementos de Pago (option_id = 3) - Pendiente definir
-- Factura con CartaPorte (option_id = 4) - Pendiente definir
```

---

## Referencias

- [InvoiceStatus.java](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/enums/InvoiceStatus.java)
- [CreditNoteStatus.java](../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/model/enums/CreditNoteStatus.java)
