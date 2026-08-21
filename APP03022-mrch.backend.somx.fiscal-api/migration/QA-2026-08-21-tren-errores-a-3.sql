-- ============================================================================
-- Tren de Estatus Factura: estatus de error reintentan a 3 "En proceso de envío"
-- Ivan 2026-08-21 (Tren_Estatus_Portal_FBC_v1.0 (4).xlsx, hoja Transicion_Estatus)
-- Base: b2b_portal | Esquema: shared_catalogs | option_id = 1 (Factura)
--
-- Los estatus de error 16 (Error envio DMS), 17 (Error envio i213), 19 (Error envio
-- SAPITO), 20 (Error contabilizacion) y 22 (Error desglose contable) antes reintentaban
-- a destinos distintos (16->7, 17->8, 19->4, 20->4, 22->4). Ahora TODOS reintentan a
-- 3 (En proceso de envio). Es config de BD, se lee en vivo, NO requiere redeploy.
--
-- OJO (implicacion): el batch invoice-status-sync ejecuta 17->8 (InvoiceFlowStatus);
-- debe cambiar a 17->3 o el tren lo rechaza (WRN7011). Los errores 16/19/20/22 los
-- reintenta la integracion SAP/SAPITO -> tambien apuntar a 3. Coordinar con Robert.
-- ============================================================================

BEGIN;

-- 1) Agregar reintento a 3 (En proceso de envio)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 16, 3, 1),
(1, 17, 3, 1),
(1, 19, 3, 1),
(1, 20, 3, 1),
(1, 22, 3, 1)
ON CONFLICT ON CONSTRAINT uk_status_train DO NOTHING;

-- 2) Quitar los destinos viejos (7/8/4) para que quede igual al Excel v1.0 (4)
DELETE FROM shared_catalogs.status_train
WHERE option_id = 1
  AND (source_status, target_status) IN ((16,7),(17,8),(19,4),(20,4),(22,4));

-- Verificacion: deben quedar SOLO -> 3
SELECT source_status, target_status
FROM shared_catalogs.status_train
WHERE option_id = 1 AND source_status IN (16,17,19,20,22)
ORDER BY source_status, target_status;

COMMIT;
