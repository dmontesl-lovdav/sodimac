package com.sodimac.cfdi.service.admin;

import java.util.List;

import com.sodimac.cfdi.models.factura.IncidentesTimbradoModel;

public interface IncidentesTimbradoService {

	public List<IncidentesTimbradoModel> findTicket(String ticket, Integer idrol);
	public int deleteTicket(String ticket);
	public int deleteIntentos(String ticket);
}
