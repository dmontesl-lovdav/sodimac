# STM-1527 — Análisis técnico (interno)

> ⚠ **Jira incompleto en XML** (placeholder). Reglas de negocio NO documentadas. Análisis basado en patrón del epic STM-1403.

## Resumen del jira

- **Módulo**: Parámetros
- **Pide**: filtrar la información de parámetros por atributos del usuario (sin reglas explícitas en el ticket)
- **Prioridad**: Alta
- **Epic**: STM-1403 (asumido por contexto)

## Inventario del código actual

**Repo objetivo**: `APP03022-mrch.backend.somx.util-api`

**Endpoints existentes** ([`parameter.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/parameter.routes.ts)):
- `GET /api/parameters` — listar con paginación y filtros
- `GET /api/parameters/:id` — por ID
- `POST /api/parameters` — crear (versión inicial 1.0)
- otros (versionado STM-1213)

**Entity**: [`CatParameter.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/CatParameter.entity.ts)
- Tabla: `cat_parameter`
- Campos: `id_parameter`, `id_module`, `id_type`, `name`, `description`, `value`, `version`, `start_date`, `end_date`, `status`, audit fields
- **NO tiene `vendor_number`, `supplier_number` ni similar**.

**Estado del filtro**: ✗ no aplicado, **pero el modelo no expone un campo directo de vendor**.

## Hallazgos críticos

### 1. El XML del jira está vacío (placeholder)
198 líneas, sin sección "Reglas de Negocio" ni "Acceptance Criteria" reales. Solo metadata.

Compárese con STM-1421 (319 líneas) que sí tiene 6 reglas explícitas.

→ **No se puede implementar sin clarificación**. Ivan debe completar el ticket con:
- Qué atributo aplica al filtrado (Proveedor, TipoProveedor, GrupoProveedor o todos)
- Cómo se relaciona un "parámetro de sistema" con un proveedor (¿por módulo? ¿por ámbito? ¿por tipo?)
- Comportamiento ante usuario sin atributos (¿WRN7029 estándar?)

### 2. El modelo de datos no soporta directamente filtrado por vendor
`cat_parameter` es un **catálogo de configuración del sistema** (STM-1212). Sus parámetros son globales (ej. timeout, paths, flags). No hay columna `vendor_number`.

**Posibles interpretaciones del filtrado**:

| Hipótesis | Implementación | Probabilidad |
|-----------|----------------|--------------|
| Filtrar por **módulo** (`id_module`) según permisos del user | JOIN con tabla de "modulos permitidos por user" | Media — requiere modelar permisos por módulo |
| Filtrar parámetros **públicos vs admin-only** | Agregar campo `audience` o usar `id_type` para distinguir | Alta — más alineado con "filtrado por atributo de usuario" |
| Filtrar solo los **activos** (`status=1`) | Ya implementado probablemente | Baja — no requiere cambio si ya filtra |
| Filtrar por **proveedor** vía join con tabla externa | Requiere tabla `parameter_by_supplier` | Baja — implica modelado nuevo |

### 3. No hay relación obvia con ATR001/ATR002/ATR004
Los atributos del epic (Proveedor, TipoProveedor, GrupoProveedor) **no aplican directamente** al modelo de parámetros.

## Propuesta

### Camino 1 (recomendado): Pedir clarificación a Ivan ANTES de implementar
Bloquear el jira con un comentario:

> Hola Ivan, el ticket STM-1527 indica filtrar parámetros por atributo de usuario pero no detalla reglas. La tabla `cat_parameter` no expone un campo de vendor directamente. Necesito:
> - ¿Qué se considera "filtrar parámetro por atributo"? Posibles interpretaciones: por módulo, por tipo (admin/proveedor), por ámbito.
> - ¿Qué usuarios deben ver qué parámetros? Ejemplo concreto.
> - ¿Las reglas son las mismas que STM-1421 / STM-1460 (Proveedor, TipoProveedor, GrupoProveedor)? Si sí, ¿cómo mapean al modelo?

### Camino 2 (si Ivan no responde): asumir y aplicar el patrón mínimo
Implementar `attachSecurityContext` en routes/parameter y **NO filtrar** los resultados (solo log y respuesta WRN7029 si user sin atributos). Esto cumple "técnicamente" con el patrón del epic pero no agrega filtrado real.

### Camino 3 (más probable después de clarificación): filtrar por módulo
Agregar:
- Tabla nueva `user_module_access (user_data_id, id_module)` o usar atributos existentes para mapear.
- Modificar `parameter.repo` para hacer JOIN: `WHERE id_module IN (modulos del user)`.

Esto excede STM-1527 — sería un epic nuevo de modelado de permisos.

## Archivos potencialmente a tocar (si se implementa)

| Archivo | Cambio |
|---------|--------|
| `src/controllers/parameter.controller.ts` | Leer `req.security`, validar WRN7029 |
| `src/services/parameter.service.ts` | Aceptar `allowedXxx` (a definir según interpretación) |
| `src/repositories/parameter.repo.ts` | Filtro JOIN o WHERE según interpretación |
| `src/middlewares/security.middleware.ts` (util-api) | Verificar si existe; sino, crear gemelo del que tiene finanzas-api |

## Dudas para Ivan (BLOQUEANTES)

1. **¿Qué atributo aplica?** Proveedor / TipoProveedor / GrupoProveedor / otro.
2. **¿Cómo se relaciona un parámetro con un proveedor?** No hay columna directa.
3. **¿Reglas de negocio idénticas al patrón del epic** (5 reglas + WRN7029)? El XML está vacío.
4. **¿Endpoint debe responder distinto según user?** ¿Lista completa filtrada, o "no tienes permiso"?

## Estimación

- **Imposible estimar sin clarificación.**
- Si finalmente es "filtrar por módulo accesible al user": 3-5 SP (modelado nuevo + endpoint + filtros).
- Si es "validar que user tiene atributos y nada más" (puramente formal): 1 SP.

## Referencias

- Entity: [`CatParameter.entity.ts`](../../../APP03022-mrch.backend.somx.util-api/src/entities/CatParameter.entity.ts)
- Routes: [`parameter.routes.ts`](../../../APP03022-mrch.backend.somx.util-api/src/routes/parameter.routes.ts)
- Epic STM-1403 para semántica de atributos: [[project_stm1403_epic]]
