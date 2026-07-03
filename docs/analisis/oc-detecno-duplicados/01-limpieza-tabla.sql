-- ============================================================
-- FASE 1 — Limpieza de duplicados OrdenCompraProveedor (53M -> ~325k)
-- BD: SODIMAC_SAP_PROD
-- REQUIERE: backup full + ventana + aprobacion. Ejecutar primero en DEV.
-- NO correr mientras haya rollback/insert activo sobre la tabla.
-- ============================================================

-- 0) Pre-check: tamano actual
SELECT COUNT(*) AS total_actual FROM OrdenCompraProveedor WITH (NOLOCK);
-- esperado ~53,453,419

-- 1) Construir tabla limpia: 1 fila por clave de negocio.
--    Regla de conservacion: prioriza fila con Uuid lleno, luego FechaRegistro mas reciente.
SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
INTO OrdenCompraProveedor_Clean
FROM (
  SELECT *,
         ROW_NUMBER() OVER (
           PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
           ORDER BY CASE WHEN ISNULL(Uuid,'') <> '' THEN 0 ELSE 1 END,
                    FechaRegistro DESC
         ) AS rn
  FROM OrdenCompraProveedor WITH (NOLOCK)
) t
WHERE rn = 1;

-- 2) Validar
SELECT COUNT(*) AS total_limpio FROM OrdenCompraProveedor_Clean;       -- esperado ~325,670
SELECT COUNT(*) AS con_uuid     FROM OrdenCompraProveedor_Clean WHERE ISNULL(Uuid,'') <> '';

-- 3) Swap de nombres (ventana, sin sesiones activas sobre la tabla)
BEGIN TRAN;
  EXEC sp_rename 'OrdenCompraProveedor',       'OrdenCompraProveedor_OLD_20260703';
  EXEC sp_rename 'OrdenCompraProveedor_Clean', 'OrdenCompraProveedor';
COMMIT;

-- ROLLBACK del swap (si algo sale mal en caliente): deshacer los renames
-- BEGIN TRAN;
--   EXEC sp_rename 'OrdenCompraProveedor',               'OrdenCompraProveedor_Clean';
--   EXEC sp_rename 'OrdenCompraProveedor_OLD_20260703',  'OrdenCompraProveedor';
-- COMMIT;

-- 4) Conservar _OLD hasta validar todo en caliente varios dias; SOLO despues:
-- DROP TABLE OrdenCompraProveedor_OLD_20260703;
