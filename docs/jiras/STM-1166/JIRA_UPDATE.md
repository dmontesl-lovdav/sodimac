# STM-1166: Servicio de Gestion del Tren de Estatus

## Estado: COMPLETADO

---

## Resumen de Implementacion

Se implemento el servicio centralizado de tren de estatus en **catalogos-api** que permite validar y administrar transiciones de estatus permitidas por tipo de documento.

---

## Componentes Implementados

### Backend (catalogos-api)

| Componente | Archivo | Descripcion |
|------------|---------|-------------|
| Entity | `StatusTrain.java` | Entidad JPA para tabla status_train |
| Repository | `StatusTrainRepository.java` | Repositorio con queries personalizados |
| Service | `StatusTrainService.java` | Interface del servicio |
| Service Impl | `StatusTrainServiceImpl.java` | Implementacion de logica de negocio |
| Controller | `StatusTrainController.java` | Endpoints REST |
| DTOs | `StatusTrainDto.java`, `StatusTrainCreateDto.java`, `StatusTrainUpdateDto.java`, `StatusTrainValidationResponse.java`, `StatusTrainListResponse.java` | Objetos de transferencia |
| Mapper | `StatusTrainMapper.java` | Conversion entity <-> DTO |

### Scripts de Base de Datos

| Script | Ubicacion | Descripcion |
|--------|-----------|-------------|
| Schema | `src/main/resources/db/11_status_train_schema.sql` | Creacion de tabla status_train |
| Data | `src/main/resources/db/12_status_train_data.sql` | Datos iniciales de transiciones |

---

## Endpoints Implementados

### 1. Validar Transicion
```
GET /status-train/validate?optionId={id}&sourceStatus={src}&targetStatus={tgt}
```
- **200 OK**: Transicion permitida (`valid: true`)
- **400 Bad Request**:
  - `WRN7010`: Estatus origen no catalogado
  - `WRN7011`: Transicion no permitida

### 2. Consultar Destinos Permitidos
```
GET /status-train/allowed-destinations?optionId={id}&sourceStatus={src}
```
- **200 OK**: Lista de destinos permitidos
- **400 Bad Request**: `WRN7010` si estatus origen no existe

### 3. CRUD de Reglas
```
POST   /status-train                  - Crear regla
GET    /status-train/{id}             - Obtener por ID
PUT    /status-train/{id}?optionId={} - Actualizar regla
DELETE /status-train/{id}             - Eliminar regla
```

---

## Opciones (option_id)

| ID | Tipo | Codigo CFDI | Descripcion |
|----|------|-------------|-------------|
| 1 | INVOICE | I | Facturas |
| 2 | CREDIT_NOTE | E | Notas de Credito |
| 3 | PAYMENT_COMPLEMENT | P | Complementos de Pago |
| 4 | CARTA_PORTE | T | Carta Porte |

---

## Codigos de Error

| Codigo | Tipo | Mensaje |
|--------|------|---------|
| WRN7010 | Warning | El estatus origen no existe catalogado. Por favor, valide la informacion antes de continuar. |
| WRN7011 | Warning | El estatus destino no existe catalogado. Por favor, valide la informacion antes de continuar. |

---

## Pruebas Realizadas

### Fecha de Pruebas: 2026-01-15

### 1. Validacion de Transiciones
| Prueba | Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|--------|---------|-------------------|-------------------|--------|
| 1.1 Transicion permitida | opt=1, src=3, tgt=4 | valid=true | valid=true | PASS |
| 1.2 Transicion NO permitida | opt=1, src=3, tgt=8 | WRN7011 | WRN7011 | PASS |
| 1.3 Origen inexistente | opt=1, src=99, tgt=4 | WRN7010 | WRN7010 | PASS |

### 2. Consulta de Destinos
| Prueba | Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|--------|---------|-------------------|-------------------|--------|
| 2.1 Destinos desde status 1 | opt=1, src=1 | [2,3,13] | count=3 | PASS |
| 2.2 Destinos desde status 3 | opt=1, src=3 | [4] | count=1 | PASS |
| 2.3 Destinos desde status 4 | opt=1, src=4 | [5,11] | count=2 | PASS |
| 2.4 NC destinos desde status 1 | opt=2, src=1 | [2,3] | count=2 | PASS |
| 2.5 Estado final (10) | opt=1, src=10 | WRN7010 | WRN7010 | PASS |

### 3. CRUD de Reglas
| Prueba | Operacion | Resultado Esperado | Resultado Obtenido | Estado |
|--------|-----------|-------------------|-------------------|--------|
| 3.1 Crear regla | POST | 201 Created | 201 Created | PASS |
| 3.2 Obtener por ID | GET /1 | 200 OK | 200 OK | PASS |
| 3.3 Actualizar regla | PUT /1 | 200 OK | 200 OK | PASS |
| 3.4 Eliminar regla | DELETE | 204 No Content | 204 No Content | PASS |

### 4. Flujo Completo Factura
| Transicion | Descripcion | Estado |
|------------|-------------|--------|
| 1 -> 3 | Addenda -> Contabilizar | PASS |
| 3 -> 4 | Contabilizar -> Descarga | PASS |
| 4 -> 5 | Descarga -> Desglose | PASS |
| 5 -> 6 | Desglose -> Envio | PASS |
| 6 -> 7 | Envio -> Pago | PASS |
| 7 -> 8 | Pago -> Pagado | PASS |
| 8 -> 9 | Pagado -> Complemento | PASS |
| 9 -> 10 | Complemento -> Completado | PASS |

### 5. Flujo Rechazo Contable
| Transicion | Descripcion | Estado |
|------------|-------------|--------|
| 4 -> 11 | Descarga -> Rechazo | PASS |
| 11 -> 7 | Rechazo -> Pago (recuperacion) | PASS |

---

## Dependencias

### Este JIRA es requerido por:
- **STM-410**: Ajuste de servicio de facturacion (fiscal-api consume este servicio)

---

## Configuracion Requerida

### Variables de Entorno (catalogos-api)
```properties
SERVER_PORT=8083
DATASOURCE_URL=jdbc:postgresql://localhost:5434/b2b_portal
DATASOURCE_USERNAME=wwwb2bportal
DATASOURCE_PASSWORD=***
```

### Schema de Base de Datos
- Schema: `shared_catalogs`
- Tabla: `status_train`

---

## Archivos de Prueba

- Coleccion Postman: `docs/jiras/STM-1166/STM-1166_Status_Train.postman_collection.json`

---

## Commits Relacionados

- feat(STM-1166): implementacion servicio tren de estatus
- feat(STM-1166): scripts de migracion status_train

---

## Notas Adicionales

1. El servicio esta disponible en puerto 8083
2. Los datos iniciales incluyen transiciones para Facturas (option_id=1) y Notas de Credito (option_id=2)
3. Complementos de Pago (option_id=3) y Carta Porte (option_id=4) pendientes de definir transiciones
4. El servicio es consumido por fiscal-api para validar cambios de estatus (STM-410)
