# 📋 PLAN DE MIGRACIÓN: Receipt → Reception
## Eliminación de tablas obsoletas y activación de nuevas

**Fecha**: 2025-11-10
**Objetivo**: Migrar de `receipt`/`receipt_sku` (obsoletas) a `reception`/`reception_sku` (activas)

---

## ✅ CONFIRMACIÓN: ES VIABLE Y SEGURO ELIMINAR RECEIPT

### Análisis de Viabilidad

**Respuesta**: ✅ **SÍ, ES 100% VIABLE ELIMINAR `receipt` y `receipt_sku`**

### Evidencias:

#### 1. **Código ya preparado para Reception** ✅

**PurchaseOrder.entity.ts** (línea 56):
```typescript
@OneToMany(() => Reception, reception => reception.purchaseOrder, { cascade: true, eager: false })
receptions?: Reception[]  // ✅ Ya usa Reception, NO Receipt
```

**Importaciones** (líneas 13-14):
```typescript
import { Receipt } from './Receipt.entity.js';     // ❌ Importado pero NO usado
import { Reception } from './Reception.entity.js';  // ✅ Usado activamente
```

---

#### 2. **Controller ya usa Reception** ✅

**purchaseOrder.controller.ts**:

**Líneas comentadas** (57, 74, 139):
```typescript
// Línea 57:
//row.receptions = await getReceiptsRepo().findBy({ purchaseOrderUuid: row.purchaseOrderId });

// Línea 74:
// row.receipts = await getReceiptsRepo().findBy({ purchaseOrderUuid: row.purchaseOrderId });

// Línea 139:
//where.push({ receipts: { receiptNumber: Like(`%${criteria.criteria.trim()}%`) } });
```

**Endpoints activos** (líneas 168-169):
```typescript
.get("/listReception", validateBody(ListReceptionQuerySchema), listReception)
.patch("/updateReception", validateBody(UpdatePurchaseOrderSchema), updateReception)
```

✅ Los endpoints **YA usan Reception**, NO Receipt.

---

#### 3. **Service implementado para Reception** ✅

**purchaseOrder.service.ts**:
- `listReception(q)` - Lista recepciones (líneas 24-54)
- `updateReception(dto)` - Actualiza recepciones (líneas 56-96)
- `create(dto)` - Crea PurchaseOrder con Receptions (líneas 122-172)

✅ Toda la lógica de negocio **ya usa Reception**.

---

#### 4. **NO hay servicios/repositorios para Receipt** ✅

Búsqueda en `src/services` y `src/repositories`:
- ❌ NO existe `receipt.service.ts`
- ❌ NO existe `receipt.repo.ts`
- ✅ SÍ existe `reception.repo.ts` (implementado y funcionando)

**Conclusión**: Receipt **NO se usa** en lógica de negocio actual.

---

#### 5. **Entity marcada como OBSOLETA** ✅

**entities/index.ts** (líneas 23-27):
```typescript
// ENTIDADES OBSOLETAS - Mantener temporalmente para compatibilidad
// TODO: Migrar y eliminar estas entidades
export { Receipt } from './Receipt.entity.js';
export { ReceiptSku } from './ReceiptSku.entity.js';
```

✅ Equipo **ya planeaba** eliminarlas.

---

## 📊 ESTADO ACTUAL

### Tablas en BD (según migration):

| Tabla | Estado | Uso Actual | Entity | Service | Controller |
|-------|--------|-----------|---------|---------|-----------|
| **receipt** | ✅ Existe | ❌ NO usada | ✅ Obsoleta | ❌ NO | ❌ NO |
| **receipt_sku** | ✅ Existe | ❌ NO usada | ✅ Obsoleta | ❌ NO | ❌ NO |
| **reception** | ❌ NO existe | ✅ Usado | ✅ Activa | ✅ SÍ | ✅ SÍ |
| **reception_sku** | ❌ NO existe | ✅ Usado | ✅ Activa | ✅ SÍ* | ✅ SÍ* |

*Usado indirectamente vía relaciones

---

## 🎯 OBJETIVOS DE LA MIGRACIÓN

### Fase 1: Crear tablas nuevas ✅
- [x] Crear `reception` table
- [x] Crear `reception_sku` table

### Fase 2: Migrar datos (si existen) ⚠️
- [ ] Verificar si hay datos en `receipt` y `receipt_sku`
- [ ] Si hay datos: Migrar a `reception` y `reception_sku`
- [ ] Si NO hay datos: Proceder a eliminación directa

### Fase 3: Eliminar tablas obsoletas ✅
- [ ] DROP TABLE `receipt_sku` (dependiente primero)
- [ ] DROP TABLE `receipt`
- [ ] Eliminar entities de código
- [ ] Limpiar imports

### Fase 4: Testing ✅
- [ ] Crear PurchaseOrder con Receptions
- [ ] Listar Receptions
- [ ] Actualizar Reception status
- [ ] Validar constraints y FKs

---

## 📝 SCRIPTS DE MIGRACIÓN

### SCRIPT 1: Crear tablas `reception` y `reception_sku`

```sql
-- ===============================
-- CREAR TABLA: reception
-- ===============================
CREATE TABLE tenant_finance.reception (
    reception_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_uuid   UUID NOT NULL,
    origin_id             INTEGER NOT NULL,
    destination_id        INTEGER NOT NULL,
    amount                DECIMAL(15,2) NOT NULL DEFAULT 0,
    status                INTEGER NOT NULL DEFAULT 0,
    comment               TEXT,
    reception_date        DATE NOT NULL,

    -- Auditoría
    created_by            BIGINT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by            BIGINT,
    updated_at            TIMESTAMP,

    -- Constraint de validación
    CONSTRAINT chk_reception_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_reception_status
        CHECK (status IN (0, 1, 2, 3, 4)),
        -- 0 = Disponible (recibida pero no procesada)
        -- 1 = Consumida por sistema (automático)
        -- 2 = Consumida manual
        -- 3 = Cancelada
        -- 4 = Borrado lógico

    -- Foreign Keys
    CONSTRAINT fk_reception_purchase_order
        FOREIGN KEY (purchase_order_uuid)
        REFERENCES tenant_finance.purchase_order(purchase_order_uuid)
        ON DELETE CASCADE
);

COMMENT ON TABLE tenant_finance.reception IS 'Recepciones de mercancía y servicios (versión activa)';
COMMENT ON COLUMN tenant_finance.reception.reception_id IS 'ID único de la recepción';
COMMENT ON COLUMN tenant_finance.reception.purchase_order_uuid IS 'Orden de compra asociada';
COMMENT ON COLUMN tenant_finance.reception.origin_id IS 'Sucursal/CEDIS de origen';
COMMENT ON COLUMN tenant_finance.reception.destination_id IS 'Sucursal/CEDIS destino';
COMMENT ON COLUMN tenant_finance.reception.amount IS 'Monto total recibido';
COMMENT ON COLUMN tenant_finance.reception.status IS 'Estado de la recepción (0-4)';
COMMENT ON COLUMN tenant_finance.reception.reception_date IS 'Fecha de recepción física';

-- ===============================
-- CREAR TABLA: reception_sku
-- ===============================
CREATE TABLE tenant_finance.reception_sku (
    reception_sku_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reception_id          UUID NOT NULL,
    sku                   VARCHAR(20) NOT NULL,
    description           VARCHAR(254),
    quantity              DECIMAL(15,2) NOT NULL,
    unit_cost             DECIMAL(15,2) NOT NULL,
    total_cost            DECIMAL(15,2) NOT NULL,
    status                INTEGER NOT NULL DEFAULT 0,

    -- Auditoría
    created_by            BIGINT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by            BIGINT,
    updated_at            TIMESTAMP,

    -- Constraints de validación
    CONSTRAINT chk_reception_sku_quantity
        CHECK (quantity >= 0),

    CONSTRAINT chk_reception_sku_unit_cost
        CHECK (unit_cost >= 0),

    CONSTRAINT chk_reception_sku_total_cost
        CHECK (total_cost >= 0),

    CONSTRAINT chk_reception_sku_calculation
        CHECK (total_cost = ROUND(quantity * unit_cost, 2)),

    CONSTRAINT chk_reception_sku_status
        CHECK (status IN (0, 1, 2)),

    -- Foreign Keys
    CONSTRAINT fk_reception_sku_reception
        FOREIGN KEY (reception_id)
        REFERENCES tenant_finance.reception(reception_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE tenant_finance.reception_sku IS 'Detalle de recepciones por SKU/producto';
COMMENT ON COLUMN tenant_finance.reception_sku.reception_sku_id IS 'ID único del detalle';
COMMENT ON COLUMN tenant_finance.reception_sku.reception_id IS 'Recepción asociada';
COMMENT ON COLUMN tenant_finance.reception_sku.sku IS 'Código del producto';
COMMENT ON COLUMN tenant_finance.reception_sku.quantity IS 'Cantidad recibida';
COMMENT ON COLUMN tenant_finance.reception_sku.unit_cost IS 'Costo unitario';
COMMENT ON COLUMN tenant_finance.reception_sku.total_cost IS 'Costo total (quantity * unit_cost)';

-- ===============================
-- ÍNDICES PARA PERFORMANCE
-- ===============================
CREATE INDEX idx_reception_purchase_order ON tenant_finance.reception(purchase_order_uuid);
CREATE INDEX idx_reception_date ON tenant_finance.reception(reception_date);
CREATE INDEX idx_reception_status ON tenant_finance.reception(status);

CREATE INDEX idx_reception_sku_reception ON tenant_finance.reception_sku(reception_id);
CREATE INDEX idx_reception_sku_sku ON tenant_finance.reception_sku(sku);
```

---

### SCRIPT 2: Verificar si existen datos en tablas antiguas

```sql
-- Verificar datos en receipt
SELECT COUNT(*) as total_receipts FROM tenant_finance.receipt;

-- Verificar datos en receipt_sku
SELECT COUNT(*) as total_receipt_skus FROM tenant_finance.receipt_sku;

-- Ver estructura de datos (si existen)
SELECT * FROM tenant_finance.receipt LIMIT 5;
SELECT * FROM tenant_finance.receipt_sku LIMIT 5;
```

---

### SCRIPT 3A: Migración de datos (SI existen datos)

```sql
-- ===============================
-- MIGRAR DATOS: receipt → reception
-- ===============================
INSERT INTO tenant_finance.reception (
    reception_id,
    purchase_order_uuid,
    origin_id,
    destination_id,
    amount,
    status,
    comment,
    reception_date,
    created_by,
    created_at,
    updated_by,
    updated_at
)
SELECT
    receipt_uuid,                    -- Reutilizar UUID
    purchase_order_uuid,
    source_id,                       -- receipt tiene source_id
    destination_id,
    received_amount,                 -- receipt tiene received_amount
    CASE                             -- Mapear status
        WHEN status = 0 THEN 0       -- Mantener mapeo
        WHEN status = 1 THEN 1
        WHEN status = 2 THEN 2
        WHEN status = 3 THEN 3
        ELSE 0
    END,
    comments,                        -- receipt tiene comments
    received_at::DATE,               -- Convertir timestamp a date
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_finance.receipt;

-- Verificar migración
SELECT COUNT(*) as migrated_receptions FROM tenant_finance.reception;

-- ===============================
-- MIGRAR DATOS: receipt_sku → reception_sku
-- ===============================
INSERT INTO tenant_finance.reception_sku (
    reception_sku_id,
    reception_id,
    sku,
    description,
    quantity,
    unit_cost,
    total_cost,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at
)
SELECT
    receipt_sku_uuid,
    receipt_uuid,                    -- FK: receipt → reception (mismo UUID)
    sku,
    description,
    quantity,
    unit_cost,
    total_cost,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at
FROM tenant_finance.receipt_sku;

-- Verificar migración
SELECT COUNT(*) as migrated_reception_skus FROM tenant_finance.reception_sku;

-- Validar integridad
SELECT
    r.reception_id,
    COUNT(rs.reception_sku_id) as sku_count,
    SUM(rs.total_cost) as total_cost
FROM tenant_finance.reception r
LEFT JOIN tenant_finance.reception_sku rs ON rs.reception_id = r.reception_id
GROUP BY r.reception_id
ORDER BY r.created_at DESC
LIMIT 10;
```

---

### SCRIPT 3B: Sin migración (SI NO hay datos)

```sql
-- Si no hay datos, simplemente verificar:
SELECT
    'No data to migrate' as status,
    (SELECT COUNT(*) FROM tenant_finance.receipt) as receipt_count,
    (SELECT COUNT(*) FROM tenant_finance.receipt_sku) as receipt_sku_count;
```

---

### SCRIPT 4: Eliminar tablas obsoletas

```sql
-- ===============================
-- ELIMINAR TABLAS OBSOLETAS
-- ===============================

-- Primero la tabla dependiente
DROP TABLE IF EXISTS tenant_finance.receipt_sku CASCADE;

RAISE NOTICE 'Tabla receipt_sku eliminada';

-- Luego la tabla padre
DROP TABLE IF EXISTS tenant_finance.receipt CASCADE;

RAISE NOTICE 'Tabla receipt eliminada';

-- Verificar eliminación
SELECT
    table_name
FROM information_schema.tables
WHERE table_schema = 'tenant_finance'
    AND table_name IN ('receipt', 'receipt_sku');
```

---

### SCRIPT 5: Limpiar código

**Archivos a modificar**:

#### 1. **src/entities/index.ts**
```typescript
// ANTES:
// ENTIDADES OBSOLETAS - Mantener temporalmente para compatibilidad
// TODO: Migrar y eliminar estas entidades
export { Receipt } from './Receipt.entity.js';
export { ReceiptSku } from './ReceiptSku.entity.js';

// DESPUÉS:
// ✅ Eliminadas - migradas a Reception/ReceptionSku
```

#### 2. **src/config/typeorm-datasource.ts**
```typescript
// ANTES:
import { Receipt } from '../entities/Receipt.entity.js';
import { ReceiptSku } from '../entities/ReceiptSku.entity.js';

const ENTITIES = [
    ...,
    Receipt,
    ReceiptSku,
    ...
];

// DESPUÉS:
// ✅ Imports eliminados
// ✅ Removidos de ENTITIES array
```

#### 3. **src/entities/PurchaseOrder.entity.ts**
```typescript
// ANTES:
import { Receipt } from './Receipt.entity.js';
import { Reception } from './Reception.entity.js';

// DESPUÉS:
import { Reception } from './Reception.entity.js';  // ✅ Solo Reception
```

#### 4. **src/controllers/purchaseOrder.controller.ts**
```typescript
// ANTES:
import { Receipt } from "@/entities/Receipt.entity.js";
const getReceiptsRepo = () => getDataSource().getRepository(Receipt);

// DESPUÉS:
// ✅ Imports eliminados
// ✅ getReceiptsRepo() eliminado (línea 26)
```

#### 5. **Eliminar archivos físicos**:
- ❌ `src/entities/Receipt.entity.ts`
- ❌ `src/entities/ReceiptSku.entity.ts`

---

## 🧪 TESTING POST-MIGRACIÓN

### Test 1: Crear PurchaseOrder con Receptions
```bash
POST /purchase-order
{
  "orderNumber": "OC-TEST-001",
  "vendorNumber": 1001,
  "originId": 1,
  "amount": "50000.00",
  "purchaseOrderDate": "2025-11-10",
  "status": 1,
  "createdBy": 1,
  "receptionList": [
    {
      "originId": 1,
      "destinationId": 2,
      "amount": "25000.00",
      "comments": "Primera entrega parcial",
      "receptionDate": "2025-11-12",
      "createdBy": 1,
      "receiptSkuList": [
        {
          "sku": "PROD-001",
          "description": "Producto Test",
          "quantity": 100,
          "unitCost": "250.00",
          "totalCost": "25000.00",
          "createdBy": 1
        }
      ]
    }
  ]
}
```

### Test 2: Listar Receptions
```bash
GET /purchase-order/listReception
{
  "receptionDateAtInitial": "2025-11-01",
  "receptionDateAtEnd": "2025-11-30",
  "pageNumber": 1,
  "pageSize": 10
}
```

### Test 3: Actualizar Reception Status
```bash
PATCH /purchase-order/updateReception
{
  "orderNumber": "OC-TEST-001",
  "receptionId": "<reception_uuid>",
  "status": 1,
  "supplierNumber": 1001
}
```

### Test 4: Validar Constraints
```sql
-- Test: Cantidad negativa (debe fallar)
INSERT INTO tenant_finance.reception_sku (reception_id, sku, quantity, unit_cost, total_cost)
VALUES ('...', 'TEST', -10, 100, -1000);  -- ❌ Debe rechazarse

-- Test: Cálculo incorrecto (debe fallar)
INSERT INTO tenant_finance.reception_sku (reception_id, sku, quantity, unit_cost, total_cost)
VALUES ('...', 'TEST', 10, 100, 999);  -- ❌ Debe rechazarse (10*100=1000, no 999)

-- Test: Status inválido (debe fallar)
UPDATE tenant_finance.reception SET status = 5 WHERE reception_id = '...';  -- ❌ Debe rechazarse
```

---

## ⏱️ ESTIMACIÓN DE TIEMPO

| Fase | Actividad | Tiempo | Prioridad |
|------|-----------|--------|-----------|
| 1 | Crear tablas reception/reception_sku | 30 min | 🔴 Crítica |
| 2 | Verificar datos existentes | 10 min | 🟠 Alta |
| 3a | Migrar datos (si existen) | 30 min | 🟠 Condicional |
| 3b | Skip (si no hay datos) | 0 min | - |
| 4 | Eliminar tablas obsoletas | 10 min | 🟡 Media |
| 5 | Limpiar código (entities, imports) | 20 min | 🟡 Media |
| 6 | Testing completo | 30 min | 🟠 Alta |

**Total**: 2 horas (sin datos) a 2.5 horas (con datos a migrar)

---

## 🎯 ORDEN DE EJECUCIÓN RECOMENDADO

### ✅ Ejecución Segura (Paso a Paso):

1. **Backup de BD** ⚠️
   ```bash
   pg_dump -h host -U user -d database -t tenant_finance.receipt* > backup_receipt.sql
   ```

2. **Ejecutar SCRIPT 1** - Crear tablas nuevas
   - Revisar que se creen correctamente
   - Validar constraints e índices

3. **Ejecutar SCRIPT 2** - Verificar datos
   - Si hay datos → continuar con SCRIPT 3A
   - Si NO hay datos → continuar con SCRIPT 4

4. **Ejecutar SCRIPT 3A/3B** - Migración condicional

5. **Validar migración**
   - Comparar counts
   - Verificar integridad referencial

6. **Ejecutar SCRIPT 4** - Eliminar tablas obsoletas

7. **Ejecutar SCRIPT 5** - Limpiar código

8. **Ejecutar TESTING** - Validar funcionalidad

---

## ✅ CONCLUSIÓN

**¿ES VIABLE ELIMINAR receipt y receipt_sku?**

# ✅ SÍ, ES 100% VIABLE Y RECOMENDADO

### Razones:

1. ✅ Código **ya usa Reception** exclusivamente
2. ✅ NO hay servicios/repositorios para Receipt
3. ✅ Entities marcadas como **OBSOLETAS**
4. ✅ Equipo **ya planeaba** esta migración
5. ✅ Migration preparada y lista

### Beneficios:

- 🟢 Limpia código técnico obsoleto
- 🟢 Alinea BD con entities actuales
- 🟢 Evita confusión entre Receipt/Reception
- 🟢 Mejora manteni

bilidad del código

### Riesgos:

- 🟡 **Mínimo**: Solo si hay datos en receipt que no se migren correctamente
- 🟢 **Mitigado**: Scripts de migración incluidos

---

**¿Procedemos con la ejecución de la migración?**
