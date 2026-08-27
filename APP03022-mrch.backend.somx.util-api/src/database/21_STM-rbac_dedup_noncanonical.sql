-- ============================================================================
-- Dedup permanente RBAC: deja solo las event keys canonicas por aplicativo
-- ============================================================================
-- Los duplicados ("Buscar", "Limpiar", "Exportar CSV" repetidos) vienen de
-- module_process que enlazan un aplicativo con eventos LEGACY por-app que tienen
-- el mismo label que el evento comun canonico. El frontend solo usa las keys
-- canonicas (EVT001..EVT018, EVT0008); los enlaces legacy son peso muerto que
-- solo ensucia las pantallas de seguridad.
--
-- Este script desactiva, SOLO para los aplicativos mapeados (los que tienen
-- eventCodes en el frontend), los module_process cuyo evento NO esta en la lista
-- canonica de ese aplicativo. No toca aplicativos sin mapeo (APL012..APL016).
-- Correr DESPUES de 20_STM-rbac_wire_app_events.sql para no dejar apps sin sus
-- enlaces canonicos.
-- ============================================================================
BEGIN;

DO $$
DECLARE
    v_deactivated int;
BEGIN
    WITH canonical(app_key, evt_key) AS (
        VALUES
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
    ),
    to_deactivate AS (
        SELECT mp.module_process_id
        FROM core_security.module_process mp
        JOIN shared_catalogs.catalog_detail app ON app.id = mp.catalog_detail_module_id
        JOIN shared_catalogs.catalog_header hApp ON hApp.id = app.header_id AND hApp.code = 'CatAplicativo'
        JOIN shared_catalogs.catalog_detail ev ON ev.id = mp.catalog_detail_process_id
        JOIN shared_catalogs.catalog_header hEv ON hEv.id = ev.header_id AND hEv.code = 'CatEvento'
        WHERE mp.status = 1
          AND app.key IN (SELECT DISTINCT app_key FROM canonical)
          AND NOT EXISTS (
              SELECT 1 FROM canonical c
              WHERE c.app_key = app.key AND c.evt_key = ev.key
          )
    )
    UPDATE core_security.module_process mp
    SET status = 0, updated_by = 'system-rbac-dedup', updated_at = now()
    FROM to_deactivate d
    WHERE mp.module_process_id = d.module_process_id;
    GET DIAGNOSTICS v_deactivated = ROW_COUNT;

    RAISE NOTICE 'Dedup RBAC: module_process no-canonicos desactivados=%', v_deactivated;
END $$;

COMMIT;
