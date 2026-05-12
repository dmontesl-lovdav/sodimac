# STM-1525 — util-api: endpoint user-attributes-by-key + Catálogo Proveedor
# Pruebas directas a util-api (puerto 3712)

$BaseApi = "http://localhost:3712/api"

function Invoke-Tests {
    param([string]$Title, [string]$Url, [hashtable]$Headers = @{})
    Write-Host ""
    Write-Host "=========================================="
    Write-Host $Title
    Write-Host "=========================================="
    try {
        $res = Invoke-WebRequest -Uri $Url -Headers $Headers -Method GET -UseBasicParsing
        Write-Host "HTTP $($res.StatusCode)"
        Write-Host $res.Content
    } catch {
        Write-Host "HTTP $($_.Exception.Response.StatusCode.value__)"
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd()
    }
}

# =============================================================================
# PARTE 1: GET /api/security/user-attributes-by-key/:userKey
# =============================================================================

Invoke-Tests "USER-ATTRIBUTES 1: Fernando (sb000001) → ATR001=11111" "$BaseApi/security/user-attributes-by-key/sb000001"
Invoke-Tests "USER-ATTRIBUTES 2: Jose (sb000003) → ATR001=11111,22222" "$BaseApi/security/user-attributes-by-key/sb000003"
Invoke-Tests "USER-ATTRIBUTES 3: Iván (sb000005) → ATR001=-1" "$BaseApi/security/user-attributes-by-key/sb000005"
Invoke-Tests "USER-ATTRIBUTES 4: Ana (sb000002) → ATR002=TPR001" "$BaseApi/security/user-attributes-by-key/sb000002"
Invoke-Tests "USER-ATTRIBUTES 5: Inexistente → 404" "$BaseApi/security/user-attributes-by-key/noexiste"
Invoke-Tests "USER-ATTRIBUTES 6: userKey vacío → 400" "$BaseApi/security/user-attributes-by-key/%20"

# =============================================================================
# PARTE 2: GET /api/suppliers (Catálogo Proveedor con filtro)
# =============================================================================

Invoke-Tests "SUPPLIERS 1: Sin header (admin) → 22 proveedores" "$BaseApi/suppliers?pageSize=5"
Invoke-Tests "SUPPLIERS 2: USR_ANA (TPR001) → 11 proveedores tipo 1" "$BaseApi/suppliers?pageSize=20" @{ "x-user-types" = "TPR001" }
Invoke-Tests "SUPPLIERS 3: Multi TPR001,TPR002 → 14 proveedores" "$BaseApi/suppliers?pageSize=20" @{ "x-user-types" = "TPR001,TPR002" }
Invoke-Tests "SUPPLIERS 4: Acceso total (-1) → 22 proveedores" "$BaseApi/suppliers?pageSize=5" @{ "x-user-types" = "-1" }
Invoke-Tests "SUPPLIERS 5: Sin ATR002 → WRN7029" "$BaseApi/suppliers" @{ "x-user-types" = "" }
Invoke-Tests "SUPPLIERS 6: Búsqueda combinada (TPR001 + businessName)" "$BaseApi/suppliers?businessName=Distribu&status=1" @{ "x-user-types" = "TPR001" }
