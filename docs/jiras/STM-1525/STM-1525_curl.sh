#!/bin/bash
# STM-1525 — util-api: endpoint user-attributes-by-key + Catálogo Proveedor
# Pruebas directas a util-api (puerto 3712)

BASE_API="http://localhost:3712/api"

# =============================================================================
# PARTE 1: GET /api/security/user-attributes-by-key/:userKey
# =============================================================================

echo "=========================================="
echo "USER-ATTRIBUTES 1: Fernando (sb000001) → ATR001=11111"
echo "=========================================="
curl -s "${BASE_API}/security/user-attributes-by-key/sb000001" | jq

echo ""
echo "=========================================="
echo "USER-ATTRIBUTES 2: Jose (sb000003) → ATR001=11111,22222"
echo "=========================================="
curl -s "${BASE_API}/security/user-attributes-by-key/sb000003" | jq

echo ""
echo "=========================================="
echo "USER-ATTRIBUTES 3: Iván (sb000005) → ATR001=-1"
echo "=========================================="
curl -s "${BASE_API}/security/user-attributes-by-key/sb000005" | jq

echo ""
echo "=========================================="
echo "USER-ATTRIBUTES 4: Ana (sb000002) → ATR002=TPR001"
echo "=========================================="
curl -s "${BASE_API}/security/user-attributes-by-key/sb000002" | jq

echo ""
echo "=========================================="
echo "USER-ATTRIBUTES 5: Inexistente → 404"
echo "=========================================="
curl -s -i "${BASE_API}/security/user-attributes-by-key/noexiste"

echo ""
echo "=========================================="
echo "USER-ATTRIBUTES 6: userKey vacío → 400"
echo "=========================================="
curl -s -i "${BASE_API}/security/user-attributes-by-key/%20"

# =============================================================================
# PARTE 2: GET /api/suppliers (Catálogo Proveedor con filtro)
# =============================================================================

echo ""
echo "=========================================="
echo "SUPPLIERS 1: Sin header (admin) → 22 proveedores"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=5" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total, totalPages: .data.totalPages}'

echo ""
echo "=========================================="
echo "SUPPLIERS 2: USR_ANA (TPR001) → 11 proveedores tipo 1"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=20" \
  -H "x-user-types: TPR001" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total, tipos: [.data.items[].supplierTypeId] | unique | sort}'

echo ""
echo "=========================================="
echo "SUPPLIERS 3: Multi TPR001,TPR002 → 14 proveedores"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=20" \
  -H "x-user-types: TPR001,TPR002" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total, tipos: [.data.items[].supplierTypeId] | unique | sort}'

echo ""
echo "=========================================="
echo "SUPPLIERS 4: Acceso total (-1) → 22 proveedores"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?pageSize=5" \
  -H "x-user-types: -1" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total}'

echo ""
echo "=========================================="
echo "SUPPLIERS 5: Sin ATR002 → WRN7029"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers" \
  -H "x-user-types;" \
  -H "Content-Type: application/json" | jq '{code, message, success}'

echo ""
echo "=========================================="
echo "SUPPLIERS 6: Búsqueda combinada (TPR001 + businessName)"
echo "=========================================="
curl -s -X GET "${BASE_API}/suppliers?businessName=Distribu&status=1" \
  -H "x-user-types: TPR001" \
  -H "Content-Type: application/json" | jq '{success: .success, total: .data.total}'
