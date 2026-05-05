# STM-321 — Three Way Match — Pruebas filtro de seguridad (PowerShell)
# finanzas-api: http://localhost:3001/api
# Params obligatorios: tipoFecha, fechaInicio, fechaFin

$BASE = "http://localhost:3001/api"
$PARAMS = "tipoFecha=fechaRecepcion&fechaInicio=2024-01-01&fechaFin=2025-12-31"

Write-Host "=========================================="
Write-Host "ESCENARIO 1: vendor con datos (actualizar con vendor real)"
Write-Host "=========================================="
(Invoke-RestMethod -Uri "$BASE/three-way-match?$PARAMS" -Method GET -Headers @{"x-user-vendors"="VENDOR_REAL"}).total
# Esperado: N registros (correr query 2 para saber vendor real)

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 2: multi-vendor (actualizar con vendors reales)"
Write-Host "=========================================="
(Invoke-RestMethod -Uri "$BASE/three-way-match?$PARAMS" -Method GET -Headers @{"x-user-vendors"="VENDOR1,VENDOR2"}).total
# Esperado: sum de ambos vendors

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 3: acceso total -1"
Write-Host "=========================================="
(Invoke-RestMethod -Uri "$BASE/three-way-match?$PARAMS" -Method GET -Headers @{"x-user-vendors"="-1"}).total
# Esperado: total sin filtro

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 4: sin atributos -> WRN7029"
Write-Host "=========================================="
try {
    Invoke-RestMethod -Uri "$BASE/three-way-match?$PARAMS" -Method GET -Headers @{"x-user-vendors"=""}
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    $body | ConvertFrom-Json | Select-Object code, message, success
}
# Esperado: HTTP 400, code=WRN7029
