show create procedure sodimacfiscal.uspObtenerDetalleExcelByParams;

CREATE DEFINER=`dba_mysql`@`%` PROCEDURE `uspObtenerDetalleExcelByParams`(

	IN `pfechaInicial` varchar(10),

	IN `pfechafinal` varchar(10),

	IN `pticket` VARCHAR(50),

	IN `pcanal` varchar(10),

	IN `ptienda` varchar(10)

)
BEGIN

/*====================================================================

-- author: Johnatan Rafael Santiago Cigala

-- date: 02/01/2023

-- description: Obtiene el detalle de la relacion venta facturacion

--====================================================================

*/	

select * from relacionventafacturacion

where pfechaInicial <= fecha_ticket and fecha_ticket < DATE_ADD(pfechafinal, INTERVAL 1 DAY)

	AND (pticket = '' OR (pticket <> '' AND pticket = ticket))

--   AND (pcanal = '' or (pcanal <> '' and pcanal = canal))

	AND (ptienda = '' or (ptienda <> '' and ptienda = tienda))

ORDER BY fecha_ticket;



END