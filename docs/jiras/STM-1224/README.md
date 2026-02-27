# STM-1224: Bloqueo de Proveedores

## Descripcion
Implementacion del sistema de bloqueos temporales de proveedores por rango de fechas. Permite bloquear proveedores durante periodos especificos con validacion de solapamiento.

## Alcance
- Tabla `supplier_block` en esquema `shared_catalogs`
- API REST completa para gestion de bloqueos
- Validacion de solapamiento de fechas
- Consulta de estado de bloqueo en tiempo real

## Modelo de Datos

### Tabla: supplier_block

| Campo | Tipo | Descripcion |
|-------|------|-------------|
| id | SERIAL | Identificador unico |
| supplier_number | VARCHAR(20) | Numero de proveedor SAP |
| valid_from | DATE | Fecha inicio de bloqueo |
| valid_to | DATE | Fecha fin de bloqueo |
| block_reason | VARCHAR(255) | Motivo del bloqueo |
| status | INTEGER | 1=Activo, 0=Inactivo |
| created_at | TIMESTAMP | Fecha de creacion |
| created_by | VARCHAR(100) | Usuario creador |
| updated_at | TIMESTAMP | Fecha de actualizacion |
| updated_by | VARCHAR(100) | Usuario actualizador |

**Constraint:** `chk_supplier_block_dates CHECK (valid_to >= valid_from)`

## Endpoints API

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/supplier-blocks` | Listar todos los bloqueos |
| POST | `/supplier-blocks` | Crear bloqueo |
| GET | `/supplier-blocks/{id}` | Obtener bloqueo por ID |
| PUT | `/supplier-blocks/{id}` | Actualizar bloqueo |
| DELETE | `/supplier-blocks/{id}` | Eliminar (desactivar) bloqueo |
| GET | `/supplier-blocks/supplier/{supplierNumber}` | Bloqueos de un proveedor |
| GET | `/supplier-blocks/supplier/{supplierNumber}/active` | Bloqueos vigentes |
| GET | `/supplier-blocks/supplier/{supplierNumber}/is-blocked` | Verificar si esta bloqueado |
| GET | `/supplier-blocks/supplier/{supplierNumber}/at-date?date=yyyy-MM-dd` | Bloqueos a fecha especifica |

## Archivos Implementados

### Backend (catalogos-api)
- `src/main/resources/db/08_schema_proveedor.sql` - Script SQL con tabla supplier_block
- `src/main/java/com/sodimac/catman/api/model/entity/SupplierBlock.java` - Entidad JPA
- `src/main/java/com/sodimac/catman/api/repository/SupplierBlockRepository.java` - Repositorio
- `src/main/java/com/sodimac/catman/api/model/dto/SupplierBlockDto.java` - DTO respuesta
- `src/main/java/com/sodimac/catman/api/model/dto/SupplierBlockCreateDto.java` - DTO creacion
- `src/main/java/com/sodimac/catman/api/model/dto/SupplierBlockUpdateDto.java` - DTO actualizacion
- `src/main/java/com/sodimac/catman/api/service/SupplierBlockService.java` - Interface servicio
- `src/main/java/com/sodimac/catman/api/service/SupplierBlockServiceImpl.java` - Implementacion
- `src/main/java/com/sodimac/catman/api/controller/SupplierBlockController.java` - Controller REST

### BFF (catalogos)
- `api.yml` - OpenAPI actualizado con endpoints de bloqueos

## Validaciones

1. **Fechas:** `valid_to >= valid_from`
2. **Solapamiento:** No permite crear bloqueos que se solapen con bloqueos activos existentes del mismo proveedor
3. **Campos requeridos:** supplierNumber, validFrom, validTo

## Ejemplo de Uso

### Crear bloqueo
```json
POST /supplier-blocks
{
  "supplierNumber": "PROV001",
  "validFrom": "2025-01-01",
  "validTo": "2025-12-31",
  "blockReason": "Incumplimiento de contrato"
}
```

### Verificar si proveedor esta bloqueado
```json
GET /supplier-blocks/supplier/PROV001/is-blocked

Response:
{
  "supplierNumber": "PROV001",
  "blocked": true
}
```

## Relacion con otros tickets
- STM-1225: Catalogo de proveedores (tabla `supplier`)
