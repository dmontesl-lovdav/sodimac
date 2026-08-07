/**
* Procedimiento para calcular a detalle todos los rebates que tiene configurado el sistema
*
* El procedimiento es llamado desde el paquete RebateCalculo.dtx que se ejecuta cada hora
* EXEC [dbo].[spu_CalculoRebates]
*/
ALTER PROCEDURE [dbo].[spu_CalculoDetalleRebates] @v_inIdCatTipoRebate INT,@v_inIdCatPeriodo INT
AS
DECLARE   
  @RC int
, @pIdPeriodo int
, @V_CODIGO int
, @V_DESC varchar(200)
, @vIdCatPeriodo INT
, @vIdCatTipoRebate INT;

SET @vIdCatPeriodo = @v_inIdCatPeriodo
SET @vIdCatTipoRebate = @v_inIdCatTipoRebate

     IF @vIdCatTipoRebate IN (11)
        BEGIN
		
		    SELECT 'Proceso de calculo de MSI Periodo ['+CONVERT(VARCHAR,@vIdCatPeriodo)+']' 
			EXECUTE @RC = [dbo].[spu_CargaRebateVentaProveedorMSI] 
						   @vIdCatPeriodo
						,  @V_CODIGO OUTPUT
						,  @V_DESC OUTPUT
				
            SELECT CONVERT(VARCHAR,@V_CODIGO)+' - '+@V_DESC
            EXEC spu_CargaExclusionRebate @vIdCatTipoRebate,@vIdCatPeriodo

       END;
	
	 IF @vIdCatTipoRebate IN (12)
	   BEGIN

		    SELECT 'Proceso de calculo de MSI 3 Periodo ['+CONVERT(VARCHAR,@vIdCatPeriodo)+']' 
			EXECUTE @RC = [dbo].[spu_CargaRebateVentaProveedorMSI3]
						   @vIdCatPeriodo
						,  @V_CODIGO OUTPUT
						,  @V_DESC OUTPUT
			SELECT CONVERT(VARCHAR,@V_CODIGO)	
			EXEC spu_CargaExclusionRebate @vIdCatTipoRebate,@vIdCatPeriodo

	    END;
    
	IF @vIdCatTipoRebate IN (1,2,3,4,5,6,7,9,10,13,14)
	   BEGIN
	        SELECT 'Proceso de calculo de rebate '+CONVERT(VARCHAR,@vIdCatTipoRebate)
	        EXEC [dbo].[spu_CargaRebate] @vIdCatTipoRebate,@vIdCatPeriodo
--			EXEC [dbo].[spu_CargaRebateV2] @vIdCatTipoRebate,@vIdCatPeriodo
			EXEC spu_CargaExclusionRebate @vIdCatTipoRebate,@vIdCatPeriodo
       END;
	   
	IF @vIdCatTipoRebate = 8
	   BEGIN
	      EXEC spu_CargaRebateFillRate @vIdCatPeriodo
		  SELECT 'Calculo de Fill Rate' 
		  EXEC spu_CargaExclusionRebate @vIdCatTipoRebate,@vIdCatPeriodo
       END;