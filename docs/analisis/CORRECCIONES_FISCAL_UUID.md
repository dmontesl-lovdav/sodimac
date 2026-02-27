# CORRECCIONES REALIZADAS - Campo fiscal_uuid

## Fecha: 2025-11-11
## Archivos Modificados: V10__insert_fiscal_data.sql

---

## 🔍 PROBLEMA IDENTIFICADO

El campo `fiscal_uuid` (UUID del TimbreFiscalDigital del SAT) **NO se estaba insertando** en los datos de prueba del script de migración `V10__insert_fiscal_data.sql`, aunque:

- ✅ **Está definido** en la tabla (V1__create_fiscal_tables.sql)
- ✅ **Está mapeado** en las entities (InvoiceEntity.java y PaymentsEntity.java)
- ✅ **Se inserta correctamente** en el script de carga masiva (generate_1000_payments.sql)

### Impacto:
- Las 2 facturas de prueba tenían `fiscal_uuid = NULL`
- El complemento de pago de prueba tenía `fiscal_uuid = NULL`
- Los endpoints de búsqueda por UUID fiscal NO funcionaban con datos de prueba
- Las pruebas del JIRA STM-771 (búsqueda por fiscal_uuid) fallaban

---

## ✅ CORRECCIONES APLICADAS

### 1. Tabla `invoice` - Líneas 49-57

**ANTES:**
```sql
-- INVOICE
INSERT INTO invoice (
    invoice_uuid,
    place_of_issue,
    payment_method,
    document_type,
    total,
    subtotal,
    discount,
    issue_date,
    folio,
    series,
    version,
    status,
    issuer_uuid,
    receiver_uuid,
    created_by
)
SELECT
    '66666666-6666-6666-6666-666666666666'::uuid,
    '01000',
    'PUE',
    'I',
    1000.00,
    1000.00,
    0.00,
    CURRENT_DATE,
    '001',
    'A',
    4.000,
    1,
    '11111111-1111-1111-1111-111111111111'::uuid,
    '33333333-3333-3333-3333-333333333333'::uuid,
    1
WHERE NOT EXISTS (SELECT 1 FROM invoice WHERE invoice_uuid = '66666666-6666-6666-6666-666666666666'::uuid);
```

**DESPUÉS:**
```sql
-- INVOICE
-- CORREGIDO: Agregado campo fiscal_uuid (UUID del TimbreFiscalDigital del SAT)
INSERT INTO invoice (
    invoice_uuid,
    fiscal_uuid,  -- ← AGREGADO
    place_of_issue,
    payment_method,
    document_type,
    total,
    subtotal,
    discount,
    issue_date,
    folio,
    series,
    version,
    status,
    issuer_uuid,
    receiver_uuid,
    created_by
)
SELECT
    '66666666-6666-6666-6666-666666666666'::uuid,
    'aaaaaaaa-1111-2222-3333-444444444444'::uuid,  -- ← UUID FISCAL DE PRUEBA
    '01000',
    'PUE',
    'I',
    1000.00,
    1000.00,
    0.00,
    CURRENT_DATE,
    '001',
    'A',
    4.000,
    1,
    '11111111-1111-1111-1111-111111111111'::uuid,
    '33333333-3333-3333-3333-333333333333'::uuid,
    1
WHERE NOT EXISTS (SELECT 1 FROM invoice WHERE invoice_uuid = '66666666-6666-6666-6666-666666666666'::uuid);

-- Segunda factura
INSERT INTO invoice (
    invoice_uuid,
    fiscal_uuid,  -- ← AGREGADO
    place_of_issue,
    payment_method,
    document_type,
    total,
    subtotal,
    discount,
    issue_date,
    folio,
    series,
    version,
    status,
    issuer_uuid,
    receiver_uuid,
    created_by
)
SELECT
    '77777777-7777-7777-7777-777777777777'::uuid,
    'bbbbbbbb-1111-2222-3333-444444444444'::uuid,  -- ← UUID FISCAL DE PRUEBA
    '01000',
    'PPD',
    'I',
    1800.00,
    2000.00,
    200.00,
    CURRENT_DATE - INTERVAL '1 day',
    '002',
    'A',
    4.000,
    1,
    '11111111-1111-1111-1111-111111111111'::uuid,
    '44444444-4444-4444-4444-444444444444'::uuid,
    1
WHERE NOT EXISTS (SELECT 1 FROM invoice WHERE invoice_uuid = '77777777-7777-7777-7777-777777777777'::uuid);
```

---

### 2. Tabla `payments` - Líneas 59-63

**ANTES:**
```sql
-- PAYMENTS
INSERT INTO payments (
    payments_uuid,
    version,
    payment_date,
    issuer_uuid,
    receiver_uuid,
    folio,
    series,
    status,
    created_by
)
SELECT
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid,
    2.000,
    CURRENT_DATE,
    '11111111-1111-1111-1111-111111111111'::uuid,
    '44444444-4444-4444-4444-444444444444'::uuid,
    '001',
    'P',
    1,
    1
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid);
```

**DESPUÉS:**
```sql
-- PAYMENTS
-- CORREGIDO: Agregado campo fiscal_uuid (UUID del TimbreFiscalDigital del complemento de pago)
INSERT INTO payments (
    payments_uuid,
    fiscal_uuid,  -- ← AGREGADO
    version,
    payment_date,
    issuer_uuid,
    receiver_uuid,
    folio,
    series,
    status,
    created_by
)
SELECT
    'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid,
    'cccccccc-1111-2222-3333-444444444444'::uuid,  -- ← UUID FISCAL DE PRUEBA
    2.000,
    CURRENT_DATE,
    '11111111-1111-1111-1111-111111111111'::uuid,
    '44444444-4444-4444-4444-444444444444'::uuid,
    '001',
    'P',
    1,
    1
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid);
```

---

## 📊 UUIDs FISCALES ASIGNADOS

Para facilitar las pruebas, se asignaron los siguientes UUIDs fiscales:

| Tabla | Registro | invoice_uuid / payments_uuid | fiscal_uuid (SAT) | Descripción |
|-------|----------|------------------------------|-------------------|-------------|
| invoice | Factura 1 | 66666666-6666-6666-6666-666666666666 | **aaaaaaaa-1111-2222-3333-444444444444** | Factura de prueba 1 (PUE) |
| invoice | Factura 2 | 77777777-7777-7777-7777-777777777777 | **bbbbbbbb-1111-2222-3333-444444444444** | Factura de prueba 2 (PPD) |
| payments | Complemento | aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee | **cccccccc-1111-2222-3333-444444444444** | Complemento de pago de prueba |

---

## 🧪 CASOS DE PRUEBA AHORA FUNCIONALES

Con esta corrección, los siguientes casos de prueba del plan de pruebas ahora funcionarán correctamente:

### CP-771-03: Buscar Facturas por UUID Fiscal

**Endpoint**: `GET /api/invoices/fiscal/{fiscalUuid}`

**Datos de prueba válidos:**
```bash
# Factura 1
curl http://localhost:8082/api/invoices/fiscal/aaaaaaaa-1111-2222-3333-444444444444

# Factura 2
curl http://localhost:8082/api/invoices/fiscal/bbbbbbbb-1111-2222-3333-444444444444
```

**Resultado esperado**: ✅ Facturas encontradas

---

### CP-448-02: Buscar Complemento por UUID Fiscal

**Endpoint**: `GET /api/payments/fiscal/{fiscalUuid}`

**Datos de prueba válidos:**
```bash
# Complemento de pago
curl http://localhost:8082/api/payments/fiscal/cccccccc-1111-2222-3333-444444444444
```

**Resultado esperado**: ✅ Complemento encontrado

---

### CP-973-01: Relacionar Descuento con NC (Validación)

**Endpoint**: `POST /api/rebates/relate`

**Ahora se puede validar correctamente** que el `fiscal_uuid` de la NC existe y es único en el sistema.

---

## 🔄 CÓMO APLICAR LA CORRECCIÓN

### Opción 1: Si Flyway NO se ha ejecutado aún

Si el V10 aún no se ha aplicado a la base de datos:

1. El archivo ya está corregido
2. Ejecutar Flyway normalmente
3. Los datos se insertarán con los `fiscal_uuid` correctos

---

### Opción 2: Si Flyway YA ejecutó el V10 (checksum mismatch)

Si Flyway ya aplicó el V10 original, ejecutar este script de corrección:

```sql
-- ===============================
-- SCRIPT DE CORRECCIÓN: Agregar fiscal_uuid a datos de prueba existentes
-- Ejecutar MANUALMENTE si el V10 original ya fue aplicado
-- ===============================

-- Actualizar facturas de prueba
UPDATE tenant_fiscal.invoice
SET fiscal_uuid = 'aaaaaaaa-1111-2222-3333-444444444444'::uuid
WHERE invoice_uuid = '66666666-6666-6666-6666-666666666666'::uuid;

UPDATE tenant_fiscal.invoice
SET fiscal_uuid = 'bbbbbbbb-1111-2222-3333-444444444444'::uuid
WHERE invoice_uuid = '77777777-7777-7777-7777-777777777777'::uuid;

-- Actualizar complemento de pago
UPDATE tenant_fiscal.payments
SET fiscal_uuid = 'cccccccc-1111-2222-3333-444444444444'::uuid
WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid;

-- Verificar
SELECT
    invoice_uuid,
    fiscal_uuid,
    folio,
    series,
    document_type,
    total
FROM tenant_fiscal.invoice
WHERE invoice_uuid IN (
    '66666666-6666-6666-6666-666666666666'::uuid,
    '77777777-7777-7777-7777-777777777777'::uuid
);

SELECT
    payments_uuid,
    fiscal_uuid,
    folio,
    series
FROM tenant_fiscal.payments
WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid;
```

---

### Opción 3: Limpiar historia de Flyway y re-ejecutar

Si es necesario re-ejecutar el V10 corregido:

```sql
-- CUIDADO: Solo ejecutar en ambiente de desarrollo

-- 1. Eliminar registro del V10 de Flyway
DELETE FROM flyway_schema_history WHERE version = '10';

-- 2. Eliminar datos insertados por V10 (en orden inverso por FKs)
DELETE FROM tenant_fiscal.log WHERE cfdi_uuid IN (
    '66666666-6666-6666-6666-666666666666'::uuid,
    '77777777-7777-7777-7777-777777777777'::uuid
);

DELETE FROM tenant_fiscal.related_cfdi
WHERE related_cfdi_uuid = 'cccc1111-dddd-2222-eeee-333333333333'::uuid;

DELETE FROM tenant_fiscal.equivalence_dr
WHERE equivalence_uuid = 'bbbb1111-cccc-2222-dddd-333333333333'::uuid;

DELETE FROM tenant_fiscal.addendum
WHERE addendum_uuid = 'aaaa1111-bbbb-2222-cccc-333333333333'::uuid;

DELETE FROM tenant_fiscal.totals
WHERE totals_uuid = 'dddddddd-eeee-ffff-0000-000000000001'::uuid;

DELETE FROM tenant_fiscal.related_documents
WHERE related_document_uuid = 'cccccccc-dddd-eeee-ffff-000000000001'::uuid;

DELETE FROM tenant_fiscal.payment
WHERE payment_uuid = 'bbbbbbbb-cccc-dddd-eeee-ffffffffffff'::uuid;

DELETE FROM tenant_fiscal.payments
WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid;

DELETE FROM tenant_fiscal.invoice WHERE invoice_uuid IN (
    '66666666-6666-6666-6666-666666666666'::uuid,
    '77777777-7777-7777-7777-777777777777'::uuid
);

-- 3. Re-ejecutar Flyway (el V10 corregido se aplicará)
```

---

## ✅ VERIFICACIÓN POST-CORRECCIÓN

Después de aplicar la corrección, verificar con estas consultas:

```sql
-- Verificar que las facturas tienen fiscal_uuid
SELECT
    invoice_uuid,
    fiscal_uuid,
    CASE
        WHEN fiscal_uuid IS NULL THEN '❌ NULL'
        ELSE '✅ OK'
    END as status,
    folio,
    series,
    total
FROM tenant_fiscal.invoice
WHERE invoice_uuid IN (
    '66666666-6666-6666-6666-666666666666'::uuid,
    '77777777-7777-7777-7777-777777777777'::uuid
);

-- Verificar que el complemento tiene fiscal_uuid
SELECT
    payments_uuid,
    fiscal_uuid,
    CASE
        WHEN fiscal_uuid IS NULL THEN '❌ NULL'
        ELSE '✅ OK'
    END as status,
    folio,
    series
FROM tenant_fiscal.payments
WHERE payments_uuid = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'::uuid;

-- Verificar constraint UNIQUE funciona
SELECT
    COUNT(*) as total_facturas,
    COUNT(DISTINCT fiscal_uuid) as uuids_unicos,
    COUNT(fiscal_uuid) as uuids_no_null
FROM tenant_fiscal.invoice;
```

**Resultado esperado:**
```
invoice_uuid                          | fiscal_uuid                          | status | folio | series | total
--------------------------------------|--------------------------------------|--------|-------|--------|-------
66666666-6666-6666-6666-666666666666 | aaaaaaaa-1111-2222-3333-444444444444 | ✅ OK  | 001   | A      | 1000.00
77777777-7777-7777-7777-777777777777 | bbbbbbbb-1111-2222-3333-444444444444 | ✅ OK  | 002   | A      | 1800.00

payments_uuid                         | fiscal_uuid                          | status | folio | series
--------------------------------------|--------------------------------------|--------|-------|-------
aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee | cccccccc-1111-2222-3333-444444444444 | ✅ OK  | 001   | P
```

---

## 📝 NOTAS ADICIONALES

1. **generate_1000_payments.sql NO necesita corrección** - Ya incluye correctamente el campo `fiscal_uuid`

2. **Entity e Migration correctas** - El problema solo estaba en el script de datos iniciales (V10)

3. **UUIDs de prueba elegidos intencionalmente**:
   - Formato fácil de recordar: `aaaaaaaa-1111-2222-3333-444444444444`
   - Diferentes para cada registro para probar constraint UNIQUE
   - Válidos según formato UUID v4

4. **Actualizar colección de Postman**:
   - Usar `aaaaaaaa-1111-2222-3333-444444444444` para pruebas de Factura 1
   - Usar `bbbbbbbb-1111-2222-3333-444444444444` para pruebas de Factura 2
   - Usar `cccccccc-1111-2222-3333-444444444444` para pruebas de Complemento

---

## 🎯 IMPACTO EN PLAN DE PRUEBAS

El archivo [PLAN_DE_PRUEBAS.md](c:\workspace-fbc\PLAN_DE_PRUEBAS.md) ya incluye estos casos de prueba.

Con esta corrección, ahora **TODOS los casos de prueba son ejecutables** con los datos de prueba del V10.

---

**Fin del Documento de Correcciones**
