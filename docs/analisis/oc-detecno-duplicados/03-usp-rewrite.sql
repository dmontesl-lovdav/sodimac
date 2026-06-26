-- ============================================================
-- FASE 2 — SP reescrito: uspRegistroOrdenCompraProveedor
-- PROPUESTA. Validar en DEV antes de PROD.
-- Depende de: indice unico UX_OrdenCompraProveedor_Negocio (IGNORE_DUP_KEY=ON) de 02.
-- ============================================================
-- Cambios vs original:
--  1) Dedup defensivo de la temp (Java solo inserta, nunca trunca -> puede acumular).
--  2) Un solo INSERT idempotente. El indice unico ignora duplicados exactos; un cambio
--     de estatus entra como clave nueva. Elimina el INSERT #2 original (era redundante:
--     el INSERT #1 ya metia el cambio de estatus, el #2 lo duplicaba).
--  3) MAX correlacionado O(n^2) -> ROW_NUMBER() OVER(PARTITION BY...) para el Uuid.
--  4) CATCH hace THROW (no traga el error en silencio -> el batch lo registra).
-- ============================================================
ALTER PROCEDURE [dbo].[uspRegistroOrdenCompraProveedor]
AS
BEGIN
  SET NOCOUNT ON;

  BEGIN TRY
    BEGIN TRAN;

    -- 0) Dedup defensivo de la temp (por si acumulo filas entre corridas previas)
    ;WITH t AS (
      SELECT *,
             ROW_NUMBER() OVER (
               PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
               ORDER BY FechaRegistro DESC) AS rn
      FROM OrdenCompraProveedorTemp)
    DELETE FROM t WHERE rn > 1;

    -- 1) INSERT idempotente. Duplicados exactos -> ignorados por el indice unico.
    --    Cambio de estatus -> clave nueva -> entra como version nueva.
    INSERT INTO OrdenCompraProveedor
      (IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid)
    SELECT
       IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, NULL
    FROM OrdenCompraProveedorTemp;

    -- 2) Ultima factura por (prov, oc, recepcion) sin MAX correlacionado
    ;WITH fact AS (
      SELECT A.Uuid,
             CONVERT(NUMERIC, B.Extra1) AS IdProveedor,
             CONVERT(NUMERIC, B.Extra2) AS OrdenCompra,
             CONVERT(NUMERIC, B.Extra3) AS Recepcion,
             ROW_NUMBER() OVER (
               PARTITION BY B.Extra1, B.Extra2, B.Extra3
               ORDER BY A.Fecha DESC) AS rn
      FROM Comprobante A
      INNER JOIN Addenda B ON A.Uuid = B.Uuid
      WHERE B.Tipo = 1)
    SELECT Uuid, IdProveedor, OrdenCompra, Recepcion
    INTO #Factura
    FROM fact
    WHERE rn = 1;

    -- 3) Asignar Uuid (vacio si estatus != 2)
    UPDATE A
      SET A.Uuid = CASE WHEN A.Estatus <> 2 THEN '' ELSE B.Uuid END
    FROM OrdenCompraProveedor A
    INNER JOIN #Factura B
      ON A.NumeroProveedor = B.IdProveedor
     AND A.OrdenCompra     = B.OrdenCompra
     AND A.Recepcion       = B.Recepcion;

    TRUNCATE TABLE OrdenCompraProveedorTemp;
    DROP TABLE #Factura;

    COMMIT;
  END TRY
  BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;   -- relanzar para que el batch registre el fallo (no silenciar)
  END CATCH;
END
