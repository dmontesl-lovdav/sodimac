call cli_2fac_3cab_4det_5lfa_6ler ('2026071610500066579');
call cli_2fac_3cab_4det_5lfa_6ler ('2025011410800107944');
call cli_2fac_3cab_4det_5lfa_6ler ('2024080910300029040');
call uspEliminarTicket('2026071610500066579');

-- ErrorMessage: null, Cause: null, StackTrace: [Ljava.lang.StackTraceElement;@21ff62e4, Ticket: 2025122611000116232
-- 249.00 
-- b2ea8653bc9ab2fdfd75bc433d27d170
-- CNE970116Q72

-- a96ae1f5f624fe07e8fbae7e9f9891a38ddbf0084b2ae79a245c1882cab56c01fdd84ccad01d8cb4fbc535878c717648d5f0d00fea94c6998a8466ac6b088cde
-- CONSEJO NACIONAL DE LA INDUSTRIA DEL CONOCIMIENTO

-- 5544fe1a4603c8deaba1becdfc4c61b8
-- 37134

-- ErrorMessage: null, Cause: null, StackTrace: [Ljava.lang.StackTraceElement;@45df8023, Ticket: 2025121610500078878

-- 2025091210800157332, 2025091210800157333, 2025091210800157334, 2025091210800157335, 2025091210800157336
call cli_2fac_3cab_4det_5lfa_6ler ('2025091210800157332'); -- EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar CFD, ErrorDesc: , Ticket: 2025091210800157332, Folio: 86266, FacturaId: 1246686
call cli_2fac_3cab_4det_5lfa_6ler ('2025091210800157333'); -- EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar CFD, ErrorDesc: , Ticket: 2025091210800157333, Folio: 86267, FacturaId: 1246693
call cli_2fac_3cab_4det_5lfa_6ler ('2025091210800157334'); -- EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar CFD, ErrorDesc: , Ticket: 2025091210800157334, Folio: 86272, FacturaId: 1246709
call cli_2fac_3cab_4det_5lfa_6ler ('2025091210800157335'); -- EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar CFD, ErrorDesc: , Ticket: 2025091210800157333, Folio: 86267, FacturaId: 1246693
call cli_2fac_3cab_4det_5lfa_6ler ('2025091210800157336'); -- EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar CFD, ErrorDesc: , Ticket: 2025091210800157333, Folio: 86267, FacturaId: 1246693

select *
from facturas f 
-- where f.ticket in  ('2025071411200060621')
-- where fechaCreacion >= str_to_date('14/07/2025', '%d/%m/%Y')
-- and  metodoPago = 'PPD'
-- limit 10
order  by 1 desc;

select *
from facturas
where uuid = '640D9D16-9A2E-4A9A-A822-52556F2A0D27';

select *
from facturas 
where rfc = '66440fae764f843dcdf3fd4c973830fa'
order by 1 desc;

Select *
From clientes
Where idCliente in (159462);

update clientes
set estatusenviado = 0
Where idCliente in (159462);



select * from facturas where ticket = '2025071010100151298';
select * from facturas where uuid='F2E2F300-8963-431C-B3BB-F0F80857456A';

select date_format(str_to_date('14/07/2025', '%d/%m/%Y'), '%Y-%m-%d') as Date from VarcharToDate;

select * 
from clientes
where rfc = '386a964385cfd48202d5abe1c2b347e6';

update clientes
set razonSocial = '4ba5ddd958571290c1ff33334f278e0647618d83844e2dd2a6d740e55a466fd4'
where rfc= '386a964385cfd48202d5abe1c2b347e6';

select *
from catConfiguracion
where NombreCampo = 'WebService.ParametrosFact.Url.ObtenerParametro';

-- https://10.138.150.88:8443/wsprmfac/api/catalogos/parametros/

select * from catconfiguracion where nombrecampo = 'ExpresionRegular.RazonSocial.Caracteres': 
-- ^[a-zA-Z0-9/@ÄËÏÖÜäëïöü"áéíóúÁÉÍÓÚñÑ&-.,()_ #]*$
-- ^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ&-.,_: ]*$

-- 2025062520000051475

select * 
from facturas 
where uuid in ('BADF9E7F-C7FE-4491-AA95-7634AD480613','5ED3A8F5-3366-450C-9126-9E5FB2936957','742FDF10-1516-4AA9-BF9D-DB26C3F93A59');


select *
from facturas 
where upper(uuid) in (upper('a7528285-537e-477d-ae95-c7372dbbb474')
			 , upper('fe8b4318-02e2-4573-bff5-760296103346')
             , upper('3e5d3616-21bc-4be0-a6f1-16b2a75a531e')
             , upper('dd6adc10-65f3-43dd-ae09-b6d9b249e358'));

select *
from facturas 
where idFacturaPac in (1113611,1113603,1113597,1113617);

select * from clientes where email is null;
select * from clientestemporal c  where email = '';

select *
from refacturacionBatch
where ticket in ('2025042210100198465','2025042210100198466','2025042210100198464','2025041510100198428')

select distinct
          ct.idClientesTemporal
        , ct.rfc
        , ct.ticket
        , concat('''',ct.ticket,''',') as ticket
        , ct.razonSocial
        , ct.idUsoCfdi
        , ct.email
        , ct.activo
        , ct.fechaCreacion
        , ct.nombreObra
        , ct.responsableObra
		, ct.codigoPostal
        , ct.regimenFiscal
        -- select count(1)
        from clientesTemporal ct inner join logfacturacion f on ct.rfc = f.rfc and ct.ticket = f.ticket
        where f.idEstatusFactura = 13
        -- and ct.ticket = '2025041710400070391' 
        -- and ct.ticket = '2025042210100198465'
        -- and ct.ticket in  ('2025042210100198465','2025042210100198466','2025042210100198464','2025041510100198428')
        and ct.ticket like '2025%'    
        and ct.ticket not in (select ticket from facturas)
        and ct.ticket not in (select ticket from logerrores where rfc = ct.rfc and errorlog like '%Este RFC del receptor no existe en la lista de RFC inscritos%' and activo = 1 and fechaCreacion > CURDATE()- INTERVAL 60 DAY)
        and ct.ticket not in (select ticket from logerrores where rfc = ct.rfc and errorlog like '%Sin Error> Datos para procesar%' and activo = 1 and fechaCreacion > CURDATE()- INTERVAL 60 DAY)
        and ct.ticket in (select ticket from listafacturas where total > 0)
        and ct.fechaCreacion > CURDATE()- INTERVAL 60 DAY
        HAVING (        select count(*)
                    from refacturacionBatch a inner join refacturacionDetail b on a.refacturacionId = b.refacturacionId 
                    where a.rfc = ct.rfc and a.ticket = ct.ticket and activo = 1) < 4 -- < 4 -- >= 4
        order by ct.fechaCreacion;


SHOW PROCESSLIST;
show variables like "max_connections";

SELECT *
FROM INFORMATION_SCHEMA.PROCESSLIST
WHERE command='Sleep' 

SELECT GROUP_CONCAT('KILL ',id SEPARATOR '; ') AS kill_list
FROM INFORMATION_SCHEMA.PROCESSLIST
WHERE command='Sleep' 
AND DB IS NOT NULL;

show status like 'Threads%';
SHOW GLOBAL STATUS;

KILL 103888; KILL 103843; KILL 103797; KILL 103771; KILL 103701; KILL 103689; KILL 103685; KILL 102664; KILL 102663

select *
from catconfiguracion
where NombreCampo = 'batch.intervalo';

show create procedure uspExistToken;
show create procedure uspFacturasPacPendientes;
show create procedure uspInsertaLogErrores;
show create procedure uspTicketsCodigo77;
show create procedure uspExistTicketEnProceso;
show create procedure uspCFDIClientesTemporal;


select distinct
          f.idFacturaPac idClientesTemporal
        , e.rfc
        , e.ticket
        , e.razonSocial
        , e.idUsoCfdi
        , e.email
        , e.activo
        , e.fechaCreacion
        , e.nombreObra
        , e.responsableObra
        , e.codigoPostal
        , e.regimenFiscal
        -- select *
        from clientesTemporal e inner join logfacturacion f on e.rfc = f.rfc and e.ticket = f.ticket
        where 1=1
        -- and e.ticket = '2025062410500076968'
        and  f.idEstatusFactura = 13
	and  e.ticket not in (select ticket from facturas)
        and e.ticket in (select ticket from logerrores where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 3, ErrorMessage: <Sin Error> Datos para procesar%'
                         union 
                         select ticket from logerrores where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar%') 
        and e.ticket in (select ticket from listafacturas where total > 0)
        and e.fechaCreacion > CURDATE()- INTERVAL 60 DAY
        and e.ticket not like '202208%'
        and e.ticket not like '202209%'
        order by e.ticket desc;

select * from clientesTemporal where ticket = '2025062410500076968';


select * from logfacturacion where ticket = '2025062410500076968';
update logfacturacion where ticket = '2025062410500076968';

 update logfacturacion 
 set idEstatusFactura = 13
 where ticket = '2025062410500076968'
 and   idEstatusFactura=9;
 
 select *
 from listafacturas
 where ticket = '2025062410500076968'
 
 select ticket 
 from logerrores 
 where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 3, ErrorMessage: <Sin Error> Datos para procesar%'
 and  ticket = '2025062410500076968'
     union 
 select ticket 
 from logerrores 
 where idFacturaPac > 0 
 and activo = 1 
 and errorlog like 'EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar%'
 and  ticket = '2025062410500076968'
 )
 
 
 select * from logfacturacion where ticket = '2025062410500076968';
 
 
 --- Notas de Crédito -------
 select *
 from catConfiguracion
 where NombreCampo = 'Request.OrdenCompra.PeriodoMeses'
 -- 'WebService.Facturacion.Url'; -- https://10.138.150.77/wsft/api/timbrarNCTicket
 -- 'NotasCredito.FechaInicial'; -- 03/03/2023
 
 
 show create procedure uspControlNCIniciar;
 
 select * 
 from ControlNC
 order by 1 desc; -- 1414
 
 show create procedure uspObtenerFacturaTicket;
 show create procedure uspExistTempTicket;
 
 
select * from facturas 
where ticket = '2025060210500074470' 
   or ticketBct = '2025060210500074470'
order by fechaCreacion limit 1;



show create procedure uspExistTempTicket;

select *
from clientestemporal 
where ticket = '2025060210500074470' 
order by fechaCreacion limit 1


select * 
from facturas
where uuid in ('5AC9E77D-AC83-4603-AF34-2B8383B8500E',
'4E662F3F-20C6-4420-8286-B5E766C2C48A',
'BC2FB97B-6C2D-459C-BF3D-CDB8CA75D067');


select *
from facturas
where ticket in ('2025060210500074470');

/*
 * 	2025-06-02 21:20:34
	2025-06-13 22:31:40
 * */


select * from CatConfiguracion
where NombreCampo = 'WebService.Configuracion.Url.Login';


select * -- count(1) -- * -- idFactura
from facturas f 
-- where f.ticket in  ('2025071411200060621')
 where fechaCreacion >= str_to_date('17/07/2025', '%d/%m/%Y')
 and   fechaCreacion <= str_to_date('18/07/2025', '%d/%m/%Y')
--  and  estatusenviado = 1
 and  metodoPago = 'PPD'
 limit 10
order  by 1;

select *
from facturas
where idFactura in (22065351,22065366,22065370,22065372,22065391,22065413,22065458,22065487,22065488,22065524);

update facturas
set estatusenviado = 0
where idFactura in (22065351,22065366,22065370,22065372,22065391,22065413,22065458,22065487,22065488,22065524);

SELECT idFactura, idPac, idCliente, rfc, email, ticket, idVersionFacturaSodimac, idFacturaPac, uuid, fechaTimbrado, versionFacturacionSat, xml, fechaCompra, idOrigen, idEstatusFactura, fechaCreacion, nombreArchivo, ticketBct, versionFactura, transaccion, nombreObra, idComprobante, uuidRelacionado, responsableObra, acuse, serie, folio, total, subTotal, metodoPago, estatusenviado 
FROM Facturas_Temp;

Select *
From clientes
Where idCliente in (144308,33438,71104,58637,99961,47736,33438,33438,49777,5730);

update clientes
set estatusenviado = 0
Where idCliente in (144308,33438,71104,58637,99961,47736,33438,33438,49777,5730);


select * from logErrores where ticket in (5509);

select * 
from clientesTemporal
where ticket = '2025072210300110926';

select *  
from catconfiguracion
where NombreCampo = 'batch.intervalo';

select distinct
          ct.idClientesTemporal
        , ct.rfc
        , ct.ticket
        , ct.razonSocial
        , ct.idUsoCfdi
        , ct.email
        , ct.activo
        , ct.fechaCreacion
        , ct.nombreObra
        , ct.responsableObra
		, ct.codigoPostal
        , ct.regimenFiscal
        from clientesTemporal ct inner join logfacturacion f on ct.rfc = f.rfc and ct.ticket = f.ticket
        where ct.ticket = '2025072210300110926'
        and f.idEstatusFactura = 13
        and ct.ticket not in (select ticket from facturas)
        and ct.ticket not in (select ticket from logerrores where rfc = ct.rfc and errorlog like '%Este RFC del receptor no existe en la lista de RFC inscritos%' and activo = 1 and fechaCreacion > CURDATE()- INTERVAL 60 DAY)
        and ct.ticket not in (select ticket from logerrores where rfc = ct.rfc and errorlog like '%Sin Error> Datos para procesar%' and activo = 1 and fechaCreacion > CURDATE()- INTERVAL 60 DAY)
        and ct.ticket in (select ticket from listafacturas where total > 0)
        and ct.fechaCreacion > CURDATE()- INTERVAL 60 DAY
        HAVING (        select count(*)
                    from refacturacionBatch a inner join refacturacionDetail b on a.refacturacionId = b.refacturacionId 
                    where a.rfc = ct.rfc and a.ticket = ct.ticket and activo = 1) < 4
        order by ct.fechaCreacion;

select count(*)
from refacturacionBatch a inner join refacturacionDetail b on a.refacturacionId = b.refacturacionId 
where a.rfc = '18e001daaffe1401dbe701315c8ba7f8' 
and   a.ticket = '2025072210300110926' 
and   activo = 1;

select * 
from refacturacionBatch a
where a.rfc = '18e001daaffe1401dbe701315c8ba7f8';

select * 
-- delete 
from refacturacionDetail
where refacturacionId = 77558;



 select distinct
          f.idFacturaPac idClientesTemporal
        , e.rfc
        , e.ticket
        , e.razonSocial
        , e.idUsoCfdi
        , e.email
        , e.activo
        , e.fechaCreacion
        , e.nombreObra
        , e.responsableObra
        , e.codigoPostal
        , e.regimenFiscal
        from clientesTemporal e inner join logfacturacion f on e.rfc = f.rfc and e.ticket = f.ticket
        where f.idEstatusFactura = 13
	and e.ticket not in (select ticket from facturas)
        and e.ticket in (select ticket from logerrores where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 3, ErrorMessage: <Sin Error> Datos para procesar%'
                         union 
                         select ticket from logerrores where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar%') 
        and e.ticket in (select ticket from listafacturas where total > 0)
        and e.fechaCreacion > CURDATE()- INTERVAL 60 DAY
        and e.ticket not like '202208%'
        and e.ticket not like '202209%'
        order by e.fechaCreacion;
 
 
 select * from catconfiguracion where nombrecampo = 'Configuracion.RFC.PublicoGeneral'
 update catconfiguracion set valor = 'XAXX0101010002' where nombrecampo = 'Configuracion.RFC.PublicoGeneral'
 
 
 update facturas
 set estatusenviado = 0
 where rfc = '66440fae764f843dcdf3fd4c973830fa';
 
 
 select *
 from facturas
 where rfc = 'c6578a31f8ff55c6d0b0b28b46fde560'
 
 select * from facturas where ticket = '5847';
 
 select *
 from catConfiguracion b 
 where NombreCampo  = 'WS.Configuracion.Url.UrlConsultaTodosUsoCfdi40';   
 -- https://10.138.150.88:8443/finanzasadminfacturacion/api/usoscfdi40/all
 
 -- WS.Configuracion.Url.UrlConsultaTodosUsoCfdi40
 
 -- https://10.138.150.88:8443/finanzasadminfacturacion/api/usoscfdi40/all
 
 
 select *
  -- delete
 from logfacturacion where ticket = '2025100610300156702';
 
 select * 
 -- delete
 from clientesTemporal where ticket = '2025100610300156702';
 
 select * 
 -- delete
 from facturas where ticket = '2025100610300156702';
 
 
 
 
 select *
 from catConfiguracion
 where NombreCampo = 'WebService.Confirmar.Url'
 
 CALL uspCFDIClientesTemporal('2025082510400054091');
 
 SELECT 
    a.ticket,
    a.rfc,
    a.razonsocial,
    b.clave AS usocfdi,
    a.email,
    a.regimenfiscal,
    a.codigopostal,
    a.fechacreacion,
    a.idusocfdi
FROM clientesTemporal a 
INNER JOIN catusoscfdi b ON a.idusocfdi = b.idusocfdi
WHERE a.ticket = '2025082510400054091' 
ORDER BY a.fechacreacion DESC 
LIMIT 1;

SELECT codigo, atiende 
FROM catIncidenciasNivelBasico 
WHERE activo = 1;

SELECT 
    idfacturapac, 
    uuid, 
    fechaTimbrado, 
    ABS(TIMESTAMPDIFF(MINUTE, fechaTimbrado, fechacreacion)) AS minutos, 
    total
FROM facturas 
WHERE ticket = '2025082510400054091' 
ORDER BY fechaTimbrado DESC 
LIMIT 1;

SELECT total 
FROM listafacturas 
WHERE ticket = '2025082510400054091' 
ORDER BY fechaIngreso DESC 
LIMIT 1;

SELECT errorlog 
FROM logerrores 
WHERE ticket = '2025082510400054091' 
ORDER BY fechacreacion DESC 
LIMIT 1;

SELECT COUNT(1) AS intentos
FROM refacturacionDetail a 
JOIN refacturacionBatch b ON a.refacturacionId = b.refacturacionId 
WHERE ticket = '2025082510400054091';

SELECT 
    a.ticket,
    a.rfc,
    a.razonsocial,
    b.clave AS usocfdi,
    a.email,
    a.regimenfiscal,
    a.codigopostal,
    a.fechacreacion,
    a.idusocfdi
FROM clientesTemporal a 
INNER JOIN catusoscfdi b ON a.idusocfdi = b.idusocfdi
WHERE a.ticket = '2025082510400054091' 
ORDER BY a.fechacreacion DESC 
LIMIT 1;

SELECT codigo, atiende 
FROM catIncidenciasNivelBasico 
WHERE activo = 1;

select *
from catconfiguracion c 
where NombreCampo = 'WebService.ObtenerTicket.Url'

update catconfiguracion c 
set Valor =  'http://10.138.150.88:8080/wsmdlwticket/Ticket/Obtener/v1.0'
where NombreCampo = 'WebService.ObtenerTicket.Url'



SELECT * -- nombreCampo, valor, aplicacion, descripcion 
FROM catConfiguracion 
WHERE NombreCampo LIKE '%WebService.ObtenerTicket.Url%'

--ErrorMessage: Fallo al acceder al WSDL en: http://10.138.153.10:8080/wsmdlwticket/Ticket/Obtener/v1.0?wsdl. Ha fallado con: 
	Connection timed out: connect., Cause: java.net.ConnectException: Connection timed out: connect, StackTrace: [Ljava.lang.StackTraceElement;@2733299d
	
	ErrorMessage: Fallo al acceder al WSDL en: http://10.138.153.10:8080/wsmdlwticket/Ticket/Obtener/v1.0?wsdl. Ha fallado con: 
	Connection timed out: connect., Cause: java.net.ConnectException: Connection timed out: connect, StackTrace: [Ljava.lang.StackTraceElement;@464ca576
	
	
	SELECT * -- nombreCampo, valor, aplicacion, descripcion 
FROM catConfiguracion 
WHERE Valor LIKE '%Confirmar%'


select *
from pacs;


SELECT * FROM confDatosEmisor;


-- uspObtenerFacturaTicket
select * from facturas where ticket = 7392 or ticketBct = 7392 order by fechaCreacion limit 1;


SELECT idFactura, ticket, ticketBct, uuid, idComprobante, uuidRelacionado,
       fechaTimbrado, idEstatusFactura, fechaCreacion
FROM facturas
WHERE ticket = '2026071610500066579'
ORDER BY fechaCreacion DESC;

SELECT COUNT(*) AS total_ingreso,
       SUM(CASE WHEN uuidRelacionado IS NOT NULL AND uuidRelacionado <> '' THEN 1 ELSE 0 END) AS con_uuid_relacionado
FROM facturas
WHERE idComprobante = 'I'
  AND fechaTimbrado >= '2026-01-01';


SELECT f.idFactura, f.ticket, f.idCliente, f.idComprobante,
       c.idUsoCfdi, cat.clave, cat.descripcionUso
FROM facturas f
LEFT JOIN clientes c    ON c.idCliente  = f.idCliente
LEFT JOIN catusoscfdi cat ON cat.idUsoCfdi = c.idUsoCfdi
WHERE f.ticket = '2026071610500066579';


SELECT cat.clave, cat.descripcionUso, COUNT(*) AS facturas_ingreso
FROM facturas f
JOIN clientes c      ON c.idCliente  = f.idCliente
JOIN catusoscfdi cat ON cat.idUsoCfdi = c.idUsoCfdi
WHERE f.idComprobante = 'I'
  AND f.fechaTimbrado >= '2026-01-01'
GROUP BY cat.clave, cat.descripcionUso
ORDER BY facturas_ingreso DESC;

SHOW CREATE PROCEDURE uspCrearTemporal;

SELECT COUNT(*) AS clientes_g02,
       SUM(CASE WHEN EXISTS (
             SELECT 1 FROM facturas f
             WHERE f.idCliente = c.idCliente
               AND f.uuidRelacionado IS NOT NULL AND f.uuidRelacionado <> ''
           ) THEN 1 ELSE 0 END) AS con_nota_credito
FROM clientes c
JOIN catusoscfdi cat ON cat.idUsoCfdi = c.idUsoCfdi
WHERE cat.clave = 'G02';


SELECT idFactura, ticket, idComprobante, uuidRelacionado, fechaTimbrado
FROM facturas WHERE idCliente = 118565 ORDER BY fechaCreacion;

