package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.ProcesoDescargaModel;

public interface DescargaService {
	public List<ProcesoDescargaModel> obtenerDescargaByParams(String fechaInicial, String fechaFinal, int start, int rowsPerPage, String idEjecucion, String estatus);
	public String registrarProceso(String parametros, String modulo, String usuario);
	public void updateStatusProceso(String idEjecucion, int estatus, String mensaje, String archivos) throws Exception;
	public String obtenerNombreArchivosByIdEjecucion(String idEjecucion) throws Exception;
	public List<String> obtenerNombreArchivoForSchedule();
	public void updateStatusProcesoSchedule();
}
