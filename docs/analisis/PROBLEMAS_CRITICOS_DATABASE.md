# 🚨 PROBLEMAS CRÍTICOS - Análisis de Base de Datos
## finanzas-api y fiscal-api (Rama: develop)

**Fecha**: 2025-11-10
**Análisis**: Comparación entre Migrations (fuente de verdad) y Entities (código)

---

## 📊 RESUMEN EJECUTIVO

| Métrica | finanzas-api | fiscal-api |
|---------|--------------|------------|
| **Tablas en Migrations** | 24 | 16 |
| **Entities definidas** | 22 | 11 |
| **Desincronizaciones** | 10 | 5 |
| **Duplicaciones** | 12 tablas CFDI duplicadas | - |
| **Bugs Críticos** | 2 | 0 |

---

## 🔴 PROBLEMAS CRÍTICOS (Acción Inmediata Requerida)

### 1. **finanzas_payments: Entity SIN Tabla en Migration**

**Severidad**: 🔴 CRÍTICA
**Impacto**: La aplicación **NO funcionará** al intentar usar FinanzasPayment

**Problema**:
- ✅ Entity existe: `src/entities/FinanzasPayment.entities.ts`
- ✅ Controller existe: `src/controllers/finanzasPayment.controller.ts`
- ✅ Service existe: `src/services/finanzasPayment.service.ts`
- ✅ Routes existen: `src/routes/finanzasPayments.routes.ts`
- ❌ **NO existe CREATE TABLE** en migration `1700000000001-CreateTablesAndIndexes.ts`
- ⚠️ Sí existe DROP TABLE en migration `1700000000000-DropAllTables.ts` (línea 31)

**Solución**:
Agregar a `1700000000001-CreateTablesAndIndexes.ts`:
```typescript
await queryRunner.query(`
    CREATE TABLE finanzas_payments (
        finanzas_payment_uuid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        company INTEGER NOT NULL,
        document_number VARCHAR(100) NOT NULL,
        document_reference VARCHAR(100) NOT NULL,
        vendor_number INTEGER NOT NULL,
        amount DECIMAL(15,2) NOT NULL,
        currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
        document_type VARCHAR(5) NOT NULL,
        sap_document VARCHAR(50),
        payment_date DATE NOT NULL,
        status INTEGER NOT NULL DEFAULT 1,

        created_by BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
        updated_by BIGINT,
        updated_at TIMESTAMP
    )
`);
```

---

### 2. **reception y reception_sku: Entities SIN Tablas en Migration**

**Severidad**: 🔴 CRÍTICA
**Impacto**: Migración de Receipt → Reception **bloqueada**

**Problema**:
- ✅ Entities existen: `Reception.entity.ts`, `ReceptionSku.entity.ts`
- ✅ Service implementado: `purchaseOrder.service.ts` usa Reception
- ✅ Repository implementado: `reception.repo.ts`
- ❌ **NO existen CREATE TABLE** para `reception` ni `reception_sku`
- ✅ Entities antiguas (`Receipt`, `ReceiptSku`) marcadas como OBSOLETAS pero aún en código

**Causa Raíz**:
- Refactorización incompleta: Se crearon las nuevas entities pero no se agregaron las tablas
- PurchaseOrder.entity.ts cambió relación de `receipts` → `receptions` pero tabla no existe

**Solución**:
1. Agregar CREATE TABLE para `reception` y `reception_sku` en migration
2. Crear migration de datos: `receipt` → `reception`
3. Eliminar entities obsoletas después de migración

---

## 🟠 PROBLEMAS DE ARQUITECTURA (Refactorización Necesaria)

### 3. **12 Tablas CFDI Duplicadas entre finanzas-api y fiscal-api**

**Severidad**: 🟠 ALTA
**Impacto**: Confusión arquitectónica, datos duplicados potenciales

**Tablas duplicadas**:
```
invoice
issuer
receiver
payments
payment
related_documents
totals
addendum
related_cfdi
equivalence_dr
pac_catalog
version_catalog
```

**Análisis**:
- Todas estas tablas pertenecen al **esquema CFDI** (tenant_fiscal)
- finanzas-api las tiene en migration pero **NO debería**
- fiscal-api es el dueño correcto

**Causa**:
Migración incompleta cuando se separaron los microservicios. finanzas-api conservó tablas CFDI que ya no le corresponden.

**Solución Recomendada**:
1. **ELIMINAR** de finanzas-api migrations todas las tablas CFDI
2. **CONFIRMAR** que finanzas-api usa `fiscalApi.client.ts` para acceder a datos fiscales
3. Actualizar migration para solo crear tablas de `tenant_finance`

---

### 4. **fiscal_payments: Ubicación Incorrecta**

**Severidad**: 🟠 ALTA
**Impacto**: Confusión entre pagos fiscales y financieros

**Problema**:
- Tabla `fiscal_payments` está en **finanzas-api** (tenant_finance)
- Debería estar en **fiscal-api** (tenant_fiscal)
- Duplica funcionalidad con tablas `payments` y `payment` de CFDI

**Entity**: `FiscalPayment.entity.ts` en finanzas-api
```typescript
@Entity({ name: 'fiscal_payments' })
export class FiscalPayment {
    paymentNumber: string (unique)
    paymentMethod: string
    bankAccount: string
    referencePayment: string
    // ... más campos fiscales
}
```

**Vs FinanzasPayment** (correcto en finanzas-api):
```typescript
@Entity({ name: 'finanzas_payments' })
export class FinanzasPayment {
    documentNumber: string
    documentReference: string
    sapDocument: string
    // ... campos operativos
}
```

**Solución**:
1. **Mover** `fiscal_payments` de finanzas-api → fiscal-api
2. **Renombrar** a `payment_complement` o similar para claridad
3. Actualizar referencias en código

---

## 🟡 PROBLEMAS MENORES (Cleanup Requerido)

### 5. **Entities Sin Tablas en Migration (finanzas-api)**

**Tablas faltantes**:
- `origin_catalog` (tiene entity)
- `status_catalog` (tiene entity)
- `supplier_block` (tiene entity)
- `stamping_rebate` (tiene entity)
- `shipping_guide_document` (tiene entity)
- `shipping_guide_purchase_order` (tiene entity)

**Causa**: TypeORM en modo `synchronize: false`, entities sin migration

**Solución**:
- Agregar CREATE TABLE para cada una
- O eliminar entities si no se usan

---

### 6. **Tablas Sin Entities (Ambos Proyectos)**

**finanzas-api**:
- `addendum` (tabla existe, no entity)
- `authorized_receiver_catalog` (tabla existe, no entity)
- `equivalence_dr` (tabla existe, no entity)
- `log` (tabla existe, no entity)
- `related_cfdi` (tabla existe, no entity)

**fiscal-api**:
- `payment_file_registry` (tabla existe, no entity)
- `payment_response_catalog` (tabla existe, no entity)

**Causa**: Tablas de soporte o auditoría que no requieren CRUD completo

**Solución**:
- Crear entities básicas para integridad
- O documentar que son tablas de sistema

---

### 7. **Entities Obsoletas No Eliminadas**

**finanzas-api** - `src/entities/index.ts` (líneas 23-28):
```typescript
// ENTIDADES OBSOLETAS - Mantener temporalmente para compatibilidad
// TODO: Migrar y eliminar estas entidades
export { Receipt } from './Receipt.entity.js';
export { ReceiptSku } from './ReceiptSku.entity.js';
export { VendorBlock } from './VendorBlock.entity.js';
export { StampedRebate } from './StampedRebate.entity.js';
```

**Problema**: Marcadas como obsoletas hace semanas pero aún exportadas

**Solución**:
1. Completar migración Receipt → Reception
2. Eliminar exports obsoletos
3. Eliminar archivos `.entity.ts`

---

## 📋 PLAN DE ACCIÓN RECOMENDADO

### Fase 1: Críticos (Esta Semana) 🔴

- [ ] **1.1** Crear tabla `finanzas_payments` en migration
- [ ] **1.2** Crear tablas `reception` y `reception_sku` en migration
- [ ] **1.3** Ejecutar migrations en entorno dev y verificar
- [ ] **1.4** Probar endpoints de FinanzasPayment
- [ ] **1.5** Probar creación de PurchaseOrder con Receptions

### Fase 2: Arquitectura (Próxima Semana) 🟠

- [ ] **2.1** Eliminar 12 tablas CFDI de finanzas-api migrations
- [ ] **2.2** Mover `fiscal_payments` a fiscal-api
- [ ] **2.3** Verificar integración vía `fiscalApi.client.ts`
- [ ] **2.4** Actualizar documentación de arquitectura

### Fase 3: Cleanup (Cuando haya tiempo) 🟡

- [ ] **3.1** Crear migrations para 6 entities sin tabla
- [ ] **3.2** Crear entities básicas para 7 tablas sin entity
- [ ] **3.3** Migrar datos Receipt → Reception
- [ ] **3.4** Eliminar entities obsoletas
- [ ] **3.5** Actualizar tests

---

## 🔍 COMANDOS DE VERIFICACIÓN

### Verificar sincronización entre Entity y Migration:

```bash
# finanzas-api
cd c:\workspace-fbc\backend\mrch.backend.somx.finanzas-api
grep -o "CREATE TABLE [a-z_]*" src/migrations/1700000000001-CreateTablesAndIndexes.ts | awk '{print $3}' | sort > /tmp/finanzas_migrations.txt
grep "@Entity" src/entities/*.ts | grep -o "'[a-z_]*'" | tr -d "'" | sort > /tmp/finanzas_entities.txt
diff /tmp/finanzas_migrations.txt /tmp/finanzas_entities.txt

# fiscal-api
cd c:\workspace-fbc\backend\mrch.backend.somx.fiscal-api
grep -o "CREATE TABLE [a-z_]*" src/main/resources/db/migration/V1__create_fiscal_tables.sql | awk '{print $3}' | sort > /tmp/fiscal_migrations.txt
find src/main/java -name "*Entity.java" -exec basename {} \; | sed 's/Entity.java//' | tr '[:upper:]' '[:lower:]' | sort > /tmp/fiscal_entities.txt
diff /tmp/fiscal_migrations.txt /tmp/fiscal_entities.txt
```

### Verificar que finanzas-api NO use directamente tablas CFDI:

```bash
cd c:\workspace-fbc\backend\mrch.backend.somx.finanzas-api
grep -r "FROM invoice\|FROM payments\|FROM payment\|FROM issuer\|FROM receiver" src/services src/repositories
# Resultado esperado: Sin matches (debe usar fiscalApi.client.ts)
```

---

## 📊 ARCHIVOS GENERADOS

1. **database_tables_analysis.csv** - Tabla comparativa completa
2. **PROBLEMAS_CRITICOS_DATABASE.md** - Este documento

---

## 🎯 PRIORIDAD DE EJECUCIÓN

**Orden recomendado**:
1. ✅ Fix `finanzas_payments` migration (30 min)
2. ✅ Fix `reception` y `reception_sku` migrations (1 hora)
3. ✅ Testing completo de ambos fixes (1 hora)
4. ⏸️ Limpieza arquitectónica (2-3 días, planificar sprint)
5. ⏸️ Cleanup general (background task)

**Total tiempo crítico**: ~2.5 horas
**Total tiempo arquitectura**: 2-3 días
**Total tiempo cleanup**: 1 semana (background)

---

¿Quieres que comience con el fix de `finanzas_payments` migration?
