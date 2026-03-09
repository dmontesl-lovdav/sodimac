package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.FacturacionVveeEntity;

@Repository
public interface FacturacionVveeRepository extends JpaRepository<FacturacionVveeEntity, Integer>{

	@Query(value = "{call uspObtenerFacturacionVveeByParams (:pfechaInicial, :pfechafinal, :pticket, :start, :rowsPerPage, :ptienda)}", nativeQuery = true)	
	public List<Object[]> getFacturacionVveeByParams(
			  @Param("pfechaInicial") String fechaInicial
			, @Param("pfechafinal") String fechaFinal
			, @Param("pticket") String ticket
			, @Param("start") int start
			, @Param("rowsPerPage") int rowsPerPage
			, @Param("ptienda") Integer pTienda);
}
