# Implementación STM-1525 — util-api: endpoint user-attributes-by-key + filtro Catálogo Proveedor

## Resumen

Se implementaron en `util-api` dos contribuciones:

1. **Endpoint nuevo `GET /api/security/user-attributes-by-key/{sub}`** — devuelve los atributos (ATR001/ATR002/ATR004) de un usuario por su `sub`. Es consumido por `fiscal-api` y `finanzas-api` para resolver el contexto de seguridad desde el JWT (epic STM-1403).

2. **Endpoint `GET /api/suppliers`** — Catálogo de Proveedor con filtro por TipoProveedor (ATR002) vía header `x-user-types`, resolviendo `catalog_detail.external_key` → `supplier.supplier_type_id`.

---

## Flujo implementado (alineado con STM-1403)

```
Frontend → JWT → GCP Cloud Endpoints
    → BFF (proxy transparente, sin lógica de seguridad)
        → backend (fiscal-api / finanzas-api):
              1. SecurityContextFilter / security.middleware decodifica JWT (sub)
              2. consulta util-api GET /api/security/user-attributes-by-key/{sub}
              3. recibe attributes[] con ATR001/ATR002/ATR004
              4. inyecta headers seguros x-user-vendors/types/groups (anti-spoof)
              5. el controller filtra usando esos headers
```

Cache: 5 min en memoria por `sub`.

---

## Cambios realizados

### Endpoint user-attributes-by-key (eje STM-1403)

| Archivo | Cambio |
|---------|--------|
| `util-api/src/entities/SecurityRelations.entity.ts` | **NUEVO** — entidades `UserData`, `UserAttribute` (schema `core_security`) |
| `util-api/src/repositories/security.repo.ts` | **NUEVO** — query JOIN user_data + user_attribute + catalog_detail |
| `util-api/src/services/security.service.ts` | **NUEVO** — resolución de atributos por `sub` |
| `util-api/src/controllers/security.controller.ts` | **NUEVO** — `GET /api/security/user-attributes-by-key/:userKey` |
| `util-api/src/routes/security.routes.ts` | **NUEVO** — router |
| `util-api/src/constants/security-shared-catalog.ts` | **NUEVO** — constantes de catálogos (CatAtributo, CatTipoProveedor) |
| `util-api/src/docs/components/security.ts` | **NUEVO** — Swagger schema |
| `util-api/src/docs/paths/security.ts` | **NUEVO** — Swagger paths |

### Endpoint suppliers (catálogo proveedor)

| Archivo | Cambio |
|---------|--------|
| `util-api/src/entities/Supplier.entity.ts` | **NUEVO** — entidad TypeORM para `shared_catalogs.supplier` |
| `util-api/src/repositories/supplier.repo.ts` | **NUEVO** — query con filtro por `supplier_type_id` resolviendo `catalog_detail.external_key` |
| `util-api/src/services/supplier.service.ts` | **NUEVO** — parseo de `x-user-types`, lógica WRN7029 |
| `util-api/src/controllers/supplier.controller.ts` | **NUEVO** — `GET /api/suppliers` con filtros opcionales |
| `util-api/src/routes/supplier.routes.ts` | **NUEVO** — router |
| `util-api/src/routes/index.ts` | Registra `/security` y `/suppliers` |
| `util-api/src/config/typeorm-datasource.ts` | Registra entidades nuevas |

### Seeds SQL

| Archivo | Propósito |
|---------|-----------|
| `docs/jiras/STM-1525/STM-1525_cat_tipo_proveedor_external_key.sql` | UPDATE de `external_key` para TPR001-TPR004 → supplier_type_id 1-4 |
| `docs/jiras/STM-1525/STM-1525_seed_atr002_user_attributes.sql` | INSERT user_attribute ATR002=TPR001 para USR_ANA (sb000002) |

---

## Endpoints disponibles

### 1. GET /api/security/user-attributes-by-key/:userKey

Devuelve los atributos activos de un usuario por su `sub`.

```
GET /api/security/user-attributes-by-key/sb000001
```

Response:
```json
{
  "success": true,
  "data": {
    "userDataId": 1,
    "sub": "sb000001",
    "preferredUsername": "USR_FERNANDO",
    "email": "fernando.perez@example.com",
    "attributes": [
      { "typeKey": "ATR001", "valueKey": "11111" }
    ]
  }
}
```

Errores:
- `404` — usuario no existe / inactivo
- `400` — userKey vacío

### 2. GET /api/suppliers

Catálogo proveedor con filtro por TipoProveedor:

```
GET /api/suppliers
  ?supplierNumber=MERC001
  &businessName=Distribu
  &status=1
  &page=1
  &pageSize=20

Header: x-user-types: TPR001  (o TPR001,TPR002 — o -1 acceso total)
```

---

## Mapeo ATR002 → supplier_type_id

ATR002 contiene keys (`TPR001`, `TPR002`...). El repositorio las resuelve a `supplier_type_id` vía `catalog_detail.external_key`:

```sql
SELECT external_key FROM shared_catalogs.catalog_detail
WHERE header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code='CatTipoProveedor')
  AND key IN ('TPR001', 'TPR002');
-- external_key = '1', '2' → supplier_type_id = 1, 2
```

Luego filtra:
```sql
SELECT * FROM shared_catalogs.supplier WHERE supplier_type_id IN (1, 2)
```

---

## Reglas de negocio aplicadas

### user-attributes-by-key

| Caso | Comportamiento |
|------|---------------|
| `sub` válido y activo | 200 OK con attributes |
| `sub` no existe | 404 con mensaje |
| `sub` vacío | 400 — userKey obligatorio |

### /suppliers

| Header `x-user-types` | Comportamiento |
|------------------------|---------------|
| Header ausente | Sin filtro — todos los proveedores |
| `-1` | Sin filtro — acceso total |
| `TPR001` | Solo `supplier_type_id = 1` |
| `TPR001,TPR002` | `supplier_type_id IN (1, 2)` (OR lógico) |
| Vacío `""` | WRN7029 — usuario sin atributos |

---

## Pruebas ejecutadas (util-api directo, puerto 3712)

### user-attributes-by-key

| Caso | sub | Resultado |
|------|-----|-----------|
| Fernando | sb000001 | `attributes: [{ATR001, 11111}]` ✅ |
| Jose | sb000003 | `attributes: [{ATR001, 22222}, {ATR001, 11111}]` ✅ |
| Iván | sb000005 | `attributes: [{ATR001, -1}]` ✅ |
| Ana | sb000002 | `attributes: [{ATR002, TPR001}]` (tras seed) ✅ |
| Inexistente | noexiste | HTTP 404 ✅ |
| Vacío | %20 | HTTP 400 ✅ |

### /suppliers

| Header `x-user-types` | Resultado |
|-----------------------|-----------|
| (ausente) | 22 proveedores totales ✅ |
| `TPR001` | 11 proveedores tipo 1 ✅ |
| `TPR001,TPR002` | 14 proveedores tipos 1 y 2 ✅ |
| `-1` | 22 proveedores (acceso total) ✅ |
| `""` (vacío) | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Endpoint expone atributos de usuario por `sub` (consumible por backends para STM-1403)
- [x] Cache implícito en backends consumidores (TTL 5 min)
- [x] Devuelve 404 si usuario no existe / inactivo
- [x] Catálogo Proveedor con filtro por TipoProveedor (ATR002)
- [x] Usuario con valor `-1` → acceso total
- [x] Usuario con múltiples tipos → OR lógico
- [x] Usuario sin atributos → WRN7029
