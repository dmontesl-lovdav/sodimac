# Implementación STM-1474 — Filtro de seguridad en Recepción (Complementos de Pago)

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Complementos de Pago** (Recepción) dentro de `fiscal-api`. El filtrado opera a nivel backend filtrando por `addendum.supplier_number` vinculado al complemento vía `addendum.payments_uuid`.

---

## Flujo implementado

```
Frontend → JWT → GCP Cloud Endpoints
    → BFF fiscal (extrae userKey, consulta util-api, inyecta headers)
        → fiscal-api (filtra complementos según headers)
```

Headers inyectados por BFF:
- `x-user-vendors` — valores del atributo ATR001 (Proveedor)

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `fiscal-api/PaymentRegistrationController.java` | Agrega `@RequestHeader(x-user-vendors)`, `parseVendorHeader()`, WRN7029 en `buscarComplementosPago()` |
| `fiscal-api/PaymentQueryService.java` | Agrega overload `searchPayments(request, List<String> allowedVendors)` |
| `fiscal-api/PaymentQueryServiceImpl.java` | Implementa overload, delega a repositorio con vendors |
| `fiscal-api/PaymentsRepositoryCustom.java` | Agrega overload con allowedVendors |
| `fiscal-api/PaymentsRepositoryCustomImpl.java` | Implementa `searchPayments(request, vendors)` con subquery JPA sobre `addendum.supplier_number` |

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

| Condición del header `x-user-vendors` | Comportamiento |
|---------------------------------------|---------------|
| Header ausente | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `11111` | Solo complementos cuyo addendum.supplier_number = 11111 |
| `11111,22222` | Complementos de supplier 11111 ó 22222 (OR lógico) |
| Vacío `""` | Retorna WRN7029 |

---

## Pruebas ejecutadas (vía BFF fiscal — puerto 3003)

| Usuario | Atributo ATR001 | Complementos | Resultado |
|---------|-----------------|--------------|-----------|
| USR_FERNANDO | 11111 | 0 | Filtro activo (sin datos para vendor 11111 en payments) ✅ |
| USR_JOSE | 11111, 22222 | 0 | Filtro activo OR lógico ✅ |
| zedlav.sd18@gmail.com | -1 | 19 | Acceso total ✅ |
| USR_ANA | (sin ATR001) | — | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve sus complementos
- [x] Usuario con valor -1 → acceso total (19 complementos)
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
