package com.sodimac.rebates.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.rebates.model.Autorizacion;

public interface AutorizacionRepository extends JpaRepository<Autorizacion, Integer> {

	@Query(nativeQuery = true, value = "SELECT * FROM AutorizacionDescuento a WHERE a.fechaInicio >= :fechaInicio AND a.fechaFinal <= :fechaFinal AND a.descripcionPeriodo LIKE CONCAT('%',:descripcionPeriodo,'%') AND CAST(a.idCatProgramaPago AS nvarchar) LIKE :tipoPeriodo AND CAST(a.idperiodo AS nvarchar) LIKE :idPeriodo AND CAST(a.tipodeRebate AS nvarchar) LIKE :tipodeRebate")
	List<Autorizacion> findByAutorizacionoWithDates(@Param("fechaInicio") Date fechaInicio,
			@Param("fechaFinal") Date fechaFinal, @Param("descripcionPeriodo") String descripcionPeriodo,
			@Param("tipoPeriodo") String tipoPeriodo, @Param("idPeriodo") String idPeriodo,
			@Param("tipodeRebate") String tipodeRebate);

	@Query(nativeQuery = true, value = "SELECT * FROM AutorizacionDescuento a WHERE a.descripcionPeriodo LIKE CONCAT('%',:descripcionPeriodo,'%') AND CAST(a.idCatProgramaPago AS nvarchar) LIKE :tipoPeriodo AND CAST(a.idperiodo AS nvarchar) LIKE :idPeriodo AND CAST(a.tipodeRebate AS nvarchar) LIKE :tipodeRebate")
	List<Autorizacion> findByAutorizacionWithOutDates(@Param("descripcionPeriodo") String descripcionPeriodo,
			@Param("tipoPeriodo") String tipoPeriodo, @Param("idPeriodo") String idPeriodo,
			@Param("tipodeRebate") String tipodeRebate);

}
