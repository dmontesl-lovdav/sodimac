SELECT tablename, concat('DROP TABLE IF EXISTS ' || tablename || ' CASCADE;') 
FROM pg_tables 
WHERE schemaname = current_schema()

-- Script para borrar todas las tablas, vistas, secuencias, índices y funciones
-- de la base de datos para comenzar desde cero
-- ADVERTENCIA: Este script eliminará TODOS los datos. Usar con precaución.

-- Desactivar las verificaciones de claves foráneas temporalmente
SET session_replication_role = 'replica';

-- Borrar todas las vistas
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema()) LOOP
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
END $$;

-- Borrar todas las secuencias
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = current_schema()) LOOP
        EXECUTE 'DROP SEQUENCE IF EXISTS ' || quote_ident(r.sequence_name) || ' CASCADE';
    END LOOP;
END $$;

-- Borrar todas las funciones
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT proname, oidvectortypes(proargtypes) as argtypes
              FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid)
              WHERE ns.nspname = current_schema()) LOOP
        EXECUTE 'DROP FUNCTION IF EXISTS ' || quote_ident(r.proname) || '(' || r.argtypes || ') CASCADE';
    END LOOP;
END $$;

-- Borrar todos los tipos personalizados (enums, etc.)
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT typname FROM pg_type
              INNER JOIN pg_namespace ns ON (pg_type.typnamespace = ns.oid)
              WHERE ns.nspname = current_schema() AND typtype = 'e') LOOP
        EXECUTE 'DROP TYPE IF EXISTS ' || quote_ident(r.typname) || ' CASCADE';
    END LOOP;
END $$;

-- Reactivar las verificaciones de claves foráneas
SET session_replication_role = 'origin';

-- Mensaje de confirmación
DO $$
BEGIN
    RAISE NOTICE 'Base de datos limpiada exitosamente. Todas las tablas, secuencias, funciones y tipos han sido eliminados.';
END $$;






