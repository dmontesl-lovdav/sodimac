package com.sodimac.bctfacturacion.service;

import java.text.ParseException;

import com.sodimac.bctfacturacion.model.VentaCabModel;

public interface IVentaCabService {

	public Integer obtenerIdVentaCab();
	
	public boolean existeTicket(String ticket);
	
	public VentaCabModel getTicket(String ticket);
	
	public void guardar(VentaCabModel dto) throws ParseException;
	
}
