#!/bin/bash
# STM-1525 — Catálogo Proveedor — Pruebas de filtro de seguridad
# util-api: http://localhost:3712 (acceso directo con x-user-types)

BASE_API="http://localhost:3712/api"

echo "=========================================="
echo "ESCENARIO 1: Sin header (admin) → 22 proveedores"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=5" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total, totalPages: .data.totalPages}'

echo ""
echo "=========================================="
echo "ESCENARIO 2: USR_ANA (ATR002=TPR001) → 11 proveedores tipo 1"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=20" \
  -H "x-user-types: TPR001" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total, tipos: [.data.items[].supplierTypeId] | unique | sort}'

echo ""
echo "=========================================="
echo "ESCENARIO 3: Multi tipo TPR001,TPR002 → 14 proveedores"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=20" \
  -H "x-user-types: TPR001,TPR002" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total, tipos: [.data.items[].supplierTypeId] | unique | sort}'

echo ""
echo "=========================================="
echo "ESCENARIO 4: Acceso total (-1) → 22 proveedores"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=5" \
  -H "x-user-types: -1" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total}'

echo ""
echo "=========================================="
echo "ESCENARIO 5: Sin ATR002 → WRN7029"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers" \
  -H "x-user-types;" \
  -H "Content-Type: application/json" | jq '{code, message, success}'

echo ""
echo "=========================================="
echo "BÚSQUEDA con filtros adicionales (TPR001 + businessName)"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?businessName=Distribu&status=1" \
  -H "x-user-types: TPR001" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total}'
