# STM-831: Descripcion Funcional y Tecnica

## Resumen Ejecutivo

**JIRA:** STM-831 - Ajustar servicio del catalogo de proveedores
**Fecha:** 2026-02-05
**Estado:** Completado

---

## 1. Descripcion Funcional

### 1.1 Necesidad del Negocio

El sistema de gestion de proveedores necesitaba una forma de **filtrar proveedores** segun dos criterios principales:

#### a) Por Tipo de Proveedor

Los proveedores se clasifican en 4 categorias segun su funcion dentro de la empresa:

| Tipo | Descripcion | Uso en el Negocio |
|------|-------------|-------------------|
| **MERCANCIA** | Proveedores de productos ODBMS | Compra de mercancia para venta en tiendas |
| **TRANSPORTE** | Proveedores de logistica Carta Porte | Servicios de transporte, envio y distribucion |
| **INDIRECTOS** | Proveedores de insumos SAP | Materiales de oficina, limpieza, consumibles |
| **SERVICIOS** | Proveedores de servicios varios | Consultoria, mantenimiento, seguridad, etc. |

#### b) Por Estado de Bloqueo

Un proveedor puede estar **bloqueado temporalmente** por diversas razones:

- Incumplimiento de contrato
- Documentacion incompleta (ej: Carta Porte)
- Problemas de facturacion
- Auditorias pendientes
- Deudas o pagos atrasados

**Importante:** El bloqueo tiene una **vigencia** (fecha inicio y fecha fin). Un proveedor solo se considera bloqueado si:
1. El registro de bloqueo esta activo (`status = 1`)
2. La fecha actual esta dentro del rango de vigencia (`CURRENT_DATE BETWEEN valid_from AND valid_to`)

### 1.2 Casos de Uso

| Usuario | Necesidad | Filtro a Usar |
|---------|-----------|---------------|
| Comprador | Ver proveedores de mercancia disponibles para hacer pedidos | tipoProveedor=1, estatusBloqueo=1 |
| Logistica | Ver transportistas disponibles para asignar envios | tipoProveedor=2, estatusBloqueo=1 |
| Finanzas | Ver todos los proveedores bloqueados para dar seguimiento | tipoProveedor=0, estatusBloqueo=0 |
| Auditoria | Ver todos los proveedores de servicios (bloqueados y activos) | tipoProveedor=4, estatusBloqueo=2 |
| Administrador | Ver el catalogo completo de proveedores | tipoProveedor=0, estatusBloqueo=2 |

### 1.3 Validacion de Errores

Si un usuario solicita un tipo de proveedor que no existe (ej: tipo 5, 10, -1), el sistema responde con un mensaje claro:

> *"No existe el tipo de proveedor [5] solicitado."*

Este mensaje esta disponible en 3 idiomas:
- Espanol (lang_id=1)
- Ingles (lang_id=2)
- Portugues (lang_id=3)

---

## 2. Implementacion Tecnica

### 2.1 Endpoint Creado

```
GET /suppliers/filter?tipoProveedor={0-4}&estatusBloqueo={0-2}
```

### 2.2 Parametros del Endpoint

| Parametro | Tipo | Obligatorio | Valores | Descripcion |
|-----------|------|-------------|---------|-------------|
| `tipoProveedor` | Integer | Si | 0-4 | Tipo de proveedor a filtrar |
| `estatusBloqueo` | Integer | Si | 0-2 | Estado de bloqueo a filtrar |

**Valores de tipoProveedor:**
| Valor | Significado |
|-------|-------------|
| 0 | Todos los tipos |
| 1 | MERCANCIA |
| 2 | TRANSPORTE |
| 3 | INDIRECTOS |
| 4 | SERVICIOS |

**Valores de estatusBloqueo:**
| Valor | Significado |
|-------|-------------|
| 0 | Solo proveedores bloqueados |
| 1 | Solo proveedores activos (no bloqueados) |
| 2 | Todos (bloqueados y activos) |

### 2.3 Arquitectura del Sistema

```
┌─────────────────┐
│    Frontend     │
│   (Angular)     │
└────────┬────────┘
         │ HTTP
         ▼
┌─────────────────┐
│      BFF        │
│  (Node.js)      │
│  Puerto: 3000   │
│  api.yml        │
└────────┬────────┘
         │ HTTP
         ▼
┌─────────────────┐
│  catalogos-api  │
│  (Spring Boot)  │
│  Puerto: 8083   │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────────────────────┐
│         PostgreSQL              │
│         Puerto: 5434            │
│  ┌────────────────────────────┐ │
│  │   shared_catalogs          │ │
│  │  ├─ supplier               │ │
│  │  ├─ supplier_type          │ │
│  │  ├─ supplier_block         │ │
│  │  ├─ catalog_detail         │ │
│  │  └─ dictionary_lang        │ │
│  └────────────────────────────┘ │
└─────────────────────────────────┘
```

### 2.4 Tablas Involucradas

| Tabla | Proposito | Columnas Clave |
|-------|-----------|----------------|
| `supplier` | Datos del proveedor | id, supplier_number, business_name, supplier_type_id, status |
| `supplier_type` | Catalogo de tipos | id, code (MERCANCIA, TRANSPORTE, etc.), description |
| `supplier_block` | Bloqueos de proveedores | supplier_number, valid_from, valid_to, block_reason, status |
| `catalog_detail` | Mensajes del sistema | key (BUS214), dict_id |
| `dictionary_lang` | Traducciones | dict_id, lang_id, description |

### 2.5 Logica de Bloqueo (Pseudocodigo)

```
Para cada proveedor:
    1. Buscar en supplier_block donde:
       - supplier_number = proveedor.supplier_number
       - status = 1 (bloqueo activo)
       - CURRENT_DATE >= valid_from
       - CURRENT_DATE <= valid_to

    2. Si encuentra registros:
       - proveedor.blocked = true
       - proveedor.blockInfo = { validFrom, validTo, blockReason }

    3. Si NO encuentra registros:
       - proveedor.blocked = false
       - proveedor.blockInfo = null
```

### 2.6 Archivos Creados/Modificados

#### Backend (catalogos-api)

| Archivo | Tipo | Descripcion |
|---------|------|-------------|
| `SupplierFilterDto.java` | Nuevo | DTO de respuesta con campos blocked y blockInfo |
| `SupplierBlockInfoDto.java` | Nuevo | DTO con detalles del bloqueo (fechas, razon) |
| `SupplierRepository.java` | Modificado | Query findByTypeFilter(tipoProveedor) |
| `SupplierService.java` | Modificado | Interfaz con metodo findByTypeAndBlockStatus() |
| `SupplierServiceImpl.java` | Modificado | Logica de filtrado y validacion |
| `SupplierController.java` | Modificado | Endpoint GET /filter |
| `SupplierMapper.java` | Modificado | Metodos toFilterDto() y toBlockInfoDto() |

#### Base de Datos (Scripts SQL)

| Script | Proposito |
|--------|-----------|
| `14_STM-831_supplier_types.sql` | Actualizar tipos de proveedor (MERCANCIA, TRANSPORTE, INDIRECTOS, SERVICIOS) |
| `14_STM-831_message_BUS214.sql` | Agregar mensaje de error para tipo invalido |
| `15_STM-831_test_data.sql` | Datos de prueba (16 proveedores, 3 bloqueos) |
| `16_STM-831_validacion.sql` | Consultas para validar resultados |

#### BFF (Catalogos)

| Archivo | Cambio |
|---------|--------|
| `api.yml` | Nuevo endpoint GET /suppliers/filter con DTOs |

### 2.7 Estructura de Respuesta

#### Proveedor Activo (no bloqueado)

```json
{
  "id": 1,
  "supplierNumber": "MERC001",
  "rfc": "MER010101ABC",
  "businessName": "Distribuidora de Materiales del Norte S.A.",
  "supplierType": {
    "id": 1,
    "code": "MERCANCIA",
    "description": "Proveedores de mercancia ODBMS"
  },
  "paymentCondition": {
    "id": 3,
    "conditionName": "30 dias",
    "days": 30
  },
  "status": 1,
  "blocked": false,
  "blockInfo": null
}
```

#### Proveedor Bloqueado

```json
{
  "id": 8,
  "supplierNumber": "MERC002",
  "rfc": "MER020202DEF",
  "businessName": "Comercializadora Industrial Maya S.A. de C.V.",
  "supplierType": {
    "id": 1,
    "code": "MERCANCIA",
    "description": "Proveedores de mercancia ODBMS"
  },
  "paymentCondition": {
    "id": 4,
    "conditionName": "45 dias",
    "days": 45
  },
  "status": 1,
  "blocked": true,
  "blockInfo": {
    "validFrom": "2026-01-07",
    "validTo": "2026-04-07",
    "blockReason": "Incumplimiento de contrato"
  }
}
```

#### Error por Tipo Invalido

```json
{
  "message": "No existe el tipo de proveedor [5] solicitado.",
  "code": 400
}
```

---

## 3. Pruebas Realizadas

### 3.1 Casos de Prueba

| # | Caso | URL | Resultado Esperado |
|---|------|-----|-------------------|
| 1 | Todos los proveedores | `?tipoProveedor=0&estatusBloqueo=2` | Lista completa |
| 2 | Solo bloqueados | `?tipoProveedor=0&estatusBloqueo=0` | Solo proveedores con bloqueo vigente |
| 3 | Solo activos | `?tipoProveedor=0&estatusBloqueo=1` | Solo proveedores sin bloqueo |
| 4 | Mercancia activos | `?tipoProveedor=1&estatusBloqueo=1` | Mercancia no bloqueados |
| 5 | Transporte todos | `?tipoProveedor=2&estatusBloqueo=2` | Todos los de transporte |
| 6 | Tipo invalido | `?tipoProveedor=5&estatusBloqueo=1` | Error BUS214 |
| 7 | Tipo negativo | `?tipoProveedor=-1&estatusBloqueo=1` | Error BUS214 |

### 3.2 Resultados con Datos de Prueba

| Metrica | Valor |
|---------|-------|
| Total proveedores | 19 |
| Proveedores bloqueados | 3 (MERC002, TRANS002, IND003) |
| Proveedores activos | 16 |
| Por tipo MERCANCIA | 8 |
| Por tipo TRANSPORTE | 3 |
| Por tipo INDIRECTOS | 4 |
| Por tipo SERVICIOS | 4 |

---

## 4. Commits y Despliegue

### 4.1 Commits Realizados

| Proyecto | Commit | Mensaje |
|----------|--------|---------|
| catalogos-api | `a92871f` | feat(STM-831): agregar endpoint de filtrado de proveedores por tipo y estado de bloqueo |
| bff-catalogos | `70f32f0` | feat(STM-831): agregar endpoint de filtrado de proveedores en OpenAPI |

### 4.2 Branch

Ambos commits fueron realizados en la rama `develop` y pusheados a origin.

---

## 5. Pendientes

- [ ] Pruebas unitarias
- [ ] Pruebas de integracion
- [ ] Validacion de performance (< 2 segundos)
- [ ] Despliegue en DEV
- [ ] Despliegue en QA
- [ ] Despliegue en PROD

---

## 6. Referencias

- **JIRA Base:** [STM-832](https://jira.falabella.tech/browse/STM-832)
- **JIRA Actual:** [STM-831](https://jira.falabella.tech/browse/STM-831)
- **Documentacion:** [README.md](./README.md)
- **Coleccion Postman:** [STM-831-Filtrado-Proveedores.postman_collection.json](./STM-831-Filtrado-Proveedores.postman_collection.json)
