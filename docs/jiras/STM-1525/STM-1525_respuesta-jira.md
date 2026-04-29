# Implementación STM-1525 — Filtro de seguridad en Catálogo Proveedor

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el **Catálogo Proveedor** dentro de `util-api`. El filtrado usa el atributo ATR002 (TipoProveedor) que se mapea al campo `supplier.supplier_type_id` mediante `catalog_detail.external_key`.

---

## Flujo implementado

```
Frontend → JWT → GCP Cloud Endpoints
    → BFF (extrae userKey, consulta util-api, inyecta headers)
        → util-api GET /api/suppliers (filtra por tipo de proveedor)
```

Headers usados:
- `x-user-types` — valores del atributo ATR002 (TipoProveedor) → ej. `TPR001,TPR002`

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `util-api/src/entities/Supplier.entity.ts` | **NUEVO** — entidad TypeORM para `shared_catalogs.supplier` |
| `util-api/src/repositories/supplier.repo.ts` | **NUEVO** — query con filtro por `supplier_type_id` resolviendo `catalog_detail.external_key` |
| `util-api/src/services/supplier.service.ts` | **NUEVO** — parseo de `x-user-types`, lógica WRN7029 |
| `util-api/src/controllers/supplier.controller.ts` | **NUEVO** — `GET /api/suppliers` con filtros opcionales |
| `util-api/src/routes/supplier.routes.ts` | **NUEVO** — router |
| `util-api/src/routes/index.ts` | Registra `router.use("/suppliers", supplierRouter)` |
| `util-api/src/config/typeorm-datasource.ts` | Registra entidad `Supplier` en ENTITIES |

---

## Mecanismo de filtrado

ATR002 contiene keys como `TPR001`, `TPR002`. El repositorio resuelve a `supplier_type_id` vía `catalog_detail.external_key`:

```sql
SELECT supplier_type_id FROM shared_catalogs.catalog_detail
WHERE key IN ('TPR001', 'TPR002')
-- external_key = '1', '2' → supplier_type_id = 1, 2
```

Luego filtra:
```sql
SELECT * FROM shared_catalogs.supplier WHERE supplier_type_id IN (1, 2)
```

---

## Reglas de negocio aplicadas

| Condición del header `x-user-types` | Comportamiento |
|--------------------------------------|---------------|
| Header ausente | Sin filtro — devuelve todo (22 proveedores) |
| `-1` | Sin filtro — acceso total |
| `TPR001` | Solo proveedores con supplier_type_id = 1 |
| `TPR001,TPR002` | Proveedores con supplier_type_id = 1 ó 2 (OR lógico) |
| Vacío `""` | Retorna WRN7029 |

---

## Endpoint disponible

```
GET /api/suppliers
  ?supplierNumber=MERC001   (búsqueda parcial)
  &businessName=Distribu    (búsqueda parcial ILIKE)
  &status=1                 (filtro por estatus)
  &page=1
  &pageSize=20
```

---

## Pruebas ejecutadas (directo util-api — puerto 3712)

| Header `x-user-types` | Resultado |
|-----------------------|-----------|
| (ausente) | 22 proveedores totales ✅ |
| `TPR001` | 11 proveedores tipo 1 ✅ |
| `TPR001,TPR002` | 14 proveedores tipos 1 y 2 ✅ |
| `-1` | 22 proveedores (acceso total) ✅ |
| `""` (vacío) | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con TipoProveedor configurado → solo ve sus proveedores
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples tipos → OR lógico
- [x] Usuario sin atributos → WRN7029
