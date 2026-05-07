#!/bin/bash
# STM-321 — Three Way Match — Pruebas de filtro de seguridad
#
# Pre-requisitos:
#   - finanzas-api en :3001 con SECURITY_ENABLED=true
#   - util-api en :3712
#   - Datos seed: usuarios sb000001..5 con ATR001 configurado en util-api

BASE_API="http://localhost:3001/api"
PARAMS="tipoFecha=fechaRecepcion&fechaInicio=2025-01-01&fechaFin=2025-06-30"

# JWT alg=none para tests locales (no se verifica firma; en prod GCP gateway valida)
JWT_FERNANDO="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMSJ9."
JWT_ANA="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMiJ9."
JWT_JOSE="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMyJ9."
JWT_IVAN="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwNSJ9."

run() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
    curl -s -i -X GET "${BASE_API}/three-way-match?${PARAMS}" \
      -H "Authorization: Bearer $2"
    echo ""
}

run "ESCENARIO 1: FERNANDO (sub=sb000001) — ATR001=11111" "$JWT_FERNANDO"
run "ESCENARIO 2: JOSE (sub=sb000003) — ATR001=11111,22222" "$JWT_JOSE"
run "ESCENARIO 3: Iván (sub=sb000005) — ATR001=-1 (acceso total)" "$JWT_IVAN"
run "ESCENARIO 4: ANA (sub=sb000002) — sin ATR001 → WRN7029" "$JWT_ANA"

echo ""
echo "=========================================="
echo "ESCENARIO 5: Sin Authorization → 401"
echo "=========================================="
curl -s -i -X GET "${BASE_API}/three-way-match?${PARAMS}"
echo ""

echo ""
echo "=========================================="
echo "ESCENARIO 6: Spoof intentado (cliente envía x-user-vendors=-1)"
echo "  Backend IGNORA header del cliente: filtro sigue activo a 11111 (FERNANDO)"
echo "=========================================="
curl -s -i -X GET "${BASE_API}/three-way-match?${PARAMS}" \
  -H "Authorization: Bearer ${JWT_FERNANDO}" \
  -H "x-user-vendors: -1"
echo ""
