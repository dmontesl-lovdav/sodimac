-- ====================================================================
-- SCRIPT DE DEBUG PARA sp_carga_envios_ap
-- ====================================================================
-- Ejecutar este script paso a paso para identificar dónde falla el SP
-- ====================================================================

DECLARE @DEBUG_MODE BIT = 1;  -- 1 = No ejecutar TRUNCATE, 0 = Ejecutar normal
DECLARE @V_NOMBRE_ARCHIVO VARCHAR(100) = 'Conversion_Envios_Ap_v2 varios fill rate Agosto2025.5.csv';
DECLARE @V_IDUSUARIO NUMERIC = 2;
DECLARE @V_CODIGO INT = 0;
DECLARE @V_DESC VARCHAR(100) = NULL;

-- Variables internas del SP
DECLARE @V_CONTADOR NUMERIC;
DECLARE @v_RESULTADO INT;
DECLARE @V_TIPO_DOCUMENTO VARCHAR(6);
DECLARE @V_TIPO_DOCUMENTO_DOC VARCHAR(6);
DECLARE @V_TIPO_DOCUMENTO_TEMP VARCHAR(6);
DECLARE @V_ID_PERIODO NUMERIC;
DECLARE @V_OBJETO VARCHAR(30);
DECLARE @V_MENSAJE VARCHAR(1500);
DECLARE @V_NUM_REGISTRO NUMERIC;

-- Inicialización
SET @v_RESULTADO = 0;
SET @V_ID_PERIODO = 0;
SET @V_NUM_REGISTRO = 0;
SET @V_TIPO_DOCUMENTO_TEMP = 'REMAN';
SET @V_TIPO_DOCUMENTO_DOC = '';
SET @V_OBJETO = '[dbo].[sp_carga_envios_ap]';

PRINT '========================================';
PRINT 'INICIO DEBUG SP sp_carga_envios_ap';
PRINT '========================================';
PRINT 'Archivo: ' + @V_NOMBRE_ARCHIVO;
PRINT 'Usuario: ' + CAST(@V_IDUSUARIO AS VARCHAR);
PRINT 'Modo DEBUG: ' + CASE WHEN @DEBUG_MODE = 1 THEN 'ACTIVO (No se borrarán datos)' ELSE 'INACTIVO' END;
PRINT '';

-- ====================================================================
-- PASO 1: Obtener tipo de documento configurado
-- ====================================================================
PRINT '--- PASO 1: Obtener tipo de documento configurado ---';

SET @V_TIPO_DOCUMENTO = (
    SELECT ISNULL(VALOR, @V_TIPO_DOCUMENTO_TEMP)
    FROM CatConfiguracion
    WHERE NombreVariable='TipoDocumentoAP'
);

PRINT 'Tipo de documento esperado: ' + ISNULL(@V_TIPO_DOCUMENTO, 'NULL');
PRINT '';

-- ====================================================================
-- PASO 2: Verificar registros en tabla temporal
-- ====================================================================
PRINT '--- PASO 2: Verificar registros en Envios_Ap_Temp ---';

SELECT @V_NUM_REGISTRO = COUNT(1)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0;

PRINT 'Total de registros con FLAG_ENVIADO = 0: ' + CAST(@V_NUM_REGISTRO AS VARCHAR);

IF @V_NUM_REGISTRO = 0
BEGIN
    PRINT 'ERROR: No hay registros para procesar (Código 3)';
    PRINT 'Verificar que existan datos en Envios_Ap_Temp';
END
ELSE
BEGIN
    PRINT 'OK: Hay registros para procesar';
END
PRINT '';

-- Ver algunos ejemplos
PRINT 'Primeros 5 registros:';
SELECT TOP 5
    ID,
    CODIGO_PROVEEDOR,
    NUMERO_DOCUMENTO,
    NUMERO_REFERENCIA,
    TIPO_DOCUMENTO,
    MONEDA,
    TIPO_CAMBIO,
    IdPeriodo,
    Timbrado,
    TipoRebate
FROM [dbo].[Envios_Ap_Temp];
PRINT '';

-- ====================================================================
-- PASO 3: Validar duplicados
-- ====================================================================
PRINT '--- PASO 3: Validar documentos duplicados ---';

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

PRINT 'Registros duplicados encontrados: ' + CAST(ISNULL(@V_CONTADOR, 0) AS VARCHAR);

IF @V_CONTADOR > 0
BEGIN
    PRINT 'ERROR: Existen documentos duplicados (Código 2)';
    PRINT 'Documentos duplicados:';

    SELECT TOP 10
        A.CODIGO_PROVEEDOR,
        A.NUMERO_DOCUMENTO,
        A.NUMERO_REFERENCIA,
        CASE
            WHEN EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Manual] B
                        WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
                        AND B.NUMERO_DOCUMENTO = A.NUMERO_DOCUMENTO
                        AND B.NUMERO_REFERENCIA = A.NUMERO_REFERENCIA)
            THEN 'Envios_Ap_Manual'
            WHEN EXISTS (SELECT 1 FROM [dbo].[Envios_Ap] C
                        WHERE C.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
                        AND C.NUMERO_DOCUMENTO = A.NUMERO_DOCUMENTO
                        AND C.NUMERO_REFERENCIA = A.NUMERO_REFERENCIA)
            THEN 'Envios_Ap'
            ELSE 'SODIMAC_SAP_PROD.Envios_Ap'
        END AS TablaConflicto
    FROM [dbo].[Envios_Ap_Temp] A
    WHERE FLAG_ENVIADO = 0
    AND
    (
        EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Manual] B
                WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
                AND B.NUMERO_DOCUMENTO = A.NUMERO_DOCUMENTO
                AND B.NUMERO_REFERENCIA = A.NUMERO_REFERENCIA)
        OR EXISTS (SELECT 1 FROM [dbo].[Envios_Ap] C
                WHERE C.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
                AND C.NUMERO_DOCUMENTO = A.NUMERO_DOCUMENTO
                AND C.NUMERO_REFERENCIA = A.NUMERO_REFERENCIA)
        OR EXISTS (SELECT 1 FROM [SODIMAC_SAP_PROD].[dbo].[Envios_Ap] B
                WHERE B.CODIGO_PROVEEDOR = A.CODIGO_PROVEEDOR
                AND B.NUMERO_DOCUMENTO = A.NUMERO_DOCUMENTO
                AND B.NUMERO_REFERENCIA = A.NUMERO_REFERENCIA)
    );

    IF @DEBUG_MODE = 0
    BEGIN
        PRINT 'TRUNCATE ejecutado';
    END
    ELSE
    BEGIN
        PRINT 'DEBUG MODE: TRUNCATE omitido';
    END
    -- No continuar si hay duplicados
    GOTO FIN_SCRIPT;
END
ELSE
BEGIN
    PRINT 'OK: No hay duplicados';
END
PRINT '';

-- ====================================================================
-- PASO 4: Validar tipo de documento
-- ====================================================================
PRINT '--- PASO 4: Validar tipo de documento ---';

SELECT @V_CONTADOR = COUNT(1)
FROM [dbo].[Envios_Ap_Temp] A
WHERE FLAG_ENVIADO = 0
AND TIPO_DOCUMENTO != @V_TIPO_DOCUMENTO;

PRINT 'Registros con tipo de documento inválido: ' + CAST(ISNULL(@V_CONTADOR, 0) AS VARCHAR);

IF @V_CONTADOR > 0
BEGIN
    PRINT 'ERROR: Tipo de documento inválido (Código 5)';
    PRINT 'Tipo esperado: ' + @V_TIPO_DOCUMENTO;

    SELECT TOP 5
        TIPO_DOCUMENTO,
        COUNT(*) AS Cantidad
    FROM [dbo].[Envios_Ap_Temp]
    WHERE FLAG_ENVIADO = 0
    GROUP BY TIPO_DOCUMENTO;

    GOTO FIN_SCRIPT;
END
ELSE
BEGIN
    PRINT 'OK: Tipo de documento válido';
END
PRINT '';

-- ====================================================================
-- PASO 5: Validar tipo de cambio para moneda diferente a MXN
-- ====================================================================
PRINT '--- PASO 5: Validar tipo de cambio (moneda != MXN) ---';

SELECT @V_CONTADOR = COUNT(1)
FROM [dbo].[Envios_Ap_Temp] A
WHERE MONEDA != 'MXN'
AND TIPO_CAMBIO <= 1;

PRINT 'Registros con moneda != MXN y tipo cambio <= 1: ' + CAST(ISNULL(@V_CONTADOR, 0) AS VARCHAR);

IF @V_CONTADOR > 0
BEGIN
    PRINT 'ERROR: Tipo de cambio inválido para moneda extranjera (Código 6)';

    SELECT TOP 5
        MONEDA,
        TIPO_CAMBIO,
        NUMERO_DOCUMENTO
    FROM [dbo].[Envios_Ap_Temp]
    WHERE MONEDA != 'MXN'
    AND TIPO_CAMBIO <= 1;

    GOTO FIN_SCRIPT;
END
ELSE
BEGIN
    PRINT 'OK: Tipo de cambio válido para moneda extranjera';
END
PRINT '';

-- ====================================================================
-- PASO 6: Validar tipo de cambio para MXN
-- ====================================================================
PRINT '--- PASO 6: Validar tipo de cambio (moneda = MXN) ---';

SELECT @V_CONTADOR = COUNT(1)
FROM [dbo].[Envios_Ap_Temp] A
WHERE MONEDA = 'MXN'
AND TIPO_CAMBIO != 1;

PRINT 'Registros con moneda = MXN y tipo cambio != 1: ' + CAST(ISNULL(@V_CONTADOR, 0) AS VARCHAR);

IF @V_CONTADOR > 0
BEGIN
    PRINT 'ERROR: Tipo de cambio debe ser 1 para MXN (Código 7)';

    SELECT TOP 5
        MONEDA,
        TIPO_CAMBIO,
        NUMERO_DOCUMENTO
    FROM [dbo].[Envios_Ap_Temp]
    WHERE MONEDA = 'MXN'
    AND TIPO_CAMBIO != 1;

    GOTO FIN_SCRIPT;
END
ELSE
BEGIN
    PRINT 'OK: Tipo de cambio válido para MXN';
END
PRINT '';

-- ====================================================================
-- PASO 7: Validar suma de sucursales
-- ====================================================================
PRINT '--- PASO 7: Validar suma de sucursales ---';

BEGIN TRY
    SELECT @V_CONTADOR = COUNT(1)
    FROM
    (
        SELECT
            REFERENCIA_DOCUMENTO,
            NUMERO_DOCUMENTO,
            CODIGO_PROVEEDOR,
            SUM(CONVERT(NUMERIC, SUCURSAL)) SUCURSAL
        FROM [dbo].[Envios_Ap_Temp]
        GROUP BY REFERENCIA_DOCUMENTO, NUMERO_DOCUMENTO, CODIGO_PROVEEDOR
        HAVING SUM(CONVERT(NUMERIC, SUCURSAL)) <= 0
    ) A;

    PRINT 'Registros con suma de sucursales <= 0: ' + CAST(ISNULL(@V_CONTADOR, 0) AS VARCHAR);

    IF @V_CONTADOR > 0
    BEGIN
        PRINT 'ERROR: Suma de sucursales inválida (Código 8)';

        SELECT TOP 5
            REFERENCIA_DOCUMENTO,
            NUMERO_DOCUMENTO,
            CODIGO_PROVEEDOR,
            SUM(CONVERT(NUMERIC, SUCURSAL)) AS SumaSucursal
        FROM [dbo].[Envios_Ap_Temp]
        GROUP BY REFERENCIA_DOCUMENTO, NUMERO_DOCUMENTO, CODIGO_PROVEEDOR
        HAVING SUM(CONVERT(NUMERIC, SUCURSAL)) <= 0;

        GOTO FIN_SCRIPT;
    END
    ELSE
    BEGIN
        PRINT 'OK: Suma de sucursales válida';
    END

END TRY
BEGIN CATCH
    PRINT 'ERROR: No se puede convertir SUCURSAL a numérico (Código 9)';
    PRINT 'Error SQL: ' + ERROR_MESSAGE();

    -- Buscar qué registro tiene el problema
    SELECT TOP 5
        ID,
        SUCURSAL,
        NUMERO_DOCUMENTO,
        CODIGO_PROVEEDOR
    FROM [dbo].[Envios_Ap_Temp]
    ORDER BY ID;

    GOTO FIN_SCRIPT;
END CATCH
PRINT '';

-- ====================================================================
-- PASO 8: Simular INSERT a Envios_Ap_Manual
-- ====================================================================
PRINT '--- PASO 8: Simular INSERT a Envios_Ap_Manual ---';

BEGIN TRY
    DECLARE @RegistrosAInsertar_Manual INT;

    SELECT @RegistrosAInsertar_Manual = COUNT(1)
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

    PRINT 'Registros que se insertarían en Envios_Ap_Manual: ' + CAST(@RegistrosAInsertar_Manual AS VARCHAR);

    IF @RegistrosAInsertar_Manual = 0
    BEGIN
        PRINT 'ADVERTENCIA: No hay registros nuevos para insertar';
    END
    ELSE
    BEGIN
        PRINT 'OK: Hay registros para insertar';
    END

    -- Verificar conversión de fechas
    PRINT 'Verificando conversión de fechas...';
    SELECT TOP 1
        FECHA_DOCUMENTO,
        CONVERT(DATE, FECHA_DOCUMENTO, 103) AS FECHA_DOC_CONVERTIDA,
        FECHA_VENCIMIENTO,
        CONVERT(DATE, FECHA_VENCIMIENTO, 103) AS FECHA_VENC_CONVERTIDA,
        FECHA_CONTABLE,
        CONVERT(DATE, FECHA_CONTABLE, 103) AS FECHA_CONT_CONVERTIDA,
        FECHA_ENVIO,
        CONVERT(DATE, FECHA_ENVIO, 103) AS FECHA_ENVIO_CONVERTIDA,
        FECHA_RECEPCION,
        CONVERT(DATE, FECHA_RECEPCION, 103) AS FECHA_RECEP_CONVERTIDA
    FROM [dbo].[Envios_Ap_Temp];

    PRINT 'OK: Conversión de fechas exitosa';

END TRY
BEGIN CATCH
    PRINT 'ERROR en INSERT a Envios_Ap_Manual';
    PRINT 'Error SQL: ' + ERROR_MESSAGE();
    PRINT 'Error Number: ' + CAST(ERROR_NUMBER() AS VARCHAR);
    PRINT 'Error Line: ' + CAST(ERROR_LINE() AS VARCHAR);
    GOTO FIN_SCRIPT;
END CATCH
PRINT '';

-- ====================================================================
-- PASO 9: Simular INSERT a SODIMAC_SAP_PROD.Envios_Ap
-- ====================================================================
PRINT '--- PASO 9: Simular INSERT a SODIMAC_SAP_PROD.Envios_Ap ---';

BEGIN TRY
    DECLARE @RegistrosAInsertar_SAP INT;

    -- Primero verificar acceso a la BD externa
    PRINT 'Verificando acceso a SODIMAC_SAP_PROD...';
    DECLARE @TotalSAP INT;
    SELECT @TotalSAP = COUNT(*) FROM [SODIMAC_SAP_PROD].[dbo].[Envios_Ap];
    PRINT 'Registros existentes en SODIMAC_SAP_PROD.Envios_Ap: ' + CAST(@TotalSAP AS VARCHAR);
    PRINT 'OK: Acceso a BD externa exitoso';
    PRINT '';

    SELECT @RegistrosAInsertar_SAP = COUNT(1)
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

    PRINT 'Registros que se insertarían en SODIMAC_SAP_PROD: ' + CAST(@RegistrosAInsertar_SAP AS VARCHAR);

    IF @RegistrosAInsertar_SAP = 0
    BEGIN
        PRINT 'ADVERTENCIA: No hay registros nuevos para insertar en SAP';
    END
    ELSE
    BEGIN
        PRINT 'OK: Hay registros para insertar en SAP';
    END

    -- IMPORTANTE: Verificar estructura de tabla destino
    PRINT '';
    PRINT 'Verificando estructura de tabla SODIMAC_SAP_PROD.Envios_Ap...';
    EXEC [SODIMAC_SAP_PROD].dbo.sp_help 'Envios_Ap';

END TRY
BEGIN CATCH
    PRINT 'ERROR en INSERT a SODIMAC_SAP_PROD.Envios_Ap';
    PRINT 'Error SQL: ' + ERROR_MESSAGE();
    PRINT 'Error Number: ' + CAST(ERROR_NUMBER() AS VARCHAR);
    PRINT 'Error Line: ' + CAST(ERROR_LINE() AS VARCHAR);
    PRINT '';
    PRINT 'POSIBLE CAUSA: Problema de conexión a BD externa o estructura de tabla diferente';
    GOTO FIN_SCRIPT;
END CATCH
PRINT '';

-- ====================================================================
-- PASO 10: Verificar INSERT a controlDocumento
-- ====================================================================
PRINT '--- PASO 10: Verificar INSERT a controlDocumento ---';

BEGIN TRY
    SELECT TOP 1 @V_ID_PERIODO = IdPeriodo
    FROM [dbo].[Envios_Ap_Temp];

    SELECT @V_NUM_REGISTRO = COUNT(1)
    FROM [dbo].[Envios_Ap_Temp];

    PRINT 'IdPeriodo: ' + CAST(@V_ID_PERIODO AS VARCHAR);
    PRINT 'Total registros: ' + CAST(@V_NUM_REGISTRO AS VARCHAR);

    -- Verificar estructura de controlDocumento
    PRINT 'Estructura esperada de INSERT:';
    PRINT 'INSERT INTO controlDocumento VALUES (14, archivo, null, periodo, 0, 3, usuario, fecha, null, null, 1, numRegistros)';

    PRINT 'OK: Datos para controlDocumento preparados';

END TRY
BEGIN CATCH
    PRINT 'ERROR en preparación de datos para controlDocumento';
    PRINT 'Error SQL: ' + ERROR_MESSAGE();
END CATCH
PRINT '';

FIN_SCRIPT:
PRINT '';
PRINT '========================================';
PRINT 'FIN DEBUG SP sp_carga_envios_ap';
PRINT '========================================';
PRINT 'Código resultado: ' + CAST(@v_RESULTADO AS VARCHAR);
PRINT 'Descripción: ' + ISNULL(@V_DESC, 'Sin errores detectados hasta este punto');
PRINT '';
