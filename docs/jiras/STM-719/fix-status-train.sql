-- ============================================================
-- STM-719: Agregar transicion 4 -> 14 en status_train
--          y agregar estatus 14 al CHECK constraint de invoice
-- Motivo: El batch necesita mover facturas a estatus 14
--         (Error en el desglose) cuando falla el XML
-- BD: b2b_portal (PostgreSQL)
--   shared_catalogs.status_train: Columnas option_id, source_status, target_status
--   tenant_fiscal.invoice: CHECK constraint chk_invoice_status
--   option_id=1 → Facturas (tipo I)
--   option_id=2 → NC / otros
-- ============================================================


-- ============================================================
-- PRE-REQUISITO: Agregar estatus 14 al CHECK constraint
-- BD: b2b_portal - tenant_fiscal.invoice
-- ============================================================

-- Verificar constraint actual
SELECT pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conname = 'chk_invoice_status';

-- Ampliar constraint para incluir estatus 14
ALTER TABLE tenant_fiscal.invoice DROP CONSTRAINT chk_invoice_status;
ALTER TABLE tenant_fiscal.invoice ADD CONSTRAINT chk_invoice_status
    CHECK (status = ANY (ARRAY[1,2,3,4,5,6,7,8,9,10,11,12,13,14]));

-- Verificar resultado
SELECT pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conname = 'chk_invoice_status';


-- ============================================================
-- PASO 1: Verificar si ya existe la transicion
-- ============================================================
SELECT *
FROM shared_catalogs.status_train
WHERE source_status = 4
  AND target_status = 14;


-- ============================================================
-- PASO 2: Si no existe, insertar transiciones de estatus 4
-- ============================================================

-- 4 -> 14 (Error en el desglose)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by, created_at)
SELECT 1, 4, 14, 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE option_id = 1
      AND source_status = 4
      AND target_status = 14
);

-- 4 -> 1 (Pendiente Addenda - Facturas, cuando addenda es invalida o falta)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by, created_at)
SELECT 1, 4, 1, 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE option_id = 1
      AND source_status = 4
      AND target_status = 1
);

-- 4 -> 14 (Error desglose - NC)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by, created_at)
SELECT 2, 4, 14, 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE option_id = 2
      AND source_status = 4
      AND target_status = 14
);

-- 4 -> 1 (Pendiente Addenda - NC, cuando addenda es invalida o falta)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by, created_at)
SELECT 2, 4, 1, 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE option_id = 2
      AND source_status = 4
      AND target_status = 1
);


-- ============================================================
-- PASO 3: Verificar todas las transiciones del estatus 4
-- ============================================================
SELECT id, option_id, source_status, target_status
FROM shared_catalogs.status_train
WHERE source_status = 4
ORDER BY option_id, target_status;


-- ============================================================
-- PRUEBAS: Resetear facturas y NC para re-ejecutar el batch
-- BD: b2b_portal - tenant_fiscal.invoice
-- NOTA: Ejecutar solo en ambiente de pruebas, no en produccion
-- ============================================================

-- Ver cuantas facturas estan atascadas en estatus 4
SELECT COUNT(*), document_type
FROM tenant_fiscal.invoice
WHERE status = 4
GROUP BY document_type;

-- Resetear Facturas (tipo I) a estatus 3 para re-procesar
UPDATE tenant_fiscal.invoice
SET status = 3
WHERE status = 4
  AND document_type = 'I';

-- Ver cuantas NC estan en estatus 3 (para proceso NC)
SELECT COUNT(*)
FROM tenant_fiscal.invoice
WHERE status = 3
  AND document_type = 'E';

-- Si no hay NC en estatus 3, resetear algunas para probar
-- (ajustar el IN con los fiscal_uuid que se quieran probar)
-- UPDATE tenant_fiscal.invoice
-- SET status = 3
-- WHERE document_type = 'E'
--   AND status IN (5, 11)
-- LIMIT 5;


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
