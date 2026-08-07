-- uspObtenerFacturasTicket
-- uspInsertaBitacoraActividades
-- uspRegistraControlPacTicket

select * from BITACORA_ACTIVIDADES order by 1 desc;
select * from [dbo].[CONTROL_PAC_TICKET] where REGION like '2025%' order by 1 desc;
select * from [dbo].[TIMBRADO_PAC_DETECNO] a where REGION like '2024%' order by 1 desc;
select * from [dbo].[CONTROL_PAC_SERIE] order by 1 desc;

		select count(1) --a0.FACTURA_ID, a0.REGION, a0.UUID, a0.SERIE, a0.FOLIO  -- 11,296
		from TIMBRADO_PAC_DETECNO a0
		where a0.FACTURA_ID in (

			select  a.FACTURA_ID
			from [dbo].[TIMBRADO_PAC_DETECNO] a
			where 1 = 1
			and   a.REGION in (select d.TICKET
							   from [dbo].[FACTURACION_CLIENTE] d
							   where d.UUID is null
							   and   isNull(d.TIENDA,'') = '')
			and   a.REGION not in (select c.REGION 
								   FROM [dbo].[CONTROL_PAC_TICKET] c
								   WHERE c.ESTATUS IN (1,3))

			and   a.REGION not in (select c.REGION 
								   FROM [dbo].[CONTROL_PAC_TICKET] c
								   WHERE  1=1 --ESTATUS = 0
								   group by c.REGION
								   HAVING COUNT(c.REGION) >= 5
								   )

			AND   a.FACTURA_ID = (select MAX(b.FACTURA_ID)
								  FROM [dbo].[TIMBRADO_PAC_DETECNO] b
								  WHERE b.REGION = a.REGION
								  AND   isNull(b.UUID,'') <> ''
								)
		     --AND a.REGION in ('2023051130000026349')
		)
			
		