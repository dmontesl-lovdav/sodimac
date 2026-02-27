# STM-1236: Servicio de Catalogo de Proveedores en BFF

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1236

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | Completado |
| **Modulo** | BFF Catalogos |
| **BFF** | mrch.bff.somx.ppsomx.catalogos (Puerto 3000) |
| **Backend** | mrch.backend.somx.catalogos-api (Puerto 8083) |
| **Fecha** | 2025-12-22 |
| **Probado** | Si |

---

## Descripcion

Implementar el servicio del catalogo de proveedores en el BFF de Catalogos para tener comunicacion con el Front.

> **Nota**: El backend fue implementado en el JIRA **STM-1225**. Ver documentacion completa en [STM-1225](../STM-1225/README.md).

---

## Endpoints Disponibles (9 endpoints)

| Metodo | Endpoint | Descripcion | Estado |
|--------|----------|-------------|--------|
| GET | `/suppliers` | Listar todos los proveedores | OK |
| GET | `/suppliers?status=1` | Listar proveedores por estado | OK |
| GET | `/suppliers/{id}` | Obtener proveedor por ID | OK |
| GET | `/suppliers/number/{supplierNumber}` | Obtener proveedor por numero | OK |
| GET | `/suppliers/rfc/{rfc}` | Obtener proveedor por RFC | OK |
| POST | `/suppliers` | Crear proveedor | OK |
| PUT | `/suppliers/{id}` | Actualizar proveedor | OK |
| DELETE | `/suppliers/{id}` | Eliminar proveedor (soft-delete) | OK |
| GET | `/suppliers/types` | Tipos de proveedor | OK |
| GET | `/suppliers/payment-conditions` | Condiciones de pago | OK |

---

## Como Probar (via BFF - Puerto 3000)

### Listar proveedores
```bash
curl http://localhost:3000/suppliers
```

### Listar proveedores activos
```bash
curl "http://localhost:3000/suppliers?status=1"
```

### Obtener proveedor por ID
```bash
curl http://localhost:3000/suppliers/1
```

### Obtener proveedor por numero
```bash
curl http://localhost:3000/suppliers/number/PROV001
```

### Obtener proveedor por RFC
```bash
curl http://localhost:3000/suppliers/rfc/ABC123456789
```

### Tipos de proveedor
```bash
curl http://localhost:3000/suppliers/types
```

### Condiciones de pago
```bash
curl http://localhost:3000/suppliers/payment-conditions
```

### Crear proveedor
```bash
curl -X POST http://localhost:3000/suppliers \
  -H "Content-Type: application/json" \
  -d '{
    "supplierNumber": "PROV999",
    "rfc": "RFC123456789",
    "businessName": "Nuevo Proveedor S.A.",
    "supplierTypeId": 1,
    "paymentConditionId": 3
  }'
```

### Actualizar proveedor
```bash
curl -X PUT http://localhost:3000/suppliers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Nombre Actualizado S.A."
  }'
```

### Eliminar proveedor
```bash
curl -X DELETE http://localhost:3000/suppliers/1
```

---

## Resultados de Pruebas

Fecha de prueba: 2025-12-22

| Endpoint | Resultado |
|----------|-----------|
| GET /suppliers | OK - Retorna lista de proveedores |
| GET /suppliers/{id} | OK - Retorna proveedor por ID |
| GET /suppliers/number/{supplierNumber} | OK - Retorna proveedor por numero |
| GET /suppliers/rfc/{rfc} | OK - Retorna proveedor por RFC |
| GET /suppliers/types | OK - Retorna 3 tipos de proveedor |
| GET /suppliers/payment-conditions | OK - Retorna condiciones de pago |
| POST /suppliers | OK - Crea proveedor |
| PUT /suppliers/{id} | OK - Actualiza proveedor |
| DELETE /suppliers/{id} | OK - Elimina proveedor (soft-delete) |

---

## Modelo de Datos

### SupplierDto (Respuesta)
```json
{
  "id": 1,
  "supplierNumber": "PROV001",
  "rfc": "ABC123456789",
  "businessName": "Proveedor ABC S.A. de C.V.",
  "supplierType": {
    "id": 1,
    "code": "NAC",
    "description": "Proveedor Nacional"
  },
  "logo": null,
  "paymentCondition": {
    "id": 3,
    "conditionName": "30 dias",
    "days": 30
  },
  "status": 1
}
```

### SupplierCreateDto (Request POST)
```json
{
  "supplierNumber": "PROV999",
  "rfc": "RFC123456789",
  "businessName": "Nuevo Proveedor S.A.",
  "supplierTypeId": 1,
  "paymentConditionId": 3,
  "logo": null
}
```

### SupplierTypeDto
```json
{
  "id": 1,
  "code": "NAC",
  "description": "Proveedor Nacional"
}
```

### PaymentConditionDto
```json
{
  "id": 3,
  "conditionName": "30 dias",
  "days": 30
}
```

---

## Coleccion Postman

Archivo: `postman/STM-1236 - Suppliers BFF.postman_collection.json`

Variable: `base_url = http://localhost:3000`

---

## Script SQL

Archivo: `docs/jiras/STM-1236/scripts/STM-1236_queries.sql`

---

## Tablas Involucradas

| Tabla | Esquema | Descripcion |
|-------|---------|-------------|
| `supplier` | shared_catalogs | Catalogo maestro de proveedores |
| `supplier_type` | shared_catalogs | Tipos de proveedor (NAC, INT, MIX) |
| `payment_condition` | shared_catalogs | Condiciones de pago |

---

## Documentacion Relacionada

| JIRA | Descripcion |
|------|-------------|
| [STM-1225](../STM-1225/README.md) | Implementacion backend de proveedores |
| [STM-1224](../STM-1224/README.md) | Bloqueo de proveedores |
| [STM-1252](../STM-1252/README.md) | Bloqueo de proveedores en BFF |

---

## Archivos del Proyecto

**Backend (catalogos-api):**
- Controller: `SupplierController.java`
- Service: `SupplierService.java`, `SupplierServiceImpl.java`
- Repository: `SupplierRepository.java`
- Entity: `Supplier.java`, `SupplierType.java`, `PaymentCondition.java`
- DTOs: `SupplierDto.java`, `SupplierCreateDto.java`, `SupplierUpdateDto.java`

**BFF (ppsomx.catalogos):**
- OpenAPI: `api.yml` (lineas 385-647)

---

## Notas Tecnicas

- Backend: catalogos-api (Puerto 8083)
- BFF: ppsomx.catalogos (Puerto 3000)
- Soft-delete con campo status (1=Activo, 0=Inactivo)
- Validacion de RFC y numero de proveedor unicos
