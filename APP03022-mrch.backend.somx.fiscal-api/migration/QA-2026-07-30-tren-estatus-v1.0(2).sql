-- ============================================================================
-- Tren de Estatus v1.0 (2) - Ivan 2026-07-30
-- Base: b2b_portal | Esquema: shared_catalogs
-- Fuente: Tren_Estatus_Portal_FBC_v1.0 (2).xlsx, hoja "Tren_Estatus"
--
-- Contiene 3 partes. Ejecutar EN ORDEN (una transaccion):
--   PARTE 1 - Catalogo CatEstatusFactura: redefine estatus 16 + agrega 19,20,21,22.
--   PARTE 2 - status_train Factura (option_id=1): +transiciones nuevas, ajuste del 16.
--   PARTE 3 - status_train Recepcion (5) y Carta Porte (4).
--
-- Idempotente: PARTE 1 usa DO (skip si ya existe el value); PARTE 2/3 ON CONFLICT.
-- Alcance fiscal-api: NO afecta el flujo actual (el codigo solo maneja estatus 1/2/3;
--   el endpoint de cambio de estatus valida generico contra esta tabla).
-- Los estatus 19-22 los debe PRODUCIR el batch (fiscal-download / sync) - fuera de este pase.
-- ============================================================================

BEGIN;

-- ============================================================================
-- PARTE 1 - CatEstatusFactura (header_id = 15)
-- ============================================================================

-- 1.a) Redefinir estatus 16: "Estructura invalida" -> "Error envio DMS"
--      dict_id 16 -> se resuelve por el value; se actualizan las 3 lenguas (ES/EN/PT).
UPDATE shared_catalogs.dictionary_lang dl
SET description = CASE dl.lang_id
        WHEN 1 THEN 'Error envio DMS'
        WHEN 2 THEN 'DMS send error'
        WHEN 3 THEN 'Erro de envio DMS'
        ELSE dl.description END
FROM shared_catalogs.catalog_detail cd
WHERE cd.header_id = 15 AND cd.value = '16'
  AND dl.dict_id = cd.dict_id
  AND dl.lang_id IN (1,2,3);

-- 1.b) Agregar estatus 19,20,21,22 (dict_id nuevo = MAX+1, key EFA00xx)
DO $$
DECLARE
    r RECORD;
    v_dict INTEGER;
BEGIN
    FOR r IN
        SELECT * FROM (VALUES
            ('19','EFA0019','Error envio SAPITO',           'SAPITO send error',        'Erro de envio SAPITO'),
            ('20','EFA0020','Error en la contabilizacion',  'Accounting error',         'Erro na contabilizacao'),
            ('21','EFA0021','Pendiente movimiento contable','Pending accounting entry', 'Pendente lancamento contabil'),
            ('22','EFA0022','Error en el desglose contable','Accounting breakdown error','Erro no detalhamento contabil')
        ) AS t(val, k, es, en, pt)
    LOOP
        -- skip si ya existe ese value en el catalogo
        IF EXISTS (SELECT 1 FROM shared_catalogs.catalog_detail
                   WHERE header_id = 15 AND value = r.val) THEN
            CONTINUE;
        END IF;

        SELECT COALESCE(MAX(dict_id),0) + 1 INTO v_dict FROM shared_catalogs.dictionary_lang;

        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_dict, 1, r.es),
            (v_dict, 2, r.en),
            (v_dict, 3, r.pt);

        INSERT INTO shared_catalogs.catalog_detail (header_id, key, value, dict_id, sort_order, status, created_by)
        VALUES (15, r.k, r.val, v_dict, 0, 1, 1);
    END LOOP;
END $$;

-- 1.c) Ampliar el CHECK de tenant_fiscal.invoice.status a 1..22
--      (mismo gap que en v1.0: sin esto el UPDATE a 19-22 truena con SQLState 23514).
ALTER TABLE tenant_fiscal.invoice DROP CONSTRAINT IF EXISTS chk_invoice_status;
ALTER TABLE tenant_fiscal.invoice ADD CONSTRAINT chk_invoice_status
    CHECK (status = ANY (ARRAY[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22]));

-- ============================================================================
-- PARTE 2 - status_train FACTURA (option_id = 1)
-- ============================================================================

-- 2.a) Ajuste ligado a la redefinicion del 16 (Estructura invalida -> Error envio DMS):
--      quitar las transiciones viejas del 16 y su origen 5->16 (reemplazadas por 5->19 y 16->7).
DELETE FROM shared_catalogs.status_train
WHERE option_id = 1 AND (source_status, target_status) IN ((5,16),(16,5));

-- 2.b) Nuevas transiciones (incluye 5->19 y 16->7)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 3,  5,  1),
(1, 3,  21, 1),
(1, 3,  22, 1),
(1, 4,  21, 1),
(1, 4,  22, 1),
(1, 5,  19, 1),
(1, 6,  3,  1),
(1, 7,  16, 1),
(1, 8,  19, 1),
(1, 9,  11, 1),
(1, 9,  14, 1),
(1, 9,  20, 1),
(1, 10, 20, 1),
(1, 16, 7,  1),
(1, 19, 4,  1),
(1, 20, 4,  1),
(1, 21, 5,  1),
(1, 21, 22, 1),
(1, 22, 4,  1)
ON CONFLICT ON CONSTRAINT uk_status_train DO NOTHING;

-- ============================================================================
-- PARTE 3 - status_train RECEPCION (5) y CARTA PORTE (4)
-- ============================================================================

-- Recepcion: +1 (Cancelada -> Disponible)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(5, 7, 0, 1)
ON CONFLICT ON CONSTRAINT uk_status_train DO NOTHING;

-- Carta Porte (modulo nuevo, option_id = 4)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(4, 1,  2,  1),
(4, 1,  9,  1),
(4, 1,  10, 1),
(4, 2,  3,  1),
(4, 2,  9,  1),
(4, 3,  4,  1),
(4, 4,  5,  1),
(4, 4,  6,  1),
(4, 5,  7,  1),
(4, 6,  5,  1),
(4, 8,  1,  1),
(4, 8,  2,  1)
ON CONFLICT ON CONSTRAINT uk_status_train DO NOTHING;

-- ============================================================================
-- VERIFICACION (revisar antes de COMMIT)
-- ============================================================================
SELECT cd.value AS estatus, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = cd.dict_id AND dl.lang_id = 1
WHERE cd.header_id = 15 AND cd.value IN ('16','19','20','21','22')
ORDER BY (cd.value)::int;

SELECT option_id, count(*) AS transiciones
FROM shared_catalogs.status_train
GROUP BY option_id ORDER BY option_id;

COMMIT;

-- ============================================================================
-- PENDIENTE (NO incluido, requiere OK de Ivan):
--   - Factura 9->17 (el Excel ya no lo trae; 17 Error envio i213 sigue existiendo).
--   - Modulo PAGOS: numeracion del Excel inconsistente (self-loop 1->1). No seedear.
--   - Modulo DESCUENTO COMERCIAL: falta definir option_id.
--   - Modulo NC (option_id 2): sigue en BD; el Excel (2) ya no lo trae. Decidir si se retira.
-- ============================================================================
