package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.TableroControlTimbradoModel;

public interface ReportesService {
	public String getTiendas();
	public String getCanales();
	public List<TableroControlTimbradoModel> getTableroByParams(String dateDesdeParse, String dateHastaParse, int start, int rowsPerPage, String ticket, String canal, String tienda);
	public boolean getTableroByParamsExcel(String dateDesdeParse, String dateHastaParse, String nombreArchivo, String ticket, String canal, String tienda);
	public boolean getDetalleByParamsExcel(String dateDesdeParse, String dateHastaParse, String nombreArchivo, String ticket, String canal, String tienda);
	public boolean ejecutarProcesamientoEnSegundoPlano(String idEjecucion, String dateDesdeParse, String dateHastaParse, String ticket, String canal, String tienda);
	
}
