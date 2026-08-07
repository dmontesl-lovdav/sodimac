select *
from pagocomplemento
where id=6407;

select * from logfacturacion order by 1 desc;

select *
from facturas
where ticket = '6407';

select *
from facturas
where idFactura in (22051620);

select * 
-- delete
from foliofacturadet 
where idFactura = 22051620;

select *
-- delete
from foliofactura
where idFolioFactura  in (5248);


select *
-- delete
from foliofacturadet
where idFolioFactura = 5248;


select * 
-- delete
from foliofacturadet 
where idFolioFacturaDet = 9376;

select * 
-- delete
from foliofacturaimpuestos
where idFolioFacturaDet = 9376;


select * 
-- delete
from pagocomplementofoliofactura
where idFolioFactura = 5248;

select * 
-- delete
FROM pagocomplementofoliofacturadet
where idPagoComplementoFolioFactura = 5258;

select *
from pagocomplemento
where id=5847;

update pagocomplemento
set estatus='PR'
 , rfc = null
 , uuid = null
 , saldoPendiente = null
 , granTotal = null
where id=5509;

UPDATE facturas
SET estatusPago = 1
where idFactura in (22051620)
;


select * 
from pagos


select * from complementos
where ticket = '5614';

/*
 * 1D014621-BB80-40A9-9B4D-A9B1BCB2DE99   --eliminar
	2E70419B-98FA-4893-A961-01D1A78C2F71  --ok
	
update complementos 
set ticket = '5614'
where idComplemento=5391;	

update complementos 
set ticket = '-5614'
where idComplemento=5390;	
	
 * */


select *
from catconfiguracion
Where NombreCampo = 'ExpresionRegular.RazonSocial.Caracteres';

update catconfiguracion
set Valor = '^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ&-.,_: ]*$'
Where NombreCampo = 'ExpresionRegular.RazonSocial.Caracteres';


select *
from facturas 
where rfc = '66440fae764f843dcdf3fd4c973830fa'
order by 1 desc;


Select *
From clientes
Where idCliente in (159462);


	select *
	from complementos c 
	where c.idComplemento  = 5577;
	
select *
from pagocomplemento
where id=5847;

select *
from facturas 
where idFactura = 22071504;

select *
from foliofacturadet f 
where idFactura = 22071504

select *
from foliofactura f 
where idFolioFactura = 5578;

select *
from pagocomplementofoliofactura
where idFolioFactura = 5578;

 select * from complementos c  where uuid = '3D7DFD41-4764-4704-8424-85CEB0E2362C';
 select * from complementos c where c.ticket = '5847';
 
 
 select * from catusoscfdi;
 
 show create procedure uspObtenerFoliosFacturasPPD;
 show create procedure uspObtenerPagoComp;
 show create procedure uspObtenerFoliosFacturasPPD;
 show create procedure uspInsertarLogFactura;
 show create procedure uspObtenerPagosByParams;
 

SELECT 
    YEAR(fechaTimbrado) as anio,
    MONTH(fechaTimbrado) as mes,
    DATE_FORMAT(fechaTimbrado, '%Y-%m') as periodo,
    versionFacturacionSat,
    metodoPago,
    idComprobante,
    COUNT(*) as total_facturas
FROM facturas 
-- where DATE_FORMAT(fechaTimbrado, '%Y-%m-%d') >= '2025-01-01'
-- AND   DATE_FORMAT(fechaTimbrado, '%Y-%m-%d') <= '2025-12-31'
GROUP BY YEAR(fechaTimbrado), MONTH(fechaTimbrado), versionFacturacionSat, metodoPago, idComprobante
ORDER BY anio DESC, mes DESC, versionFacturacionSat, metodoPago;
 

SELECT 
     YEAR(fechaTimbrado) as anio,
     MONTH(fechaTimbrado) as mes,
     versionFacturacionSat,
     metodoPago,
    COUNT(*) as total_facturas
FROM facturas 
where DATE_FORMAT(fechaTimbrado, '%Y-%m-%d') >= '2025-01-01'
AND   DATE_FORMAT(fechaTimbrado, '%Y-%m-%d') <= '2025-12-31'
GROUP BY YEAR(fechaTimbrado), MONTH(fechaTimbrado), versionFacturacionSat, metodoPago
ORDER BY anio DESC, mes DESC, versionFacturacionSat, metodoPago;
 

SELECT 
    YEAR(fechaTimbrado) as anio,
    MONTH(fechaTimbrado) as mes,
    COUNT(*) as total_facturas
FROM facturas 
WHERE metodoPago = 'PPD'
  AND idComprobante = 'I'
  AND versionFacturacionSat = '4.0'
GROUP BY YEAR(fechaTimbrado), MONTH(fechaTimbrado)
ORDER BY anio DESC, mes DESC;

SELECT 
    YEAR(fechaTimbrado) as anio,
    MONTH(fechaTimbrado) as mes,
    COUNT(*) as total_facturas
FROM facturas 
WHERE metodoPago = 'PPD'
  AND idComprobante = 'I'
  AND versionFacturacionSat = '4.0'
  AND rfc = 'e67e2121b98b00e087b83926d479208a'
  and DATE_FORMAT(fechaTimbrado, '%Y-%m-%d') >= '2025-01-01'
  AND DATE_FORMAT(fechaTimbrado, '%Y-%m-%d') <= '2025-12-31'
GROUP BY YEAR(fechaTimbrado), MONTH(fechaTimbrado)
ORDER BY anio DESC, mes DESC;


SELECT c.idFolioFactura
     , a.idFactura
     , c.razonSocial
     , a.uuid             
     , a.serie
     , a.folio
     , a.total
     , a.subtotal
     , a.fechaTimbrado
     , 1 pagoRelacionado
     , a.rfc
     , a.ticket
     , a.estatusPago
     , c.folioFactura
     , (select count(1)
       from pagocomplementofoliofactura pc
          , pagocomplementofoliofacturadet pcd
       where pc.idPagoComplementoFolioFactura = pcd.idPagoComplementoFolioFactura
       and   pcd.idFactura = a.idFactura
       and   pc.estatus = 1) facturaAsignada
     , c.orden
from facturas a 
join clientes b on (a.idCliente = b.idCliente)
left join vw_foliofacturadet c on (a.idFactura = c.idFactura)
where a.metodoPago = 'PPD'
AND   a.idComprobante = 'I'
and   a.versionFacturacionSat = '4.0'
AND a.rfc = 'e67e2121b98b00e087b83926d479208a'
and  date_format(a.fechaTimbrado,'%Y-%m-%d') >= '2025-08-01'
and  date_format(a.fechaTimbrado,'%Y-%m-%d') <= '2025-08-31'
order by a.idFactura


if (rowsPerPage>0) then
        SET @Condition = CONCAT(@Condition,' LIMIT ',startRow,', ',rowsPerPage);
end if;
        
        SET @sql = CONCAT(@sql, @Condition);

        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;  
END



CREATE DEFINER=`dba_mysql`@`%` PROCEDURE `uspObtenerPagoComp`(
      pidTransaccionPago int
)
    READS SQL DATA
BEGIN
/*====================================================================
-- author: Ruben Martinez Tapia
-- date:   01/09/2022
-- description: Obtiene el pagoComplemento del idTransaccionPago
--====================================================================

Ejemplo:
call uspObtenerPagoComp (30);
*/

    select
      id
    , numeroCuenta
    , fechaHoraMovimiento
    , importe
    , folioBanco
    , refInterbancaria
    , folioCliente
    , tipoDivisa
    , folioOperacion
    , formaPago
	, 0 foliofactura
	from pagocomplemento 
	where id = 6025;
    -- 65506284731
    select *
    from pagocomplemento 
	where id = 6025
	union
    select * -- substr(folioCliente,1,2), count(1) as total
    from pagocomplemento
    where uuid is not null
--     and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    -- group by substr(folioCliente,1,2);
    and rfc = '1f08f26134e0aa2dbd590bac84eff374'
    order by 1 desc
    limit 10
    
    select substr(folioCliente,1,2), count(1)
    from pagocomplemento
    where uuid is null
    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) >= 18
    group by substr(folioCliente,1,2)  ;
    
    -- 22000514704
    
    select folioCliente, substr(folioCliente,3,length (folioCliente))
    from pagocomplemento
    where uuid is null
    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) <> 18
    limit 10;
    
    -- 65504504719
   
    update pagocomplemento
    set folioCliente = substr(folioCliente,3,length (folioCliente))
    where uuid is null
    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) > 18;
    
    update pagocomplemento
    set folioCliente = concat('0000000',folioCliente)
    where uuid is null
    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) <> 18
    
    -- 00000021804350015967
    -- 000021804350015967
    
    -- 00000301809000305430
    
    
    -- 65506284731
    
    update pagocomplemento
    set folioCliente = '000215400404438728'
      , numeroCuenta = '000000065506284731'
	where id = 6025

	select numeroCuenta, count(1)
    from pagocomplemento
    where uuid is null
    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (numeroCuenta) <> 18 
    group by numeroCuenta;
    limit 10;

	
	-- facturaId 1275285
END
'000215400404438728'
'000000065506284731'

update pagocomplemento
set numeroCuenta = '000000065506284731'
    where uuid is null
    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (numeroCuenta) <> 18 
    
 
select distinct numeroCuenta
from pagos
limit 10;

update pagos
set numeroCuenta = '000000065506284731'
where numeroCuenta = '65506284731'
-- 65506284731


update pagos
    set folioCliente = concat('0000000',folioCliente)
    where length (folioCliente) = 11;

select folioCliente, substr(folioCliente,3,length (folioCliente))
from pagos
where length (folioCliente) = 11
limit 10;

select distinct length(folioCliente)
from pagos
where length (folioCliente) <> 18
group by folioCliente;

select *
from pagos
where importe = 105000
and   idPago = 50831;

update pagos
set estatus = 'PL'
where importe = 105000
and   idPago = 50869;

select *
from pagos
where length (folioCliente) <> 18;

-- 6099

update pagos
set numeroCuenta = '000000065506284731'
where numeroCuenta = '65506284731'

update pagos
    set folioCliente = substr(folioCliente,3,length (folioCliente))
    where length (folioCliente) = 20;


select *
from pagos
where length (folioCliente) = 20;

update pagocomplemento
set numeroCuenta = '000000065506284731'
where numeroCuenta = '65506284731'
and   uuid is null
-- and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
-- and  length (numeroCuenta) <> 18
;

  update pagocomplemento
    set folioCliente = substr(folioCliente,3,length (folioCliente))
    where uuid is null
--    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) = 20;
  
  update pagocomplemento
    set folioCliente = concat('0000000',folioCliente)
    where uuid is null
--    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) = 11;
  
select * 
from pagocomplemento
where numeroCuenta = '65506284731'
and   uuid is null
union all
select *
from pagocomplemento
where uuid is null
and  length (folioCliente) = 20
union all 
select *
from pagocomplemento
where uuid is null
and  length (folioCliente) = 11;

-- 6951

select *
from pagocomplemento
where uuid is null
-- and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
and  length (numeroCuenta) <> 18;

select distinct  LENGTH(folioCliente)
from pagocomplemento
where uuid is null
-- and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
and  length (folioCliente) <> 18;



  update pagocomplemento
    set folioCliente = concat('00',folioCliente)
    where uuid is null
--    and  concepto in ('AB TRANSF SPEI','AB TRANS ELECT')
    and  length (folioCliente) = 16;

select *
from pagocomplemento
where id = 6099;


-- Prueba básica
CALL uspObtenerFoliosFacturasPPD('2025-07-15', '2025-11-03', 'CIA020213EN0', NULL, 0, 10);

-- Comparar tiempos
SET @inicio = NOW(6);
CALL uspObtenerFoliosFacturasPPD('2025-07-15', '2025-11-03', 'e67e2121b98b00e087b83926d479208a', NULL, 0, 500);
SELECT TIMESTAMPDIFF(MICROSECOND, @inicio, NOW(6))/1000000 as 'Tiempo Original (seg)';

SET @inicio = NOW(6);
CALL uspObtenerFoliosFacturasPPD_OPTIMIZED('2025-07-15', '2025-11-03', 'e67e2121b98b00e087b83926d479208a', NULL, 0, 500);
SELECT TIMESTAMPDIFF(MICROSECOND, @inicio, NOW(6))/1000000 as 'Tiempo Optimizado (seg)';
DROP PROCEDURE IF EXISTS uspObtenerFoliosFacturasPPD_OPTIMIZED;

DELIMITER $$

CREATE PROCEDURE uspObtenerFoliosFacturasPPD_OPTIMIZED(
    IN pfechaInicial VARCHAR(10),
    IN pfechafinal VARCHAR(10),
    IN pRfc VARCHAR(200),
    IN pFolio INT,
    IN startRow INT,
    IN rowsPerPage INT
)
READS SQL DATA
BEGIN
    DECLARE sqlQuery TEXT;
    
    -- Construir la query base
    SET sqlQuery = CONCAT(
        'SELECT c.idFolioFactura, ',
        'a.idFactura, ',
        'c.razonSocial, ',
        'a.uuid, ',
        'a.serie, ',
        'a.folio, ',
        'a.total, ',
        'a.subtotal, ',
        'a.fechaTimbrado, ',
        '1 AS pagoRelacionado, ',
        'a.rfc, ',
        'a.ticket, ',
        'a.estatusPago, ',
        'c.folioFactura, ',
        'COALESCE(pag.cantidadAsignada, 0) AS facturaAsignada, ',
        'c.orden ',
        'FROM facturas a ',
        'JOIN clientes b ON (a.idCliente = b.idCliente) ',
        'LEFT JOIN vw_foliofacturadet c ON (a.idFactura = c.idFactura) ',
        'LEFT JOIN ( ',
        '    SELECT pcd.idFactura, COUNT(1) AS cantidadAsignada ',
        '    FROM pagocomplementofoliofactura pc ',
        '    INNER JOIN pagocomplementofoliofacturadet pcd ',
        '        ON pc.idPagoComplementoFolioFactura = pcd.idPagoComplementoFolioFactura ',
        '    WHERE pc.estatus = 1 ',
        '    GROUP BY pcd.idFactura ',
        ') pag ON (a.idFactura = pag.idFactura) ',
        'WHERE a.metodoPago = ''PPD'' ',
        'AND a.idComprobante = ''I'' ',
        'AND a.versionFacturacionSat = ''4.0'' '
    );
    
    -- Agregar filtro de RFC si existe
    IF pRfc IS NOT NULL AND pRfc != '' THEN
        SET sqlQuery = CONCAT(sqlQuery, 'AND a.rfc = ''', pRfc, ''' ');
    END IF;
    
    -- Agregar filtro de Folio si existe
    IF pFolio IS NOT NULL AND pFolio > 0 THEN
        SET sqlQuery = CONCAT(sqlQuery, 'AND c.folioFactura = ', pFolio, ' ');
    END IF;
    
    -- Agregar filtros de fecha (SIN date_format para usar índices)
    SET sqlQuery = CONCAT(sqlQuery, 'AND a.fechaTimbrado >= ''', pfechaInicial, ' 00:00:00'' ');
    SET sqlQuery = CONCAT(sqlQuery, 'AND a.fechaTimbrado <= ''', pfechafinal, ' 23:59:59'' ');
    
    -- Ordenar
    SET sqlQuery = CONCAT(sqlQuery, 'ORDER BY a.idFactura ');
    
    -- Paginación
    IF rowsPerPage > 0 THEN
        SET sqlQuery = CONCAT(sqlQuery, 'LIMIT ', startRow, ', ', rowsPerPage);
    END IF;
    
    -- Ejecutar la query
    SET @finalQuery = sqlQuery;
    PREPARE stmt FROM @finalQuery;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END$$

DELIMITER ;




select * 
from catconfiguracion
where NombreCampo in ('ConsultaOrdenesCompra.FechaInicio','ConsultaOrdenesCompra.FechaFin');

update catconfiguracion
set Valor = '2026/01/01'
where NombreCampo in ('ConsultaOrdenesCompra.FechaInicio') -- ,'ConsultaOrdenesCompra.FechaFin');

update catconfiguracion
set Valor = '2027/01/01'
where NombreCampo in ('ConsultaOrdenesCompra.FechaFin') -- ,'ConsultaOrdenesCompra.FechaFin');



 SELECT p.idPago
    , p.numeroCuenta
    , p.fechaHoraMovimiento
    , p.concepto
    , p.importe
    , p.folioBanco
    , p.refInterbancaria
    , p.folioCliente
    , p.tipoDivisa
    , p.folioOperacion
    , p.formaPago
    , e.idEstatusPago
    , e.descripcion 
    FROM pagos p 
    inner join catestatuspago e on p.estatus = e.idEstatusPago
    where '2026-04-18' <= p.fechaHoraMovimiento and p.fechaHoraMovimiento <= DATE_ADD('2026-04-20', INTERVAL 1 DAY)
  -- and ('PL' = '' or ('PL' <> '' and 'PL' = e.idEstatusPago))
   --   and (48125 = 0 or (48125 <> 0 and 48125 = p.importe))
    order by p.fechaHoraMovimiento desc
    
    
    update pagos
    set estatus = 'PL'
    where idPago = 53708;
    
    select * 
    from usuarios
    -- ORDER BY apellidoPaterno ;
    where usuario = 'callcenter1@sodimac.com.mx'
    
    '3e71387783dae1eb5f1d87951ce266c5'
    
    update usuarios
    set password = '3e71387783dae1eb5f1d87951ce266c5'
    where usuario = 'callcenter1@sodimac.com.mx'
    
    