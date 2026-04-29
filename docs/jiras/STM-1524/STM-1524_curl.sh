#!/bin/bash
# STM-1524 — Estado de Cuenta — Pruebas de filtro de seguridad
# BFF finanzas: http://localhost:3000  (inyecta headers via util-api)
# Backend directo: http://localhost:3001/api

BASE_BFF="http://localhost:3000"
BASE_API="http://localhost:3001/api"

echo "=========================================="
echo "ESCENARIO 1: USR_FERNANDO — proveedor 11111"
echo "  ATR001=11111 (vendor no existe en finanzas → 0 resultados)"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/account-statement?year=2026" \
  -H "x-user-key: USR_FERNANDO" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "=========================================="
echo "ESCENARIO 2: USR_JOSE — proveedores 11111,22222"
echo "  ATR001=11111,22222 (vendors no existen en finanzas → 0 resultados)"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/account-statement?year=2026" \
  -H "x-user-key: USR_JOSE" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "=========================================="
echo "ESCENARIO 3: zedlav.sd18@gmail.com — acceso total (-1)"
echo "  ATR001=-1 → sin filtro → 4 estados de cuenta"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/account-statement?year=2026" \
  -H "x-user-key: zedlav.sd18@gmail.com" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "=========================================="
echo "ESCENARIO 4: USR_ANA — sin ATR001 → WRN7029"
echo "  Tiene ATR002=TPR001 pero NO ATR001 → HTTP 400"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/account-statement?year=2026" \
  -H "x-user-key: USR_ANA" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "=========================================="
echo "ESCENARIO 5: Consulta por UUID con filtro"
echo "  Reemplazar {uuid} con un UUID real"
echo "=========================================="
# curl -s -X GET "${BASE_BFF}/account-statement/{uuid}" \
#   -H "x-user-key: fernando" | jq .

echo ""
echo "=========================================="
echo "ALTERNATIVA: Prueba directa al backend"
echo "=========================================="

echo "--- Proveedor 1001 ---"
curl -s -X GET "${BASE_API}/account-statement?year=2025" \
  -H "x-user-vendors: 1001" \
  -H "Content-Type: application/json" | jq '.total // .data | length'

echo "--- Proveedores 1001,1002 ---"
curl -s -X GET "${BASE_API}/account-statement?year=2025" \
  -H "x-user-vendors: 1001,1002" \
  -H "Content-Type: application/json" | jq '.total // .data | length'

echo "--- Acceso total -1 ---"
curl -s -X GET "${BASE_API}/account-statement?year=2025" \
  -H "x-user-vendors: -1" \
  -H "Content-Type: application/json" | jq '.total // .data | length'

echo "--- Sin atributos (WRN7029) ---"
curl -s -X GET "${BASE_API}/account-statement?year=2025" \
  -H "x-user-vendors: " \
  -H "Content-Type: application/json" | jq .
