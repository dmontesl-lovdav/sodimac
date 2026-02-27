-- ============================================================================
-- CATALOGOS API - Eliminación de Datos y Tablas
-- Esquema: shared_catalogs
-- ADVERTENCIA: Este script elimina TODOS los datos y tablas del esquema
-- ============================================================================

SET search_path TO shared_catalogs;

-- Eliminar tablas en orden por dependencias (detalle primero, luego header, luego dictionary)
DROP TABLE IF EXISTS shared_catalogs.catalog_detail CASCADE;
DROP TABLE IF EXISTS shared_catalogs.catalog_header CASCADE;
DROP TABLE IF EXISTS shared_catalogs.dictionary_lang CASCADE;

-- Opcional: Eliminar el esquema completo (descomentar si es necesario)
-- DROP SCHEMA IF EXISTS shared_catalogs CASCADE;

-- ============================================================================
-- FIN DEL SCRIPT DE ELIMINACIÓN
-- ============================================================================
