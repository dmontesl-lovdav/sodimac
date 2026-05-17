# STM-1421 — Análisis técnico (interno)

> Documento interno para implementación. No es respuesta de Jira.

## Resumen del jira

- **Módulo**: Descuentos Comerciales (rebates) — `tenant_finance.rebate`
- **Pide**: filtrar la información de descuentos comerciales por atributos del usuario (Proveedor, TipoProveedor, GrupoProveedor)
- **Prioridad**: Alta | **SP**: 3 | **Sprint**: 8-2027
- **Epic**: STM-1403

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.finanzas-api` (Node TypeScript)

**Endpoints existentes** (`src/routes/rebate.routes.ts` + `src/routes/index.ts`):
- `GET /rebates` — listar con filtros dinámicos (`findWithDynamicFilters`)
- `GET /rebates/published` — solo publicados (`status=1`)
- `GET /rebates/published/export/csv` — exportación CSV
- `GET /rebates/vendor/:vendorNumber` — por proveedor específico
- `GET /rebates/{uuid}` — detalle
- `POST /rebates` — crear
- `PUT /rebates/{uuid}` — actualizar
- `DELETE /rebates/{uuid}` — eliminar

**Endpoints stamped-rebates** (relacionados): `GET/POST/PUT /stamped-rebates`

**Repo TS**: `src/repositories/rebate.repo.ts` — implementa filtros dinámicos via QueryBuilder. **NO usa headers `x-user-vendors`.**

**Controller**: `src/controllers/rebate.controller.ts` — NO lee `req.security`. NO pasa `allowedVendors` al servicio.

**Estado del filtro**: ✗ **No aplicado**. Trabajo nuevo confirmado.

## Hallazgos

### Patrón aplicable
Modelo de referencia: `accountStatement.controller.ts` (STM-321) ya implementa el patrón en finanzas-api:

```ts
// controller
const sec = req.security;
if (Array.isArray(sec.vendors) && sec.vendors.length === 0) {
    return res.status(400).json({
        success: false,
        code: "WRN7029",
        message: "El usuario no tiene configurado los atributos..."
    });
}
const allowedVendors = sec.vendors;  // null = admin, string[] = filtro

// service/repo
await svc.listByFilters(filters, allowedVendors);
```

### Archivos a modificar

| Archivo | Cambio |
|---------|--------|
| `src/controllers/rebate.controller.ts` | Leer `req.security`, calcular `allowedVendors`, WRN7029 si vacío, pasar a service |
| `src/services/rebate.service.ts` | Aceptar `allowedVendors: string[] \| null` y propagar al repo |
| `src/repositories/rebate.repo.ts` | En `findAll`, `findAllPublished`, `findWithDynamicFilters`, `findByDocumentNumber`: agregar predicado `WHERE supplier_number IN (...)` cuando `allowedVendors !== null` |
| `src/schemas/rebate.schema.ts` | (Opcional) extender filter DTO si se requiere para tests |

### Endpoints afectados (todos los de lectura)
- `GET /rebates`
- `GET /rebates/published`
- `GET /rebates/published/export/csv`
- `GET /rebates/vendor/:vendorNumber` — caso interesante: si user pide `/vendor/X` pero `X` NO está en `allowedVendors`, debe responder vacío o 403. Definir comportamiento.
- `GET /rebates/{uuid}` — detalle: si rebate.supplier no está en allowedVendors → 404 o 403.

Endpoints de escritura (POST/PUT/DELETE): la regla del jira es **filtrado**, no autorización de escritura. Por defecto, no se tocan. Confirmar con Ivan.

### Campo de relación con vendor
`Rebate.entity.ts` tiene `supplierNumber` (basado en el uso del QueryBuilder línea 47 del repo). Tipo: probablemente `int` o `numeric`. Verificar en entity para definir tipo del filtro.

### Regla del valor `-1` (wildcard admin)
- BFF inyecta `x-user-vendors: "-1"` → `parseHeader` retorna `null` → `req.security.vendors === null` → **no aplicar filtro** (admin).

### Regla atributos múltiples (OR lógico)
- Si user tiene `ATR001=11111,22222`, BFF inyecta `x-user-vendors: "11111,22222"`.
- Repo: `WHERE supplier_number IN (11111, 22222)` (es OR lógico nativo de SQL).

### Atributos a considerar
El XML del jira lista: Proveedor, TipoProveedor, GrupoProveedor. En el dominio rebate **solo `supplier_number` mapea directamente** (Proveedor / ATR001). Para TipoProveedor (ATR002) y GrupoProveedor (ATR004) requeriría join a tabla de catálogo proveedor (mapea supplier_number → tipo/grupo).

**Recomendación inicial**: implementar filtro por `supplier_number` (Proveedor) en esta primera fase. Si Ivan exige filtros adicionales, evaluar join con `shared_catalogs` o tabla de suppliers. Memoria menciona que STM-1525 ya tiene mapeo TipoProveedor en shared_catalogs.

## Propuesta

### Implementación (mañana)
1. Agregar parámetro `allowedVendors: string[] | null` en signature del service `listX` y repo `findX`.
2. En cada query del repo (`findAll`, `findAllPublished`, `findWithDynamicFilters`, `findByDocumentNumber`): agregar:
   ```ts
   if (allowedVendors && allowedVendors.length > 0) {
       queryBuilder.andWhere('rebate.supplierNumber IN (:...allowedVendors)', { allowedVendors });
   }
   ```
3. En controller: extraer `req.security.vendors`, validar WRN7029 si array vacío, pasar a service.
4. Tests: regresión con admin (null), restringido (vendor único), bloqueado (vacío → WRN7029), multi-vendor.

### Entregables del jira (cuando se implemente)
- `STM-1421_postman.json` — collection con casos: admin, restringido, bloqueado, multi-vendor.
- `STM-1421_curl.ps1` + `.sh`.
- `STM-1421_queries_bd.sql` — query directa a `tenant_finance.rebate WHERE supplier_number IN (...)` para validación post-test.
- `STM-1421_respuesta-jira.md` — retro al cerrar.

## Dudas para Ivan

- **¿Filtrar también endpoints de escritura (POST/PUT/DELETE)?** El jira habla solo de consulta. Default: no tocar.
- **¿Endpoint `/rebates/vendor/{X}` cuando `X` no está en `allowedVendors`?** Opciones: (a) lista vacía con 404, (b) 403 explícito. Recomiendo (a) por consistencia con el filtro genérico.
- **¿Filtro adicional por TipoProveedor / GrupoProveedor?** Default: solo Proveedor (supplier_number). Confirmar si requieren join con catálogo.

## Estimación
- **Implementación**: 2-3 horas (patrón ya conocido por STM-321).
- **Postman/curl/SQL**: 30 min.
- **Tests + QA**: 1-2 horas.
- Total: ~5 horas / 3 SP coincide.

## Referencias
- Patrón: [`accountStatement.controller.ts`](../../../APP03022-mrch.backend.somx.finanzas-api/src/controllers/accountStatement.controller.ts) (STM-321)
- Repo a modificar: [`rebate.repo.ts`](../../../APP03022-mrch.backend.somx.finanzas-api/src/repositories/rebate.repo.ts)
- Epic STM-1403 + semántica de headers: [[project_security_headers_semantics]] en memoria
