#!/bin/bash
# STM-1524 — Estado de Cuenta — Pruebas de filtro de seguridad
#
# Pre-requisitos:
#   - finanzas-api en :3001 con SECURITY_ENABLED=true
#   - util-api en :3712

BASE_API="http://localhost:3001/api"
YEAR="2026"

JWT_FERNANDO="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMSJ9."
JWT_ANA="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMiJ9."
JWT_JOSE="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMyJ9."
JWT_IVAN="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwNSJ9."

run() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
    curl -s -i -X GET "${BASE_API}/account-statement?year=${YEAR}" \
      -H "Authorization: Bearer $2"
    echo ""
}

run "ESCENARIO 1: FERNANDO — ATR001=11111 (3 estados año 2026)" "$JWT_FERNANDO"
run "ESCENARIO 2: JOSE — ATR001=11111,22222 (5 estados)" "$JWT_JOSE"
run "ESCENARIO 3: Iván — ATR001=-1 (6 estados, sin filtro)" "$JWT_IVAN"
run "ESCENARIO 4: ANA — sin ATR001 → WRN7029" "$JWT_ANA"

echo ""
echo "=========================================="
echo "ESCENARIO 5: Spoof intentado (x-user-vendors=-1)"
echo "  Backend IGNORA header cliente: filtro sigue a 11111 (FERNANDO)"
echo "=========================================="
curl -s -i -X GET "${BASE_API}/account-statement?year=${YEAR}" \
  -H "Authorization: Bearer ${JWT_FERNANDO}" \
  -H "x-user-vendors: -1"
echo ""
