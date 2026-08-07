-- select * from shared_catalogs.catalog_header;
-- select * from shared_catalogs.catalog_detail;
-- select * from shared_catalogs.dictionary_lang;


-- ============================================================================
-- CATALOGOS API - Catálogos del SAT para CFDI 4.0
-- Esquema: shared_catalogs
-- Descripción: Catálogos fiscales del SAT con external_key, vigencia y atributos
-- Fuente: Anexo 20 CFDI 4.0 - SAT
-- Fecha: 2025-12-04
-- ============================================================================

-- SET search_path TO shared_catalogs;

-- ============================================================================
-- CATALOG_HEADER: Nuevos catálogos del SAT
-- ============================================================================

-- Obtener el próximo ID disponible
DO $$
DECLARE
    next_header_id INTEGER;
    next_dict_id INTEGER;
BEGIN
    SELECT COALESCE(MAX(id), 0) + 1 INTO next_header_id FROM shared_catalogs.catalog_header;
    SELECT COALESCE(MAX(dict_id), 0) + 1 INTO next_dict_id FROM shared_catalogs.dictionary_lang;

    RAISE NOTICE 'Next header_id: %, Next dict_id: %', next_header_id, next_dict_id;
END $$;

-- ============================================================================
-- 1. CATÁLOGO: c_RegimenFiscal (SAT)
-- ============================================================================

-- Diccionario para RegimenFiscal (dict_id: 5000-5019)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5000, 1, 'General de Ley Personas Morales'),
(5000, 2, 'General Law Legal Entities'),
(5001, 1, 'Personas Morales con Fines no Lucrativos'),
(5001, 2, 'Non-Profit Legal Entities'),
(5002, 1, 'Sueldos y Salarios e Ingresos Asimilados a Salarios'),
(5002, 2, 'Wages, Salaries and Assimilated Income'),
(5003, 1, 'Arrendamiento'),
(5003, 2, 'Leasing'),
(5004, 1, 'Régimen de Enajenación o Adquisición de Bienes'),
(5004, 2, 'Asset Disposal or Acquisition Regime'),
(5005, 1, 'Demás ingresos'),
(5005, 2, 'Other Income'),
(5006, 1, 'Residentes en el Extranjero sin Establecimiento Permanente en México'),
(5006, 2, 'Foreign Residents without Permanent Establishment in Mexico'),
(5007, 1, 'Ingresos por Dividendos (socios y accionistas)'),
(5007, 2, 'Dividend Income (partners and shareholders)'),
(5008, 1, 'Personas Físicas con Actividades Empresariales y Profesionales'),
(5008, 2, 'Individuals with Business and Professional Activities'),
(5009, 1, 'Ingresos por intereses'),
(5009, 2, 'Interest Income'),
(5010, 1, 'Régimen de los ingresos por obtención de premios'),
(5010, 2, 'Prize Income Regime'),
(5011, 1, 'Sin obligaciones fiscales'),
(5011, 2, 'Without Tax Obligations'),
(5012, 1, 'Sociedades Cooperativas de Producción que optan por diferir sus ingresos'),
(5012, 2, 'Production Cooperative Societies opting to defer income'),
(5013, 1, 'Incorporación Fiscal'),
(5013, 2, 'Fiscal Incorporation'),
(5014, 1, 'Actividades Agrícolas, Ganaderas, Silvícolas y Pesqueras'),
(5014, 2, 'Agricultural, Livestock, Forestry and Fishing Activities'),
(5015, 1, 'Opcional para Grupos de Sociedades'),
(5015, 2, 'Optional for Corporate Groups'),
(5016, 1, 'Coordinados'),
(5016, 2, 'Coordinated'),
(5017, 1, 'Régimen de las Actividades Empresariales con ingresos a través de Plataformas Tecnológicas'),
(5017, 2, 'Business Activities through Technology Platforms Regime'),
(5018, 1, 'Régimen Simplificado de Confianza'),
(5018, 2, 'Simplified Trust Regime');

-- Header para RegimenFiscal
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(23, 'c_RegimenFiscal', 'RGF', 'Régimen Fiscal SAT', 'Catálogo de regímenes fiscales del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1);

-- Detalles de RegimenFiscal con external_key
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to, attributes) VALUES
(23, 'RGF001', '601', 5000, 1, 1, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": false, "persona_moral": true}'),
(23, 'RGF002', '603', 5001, 2, 2, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": false, "persona_moral": true}'),
(23, 'RGF003', '605', 5002, 3, 3, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF004', '606', 5003, 4, 4, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF005', '607', 5004, 5, 5, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF006', '608', 5005, 6, 6, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF007', '610', 5006, 7, 7, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": true}'),
(23, 'RGF008', '611', 5007, 8, 8, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF009', '612', 5008, 9, 9, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF010', '614', 5009, 10, 10, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF011', '615', 5010, 11, 11, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF012', '616', 5011, 12, 12, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF013', '620', 5012, 13, 13, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": false, "persona_moral": true}'),
(23, 'RGF014', '621', 5013, 14, 14, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF015', '622', 5014, 15, 15, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": false, "persona_moral": true}'),
(23, 'RGF016', '623', 5015, 16, 16, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": false, "persona_moral": true}'),
(23, 'RGF017', '624', 5016, 17, 17, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": false, "persona_moral": true}'),
(23, 'RGF018', '625', 5017, 18, 18, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": false}'),
(23, 'RGF019', '626', 5018, 19, 19, 1, '2000-01-01', '2030-12-31', '{"persona_fisica": true, "persona_moral": true}');

-- ============================================================================
-- 2. CATÁLOGO: c_UsoCFDI (SAT)
-- ============================================================================

-- Diccionario para UsoCFDI (dict_id: 5100-5122)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5100, 1, 'Adquisición de mercancías'),
(5100, 2, 'Merchandise acquisition'),
(5101, 1, 'Devoluciones, descuentos o bonificaciones'),
(5101, 2, 'Returns, discounts or bonuses'),
(5102, 1, 'Gastos en general'),
(5102, 2, 'General expenses'),
(5103, 1, 'Construcciones'),
(5103, 2, 'Construction'),
(5104, 1, 'Mobiliario y equipo de oficina por inversiones'),
(5104, 2, 'Office furniture and equipment'),
(5105, 1, 'Equipo de transporte'),
(5105, 2, 'Transport equipment'),
(5106, 1, 'Equipo de cómputo y accesorios'),
(5106, 2, 'Computing equipment and accessories'),
(5107, 1, 'Dados, troqueles, moldes, matrices y herramental'),
(5107, 2, 'Dies, stamps, molds, matrices and tools'),
(5108, 1, 'Comunicaciones telefónicas'),
(5108, 2, 'Telephone communications'),
(5109, 1, 'Comunicaciones satelitales'),
(5109, 2, 'Satellite communications'),
(5110, 1, 'Otra maquinaria y equipo'),
(5110, 2, 'Other machinery and equipment'),
(5111, 1, 'Honorarios médicos, dentales y gastos hospitalarios'),
(5111, 2, 'Medical, dental fees and hospital expenses'),
(5112, 1, 'Gastos médicos por incapacidad o discapacidad'),
(5112, 2, 'Medical expenses for disability'),
(5113, 1, 'Gastos funerales'),
(5113, 2, 'Funeral expenses'),
(5114, 1, 'Donativos'),
(5114, 2, 'Donations'),
(5115, 1, 'Intereses reales efectivamente pagados por créditos hipotecarios (casa habitación)'),
(5115, 2, 'Real interest paid on mortgage loans (housing)'),
(5116, 1, 'Aportaciones voluntarias al SAR'),
(5116, 2, 'Voluntary SAR contributions'),
(5117, 1, 'Primas por seguros de gastos médicos'),
(5117, 2, 'Medical expense insurance premiums'),
(5118, 1, 'Gastos de transportación escolar obligatoria'),
(5118, 2, 'Mandatory school transportation expenses'),
(5119, 1, 'Depósitos en cuentas para el ahorro, primas de pensiones'),
(5119, 2, 'Savings account deposits, pension premiums'),
(5120, 1, 'Pagos por servicios educativos (colegiaturas)'),
(5120, 2, 'Educational service payments (tuition)'),
(5121, 1, 'Sin efectos fiscales'),
(5121, 2, 'Without tax effects'),
(5122, 1, 'Pagos'),
(5122, 2, 'Payments'),
(5123, 1, 'Nómina'),
(5123, 2, 'Payroll');

-- Header para UsoCFDI
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(24, 'c_UsoCFDI', 'UCF', 'Uso de CFDI SAT', 'Catálogo de usos de CFDI del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1);

-- Detalles de UsoCFDI con external_key y atributos
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to, attributes) VALUES
(24, 'UCF001', 'G01', 5100, 1, 1, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF002', 'G02', 5101, 2, 2, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF003', 'G03', 5102, 3, 3, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF004', 'I01', 5103, 4, 4, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF005', 'I02', 5104, 5, 5, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF006', 'I03', 5105, 6, 6, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF007', 'I04', 5106, 7, 7, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF008', 'I05', 5107, 8, 8, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF009', 'I06', 5108, 9, 9, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF010', 'I07', 5109, 10, 10, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF011', 'I08', 5110, 11, 11, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF012', 'D01', 5111, 12, 12, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF013', 'D02', 5112, 13, 13, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF014', 'D03', 5113, 14, 14, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF015', 'D04', 5114, 15, 15, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF016', 'D05', 5115, 16, 16, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF017', 'D06', 5116, 17, 17, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF018', 'D07', 5117, 18, 18, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF019', 'D08', 5118, 19, 19, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF020', 'D09', 5119, 20, 20, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF021', 'D10', 5120, 21, 21, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}'),
(24, 'UCF022', 'S01', 5121, 22, 22, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF023', 'CP01', 5122, 23, 23, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": true}'),
(24, 'UCF024', 'CN01', 5123, 24, 24, 1, '2000-01-01', '2030-12-31', '{"aplica_pf": true, "aplica_pm": false}');

-- ============================================================================
-- 3. CATÁLOGO: c_TasaOCuota (SAT) - Tasas de IVA
-- ============================================================================

-- Diccionario para TasaOCuota (dict_id: 5200-5205)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5200, 1, 'IVA Tasa 16%'),
(5200, 2, 'VAT Rate 16%'),
(5201, 1, 'IVA Tasa 8% Región Fronteriza'),
(5201, 2, 'VAT Rate 8% Border Region'),
(5202, 1, 'IVA Tasa 0%'),
(5202, 2, 'VAT Rate 0%'),
(5203, 1, 'IVA Exento'),
(5203, 2, 'VAT Exempt'),
(5204, 1, 'Retención IVA 10.6667%'),
(5204, 2, 'VAT Withholding 10.6667%'),
(5205, 1, 'Retención IVA 4%'),
(5205, 2, 'VAT Withholding 4%');

-- Header para TasaOCuota
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(25, 'c_TasaOCuota', 'TOC', 'Tasa o Cuota IVA SAT', 'Catálogo de tasas o cuotas de IVA del SAT', 'fiscal', 'SAT_FISCAL', 1);

-- Detalles de TasaOCuota con external_key y value
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, value, dict_id, internal_status, sort_order, status, valid_from, valid_to, attributes) VALUES
(25, 'TOC001', '0.160000', '0.16', 5200, 1, 1, 1, '2000-01-01', '2030-12-31', '{"impuesto": "002", "factor": "Tasa", "traslado": true, "retencion": false}'),
(25, 'TOC002', '0.080000', '0.08', 5201, 2, 2, 1, '2000-01-01', '2030-12-31', '{"impuesto": "002", "factor": "Tasa", "traslado": true, "retencion": false, "region_fronteriza": true}'),
(25, 'TOC003', '0.000000', '0.00', 5202, 3, 3, 1, '2000-01-01', '2030-12-31', '{"impuesto": "002", "factor": "Tasa", "traslado": true, "retencion": false}'),
(25, 'TOC004', 'Exento', '0.00', 5203, 4, 4, 1, '2000-01-01', '2030-12-31', '{"impuesto": "002", "factor": "Exento", "traslado": true, "retencion": false}'),
(25, 'TOC005', '0.106667', '0.106667', 5204, 5, 5, 1, '2000-01-01', '2030-12-31', '{"impuesto": "002", "factor": "Tasa", "traslado": false, "retencion": true}'),
(25, 'TOC006', '0.040000', '0.04', 5205, 6, 6, 1, '2000-01-01', '2030-12-31', '{"impuesto": "002", "factor": "Tasa", "traslado": false, "retencion": true}');

-- ============================================================================
-- 4. CATÁLOGO: c_FormaPago (SAT)
-- ============================================================================

-- Diccionario para FormaPago (dict_id: 5300-5320)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5300, 1, 'Efectivo'),
(5300, 2, 'Cash'),
(5301, 1, 'Cheque nominativo'),
(5301, 2, 'Nominative check'),
(5302, 1, 'Transferencia electrónica de fondos'),
(5302, 2, 'Electronic funds transfer'),
(5303, 1, 'Tarjeta de crédito'),
(5303, 2, 'Credit card'),
(5304, 1, 'Monedero electrónico'),
(5304, 2, 'Electronic wallet'),
(5305, 1, 'Dinero electrónico'),
(5305, 2, 'Electronic money'),
(5306, 1, 'Vales de despensa'),
(5306, 2, 'Grocery vouchers'),
(5307, 1, 'Dación en pago'),
(5307, 2, 'Payment in kind'),
(5308, 1, 'Pago por subrogación'),
(5308, 2, 'Subrogation payment'),
(5309, 1, 'Pago por consignación'),
(5309, 2, 'Consignment payment'),
(5310, 1, 'Condonación'),
(5310, 2, 'Remission'),
(5311, 1, 'Compensación'),
(5311, 2, 'Compensation'),
(5312, 1, 'Novación'),
(5312, 2, 'Novation'),
(5313, 1, 'Confusión'),
(5313, 2, 'Confusion'),
(5314, 1, 'Remisión de deuda'),
(5314, 2, 'Debt remission'),
(5315, 1, 'Prescripción o caducidad'),
(5315, 2, 'Prescription or expiration'),
(5316, 1, 'A satisfacción del acreedor'),
(5316, 2, 'To creditor satisfaction'),
(5317, 1, 'Tarjeta de débito'),
(5317, 2, 'Debit card'),
(5318, 1, 'Tarjeta de servicios'),
(5318, 2, 'Services card'),
(5319, 1, 'Aplicación de anticipos'),
(5319, 2, 'Advance payment application'),
(5320, 1, 'Por definir'),
(5320, 2, 'To be defined');

-- Header para FormaPago
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(26, 'c_FormaPago', 'FPG', 'Forma de Pago SAT', 'Catálogo de formas de pago del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1);

-- Detalles de FormaPago
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(26, 'FPG001', '01', 5300, 1, 1, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG002', '02', 5301, 2, 2, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG003', '03', 5302, 3, 3, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG004', '04', 5303, 4, 4, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG005', '05', 5304, 5, 5, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG006', '06', 5305, 6, 6, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG007', '08', 5306, 7, 7, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG008', '12', 5307, 8, 8, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG009', '13', 5308, 9, 9, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG010', '14', 5309, 10, 10, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG011', '15', 5310, 11, 11, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG012', '17', 5311, 12, 12, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG013', '23', 5312, 13, 13, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG014', '24', 5313, 14, 14, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG015', '25', 5314, 15, 15, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG016', '26', 5315, 16, 16, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG017', '27', 5316, 17, 17, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG018', '28', 5317, 18, 18, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG019', '29', 5318, 19, 19, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG020', '30', 5319, 20, 20, 1, '2000-01-01', '2030-12-31'),
(26, 'FPG021', '99', 5320, 21, 21, 1, '2000-01-01', '2030-12-31');

-- ============================================================================
-- 5. CATÁLOGO: c_MetodoPago (SAT)
-- ============================================================================

-- Diccionario para MetodoPago (dict_id: 5400-5401)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5400, 1, 'Pago en una sola exhibición'),
(5400, 2, 'Payment in one installment'),
(5401, 1, 'Pago en parcialidades o diferido'),
(5401, 2, 'Payment in installments or deferred');

-- Header para MetodoPago
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(27, 'c_MetodoPago', 'MPG', 'Método de Pago SAT', 'Catálogo de métodos de pago del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1);

-- Detalles de MetodoPago
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(27, 'MPG001', 'PUE', 5400, 1, 1, 1, '2000-01-01', '2030-12-31'),
(27, 'MPG002', 'PPD', 5401, 2, 2, 1, '2000-01-01', '2030-12-31');

-- ============================================================================
-- 6. CATÁLOGO: c_TipoDeComprobante (SAT)
-- ============================================================================

-- Diccionario para TipoDeComprobante (dict_id: 5500-5505)
INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
(5500, 1, 'Ingreso'),
(5500, 2, 'Income'),
(5501, 1, 'Egreso'),
(5501, 2, 'Expense'),
(5502, 1, 'Traslado'),
(5502, 2, 'Transfer'),
(5503, 1, 'Nómina'),
(5503, 2, 'Payroll'),
(5504, 1, 'Pago'),
(5504, 2, 'Payment');

-- Header para TipoDeComprobante
INSERT INTO shared_catalogs.catalog_header (id, code, prefix, name, description, module, catalog_type, status) VALUES
(28, 'c_TipoDeComprobante', 'TDC', 'Tipo de Comprobante SAT', 'Catálogo de tipos de comprobante del SAT para CFDI 4.0', 'fiscal', 'SAT_FISCAL', 1);

-- Detalles de TipoDeComprobante
INSERT INTO shared_catalogs.catalog_detail (header_id, key, external_key, dict_id, internal_status, sort_order, status, valid_from, valid_to) VALUES
(28, 'TDC001', 'I', 5500, 1, 1, 1, '2000-01-01', '2030-12-31'),
(28, 'TDC002', 'E', 5501, 2, 2, 1, '2000-01-01', '2030-12-31'),
(28, 'TDC003', 'T', 5502, 3, 3, 1, '2000-01-01', '2030-12-31'),
(28, 'TDC004', 'N', 5503, 4, 4, 1, '2000-01-01', '2030-12-31'),
(28, 'TDC005', 'P', 5504, 5, 5, 1, '2000-01-01', '2030-12-31');

-- ============================================================================
-- Actualizar secuencia de IDs
-- ============================================================================
SELECT setval('shared_catalogs.catalog_header_id_seq', 28, true);

-- ============================================================================
-- 7. RELACIONES DE DEPENDENCIA: c_UsoCFDI depende de c_RegimenFiscal
-- ============================================================================
-- Según el SAT, cada Uso de CFDI tiene regímenes fiscales específicos donde aplica.
-- La relación es: UsoCFDI (source) -> DEPENDS_ON -> RegimenFiscal (target)
--
-- Mapeo según Anexo 20 CFDI 4.0:
-- - Usos G01, G02, G03 (Adquisiciones, Devoluciones, Gastos): Aplica a casi todos los regímenes
-- - Usos I01-I08 (Inversiones): Aplica a regímenes empresariales
-- - Usos D01-D10 (Deducciones personales): Solo personas físicas
-- - S01 (Sin efectos fiscales): Universal
-- - CP01 (Pagos): Universal
-- - CN01 (Nómina): Solo para empleadores

-- Para este ejemplo, insertamos las relaciones más comunes.
-- La lógica completa del SAT indica que:
-- - Regímenes de Personas Morales (601, 603, 620, 622, 623, 624): Usos con aplica_pm=true
-- - Regímenes de Personas Físicas (605, 606, 607, 608, 611, 612, 614, 615, 616, 621, 625, 626): Usos con aplica_pf=true

-- Nota: Para una implementación completa, las relaciones se manejan mejor consultando
-- los atributos JSON (aplica_pf, aplica_pm) en tiempo de ejecución.
-- Aquí se muestran ejemplos de relaciones explícitas para regímenes específicos.

-- Ejemplo: Relaciones para Régimen 601 (General de Ley PM) - solo usos que aplican a PM
-- source_detail_id = ID del UsoCFDI, target_detail_id = ID del RegimenFiscal
-- relation_type = 'DEPENDS_ON', status = 1

-- Las relaciones se insertarían con los IDs reales de catalog_detail
-- Por ahora se dejan comentadas ya que los IDs se generan dinámicamente

-- Insertar relaciones UsoCFDI -> RegimenFiscal basándose en atributos JSON
-- Solo se insertan si no existen previamente
INSERT INTO shared_catalogs.catalog_detail_relation
(source_detail_id, target_detail_id, relation_type, status, created_at)
SELECT
    uso.id as source_detail_id,
    regimen.id as target_detail_id,
    'DEPENDS_ON' as relation_type,
    1 as status,
    NOW() as created_at
FROM shared_catalogs.catalog_detail uso
CROSS JOIN shared_catalogs.catalog_detail regimen
WHERE uso.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'c_UsoCFDI')
AND regimen.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'c_RegimenFiscal')
AND (
    -- Si el régimen es de PM y el uso aplica a PM
    (regimen.attributes->>'persona_moral' = 'true' AND uso.attributes->>'aplica_pm' = 'true')
    OR
    -- Si el régimen es de PF y el uso aplica a PF
    (regimen.attributes->>'persona_fisica' = 'true' AND uso.attributes->>'aplica_pf' = 'true')
)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- FIN DEL SCRIPT DE CATÁLOGOS SAT
-- ============================================================================

/*
EJEMPLOS DE USO:

1. Obtener regímenes fiscales para personas físicas:
   SELECT * FROM shared_catalogs.catalog_detail
   WHERE header_id = 23
   AND attributes->>'persona_fisica' = 'true';

2. Obtener usos de CFDI para personas morales:
   SELECT * FROM shared_catalogs.catalog_detail
   WHERE header_id = 24
   AND attributes->>'aplica_pm' = 'true';

3. Obtener tasas de IVA de traslado:
   SELECT * FROM shared_catalogs.catalog_detail
   WHERE header_id = 25
   AND attributes->>'traslado' = 'true';

4. Buscar por external_key (clave SAT):
   SELECT * FROM shared_catalogs.catalog_detail
   WHERE external_key = '601';
*/


SELECT
    i.fiscal_uuid,
    i.document_type AS tipo,
    i.status,
    i.series || '-' || i.folio AS serie_folio,
    i.total,
    i.currency,
    iss.rfc AS rfc_emisor,
    a.supplier_number AS num_proveedor,
    a.purchase_order_number AS oc,
    a.reception_number AS recepcion
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.issuer iss ON i.issuer_uuid = iss.issuer_uuid
LEFT JOIN tenant_fiscal.addendum a ON i.invoice_uuid = a.invoice_uuid
ORDER BY i.status, i.created_at desc


--------- DML ----

DO $$
DECLARE
    v_base_dict_id INTEGER;
    v_existing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_existing_count
    FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
      AND cd.key IN ('WRN7024', 'WRN7025');

    IF v_existing_count > 0 THEN
        RAISE NOTICE 'WRN7024/WRN7025 ya existen en CatMsgAdvertencia. Saltando.';
    ELSE
        SELECT COALESCE(MAX(dict_id), 0) + 1 INTO v_base_dict_id
        FROM shared_catalogs.dictionary_lang;

        RAISE NOTICE 'Insertando traducciones WRN7024/WRN7025 desde dict_id = %', v_base_dict_id;

        -- WRN7024
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 0, 1, 'El valor del pago total no es igual al desglose del pago, favor de validar.'),
            (v_base_dict_id + 0, 2, 'The total payment value does not match the payment breakdown, please validate.'),
            (v_base_dict_id + 0, 3, 'O valor do pagamento total não é igual ao detalhamento do pagamento, favor validar.');

        -- WRN7025
        INSERT INTO shared_catalogs.dictionary_lang (dict_id, lang_id, description) VALUES
            (v_base_dict_id + 1, 1, 'El desglose del pago no contiene un ingreso, favor de validar.'),
            (v_base_dict_id + 1, 2, 'The payment breakdown does not contain an income entry, please validate.'),
            (v_base_dict_id + 1, 3, 'O detalhamento do pagamento não contém uma receita, favor validar.');
    END IF;
END $$;

-- =============================================================
-- 2. Insertar catalog_detail (si no existen)
-- =============================================================
INSERT INTO shared_catalogs.catalog_detail
    (header_id, key, dict_id, sort_order, status, created_at)
SELECT
    (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia'),
    v.key,
    (SELECT dl.dict_id FROM shared_catalogs.dictionary_lang dl WHERE dl.description = v.desc_es AND dl.lang_id = 1 LIMIT 1),
    v.sort_order,
    1,
    NOW()
FROM (VALUES
    ('WRN7024', 'El valor del pago total no es igual al desglose del pago, favor de validar.', 7024),
    ('WRN7025', 'El desglose del pago no contiene un ingreso, favor de validar.', 7025)
) AS v(key, desc_es, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.catalog_detail cd
    WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
    AND cd.key = v.key
);

-- =============================================================
-- 3. Verificar resultado
-- =============================================================
SELECT cd.key, cd.sort_order, cd.status, dl.lang_id, dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.header_id = (SELECT id FROM shared_catalogs.catalog_header WHERE code = 'CatMsgAdvertencia')
  AND cd.key IN ('WRN7024', 'WRN7025')
ORDER BY cd.key, dl.lang_id;

