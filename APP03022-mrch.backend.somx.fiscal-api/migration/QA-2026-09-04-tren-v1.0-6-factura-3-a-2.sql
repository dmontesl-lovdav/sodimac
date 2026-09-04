-- =====================================================================
-- Tren de Estatus v1.0(6) - Ivan 2026-09-04
-- Agrega la transicion de FACTURA: 3 (En proceso de envio) -> 2 (Recibido Parcial)
--
-- Contexto: al cancelar una NC, si a la factura relacionada no le quedan NCs
-- activas, la factura regresa a 2 (Recibido Parcial) para que no viaje. El
-- backend hace ese cambio directo (setStatus), pero la transicion se agrega al
-- tren para dejarlo consistente y valido si algun flujo la mueve por el
-- endpoint validado (PUT /invoices/{uuid}/status).
--
-- option_id 1 = Factura. Idempotente (ON CONFLICT no re-inserta).
-- created_by = 1 (mismo criterio que el resto del seed del tren).
-- =====================================================================

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by)
VALUES (1, 3, 2, 1)
ON CONFLICT (option_id, source_status, target_status) DO NOTHING;

-- Verificacion (esperado: 3->{2,4,5,6,20})
-- SELECT option_id, source_status, target_status
-- FROM shared_catalogs.status_train
-- WHERE option_id = 1 AND source_status = 3
-- ORDER BY target_status;

-- NOTA para Ivan: el Excel v1.0(6) NO lista la transicion Factura 17->19 ni
-- NC 17->19 (complemento cierra en Completado), que SI existen en el tren
-- desde v1.0(5) y estan validadas en UAT. NO se eliminan aqui. Confirmar si es
-- omision del Excel (lo mas probable) o cambio intencional antes de tocarlas.
