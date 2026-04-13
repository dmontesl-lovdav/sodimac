package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.dto.ReporteFinancieroDto;
import com.sodimac.rebates.filter.ReporteFinancieroFilter;

public interface IReporteFinancieroService {

	public List<ReporteFinancieroDto> getReporteFinanciero(ReporteFinancieroFilter reporteFinanciero);
	
}
