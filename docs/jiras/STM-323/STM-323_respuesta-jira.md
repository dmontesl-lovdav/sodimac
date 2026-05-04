# Implementación STM-323 — Filtro de seguridad en Facturas

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Facturas** dentro de `fiscal-api`. El filtrado opera exclusivamente en backend a partir de encabezados inyectados por el BFF fiscal.

---

## Flujo implementado

```
Frontend (recupera token JWT, envía Authorization header)
    → GCP Cloud Endpoints (valida token)
        → BFF fiscal (consulta util-api, inyecta headers de seguridad)
            → fiscal-api (filtra facturas según headers)
```

Headers inyectados por BFF:
- `x-user-vendors` — valores del atributo ATR001 (Proveedor)
- `x-user-types` — valores del atributo ATR002 (TipoProveedor)
- `x-user-groups` — valores del atributo ATR004 (GrupoProveedor)

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `bff.fiscal/src/App.js` | Agrega `extractUserKey`, `fetchSecurityContext`, `buildSecurityHeaders`; inyecta headers en `proxyReqOptDecorator` |
| `fiscal-api/InvoiceController.java` | Agrega `@RequestHeader(x-user-vendors)`, `parseVendorHeader()`, retorna WRN7029 si lista vacía |
| `fiscal-api/InvoiceService.java` | Nuevo método `searchInvoices(request, List<String> allowedVendors)` |
| `fiscal-api/InvoiceServiceImpl.java` | Implementación del nuevo método, delega a `InvoiceSpecification` con vendors |
| `fiscal-api/InvoiceSpecification.java` | Nuevo `buildSpecification(request, allowedVendors)`: subquery JPA Criteria sobre `addendum.supplierNumber` |

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

| Condición del header `x-user-vendors` | Comportamiento |
|---------------------------------------|---------------|
| Header ausente (`null`) | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `11111` | Solo facturas cuyo addendum.supplierNumber = 11111 |
| `11111,22222` | Facturas con supplierNumber 11111 ó 22222 (OR lógico) |
| Vacío `""` | Retorna WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — Usuario con proveedor específico
```
POST /invoices/search
Content-Type: application/json
x-user-vendors: 11111

{"page": 0, "size": 20}
```
**Resultado**: Solo facturas del proveedor 11111 — **4 registros**

### Escenario 2 — Usuario con múltiples proveedores
```
POST /invoices/search
Content-Type: application/json
x-user-vendors: 11111,22222

{"page": 0, "size": 20}
```
**Resultado**: Facturas del proveedor 11111 ó 22222 — **8 registros**

### Escenario 3 — Usuario con acceso total (-1)
```
POST /invoices/search
Content-Type: application/json
x-user-vendors: -1

{"page": 0, "size": 20}
```
**Resultado**: Todas las facturas sin restricción — **23 registros**

### Escenario 4 — Usuario sin atributos configurados
```
POST /invoices/search
Content-Type: application/json
x-user-vendors: (vacío)

{"page": 0, "size": 20}
```
**Resultado**: HTTP 400 — WRN7029

```json
{
  "code": "WRN7029",
  "message": "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador"
}
```

---

## Pruebas ejecutadas (vía BFF fiscal local — puerto 3003)

| Usuario | Atributo ATR001 | Registros | Resultado |
|---------|-----------------|-----------|-----------|
| fernando | 11111 | 4 | Filtro por proveedor 11111 ✅ |
| jose | 11111, 22222 | 8 | OR lógico proveedores 11111/22222 ✅ |
| ivan | -1 | 23 | Acceso total ✅ |
| ana | (sin ATR001) | — | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve sus facturas
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
