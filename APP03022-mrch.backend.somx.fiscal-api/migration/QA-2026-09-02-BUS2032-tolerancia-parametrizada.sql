-- ============================================================================
-- BUS2032: parametriza el texto de la tolerancia (Ivan 2026-09-02).
-- Base: b2b_portal | Esquema: core_utils (cat_message manda sobre el enum).
--
-- Antes el texto tenía "$40.00 MXN" LITERAL, así que aunque el código leyera otro
-- valor, el mensaje seguía mostrando $40. Ahora usa el placeholder {0} y el código
-- inyecta la tolerancia REAL (última versión activa de "ToleranciaImporteRebate"),
-- formateada con maskMoney (ej. "$4.00").
--
-- OJO UAT: tras aplicar, REINICIAR util-api (cachea los mensajes).
-- ============================================================================

UPDATE core_utils.cat_message
SET description = 'El importe de la nota de crédito es inferior al valor del descuento comercial, considerando la tolerancia permitida de {0} MXN. Por favor, valide la información y, en caso necesario, realice las correcciones correspondientes.',
    updated_by = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE message_code = 'BUS2032';

-- Verificación
SELECT message_code, description FROM core_utils.cat_message WHERE message_code = 'BUS2032';
