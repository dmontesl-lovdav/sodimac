-- ============================================================================
-- STM-1166: Tren de Estatus - Datos Iniciales
-- Descripción: Transiciones de estatus migradas desde enums de fiscal-api
--              InvoiceStatus.java y CreditNoteStatus.java
-- ============================================================================

-- ============================================================================
-- Facturas (option_id = 1) - Desde InvoiceStatus.java
-- Tipo de documento: I (Factura)
-- ============================================================================

-- PENDIENTE_ADDENDA (1) -> RECIBIDO_PARCIAL (2), PENDIENTE_CONTABILIZAR (3), PAGO_MANUAL (13)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 1, 2, 1),
(1, 1, 3, 1),
(1, 1, 13, 1);

-- RECIBIDO_PARCIAL (2) -> PENDIENTE_CONTABILIZAR (3), PAGO_MANUAL (13)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 2, 3, 1),
(1, 2, 13, 1);

-- PENDIENTE_CONTABILIZAR (3) -> PROCESO_DESCARGA (4)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 3, 4, 1);

-- PROCESO_DESCARGA (4) -> DESGLOSE_FACTURA (5), RECHAZO_CONTABLE (11)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 4, 5, 1),
(1, 4, 11, 1);

-- DESGLOSE_FACTURA (5) -> PENDIENTE_ENVIO_CONTABILIZAR (6), RECHAZO_CONTABLE (11)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 5, 6, 1),
(1, 5, 11, 1);

-- PENDIENTE_ENVIO_CONTABILIZAR (6) -> PENDIENTE_PAGO (7), RECHAZO_CONTABLE (11)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 6, 7, 1),
(1, 6, 11, 1);

-- PENDIENTE_PAGO (7) -> PAGADO (8)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 7, 8, 1);

-- PAGADO (8) -> PENDIENTE_COMPLEMENTO (9)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 8, 9, 1);

-- PENDIENTE_COMPLEMENTO (9) -> COMPLETADO (10)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 9, 10, 1);

-- RECHAZO_CONTABLE (11) -> PENDIENTE_PAGO (7)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(1, 11, 7, 1);


-- ============================================================================
-- Notas de Crédito (option_id = 2) - Desde CreditNoteStatus.java
-- Tipo de documento: E (Nota de Crédito)
-- ============================================================================

-- PENDIENTE_ADDENDA (1) -> RECIBIDO_PARCIAL (2), PENDIENTE_CONTABILIZAR (3)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 1, 2, 1),
(2, 1, 3, 1);

-- RECIBIDO_PARCIAL (2) -> PENDIENTE_CONTABILIZAR (3)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 2, 3, 1);

-- PENDIENTE_CONTABILIZAR (3) -> PROCESO_DESCARGA (4)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 3, 4, 1);

-- PROCESO_DESCARGA (4) -> DESGLOSE_NC (5), RECHAZO_CONTABLE (11)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 4, 5, 1),
(2, 4, 11, 1);

-- DESGLOSE_NC (5) -> PENDIENTE_ENVIO_CONTABILIZAR (6), RECHAZO_CONTABLE (11)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 5, 6, 1),
(2, 5, 11, 1);

-- PENDIENTE_ENVIO_CONTABILIZAR (6) -> APLICADO (7), RECHAZO_CONTABLE (11)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 6, 7, 1),
(2, 6, 11, 1);

-- APLICADO (7) -> COMPLETADO (8)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 7, 8, 1);

-- RECHAZO_CONTABLE (11) -> APLICADO (7)
INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
(2, 11, 7, 1);


-- ============================================================================
-- Complementos de Pago (option_id = 3) - Pendiente definir
-- Tipo de documento: P (Complemento de Pago)
-- NOTA: Actualmente PaymentsEntity.java no tiene un enum de estatus definido.
--       Se usa un campo status INTEGER con valor por defecto 1.
--       Las transiciones serán agregadas cuando se definan los estatus.
-- ============================================================================


-- ============================================================================
-- Factura con CartaPorte (option_id = 4) - Pendiente definir
-- Tipo de documento: T (CartaPorte)
-- NOTA: Evaluar si aplican los mismos estatus que Factura (option_id = 1)
--       o si requiere un flujo diferente.
-- ============================================================================


-- ============================================================================
-- FIN DEL SCRIPT DE DATOS
-- ============================================================================
