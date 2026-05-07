# Implementación STM-1524 — Filtro de seguridad en Estado de Cuenta

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Estado de Cuenta** dentro de `finanzas-api`. El backend decodifica el JWT del request, extrae el `sub` y consulta `util-api` para resolver los atributos del usuario. El cliente no controla los headers de seguridad.

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
| `finanzas-api/src/middlewares/security.middleware.ts` | Middleware compartido reescrito: decodifica JWT, llama util-api, cachea, setea `req.security` |
| `finanzas-api/src/controllers/accountStatement.controller.ts` | Lee `req.security.vendors`, retorna WRN7029 si lista vacía |
| `finanzas-api/src/repositories/accountStatement.repo.ts` | Aplica `CAST(a.vendor_number AS TEXT) IN (:...allowedVendors)` en `findByFilters` y `findById` |

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
| `1001` | Solo estados de cuenta del proveedor 1001 |
| `1001,1002` | Estados del proveedor 1001 ó 1002 (OR lógico) |
| Sin ATR001 configurado en BD | HTTP 400 — WRN7029 |

---

## Escenarios de prueba

### Escenario 1 — FERNANDO (ATR001=11111)
```
GET /api/account-statement?year=2026
Authorization: Bearer <JWT con sub=sb000001>
```
**Resultado**: Solo estados de cuenta donde `vendor_number = 11111`

### Escenario 2 — JOSE (ATR001=11111,22222)
```
Authorization: Bearer <JWT con sub=sb000003>
```
**Resultado**: Estados de cuenta del proveedor 11111 ó 22222

### Escenario 3 — Iván (ATR001=-1)
```
Authorization: Bearer <JWT con sub=sb000005>
```
**Resultado**: Todos los estados de cuenta sin restricción

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

### Escenario 5 — Consulta por UUID con filtro activo
```
GET /api/account-statement/{uuid}
Authorization: Bearer <JWT con sub=sb000001>  (FERNANDO ATR001=11111)
```
**Resultado**: 404 si el estado pertenece a otro proveedor; 200 si es del proveedor 11111

---

## Pruebas ejecutadas

| `sub` (JWT) | Usuario | Atributo | Estados (year=2026) | Resultado |
|-------------|---------|----------|---------------------|-----------|
| `sb000001` | FERNANDO | ATR001=11111 | 3 | Filtro activo ✅ |
| `sb000003` | JOSE | ATR001=11111,22222 | 5 | Filtro activo ✅ |
| `sb000005` | Iván | ATR001=-1 | 6 | Acceso total ✅ |
| `sb000002` | ANA | sin ATR001 | — | HTTP 400 WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve su información
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
- [x] Filtro aplica también a consulta por UUID (getById)
- [x] Headers de cliente no spoofeable
