package com.sodimac.rebates.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.ProgramaPago;

public interface PeriodoRepository extends JpaRepository<Periodo, Integer> {

	List<Periodo> findByActivo(boolean activo);
	
	List<Periodo> findByActivoOrderByIdCatPeriodoDesc(boolean activo);

	List<Periodo> findByActivoAndEstatusIn(boolean activo, Collection<Integer> estatus);
	
	List<Periodo> findByActivoAndEstatusNotIn(boolean activo, Collection<Integer> estatus);

	@Query("SELECT p FROM Periodo p WHERE p.fechaIni >= ?#{#fechaIni} AND p.fechaFin <= ?#{#fechaFin} AND p.detallePeriodo LIKE CONCAT('%',:detallePeriodo,'%') AND p.activo = true ORDER BY p.idCatPeriodo DESC")
	List<Periodo> findByPeriodoBetweenFechasAndDetallePeriodoLike(@Param("fechaIni") Date fechaIni,
			@Param("fechaFin") Date fechaFin, @Param("detallePeriodo") String detallePeriodo);

	@Query("SELECT p FROM Periodo p WHERE p.fechaIni >= ?#{#fechaIni} AND p.fechaFin <= ?#{#fechaFin} AND p.programaPago.idCatProgramaPago = :#{#programaPago.idCatProgramaPago} AND p.detallePeriodo LIKE CONCAT('%',:detallePeriodo,'%') AND p.activo = true ORDER BY p.idCatPeriodo DESC")
	List<Periodo> findByPeriodoWithOptions(@Param("fechaIni") Date fechaIni, @Param("fechaFin") Date fechaFin,
			@Param("programaPago") ProgramaPago programaPago, @Param("detallePeriodo") String detallePeriodo);

	@Query("SELECT p FROM Periodo p WHERE p.programaPago.idCatProgramaPago = :#{#programaPago.idCatProgramaPago} AND p.estatus IN(1,2,4,5) AND p.activo = true")
	List<Periodo> findProceso(@Param("programaPago") ProgramaPago programaPagoo);

	@Query(value="select a.idPerfilDestino from configflujo a inner join RelPeriodoTipoRebate b on b.IdCatPeriodo = :idCatPeriodo and a.idTipoRebate =b.IdCatTipoRebate and b.Activo=1 where a.idTipoApp=1 and a.idTipoFlujo=1 and a.idPerfilOrigen=:idPerfilUser and a.estatusOrigen=:estatusOrigen and a.estatusDestino=:estatusDestino ", nativeQuery = true)
	Integer getPerfil(@Param("idCatPeriodo") int idCatPeriodo, @Param("idPerfilUser") int idPerfilUser, @Param("estatusOrigen") int estatusOrigen, @Param("estatusDestino") Integer estatusDestino);

}
