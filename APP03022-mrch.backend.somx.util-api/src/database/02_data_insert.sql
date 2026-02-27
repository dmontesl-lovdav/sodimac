-- =====================================================
-- UTILS-API: Script de Carga Inicial de Datos
-- Modulo: Herramientas y Utilerias
-- Base de datos: PostgreSQL
-- Esquema: core_utils
-- Fecha: 2025-12-15
-- =====================================================

SET search_path TO core_utils;

-- =====================================================
-- 1. CATALOGO DE MODULOS (cat_module)
-- =====================================================
INSERT INTO core_utils.cat_module (name, description, created_by, created_at) VALUES
('FISCAL', 'Modulo de facturacion electronica y timbrado', 1, CURRENT_TIMESTAMP),
('FINANZAS', 'Modulo de gestion financiera y contable', 1, CURRENT_TIMESTAMP),
('ACLARACIONES', 'Modulo de gestion de aclaraciones y disputas', 1, CURRENT_TIMESTAMP),
('CATALOGOS', 'Modulo de administracion de catalogos', 1, CURRENT_TIMESTAMP),
('SEGURIDAD', 'Modulo de autenticacion y autorizacion', 1, CURRENT_TIMESTAMP),
('REPORTES', 'Modulo de generacion de reportes', 1, CURRENT_TIMESTAMP),
('NOTIFICACIONES', 'Modulo de envio de notificaciones', 1, CURRENT_TIMESTAMP),
('INTEGRACIONES', 'Modulo de integracion con sistemas externos', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. CATALOGO DE TIPOS DE ELEMENTO (cat_item_type)
-- =====================================================
INSERT INTO core_utils.cat_item_type (name, description, created_by, created_at) VALUES
('DOCUMENTO', 'Tipo de elemento documento', 1, CURRENT_TIMESTAMP),
('CONFIGURACION', 'Tipo de elemento configuracion', 1, CURRENT_TIMESTAMP),
('PROCESO', 'Tipo de elemento proceso', 1, CURRENT_TIMESTAMP),
('SERVICIO', 'Tipo de elemento servicio', 1, CURRENT_TIMESTAMP),
('REPORTE', 'Tipo de elemento reporte', 1, CURRENT_TIMESTAMP),
('NOTIFICACION', 'Tipo de elemento notificacion', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 3. CATALOGO DE ELEMENTOS (cat_item)
-- =====================================================
INSERT INTO core_utils.cat_item (name, description, created_by, created_at) VALUES
('FACTURA', 'Elemento factura electronica', 1, CURRENT_TIMESTAMP),
('NOTA_CREDITO', 'Elemento nota de credito', 1, CURRENT_TIMESTAMP),
('NOTA_DEBITO', 'Elemento nota de debito', 1, CURRENT_TIMESTAMP),
('COMPLEMENTO_PAGO', 'Elemento complemento de pago', 1, CURRENT_TIMESTAMP),
('RECIBO_NOMINA', 'Elemento recibo de nomina', 1, CURRENT_TIMESTAMP),
('CARTA_PORTE', 'Elemento carta porte', 1, CURRENT_TIMESTAMP),
('RETENCION', 'Elemento retencion', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 4. CATALOGO DE PROCESOS (cat_process)
-- =====================================================
INSERT INTO core_utils.cat_process (name, description, created_by, created_at) VALUES
('TIMBRADO', 'Proceso de timbrado de CFDI', 1, CURRENT_TIMESTAMP),
('CANCELACION', 'Proceso de cancelacion de CFDI', 1, CURRENT_TIMESTAMP),
('VALIDACION', 'Proceso de validacion de documentos', 1, CURRENT_TIMESTAMP),
('ENVIO_SAT', 'Proceso de envio al SAT', 1, CURRENT_TIMESTAMP),
('GENERACION_PDF', 'Proceso de generacion de PDF', 1, CURRENT_TIMESTAMP),
('GENERACION_XML', 'Proceso de generacion de XML', 1, CURRENT_TIMESTAMP),
('NOTIFICACION_EMAIL', 'Proceso de notificacion por correo', 1, CURRENT_TIMESTAMP),
('SINCRONIZACION', 'Proceso de sincronizacion de datos', 1, CURRENT_TIMESTAMP),
('RESPALDO', 'Proceso de respaldo de informacion', 1, CURRENT_TIMESTAMP),
('DEPURACION', 'Proceso de depuracion de registros', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 5. CATALOGO DE MENSAJES (cat_message)
-- Tipos: 1=Error, 2=Warning, 3=Info, 4=Success
-- =====================================================
INSERT INTO core_utils.cat_message (message_code, id_message_type, description, created_by, created_at) VALUES
-- Errores (tipo 1)
('ERR001', 1, 'Error de conexion con el servicio de timbrado', 1, CURRENT_TIMESTAMP),
('ERR002', 1, 'Error al validar estructura del XML', 1, CURRENT_TIMESTAMP),
('ERR003', 1, 'Error de autenticacion: credenciales invalidas', 1, CURRENT_TIMESTAMP),
('ERR004', 1, 'Error al procesar la solicitud: datos incompletos', 1, CURRENT_TIMESTAMP),
('ERR005', 1, 'Error de timeout: el servicio no responde', 1, CURRENT_TIMESTAMP),
('ERR006', 1, 'Error al guardar en base de datos', 1, CURRENT_TIMESTAMP),
('ERR007', 1, 'Error: RFC no valido', 1, CURRENT_TIMESTAMP),
('ERR008', 1, 'Error: Certificado expirado', 1, CURRENT_TIMESTAMP),
('ERR009', 1, 'Error: Sello digital invalido', 1, CURRENT_TIMESTAMP),
('ERR010', 1, 'Error: Folio fiscal duplicado', 1, CURRENT_TIMESTAMP),
-- Advertencias (tipo 2)
('WRN001', 2, 'Advertencia: El certificado expira en menos de 30 dias', 1, CURRENT_TIMESTAMP),
('WRN002', 2, 'Advertencia: Limite de folios cercano al maximo', 1, CURRENT_TIMESTAMP),
('WRN003', 2, 'Advertencia: Proceso demorado, reintentando', 1, CURRENT_TIMESTAMP),
('WRN004', 2, 'Advertencia: Datos opcionales no proporcionados', 1, CURRENT_TIMESTAMP),
('WRN005', 2, 'Advertencia: Version de CFDI proxima a deprecar', 1, CURRENT_TIMESTAMP),
-- Informativos (tipo 3)
('INF001', 3, 'Proceso iniciado correctamente', 1, CURRENT_TIMESTAMP),
('INF002', 3, 'Validacion en progreso', 1, CURRENT_TIMESTAMP),
('INF003', 3, 'Documento en cola de procesamiento', 1, CURRENT_TIMESTAMP),
('INF004', 3, 'Sincronizacion programada', 1, CURRENT_TIMESTAMP),
('INF005', 3, 'Mantenimiento programado proximo', 1, CURRENT_TIMESTAMP),
-- Exito (tipo 4)
('SUC001', 4, 'Documento timbrado exitosamente', 1, CURRENT_TIMESTAMP),
('SUC002', 4, 'Cancelacion procesada correctamente', 1, CURRENT_TIMESTAMP),
('SUC003', 4, 'Validacion completada sin errores', 1, CURRENT_TIMESTAMP),
('SUC004', 4, 'PDF generado correctamente', 1, CURRENT_TIMESTAMP),
('SUC005', 4, 'Notificacion enviada exitosamente', 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 6. MENSAJES POR APLICATIVO (application_msg)
-- Aplicativos: 1=Portal Web, 2=API REST, 3=Batch, 4=Mobile
-- =====================================================
INSERT INTO core_utils.application_msg (id_message, id_application, created_by, created_at)
SELECT id_message, 1, 1, CURRENT_TIMESTAMP FROM core_utils.cat_message;

INSERT INTO core_utils.application_msg (id_message, id_application, created_by, created_at)
SELECT id_message, 2, 1, CURRENT_TIMESTAMP FROM core_utils.cat_message;

INSERT INTO core_utils.application_msg (id_message, id_application, created_by, created_at)
SELECT id_message, 3, 1, CURRENT_TIMESTAMP FROM core_utils.cat_message
WHERE message_code LIKE 'ERR%' OR message_code LIKE 'SUC%';

-- =====================================================
-- 7. CATALOGO DE PARAMETROS (cat_parameter)
-- =====================================================
INSERT INTO core_utils.cat_parameter (id_module, id_type, name, description, value, version, start_date, status, created_by, created_at) VALUES
-- FISCAL (id_module=1)
(1, 1, 'MAX_RETRIES_TIMBRADO', 'Numero maximo de reintentos para timbrado', '3', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(1, 1, 'TIMEOUT_TIMBRADO_MS', 'Timeout en milisegundos para servicio de timbrado', '30000', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(1, 1, 'URL_PAC_PRODUCCION', 'URL del PAC en ambiente de produccion', 'https://pac.ejemplo.com/timbrado', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(1, 1, 'URL_PAC_SANDBOX', 'URL del PAC en ambiente de pruebas', 'https://sandbox.pac.ejemplo.com/timbrado', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(1, 1, 'VERSION_CFDI', 'Version de CFDI vigente', '4.0', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(1, 1, 'DIAS_CANCELACION', 'Dias limite para cancelacion de CFDI', '30', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
-- FINANZAS (id_module=2)
(2, 1, 'MONEDA_DEFAULT', 'Moneda por defecto del sistema', 'MXN', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(2, 1, 'TIPO_CAMBIO_API', 'URL de API para tipo de cambio', 'https://api.banxico.org.mx/tipocambio', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(2, 1, 'IVA_TASA_GENERAL', 'Tasa general de IVA', '0.16', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(2, 1, 'RETENCION_ISR', 'Tasa de retencion ISR', '0.10', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
-- SEGURIDAD (id_module=5)
(5, 1, 'JWT_EXPIRATION_HOURS', 'Horas de expiracion del token JWT', '8', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(5, 1, 'MAX_LOGIN_ATTEMPTS', 'Intentos maximos de login antes de bloqueo', '5', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(5, 1, 'PASSWORD_MIN_LENGTH', 'Longitud minima de contrasena', '8', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(5, 1, 'SESSION_TIMEOUT_MINUTES', 'Timeout de sesion en minutos', '30', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
-- REPORTES (id_module=6)
(6, 1, 'REPORT_PAGE_SIZE', 'Tamano de pagina por defecto en reportes', '50', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(6, 1, 'REPORT_MAX_ROWS', 'Maximo de filas en exportacion', '10000', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(6, 1, 'REPORT_CACHE_MINUTES', 'Minutos de cache para reportes', '15', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
-- NOTIFICACIONES (id_module=7)
(7, 1, 'SMTP_HOST', 'Servidor SMTP para envio de correos', 'smtp.empresa.com', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(7, 1, 'SMTP_PORT', 'Puerto SMTP', '587', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(7, 1, 'EMAIL_FROM', 'Correo remitente por defecto', 'noreply@empresa.com', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP),
(7, 1, 'MAX_EMAILS_BATCH', 'Maximo de correos por lote', '100', 1.0, CURRENT_TIMESTAMP, 1, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- VERIFICACION
-- =====================================================
SELECT 'cat_module' AS tabla, COUNT(*) AS registros FROM core_utils.cat_module
UNION ALL SELECT 'cat_item_type', COUNT(*) FROM core_utils.cat_item_type
UNION ALL SELECT 'cat_item', COUNT(*) FROM core_utils.cat_item
UNION ALL SELECT 'cat_process', COUNT(*) FROM core_utils.cat_process
UNION ALL SELECT 'cat_message', COUNT(*) FROM core_utils.cat_message
UNION ALL SELECT 'application_msg', COUNT(*) FROM core_utils.application_msg
UNION ALL SELECT 'cat_parameter', COUNT(*) FROM core_utils.cat_parameter;
