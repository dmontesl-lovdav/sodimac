/**
* Publica el layout en la contabilidad de manera temporal 
* @author: ICZ
* @result: 0 - En proceso
           1 - Registro OK
           2 - Factura duplicada
		   3 - No existen registros para procesar
		   4 - Error en la carga de información
		   5 - Tipo de documento invalido para la carga
  @comment: Revisar la tabla de adminCatalogo para conocer todos los estatus relacionados
*/
ALTER PROCEDURE [dbo].[sp_carga_envios_ap] 
  @V_NOMBRE_ARCHIVO   VARCHAR(100),
  @V_IDUSUARIO        NUMERIC,
  @V_CODIGO			  INT	       = 0		OUTPUT,
  @V_DESC			  VARCHAR(100) = NULL	OUTPUT
AS
BEGIN
DECLARE
@V_CONTADOR            NUMERIC,
@v_RESULTADO           INT,
@V_TIPO_DOCUMENTO      VARCHAR(6),
@V_TIPO_DOCUMENTO_DOC  VARCHAR(6),
@V_TIPO_DOCUMENTO_TEMP VARCHAR(6),
@V_ID_PERIODO          NUMERIC,
@V_OBJETO              VARCHAR(30),
@V_MENSAJE             VARCHAR(1500),
@V_NUM_REGISTRO        NUMERIC;

SET @v_RESULTADO           = 0;
SET @V_ID_PERIODO          = 0;
SET @V_NUM_REGISTRO        = 0;
SET @V_TIPO_DOCUMENTO_TEMP = 'REMAN';
SET @V_TIPO_DOCUMENTO_DOC  = '';
SET @V_OBJETO              = '[dbo].[sp_carga_envios_ap]';

SET @V_TIPO_DOCUMENTO = (
SELECT ISNULL(VALOR,@V_TIPO_DOCUMENTO_TEMP)
FROM CatConfiguracion
WHERE NombreVariable='TipoDocumentoAP'
);

-- Valida que no exista la referencia y documento previamente registrados
SELECT @V_CONTADOR = COUNT(1)
FROM [dbo].[Envios_Ap_Temp] A
WHERE FLAG_ENVIADO = 0
AND 
(
	EXISTS 
	( 
		SELECT 1
		FROM [dbo].[Envios_Ap_Manual] B
		WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
		AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
		AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
	)
	OR EXISTS 
	( 
		SELECT 1
		FROM [dbo].[Envios_Ap] C
		WHERE C.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
		AND C.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
		AND C.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
	)
	OR EXISTS 
	( 
		SELECT 1
		FROM [SODIMAC_SAP_PROD].[dbo].[Envios_Ap] B
		WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
		AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
		AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
	)
);

   IF @V_CONTADOR > 0
      BEGIN
	     TRUNCATE TABLE [dbo].[Envios_Ap_Temp];
	     -- Factura duplicada
         SET    @v_RESULTADO = 2;
	     SELECT @V_CODIGO    = @v_RESULTADO;
		 --SELECT @v_RESULTADO CODIGO,'Factura duplicada' DESCRIPCION
		 SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
		 exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
         RETURN;
	  END;

   SET @V_CONTADOR = 0;

   SELECT @V_CONTADOR = COUNT(1)
   FROM [dbo].[Envios_Ap_Temp] A
   WHERE FLAG_ENVIADO = 0;

   IF @V_CONTADOR = 0
      BEGIN
	      -- Factura sin registros que procesar
         SET @v_RESULTADO        = 3;
		 SET @V_CODIGO = @v_RESULTADO;
		 SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
		 exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
		 RETURN;
	  END;

    SET @V_CONTADOR = 0;

    SELECT @V_CONTADOR = COUNT(1)
    FROM [dbo].[Envios_Ap_Temp] A
    WHERE FLAG_ENVIADO = 0
	AND TIPO_DOCUMENTO!=@V_TIPO_DOCUMENTO;

	-- Registros contables con un tipo de documento invalido
    IF @V_CONTADOR > 0
      BEGIN

	     TRUNCATE TABLE [dbo].[Envios_Ap_Temp];

	     SELECT TOP 1 @V_TIPO_DOCUMENTO_DOC = TIPO_DOCUMENTO
		 FROM [dbo].[Envios_Ap_Temp];

         SET @v_RESULTADO        = 5;
		 SET @V_CODIGO = @v_RESULTADO;
	     --SELECT @v_RESULTADO CODIGO,'Factura con documento invalido' DESCRIPCION
		 SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,@V_TIPO_DOCUMENTO_DOC,@V_TIPO_DOCUMENTO,null,null,null);
		 exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
         RETURN;
	  END;

	  SET @V_CONTADOR = 0;

	  SELECT @V_CONTADOR = COUNT(1)
      FROM [dbo].[Envios_Ap_Temp] A
  WHERE MONEDA !='MXN'
	  AND TIPO_CAMBIO <= 1

     IF @V_CONTADOR > 0
        BEGIN
	      TRUNCATE TABLE [dbo].[Envios_Ap_Temp];
          SET @V_RESULTADO        = 6;
		  SET @V_CODIGO = @V_RESULTADO;
	      --SELECT @v_RESULTADO CODIGO,'Factura con documento invalido' DESCRIPCION
		  SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);    
		  exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
		  RETURN;
	  END;

	  SET @V_CONTADOR = 0;

	  SELECT @V_CONTADOR = COUNT(1)
      FROM [dbo].[Envios_Ap_Temp] A
      WHERE MONEDA ='MXN'
	  AND TIPO_CAMBIO != 1

     IF @V_CONTADOR > 0
        BEGIN
	       TRUNCATE TABLE [dbo].[Envios_Ap_Temp];
           SET @v_RESULTADO        = 7;
		   SET @V_CODIGO = @v_RESULTADO;
		   --SELECT @v_RESULTADO CODIGO,'Factura con documento invalido' DESCRIPCION
		   SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
		   exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
           RETURN;
	  END;

	  SET @V_CONTADOR = 0;

	  BEGIN TRY

			  SELECT @V_CONTADOR = COUNT(1)
			  FROM 
			  (
				  SELECT REFERENCIA_DOCUMENTO,NUMERO_DOCUMENTO,CODIGO_PROVEEDOR,SUM(CONVERT(NUMERIC,SUCURSAL)) SUCURSAL
				  FROM [dbo].[Envios_Ap_Temp] 
				  GROUP BY REFERENCIA_DOCUMENTO,NUMERO_DOCUMENTO,CODIGO_PROVEEDOR
				  HAVING SUM(CONVERT(NUMERIC,SUCURSAL))<=0
			  ) A;

	  END TRY

      BEGIN CATCH

		 TRUNCATE TABLE [dbo].[Envios_Ap_Temp];	 
		 SET @v_RESULTADO = 9;
		 SET @V_CODIGO = @v_RESULTADO;
		 SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
		 --SELECT @v_RESULTADO CODIGO,'Error al intentar procesar las facturas' DESCRIPCION
		 exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
		 RETURN;

	 END CATCH

     IF @V_CONTADOR > 0
        BEGIN
	       TRUNCATE TABLE [dbo].[Envios_Ap_Temp];
           SET @v_RESULTADO        = 8;
		   SET @V_CODIGO = @v_RESULTADO;
		   SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
	       --SELECT @v_RESULTADO CODIGO,'Factura con documento invalido' DESCRIPCION
		   exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
           RETURN;
	  END;

BEGIN TRY
    BEGIN TRANSACTION
	
	    -- Se envia la información a la BD de contabilidad
		INSERT INTO [Envios_Ap_Manual]
		SELECT 
		NEWID() ID,
		EMPRESA,	
		CONVERT(DATE,FECHA_DOCUMENTO,103) FECHA_DOCUMENTO,
		LTRIM(RTRIM(REFERENCIA_DOCUMENTO)) REFERENCIA_DOCUMENTO,	
		LTRIM(RTRIM(NUMERO_DOCUMENTO)) NUMERO_DOCUMENTO,
		LTRIM(RTRIM(MONEDA)) MONEDA,	
		TIPO_CAMBIO,	
		DEBITO_CREDITO,	
		CUENTA_CONTABLE,	
		CODIGO_PROVEEDOR,	
		IMPORTE,	
		SUCURSAL,	
		CONDICION_PAGO,
		CONVERT(DATE,FECHA_VENCIMIENTO,103) FECHA_VENCIMIENTO,
		NULL BLOQUEO_PAGO,	
		SISTEMA_ORIGEN,
		CONVERT(DATE,FECHA_ENVIO,103) FECHA_ENVIO,
		CONVERT(DATE,FECHA_CONTABLE,103) FECHA_CONTABLE,
		CLASE_DOCUMENTO,	
		NUMERO_REFERENCIA,	
		CENTRO_COSTO,	
		CENTRO_BENEFICIO,	
		(CASE WHEN LTRIM(RTRIM(NUMERO_UUID)) = '' THEN NULL ELSE NUMERO_UUID END) NUMERO_UUID,
		FLAG_ENVIADO,	
		CONVERT(DATE,FECHA_RECEPCION,103) FECHA_RECEPCION,	
		LTRIM(RTRIM(TIPO_DOCUMENTO)),	
		'SODISAP' ORIGEN_ETL,	
		IdPeriodo,
		Timbrado,
		TipoRebate
		FROM [dbo].[Envios_Ap_Temp] A
		WHERE
		(
			NOT EXISTS 
			( 
				SELECT 1
				FROM [dbo].[Envios_Ap_Manual] B
				WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
				AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
				AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
			)
			OR NOT EXISTS 
			( 
				SELECT 1
				FROM [dbo].[Envios_Ap] B
				WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
				AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
				AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
			)
			AND NOT EXISTS 
			( 
				SELECT 1
				FROM [SODIMAC_SAP_PROD].[dbo].[Envios_Ap] B
				WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
				AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
				AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
			)
		);
		
		INSERT INTO [SODIMAC_SAP_PROD].[dbo].[Envios_Ap]
		SELECT 
			NEWID() ID,
			EMPRESA,	
			CONVERT(DATE,FECHA_DOCUMENTO,103) FECHA_DOCUMENTO,
			LEFT(LTRIM(RTRIM(REFERENCIA_DOCUMENTO)), 20) REFERENCIA_DOCUMENTO,	
			-- LTRIM(RTRIM(NUMERO_DOCUMENTO)) NUMERO_DOCUMENTO,
			LEFT(LTRIM(RTRIM(NUMERO_DOCUMENTO)), 30) NUMERO_DOCUMENTO,
			LTRIM(RTRIM(MONEDA)) MONEDA,	
			TIPO_CAMBIO,	
			DEBITO_CREDITO,	
			CUENTA_CONTABLE,	
			CODIGO_PROVEEDOR,	
			IMPORTE,	
			SUCURSAL,	
			CONDICION_PAGO,
			CONVERT(DATE,FECHA_VENCIMIENTO,103) FECHA_VENCIMIENTO,
			NULL BLOQUEO_PAGO,	
			LEFT(LTRIM(RTRIM(SISTEMA_ORIGEN)), 30) SISTEMA_ORIGEN,
			CONVERT(DATE,FECHA_ENVIO,103) FECHA_ENVIO,
			CONVERT(DATE,FECHA_CONTABLE,103) FECHA_CONTABLE,
			CLASE_DOCUMENTO,	
			NUMERO_REFERENCIA,	
			CENTRO_COSTO,	
			CENTRO_BENEFICIO,	
			(CASE WHEN LTRIM(RTRIM(NUMERO_UUID)) = '' THEN NULL ELSE NUMERO_UUID END) NUMERO_UUID,
			FLAG_ENVIADO,	
			CONVERT(DATE,FECHA_RECEPCION,103) FECHA_RECEPCION,	
			LTRIM(RTRIM(TIPO_DOCUMENTO)),	
			'SODISAP' ORIGEN_ETL,	
			IdPeriodo
			FROM [dbo].[Envios_Ap_Temp] A
		WHERE
		(
			NOT EXISTS 
			( 
				SELECT 1
				FROM [dbo].[Envios_Ap_Manual] B
				WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
				AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
				AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
			)
			OR NOT EXISTS 
			( 
				SELECT 1
				FROM [dbo].[Envios_Ap] B
				WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
				AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
				AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
			)
			AND NOT EXISTS 
			( 
				SELECT 1
				FROM [SODIMAC_SAP_PROD].[dbo].[Envios_Ap] B
				WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
				AND B.NUMERO_DOCUMENTO   = A.NUMERO_DOCUMENTO
				AND B.NUMERO_REFERENCIA  = A.NUMERO_REFERENCIA
			)
		);

		
		SELECT TOP 1 @V_ID_PERIODO = IdPeriodo
		FROM [dbo].[Envios_Ap_Temp] 

		SELECT @V_NUM_REGISTRO = COUNT(1)
		FROM [dbo].[Envios_Ap_Temp];

	    TRUNCATE TABLE [dbo].[Envios_Ap_Temp];

		INSERT INTO controlDocumento VALUES (14,@V_NOMBRE_ARCHIVO,null,@V_ID_PERIODO,0,3,@V_IDUSUARIO,getdate(),null,null,1,@V_NUM_REGISTRO);

		
	    SET @v_RESULTADO = 1;
		COMMIT TRANSACTION;
END TRY

BEGIN CATCH
	 SET @v_RESULTADO = 4;
	 SET @V_CODIGO = @v_RESULTADO;
	 SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
	 SET @V_MENSAJE = @V_DESC+' - '+ERROR_MESSAGE();
	 ROLLBACK TRANSACTION;
	 --SELECT @v_RESULTADO CODIGO,'Error al intentar procesar las facturas' DESCRIPCION
	 TRUNCATE TABLE [dbo].[Envios_Ap_Temp];
     exec [dbo].[usp_RegistroLog] @V_MENSAJE, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
	 RETURN;
END CATCH

--SELECT @v_RESULTADO CODIGO,'Registros aplicados con exito' DESCRIPCION
SET @V_CODIGO = @v_RESULTADO;
SET @V_DESC = [dbo].[fn_obtiene_mensaje](@v_RESULTADO,null,null,null,null,null);
exec [dbo].[usp_RegistroLog] @V_DESC, @V_OBJETO, '', '', '', @V_NOMBRE_ARCHIVO, @V_IDUSUARIO;
RETURN;

END;