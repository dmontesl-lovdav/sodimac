-- ============================================================
-- FASE 2 — Blindaje contra re-duplicacion
-- BD: SODIMAC_SAP_PROD
-- Ejecutar DESPUES de 01-limpieza-tabla.sql (tabla ya en ~325k).
-- ============================================================

-- 0) Verificar que NO queden duplicados (debe dar 0 filas)
SELECT NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, COUNT(*) copias
FROM OrdenCompraProveedor
GROUP BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
HAVING COUNT(*) > 1;

-- 1) Indice UNICO sobre la clave de negocio.
--    IGNORE_DUP_KEY=ON -> un INSERT de duplicado se ignora (warning) en vez de fallar.
--    Esto hace el dedup a nivel motor: imposible volver a duplicar.
CREATE UNIQUE INDEX UX_OrdenCompraProveedor_Negocio
ON OrdenCompraProveedor (NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus)
WITH (IGNORE_DUP_KEY = ON);

-- 2) Indice de apoyo para los lookups por (prov, oc, recepcion) del SP/vista
CREATE INDEX IX_OrdenCompraProveedor_ProvOcRec
ON OrdenCompraProveedor (NumeroProveedor, OrdenCompra, Recepcion)
INCLUDE (FechaRecepcion, Estatus, FechaRegistro, Uuid);

-- Nota: si FechaRecepcion es VARCHAR con formato inconsistente, normalizar a DATETIME
-- ANTES de crear el indice unico, o el blindaje no cubre variantes del mismo instante.
-- Verificar tipo:  EXEC sp_help 'OrdenCompraProveedor';
