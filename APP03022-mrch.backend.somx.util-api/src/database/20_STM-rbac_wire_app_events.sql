-- ============================================================================
-- Wiring RBAC: enlaza cada aplicativo con las event keys que usa su frontend
-- ============================================================================
-- Todos los frontends (util.spa, finanzas-spa, fiscal.spa) resuelven cada boton
-- con hasEvent(APP_KEY, EVENT_KEY) usando un set COMUN de event keys
-- (EVT001..EVT018 y EVT0008). Para que un boton aparezca deben existir y estar
-- activos:
--   1) module_process           (aplicativo  -> evento)
--   2) profile_module           (perfil admin -> aplicativo)
--   3) profile_module_process   (perfil admin -> module_process)
--
-- Facturacion (APL009) y Notas de Credito (APL010) no mostraban permisos porque
-- estos enlaces nunca se crearon (el script fiscal previo se perdio). Este
-- script los crea/activa de forma idempotente, derivado del eventCodes real de
-- cada SPA. Resuelve todo por KEY (unico por catalogo), no por id.
--
-- Post: invalidar cache (DELETE /api/security/user-details/cache o reiniciar
-- util-api) y que el usuario cierre/abra sesion.
-- ============================================================================
BEGIN;

DO $$
DECLARE
    v_admin_profile_key text := 'PER009';
    v_profile_id  int;
    v_app_id      int;
    v_evt_id      int;
    v_mp_id       int;
    r             record;
    v_mp_created  int := 0;
    v_mp_active   int := 0;
    v_pm_new      int := 0;
    v_pmp_new     int := 0;
BEGIN
    SELECT cd.id INTO v_profile_id
    FROM shared_catalogs.catalog_detail cd
    JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatPerfil'
    WHERE cd.key = v_admin_profile_key;
    IF v_profile_id IS NULL THEN
        RAISE EXCEPTION 'No existe el perfil admin key=%', v_admin_profile_key;
    END IF;

    FOR r IN
        SELECT app_key, evt_key FROM (VALUES
            ('APL001','EVT001'),('APL001','EVT002'),('APL001','EVT0008'),
            ('APL002','EVT001'),('APL002','EVT002'),('APL002','EVT003'),('APL002','EVT004'),('APL002','EVT0008'),
            ('APL003','EVT001'),('APL003','EVT002'),('APL003','EVT003'),('APL003','EVT004'),('APL003','EVT005'),('APL003','EVT006'),('APL003','EVT007'),('APL003','EVT0008'),
            ('APL004','EVT001'),('APL004','EVT002'),('APL004','EVT003'),('APL004','EVT009'),('APL004','EVT010'),('APL004','EVT011'),('APL004','EVT012'),('APL004','EVT0008'),
            ('APL005','EVT001'),('APL005','EVT002'),('APL005','EVT003'),('APL005','EVT007'),('APL005','EVT0008'),
            ('APL006','EVT001'),('APL006','EVT003'),('APL006','EVT013'),('APL006','EVT014'),('APL006','EVT015'),('APL006','EVT0008'),
            ('APL007','EVT001'),('APL007','EVT002'),('APL007','EVT009'),('APL007','EVT0008'),
            ('APL008','EVT001'),('APL008','EVT002'),('APL008','EVT003'),('APL008','EVT006'),('APL008','EVT016'),('APL008','EVT017'),('APL008','EVT018'),('APL008','EVT0008'),
            ('APL009','EVT001'),('APL009','EVT002'),('APL009','EVT003'),('APL009','EVT007'),('APL009','EVT010'),('APL009','EVT011'),('APL009','EVT012'),('APL009','EVT013'),('APL009','EVT0008'),
            ('APL010','EVT001'),('APL010','EVT002'),('APL010','EVT003'),('APL010','EVT005'),('APL010','EVT010'),('APL010','EVT011'),('APL010','EVT013'),('APL010','EVT016'),('APL010','EVT0008'),
            ('APL011','EVT001'),('APL011','EVT002'),('APL011','EVT003'),('APL011','EVT006'),('APL011','EVT010'),('APL011','EVT013'),('APL011','EVT016'),('APL011','EVT0008'),
            ('APL0017','EVT001'),('APL0017','EVT002'),('APL0017','EVT003'),('APL0017','EVT006'),('APL0017','EVT0008')
        ) AS m(app_key, evt_key)
    LOOP
        SELECT cd.id INTO v_app_id
        FROM shared_catalogs.catalog_detail cd
        JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatAplicativo'
        WHERE cd.key = r.app_key;

        SELECT cd.id INTO v_evt_id
        FROM shared_catalogs.catalog_detail cd
        JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatEvento'
        WHERE cd.key = r.evt_key;

        IF v_app_id IS NULL OR v_evt_id IS NULL THEN
            RAISE NOTICE 'Saltando %/%: aplicativo o evento inexistente', r.app_key, r.evt_key;
            CONTINUE;
        END IF;

        -- 1) module_process (app -> evento)
        SELECT module_process_id INTO v_mp_id
        FROM core_security.module_process
        WHERE catalog_detail_module_id = v_app_id AND catalog_detail_process_id = v_evt_id
        ORDER BY module_process_id
        LIMIT 1;

        IF v_mp_id IS NULL THEN
            INSERT INTO core_security.module_process
                (catalog_detail_module_id, catalog_detail_process_id, status, created_by, created_at)
            VALUES (v_app_id, v_evt_id, 1, 'system-rbac-wire', now())
            RETURNING module_process_id INTO v_mp_id;
            v_mp_created := v_mp_created + 1;
        ELSE
            UPDATE core_security.module_process
            SET status = 1, updated_by = 'system-rbac-wire', updated_at = now()
            WHERE module_process_id = v_mp_id AND status <> 1;
            IF FOUND THEN v_mp_active := v_mp_active + 1; END IF;
        END IF;

        -- 2) profile_module (admin -> app)
        IF EXISTS (SELECT 1 FROM core_security.profile_module
                   WHERE catalog_detail_profile_id = v_profile_id AND catalog_detail_module_id = v_app_id) THEN
            UPDATE core_security.profile_module
            SET status = 1, updated_by = 'system-rbac-wire', updated_at = now()
            WHERE catalog_detail_profile_id = v_profile_id AND catalog_detail_module_id = v_app_id AND status <> 1;
        ELSE
            INSERT INTO core_security.profile_module
                (catalog_detail_profile_id, catalog_detail_module_id, status, created_by, created_at)
            VALUES (v_profile_id, v_app_id, 1, 'system-rbac-wire', now());
            v_pm_new := v_pm_new + 1;
        END IF;

        -- 3) profile_module_process (admin -> module_process)
        IF EXISTS (SELECT 1 FROM core_security.profile_module_process
                   WHERE catalog_detail_profile_id = v_profile_id AND module_process_id = v_mp_id) THEN
            UPDATE core_security.profile_module_process
            SET status = 1, updated_by = 'system-rbac-wire', updated_at = now()
            WHERE catalog_detail_profile_id = v_profile_id AND module_process_id = v_mp_id AND status <> 1;
        ELSE
            INSERT INTO core_security.profile_module_process
                (catalog_detail_profile_id, module_process_id, status, created_by, created_at)
            VALUES (v_profile_id, v_mp_id, 1, 'system-rbac-wire', now());
            v_pmp_new := v_pmp_new + 1;
        END IF;
    END LOOP;

    RAISE NOTICE 'Wiring RBAC: module_process creados=%, activados=%, profile_module nuevos=%, profile_module_process nuevos=%',
        v_mp_created, v_mp_active, v_pm_new, v_pmp_new;
END $$;

COMMIT;
