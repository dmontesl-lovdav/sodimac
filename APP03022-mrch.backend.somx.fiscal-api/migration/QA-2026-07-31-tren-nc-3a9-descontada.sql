-- ============================================================================
-- Tren de Estatus NC - transicion 3 -> 9 (Descontada) para Descuento Comercial
-- Robert / Ivan 2026-07-31
-- Base: b2b_portal | Esquema: shared_catalogs | option_id = 2 (Nota de Credito)
--
-- Contexto: el batch CreditNoteDownloadBatchService, para NC con TipoNC=2
-- (Descuento Comercial), NO descarga a SODIMAC_SAP_DEV y mueve el estatus
-- 3 (En proceso de envio) -> 9 (Descontada). El tren NC no tenia esa transicion
-- -> el servicio de estatus devolvia WRN7011 "Transicion 3 -> 9 no permitida".
--
-- Catalogo CatEstatusNotaCredito ya actualizado (Ivan): 9 = "Descontada",
-- 11 = Cancelada, 12 = Borrada. Solo faltaba la transicion en el tren.
-- Es config de BD; se lee en vivo, NO requiere redeploy.
-- ============================================================================

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (2, 3, 9, 1)
ON CONFLICT ON CONSTRAINT uk_status_train DO NOTHING;

-- Verificacion: transiciones NC desde el estatus 3
SELECT source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 2 AND source_status = 3
ORDER BY target_status;
