package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.rebates.model.entity.ExclusionCargaDetEntity;

@Repository
public interface ExclusionCargaDetRepository extends JpaRepository<ExclusionCargaDetEntity, Long> {

	@Modifying
	@Query(value = "{call uspRegistraExclusionProveedor(:pIdCarga, :pNumProveedor, :pIdPeriodo, :pIdCatTipoRebate, :pIdUser)}", nativeQuery = true)	
	public void registraExclusionProveedor(
			  @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor") String pNumProveedor
			, @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdCatTipoRebate") Integer pIdCatTipoRebate
			, @Param("pIdUser") Integer idUser);
	
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionOrdenCompra(:pIdCarga, :pOrdenCompra, :pIdPeriodo, :pIdCatTipoRebate, :pIdUser)}", nativeQuery = true)	
	public void registraExclusionOrdenCompra(
			  @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pOrdenCompra") String pOrdenCompra
			, @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdCatTipoRebate") Integer pIdCatTipoRebate
			, @Param("pIdUser") Integer idUser);
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionFamilia(:pIdCarga, :pNumProveedor, :pFamilia, :pIdPeriodo, :pIdCatTipoRebate, :pIdUser)}", nativeQuery = true)	
	public void registraExclusionFamilia(
			  @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor")  String pNumProveedor
			, @Param("pFamilia") String pFamilia
			, @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdCatTipoRebate") Integer pIdCatTipoRebate
			, @Param("pIdUser") Integer idUser);
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionSKU(:pIdCarga, :pNumProveedor, :pSku, :pIdPeriodo, :pIdCatTipoRebate, :pIdUser)}", nativeQuery = true)	
	public void registraExclusionSKU(
			  @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor")  String pNumProveedor
			, @Param("pSku") String pSku
			, @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdCatTipoRebate") Integer pIdCatTipoRebate
			, @Param("pIdUser") Integer idUser);
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionProveedorManual(:pIdPeriodo, :pIdCarga, :pNumProveedor)}", nativeQuery = true)	
	public void registraExclusionProveedorManual(
			  @Param("pIdPeriodo") Integer pIdCatPeriodo
			, @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor") String pNumProveedor);
	
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionOrdenCompraManual(:pIdCarga, :pNumProveedor, :pOrdenCompra)}", nativeQuery = true)	
	public void registraExclusionOrdenCompraManual(
			  @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor") String pNumProveedor
			, @Param("pOrdenCompra") String pOrdenCompra);
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionFamiliaManual(:pIdPeriodo, :pIdCarga, :pNumProveedor, :pFamilia)}", nativeQuery = true)	
	public void registraExclusionFamiliaManual(
			  @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor") String pNumProveedor
			, @Param("pFamilia") String pFamilia);
	
	@Modifying
	@Query(value = "{call uspRegistraExclusionSKUManual(:pIdPeriodo, :pIdCarga, :pNumProveedor, :pFamilia, :pOrdenCompra, :pSku)}", nativeQuery = true)	
	public void uspRegistraExclusionSKUManual(
			  @Param("pIdPeriodo") Integer pIdPeriodo
			, @Param("pIdCarga") Long pIdCargaExclusion
			, @Param("pNumProveedor") String pNumProveedor
			, @Param("pFamilia") String pFamilia
			, @Param("pOrdenCompra") String pOrdenCompra
			, @Param("pSku") String pSku);

	public ExclusionCargaDetEntity findByIdExclusionCargaDet(Long idExclusionCargaDet);
	
}
