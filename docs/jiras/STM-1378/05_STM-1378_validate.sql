-- ============================================================================
-- STM-1378: Script de Validacion Three Way Match
-- Esquema: tenant_finance / shared_catalogs
-- Fecha: 2026-02-11
-- ============================================================================

-- ============================================================================
-- 1. VALIDAR TABLA CREADA
-- ============================================================================

SELECT '=== 1. VALIDACION DE TABLA ===' as validacion;

SELECT
    CASE WHEN COUNT(*) = 1 THEN '[PASS] Tabla three_way_match existe'
         ELSE '[FAIL] Tabla three_way_match NO existe'
    END as resultado
FROM information_schema.tables
WHERE table_schema = 'tenant_finance'
AND table_name = 'three_way_match';


-- ============================================================================
-- 2. VALIDAR VISTAS CREADAS (debe retornar 2 vistas)
-- ============================================================================

SELECT '=== 2. VALIDACION DE VISTAS ===' as validacion;

SELECT
    table_name as vista,
    '[PASS]' as status
FROM information_schema.views
WHERE table_schema = 'tenant_finance'
AND table_name LIKE 'vw_three_way_match%'
ORDER BY table_name;

SELECT
    CASE WHEN COUNT(*) = 2 THEN '[PASS] 2 vistas creadas'
         ELSE '[FAIL] Se esperaban 2 vistas, hay ' || COUNT(*)::text
    END as resultado
FROM information_schema.views
WHERE table_schema = 'tenant_finance'
AND table_name LIKE 'vw_three_way_match%';


-- ============================================================================
-- 3. VALIDAR CATALOGO DE ESTATUS
-- ============================================================================

SELECT '=== 3. VALIDACION DE CATALOGO ===' as validacion;

SELECT
    CASE WHEN COUNT(*) = 1 THEN '[PASS] CatEstatusTWM existe'
         ELSE '[FAIL] CatEstatusTWM NO existe'
    END as resultado
FROM shared_catalogs.catalog_header
WHERE code = 'CatEstatusTWM';


-- ============================================================================
-- 4. VALIDAR INDICES
-- ============================================================================

SELECT '=== 4. VALIDACION DE INDICES ===' as validacion;

SELECT
    indexname as indice,
    '[PASS]' as status
FROM pg_indexes
WHERE schemaname = 'tenant_finance'
AND tablename = 'three_way_match'
AND indexname LIKE 'ix_twm%'
ORDER BY indexname;

SELECT
    CASE WHEN COUNT(*) >= 8 THEN '[PASS] ' || COUNT(*)::text || ' indices creados'
         ELSE '[FAIL] Se esperaban >= 8 indices, hay ' || COUNT(*)::text
    END as resultado
FROM pg_indexes
WHERE schemaname = 'tenant_finance'
AND tablename = 'three_way_match'
AND indexname LIKE 'ix_twm%';


-- ============================================================================
-- 5. VALIDAR CONSTRAINT UNIQUE
-- ============================================================================

SELECT '=== 5. VALIDACION DE CONSTRAINT UNIQUE ===' as validacion;

SELECT
    constraint_name,
    CASE WHEN constraint_name = 'uq_twm_vendor_po_reception'
         THEN '[PASS]' ELSE '[INFO]'
    END as status
FROM information_schema.table_constraints
WHERE table_schema = 'tenant_finance'
AND table_name = 'three_way_match'
AND constraint_type = 'UNIQUE';


-- ============================================================================
-- 6. VALIDAR VISTAS EJECUTAN CORRECTAMENTE
-- ============================================================================

SELECT '=== 6. VALIDACION DE EJECUCION DE VISTAS ===' as validacion;

SELECT 'vw_three_way_match' as vista, COUNT(*) as registros, '[PASS]' as status
FROM tenant_finance.vw_three_way_match
UNION ALL
SELECT 'vw_three_way_match_summary', COUNT(*), '[PASS]'
FROM tenant_finance.vw_three_way_match_summary;


-- ============================================================================
-- 7. RESUMEN FINAL
-- ============================================================================

SELECT '=== RESUMEN DE VALIDACION ===' as validacion;

SELECT
    (SELECT COUNT(*) FROM information_schema.tables
     WHERE table_schema = 'tenant_finance' AND table_name = 'three_way_match') as tabla,
    (SELECT COUNT(*) FROM information_schema.views
     WHERE table_schema = 'tenant_finance' AND table_name LIKE 'vw_three_way_match%') as vistas,
    (SELECT COUNT(*) FROM pg_indexes
     WHERE schemaname = 'tenant_finance' AND tablename = 'three_way_match' AND indexname LIKE 'ix_twm%') as indices,
    (SELECT COUNT(*) FROM shared_catalogs.catalog_detail cd
     JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id
     WHERE ch.code = 'CatEstatusTWM') as estatus_catalogo;


-- ============================================================================
-- FIN DEL SCRIPT DE VALIDACION
-- ============================================================================
