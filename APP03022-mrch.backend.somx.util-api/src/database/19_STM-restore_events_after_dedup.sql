-- ============================================================================
-- Restauracion de eventos/botones perdidos por el guardado con dedup
-- ============================================================================
-- Contexto:
--   El dedup de display en las pantallas "Aplicativo Evento" y "Perfil Evento"
--   colapsaba los eventos con el mismo label ("Buscar", "Limpiar", "Exportar
--   CSV", etc.) a un solo representante. Al GUARDAR esas pantallas,
--   syncModuleProcessesForModule / syncProfileModuleProcesses desactivan todo lo
--   que no venga en la lista enviada, por lo que los duplicados colapsados
--   quedaron con status = 0. Eso hizo que a muchos usuarios les desaparecieran
--   botones/eventos que si tienen por perfil.
--
--   El dedup ya fue revertido en el codigo. Este script reactiva unicamente los
--   registros que son "duplicados colapsados": inactivos pero con un hermano
--   ACTIVO del mismo label en el mismo ambito (mismo modulo / mismo perfil).
--   No reactiva eventos removidos legitimamente (esos no tienen hermano activo
--   del mismo label).
--
-- Post-condiciones: invalidar cache (DELETE /api/security/user-details/cache o
-- reiniciar util-api) y pedir a los usuarios cerrar/abrir sesion.
-- ============================================================================
BEGIN;

DO $$
DECLARE
    v_mp_restored  int;
    v_pmp_restored int;
BEGIN
    -- 1) Reactivar module_process colapsados: inactivos con un hermano activo
    --    del mismo label en el mismo modulo.
    UPDATE core_security.module_process mp_dead
    SET status = 1, updated_at = now(), updated_by = 'system-restore-dedup'
    FROM shared_catalogs.catalog_detail ev_dead
    WHERE mp_dead.status = 0
      AND ev_dead.id = mp_dead.catalog_detail_process_id
      AND EXISTS (
          SELECT 1
          FROM core_security.module_process mp_live
          JOIN shared_catalogs.catalog_detail ev_live
              ON ev_live.id = mp_live.catalog_detail_process_id
          WHERE mp_live.status = 1
            AND mp_live.catalog_detail_module_id = mp_dead.catalog_detail_module_id
            AND mp_live.module_process_id <> mp_dead.module_process_id
            AND COALESCE((SELECT dl.description FROM shared_catalogs.dictionary_lang dl
                          WHERE dl.dict_id = ev_live.dict_id AND dl.lang_id = 1 LIMIT 1),
                         NULLIF(TRIM(ev_live.value), ''), ev_live.key)
              = COALESCE((SELECT dl.description FROM shared_catalogs.dictionary_lang dl
                          WHERE dl.dict_id = ev_dead.dict_id AND dl.lang_id = 1 LIMIT 1),
                         NULLIF(TRIM(ev_dead.value), ''), ev_dead.key)
      );
    GET DIAGNOSTICS v_mp_restored = ROW_COUNT;

    -- 2) Reactivar profile_module_process colapsados: inactivos, con su
    --    module_process ya activo, y con un hermano pmp activo del mismo label
    --    para el mismo perfil y modulo.
    UPDATE core_security.profile_module_process pmp_dead
    SET status = 1, updated_at = now(), updated_by = 'system-restore-dedup'
    FROM core_security.module_process mp_dead
    JOIN shared_catalogs.catalog_detail ev_dead
        ON ev_dead.id = mp_dead.catalog_detail_process_id
    WHERE pmp_dead.status = 0
      AND mp_dead.module_process_id = pmp_dead.module_process_id
      AND mp_dead.status = 1
      AND EXISTS (
          SELECT 1
          FROM core_security.profile_module_process pmp_live
          JOIN core_security.module_process mp_live
              ON mp_live.module_process_id = pmp_live.module_process_id AND mp_live.status = 1
          JOIN shared_catalogs.catalog_detail ev_live
              ON ev_live.id = mp_live.catalog_detail_process_id
          WHERE pmp_live.status = 1
            AND pmp_live.catalog_detail_profile_id = pmp_dead.catalog_detail_profile_id
            AND mp_live.catalog_detail_module_id = mp_dead.catalog_detail_module_id
            AND mp_live.module_process_id <> mp_dead.module_process_id
            AND COALESCE((SELECT dl.description FROM shared_catalogs.dictionary_lang dl
                          WHERE dl.dict_id = ev_live.dict_id AND dl.lang_id = 1 LIMIT 1),
                         NULLIF(TRIM(ev_live.value), ''), ev_live.key)
              = COALESCE((SELECT dl.description FROM shared_catalogs.dictionary_lang dl
                          WHERE dl.dict_id = ev_dead.dict_id AND dl.lang_id = 1 LIMIT 1),
                         NULLIF(TRIM(ev_dead.value), ''), ev_dead.key)
      );
    GET DIAGNOSTICS v_pmp_restored = ROW_COUNT;

    RAISE NOTICE 'Restore dedup: module_process reactivados=%, profile_module_process reactivados=%',
        v_mp_restored, v_pmp_restored;
END $$;

COMMIT;
