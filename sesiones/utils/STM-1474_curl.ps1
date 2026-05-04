# STM-1474 - Complementos de Pago - Filtro seguridad por vendor
# fiscal-api: http://localhost:8082
# Datos BD Sodimac: vendor 1 = 4 complementos | total = 19

$baseUrl = "http://localhost:8082/fiscal/complementos-pago/buscar"

function Test-Payments {
    param($vendor, $label, $expected)
    Write-Host "`n=== $label → esperado: $expected ===" -ForegroundColor Cyan
    try {
        $headers = @{}
        if ($null -ne $vendor) { $headers["x-user-vendors"] = $vendor }
        $r = Invoke-WebRequest -Uri $baseUrl -Method GET -Headers $headers -UseBasicParsing
        $json = $r.Content | ConvertFrom-Json
        Write-Host "HTTP $($r.StatusCode) | totalElements: $($json.totalElements)"
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        $errBody = $_.ErrorDetails.Message
        if (-not $errBody) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $errBody = $reader.ReadToEnd()
        }
        Write-Host "HTTP $status | $errBody" -ForegroundColor Red
    }
}

Test-Payments -vendor "-1"    -label "Acceso total -1"         -expected 19
Test-Payments -vendor "1"     -label "Vendor 1"                -expected 4
Test-Payments -vendor "11111" -label "Vendor sin complementos" -expected 0
Test-Payments -vendor ""      -label "Sin atributos (WRN7029)" -expected "HTTP 400 WRN7029"
Test-Payments -vendor $null   -label "Sin header (admin)"      -expected 19


=== Acceso total -1 → esperado: 19 ===
HTTP 200 | totalElements: 19
PS C:\> Test-Payments -vendor "1"     -label "Vendor 1"                -expected 4

=== Vendor 1 → esperado: 4 ===
HTTP 200 | totalElements: 4
PS C:\> Test-Payments -vendor "11111" -label "Vendor sin complementos" -expected 0

=== Vendor sin complementos → esperado: 0 ===
HTTP 200 | totalElements: 0
PS C:\> Test-Payments -vendor ""      -label "Sin atributos (WRN7029)" -expected "HTTP 400 WRN7029"

=== Sin atributos (WRN7029) → esperado: HTTP 400 WRN7029 ===
HTTP 400 | {"code":"WRN7029","message":"El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador"}
PS C:\> Test-Payments -vendor $null   -label "Sin header (admin)"      -expected 19

=== Sin header (admin) → esperado: 19 ===
HTTP 200 | totalElements: 19
