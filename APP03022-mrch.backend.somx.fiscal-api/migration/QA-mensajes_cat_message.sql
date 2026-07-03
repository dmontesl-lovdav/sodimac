-- ============================================================
-- Mensajes fiscal-api al catálogo que consulta util-api: core_utils.cat_message
-- id_message_type: 1=BUS/ERR (negocio), 2=WRN (advertencia)
-- Idempotente (message_code es UNIQUE). Texto = enum FiscalMessageCode (fallback).
-- ============================================================
INSERT INTO core_utils.cat_message (message_code, id_message_type, description) VALUES
 ('WRN7030', 2, 'La factura se registró como Recibido Parcial: la diferencia entre el subtotal de la factura ({0}) y el importe de la recepción ({1}) supera la tolerancia permitida de {2}. Se requiere una nota de crédito para conciliar el monto y dar inicio al proceso de pago de la factura.'),
 ('WRN7031', 2, 'La factura se registró como Rechazo Comercial: el subtotal de la factura ({0}) es menor al importe de la recepción ({1}) y la diferencia supera la tolerancia permitida de {2}.'),
 ('WRN7032', 2, 'La factura se encuentra previamente registrada manualmente, Por favor, validar con el área de finanzas.'),
 ('WRN7033', 2, 'La factura se registró correctamente, pero el PDF no se pudo almacenar. El documento podría no estar disponible para descarga; intente cargarlo nuevamente o contacte a soporte.'),
 ('WRN7034', 2, 'La factura será rechazada y las notas de crédito serán canceladas, ya que el monto total de la factura menos las notas de crédito son menor al monto disponible de la recepción, ¿Desea continuar?'),
 ('BUS3103', 1, 'Las fechas de recepción son obligatorias cuando no se realiza la búsqueda por UUID.')
ON CONFLICT (message_code) DO NOTHING;
