# STM-831 - Respuesta para JIRA

## Comentario de Resolucion

```
Se implemento el endpoint de filtrado de proveedores segun los requerimientos de STM-831.

**Endpoint implementado:**
GET /suppliers/filter?tipoProveedor={0-4}&estatusBloqueo={0-2}

**Parametros:**
- tipoProveedor: 0=Todos, 1=Mercancia, 2=Transporte, 3=Indirectos, 4=Servicios
- estatusBloqueo: 0=Solo bloqueados, 1=Solo activos, 2=Todos

**Cambios realizados:**

1. Base de datos:
   - Actualizados tipos de proveedor (MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS)
   - Agregado mensaje de error BUS214 para tipo invalido

2. Backend (catalogos-api):
   - Nuevo endpoint GET /suppliers/filter
   - DTOs: SupplierFilterDto, SupplierBlockInfoDto
   - Validacion de bloqueo: status=1 AND fecha actual entre valid_from y valid_to
   - Commit: a92871f

3. BFF (catalogos):
   - Actualizado api.yml con nuevo endpoint y DTOs
   - Commit: 70f32f0

**Pruebas realizadas:**
- Filtrado por tipo de proveedor (0-4)
- Filtrado por estado de bloqueo (0-2)
- Validacion de error BUS214 para tipo invalido
- Verificacion de respuesta con blockInfo para proveedores bloqueados

**Pendiente:**
- Pruebas unitarias
- Validacion de performance (< 2 seg)
- Despliegue en ambientes
```

---

## Ejemplo de Respuesta - Proveedor Bloqueado

```json
{
  "id": 8,
  "supplierNumber": "MERC002",
  "rfc": "MER020202DEF",
  "businessName": "Comercializadora Industrial Maya S.A. de C.V.",
  "supplierType": {
    "id": 1,
    "code": "MERCANCIA",
    "description": "Proveedores de mercancia ODBMS"
  },
  "paymentCondition": {
    "id": 4,
    "conditionName": "45 dias",
    "days": 45
  },
  "status": 1,
  "blocked": true,
  "blockInfo": {
    "validFrom": "2026-01-07",
    "validTo": "2026-04-07",
    "blockReason": "Incumplimiento de contrato"
  }
}
```

---

## Ejemplo de Respuesta - Error BUS214

```json
{
  "message": "No existe el tipo de proveedor [5] solicitado.",
  "code": 400
}
```

---

## URLs de Prueba

| Caso | URL |
|------|-----|
| Todos los proveedores | `GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=2` |
| Solo bloqueados | `GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=0` |
| Solo activos | `GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=1` |
| Mercancia activos | `GET /suppliers/filter?tipoProveedor=1&estatusBloqueo=1` |
| Transporte todos | `GET /suppliers/filter?tipoProveedor=2&estatusBloqueo=2` |
| Error tipo invalido | `GET /suppliers/filter?tipoProveedor=5&estatusBloqueo=1` |

---

## Archivos Modificados

### Backend (catalogos-api)
- `SupplierFilterDto.java` - Nuevo DTO
- `SupplierBlockInfoDto.java` - Nuevo DTO
- `SupplierRepository.java` - Query findByTypeFilter
- `SupplierService.java` - Metodo findByTypeAndBlockStatus
- `SupplierServiceImpl.java` - Implementacion
- `SupplierController.java` - Endpoint GET /filter
- `SupplierMapper.java` - Metodos toFilterDto, toBlockInfoDto

### Scripts SQL
- `14_STM-831_supplier_types.sql` - Tipos de proveedor
- `14_STM-831_message_BUS214.sql` - Mensaje de error

### BFF (catalogos)
- `api.yml` - OpenAPI actualizado

---

## Commits

| Proyecto | Commit | Descripcion |
|----------|--------|-------------|
| catalogos-api | `a92871f` | feat(STM-831): agregar endpoint de filtrado de proveedores por tipo y estado de bloqueo |
| bff-catalogos | `70f32f0` | feat(STM-831): agregar endpoint de filtrado de proveedores en OpenAPI |
