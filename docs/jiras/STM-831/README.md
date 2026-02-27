# STM-831: Ajustar servicio del catálogo de proveedores

> **Enlace JIRA**: https://jira.falabella.tech/browse/STM-831

## Estado General

| Campo | Valor |
|-------|-------|
| **Estado** | :large_blue_circle: Code Review |
| **Asignado** | [Nombre] |
| **Sprint** | Sprint XX |
| **Fecha Inicio** | 2026-02-05 |
| **Fecha Fin** | 2026-02-05 |
| **Proyecto** | catalogos-api, bff-catalogos |
| **Avance** | Backend: 100%, DB: 100%, BFF: 100%, Testing: 25% |

---

## Descripcion

**Historia de Usuario**

Como usuario administrador de finanzas y proveedor, quiero realizar ajustes en los servicios del catálogo de proveedores para filtrar la información por tipo de proveedor y proveedores bloqueados, para mejorar la gestión y control de los proveedores en la plataforma.

**Referencia Base**: [STM-832](https://jira.falabella.tech/browse/STM-832)

---

## Reglas de Negocio

### 1. Filtro por Tipo de Proveedor (Obligatorio)

| idTipoProveedor | Código | Descripción |
|-----------------|--------|-------------|
| 0 | - | Todos los proveedores (sin filtrar por tipo) |
| 1 | MERCANCIA | Proveedores de mercancía ODBMS |
| 2 | TRANSPORTE | Proveedores de transporte Carta Porte |
| 3 | INDIRECTOS | Proveedores de insumos SAP |
| 4 | SERVICIOS | Proveedores de servicios |

### 2. Filtro de Proveedor Bloqueado (Obligatorio)

| Valor | Descripción |
|-------|-------------|
| 0 | Solo proveedores bloqueados |
| 1 | Solo proveedores activos (no bloqueados) |
| 2 | Todos (bloqueados y activos) |

### 3. Identificación de Proveedores Bloqueados

Para identificar proveedores bloqueados se debe:
- Realizar **JOIN** con la tabla `supplier_block` cuando:
  - `status = 1` (bloqueo activo)
  - `CURRENT_DATE BETWEEN valid_from AND valid_to` (vigencia activa)

### 4. Manejo de Errores

En caso de enviar un `idTipoProveedor` incorrecto (fuera de 0-4):
- **Catálogo**: `CatMsgNegocio` (header_id=7)
- **Key**: `BUS214`
- **Mensaje**: `No existe el tipo de proveedor [{0}] solicitado.`

---

## Acceptance Criteria

- [x] El servicio permite filtrar proveedores por **tipo de proveedor** (0-4)
- [x] El servicio permite filtrar por **estado de bloqueo** (0, 1, 2)
- [x] El filtro de proveedores bloqueados valida:
  - [x] `status = 1` en supplier_block
  - [x] Vigencia activa del bloqueo (CURRENT_DATE entre valid_from y valid_to)
- [x] El método está documentado con:
  - [x] Nombre del método: `findByTypeAndBlockStatus(tipoProveedor, estatusBloqueo)`
  - [x] Parámetros requeridos: `tipoProveedor` (0-4), `estatusBloqueo` (0-2)
  - [x] Ejemplo de uso: `GET /suppliers/filter?tipoProveedor=1&estatusBloqueo=1`
- [x] La respuesta del servicio:
  - [x] Devuelve solo proveedores que cumplen las condiciones
  - [ ] Tiempo de respuesta ≤ 2 segundos (pendiente validación)
  - [x] Incluye flag `blocked` para indicar si está bloqueado
- [x] El método es accesible para otros módulos (endpoint público REST)
- [x] Validación de tipo de proveedor inválido retorna mensaje BUS214

---

## Especificación Técnica

### Endpoint

```
GET /suppliers/filter
```

### Parámetros

| Parámetro | Tipo | Obligatorio | Descripción | Valores |
|-----------|------|-------------|-------------|---------|
| `tipoProveedor` | Integer | Sí | Tipo de proveedor | 0-4 |
| `estatusBloqueo` | Integer | Sí | Estado de bloqueo | 0, 1, 2 |

### Ejemplo de Uso

```http
# Proveedores de mercancía activos (no bloqueados)
GET /suppliers/filter?tipoProveedor=1&estatusBloqueo=1

# Todos los proveedores bloqueados
GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=0

# Proveedores de transporte (todos, bloqueados y activos)
GET /suppliers/filter?tipoProveedor=2&estatusBloqueo=2
```

### Respuesta Exitosa

```json
[
  {
    "id": 1,
    "supplierNumber": "PROV001",
    "rfc": "ABC123456789",
    "businessName": "Proveedor ABC S.A. de C.V.",
    "supplierType": {
      "id": 1,
      "code": "MERCANCIA",
      "description": "Proveedores de mercancía ODBMS"
    },
    "paymentCondition": {
      "id": 3,
      "conditionName": "30 dias",
      "days": 30
    },
    "status": 1,
    "blocked": false
  },
  {
    "id": 2,
    "supplierNumber": "PROV002",
    "rfc": "XYZ987654321",
    "businessName": "Distribuidora XYZ S.A.",
    "supplierType": {
      "id": 1,
      "code": "MERCANCIA",
      "description": "Proveedores de mercancía ODBMS"
    },
    "paymentCondition": null,
    "status": 1,
    "blocked": true,
    "blockInfo": {
      "validFrom": "2025-12-01",
      "validTo": "2025-12-31",
      "blockReason": "Bloqueo temporal por auditoria"
    }
  }
]
```

### Respuesta de Error

```json
{
  "success": false,
  "code": "BUS214",
  "message": "No existe el tipo de proveedor [5] solicitado."
}
```

---

## Plan de Implementación

### 1. Base de Datos

#### 1.1 Actualizar tipos de proveedor

```sql
-- Archivo: 14_STM-831_supplier_types.sql

-- Eliminar tipos de prueba (NAC, INT, MIX)
DELETE FROM shared_catalogs.supplier_type;

-- Insertar tipos definitivos según STM-831
INSERT INTO shared_catalogs.supplier_type (id, code, description, status, created_by) VALUES
(1, 'MERCANCIA', 'Proveedores de mercancía ODBMS', 1, 'STM-831'),
(2, 'TRANSPORTE', 'Proveedores de transporte Carta Porte', 1, 'STM-831'),
(3, 'INDIRECTOS', 'Proveedores de insumos SAP', 1, 'STM-831'),
(4, 'SERVICIOS', 'Proveedores de servicios', 1, 'STM-831');

-- Resetear secuencia
SELECT setval('shared_catalogs.supplier_type_id_seq', 4, true);
```

#### 1.2 Agregar mensaje de error BUS214

```sql
-- Archivo: 14_STM-831_message_BUS214.sql

-- Agregar traducción (dict_id: 7014)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(7014, 1, 'No existe el tipo de proveedor [{0}] solicitado.'),
(7014, 2, 'Supplier type [{0}] does not exist.'),
(7014, 3, 'O tipo de fornecedor [{0}] não existe.');

-- Agregar al catálogo CatMsgNegocio (header_id=7)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status)
VALUES (7, 'BUS214', 7014, NULL, 214, 1);
```

### 2. Backend (catalogos-api)

#### 2.1 Archivos a Crear/Modificar

| Archivo | Acción | Descripción |
|---------|--------|-------------|
| `SupplierFilterDto.java` | Crear | DTO con campo `blocked` y `blockInfo` |
| `SupplierBlockInfoDto.java` | Crear | DTO para info del bloqueo |
| `SupplierRepository.java` | Modificar | Agregar query con JOIN |
| `SupplierService.java` | Modificar | Agregar método `findByTypeAndBlockStatus()` |
| `SupplierServiceImpl.java` | Modificar | Implementar lógica de filtrado |
| `SupplierController.java` | Modificar | Agregar endpoint `GET /filter` |

#### 2.2 Query JPQL Principal

```java
@Query("""
    SELECT DISTINCT s FROM Supplier s
    LEFT JOIN SupplierBlock sb ON s.supplierNumber = sb.supplierNumber
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.validFrom AND sb.validTo
    WHERE s.status = 1
        AND (:tipoProveedor = 0 OR s.supplierType.id = :tipoProveedor)
        AND (
            CASE :estatusBloqueo
                WHEN 0 THEN sb.id IS NOT NULL
                WHEN 1 THEN sb.id IS NULL
                WHEN 2 THEN true
            END
        )
    """)
List<Supplier> findByTypeAndBlockStatus(
    @Param("tipoProveedor") Integer tipoProveedor,
    @Param("estatusBloqueo") Integer estatusBloqueo
);
```

### 3. BFF (opcional)

Exponer el nuevo endpoint en el BFF de catálogos si es necesario para el frontend.

---

## Checklist de Desarrollo

### Base de Datos
- [x] Crear script `14_STM-831_supplier_types.sql`
- [x] Crear script `14_STM-831_message_BUS214.sql`
- [x] Ejecutar scripts en ambiente DEV
- [x] Verificar tipos de proveedor actualizados
- [x] Verificar mensaje BUS214 creado

### Backend (catalogos-api)
- [x] Crear `SupplierFilterDto.java`
- [x] Crear `SupplierBlockInfoDto.java`
- [x] Agregar query en `SupplierRepository.java`
- [x] Agregar método en `SupplierService.java`
- [x] Implementar lógica en `SupplierServiceImpl.java`
- [x] Agregar endpoint `GET /filter` en `SupplierController.java`
- [x] Validar `tipoProveedor` (0-4)
- [x] Validar `estatusBloqueo` (0-2)
- [x] Manejo de error BUS214
- [x] Documentar Swagger/OpenAPI

### BFF Catalogos
- [x] Agregar endpoint `GET /suppliers/filter` en api.yml
- [x] Agregar DTO `SupplierFilterDto`
- [x] Agregar DTO `SupplierBlockInfoDto`
- [x] Actualizar descripción del API

### Testing
- [ ] Pruebas unitarias
- [ ] Pruebas de integración
- [x] Colección Postman
- [ ] Validación de performance (≤ 2 seg)

### Despliegue
- [ ] Code Review aprobado
- [ ] Merge a develop
- [ ] Desplegado en DEV
- [ ] Desplegado en QA
- [ ] Desplegado en PROD

---

## JIRAs Relacionados

| JIRA | Descripción | Relación | Estado |
|------|-------------|----------|--------|
| **[STM-832](https://jira.falabella.tech/browse/STM-832)** | Historia base | Base de requerimientos | Referencia |
| **[STM-1224](../STM-1224/README.md)** | Bloqueo de Proveedores | Tabla `supplier_block` | :green_circle: Completado |
| **[STM-1225](../STM-1225/README.md)** | API Catálogo Proveedores | Tabla `supplier` | :green_circle: Completado |
| **[STM-1236](../STM-1236/README.md)** | Proveedores en BFF | BFF a extender | :green_circle: Completado |
| **[STM-1252](../STM-1252/README.md)** | Bloqueo en BFF | Lógica de bloqueo | :green_circle: Completado |

---

## Validación en Base de Datos (2026-02-05)

### Estado Actual (Actualizado)

| Tabla | Registros | Nota |
|-------|-----------|------|
| `supplier_type` | 4 | MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS :white_check_mark: |
| `supplier` | 6 | Todos asignados a tipo MERCANCIA por defecto |
| `supplier_block` | 6 | 4 activos, 2 inactivos |
| Mensaje BUS214 | 1 | :white_check_mark: Creado en 3 idiomas |

### Tipos de Proveedor (Actualizados)

| ID | Código | Descripción |
|----|--------|-------------|
| 1 | MERCANCIA | Proveedores de mercancía ODBMS |
| 2 | TRANSPORTE | Proveedores de transporte Carta Porte |
| 3 | INDIRECTOS | Proveedores de insumos SAP |
| 4 | SERVICIOS | Proveedores de servicios |

### Bloqueos Actuales

| ID | Proveedor | Vigencia | Status |
|----|-----------|----------|--------|
| 1 | PROV001 | 2025-01-01 → 2025-06-30 | Activo |
| 2 | PROV001 | 2025-08-01 → 2025-08-31 | Activo |
| 3 | PROV002 | 2025-12-01 → 2025-12-31 | Activo |
| 4 | PROV003 | 2025-01-01 → 2025-12-31 | Activo |
| 5 | PROV004 | 2024-01-01 → 2024-12-31 | Inactivo |
| 6 | PROV005 | 2025-12-20 → 2025-12-25 | Inactivo |

---

## Consultas de Validación en Base de Datos

### Verificar tipos de proveedor

```sql
-- Verificar que existan los 4 tipos de proveedor definitivos
SELECT id, code, description, status, created_by, created_at
FROM shared_catalogs.supplier_type
ORDER BY id;

-- Resultado esperado:
-- 1 | MERCANCIA  | Proveedores de mercancia ODBMS         | 1 | STM-831
-- 2 | TRANSPORTE | Proveedores de transporte Carta Porte  | 1 | STM-831
-- 3 | INDIRECTOS | Proveedores de insumos SAP             | 1 | STM-831
-- 4 | SERVICIOS  | Proveedores de servicios               | 1 | STM-831
```

### Verificar mensaje BUS214

```sql
-- Verificar que el mensaje BUS214 exista en catalog_detail
SELECT cd.key, cd.dict_id, cd.status, cd.sort_order
FROM shared_catalogs.catalog_detail cd
WHERE cd.key = 'BUS214';

-- Verificar las traducciones del mensaje
SELECT cd.key, dl.lang_id, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.key = 'BUS214'
ORDER BY dl.lang_id;

-- Resultado esperado:
-- BUS214 | 1 | No existe el tipo de proveedor [{0}] solicitado.
-- BUS214 | 2 | Supplier type [{0}] does not exist.
-- BUS214 | 3 | O tipo de fornecedor [{0}] nao existe.
```

### Verificar proveedores con su tipo

```sql
-- Ver proveedores con su tipo asignado
SELECT s.id, s.supplier_number, s.business_name,
       st.id as tipo_id, st.code as tipo_code, s.status
FROM shared_catalogs.supplier s
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
ORDER BY s.id;
```

### Verificar bloqueos activos vigentes

```sql
-- Ver bloqueos activos vigentes (status=1 y fecha actual entre valid_from y valid_to)
SELECT sb.id, sb.supplier_number, sb.valid_from, sb.valid_to,
       sb.block_reason, sb.status,
       CASE WHEN CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
            THEN 'VIGENTE' ELSE 'NO VIGENTE' END as vigencia
FROM shared_catalogs.supplier_block sb
WHERE sb.status = 1
ORDER BY sb.supplier_number, sb.valid_from;
```

### Simular filtrado de proveedores

```sql
-- Simulacion: Proveedores de tipo MERCANCIA (1) que NO estan bloqueados
SELECT s.id, s.supplier_number, s.business_name, st.code as tipo,
       CASE WHEN EXISTS (
           SELECT 1 FROM shared_catalogs.supplier_block sb
           WHERE sb.supplier_number = s.supplier_number
             AND sb.status = 1
             AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
       ) THEN 'BLOQUEADO' ELSE 'ACTIVO' END as estado_bloqueo
FROM shared_catalogs.supplier s
JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
  AND st.id = 1  -- MERCANCIA
  AND NOT EXISTS (
      SELECT 1 FROM shared_catalogs.supplier_block sb
      WHERE sb.supplier_number = s.supplier_number
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  )
ORDER BY s.id;
```

---

## Scripts SQL Ejecutados

### Script 1: 14_STM-831_supplier_types.sql

```sql
-- ============================================================================
-- STM-831: Actualizar tipos de proveedor
-- Fecha: 2026-02-05
-- Descripcion: Reemplazar tipos de proveedor de prueba por tipos definitivos
-- ============================================================================

SET search_path TO shared_catalogs;

-- PASO 1: Desasociar proveedores existentes del tipo
UPDATE shared_catalogs.supplier SET supplier_type_id = NULL WHERE supplier_type_id IS NOT NULL;

-- PASO 2: Eliminar tipos de prueba (NAC, INT, MIX)
DELETE FROM shared_catalogs.supplier_type;

-- PASO 3: Insertar tipos de proveedor definitivos segun STM-831
INSERT INTO shared_catalogs.supplier_type (id, code, description, status, created_by, created_at) VALUES
(1, 'MERCANCIA', 'Proveedores de mercancia ODBMS', 1, 'STM-831', NOW()),
(2, 'TRANSPORTE', 'Proveedores de transporte Carta Porte', 1, 'STM-831', NOW()),
(3, 'INDIRECTOS', 'Proveedores de insumos SAP', 1, 'STM-831', NOW()),
(4, 'SERVICIOS', 'Proveedores de servicios', 1, 'STM-831', NOW());

-- PASO 4: Resetear secuencia para nuevos registros
SELECT setval('shared_catalogs.supplier_type_id_seq', 4, true);

-- PASO 5: Asignar tipo por defecto a proveedores existentes (MERCANCIA)
UPDATE shared_catalogs.supplier SET supplier_type_id = 1 WHERE supplier_type_id IS NULL;
```

### Script 2: 14_STM-831_message_BUS214.sql

```sql
-- ============================================================================
-- STM-831: Agregar mensaje de error BUS214
-- Fecha: 2026-02-05
-- Descripcion: Mensaje de error para tipo de proveedor invalido
-- dict_id: 7014
-- ============================================================================

SET search_path TO shared_catalogs;

-- Insertar traducciones para dict_id 7014
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(7014, 1, 'No existe el tipo de proveedor [{0}] solicitado.'),
(7014, 2, 'Supplier type [{0}] does not exist.'),
(7014, 3, 'O tipo de fornecedor [{0}] nao existe.');

-- Insertar en catalog_detail (header_id=7 = CatMsgNegocio)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, color, sort_order, status)
VALUES (7, 'BUS214', 7014, NULL, 214, 1);
```

---

## Archivos del Proyecto

### Scripts SQL (creados)
- `src/main/resources/db/14_STM-831_supplier_types.sql` :white_check_mark:
- `src/main/resources/db/14_STM-831_message_BUS214.sql` :white_check_mark:

### Backend (creados/modificados)
- `src/main/java/com/sodimac/catman/api/model/dto/SupplierFilterDto.java` :white_check_mark:
- `src/main/java/com/sodimac/catman/api/model/dto/SupplierBlockInfoDto.java` :white_check_mark:
- `src/main/java/com/sodimac/catman/api/repository/SupplierRepository.java` :white_check_mark:
- `src/main/java/com/sodimac/catman/api/service/SupplierService.java` :white_check_mark:
- `src/main/java/com/sodimac/catman/api/service/SupplierServiceImpl.java` :white_check_mark:
- `src/main/java/com/sodimac/catman/api/controller/SupplierController.java` :white_check_mark:
- `src/main/java/com/sodimac/catman/api/mapper/SupplierMapper.java` :white_check_mark:

### Colección Postman (creada)
- [STM-831-Filtrado-Proveedores.postman_collection.json](./STM-831-Filtrado-Proveedores.postman_collection.json) :white_check_mark:

### BFF Catalogos (actualizado)
- `APP03022-mrch.bff.somx.ppsomx.catalogos/api.yml` :white_check_mark:
  - Nuevo endpoint: `GET /suppliers/filter`
  - Nuevos DTOs: `SupplierFilterDto`, `SupplierBlockInfoDto`

---

## Ejemplos de Respuesta del Servicio

### Respuesta Exitosa - Proveedores activos (no bloqueados)

**Request:**
```http
GET /suppliers/filter?tipoProveedor=1&estatusBloqueo=1
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "supplierNumber": "PROV001",
    "rfc": "ABC123456789",
    "businessName": "Proveedor ABC S.A. de C.V.",
    "supplierType": {
      "id": 1,
      "code": "MERCANCIA",
      "description": "Proveedores de mercancia ODBMS"
    },
    "paymentCondition": {
      "id": 1,
      "conditionName": "30 dias",
      "days": 30
    },
    "status": 1,
    "blocked": false,
    "blockInfo": null
  }
]
```

### Respuesta Exitosa - Proveedor bloqueado

**Request:**
```http
GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=0
```

**Response:** `200 OK`
```json
[
  {
    "id": 2,
    "supplierNumber": "PROV002",
    "rfc": "XYZ987654321",
    "businessName": "Distribuidora XYZ S.A.",
    "supplierType": {
      "id": 1,
      "code": "MERCANCIA",
      "description": "Proveedores de mercancia ODBMS"
    },
    "paymentCondition": null,
    "status": 1,
    "blocked": true,
    "blockInfo": {
      "validFrom": "2025-12-01",
      "validTo": "2025-12-31",
      "blockReason": "Bloqueo temporal por auditoria"
    }
  }
]
```

### Respuesta de Error - Tipo de proveedor inválido

**Request:**
```http
GET /suppliers/filter?tipoProveedor=5&estatusBloqueo=1
```

**Response:** `400 Bad Request`
```json
{
  "success": false,
  "code": "BUS214",
  "message": "No existe el tipo de proveedor [5] solicitado.",
  "timestamp": "2026-02-05T10:30:00.000Z"
}
```

### Respuesta Vacía - Sin proveedores que coincidan

**Request:**
```http
GET /suppliers/filter?tipoProveedor=4&estatusBloqueo=0
```

**Response:** `200 OK`
```json
[]
```

---

## Notas y Decisiones

| Fecha | Decisión | Razón |
|-------|----------|-------|
| 2026-02-05 | Reemplazar tipos NAC/INT/MIX por tipos definitivos | Tipos actuales eran de prueba |
| 2026-02-05 | Usar código BUS214 en lugar de PAR2015 | Seguir convención de mensajes existente (prefijo BUS para negocio) |
| 2026-02-05 | Crear endpoint `/filter` en lugar de modificar `/suppliers` | Mantener compatibilidad hacia atrás |

---

## Respuesta para JIRA

### Comentario de Resolucion

```
Se implemento el endpoint de filtrado de proveedores segun los requerimientos de STM-831.

**Endpoint implementado:**
GET /suppliers/filter?tipoProveedor={0-4}&estatusBloqueo={0-2}

**Parametros:**
- tipoProveedor: 0=Todos, 1=Mercancia, 2=Transporte, 3=Indirectos, 4=Servicios
- estatusBloqueo: 0=Solo bloqueados, 1=Solo activos, 2=Todos

**Cambios realizados:**

1. Base de datos:
   - Actualizados tipos de proveedor (MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS)
   - Agregado mensaje de error BUS214 para tipo invalido

2. Backend (catalogos-api):
   - Nuevo endpoint GET /suppliers/filter
   - DTOs: SupplierFilterDto, SupplierBlockInfoDto
   - Validacion de bloqueo: status=1 AND fecha actual entre valid_from y valid_to
   - Commit: a92871f

3. BFF (catalogos):
   - Actualizado api.yml con nuevo endpoint y DTOs
   - Commit: 70f32f0

**Pruebas realizadas:**
- Filtrado por tipo de proveedor (0-4)
- Filtrado por estado de bloqueo (0-2)
- Validacion de error BUS214 para tipo invalido
- Verificacion de respuesta con blockInfo para proveedores bloqueados

**Pendiente:**
- Pruebas unitarias
- Validacion de performance (< 2 seg)
- Despliegue en ambientes
```

---

## Consultas de Validacion Comparativas

Las siguientes consultas permiten validar que los resultados del endpoint coincidan con los datos en base de datos.

### 1. Validar Total de Proveedores (tipoProveedor=0, estatusBloqueo=2)

**Endpoint:**
```bash
curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=2" | jq 'length'
```

**SQL equivalente:**
```sql
-- Contar todos los proveedores activos
SELECT COUNT(*) as total
FROM shared_catalogs.supplier s
WHERE s.status = 1;
```

**Validacion:** El numero del endpoint debe coincidir con el COUNT de la consulta SQL.

---

### 2. Validar Proveedores Bloqueados (tipoProveedor=0, estatusBloqueo=0)

**Endpoint:**
```bash
curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=0" | jq '.[].supplierNumber'
```

**SQL equivalente:**
```sql
-- Proveedores con bloqueo activo vigente
SELECT DISTINCT s.supplier_number
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
WHERE s.status = 1
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
ORDER BY s.supplier_number;
```

**Validacion:** Los supplier_number del endpoint deben coincidir exactamente con los de la consulta SQL.

---

### 3. Validar Proveedores Activos (tipoProveedor=0, estatusBloqueo=1)

**Endpoint:**
```bash
curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=1" | jq 'length'
```

**SQL equivalente:**
```sql
-- Proveedores SIN bloqueo activo vigente
SELECT COUNT(*) as total
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND NOT EXISTS (
      SELECT 1 FROM shared_catalogs.supplier_block sb
      WHERE sb.supplier_number = s.supplier_number
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  );
```

**Validacion:** Total endpoint = Total SQL

---

### 4. Validar Proveedores por Tipo (tipoProveedor=1, estatusBloqueo=2)

**Endpoint:**
```bash
curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=1&estatusBloqueo=2" | jq 'length'
```

**SQL equivalente:**
```sql
-- Proveedores de tipo MERCANCIA (id=1)
SELECT COUNT(*) as total
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 1;
```

**Validacion:** Repetir para cada tipo (1-4)

---

### 5. Validar Detalle de Bloqueo

**Endpoint:**
```bash
curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=0" | jq '.[] | {supplierNumber, blocked, validFrom: .blockInfo.validFrom, validTo: .blockInfo.validTo, reason: .blockInfo.blockReason}'
```

**SQL equivalente:**
```sql
-- Detalle de bloqueos vigentes
SELECT s.supplier_number, sb.valid_from, sb.valid_to, sb.block_reason
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
WHERE s.status = 1
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
ORDER BY s.supplier_number;
```

**Validacion:** Las fechas y razones de bloqueo deben coincidir.

---

### 6. Validar Error BUS214

**Endpoint:**
```bash
curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=5&estatusBloqueo=1"
```

**Respuesta esperada:**
```json
{"message":"No existe el tipo de proveedor [5] solicitado.","code":400}
```

**SQL para verificar mensaje:**
```sql
SELECT dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.key = 'BUS214' AND dl.lang_id = 1;
```

---

### 7. Resumen de Validacion Rapida

Ejecutar estos comandos para validacion rapida:

```bash
# 1. Total proveedores
echo "Total proveedores:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=2" | grep -o '"id"' | wc -l

# 2. Proveedores bloqueados
echo "Bloqueados:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=0" | grep -o '"id"' | wc -l

# 3. Proveedores activos
echo "Activos:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=0&estatusBloqueo=1" | grep -o '"id"' | wc -l

# 4. Por tipo
echo "Mercancia:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=1&estatusBloqueo=2" | grep -o '"id"' | wc -l
echo "Transporte:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=2&estatusBloqueo=2" | grep -o '"id"' | wc -l
echo "Indirectos:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=3&estatusBloqueo=2" | grep -o '"id"' | wc -l
echo "Servicios:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=4&estatusBloqueo=2" | grep -o '"id"' | wc -l

# 5. Error BUS214
echo "Error BUS214:" && curl -s "http://localhost:8083/suppliers/filter?tipoProveedor=99&estatusBloqueo=1"
```

---

### 8. Script SQL de Validacion Completa

```sql
-- ============================================================================
-- STM-831: Script de validacion completa
-- Ejecutar despues de insertar datos de prueba
-- ============================================================================

-- 1. Resumen de proveedores por tipo
SELECT 'PROVEEDORES POR TIPO' as seccion;
SELECT st.code as tipo, COUNT(*) as total
FROM shared_catalogs.supplier s
JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
GROUP BY st.code
ORDER BY st.code;

-- 2. Resumen de bloqueos vigentes
SELECT 'BLOQUEOS VIGENTES' as seccion;
SELECT COUNT(*) as total_bloqueados
FROM (
    SELECT DISTINCT s.supplier_number
    FROM shared_catalogs.supplier s
    INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
    WHERE s.status = 1 AND sb.status = 1
      AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
) sub;

-- 3. Detalle de proveedores bloqueados
SELECT 'DETALLE BLOQUEADOS' as seccion;
SELECT s.supplier_number, s.business_name, sb.block_reason,
       sb.valid_from, sb.valid_to
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
WHERE s.status = 1 AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
ORDER BY s.supplier_number;

-- 4. Totales esperados para validacion
SELECT 'TOTALES ESPERADOS' as seccion;
SELECT
    (SELECT COUNT(*) FROM shared_catalogs.supplier WHERE status = 1) as total_proveedores,
    (SELECT COUNT(DISTINCT s.supplier_number)
     FROM shared_catalogs.supplier s
     INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
     WHERE s.status = 1 AND sb.status = 1
       AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to) as total_bloqueados,
    (SELECT COUNT(*) FROM shared_catalogs.supplier s
     WHERE s.status = 1
       AND NOT EXISTS (
           SELECT 1 FROM shared_catalogs.supplier_block sb
           WHERE sb.supplier_number = s.supplier_number
             AND sb.status = 1
             AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
       )) as total_activos;
```

---

## Referencias

- [STM-832 - Historia Base](https://jira.falabella.tech/browse/STM-832)
- [Documentacion catalogos-api](../../APP03022-mrch.backend.somx.catalogos-api/README.md)
