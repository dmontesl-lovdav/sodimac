package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.PolizaContableDto;
import com.sodimac.rebates.dto.PolizaContableReporteDto;
import com.sodimac.rebates.filter.PolizaContableFilter;

public interface IPolizaContableService {

	public List<PolizaContableDto> getPolizasContables(PolizaContableFilter filter);
	
	public List<PolizaContableReporteDto> getReportePolizasContables(PolizaContableFilter filter);
}
