-- ============================================================
-- STM-719: Migración SODIMAC_SAP_DEV (SQL Server)
-- Agregar columnas fiscal_uuid e invoice_uuid a tabla Comprobante
-- Motivo: El batch intenta insertar/consultar estas columnas que
--         no existen en la tabla original
-- BD: SODIMAC_SAP_DEV (SQL Server)
-- ============================================================


-- ============================================================
-- PRE: Verificar estructura actual de la tabla
-- ============================================================
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Comprobante'
ORDER BY ORDINAL_POSITION;


-- ============================================================
-- PASO 1: Agregar columna fiscal_uuid si no existe
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Comprobante' AND COLUMN_NAME = 'fiscal_uuid'
)
BEGIN
    ALTER TABLE Comprobante ADD fiscal_uuid VARCHAR(36) NOT NULL DEFAULT '';
    PRINT 'Columna fiscal_uuid agregada OK';
END
ELSE
    PRINT 'Columna fiscal_uuid ya existe';


-- ============================================================
-- PASO 2: Agregar columna invoice_uuid si no existe
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Comprobante' AND COLUMN_NAME = 'invoice_uuid'
)
BEGIN
    ALTER TABLE Comprobante ADD invoice_uuid VARCHAR(36) NULL;
    PRINT 'Columna invoice_uuid agregada OK';
END
ELSE
    PRINT 'Columna invoice_uuid ya existe';


-- ============================================================
-- POST: Verificar resultado
-- ============================================================
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Comprobante'
  AND COLUMN_NAME IN ('fiscal_uuid', 'invoice_uuid');
