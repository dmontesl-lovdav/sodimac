-- ====================================================================
-- SCRIPT DE DIAGNOSTICO PARA ENCONTRAR DATOS TRUNCADOS
-- ====================================================================
-- Este script identifica qué columnas tienen datos muy largos
-- ====================================================================

PRINT '========================================';
PRINT 'DIAGNOSTICO DE DATOS TRUNCADOS';
PRINT '========================================';
PRINT '';

-- ====================================================================
-- PASO 1: Ver estructura de tabla destino SODIMAC_SAP_PROD.Envios_Ap
-- ====================================================================
PRINT '--- PASO 1: Estructura de tabla SODIMAC_SAP_PROD.Envios_Ap ---';
PRINT '';

SELECT
    COLUMN_NAME AS Columna,
    DATA_TYPE AS TipoDato,
    CHARACTER_MAXIMUM_LENGTH AS LongitudMaxima,
    IS_NULLABLE AS Nullable
FROM [SODIMAC_SAP_PROD].INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Envios_Ap'
AND TABLE_SCHEMA = 'dbo'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';

-- ====================================================================
-- PASO 2: Ver estructura de tabla origen Envios_Ap_Temp
-- ====================================================================
PRINT '--- PASO 2: Estructura de tabla Envios_Ap_Temp ---';
PRINT '';

SELECT
    COLUMN_NAME AS Columna,
    DATA_TYPE AS TipoDato,
    CHARACTER_MAXIMUM_LENGTH AS LongitudMaxima,
    IS_NULLABLE AS Nullable
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Envios_Ap_Temp'
AND TABLE_SCHEMA = 'dbo'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT '========================================';

-- ====================================================================
-- PASO 3: Analizar longitud de campos VARCHAR en Envios_Ap_Temp
-- ====================================================================
PRINT '--- PASO 3: Longitud de campos en Envios_Ap_Temp ---';
PRINT '';

SELECT
    'EMPRESA' AS Campo,
    MAX(LEN(ISNULL(EMPRESA, ''))) AS LongitudMaxima,
    MIN(LEN(ISNULL(EMPRESA, ''))) AS LongitudMinima,
    AVG(LEN(ISNULL(EMPRESA, ''))) AS LongitudPromedio
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'REFERENCIA_DOCUMENTO',
    MAX(LEN(ISNULL(LTRIM(RTRIM(REFERENCIA_DOCUMENTO)), ''))),
    MIN(LEN(ISNULL(LTRIM(RTRIM(REFERENCIA_DOCUMENTO)), ''))),
    AVG(LEN(ISNULL(LTRIM(RTRIM(REFERENCIA_DOCUMENTO)), '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'NUMERO_DOCUMENTO',
    MAX(LEN(ISNULL(LTRIM(RTRIM(NUMERO_DOCUMENTO)), ''))),
    MIN(LEN(ISNULL(LTRIM(RTRIM(NUMERO_DOCUMENTO)), ''))),
    AVG(LEN(ISNULL(LTRIM(RTRIM(NUMERO_DOCUMENTO)), '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'MONEDA',
    MAX(LEN(ISNULL(LTRIM(RTRIM(MONEDA)), ''))),
    MIN(LEN(ISNULL(LTRIM(RTRIM(MONEDA)), ''))),
    AVG(LEN(ISNULL(LTRIM(RTRIM(MONEDA)), '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'CODIGO_PROVEEDOR',
    MAX(LEN(ISNULL(CODIGO_PROVEEDOR, ''))),
    MIN(LEN(ISNULL(CODIGO_PROVEEDOR, ''))),
    AVG(LEN(ISNULL(CODIGO_PROVEEDOR, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'CUENTA_CONTABLE',
    MAX(LEN(ISNULL(CUENTA_CONTABLE, ''))),
    MIN(LEN(ISNULL(CUENTA_CONTABLE, ''))),
    AVG(LEN(ISNULL(CUENTA_CONTABLE, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'SUCURSAL',
    MAX(LEN(ISNULL(SUCURSAL, ''))),
    MIN(LEN(ISNULL(SUCURSAL, ''))),
    AVG(LEN(ISNULL(SUCURSAL, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'CONDICION_PAGO',
    MAX(LEN(ISNULL(CONDICION_PAGO, ''))),
    MIN(LEN(ISNULL(CONDICION_PAGO, ''))),
    AVG(LEN(ISNULL(CONDICION_PAGO, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'SISTEMA_ORIGEN',
    MAX(LEN(ISNULL(SISTEMA_ORIGEN, ''))),
    MIN(LEN(ISNULL(SISTEMA_ORIGEN, ''))),
    AVG(LEN(ISNULL(SISTEMA_ORIGEN, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'CLASE_DOCUMENTO',
    MAX(LEN(ISNULL(CLASE_DOCUMENTO, ''))),
    MIN(LEN(ISNULL(CLASE_DOCUMENTO, ''))),
    AVG(LEN(ISNULL(CLASE_DOCUMENTO, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'NUMERO_REFERENCIA',
    MAX(LEN(ISNULL(NUMERO_REFERENCIA, ''))),
    MIN(LEN(ISNULL(NUMERO_REFERENCIA, ''))),
    AVG(LEN(ISNULL(NUMERO_REFERENCIA, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'CENTRO_COSTO',
    MAX(LEN(ISNULL(CENTRO_COSTO, ''))),
    MIN(LEN(ISNULL(CENTRO_COSTO, ''))),
    AVG(LEN(ISNULL(CENTRO_COSTO, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'CENTRO_BENEFICIO',
    MAX(LEN(ISNULL(CENTRO_BENEFICIO, ''))),
    MIN(LEN(ISNULL(CENTRO_BENEFICIO, ''))),
    AVG(LEN(ISNULL(CENTRO_BENEFICIO, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'NUMERO_UUID',
    MAX(LEN(ISNULL(LTRIM(RTRIM(NUMERO_UUID)), ''))),
    MIN(LEN(ISNULL(LTRIM(RTRIM(NUMERO_UUID)), ''))),
    AVG(LEN(ISNULL(LTRIM(RTRIM(NUMERO_UUID)), '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'TIPO_DOCUMENTO',
    MAX(LEN(ISNULL(LTRIM(RTRIM(TIPO_DOCUMENTO)), ''))),
    MIN(LEN(ISNULL(LTRIM(RTRIM(TIPO_DOCUMENTO)), ''))),
    AVG(LEN(ISNULL(LTRIM(RTRIM(TIPO_DOCUMENTO)), '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0

UNION ALL

SELECT
    'DEBITO_CREDITO',
    MAX(LEN(ISNULL(DEBITO_CREDITO, ''))),
    MIN(LEN(ISNULL(DEBITO_CREDITO, ''))),
    AVG(LEN(ISNULL(DEBITO_CREDITO, '')))
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0;

PRINT '';
PRINT '========================================';

-- ====================================================================
-- PASO 4: Buscar registros con campos sospechosamente largos
-- ====================================================================
PRINT '--- PASO 4: Registros con campos potencialmente problematicos ---';
PRINT '';

-- Ver todos los registros con sus longitudes
SELECT TOP 10
    ID,
    LEN(ISNULL(EMPRESA, '')) AS Len_EMPRESA,
    LEN(ISNULL(LTRIM(RTRIM(REFERENCIA_DOCUMENTO)), '')) AS Len_REFERENCIA_DOCUMENTO,
    LEN(ISNULL(LTRIM(RTRIM(NUMERO_DOCUMENTO)), '')) AS Len_NUMERO_DOCUMENTO,
    LEN(ISNULL(LTRIM(RTRIM(MONEDA)), '')) AS Len_MONEDA,
    LEN(ISNULL(CODIGO_PROVEEDOR, '')) AS Len_CODIGO_PROVEEDOR,
    LEN(ISNULL(CUENTA_CONTABLE, '')) AS Len_CUENTA_CONTABLE,
    LEN(ISNULL(NUMERO_REFERENCIA, '')) AS Len_NUMERO_REFERENCIA,
    LEN(ISNULL(CENTRO_COSTO, '')) AS Len_CENTRO_COSTO,
    LEN(ISNULL(CENTRO_BENEFICIO, '')) AS Len_CENTRO_BENEFICIO,
    LEN(ISNULL(LTRIM(RTRIM(NUMERO_UUID)), '')) AS Len_NUMERO_UUID,
    LEN(ISNULL(LTRIM(RTRIM(TIPO_DOCUMENTO)), '')) AS Len_TIPO_DOCUMENTO,
    LEN(ISNULL(DEBITO_CREDITO, '')) AS Len_DEBITO_CREDITO,
    LEN(ISNULL(SISTEMA_ORIGEN, '')) AS Len_SISTEMA_ORIGEN,
    LEN(ISNULL(CLASE_DOCUMENTO, '')) AS Len_CLASE_DOCUMENTO,
    LEN(ISNULL(CONDICION_PAGO, '')) AS Len_CONDICION_PAGO,
    LEN(ISNULL(SUCURSAL, '')) AS Len_SUCURSAL
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
ORDER BY ID;

PRINT '';
PRINT '========================================';

-- ====================================================================
-- PASO 5: Mostrar valores reales de registros para inspección
-- ====================================================================
PRINT '--- PASO 5: Valores reales de los primeros 3 registros ---';
PRINT '';

SELECT TOP 3
    ID,
    EMPRESA,
    REFERENCIA_DOCUMENTO,
    NUMERO_DOCUMENTO,
    MONEDA,
    CODIGO_PROVEEDOR,
    CUENTA_CONTABLE,
    NUMERO_REFERENCIA,
    CENTRO_COSTO,
    CENTRO_BENEFICIO,
    NUMERO_UUID,
    TIPO_DOCUMENTO,
    DEBITO_CREDITO,
    SISTEMA_ORIGEN,
    CLASE_DOCUMENTO,
    CONDICION_PAGO,
    SUCURSAL
FROM [dbo].[Envios_Ap_Temp]
WHERE FLAG_ENVIADO = 0
ORDER BY ID;

PRINT '';
PRINT '========================================';
PRINT 'FIN DIAGNOSTICO';
PRINT '========================================';
PRINT '';
PRINT 'INSTRUCCIONES:';
PRINT '1. Compara la columna LongitudMaxima del PASO 1 (destino) con el PASO 3 (origen)';
PRINT '2. Si alguna LongitudMaxima del PASO 3 es mayor que la del PASO 1, ese es el problema';
PRINT '3. Revisa el PASO 5 para ver los valores reales que estan causando el problema';
