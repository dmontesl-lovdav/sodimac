CREATE DEFINER=`root`@`localhost` PROCEDURE `uspObtenerFacturaTicket`(
  pticket varchar(19)
)
    READS SQL DATA
BEGIN
/*====================================================================
-- author: Ruben Martinez Tapia
-- date: 28/04/2021
-- description: Leer factura por ticket
--====================================================================

Ejemplo:
call uspObtenerFacturaTicket (
  '2021042810100023299'
);
*/

select * from facturas where ticket = pticket or ticketBct = pticket order by fechaCreacion limit 1;

END


CREATE DEFINER=`root`@`localhost` PROCEDURE `uspExistTempTicket`(
  pticket varchar(19)
)
    READS SQL DATA
BEGIN
/*====================================================================
-- author: Ruben Martinez Tapia
-- date: 18/05/2021
-- description: Valida si existe el ticket en la tabla temporal
--====================================================================

Ejemplo:
call uspExistTempTicket (
  '2021042810100023299'
);
*/
declare vExiste int;
set vExiste = 0;

if exists(select 1 from clientestemporal where ticket = pticket order by fechaCreacion limit 1) then
    set vExiste = 1;
end if;

select vExiste as existe;

END;

CREATE DEFINER=`facturaUser`@`%` PROCEDURE `uspTicketsCodigo77`()
    READS SQL DATA
BEGIN
/*====================================================================
-- author: Josue López Salcedo
-- date: 05/02/2020
-- description: Obtiene los tickets que no se pudieron factutar por error datos que no existen en la BCT (Codigo 77)
--              Toma todos los tipos de errores excepto rfc no activos en el Sat
--====================================================================

Ejemplo:
call uspTicketsCodigo77 ();
*/
        declare vdaysFrom int;

        select Valor into vdaysFrom  from catconfiguracion
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
        where f.idEstatusFactura = 13
        and ct.ticket not in (select ticket from facturas)
        and ct.ticket not in (select ticket from logerrores where rfc = ct.rfc and errorlog like '%Este RFC del receptor no existe en la lista de RFC inscritos%' and activo = 1 and fechaCreacion > CURDATE()- INTERVAL vdaysFrom DAY)
        and ct.ticket not in (select ticket from logerrores where rfc = ct.rfc and errorlog like '%Sin Error> Datos para procesar%' and activo = 1 and fechaCreacion > CURDATE()- INTERVAL vdaysFrom DAY)
        and ct.ticket in (select ticket from listafacturas where total > 0)
        and ct.fechaCreacion > CURDATE()- INTERVAL vdaysFrom DAY
        HAVING (        select count(*)
                    from refacturacionBatch a inner join refacturacionDetail b on a.refacturacionId = b.refacturacionId 
                    where a.rfc = ct.rfc and a.ticket = ct.ticket and activo = 1) < 4
        order by ct.fechaCreacion;

END


CREATE DEFINER=`root`@`localhost` PROCEDURE `uspFacturasPacPendientes`()
    READS SQL DATA
BEGIN
/*====================================================================
-- author: Ruben Martinez Tapia
-- date: 04/02/2020
-- description: Obtiene las facturas generadas en el Pac pendientes
--====================================================================

Ejemplo:
call uspFacturasPacPendientes ();
*/
        declare vdaysFrom int;

        select Valor into vdaysFrom  from catconfiguracion
        where NombreCampo = 'batch.intervalo';
        
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
        and e.fechaCreacion > CURDATE()- INTERVAL vdaysFrom DAY
        and e.ticket not like '202208%'
        and e.ticket not like '202209%'
        order by e.fechaCreacion;


END


CREATE DEFINER=`facturaUser`@`%` PROCEDURE `uspExistTicketEnProceso`(
      pticket varchar(19)
)
    READS SQL DATA
BEGIN
declare vExiste int;
set vExiste = 0;
if exists(select 1 from clientesTemporal ct inner join logfacturacion f on ct.rfc = f.rfc and ct.ticket = f.ticket
        where ct.ticket = pticket and ((f.idEstatusFactura = 9 and f.fechaCreacion > CURRENT_DATE) or f.idEstatusFactura = 13)) then
    set vExiste = 1;
end if;

select vExiste as existe;

END