select count(1) from tmpIdFactura;
delete from tmpIdFactura;

select count(1) from facturas
-- update facturas set estatusenviado=0
where fechaCreacion >= str_to_date('01/07/2025', '%d/%m/%Y')
 and estatusenviado=0
and idFactura not in (select idFactura from tmpIdFactura);


select distinct a.idCliente -- count(1) -- a.idCliente
from facturas a
   , tmpIdFactura b
where a.idFactura = b.idFactura
and   a.fechaCreacion >= str_to_date('01/07/2025', '%d/%m/%Y')
-- and estatusenviado=0
and a.idFactura not in (select idFactura from tmpIdFactura);

update clientes 
set estatusenviado=0
where idCliente in (select distinct a.idCliente -- count(1) -- a.idCliente
					from facturas a
					   , tmpIdFactura b
					where a.idFactura = b.idFactura
					and   a.fechaCreacion >= str_to_date('01/07/2025', '%d/%m/%Y')
					)

					
SELECT *
FROM facturas 
WHERE ticket = '2025121110500069488';