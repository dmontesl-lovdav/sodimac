-- ====================================================================
-- SCRIPT DE DIAGNOSTICO PARA ENCONTRAR DATOS TRUNCADOS
-- ====================================================================
-- Este script identifica qué columnas tienen datos muy largos
-- que pueden causar el error "String or binary data would be truncated"
-- ====================================================================

PRINT '========================================';
PRINT 'DIAGNOSTICO DE DATOS TRUNCADOS';
PRINT '========================================';
PRINT '';

-- ====================================================================
-- PASO 1: Ver estructura de tabla destino SODIMAC_SAP_PROD.Envios_Ap
-- ====================================================================
PRINT '--- Estructura de tabla SODIMAC_SAP_PROD.Envios_Ap ---';
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM [SODIMAC_SAP_PROD].INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Envios_Ap'
ORDER BY ORDINAL_POSITION;
PRINT '';

-- ====================================================================
-- PASO 2: Ver estructura de tabla origen Envios_Ap_Temp
-- ====================================================================
PRINT '--- Estructura de tabla Envios_Ap_Temp ---';
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Envios_Ap_Temp'
ORDER BY ORDINAL_POSITION;
PRINT '';

-- ====================================================================
-- PASO 3: Verificar longitud de datos en Envios_Ap_Temp
-- ====================================================================
PRINT '--- Verificando longitud de campos VARCHAR/CHAR en Envios_Ap_Temp ---';

SELECT
    'EMPRESA' AS Campo,
    MAX(LEN(EMPRESA)) AS LongitudMaxima,
    COUNT(*) AS TotalRegistros
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'REFERENCIA_DOCUMENTO',
    MAX(LEN(LTRIM(RTRIM(REFERENCIA_DOCUMENTO)))),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'NUMERO_DOCUMENTO',
    MAX(LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO)))),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'MONEDA',
    MAX(LEN(LTRIM(RTRIM(MONEDA)))),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'DEBITO_CREDITO',
    MAX(LEN(DEBITO_CREDITO)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'CUENTA_CONTABLE',
    MAX(LEN(CUENTA_CONTABLE)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'CODIGO_PROVEEDOR',
    MAX(LEN(CODIGO_PROVEEDOR)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'SUCURSAL',
    MAX(LEN(SUCURSAL)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'CONDICION_PAGO',
    MAX(LEN(CONDICION_PAGO)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'SISTEMA_ORIGEN',
    MAX(LEN(SISTEMA_ORIGEN)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'CLASE_DOCUMENTO',
    MAX(LEN(CLASE_DOCUMENTO)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'NUMERO_REFERENCIA',
    MAX(LEN(NUMERO_REFERENCIA)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'CENTRO_COSTO',
    MAX(LEN(CENTRO_COSTO)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'CENTRO_BENEFICIO',
    MAX(LEN(CENTRO_BENEFICIO)),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'NUMERO_UUID',
    MAX(LEN(LTRIM(RTRIM(NUMERO_UUID)))),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
UNION ALL
SELECT
    'TIPO_DOCUMENTO',
    MAX(LEN(LTRIM(RTRIM(TIPO_DOCUMENTO)))),
    COUNT(*)
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0;

PRINT '';

-- ====================================================================
-- PASO 4: Buscar registros específicos con campos muy largos
-- ====================================================================
PRINT '--- Registros con campos potencialmente problemáticos ---';

-- Verificar REFERENCIA_DOCUMENTO > 16 caracteres (ejemplo, ajusta según estructura)
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(LTRIM(RTRIM(REFERENCIA_DOCUMENTO))) > 16)
BEGIN
    PRINT 'ALERTA: Registros con REFERENCIA_DOCUMENTO > 16 caracteres:';
    SELECT TOP 5
        ID,
        REFERENCIA_DOCUMENTO,
        LEN(LTRIM(RTRIM(REFERENCIA_DOCUMENTO))) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(LTRIM(RTRIM(REFERENCIA_DOCUMENTO))) > 16
    AND FLAG_ENVIADO = 0;
END

-- Verificar NUMERO_DOCUMENTO > 16 caracteres
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) > 16)
BEGIN
    PRINT 'ALERTA: Registros con NUMERO_DOCUMENTO > 16 caracteres:';
    SELECT TOP 5
        ID,
        NUMERO_DOCUMENTO,
        LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) > 16
    AND FLAG_ENVIADO = 0;
END

-- Verificar NUMERO_REFERENCIA > 16 caracteres
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(NUMERO_REFERENCIA) > 16)
BEGIN
    PRINT 'ALERTA: Registros con NUMERO_REFERENCIA > 16 caracteres:';
    SELECT TOP 5
        ID,
        NUMERO_REFERENCIA,
        LEN(NUMERO_REFERENCIA) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(NUMERO_REFERENCIA) > 16
    AND FLAG_ENVIADO = 0;
END

-- Verificar NUMERO_UUID muy largo
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(LTRIM(RTRIM(NUMERO_UUID))) > 50)
BEGIN
    PRINT 'ALERTA: Registros con NUMERO_UUID > 50 caracteres:';
    SELECT TOP 5
        ID,
        NUMERO_UUID,
        LEN(LTRIM(RTRIM(NUMERO_UUID))) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(LTRIM(RTRIM(NUMERO_UUID))) > 50
    AND FLAG_ENVIADO = 0;
END

-- Verificar CODIGO_PROVEEDOR muy largo
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(CODIGO_PROVEEDOR) > 10)
BEGIN
    PRINT 'ALERTA: Registros con CODIGO_PROVEEDOR > 10 caracteres:';
    SELECT TOP 5
        ID,
        CODIGO_PROVEEDOR,
        LEN(CODIGO_PROVEEDOR) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(CODIGO_PROVEEDOR) > 10
    AND FLAG_ENVIADO = 0;
END

-- Verificar CUENTA_CONTABLE muy largo
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(CUENTA_CONTABLE) > 10)
BEGIN
    PRINT 'ALERTA: Registros con CUENTA_CONTABLE > 10 caracteres:';
    SELECT TOP 5
        ID,
        CUENTA_CONTABLE,
        LEN(CUENTA_CONTABLE) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(CUENTA_CONTABLE) > 10
    AND FLAG_ENVIADO = 0;
END

-- Verificar CENTRO_COSTO muy largo
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(CENTRO_COSTO) > 10)
BEGIN
    PRINT 'ALERTA: Registros con CENTRO_COSTO > 10 caracteres:';
    SELECT TOP 5
        ID,
        CENTRO_COSTO,
        LEN(CENTRO_COSTO) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(CENTRO_COSTO) > 10
    AND FLAG_ENVIADO = 0;
END

-- Verificar CENTRO_BENEFICIO muy largo
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(CENTRO_BENEFICIO) > 10)
BEGIN
    PRINT 'ALERTA: Registros con CENTRO_BENEFICIO > 10 caracteres:';
    SELECT TOP 5
        ID,
        CENTRO_BENEFICIO,
        LEN(CENTRO_BENEFICIO) AS Longitud
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(CENTRO_BENEFICIO) > 10
    AND FLAG_ENVIADO = 0;
END

PRINT '';
PRINT '========================================';
PRINT 'FIN DIAGNOSTICO';
PRINT '========================================';
