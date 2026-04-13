package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.filter.UsuarioFillRateFilter;
import com.sodimac.rebates.model.RebateUsuarioFillRateEntity;

public interface IRebateUsuarioFillRateService {

	public List<RebateUsuarioFillRateEntity> getUsuarioFillRate(UsuarioFillRateFilter usuarioFillRate, Integer idUser);
	public List<RebateUsuarioFillRateEntity> getUsuarioFillRateReport();
	
}
