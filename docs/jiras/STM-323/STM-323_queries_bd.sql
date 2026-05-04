-- =============================================================================
-- STM-323: Facturas — Consultas de validación en BD
-- DB: b2b_portal (PostgreSQL, puerto 5434)
-- Nota: fiscal-api filtra por addendum.supplier_number JOIN invoice
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Configuración de seguridad: usuarios y atributos ATR001 (Proveedor)
-- -----------------------------------------------------------------------------
SELECT
    ud.user_data_id,
    ud.preferred_username,
    cd_type.key  AS tipo_atributo,
    cd_val.key   AS valor_atributo
FROM core_security.user_data ud
JOIN core_security.user_attribute ua
    ON ua.user_data_id = ud.user_data_id
JOIN shared_catalogs.catalog_detail cd_type
    ON cd_type.id = ua.catalog_detail_attribute_type_id
LEFT JOIN shared_catalogs.catalog_detail cd_val
    ON cd_val.id = ua.catalog_detail_attribute_value_id
WHERE cd_type.key = 'ATR001'
ORDER BY ud.preferred_username;

-- Resultado esperado:
-- USR_FERNANDO  → ATR001 = 11111
-- USR_JOSE      → ATR001 = 11111, 22222
-- zedlav.sd18   → ATR001 = -1  (acceso total)
-- USR_ANA       → (sin ATR001) → WRN7029


-- -----------------------------------------------------------------------------
-- 2. Distribución de facturas por supplier_number en addendum
-- -----------------------------------------------------------------------------
SELECT
    CAST(a.supplier_number AS TEXT) AS supplier_number,
    COUNT(DISTINCT i.invoice_uuid) AS facturas
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
GROUP BY a.supplier_number
ORDER BY a.supplier_number;

-- Resultado actual:
-- 1      →   1 factura
-- 11111  →  15 facturas
-- 12345  →  13 facturas
-- 22222  →   9 facturas
-- 33333  →  12 facturas
-- 44444  →  13 facturas
-- 54321  →  14 facturas
-- 55555  →  13 facturas
-- 67890  →  13 facturas
-- Total invoices con addendum: 103
-- Total invoices (sin filtro): 127


-- -----------------------------------------------------------------------------
-- 3. Simular filtro: USR_FERNANDO (ATR001 = 11111)
-- -----------------------------------------------------------------------------
SELECT COUNT(DISTINCT i.invoice_uuid) AS total_resultado
FROM tenant_fiscal.invoice i
WHERE i.invoice_uuid IN (
    SELECT a.invoice_uuid
    FROM tenant_fiscal.addendum a
    WHERE CAST(a.supplier_number AS TEXT) IN ('11111')
);
-- Esperado: 15 facturas


-- -----------------------------------------------------------------------------
-- 4. Simular filtro: USR_JOSE (ATR001 = 11111, 22222 — OR lógico)
-- -----------------------------------------------------------------------------
SELECT COUNT(DISTINCT i.invoice_uuid) AS total_resultado
FROM tenant_fiscal.invoice i
WHERE i.invoice_uuid IN (
    SELECT a.invoice_uuid
    FROM tenant_fiscal.addendum a
    WHERE CAST(a.supplier_number AS TEXT) IN ('11111', '22222')
);
-- Esperado: 24 facturas (15 + 9)


-- -----------------------------------------------------------------------------
-- 5. Simular filtro: Ivan (ATR001 = -1 → sin filtro, todas las facturas)
-- -----------------------------------------------------------------------------
SELECT COUNT(*) AS total_resultado
FROM tenant_fiscal.invoice;
-- Esperado: 121 facturas (sin filtro de addendum)


-- -----------------------------------------------------------------------------
-- 6. Ver facturas filtradas con detalle (proveedor 11111)
-- -----------------------------------------------------------------------------
SELECT
    i.invoice_uuid,
    i.folio,
    i.series,
    i.issue_date,
    i.total,
    i.status,
    CAST(a.supplier_number AS TEXT) AS supplier_number,
    a.reception_number,
    a.purchase_order_number
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
WHERE CAST(a.supplier_number AS TEXT) IN ('11111')
ORDER BY i.issue_date DESC
LIMIT 10;


-- -----------------------------------------------------------------------------
-- 7. Ver facturas filtradas con detalle (proveedor 11111 + 22222)
-- -----------------------------------------------------------------------------
SELECT
    CAST(a.supplier_number AS TEXT) AS supplier_number,
    COUNT(DISTINCT i.invoice_uuid) AS facturas,
    MIN(i.issue_date) AS primera_fecha,
    MAX(i.issue_date) AS ultima_fecha
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid
WHERE CAST(a.supplier_number AS TEXT) IN ('11111', '22222')
GROUP BY a.supplier_number
ORDER BY a.supplier_number;
-- Esperado: 11111→15, 22222→9


-- -----------------------------------------------------------------------------
-- 8. Verificar mecanismo de subquery JPA usado en InvoiceSpecification
--    (replica lo que hace el backend Java con Criteria API)
-- -----------------------------------------------------------------------------
SELECT i.invoice_uuid, i.folio, i.status
FROM tenant_fiscal.invoice i
WHERE i.invoice_uuid IN (
    SELECT a.invoice_uuid
    FROM tenant_fiscal.addendum a
    WHERE CAST(a.supplier_number AS TEXT) IN ('11111', '22222')
)
ORDER BY i.status, i.issue_date DESC
LIMIT 20;


-- -----------------------------------------------------------------------------
-- 9. Verificar USR_ANA → WRN7029 (tiene ATR002 pero NO ATR001)
-- -----------------------------------------------------------------------------
SELECT
    ud.preferred_username,
    cd_type.key AS tipo_atributo,
    cd_val.key  AS valor_atributo
FROM core_security.user_data ud
JOIN core_security.user_attribute ua ON ua.user_data_id = ud.user_data_id
JOIN shared_catalogs.catalog_detail cd_type ON cd_type.id = ua.catalog_detail_attribute_type_id
LEFT JOIN shared_catalogs.catalog_detail cd_val ON cd_val.id = ua.catalog_detail_attribute_value_id
WHERE ud.preferred_username = 'USR_ANA';
-- Esperado: ATR002 = TPR001 → NO tiene ATR001
-- → BFF inyecta x-user-vendors = '' (vacío)
-- → Backend InvoiceController.parseVendorHeader('') retorna lista vacía
-- → Retorna HTTP 400 WRN7029


-- -----------------------------------------------------------------------------
-- 10. Resumen general de cobertura del filtro
-- -----------------------------------------------------------------------------
SELECT
    COUNT(*) FILTER (WHERE CAST(a.supplier_number AS TEXT) IN ('11111'))          AS facturas_fernando,
    COUNT(*) FILTER (WHERE CAST(a.supplier_number AS TEXT) IN ('11111', '22222')) AS facturas_jose,
    COUNT(*)                                                                        AS facturas_ivan_total
FROM tenant_fiscal.invoice i
JOIN tenant_fiscal.addendum a ON a.invoice_uuid = i.invoice_uuid;
-- Esperado: fernando=15, jose=24, ivan=103 (con addendum) o 127 (total)
