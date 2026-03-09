package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.PagoComplementoEntity;

@Repository("complementoPagoRepository")
public interface ComplementoPagoRepository extends JpaRepository<PagoComplementoEntity, Integer>{
	
	@Query(value = "{call uspObtenerComplementosPagoByParams (:fechaInicial, :fechaFinal, :rfc, :start, :rowsPerPage, :estatusComplemento, :pmonto)}", nativeQuery = true)	
	List<Object[]> getComplementosByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("rfc") String rfc
			, @Param("start") int start
			, @Param("rowsPerPage") int rowsPerPage
			, @Param("estatusComplemento") String estatusComplemento
			, @Param("pmonto") Double pmonto);
	
	@Query(value = "{call uspObtenerComplementosExcelByParams (:fechaInicial, :fechaFinal, :rfc, :estatusPago, :pmonto )}", nativeQuery = true)	
	List<Object[]> getComplementosExcelByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("rfc") String rfc
			, @Param("estatusPago") String estatusPago
			, @Param("pmonto") Double pmonto);

	@Modifying
	@Query(value = "{call uspActualizarEstatusTimbrado(:idPagoComplemento, :estatusComplemento)}", nativeQuery = true)
	public void actualizarEstatusTimbrado(@Param("idPagoComplemento") Integer idPagoComplemento, @Param("estatusComplemento") String estatusComplemento);
	
	
	@Query(value = "{call uspObtenerReporteComplementosByParams (:fechaInicial, :fechaFinal, :rfc, :start, :rowsPerPage, :ticket, :uuid, :monto )}", nativeQuery = true)	
	List<Object[]> getReporteComplementosByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("rfc") String rfc
			, @Param("start") int start
			, @Param("rowsPerPage") int rowsPerPage
			, @Param("ticket") String ticket
			, @Param("uuid") String uuid
			, @Param("monto") String monto);
	
	@Query(value = "{call uspObtenerReporteComplementosExcelByParams (:fechaInicial, :fechaFinal, :rfc, :ticket, :uuid, :monto )}", nativeQuery = true)	
	List<Object[]> getReporteComplementosExcelByParams(
			  @Param("fechaInicial") String fechaInicial
			, @Param("fechaFinal") String fechaFinal
			, @Param("rfc") String rfc
			, @Param("ticket") String ticket
			, @Param("uuid") String uuid
			, @Param("monto") String monto);
}
