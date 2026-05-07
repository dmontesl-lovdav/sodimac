# Implementación STM-1474 — Filtro de seguridad en Recepción (Complementos de Pago)

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Complementos de Pago** (Recepción) dentro de `fiscal-api`. El backend decodifica el JWT del request, extrae el `sub`, consulta `util-api` para los atributos y filtra por `addendum.supplier_number` vinculado al complemento vía `addendum.payments_uuid`. El cliente NO controla los headers de seguridad — un Filter Spring los reescribe siempre con los valores derivados del JWT.

---

## Flujo implementado

```
Frontend (envía Authorization: Bearer <JWT>)
    → GCP Cloud Endpoints (valida firma JWT en uat/prod)
        → fiscal-api SecurityContextFilter
              1. extrae sub del JWT
              2. consulta util-api /api/security/user-attributes-by-key/{sub}
              3. recibe atributos: ATR001 (vendor)
              4. envuelve request: getHeader("x-user-vendors") devuelve valor calculado
        → JwtTokenInterceptor valida firma JWT
        → PaymentRegistrationController lee @RequestHeader("x-user-vendors") (ya seguro)
```

Cache: 5 min en memoria por `sub`. Patrón consistente con `aclaraciones-api` (`JwtTokenInterceptor`).

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `fiscal-api/.../security/UtilApiSecurityClient.java` | **Nuevo (compartido con STM-323)**: HTTP client hacia util-api con cache 5 min |
| `fiscal-api/.../security/SecurityContextFilter.java` | **Nuevo (compartido con STM-323)**: `OncePerRequestFilter` que envuelve request con header de seguridad calculado del JWT lookup |
| `fiscal-api/PaymentRegistrationController.java` | Mantiene `@RequestHeader("x-user-vendors")` (sin cambio). Recibe valor seguro del filter |
| `fiscal-api/PaymentQueryService.java` | Sin cambio: overload `searchPayments(request, List<String> allowedVendors)` |
| `fiscal-api/PaymentQueryServiceImpl.java` | Sin cambio |
| `fiscal-api/PaymentsRepositoryCustom.java` | Sin cambio |
| `fiscal-api/PaymentsRepositoryCustomImpl.java` | Sin cambio: subquery JPA sobre `addendum.supplier_number` |

---

## Configuración

| Variable | Default | Descripción |
|----------|---------|-------------|
| `security.enabled` | `true` | `false` en dev (filter pasa headers cliente). `true` en uat/prod |
| `fiscal.util-api.url` | `http://localhost:3712` | URL de util-api |

---

## Mecanismo de filtrado

Subquery JPA Criteria que filtra pagos por proveedor vía addendum:

```java
// payments_uuid IN (SELECT payments_uuid FROM addendum
//                   WHERE supplier_number IN (allowedVendors)
//                   AND payments_uuid IS NOT NULL)
List<BigDecimal> vendorNumbers = allowedVendors.stream()
    .map(v -> new BigDecimal(v.trim()))
    .collect(Collectors.toList());
Subquery<UUID> sub = query.subquery(UUID.class);
Root<AddendumEntity> addRoot = sub.from(AddendumEntity.class);
sub.select(addRoot.get("paymentsUuid"))
   .where(cb.and(
       cb.isNotNull(addRoot.get("paymentsUuid")),
       addRoot.get("supplierNumber").in(vendorNumbers)
   ));
predicates.add(root.get("paymentsUuid").in(sub));
```

---

## Reglas de negocio aplicadas

| Atributo ATR001 del usuario | Comportamiento |
|------------------------------|----------------|
| Sin token (security.enabled=false) | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `11111` | Solo complementos cuyo addendum.supplier_number = 11111 |
| `11111,22222` | Complementos de supplier 11111 ó 22222 (OR lógico) |
| Sin ATR001 configurado en BD | HTTP 400 — WRN7029 |

---

## Pruebas ejecutadas

| `sub` (JWT) | Usuario | Atributo ATR001 | Complementos | Resultado |
|-------------|---------|------------------|--------------|-----------|
| `sb000001` | FERNANDO | 11111 | 0 | Filtro activo (sin datos en payments) ✅ |
| `sb000003` | JOSE | 11111,22222 | 0 | Filtro activo OR lógico ✅ |
| `sb000005` | Iván | -1 | 19 | Acceso total ✅ |
| `sb000002` | ANA | sin ATR001 | — | HTTP 400 WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve sus complementos
- [x] Usuario con valor -1 → acceso total (19 complementos)
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
- [x] Headers de cliente no spoofeable (filter sobrescribe siempre con valor del JWT)
