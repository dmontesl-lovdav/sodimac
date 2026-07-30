-- ============================================================================
-- Tren de Estatus v1.0 (2) - transicion faltante 4 -> 6 (Ivan 2026-07-30)
-- Base: b2b_portal | Esquema: shared_catalogs
--
-- Ivan la olvido en el Excel. Sin ella el portal da WRN7011 al pasar de
-- "En proceso de desglose" (4) a "Error en el desglose de la factura" (6).
-- Es config del tren; se lee de BD, no requiere redeploy (el enum es solo fallback).
-- ============================================================================

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (1, 4, 6, 1)
ON CONFLICT ON CONSTRAINT uk_status_train DO NOTHING;

-- Verificacion: transiciones desde el estatus 4 (Factura)
SELECT source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 1 AND source_status = 4
ORDER BY target_status;
