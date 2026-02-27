-- =====================================================
-- STM-1212: Script de Eliminacion de Tablas
-- Modulo: Herramientas y Utilerias
-- Base de datos: PostgreSQL
-- Fecha: 2025-12-08
--
-- ADVERTENCIA: Este script elimina las TABLAS completas
-- incluyendo estructura y datos. Usar con extrema precaucion.
-- =====================================================

-- =====================================================
-- DROP TABLES (en orden de dependencias)
-- =====================================================

-- Primero las tablas que dependen de otras
DROP TABLE IF EXISTS application_msg CASCADE;
DROP TABLE IF EXISTS cat_parameter CASCADE;

-- Luego las tablas independientes
DROP TABLE IF EXISTS cat_message CASCADE;
DROP TABLE IF EXISTS cat_process CASCADE;
DROP TABLE IF EXISTS cat_item CASCADE;
DROP TABLE IF EXISTS cat_item_type CASCADE;
DROP TABLE IF EXISTS cat_module CASCADE;

-- =====================================================
-- VERIFICACION
-- =====================================================
SELECT table_name
FROM information_schema.tables
WHERE table_schema = current_schema()
AND table_name IN (
    'cat_module',
    'cat_item_type',
    'cat_item',
    'cat_process',
    'cat_message',
    'application_msg',
    'cat_parameter'
);

-- Si no hay resultados, las tablas fueron eliminadas correctamente

-- =====================================================
-- FIN DEL SCRIPT DE ELIMINACION
-- =====================================================
