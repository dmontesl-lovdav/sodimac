-- ============================================================
-- Máscara de valor económico en mensajes de tolerancia (QA jul-2026, Fer)
-- Base: b2b_portal | Esquema: core_utils (tabla que lee util-api)
-- ============================================================
--
-- WRN7030 y WRN7031 NO cambian de texto: ya tienen los placeholders {0}{1}{2};
-- la máscara ($#,##0.00) se aplica del lado del back en el VALOR que se envía.
--
-- Solo WRN7034 cambia: pasa de texto estático a mostrar los montos ({0} neto
-- factura-NCs, {1} monto disponible de la recepción), con máscara.

UPDATE core_utils.cat_message
SET description = 'La factura será rechazada y las notas de crédito serán canceladas: el monto de la factura menos las notas de crédito ({0}) es menor al monto disponible de la recepción ({1}). ¿Desea continuar?',
    updated_by = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE message_code = 'WRN7034';

-- Verificación
SELECT message_code, description
FROM core_utils.cat_message
WHERE message_code = 'WRN7034';
