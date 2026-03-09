package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.ReporteComplementoModel;

public interface ReporteComplementoService {

	public List<ReporteComplementoModel> getReporteComplementosByParams(String dateDesdeParse, String dateHastaParse,
			String rfcEncriptado, int start, int rowsPerPage, String ticket, String uuid, String monto);

	public boolean getReporteComplementosExcelFechas(String dateDesdeParse, String dateHastaParse, String rfcEncriptado,
			String ticket, String uuid, String monto, String nombreArchivo);

}
