@echo off
REM =============================================================================
REM STM-338: Consulta de Facturas y NC con filtros de OC y Recepcion
REM Endpoint: POST /api/invoices/search
REM Proyecto: fiscal-api
REM Fecha: 2025-12-10
REM =============================================================================

REM -----------------------------------------------------------------------------
REM CONFIGURACION - MODIFICAR SEGUN AMBIENTE
REM -----------------------------------------------------------------------------
set BASE_URL=https://dev.fbusinesscenter.com/ppsomx/fiscal
set TOKEN=TOKEN_AQUI
set RFC_EMISOR=AAA010101AAA
set FECHA_INICIO=2025-01-01
set FECHA_FIN=2025-12-31
set NO_OC=OC-2025-001234
set NO_RECEPCION=REC-2025-005678

echo.
echo =============================================================================
echo STM-338: Test de busqueda de Facturas y NC
echo URL: %BASE_URL%
echo RFC Emisor: %RFC_EMISOR%
echo Fecha: %FECHA_INICIO% a %FECHA_FIN%
echo =============================================================================

REM -----------------------------------------------------------------------------
REM TEST 1: Buscar Facturas por Orden de Compra
REM -----------------------------------------------------------------------------
echo.
echo [TEST 1] Buscar Facturas por Orden de Compra: %NO_OC%
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"rfcEmisor\": \"%RFC_EMISOR%\", \"noOrdenCompra\": \"%NO_OC%\", \"fechaInicioRecepcion\": \"%FECHA_INICIO%\", \"fechaFinalRecepcion\": \"%FECHA_FIN%\", \"tipoDocumento\": \"I\", \"page\": 0, \"size\": 20}"
echo.

REM -----------------------------------------------------------------------------
REM TEST 2: Buscar Facturas por Numero de Recepcion
REM -----------------------------------------------------------------------------
echo.
echo [TEST 2] Buscar Facturas por Recepcion: %NO_RECEPCION%
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"rfcEmisor\": \"%RFC_EMISOR%\", \"noRecepcion\": \"%NO_RECEPCION%\", \"fechaInicioRecepcion\": \"%FECHA_INICIO%\", \"fechaFinalRecepcion\": \"%FECHA_FIN%\", \"tipoDocumento\": \"I\", \"page\": 0, \"size\": 20}"
echo.

REM -----------------------------------------------------------------------------
REM TEST 3: Buscar Notas de Credito por OC y Recepcion
REM -----------------------------------------------------------------------------
echo.
echo [TEST 3] Buscar NC por OC: %NO_OC% y Recepcion: %NO_RECEPCION%
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"rfcEmisor\": \"%RFC_EMISOR%\", \"noOrdenCompra\": \"%NO_OC%\", \"noRecepcion\": \"%NO_RECEPCION%\", \"fechaInicioRecepcion\": \"%FECHA_INICIO%\", \"fechaFinalRecepcion\": \"%FECHA_FIN%\", \"tipoDocumento\": \"E\", \"page\": 0, \"size\": 20}"
echo.

REM -----------------------------------------------------------------------------
REM TEST 4: Buscar Facturas (basico sin filtros OC/Recepcion)
REM -----------------------------------------------------------------------------
echo.
echo [TEST 4] Buscar Facturas (basico)
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"rfcEmisor\": \"%RFC_EMISOR%\", \"fechaInicioRecepcion\": \"%FECHA_INICIO%\", \"fechaFinalRecepcion\": \"%FECHA_FIN%\", \"tipoDocumento\": \"I\", \"page\": 0, \"size\": 20}"
echo.

echo.
echo =============================================================================
echo FIN DE PRUEBAS
echo =============================================================================
pause
