-- =============================================
-- Script: Creación de Base de Datos SODIMAC_BATCH_DEV
-- Propósito: Base de datos para monitoreo, trazabilidad y auditoría de ejecuciones batch
-- Autor: Sistema
-- Fecha: 2026-01-20
-- =============================================

USE master;
GO

-- Verificar si la base de datos existe y eliminarla si es necesario (solo para desarrollo)
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'SODIMAC_BATCH_DEV')
BEGIN
    ALTER DATABASE SODIMAC_BATCH_DEV SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE SODIMAC_BATCH_DEV;
    PRINT 'Base de datos SODIMAC_BATCH_DEV eliminada.';
END
GO

-- Crear la base de datos
CREATE DATABASE SODIMAC_BATCH_DEV
ON PRIMARY
(
    NAME = 'SODIMAC_BATCH_DEV_Data',
    FILENAME = 'C:\SQLData\SODIMAC_BATCH_DEV_Data.mdf',
    SIZE = 100MB,
    MAXSIZE = UNLIMITED,
    FILEGROWTH = 50MB
)
LOG ON
(
    NAME = 'SODIMAC_BATCH_DEV_Log',
    FILENAME = 'C:\SQLData\SODIMAC_BATCH_DEV_Log.ldf',
    SIZE = 50MB,
    MAXSIZE = 500MB,
    FILEGROWTH = 25MB
);
GO

PRINT 'Base de datos SODIMAC_BATCH_DEV creada exitosamente.';
GO

-- Configurar opciones de base de datos
ALTER DATABASE SODIMAC_BATCH_DEV SET RECOVERY SIMPLE;
ALTER DATABASE SODIMAC_BATCH_DEV SET AUTO_SHRINK OFF;
ALTER DATABASE SODIMAC_BATCH_DEV SET AUTO_CREATE_STATISTICS ON;
ALTER DATABASE SODIMAC_BATCH_DEV SET AUTO_UPDATE_STATISTICS ON;
GO

PRINT 'Configuración de base de datos completada.';
GO
