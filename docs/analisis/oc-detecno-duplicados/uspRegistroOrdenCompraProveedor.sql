-- ============================================================
-- SP: dbo.uspRegistroOrdenCompraProveedor
-- BD: SODIMAC_SAP_PROD (10.138.150.124:5319)
-- Capturado: 2026-06-25 (referencia analisis duplicados OC Detecno)
-- VERSION ORIGINAL (con bug de duplicacion + O(n^2)). NO re-desplegar tal cual.
-- ============================================================
CREATE PROCEDURE [dbo].[uspRegistroOrdenCompraProveedor]
AS

BEGIN TRANSACTION;

BEGIN
TRY

	INSERT INTO OrdenCompraProveedor
	SELECT
	  IdOrdenCompra
	, NumeroProveedor
	, OrdenCompra
	, Recepcion
	, Sucursal
	, NoGuia
	, ImporteSinImpuesto
	, FechaRecepcion
	, Estatus
	, Origen
	, FechaMovimiento
	, MotivoCancelacion
	, FechaRegistro
	, NULL Uuid
	FROM OrdenCompraProveedorTemp A
	WHERE  NOT EXISTS (
		SELECT	1
		FROM OrdenCompraProveedor B
		WHERE A.NumeroProveedor = B.NumeroProveedor
		AND A.OrdenCompra = B.OrdenCompra
		AND A.Recepcion = B.Recepcion
		AND A.FechaRecepcion = B.FechaRecepcion
		AND A.Estatus = B.Estatus
	);

	SELECT *
	INTO #OrdenCompraActual_Temp
	FROM vw_ordencompraproveedor A
	WHERE A.FechaRegistro = (
	   SELECT MAX(B.FechaRegistro)
	   FROM [dbo].[vw_ordencompraproveedor] B
	   WHERE A.NumeroProveedor = B.NumeroProveedor
	   AND A.OrdenCompra = B.OrdenCompra
	   AND A.Recepcion = B.Recepcion
	);

	INSERT INTO OrdenCompraProveedor
	SELECT
	  IdOrdenCompra
	, NumeroProveedor
	, OrdenCompra
	, Recepcion
	, Sucursal
	, NoGuia
	, ImporteSinImpuesto
	, FechaRecepcion
	, Estatus
	, Origen
	, FechaMovimiento
	, MotivoCancelacion
	, FechaRegistro
	, NULL Uuid
	FROM OrdenCompraProveedorTemp A
	WHERE  EXISTS (
		SELECT	1
		FROM #OrdenCompraActual_Temp B
		WHERE A.NumeroProveedor = B.NumeroProveedor
		AND A.OrdenCompra = B.OrdenCompra
		AND A.Recepcion = B.Recepcion
		AND A.FechaRecepcion = B.FechaRecepcion
		AND A.Estatus <> B.EstatusDetecno
	);

	SELECT A.Uuid
	, CONVERT(NUMERIC,B.Extra1)  IdProveedor
	, CONVERT(NUMERIC,B.Extra2)  OrdenCompra
	, CONVERT(NUMERIC,B.Extra3)  Recepcion
	INTO #FacturaTemp
	FROM Comprobante A
	INNER JOIN Addenda B
	ON A.Uuid = B.Uuid
	WHERE B.Tipo = 1
	AND A.Fecha = (
	    SELECT MAX(SUBA.Fecha)
		FROM Comprobante SUBA
		INNER JOIN Addenda SUBB
		ON SUBA.Uuid = SUBB.Uuid
		WHERE SUBB.Tipo = 1
		AND SUBB.Extra1 = B.Extra1
		AND SUBB.Extra2 = B.Extra2
		AND SUBB.Extra3 = B.Extra3
	)

	 UPDATE A  SET A.Uuid = (CASE WHEN A.Estatus != 2 THEN '' ELSE  B.Uuid END)
	 FROM OrdenCompraProveedor A
	 INNER JOIN #FacturaTemp B
	 ON A.NumeroProveedor = B.IdProveedor
	 AND A.OrdenCompra = B.OrdenCompra
	 AND A.Recepcion = B.Recepcion;

     TRUNCATE TABLE OrdenCompraProveedorTemp;
     DROP TABLE  #OrdenCompraActual_Temp
	--DROP TABLE #OrdenCompraTemp;
	 DROP TABLE #FacturaTemp;


	COMMIT;

END TRY

BEGIN CATCH
    ROLLBACK;
END CATCH;
