# STM-1376: Ajustes al Modelo de Datos para Catalogos y Proveedores

> **Enlace JIRA**: https://jira.empresa.com/browse/STM-1376

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :yellow_circle: En Progreso |
| **Asignado** | [Nombre] |
| **Sprint** | Sprint XX |
| **Fecha Inicio** | 2026-02-23 |
| **Fecha Fin** | - |

---

## Descripcion

Como Analista Funcional, se requiere ajustar y normalizar el modelo de datos relacionado con catalogos y proveedores en el esquema `shared_catalogs`, para asegurar la trazabilidad, consistencia, vigencias y simplificacion del modelo, eliminando tablas obsoletas y agregando campos faltantes.

---

## Analisis Tecnico

### Esquema: `shared_catalogs` (Base de datos: `b2b_portal`)

### Estado Actual de las Tablas (consultado en BD)

**15 objetos** en el esquema:

| Tabla/Vista | Tipo |
|-------------|------|
| `accounting_account` | Tabla (a eliminar) |
| `benefit_center` | Tabla (a eliminar) |
| `catalog_conversion` | Tabla |
| `catalog_detail` | Tabla (a modificar) |
| `catalog_detail_relation` | Tabla (a modificar) |
| `catalog_header` | Tabla (a modificar) |
| `cost_center` | Tabla (a eliminar) |
| `dictionary_lang` | Tabla |
| `payment_condition` | Tabla |
| `status_train` | Tabla |
| `supplier` | Tabla (a modificar) |
| `supplier_block` | Tabla |
| `supplier_type` | Tabla (**NO se elimina** - ver seccion 2.1) |
| `v_catalog_detail_full` | Vista |
| `v_catalog_relations` | Vista |

---

## 1. Campos a Agregar

### 1.1 Tabla `supplier`

**Columnas actuales:**

| Columna | Tipo | Nullable | Default |
|---------|------|----------|---------|
| `id` | integer | NO | nextval(sequence) |
| `supplier_number` | varchar(20) | NO | - |
| `rfc` | varchar(13) | NO | - |
| `business_name` | varchar(255) | NO | - |
| `supplier_type_id` | integer | YES | - |
| `logo` | varchar(500) | YES | - |
| `payment_condition_id` | integer | YES | - |
| `status` | integer | NO | 1 |
| `created_at` | timestamp | NO | CURRENT_TIMESTAMP |
| `created_by` | varchar(100) | YES | - |
| `updated_at` | timestamp | YES | - |
| `updated_by` | varchar(100) | YES | - |

**Analisis de campos solicitados por el JIRA:**

| Campo solicitado | Estado en BD | Accion |
|-----------------|-------------|--------|
| `created_by` | YA EXISTE | Ninguna |
| `updated_at` | YA EXISTE | Ninguna |
| `updated_by` | YA EXISTE | Ninguna |
| `email_financial` (varchar) | **NO EXISTE** | **AGREGAR** |

> **Nota:** Los campos de auditoria (`created_by`, `updated_at`, `updated_by`) ya existen en la tabla. Solo falta agregar `email_financial`.

### 1.2 Tabla `catalog_header`

**Columnas actuales:**

| Columna | Tipo | Nullable | Default |
|---------|------|----------|---------|
| `id` | integer | NO | nextval(sequence) |
| `code` | varchar(64) | NO | - |
| `prefix` | varchar(4) | NO | - |
| `name` | varchar(128) | NO | - |
| `description` | varchar(512) | YES | - |
| `module` | varchar(32) | YES | - |
| `status` | integer | NO | 1 |
| `created_at` | timestamp | NO | CURRENT_TIMESTAMP |
| `updated_at` | timestamp | YES | - |
| `catalog_type` | varchar(20) | NO | 'SIMPLE' |

**Analisis de campos solicitados por el JIRA:**

| Campo solicitado | Estado en BD | Accion |
|-----------------|-------------|--------|
| `created_by` (varchar) | **NO EXISTE** | **AGREGAR** |
| `updated_by` (varchar) | **NO EXISTE** | **AGREGAR** |

### 1.3 Tabla `catalog_detail`

**Columnas actuales (18 columnas):**

| Columna | Tipo | Nullable | Default |
|---------|------|----------|---------|
| `id` | integer | NO | nextval(sequence) |
| `header_id` | integer | NO | - |
| `key` | varchar(64) | NO | - |
| `dict_id` | integer | NO | - |
| `color` | varchar(16) | YES | - |
| `sort_order` | integer | NO | 0 |
| `status` | integer | NO | 1 |
| `created_at` | timestamp | NO | CURRENT_TIMESTAMP |
| `updated_at` | timestamp | YES | - |
| `internal_status` | integer | YES | - |
| `external_key` | varchar(50) | YES | - |
| `value` | varchar(100) | YES | - |
| `valid_from` | date | YES | - |
| `valid_to` | date | YES | - |
| `created_by` | varchar(100) | YES | - |
| `attributes` | jsonb | YES | - |
| `parent_catalog_id` | integer | YES | - |
| `parent_element_id` | integer | YES | - |
| `updated_by` | varchar(100) | YES | - |

**Analisis de campos solicitados por el JIRA:**

| Campo solicitado | Estado en BD | Accion |
|-----------------|-------------|--------|
| `updated_by` (varchar) | YA EXISTE | Ninguna |

> **Nota:** Todos los campos solicitados ya existen en `catalog_detail`. No se requieren cambios.

### 1.4 Tabla `catalog_detail_relation`

**Columnas actuales:**

| Columna | Tipo | Nullable | Default |
|---------|------|----------|---------|
| `id` | integer | NO | nextval(sequence) |
| `source_detail_id` | integer | NO | - |
| `target_detail_id` | integer | NO | - |
| `relation_type` | varchar(20) | NO | 'DEPENDS_ON' |
| `status` | integer | NO | 1 |
| `created_at` | timestamp | NO | CURRENT_TIMESTAMP |
| `created_by` | varchar(100) | YES | - |

**Analisis de campos solicitados por el JIRA:**

| Campo solicitado | Estado en BD | Accion |
|-----------------|-------------|--------|
| `valid_from` (date) | **NO EXISTE** | **AGREGAR** |
| `valid_to` (date) | **NO EXISTE** | **AGREGAR** |
| `updated_by` (varchar) | **NO EXISTE** | **AGREGAR** |
| `updated_at` (timestamp) | **NO EXISTE** | **AGREGAR** |

---

## 2. Tablas a Eliminar

### 2.1 `supplier_type` — NO ES POSIBLE ELIMINAR

#### Definicion Funcional

La tabla `supplier_type` es el **clasificador maestro de tipos de proveedor** del sistema. Define las categorias de negocio bajo las cuales opera cada proveedor, y esta clasificacion determina el flujo operativo, documental y fiscal que le aplica:

| ID | Codigo | Descripcion | Proveedores activos |
|----|--------|-------------|---------------------|
| 1 | `MERCANCIA` | Proveedores de mercancia ODBMS | 11 |
| 2 | `TRANSPORTE` | Proveedores de transporte Carta Porte | 3 |
| 3 | `INDIRECTOS` | Proveedores de insumos SAP | 4 |
| 4 | `SERVICIOS` | Proveedores de servicios | 4 |

#### Justificacion tecnica y funcional de por que NO se puede eliminar

1. **Dependencia de datos activa**: Existen **22 proveedores** en produccion que referencian esta tabla mediante la FK `fk_supplier_type` (`supplier.supplier_type_id` -> `supplier_type.id`). Eliminar la tabla romperia la integridad referencial.

2. **Uso activo en multiples modulos**:
   - **catalogos-api**: `SupplierServiceImpl` valida y asigna el tipo de proveedor en operaciones de creacion y actualizacion de proveedores. Existe endpoint para listar tipos activos.
   - **fiscal-api**: `InvoiceServiceImpl` almacena el `supplierType` en la addenda de facturas y notas de credito.
   - **finanzas-api**: Consulta el tipo de proveedor al obtener informacion de proveedores.

3. **No es una tabla obsoleta**: A diferencia de `accounting_account`, `benefit_center` y `cost_center` (que fueron reemplazadas por el sistema de catalogos generico), `supplier_type` **sigue siendo utilizada activamente** en 3 proyectos y no tiene un reemplazo funcional equivalente en `catalog_detail`.

> **OBSERVACION**: La tabla `supplier_type` actualmente clasifica a los 22 proveedores registrados en 4 tipos de negocio (MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS), y esta vinculada mediante FK activa desde `supplier.supplier_type_id`. Eliminarla implica romper la integridad referencial, refactorizar el modulo de proveedores y perder la clasificacion que diferencia los flujos operativos por tipo.
>
> **PREGUNTA PARA EL EQUIPO FUNCIONAL**: ¿Se desea eliminar la tabla `supplier_type` y quedarnos unicamente con el nombre del proveedor (`business_name`), sin clasificacion por tipo? De ser asi, se requiere definir como se manejaran los flujos diferenciados (facturacion, carta porte, indirectos, servicios) que hoy dependen de esta clasificacion, y se debe considerar como un cambio de alcance mayor que impacta otros modulos.
>
> **DECISION ACTUAL**: Hasta recibir confirmacion, la tabla `supplier_type` **se mantiene sin cambios**. Se excluye del alcance de eliminacion de este JIRA. Solo se eliminaran las 3 tablas restantes (`accounting_account`, `benefit_center`, `cost_center`).

---

### 2.2 `accounting_account` (12 registros)

| Columna | Tipo |
|---------|------|
| `id` | integer (PK) |
| `benefit_center_code` | varchar(20) |
| `sap_branch` | varchar(20) |
| `sodimac_branch` | varchar(20) |
| `description` | varchar(255) |
| `status` | integer |
| `created_at` | timestamp |
| `created_by` | varchar(100) |
| `updated_at` | timestamp |
| `updated_by` | varchar(100) |

**Dependencias:**
- **FK**: Ninguna FK apunta a esta tabla
- **Codigo Java**: Entity `AccountingAccount.java`, DTO `AccountingAccountDto.java`, Repository `AccountingAccountRepository.java`
- **Servicio**: `CenterServiceImpl`
- **Controller**: `CenterController` (endpoints `/centers/accounting/*`)

### 2.3 `benefit_center` (5 registros)

| Columna | Tipo |
|---------|------|
| `id` | integer (PK) |
| `benefit_center_code` | varchar(20) |
| `sap_branch` | varchar(20) |
| `sodimac_branch` | varchar(20) |
| `description` | varchar(255) |
| `status` | integer |
| `created_at` | timestamp |
| `created_by` | varchar(100) |
| `updated_at` | timestamp |
| `updated_by` | varchar(100) |

**Dependencias:**
- **FK**: Ninguna FK apunta a esta tabla
- **Codigo Java**: Entity `BenefitCenter.java`, DTO `BenefitCenterDto.java`, Repository `BenefitCenterRepository.java`
- **Servicio**: `CenterServiceImpl`
- **Controller**: `CenterController` (endpoints `/centers/benefit/*`)

### 2.4 `cost_center` (5 registros)

| Columna | Tipo |
|---------|------|
| `id` | integer (PK) |
| `cost_center_code` | varchar(20) |
| `sap_branch` | varchar(20) |
| `sodimac_branch` | varchar(20) |
| `description` | varchar(255) |
| `status` | integer |
| `created_at` | timestamp |
| `created_by` | varchar(100) |
| `updated_at` | timestamp |
| `updated_by` | varchar(100) |

**Dependencias:**
- **FK**: Ninguna FK apunta a esta tabla
- **Codigo Java**: Entity `CostCenter.java`, DTO `CostCenterDto.java`, Repository `CostCenterRepository.java`
- **Servicio**: `CenterServiceImpl`
- **Controller**: `CenterController` (endpoints `/centers/cost/*`)

---

## 3. Resumen de Impacto en Codigo (catalogos-api)

### Archivos a Eliminar

| Tipo | Archivo | Razon |
|------|---------|-------|
| Entity | `AccountingAccount.java` | Tabla eliminada |
| Entity | `BenefitCenter.java` | Tabla eliminada |
| Entity | `CostCenter.java` | Tabla eliminada |
| DTO | `AccountingAccountDto.java` | Tabla eliminada |
| DTO | `BenefitCenterDto.java` | Tabla eliminada |
| DTO | `CostCenterDto.java` | Tabla eliminada |
| Repository | `AccountingAccountRepository.java` | Tabla eliminada |
| Repository | `BenefitCenterRepository.java` | Tabla eliminada |
| Repository | `CostCenterRepository.java` | Tabla eliminada |
| Service | `CenterServiceImpl.java` | Usa las 3 tablas eliminadas |
| Service | `CenterService.java` (interface) | Servicio eliminado |
| Controller | `CenterController.java` | Expone endpoints de tablas eliminadas |

> **Nota:** `SupplierType.java`, `SupplierTypeDto.java` y `SupplierTypeRepository.java` **se mantienen** ya que la tabla `supplier_type` no se elimina.

### Archivos a Modificar

| Tipo | Archivo | Cambio |
|------|---------|--------|
| Entity | `Supplier.java` | Agregar campo `emailFinancial` |
| Entity | `CatalogHeader.java` | Agregar campos `createdBy`, `updatedBy` |
| Entity | `CatalogDetailRelation.java` | Agregar campos `validFrom`, `validTo`, `updatedBy`, `updatedAt` |

---

## 4. Resumen de Acciones DDL

### Campos a Agregar (ALTER TABLE)

```sql
-- 1. supplier: agregar email_financial
ALTER TABLE shared_catalogs.supplier
    ADD COLUMN email_financial VARCHAR(255);

-- 2. catalog_header: agregar campos de auditoria
ALTER TABLE shared_catalogs.catalog_header
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

-- 3. catalog_detail_relation: agregar vigencias y auditoria
ALTER TABLE shared_catalogs.catalog_detail_relation
    ADD COLUMN valid_from DATE,
    ADD COLUMN valid_to DATE,
    ADD COLUMN updated_by VARCHAR(100),
    ADD COLUMN updated_at TIMESTAMP;
```

### Tablas a Eliminar (DROP TABLE)

> **Nota:** `supplier_type` NO se elimina (ver seccion 2.1).

```sql
-- Eliminar tablas obsoletas (supplier_type se MANTIENE)
DROP TABLE IF EXISTS shared_catalogs.accounting_account CASCADE;
DROP TABLE IF EXISTS shared_catalogs.benefit_center CASCADE;
DROP TABLE IF EXISTS shared_catalogs.cost_center CASCADE;
```

---

## 5. Foreign Keys Completas del Esquema (Referencia)

| Tabla Origen | Columna | Tabla Destino | Columna | Constraint |
|-------------|---------|---------------|---------|------------|
| `catalog_detail` | `header_id` | `catalog_header` | `id` | `fk_catalog_detail_header` |
| `catalog_detail_relation` | `source_detail_id` | `catalog_detail` | `id` | `fk_relation_source` |
| `catalog_detail_relation` | `target_detail_id` | `catalog_detail` | `id` | `fk_relation_target` |
| `catalog_conversion` | `source_element_id` | `catalog_detail` | `id` | `catalog_conversion_source_element_id_fkey` |
| `catalog_conversion` | `target_element_id` | `catalog_detail` | `id` | `catalog_conversion_target_element_id_fkey` |
| `supplier` | `supplier_type_id` | `supplier_type` | `id` | `fk_supplier_type` **(SE MANTIENE)** |
| `supplier` | `payment_condition_id` | `payment_condition` | `id` | `fk_supplier_payment_condition` |

---

## Checklist de Desarrollo

### Analisis
- [x] Requerimientos entendidos
- [x] Analisis tecnico completado (consulta directa a BD)
- [ ] Diseno aprobado por el equipo

### Backend (catalogos-api) — Rama: `feature/STM-1376`
- [x] Script DDL para agregar campos (supplier, catalog_header, catalog_detail_relation) — `15_STM-1376_model_adjustments.sql`
- [x] Script DDL para eliminar tablas (accounting_account, benefit_center, cost_center)
- [x] Script ejecutado en BD local y verificado
- [x] Actualizar Entity `Supplier.java` (agregar emailFinancial)
- [x] Actualizar Entity `CatalogHeader.java` (agregar createdBy, updatedBy)
- [x] Actualizar Entity `CatalogDetailRelation.java` (agregar validFrom, validTo, updatedBy, updatedAt, @PreUpdate)
- [x] Eliminar Entities de tablas obsoletas (AccountingAccount, BenefitCenter, CostCenter)
- [x] Eliminar DTOs de tablas obsoletas (AccountingAccountDto, BenefitCenterDto, CostCenterDto)
- [x] Eliminar Repositories de tablas obsoletas
- [x] Eliminar CenterService, CenterServiceImpl, CenterController, CenterMapper
- [x] Compilacion exitosa (`mvn clean compile`) — sin errores ni imports huerfanos

### BFF (ppsomx.catalogos)
- [x] Eliminar endpoints `/centers/*` de `api.yml`
- [x] Eliminar definiciones de DTOs: CostCenterDto, BenefitCenterDto, AccountingAccountDto

### Testing
- [ ] Pruebas unitarias actualizadas
- [ ] Pruebas de integracion
- [ ] Pruebas en Postman actualizadas
- [ ] QA aprobado

### Despliegue
- [ ] Code Review aprobado
- [ ] Merge a develop
- [ ] Desplegado en DEV
- [ ] Desplegado en QA
- [ ] Desplegado en PROD

### Documentacion
- [x] README de JIRA creado
- [x] Swagger/OpenAPI actualizado (api.yml limpiado)
- [ ] Diagrama ER (MER) actualizado y publicado en Confluence

---

## Notas y Decisiones

| Fecha | Decision | Razon |
|-------|----------|-------|
| 2026-02-23 | Analisis confirma que `created_by`, `updated_at`, `updated_by` ya existen en `supplier` | Campos de auditoria fueron agregados previamente; solo falta `email_financial` |
| 2026-02-23 | `catalog_detail` ya tiene `updated_by` | Campo fue agregado en la evolucion v2 del schema (script `05_schema_evolution_v2.sql`) |
| 2026-02-23 | **`supplier_type` NO se elimina** | Tiene FK activa con 22 proveedores, define logica de negocio por tipo (MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS) y no tiene reemplazo funcional en catalog_detail |
| 2026-02-23 | Solo se eliminan 3 tablas: accounting_account, benefit_center, cost_center | Estas tablas no tienen FK dependientes y fueron reemplazadas por el sistema de catalogos generico |

---

## Problemas Encontrados

### Problema 1: Discrepancia Entity vs BD en AccountingAccount
- **Descripcion**: La Entity `AccountingAccount.java` usa `accountCode` como campo principal, pero en la BD la columna se llama `benefit_center_code`. Esto es una discrepancia que ya existe.
- **Solucion**: No aplica correccion ya que la tabla sera eliminada.
- **Fecha**: 2026-02-23

### Problema 2: `supplier_type` no puede eliminarse
- **Descripcion**: El JIRA solicita eliminar `supplier_type`, pero 22 proveedores activos la referencian con FK, y la clasificacion de tipo de proveedor (MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS) define flujos de negocio diferenciados en facturacion, recepcion y pagos.
- **Solucion**: Se excluye `supplier_type` del alcance de eliminacion. Se mantiene la tabla, su FK, y todos los artefactos Java asociados (Entity, DTO, Repository). Se comunica la desviacion al equipo funcional.
- **Fecha**: 2026-02-23

---

## Criterios de Aceptacion (Validacion)

| CA | Descripcion | Estado |
|----|-------------|--------|
| CA-01 | Todas las tablas reflejan campos agregados con tipos correctos | :yellow_circle: Pendiente |
| CA-02 | Tablas accounting_account, benefit_center, cost_center eliminadas sin referencias residuales. **`supplier_type` se mantiene** (ver justificacion en seccion 2.1) | :yellow_circle: Pendiente |
| CA-03 | Campos created_by, updated_by, created_at, updated_at registran automaticamente en CRUD | :yellow_circle: Pendiente |
| CA-04 | En catalog_detail_relation, valid_from y valid_to usan formato ISO-8601 | :yellow_circle: Pendiente |
| CA-05 | No existen llaves foraneas huerfanas tras los cambios | :yellow_circle: Pendiente |
| CA-06 | Diagrama ER actualizado y publicado en Confluence | :yellow_circle: Pendiente |
| CA-07 | Documentacion tecnica y funcional actualizada | :yellow_circle: Pendiente |
| CA-08 | Servicios validados para evitar fallos | :yellow_circle: Pendiente |

---

## Referencias

- [STM-1166 - Tren de Estatus](../STM-1166/README.md) - Tabla `status_train` en mismo esquema, no afectada
- [Base de Datos](../../BASE-DE-DATOS.md) - Datos de conexion
