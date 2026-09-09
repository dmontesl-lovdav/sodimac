-- =====================================================================
-- Tren de Estatus v1.0(8) - Ivan 2026-09-09
-- Factura: Pago Manual (18) NO debe tener transicion (queda terminal).
--   -> QUITAR Factura 18 -> 19.
-- Asegura (idempotente) las otras dos que Ivan valido:
--   15 -> 17 (agregada en v1.0(7)) y 17 -> 19 (desde v1.0(5)).
--
-- option_id 1 = Factura. La factura pagada manualmente cierra a 19 por la
-- conversion Complemento Pago -> Factura 19 (no por transicion 18->19).
--
-- NOTA: NC (option 2) NO se toca aqui. El Excel v1.0(8) mantiene NC 18->19 y
-- NO lista NC 17->19, pero 17->19 SI existe en el tren NC desde v1.0(5) y es
-- necesaria para la NC de descuento comercial (nace 17, cierra 19). Pendiente
-- confirmar con Ivan si NC tambien pierde 18->19 / si mantiene 17->19.
-- =====================================================================

-- 1. Quitar Factura 18 -> 19 (Pago Manual terminal)
DELETE FROM shared_catalogs.status_train
WHERE option_id = 1 AND source_status = 18 AND target_status = 19;

-- 2. Asegurar 15->17 y 17->19 (idempotente; setval evita 23505)
SELECT setval('shared_catalogs.status_train_id_seq', (SELECT MAX(id) FROM shared_catalogs.status_train));

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (1, 15, 17, 1)
ON CONFLICT (option_id, source_status, target_status) DO NOTHING;

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (1, 17, 19, 1)
ON CONFLICT (option_id, source_status, target_status) DO NOTHING;

-- 3. Validacion (esperado: 15->17, 15->18, 17->19 ; SIN 18->19)
-- SELECT source_status, target_status FROM shared_catalogs.status_train
-- WHERE option_id = 1 AND source_status IN (15,17,18) ORDER BY source_status, target_status;
