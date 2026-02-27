-- Script para verificar datos en la base de datos
-- Ejecutar en pgAdmin o psql

-- Verificar tablas creadas
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Consultar datos de ejemplo (si existen)
SELECT COUNT(*) as total_records FROM authorized_receiver_catalog;
SELECT COUNT(*) as total_records FROM concept_catalog;
SELECT COUNT(*) as total_records FROM country_catalog;

-- Ver estructura de una tabla
\d authorized_receiver_catalog;