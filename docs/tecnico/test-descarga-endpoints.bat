@echo off
REM ============================================
REM Test endpoints Descarga - Fiscal API (BFF DEV)
REM ============================================
REM Cambiar UUID_FISCAL y UUID_INTERNO por valores reales

set BFF=https://dev.fbusinesscenter.com/ppsomx/fiscal
set LOG=C:\log
set UUID_FISCAL=aabb0001-ae01-4000-a000-000000000001
set UUID_INTERNO=41c5d355-4089-41d1-896a-1f9f9da73798

if not exist %LOG% mkdir %LOG%

echo.
echo === 1. Descargar XML individual ===
curl -s "%BFF%/invoices/%UUID_FISCAL%/xml?download=true" -o %LOG%\factura.xml
echo Guardado en %LOG%\factura.xml
echo.

echo.
echo === 2. Descargar PDF individual ===
curl -s "%BFF%/api/fiscal/pdf/from-fiscal-uuid/%UUID_FISCAL%?inline=false" -o %LOG%\factura.pdf
echo Guardado en %LOG%\factura.pdf
echo.

echo.
echo === 3. Generar PDF desde archivo XML ===
curl -s -X POST "%BFF%/api/fiscal/pdf/from-file?inline=false" -F "file=@%LOG%\factura.xml" -o %LOG%\factura-desde-xml.pdf
echo Guardado en %LOG%\factura-desde-xml.pdf
echo.

echo.
echo === 4. Descarga masiva XMLs (ZIP) ===
curl -s -X POST "%BFF%/invoices/download/xml" -H "Content-Type: application/json" -d "{\"invoiceUuids\":[\"%UUID_INTERNO%\"],\"documentType\":\"I\"}" -o %LOG%\facturas-xml.zip
echo Guardado en %LOG%\facturas-xml.zip
echo.

echo.
echo === 5. Descarga masiva PDFs (ZIP) ===
curl -s -X POST "%BFF%/invoices/download/pdf" -H "Content-Type: application/json" -d "{\"invoiceUuids\":[\"%UUID_INTERNO%\"],\"documentType\":\"I\"}" -o %LOG%\facturas-pdf.zip
echo Guardado en %LOG%\facturas-pdf.zip
echo.

echo.
echo === 6. Exportar CSV ===
curl -s -X POST "%BFF%/invoices/export/csv" -H "Content-Type: application/json" -d "{\"tipoDocumento\":\"I\",\"fechaInicioRecepcion\":\"2025-06-01\",\"fechaFinalRecepcion\":\"2025-12-01\"}" -o %LOG%\facturas.csv
echo Guardado en %LOG%\facturas.csv
echo.

echo.
echo === 7. Exportar XLSX ===
curl -s -X POST "%BFF%/invoices/export/xlsx" -H "Content-Type: application/json" -d "{\"tipoDocumento\":\"I\",\"fechaInicioRecepcion\":\"2025-06-01\",\"fechaFinalRecepcion\":\"2025-12-01\"}" -o %LOG%\facturas.xlsx
echo Guardado en %LOG%\facturas.xlsx
echo.

echo.
echo === Archivos guardados en %LOG% ===
dir %LOG%\factura.xml %LOG%\factura.pdf %LOG%\facturas-xml.zip %LOG%\facturas-pdf.zip %LOG%\facturas.csv %LOG%\facturas.xlsx 2>nul
echo.
pause
