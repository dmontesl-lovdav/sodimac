package com.sodimac.oc.batch.service;

import java.util.List;

import com.sodimac.oc.batch.dto.detecno.DetecnoData;

public interface OrdenCompraService {

	public void saveOrdenesBatch(List<DetecnoData> data);
	public void ejecutaSP();
	
}
