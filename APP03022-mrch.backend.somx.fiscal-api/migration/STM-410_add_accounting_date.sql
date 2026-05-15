-- ============================================================================
-- STM-410: Agregar columna accounting_date a tabla invoice
-- ============================================================================
-- Descripción: Agrega la columna para almacenar la fecha de contabilización
--              que se guarda cuando el estatus cambia a 7 (Pendiente de Pago)
--
-- Autor: Sodimac Tech Team
-- Fecha: 2025-01-12
-- JIRA: STM-410
-- ============================================================================

-- Agregar columna accounting_date a la tabla invoice
ALTER TABLE tenant_fiscal.invoice
ADD COLUMN IF NOT EXISTS accounting_date DATE;

-- Comentario explicativo de la columna
COMMENT ON COLUMN tenant_fiscal.invoice.accounting_date IS 'Fecha de contabilización. Se guarda cuando el estatus cambia a 7 (Pendiente de Pago) - STM-410';

-- Crear índice para búsquedas por fecha de contabilización
CREATE INDEX IF NOT EXISTS idx_invoice_accounting_date ON tenant_fiscal.invoice(accounting_date);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
