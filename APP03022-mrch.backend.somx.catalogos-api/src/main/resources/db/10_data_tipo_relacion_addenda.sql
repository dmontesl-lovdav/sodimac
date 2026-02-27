-- ============================================================================
-- CATALOGOS API - Catálogos c_TipoRelacion (SAT) y TipoAddenda (Sodimac)
-- Esquema: shared_catalogs
-- Descripción: Catálogos para tipos de relación entre CFDIs y tipos de addenda
-- Fuente: Anexo 20 CFDI 4.0 - SAT / Definición interna Sodimac
-- Fecha: 2025-12-15
-- JIRA: STM-396
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- 1. CATÁLOGO: c_TipoRelacion (SAT)
-- Tipos de relación entre comprobantes fiscales digitales
-- ============================================================================

-- Diccionario para TipoRelacion (dict_id: 5600-5608) - ES(1), EN(2), PT(3)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5600, 1, 'Nota de crédito de los documentos relacionados'),
(5600, 2, 'Credit note for related documents'),
(5600, 3, 'Nota de crédito dos documentos relacionados'),
(5601, 1, 'Nota de débito de los documentos relacionados'),
(5601, 2, 'Debit note for related documents'),
(5601, 3, 'Nota de débito dos documentos relacionados'),
(5602, 1, 'Devolución de mercancía sobre facturas o traslados previos'),
(5602, 2, 'Merchandise return on previous invoices or transfers'),
(5602, 3, 'Devolução de mercadoria sobre faturas ou transferências anteriores'),
(5603, 1, 'Sustitución de los CFDI previos'),
(5603, 2, 'Substitution of previous CFDIs'),
(5603, 3, 'Substituição dos CFDIs anteriores'),
(5604, 1, 'Traslados de mercancías facturados previamente'),
(5604, 2, 'Previously invoiced merchandise transfers'),
(5604, 3, 'Transferências de mercadorias faturadas anteriormente'),
(5605, 1, 'Factura generada por los traslados previos'),
(5605, 2, 'Invoice generated for previous transfers'),
(5605, 3, 'Fatura gerada pelas transferências anteriores'),
(5606, 1, 'CFDI por aplicación de anticipo'),
(5606, 2, 'CFDI for advance payment application'),
(5606, 3, 'CFDI por aplicação de adiantamento'),
(5607, 1, 'Factura generada por pagos en parcialidades'),
(5607, 2, 'Invoice generated for installment payments'),
(5607, 3, 'Fatura gerada por pagamentos parcelados'),
(5608, 1, 'Factura generada por pagos diferidos'),
(5608, 2, 'Invoice generated for deferred payments'),
(5608, 3, 'Fatura gerada por pagamentos diferidos')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- Header para TipoRelacion
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(29, 'c_TipoRelacion', 'TRL', 'Tipo de Relación SAT', 'Catálogo de tipos de relación entre CFDIs del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1)
ON CONFLICT (id) DO NOTHING;

-- Detalles de TipoRelacion con external_key
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(29, 'TRL001', '01', 5600, 1, 1, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL002', '02', 5601, 2, 2, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL003', '03', 5602, 3, 3, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL004', '04', 5603, 4, 4, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL005', '05', 5604, 5, 5, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL006', '06', 5605, 6, 6, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL007', '07', 5606, 7, 7, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL008', '08', 5607, 8, 8, 1, '2000-01-01', '2030-12-31'),
(29, 'TRL009', '09', 5608, 9, 9, 1, '2000-01-01', '2030-12-31')
ON CONFLICT (header_id, key) DO NOTHING;

-- ============================================================================
-- 2. CATÁLOGO: TipoAddenda (Sodimac)
-- Tipos de addenda según clasificación interna Sodimac
-- ============================================================================

-- Diccionario para TipoAddenda (dict_id: 5700-5704) - ES(1), EN(2), PT(3)
-- Valores basados en la implementación actual de fiscal-api
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5700, 1, 'Addenda Estándar'),
(5700, 2, 'Standard Addenda'),
(5700, 3, 'Addenda Padrão'),
(5701, 1, 'Addenda con Carta Porte'),
(5701, 2, 'Addenda with Bill of Lading'),
(5701, 3, 'Addenda com Carta de Porte'),
(5702, 1, 'Addenda Complemento de Pago'),
(5702, 2, 'Payment Complement Addenda'),
(5702, 3, 'Addenda Complemento de Pagamento'),
(5703, 1, 'Addenda Internacional'),
(5703, 2, 'International Addenda'),
(5703, 3, 'Addenda Internacional'),
(5704, 1, 'Addenda Sodimac'),
(5704, 2, 'Sodimac Addenda'),
(5704, 3, 'Addenda Sodimac')
ON CONFLICT (dict_id, lang_id) DO NOTHING;

-- Header para TipoAddenda
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(30, 'TipoAddenda', 'TAD', 'Tipo de Addenda Sodimac', 'Catálogo de tipos de addenda según clasificación Sodimac', 'fiscal', 'SIMPLE', 1)
ON CONFLICT (id) DO NOTHING;

-- Detalles de TipoAddenda con external_key (clave numérica)
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(30, 'TAD001', '1', 5700, 1, 1, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD002', '2', 5701, 2, 2, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD003', '3', 5702, 3, 3, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD004', '4', 5703, 4, 4, 1, '2000-01-01', '2030-12-31'),
(30, 'TAD005', '5', 5704, 5, 5, 1, '2000-01-01', '2030-12-31')
ON CONFLICT (header_id, key) DO NOTHING;

-- ============================================================================
-- Actualizar secuencia de IDs
-- ============================================================================
SELECT setval('shared_catalogs.catalog_header_id_seq', 30, true);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================

/*
EJEMPLOS DE USO:

1. Obtener descripción de TipoRelacion por external_key (código SAT):
   GET /c_TipoRelacion/details/01?lang=1
   Respuesta: {"key": "TRL001", "externalKey": "01", "description": "Nota de crédito de los documentos relacionados"}

2. Obtener todos los tipos de relación para combo:
   GET /c_TipoRelacion/details?lang=1

3. Obtener descripción de TipoAddenda por external_key:
   GET /TipoAddenda/details/1?lang=1
   Respuesta: {"key": "TAD001", "externalKey": "1", "description": "Addenda Estándar"}

4. Buscar por external_key directamente en BD:
   SELECT dl.description
   FROM shared_catalogs.catalog_detail cd
   JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id AND dl.lang_id = 1
   WHERE cd.header_id = 29 AND cd.external_key = '01';
*/
