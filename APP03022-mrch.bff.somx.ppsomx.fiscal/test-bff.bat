@echo off
REM ============================================
REM MENU PRINCIPAL - PRUEBAS BFF FISCAL
REM ============================================
setlocal enabledelayedexpansion

:MENU
cls
echo ============================================
echo    PRUEBAS BFF FISCAL - MENU PRINCIPAL
echo ============================================
echo.
echo    BFF URL: http://localhost:3000
echo    Fiscal API: http://localhost:8082
echo.
echo ============================================
echo.
echo    1. Ejecutar TODAS las pruebas basicas
echo    2. Pruebas de Health Check (rapido)
echo    3. Pruebas de Catalogos
echo    4. Busqueda de Complementos (simple)
echo    5. Busqueda Avanzada (multiples filtros)
echo    6. Subir y procesar XML (requiere archivo)
echo    7. Ver configuracion del BFF
echo    8. Probar conexion al BFF
echo    9. Salir
echo.
echo ============================================
echo.

set /p OPCION="Selecciona una opcion (1-9): "

if "%OPCION%"=="1" goto TODAS
if "%OPCION%"=="2" goto HEALTH
if "%OPCION%"=="3" goto CATALOGOS
if "%OPCION%"=="4" goto BUSQUEDA_SIMPLE
if "%OPCION%"=="5" goto BUSQUEDA_AVANZADA
if "%OPCION%"=="6" goto UPLOAD_XML
if "%OPCION%"=="7" goto CONFIG
if "%OPCION%"=="8" goto TEST_CONEXION
if "%OPCION%"=="9" goto SALIR

echo Opcion invalida. Presiona cualquier tecla para continuar...
pause >nul
goto MENU

:TODAS
cls
echo Ejecutando TODAS las pruebas basicas...
echo.
call test-endpoints.bat
goto MENU

:HEALTH
cls
echo ============================================
echo PRUEBAS DE HEALTH CHECK
echo ============================================
echo.
set AUTH_HEADER=Authorization: Bearer dummy-token

echo [1/3] Health Check del BFF (sin auth)...
curl -s http://localhost:3000/health
echo.
echo.

echo [2/3] Health Check XML Processor (con auth)...
curl -s -H "%AUTH_HEADER%" http://localhost:3000/api/v1/fiscal/api/fiscal/xml/health
echo.
echo.

echo [3/3] Actuator Health (con auth)...
curl -s -H "%AUTH_HEADER%" http://localhost:3000/api/v1/fiscal/actuator/health
echo.
echo.

pause
goto MENU

:CATALOGOS
cls
echo ============================================
echo PRUEBAS DE CATALOGOS FISCALES
echo ============================================
echo.
set AUTH_HEADER=Authorization: Bearer dummy-token

echo [1/5] Catalogo de PACs...
curl -s -H "%AUTH_HEADER%" http://localhost:3000/api/v1/fiscal/api/pac-catalog
echo.
echo.

echo [2/5] Emisores...
curl -s -H "%AUTH_HEADER%" http://localhost:3000/api/v1/fiscal/api/issuers
echo.
echo.

echo [3/5] Receptores...
curl -s -H "%AUTH_HEADER%" http://localhost:3000/api/v1/fiscal/api/receivers
echo.
echo.

echo [4/5] Versiones CFDI...
curl -s -H "%AUTH_HEADER%" "http://localhost:3000/api/v1/fiscal/api/version-catalog?page=0&size=5"
echo.
echo.

echo [5/5] Receptores Autorizados...
curl -s -H "%AUTH_HEADER%" "http://localhost:3000/api/v1/fiscal/api/authorized-receivers?page=0&size=5"
echo.
echo.

pause
goto MENU

:BUSQUEDA_SIMPLE
cls
echo ============================================
echo BUSQUEDA SIMPLE DE COMPLEMENTOS DE PAGO
echo ============================================
echo.
set AUTH_HEADER=Authorization: Bearer dummy-token

echo Buscando complementos de pago (todos, primeras 10)...
curl -s -H "%AUTH_HEADER%" "http://localhost:3000/api/v1/fiscal/api/fiscal/complementos-pago/buscar?page=0&size=10"
echo.
echo.

pause
goto MENU

:BUSQUEDA_AVANZADA
cls
echo Ejecutando busquedas avanzadas...
echo.
call test-search-advanced.bat
goto MENU

:UPLOAD_XML
cls
echo Ejecutando pruebas de subida de XML...
echo.
call test-upload-xml.bat
goto MENU

:CONFIG
cls
echo ============================================
echo CONFIGURACION DEL BFF
echo ============================================
echo.
if exist .env (
    type .env
) else (
    echo ERROR: No se encuentra el archivo .env
)
echo.
echo ============================================
echo.
pause
goto MENU

:TEST_CONEXION
cls
echo ============================================
echo PRUEBA DE CONEXION AL BFF
echo ============================================
echo.
echo Probando conexion a http://localhost:3000/health...
echo.

curl -s http://localhost:3000/health

if %ERRORLEVEL% EQU 0 (
    echo.
    echo.
    echo [OK] BFF esta respondiendo correctamente!
) else (
    echo.
    echo.
    echo [ERROR] No se puede conectar al BFF.
    echo.
    echo Verifica que:
    echo 1. El BFF este corriendo (npm start)
    echo 2. Este escuchando en el puerto 3000
    echo 3. No haya firewall bloqueando la conexion
)
echo.
echo.
pause
goto MENU

:SALIR
cls
echo.
echo Saliendo...
echo.
exit /b 0
