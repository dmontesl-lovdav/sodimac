-- =====================================================================
-- Tren de Estatus v1.0(7) - Ivan 2026-09-08
-- Agrega la transicion de FACTURA: 15 (Pendiente de Pago) -> 17 (Pendiente de complemento)
--
-- Contexto: el paso 5 de la HU (proceso de pago) mueve la factura a 17 cuando se
-- registra el detalle de pago. En el tren NO existia ninguna entrada a 17 para
-- Factura -> el PUT /invoices/{uuid}/status daba WRN7011. Se agrega 15->17 para
-- habilitar ese cambio por el WS validado.
--
-- Al pasar a 17, fiscal-api ejecuta la cascada (recepcion -> 6 Pagada; guia -> 7
-- Pagada si es transporte), gemela de la cascada de cancelacion.
--
-- option_id 1 = Factura. Idempotente. setval por si la secuencia esta atras del
-- MAX(id) (restore/seeds) -> evita SQLState 23505 en el INSERT.
-- =====================================================================

SELECT setval('shared_catalogs.status_train_id_seq', (SELECT MAX(id) FROM shared_catalogs.status_train));

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (1, 15, 17, 1)
ON CONFLICT (option_id, source_status, target_status) DO NOTHING;

-- Verificacion (esperado incluir 15->17):
-- SELECT option_id, source_status, target_status
-- FROM shared_catalogs.status_train
-- WHERE option_id = 1 AND target_status = 17;
