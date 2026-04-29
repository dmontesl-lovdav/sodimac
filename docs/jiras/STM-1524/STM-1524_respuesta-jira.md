# Implementación STM-1524 — Filtro de seguridad en Estado de Cuenta

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Estado de Cuenta** dentro de `finanzas-api`. El filtrado opera exclusivamente en backend a partir de encabezados inyectados por el BFF.

---

## Flujo implementado

```
Frontend → JWT → GCP Cloud Endpoints
    → BFF (extrae userKey del token, consulta util-api, inyecta headers)
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
| `bff.ppsomx.finanzas/src/App.js` | Inyección de headers de seguridad (compartido con STM-321) |
| `finanzas-api/src/middlewares/security.middleware.ts` | Middleware compartido que parsea `x-user-vendors` |
| `finanzas-api/src/controllers/accountStatement.controller.ts` | Lee `req.security.vendors`, retorna WRN7029 si lista vacía |
| `finanzas-api/src/repositories/accountStatement.repo.ts` | Aplica `CAST(a.vendor_number AS TEXT) IN (:...allowedVendors)` en `findByFilters` y `findById` |

---

## Reglas de negocio aplicadas

| Condición del header `x-user-vendors` | Comportamiento |
|---------------------------------------|---------------|
| Header ausente | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `1001` | Solo estados de cuenta del proveedor 1001 |
| `1001,1002` | Estados del proveedor 1001 ó 1002 (OR lógico) |
| Vacío `""` | Retorna WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — Usuario con proveedor específico
```
GET /account-statement?year=2025
x-user-vendors: 1001
```
**Resultado**: Solo estados de cuenta donde `vendor_number = 1001`

### Escenario 2 — Usuario con múltiples proveedores
```
GET /account-statement?year=2025
x-user-vendors: 1001,1002
```
**Resultado**: Estados de cuenta del proveedor 1001 ó 1002

### Escenario 3 — Usuario con acceso total (-1)
```
GET /account-statement?year=2025
x-user-vendors: -1
```
**Resultado**: Todos los estados de cuenta sin restricción

### Escenario 4 — Usuario sin atributos configurados
```
GET /account-statement?year=2025
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

### Escenario 5 — Consulta por UUID con filtro activo
```
GET /account-statement/{uuid}
x-user-vendors: 1001
```
**Resultado**: 404 si el estado pertenece a otro proveedor; 200 si es del proveedor 1001

---

## Pruebas ejecutadas (vía BFF local)

| Usuario | Atributo ATR001 | Resultado |
|---------|-----------------|-----------|
| fernando | 1001 | Solo estados del proveedor 1001 ✅ |
| jose | 1001, 1002 | Estados de 1001 ó 1002 ✅ |
| ivan | -1 | Todos los estados ✅ |
| ana | (sin ATR001) | HTTP 400 — WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve su información
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
- [x] Filtro aplica también a consulta por UUID (getById)
