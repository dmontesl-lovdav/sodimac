# STM-1526 — Análisis técnico (interno)

> ⚠ **Jira incompleto en XML** (placeholder). Reglas de negocio NO documentadas. Análisis basado en patrón del epic STM-1403.

## Resumen del jira

- **Módulo**: Catálogo de Catálogos
- **Pide**: filtrar la información del catálogo de catálogos por atributos del usuario
- **Prioridad**: Alta
- **Epic**: STM-1403 (asumido)

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.util-api` (catalogos-api fue **deprecado**, su funcionalidad ahora vive en util-api)

**Dos routers de catálogos en util-api**:

### A) `/api/catalog` (singular) — lectura
[`catalog.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/catalog.routes.ts) — endpoints de **lectura**:
- `GET /catalog/` → all catalogs
- `GET /catalog/id/:id`
- `GET /catalog/prefix/:prefix`
- `GET /catalog/message/:key[/format]`
- `GET /catalog/module/:module`
- `GET /catalog/:code`
- `GET /catalog/:code/details`
- `GET /catalog/:code/details/:key`

### B) `/api/catalogos` (plural) — gestión
[`catalogos.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/catalogos.routes.ts) — endpoints de **gestión/CRUD**:
- `GET/POST /catalogos/` — listar/crear catálogos
- `GET /catalogos/primarios`
- `GET /catalogos/:catalogId/elementos[/activos|/:elementId|/detalle]`
- `POST /catalogos/:catalogId/elementos`
- `PUT /catalogos/:catalogId/elementos/:elementId`
- `PATCH /catalogos/elementos/:elementId/estatus`
- `POST /catalogos/validate-layout`

**Entity `CatalogHeader`**:
- Campos: `code`, `prefix`, `name`, `description`, `module`, `catalog_type`, `status`, audit fields.
- **NO tiene `vendor_number`, `supplier_number`, ni similar**.

**Entity `CatalogDetail`** (no inspeccionado a fondo): contiene los elementos individuales de cada catálogo.

## Hallazgos

### 1. XML placeholder, sin reglas
198 líneas, sin sección de reglas. Mismo problema que STM-1527 y STM-1528.

### 2. Modelo de datos: filtrado NO obvio por vendor
`catalog_header` representa catálogos del sistema (ej. catálogo de monedas, países, tipos de documento). **No tiene relación directa con un proveedor**.

`catalog_detail` contiene valores: si algún catálogo tiene como key/value un identificador de proveedor (ej. `Catalogo "Proveedores"` con valores `11111, 22222...`), entonces podría filtrarse.

### 3. Interpretaciones posibles

| Hipótesis | Implementación | Probabilidad |
|-----------|----------------|--------------|
| Filtrar catálogos cuyo `module` está permitido al user | Mapeo `user → modules permitidos` | Media |
| Filtrar **elementos** (`catalog_detail`) según atributos del user — solo aplicable a ciertos catálogos | Lógica especial por catálogo | Alta |
| Mostrar/ocultar **catálogos enteros** según rol | Tabla nueva `catalog_role_access` | Baja |
| Catálogo "tipo de proveedor" o "grupo de proveedor" → filtrar elementos visibles según atributos del user | JOIN con `core_security.user_attribute` | Alta (esto es lo que hizo util-api en STM-1525) |

### 4. Pista importante: SecurityCatalogs.entity.ts
Existe ya un entity `SecurityCatalogs.entity.ts` en util-api. STM-1525 ya usa catálogos para mapear atributos (TPR001 → supplier_type_id). El "Catálogo de Catálogos" puede referirse específicamente a este modelo de catálogos de seguridad.

## Propuesta

### Camino 1 (recomendado): Pedir clarificación a Ivan

> STM-1526 dice "Catálogo de catálogos por atributo de usuario". El módulo de catálogos tiene 2 routers (`/catalog` y `/catalogos`) con +15 endpoints, y `catalog_header` no tiene relación directa con proveedor. Necesito:
> - ¿"Catálogo de catálogos" se refiere al menú/UI donde se administran los catálogos? ¿O a algún catálogo específico (ej. el de tipos de proveedor que ya usa STM-1525)?
> - ¿El filtro es por catálogo completo (admin ve todos, user normal solo los públicos) o por elementos dentro de cada catálogo?
> - ¿Hay catálogos "sensibles" que solo admins deben ver? Listado.

### Camino 2 (asumido): filtrar elementos por catálogos específicos
Si hay catálogos cuyo contenido se filtra por atributos (ej. catálogo "supplier_type" solo muestra los tipos permitidos al user), implementar filtros condicionales por código de catálogo.

Esto es ad-hoc y requiere lista explícita de catálogos sensibles → Ivan debe proveer.

## Archivos a tocar (si se implementa)

| Archivo | Cambio |
|---------|--------|
| `src/controllers/catalog.controller.ts` | Leer `req.security`, filtrar resultado según interpretación |
| `src/controllers/catalogManagement.controller.ts` | Mismo, para endpoints de gestión |
| `src/controllers/catalogElement.controller.ts` | Filtrar elementos individuales |
| `src/services/catalog*.service.ts` | Propagar filtros |
| `src/repositories/catalog*.repo.ts` | Aplicar filtros condicionales |
| `src/middlewares/security.middleware.ts` (util-api) | Crear si no existe |

## Dudas para Ivan (BLOQUEANTES)

1. **¿Qué entidad concreta debe filtrarse?** `catalog_header` (catálogo completo) o `catalog_detail` (elementos)?
2. **¿Qué catálogos son sensibles?** Listar códigos.
3. **¿Hay relación entre catálogos y vendors/tipos/grupos?** Sin esto, no se puede mapear.
4. **¿Reglas iguales al patrón epic (5 reglas + WRN7029)?**

## Estimación

- **No estimable sin clarificación.**
- Si solo es UI/admin ve "el catálogo de catálogos" y filtrarlo por rol: 1-2 SP.
- Si requiere filtrar elementos por mapeo con atributos: 3-5 SP.

## Notas adicionales

- Ya hay infra de seguridad en util-api por STM-1525 (`SecurityCatalogs.entity.ts`, `security.routes.ts`). Reutilizar antes de inventar.
- catalogos-api estaba deprecado y migrado aquí — verificar si el código actual incluye TODO lo que tenía el repo viejo.

## Referencias

- Entities: [`CatalogHeader.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/CatalogHeader.entity.ts), [`CatalogDetail.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/CatalogDetail.entity.ts), [`SecurityCatalogs.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/SecurityCatalogs.entity.ts)
- Routes: [`catalog.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/catalog.routes.ts), [`catalogos.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/catalogos.routes.ts)
