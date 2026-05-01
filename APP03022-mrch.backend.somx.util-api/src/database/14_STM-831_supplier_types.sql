-- ============================================================================
-- STM-831: Actualizar tipos de proveedor
-- Fecha: 2026-02-05
-- Descripcion: Reemplazar tipos de proveedor de prueba por tipos definitivos
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- PASO 1: Eliminar tipos de proveedor de prueba (NAC, INT, MIX)
-- ============================================================================

-- Primero desasociar proveedores existentes del tipo (para evitar FK violation)
-- Los proveedores quedaran temporalmente sin tipo asignado
UPDATE shared_catalogs.supplier SET supplier_type_id = NULL WHERE supplier_type_id IS NOT NULL;

-- Eliminar tipos de prueba
DELETE FROM shared_catalogs.supplier_type;

-- ============================================================================
-- PASO 2: Insertar tipos de proveedor definitivos segun STM-831
-- ============================================================================

INSERT INTO shared_catalogs.supplier_type (id, code, description, status, created_by, created_at) VALUES
(1, 'MERCANCIA', 'Proveedores de mercancia ODBMS', 1, 'STM-831', NOW()),
(2, 'TRANSPORTE', 'Proveedores de transporte Carta Porte', 1, 'STM-831', NOW()),
(3, 'INDIRECTOS', 'Proveedores de insumos SAP', 1, 'STM-831', NOW()),
(4, 'SERVICIOS', 'Proveedores de servicios', 1, 'STM-831', NOW());

-- ============================================================================
-- PASO 3: Resetear secuencia para nuevos registros
-- ============================================================================

SELECT setval('shared_catalogs.supplier_type_id_seq', 4, true);

-- ============================================================================
-- PASO 4: Asignar tipo por defecto a proveedores existentes (MERCANCIA)
-- Esto es temporal, se debe actualizar manualmente segun corresponda
-- ============================================================================

UPDATE shared_catalogs.supplier SET supplier_type_id = 1 WHERE supplier_type_id IS NULL;

-- ============================================================================
-- VERIFICACION
-- ============================================================================

SELECT 'Tipos de proveedor actualizados:' AS info;
SELECT id, code, description, status FROM shared_catalogs.supplier_type ORDER BY id;

SELECT 'Proveedores con tipo asignado:' AS info;
SELECT s.id, s.supplier_number, s.business_name, st.code AS tipo
FROM shared_catalogs.supplier s
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
ORDER BY s.id;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
