-- Eliminar índices existentes si existen
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_NumeroProveedor' AND object_id = OBJECT_ID('RebateAcuerdosTemp'))
    DROP INDEX IX_RebateAcuerdosTemp_NumeroProveedor ON RebateAcuerdosTemp;

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_NumeroAcuerdo' AND object_id = OBJECT_ID('RebateAcuerdosTemp'))
    DROP INDEX IX_RebateAcuerdosTemp_NumeroAcuerdo ON RebateAcuerdosTemp;

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_Familia' AND object_id = OBJECT_ID('RebateAcuerdosTemp'))
    DROP INDEX IX_RebateAcuerdosTemp_Familia ON RebateAcuerdosTemp;

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_TipoAcuerdo' AND object_id = OBJECT_ID('RebateAcuerdosTemp'))
    DROP INDEX IX_RebateAcuerdosTemp_TipoAcuerdo ON RebateAcuerdosTemp;

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_Estado' AND object_id = OBJECT_ID('RebateAcuerdosTemp'))
    DROP INDEX IX_RebateAcuerdosTemp_Estado ON RebateAcuerdosTemp;

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RebateAcuerdosTemp_Proveedor_Acuerdo' AND object_id = OBJECT_ID('RebateAcuerdosTemp'))
    DROP INDEX IX_RebateAcuerdosTemp_Proveedor_Acuerdo ON RebateAcuerdosTemp;

-- Crear índice compuesto principal: NumeroProveedor + NumeroAcuerdo
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Proveedor_Acuerdo
ON RebateAcuerdosTemp(NumeroProveedor, NumeroAcuerdo)
WITH (FILLFACTOR = 90, ONLINE = ON);

-- Crear índice para búsquedas por Familia con INCLUDE
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Familia
ON RebateAcuerdosTemp(Familia)
INCLUDE (NumeroProveedor, NumeroAcuerdo, RazonSocial)
WITH (FILLFACTOR = 90, ONLINE = ON);

-- Crear índice para búsquedas por Tipo de Acuerdo con INCLUDE
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_TipoAcuerdo
ON RebateAcuerdosTemp(TipoAcuerdo)
INCLUDE (NumeroProveedor, NumeroAcuerdo, Moneda, Valor)
WITH (FILLFACTOR = 90, ONLINE = ON);

-- Crear índice para búsquedas por Estado
CREATE NONCLUSTERED INDEX IX_RebateAcuerdosTemp_Estado
ON RebateAcuerdosTemp(Estado)
WITH (FILLFACTOR = 90, ONLINE = ON);

-- Actualizar estadísticas
UPDATE STATISTICS RebateAcuerdosTemp WITH FULLSCAN;

-- Habilitar auto-update de estadísticas
ALTER DATABASE SODIMAC_REBATES_DEV SET AUTO_UPDATE_STATISTICS ON;
ALTER DATABASE SODIMAC_REBATES_DEV SET AUTO_UPDATE_STATISTICS_ASYNC ON;
