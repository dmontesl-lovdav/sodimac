# Análisis Técnico - Epic STM-1403: Filtro de Seguridad por Atributo de Usuario

**Jiras:** STM-321, STM-323, STM-1461, STM-1474, STM-1524, STM-1525
**Fecha análisis:** 2026-04-20
**Identity Provider:** Keycloak (JWT emitido por Keycloak, front lo administra)
**Modelo BD seguridad:** Pendiente (usuario compartirá imagen del modelo)

---

## 1. Mapeo Corregido de Módulos → Proyectos

| Jira | Módulo | Proyecto | Controlador | Endpoint | Tabla BD |
|------|--------|----------|-------------|----------|----------|
| STM-323 | Facturas | `fiscal-api` :8082 | `InvoiceController.java` | `POST /invoices/search` | `tenant_fiscal.invoice` |
| STM-1474 | Recepción | `fiscal-api` :8082 | `PaymentRegistrationController.java` | `GET /fiscal/complementos-pago/buscar` | `tenant_fiscal` |
| STM-1461 | Carta Porte | `finanzas-api` (Node.js) | `shippingGuide.controller.ts` | `GET /api/shipping-guide` | `tenant_finance.shipping_guide` |
| STM-1524 | Estado de Cuenta | `finanzas-api` (Node.js) | **No encontrado** | ⚠️ Ver dudas | `tenant_finance.account_statement` |
| STM-1525 | Catálogo Proveedor | `catalogos-api` :8083 | `SupplierController.java` | `GET /suppliers` | `shared_catalogs.supplier` |
| STM-321 | Three Way Match | **No encontrado** | **No encontrado** | ⚠️ Ver dudas | `tenant_finance.three_way_match` |

> **Nota Carta Porte**: `fiscal-api` tiene un `RelatedDocumentsController` sobre "documentos relacionados en complementos fiscales". Eso es diferente al listado de guías carta porte que está en `finanzas-api/shippingGuide.controller.ts`.

---

## 2. Campo clave para filtrado por Proveedor

Todas las tablas comparten el campo `vendor_number` (INTEGER) que representa el Proveedor:

```sql
tenant_finance.three_way_match        → vendor_number
tenant_finance.account_statement      → vendor_number
tenant_finance.shipping_guide         → (por confirmar)
tenant_finance.accounts_payable       → vendor_number
tenant_fiscal.invoice                 → via addenda.supplierNumber / numeroProveedor
shared_catalogs.supplier              → supplier_number
```

El filtro base en SQL sería:
```sql
WHERE vendor_number IN (:proveedoresDelUsuario)
-- O si atributo = -1: sin filtro (acceso total)
```

---

## 3. Catálogos relevantes en shared_catalogs.catalog_header

| id | code | name | Uso en estos Jiras |
|----|------|------|-------------------|
| 22 | CatTipoProveedor | Tipo Proveedor | Filtro TipoProveedor |
| 18 | CatEstatusRecepcion | Estatus Recepción | Catálogo estado recepciones |
| 38 | CatEstatusEstadoCuenta | Estatus Estado de Cuenta | Catálogo estado cta |
| 1 | CatEstatusCartaPorte | Estatus Carta Porte | Catálogo estado guías |
| 2 | CatEstatusCartaPorteFBC | Estatus Carta Porte FBC | Catálogo estado guías FBC |

**CatTipoProveedor** valores actuales:
```
TPR001 → Proveedores de mercancía
TPR002 → Proveedores de transporte
TPR003 → Proveedores indirectos
TPR004 → Proveedores de servicios
```

**GrupoProveedor** → NO existe aún en catalog_header. Será parte del nuevo modelo de seguridad.

---

## 4. Infraestructura de Seguridad Actual

### fiscal-api (único con JWT implementado)

**Archivos:**
- [`JwtTokenInterceptor.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/security/JwtTokenInterceptor.java) — parsea JWT Keycloak, crea `Session`
- [`Session.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/security/Session.java) — contexto de usuario
- [`GroupValidator.java`](../../../APP03022-mrch.backend.somx.fiscal-api/src/main/java/com/sodimac/fiscal/api/security/GroupValidator.java) — `@Aspect` valida roles vía `@RequireRole`

**Claims JWT extraídos actualmente:**
```java
// Session.java
String name
String email
List<String> groups   // roles de Keycloak

// FALTA → atributos del nuevo modelo de seguridad:
// proveedor, tipoProveedor, grupoProveedor
```

**Roles configurados:**
```properties
fiscal.jwt.role.operador=operadores
fiscal.jwt.role.proveedor=proveedores
```

### finanzas-api y catalogos-api — SIN JWT

- `finanzas-api`: No tiene interceptor JWT. Usa `logActivity` para auditoría.
- `catalogos-api`: No tiene interceptor JWT. Usa `@RequestHeader("X-User-Id")`.

**Keycloak** es el IdP corporativo. El front administra el token. El back confía en el front.
Opción más probable: el BFF lee el token Keycloak y pasa los atributos por header a los backends.

---

## 5. Modelo de Seguridad — Nuevo Schema `core_security`

Basado en ER compartido (`sesiones/modelo ER.pdf`, sección "Modelo Seguridad").
DDL completo: [01_core_security_schema.sql](./01_core_security_schema.sql)

### Tablas clave para STM-1403

```
core_security.cat_attribute          → define atributos: Proveedor, TipoProveedor, GrupoProveedor
core_security.user_attribute_value   → user_id + attribute_id + value (una fila por valor)
core_security.cat_user               → usuarios con email + user_id_external (sub Keycloak)
```

### Query de filtrado (patrón base)

```sql
-- Obtener valores de atributo para un usuario autenticado
SELECT uav.value
FROM core_security.user_attribute_value uav
JOIN core_security.cat_attribute        ca  ON ca.attribute_id = uav.attribute_id
WHERE uav.user_id    = :userId          -- id interno, resuelto por email del JWT
  AND ca.name        = 'Proveedor'      -- o 'TipoProveedor', 'GrupoProveedor'
  AND uav.status     = 1;

-- Si resultado contiene '-1' → acceso total (sin filtro en ese atributo)
-- Si resultado vacío → retornar WRN7029 (sin atributos configurados)
-- Si múltiples valores → WHERE vendor_number IN (1001, 1002, ...) [OR lógico]
```

### Tablas moradas (en módulo catalogos — pendiente schema destino)
`cat_attribute`, `cat_profile`, `cat_application`, `cat_event`, `application_event`, `profile_application`, `profile_application_event`

### Leyenda del ER
- **Azules** → `core_security` (tablas principales)
- **Moradas** → módulo catálogos (`shared_catalogs` o nuevo esquema, pendiente decisión)

### Datos iniciales insertados
```sql
-- cat_attribute seeds (en DDL)
('Proveedor',      'Número de proveedor permitido')
('TipoProveedor',  'Tipo proveedor permitido (TPR001-TPR004 o -1)')
('GrupoProveedor', 'Grupo de proveedor permitido')
```

---

## 6. Estrategia de implementación por proyecto

### fiscal-api (STM-323 Facturas, STM-1474 Recepción)
1. Agregar campos a `Session.java`: `List<Integer> proveedores`, `List<String> tiposProveedor`, `List<String> gruposProveedor`
2. `JwtTokenInterceptor.java`: extraer esos claims del JWT (o consultar BD de seguridad)
3. `InvoiceSpecification.java`: nuevo método `filterByUserAttributes(Session session)`
4. `InvoiceServiceImpl`: aplicar spec de seguridad si `session.isProveedor()` (no operador)
5. Mismo patrón en servicio de recepciones

### finanzas-api (STM-1461 Carta Porte, STM-1524 Estado de Cuenta)
- `shippingGuide.service.ts`: agregar filtro `vendor_number IN (user.proveedores)` en query
- Middleware o header: recibir atributos del usuario desde BFF o desde JWT si se agrega middleware Keycloak

### catalogos-api (STM-1525 Catálogo Proveedor)
- `SupplierController.java` `GET /suppliers`: filtrar por `supplier_type_id` (TipoProveedor) y `group` (GrupoProveedor)
- Sin `Proveedor` en este Jira → solo TipoProveedor y GrupoProveedor

---

## 7. Dudas pendientes

### Bloqueantes

1. **STM-321 Three Way Match**: La tabla `tenant_finance.three_way_match` existe en BD. ¿Hay endpoint en algún proyecto NO clonado localmente? ¿O es un módulo pendiente de crear?

2. **STM-1524 Estado de Cuenta**: En finanzas-api existe `/api/accounts-payable` → tabla `accounts_payable` (documentos individuales). También existe tabla `account_statement` (resumen mensual por proveedor). ¿Cuál de las dos aplica para STM-1524?

3. **Modelo nuevo de seguridad**: Pendiente imagen del usuario. Necesario saber:
   - ¿Los atributos (proveedor, tipoProveedor, grupoProveedor) vienen en claims del JWT de Keycloak?
   - ¿O se consultan en BD de seguridad con el `email`/`sub` del token?
   - ¿GrupoProveedor es un catalog nuevo a crear en shared_catalogs?

4. **finanzas-api y catalogos-api**: ¿El filtro de seguridad va en el BFF (leen JWT y pasan headers) o se agrega interceptor Keycloak directo en esos servicios?

### Funcionales

5. **Operadores con atributos restringidos**: ¿Los usuarios operador/analista también pueden tener atributos restringidos, o el filtro solo aplica a tipo `proveedor`?

6. **OR entre atributos diferentes**: Múltiples Proveedores = `OR` dentro del mismo atributo. ¿Y entre atributos distintos? ¿`(Proveedor=1001 OR TipoProveedor='NAC')` o es `AND`?

---

## 8. Estado

| Jira | Proyecto | Endpoint | Tabla BD | Nuevo modelo seg. | Listo |
|------|----------|----------|----------|--------------------|-------|
| STM-321 | ❌ No ubicado | ❌ | ✅ three_way_match | ⏳ | ❌ Bloqueado |
| STM-323 | ✅ fiscal-api | ✅ | ✅ invoice | ⏳ | ⚠️ Pendiente modelo |
| STM-1461 | ✅ finanzas-api | ✅ | ✅ shipping_guide | ⏳ | ⚠️ Pendiente modelo |
| STM-1474 | ✅ fiscal-api | ✅ | ✅ tenant_fiscal | ⏳ | ⚠️ Pendiente modelo |
| STM-1524 | ✅ finanzas-api | ⚠️ Por confirmar | ✅ account_statement | ⏳ | ⚠️ Bloqueado |
| STM-1525 | ✅ catalogos-api | ✅ | ✅ supplier | ⏳ | ⚠️ Pendiente modelo |
