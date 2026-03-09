package com.sodimac.cfdi.repository.fiscal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.ProcesoDescargaEntity;


@Repository
public interface ProcesoDescargaRepository extends JpaRepository<ProcesoDescargaEntity, String> {

	@Query("SELECT u FROM ProcesoDescargaEntity u")
	public List<ProcesoDescargaEntity> findAllProcesos();
	
	@Query(value = "{call uspObtenerDescargaByParams (:fechaInicial, :fechaFinal, :start, :rowsPerPage, :idEjecucion, :estatus)}", nativeQuery = true)
	public List<Object[]> getDescargaByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage, String idEjecucion, String estatus);
	
	@Query(value = "{call uspObtenerProcesosDescargaSchedule()}", nativeQuery = true)
	public List<Object[]> getProcesosDescargaSchedule();
	
	@Modifying
	@Query(value = "{call uspUpdateEstatusProcesoDescarga()}", nativeQuery = true)
	public void updateProcesosDescargaSchedule();
}
