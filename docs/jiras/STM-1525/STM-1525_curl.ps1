# STM-1525 — Catálogo Proveedor — Pruebas filtro seguridad (PowerShell)
# util-api: http://localhost:3712 (acceso directo con x-user-types)

$BaseApi = "http://localhost:3712/api"

function Invoke-Tests {
    param([string]$Title, [hashtable]$Headers, [string]$QueryParams = "?pageSize=5")
    Write-Host ""
    Write-Host "=========================================="
    Write-Host $Title
    Write-Host "=========================================="
    try {
        $res = Invoke-WebRequest -Uri "$BaseApi/suppliers$QueryParams" -Headers $Headers -Method GET -UseBasicParsing
        Write-Host "HTTP $($res.StatusCode)"
        Write-Host $res.Content
    } catch {
        Write-Host "HTTP $($_.Exception.Response.StatusCode.value__)"
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd()
    }
}

Invoke-Tests "ESCENARIO 1: Sin header (admin) → 22 proveedores" @{ "Content-Type" = "application/json" }
Invoke-Tests "ESCENARIO 2: USR_ANA (ATR002=TPR001) → 11 proveedores tipo 1" @{ "x-user-types" = "TPR001"; "Content-Type" = "application/json" } "?pageSize=20"
Invoke-Tests "ESCENARIO 3: Multi tipo TPR001,TPR002 → 14 proveedores" @{ "x-user-types" = "TPR001,TPR002"; "Content-Type" = "application/json" } "?pageSize=20"
Invoke-Tests "ESCENARIO 4: Acceso total (-1) → 22 proveedores" @{ "x-user-types" = "-1"; "Content-Type" = "application/json" }
Invoke-Tests "ESCENARIO 5: Sin ATR002 → WRN7029" @{ "x-user-types" = ""; "Content-Type" = "application/json" } ""
Invoke-Tests "BUSQUEDA con filtros adicionales (TPR001 + businessName)" @{ "x-user-types" = "TPR001"; "Content-Type" = "application/json" } "?businessName=Distribu&status=1"
