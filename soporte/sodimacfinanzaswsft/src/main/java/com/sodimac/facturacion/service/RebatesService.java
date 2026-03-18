package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.models.DescuentosRebatesModel;

public interface RebatesService {

	public List<DescuentosRebatesModel> getDescuentos();
	public int actualizaTimbrado (String numeroDocumento, String numeroReferencia, String ticket, String uuid, String fechaTimbrado);
}
