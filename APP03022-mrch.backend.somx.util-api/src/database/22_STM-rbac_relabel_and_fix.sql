-- ============================================================================
-- RBAC definitivo: Pagos + dedup Parametros + relabel de eventos comunes
-- ============================================================================
-- Query A/B confirmaron:
--  1) Pagos (APL0017) quedo enlazado a eventos legacy EVT0024..EVT0028 en vez de
--     las keys canonicas que usa el frontend (EVT0008/EVT001/EVT002/EVT006/EVT003).
--  2) Config. de Parametros (APL012) tiene Buscar/Limpiar duplicados (EVT0008 vs
--     EVT0066 y EVT001 vs EVT0067).
--  3) Los "duplicados" de las apps gated (Guia, Three way match, Estado de cuenta,
--     facturas, NC, complementos, MIGO) son labels colisionados: EVT009..EVT018
--     se crearon para "Bloqueo de proveedores"/"Aplicativo Evento" con labels
--     Buscar/Limpiar/Inactivar/Exportar CSV/Excel, pero finanzas/fiscal las usan
--     como acciones comunes. hasEvent es por KEY, no por label, asi que relabelar
--     no afecta permisos; solo corrige lo que se ve.
--
-- NOTA: "Bloqueo de proveedores" (APL0018) y "Aplicativo Evento" (APL0020) NO
-- estan gateados en el frontend, por lo que sus labels no afectan funcionalidad.
-- Tras este relabel, en las pantallas de seguridad esas dos apps mostraran los
-- labels de accion comun. Si se requiere conservar sus labels propios, se hara
-- un paso posterior dandoles keys dedicadas.
--
-- Post: invalidar cache (DELETE /api/security/user-details/cache) y relogin.
-- ============================================================================
BEGIN;

DO $$
DECLARE
    v_profile_id int;
    v_app_id     int;
    v_evt_id     int;
    v_mp_id      int;
    r            record;
    v_pagos_wired int := 0;
    v_pagos_off   int := 0;
    v_param_off   int := 0;
    v_relabeled   int := 0;
BEGIN
    SELECT cd.id INTO v_profile_id
    FROM shared_catalogs.catalog_detail cd
    JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatPerfil'
    WHERE cd.key = 'PER009';
    IF v_profile_id IS NULL THEN RAISE EXCEPTION 'No existe el perfil PER009'; END IF;

    SELECT cd.id INTO v_app_id
    FROM shared_catalogs.catalog_detail cd
    JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatAplicativo'
    WHERE cd.key = 'APL0017';
    IF v_app_id IS NULL THEN RAISE EXCEPTION 'No existe el aplicativo APL0017 (Pagos)'; END IF;

    -- 1a) Pagos: desactivar enlaces no canonicos (EVT0024..EVT0028 y cualquier otro)
    UPDATE core_security.module_process mp
    SET status = 0, updated_by = 'system-rbac-pagos', updated_at = now()
    FROM shared_catalogs.catalog_detail ev
    WHERE mp.catalog_detail_module_id = v_app_id
      AND mp.status = 1
      AND ev.id = mp.catalog_detail_process_id
      AND ev.key NOT IN ('EVT0008','EVT001','EVT002','EVT006','EVT003');
    GET DIAGNOSTICS v_pagos_off = ROW_COUNT;

    -- 1b) Pagos: cablear las keys canonicas + asignar al perfil admin
    FOR r IN SELECT unnest(ARRAY['EVT0008','EVT001','EVT002','EVT006','EVT003']) AS evt_key
    LOOP
        SELECT cd.id INTO v_evt_id
        FROM shared_catalogs.catalog_detail cd
        JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatEvento'
        WHERE cd.key = r.evt_key;
        IF v_evt_id IS NULL THEN CONTINUE; END IF;

        SELECT module_process_id INTO v_mp_id
        FROM core_security.module_process
        WHERE catalog_detail_module_id = v_app_id AND catalog_detail_process_id = v_evt_id
        ORDER BY module_process_id LIMIT 1;

        IF v_mp_id IS NULL THEN
            INSERT INTO core_security.module_process
                (catalog_detail_module_id, catalog_detail_process_id, status, created_by, created_at)
            VALUES (v_app_id, v_evt_id, 1, 'system-rbac-pagos', now())
            RETURNING module_process_id INTO v_mp_id;
        ELSE
            UPDATE core_security.module_process
            SET status = 1, updated_by = 'system-rbac-pagos', updated_at = now()
            WHERE module_process_id = v_mp_id AND status <> 1;
        END IF;
        v_pagos_wired := v_pagos_wired + 1;

        IF NOT EXISTS (SELECT 1 FROM core_security.profile_module
                       WHERE catalog_detail_profile_id = v_profile_id AND catalog_detail_module_id = v_app_id) THEN
            INSERT INTO core_security.profile_module
                (catalog_detail_profile_id, catalog_detail_module_id, status, created_by, created_at)
            VALUES (v_profile_id, v_app_id, 1, 'system-rbac-pagos', now());
        ELSE
            UPDATE core_security.profile_module SET status = 1, updated_by = 'system-rbac-pagos', updated_at = now()
            WHERE catalog_detail_profile_id = v_profile_id AND catalog_detail_module_id = v_app_id AND status <> 1;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM core_security.profile_module_process
                       WHERE catalog_detail_profile_id = v_profile_id AND module_process_id = v_mp_id) THEN
            INSERT INTO core_security.profile_module_process
                (catalog_detail_profile_id, module_process_id, status, created_by, created_at)
            VALUES (v_profile_id, v_mp_id, 1, 'system-rbac-pagos', now());
        ELSE
            UPDATE core_security.profile_module_process SET status = 1, updated_by = 'system-rbac-pagos', updated_at = now()
            WHERE catalog_detail_profile_id = v_profile_id AND module_process_id = v_mp_id AND status <> 1;
        END IF;
    END LOOP;

    -- 2) Config. de Parametros (APL012): quitar Buscar/Limpiar duplicados legacy,
    --    dejando los canonicos EVT0008 / EVT001.
    UPDATE core_security.module_process mp
    SET status = 0, updated_by = 'system-rbac-dedup', updated_at = now()
    FROM shared_catalogs.catalog_detail app, shared_catalogs.catalog_detail ev
    WHERE app.key = 'APL012'
      AND mp.catalog_detail_module_id = app.id
      AND ev.id = mp.catalog_detail_process_id
      AND mp.status = 1
      AND ev.key IN ('EVT0066','EVT0067');
    GET DIAGNOSTICS v_param_off = ROW_COUNT;

    -- 3) Relabel de EVT009..EVT018 a su significado comun (por dict_id y value).
    FOR r IN
        SELECT evt_key, label FROM (VALUES
            ('EVT009','Exportar Excel'),
            ('EVT010','Descargar XML'),
            ('EVT011','Cancelar'),
            ('EVT012','Actualizar estatus'),
            ('EVT013','Descargar PDF'),
            ('EVT014','Confirmar revisión'),
            ('EVT015','Solicitar revisión'),
            ('EVT016','Publicar'),
            ('EVT017','Autorizar'),
            ('EVT018','Rechazar')
        ) AS m(evt_key, label)
    LOOP
        SELECT cd.id, cd.dict_id INTO v_evt_id, v_mp_id
        FROM shared_catalogs.catalog_detail cd
        JOIN shared_catalogs.catalog_header h ON h.id = cd.header_id AND h.code = 'CatEvento'
        WHERE cd.key = r.evt_key;
        IF v_evt_id IS NULL THEN CONTINUE; END IF;

        UPDATE shared_catalogs.catalog_detail SET value = r.label WHERE id = v_evt_id;
        IF v_mp_id IS NOT NULL AND v_mp_id > 0 THEN
            UPDATE shared_catalogs.dictionary_lang SET description = r.label WHERE dict_id = v_mp_id;
        END IF;
        v_relabeled := v_relabeled + 1;
    END LOOP;

    RAISE NOTICE 'Pagos: enlaces off=%, canonicos wired=% | Parametros dedup off=% | eventos relabelados=%',
        v_pagos_off, v_pagos_wired, v_param_off, v_relabeled;
END $$;

COMMIT;
