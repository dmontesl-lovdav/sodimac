-- ============================================================================
-- STM-1294: Schema migration for Catalog Elements CRUD
-- Adds new columns to catalog_detail for parent relationships and audit
-- ============================================================================

-- Add parent_catalog_id column to catalog_detail
ALTER TABLE catalog_detail
ADD COLUMN IF NOT EXISTS parent_catalog_id INTEGER;

-- Add parent_element_id column to catalog_detail
ALTER TABLE catalog_detail
ADD COLUMN IF NOT EXISTS parent_element_id INTEGER;

-- Add updated_by column to catalog_detail
ALTER TABLE catalog_detail
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Add indexes for parent relationships (improves query performance)
CREATE INDEX IF NOT EXISTS idx_catalog_detail_parent_catalog_id
ON catalog_detail(parent_catalog_id);

CREATE INDEX IF NOT EXISTS idx_catalog_detail_parent_element_id
ON catalog_detail(parent_element_id);

-- Add foreign key constraint for parent_catalog_id (optional - comment out if not needed)
-- ALTER TABLE catalog_detail
-- ADD CONSTRAINT fk_catalog_detail_parent_catalog
-- FOREIGN KEY (parent_catalog_id) REFERENCES catalog_header(id)
-- ON DELETE SET NULL;

-- Add foreign key constraint for parent_element_id (optional - comment out if not needed)
-- ALTER TABLE catalog_detail
-- ADD CONSTRAINT fk_catalog_detail_parent_element
-- FOREIGN KEY (parent_element_id) REFERENCES catalog_detail(id)
-- ON DELETE SET NULL;

-- ============================================================================
-- Update catalog_type values to match new constants (PRIMARIO/SECUNDARIO)
-- ============================================================================

-- Update existing catalogs to use new type names if needed
-- This is a sample update - adjust based on your existing data
UPDATE catalog_header
SET catalog_type = 'PRIMARIO'
WHERE catalog_type = 'SIMPLE' OR catalog_type IS NULL;

-- ============================================================================
-- Comments for documentation
-- ============================================================================
COMMENT ON COLUMN catalog_detail.parent_catalog_id IS 'ID del catálogo padre (solo para catálogos secundarios)';
COMMENT ON COLUMN catalog_detail.parent_element_id IS 'ID del elemento padre (solo para catálogos secundarios)';
COMMENT ON COLUMN catalog_detail.updated_by IS 'Usuario que actualizó el registro';







