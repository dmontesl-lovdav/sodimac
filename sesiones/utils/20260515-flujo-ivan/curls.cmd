@echo off
REM ============================================================================
REM Flujo demo Pago + Descuento Comercial para Ivan
REM Ejecutar en PC Sodimac (con acceso a UAT)
REM Fecha: 2026-05-15
REM Validado en local antes de UAT.
REM ============================================================================
REM
REM Escenario:
REM   Proveedor: vendor 34786 (Distribuidora Mexicana)
REM   OC: PO-2026-001234 por $116,000 MXN
REM   Factura: FAC-SOD-A-12345
REM   Acuerdo Q2-2026: 5%% bonificacion = $5,800 descuento
REM   Pago neto: $110,200 ($116,000 - $5,800)
REM
REM Resultado esperado:
REM   - 1 pago en tenant_finance.fiscal_payments
REM   - 1 stamped_rebate + 1 rebate en tenant_finance
REM   - GET /rebates regresa el rebate creado
REM ============================================================================

set BASE=https://uat.fbusinesscenter.com/ppsomx/backend-finanzas

echo.
echo === 1. POST /fiscal-payments (alta de pago) ===
curl -i -X POST "%BASE%/fiscal-payments" -H "Content-Type: application/json" --data-binary "@body-pago.json"
echo.

echo.
echo === 2. POST /stamped-rebates (prerequisito FK) ===
curl -i -X POST "%BASE%/stamped-rebates" -H "Content-Type: application/json" --data-binary "@body-stamped-rebate.json"
echo.

echo.
echo === 3. POST /rebates (alta descuento comercial) ===
curl -i -X POST "%BASE%/rebates" -H "Content-Type: application/json" --data-binary "@body-rebate.json"
echo.

echo.
echo === 4. GET /rebates (verificacion - endpoint que Ivan paso) ===
curl -i "%BASE%/rebates?pageNumber=1&pageSize=10"
echo.

echo.
echo === FIN ===
echo Si paso 3 retorna 400 con codigo 23502 (NOT NULL violation):
echo   - UAT tiene el bug de mapeo entity/schema de /rebates
echo   - Esperar deploy del fix (rebate.entity.ts: renombrar 3 props)
echo   - O usar POST /rebates/relate (que requiere XML de NC)
