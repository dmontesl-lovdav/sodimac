@echo off
REM ============================================
REM Script de prueba VERBOSE - BFF Fiscal
REM Con manejo de errores y diagnostico
REM ============================================
setlocal enabledelayedexpansion

set BFF_URL=http://localhost:3000
set API_URL=http://localhost:8082
set AUTH_HEADER=Authorization: Bearer dummy-token

echo ============================================
echo PRUEBA DE ENDPOINTS - BFF FISCAL (VERBOSE)
echo ============================================
echo.
echo BFF URL: %BFF_URL%
echo API URL: %API_URL%
echo.

REM ============================================
REM VERIFICACION PREVIA
REM ============================================
echo ============================================
echo VERIFICACION DE SERVICIOS
echo ============================================
echo.

echo [CHECK 1] Verificando BFF (puerto 3000)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" --max-time 5 %BFF_URL%/health
if errorlevel 1 (
    echo [ERROR] BFF no esta respondiendo en puerto 3000
    echo.
    echo Soluciones:
    echo 1. Verifica que el BFF este corriendo: npm start
    echo 2. Verifica que este en el puerto 3000
    echo 3. Revisa los logs del BFF
    echo.
    pause
    exit /b 1
) else (
    echo [OK] BFF esta respondiendo
)
echo.

echo [CHECK 2] Verificando Fiscal-API (puerto 8082)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" --max-time 5 %API_URL%/actuator/health
if errorlevel 1 (
    echo [ERROR] Fiscal-API no esta respondiendo en puerto 8082
    echo.
    echo Soluciones:
    echo 1. Verifica que fiscal-api este corriendo
    echo 2. Verifica que este en el puerto 8082
    echo 3. Revisa los logs de fiscal-api
    echo.
    echo NOTA: El BFF funciona pero no puede comunicarse con fiscal-api
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Fiscal-API esta respondiendo
)
echo.
echo ============================================
echo.

REM ============================================
REM 1. HEALTH CHECKS
REM ============================================
echo ============================================
echo PRUEBAS DE HEALTH CHECK
echo ============================================
echo.

echo [1/10] Health Check del BFF (sin auth)...
echo URL: %BFF_URL%/health
curl -s -w "\nHTTP Status: %%{http_code}\n" %BFF_URL%/health
echo.
echo -------------------------------------------
echo.

echo [2/10] Health Check del XML Processor (con auth)...
echo URL: %BFF_URL%/api/v1/fiscal/api/fiscal/xml/health
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" %BFF_URL%/api/v1/fiscal/api/fiscal/xml/health
echo.
echo -------------------------------------------
echo.

echo [3/10] Actuator Health (con auth)...
echo URL: %BFF_URL%/api/v1/fiscal/actuator/health
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" %BFF_URL%/api/v1/fiscal/actuator/health
echo.
echo -------------------------------------------
echo.

REM ============================================
REM 2. CATALOGOS FISCALES
REM ============================================
echo ============================================
echo PRUEBAS DE CATALOGOS FISCALES
echo ============================================
echo.

echo [4/10] Catalogo de PACs...
echo URL: %BFF_URL%/api/v1/fiscal/api/pac-catalog
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" %BFF_URL%/api/v1/fiscal/api/pac-catalog
echo.
echo -------------------------------------------
echo.

echo [5/10] Emisores...
echo URL: %BFF_URL%/api/v1/fiscal/api/issuers
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" %BFF_URL%/api/v1/fiscal/api/issuers
echo.
echo -------------------------------------------
echo.

echo [6/10] Receptores...
echo URL: %BFF_URL%/api/v1/fiscal/api/receivers
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" %BFF_URL%/api/v1/fiscal/api/receivers
echo.
echo -------------------------------------------
echo.

echo [7/10] Versiones CFDI...
echo URL: %BFF_URL%/api/v1/fiscal/api/version-catalog?page=0^&size=5
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" "%BFF_URL%/api/v1/fiscal/api/version-catalog?page=0&size=5"
echo.
echo -------------------------------------------
echo.

echo [8/10] Receptores Autorizados...
echo URL: %BFF_URL%/api/v1/fiscal/api/authorized-receivers?page=0^&size=5
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" "%BFF_URL%/api/v1/fiscal/api/authorized-receivers?page=0&size=5"
echo.
echo -------------------------------------------
echo.

REM ============================================
REM 3. CONSULTAS
REM ============================================
echo ============================================
echo PRUEBAS DE CONSULTAS
echo ============================================
echo.

echo [9/10] Logs del Sistema...
echo URL: %BFF_URL%/api/v1/fiscal/api/logs?page=0^&size=5
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" "%BFF_URL%/api/v1/fiscal/api/logs?page=0&size=5"
echo.
echo -------------------------------------------
echo.

echo [10/10] Busqueda de Complementos...
echo URL: %BFF_URL%/api/v1/fiscal/api/fiscal/complementos-pago/buscar?page=0^&size=5
curl -s -w "\nHTTP Status: %%{http_code}\n" -H "%AUTH_HEADER%" "%BFF_URL%/api/v1/fiscal/api/fiscal/complementos-pago/buscar?page=0&size=5"
echo.
echo -------------------------------------------
echo.

REM ============================================
REM RESUMEN
REM ============================================
echo ============================================
echo PRUEBAS COMPLETADAS
echo ============================================
echo.
echo Revisa los HTTP Status de cada endpoint:
echo - 200: Exitoso
echo - 404: Endpoint no encontrado (verifica la URL)
echo - 500: Error interno del servidor
echo - 000: No se pudo conectar al servicio
echo.
echo Si todos devuelven 200, el BFF esta funcionando correctamente.
echo.

pause
