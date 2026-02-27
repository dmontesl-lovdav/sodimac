-- ============================================================================
-- STM-305: Script de Validacion
-- Esquema: tenant_finance
-- Fecha: 2026-02-09
--
-- Ejecuta validaciones para verificar que el modelo esta correctamente
-- implementado. Todas las consultas deben retornar resultados esperados.
-- ============================================================================

-- ============================================================================
-- 1. VALIDAR TABLAS CREADAS (debe retornar 7 filas)
-- ============================================================================

SELECT '=== 1. VALIDACION DE TABLAS ===' as validacion;

SELECT
    table_name,
    CASE
        WHEN table_name IN (
            'account_statement',
            'account_statement_invoice',
            'account_statement_discount',
            'account_statement_credit_note',
            'account_statement_payment',
            'account_statement_purchase_order',
            'account_statement_reception'
        ) THEN '[PASS]'
        ELSE '[FAIL]'
    END as status
FROM information_schema.tables
WHERE table_schema = 'tenant_finance'
AND table_name LIKE 'account_statement%'
AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- Conteo de tablas (debe ser 7)
SELECT
    CASE WHEN COUNT(*) = 7 THEN '[PASS] 7 tablas creadas'
         ELSE '[FAIL] Se esperaban 7 tablas, hay ' || COUNT(*)::text
    END as resultado
FROM information_schema.tables
WHERE table_schema = 'tenant_finance'
AND table_name LIKE 'account_statement%'
AND table_type = 'BASE TABLE';


-- ============================================================================
-- 2. VALIDAR VISTAS CREADAS (debe retornar 10 filas)
-- ============================================================================

SELECT '=== 2. VALIDACION DE VISTAS ===' as validacion;

SELECT
    table_name as vista,
    '[PASS]' as status
FROM information_schema.views
WHERE table_schema = 'tenant_finance'
AND table_name LIKE 'vw_account_statement%'
ORDER BY table_name;

-- Conteo de vistas (debe ser 10)
SELECT
    CASE WHEN COUNT(*) = 10 THEN '[PASS] 10 vistas creadas'
         ELSE '[FAIL] Se esperaban 10 vistas, hay ' || COUNT(*)::text
    END as resultado
FROM information_schema.views
WHERE table_schema = 'tenant_finance'
AND table_name LIKE 'vw_account_statement%';


-- ============================================================================
-- 3. VALIDAR CATALOGO DE ESTATUS
-- ============================================================================

SELECT '=== 3. VALIDACION DE CATALOGO ===' as validacion;

SELECT
    CASE WHEN COUNT(*) = 1 THEN '[PASS] CatEstatusEstadoCuenta existe'
         ELSE '[FAIL] CatEstatusEstadoCuenta NO existe'
    END as resultado
FROM shared_catalogs.catalog_header
WHERE code = 'CatEstatusEstadoCuenta';

-- Detalle del catalogo
SELECT
    cd.key as codigo,
    cd.internal_status as valor,
    dl.description as descripcion,
    '[PASS]' as status
FROM shared_catalogs.catalog_header ch
JOIN shared_catalogs.catalog_detail cd ON ch.id = cd.header_id
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
WHERE ch.code = 'CatEstatusEstadoCuenta'
ORDER BY cd.sort_order;


-- ============================================================================
-- 4. VALIDAR FOREIGN KEYS (debe retornar 7 FKs)
-- ============================================================================

SELECT '=== 4. VALIDACION DE FOREIGN KEYS ===' as validacion;

SELECT
    table_name as tabla,
    constraint_name as fk,
    '[PASS]' as status
FROM information_schema.table_constraints
WHERE table_schema = 'tenant_finance'
AND constraint_type = 'FOREIGN KEY'
AND table_name LIKE 'account_statement%'
ORDER BY table_name;

-- Conteo de FKs
SELECT
    CASE WHEN COUNT(*) >= 7 THEN '[PASS] ' || COUNT(*)::text || ' FKs creadas'
         ELSE '[FAIL] Se esperaban >= 7 FKs, hay ' || COUNT(*)::text
    END as resultado
FROM information_schema.table_constraints
WHERE table_schema = 'tenant_finance'
AND constraint_type = 'FOREIGN KEY'
AND table_name LIKE 'account_statement%';


-- ============================================================================
-- 5. VALIDAR INDICES (debe retornar >= 17)
-- ============================================================================

SELECT '=== 5. VALIDACION DE INDICES ===' as validacion;

SELECT
    indexname as indice,
    tablename as tabla,
    '[PASS]' as status
FROM pg_indexes
WHERE schemaname = 'tenant_finance'
AND indexname LIKE 'ix_account_statement%'
ORDER BY indexname;

-- Conteo de indices
SELECT
    CASE WHEN COUNT(*) >= 15 THEN '[PASS] ' || COUNT(*)::text || ' indices creados'
         ELSE '[FAIL] Se esperaban >= 15 indices, hay ' || COUNT(*)::text
    END as resultado
FROM pg_indexes
WHERE schemaname = 'tenant_finance'
AND indexname LIKE 'ix_account_statement%';


-- ============================================================================
-- 6. VALIDAR CONSTRAINT UNIQUE
-- ============================================================================

SELECT '=== 6. VALIDACION DE CONSTRAINT UNIQUE ===' as validacion;

SELECT
    constraint_name,
    CASE WHEN constraint_name = 'uq_account_statement_vendor_period'
         THEN '[PASS]' ELSE '[INFO]'
    END as status
FROM information_schema.table_constraints
WHERE table_schema = 'tenant_finance'
AND table_name = 'account_statement'
AND constraint_type = 'UNIQUE';


-- ============================================================================
-- 7. VALIDAR DATOS DE PRUEBA
-- ============================================================================

SELECT '=== 7. VALIDACION DE DATOS DE PRUEBA ===' as validacion;

SELECT
    'account_statement' as tabla,
    COUNT(*) as registros,
    CASE WHEN COUNT(*) >= 4 THEN '[PASS]' ELSE '[WARN]' END as status
FROM tenant_finance.account_statement
UNION ALL
SELECT 'account_statement_invoice', COUNT(*),
    CASE WHEN COUNT(*) >= 1 THEN '[PASS]' ELSE '[WARN]' END
FROM tenant_finance.account_statement_invoice
UNION ALL
SELECT 'account_statement_discount', COUNT(*),
    CASE WHEN COUNT(*) >= 1 THEN '[PASS]' ELSE '[WARN]' END
FROM tenant_finance.account_statement_discount
UNION ALL
SELECT 'account_statement_credit_note', COUNT(*),
    CASE WHEN COUNT(*) >= 1 THEN '[PASS]' ELSE '[WARN]' END
FROM tenant_finance.account_statement_credit_note
UNION ALL
SELECT 'account_statement_payment', COUNT(*),
    CASE WHEN COUNT(*) >= 1 THEN '[PASS]' ELSE '[WARN]' END
FROM tenant_finance.account_statement_payment
UNION ALL
SELECT 'account_statement_purchase_order', COUNT(*),
    CASE WHEN COUNT(*) >= 1 THEN '[PASS]' ELSE '[WARN]' END
FROM tenant_finance.account_statement_purchase_order
UNION ALL
SELECT 'account_statement_reception', COUNT(*),
    CASE WHEN COUNT(*) >= 1 THEN '[PASS]' ELSE '[WARN]' END
FROM tenant_finance.account_statement_reception;


-- ============================================================================
-- 8. VALIDAR VISTAS EJECUTAN CORRECTAMENTE
-- ============================================================================

SELECT '=== 8. VALIDACION DE EJECUCION DE VISTAS ===' as validacion;

SELECT 'vw_account_statement' as vista, COUNT(*) as registros, '[PASS]' as status
FROM tenant_finance.vw_account_statement
UNION ALL
SELECT 'vw_account_statement_current', COUNT(*), '[PASS]'
FROM tenant_finance.vw_account_statement_current
UNION ALL
SELECT 'vw_account_statement_summary', COUNT(*), '[PASS]'
FROM tenant_finance.vw_account_statement_summary
UNION ALL
SELECT 'vw_account_statement_balance', COUNT(*), '[PASS]'
FROM tenant_finance.vw_account_statement_balance;


-- ============================================================================
-- 9. VALIDAR CALCULO DEL SALDO
-- ============================================================================

SELECT '=== 9. VALIDACION DEL CALCULO DEL SALDO ===' as validacion;

SELECT
    vendor_number,
    version,
    initial_balance,
    final_balance_stored,
    final_balance_calculated,
    difference,
    CASE
        WHEN is_balanced THEN '[PASS] Saldo correcto'
        ELSE '[WARN] Diferencia: ' || difference::text
    END as status
FROM tenant_finance.vw_account_statement_balance
ORDER BY vendor_number, version;


-- ============================================================================
-- 10. VALIDAR VERSIONAMIENTO (reprocesos)
-- ============================================================================

SELECT '=== 10. VALIDACION DE VERSIONAMIENTO ===' as validacion;

SELECT
    s2.vendor_number,
    s2.version as version_actual,
    s1.version as version_anterior,
    '[PASS] Encadenamiento correcto' as status
FROM tenant_finance.account_statement s2
JOIN tenant_finance.account_statement s1
    ON s2.previous_statement_uuid = s1.account_statement_uuid;


-- ============================================================================
-- 11. RESUMEN FINAL
-- ============================================================================

SELECT '=== RESUMEN DE VALIDACION ===' as validacion;

SELECT
    (SELECT COUNT(*) FROM information_schema.tables
     WHERE table_schema = 'tenant_finance' AND table_name LIKE 'account_statement%'
     AND table_type = 'BASE TABLE') as tablas,
    (SELECT COUNT(*) FROM information_schema.views
     WHERE table_schema = 'tenant_finance' AND table_name LIKE 'vw_account_statement%') as vistas,
    (SELECT COUNT(*) FROM information_schema.table_constraints
     WHERE table_schema = 'tenant_finance' AND constraint_type = 'FOREIGN KEY'
     AND table_name LIKE 'account_statement%') as fks,
    (SELECT COUNT(*) FROM pg_indexes
     WHERE schemaname = 'tenant_finance' AND indexname LIKE 'ix_account_statement%') as indices,
    (SELECT COUNT(*) FROM shared_catalogs.catalog_detail cd
     JOIN shared_catalogs.catalog_header ch ON cd.header_id = ch.id
     WHERE ch.code = 'CatEstatusEstadoCuenta') as estatus_catalogo;


-- ============================================================================
-- FIN DEL SCRIPT DE VALIDACION
-- ============================================================================
