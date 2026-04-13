package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.rebates.model.RebateAcuerdos;

public interface RebateAcuerdosRepository extends JpaRepository<RebateAcuerdos, Integer>{
	
	@Query(nativeQuery = true, value = "SELECT * FROM vw_acuerdos_comerciales c WHERE (:proveedor IS NULL OR c.NumeroProveedor = :proveedor) AND (:razonSocial IS NULL OR UPPER(c.RazonSocial) LIKE UPPER(CONCAT('%', :razonSocial, '%'))) AND (:tipoAcuerdo IS NULL OR c.TipoAcuerdo = :tipoAcuerdo) AND (:programaPago IS NULL OR c.ProgramaPago = :programaPago)")
	List<RebateAcuerdos> findAcuerdosByParamsConsult(@Param("proveedor") String numeroProveedor, @Param("razonSocial") String razonSocial, @Param("tipoAcuerdo") String tipoAcuerdo, @Param("programaPago") String programaPago);

	@Query(nativeQuery = true, value = "SELECT distinct c.TipoAcuerdo FROM vw_acuerdos_comerciales c order by c.TipoAcuerdo asc")
	List<String> findTiposAcuerdos();

}
