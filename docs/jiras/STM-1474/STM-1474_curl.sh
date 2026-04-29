#!/bin/bash
# STM-1474 — Complementos de Pago — Pruebas de filtro de seguridad
# BFF fiscal: http://localhost:3003  (inyecta headers via util-api)
# Backend directo: http://localhost:8082

BASE_BFF="http://localhost:3003"
BASE_API="http://localhost:8082"

echo "=========================================="
echo "ESCENARIO 1: USR_FERNANDO — proveedor 11111"
echo "  ATR001=11111 → 0 complementos"
echo "  (vendor 11111 no tiene complementos en addendum.payments_uuid)"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/fiscal/complementos-pago/buscar" \
  -H "x-user-key: USR_FERNANDO" \
  -H "Content-Type: application/json" | jq '{totalElements, contentCount: (.content | length)}'

echo ""
echo "=========================================="
echo "ESCENARIO 2: USR_JOSE — proveedores 11111,22222"
echo "  ATR001=11111,22222 → 0 complementos (OR lógico)"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/fiscal/complementos-pago/buscar" \
  -H "x-user-key: USR_JOSE" \
  -H "Content-Type: application/json" | jq '{totalElements, contentCount: (.content | length)}'

echo ""
echo "=========================================="
echo "ESCENARIO 3: zedlav.sd18@gmail.com — acceso total (-1)"
echo "  ATR001=-1 → sin filtro → 19 complementos"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/fiscal/complementos-pago/buscar" \
  -H "x-user-key: zedlav.sd18@gmail.com" \
  -H "Content-Type: application/json" | jq '{totalElements, contentCount: (.content | length)}'

echo ""
echo "=========================================="
echo "ESCENARIO 4: USR_ANA — sin ATR001 → WRN7029"
echo "  Tiene ATR002=TPR001 pero NO ATR001 → HTTP 400"
echo "=========================================="
curl -s -X GET "${BASE_BFF}/fiscal/complementos-pago/buscar" \
  -H "x-user-key: USR_ANA" \
  -H "Content-Type: application/json" | jq '{code, message}'

echo ""
echo "=========================================="
echo "ALTERNATIVA: Prueba directa al backend"
echo "=========================================="

echo "--- Vendor 12345 (tiene 4 complementos reales) ---"
curl -s -X GET "${BASE_API}/fiscal/complementos-pago/buscar" \
  -H "x-user-vendors: 12345" \
  -H "Content-Type: application/json" | jq '.totalElements'

echo "--- Vendor 11111 (sin complementos → 0) ---"
curl -s -X GET "${BASE_API}/fiscal/complementos-pago/buscar" \
  -H "x-user-vendors: 11111" \
  -H "Content-Type: application/json" | jq '.totalElements'

echo "--- Acceso total -1 ---"
curl -s -X GET "${BASE_API}/fiscal/complementos-pago/buscar" \
  -H "x-user-vendors: -1" \
  -H "Content-Type: application/json" | jq '.totalElements'

echo "--- Sin atributos (WRN7029) ---"
curl -s -X GET "${BASE_API}/fiscal/complementos-pago/buscar" \
  -H "x-user-vendors;" \
  -H "Content-Type: application/json" | jq '{code, message}'
