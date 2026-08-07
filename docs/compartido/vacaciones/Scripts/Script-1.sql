select * from vw_lista_periodo;
select * from dbo.vw_cat_periodo_lista;

select * from SODIMAC_REBATES_DEV.dbo.vw_listado_calculo_periodo;


select NumeroProveedor
     , Estado 
     , Familia 
     , ClasificacionComercial 
     , NumeroAcuerdo
     , TipoAcuerdo
     , Moneda
     , Valor
     , TipoValor
     , FillRate
     , ProgramaPago
     , Marca
     , count(1)
from RebateAcuerdosTemp
where NumeroProveedor = '252012'
group by NumeroProveedor
     , Estado 
     , Familia 
     , ClasificacionComercial 
     , NumeroAcuerdo
     , TipoAcuerdo
     , Moneda
     , Valor
     , TipoValor
     , FillRate
     , ProgramaPago
     , Marca
having count(*) > 1;


select * 
from catDocumento;

update catDocumento
set rutaDocumento = '/Test/Staging/' -- '/Staging/'
where idDocumento = 2;

select *
from CatPeriodo cp;

select *
-- delete
from controlDocumento 
where idPeriodo = 159
and   idDocumento = 2;

select count(1) from RebateAcuerdos ra 
select * from RebateAcuerdosTemp ra 

USE [SODIMAC_REBATES_DEV]
DECLARE @return_value int

EXEC	@return_value = [dbo].[spu_CargaCatalogoBase]

SELECT	'Return Value' = @return_value