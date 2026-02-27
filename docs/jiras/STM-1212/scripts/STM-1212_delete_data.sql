-- =====================================================
-- STM-1212: Script de Borrado de Datos
-- Modulo: Herramientas y Utilerias
-- Base de datos: PostgreSQL
-- Fecha: 2025-12-08
--
-- ADVERTENCIA: Este script elimina TODOS los datos
-- de las tablas del modulo. Usar con precaucion.
-- =====================================================

-- =====================================================
-- OPCION 1: BORRADO COMPLETO (TRUNCATE)
-- Mas rapido, reinicia secuencias
-- =====================================================

-- Deshabilitar restricciones temporalmente si hay FKs
-- SET session_replication_role = 'replica';

-- Borrar en orden inverso a las dependencias
TRUNCATE TABLE application_msg RESTART IDENTITY CASCADE;
TRUNCATE TABLE cat_parameter RESTART IDENTITY CASCADE;
TRUNCATE TABLE cat_message RESTART IDENTITY CASCADE;
TRUNCATE TABLE cat_process RESTART IDENTITY CASCADE;
TRUNCATE TABLE cat_item RESTART IDENTITY CASCADE;
TRUNCATE TABLE cat_item_type RESTART IDENTITY CASCADE;
TRUNCATE TABLE cat_module RESTART IDENTITY CASCADE;

-- Rehabilitar restricciones
-- SET session_replication_role = 'origin';

-- =====================================================
-- OPCION 2: BORRADO CON DELETE (alternativa)
-- Mas lento pero permite WHERE
-- =====================================================

/*
-- Borrar en orden inverso a las dependencias
DELETE FROM application_msg;
DELETE FROM cat_parameter;
DELETE FROM cat_message;
DELETE FROM cat_process;
DELETE FROM cat_item;
DELETE FROM cat_item_type;
DELETE FROM cat_module;

-- Reiniciar secuencias manualmente
ALTER SEQUENCE cat_module_id_module_seq RESTART WITH 1;
ALTER SEQUENCE cat_item_type_id_item_type_seq RESTART WITH 1;
ALTER SEQUENCE cat_item_id_item_seq RESTART WITH 1;
ALTER SEQUENCE cat_process_id_process_seq RESTART WITH 1;
ALTER SEQUENCE cat_message_id_message_seq RESTART WITH 1;
ALTER SEQUENCE application_msg_id_application_msg_seq RESTART WITH 1;
ALTER SEQUENCE cat_parameter_id_parameter_seq RESTART WITH 1;
*/

-- =====================================================
-- OPCION 3: BORRADO SELECTIVO POR TABLA
-- Para borrar solo datos especificos
-- =====================================================

/*
-- Borrar solo mensajes de error
DELETE FROM cat_message WHERE message_code LIKE 'ERR%';

-- Borrar parametros de un modulo especifico
DELETE FROM cat_parameter WHERE id_module = 1;

-- Borrar mensajes por aplicativo especifico
DELETE FROM application_msg WHERE id_application = 3;

-- Borrar procesos inactivos (si hubiera columna status)
-- DELETE FROM cat_process WHERE status = 0;
*/

-- =====================================================
-- VERIFICACION DE BORRADO
-- =====================================================
SELECT 'cat_module' AS tabla, COUNT(*) AS registros FROM cat_module
UNION ALL
SELECT 'cat_item_type', COUNT(*) FROM cat_item_type
UNION ALL
SELECT 'cat_item', COUNT(*) FROM cat_item
UNION ALL
SELECT 'cat_process', COUNT(*) FROM cat_process
UNION ALL
SELECT 'cat_message', COUNT(*) FROM cat_message
UNION ALL
SELECT 'application_msg', COUNT(*) FROM application_msg
UNION ALL
SELECT 'cat_parameter', COUNT(*) FROM cat_parameter;

-- =====================================================
-- FIN DEL SCRIPT DE BORRADO
-- =====================================================
