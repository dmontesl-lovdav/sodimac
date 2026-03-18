package com.sodimac.facturacion.repository.fis;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.facturacion.entity.fis.FolioFacturaImpuestosEntity;

@Repository("folioFacturaImpuestosRepository")
public interface FolioFacturaImpuestosRepository extends JpaRepository<FolioFacturaImpuestosEntity, Integer> {

	@Query(value = "{call uspObtenerImpuestosFolioFactura (:folioFactura)}", nativeQuery = true)	
	List<Object[]> obtenerTotalesImpuestosFolioFactura(@Param("folioFactura") int folioFactura);

	@Query(value = "{call uspObtenerImpuestosUuid (:uuid)}", nativeQuery = true)	
	List<FolioFacturaImpuestosEntity> obtenerImpuestosUuid(@Param("uuid") String uuid);
	
}
