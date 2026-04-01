----------------------
USE [SODIMAC_REBATES_PROD]
DECLARE @return_value int

EXEC	@return_value = [dbo].[uspGetExclusiones] 2, '', '', 0, 0, '', ''

SELECT	'Return Value' = @return_value

----------------
CREATE PROCEDURE [dbo].[uspGetExclusiones]    @pIdUsuario int
											, @pFolio varchar(50) 
										    , @pComentario varchar(250)
											, @pIdPeriodo int
											, @pIdTipoExclusion int
											, @pNumProveedor varchar(50)
  											, @pOrdenCompra varchar(50)
AS

BEGIN
	
	DECLARE @vIdPerfil int;
	DECLARE @vIdDirectorComercial int;
	DECLARE @vIdGerenteComercial int;
	DECLARE @vIdJefeComercial int;
	DECLARE @countProveedores int;
	DECLARE @ListaProveedores TABLE (numeroProveedor varchar(100));
	DECLARE @vIdPerfilFinanciero int;
	DECLARE @vIdPerfilLogistico int;
	DECLARE @vCompradorAsignado INT;
	DECLARE @vSeguridadProveedor INT;

	CREATE TABLE #ProveedorCompradorTemp (
		idProveedor NUMERIC,
		idComprador NUMERIC
	);

	SET  @vCompradorAsignado = 0;

	SET @vIdDirectorComercial = 3;
	SET @vIdGerenteComercial = 4;
	SET @vIdJefeComercial = 11

	SET @countProveedores = 0;
	SET @vIdPerfilFinanciero = 1;
	SET @vIdPerfilLogistico = 2;

	SELECT @vIdPerfil = idperfil 
	FROM CatUsuarioPerfil
	WHERE idusuario = @pIdUsuario;
	
	IF ( @vIdPerfil NOT IN ( @vIdGerenteComercial, @vIdDirectorComercial, @vIdJefeComercial) ) 
		BEGIN 

			SELECT 
			e.IdExclusion,	e.IdCatTipoRebate,	IdCatTipoExclusion,	IdCatEstatusExclusion,	IdCatPeriodo,	
			Folio,	Comentario,	IdUsuarioSolicitud,	IdUsuarioAutorizacion,	FechaHoraSolicitud,	FechaHoraAutorizacion,	
			e.Activo,	Contabilizado,	Evidencia,	Imagen
			FROM [dbo].[Exclusion] e 
			LEFT JOIN [dbo].[ExclusionCarga] ec 
			ON e.IdExclusion = ec.IdExclusion
			LEFT JOIN [dbo].[ExclusionCargaDet] ecd 
			ON ec.IdExclusionCarga = ecd.IdExclusionCarga
			INNER JOIN vw_perfilRebate Reb
			ON e.IdCatTipoRebate = Reb.IdCatTipoRebate
			AND Reb.idCatPerfil = @vIdPerfil
			AND Reb.IdUsuario = @pIdUsuario
			WHERE e.Activo=1 AND
			(ecd.Activo  = 1 OR ecd.Activo  IS NULL)  AND 
			(ec.Activo   = 1 OR ec.Activo IS NULL)  AND
			(((CASE WHEN @pFolio IS NULL THEN e.Folio ELSE  @pFolio END) = e.Folio ) OR e.Folio LIKE '%'+@pFolio+'%') AND
		    (((CASE WHEN @pComentario IS NULL THEN e.Comentario ELSE  @pComentario END) = e.Comentario ) OR e.Comentario LIKE '%'+@pComentario+'%') AND
		    ((CASE WHEN @pIdPeriodo IS NULL THEN e.IdCatPeriodo ELSE  @pIdPeriodo END) = e.IdCatPeriodo )  AND
		    ((CASE WHEN @pIdTipoExclusion IS NULL THEN e.IdCatTipoExclusion ELSE  @pIdTipoExclusion END) = e.IdCatTipoExclusion ) AND
		    (@pNumProveedor IS NULL OR ecd.NumProveedor = @pNumProveedor )  AND
			(@pOrdenCompra IS NULL  OR ecd.OrdenCompra  = @pOrdenCompra )  
			GROUP BY e.IdExclusion,	e.IdCatTipoRebate,	IdCatTipoExclusion,	IdCatEstatusExclusion,	IdCatPeriodo,	
			Folio,	Comentario,	IdUsuarioSolicitud,	IdUsuarioAutorizacion,	FechaHoraSolicitud,	FechaHoraAutorizacion,	
			e.Activo,	Contabilizado,	Evidencia,	Imagen
			ORDER BY e.IdExclusion DESC;

		END;

    IF ( @vIdPerfil  IN ( @vIdGerenteComercial, @vIdDirectorComercial, @vIdJefeComercial) )
	   BEGIN

			SELECT @vSeguridadProveedor = COUNT(1)
			FROM (
            	SELECT DISTINCT cp.numeroProveedor,jc.idcomprador 
				FROM CatCompradorProveedor cp 
				INNER JOIN CatJefeComprador jc
				ON cp.idcomprador = jc.idcomprador
				WHERE jc.idjefe  =  (CASE WHEN @vIdPerfil  IN ( @vIdGerenteComercial, @vIdDirectorComercial) THEN  @pIdUsuario ELSE jc.idjefe END)
				AND jc.idcomprador = (CASE WHEN @vIdPerfil  IN ( @vIdJefeComercial) THEN  @pIdUsuario ELSE jc.idcomprador END)
			) A;

			IF @vSeguridadProveedor > 0
				BEGIN 

					INSERT INTO #ProveedorCompradorTemp
            		SELECT DISTINCT cp.numeroProveedor,jc.idcomprador 
					FROM CatCompradorProveedor cp 
					INNER JOIN CatJefeComprador jc
					ON cp.idcomprador = jc.idcomprador
					WHERE jc.idjefe  =  (CASE WHEN @vIdPerfil  IN ( @vIdGerenteComercial, @vIdDirectorComercial) THEN  @pIdUsuario ELSE jc.idjefe END)
					AND jc.idcomprador = (CASE WHEN @vIdPerfil  IN ( @vIdJefeComercial) THEN  @pIdUsuario ELSE jc.idcomprador END)
	
				END;

		    ELSE
				BEGIN

					INSERT INTO #ProveedorCompradorTemp
            		SELECT DISTINCT cp.numeroProveedor,@pIdUsuario 
	                FROM RebateAcuerdos cp
					union
					select CodigoProveedor,@pIdUsuario from RebateProveedor

				END;

    
		SELECT @vCompradorAsignado = COUNT(1) 
		FROM CatJefeComprador WHERE idjefe = @pIdUsuario

		IF  @vCompradorAsignado = 0
			BEGIN

				SELECT 
				e.IdExclusion,	e.IdCatTipoRebate,	IdCatTipoExclusion,	IdCatEstatusExclusion,	IdCatPeriodo,	
				Folio,	Comentario,	IdUsuarioSolicitud,	IdUsuarioAutorizacion,	FechaHoraSolicitud,	FechaHoraAutorizacion,	
				e.Activo,	Contabilizado,	Evidencia,	Imagen
				FROM [dbo].[Exclusion] e 
				LEFT JOIN [dbo].[ExclusionCarga] ec 
				ON e.IdExclusion = ec.IdExclusion
				LEFT JOIN [dbo].[ExclusionCargaDet] ecd 
				ON ec.IdExclusionCarga = ecd.IdExclusionCarga
				INNER JOIN vw_perfilRebate Reb
				ON e.IdCatTipoRebate = Reb.IdCatTipoRebate
				AND Reb.idCatPerfil = @vIdPerfil
				AND Reb.IdUsuario = @pIdUsuario
				WHERE e.Activo=1 AND
				(ecd.Activo  = 1 OR ecd.Activo  IS NULL)  AND 
				(ec.Activo   = 1 OR ec.Activo IS NULL)  AND
				(((CASE WHEN @pFolio IS NULL THEN e.Folio ELSE  @pFolio END) = e.Folio ) OR e.Folio LIKE '%'+@pFolio+'%') AND
				(((CASE WHEN @pComentario IS NULL THEN e.Comentario ELSE  @pComentario END) = e.Comentario ) OR e.Comentario LIKE '%'+@pComentario+'%') AND
				((CASE WHEN @pIdPeriodo IS NULL THEN e.IdCatPeriodo ELSE  @pIdPeriodo END) = e.IdCatPeriodo )  AND
				((CASE WHEN @pIdTipoExclusion IS NULL THEN e.IdCatTipoExclusion ELSE  @pIdTipoExclusion END) = e.IdCatTipoExclusion ) AND
				(@pNumProveedor IS NULL OR ecd.NumProveedor = @pNumProveedor )  AND
				(@pOrdenCompra IS NULL  OR ecd.OrdenCompra  = @pOrdenCompra )  AND
				(ecd.NumProveedor IN (SELECT idProveedor FROM #ProveedorCompradorTemp) OR ecd.NumProveedor IS NULL) AND
				(e.IdUsuarioSolicitud = @pIdUsuario or (e.IdUsuarioSolicitud in (select idusuario from CatUsuarioPerfil where idperfil in (@vIdPerfilFinanciero, @vIdPerfilLogistico)))   
				or  e.IdUsuarioSolicitud IN ( SELECT x.idJefe FROM CatJefeComprador x WHERE x.idComprador = @pIdUsuario) )
				GROUP BY e.IdExclusion,	e.IdCatTipoRebate,	IdCatTipoExclusion,	IdCatEstatusExclusion,	IdCatPeriodo,	
				Folio,	Comentario,	IdUsuarioSolicitud,	IdUsuarioAutorizacion,	FechaHoraSolicitud,	FechaHoraAutorizacion,	
				e.Activo,	Contabilizado,	Evidencia,	Imagen
				ORDER BY e.IdExclusion DESC;
		
			END;

		IF @vCompradorAsignado >  0
			BEGIN

				SELECT 
				e.IdExclusion,	e.IdCatTipoRebate,	IdCatTipoExclusion,	IdCatEstatusExclusion,	IdCatPeriodo,	
				Folio,	Comentario,	IdUsuarioSolicitud,	IdUsuarioAutorizacion,	FechaHoraSolicitud,	FechaHoraAutorizacion,	
				e.Activo, Contabilizado,	Evidencia,	Imagen
				FROM [dbo].[Exclusion] e 
				LEFT JOIN [dbo].[ExclusionCarga] ec 
				ON e.IdExclusion = ec.IdExclusion
				LEFT JOIN [dbo].[ExclusionCargaDet] ecd 
				ON ec.IdExclusionCarga = ecd.IdExclusionCarga
				INNER JOIN vw_perfilRebate Reb
				ON e.IdCatTipoRebate = Reb.IdCatTipoRebate
				AND Reb.idCatPerfil = @vIdPerfil
				AND Reb.IdUsuario = @pIdUsuario
				WHERE e.Activo=1 AND
				(ecd.Activo  = 1 OR ecd.Activo  IS NULL)  AND 
				(ec.Activo   = 1 OR ec.Activo IS NULL)  AND
				(((CASE WHEN @pFolio IS NULL THEN e.Folio ELSE  @pFolio END) = e.Folio ) OR e.Folio LIKE '%'+@pFolio+'%') AND
				(((CASE WHEN @pComentario IS NULL THEN e.Comentario ELSE  @pComentario END) = e.Comentario ) OR e.Comentario LIKE '%'+@pComentario+'%') AND
				((CASE WHEN @pIdPeriodo IS NULL THEN e.IdCatPeriodo ELSE  @pIdPeriodo END) = e.IdCatPeriodo )  AND
				((CASE WHEN @pIdTipoExclusion IS NULL THEN e.IdCatTipoExclusion ELSE  @pIdTipoExclusion END) = e.IdCatTipoExclusion ) AND
				(@pNumProveedor IS NULL OR ecd.NumProveedor = @pNumProveedor )  AND
				(@pOrdenCompra IS NULL  OR ecd.OrdenCompra  = @pOrdenCompra )  AND
				(ecd.NumProveedor IN (SELECT idProveedor FROM #ProveedorCompradorTemp) OR ecd.NumProveedor IS NULL) AND
				(e.IdUsuarioSolicitud = @pIdUsuario or (e.IdUsuarioSolicitud in (select idusuario from CatUsuarioPerfil where idperfil in (@vIdPerfilFinanciero, @vIdPerfilLogistico)))  
				or  e.IdUsuarioSolicitud IN ( SELECT x.idComprador FROM CatJefeComprador x WHERE x.idjefe = @pIdUsuario) )
				GROUP BY e.IdExclusion,	e.IdCatTipoRebate,	IdCatTipoExclusion,	IdCatEstatusExclusion,	IdCatPeriodo,	
				Folio,	Comentario,	IdUsuarioSolicitud,	IdUsuarioAutorizacion,	FechaHoraSolicitud,	FechaHoraAutorizacion,	
				e.Activo,	Contabilizado,	Evidencia,	Imagen
				ORDER BY e.IdExclusion DESC;

			END;

	END;

END;