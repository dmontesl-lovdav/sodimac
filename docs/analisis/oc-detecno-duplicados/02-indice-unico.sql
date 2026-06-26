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

-- Contexto: la tabla original es un HEAP (sin PK, sin clustered, sin indice unico).
-- Ya existe IX_OrdenCompraProveedor_OrdenCompra_Recepcion_Proveedor sobre
-- (NumeroProveedor, OrdenCompra, Recepcion) -> NO recrearlo.

-- 1) Indice UNICO CLUSTERED sobre la clave de negocio.
--    - Elimina el HEAP (ordena fisicamente por clave de negocio).
--    - IGNORE_DUP_KEY=ON -> un INSERT de duplicado se ignora (warning) en vez de fallar.
--    - Dedup a nivel motor: imposible volver a duplicar.
CREATE UNIQUE CLUSTERED INDEX UX_OrdenCompraProveedor_Negocio
ON OrdenCompraProveedor (NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus)
WITH (IGNORE_DUP_KEY = ON);

-- FechaRecepcion es varchar(50). Los duplicados medidos eran byte-identicos
-- ("2026-01-05T00:00:00") -> Detecno manda formato consistente -> el indice unico
-- los colapsa sin normalizar. NO requiere convertir a datetime para el fix actual.
--
-- Riesgo residual: si Detecno cambia el formato del string a futuro, generaria claves
-- "distintas". Hardening opcional (no bloqueante): normalizar FechaRecepcion a datetime
-- en tabla y en el batch (entity Java + columna).
--
-- Ojo NULL: FechaRecepcion es nullable. Un indice UNICO en SQL Server permite UNA sola
-- fila con NULL por combinacion. No deberia haber ordenes reales con FechaRecepcion NULL.
