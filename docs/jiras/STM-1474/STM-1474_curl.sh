#!/bin/bash
# STM-1474 — Complementos de Pago — Pruebas de filtro de seguridad
#
# Pre-requisitos:
#   - fiscal-api en :8082 con security.enabled=true
#   - util-api en :3712

BASE_API="http://localhost:8082/api"

JWT_FERNANDO="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMSJ9."
JWT_ANA="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMiJ9."
JWT_JOSE="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwMyJ9."
JWT_IVAN="eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJzYjAwMDAwNSJ9."

run() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
    curl -s -i -X GET "${BASE_API}/fiscal/complementos-pago/buscar?page=0&size=20" \
      -H "Authorization: Bearer $2"
    echo ""
}

run "ESCENARIO 1: FERNANDO — ATR001=11111" "$JWT_FERNANDO"
run "ESCENARIO 2: JOSE — ATR001=11111,22222" "$JWT_JOSE"
run "ESCENARIO 3: Iván — ATR001=-1 (19 complementos, todos)" "$JWT_IVAN"
run "ESCENARIO 4: ANA — sin ATR001 → WRN7029" "$JWT_ANA"

echo ""
echo "=========================================="
echo "ESCENARIO 5: Spoof intentado (x-user-vendors=-1)"
echo "  SecurityContextFilter sobrescribe header con valor del JWT"
echo "=========================================="
curl -s -i -X GET "${BASE_API}/fiscal/complementos-pago/buscar?page=0&size=20" \
  -H "Authorization: Bearer ${JWT_FERNANDO}" \
  -H "x-user-vendors: -1"
echo ""
