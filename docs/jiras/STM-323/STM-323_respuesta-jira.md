# Implementación STM-323 — Filtro de seguridad en Facturas

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Facturas** dentro de `fiscal-api`. El backend decodifica el JWT del request, extrae el `sub` del usuario, consulta `util-api` para resolver sus atributos y aplica el filtro al querying. El cliente NO controla los headers de seguridad — un Filter Spring los reescribe siempre con los valores derivados del JWT.

---

## Flujo implementado

```
Frontend (envía Authorization: Bearer <JWT>)
    → GCP Cloud Endpoints (valida firma JWT en uat/prod)
        → fiscal-api SecurityContextFilter
              1. extrae sub del JWT
              2. consulta util-api /api/security/user-attributes-by-key/{sub}
              3. recibe atributos: ATR001 (vendor), ATR002 (tipo), ATR004 (grupo)
              4. envuelve request: getHeader("x-user-vendors") devuelve valor calculado
        → JwtTokenInterceptor valida firma JWT
        → InvoiceController lee @RequestHeader("x-user-vendors") (ya seguro)
```

Cache: 5 min en memoria por `sub`. Patrón consistente con `aclaraciones-api` (`JwtTokenInterceptor`).

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `fiscal-api/.../security/UtilApiSecurityClient.java` | **Nuevo**: HTTP client (java.net.http) hacia util-api con cache 5 min |
| `fiscal-api/.../security/SecurityContextFilter.java` | **Nuevo**: `OncePerRequestFilter` (HIGHEST_PRECEDENCE). Decodifica JWT, llama `UtilApiSecurityClient`, envuelve request con `HttpServletRequestWrapper` que sobrescribe `x-user-vendors`/`x-user-types`/`x-user-groups`. Cliente NO puede falsificar |
| `fiscal-api/InvoiceController.java` | Mantiene `@RequestHeader("x-user-vendors")` (sin cambio). Recibe valor seguro del filter |
| `fiscal-api/InvoiceService.java` | Sin cambio: método `searchInvoices(request, List<String> allowedVendors)` |
| `fiscal-api/InvoiceSpecification.java` | Sin cambio: subquery JPA Criteria sobre `addendum.supplierNumber` |

---

## Configuración

| Variable | Default | Descripción |
|----------|---------|-------------|
| `security.enabled` | `true` | `false` en dev (filter deja pasar headers cliente). `true` en uat/prod (filter sobrescribe siempre) |
| `fiscal.util-api.url` | `http://localhost:3712` | URL de util-api |

---

## Mecanismo de filtrado (fiscal-api)

El filtrado usa **JPA Criteria API** con subquery:

```java
// Subquery: facturas cuyo addendum.supplierNumber está en allowedVendors
Subquery<Long> sub = query.subquery(Long.class);
Root<Addendum> addRoot = sub.from(Addendum.class);
sub.select(addRoot.get("invoiceId"))
   .where(cb.in(addRoot.get("supplierNumber").as(String.class)).value(vendors));

predicates.add(cb.in(root.get("id")).value(sub));
```

---

## Reglas de negocio aplicadas

| Atributo ATR001 del usuario en util-api | Comportamiento |
|------------------------------------------|----------------|
| Sin token (security.enabled=false) | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `11111` | Solo facturas cuyo addendum.supplierNumber = 11111 |
| `11111,22222` | Facturas con supplierNumber 11111 ó 22222 (OR lógico) |
| Sin ATR001 configurado en BD | HTTP 400 — WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — FERNANDO (ATR001=11111)
```
POST /api/invoices/search
Authorization: Bearer <JWT con sub=sb000001>
Content-Type: application/json

{"page": 0, "size": 20}
```
**Resultado**: Solo facturas del proveedor 11111

### Escenario 2 — JOSE (ATR001=11111,22222)
```
Authorization: Bearer <JWT con sub=sb000003>
```
**Resultado**: Facturas del proveedor 11111 ó 22222

### Escenario 3 — Iván (ATR001=-1)
```
Authorization: Bearer <JWT con sub=sb000005>
```
**Resultado**: Todas las facturas sin restricción

### Escenario 4 — ANA (sin ATR001)
```
Authorization: Bearer <JWT con sub=sb000002>
```
**Resultado**: HTTP 400 — WRN7029

```json
{
  "code": "WRN7029",
  "message": "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador"
}
```

---

## Pruebas ejecutadas

| `sub` (JWT) | Usuario | Atributo ATR001 | Registros | Resultado |
|-------------|---------|------------------|-----------|-----------|
| `sb000001` | FERNANDO | 11111 | 4 | Filtro por proveedor 11111 ✅ |
| `sb000003` | JOSE | 11111,22222 | 8 | OR lógico ✅ |
| `sb000005` | Iván | -1 | 23 | Acceso total ✅ |
| `sb000002` | ANA | sin ATR001 | — | HTTP 400 WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve sus facturas
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
- [x] Headers de cliente no spoofeable (filter sobrescribe siempre con valor del JWT)
