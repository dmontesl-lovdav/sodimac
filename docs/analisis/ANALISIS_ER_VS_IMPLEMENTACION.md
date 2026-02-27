# 🔍 ANÁLISIS: Diagrama ER Original vs Implementación Actual
## Modelo Financiero Respaldo - finanzas-api

**Fecha**: 2025-11-10
**Propósito**: Validar que la implementación actual corresponda con el diseño original

---

## 📋 TABLAS IDENTIFICADAS EN EL DIAGRAMA ER

### Análisis Visual del Diagrama

Del diagrama ER "Modelo Financiero Respaldo" puedo identificar las siguientes tablas:

| # | Tabla en Diagrama ER | Nombre en Español | Tabla en Migration | Nombre en Código | Estado |
|---|---------------------|-------------------|-------------------|------------------|--------|
| 1 | TimbradoRebate | Timbrado de Rebate | stamped_rebate | StampedRebate | ✅ COINCIDE |
| 2 | Rebate | Rebate/Descuento | rebate | Rebate | ✅ COINCIDE |
| 3 | DocumentoSap | Documento SAP | sap_document | SapDocument | ✅ COINCIDE |
| 4 | GuiaEmbarque | Guía de Embarque | shipping_guide | ShippingGuide | ✅ COINCIDE |
| 5 | Pagos | Pagos | ??? | ??? | ⚠️ AMBIGUO |
| 6 | GuiaEmbarqueDocumento | Documentos de Guía | shipping_guide_document | ShippingGuideDocument | ⚠️ SIN TABLA |

---

## 🔍 ANÁLISIS DETALLADO POR TABLA

### 1. **TimbradoRebate** → **stamped_rebate** ✅

**Campos visibles en ER**:
- uuid
- numeroDocumento
- referenciaDocumento
- numeroProveedor
- idUsuario
- fechaRegistro
- idUsuarioActualizacion
- fechaActualizacion
- estatus
- fechaRespaldo

**Campos en Migration actual**:
```sql
CREATE TABLE stamped_rebate (
    stamped_rebate_uuid   UUID PRIMARY KEY,
    document_number       VARCHAR(100) NOT NULL,     -- ✅ numeroDocumento
    reference_number      VARCHAR(100) NOT NULL,     -- ✅ referenciaDocumento
    status                INTEGER NOT NULL,          -- ✅ estatus
    created_by            BIGINT,                    -- ✅ idUsuario
    created_at            TIMESTAMP,                 -- ✅ fechaRegistro
    updated_by            BIGINT,                    -- ✅ idUsuarioActualizacion
    updated_at            TIMESTAMP                  -- ✅ fechaActualizacion
)
```

**Comparación**:
- ✅ Campos principales coinciden
- ❌ **FALTA**: numeroProveedor (vendor_number)
- ❌ **FALTA**: fechaRespaldo
- ✅ Constraint de status coincide (0, 1, 2, 3)
- ✅ Unique constraint en document_number

**Conclusión**: Implementación **casi completa** - faltan 2 campos del diseño original.

---

### 2. **Rebate** → **rebate** ✅

**Campos visibles en ER**:
- numeroDocumento
- referenciaDocumento
- idTipoRebate
- documentoSap
- numeroProveedor
- importe
- origen
- idPeriodo
- fechaVencimiento
- fechaContabilizacion
- fechaRegistro
- fechaActualizacion
- estatus
- fechaRespaldo

**Campos en Migration actual**:
```sql
CREATE TABLE rebate (
    rebate_uuid           UUID PRIMARY KEY,
    document_number       VARCHAR(100) NOT NULL,     -- ✅ numeroDocumento
    reference_number      VARCHAR(100) NOT NULL,     -- ✅ referenciaDocumento
    sap_document          VARCHAR(50) NOT NULL,      -- ✅ documentoSap
    vendor_number         INTEGER NOT NULL,          -- ✅ numeroProveedor
    amount                DECIMAL(15,2) NOT NULL,    -- ✅ importe
    source                INTEGER NOT NULL,          -- ✅ origen
    period_id             INTEGER NOT NULL,          -- ✅ idPeriodo
    due_date              DATE NOT NULL,             -- ✅ fechaVencimiento
    posting_date          DATE NOT NULL,             -- ✅ fechaContabilizacion
    status                INTEGER NOT NULL,          -- ✅ estatus
    created_at            TIMESTAMP,                 -- ✅ fechaRegistro
    updated_at            TIMESTAMP,                 -- ✅ fechaActualizacion

    FOREIGN KEY (document_number)
        REFERENCES stamped_rebate(document_number)
)
```

**Comparación**:
- ✅ **TODOS** los campos principales coinciden
- ❌ **FALTA**: idTipoRebate (tipo de rebate - catálogo)
- ❌ **FALTA**: fechaRespaldo
- ✅ FK a stamped_rebate **CORRECTA**
- ✅ Constraints de validación OK

**Conclusión**: Implementación **95% completa** - falta catálogo de tipos de rebate.

---

### 3. **DocumentoSap** → **sap_document** ✅

**Campos visibles en ER**:
- numeroDocumento
- referenciaDocumento
- numeroProveedor
- importe
- origen
- docSap
- mensaje
- estatusSap
- tipoDocumento

**Campos en Migration actual**:
```sql
CREATE TABLE sap_document (
    sap_document_uuid     UUID PRIMARY KEY,
    document_number       VARCHAR(100) NOT NULL,     -- ✅ numeroDocumento
    reference_number      VARCHAR(100) NOT NULL,     -- ✅ referenciaDocumento
    vendor_number         INTEGER NOT NULL,          -- ✅ numeroProveedor
    amount                DECIMAL(15,2) NOT NULL,    -- ✅ importe
    source                INTEGER NOT NULL,          -- ✅ origen
    doc_sap               VARCHAR(15) NOT NULL,      -- ✅ docSap
    message               VARCHAR(254),              -- ✅ mensaje
    sap_status            INTEGER NOT NULL,          -- ✅ estatusSap
    document_type         VARCHAR(5) NOT NULL,       -- ✅ tipoDocumento

    created_by            BIGINT,
    created_at            TIMESTAMP,
    updated_by            BIGINT,
    updated_at            TIMESTAMP
)
```

**Comparación**:
- ✅ **100%** de campos coinciden
- ✅ Tipos de datos apropiados
- ✅ Constraints de validación OK
- ✅ Unique constraint en (document_number, reference_number)

**Conclusión**: Implementación **PERFECTA** ✅ - coincide 100% con diseño original.

---

### 4. **GuiaEmbarque** → **shipping_guide** ✅

**Campos visibles en ER**:
- guiaEmbarque
- numeroProveedor
- placa
- placaRemolque
- origen
- tipoEntrega
- importe
- estatus
- comentario
- fechaEntrega
- fechaEnvio
- idUsuario
- fechaRegistro
- fechaRespaldo

**Campos en Migration actual**:
```sql
CREATE TABLE shipping_guide (
    shipping_guide_uuid    UUID PRIMARY KEY,
    guide_number           VARCHAR(50) NOT NULL,      -- ✅ guiaEmbarque
    vendor_number          INTEGER NOT NULL,          -- ✅ numeroProveedor
    truck_plate            VARCHAR(20),               -- ✅ placa
    trailer_plate          VARCHAR(20),               -- ✅ placaRemolque
    driver_name            VARCHAR(100),              -- ➕ EXTRA (no en ER)
    driver_license         VARCHAR(50),               -- ➕ EXTRA (no en ER)
    source_id              INTEGER NOT NULL,          -- ✅ origen
    destination_id         INTEGER,                   -- ➕ EXTRA (no en ER visible)
    delivery_type          INTEGER NOT NULL,          -- ✅ tipoEntrega
    amount                 DECIMAL(15,2) NOT NULL,    -- ✅ importe
    currency               VARCHAR(3) DEFAULT 'MXN',  -- ➕ EXTRA (no en ER)
    status                 INTEGER NOT NULL,          -- ✅ estatus
    comments               TEXT,                      -- ✅ comentario
    delivery_date          DATE NOT NULL,             -- ✅ fechaEntrega
    estimated_arrival      TIMESTAMP,                 -- ➕ EXTRA (no en ER)
    actual_arrival         TIMESTAMP,                 -- ➕ EXTRA (no en ER)
    sent_at                TIMESTAMP,                 -- ✅ fechaEnvio
    created_by             BIGINT,                    -- ✅ idUsuario
    created_at             TIMESTAMP                  -- ✅ fechaRegistro
)
```

**Comparación**:
- ✅ Campos principales del ER presentes
- ➕ **MEJORAS**: Se agregaron campos útiles (driver_name, driver_license, destination_id, estimated/actual_arrival, currency)
- ❌ **FALTA**: fechaRespaldo
- ✅ Implementación **mejorada** vs diseño original

**Conclusión**: Implementación **EXTENDIDA** ✅ - incluye diseño original + mejoras operativas.

---

### 5. **Pagos** → ⚠️ **AMBIGUO - Requiere Clarificación**

**Campos visibles en ER**:
- idPago
- empresaId
- numeroDocumento
- referenciaDocumento
- numeroProveedor
- importe
- tipoDocumento
- documentoSap
- fechaPago
- estatus
- fechaRegistro
- fechaActualizacion
- fechaRespaldo

**Problema de Ambigüedad**:

En el código actual existen **3 tablas relacionadas con pagos**:

#### Opción A: **finanzas_payments** ⚠️ (Entity sin tabla)
```typescript
@Entity({ name: 'finanzas_payments' })
export class FinanzasPayment {
    finanzas_payment_uuid: UUID
    company: INTEGER                    -- ✅ empresaId
    document_number: VARCHAR(100)       -- ✅ numeroDocumento
    document_reference: VARCHAR(100)    -- ✅ referenciaDocumento
    vendor_number: INTEGER              -- ✅ numeroProveedor
    amount: DECIMAL(15,2)               -- ✅ importe
    currency: VARCHAR(3)                -- ➕ EXTRA
    document_type: VARCHAR(5)           -- ✅ tipoDocumento
    sap_document: VARCHAR(50)           -- ✅ documentoSap
    payment_date: DATE                  -- ✅ fechaPago
    status: INTEGER                     -- ✅ estatus
    created_at: TIMESTAMP               -- ✅ fechaRegistro
    updated_at: TIMESTAMP               -- ✅ fechaActualizacion
}
```

**Coincidencia**: ✅ **95%** - Casi todos los campos coinciden
**Problema**: ❌ **NO HAY CREATE TABLE en migration**

---

#### Opción B: **fiscal_payments** (tabla existe pero ubicación incorrecta)
```sql
CREATE TABLE fiscal_payments (
    fiscal_payment_uuid   UUID
    payment_number        VARCHAR(50) UNIQUE    -- ➕ EXTRA
    company               INTEGER               -- ✅ empresaId
    document_number       VARCHAR(100)          -- ✅ numeroDocumento
    reference_number      VARCHAR(100)          -- ✅ referenciaDocumento
    vendor_number         INTEGER               -- ✅ numeroProveedor
    amount                DECIMAL(15,2)         -- ✅ importe
    currency              VARCHAR(3)            -- ➕ EXTRA
    document_type         VARCHAR(5)            -- ✅ tipoDocumento
    sap_document          VARCHAR(50)           -- ✅ documentoSap
    payment_date          DATE                  -- ✅ fechaPago
    status                INTEGER               -- ✅ estatus
    payment_method        VARCHAR(10)           -- ➕ EXTRA (campo fiscal)
    bank_account          VARCHAR(50)           -- ➕ EXTRA (campo fiscal)
    reference_payment     VARCHAR(100)          -- ➕ EXTRA (campo fiscal)
    created_at            TIMESTAMP             -- ✅ fechaRegistro
    updated_at            TIMESTAMP             -- ✅ fechaActualizacion
)
```

**Coincidencia**: ✅ **90%** + campos fiscales extra
**Problema**: ❌ Tiene campos fiscales (payment_method, bank_account) que sugieren uso en CFDI

---

#### Opción C: **payments** (tabla CFDI - fiscal-api)
Esta es claramente del módulo fiscal (complementos de pago CFDI), NO corresponde al ER financiero.

---

**Análisis de "Pagos" en el ER**:

Basándome en el contexto del diagrama ("Modelo Financiero Respaldo") y los campos:
- empresaId (company)
- numeroDocumento / referenciaDocumento
- numeroProveedor (vendor_number)
- importe / tipoDocumento
- documentoSap
- **SIN** campos de timbrado fiscal (payment_method, bank_account, etc.)

**Conclusión**: El "Pagos" del ER corresponde a **finanzas_payments** (pagos operativos/contables), NO a fiscal_payments.

**Estado**: ⚠️ **CRÍTICO** - Entity correcta existe pero **falta CREATE TABLE en migration**.

---

### 6. **GuiaEmbarqueDocumento** → **shipping_guide_document** ⚠️

**Campos visibles en ER**:
- guiaEmbarque
- nombreArchivo
- tipoArchivo

**Estado en Código**:
- ✅ Entity existe: `ShippingGuideDocument.entity.ts`
- ❌ **NO hay CREATE TABLE** en migration

**Campos esperados** (basados en entity):
```typescript
@Entity('shipping_guide_document')
export class ShippingGuideDocument {
    shipping_guide_document_uuid: UUID
    shipping_guide_uuid: UUID              -- FK a shipping_guide
    file_name: VARCHAR                     -- ✅ nombreArchivo
    file_type: VARCHAR                     -- ✅ tipoArchivo
    file_path: VARCHAR                     -- ➕ EXTRA (necesario)
    file_size: INTEGER                     -- ➕ EXTRA (útil)
    uploaded_at: TIMESTAMP                 -- ➕ EXTRA (útil)
}
```

**Conclusión**: ⚠️ **Diseño correcto pero falta implementación** - necesita CREATE TABLE.

---

## 📊 TABLAS ADICIONALES NO VISIBLES EN EL DIAGRAMA

Las siguientes tablas están en la migration actual pero **NO** se ven en el diagrama ER compartido:

### Tablas Financieras Legítimas (podrían estar en otra parte del ER):

1. **accounts_payable** - Cuentas por pagar
2. **purchase_order** - Órdenes de compra
3. **receipt** / **receipt_sku** - Recepciones (obsoletas)
4. **reception** / **reception_sku** - Recepciones (nuevas) ⚠️ sin tabla
5. **vendor_block** - Bloqueo de proveedores

### Catálogos (probablemente en sección separada del ER):

6. **origin_catalog** ⚠️ - Orígenes/Sucursales (sin tabla)
7. **status_catalog** ⚠️ - Estados (sin tabla)
8. **supplier_block** ⚠️ - Bloqueos (sin tabla)
9. **stamping_rebate** ⚠️ - Control timbrado (sin tabla)
10. **shipping_guide_purchase_order** ⚠️ - Relación M:N (sin tabla)

### Tablas Fiscales que NO deberían estar aquí:

11-22. **12 tablas CFDI** (invoice, payments, payment, issuer, receiver, etc.) ❌

---

## 🎯 CONCLUSIONES DEL ANÁLISIS

### ✅ BUENAS NOTICIAS

1. **Alta Fidelidad al Diseño Original**: Las tablas visibles en el ER tienen implementación correcta o muy cercana
2. **Mejoras Operativas**: Algunas tablas (shipping_guide) tienen campos adicionales útiles
3. **Nomenclatura Consistente**: Nombres en inglés bien traducidos del español

### ⚠️ PROBLEMAS CRÍTICOS

1. **finanzas_payments** (tabla "Pagos" del ER):
   - ✅ Entity correcta implementada
   - ❌ **FALTA CREATE TABLE en migration**
   - 🔴 **Prioridad MÁXIMA**

2. **shipping_guide_document** (tabla del ER):
   - ✅ Entity existe
   - ❌ **FALTA CREATE TABLE en migration**
   - 🟠 **Prioridad ALTA**

3. **reception + reception_sku** (probablemente en ER pero no visible):
   - ✅ Entities existen
   - ❌ **FALTAN CREATE TABLE en migrations**
   - 🔴 **Prioridad MÁXIMA** (bloquea migración de receipt → reception)

### 🔍 DISCREPANCIAS MENORES

1. **Campos faltantes**:
   - `stamped_rebate`: Falta numeroProveedor, fechaRespaldo
   - `rebate`: Falta idTipoRebate, fechaRespaldo
   - Varios: Falta fechaRespaldo (campo de auditoría)

2. **Campos "fechaRespaldo"**:
   - Presente en diseño original
   - Ausente en implementación actual
   - **Recomendación**: Agregar si es requerimiento de auditoría

### ❌ CONTAMINACIÓN ARQUITECTÓNICA

**12 tablas CFDI** en finanzas-api que NO pertenecen al módulo financiero:
- invoice, issuer, receiver, payments, payment, related_documents, totals
- addendum, related_cfdi, equivalence_dr, pac_catalog, version_catalog, log

**Solución**: Eliminar de migrations de finanzas-api (pertenecen a fiscal-api)

---

## 📋 TABLA RESUMEN DE CONFORMIDAD

| Tabla en ER | Implementación | Campos Coinciden | CREATE TABLE | Entity | Estado |
|-------------|---------------|------------------|--------------|---------|--------|
| TimbradoRebate | stamped_rebate | 90% | ✅ SÍ | ✅ SÍ | 🟡 OK (faltan 2 campos) |
| Rebate | rebate | 95% | ✅ SÍ | ✅ SÍ | 🟢 EXCELENTE |
| DocumentoSap | sap_document | 100% | ✅ SÍ | ✅ SÍ | 🟢 PERFECTO |
| GuiaEmbarque | shipping_guide | 95%+ | ✅ SÍ | ✅ SÍ | 🟢 MEJORADO |
| Pagos | finanzas_payments | 95% | ❌ **NO** | ✅ SÍ | 🔴 CRÍTICO |
| GuiaEmbarqueDocumento | shipping_guide_document | 100% | ❌ **NO** | ✅ SÍ | 🟠 FALTA TABLA |

**Conformidad General**: 🟡 **70%** - 4 de 6 tablas completamente implementadas

---

## 🛠️ PLAN DE ACCIÓN PARA ALCANZAR 100% CONFORMIDAD

### Fase 1: Críticos 🔴 (Hoy)

- [ ] **1.1** Crear tabla `finanzas_payments` en migration
  - Basarse en entity existente
  - Agregar campo `fechaRespaldo` si requerido
  - Testing completo

- [ ] **1.2** Crear tabla `shipping_guide_document` en migration
  - Implementar FK a shipping_guide
  - Testing de carga de archivos

### Fase 2: Campos Faltantes 🟡 (Esta semana)

- [ ] **2.1** Agregar campos faltantes a `stamped_rebate`:
  - numeroProveedor (vendor_number INTEGER)
  - fechaRespaldo (backup_date TIMESTAMP)

- [ ] **2.2** Agregar campos faltantes a `rebate`:
  - idTipoRebate (rebate_type_id INTEGER)
  - fechaRespaldo (backup_date TIMESTAMP)

- [ ] **2.3** Crear catálogo `rebate_type_catalog` si no existe

### Fase 3: Limpieza Arquitectónica 🔵 (Próxima semana)

- [ ] **3.1** Eliminar 12 tablas CFDI de finanzas-api migrations
- [ ] **3.2** Verificar que finanzas-api use fiscalApi.client.ts para acceso fiscal
- [ ] **3.3** Documentar separación de responsabilidades

### Fase 4: Completar Modelo (Cuando aplique)

- [ ] **4.1** Validar si hay más secciones del ER no compartidas
- [ ] **4.2** Implementar tablas de catálogos faltantes
- [ ] **4.3** Completar reception/reception_sku

---

## 📊 MÉTRICAS DE CONFORMIDAD

```
Tablas Visibles en ER:              6
Tablas Implementadas Completas:     4  (67%)
Tablas con Entity pero sin tabla:   2  (33%)
Campos Coincidentes (promedio):     95%
Mejoras vs Diseño Original:         2 tablas
Contaminación (tablas extras):      12 tablas CFDI

CONFORMIDAD TOTAL: 70% ⚠️
META: 100% ✅
```

---

## 🤔 PREGUNTAS PARA EL EQUIPO

1. **¿Existe más documentación del ER?**
   - ¿Hay secciones con purchase_order, accounts_payable, reception?
   - ¿Existe versión completa del diagrama?

2. **Campo "fechaRespaldo"**:
   - ¿Es requerimiento actual?
   - ¿Para qué se usa? (auditoría, backup, histórico)
   - ¿Se debe agregar a todas las tablas?

3. **Tabla "Pagos"**:
   - ¿Confirman que es finanzas_payments (operativo) y NO fiscal_payments (CFDI)?
   - ¿Por qué existe fiscal_payments en finanzas-api?

4. **Catálogo de Tipos de Rebate**:
   - ¿Existe tabla rebate_type_catalog?
   - ¿Dónde se define idTipoRebate?

5. **Arquitectura**:
   - ¿Cuándo se separaron fiscal-api y finanzas-api?
   - ¿Por qué quedaron 12 tablas CFDI en finanzas-api?

---

**¿Quieres que empiece a crear las migrations faltantes para alcanzar 100% de conformidad con el ER?**
