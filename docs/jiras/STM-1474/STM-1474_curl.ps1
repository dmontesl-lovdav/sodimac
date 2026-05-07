# STM-1474 — Complementos de Pago (fiscal-api) — Pruebas filtro seguridad (PowerShell)

$BaseApi = "http://localhost:8082"

$JwtFernando = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMSJ9."
$JwtAna      = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMiJ9."
$JwtJose     = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMyJ9."
$JwtIvan     = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwNSJ9."

function Invoke-Tests {
    param([string]$Title, [hashtable]$Headers)
    Write-Host ""
    Write-Host "=========================================="
    Write-Host $Title
    Write-Host "=========================================="
    try {
        $res = Invoke-WebRequest -Uri "$BaseApi/fiscal/complementos-pago/buscar?page=0&size=20" -Headers $Headers -Method GET -UseBasicParsing
        Write-Host "HTTP $($res.StatusCode)"
        Write-Host $res.Content
    } catch {
        Write-Host "HTTP $($_.Exception.Response.StatusCode.value__)"
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd()
    }
}

Invoke-Tests "ESCENARIO 1: FERNANDO ATR001=11111" @{ Authorization = "Bearer $JwtFernando" }
Invoke-Tests "ESCENARIO 2: JOSE ATR001=11111,22222" @{ Authorization = "Bearer $JwtJose" }
Invoke-Tests "ESCENARIO 3: Iván ATR001=-1 (19 complementos)" @{ Authorization = "Bearer $JwtIvan" }
Invoke-Tests "ESCENARIO 4: ANA sin ATR001 → WRN7029" @{ Authorization = "Bearer $JwtAna" }
Invoke-Tests "ESCENARIO 5: Spoof intentado" @{
    Authorization = "Bearer $JwtFernando"
    "x-user-vendors" = "-1"
}
