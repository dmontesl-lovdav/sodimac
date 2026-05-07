#!/bin/bash
# STM-323 — Facturas — Pruebas de filtro de seguridad
#
# Pre-requisitos:
#   - fiscal-api en :8082 con security.enabled=true
#   - util-api en :3712

BASE_API="http://localhost:8082"

JWT_FERNANDO="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMSJ9."
JWT_ANA="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMiJ9."
JWT_JOSE="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMyJ9."
JWT_IVAN="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwNSJ9."

BODY='{"fechaInicioRecepcion":"2025-01-01","fechaFinalRecepcion":"2026-12-31","tipoDocumento":"I","page":0,"size":20}'

run() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
    curl -s -i -X POST "${BASE_API}/invoices/search" \
      -H "Authorization: Bearer $2" \
      -H "Content-Type: application/json" \
      -d "${BODY}"
    echo ""
}

run "ESCENARIO 1: FERNANDO — ATR001=11111 (4 facturas)" "$JWT_FERNANDO"
run "ESCENARIO 2: JOSE — ATR001=11111,22222 (8 facturas)" "$JWT_JOSE"
run "ESCENARIO 3: Iván — ATR001=-1 (23 facturas, todas)" "$JWT_IVAN"
run "ESCENARIO 4: ANA — sin ATR001 → WRN7029" "$JWT_ANA"

echo ""
echo "=========================================="
echo "ESCENARIO 5: Spoof intentado (x-user-vendors=-1)"
echo "  SecurityContextFilter sobrescribe header con valor del JWT"
echo "  FERNANDO sigue limitado a 11111"
echo "=========================================="
curl -s -i -X POST "${BASE_API}/invoices/search" \
  -H "Authorization: Bearer ${JWT_FERNANDO}" \
  -H "x-user-vendors: -1" \
  -H "Content-Type: application/json" \
  -d "${BODY}"
echo ""
