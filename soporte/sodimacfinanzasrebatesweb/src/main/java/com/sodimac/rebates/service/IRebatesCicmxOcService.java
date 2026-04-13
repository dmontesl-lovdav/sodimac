package com.sodimac.rebates.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.RebatesCicmxOcDto;
import com.sodimac.rebates.filter.ExclusionCargaFilter;

public interface IRebatesCicmxOcService {

	public List<RebatesCicmxOcDto> obtenerProveedoresDisponibles(ExclusionCargaFilter filter);	
	
	public List<RebatesCicmxOcDto> obtenerOrdenesCompraDisponible(ExclusionCargaFilter filter) throws ParseException;
	
	public List<RebatesCicmxOcDto> obtenerFamiliaDisponible(ExclusionCargaFilter filter);
	
	public List<RebatesCicmxOcDto> obtenerFamiliaUnicaDisponible(ExclusionCargaFilter filter);
	
	public List<RebatesCicmxOcDto> obtenerSkuDisponible(ExclusionCargaFilter filter);

	public List<RebatesCicmxOcDto> obtenerSkuUnicoDisponible(ExclusionCargaFilter filter);

	public boolean existeExclusion(String exclusion, Integer idCatPeriodo, Integer idCatTipoExclusion, String numProveedor, Integer idCatTipoRebate);

	public String obtenerFechaRecepcion(ExclusionDto exclusion);

}
