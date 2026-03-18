package com.sodimac.facturacion.service.catalogospdf;

import com.sodimac.facturacion.entity.catalogospdf.PacsEntity;

public interface PacsService {
	
	public int getIdDefault();
	public PacsEntity getById(int id);
}
