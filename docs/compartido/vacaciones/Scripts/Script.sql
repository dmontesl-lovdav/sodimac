call cli_2fac_3cab_4det_5lfa_6ler ('2025062410500076968');

select *
from facturas f 
-- where f.ticket in  ('2025071411200060621')
 where fechaCreacion >= str_to_date('14/07/2025', '%d/%m/%Y')
 and  metodoPago = 'PPD'
-- limit 10
order  by 1;

select date_format(str_to_date('14/07/2025', '%d/%m/%Y'), '%Y-%m-%d') as Date from VarcharToDate;



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
        and e.ticket = '2025062410500076968'
        and  f.idEstatusFactura = 13
	and e.ticket not in (select ticket from facturas)
        /*and e.ticket in (select ticket from logerrores where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 3, ErrorMessage: <Sin Error> Datos para procesar%'
                         union 
                         select ticket from logerrores where idFacturaPac > 0 and activo = 1 and errorlog like 'EstatusId: 1, ErrorMessage: <Sin Error> Datos para procesar%') */
        and e.ticket in (select ticket from listafacturas where total > 0)
        and e.fechaCreacion > CURDATE()- INTERVAL 60 DAY
        and e.ticket not like '202208%'
        and e.ticket not like '202209%'
        order by e.fechaCreacion;

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