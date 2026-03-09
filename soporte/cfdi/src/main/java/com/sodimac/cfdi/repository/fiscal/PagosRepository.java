package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.PagosEntity;

@Repository("pagosRepository")
public interface PagosRepository extends JpaRepository<PagosEntity, Integer>{

	@Query(value = "{call uspObtenerEstatusPagos (:tipoPago)}", nativeQuery = true)
	List<Object[]> findEstatusByTipoPago(@Param("tipoPago") String tipoPago);
	
	@Query(value = "{call uspObtenerPagosByParams (:fechaInicial, :fechaFinal, :start, :rowsPerPage, :estatusPago, :pmonto )}", nativeQuery = true)	
	List<Object[]> getPagosByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("start") int start
			, @Param("rowsPerPage") int rowsPerPage
			, @Param("estatusPago") String estatusPago
			, @Param("pmonto") Double pmonto);
	
	@Query(value = "{call uspObtenerPagosExcelByParams (:fechaInicial, :fechaFinal, :estatusPago, :pmonto)}", nativeQuery = true)	
	List<Object[]> getPagosExcelByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("estatusPago") String estatusPago
			, @Param("pmonto") Double pmonto);
	
	@Query(value = "select count(*) from catestatuspago where idEstatusPago = :idEstatusPago", nativeQuery = true) 
    int findEstatusByIdEstatusPago(@Param("idEstatusPago") String idEstatusPago);
}
