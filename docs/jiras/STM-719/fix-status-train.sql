-- ============================================================
-- STM-719: Agregar transicion 4 -> 14 en status_train
-- Motivo: El batch necesita mover facturas a estatus 14
--         (Error en el desglose) cuando falla el XML
-- BD: b2b_portal (PostgreSQL) - esquema shared_catalogs
-- ============================================================


-- ============================================================
-- PASO 1: Verificar si ya existe la transicion
-- ============================================================
SELECT *
FROM shared_catalogs.status_train
WHERE source_status_id = 4
  AND target_status_id = 14;


-- ============================================================
-- PASO 2: Si no existe, insertar
-- ============================================================
INSERT INTO shared_catalogs.status_train (source_status_id, target_status_id, document_type)
SELECT 4, 14, 'I'
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE source_status_id = 4
      AND target_status_id = 14
      AND document_type = 'I'
);


-- ============================================================
-- PASO 3: Verificar todas las transiciones del estatus 4
-- ============================================================
SELECT st.id,
       s_src.nombre AS estatus_origen,
       s_tgt.nombre AS estatus_destino,
       st.document_type
FROM shared_catalogs.status_train st
JOIN shared_catalogs.status s_src ON s_src.id = st.source_status_id
JOIN shared_catalogs.status s_tgt ON s_tgt.id = st.target_status_id
WHERE st.source_status_id = 4
ORDER BY st.target_status_id;


-- ============================================================
-- MONITOREO: Consultas de trazabilidad en SODIMAC_BATCH_DEV
-- BD: SQL Server 10.138.153.10:1433
-- ============================================================

-- Ver ultimas ejecuciones del batch
SELECT TOP 20 *
FROM ctrlProcesoCab
ORDER BY fecha_inicio DESC;

-- Ver ejecuciones fallidas
SELECT TOP 20 *
FROM ctrlProcesoCab
WHERE estatus = 'FAILED'
ORDER BY fecha_inicio DESC;

-- Ver detalle de una ejecucion especifica (reemplazar id=25)
SELECT *
FROM ctrlProcesoDet
WHERE proceso_cab_id = 25
ORDER BY fecha_inicio;

-- Ver log de errores de una ejecucion (reemplazar id=25)
SELECT *
FROM ctrlLog
WHERE proceso_cab_id = 25
ORDER BY fecha;

-- Ver elementos procesados de una ejecucion (reemplazar id=25)
SELECT *
FROM ctrlProcesoElemento
WHERE proceso_det_id IN (
    SELECT id FROM ctrlProcesoDet WHERE proceso_cab_id = 25
)
ORDER BY fecha_inicio;

-- Resumen de ejecuciones por dia
SELECT
    CAST(fecha_inicio AS DATE) AS dia,
    COUNT(*) AS total_ejecuciones,
    SUM(CASE WHEN estatus = 'SUCCESS' THEN 1 ELSE 0 END) AS exitosas,
    SUM(CASE WHEN estatus = 'FAILED'  THEN 1 ELSE 0 END) AS fallidas,
    SUM(elementos_origen)  AS total_origen,
    SUM(elementos_destino) AS total_procesados
FROM ctrlProcesoCab
GROUP BY CAST(fecha_inicio AS DATE)
ORDER BY dia DESC;
