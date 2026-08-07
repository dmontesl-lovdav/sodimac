BEGIN;

-- ----------------------------------------------------------------------------
-- 1. Borrar solo los catalogos que este script maneja (por code)
-- ----------------------------------------------------------------------------
DELETE FROM shared_catalogs.catalog_detail_relation
WHERE source_detail_id IN (
    SELECT d.id FROM shared_catalogs.catalog_detail d
    JOIN shared_catalogs.catalog_header h ON d.header_id = h.id
    WHERE h.code IN ('CatPerfil','CatModulo','CatAplicativo','CatEvento','CatAtributo','CatRol','CatPermiso')
);

DELETE FROM shared_catalogs.dictionary_lang
WHERE dict_id IN (
    SELECT d.dict_id FROM shared_catalogs.catalog_detail d
    JOIN shared_catalogs.catalog_header h ON d.header_id = h.id
    WHERE h.code IN ('CatPerfil','CatModulo','CatAplicativo','CatEvento','CatAtributo','CatRol','CatPermiso')
);

DELETE FROM shared_catalogs.catalog_detail
WHERE header_id IN (
    SELECT id FROM shared_catalogs.catalog_header
    WHERE code IN ('CatPerfil','CatModulo','CatAplicativo','CatEvento','CatAtributo','CatRol','CatPermiso')
);

DELETE FROM shared_catalogs.catalog_header
WHERE code IN ('CatPerfil','CatModulo','CatAplicativo','CatEvento','CatAtributo','CatRol','CatPermiso');

-- ----------------------------------------------------------------------------
-- 2. Insertar headers
-- ----------------------------------------------------------------------------
INSERT INTO shared_catalogs.catalog_header (code, prefix, name, description, module, catalog_type) VALUES
  ('CatPerfil',     'PER', 'Perfil',     'Perfiles de proveedores y usuarios internos', 'seguridad', 'SIMPLE'),
  ('CatModulo',     'MOD', 'Modulo',     'Modulos del sistema',                          'seguridad', 'SIMPLE'),
  ('CatAplicativo', 'APL', 'Aplicativo', 'Aplicativos por modulo',                       'seguridad', 'SIMPLE'),
  ('CatEvento',     'EVT', 'Evento',     'Eventos (botones/acciones) por aplicativo',    'seguridad', 'SIMPLE'),
  ('CatAtributo',   'ATR', 'Atributo',   'Atributos de segmentacion de seguridad',       'seguridad', 'SIMPLE'),
  ('CatRol',        'ROL', 'Rol',        'Roles internos y externos',                    'seguridad', 'SIMPLE'),
  ('CatPermiso',    'PRM', 'Permiso',    'Acciones permitidas sobre aplicativos',        'seguridad', 'SIMPLE');

-- ============================================================================
-- 3. CatPerfil (9 registros, dict_id 10001-10009)
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order)
SELECT h.id, v.key, v.dict_id, v.sort_order
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('PER001', 10001, 1),
        ('PER002', 10002, 2),
        ('PER003', 10003, 3),
        ('PER004', 10004, 4),
        ('PER005', 10005, 5),
        ('PER006', 10006, 6),
        ('PER007', 10007, 7),
        ('PER008', 10008, 8),
        ('PER009', 10009, 9)
     ) AS v(key, dict_id, sort_order)
WHERE h.code = 'CatPerfil';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10001, 1, 'Transporte'),              (10001, 2, 'Transport'),              (10001, 3, 'Transporte'),
  (10002, 1, 'Mercancia'),               (10002, 2, 'Merchandise'),            (10002, 3, 'Mercadoria'),
  (10003, 1, 'Servicios'),               (10003, 2, 'Services'),               (10003, 3, 'Servicos'),
  (10004, 1, 'Indirecto'),               (10004, 2, 'Indirect'),               (10004, 3, 'Indireto'),
  (10005, 1, 'Finanzas'),                (10005, 2, 'Finance'),                (10005, 3, 'Financas'),
  (10006, 1, 'Comercial'),               (10006, 2, 'Commercial'),             (10006, 3, 'Comercial'),
  (10007, 1, 'Auditoria'),               (10007, 2, 'Audit'),                  (10007, 3, 'Auditoria'),
  (10008, 1, 'Administrador funcional'), (10008, 2, 'Functional Administrator'),(10008, 3, 'Administrador funcional'),
  (10009, 1, 'Administrador'),           (10009, 2, 'Administrator'),          (10009, 3, 'Administrador');

-- ============================================================================
-- 4. CatModulo (6 registros, dict_id 10011-10016)
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order)
SELECT h.id, v.key, v.dict_id, v.sort_order
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('MOD001', 10011, 1),
        ('MOD002', 10012, 2),
        ('MOD003', 10013, 3),
        ('MOD004', 10014, 4),
        ('MOD005', 10015, 5),
        ('MOD006', 10016, 6)
     ) AS v(key, dict_id, sort_order)
WHERE h.code = 'CatModulo';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10011, 1, 'Catalogos'),         (10011, 2, 'Catalogs'),         (10011, 3, 'Catalogos'),
  (10012, 1, 'Finanzas'),          (10012, 2, 'Finance'),          (10012, 3, 'Financas'),
  (10013, 1, 'Fiscal'),            (10013, 2, 'Tax'),              (10013, 3, 'Fiscal'),
  (10014, 1, 'Utilerias'),         (10014, 2, 'Utilities'),        (10014, 3, 'Utilitarios'),
  (10015, 1, 'Auditoria'),         (10015, 2, 'Audit'),            (10015, 3, 'Auditoria'),
  (10016, 1, 'Control de acceso'), (10016, 2, 'Access Control'),   (10016, 3, 'Controle de acesso');

-- ============================================================================
-- 5. CatAplicativo (16 registros, dict_id 10021-10036)
--    attributes.idModulo = sort_order del CatModulo correspondiente
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, attributes)
SELECT h.id, v.key, v.dict_id, v.sort_order, v.attrs::jsonb
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('APL001', 10021,  1, '{"idModulo":1}'),
        ('APL002', 10022,  2, '{"idModulo":1}'),
        ('APL003', 10023,  3, '{"idModulo":2}'),
        ('APL004', 10024,  4, '{"idModulo":2}'),
        ('APL005', 10025,  5, '{"idModulo":2}'),
        ('APL006', 10026,  6, '{"idModulo":2}'),
        ('APL007', 10027,  7, '{"idModulo":2}'),
        ('APL008', 10028,  8, '{"idModulo":2}'),
        ('APL009', 10029,  9, '{"idModulo":3}'),
        ('APL010', 10030, 10, '{"idModulo":3}'),
        ('APL011', 10031, 11, '{"idModulo":3}'),
        ('APL012', 10032, 12, '{"idModulo":4}'),
        ('APL013', 10033, 13, '{"idModulo":5}'),
        ('APL014', 10034, 14, '{"idModulo":6}'),
        ('APL015', 10035, 15, '{"idModulo":6}'),
        ('APL016', 10036, 16, '{"idModulo":6}')
     ) AS v(key, dict_id, sort_order, attrs)
WHERE h.code = 'CatAplicativo';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10021, 1, 'Catalogo de proveedores'),          (10021, 2, 'Supplier Catalog'),                    (10021, 3, 'Catalogo de fornecedores'),
  (10022, 1, 'Catalogo de catalogos'),            (10022, 2, 'Catalog of Catalogs'),                 (10022, 3, 'Catalogo de catalogos'),
  (10023, 1, 'Recepciones'),                      (10023, 2, 'Receptions'),                          (10023, 3, 'Recepcoes'),
  (10024, 1, 'Carta Porte'),                      (10024, 2, 'Bill of Lading'),                      (10024, 3, 'Carta de Porte'),
  (10025, 1, 'Descuentos comerciales'),           (10025, 2, 'Commercial Discounts'),                (10025, 3, 'Descontos comerciais'),
  (10026, 1, 'Estado de cuenta'),                 (10026, 2, 'Account Statement'),                   (10026, 3, 'Extrato de conta'),
  (10027, 1, 'Three way match'),                  (10027, 2, 'Three Way Match'),                     (10027, 3, 'Three Way Match'),
  (10028, 1, 'Recepciones Migo'),                 (10028, 2, 'MIGO Receptions'),                     (10028, 3, 'Recepcoes Migo'),
  (10029, 1, 'Gestion de facturas'),              (10029, 2, 'Invoice Management'),                  (10029, 3, 'Gestao de faturas'),
  (10030, 1, 'Gestion de notas de credito'),      (10030, 2, 'Credit Note Management'),              (10030, 3, 'Gestao de notas de credito'),
  (10031, 1, 'Gestion de complemento de pago'),   (10031, 2, 'Payment Complement Management'),       (10031, 3, 'Gestao de complemento de pagamento'),
  (10032, 1, 'Parametros'),                       (10032, 2, 'Parameters'),                          (10032, 3, 'Parametros'),
  (10033, 1, 'Bitacora de actividades'),          (10033, 2, 'Activity Log'),                        (10033, 3, 'Registro de atividades'),
  (10034, 1, 'Administracion de perfiles'),       (10034, 2, 'Profile Administration'),              (10034, 3, 'Administracao de perfis'),
  (10035, 1, 'Administracion de roles'),          (10035, 2, 'Role Administration'),                 (10035, 3, 'Administracao de funcoes'),
  (10036, 1, 'Administracion de permisos'),       (10036, 2, 'Permission Administration'),           (10036, 3, 'Administracao de permissoes');

-- ============================================================================
-- 6. CatEvento (7 registros, dict_id 10041-10047)
--    idEvento 3-7 asignados secuencialmente (vacios en xlsx)
--    attributes.idAplicativo = sort_order del CatAplicativo correspondiente
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, attributes)
SELECT h.id, v.key, v.dict_id, v.sort_order, v.attrs::jsonb
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('EVT001', 10041, 1, '{"idAplicativo":1}'),
        ('EVT002', 10042, 2, '{"idAplicativo":1}'),
        ('EVT003', 10043, 3, '{"idAplicativo":3}'),
        ('EVT004', 10044, 4, '{"idAplicativo":3}'),
        ('EVT005', 10045, 5, '{"idAplicativo":4}'),
        ('EVT006', 10046, 6, '{"idAplicativo":4}'),
        ('EVT007', 10047, 7, '{"idAplicativo":4}')
     ) AS v(key, dict_id, sort_order, attrs)
WHERE h.code = 'CatEvento';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10041, 1, 'Boton Buscar'),               (10041, 2, 'Search Button'),               (10041, 3, 'Botao de busca'),
  (10042, 1, 'Descargar Reporte'),          (10042, 2, 'Download Report'),             (10042, 3, 'Baixar relatorio'),
  (10043, 1, 'Relacionar Factura'),         (10043, 2, 'Link Invoice'),                (10043, 3, 'Vincular fatura'),
  (10044, 1, 'Cancelar Recepcion'),         (10044, 2, 'Cancel Reception'),            (10044, 3, 'Cancelar recepcao'),
  (10045, 1, 'Cancelar Carta Porte'),       (10045, 2, 'Cancel Bill of Lading'),       (10045, 3, 'Cancelar carta de porte'),
  (10046, 1, 'Descargar Carta Porte CSV'),  (10046, 2, 'Download Bill of Lading CSV'), (10046, 3, 'Baixar CSV carta de porte'),
  (10047, 1, 'Descargar Carta Porte PDF'),  (10047, 2, 'Download Bill of Lading PDF'), (10047, 3, 'Baixar PDF carta de porte');

-- ============================================================================
-- 7. CatAtributo (5 registros, dict_id 10051-10055)
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order)
SELECT h.id, v.key, v.dict_id, v.sort_order
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('ATR001', 10051, 1),
        ('ATR002', 10052, 2),
        ('ATR003', 10053, 3),
        ('ATR004', 10054, 4),
        ('ATR005', 10055, 5)
     ) AS v(key, dict_id, sort_order)
WHERE h.code = 'CatAtributo';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10051, 1, 'Proveedor'),       (10051, 2, 'Supplier'),       (10051, 3, 'Fornecedor'),
  (10052, 1, 'TipoProveedor'),   (10052, 2, 'Supplier Type'),  (10052, 3, 'Tipo de fornecedor'),
  (10053, 1, 'Empresa'),         (10053, 2, 'Company'),        (10053, 3, 'Empresa'),
  (10054, 1, 'GrupoProveedor'),  (10054, 2, 'Supplier Group'), (10054, 3, 'Grupo de fornecedores'),
  (10055, 1, 'TipoRebate'),      (10055, 2, 'Rebate Type'),    (10055, 3, 'Tipo de rebate');

-- ============================================================================
-- 8. CatRol (10 registros, dict_id 10061-10070)
--    tipoRol en attributes
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order, attributes)
SELECT h.id, v.key, v.dict_id, v.sort_order, v.attrs::jsonb
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('ROL001', 10061,  1, '{"tipoRol":"Externo"}'),
        ('ROL002', 10062,  2, '{"tipoRol":"Externo"}'),
        ('ROL003', 10063,  3, '{"tipoRol":"Externo"}'),
        ('ROL004', 10064,  4, '{"tipoRol":"Externo"}'),
        ('ROL005', 10065,  5, '{"tipoRol":"Interno"}'),
        ('ROL006', 10066,  6, '{"tipoRol":"Interno"}'),
        ('ROL007', 10067,  7, '{"tipoRol":"Interno"}'),
        ('ROL008', 10068,  8, '{"tipoRol":"Interno"}'),
        ('ROL009', 10069,  9, '{"tipoRol":"Interno"}'),
        ('ROL010', 10070, 10, '{"tipoRol":"Interno"}')
     ) AS v(key, dict_id, sort_order, attrs)
WHERE h.code = 'CatRol';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10061, 1, 'Proveedor - Mercancia'),               (10061, 2, 'Supplier - Merchandise'),          (10061, 3, 'Fornecedor - Mercadoria'),
  (10062, 1, 'Proveedor - Transporte'),              (10062, 2, 'Supplier - Transport'),             (10062, 3, 'Fornecedor - Transporte'),
  (10063, 1, 'Proveedor - Servicios'),               (10063, 2, 'Supplier - Services'),              (10063, 3, 'Fornecedor - Servicos'),
  (10064, 1, 'Proveedor - Indirectos'),              (10064, 2, 'Supplier - Indirect'),              (10064, 3, 'Fornecedor - Indiretos'),
  (10065, 1, 'Analista Financiero - General'),       (10065, 2, 'Financial Analyst - General'),      (10065, 3, 'Analista Financeiro - Geral'),
  (10066, 1, 'Analista Financiero - Especializado'), (10066, 2, 'Financial Analyst - Specialized'),  (10066, 3, 'Analista Financeiro - Especializado'),
  (10067, 1, 'Gerente Financiero'),                  (10067, 2, 'Finance Manager'),                  (10067, 3, 'Gerente Financeiro'),
  (10068, 1, 'Gerente de Transporte'),               (10068, 2, 'Transport Manager'),                (10068, 3, 'Gerente de Transporte'),
  (10069, 1, 'Administrador Funcional'),             (10069, 2, 'Functional Administrator'),         (10069, 3, 'Administrador Funcional'),
  (10070, 1, 'Administrador del Sistema'),           (10070, 2, 'System Administrator'),             (10070, 3, 'Administrador do Sistema');

-- ============================================================================
-- 9. CatPermiso (8 registros, dict_id 10081-10088)
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail (header_id, key, dict_id, sort_order)
SELECT h.id, v.key, v.dict_id, v.sort_order
FROM shared_catalogs.catalog_header h,
     (VALUES
        ('PRM001', 10081, 1),
        ('PRM002', 10082, 2),
        ('PRM003', 10083, 3),
        ('PRM004', 10084, 4),
        ('PRM005', 10085, 5),
        ('PRM006', 10086, 6),
        ('PRM007', 10087, 7),
        ('PRM008', 10088, 8)
     ) AS v(key, dict_id, sort_order)
WHERE h.code = 'CatPermiso';

INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
  (10081, 1, 'Consulta'),       (10081, 2, 'Query'),          (10081, 3, 'Consulta'),
  (10082, 1, 'Registro'),       (10082, 2, 'Registration'),   (10082, 3, 'Registro'),
  (10083, 1, 'Actualizacion'),  (10083, 2, 'Update'),         (10083, 3, 'Atualizacao'),
  (10084, 1, 'Borrado'),        (10084, 2, 'Deletion'),       (10084, 3, 'Exclusao'),
  (10085, 1, 'Cancelacion'),    (10085, 2, 'Cancellation'),   (10085, 3, 'Cancelamento'),
  (10086, 1, 'Autorizacion'),   (10086, 2, 'Authorization'),  (10086, 3, 'Autorizacao'),
  (10087, 1, 'Aprobacion'),     (10087, 2, 'Approval'),       (10087, 3, 'Aprovacao'),
  (10088, 1, 'Rechazo'),        (10088, 2, 'Rejection'),      (10088, 3, 'Rejeicao');

-- ============================================================================
-- 10. Relaciones: CatAplicativo -> CatModulo  (DEPENDS_ON)
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail_relation (source_detail_id, target_detail_id, relation_type)
SELECT apl.id, mod.id, 'DEPENDS_ON'
FROM shared_catalogs.catalog_detail apl
JOIN shared_catalogs.catalog_header h_apl ON h_apl.id = apl.header_id AND h_apl.code = 'CatAplicativo'
JOIN shared_catalogs.catalog_header h_mod ON h_mod.code = 'CatModulo'
JOIN shared_catalogs.catalog_detail mod
     ON mod.header_id = h_mod.id
    AND mod.sort_order = (apl.attributes->>'idModulo')::int;

-- ============================================================================
-- 11. Relaciones: CatEvento -> CatAplicativo  (DEPENDS_ON)
-- ============================================================================
INSERT INTO shared_catalogs.catalog_detail_relation (source_detail_id, target_detail_id, relation_type)
SELECT evt.id, apl.id, 'DEPENDS_ON'
FROM shared_catalogs.catalog_detail evt
JOIN shared_catalogs.catalog_header h_evt ON h_evt.id = evt.header_id AND h_evt.code = 'CatEvento'
JOIN shared_catalogs.catalog_header h_apl ON h_apl.code = 'CatAplicativo'
JOIN shared_catalogs.catalog_detail apl
     ON apl.header_id = h_apl.id
    AND apl.sort_order = (evt.attributes->>'idAplicativo')::int;

COMMIT;

-- ============================================================================
-- Verificacion
-- ============================================================================
SELECT h.code, COUNT(d.id) AS detalles,
       COUNT(dl.id) FILTER (WHERE dl.lang_id = 1) AS es,
       COUNT(dl.id) FILTER (WHERE dl.lang_id = 2) AS en,
       COUNT(dl.id) FILTER (WHERE dl.lang_id = 3) AS pt
FROM shared_catalogs.catalog_header h
JOIN shared_catalogs.catalog_detail d ON d.header_id = h.id
JOIN shared_catalogs.dictionary_lang dl ON dl.dict_id = d.dict_id
WHERE h.code IN ('CatPerfil','CatModulo','CatAplicativo','CatEvento','CatAtributo','CatRol','CatPermiso')
GROUP BY h.code
ORDER BY h.code;

SELECT 'Relaciones' AS info, COUNT(*) AS total
FROM shared_catalogs.catalog_detail_relation r
JOIN shared_catalogs.catalog_detail d ON d.id = r.source_detail_id
JOIN shared_catalogs.catalog_header h ON h.id = d.header_id
WHERE h.code IN ('CatAplicativo','CatEvento');
