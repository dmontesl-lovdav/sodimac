# Implementación STM-1461 — Filtro de seguridad en Carta Porte

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Guías de Carta Porte** (Shipping Guide) dentro de `finanzas-api`. El backend decodifica el JWT del request, extrae el `sub` y consulta `util-api` para resolver los atributos del usuario. El cliente no controla los headers de seguridad.

---

## Flujo implementado

```
Frontend (envía Authorization: Bearer <JWT>)
    → GCP Cloud Endpoints (valida firma JWT en uat/prod)
        → finanzas-api middleware
              1. extrae sub del JWT
              2. consulta util-api /api/security/user-attributes-by-key/{sub}
              3. recibe atributos: ATR001 (vendor), ATR002 (tipo), ATR004 (grupo)
              4. setea req.security con los valores
        → controller filtra usando req.security.vendors
```

Cache: 5 min en memoria por `sub`. Patrón consistente con `aclaraciones-api` (`JwtTokenInterceptor`).

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `finanzas-api/src/middlewares/security.middleware.ts` | Reescrito: decodifica JWT, llama util-api, cachea, setea `req.security` (compartido con STM-321 y STM-1524) |
| `finanzas-api/src/controllers/shippingGuide.controller.ts` | Lee `req.security.vendors`, convierte a `number[]`, retorna WRN7029 si lista vacía |
| `finanzas-api/src/services/shippingGuide.service.ts` | `listPaginated` acepta `allowedVendors: number[] \| null`; aplica `In(allowedVendors)` de TypeORM en `filter.vendorNumber` |

---

## Configuración

| Variable | Default | Descripción |
|----------|---------|-------------|
| `SECURITY_ENABLED` | `false` | `true` en uat/prod. `false` en dev |
| `UTIL_API_URL` | `http://localhost:3712` | URL de util-api |

---

## Reglas de negocio aplicadas

| Atributo ATR001 del usuario | Comportamiento |
|------------------------------|----------------|
| Sin token (SECURITY_ENABLED=false) | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `1001` | Solo guías del proveedor 1001 |
| `1001,1002` | Guías del proveedor 1001 ó 1002 (OR lógico) |
| Sin ATR001 configurado en BD | HTTP 400 — WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — FERNANDO (ATR001=11111)
```
GET /api/shipping-guide?from=2024-01-01&to=2025-12-31
Authorization: Bearer <JWT con sub=sb000001>
```
**Resultado**: Solo guías donde `vendorNumber = 11111`

### Escenario 2 — JOSE (ATR001=11111,22222)
```
Authorization: Bearer <JWT con sub=sb000003>
```
**Resultado**: Guías del proveedor 11111 ó 22222

### Escenario 3 — Iván (ATR001=-1)
```
Authorization: Bearer <JWT con sub=sb000005>
```
**Resultado**: Todas las guías sin restricción

### Escenario 4 — ANA (sin ATR001)
```
Authorization: Bearer <JWT con sub=sb000002>
```
**Resultado**: HTTP 400 — WRN7029

```json
{
  "success": false,
  "code": "WRN7029",
  "message": "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador"
}
```

---

## Pruebas ejecutadas

| `sub` (JWT) | Usuario | Atributo | Resultado |
|-------------|---------|----------|-----------|
| `sb000001` | FERNANDO | ATR001=11111 | Filtro activo ✅ |
| `sb000005` | Iván | ATR001=-1 | Acceso total ✅ |
| `sb000002` | ANA | sin ATR001 | HTTP 400 WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve su información
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
- [x] Headers de cliente no spoofeable
