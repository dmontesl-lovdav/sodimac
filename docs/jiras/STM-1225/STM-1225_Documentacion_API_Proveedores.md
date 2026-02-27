# STM-1225 - Implementacion de API para gestionar el catalogo de proveedores

## Resumen

Implementacion completa de la API REST para gestion del catalogo de proveedores en el servicio `catalogos-api`. Incluye CRUD de proveedores, catalogos auxiliares (tipos de proveedor, condiciones de pago) y consulta de centros de costo, beneficio y cuentas contables.

## Componentes Implementados

### 1. Modelo de Datos (Entidades JPA)

| Entidad | Tabla | Descripcion |
|---------|-------|-------------|
| Supplier | `shared_catalogs.supplier` | Catalogo maestro de proveedores |
| SupplierType | `shared_catalogs.supplier_type` | Tipos de proveedor (NAC, INT, MIX) |
| PaymentCondition | `shared_catalogs.payment_condition` | Condiciones de pago |
| CostCenter | `shared_catalogs.cost_center` | Centros de costo |
| BenefitCenter | `shared_catalogs.benefit_center` | Centros de beneficio |
| AccountingAccount | `shared_catalogs.accounting_account` | Cuentas contables |

### 2. Endpoints Implementados

#### Suppliers (Proveedores)

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/suppliers` | Lista todos los proveedores activos |
| GET | `/suppliers?status={0\|1}` | Lista proveedores por estado |
| GET | `/suppliers/{id}` | Obtiene proveedor por ID |
| GET | `/suppliers/number/{supplierNumber}` | Obtiene proveedor por numero |
| GET | `/suppliers/rfc/{rfc}` | Obtiene proveedor por RFC |
| POST | `/suppliers` | Crea nuevo proveedor |
| PUT | `/suppliers/{id}` | Actualiza proveedor existente |
| DELETE | `/suppliers/{id}` | Elimina (desactiva) proveedor |

#### Catalogos Auxiliares

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/suppliers/types` | Lista tipos de proveedor |
| GET | `/suppliers/payment-conditions` | Lista condiciones de pago |

#### Centros de Costo

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/centers/cost` | Lista centros de costo |
| GET | `/centers/cost/{id}` | Obtiene por ID |
| GET | `/centers/cost/code/{code}` | Obtiene por codigo |
| GET | `/centers/cost/sap-branch/{sapBranch}` | Lista por sucursal SAP |

#### Centros de Beneficio

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/centers/benefit` | Lista centros de beneficio |
| GET | `/centers/benefit/{id}` | Obtiene por ID |
| GET | `/centers/benefit/code/{code}` | Obtiene por codigo |

#### Cuentas Contables

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/centers/accounting` | Lista cuentas contables |
| GET | `/centers/accounting/{id}` | Obtiene por ID |
| GET | `/centers/accounting/code/{code}` | Obtiene por codigo |

### 3. Estructura de Archivos

```
src/main/java/com/sodimac/catman/api/
├── controller/
│   ├── SupplierController.java
│   └── CenterController.java
├── model/
│   ├── entity/
│   │   ├── Supplier.java
│   │   ├── SupplierType.java
│   │   ├── PaymentCondition.java
│   │   ├── CostCenter.java
│   │   ├── BenefitCenter.java
│   │   └── AccountingAccount.java
│   └── dto/
│       ├── SupplierDto.java
│       ├── SupplierCreateDto.java
│       ├── SupplierUpdateDto.java
│       ├── SupplierTypeDto.java
│       ├── PaymentConditionDto.java
│       ├── CostCenterDto.java
│       ├── BenefitCenterDto.java
│       └── AccountingAccountDto.java
├── repository/
│   ├── SupplierRepository.java
│   ├── SupplierTypeRepository.java
│   ├── PaymentConditionRepository.java
│   ├── CostCenterRepository.java
│   ├── BenefitCenterRepository.java
│   └── AccountingAccountRepository.java
├── service/
│   ├── SupplierService.java
│   ├── SupplierServiceImpl.java
│   ├── CenterService.java
│   └── CenterServiceImpl.java
└── mapper/
    ├── SupplierMapper.java
    └── CenterMapper.java

src/main/resources/db/
└── 08_schema_proveedor.sql
```

### 4. Scripts SQL

**Archivo:** `08_schema_proveedor.sql`

Crea las siguientes tablas:
- `supplier_type` - Tipos de proveedor
- `payment_condition` - Condiciones de pago
- `supplier` - Proveedores
- `supplier_role` - Roles por proveedor
- `cost_center` - Centros de costo
- `benefit_center` - Centros de beneficio
- `accounting_account` - Cuentas contables

**Datos iniciales:**
- 3 tipos de proveedor: NAC, INT, MIX
- 6 condiciones de pago: Contado, 15, 30, 45, 60, 90 dias

## Ejemplos de Uso

### Crear proveedor

```bash
curl -X POST "http://localhost:8083/suppliers" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: usuario-test" \
  -d '{
    "supplierNumber": "PROV001",
    "rfc": "ABC123456789",
    "businessName": "Proveedor Test S.A. de C.V.",
    "supplierTypeId": 1,
    "logo": "https://example.com/logo.png",
    "paymentConditionId": 3
  }'
```

**Respuesta (201 Created):**
```json
{
  "id": 1,
  "supplierNumber": "PROV001",
  "rfc": "ABC123456789",
  "businessName": "Proveedor Test S.A. de C.V.",
  "supplierType": {
    "id": 1,
    "code": "NAC",
    "description": "Proveedor Nacional"
  },
  "logo": "https://example.com/logo.png",
  "paymentCondition": {
    "id": 3,
    "conditionName": "30 dias",
    "days": 30
  },
  "status": 1
}
```

### Actualizar proveedor

```bash
curl -X PUT "http://localhost:8083/suppliers/1" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: usuario-test" \
  -d '{
    "businessName": "Proveedor Actualizado S.A. de C.V.",
    "paymentConditionId": 5
  }'
```

### Eliminar proveedor

```bash
curl -X DELETE "http://localhost:8083/suppliers/1" \
  -H "X-User-Id: usuario-test"
```

**Respuesta:** 204 No Content

### Consultar tipos de proveedor

```bash
curl -X GET "http://localhost:8083/suppliers/types"
```

**Respuesta:**
```json
[
  {"id": 1, "code": "NAC", "description": "Proveedor Nacional"},
  {"id": 2, "code": "INT", "description": "Proveedor Internacional"},
  {"id": 3, "code": "MIX", "description": "Proveedor Mixto"}
]
```

### Consultar condiciones de pago

```bash
curl -X GET "http://localhost:8083/suppliers/payment-conditions"
```

**Respuesta:**
```json
[
  {"id": 1, "conditionName": "Contado", "days": 0},
  {"id": 2, "conditionName": "15 dias", "days": 15},
  {"id": 3, "conditionName": "30 dias", "days": 30},
  {"id": 4, "conditionName": "45 dias", "days": 45},
  {"id": 5, "conditionName": "60 dias", "days": 60},
  {"id": 6, "conditionName": "90 dias", "days": 90}
]
```

## Validaciones

### Proveedor (POST/PUT)
- `supplierNumber`: Requerido, unico
- `rfc`: Requerido, 12-13 caracteres
- `businessName`: Requerido

### Headers
- `X-User-Id`: Usuario que realiza la operacion (para auditoria)

## Codigos de Respuesta

| Codigo | Descripcion |
|--------|-------------|
| 200 | Operacion exitosa |
| 201 | Recurso creado |
| 204 | Eliminado sin contenido |
| 400 | Datos invalidos |
| 404 | Recurso no encontrado |
| 500 | Error interno |

## Coleccion Postman

Disponible en: `docs/STM-1225_Suppliers_API.postman_collection.json`

## Dependencias

- Spring Boot 3.4.2
- Spring Data JPA
- PostgreSQL
- Lombok
- MapStruct (mapeo manual)

## Notas de Implementacion

1. **Eliminacion logica**: DELETE realiza soft-delete (status=0)
2. **Auditoria**: Campos created_at, created_by, updated_at, updated_by
3. **Filtro por defecto**: Endpoints GET retornan solo registros activos (status=1)
4. **Esquema**: Todas las tablas en schema `shared_catalogs`
