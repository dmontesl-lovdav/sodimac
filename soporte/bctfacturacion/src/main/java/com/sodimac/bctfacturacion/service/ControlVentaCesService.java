package com.sodimac.bctfacturacion.service;

public interface ControlVentaCesService {

	  Integer getIdControlCes();
	  
	  Integer getIdControlCesPorFecha(String paramString);
	  
	  Integer getEstatusControlCesPorFecha(Integer pIdControlVentaCes);
	  
	  boolean existeControlCes(String pFecha);
	  
	  void registraControlCes(Integer pIdControlVentaCes, String pFecha, Integer pTotalVentasCes);
	  
	  void actualizaControlCes(Integer pIdControlVentaMSI, Integer pEstatus, Integer pTotalRegistrados);
	  
	  String[] getTipoTransCesPermitidos();
	}

