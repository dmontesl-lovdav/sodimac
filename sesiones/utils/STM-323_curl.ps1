# STM-323 - Facturas - Filtro seguridad por vendor
# fiscal-api: http://localhost:8082
# tipoDocumento: "I"=Factura | "E"=NC
# Rango: 2025-01-01 a 2025-06-30
# Datos BD Sodimac (en ese rango): total=30 | 11111=6 | 22222=3 | 11111+22222=9

$body = '{"page":0,"size":20,"tipoDocumento":"I","fechaInicioRecepcion":"2025-01-01","fechaFinalRecepcion":"2025-06-30"}'
$baseUrl = "http://localhost:8082/invoices/search"

function Test-Invoices {
    param($vendor, $label, $expected)
    Write-Host "`n=== $label → esperado: $expected ===" -ForegroundColor Cyan
    try {
        $headers = @{"Content-Type"="application/json"}
        if ($null -ne $vendor) { $headers["x-user-vendors"] = $vendor }
        $r = Invoke-WebRequest -Uri $baseUrl -Method POST -Headers $headers -Body $body -UseBasicParsing
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

Test-Invoices -vendor "-1"          -label "Acceso total -1"         -expected 30
Test-Invoices -vendor "11111"       -label "Vendor 11111"            -expected 6
Test-Invoices -vendor "22222"       -label "Vendor 22222"            -expected 3
Test-Invoices -vendor "11111,22222" -label "Multi 11111,22222"       -expected 9
Test-Invoices -vendor ""            -label "Sin atributos (WRN7029)" -expected "HTTP 400 WRN7029"
Test-Invoices -vendor $null         -label "Sin header (admin)"      -expected 30


=== Acceso total -1 → esperado: 30 ===
HTTP 200 | totalElements: 30
PS C:\> Test-Invoices -vendor "11111"       -label "Vendor 11111"            -expected 6

=== Vendor 11111 → esperado: 6 ===
HTTP 200 | totalElements: 6
PS C:\> Test-Invoices -vendor "22222"       -label "Vendor 22222"            -expected 3

=== Vendor 22222 → esperado: 3 ===
HTTP 200 | totalElements: 3
PS C:\> Test-Invoices -vendor "11111,22222" -label "Multi 11111,22222"       -expected 9

=== Multi 11111,22222 → esperado: 9 ===
HTTP 200 | totalElements: 9
PS C:\> Test-Invoices -vendor ""            -label "Sin atributos (WRN7029)" -expected "HTTP 400 WRN7029"

=== Sin atributos (WRN7029) → esperado: HTTP 400 WRN7029 ===
HTTP 400 | {"success":false,"message":"El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador","code":"WRN7029"}
PS C:\> Test-Invoices -vendor $null         -label "Sin header (admin)"      -expected 30

=== Sin header (admin) → esperado: 30 ===
HTTP 200 | totalElements: 30
