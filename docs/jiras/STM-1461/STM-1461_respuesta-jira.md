# Implementación STM-1461 — Filtro de seguridad en Carta Porte

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Guías de Carta Porte** (Shipping Guide) dentro de `finanzas-api`. El filtrado opera exclusivamente en backend a partir de encabezados inyectados por el BFF.

---

## Flujo implementado

```
Frontend (recupera token JWT, envía Authorization header)
    → GCP Cloud Endpoints (valida token)
        → BFF finanzas (consulta util-api, inyecta headers de seguridad)
            → finanzas-api (filtra datos según headers)
```

Headers inyectados por BFF:
- `x-user-vendors` — valores del atributo ATR001 (Proveedor)
- `x-user-types` — valores del atributo ATR002 (TipoProveedor)
- `x-user-groups` — valores del atributo ATR004 (GrupoProveedor)

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `bff.ppsomx.finanzas/src/App.js` | Inyección de headers de seguridad (compartido) |
| `finanzas-api/src/middlewares/security.middleware.ts` | Middleware compartido que parsea `x-user-vendors` |
| `finanzas-api/src/controllers/shippingGuide.controller.ts` | Lee `req.security.vendors`, convierte a `number[]`, retorna WRN7029 si lista vacía |
| `finanzas-api/src/services/shippingGuide.service.ts` | `listPaginated` acepta `allowedVendors: number[] \| null`; aplica `In(allowedVendors)` de TypeORM en `filter.vendorNumber` |

---

## Reglas de negocio aplicadas

| Condición del header `x-user-vendors` | Comportamiento |
|---------------------------------------|---------------|
| Header ausente | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `1001` | Solo guías del proveedor 1001 |
| `1001,1002` | Guías del proveedor 1001 ó 1002 (OR lógico) |
| Vacío `""` | Retorna WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — Usuario con proveedor específico
```
GET /shipping-guide?from=2024-01-01&to=2025-12-31
x-user-vendors: 1001
```
**Resultado**: Solo guías donde `vendorNumber = 1001`

### Escenario 2 — Usuario con múltiples proveedores
```
GET /shipping-guide?from=2024-01-01&to=2025-12-31
x-user-vendors: 1001,1002
```
**Resultado**: Guías del proveedor 1001 ó 1002

### Escenario 3 — Usuario con acceso total (-1)
```
GET /shipping-guide?from=2024-01-01&to=2025-12-31
x-user-vendors: -1
```
**Resultado**: Todas las guías sin restricción

### Escenario 4 — Usuario sin atributos configurados
```
GET /shipping-guide?from=2024-01-01&to=2025-12-31
x-user-vendors: (vacío)
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

## Pruebas ejecutadas (directo a finanzas-api — puerto 3001)

| Header `x-user-vendors` | Guías | Resultado |
|--------------------------|-------|-----------|
| `11111` | 2 | Filtro activo ✅ |
| `-1` | 5 | Acceso total ✅ |
| `""` (vacío) | — | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve su información
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
