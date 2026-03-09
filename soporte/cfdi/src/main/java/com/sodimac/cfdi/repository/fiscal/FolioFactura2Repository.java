package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.FolioFacturaEntity;

@Repository("folioFactura2Repository")
public interface FolioFactura2Repository extends JpaRepository<FolioFacturaEntity, Integer> { 

	@Query(value = "{call uspObtenerIdFolioFactura (:pFolioFactura)}", nativeQuery = true)	
	public Integer getIdFolioFactura(@Param("pFolioFactura") Integer pFolioFactura);
	
	@Query(value = "{call uspObtenerTotalFolioFactura (:pIdFolioFactura)}", nativeQuery = true)	
	public Double getTotalFolioFactura(@Param("pIdFolioFactura") Integer pIdFolioFactura);
	
	@Query(value = "{call uspObtenerTotalPagosFolioFactura (:pIdFolioFactura)}", nativeQuery = true)	
	public Double getTotalPagosFolioFactura(@Param("pIdFolioFactura") Integer pIdFolioFactura);
	
	@Query(value = "{call uspObtenerTotalOtrosPagosFolioFactura (:pIdFolioFactura, :pIdPagoComplemento)}", nativeQuery = true)	
	public Double getTotalOtrosPagosFolioFactura(@Param("pIdFolioFactura") Integer pIdFolioFactura
											  , @Param("pIdPagoComplemento") Integer pIdPagoComplemento);
	
	@Modifying
	@Query(value = "{call uspObtenerFacturasByIdFolioFactura(:pIdFolioFactura)}", nativeQuery = true)	
	public List<Object[]> getFacturasByIdFolioFactura(@Param("pIdFolioFactura") Integer pIdFolioFactura);
	
		
}
