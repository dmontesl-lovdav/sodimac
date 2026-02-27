-- ============================================================================
-- STM-410: Actualizar constraint de estatus para soportar tren de estatus
-- ============================================================================
-- Problema: El constraint chk_invoice_status solo permite status 1, 2, 3
-- Solucion: Actualizar para permitir todos los estatus del tren (1-13)
-- ============================================================================

-- ============================================================================
-- PASO 1: VALIDACION - Ejecutar para ver el estado actual
-- ============================================================================

-- Ver el constraint actual
SELECT
    tc.constraint_name,
    tc.table_schema,
    tc.table_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_schema = 'tenant_fiscal'
  AND tc.table_name = 'invoice'
  AND tc.constraint_type = 'CHECK';

-- Ver valores de estatus actualmente en uso
SELECT DISTINCT status, COUNT(*) as count
FROM tenant_fiscal.invoice
GROUP BY status
ORDER BY status;

-- ============================================================================
-- PASO 2: CORRECCION - Ejecutar para actualizar el constraint
-- ============================================================================

-- Eliminar el constraint existente
ALTER TABLE tenant_fiscal.invoice
DROP CONSTRAINT IF EXISTS chk_invoice_status;

-- Crear nuevo constraint que permite todos los estatus del tren de estatus
-- Segun STM-1166, los estatus son:
-- 1  = Pendiente Addenda
-- 2  = Validada
-- 3  = Pendiente de Contabilizar
-- 4  = Proceso de descarga
-- 5  = Desglose de factura
-- 6  = Pendiente de envio
-- 7  = Pendiente de pago
-- 8  = Pagado
-- 9  = Complemento de pago
-- 10 = Completado
-- 11 = Rechazo contable
-- 12 = Rechazo de pago
-- 13 = Rechazada

ALTER TABLE tenant_fiscal.invoice
ADD CONSTRAINT chk_invoice_status
CHECK (status IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));

-- ============================================================================
-- PASO 3: VERIFICACION - Ejecutar para confirmar el cambio
-- ============================================================================

-- Verificar que el constraint se actualizo correctamente
SELECT
    tc.constraint_name,
    tc.table_schema,
    tc.table_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_schema = 'tenant_fiscal'
  AND tc.table_name = 'invoice'
  AND tc.constraint_type = 'CHECK';

-- ============================================================================
-- NOTAS:
-- - Este script debe ejecutarse en la base de datos tenant_fiscal
-- - El constraint original probablemente era: CHECK (status IN (1, 2, 3))
-- - El nuevo constraint permite todos los estatus definidos en el tren de estatus
-- - Si hay otros constraints similares, revisar y actualizar tambien
-- ============================================================================
