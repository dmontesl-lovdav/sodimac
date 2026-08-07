select *
from confdatosemisor;

SELECT id, rfc, razonSocial, activo FROM confdatosemisor;


-- rfc: 5e58505b01276543a39dbfe7e2fc1c40
-- razon social: 885ae81d06408ff89c7920fe90589ee029b3f616e9bee2aac584447768b9b679ce8207f1f2eaf1d8d0e3153532ca4ec9
-- XIA190128J61

-- 
update confdatosemisor
set rfc = '6dd307560c3ffc6a54179f82b4231a7a'
 , razonSocial = '81c506e956ebe75e7673df417147e0cdb5243363aaed58097827cb47e22ef321'
 where id = 1;


-- XIA190128J61
update confdatosemisor
set rfc = '5e58505b01276543a39dbfe7e2fc1c40'
 , razonSocial = '885ae81d06408ff89c7920fe90589ee0e5a1b5dc63eb5aff490cc29641246332'
 where id = 1;

SELECT * FROM confdatosemisortienda WHERE idTienda = 1000;



SELECT a.* FROM confformametodopago a
JOIN cattipocomprobantesodimac ccs ON a.tipoComprobante = ccs.id
JOIN catmediopago cmp ON a.medioPago = cmp.id
JOIN versiones v ON a.version = v.id
WHERE ccs.tipocomprobante = 'F'
  AND cmp.idMedioPago = '01'
  AND v.verson = '4.0';


-- 1. Emisor (RFC encriptado, por eso mejor consulta todo)
SELECT id, rfc, razonSocial, regimenFiscal, calle, noExterior, noInterior, 
       colonia, localidad, municipio, estado, pais, idcatcodigopostal, activo 
FROM confdatosemisor;


-- 2. Tienda (sucursal = 1000)
SELECT id, idConfDatosEmisor, idTienda, descripcion, calle, noExterior, noInterior,
       colonia, localidad, municipio, estado, pais, idcatcodigopostal, idcattipotienda, activo 
FROM confdatosemisortienda 
WHERE idTienda = 1000;

-- 3. Tipo Comprobante Sodimac 
SELECT ccs.id, ccs.tipocomprobante, ccs.descripcion, ccs.idcattipocomprobantesat, ccs.formaMetodoPago,
       sat.tipocomprobante AS tipoSat, sat.descripcion AS descSat
FROM cattipocomprobantesodimac ccs
JOIN cattipocomprobantesat sat ON ccs.idcattipocomprobantesat = sat.id
WHERE ccs.tipocomprobante = 'F';

-- 4. Forma/Método de Pago (tipoComprobante='F', medioPago='01', version='4.0')
SELECT a.id, a.tipoComprobante, a.formaPago, a.metodoPago, a.medioPago, a.version
FROM confformametodopago a
JOIN cattipocomprobantesodimac ccs ON a.tipoComprobante = ccs.id
JOIN catmediopago cmp ON a.medioPago = cmp.id
JOIN versiones v ON a.version = v.id
WHERE ccs.tipocomprobante = 'F'
  AND cmp.idMedioPago = '01'
  AND v.verson = '4.0';

--- 5. Serie (tipocomprobante='F', idTienda=1000)
SELECT s.id, s.serie, s.descripcion, s.idcattipocomprobantesodimac, s.idcattipotienda, s.activo,
       ccs.tipocomprobante, cdet.idTienda, cdet.idcattipotienda AS tiendaTipo
FROM catserie s
JOIN cattipocomprobantesodimac ccs ON s.idcattipocomprobantesodimac = ccs.id
JOIN confdatosemisortienda cdet ON s.idcattipotienda = cdet.idcattipotienda
WHERE ccs.tipocomprobante = 'F'
  AND cdet.idTienda = 1000;

-- 6. Folio (depende de los IDs del paso 5)
SELECT id, idcatserie, idconfdatosemisortienda, folio, fechaCreacion, fechaModificacion 
FROM folio 
WHERE idcatserie = -- <id_de_catserie_paso5>
  AND idconfdatosemisortienda = <id_de_confdatosemisortienda_paso5>;

-- 7. Tipo Tienda (catálogo auxiliar)
SELECT id, tipotienda, descripcion, activo FROM cattipotienda;

-- 8. Versiones
SELECT id, verson, activo FROM versiones WHERE verson = '4.0';



select *
from catconfiguracion