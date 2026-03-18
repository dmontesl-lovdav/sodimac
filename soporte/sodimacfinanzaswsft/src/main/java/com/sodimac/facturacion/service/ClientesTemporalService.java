package com.sodimac.facturacion.service;

import com.sodimac.facturacion.entity.fac.ClientesTemporalEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;

public interface ClientesTemporalService {

	public ClientesTemporalEntity getCliente(String rfc);
	public int insertarTemporal(ClientesTemporalModel model);
}
