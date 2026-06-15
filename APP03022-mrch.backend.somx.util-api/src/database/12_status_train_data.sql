-- ============================================================================
-- STM-1166: Tren de Estatus - Datos Iniciales
-- Descripción: Transiciones de estatus migradas desde enums de fiscal-api
--              InvoiceStatus.java y CreditNoteStatus.java
-- ============================================================================

-- ============================================================================
-- Facturas (option_id = 1) - Tren de Estatus v1.0 (Ivan, 2026-06-02)
-- Tipo de documento: I (Factura)
-- Fuente: Tren_Estatus_Portal_FBC_v1.0.xlsx + docs/analisis/TREN-ESTATUS-v1.0-vs-codigo.md
-- Renumeración completa 1-18, sin Pendiente Addenda. Cancelar = Rechazo Comercial (1),
-- permitido solo desde Recibido Parcial (2). Alineado con InvoiceStatus.java.
-- ============================================================================

INSERT INTO shared_catalogs.status_train (option_id, source_status, target_status, created_by) VALUES
-- Recibido Parcial (2) -> Rechazo Comercial (1), En proceso de envío (3), Pago Manual (18)
(1, 2,  1,  1),
(1, 2,  3,  1),
(1, 2,  18, 1),
-- En proceso de envío (3) -> En proceso de descarga (4)
(1, 3,  4,  1),
-- En proceso de descarga (4) -> Desglose de factura (5)
(1, 4,  5,  1),
-- Desglose de factura (5) -> Pendiente registro SAPITO (7), Estructura inválida (16)
(1, 5,  7,  1),
(1, 5,  16, 1),
-- Pendiente registro SAPITO (7) -> Pendiente envío i213 (8)
(1, 7,  8,  1),
-- Pendiente envío i213 (8) -> Factura enviada i213 (9)
(1, 8,  9,  1),
-- Factura enviada i213 (9) -> Pendiente de contabilizar (10), Error envío i213 (17)
(1, 9,  10, 1),
(1, 9,  17, 1),
-- Pendiente de contabilizar (10) -> Pendiente de Pago (11), Rechazo Contable (14)
(1, 10, 11, 1),
(1, 10, 14, 1),
-- Pendiente de Pago (11) -> Pendiente de complemento (12)
(1, 11, 12, 1),
-- Pendiente de complemento (12) -> Completado (13)
(1, 12, 13, 1),
-- Rechazo Contable (14) -> Pendiente envío i213 (8)
(1, 14, 8,  1),
-- Estructura inválida (16) -> Desglose de factura (5)
(1, 16, 5,  1),
-- Error envío i213 (17) -> Pendiente envío i213 (8)
(1, 17, 8,  1);


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
