# STM-605: Mejoras API Bloqueo de Proveedores

## Descripcion

Mejoras al CRUD de bloqueos de proveedores (creado en STM-1224) para cumplir con estandares de negocio:
1. Validar que el proveedor exista en el sistema antes de crear un bloqueo
2. Reemplazar mensajes hardcodeados por codigos del catalogo de mensajes (`shared_catalogs`)
3. Retornar mensaje de exito con codigo del catalogo al crear/actualizar
4. Mensajes personalizados para errores de negocio (proveedor no existe, bloqueo solapado)

## Codigos de Mensaje Utilizados

| Codigo | Catalogo | Descripcion | Estado |
|--------|----------|-------------|--------|
| **WRN001** | CatMsgAdvertencia | "La fecha inicio no puede ser superior a la fecha final." | Ya existia |
| **RES001** | CatMsgExitoso | "El registro del proveedor {0} se realizó exitosamente." | Ya existia |
| **BUS215** | CatMsgNegocio | "El proveedor con número {0} no se encuentra registrado." | **Nuevo** (dict_id 7015) |
| **BUS216** | CatMsgNegocio | "Ya existe un bloqueo activo para el proveedor {0} que se solapa con las fechas especificadas." | **Nuevo** (dict_id 7016) |

> **Nota para el JIRA**: El ticket original referenciaba codigos WRN7000 y RES1001 que no existen. Se reutilizaron los codigos existentes WRN001 y RES001 que cumplen el mismo proposito. Solo se crearon BUS215 y BUS216 como nuevos.

## Cambios Realizados

### SQL - Nuevos mensajes BUS215 y BUS216
- **Script**: [`seed_BUS215_BUS216.sql`](./seed_BUS215_BUS216.sql)
- Inserta en `shared_catalogs.dictionary_lang` (3 idiomas: es, en, pt) y `shared_catalogs.catalog_detail` (header CatMsgNegocio)
- Idempotente: verifica existencia antes de insertar

### Backend (catalogos-api)

| Archivo | Cambio |
|---------|--------|
| `model/dto/SupplierBlockResponseDto.java` | **Nuevo** - DTO wrapper con `message` + `data` |
| `service/SupplierBlockService.java` | `create()` retorna `SupplierBlockResponseDto`, `update()` retorna `Optional<SupplierBlockResponseDto>` |
| `service/SupplierBlockServiceImpl.java` | Inyecta `SupplierRepository` y `CatalogHeaderService`, agrega `getMessage()`, valida proveedor existente, usa codigos de catalogo |
| `controller/SupplierBlockController.java` | Tipos de retorno actualizados en POST y PUT, Swagger actualizado |
| `resources/db/16_STM-605_supplier_block_messages.sql` | Script de migracion para los mensajes |

### BFF (catalogos)

| Archivo | Cambio |
|---------|--------|
| `api.yml` | Nuevo schema `SupplierBlockResponseDto`, POST 201 y PUT 200 referencian `SupplierBlockResponseDto` |

## Respuesta del API (antes vs despues)

### Antes (STM-1224) - POST /supplier-blocks → 201
```json
{
  "id": 1,
  "supplierNumber": "0000100025",
  "validFrom": "2026-06-01",
  "validTo": "2026-06-30",
  "blockReason": "Incumplimiento",
  "status": 1,
  ...
}
```

### Despues (STM-605) - POST /supplier-blocks → 201
```json
{
  "message": "El registro del proveedor 0000100025 se realizó exitosamente.",
  "data": {
    "id": 1,
    "supplierNumber": "0000100025",
    "validFrom": "2026-06-01",
    "validTo": "2026-06-30",
    "blockReason": "Incumplimiento",
    "status": 1,
    ...
  }
}
```

### Error - Proveedor no existe → 400
```json
{
  "error": "El proveedor con número 99999 no se encuentra registrado.",
  "code": 400,
  "details": "..."
}
```

### Error - Bloqueo solapado → 400
```json
{
  "error": "Ya existe un bloqueo activo para el proveedor 0000100025 que se solapa con las fechas especificadas.",
  "code": 400,
  "details": "..."
}
```

## Verificacion

```bash
# 1. Proveedor no existe → 400 + BUS215
curl -s -X POST http://localhost:8083/supplier-blocks \
  -H "Content-Type: application/json" \
  -d '{"supplierNumber":"99999","validFrom":"2026-01-01","validTo":"2026-12-31"}' | jq .

# 2. Fecha inicio > fin → 400 + WRN001
curl -s -X POST http://localhost:8083/supplier-blocks \
  -H "Content-Type: application/json" \
  -d '{"supplierNumber":"0000100025","validFrom":"2026-12-31","validTo":"2026-01-01"}' | jq .

# 3. Exito → 201 + RES001 con message + data
curl -s -X POST http://localhost:8083/supplier-blocks \
  -H "Content-Type: application/json" \
  -d '{"supplierNumber":"0000100025","validFrom":"2026-06-01","validTo":"2026-06-30","blockReason":"Prueba STM-605"}' | jq .

# 4. Bloqueo solapado → 400 + BUS216
curl -s -X POST http://localhost:8083/supplier-blocks \
  -H "Content-Type: application/json" \
  -d '{"supplierNumber":"0000100025","validFrom":"2026-06-15","validTo":"2026-07-15"}' | jq .
```

## Archivos Adjuntos
- [`seed_BUS215_BUS216.sql`](./seed_BUS215_BUS216.sql) - Script SQL para mensajes de negocio
- [`STM-605_Supplier_Block.postman_collection.json`](./STM-605_Supplier_Block.postman_collection.json) - Coleccion Postman (API directa + BFF)

## Relacion con otros tickets
- **STM-1224**: Creacion del CRUD de bloqueos de proveedores (base)
- **STM-1225**: Catalogo de proveedores (tabla `supplier` usada para validacion)
