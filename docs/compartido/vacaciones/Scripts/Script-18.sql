-- DROP PROCEDURE [dbo].[spu_CargaCatalogoBase]
/**  
* Procedimiento para realizar la carga de los catálogos base para el calculo de rebates  
*  
*/  
CREATE PROCEDURE [dbo].[spu_CargaCatalogoBase]  
AS  
DECLARE   
  @v_TotalProveedor INT = 0  
, @v_TotalImpuestos INT = 0  
, @v_TotalRebateAcuerdo INT = 0  
, @v_TotalMoneda INT = 0  
, @v_TotalArticulo INT = 0  
, @v_TotalLeadTime INT = 0  
, @v_TotalArticulosODBMS INT  
, @v_ValidacionCargaDocumento INT = 0  
  
SET @v_ValidacionCargaDocumento = 0;  
  
SELECT @v_ValidacionCargaDocumento = COUNT(1)  
FROM controlDocumento  
WHERE idEstatusArchivo=0  
AND activo = 1  
AND idDocumento = (   
                 SELECT idDocumento   
     FROM catDocumento   
     WHERE nombreDocumento='Catálogo proveedores'   
     AND activo=1  
      );  
  
SET  @v_TotalProveedor = 0  
  
SELECT @v_TotalProveedor = COUNT(1)   
FROM RebateProveedorTemp  
  
IF @v_ValidacionCargaDocumento > 0 AND @v_TotalProveedor > 0  
   BEGIN  
  
    UPDATE [RebateProveedorTemp] SET RFC = REPLACE(RFC,'¬¬¬¬','')  
  
    TRUNCATE TABLE RebateProveedor;  
  
    INSERT INTO RebateProveedor  
    SELECT DISTINCT CodigoProveedor  
    , NombreProveedor  
    , Origen  
    , RUTDV  
    , RFC  
    , Correo  
    , RegimenFiscal  
    , CodigoPostal  
	,null
    FROM RebateProveedorTemp  
  
    UPDATE controlDocumento SET idEstatusArchivo=4,numeroRegistro=@v_TotalProveedor,fechaHoraProceso=GETDATE()   
    WHERE idEstatusArchivo=0  
    AND activo = 1  
    AND idDocumento = (   
                        SELECT idDocumento   
         FROM catDocumento   
         WHERE nombreDocumento='Catálogo proveedores'   
         and activo=1  
       );  
  
  TRUNCATE TABLE RebateProveedorTemp;  
  
   END;  
  
SET @v_ValidacionCargaDocumento = 0  
  
SELECT @v_ValidacionCargaDocumento = COUNT(1)  
--SELECT *  
FROM controlDocumento  
WHERE idEstatusArchivo=0  
AND activo = 1  
AND idDocumento = (   
     SELECT idDocumento   
     FROM catDocumento   
     WHERE nombreDocumento='Catálogo de impuestos'    
     AND activo=1  
      );  
  
SET  @v_TotalImpuestos = 0  
  
SELECT @v_TotalImpuestos = COUNT(1)  
FROM RebateImpuestosTemp  
  
IF @v_ValidacionCargaDocumento > 0 AND @v_TotalImpuestos > 0  
   BEGIN  
  
    SELECT @v_TotalImpuestos = COUNT(1)  
    FROM RebateImpuestosTemp  
  
    TRUNCATE TABLE RebateImpuestos  
  
    INSERT INTO RebateImpuestos  
    SELECT DISTINCT   
    CONVERT(VARCHAR(8),SKU),  
    CONVERT(VARCHAR(16),StepId),  
    CONVERT(VARCHAR(200),DescripcionProducto),  
    CONVERT(VARCHAR(16),Origen),  
    CONVERT(VARCHAR(200),JefeLinea),  
    case when iva ='' then 0.00  
    else  
    ISNULL(REPLACE(REPLACE(REPLACE(REPLACE(IVA,'Iva 16%',.16),'Iva cero',0),'Exento',0),'IVA 0%',0),0)  
    end  
    Iva,  
    CASE WHEN IEPS = 'Plaguicidas B. Toxicidad' THEN (SELECT Valor FROM [CatConfiguracion] WHERE [NombreVariable] = 'Plaguicidas B. Toxicidad')  
    WHEN IEPS = 'Plaguicidas M. Toxicidad' THEN (SELECT Valor FROM [CatConfiguracion] WHERE [NombreVariable] = 'Plaguicidas M. Toxicidad')  
    ELSE 0.0000 END AS IPES,  
    CONVERT(VARCHAR(60),ClasificacionComercial), null
    FROM RebateImpuestosTemp  
    ORDER BY Iva;  
  
    UPDATE controlDocumento SET idEstatusArchivo=4,numeroRegistro=@v_TotalImpuestos,fechaHoraProceso=GETDATE()   
    WHERE idEstatusArchivo=0  
    AND activo = 1  
    AND idDocumento = (   
       SELECT idDocumento   
       FROM catDocumento   
       WHERE nombreDocumento='Catálogo de impuestos'    
       AND activo=1  
        );  
  
  
      END;  
  
SET @v_ValidacionCargaDocumento = 0  
  
SELECT @v_ValidacionCargaDocumento = COUNT(1)  
--SELECT *  
FROM controlDocumento  
WHERE idEstatusArchivo=0  
AND activo = 1  
AND idDocumento = (   
                 SELECT idDocumento   
     FROM catDocumento   
     WHERE nombreDocumento='Catálogo acuerdo comercial'    
AND activo=1  
      );  
  
SET  @v_TotalRebateAcuerdo = 0  
  
SELECT @v_TotalRebateAcuerdo = COUNT(1)  
FROM RebateAcuerdosTemp  
  
IF @v_ValidacionCargaDocumento > 0 AND @v_TotalRebateAcuerdo > 0  
      BEGIN  
  
		   UPDATE RebateAcuerdosTemp 
		   SET   NumeroProveedor = (CASE WHEN CHARINDEX(CHAR(194), NumeroProveedor COLLATE Latin1_General_BIN) > 0 THEN LEFT(NumeroProveedor, LEN(NumeroProveedor) - 1) ELSE NumeroProveedor END)
			   , RFC = (CASE WHEN CHARINDEX(CHAR(194), RFC COLLATE Latin1_General_BIN) > 0 THEN LEFT(RFC, LEN(RFC) - 1) ELSE RFC END)
			   , RazonSocial = (CASE WHEN CHARINDEX(CHAR(194), RazonSocial COLLATE Latin1_General_BIN) > 0 THEN LEFT(RazonSocial, LEN(RazonSocial) - 1) ELSE RazonSocial END)
			   , Estado = (CASE WHEN CHARINDEX(CHAR(194), Estado COLLATE Latin1_General_BIN) > 0 THEN LEFT(Estado, LEN(Estado) - 1) ELSE Estado END)
			   , Familia = (CASE WHEN CHARINDEX(CHAR(194), Familia COLLATE Latin1_General_BIN) > 0 THEN LEFT(Familia, LEN(Familia) - 1) ELSE Familia END)
			   , ClasificacionComercial = (CASE WHEN CHARINDEX(CHAR(194), ClasificacionComercial COLLATE Latin1_General_BIN) > 0 THEN LEFT(ClasificacionComercial, LEN(ClasificacionComercial) - 1) ELSE ClasificacionComercial END)
			   , NumeroAcuerdo = (CASE WHEN CHARINDEX(CHAR(194), NumeroAcuerdo COLLATE Latin1_General_BIN) > 0 THEN LEFT(NumeroAcuerdo, LEN(NumeroAcuerdo) - 1) ELSE NumeroAcuerdo END)
			   , TipoAcuerdo = (CASE WHEN CHARINDEX(CHAR(194), TipoAcuerdo COLLATE Latin1_General_BIN) > 0 THEN LEFT(TipoAcuerdo, LEN(TipoAcuerdo) - 1) ELSE TipoAcuerdo END)
			   , Moneda = (CASE WHEN CHARINDEX(CHAR(194), Moneda COLLATE Latin1_General_BIN) > 0 THEN LEFT(Moneda, LEN(Moneda) - 1) ELSE Moneda END)
			   , Valor = (CASE WHEN CHARINDEX(CHAR(194), Valor COLLATE Latin1_General_BIN) > 0 THEN LEFT(Valor, LEN(Valor) - 1) ELSE Valor END)
			   , TipoValor = (CASE WHEN CHARINDEX(CHAR(194), TipoValor COLLATE Latin1_General_BIN) > 0 THEN LEFT(TipoValor, LEN(TipoValor) - 1) ELSE TipoValor END)
			   , FillRate = (CASE WHEN CHARINDEX(CHAR(194), FillRate COLLATE Latin1_General_BIN) > 0 THEN LEFT(FillRate, LEN(FillRate) - 1) ELSE FillRate END)
			   , ProgramaPago = (CASE WHEN CHARINDEX(CHAR(194), ProgramaPago COLLATE Latin1_General_BIN) > 0 THEN LEFT(ProgramaPago, LEN(ProgramaPago) - 1) ELSE ProgramaPago END)
			   , Marca = (CASE WHEN CHARINDEX(CHAR(194), Marca COLLATE Latin1_General_BIN) > 0 THEN LEFT(Marca, LEN(Marca) - 1) ELSE Marca END)
			WHERE CHARINDEX(CHAR(194), NumeroProveedor COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), RFC COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), RazonSocial COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), Estado COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), Familia COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), ClasificacionComercial COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), NumeroAcuerdo COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), TipoAcuerdo COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), Moneda COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), Valor COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), TipoValor COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), FillRate COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), ProgramaPago COLLATE Latin1_General_BIN) > 0
			OR CHARINDEX(CHAR(194), Marca COLLATE Latin1_General_BIN) > 0;


		    TRUNCATE TABLE RebateAcuerdos; -- 2025/07/22 DML Se eliminan Rebates acuerdos para que se continue con la misma lógica de los catálogos anteriores
           		   
		   INSERT INTO RebateAcuerdos  
		   SELECT DISTINCT   
		   CONVERT(VARCHAR(8),  NumeroProveedor),  
		   CONVERT(VARCHAR(16),RFC),  
		   CONVERT(VARCHAR(200),RazonSocial),  
		   CONVERT(VARCHAR(20),Estado),  
		   ( CASE WHEN  LEN(LTRIM(RTRIM(CONVERT(VARCHAR(8),Familia)))) <= 3 THEN RIGHT('0000' + LTRIM(RTRIM(CONVERT(VARCHAR(8),Familia))),4) ELSE LTRIM(RTRIM(CONVERT(VARCHAR(8),Familia))) END )  ,  
		   CONVERT(VARCHAR(60),ClasificacionComercial),  
		   CONVERT(INT,NumeroAcuerdo),  
		   CONVERT(VARCHAR(20),TipoAcuerdo),  
		   CONVERT(VARCHAR(8),Moneda),  
		   CASE WHEN ISNUMERIC(Valor) = 0 THEN '0.0000'  
		   ELSE RTRIM(LTRIM(REPLACE(Valor,',','')))   
		   END AS Valor,  
		   CONVERT(VARCHAR(8),REPLACE(TipoValor,'"','')),  
		   CONVERT(VARCHAR(12),FillRate),  
		   CONVERT(VARCHAR(20),ProgramaPago),  
		   Marca , null
		   FROM RebateAcuerdosTemp;  
  
		   UPDATE controlDocumento SET idEstatusArchivo=4,numeroRegistro=@v_TotalRebateAcuerdo,fechaHoraProceso=GETDATE()   
		   WHERE idEstatusArchivo=0  
		   AND activo = 1  
		   AND idDocumento = (   
				SELECT idDocumento   
				FROM catDocumento   
				WHERE nombreDocumento='Catálogo acuerdo comercial'    
				AND activo=1  
				 );  
  
            TRUNCATE TABLE RebateAcuerdosTemp;  

   END;  
  
SET @v_ValidacionCargaDocumento = 0  
  
SELECT @v_ValidacionCargaDocumento = COUNT(1)  
--SELECT *  
FROM controlDocumento  
WHERE idEstatusArchivo=0  
AND activo = 1  
AND idDocumento = (   
     SELECT idDocumento   
     FROM catDocumento   
     WHERE nombreDocumento='Catálogo artículo'    
     AND activo=1  
      );  
  
SELECT @v_TotalArticulo = COUNT(1)   
FROM RebateArticulosTemp  
  
IF   @v_TotalArticulo > 0  
      BEGIN  
  
   TRUNCATE TABLE RebateArticulos  
  
   INSERT INTO RebateArticulos  
   SELECT DISTINCT SKU  
   , DescripcionProducto  
   , NumeroDepartamento  
   , RIGHT( ('0000'+Familia) ,4) Familia  
   , NumeroSubFamilia  
   , NumeroProveedor  
   , NombreProveedor  
   , NumeroComprador  
   , NombreJefeCompra  
   , NULL Marca  
   FROM RebateArticulosTemp;  
  
   UPDATE controlDocumento SET idEstatusArchivo=4,numeroRegistro=@v_TotalArticulo,fechaHoraProceso=GETDATE()   
   WHERE idEstatusArchivo=0  
   AND activo = 1  
   AND idDocumento = (   
       SELECT idDocumento   
       FROM catDocumento   
       WHERE nombreDocumento='Catálogo artículo'    
       AND activo=1  
        );  
  
  TRUNCATE TABLE RebateArticulosTemp;  
  
   END;  
  
SELECT @v_TotalLeadTime = COUNT(1)   
FROM RebateLeadTimeTemp  
  
/*SELECT @v_TotalArticulosODBMS = COUNT(1)  
FROM SODIMAC_PRODUCTS.dbo.CICMX_ODBMS  
  
IF @v_TotalLeadTime > 0  
      BEGIN  
       
      TRUNCATE TABLE RebateLeadTime  
  
   INSERT INTO RebateLeadTime  
   SELECT CodigoProveedor  
   , NombreProveedor  
   , Dias_Proceso_oc  
   , Dias_de_envio_oc  
   , Total  
   FROM RebateLeadTimeTemp  
  
   TRUNCATE TABLE RebateLeadTimeTemp;  
  
  
   END;  
  
IF @v_TotalArticulosODBMS > 0  
      BEGIN  
  
          TRUNCATE TABLE RebateArticulosODBMS  
  
    INSERT INTO RebateArticulosODBMS  
    SELECT    
      SKU  
    , Desc_Producto  
    , VIN  
    , Costo_casepack  
    , Cost_Unitario  
    , Moneda  
    , Costo_unitario_tda  
    , Und_Casepack  
    , CPI  
    , NombreProveedor  
    , Origen  
    , Pais  
    , Fabricacion  
    ,RegEcon  
    ,Peso  
    ,Alto  
    ,Ancho  
    ,Largo  
    ,Pallet_hi  
    ,Pallet_tier  
    ,Precio_tda1010  
    ,Comprador  
    ,Jefe_Linea  
    ,Gerente_Negocio  
    ,Metodo_Distribucion  
    ,rpl_dist_method  
    ,Bodega_Number  
    ,[Prd_desc Marca]  
    ,Fecha_Activacion  
    ,Codigo_Barra  
    ,Clacom  
    ,Origen_SKU  
    ,Complemento  
    ,Factor_Internacion  
    ,IVA  
    ,IEPS  
    ,Numero_Proveedor  
    ,Atrib_assortment_Letras  
    ,Atrib_assortment  
    ,dias_proceso_oc  
    ,dias_envio_oc  
    ,minimo_despacho  
    ,NUM_DEPTO  
    ,DESC_DEPTO  
    ,NUM_FAM  
    ,FAM  
    ,NUM_SUB_FAM  
    , SUB_FAM  
    , GETDATE()  
    ,Nro_Comprador  
    , MD_TDA_1010  
    , MD_TDA_1030  
    , MD_TDA_1040  
    , MD_TDA_1050  
    , MD_TDA_1110  
    , MD_TDA_1120  
    , ASSORMENT_1010  
    , ASSORMENT_1030  
    , ASSORMENT_1040  
    , ASSORMENT_1050  
    , ASSORMENT_1110  
    , ASSORMENT_1120  
    , Atributo_Push  
    , Volumen_Case_M3  
    , Cod_Madre  
    FROM SODIMAC_PRODUCTS.dbo.CICMX_ODBMS  
  
  
   END;*/  
  
      INSERT INTO CatFamilia  
   SELECT A.NUM_FAM,A.FAM,GETDATE(),1,NULL   
  FROM (  
  SELECT FORMAT(CONVERT(NUMERIC,NUM_FAM), '0000') NUM_FAM,FAM,COUNT(1) TOTAL  
  FROM RebateArticulosODBMS  
  GROUP BY FORMAT(CONVERT(NUMERIC,NUM_FAM), '0000'),FAM  
   ) A  
   WHERE NOT EXISTS (  
      SELECT 1  
   FROM CatFamilia B  
   WHERE B.numFamilia = A.NUM_FAM  
   );  
  
   TRUNCATE TABLE RebateProveedorTemp;  
   TRUNCATE TABLE RebateImpuestosTemp;  
   TRUNCATE TABLE RebateAcuerdosTemp;  
  
--   EXEC [dbo].[usp_CargaOrdenCompra];
   
   
   select top 100 *
from view_calculo_rebate_portal;




-- ============================================================
-- MONITOREO: Consultas de trazabilidad en SODIMAC_BATCH_DEV
-- BD: SQL Server 10.138.153.10:1433
-- ============================================================

-- Ver ejecuciones fallidas
SELECT TOP 20 *
-- delete
FROM ctrlProcesoCab
WHERE estatus = 'FAILED'
ORDER BY fecha_inicio DESC;

-- Ver detalle de una ejecucion especifica (reemplazar id=25)
SELECT *
-- delete
FROM ctrlProcesoDet
WHERE id_ejecucion = 43
ORDER BY fecha_inicio_registro;

-- Ver log de errores de una ejecucion (reemplazar id=25)
SELECT *
-- delete
FROM ctrlLog
WHERE id_ejecucion = 43
ORDER BY fecha_registro;

-- Ver elementos procesados de una ejecucion (reemplazar id=25)
SELECT *
-- delete
FROM ctrlProcesoElemento
WHERE proceso_det_id IN (
    SELECT id FROM ctrlProcesoDet WHERE proceso_cab_id = 25
)
ORDER BY fecha_inicio;




SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Comprobante'
ORDER BY ORDINAL_POSITION;


-- ============================================================
-- PASO 1: Agregar columna fiscal_uuid si no existe
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Comprobante' AND COLUMN_NAME = 'fiscal_uuid'
)
BEGIN
    ALTER TABLE Comprobante ADD fiscal_uuid VARCHAR(36) NOT NULL DEFAULT '';
    PRINT 'Columna fiscal_uuid agregada OK';
END
ELSE
    PRINT 'Columna fiscal_uuid ya existe';


-- ============================================================
-- PASO 2: Agregar columna invoice_uuid si no existe
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Comprobante' AND COLUMN_NAME = 'invoice_uuid'
)
BEGIN
    ALTER TABLE Comprobante ADD invoice_uuid VARCHAR(36) NULL;
    PRINT 'Columna invoice_uuid agregada OK';
END
ELSE
    PRINT 'Columna invoice_uuid ya existe';


-- ============================================================
-- POST: Verificar resultado
-- ============================================================
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Comprobante'
  AND COLUMN_NAME IN ('fiscal_uuid', 'invoice_uuid');