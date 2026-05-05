# STM-1461 — Guías Carta Porte (Shipping Guide) — Pruebas filtro de seguridad (PowerShell)
# finanzas-api: http://localhost:3001/api
# Params opcionales: from, to, vendorNumber

$BASE = "http://localhost:3001/api"
$PARAMS = "from=2024-01-01&to=2025-12-31"

Write-Host "=========================================="
Write-Host "ESCENARIO 1: vendor con datos (actualizar con vendor real)"
Write-Host "=========================================="
(Invoke-RestMethod -Uri "$BASE/shipping-guide?$PARAMS" -Method GET -Headers @{"x-user-vendors"="VENDOR_REAL"}).data.Count
# Esperado: N registros (correr query 2 para saber vendor real)

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 2: multi-vendor (actualizar con vendors reales)"
Write-Host "=========================================="
(Invoke-RestMethod -Uri "$BASE/shipping-guide?$PARAMS" -Method GET -Headers @{"x-user-vendors"="VENDOR1,VENDOR2"}).data.Count
# Esperado: sum de ambos vendors

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 3: acceso total -1"
Write-Host "=========================================="
(Invoke-RestMethod -Uri "$BASE/shipping-guide?$PARAMS" -Method GET -Headers @{"x-user-vendors"="-1"}).data.Count
# Esperado: todas las guias sin restriccion

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 4: sin atributos -> WRN7029"
Write-Host "=========================================="
try {
    Invoke-RestMethod -Uri "$BASE/shipping-guide?$PARAMS" -Method GET -Headers @{"x-user-vendors"=""}
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    $body | ConvertFrom-Json | Select-Object code, message, success
}
# Esperado: HTTP 400, code=WRN7029
