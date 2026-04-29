# Análisis Técnico - Epic STM-1403: Filtro de Seguridad por Atributo de Usuario

**Jiras:** STM-321, STM-323, STM-1461, STM-1474, STM-1524, STM-1525
**Epic:** [STM-1403](../STM-1403/MXSTM-1403.md) — PP_FBC: Módulo de Autorización
**Última actualización:** 2026-04-28
**Identity Provider:** Keycloak / FBC (JWT emitido por FBC, validado en BFF)
**Schema seguridad:** `core_security` — DDL real en [sesiones/security 1.sql](../../sesiones/security%201.sql)

---

## 1. Mapeo de Módulos → Proyectos

| Jira | Módulo | Proyecto | Controlador | Endpoint | Tabla BD |
|------|--------|----------|-------------|----------|----------|
| STM-323 | Facturas | `fiscal-api` :8082 | `InvoiceController.java` | `POST /invoices/search` | `tenant_fiscal.invoice` |
| STM-1474 | Recepción | `fiscal-api` :8082 | `PaymentRegistrationController.java` | `GET /fiscal/complementos-pago/buscar` | `tenant_fiscal.addendum` |
| STM-1461 | Carta Porte | `finanzas-api` (Node.js) | `shippingGuide.controller.ts` | `GET /shipping-guide` | `tenant_finance.shipping_guide` |
| STM-321 | Three Way Match | `finanzas-api` (Node.js) | `threeWayMatch.controller.ts` | `GET /three-way-match` | `tenant_finance.three_way_match` |
| STM-1524 | Estado de Cuenta | `finanzas-api` (Node.js) | `accountStatement.controller.ts` | `GET /account-statement` | `tenant_finance.account_statement` |
| STM-1525 | Catálogo Proveedor | `catalogos-api` :8083 | `SupplierController.java` | `GET /suppliers` | `shared_catalogs.supplier` |

---

## 2. Campo de filtro por tabla

| Jira | Tabla principal | Campo Proveedor | Campo TipoProveedor | GrupoProveedor |
|------|----------------|-----------------|---------------------|----------------|
| STM-323 | `tenant_fiscal.addendum` | `supplier_number` (numeric) | join `supplier.supplier_type_id` via `external_key` | ⚠️ sin campo aún |
| STM-1474 | `tenant_fiscal.addendum` | `supplier_number` (numeric) | join `supplier.supplier_type_id` via `external_key` | ⚠️ sin campo aún |
| STM-1461 | `tenant_finance.shipping_guide` | `vendor_number` (integer) | join `supplier.supplier_type_id` via `external_key` | ⚠️ sin campo aún |
| STM-321 | `tenant_finance.three_way_match` | `vendor_number` (integer) | join `supplier.supplier_type_id` via `external_key` | ⚠️ sin campo aún |
| STM-1524 | `tenant_finance.account_statement` | `vendor_number` (bigint) | join `supplier.supplier_type_id` via `external_key` | ⚠️ sin campo aún |
| STM-1525 | `shared_catalogs.supplier` | `supplier_number` (varchar) — no aplica este Jira | `supplier_type_id` directo | ⚠️ sin campo aún |

> `GrupoProveedor` no existe como columna en ninguna tabla transaccional. Pendiente de diseño.

---

## 3. Catálogos de seguridad en `shared_catalogs`

| id | code | Contenido |
|----|------|-----------|
| 22 | CatTipoProveedor | TPR001=Mercancia, TPR002=Transporte, TPR003=Indirectos, TPR004=Servicios |
| 58 | CatPerfil | PER001-PER009 (Transporte, Mercancia, Servicios, Indirecto, Finanzas, Comercial, Auditoria, Admon Funcional, Admon) |
| 59 | CatModulo | MOD001-MOD006 (Catalogos, Finanzas, Fiscal, Utilerias, Auditoria, Control acceso) |
| 60 | CatAplicativo | APL001-APL016 con `attributes.idModulo` |
| 61 | CatEvento | EVT001-EVT007 con `attributes.idAplicativo` |
| 62 | CatAtributo | ATR001=Proveedor, ATR002=TipoProveedor, ATR003=Empresa, ATR004=GrupoProveedor, ATR005=TipoRebate |
| 63 | CatRol | ROL001-004 externos (proveedores), ROL005-010 internos |
| 64 | CatPermiso | PRM001-PRM008 (Consulta, Registro, Actualización, Borrado, Cancelación, Autorización, Aprobación, Rechazo) |
| 65 | CatProveedor | Vendors reales: '1', '1001', '1002', '1003', '-1' (acceso total) |

**Mapeo CatTipoProveedor → supplier_type** (via `catalog_detail.external_key`):
```
TPR001 → external_key='1' → supplier_type.id=1 (MERCANCIA)
TPR002 → external_key='2' → supplier_type.id=2 (TRANSPORTE)
TPR003 → external_key='3' → supplier_type.id=3 (INDIRECTOS)
TPR004 → external_key='4' → supplier_type.id=4 (SERVICIOS)
```

**Mapeo Aplicativos → Jiras:**

| APL | Aplicativo | Jira |
|-----|-----------|------|
| APL001 | Catálogo de proveedores | STM-1525 |
| APL003 | Recepciones | STM-1474 |
| APL004 | Carta Porte | STM-1461 |
| APL006 | Estado de cuenta | STM-1524 |
| APL007 | Three way match | STM-321 |
| APL009 | Gestión de facturas | STM-323 |
| APL011 | Gestión de complemento de pago | STM-1474 |

---

## 4. Arquitectura de Seguridad

### Flujo confirmado (STM-1403 AC-03, AC-04, AC-07)

```
FBC → JWT [sub, role, providerId, country, providerType]
    ↓
BFF — valida JWT, extrae sub, pasa X-User-Sub header
    ↓
Backend:
  1. user_data WHERE sub = :sub  → user_data_id
  2. user_attribute WHERE user_data_id + ATRxxx → attribute_value_key
  3. WHERE vendor_number IN (valores)  /  WHERE supplier_type_id IN (valores)
```

**Regla admin (AC-07):** ROL009/ROL010 (`tipoRol: "Interno"` en attributes JSON) → sin filtro, ven todos.

**Valor `-1`:** si `attribute_value_key = '-1'` → sin filtro para ese atributo (acceso total).

**Sin atributos:** retornar `WRN7029`.

### fiscal-api — JWT ya implementado

- `JwtTokenInterceptor.java` → parsea JWT, crea `Session`
- `Session.java` → actualmente: name, email, groups. Falta: sub del FBC
- `GroupValidator.java` → `@Aspect` valida roles via `@RequireRole`

### finanzas-api y catalogos-api — SIN JWT

Reciben header `X-User-Sub` del BFF. Backend resuelve `user_data_id` y aplica filtro.
`catalogos-api` ya usa `@RequestHeader("X-User-Id")` para auditoría (no para filtro).

---

## 5. Modelo `core_security` — Tablas y Query

### Tablas (DDL: [sesiones/security 1.sql](../../sesiones/security%201.sql))

```
user_data          → sub (Keycloak), email, preferred_username
user_attribute     → user_data_id + attribute_type_id (ATRxxx) + attribute_value_id (valor en catalog_detail)
role_user          → user_data_id + catalog_detail_role_id
role_provider      → catalog_detail_role_id + catalog_detail_provider_id
role_permission    → catalog_detail_role_id + catalog_detail_permission_id
profile_user       → user_data_id + catalog_detail_profile_id
profile_module     → catalog_detail_profile_id + catalog_detail_module_id
module_process     → catalog_detail_module_id + catalog_detail_process_id
profile_module_process → catalog_detail_profile_id + module_process_id
```

### `user_attribute` — estructura corregida (security 1.sql)

```sql
user_attribute_id                 SERIAL PK
user_data_id                      FK → user_data
catalog_detail_attribute_type_id  FK → catalog_detail (ATR001, ATR002...)
catalog_detail_attribute_value_id FK → catalog_detail (TPR001, '1001', '-1'...)
UNIQUE (user_data_id, attribute_type_id, attribute_value_id)  -- multi-valor OK
```

### Query de Fer — obtener atributos de un usuario

```sql
SELECT
    ua.user_data_id,
    t.id   AS attribute_type_id,
    t.key  AS attribute_type_key,   -- 'ATR001', 'ATR002', etc.
    v.id   AS attribute_value_id,
    v.key  AS attribute_value_key   -- '1001', 'TPR001', '-1', etc.
FROM core_security.user_attribute ua
JOIN shared_catalogs.catalog_detail t ON t.id = ua.catalog_detail_attribute_type_id
JOIN shared_catalogs.catalog_detail v ON v.id = ua.catalog_detail_attribute_value_id
WHERE ua.user_data_id = :userId
  AND ua.status = 1;
```

---

## 6. Queries de filtrado validadas localmente

### Proveedor (ATR001) — finanzas-api

```sql
-- Aplicar en: three_way_match, account_statement, shipping_guide
WHERE vendor_number::text IN (
    SELECT v.key
    FROM core_security.user_attribute ua
    JOIN shared_catalogs.catalog_detail t ON t.id = ua.catalog_detail_attribute_type_id
    JOIN shared_catalogs.catalog_detail v ON v.id = ua.catalog_detail_attribute_value_id
    WHERE ua.user_data_id = :userId
      AND t.key = 'ATR001'
      AND ua.status = 1
      AND v.key != '-1'
)
-- ANTES de aplicar: verificar si existe fila con v.key='-1' → si sí, sin filtro
```

### Proveedor (ATR001) — fiscal-api (addendum)

```sql
WHERE addendum.supplier_number::text IN (
    SELECT v.key FROM core_security.user_attribute ua
    JOIN shared_catalogs.catalog_detail t ON t.id = ua.catalog_detail_attribute_type_id
    JOIN shared_catalogs.catalog_detail v ON v.id = ua.catalog_detail_attribute_value_id
    WHERE ua.user_data_id = :userId AND t.key = 'ATR001' AND ua.status = 1 AND v.key != '-1'
)
```

### TipoProveedor (ATR002) — catalogos-api / supplier

```sql
WHERE supplier.supplier_type_id IN (
    SELECT CAST(v.external_key AS INTEGER)
    FROM core_security.user_attribute ua
    JOIN shared_catalogs.catalog_detail t ON t.id = ua.catalog_detail_attribute_type_id
    JOIN shared_catalogs.catalog_detail v ON v.id = ua.catalog_detail_attribute_value_id
    WHERE ua.user_data_id = :userId AND t.key = 'ATR002' AND ua.status = 1 AND v.key != '-1'
)
-- Requiere: catalog_detail.external_key de CatTipoProveedor = supplier_type.id
```

### Datos de prueba locales (b2b_portal)

| user_data_id | Usuario | Atributo | Valor | Resultado |
|-------------|---------|----------|-------|-----------|
| 1 | Fernando | ATR001 Proveedor | 1001 | Solo registros vendor=1001 |
| 2 | Ana | ATR002 TipoProveedor | TPR001 | Solo proveedores Mercancia |
| 3 | Jose | ATR001 Proveedor | 1001 + 1002 | Registros vendor=1001 y 1002 |
| 5 | Ivan | ATR001 Proveedor | -1 | Sin filtro, todos |

---

## 7. Estrategia de implementación por proyecto

### fiscal-api (STM-323, STM-1474)

1. `JwtTokenInterceptor.java`: extraer `sub` del JWT FBC → resolver `user_data_id`
2. `Session.java`: agregar `List<String> allowedVendors`, `List<Integer> allowedSupplierTypeIds`
3. Poblar `Session` consultando `core_security.user_attribute` con `user_data_id`
4. `InvoiceServiceImpl` / servicio recepciones: aplicar filtro en query si `session.isProveedor()`

### finanzas-api (STM-321, STM-1461, STM-1524)

1. Middleware `securityContext.middleware.ts`: lee `X-User-Sub`, resuelve `user_data_id`, consulta `user_attribute`, adjunta lista a `req.security`
2. `threeWayMatchQuery.service.ts`, `accountStatement.service.ts`, `shippingGuide.service.ts`: leer `req.security.allowedVendors` y aplicar en query (no desde query param)
3. Repos: agregar parámetro `allowedVendorNumbers` al `findByFilters`

### catalogos-api (STM-1525)

1. `GET /suppliers`: leer `X-User-Sub`, resolver `user_data_id`
2. Filtrar por `supplier_type_id` (ATR002) y `group` (ATR004, pendiente columna en supplier)
3. Sin filtro ATR001 Proveedor en este Jira — solo TipoProveedor y GrupoProveedor

---

## 8. Dudas pendientes

### Resueltas ✅

- ~~JWT vs BD~~: `user_attribute` con `catalog_detail_attribute_value_id` (Fer, security 1.sql)
- ~~BFF vs interceptor~~: BFF pasa `X-User-Sub`, backend resuelve en `user_data`
- ~~Admin sin filtro~~: `tipoRol: "Interno"` en `attributes` JSON del CatRol (AC-07)
- ~~OR lógico~~: UNIQUE(user_data_id, type_id, value_id) → múltiples filas = OR (STM-1525 R6)
- ~~STM-321 endpoint~~: `GET /three-way-match` finanzas-api ✅
- ~~STM-1524 endpoint~~: `GET /account-statement` finanzas-api ✅
- ~~Tabla `user_attribute` sin value~~: corregido en security 1.sql con `catalog_detail_attribute_value_id`

### Pendientes

1. **GrupoProveedor campo BD**: ninguna tabla transaccional tiene columna para grupo. ¿Columna nueva en `supplier`? ¿Tabla relación?
2. **AND vs OR entre atributos distintos**: TipoProveedor=TPR001 AND GrupoProveedor=G01, ¿o OR?
3. **Header exacto BFF**: ¿`X-User-Sub` (sub del JWT) o `X-User-Id` (user_data_id ya resuelto)?
4. **`external_key` CatTipoProveedor en prod**: confirmar que el equipo aplica el mapeo `external_key = supplier_type.id`

---

## 9. Estado

| Jira | Proyecto | Endpoint | Campo filtro | Query filtro | Listo |
|------|----------|----------|-------------|-------------|-------|
| STM-321 | ✅ finanzas-api | ✅ `GET /three-way-match` | `vendor_number` | ✅ validada | ⚠️ implementar |
| STM-323 | ✅ fiscal-api | ✅ | `addendum.supplier_number` | ✅ validada | ⚠️ implementar |
| STM-1461 | ✅ finanzas-api | ✅ `GET /shipping-guide` | `vendor_number` | ✅ validada | ⚠️ implementar |
| STM-1474 | ✅ fiscal-api | ✅ | `addendum.supplier_number` | ✅ validada | ⚠️ implementar |
| STM-1524 | ✅ finanzas-api | ✅ `GET /account-statement` | `vendor_number` | ✅ validada | ⚠️ implementar |
| STM-1525 | ✅ catalogos-api | ✅ `GET /suppliers` | `supplier_type_id` | ✅ validada | ⚠️ implementar |
