# STM-323 — Facturas (fiscal-api) — Pruebas filtro seguridad (PowerShell)

$BaseApi = "http://localhost:8082"

$JwtFernando = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMSJ9."
$JwtAna      = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMiJ9."
$JwtJose     = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMyJ9."
$JwtIvan     = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwNSJ9."

$Body = '{"fechaInicioRecepcion":"2025-01-01","fechaFinalRecepcion":"2026-12-31","tipoDocumento":"I","page":0,"size":20}'

function Invoke-Tests {
    param([string]$Title, [hashtable]$Headers)
    Write-Host ""
    Write-Host "=========================================="
    Write-Host $Title
    Write-Host "=========================================="
    $Headers["Content-Type"] = "application/json"
    try {
        $res = Invoke-WebRequest -Uri "$BaseApi/invoices/search" -Headers $Headers -Method POST -Body $Body -UseBasicParsing
        Write-Host "HTTP $($res.StatusCode)"
        Write-Host $res.Content
    } catch {
        Write-Host "HTTP $($_.Exception.Response.StatusCode.value__)"
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd()
    }
}

Invoke-Tests "ESCENARIO 1: FERNANDO ATR001=11111 (4 facturas)" @{ Authorization = "Bearer $JwtFernando" }
Invoke-Tests "ESCENARIO 2: JOSE ATR001=11111,22222 (8 facturas)" @{ Authorization = "Bearer $JwtJose" }
Invoke-Tests "ESCENARIO 3: Iván ATR001=-1 (23 facturas)" @{ Authorization = "Bearer $JwtIvan" }
Invoke-Tests "ESCENARIO 4: ANA sin ATR001 → WRN7029" @{ Authorization = "Bearer $JwtAna" }
Invoke-Tests "ESCENARIO 5: Spoof intentado" @{
    Authorization = "Bearer $JwtFernando"
    "x-user-vendors" = "-1"
}
