package com.sodimac.bctfacturacion.service;

import java.text.ParseException;
import java.util.List;

import com.sodimac.bctfacturacion.model.AdminPuntosCesModel;

public interface IAdminPuntosCesService {

	public boolean existeTicket(String ticket, String tipoTransaccionBct);
	
	public AdminPuntosCesModel getTicket(String ticket, String tipoTransaccionBct);
	
	public List<AdminPuntosCesModel> getTickets(String tipoTransaccionBct);
	
	public void guardar(AdminPuntosCesModel dto) throws ParseException;
	
}
