package com.sodimac.bctfacturacion.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.bctfacturacion.model.AdminPuntosCesModel;
import com.sodimac.bctfacturacion.model.ListaPuntosSkuModel;
import com.sodimac.bctfacturacion.model.VentaCabModel;
import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;

public interface ITrxPointsService {

	public List<AdminPuntosCesModel> obtenerPuntosCes(String fecha);

	public List<AdminPuntosCesModel> obtenerPuntosCesPorTickets(List<String> tickets);

	public ListaPuntosSkuModel obtenerSkuPuntos(Long idPuntosCes);

	public List<VentaCabModel> obtenerTicket(String ticket) throws ParseException;

	public List<VentaDetImpuestoModel> obtenerImpuestos(String ticket) throws ParseException;
	
}
