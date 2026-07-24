-- ============================================================
-- BUS045: mensaje claro cuando la NC trae un TipoRelacion NO permitido (retro Ivan 2026-07-24)
-- Base: b2b_portal | Esquema: core_utils (tabla que lee util-api; manda sobre el enum)
-- ============================================================
--
-- Antes: "El tipo de relación no es válido. Para Notas de Crédito debe ser 01" (hardcodeaba 01).
-- Ahora (por catálogo CatTipoRelacionFacturaNC): texto neutro que remite a Finanzas.
--
-- Se usa en fiscal-api InvoiceServiceImpl.saveRelatedCfdis cuando la NC SÍ trae CfdiRelacionados
-- pero ninguno con TipoRelacion permitido (distinto de BUS042 = no trae ningún relacionado).

UPDATE core_utils.cat_message
SET description = 'El tipo de relación de la Nota de Crédito no se encuentra permitido. Por favor, validar con el área financiera la relación permitida.',
    updated_by = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE message_code = 'BUS045';

-- Verificación
SELECT message_code, description
FROM core_utils.cat_message
WHERE message_code = 'BUS045';
