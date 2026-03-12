@echo off
REM =============================================================================
REM STM-335: Test de relacion Factura-NC, filtro y cancelacion
REM Endpoint search: POST /invoices/search
REM Endpoint status: PUT /invoices/{uuid}/status
REM Proyecto: fiscal-api (puerto 8082)
REM Fecha: 2026-03-11
REM =============================================================================

set BASE_URL=http://localhost:8082

echo.
echo =============================================================================
echo STM-335: Test de Facturas-NC (Conteo, Filtro, Cancelacion)
echo URL: %BASE_URL%
echo =============================================================================

REM =============================================================================
REM TAREA 1: CONTEO DE NCs POR FACTURA (creditNotesCount)
REM =============================================================================
echo.
echo =============================================
echo TAREA 1: CONTEO DE NCs POR FACTURA
echo =============================================

echo.
echo [TEST 1.1] Buscar Facturas - Verificar creditNotesCount
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -d "{\"fechaInicioRecepcion\": \"2025-01-01\", \"fechaFinalRecepcion\": \"2026-12-31\", \"tipoDocumento\": \"I\", \"page\": 0, \"size\": 20}"
echo.
echo.
echo ESPERADO: Cada factura debe tener campo creditNotesCount (0 o mayor)
echo.

REM =============================================================================
REM TAREA 2: FILTRAR NCs POR FACTURA (relatedInvoiceUuid)
REM =============================================================================
echo.
echo =============================================
echo TAREA 2: FILTRAR NCs POR FACTURA
echo =============================================

echo.
echo [TEST 2.1] NCs de factura FA/10002 (uuid: f0000010-0010-4010-a010-000000000010)
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -d "{\"fechaInicioRecepcion\": \"2025-01-01\", \"fechaFinalRecepcion\": \"2026-12-31\", \"tipoDocumento\": \"E\", \"relatedInvoiceUuid\": \"f0000010-0010-4010-a010-000000000010\", \"page\": 0, \"size\": 20}"
echo.
echo.
echo ESPERADO: 1 resultado - NC/50001 (total 15,000)
echo.

echo.
echo [TEST 2.2] NCs de factura MC/10003 (uuid: f0000011-0011-4011-a011-000000000011)
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -d "{\"fechaInicioRecepcion\": \"2025-01-01\", \"fechaFinalRecepcion\": \"2026-12-31\", \"tipoDocumento\": \"E\", \"relatedInvoiceUuid\": \"f0000011-0011-4011-a011-000000000011\", \"page\": 0, \"size\": 20}"
echo.
echo.
echo ESPERADO: 1 resultado - NC/50002 (total 8,500)
echo.

echo.
echo [TEST 2.3] NCs de factura inexistente
echo -----------------------------------------------------------------------------
curl -s -X POST "%BASE_URL%/invoices/search" ^
  -H "Content-Type: application/json" ^
  -d "{\"fechaInicioRecepcion\": \"2025-01-01\", \"fechaFinalRecepcion\": \"2026-12-31\", \"tipoDocumento\": \"E\", \"relatedInvoiceUuid\": \"00000000-0000-0000-0000-000000000000\", \"page\": 0, \"size\": 20}"
echo.
echo.
echo ESPERADO: 0 resultados (lista vacia, sin error)
echo.

REM =============================================================================
REM TAREA 3: CANCELACION DE NCs
REM =============================================================================
echo.
echo =============================================
echo TAREA 3: CANCELACION DE NCs
echo =============================================

echo.
echo [TEST 3.1] ERROR - Cancelar NC estatus 7 (Aplicado) - Debe dar WRN7023
echo -----------------------------------------------------------------------------
curl -s -X PUT "%BASE_URL%/invoices/e0000002-0002-4002-e002-000000000002/status" ^
  -H "Content-Type: application/json" ^
  -d "{\"numeroProveedor\": 67890, \"estatusOrigen\": 7, \"estatusDestino\": 10, \"idUsuarioActualizacion\": 99999, \"comentario\": \"Test cancelacion desde estatus 7\"}"
echo.
echo.
echo ESPERADO: ERROR WRN7023 - La nota de credito no puede cancelarse porque ya cuenta con una afectacion contable.
echo.

echo.
echo [TEST 3.2] ERROR - Cancelar NC estatus 1 (Pend. Addenda) - Debe dar WRN7023
echo -----------------------------------------------------------------------------
curl -s -X PUT "%BASE_URL%/invoices/e0000004-0004-4004-e004-000000000004/status" ^
  -H "Content-Type: application/json" ^
  -d "{\"numeroProveedor\": 1, \"estatusOrigen\": 1, \"estatusDestino\": 10, \"idUsuarioActualizacion\": 99999, \"comentario\": \"Test cancelacion desde estatus 1\"}"
echo.
echo.
echo ESPERADO: ERROR WRN7023 - La nota de credito no puede cancelarse porque ya cuenta con una afectacion contable.
echo.

echo.
echo [TEST 3.3] EXITO - Cancelar NC estatus 3 (Pend. Contabilizar)
echo -----------------------------------------------------------------------------
echo ATENCION: Este test MODIFICA datos en BD. La NC/50001 cambiara a estatus 10.
echo.
set /p CONFIRMAR="Ejecutar? (S/N): "
if /I "%CONFIRMAR%" NEQ "S" (
    echo Saltando test 3.3...
    goto FIN
)
curl -s -X PUT "%BASE_URL%/invoices/e0000001-0001-4001-e001-000000000001/status" ^
  -H "Content-Type: application/json" ^
  -d "{\"numeroProveedor\": 12345, \"estatusOrigen\": 3, \"estatusDestino\": 10, \"idUsuarioActualizacion\": 99999, \"comentario\": \"STM-335: Cancelacion exitosa desde estatus 3\"}"
echo.
echo.
echo ESPERADO: HTTP 200 - NC cancelada exitosamente (estatus -> 10)
echo.

:FIN
echo.
echo =============================================================================
echo FIN DE PRUEBAS STM-335
echo =============================================================================
pause
