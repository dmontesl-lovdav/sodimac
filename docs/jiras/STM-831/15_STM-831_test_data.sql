-- ============================================================================
-- STM-831: Script de datos de prueba para filtrado de proveedores
-- Ejecutar despues de 14_STM-831_supplier_types.sql
-- ============================================================================

-- Limpiar datos de prueba existentes (opcional, comentar si no se desea)
-- DELETE FROM shared_catalogs.supplier_block WHERE created_by = 'STM-831-TEST';
-- DELETE FROM shared_catalogs.supplier WHERE created_by = 'STM-831-TEST';

-- ============================================================================
-- 1. PROVEEDORES DE MERCANCIA (supplier_type_id = 1)
-- ============================================================================
INSERT INTO shared_catalogs.supplier (supplier_number, rfc, business_name, supplier_type_id, payment_condition_id, status, created_by, created_at)
VALUES
    ('MERC001', 'MER010101ABC', 'Distribuidora de Materiales del Norte S.A.', 1, 3, 1, 'STM-831-TEST', NOW()),
    ('MERC002', 'MER020202DEF', 'Comercializadora Industrial Maya S.A. de C.V.', 1, 4, 1, 'STM-831-TEST', NOW()),
    ('MERC003', 'MER030303GHI', 'Productos Ferreteros del Centro', 1, 5, 1, 'STM-831-TEST', NOW()),
    ('MERC004', 'MER040404JKL', 'Aceros y Metales Monterrey', 1, 3, 1, 'STM-831-TEST', NOW()),
    ('MERC005', 'MER050505MNO', 'Pinturas y Recubrimientos Pacific', 1, 6, 1, 'STM-831-TEST', NOW())
ON CONFLICT (supplier_number) DO NOTHING;

-- ============================================================================
-- 2. PROVEEDORES DE TRANSPORTE (supplier_type_id = 2)
-- ============================================================================
INSERT INTO shared_catalogs.supplier (supplier_number, rfc, business_name, supplier_type_id, payment_condition_id, status, created_by, created_at)
VALUES
    ('TRANS001', 'TRA010101PQR', 'Transportes Especializados del Bajio S.A.', 2, 2, 1, 'STM-831-TEST', NOW()),
    ('TRANS002', 'TRA020202STU', 'Fletes y Mudanzas Express', 2, 3, 1, 'STM-831-TEST', NOW()),
    ('TRANS003', 'TRA030303VWX', 'Logistica Integral Mexicana', 2, 4, 1, 'STM-831-TEST', NOW())
ON CONFLICT (supplier_number) DO NOTHING;

-- ============================================================================
-- 3. PROVEEDORES INDIRECTOS (supplier_type_id = 3)
-- ============================================================================
INSERT INTO shared_catalogs.supplier (supplier_number, rfc, business_name, supplier_type_id, payment_condition_id, status, created_by, created_at)
VALUES
    ('IND001', 'IND010101YZA', 'Suministros de Oficina Global', 3, 2, 1, 'STM-831-TEST', NOW()),
    ('IND002', 'IND020202BCD', 'Papeleria y Consumibles del Valle', 3, 3, 1, 'STM-831-TEST', NOW()),
    ('IND003', 'IND030303EFG', 'Equipos de Computo Tecnomex', 3, 4, 1, 'STM-831-TEST', NOW()),
    ('IND004', 'IND040404HIJ', 'Servicios de Limpieza Industrial', 3, 2, 1, 'STM-831-TEST', NOW())
ON CONFLICT (supplier_number) DO NOTHING;

-- ============================================================================
-- 4. PROVEEDORES DE SERVICIOS (supplier_type_id = 4)
-- ============================================================================
INSERT INTO shared_catalogs.supplier (supplier_number, rfc, business_name, supplier_type_id, payment_condition_id, status, created_by, created_at)
VALUES
    ('SERV001', 'SRV010101KLM', 'Consultoria Empresarial ABC', 4, 3, 1, 'STM-831-TEST', NOW()),
    ('SERV002', 'SRV020202NOP', 'Mantenimiento Electrico Industrial', 4, 4, 1, 'STM-831-TEST', NOW()),
    ('SERV003', 'SRV030303QRS', 'Seguridad Privada Nacional', 4, 5, 1, 'STM-831-TEST', NOW()),
    ('SERV004', 'SRV040404TUV', 'Asesoria Legal Corporativa', 4, 3, 1, 'STM-831-TEST', NOW())
ON CONFLICT (supplier_number) DO NOTHING;

-- ============================================================================
-- 5. BLOQUEOS ACTIVOS (para probar el filtro de bloqueados)
-- Los bloqueos vigentes son aquellos donde status=1 y CURRENT_DATE entre valid_from y valid_to
-- ============================================================================

-- Bloqueo activo para proveedor de Mercancia MERC002 (vigente)
INSERT INTO shared_catalogs.supplier_block (supplier_number, block_reason, valid_from, valid_to, status, created_by, created_at)
VALUES
    ('MERC002', 'Incumplimiento de contrato - entrega tardia', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '60 days', 1, 'STM-831-TEST', NOW())
ON CONFLICT DO NOTHING;

-- Bloqueo activo para proveedor de Transporte TRANS002 (vigente)
INSERT INTO shared_catalogs.supplier_block (supplier_number, block_reason, valid_from, valid_to, status, created_by, created_at)
VALUES
    ('TRANS002', 'Documentacion de carta porte incompleta', CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '45 days', 1, 'STM-831-TEST', NOW())
ON CONFLICT DO NOTHING;

-- Bloqueo activo para proveedor Indirecto IND003 (vigente)
INSERT INTO shared_catalogs.supplier_block (supplier_number, block_reason, valid_from, valid_to, status, created_by, created_at)
VALUES
    ('IND003', 'Facturacion irregular detectada', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE + INTERVAL '90 days', 1, 'STM-831-TEST', NOW())
ON CONFLICT DO NOTHING;

-- Bloqueo EXPIRADO para MERC003 (no debe aparecer como bloqueado)
INSERT INTO shared_catalogs.supplier_block (supplier_number, block_reason, valid_from, valid_to, status, created_by, created_at)
VALUES
    ('MERC003', 'Bloqueo temporal resuelto', CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE - INTERVAL '30 days', 1, 'STM-831-TEST', NOW())
ON CONFLICT DO NOTHING;

-- Bloqueo INACTIVO para SERV001 (status=0, no debe aparecer como bloqueado)
INSERT INTO shared_catalogs.supplier_block (supplier_number, block_reason, valid_from, valid_to, status, created_by, created_at)
VALUES
    ('SERV001', 'Bloqueo cancelado manualmente', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '50 days', 0, 'STM-831-TEST', NOW())
ON CONFLICT DO NOTHING;

-- ============================================================================
-- RESUMEN DE DATOS DE PRUEBA
-- ============================================================================
-- Total proveedores: 16
--   - Mercancia (tipo 1): 5 proveedores (1 bloqueado: MERC002)
--   - Transporte (tipo 2): 3 proveedores (1 bloqueado: TRANS002)
--   - Indirectos (tipo 3): 4 proveedores (1 bloqueado: IND003)
--   - Servicios (tipo 4): 4 proveedores (0 bloqueados)
--
-- Total bloqueados vigentes: 3 (MERC002, TRANS002, IND003)
-- Total activos (no bloqueados): 13
--
-- Casos de prueba esperados:
-- tipoProveedor=0, estatusBloqueo=2 -> 16 proveedores (todos)
-- tipoProveedor=0, estatusBloqueo=0 -> 3 proveedores (solo bloqueados)
-- tipoProveedor=0, estatusBloqueo=1 -> 13 proveedores (solo activos)
-- tipoProveedor=1, estatusBloqueo=1 -> 4 proveedores (mercancia activos)
-- tipoProveedor=1, estatusBloqueo=0 -> 1 proveedor (mercancia bloqueado)
-- tipoProveedor=2, estatusBloqueo=1 -> 2 proveedores (transporte activos)
-- tipoProveedor=3, estatusBloqueo=0 -> 1 proveedor (indirecto bloqueado)
-- tipoProveedor=4, estatusBloqueo=2 -> 4 proveedores (servicios todos)
-- tipoProveedor=5, estatusBloqueo=1 -> Error BUS214
-- ============================================================================

-- Verificar datos insertados
SELECT 'Proveedores por tipo:' AS info;
SELECT st.code as tipo, COUNT(*) as total
FROM shared_catalogs.supplier s
JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.created_by = 'STM-831-TEST'
GROUP BY st.code
ORDER BY st.code;

SELECT 'Bloqueos vigentes:' AS info;
SELECT sb.supplier_number, sb.block_reason, sb.valid_from, sb.valid_to
FROM shared_catalogs.supplier_block sb
WHERE sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  AND sb.created_by = 'STM-831-TEST';
