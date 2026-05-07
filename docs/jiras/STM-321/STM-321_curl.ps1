# STM-321 — Three Way Match — Pruebas filtro seguridad (PowerShell)
#
# Pre-requisitos:
#   - finanzas-api en :3001 con SECURITY_ENABLED=true
#   - util-api en :3712

$BaseApi = "http://localhost:3001/api"
$Params = "tipoFecha=fechaRecepcion&fechaInicio=2025-01-01&fechaFin=2025-06-30"

# JWT alg=none (sin firma; en prod GCP gateway valida)
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
        $res = Invoke-WebRequest -Uri "$BaseApi/three-way-match?$Params" -Headers $Headers -Method GET -UseBasicParsing
        Write-Host "HTTP $($res.StatusCode)"
        Write-Host $res.Content
    } catch {
        Write-Host "HTTP $($_.Exception.Response.StatusCode.value__)"
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd()
    }
}

Invoke-Tests "ESCENARIO 1: FERNANDO (sub=sb000001) ATR001=11111" @{ Authorization = "Bearer $JwtFernando" }
Invoke-Tests "ESCENARIO 2: JOSE (sub=sb000003) ATR001=11111,22222" @{ Authorization = "Bearer $JwtJose" }
Invoke-Tests "ESCENARIO 3: Iván (sub=sb000005) ATR001=-1 acceso total" @{ Authorization = "Bearer $JwtIvan" }
Invoke-Tests "ESCENARIO 4: ANA (sb000002) sin ATR001 → WRN7029" @{ Authorization = "Bearer $JwtAna" }
Invoke-Tests "ESCENARIO 5: Sin Authorization → 401" @{}
Invoke-Tests "ESCENARIO 6: Spoof x-user-vendors=-1 (debe ignorarse)" @{
    Authorization = "Bearer $JwtFernando"
    "x-user-vendors" = "-1"
}
