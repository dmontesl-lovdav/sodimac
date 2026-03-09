package com.sodimac.cfdi.service.catalogospdf;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.PacsEntity;

public interface PacsService {
	
	public int getIdDefault();
	public PacsEntity getById(int id);
}
