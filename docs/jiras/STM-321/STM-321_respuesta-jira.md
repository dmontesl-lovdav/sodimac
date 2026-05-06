# Implementación STM-321 — Filtro de seguridad en Three Way Match

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Three Way Match** dentro de `finanzas-api`. El filtrado opera exclusivamente en backend a partir de encabezados inyectados por el BFF.

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
| `bff.ppsomx.finanzas/src/App.js` | Agrega `extractUserKey`, `fetchSecurityContext`, `buildSecurityHeaders`; inyecta headers en `proxyReqOptDecorator` |
| `finanzas-api/src/middlewares/security.middleware.ts` | Nuevo middleware: parsea `x-user-vendors` y adjunta `req.security` |
| `finanzas-api/src/app.ts` | Registra `attachSecurityContext` en `/api` antes del router |
| `finanzas-api/src/controllers/threeWayMatch.controller.ts` | Lee `req.security.vendors`, retorna WRN7029 si lista vacía |
| `finanzas-api/src/services/threeWayMatchQuery.service.ts` | Recibe `allowedVendors: string[] \| null` y lo pasa al repo |
| `finanzas-api/src/repositories/threeWayMatch.repo.ts` | Aplica `CAST(t.numeroProveedor AS TEXT) IN (:...allowedVendors)` cuando hay restricción |

---

## Reglas de negocio aplicadas

| Condición del header `x-user-vendors` | Comportamiento |
|---------------------------------------|---------------|
| Header ausente (admin/sistema) | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total al proveedor |
| `1001` | Filtra solo registros del proveedor 1001 |
| `1001,1002` | Filtra registros del proveedor 1001 ó 1002 (OR lógico) |
| Vacío `""` | Retorna WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — Usuario con proveedor específico
```
GET /three-way-match?tipoFecha=fechaRecepcion&fechaInicio=2024-01-01&fechaFin=2025-12-31
x-user-vendors: 1001
```
**Resultado**: Solo registros donde `numeroProveedor = 1001`

### Escenario 2 — Usuario con múltiples proveedores
```
GET /three-way-match?tipoFecha=fechaRecepcion&fechaInicio=2024-01-01&fechaFin=2025-12-31
x-user-vendors: 1001,1002
```
**Resultado**: Registros del proveedor 1001 ó 1002

### Escenario 3 — Usuario con acceso total (-1)
```
GET /three-way-match?tipoFecha=fechaRecepcion&fechaInicio=2024-01-01&fechaFin=2025-12-31
x-user-vendors: -1
```
**Resultado**: Todos los registros sin restricción

### Escenario 4 — Usuario sin atributos configurados
```
GET /three-way-match?tipoFecha=fechaRecepcion&fechaInicio=2024-01-01&fechaFin=2025-12-31
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

| Header `x-user-vendors` | Registros (rango 2025-01-01 / 2025-06-30) | Resultado |
|--------------------------|------------------------------------------|-----------|
| `11111` | 2 | Filtro activo ✅ |
| `-1` | 6 | Acceso total ✅ |
| `""` (vacío) | — | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve su información
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
