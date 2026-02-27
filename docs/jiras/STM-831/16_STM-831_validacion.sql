-- ============================================================================
-- STM-831: Script de Validacion de Resultados
-- Esquema: shared_catalogs
-- Fecha: 2026-02-05
--
-- Este script contiene consultas para validar que los resultados del endpoint
-- GET /suppliers/filter coincidan con los datos en base de datos.
-- ============================================================================

SET search_path TO shared_catalogs;

-- ============================================================================
-- 1. VALIDAR TIPOS DE PROVEEDOR
-- Endpoint: GET /suppliers/types
-- ============================================================================

-- 1.1 Listar todos los tipos de proveedor activos
SELECT
    st.id,
    st.code,
    st.description,
    st.status,
    st.created_by,
    st.created_at
FROM shared_catalogs.supplier_type st
WHERE st.status = 1
ORDER BY st.id;

-- Resultado esperado:
-- id | code       | description                            | status
-- 1  | MERCANCIA  | Proveedores de mercancia ODBMS         | 1
-- 2  | TRANSPORTE | Proveedores de transporte Carta Porte  | 1
-- 3  | INDIRECTOS | Proveedores de insumos SAP             | 1
-- 4  | SERVICIOS  | Proveedores de servicios               | 1


-- ============================================================================
-- 2. VALIDAR MENSAJE DE ERROR BUS214
-- ============================================================================

-- 2.1 Verificar que el mensaje BUS214 exista en catalog_detail
SELECT
    cd.id,
    cd.header_id,
    cd.key,
    cd.dict_id,
    cd.status,
    cd.sort_order
FROM shared_catalogs.catalog_detail cd
WHERE cd.key = 'BUS214';

-- 2.2 Verificar traducciones del mensaje BUS214
SELECT
    cd.key,
    dl.lang_id,
    CASE dl.lang_id
        WHEN 1 THEN 'Espanol'
        WHEN 2 THEN 'Ingles'
        WHEN 3 THEN 'Portugues'
    END as idioma,
    dl.description
FROM shared_catalogs.catalog_detail cd
JOIN shared_catalogs.dictionary_lang dl ON cd.dict_id = dl.dict_id
WHERE cd.key = 'BUS214'
ORDER BY dl.lang_id;

-- Resultado esperado:
-- key    | lang_id | idioma    | description
-- BUS214 | 1       | Espanol   | No existe el tipo de proveedor [{0}] solicitado.
-- BUS214 | 2       | Ingles    | Supplier type [{0}] does not exist.
-- BUS214 | 3       | Portugues | O tipo de fornecedor [{0}] nao existe.


-- ============================================================================
-- 3. VALIDAR TOTAL DE PROVEEDORES
-- Endpoint: GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=2
-- ============================================================================

-- 3.1 Contar todos los proveedores activos (debe coincidir con el endpoint)
SELECT COUNT(*) as total_proveedores
FROM shared_catalogs.supplier s
WHERE s.status = 1;


-- ============================================================================
-- 4. VALIDAR PROVEEDORES POR TIPO
-- Endpoint: GET /suppliers/filter?tipoProveedor={1-4}&estatusBloqueo=2
-- ============================================================================

-- 4.1 Conteo de proveedores por tipo
SELECT
    st.id as tipo_id,
    st.code as tipo_code,
    COUNT(s.id) as total
FROM shared_catalogs.supplier s
JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
GROUP BY st.id, st.code
ORDER BY st.id;

-- 4.2 Proveedores de tipo MERCANCIA (tipoProveedor=1)
SELECT COUNT(*) as total_mercancia
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 1;

-- 4.3 Proveedores de tipo TRANSPORTE (tipoProveedor=2)
SELECT COUNT(*) as total_transporte
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 2;

-- 4.4 Proveedores de tipo INDIRECTOS (tipoProveedor=3)
SELECT COUNT(*) as total_indirectos
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 3;

-- 4.5 Proveedores de tipo SERVICIOS (tipoProveedor=4)
SELECT COUNT(*) as total_servicios
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 4;


-- ============================================================================
-- 5. VALIDAR PROVEEDORES BLOQUEADOS
-- Endpoint: GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=0
-- ============================================================================

-- 5.1 Contar proveedores con bloqueo activo vigente
SELECT COUNT(DISTINCT s.supplier_number) as total_bloqueados
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
WHERE s.status = 1
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to;

-- 5.2 Listar proveedores bloqueados con detalle
SELECT
    s.id,
    s.supplier_number,
    s.business_name,
    st.code as tipo_proveedor,
    sb.block_reason,
    sb.valid_from,
    sb.valid_to
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
ORDER BY s.supplier_number;


-- ============================================================================
-- 6. VALIDAR PROVEEDORES ACTIVOS (NO BLOQUEADOS)
-- Endpoint: GET /suppliers/filter?tipoProveedor=0&estatusBloqueo=1
-- ============================================================================

-- 6.1 Contar proveedores SIN bloqueo activo vigente
SELECT COUNT(*) as total_activos
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND NOT EXISTS (
      SELECT 1
      FROM shared_catalogs.supplier_block sb
      WHERE sb.supplier_number = s.supplier_number
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  );

-- 6.2 Listar proveedores activos (no bloqueados)
SELECT
    s.id,
    s.supplier_number,
    s.business_name,
    st.code as tipo_proveedor
FROM shared_catalogs.supplier s
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
  AND NOT EXISTS (
      SELECT 1
      FROM shared_catalogs.supplier_block sb
      WHERE sb.supplier_number = s.supplier_number
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  )
ORDER BY s.id;


-- ============================================================================
-- 7. VALIDAR COMBINACIONES DE FILTROS
-- ============================================================================

-- 7.1 Proveedores MERCANCIA activos (tipoProveedor=1, estatusBloqueo=1)
SELECT COUNT(*) as mercancia_activos
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 1
  AND NOT EXISTS (
      SELECT 1
      FROM shared_catalogs.supplier_block sb
      WHERE sb.supplier_number = s.supplier_number
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  );

-- 7.2 Proveedores MERCANCIA bloqueados (tipoProveedor=1, estatusBloqueo=0)
SELECT COUNT(*) as mercancia_bloqueados
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
WHERE s.status = 1
  AND s.supplier_type_id = 1
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to;

-- 7.3 Proveedores TRANSPORTE activos (tipoProveedor=2, estatusBloqueo=1)
SELECT COUNT(*) as transporte_activos
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id = 2
  AND NOT EXISTS (
      SELECT 1
      FROM shared_catalogs.supplier_block sb
      WHERE sb.supplier_number = s.supplier_number
        AND sb.status = 1
        AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
  );

-- 7.4 Proveedores INDIRECTOS bloqueados (tipoProveedor=3, estatusBloqueo=0)
SELECT COUNT(*) as indirectos_bloqueados
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
WHERE s.status = 1
  AND s.supplier_type_id = 3
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to;


-- ============================================================================
-- 8. RESUMEN COMPLETO DE VALIDACION
-- ============================================================================

SELECT 'RESUMEN DE VALIDACION STM-831' as titulo;

-- 8.1 Totales generales
SELECT
    (SELECT COUNT(*) FROM shared_catalogs.supplier WHERE status = 1) as total_proveedores,
    (SELECT COUNT(DISTINCT s.supplier_number)
     FROM shared_catalogs.supplier s
     INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
     WHERE s.status = 1 AND sb.status = 1
       AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to) as total_bloqueados,
    (SELECT COUNT(*) FROM shared_catalogs.supplier s
     WHERE s.status = 1
       AND NOT EXISTS (
           SELECT 1 FROM shared_catalogs.supplier_block sb
           WHERE sb.supplier_number = s.supplier_number
             AND sb.status = 1
             AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
       )) as total_activos;

-- 8.2 Desglose por tipo de proveedor
SELECT
    st.id as tipo_id,
    st.code as tipo_code,
    COUNT(s.id) as total,
    SUM(CASE WHEN EXISTS (
        SELECT 1 FROM shared_catalogs.supplier_block sb
        WHERE sb.supplier_number = s.supplier_number
          AND sb.status = 1
          AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
    ) THEN 1 ELSE 0 END) as bloqueados,
    SUM(CASE WHEN NOT EXISTS (
        SELECT 1 FROM shared_catalogs.supplier_block sb
        WHERE sb.supplier_number = s.supplier_number
          AND sb.status = 1
          AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
    ) THEN 1 ELSE 0 END) as activos
FROM shared_catalogs.supplier s
JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
GROUP BY st.id, st.code
ORDER BY st.id;


-- ============================================================================
-- 9. VALIDAR DETALLE DE BLOQUEOS
-- Comparar con blockInfo del endpoint
-- ============================================================================

-- 9.1 Todos los bloqueos vigentes con detalle completo
SELECT
    s.id as supplier_id,
    s.supplier_number,
    s.rfc,
    s.business_name,
    st.code as supplier_type,
    sb.id as block_id,
    sb.valid_from,
    sb.valid_to,
    sb.block_reason,
    sb.status as block_status
FROM shared_catalogs.supplier s
INNER JOIN shared_catalogs.supplier_block sb ON s.supplier_number = sb.supplier_number
LEFT JOIN shared_catalogs.supplier_type st ON s.supplier_type_id = st.id
WHERE s.status = 1
  AND sb.status = 1
  AND CURRENT_DATE BETWEEN sb.valid_from AND sb.valid_to
ORDER BY s.supplier_number, sb.valid_from;


-- ============================================================================
-- 10. VERIFICAR INTEGRIDAD DE DATOS
-- ============================================================================

-- 10.1 Proveedores sin tipo asignado
SELECT COUNT(*) as proveedores_sin_tipo
FROM shared_catalogs.supplier s
WHERE s.status = 1
  AND s.supplier_type_id IS NULL;

-- 10.2 Bloqueos huerfanos (sin proveedor asociado)
SELECT COUNT(*) as bloqueos_huerfanos
FROM shared_catalogs.supplier_block sb
WHERE NOT EXISTS (
    SELECT 1 FROM shared_catalogs.supplier s
    WHERE s.supplier_number = sb.supplier_number
);

-- 10.3 Tipos de proveedor en uso
SELECT
    st.id,
    st.code,
    st.description,
    COUNT(s.id) as proveedores_asignados
FROM shared_catalogs.supplier_type st
LEFT JOIN shared_catalogs.supplier s ON s.supplier_type_id = st.id AND s.status = 1
WHERE st.status = 1
GROUP BY st.id, st.code, st.description
ORDER BY st.id;


-- ============================================================================
-- FIN DEL SCRIPT DE VALIDACION
-- ============================================================================
