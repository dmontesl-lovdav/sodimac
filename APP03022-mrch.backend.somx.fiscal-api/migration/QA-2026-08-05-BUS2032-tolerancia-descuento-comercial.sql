-- ============================================================================
-- BUS2032: rechazo por tolerancia del descuento comercial en NC (Ivan ago-2026)
-- Base: b2b_portal | Esquema: core_utils (cat_message manda sobre el enum)
--
-- Se lanza al registrar una NC de tipo 2 (Descuento Comercial) cuando el subtotal
-- de la NC queda por debajo del valor del descuento comercial (tenant_finance.rebate.amount)
-- más allá de la tolerancia PARAM-16 (ToleranciaImporteRebate = 40).
--
-- Idempotente: inserta si no existe, actualiza el texto si ya existe.
-- (id_message_type = 1 = error de negocio, igual que BUS045.)
-- ============================================================================

INSERT INTO core_utils.cat_message (message_code, id_message_type, description, created_by)
SELECT 'BUS2032', 1,
       'El importe de la nota de crédito es inferior al valor del descuento comercial, considerando la tolerancia permitida de $40.00 MXN. Por favor, valide la información y, en caso necesario, realice las correcciones correspondientes.',
       1
WHERE NOT EXISTS (SELECT 1 FROM core_utils.cat_message WHERE message_code = 'BUS2032');

UPDATE core_utils.cat_message
SET description = 'El importe de la nota de crédito es inferior al valor del descuento comercial, considerando la tolerancia permitida de $40.00 MXN. Por favor, valide la información y, en caso necesario, realice las correcciones correspondientes.',
    updated_by = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE message_code = 'BUS2032';

-- Verificación
SELECT message_code, description FROM core_utils.cat_message WHERE message_code = 'BUS2032';
