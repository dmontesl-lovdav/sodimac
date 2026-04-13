package com.sodimac.rebates.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sodimac.rebates.model.ControlDocumento;
import com.sodimac.rebates.model.Documento;
import com.sodimac.rebates.model.Periodo;

public interface ControlDocumentoRepository extends JpaRepository<ControlDocumento, Integer> {

	List<ControlDocumento> findByActivo(boolean activo);

	List<ControlDocumento> findByDocumentoAndPeriodoAndActivo(Documento documento, Periodo periodo, boolean activo);

	List<ControlDocumento> findByPeriodoAndActivo(Periodo periodo, boolean activo);

	@Query("SELECT c FROM ControlDocumento c WHERE c.activo = true AND ( EXISTS(SELECT p FROM Periodo p WHERE c.periodo.idCatPeriodo = p.idCatPeriodo AND p.estatus <> 6) OR c.documento.idDocumento = 14 )")
	List<ControlDocumento> findDistinctByPeriodoAndActivo();

	@Query("SELECT c FROM ControlDocumento c WHERE c.fechaHoraCarga BETWEEN ?#{#fechaCargaIni} AND ?#{#fechaCargaFin} AND c.nombreArchivo LIKE CONCAT('%',:nombreArchivo,'%') AND CAST(c.documento.idDocumento AS string) LIKE :idDocumento AND CAST(c.periodo.idCatPeriodo AS string) LIKE :idPeriodo AND c.activo = true AND EXISTS(SELECT p FROM Periodo p WHERE c.periodo.idCatPeriodo = p.idCatPeriodo AND p.estatus <> 6)")
	List<ControlDocumento> findByActivoWithDates(@Param("fechaCargaIni") Date fechaCargaIni,
			@Param("fechaCargaFin") Date fechaCargaFin, @Param("nombreArchivo") String nombreArchivo,
			@Param("idDocumento") String idDocumento, @Param("idPeriodo") String idPeriodo);
	
	@Query("SELECT c FROM ControlDocumento c WHERE c.nombreArchivo LIKE CONCAT('%',:nombreArchivo,'%') AND CAST(c.documento.idDocumento AS string) LIKE :idDocumento AND CAST(c.periodo.idCatPeriodo AS string) LIKE :idPeriodo AND c.activo = true AND EXISTS(SELECT p FROM Periodo p WHERE c.periodo.idCatPeriodo = p.idCatPeriodo AND p.estatus <> 6)")
	List<ControlDocumento> findByActivoWithOutDates( @Param("nombreArchivo") String nombreArchivo,
			@Param("idDocumento") String idDocumento, @Param("idPeriodo") String idPeriodo);

	public int countByDocumentoAndPeriodoAndActivo(Documento documento, Periodo periodo, boolean activo);

}
