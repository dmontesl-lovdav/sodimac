-- =============================================
-- Script Maestro: Ejecutar todos los scripts de SODIMAC_BATCH_DEV
-- Propósito: Ejecutar todos los scripts en el orden correcto
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

PRINT '===================================';
PRINT 'INICIANDO CREACIÓN DE SODIMAC_BATCH_DEV';
PRINT '===================================';
PRINT '';
GO

-- =============================================
-- 1. Crear Base de Datos
-- =============================================
PRINT '1. Creando base de datos...';
:r 01_create_database.sql
PRINT '';
GO

-- =============================================
-- 2. Crear Tablas
-- =============================================
PRINT '2. Creando tablas...';
:r 02_create_tables.sql
PRINT '';
GO

-- =============================================
-- 3. Insertar Datos Iniciales
-- =============================================
PRINT '3. Insertando datos iniciales...';
:r 03_seed_data.sql
PRINT '';
GO

-- =============================================
-- 4. Crear Stored Procedures
-- =============================================
PRINT '4. Creando stored procedures...';
:r 04_stored_procedures.sql
PRINT '';
GO

-- =============================================
-- 5. Crear Vistas
-- =============================================
PRINT '5. Creando vistas...';
:r 05_views.sql
PRINT '';
GO

-- =============================================
-- Resumen Final
-- =============================================
USE SODIMAC_BATCH_DEV;
GO

PRINT '';
PRINT '===================================';
PRINT 'RESUMEN DE BASE DE DATOS CREADA';
PRINT '===================================';
PRINT '';

-- Mostrar tablas creadas
PRINT 'Tablas creadas:';
SELECT
    TABLE_NAME AS Tabla,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = t.TABLE_NAME) AS Columnas
FROM INFORMATION_SCHEMA.TABLES t
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
PRINT '';

-- Mostrar stored procedures creados
PRINT 'Stored Procedures creados:';
SELECT name AS [Stored Procedure]
FROM sys.procedures
ORDER BY name;
PRINT '';

-- Mostrar vistas creadas
PRINT 'Vistas creadas:';
SELECT TABLE_NAME AS Vista
FROM INFORMATION_SCHEMA.VIEWS
ORDER BY TABLE_NAME;
PRINT '';

-- Mostrar catálogos con datos
PRINT 'Catálogos con datos:';
SELECT
    c.nombre AS Catalogo,
    COUNT(a.idElemento) AS TotalElementos
FROM catCatalogo c
LEFT JOIN adminCatalogo a ON c.idCatalogo = a.idCatalogo
GROUP BY c.nombre
ORDER BY c.nombre;
PRINT '';

PRINT '===================================';
PRINT 'BASE DE DATOS SODIMAC_BATCH_DEV';
PRINT 'CREADA EXITOSAMENTE';
PRINT '===================================';
GO
