-- ============================================================================
-- Relacion CatAplicativo -> CatModulo (para la cascada Modulo/Aplicativo en
-- la pantalla "Bitacora de Actividades").
-- ============================================================================
-- Diagnostico mostro que solo APL0017 (Pagos) tenia parent_element_id -> MOD002.
-- Este script asigna a cada aplicativo su modulo via parent_element_id,
-- resolviendo el id del catalog_detail del modulo por su key (MOD00x).
-- Idempotente: solo actualiza los que difieran.
--
-- Mapeo (ajustar aqui si algun aplicativo va en otro modulo):
--   MOD001 Catalogos        : APL001, APL002, APL0018
--   MOD002 Finanzas         : APL003, APL004, APL005, APL006, APL007, APL008, APL0017
--   MOD003 Fiscal           : APL009, APL010, APL011
--   MOD004 Utilerias        : APL012
--   MOD005 Auditoria        : APL013
--   MOD006 Gestion de acceso: APL014, APL015, APL016, APL0019, APL0020, APL0021, APL0022, APL0023
-- ============================================================================
BEGIN;

DO $$
DECLARE
    v_app_header int;
    v_mod_header int;
    r record;
    v_app_id int;
    v_mod_id int;
    v_updated int := 0;
BEGIN
    SELECT id INTO v_app_header FROM shared_catalogs.catalog_header WHERE code = 'CatAplicativo';
    SELECT id INTO v_mod_header FROM shared_catalogs.catalog_header WHERE code = 'CatModulo';
    IF v_app_header IS NULL OR v_mod_header IS NULL THEN
        RAISE EXCEPTION 'No se encontro CatAplicativo o CatModulo';
    END IF;

    FOR r IN
        SELECT app_key, mod_key FROM (VALUES
            ('APL001','MOD001'),('APL002','MOD001'),('APL0018','MOD001'),
            ('APL003','MOD002'),('APL004','MOD002'),('APL005','MOD002'),
            ('APL006','MOD002'),('APL007','MOD002'),('APL008','MOD002'),('APL0017','MOD002'),
            ('APL009','MOD003'),('APL010','MOD003'),('APL011','MOD003'),
            ('APL012','MOD004'),
            ('APL013','MOD005'),
            ('APL014','MOD006'),('APL015','MOD006'),('APL016','MOD006'),
            ('APL0019','MOD006'),('APL0020','MOD006'),('APL0021','MOD006'),
            ('APL0022','MOD006'),('APL0023','MOD006')
        ) AS m(app_key, mod_key)
    LOOP
        SELECT id INTO v_app_id FROM shared_catalogs.catalog_detail
            WHERE header_id = v_app_header AND key = r.app_key;
        SELECT id INTO v_mod_id FROM shared_catalogs.catalog_detail
            WHERE header_id = v_mod_header AND key = r.mod_key;

        IF v_app_id IS NULL OR v_mod_id IS NULL THEN
            RAISE NOTICE 'Saltando %/%: no existe aplicativo o modulo', r.app_key, r.mod_key;
            CONTINUE;
        END IF;

        UPDATE shared_catalogs.catalog_detail
        SET parent_catalog_id = v_mod_header,
            parent_element_id = v_mod_id,
            updated_by = 'system-cat-modulo-link',
            updated_at = now()
        WHERE id = v_app_id
          AND (parent_element_id IS DISTINCT FROM v_mod_id OR parent_catalog_id IS DISTINCT FROM v_mod_header);
        IF FOUND THEN v_updated := v_updated + 1; END IF;
    END LOOP;

    RAISE NOTICE 'CatAplicativo->CatModulo: aplicativos actualizados=%', v_updated;
END $$;

COMMIT;
