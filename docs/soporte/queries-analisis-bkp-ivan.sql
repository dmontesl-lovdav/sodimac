-- ============================================================
-- Analisis estructura TRX_POINTS_BKP vs TRX_POINTS
-- Conexion: f8cloud1129.falabella.cl:1541/arsmxts
-- Usuario: USW_BCT / ubct392sK7
-- Fecha: 2026-03-31
-- ============================================================

-- 1. Estructura de TRX_POINTS_BKP (columnas y tipos)
SELECT column_name, data_type, data_length, nullable
FROM all_tab_columns
WHERE owner = 'SW_CEM'
AND table_name = 'TRX_POINTS_BKP'
ORDER BY column_id;

-- 2. Comparar columnas TRX_POINTS vs TRX_POINTS_BKP
SELECT a.column_name, a.data_type AS tipo_points, b.data_type AS tipo_bkp
FROM all_tab_columns a
FULL OUTER JOIN all_tab_columns b
  ON a.column_name = b.column_name AND b.owner = 'SW_CEM' AND b.table_name = 'TRX_POINTS_BKP'
WHERE a.owner = 'SW_CEM' AND a.table_name = 'TRX_POINTS'
ORDER BY a.column_id;

-- 3. Existe TRX_POINTS_SKU_BKP?
SELECT table_name
FROM all_tables
WHERE owner = 'SW_CEM'
AND table_name LIKE 'TRX_POINTS%'
ORDER BY table_name;

-- 4. Tipos de transaccion en BKP
SELECT TRANSACTIONTYPE, COUNT(*) total
FROM SW_CEM.TRX_POINTS_BKP
GROUP BY TRANSACTIONTYPE
ORDER BY total DESC;

-- 5. Muestra de registros recientes en BKP
SELECT ID, IDENTIFICATIONNUMBER, "DATE", BRANCH, POS, SEQUENCE, POINTS, TRANSACTIONTYPE
FROM SW_CEM.TRX_POINTS_BKP
WHERE ROWNUM <= 10
ORDER BY "DATE" DESC;

-- 6. Cancelaciones en TRX_POINTS (para comparar si aun existen ahi)
SELECT TRANSACTIONTYPE, COUNT(*) total
FROM SW_CEM.TRX_POINTS
WHERE TRANSACTIONTYPE IN ('nc', 'sale_ces', 'cn')
AND "DATE" > TO_DATE('2025-01-01','YYYY-MM-DD')
GROUP BY TRANSACTIONTYPE;
