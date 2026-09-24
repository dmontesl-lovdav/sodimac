-- Tren de Estatus Factura — agrega transición 14 (Rechazo Contable) -> 3 (En proceso de envío)
-- Solicitado por Ivan 2026-08-21 (Tren_Estatus v1.0(4), fila id 30).
-- Es transición ADICIONAL: 14->8 (id 63) sigue existiendo.
-- status_train se lee en vivo (sin redeploy). created_by es bigint (NO uuid).
-- Idempotente por (option_id, source_status, target_status).

INSERT INTO shared_catalogs.status_train (id, option_id, source_status, target_status, created_by, created_at)
SELECT
    COALESCE((SELECT MAX(id) FROM shared_catalogs.status_train), 0) + 1,
    1, 14, 3, 1, now()
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.status_train
    WHERE option_id = 1 AND source_status = 14 AND target_status = 3
);

-- Verificación
SELECT id, source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 1 AND source_status = 14
ORDER BY id;
