-- ============================================================================
-- Script de Optimización de Índices para Rebate Agreements Sync
-- ============================================================================
-- Propósito: Optimizar índices para mejorar el rendimiento de inserciones
--            masivas diarias y consultas frecuentes
-- ============================================================================

USE SODIMAC_REBATES_DEV;
GO

PRINT '========================================';
PRINT 'Optimizando índices de RebateAcuerdosTemp';
PRINT '========================================';
PRINT '';

-- ============================================================================
-- PASO 1: Eliminar índices existentes que serán reemplazados
-- ============================================================================
PRINT 'PASO 1: Limpiando índices antiguos...';

-- Eliminar índices individuales para reemplazarlos con índices compuestos
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_NumeroProveedor')
BEGIN
    DROP INDEX IX_RebateAcuerdosTemp_NumeroProveedor ON RebateAcuerdosTemp;
    PRINT '  ✓ Índice individual NumeroProveedor eliminado';
END

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_NumeroAcuerdo')
BEGIN
    DROP INDEX IX_RebateAcuerdosTemp_NumeroAcuerdo ON RebateAcuerdosTemp;
    PRINT '  ✓ Índice individual NumeroAcuerdo eliminado';
END

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_Familia')
BEGIN
    DROP INDEX IX_RebateAcuerdosTemp_Familia ON RebateAcuerdosTemp;
    PRINT '  ✓ Índice individual Familia eliminado';
END

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_TipoAcuerdo')
BEGIN
    DROP INDEX IX_RebateAcuerdosTemp_TipoAcuerdo ON RebateAcuerdosTemp;
    PRINT '  ✓ Índice individual TipoAcuerdo eliminado';
END

PRINT '';

-- ============================================================================
-- PASO 2: Crear índices optimizados con FILLFACTOR
-- ============================================================================
PRINT 'PASO 2: Creando índices optimizados...';

-- Índice compuesto principal: NumeroProveedor + NumeroAcuerdo
-- FILLFACTOR 90: Deja 10% de espacio libre para reducir fragmentación
-- en inserciones masivas diarias
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Proveedor_Acuerdo
ON RebateAcuerdosTemp(NumeroProveedor, NumeroAcuerdo)
WITH (FILLFACTOR = 90, ONLINE = ON);
PRINT '  ✓ Índice compuesto Proveedor+Acuerdo creado (FILLFACTOR 90)';

-- Índice para búsquedas por Familia
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Familia
ON RebateAcuerdosTemp(Familia)
INCLUDE (NumeroProveedor, NumeroAcuerdo, RazonSocial)
WITH (FILLFACTOR = 90, ONLINE = ON);
PRINT '  ✓ Índice Familia con INCLUDE creado';

-- Índice para búsquedas por Tipo de Acuerdo
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_TipoAcuerdo
ON RebateAcuerdosTemp(TipoAcuerdo)
INCLUDE (NumeroProveedor, NumeroAcuerdo, Moneda, Valor)
WITH (FILLFACTOR = 90, ONLINE = ON);
PRINT '  ✓ Índice TipoAcuerdo con INCLUDE creado';

-- Índice para búsquedas por Estado
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Estado
ON RebateAcuerdosTemp(Estado)
WITH (FILLFACTOR = 90, ONLINE = ON);
PRINT '  ✓ Índice Estado creado';

PRINT '';

-- ============================================================================
-- PASO 3: Actualizar estadísticas
-- ============================================================================
PRINT 'PASO 3: Actualizando estadísticas...';

UPDATE STATISTICS RebateAcuerdosTemp WITH FULLSCAN;
PRINT '  ✓ Estadísticas actualizadas';

PRINT '';

-- ============================================================================
-- PASO 4: Configurar actualización automática de estadísticas
-- ============================================================================
PRINT 'PASO 4: Configurando actualización automática...';

-- Habilitar auto-update de estadísticas
ALTER DATABASE SODIMAC_REBATES_DEV SET AUTO_UPDATE_STATISTICS ON;
ALTER DATABASE SODIMAC_REBATES_DEV SET AUTO_UPDATE_STATISTICS_ASYNC ON;
PRINT '  ✓ Actualización automática de estadísticas habilitada';

PRINT '';

-- ============================================================================
-- PASO 5: Mostrar resumen de índices
-- ============================================================================
PRINT '========================================';
PRINT 'Resumen de Índices Optimizados';
PRINT '========================================';

SELECT
    i.name AS NombreIndice,
    i.type_desc AS TipoIndice,
    i.fill_factor AS FillFactor,
    STUFF((
        SELECT ', ' + c.name
        FROM sys.index_columns ic
        INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
        WHERE ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.is_included_column = 0
        ORDER BY ic.key_ordinal
        FOR XML PATH('')
    ), 1, 2, '') AS ColumnasIndexadas,
    STUFF((
        SELECT ', ' + c.name
        FROM sys.index_columns ic
        INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
        WHERE ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.is_included_column = 1
        FOR XML PATH('')
    ), 1, 2, '') AS ColumnasIncluidas
FROM sys.indexes i
WHERE i.object_id = OBJECT_ID('RebateAcuerdosTemp')
AND i.name IS NOT NULL
ORDER BY i.name;

PRINT '';
PRINT '========================================';
PRINT '✅ Optimización completada exitosamente';
PRINT '========================================';
PRINT '';
PRINT 'Beneficios:';
PRINT '  • FILLFACTOR 90: Reduce fragmentación en inserciones masivas';
PRINT '  • Índices compuestos: Mejoran consultas multi-columna';
PRINT '  • INCLUDE: Evita lookups adicionales (covering index)';
PRINT '  • Estadísticas auto: Mantiene planes de ejecución óptimos';
PRINT '';
