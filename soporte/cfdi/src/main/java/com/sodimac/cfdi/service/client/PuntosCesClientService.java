package com.sodimac.cfdi.service.client;

import java.util.List;

import com.sodimac.cfdi.model.puntosces.CatalogoDto;
import com.sodimac.cfdi.model.puntosces.PolizaContableDto;
import com.sodimac.cfdi.model.puntosces.PolizasFilterDto;

public interface PuntosCesClientService {

	List<CatalogoDto> getCatalogo(Integer idCatalogo);
	List<CatalogoDto> getSucursales();
	List<PolizaContableDto> getPolizasContablesFilter(PolizasFilterDto polizaContable);
	PolizaContableDto getPolizaContable(Integer idConfigContable);
	void savePolizaContable(PolizaContableDto polizaContable);
	void updatePolizaContable(PolizaContableDto polizaContable);
	void deletePolizaContable(Integer idConfigContable);
	
}
