# Curls validados UAT - Sodimac FBC

Catálogo de curls/llamadas validadas funcionando en UAT.
Fecha última validación: 2026-05-26

## Resumen rápido

| Endpoint | Método | Body | Estado |
|---|---|---|---|
| `/ppsomx/fiscal/health` | GET | - | 200 |
| `/ppsomx/fiscal/invoices` | GET | - | 200 |
| `/ppsomx/fiscal/invoices/search` | POST | JSON | 200 |
| `/ppsomx/fiscal/fiscal/xml/process/file` | POST | multipart | 200 |
| `/ppsomx/backend-util/messages/code/BUS001` | GET | - | 200 |
| `/ppsomx/backend-util/parameters?name=...` | GET | - | 200 |
| `/ppsomx/backend-finanzas/healthcheck` | GET | - | 200 |
| `/ppsomx/backend-finanzas/finanzas-payment` | GET | - | 200 |

---

## BFF fiscal

### Health

```bash
curl -i "https://uat.fbusinesscenter.com/ppsomx/fiscal/health"
# → 200 {"message":"healthy"}
```

```powershell
Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/fiscal/health"
```

### GET /invoices (lista paginada)

```bash
curl -i "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices?page=0"
```

```powershell
Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices?page=0" -Method GET
```

### POST /invoices/search (búsqueda con filtros)

**Validado tras fix del BFF (commit 1d226bd)**.

#### Bash:
```bash
curl -i -X POST "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/search" \
  -H "Content-Type: application/json" \
  -H "x-user-vendors: *" \
  -d '{
    "tipoDocumento": "I",
    "fechaInicioRecepcion": "2026-01-01",
    "fechaFinalRecepcion": "2026-05-26",
    "page": 0,
    "size": 3
  }'
```

#### PowerShell:
```powershell
$body = @{
  tipoDocumento = "I"
  fechaInicioRecepcion = "2026-01-01"
  fechaFinalRecepcion = "2026-05-26"
  page = 0
  size = 3
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/search" `
  -Method POST -ContentType "application/json" `
  -Headers @{ "x-user-vendors" = "*" } -Body $body
```

#### PowerShell con manejo de error (para diagnóstico):
```powershell
try {
  Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/search" `
    -Method POST -ContentType "application/json" `
    -Headers @{ "x-user-vendors" = "*" } -Body $body
} catch {
  $_.Exception.Response.StatusCode
  $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
  $reader.ReadToEnd()
}
```

#### Payload original reportado por usuario:
```json
{
  "rfcEmisor": "",
  "fechaInicioRecepcion": "2026-05-20",
  "fechaFinalRecepcion": "2026-05-27",
  "tipoDocumento": "I",
  "page": 0,
  "size": 10,
  "serie": "",
  "folio": "",
  "idProveedor": "1"
}
```

#### Validación adicional: expandir notas de crédito relacionadas (confirma fiscal-api → util-api)

```powershell
$result = Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/fiscal/invoices/search" `
  -Method POST -ContentType "application/json" `
  -Headers @{ "x-user-vendors" = "*" } `
  -Body (@{
    tipoDocumento = "I"
    fechaInicioRecepcion = "2026-01-01"
    fechaFinalRecepcion = "2026-05-26"
    page = 0
    size = 3
  } | ConvertTo-Json)

$result.content | ForEach-Object {
  $_.notasCreditoRelacionadas | Format-Table tipoRelacion, tipoRelacionNombre
}
# Si tipoRelacionNombre tiene texto descriptivo del SAT → util-api respondió
# Si vacío o "Desconocido" → fallback hardcoded (util-api falló silencioso)
```

### POST /fiscal/xml/process/file (multipart)

```bash
curl -i -X POST "https://uat.fbusinesscenter.com/ppsomx/fiscal/fiscal/xml/process/file" \
  -F "file=@ruta/al/archivo.xml"
```

```powershell
# PowerShell con Invoke-RestMethod (5.1+)
$filePath = "C:\log\01-factura-registro-exitoso.xml"
$form = @{ file = Get-Item -Path $filePath }

Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/fiscal/fiscal/xml/process/file" `
  -Method POST -Form $form
```

---

## BFF util

### GET /messages/code/{code}

```bash
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-util/messages/code/BUS001"
# → {"success":true,"data":{"description":"La addenda de la factura..."}}
```

```powershell
Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/backend-util/messages/code/BUS001"
```

### GET /parameters?name=

```bash
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-util/parameters?name=MAX_SEARCH_MONTHS"
# → {"success":true,"data":[{"name":"MAX_SEARCH_MONTHS","value":"6",...}]}
```

```powershell
Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/backend-util/parameters?name=MAX_SEARCH_MONTHS"
```

---

## BFF finanzas

### Healthcheck

```bash
curl -i "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/healthcheck"
# → 200 {"alive":true,...}
```

```powershell
Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/healthcheck"
```

### GET /finanzas-payment (consulta pagos)

```bash
curl -s "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/finanzas-payment?createdAtInitial=2026-01-01&createdAtEnd=2026-05-26&pageNumber=1&pageSize=5"
```

```powershell
Invoke-RestMethod -Uri "https://uat.fbusinesscenter.com/ppsomx/backend-finanzas/finanzas-payment?createdAtInitial=2026-01-01&createdAtEnd=2026-05-26&pageNumber=1&pageSize=5"
```

---

## Diagnóstico rápido

### Si /invoices/search da 500 con body `{"message":"Required request body is missing"...}`

→ BFF está consumiendo el body antes del proxy. Bug del patrón `parseReqBody:false + express.json()`. Verificar `bff.fiscal/src/App.js` que no tenga body parsers globales.

### Si /invoices/search da 500 body vacío `{}`

→ Pod fiscal-api caído. Verificar:
```bash
kubectl get pods -n vendor-portal | grep fiscal
kubectl logs deploy/mrch-backend-somx-fiscal-api -n vendor-portal --tail=100
```

### Si /invoices da 200 pero /invoices/search da 500

→ Bug específico en el endpoint POST con body. Revisar diff entre uno y otro (JPA Specification + INNER JOIN, mapToSearchResponse, body deserialization).

---

## Headers de seguridad (x-user-*)

| Header | Valor | Semántica |
|---|---|---|
| `x-user-vendors` | `null` (omitido) | admin — sin filtro |
| `x-user-vendors` | `""` (vacío) | WRN7029 — sin atributos |
| `x-user-vendors` | `"-1"` | wildcard — acceso total |
| `x-user-vendors` | `"*"` | parseado como `["*"]` → BigDecimal falla → sin filtro |
| `x-user-vendors` | `"123,456"` | filtro por proveedores 123 y 456 |

Ver [project_security_headers_semantics.md] en memoria Claude.
