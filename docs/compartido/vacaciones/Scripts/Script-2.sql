select * from vw_lista_periodo;
select * from dbo.vw_cat_periodo_lista;

select * from CatPeriodo cp;

SELECT *
   FROM RebateAcuerdosTemp; 

select *
FROM controlDocumento  
WHERE idEstatusArchivo=0  
AND activo = 1  
AND idDocumento = (   
                 SELECT idDocumento   
     FROM catDocumento   
     WHERE nombreDocumento='Catálogo proveedores'   
     AND activo=1  
      );  





ALTER VIEW [dbo].[vw_cat_periodo_lista]
    AS
	SELECT A.IdCatPeriodo
	FROM CatPeriodo A
	INNER JOIN [dbo].[RelPeriodoTipoRebate] B
	ON A.IdCatPeriodo = B.IdCatPeriodo
	AND B.Activo = 1
    AND A.Activo = 1
	AND A.Estatus IN (3,6,8)
	AND B.IdCatTipoRebate = 8
	AND A.FechaIni >= CONVERT(DATETIME, '2025-01-01', 103);


select * from dbo.vw_listado_calculo_periodo
drop view vw_listado_calculo_periodo;

CREATE view [dbo].[vw_listado_calculo_periodo] as
SELECT 
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'CarpetaEntrada')) AS CarpetaEntrada,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'CarpetaProceso')) AS CarpetaProceso,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'CarpetaSalida'))  AS CarpetaSalida,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'GeneraArchivos')) AS GeneraArchivos,
	FechaIni,
	FechaFin,
	ProgramaPago,
	NumeroMeses,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'IpFtp')) AS IpFtp,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'PuertoFtp')) AS PuertoFtp,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'UserFtp')) AS UserFtp,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'CarpetaEntradaFtp')) AS CarpetaEntradaFtp,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'CarpetaSalidaFtp')) AS CarpetaSalidaFtp,
	CONVERT(VARCHAR(200),(SELECT Valor FROM CatConfiguracion WHERE NombreVariable = 'PasswordFtp')) AS PasswordFtp
	,SUBSTRING( CONVERT(VARCHAR, CONVERT(DATE,FechaIni)), 9,2) +'/'+
	SUBSTRING( CONVERT(VARCHAR, CONVERT(DATE,FechaIni)), 6,2) +'/'+
	SUBSTRING( CONVERT(VARCHAR, CONVERT(DATE,FechaIni)), 1,4) FechaIniOracle,
	SUBSTRING( CONVERT(VARCHAR, CONVERT(DATE,FechaFin)), 9,2) +'/'+
	SUBSTRING( CONVERT(VARCHAR, CONVERT(DATE,FechaFin)), 6,2) +'/'+
	SUBSTRING( CONVERT(VARCHAR, CONVERT(DATE,FechaFin)), 1,4) FechaFinOracle,
	IdCatPeriodo
FROM (
    
	SELECT C.IdCatPeriodo,C.FechaIni,C.FechaFin,P.ProgramaPago,P.NumeroMeses,1 CONTADOR
	FROM CatPeriodo c
	INNER JOIN CatProgramaPago p ON c.IdCatProgramaPago = p.IdCatProgramaPago
	WHERE c.Estatus = 1 AND c.Activo = 1
	
	UNION 

	SELECT C.IdCatPeriodo,C.FechaIni,C.FechaFin,P.ProgramaPago,P.NumeroMeses,COUNT(1) CONTADOR
	FROM CatPeriodo c
	INNER JOIN CatProgramaPago p ON c.IdCatProgramaPago = p.IdCatProgramaPago
	INNER JOIN RelPeriodoTipoRebate RPT ON RPT.IdCatPeriodo = C.IdCatPeriodo
	WHERE c.Estatus IN (3,0) 
	AND c.Activo = 1
	AND RPT.Activo = 1
	AND (DATEPART(WEEKDAY, GETDATE()))=ISNULL((SELECT Valor FROM CatConfiguracion WHERE NombreVariable='DiaRecalculoRebate'),1)
	AND (CASE WHEN (CAST(GETDATE() AS TIME) BETWEEN (SELECT Valor FROM CatConfiguracion WHERE NombreVariable='CalculoRebates.HoraInicial') AND (SELECT Valor FROM CatConfiguracion WHERE NombreVariable='CalculoRebates.HoraFinal')) THEN 1 ELSE 0 END) = 1
	AND RPT.IdCatTipoRebate IN (
		select Valor from CatConfiguracion WHERE NombreVariable='RecalculoRebate'
	)
	AND (
		C.IdUsuarioModifEstatus NOT IN (
			SELECT Y.idusuario
			FROM CatPerfil W
			INNER JOIN CatUsuarioPerfil Y
			ON W.id = Y.idperfil
			WHERE W.id = 1
			AND W.activo = 1
		) OR C.IdUsuarioModifEstatus IS NULL
	)
    GROUP BY C.IdCatPeriodo,C.FechaIni,C.FechaFin,P.ProgramaPago,P.NumeroMeses
) A;


--exec [dbo].[spu_CargaRebate] 1 INT,161

SELECT COUNT(1)
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo        = 1
AND A.Estatus         IN (0,1,2,4)
AND B.Activo          = 1  -- Rebate Seleccionado por el usuario
AND B.IdCatTipoRebate = 8
AND B.IdCatPeriodo  != 118
AND A.FechaIni       >= CONVERT(DATE,'01/01/2025',103)
AND A.IdPerfil       = 1
AND A.FechaHoraRespaldo IS NULL
AND EXISTS (
   SELECT 1
   FROM controlDocumento X
   WHERE X.idPeriodo = A.IdCatPeriodo
   AND X.idDocumento IN (1,12)
);

SELECT COUNT(DISTINCT A.IdCatPeriodo)
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo        = 1
AND A.Estatus         IN (2,3)
AND B.Activo          = 1  -- Rebate Seleccionado por el usuario


SELECT DISTINCT B.IdCatPeriodo  --161
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo        = 1
AND A.Estatus         IN (2,3)
AND B.Activo          = 1  -- Rebate Seleccionado por el usuario

SELECT 
		DISTINCT  B.IdCatTipoRebate  --1 
		FROM CatPeriodo A
		INNER JOIN RelPeriodoTipoRebate B
		ON A.IdCatPeriodo = B.IdCatPeriodo
		WHERE A.Activo        = 1
		AND A.Estatus         IN (2,3)
		AND B.Activo          = 1  -- Rebate Seleccionado por el usuario
		AND A.IdCatPeriodo    = 161;

select * from CatTipoRebate ctr 


SELECT COUNT(1)
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo = 1
AND A.Estatus IN (2,3)
AND A.IdCatPeriodo = 161
AND B.IdCatTipoRebate = 1
AND B.Activo = 1  -- Rebate Seleccionado por el usuario

SELECT TipoCambio -- 19.57
FROM RebateCambioMoneda
WHERE FechaCambio = (select MAX(FechaCambio) from RebateCambioMoneda);

SELECT 
  /*@v_FechaInicioPeriodo = */A.FechaIni
, /*@v_FechaFinalPeriodo  = */A.FechaFin
, /*@v_IdCatPrograma    = */A.IdCatProgramaPago
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo        = 1
AND A.Estatus         IN (2,3)
AND A.IdCatPeriodo    = 161
AND B.IdCatTipoRebate = 1
AND B.Activo = 1  -- Rebate Seleccionado por el usuario



select
	rebateusua0_.RowNumber as rownumbe1_47_,
	rebateusua0_.CodigoProveedor as codigopr2_47_,
	rebateusua0_.DescripcionProducto as descripc3_47_,
	rebateusua0_.Exclusion as exclusio4_47_,
	rebateusua0_.Familia as familia5_47_,
	rebateusua0_.FechaEmision as fechaemi6_47_,
	rebateusua0_.FechaExclusion as fechaexc7_47_,
	rebateusua0_.FechaRecepcion as fecharec8_47_,
	rebateusua0_.GerenteNegocio as gerenten9_47_,
	rebateusua0_.IdCatProgramaPago as idcatpr10_47_,
	rebateusua0_.IdExclusion as idexclu11_47_,
	rebateusua0_.IdTipoRebate as idtipor12_47_,
	rebateusua0_.IEPS as ieps13_47_,
	rebateusua0_.IVA as iva14_47_,
	rebateusua0_.Marca as marca15_47_,
	rebateusua0_.Moneda as moneda16_47_,
	rebateusua0_.Monedas as monedas17_47_,
	rebateusua0_.MontoDescuento as montode18_47_,
	rebateusua0_.MontoIeps as montoie19_47_,
	rebateusua0_.MontoIva as montoiv20_47_,
	rebateusua0_.MontoRebate as montore21_47_,
	rebateusua0_.MontoRecibido as montore22_47_,
	rebateusua0_.NombreFamilia as nombref23_47_,
	rebateusua0_.NombreGerente as nombreg24_47_,
	rebateusua0_.NombreJefeLinea as nombrej25_47_,
	rebateusua0_.NumeroJefeLinea as numeroj26_47_,
	rebateusua0_.OrdenCompra as ordenco27_47_,
	rebateusua0_.Origen as origen28_47_,
	rebateusua0_.IdCatPeriodo as idcatpe29_47_,
	rebateusua0_.ProgramaPago as program30_47_,
	rebateusua0_.Proveedor as proveed31_47_,
	rebateusua0_.RFC as rfc32_47_,
	rebateusua0_.SKU as sku33_47_,
	rebateusua0_.TipoAcuerdo as tipoacu34_47_,
	rebateusua0_.TipoCambio as tipocam35_47_,
	rebateusua0_.TipoOrdenCompra as tipoord36_47_,
	rebateusua0_.TipoValor as tipoval37_47_,
	rebateusua0_.Valor as valor38_47_
from
	vw_reporte_usuario rebateusua0_
where
	rebateusua0_.IdCatPeriodo =160;


select
	TOP(?) rebateusua0_.RowNumber as rownumbe1_47_,
	rebateusua0_.CodigoProveedor as codigopr2_47_,
	rebateusua0_.DescripcionProducto as descripc3_47_,
	rebateusua0_.Exclusion as exclusio4_47_,
	rebateusua0_.Familia as familia5_47_,
	rebateusua0_.FechaEmision as fechaemi6_47_,
	rebateusua0_.FechaExclusion as fechaexc7_47_,
	rebateusua0_.FechaRecepcion as fecharec8_47_,
	rebateusua0_.GerenteNegocio as gerenten9_47_,
	rebateusua0_.IdCatProgramaPago as idcatpr10_47_,
	rebateusua0_.IdExclusion as idexclu11_47_,
	rebateusua0_.IdTipoRebate as idtipor12_47_,
	rebateusua0_.IEPS as ieps13_47_,
	rebateusua0_.IVA as iva14_47_,
	rebateusua0_.Marca as marca15_47_,
	rebateusua0_.Moneda as moneda16_47_,
	rebateusua0_.Monedas as monedas17_47_,
	rebateusua0_.MontoDescuento as montode18_47_,
	rebateusua0_.MontoIeps as montoie19_47_,
	rebateusua0_.MontoIva as montoiv20_47_,
	rebateusua0_.MontoRebate as montore21_47_,
	rebateusua0_.MontoRecibido as montore22_47_,
	rebateusua0_.NombreFamilia as nombref23_47_,
	rebateusua0_.NombreGerente as nombreg24_47_,
	rebateusua0_.NombreJefeLinea as nombrej25_47_,
	rebateusua0_.NumeroJefeLinea as numeroj26_47_,
	rebateusua0_.OrdenCompra as ordenco27_47_,
	rebateusua0_.Origen as origen28_47_,
	rebateusua0_.IdCatPeriodo as idcatpe29_47_,
	rebateusua0_.ProgramaPago as program30_47_,
	rebateusua0_.Proveedor as proveed31_47_,
	rebateusua0_.RFC as rfc32_47_,
	rebateusua0_.SKU as sku33_47_,
	rebateusua0_.TipoAcuerdo as tipoacu34_47_,
	rebateusua0_.TipoCambio as tipocam35_47_,
	rebateusua0_.TipoOrdenCompra as tipoord36_47_,
	rebateusua0_.TipoValor as tipoval37_47_,
	rebateusua0_.Valor as valor38_47_
from
	vw_reporte_usuario rebateusua0_
where
	rebateusua0_.IdCatPeriodo =?;

select * from SODIMAC_REBATES_PROD.dbo.vw_reporte_usuario_fillrate
where IdCatPeriodo = 160;;

select * from usuario u where nombre like '%Diana%';

select
	periodo0_.idCatPeriodo as idcatper1_17_,
	periodo0_.activo as activo2_17_,
	periodo0_.detallePeriodo as detallep3_17_,
	periodo0_.estatus as estatus4_17_,
	periodo0_.fechaCalculo as fechacal5_17_,
	periodo0_.fechaEnvio as fechaenv6_17_,
	periodo0_.fechaFin as fechafin7_17_,
	periodo0_.fechaHoraCierre as fechahor8_17_,
	periodo0_.fechaHoraCreacion as fechahor9_17_,
	periodo0_.fechaHoraModifEstatus as fechaho10_17_,
	periodo0_.fechaHoraModificacion as fechaho11_17_,
	periodo0_.fechaHoraRespaldo as fechaho12_17_,
	periodo0_.fechaIni as fechain13_17_,
	periodo0_.IdPerfil as idperfi14_17_,
	periodo0_.idUsuarioCreacion as idusuar15_17_,
	periodo0_.idUsuarioModifEstatus as idusuar16_17_,
	periodo0_.idUsuarioModificacion as idusuar17_17_,
	periodo0_.Orden as orden18_17_,
	periodo0_.idCatProgramaPago as idcatpr19_17_
from
	CatPeriodo periodo0_
where
	periodo0_.activo =1
	and (periodo0_.estatus = 0
		or periodo0_.estatus = 3)
order by
	periodo0_.Orden asc,
	periodo0_.idCatPeriodo desc

select
	periodo0_.idCatPeriodo as idcatper1_17_0_,
	periodo0_.activo as activo2_17_0_,
	periodo0_.detallePeriodo as detallep3_17_0_,
	periodo0_.estatus as estatus4_17_0_,
	periodo0_.fechaCalculo as fechacal5_17_0_,
	periodo0_.fechaEnvio as fechaenv6_17_0_,
	periodo0_.fechaFin as fechafin7_17_0_,
	periodo0_.fechaHoraCierre as fechahor8_17_0_,
	periodo0_.fechaHoraCreacion as fechahor9_17_0_,
	periodo0_.fechaHoraModifEstatus as fechaho10_17_0_,
	periodo0_.fechaHoraModificacion as fechaho11_17_0_,
	periodo0_.fechaHoraRespaldo as fechaho12_17_0_,
	periodo0_.fechaIni as fechain13_17_0_,
	periodo0_.IdPerfil as idperfi14_17_0_,
	periodo0_.idUsuarioCreacion as idusuar15_17_0_,
	periodo0_.idUsuarioModifEstatus as idusuar16_17_0_,
	periodo0_.idUsuarioModificacion as idusuar17_17_0_,
	periodo0_.Orden as orden18_17_0_,
	periodo0_.idCatProgramaPago as idcatpr19_17_0_,
	programapa1_.idCatProgramaPago as idcatpro1_19_1_,
	programapa1_.activo as activo2_19_1_,
	programapa1_.nomenclatura as nomencla3_19_1_,
	programapa1_.numeroMeses as numerome4_19_1_,
	programapa1_.programaPago as programa5_19_1_
from
	CatPeriodo periodo0_
left outer join CatProgramaPago programapa1_ on
	periodo0_.idCatProgramaPago = programapa1_.idCatProgramaPago
where
	periodo0_.idCatPeriodo =?;

select * from CatPeriodo order by 1;
select * from CatProgramaPago order by;


SELECT * 
FROM ControlDocumento c WHERE c.activo = true AND ( EXISTS(SELECT * FROM Periodo p WHERE c.periodo.idCatPeriodo = p.idCatPeriodo AND p.estatus <> 6) OR c.documento.idDocumento = 14

SELECT * 
-- delete
FROM ControlDocumento
-- where idCarga = 2044
order by idPeriodo ;

--Envios_Ap_Temp

select * from usuario;

insert into usuario values (37,'dlhernandezv@Sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMO7NDeLMkz1dQCWSGHi3wUU=','Diana Lucia','Hernandez','Varela',getdate(),null,1,0);
insert into usuario values (38,'jlgomezg@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwML7x7yMt0K78OflbivVz/i4=','Jose Luis','Gomez','Guzman',getdate(),null,1,0);
insert into usuario values (52,'jvalgredom@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwML7x7yMt0K78OflbivVz/i4=','Jose Luis','Gomez','Guzman',getdate(),null,1,0);

delete from usuario where id=39;

update usuario
set pass = 'MDAwMDAwMDAwMDAwMDAwMDLIbVoPrCyjCQQ6VB54Sso='
where id=39;

insert into usuario values (39,'dderonc@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMDLIbVoPrCyjCQQ6VB54Sso=','Daniela Del Pilar','Ceron',null,getdate(),null,1,0);
insert into usuario values (40,'erevillag@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMHzT0NUw6W7sQHUvmGHBSuy3OUserGlvtERgVyH4oGYg','Eduardo', 'Revilla', 'Garcia',getdate(),null,1,0);
insert into usuario values (41,'fagarciar@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMJ+U2A0ynJKIKWLOR4UytWM=','Fabio Alan', 'Garcia', 'Rodriguez',getdate(),null,1,0);
insert into usuario values (42,'mbrito@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMH9joiP8n+g0qSRvlLx4vGc=','Mariana', 'Brito', 'Altamirano',getdate(),null,1,0);
insert into usuario values (43,'meramirezcr@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMHIQ+RVedmo33QEZTuEIk78=','Melissa', 'Ramírez', 'Cruz',getdate(),null,1,0);
insert into usuario values (44,'zaguirrec@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMAOW4UCnp81f7jGlbc2F3Zg=','Xaviera', 'Aguirre', 'Chavez',getdate(),null,1,0);
insert into usuario values (45,'camarroquin@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMG+hkmTWBlZjbGoNwqzWDnM=','Carlos Alberto', 'Marroquin', 'Arciniegas',getdate(),null,1,0);
insert into usuario values (46,'frjimenez@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMHEnB0gjO/h2uHKArgLyqM4=','Francisco Ramon', 'Jimenez', 'Diaz', getdate(),null,1,0);

-- delete from usuario where id=47;
-- delete from dbo.CatUsuarioPerfil where idusuario=47;
insert into usuario values (47,'g_dco018@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMOouTHczB6tek7OpJNENjRU=','Enrique Eulogio', 'Peña', 'Sanchez', getdate(),null,1,0);
insert into usuario values (48,'g_dco018@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMOouTHczB6tek7OpJNENjRU=','Luis Eduardo', 'Moreno', 'Cruz', getdate(),null,1,0);
insert into usuario values (52,'g_dco018@sodimac.com.mx','MDAwMDAwMDAwMDAwMDAwMOouTHczB6tek7OpJNENjRU=','Jessica Vanessa', 'Algredo', 'Martinez', getdate(),null,1,0);

-- Y3MEJJK6
-- jvalgredom@sodimac.com.mx
update usuario set usuario ='jvalgredom@sodimac.com.mx'  where id = 52;

update usuario set pass ='MDAwMDAwMDAwMDAwMDAwML7x7yMt0K78OflbivVz/i4=' where id=38;

select * from CatUsuarioPerfil where idperfil  = 1;



-- jlgomezg@sodimac.com.mx 


select * from CatPerfil;
select * from CatRol;

select * from RelRolUsuario where idUsuario in (2,33,34);
select * from CatUsuarioPerfil where idUsuario in (37);

--20r3B4t3sS41d
insert into CatUsuarioPerfil values (37,1,getdate(),null);
insert into CatUsuarioPerfil values (38,1,getdate(),null);

insert into CatUsuarioPerfil values (39,11,getdate(),null);
insert into CatUsuarioPerfil values (40,11,getdate(),null);
insert into CatUsuarioPerfil values (41,11,getdate(),null);
insert into CatUsuarioPerfil values (42,11,getdate(),null);
insert into CatUsuarioPerfil values (43,11,getdate(),null);
insert into CatUsuarioPerfil values (44,11,getdate(),null);
insert into CatUsuarioPerfil values (47,11,getdate(),null);

insert into CatUsuarioPerfil values (45,2,getdate(),null);
insert into CatUsuarioPerfil values (46,2,getdate(),null);
insert into CatUsuarioPerfil values (47,11,getdate(),null);
insert into CatUsuarioPerfil values (48,11,getdate(),null);
insert into CatUsuarioPerfil values (52,1,getdate(),null);


select TOP(?) rebateusua0_.RowNumber as rownumbe1_47_, rebateusua0_.CodigoProveedor as codigopr2_47_, rebateusua0_.DescripcionProducto as descripc3_47_, rebateusua0_.Exclusion as exclusio4_47_, rebateusua0_.Familia as familia5_47_, rebateusua0_.FechaEmision as fechaemi6_47_, rebateusua0_.FechaExclusion as fechaexc7_47_, rebateusua0_.FechaRecepcion as fecharec8_47_, rebateusua0_.GerenteNegocio as gerenten9_47_, rebateusua0_.IdCatProgramaPago as idcatpr10_47_, rebateusua0_.IdExclusion as idexclu11_47_, rebateusua0_.IdTipoRebate as idtipor12_47_, rebateusua0_.IEPS as ieps13_47_, rebateusua0_.IVA as iva14_47_, rebateusua0_.Marca as marca15_47_, rebateusua0_.Moneda as moneda16_47_, rebateusua0_.Monedas as monedas17_47_, rebateusua0_.MontoDescuento as montode18_47_, rebateusua0_.MontoIeps as montoie19_47_, rebateusua0_.MontoIva as montoiv20_47_, rebateusua0_.MontoRebate as montore21_47_, rebateusua0_.MontoRecibido as montore22_47_, rebateusua0_.NombreFamilia as nombref23_47_, rebateusua0_.NombreGerente as nombreg24_47_, rebateusua0_.NombreJefeLinea as nombrej25_47_, rebateusua0_.NumeroJefeLinea as numeroj26_47_, rebateusua0_.OrdenCompra as ordenco27_47_, rebateusua0_.Origen as origen28_47_, rebateusua0_.IdCatPeriodo as idcatpe29_47_, rebateusua0_.ProgramaPago as program30_47_, rebateusua0_.Proveedor as proveed31_47_, rebateusua0_.RFC as rfc32_47_, rebateusua0_.SKU as sku33_47_, rebateusua0_.TipoAcuerdo as tipoacu34_47_, rebateusua0_.TipoCambio as tipocam35_47_, rebateusua0_.TipoOrdenCompra as tipoord36_47_, rebateusua0_.TipoValor as tipoval37_47_, rebateusua0_.Valor as valor38_47_ 
from vw_reporte_usuario rebateusua0_ 
where rebateusua0_.FechaRecepcion>=? 
and rebateusua0_.FechaRecepcion<=?;




SELECT ROW_NUMBER() OVER(ORDER BY [FechaRecepcion] DESC) AS RowNumber, [Origen]
      ,[Moneda]
      ,[RFC]
      ,[CodigoProveedor]
      ,[Proveedor]
      ,[GerenteNegocio]
      ,[NombreGerente]
      ,[NumeroJefeLinea]
      ,[NombreJefeLinea]
      ,[Familia]
      ,[NombreFamilia]
      ,[MontoRecibido]
      ,[OrdenCompra]
      ,[FechaEmision]
      ,[FechaRecepcion]
      ,[TipoAcuerdo]
      ,[Monedas]
      ,[Valor]
      ,[TipoValor]
      ,[MontoRebate]
      ,[IdCatProgramaPago]
      ,[ProgramaPago]
      ,[TipoOrdenCompra]
      ,[SKU]
      ,[DescripcionProducto]
      ,[IVA]
      ,[IEPS]
      ,[MontoIva]
      ,[MontoIeps]
      ,[MontoDescuento]
      ,[TipoCambio]
      ,[IdCatPeriodo]
      ,[IdTipoRebate]
      ,[Exclusion]
      ,[FechaExclusion]
      ,[IdExclusion]
      ,[Marca]
FROM (
	
	SELECT top 10 [Origen]
      ,[Moneda]
      ,[RFC]
      ,[CodigoProveedor]
      ,[Proveedor]
      ,[GerenteNegocio]
      ,[NombreGerente]
      ,[NumeroJefeLinea]
      ,[NombreJefeLinea]
      ,[Familia]
      ,[NombreFamilia]
      ,[MontoRecibido]
      ,[OrdenCompra]
      ,[FechaEmision]
      ,convert(datetime, [FechaRecepcion], 103) AS [FechaRecepcion]
      ,[TipoAcuerdo]
      ,[Monedas]
      ,[Valor]
      ,[TipoValor]
      ,[MontoRebate]
      ,[IdCatProgramaPago]
      ,[ProgramaPago]
      ,[TipoOrdenCompra]
      ,[SKU]
      ,[DescripcionProducto]
      ,[IVA]
      ,[IEPS]
      ,[MontoIva]
      ,[MontoIeps]
      ,[MontoDescuento]
      ,[TipoCambio]
      ,[IdCatPeriodo]
      ,[IdTipoRebate]
      ,[Exclusion]
      ,[FechaExclusion]
      ,[IdExclusion]
      ,[Marca]
	FROM [dbo].[CalculoRebate]
	where convert(datetime, [FechaRecepcion], 103) >= convert(datetime,'2025-06-01',120) 
	
	select max(convert(datetime, [FechaRecepcion], 103)) FROM [dbo].[CalculoRebate]
	
	UNION
	SELECT [Origen]
      ,[Moneda]
      ,[RFC]
      ,[CodigoProveedor]
      ,[Proveedor]
      ,[GerenteNegocio]
      ,[NombreGerente]
      ,[NumeroJefeLinea]
      ,[NombreJefeLinea]
      ,[Familia]
      ,[NombreFamilia]
      ,[MontoRecibido]
      ,[OrdenCompra]
      ,[FechaEmision]
      ,convert(datetime, [FechaRecepcion], 103) AS [FechaRecepcion]
      ,[TipoAcuerdo]
      ,[Monedas]
      ,[Valor]
      ,[TipoValor]
      ,[MontoRebate]
      ,[IdCatProgramaPago]
      ,[ProgramaPago]
      ,[TipoOrdenCompra]
      ,[SKU]
      ,[DescripcionProducto]
      ,[IVA]
      ,[IEPS]
      ,[MontoIva]
      ,[MontoIeps]
      ,[MontoDescuento]
      ,[TipoCambio]
      ,[IdCatPeriodo]
      ,[IdTipoRebate]
      ,[Exclusion]
      ,[FechaExclusion]
      ,[IdExclusion]
      ,[Marca]
	  FROM [dbo].[CalculoRebateTemp]
	  
	  	select max(convert(datetime, [FechaRecepcion], 103)) FROM [dbo].[CalculoRebateTemp];
	  	
	  	select * from CatPerfil;
	  	select * from CatEvento;
	  	select * from CatRol;
	  	select * from CatPermiso; -- 8 [REBATES_USUARIO_FILLRATE_INDEX]
	  	select * from CatPerfilRol;
	  	select * from CatRolPermiso;
	  	
	  	select * from usuario where usuario = 'kayalao@sodimac.com.mx'; -- id=23
		select * from CatUsuarioPerfil cup where idusuario =23; -- Perfil -- 11
		select * from CatPerfilRol cpr where cpr.idperfil = 23;
	  	select * from CatRol cr where cr.id in (2,4,6);
	  	select * from perfilesExclusionAutorizado;
	  	
	  	-- Rol [2,4,6]
	  	/*
	  	 * 	2	Especialista en Cuentas por Pagar
			4	Gestión de la Cadena de Suministro
			6	Auditoria
	  	 * */
	  	
select * 
FROM CatPermiso cp
   , CatRolPermiso crp 
where cp.idpermiso = crp.idpermiso
and   cp.idpermiso = 8;

select a.id, a.idPerfil, cp.nombre, cte.IdCatTipoExclusion, cte.Descripcion
from perfilesExclusionAutorizado a
   , CatPerfil cp 
   , CatTipoExclusion cte 
where a.idPerfil = cp.id
and   a.idTipoExclusion = cte.IdCatTipoExclusion;


select IdExclusion , IdUsuarioSolicitud, IdUsuarioAutorizacion   from Exclusion e 

	  	
select cr.id, cr.nombre, cp.idpermiso, cp.nombre
from CatRolPermiso crp
   , CatRol cr 
   , CatPermiso cp 
where crp.idrol = cr.id
and   crp.idpermiso = cp.idpermiso
order by cr.id, crp.idpermiso;

insert into CatRol values (12, 'Reportes',1,1,1,getdate(),null,1,null);

select * from CatRolPermiso;
select * from CatPermiso where nombre in ('REBASTES_MSI_INDEX','REBATES_MSI3_INDEX','REBATES_USUARIO_FILLRATE_INDEX','REBATES_USUARIO_INDEX');

insert into CatRolPermiso values (12,6);
insert into CatRolPermiso values (12,7);
insert into CatRolPermiso values (12,8);
insert into CatRolPermiso values (12,13);

select * from CatPerfilRol cpr where idperfil =11;

insert into CatPerfilRol values (11,12,getdate(), null);
select * from Exclusion e;

select a.id as idUsuario
     , a.usuario
     , a.nombre
     , a.apellidoPaterno
     , a.apellidoMaterno
  	 , cp.id as idPerfil
  	 , cp.nombre as nombrePerfil
  	 , cr.id as idrol 
  	 , cr.nombre as nombreRol
FROM usuario a
    left join CatUsuarioPerfil cup on (a.id = cup.idusuario)
    left join CatPerfil cp on (cp.id = cup.idperfil )
    left join CatPerfilRol cpr on (cpr.idperfil = cup.idperfil )
    left join CatRol cr on (cr.id = cpr.idrol )
    
   select * from CatPermiso where nombre = 'EXC_AUTORIZAR';
   
   insert into CatPermiso values (15,'EXC_AUTORIZAR','EXC_AUTORIZAR',1,1,null, getdate(), null);
   
INSERT INTO SODIMAC_REBATES_PROD.dbo.CatPermiso
(nombre, grantedAuthority, activo, usuarioCreacion, usuarioActualizacion, fechaCreacion, fechaActualizacion)
VALUES('EXC_AUTORIZAR', 'EXC_AUTORIZAR', 1, 1, NULL, getdate(), NULL);

select * from CatRolPermiso where idrol = 2;

insert into CatRolPermiso values (2,15);

select u.id, u.usuario, u.nombre, u.apellidoPaterno, u.apellidoMaterno, cp.nombre as perfil
FROM usuario u 
  , CatPerfil cp 
  , CatUsuarioPerfil cup 
where cup.idperfil = cp.id
and   cup.idusuario = u.id
and   u.id >= 39;


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
-- delete
from RebateAcuerdosTemp
where NumeroProveedor = '252012'
and   Estado = 'Ingresado'
and   Familia = '0427'
and   ClasificacionComercial = 'Clacom Completa'
and   NumeroAcuerdo = '785'
and   TipoAcuerdo = 'Rebate Fijo'
and   Moneda = 'MXN'
and   Valor = '0.0200'
and   TipoValor = '%'
and   FillRate = '0.00'
and   ProgramaPago = 'Mensual'
-- and   IdRebateAcuerdos = 23228
and   Marca is null;


select * from RebateAcuerdosTemp;

select * from SODIMAC_REBATES_PROD.dbo.usuario;

select * 
from RebateAcuerdos
order by 1 desc; 

select cp.nombre, cte.Descripcion
from perfilesExclusionAutorizado a
 , CatPerfil cp 
 , CatTipoExclusion cte
where a.idTipoExclusion = cte.idCatTipoExclusion
and   a.idPerfil = cp.id


select  CAST(NUM_OC AS varchar), min(FechaRecepcion) 
from vw_rebate_orden_compra 
where CAST(NUM_OC AS varchar) in ('380754','381636','378815','375737','381288')
group by  CAST(NUM_OC AS varchar);


select * from Exclusion e where Folio = '000144';
select * from ExclusionCarga ec where IdExclusion = 233;
select * from ExclusionCargaDet ecd where IdExclusionCarga in (1213,1214,1215,1216,1217);



select top 100 *
from view_calculo_rebate_portal;



SELECT COUNT(*) 
FROM [SODIMAC_SAP_PROD].[dbo].[Envios_Ap];

EXEC sp_help 'Envios_Ap_Manual';

SELECT * FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0;

SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Envios_Ap_Temp'
ORDER BY ORDINAL_POSITION

-- 2. Ver si existe algún trigger en la tabla
SELECT 
    name AS TriggerName,
    OBJECT_NAME(parent_id) AS TableName,
    is_disabled
FROM sys.triggers
WHERE parent_id = OBJECT_ID('Envios_Ap_Temp')

-- 3. Ver constraints de la tabla
SELECT 
    tc.CONSTRAINT_NAME,
    tc.CONSTRAINT_TYPE,
    cc.COLUMN_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
LEFT JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE cc
    ON tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME
WHERE tc.TABLE_NAME = 'Envios_Ap_Temp'

-- 4. Buscar tablas de log (si existen)
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME LIKE '%log%' 
   OR TABLE_NAME LIKE '%Log%'
   
SELECT * 
FROM AdminCatalogo 
WHERE idCatalogo = 9 
ORDER BY idElemento;

SELECT 
    CODIGO_PROVEEDOR,
    NUMERO_DOCUMENTO,
    NUMERO_REFERENCIA,
    COUNT(*) AS Cantidad
FROM [dbo].[Envios_Ap_Manual]
GROUP BY CODIGO_PROVEEDOR, NUMERO_DOCUMENTO, NUMERO_REFERENCIA
HAVING COUNT(*) > 1


