package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.ExclusionEntity;

@Repository
public interface ExclusionRepository extends JpaRepository<ExclusionEntity, Integer> {

	@Query(value = "select (max(folio) + 1) from Exclusion", nativeQuery = true)
	public Integer getMaxFolio();

	public ExclusionEntity findByIdExclusion(Integer idExclusion);
	
	@Modifying
	@Query(value = "{call uspAutorizarExclusion(:pIdExclusion, :pIdUsuario)}", nativeQuery = true)	
	public void autorizarExclusion(
			  @Param("pIdExclusion") Integer pIdExclusion
			, @Param("pIdUsuario") Integer pIdUsuario);
	
	@Modifying
	@Query(value = "{call uspRechazarExclusion(:pIdExclusion, :pIdUsuario)}", nativeQuery = true)	
	public void rechazarExclusion(
			  @Param("pIdExclusion") Integer pIdExclusion
			, @Param("pIdUsuario") Integer pIdUsuario);
	
	@Query(value = "{call uspGetExclusiones(:pIdUsuario, :pFolio, :pComentario, :pIdPeriodo, :pIdTipoExclusion, :pNumProveedor, :pOrdenCompra)}", nativeQuery = true)	
	public List<ExclusionEntity> getExclusiones(
			@Param("pIdUsuario") Integer idUsuario
			, @Param("pFolio") String pFolio
			, @Param("pComentario") String pComentario
			, @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdTipoExclusion") Integer pIdTipoExclusion
			, @Param("pNumProveedor") String pNumProveedor
			, @Param("pOrdenCompra") String pOrdenCompra);
	
	@Query(value = "{call uspGetOrdenesCompraDisponibles(:pIdUsuario, :pIdPeriodo, :pIdTipoExclusion)}", nativeQuery = true)	
	public List<Object[]> getOrdenesCompraDisponibles(
			@Param("pIdUsuario") Integer idUsuario
			, @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdTipoExclusion") Integer pIdTipoExclusion);

	@Query(value = "select idPerfil from perfilesExclusionAutorizado where idPerfil=:pIdPerfil and idTipoExclusion =:pIdTipoExclusion and activo = 1", nativeQuery = true)
	public Integer getperfilAutorizado(@Param("pIdPerfil") Integer idPerfil, @Param("pIdTipoExclusion") Integer idTipoExclusion);

	@Query(value = "select idjefe from CatJefeComprador where idjefe = :pidjefe and idcomprador = :pidcomprador", nativeQuery = true)
	public Integer getJefeComprador(@Param("pidjefe") Integer idjefe, @Param("pidcomprador") Integer idcomprador);
}
