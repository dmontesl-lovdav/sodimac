# STM-1524 — Estado de Cuenta (Account Statement) — Pruebas filtro de seguridad (PowerShell)
# finanzas-api: http://localhost:3001/api
# Params obligatorios: year (min 2026)

$BASE = "http://localhost:3001/api"
$PARAMS = "year=2026"

Write-Host "=========================================="
Write-Host "ESCENARIO 1: vendor con datos (actualizar con vendor real)"
Write-Host "=========================================="
$r = Invoke-RestMethod -Uri "$BASE/account-statement?$PARAMS" -Method GET -Headers @{"x-user-vendors"="VENDOR_REAL"}
$r.total
# Esperado: N registros (correr query 2 para saber vendor real)

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 2: multi-vendor (actualizar con vendors reales)"
Write-Host "=========================================="
$r = Invoke-RestMethod -Uri "$BASE/account-statement?$PARAMS" -Method GET -Headers @{"x-user-vendors"="VENDOR1,VENDOR2"}
$r.total
# Esperado: sum de ambos vendors

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 3: acceso total -1"
Write-Host "=========================================="
$r = Invoke-RestMethod -Uri "$BASE/account-statement?$PARAMS" -Method GET -Headers @{"x-user-vendors"="-1"}
$r.total
# Esperado: total sin restriccion

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 4: sin atributos -> WRN7029"
Write-Host "=========================================="
try {
    Invoke-RestMethod -Uri "$BASE/account-statement?$PARAMS" -Method GET -Headers @{"x-user-vendors"=""}
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $body = $reader.ReadToEnd()
    $body | ConvertFrom-Json | Select-Object code, message, success
}
# Esperado: HTTP 400, code=WRN7029

Write-Host ""
Write-Host "=========================================="
Write-Host "ESCENARIO 5: getById con filtro activo"
Write-Host "UUID del vendor correcto -> 200, UUID de otro vendor -> 404"
Write-Host "=========================================="
# Primero obtener un UUID del vendor correcto con escenario 1, luego:
# (Invoke-RestMethod -Uri "$BASE/account-statement/UUID_DEL_VENDOR" -Method GET -Headers @{"x-user-vendors"="VENDOR_REAL"})
# (Invoke-RestMethod -Uri "$BASE/account-statement/UUID_OTRO_VENDOR" -Method GET -Headers @{"x-user-vendors"="VENDOR_REAL"})
