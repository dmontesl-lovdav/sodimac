-- ============================================================================
-- RBAC: cableado COMPLETO del perfil admin (PER009)
-- ============================================================================
-- Sintoma: el admin tiene TODOS los eventos asignados (profile_module_process,
-- p.ej. 119) pero NO ve botones en Facturas (APL009) ni Parametros (APL012),
-- aunque "tiene todos los permisos".
--
-- Causa: el runtime (getSecurityUserDetailsByCatalogKey) arma la lista de
-- aplicativos desde profile_module (aplicativo -> perfil) y SOLO expone los
-- eventos de los aplicativos presentes ahi. Si falta profile_module para un
-- aplicativo, sus eventos no llegan al contexto aunque existan en
-- profile_module_process. Es decir: faltan las asignaciones de APLICATIVO
-- (pantalla "Perfil Aplicativo"), no las de EVENTO.
--
-- Este script, para PER009 y de forma idempotente:
--   1. Reactiva todos los module_process (defensivo).
--   2. Inserta/activa profile_module para TODO aplicativo con algun
--      module_process activo.
--   3. Inserta/activa profile_module_process para TODO module_process activo.
--
-- Post: invalidar cache (DELETE /api/security/user-details/cache o reiniciar
-- util-api) y que el usuario cierre/abra sesion.
-- ============================================================================
BEGIN;

DO $$
DECLARE
    v_admin_profile_key text := 'PER009';
    v_profile_id int;
    v_mp_reactivated int;
    v_pm_new int;
    v_pm_reactivated int;
    v_pmp_new int;
    v_pmp_reactivated int;
BEGIN
    SELECT cd.id INTO v_profile_id
    FROM shared_catalogs.catalog_detail cd
    JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatPerfil'
    WHERE cd.key = v_admin_profile_key;
    IF v_profile_id IS NULL THEN
        RAISE EXCEPTION 'No existe el perfil admin key=%', v_admin_profile_key;
    END IF;

    -- 1) Reactivar todos los module_process (defensivo)
    UPDATE core_security.module_process
    SET status = 1, updated_at = now(), updated_by = 'system-admin-full'
    WHERE status <> 1;
    GET DIAGNOSTICS v_mp_reactivated = ROW_COUNT;

    -- 2) profile_module: aplicativos con module_process activo -> perfil admin
    INSERT INTO core_security.profile_module
        (catalog_detail_profile_id, catalog_detail_module_id, status, created_by, created_at)
    SELECT DISTINCT v_profile_id, mp.catalog_detail_module_id, 1, 'system-admin-full', now()
    FROM core_security.module_process mp
    WHERE mp.status = 1
      AND NOT EXISTS (
          SELECT 1 FROM core_security.profile_module pm
          WHERE pm.catalog_detail_profile_id = v_profile_id
            AND pm.catalog_detail_module_id = mp.catalog_detail_module_id
      );
    GET DIAGNOSTICS v_pm_new = ROW_COUNT;

    UPDATE core_security.profile_module pm
    SET status = 1, updated_at = now(), updated_by = 'system-admin-full'
    WHERE pm.catalog_detail_profile_id = v_profile_id
      AND pm.status <> 1
      AND EXISTS (
          SELECT 1 FROM core_security.module_process mp
          WHERE mp.status = 1 AND mp.catalog_detail_module_id = pm.catalog_detail_module_id
      );
    GET DIAGNOSTICS v_pm_reactivated = ROW_COUNT;

    -- 3) profile_module_process: todo module_process activo -> perfil admin
    INSERT INTO core_security.profile_module_process
        (catalog_detail_profile_id, module_process_id, status, created_by, created_at)
    SELECT v_profile_id, mp.module_process_id, 1, 'system-admin-full', now()
    FROM core_security.module_process mp
    WHERE mp.status = 1
      AND NOT EXISTS (
          SELECT 1 FROM core_security.profile_module_process pmp
          WHERE pmp.catalog_detail_profile_id = v_profile_id
            AND pmp.module_process_id = mp.module_process_id
      );
    GET DIAGNOSTICS v_pmp_new = ROW_COUNT;

    UPDATE core_security.profile_module_process pmp
    SET status = 1, updated_at = now(), updated_by = 'system-admin-full'
    WHERE pmp.catalog_detail_profile_id = v_profile_id
      AND pmp.status <> 1
      AND EXISTS (
          SELECT 1 FROM core_security.module_process mp
          WHERE mp.status = 1 AND mp.module_process_id = pmp.module_process_id
      );
    GET DIAGNOSTICS v_pmp_reactivated = ROW_COUNT;

    RAISE NOTICE 'Admin full wiring: module_process reactivados=%, profile_module nuevos=%, profile_module reactivados=%, profile_module_process nuevos=%, profile_module_process reactivados=%',
        v_mp_reactivated, v_pm_new, v_pm_reactivated, v_pmp_new, v_pmp_reactivated;
END $$;

COMMIT;
