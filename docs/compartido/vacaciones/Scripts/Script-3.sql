/**
* Proceso para calcular los descuentos comerciales
*
* Parametros
* @v_InTipoRebate Id del tipo de inventario
* @Periodo        Id del periodo a calcular
*/
ALTER PROCEDURE [dbo].[spu_CargaRebate] @v_InTipoRebate INT,@Periodo INT
AS

----
--DECLARE @FechaInicio DATE = '2021-04-26', @FechaFin DATE= '2021-05-22',@NumeroMeses INT = 1,@Periodo VARCHAR(20) = 'MENSUAL'
DECLARE 
  @TipoCambio           DECIMAL(18,4)
, @v_FechaInicioPeriodo DATE
, @v_FechaFinalPeriodo  DATE
, @v_ValidaPeriodo      INT
, @v_IdCatPrograma      INT


SELECT @v_ValidaPeriodo = COUNT(1)
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo = 1
AND A.Estatus IN (2,3)
AND A.IdCatPeriodo = @Periodo
AND B.IdCatTipoRebate = @v_InTipoRebate
AND B.Activo = 1  -- Rebate Seleccionado por el usuario

IF @v_ValidaPeriodo = 0
   BEGIN
       SELECT 'El rebate '+CONVERT(VARCHAR,@v_InTipoRebate)+' no se encuentra configurado para el periodo '+CONVERT(VARCHAR,@Periodo) Msg
	   RETURN;
   END;

SELECT @TipoCambio = TipoCambio 
FROM RebateCambioMoneda
WHERE FechaCambio = (select MAX(FechaCambio) from RebateCambioMoneda)

SELECT 
  @v_FechaInicioPeriodo = A.FechaIni
, @v_FechaFinalPeriodo  = A.FechaFin
, @v_IdCatPrograma      = A.IdCatProgramaPago
FROM CatPeriodo A
INNER JOIN RelPeriodoTipoRebate B
ON A.IdCatPeriodo = B.IdCatPeriodo
WHERE A.Activo        = 1
AND A.Estatus         IN (2,3)
AND A.IdCatPeriodo    = @Periodo
AND B.IdCatTipoRebate = @v_InTipoRebate
AND B.Activo = 1  -- Rebate Seleccionado por el usuario

SELECT 'Parametros de calculo del rebate '+CONVERT(VARCHAR,@v_InTipoRebate)+' - Periodo '+CONVERT(VARCHAR,@Periodo) MSG
SELECT 'Fecha de cálculo, Fecha Inicio '+CONVERT(VARCHAR,@v_FechaInicioPeriodo)+' - Fecha Final '+CONVERT(VARCHAR,@v_FechaFinalPeriodo)+ ' Programa '+CONVERT(VARCHAR,@v_IdCatPrograma) MSG

IF @v_InTipoRebate = 7 
  BEGIN

INSERT INTO CalculoRebateTemp
 /*Apertura*/
SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Monedas,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra],
SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate)   * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate)   * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS) + (SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM  (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente	
	,oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu 
ON  acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND acu.ProgramaPago = @Periodo 
--AND acu.TipoAcuerdo = 'Apertura'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE --Ex.OrdenCompra is null 
--AND ExPr.Proveedor IS NULL 
--AND ExPrSku.Proveedor IS NULL
    oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
AND acu.TipoAcuerdo = 'Apertura'
AND oc.Estado       <>'Cancelada'
AND oc.TipoOrdenCompra IN (
							SELECT Valor FROM [dbo].[CatConfiguracion]
							WHERE NombreVariable = 'Apertura'
	                       )
-- Quita la OC
--AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END 


IF @v_InTipoRebate = 4 
  BEGIN

INSERT INTO CalculoRebateTemp 


/* COOP*/
SELECT
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra  AS [Tipo de Orden de Compra],
SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA
	,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND acu.ProgramaPago = @Periodo 
--AND acu.TipoAcuerdo = 'Coop'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--acu.ProgramaPago = @Periodo
oc.CantidadRecibida > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
--AND acu.TipoAcuerdo = 'Coop' 
AND acu.TipoValor = '%'
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
AND acu.TipoAcuerdo = 'Coop'
-- AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END

IF @v_InTipoRebate = 4 
  BEGIN

INSERT INTO CalculoRebateTemp

/* COOP Marca */
SELECT
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra  AS [Tipo de Orden de Compra],
SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
--AND acu.ProgramaPago = @Periodo 
--AND acu.TipoAcuerdo = 'Coop'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
INNER JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
INNER JOIN RebateAcuerdos acu 
ON acu.NumeroProveedor = OC.NumeroProveedor
AND (acu.Familia   = '0' OR LTRIM(RTRIM(ISNULL(acu.Familia,''))) ='' OR acu.Familia ='0000')
AND acu.Marca = ODBMS.[Prd_desc Marca]
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--acu.ProgramaPago = @Periodo
oc.CantidadRecibida > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
--AND acu.TipoAcuerdo = 'Coop' 
AND acu.TipoValor = '%'
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
AND acu.TipoAcuerdo = 'Coop'
-- AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END

IF @v_InTipoRebate = 1 
  BEGIN

INSERT INTO CalculoRebateTemp

/*Cross Docking*/
SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	 (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	, oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'Cross Docking'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
-- oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
    oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
AND oc.Estado <>'Cancelada'
AND oc.TipoOrdenCompra = 'Cross Docking'
AND acu.TipoAcuerdo = 'Cross Docking'
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END

IF @v_InTipoRebate = 6 
  BEGIN

INSERT INTO CalculoRebateTemp

/*Rebate Fijo */

SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	, oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'Omnicanal'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--AND
--oc.NumeroTienda IN (SELECT NumeroTienda FROM CatTienda WHERE IdCatTipoTienda = 2)
oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
AND oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
AND acu.TipoAcuerdo = 'Rebate Fijo'
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END 

IF @v_InTipoRebate = 6 
  BEGIN

INSERT INTO CalculoRebateTemp

/*Rebate Fijo por Marca */

SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'Rebate Fijo'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
INNER JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
INNER JOIN RebateAcuerdos acu 
ON acu.NumeroProveedor = OC.NumeroProveedor
AND (acu.Familia   = '0' OR LTRIM(RTRIM(ISNULL(acu.Familia,''))) ='' OR acu.Familia ='0000' )
AND acu.Marca = ODBMS.[Prd_desc Marca]
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--AND
--oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
    oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
AND acu.TipoAcuerdo = 'Rebate Fijo'
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END

IF @v_InTipoRebate = 14 
  BEGIN

INSERT INTO CalculoRebateTemp
/*Rebate Omincanal */

SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	, oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'Omnicanal'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--AND
--oc.NumeroTienda IN (SELECT NumeroTienda FROM CatTienda WHERE IdCatTipoTienda = 2)
oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
AND oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
AND acu.TipoAcuerdo = 'Omnicanal'
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END


IF @v_InTipoRebate = 10 
  BEGIN

INSERT INTO CalculoRebateTemp
/*Rebate MSI OC */

SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
/*
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND CONVERT(int, acu.Familia) = art.Familia  
AND ProgramaPago = @Periodo AND TipoAcuerdo = 'MSI OC'
*/
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
LEFT JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
/*
LEFT JOIN RebateExcluirOrdenCompra Ex ON 
Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
LEFT JOIN RebateExcluirProveedor ExPr ON 
Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
AND ExPrSku.sku = Oc.SKU
*/
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--AND 
Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
--oc.NumeroTienda IN (SELECT NumeroTienda FROM CatTienda WHERE IdCatTipoTienda = 2)
AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
AND oc.TotalRecibido > 0
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
AND acu.TipoAcuerdo = 'MSI OC'
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END


IF @v_InTipoRebate = 5
  BEGIN


INSERT INTO CalculoRebateTemp
/*Merma*/
SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra  AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA )) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'Merma' 
AND TipoValor = '%'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--AND
oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
AND oc.Estado <>'Cancelada'
AND acu.TipoAcuerdo = 'Merma'
AND acu.TipoValor = '%'
--AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
    (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca


END

IF @v_InTipoRebate = 3 
  BEGIN

INSERT INTO CalculoRebateTemp
/*MET*/
SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia 
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'MET'  
AND TipoValor = '%'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE 
--Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL
--AND 
oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
AND acu.TipoAcuerdo = 'MET'
AND oc.Estado <>'Cancelada'
AND acu.TipoValor = '%'
--AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
    (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END),
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END

IF @v_InTipoRebate = 2 
  BEGIN

INSERT INTO CalculoRebateTemp

/*Stock Bodega*/
SELECT 
Origen,
Moneda,
REPLACE(RFC,'¬','') AS RFC,
CodigoProveedor AS [Código de Proveedor],
Proveedor,
'' AS [Gerente de negocio],
[NombreGerente],
NumeroJefeLinea AS [Número jefe de línea],
NombreJefeLinea AS [Nombre de Jefe de Línea],
--RIGHT('0000000000' + Ltrim(Rtrim( ClasificacionComercial)),10) AS CLACOM,
Familia, 
NombreFamilia,
CONVERT(DECIMAL(18,4),SUM(MontoRecibido)) AS [Monto Recibido],
OrdenCompra AS [Orden Compra],
CONVERT(nvarchar(10), FechaEmision, 103) AS [Fecha Emisión],
CONVERT(nvarchar(10), FechaRecepcion, 103) AS [Fecha de Recepción],
TipoAcuerdo AS [Tipo de Acuerdo],
MonedaOc AS Moneda,
Valor,
TipoValor AS [Tipo de Valor],
CONVERT(DECIMAL(18,4),SUM(Rebate)) AS MontoRebate,
IdCatProgramaPago,
ProgramaPago AS [Programa Pago],
TipoOrdenCompra AS [Tipo de Orden de Compra]
,SKU,
DescripcionProducto,
CONVERT(DECIMAL(18,2),IVA) IVA,
CONVERT(DECIMAL(18,2),IEPS) IEPS,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IVA) AS MontoIva,
CONVERT(DECIMAL(18,4),SUM(Rebate) * IEPS) AS MontoIeps,
CONVERT(DECIMAL(18,4),((SUM(Rebate) * IEPS)+(SUM(Rebate) * IVA)) + SUM(Rebate)) AS [Monto descuento]
,CASE WHEN Moneda = 'MXN' THEN 1.00 else @TipoCambio end as TipoCambio 
,IdCatPeriodo
,IdCatTipoRebate
,0 Exclusion
,NULL FechaExclusion
,0 IdExclusion
,Marca
FROM (
SELECT 
	imp.Origen,
	oc.MonedaOrdenCompra Moneda,
	pro.RFC,
	OC.NumeroProveedor AS CodigoProveedor,
	REPLACE(pro.NombreProveedor,'"','') AS Proveedor,
	RTRIM(LTRIM(SUBSTRING(imp.JefeLinea,0,CHARINDEX('-', imp.JefeLinea)))) AS NumeroJefeLinea,
	RTRIM(LTRIM(REPLACE(SUBSTRING(imp.JefeLinea,CHARINDEX('-', imp.JefeLinea),LEN(imp.JefeLinea)),'-',''))) AS NombreJefeLinea,
	imp.ClasificacionComercial,
	sum(oc.TotalRecibido) AS MontoRecibido,
	oc.NumeroOrdenCompra AS OrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra AS MonedaOc,
	CONVERT(DECIMAL(18,4),acu.Valor) AS Valor, 
	acu.TipoValor,
	CONVERT(DECIMAL(18,4),
	CASE WHEN acu.TipoValor = '%' THEN CONVERT(DECIMAL(18,4),sum(oc.TotalRecibido) * acu.Valor)
	WHEN acu.TipoValor = 'USD' THEN CONVERT(DECIMAL(18,4),acu.Valor * @TipoCambio)
	WHEN acu.TipoValor = 'MXN' THEN CONVERT(DECIMAL(18,4),acu.Valor)
	ELSE 0 END
	)AS Rebate,
	@v_IdCatPrograma IdCatProgramaPago,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	  (CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END) Familia
    , ODBMS.FAM NombreFamilia
	, ODBMS.Gerente_Negocio NombreGerente
	,oc.SKU,
	art.DescripcionProducto,
	 (
	   CASE 
	       WHEN imp.IVA = 0.00 THEN 
		   0.16
		   ELSE 
		   imp.IVA
        END
      ) IVA,
	imp.IEPS,
	@Periodo IdCatPeriodo,
	@v_InTipoRebate IdCatTipoRebate,
	ISNULL(ODBMS.[Prd_desc Marca],'') Marca
FROM RebateOrdenCompra oc
INNER JOIN RebateImpuestos imp ON oc.SKU = imp.SKU
INNER JOIN RebateArticulos art ON art.SKU = imp.SKU
INNER JOIN RebateAcuerdos acu ON acu.NumeroProveedor = OC.NumeroProveedor
AND acu.Familia  = art.Familia
--AND ProgramaPago = @Periodo 
--AND TipoAcuerdo = 'Stock Bodega'
INNER JOIN RebateProveedor pro ON pro.CodigoProveedor = OC.NumeroProveedor
INNER JOIN CatTipoRebate Tr ON acu.TipoAcuerdo = Tr.TipoRebate
--LEFT JOIN RebateExcluirOrdenCompra Ex ON 
--Oc.NumeroOrdenCompra = Ex.OrdenCompra AND Tr.IdCatTipoRebate = Ex.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedor ExPr ON 
--Oc.NumeroProveedor = ExPr.Proveedor AND Tr.IdCatTipoRebate = ExPr.IdCatTipoRebate
--LEFT JOIN RebateExcluirProveedorSKU ExPrSku ON 
--Oc.NumeroProveedor = ExPrSku.Proveedor AND Tr.IdCatTipoRebate = ExPrSku.IdCatTipoRebate
--AND ExPrSku.sku = Oc.SKU
LEFT JOIN [dbo].[RebateArticulosODBMS] ODBMS
ON  ODBMS.SKU = oc.SKU 
AND ODBMS.Numero_Proveedor = oc.NumeroProveedor
WHERE -- Ex.OrdenCompra is null AND ExPr.Proveedor IS NULL AND ExPrSku.Proveedor IS NULL

--AND
--oc.NumeroTienda IN (SELECT NumeroTienda FROM CatTienda WHERE IdCatTipoTienda = 2)
    oc.TotalRecibido > 0
AND Tr.IdCatTipoRebate  = @v_InTipoRebate
and acu.ProgramaPago    =   (  SELECT ProgramaPago FROM CatProgramaPago CPP WHERE CPP.IdCatProgramaPago = @v_IdCatPrograma )
AND Tr.Activo           = 1
AND oc.FechaRecepcion between @v_FechaInicioPeriodo AND @v_FechaFinalPeriodo
--AND oc.FechaRecepcion between @FechaInicio AND @FechaFin
--AND imp.Origen= 'Nacional'
AND oc.Estado <>'Cancelada'
AND oc.TipoOrdenCompra in ('Rep Central','Rep Central Volumen')
AND oc.NumeroTienda in ('5000','5010')
AND acu.TipoAcuerdo = 'Stock Bodega'
AND oc.NumeroOrdenCompra NOT IN (SELECT OrdenCompra FROM RebateOrdenCompraCancelada)
GROUP BY 	imp.Origen,
	oc.MonedaOrdenCompra ,
	pro.RFC,
	OC.NumeroProveedor,
	pro.NombreProveedor,
	imp.JefeLinea,
	imp.ClasificacionComercial,
	oc.CantidadRecibida,
	oc.NumeroOrdenCompra,
	oc.FechaEmision,
	oc.FechaRecepcion,
	acu.TipoAcuerdo,
	oc.MonedaOrdenCompra,
	acu.Valor,
	acu.TipoValor,
	acu.ProgramaPago,
	oc.TipoOrdenCompra,
	(CASE WHEN (ODBMS.NUM_FAM IS NULL OR LTRIM(RTRIM(ODBMS.NUM_FAM)) ='' ) THEN art.Familia ELSE  ODBMS.NUM_FAM END), 
	ODBMS.FAM,
	ODBMS.Gerente_Negocio,
	oc.SKU,
	art.DescripcionProducto,
	imp.IVA,
	imp.IEPS,
	ISNULL(ODBMS.[Prd_desc Marca],'')
) G 
GROUP BY Origen,Moneda,RFC,CodigoProveedor,Proveedor,NumeroJefeLinea,NombreJefeLinea,ClasificacionComercial,OrdenCompra,FechaEmision,FechaRecepcion,TipoAcuerdo,MonedaOc,Valor,TipoValor,IdCatProgramaPago,ProgramaPago,TipoOrdenCompra,Familia,NombreFamilia
,SKU,DescripcionProducto,IVA,IEPS,
 IdCatPeriodo
,IdCatTipoRebate
,NombreGerente
,Marca

END;