package com.sodimac.rebates.service;

import java.util.List;

import com.sodimac.rebates.filter.RebateUsuarioFilter;
import com.sodimac.rebates.model.RebateUsuarioEntity;

public interface IRebateUsuarioService {

	public List<RebateUsuarioEntity> getRebateUsuario(RebateUsuarioFilter rebateUsuario);
	
}
