#!/bin/bash
# STM-323 — Facturas — Pruebas de filtro de seguridad
# BFF fiscal: http://localhost:3003  (inyecta headers via util-api)
# Backend directo: http://localhost:8082

BASE_BFF="http://localhost:3003"
BASE_API="http://localhost:8082"

echo "=========================================="
echo "ESCENARIO 1: USR_FERNANDO — proveedor 11111"
echo "  ATR001=11111 → Esperado: 15 facturas"
echo "=========================================="
curl -s -X POST "${BASE_BFF}/invoices/search" \
  -H "x-user-key: USR_FERNANDO" \
  -H "Content-Type: application/json" \
  -d '{"page": 0, "size": 20}' | jq '.totalElements // .total // .'

echo ""
echo "=========================================="
echo "ESCENARIO 2: USR_JOSE — proveedores 11111,22222"
echo "  ATR001=11111,22222 → Esperado: 24 facturas (OR lógico)"
echo "=========================================="
curl -s -X POST "${BASE_BFF}/invoices/search" \
  -H "x-user-key: USR_JOSE" \
  -H "Content-Type: application/json" \
  -d '{"page": 0, "size": 20}' | jq '.totalElements // .total // .'

echo ""
echo "=========================================="
echo "ESCENARIO 3: zedlav.sd18@gmail.com — acceso total (-1)"
echo "  ATR001=-1 → sin filtro → Esperado: 127 facturas"
echo "=========================================="
curl -s -X POST "${BASE_BFF}/invoices/search" \
  -H "x-user-key: zedlav.sd18@gmail.com" \
  -H "Content-Type: application/json" \
  -d '{"page": 0, "size": 20}' | jq '.totalElements // .total // .'

echo ""
echo "=========================================="
echo "ESCENARIO 4: USR_ANA — sin ATR001 → WRN7029"
echo "  Tiene ATR002=TPR001 pero NO ATR001 → HTTP 400"
echo "=========================================="
curl -s -X POST "${BASE_BFF}/invoices/search" \
  -H "x-user-key: USR_ANA" \
  -H "Content-Type: application/json" \
  -d '{"page": 0, "size": 20}' | jq .

echo ""
echo "=========================================="
echo "ALTERNATIVA: Prueba directa al backend"
echo "=========================================="

BODY='{"page":0,"size":20,"tipoDocumento":"I","fechaInicioRecepcion":"2025-01-01","fechaFinalRecepcion":"2025-06-30"}'

echo "--- Proveedor 11111 ---"
curl -s -X POST "${BASE_API}/invoices/search" \
  -H "x-user-vendors: 11111" \
  -H "Content-Type: application/json" \
  -d "$BODY" | jq '.totalElements'

echo "--- Proveedores 11111,22222 (OR lógico) ---"
curl -s -X POST "${BASE_API}/invoices/search" \
  -H "x-user-vendors: 11111,22222" \
  -H "Content-Type: application/json" \
  -d "$BODY" | jq '.totalElements'

echo "--- Acceso total -1 ---"
curl -s -X POST "${BASE_API}/invoices/search" \
  -H "x-user-vendors: -1" \
  -H "Content-Type: application/json" \
  -d "$BODY" | jq '.totalElements'

echo "--- Sin atributos (WRN7029) ---"
curl -s -X POST "${BASE_API}/invoices/search" \
  -H "x-user-vendors: " \
  -H "Content-Type: application/json" \
  -d "$BODY" | jq '{code,message,success}'
