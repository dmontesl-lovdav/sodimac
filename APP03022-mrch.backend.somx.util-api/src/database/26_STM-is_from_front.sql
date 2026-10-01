-- ============================================================================
-- Origen de asignacion perfil/rol (is_from_front)
-- 1 = creado por sync de macrorol (Finanzas / Fiscal / Utils)
-- 0 = creado o guardado por el administrador en utilerias
-- Default de columna: FALSE (0). Registros existentes se marcan TRUE (1)
-- para que el sync de macrorol siga pudiendo ajustarlos.
-- Idempotente: no vuelve a backfillear si la columna ya existe.
-- ============================================================================
BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'core_security'
          AND table_name = 'profile_user'
          AND column_name = 'is_from_front'
    ) THEN
        ALTER TABLE core_security.profile_user
            ADD COLUMN is_from_front BOOLEAN NOT NULL DEFAULT FALSE;
        UPDATE core_security.profile_user SET is_from_front = TRUE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'core_security'
          AND table_name = 'role_user'
          AND column_name = 'is_from_front'
    ) THEN
        ALTER TABLE core_security.role_user
            ADD COLUMN is_from_front BOOLEAN NOT NULL DEFAULT FALSE;
        UPDATE core_security.role_user SET is_from_front = TRUE;
    END IF;
END $$;

COMMENT ON COLUMN core_security.profile_user.is_from_front IS
    'TRUE si la fila nacio del sync de macrorol; FALSE si la asigno el admin de utilerias';
COMMENT ON COLUMN core_security.role_user.is_from_front IS
    'TRUE si la fila nacio del sync de macrorol; FALSE si la asigno el admin de utilerias';

COMMIT;
