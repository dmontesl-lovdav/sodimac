-- ====================================================================
-- DIAGNOSTICO PRECISO: Campos que SE DESBORDAN actualmente
-- ====================================================================
PRINT '========================================';
PRINT 'CAMPOS QUE EXCEDEN EL LIMITE';
PRINT '========================================';
PRINT '';

-- Ver SOLO los campos que actualmente exceden el límite
SELECT
    'NUMERO_DOCUMENTO' AS Campo,
    30 AS Limite,
    COUNT(*) AS CantidadRegistrosProblematicos,
    MAX(LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO)))) AS LongitudMaxima
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) > 30
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'SISTEMA_ORIGEN',
    30,
    COUNT(*),
    MAX(LEN(SISTEMA_ORIGEN))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(SISTEMA_ORIGEN) > 30
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'REFERENCIA_DOCUMENTO',
    20,
    COUNT(*),
    MAX(LEN(LTRIM(RTRIM(REFERENCIA_DOCUMENTO))))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(LTRIM(RTRIM(REFERENCIA_DOCUMENTO))) > 20
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'CODIGO_PROVEEDOR',
    10,
    COUNT(*),
    MAX(LEN(CODIGO_PROVEEDOR))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(CODIGO_PROVEEDOR) > 10
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'NUMERO_REFERENCIA',
    18,
    COUNT(*),
    MAX(LEN(NUMERO_REFERENCIA))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(NUMERO_REFERENCIA) > 18
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'CENTRO_COSTO',
    10,
    COUNT(*),
    MAX(LEN(CENTRO_COSTO))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(CENTRO_COSTO) > 10
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'CENTRO_BENEFICIO',
    10,
    COUNT(*),
    MAX(LEN(CENTRO_BENEFICIO))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(CENTRO_BENEFICIO) > 10
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0

UNION ALL

SELECT
    'CUENTA_CONTABLE',
    12,
    COUNT(*),
    MAX(LEN(CUENTA_CONTABLE))
FROM [dbo].[Envios_Ap_Temp]
WHERE LEN(CUENTA_CONTABLE) > 12
AND FLAG_ENVIADO = 0
HAVING COUNT(*) > 0;

PRINT '';
PRINT '--- EJEMPLOS DE VALORES PROBLEMATICOS ---';
PRINT '';

-- Mostrar ejemplos de NUMERO_DOCUMENTO si excede
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) > 30 AND FLAG_ENVIADO = 0)
BEGIN
    PRINT 'NUMERO_DOCUMENTO (limite: 30):';
    SELECT TOP 3
        NUMERO_DOCUMENTO AS ValorCompleto,
        LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) AS Longitud,
        LEFT(LTRIM(RTRIM(NUMERO_DOCUMENTO)), 30) AS ValorTruncado
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(LTRIM(RTRIM(NUMERO_DOCUMENTO))) > 30
    AND FLAG_ENVIADO = 0;
    PRINT '';
END

-- Mostrar ejemplos de SISTEMA_ORIGEN si excede
IF EXISTS (SELECT 1 FROM [dbo].[Envios_Ap_Temp] WHERE LEN(SISTEMA_ORIGEN) > 30 AND FLAG_ENVIADO = 0)
BEGIN
    PRINT 'SISTEMA_ORIGEN (limite: 30):';
    SELECT TOP 3
        SISTEMA_ORIGEN AS ValorCompleto,
        LEN(SISTEMA_ORIGEN) AS Longitud,
        LEFT(LTRIM(RTRIM(SISTEMA_ORIGEN)), 30) AS ValorTruncado
    FROM [dbo].[Envios_Ap_Temp]
    WHERE LEN(SISTEMA_ORIGEN) > 30
    AND FLAG_ENVIADO = 0;
    PRINT '';
END

PRINT '========================================';
PRINT 'FIN DIAGNOSTICO';
PRINT '========================================';
PRINT '';
PRINT 'INSTRUCCIONES:';
PRINT '1. Si NO aparece ningun campo arriba, entonces no hay problemas de desbordamiento';
PRINT '2. Si aparece algun campo, decide si se puede truncar o si es clave para otros procesos';
PRINT '3. Para campos clave (como NUMERO_DOCUMENTO), considera modificar la tabla destino';
