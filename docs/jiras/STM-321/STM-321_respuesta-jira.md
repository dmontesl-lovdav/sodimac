# Implementación STM-321 — Filtro de seguridad en Three Way Match

## Resumen

Se implementó el filtro de seguridad por atributo de usuario en el módulo de **Three Way Match** dentro de `finanzas-api`. El filtrado opera exclusivamente en backend: el middleware decodifica el JWT del request, extrae el `sub` del usuario y consulta `util-api` para resolver sus atributos. El cliente NO controla los headers de seguridad.

---

## Flujo implementado

```
Frontend (envía Authorization: Bearer <JWT>)
    → GCP Cloud Endpoints (valida firma JWT en uat/prod)
        → finanzas-api middleware
              1. extrae sub del JWT (sin verificar firma — gateway ya validó)
              2. consulta util-api /api/security/user-attributes-by-key/{sub}
              3. recibe atributos: ATR001 (vendor), ATR002 (tipo), ATR004 (grupo)
              4. setea req.security con los valores
        → controller filtra usando req.security.vendors
```

Cache: 5 min en memoria por `sub` (alineado con util-api).

Patrón consistente con `aclaraciones-api` (`JwtTokenInterceptor`) que ya implementó el equipo.

---

## Cambios realizados

| Archivo | Cambio |
|---------|--------|
| `finanzas-api/src/middlewares/security.middleware.ts` | Reescrito: decodifica JWT, llama util-api, cachea, setea `req.security` |
| `finanzas-api/src/app.ts` | Registra `attachSecurityContext` en `/api` antes del router |
| `finanzas-api/src/controllers/threeWayMatch.controller.ts` | Lee `req.security.vendors`, retorna WRN7029 si lista vacía |
| `finanzas-api/src/services/threeWayMatchQuery.service.ts` | Recibe `allowedVendors: string[] \| null` y lo pasa al repo |
| `finanzas-api/src/repositories/threeWayMatch.repo.ts` | Aplica `CAST(t.numeroProveedor AS TEXT) IN (:...allowedVendors)` cuando hay restricción |

---

## Configuración

| Variable | Default | Descripción |
|----------|---------|-------------|
| `SECURITY_ENABLED` | `false` | `true` en uat/prod (exige JWT). `false` en dev (sin restricción) |
| `UTIL_API_URL` | `http://localhost:3712` | URL de util-api para lookup de atributos |

---

## Reglas de negocio aplicadas

| Atributo ATR001 del usuario en util-api | Comportamiento |
|------------------------------------------|----------------|
| Sin token (SECURITY_ENABLED=false) | Sin filtro — devuelve todo |
| `-1` | Sin filtro — acceso total |
| `1001` | Solo registros del proveedor 1001 |
| `1001,1002` | Registros del proveedor 1001 ó 1002 (OR lógico) |
| Sin ATR001 configurado en BD | HTTP 400 — WRN7029 |

---

## Escenarios de prueba

> Generar JWT de prueba con `sub` válido en https://jwt.io (algoritmo `none` o `HS256` con cualquier secret — el backend solo decodifica payload, no verifica firma; en uat/prod GCP gateway valida).

### Escenario 1 — FERNANDO (ATR001=11111)
```
GET /api/three-way-match?tipoFecha=fechaRecepcion&fechaInicio=2025-01-01&fechaFin=2025-06-30
Authorization: Bearer <JWT con sub=sb000001>
```
**Resultado**: Solo registros donde `numeroProveedor = 11111`

### Escenario 2 — JOSE (ATR001=11111,22222)
```
Authorization: Bearer <JWT con sub=sb000003>
```
**Resultado**: Registros del proveedor 11111 ó 22222

### Escenario 3 — Iván (ATR001=-1, acceso total)
```
Authorization: Bearer <JWT con sub=sb000005>
```
**Resultado**: Todos los registros sin restricción

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

| `sub` (JWT) | Usuario | Atributo | Resultado esperado |
|-------------|---------|----------|---------------------|
| `sb000001` | FERNANDO | ATR001=11111 | filtro activo a 11111 ✅ |
| `sb000003` | JOSE | ATR001=11111,22222 | filtro activo a 11111∪22222 ✅ |
| `sb000005` | Iván | ATR001=-1 | acceso total ✅ |
| `sb000002` | ANA | sin ATR001 | HTTP 400 WRN7029 ✅ |

---

## Criterios de aceptación cubiertos

- [x] Usuario con atributos → solo ve su información
- [x] Usuario con valor -1 → acceso total
- [x] Usuario con múltiples atributos → OR lógico
- [x] Usuario sin atributos → WRN7029
- [x] Headers de cliente no spoofeable (backend ignora `x-user-vendors` del request, deriva de JWT firmado)
