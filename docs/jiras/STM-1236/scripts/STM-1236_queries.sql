-- ============================================================================
-- STM-1236: Queries del Catalogo de Proveedores
-- Esquema: shared_catalogs
-- Fecha: 2025-12-22
-- ============================================================================

-- ============================================================================
-- 1. CONSULTAS DE PROVEEDORES (supplier)
-- ============================================================================

-- ============================================================================
-- STM-1236: Queries del Catalogo de Proveedores
-- Esquema: shared_catalogs
-- Fecha: 2025-12-22
-- ============================================================================

-- ============================================================================
-- 1. CONSULTAS DE PROVEEDORES (supplier)
-- ============================================================================

-- Listar todos los proveedores activos
SELECT
    s.id,
    s.supplier_number,
    s.rfc,
    s.business_name,
    st.code AS supplier_type_code,
    st.description AS supplier_type_desc,
    pc.condition_name AS payment_condition,
    pc.days AS payment_days,
    s.logo,
    s.status,
    s.created_at,
    s.created_by
FROM shared_catalogs.supplier s
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
LEFT JOIN shared_catalogs.payment_condition pc ON s.payment_condition_id = pc.id
WHERE s.status = 1
ORDER BY s.supplier_number;

-- Buscar proveedor por ID
SELECT
    s.id,
    s.supplier_number,
    s.rfc,
    s.business_name,
    st.code AS supplier_type_code,
    st.description AS supplier_type_desc,
    pc.condition_name AS payment_condition,
    pc.days AS payment_days,
    s.status
FROM shared_catalogs.supplier s
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
LEFT JOIN shared_catalogs.payment_condition pc ON s.payment_condition_id = pc.id
WHERE s.id = 1;

-- Buscar proveedor por numero
SELECT * FROM shared_catalogs.supplier
WHERE supplier_number = 'PROV001';

-- Buscar proveedor por RFC
SELECT * FROM shared_catalogs.supplier
WHERE rfc = 'ABC123456789';

-- Contar proveedores por tipo
SELECT
    st.code,
    st.description,
    COUNT(s.id) AS total_proveedores
FROM shared_catalogs.supplier_type st
LEFT JOIN shared_catalogs.supplier s ON st.id = s.supplier_type_id AND s.status = 1
GROUP BY st.id, st.code, st.description
ORDER BY st.code;

-- Contar proveedores por condicion de pago
select *
FROM shared_catalogs.payment_condition pc
JOIN shared_catalogs.supplier s ON pc.id = s.payment_condition_id AND s.status = 1
;

-- ============================================================================
-- 2. CONSULTAS DE TIPOS DE PROVEEDOR (supplier_type)
-- ============================================================================

-- Listar todos los tipos de proveedor
SELECT
    id,
    code,
    description,
    status,
    created_at
FROM shared_catalogs.supplier_type
WHERE status = 1
ORDER BY code;

-- ============================================================================
-- 3. CONSULTAS DE CONDICIONES DE PAGO (payment_condition)
-- ============================================================================

-- Listar todas las condiciones de pago
SELECT * 
FROM shared_catalogs.payment_condition
WHERE status = 1
ORDER BY 1;




-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
