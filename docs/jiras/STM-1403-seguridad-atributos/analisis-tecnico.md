# Análisis Técnico - Epic STM-1403: Filtro de Seguridad por Atributo de Usuario

**Jiras:** STM-321, STM-323, STM-1461, STM-1474, STM-1524, STM-1525
**Fecha análisis:** 2026-04-20
**Estado esquema de seguridad:** NO implementado aún (pendiente de corporativo)

---

## 1. Mapeo de Módulos a Proyectos

| Jira | Módulo | Proyecto | Controlador | Endpoint actual |
|------|--------|----------|-------------|-----------------|
| STM-323 | Facturas | `fiscal-api` :8082 | `InvoiceController.java` | `POST /invoices/search` |
| STM-1474 | Recepción | `fiscal-api` :8082 | `PaymentRegistrationController.java` | `GET /fiscal/complementos-pago/buscar` |
| STM-1461 | Carta Porte | `fiscal-api` :8082 | `RelatedDocumentsController.java` | `GET /related-documents` |
| STM-1524 | Estado de Cuenta | `finanzas-api` (Node.js) | `accountsPayable.controller.ts` | `GET /api/accounts-payable` |
| STM-1525 | Catálogo Proveedor | `catalogos-api` :8083 | `SupplierController.java` | `GET /suppliers` |
| STM-321 | Three Way Match | **NO ENCONTRADO** | — | — |

---

## 2. Infraestructura de Seguridad Actual

### fiscal-api (único con JWT implementado)

**Archivos clave:**

- [`JwtTokenInterceptor.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/security/JwtTokenInterceptor.java) — intercepta todas las requests, parsea JWT, crea objeto `Session`
- [`Session.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/security/Session.java) — contenedor del contexto de usuario
- [`GroupValidator.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/security/GroupValidator.java) — `@Aspect` que valida roles via `@RequireRole`

**Campos actuales del JWT extraídos:**
```
name    → Session.name
email   → Session.email
groups  → Session.groups (List<String>)
```

**Lo que FALTA en Session para estos Jiras:**
```
proveedor      → no existe
tipoProveedor  → no existe
grupoProveedor → no existe
```

### finanzas-api (Node.js — SIN JWT)
- No tiene middleware JWT
- Usa `X-User-Id` header para auditoría únicamente
- No tiene contexto de usuario autenticado

### catalogos-api (Java — SIN JWT)
- No tiene `JwtTokenInterceptor`
- Usa `@RequestHeader("X-User-Id")` para auditoría únicamente
- No tiene `Session` ni `GroupValidator`

---

## 3. Base de Datos — Esquema de Atributos

### shared_catalogs (catalogos-api PostgreSQL)

**Catálogo de catálogos** = tablas `catalog_header` + `catalog_detail`
- `catalog_header` → define cada catálogo (ej: TipoProveedor, GrupoProveedor)
- `catalog_detail` → valores de cada catálogo
- `catalog_detail.attributes` → campo JSONB para atributos extendidos (GIN index)

**Tabla de proveedores:**
```sql
-- shared_catalogs.supplier
id, supplier_number, rfc, business_name, supplier_type_id, status...

-- shared_catalogs.supplier_type
id, code (NAC/INT/MIX), name, status...
```

### ¿Tabla de atributos de usuario?
**NO ENCONTRADA** en ningún esquema local.

No existe tabla que relacione `usuario → proveedor/tipoProveedor/grupoProveedor`.
El Jira dice que el esquema de seguridad aún no está creado.

---

## 4. Flujo esperado (una vez que el esquema esté listo)

```
Request → BFF → fiscal-api
                  ↓
          JwtTokenInterceptor
          parsea JWT → Session {
              name, email, groups,
              [proveedor],        ← NUEVO
              [tipoProveedor],    ← NUEVO
              [grupoProveedor]    ← NUEVO
          }
                  ↓
          Controller → Service → Specification
                                  ↓
                          WHERE proveedor IN (user.proveedores)
                           AND tipoProveedor IN (user.tipoProveedores)
                           OR -1 = skip filter for that attribute
```

### Reglas a implementar por código:
- Si atributo == `-1` → no aplicar filtro (acceso total a ese atributo)
- Si usuario tiene múltiples valores → `OR` lógico
- Si usuario sin atributos configurados → retornar `WRN7029`

---

## 5. Cambios técnicos necesarios por proyecto

### A) fiscal-api (STM-323, STM-1461, STM-1474)

1. **`Session.java`** — agregar campos `proveedor`, `tipoProveedor`, `grupoProveedor` (List)
2. **`JwtTokenInterceptor.java`** — extraer esos claims del JWT
3. **`InvoiceSpecification.java`** — agregar filtros por atributos del usuario autenticado
4. **Servicio de facturas** — aplicar especificación de seguridad si usuario es proveedor (no operador)
5. **`RelatedDocumentsController`** / servicio carta porte — mismo patrón
6. **`PaymentRegistrationController`** / servicio recepciones — mismo patrón

### B) catalogos-api (STM-1525 — Catálogo Proveedor)
- **SupplierController.java** `GET /suppliers`
- Actualmente sin JWT — necesitará interceptor o recibirá atributos vía header desde BFF
- Filtrar por `supplier_type_id` (TipoProveedor) y grupo (GrupoProveedor)

### C) finanzas-api (STM-1524 — Estado de Cuenta)
- **accountsPayable.controller.ts** `GET /api/accounts-payable`
- Actualmente sin JWT — mismo dilema que catalogos-api
- Requiere definir si el filtro se aplica en finanzas-api o en la capa BFF

### D) ??? (STM-321 — Three Way Match)
- **No encontrado** en ningún proyecto local
- Requiere aclaración (ver dudas)

---

## 6. DUDAS Y PREGUNTAS PARA EL EQUIPO

### Críticas (bloquean inicio de implementación)

1. **Three Way Match (STM-321)**: ¿En qué proyecto/repositorio está el backend del three way match? No se encontró endpoint ni controlador en ningún proyecto clonado.

2. **Origen de los atributos en JWT**: ¿Los atributos Proveedor, TipoProveedor y GrupoProveedor del usuario vienen dentro del payload del token JWT, o se deben consultar en una tabla de BD a partir del `email`/`userId` del token?

3. **Tabla de atributos de usuario**: ¿Ya existe o está pendiente de crear? El Jira menciona "catálogo de catálogos" — ¿es el `catalog_header/catalog_detail` de catalogos-api o una tabla nueva del esquema de seguridad?

4. **finanzas-api y catalogos-api sin JWT**: Estos proyectos no tienen interceptor JWT. ¿El filtro de seguridad se implementa:
   - a) Agregando JWT a esos proyectos (interceptor nuevo), o
   - b) En la capa BFF (bff.finanzas, bff.catalogos) leyendo el token y pasando los atributos por header?

### Funcionales

5. **Estado de Cuenta = accounts-payable**: ¿El módulo "estado de cuenta" corresponde al endpoint `GET /api/accounts-payable` de finanzas-api, o hay otro endpoint/módulo?

6. **Catálogo Proveedor (STM-1525) sin atributo Proveedor**: Este Jira solo filtra por TipoProveedor y GrupoProveedor (no por Proveedor). ¿Es intencional? Tiene sentido si el catálogo proveedor es la fuente de datos de proveedores.

7. **Roles operador vs proveedor**: El GroupValidator ya distingue `operador` vs `proveedor`. ¿El filtro de atributos aplica SOLO a usuarios tipo proveedor, o también a operadores/analistas con atributos restringidos?

8. **Múltiples proveedores**: Si un usuario proveedor tiene 3 valores de `Proveedor` (ej: 1001, 1002, 1003), ¿el OR lógico aplica dentro de Proveedor, o también entre Proveedor y TipoProveedor? Ejemplo: ¿`(Proveedor IN (1001,1002) OR TipoProveedor IN ('NAC'))`?

---

## 7. Estado de implementación

| Jira | Proyecto identificado | Endpoint encontrado | Esquema BD | Listo para implementar |
|------|-----------------------|--------------------|-----------|-----------------------|
| STM-321 | ❌ No encontrado | ❌ | ❌ | ❌ Bloqueado |
| STM-323 | ✅ fiscal-api | ✅ `/invoices/search` | ⚠️ Pendiente tabla users-attrs | ⚠️ Pendiente esquema |
| STM-1461 | ✅ fiscal-api | ✅ `/related-documents` | ⚠️ Pendiente tabla users-attrs | ⚠️ Pendiente esquema |
| STM-1474 | ✅ fiscal-api | ✅ `/fiscal/complementos-pago/buscar` | ⚠️ Pendiente tabla users-attrs | ⚠️ Pendiente esquema |
| STM-1524 | ✅ finanzas-api | ✅ `/api/accounts-payable` | ⚠️ Pendiente tabla users-attrs | ⚠️ Pendiente JWT/filtro |
| STM-1525 | ✅ catalogos-api | ✅ `/suppliers` | ⚠️ Pendiente tabla users-attrs | ⚠️ Pendiente JWT/filtro |
