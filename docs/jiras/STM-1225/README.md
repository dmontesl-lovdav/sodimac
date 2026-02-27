# STM-1225: API para Catalogo de Proveedores

> **Estado:** Completado
> **Fecha inicio:** 2025-12-10
> **Fecha fin:** 2025-12-11
> **Servicio:** catalogos-api + BFF catalogos

---

## Descripcion

Implementacion de API REST para gestionar el catalogo de proveedores, incluyendo CRUD completo, catalogos auxiliares (tipos de proveedor, condiciones de pago) y consulta de centros de costo, beneficio y cuentas contables.

## Alcance

- **Backend:** `mrch.backend.somx.catalogos-api`
- **BFF:** `mrch.bff.somx.ppsomx.catalogos`

## Tareas Completadas

- [x] Modelo de datos (6 entidades: Supplier, SupplierType, PaymentCondition, CostCenter, BenefitCenter, AccountingAccount)
- [x] Script SQL `08_schema_proveedor.sql`
- [x] Endpoints CRUD proveedores (8 endpoints)
- [x] Endpoints catalogos auxiliares (2 endpoints)
- [x] Endpoints centros (10 endpoints)
- [x] Exponer en BFF (api.yml actualizado)
- [x] Coleccion Postman
- [x] Documentacion tecnica

---

## Endpoints Implementados (19 total)

### Proveedores
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/suppliers` | Lista proveedores |
| GET | `/suppliers/{id}` | Por ID |
| GET | `/suppliers/number/{num}` | Por numero |
| GET | `/suppliers/rfc/{rfc}` | Por RFC |
| POST | `/suppliers` | Crear |
| PUT | `/suppliers/{id}` | Actualizar |
| DELETE | `/suppliers/{id}` | Eliminar |

### Catalogos Auxiliares
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/suppliers/types` | Tipos de proveedor |
| GET | `/suppliers/payment-conditions` | Condiciones de pago |

### Centros
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/centers/cost` | Centros de costo |
| GET | `/centers/cost/{id}` | Por ID |
| GET | `/centers/cost/code/{code}` | Por codigo |
| GET | `/centers/cost/sap-branch/{sap}` | Por sucursal SAP |
| GET | `/centers/benefit` | Centros de beneficio |
| GET | `/centers/benefit/{id}` | Por ID |
| GET | `/centers/benefit/code/{code}` | Por codigo |
| GET | `/centers/accounting` | Cuentas contables |
| GET | `/centers/accounting/{id}` | Por ID |
| GET | `/centers/accounting/code/{code}` | Por codigo |

---

## Archivos de Documentacion

- `STM-1225_Documentacion_API_Proveedores.md` - Documentacion tecnica completa
- `STM-1225_Suppliers_API.postman_collection.json` - Coleccion Postman

## Commits Relacionados

- `8bf3b4b` - feat: Agregar API de proveedores, centros de costo, beneficio y cuentas contables
- `b3b1a6d` - feat: Agregar endpoints de proveedores y centros al OpenAPI (BFF)

---

## Notas

- Todas las tablas en schema `shared_catalogs`
- Eliminacion logica (soft-delete) con campo status
- Campos de auditoria: created_at, created_by, updated_at, updated_by

## Datos Iniciales (Script 08_schema_proveedor.sql)

| Tabla | Registros | Descripcion |
|-------|-----------|-------------|
| `supplier_type` | 3 | NAC, INT, MIX |
| `payment_condition` | 6 | Contado, 15, 30, 45, 60, 90 dias |
| `cost_center` | 5 | CDMX, Guadalajara, Monterrey, Puebla, Queretaro |
| `benefit_center` | 5 | Retail, Mayoreo, E-Commerce, Servicios, Corporativo |
| `accounting_account` | 6 | Gastos Operativos, Inventarios, Ventas, etc. |
| `supplier` | 0 | Se pobla mediante API |
