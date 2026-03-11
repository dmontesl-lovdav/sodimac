@echo off
REM ============================================
REM Test endpoints ETL - Fiscal API (BFF DEV)
REM ============================================

set BFF=https://dev.fbusinesscenter.com/ppsomx/fiscal
set LOG=C:\log

if not exist %LOG% mkdir %LOG%

echo.
echo === 1. Registrar Factura ===
curl -s -X POST "%BFF%/invoices/register" -F "file=@%LOG%\factura.xml" -o %LOG%\resp-factura.json
type %LOG%\resp-factura.json
echo.

echo.
echo === 2. Registrar Nota de Credito ===
curl -s -X POST "%BFF%/invoices/register" -F "file=@%LOG%\nota-credito.xml" -o %LOG%\resp-nc.json
type %LOG%\resp-nc.json
echo.

echo.
echo === 3. Registrar Complemento de Pago ===
curl -s -X POST "%BFF%/fiscal/complementos-pago/registrar" -F "xmlFile=@%LOG%\complemento-pago.xml" -F "tipoAddenda=5" -F "idProveedor=12345" -F "tipoProveedor=SLI" -F "idUsuario=1" -o %LOG%\resp-complemento.json
type %LOG%\resp-complemento.json
echo.

echo.
echo === 4. Buscar Facturas ===
curl -s -X POST "%BFF%/invoices/search" -H "Content-Type: application/json" -d "{\"tipoDocumento\":\"I\",\"fechaInicioRecepcion\":\"2025-06-01\",\"fechaFinalRecepcion\":\"2025-12-01\",\"page\":0,\"size\":5}" -o %LOG%\resp-search-facturas.json
type %LOG%\resp-search-facturas.json
echo.

echo.
echo === 5. Buscar Notas de Credito ===
curl -s -X POST "%BFF%/invoices/search" -H "Content-Type: application/json" -d "{\"tipoDocumento\":\"E\",\"fechaInicioRecepcion\":\"2025-06-01\",\"fechaFinalRecepcion\":\"2025-12-01\",\"page\":0,\"size\":5}" -o %LOG%\resp-search-nc.json
type %LOG%\resp-search-nc.json
echo.

echo.
echo === 6. Buscar Complementos de Pago ===
curl -s "%BFF%/fiscal/complementos-pago/buscar?rfcEmisor=&page=0&size=5" -o %LOG%\resp-search-complementos.json
type %LOG%\resp-search-complementos.json
echo.

echo.
echo === Respuestas guardadas en %LOG% ===
pause
