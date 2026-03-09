package com.sodimac.cfdi.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.sodimac.cfdi.model.puntosces.CatalogoDto;
import com.sodimac.cfdi.model.puntosces.PolizaContableDto;
import com.sodimac.cfdi.model.puntosces.PolizasFilterDto;

public interface PolizaContableService {
	
	Map<String, List<CatalogoDto>> getCatalogos();
	List<PolizaContableDto> findParameters(PolizasFilterDto filterDto);
	String guardarPolizaContable(PolizaContableDto polizaContable, boolean isNewPoliza);
	void eliminarPolizaContable(String idConfigContable);
	public OutputStream getExcel(PolizasFilterDto filterDto);
}
