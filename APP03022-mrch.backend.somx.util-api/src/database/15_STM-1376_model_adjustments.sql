-- ============================================================================
-- STM-1376: Ajustes al Modelo de Datos para Catalogos y Proveedores
-- Esquema: shared_catalogs
-- Fecha: 2026-02-23
-- ============================================================================

-- 1. supplier: agregar email_financial
ALTER TABLE shared_catalogs.supplier
    ADD COLUMN IF NOT EXISTS email_financial VARCHAR(255);

COMMENT ON COLUMN shared_catalogs.supplier.email_financial IS 'Correo electronico financiero del proveedor';

-- 2. catalog_header: agregar campos de auditoria
ALTER TABLE shared_catalogs.catalog_header
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

COMMENT ON COLUMN shared_catalogs.catalog_header.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.catalog_header.updated_by IS 'Usuario que actualizo el registro';

-- 3. catalog_detail_relation: agregar vigencias y auditoria
ALTER TABLE shared_catalogs.catalog_detail_relation
    ADD COLUMN IF NOT EXISTS valid_from DATE,
    ADD COLUMN IF NOT EXISTS valid_to DATE,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.valid_from IS 'Fecha de inicio de vigencia (ISO-8601)';
COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.valid_to IS 'Fecha de fin de vigencia (ISO-8601)';
COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.updated_by IS 'Usuario que actualizo el registro';
COMMENT ON COLUMN shared_catalogs.catalog_detail_relation.updated_at IS 'Fecha de ultima actualizacion';

-- 4. Eliminar tablas obsoletas (supplier_type se MANTIENE)
DROP TABLE IF EXISTS shared_catalogs.accounting_account CASCADE;
DROP TABLE IF EXISTS shared_catalogs.benefit_center CASCADE;
DROP TABLE IF EXISTS shared_catalogs.cost_center CASCADE;
