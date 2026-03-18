package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.fac.ClientesTemporalEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.repository.fac.ClientesTemporalRepository;

@Service
public class ClientesTemporalServiceImpl implements ClientesTemporalService {

	@Autowired
	private ClientesTemporalRepository clientesTemporalRepository;
	@Autowired
	private SeguridadService seguridadService;

	@Transactional
	@Override
	public ClientesTemporalEntity getCliente(String rfc) {
		return clientesTemporalRepository.findTop1ByRfcOrderByFechaCreacionDesc(seguridadService.encriptar(rfc));
	}
	
	@Transactional
	public int insertarTemporal(ClientesTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String razonSocial = seguridadService.encriptar(model.getRazonSocial());
		String email = "";
		if (!model.getEmail().isEmpty()) {
			email = seguridadService.encriptar(model.getEmail()); 
		}
		String codigoPostal = "";
		if (!model.getCodigoPostal().isEmpty()) {
			codigoPostal = seguridadService.encriptar(model.getCodigoPostal()); 
		}
		
		return clientesTemporalRepository.insertarTemporal(rfc, model.getTicket(), razonSocial, model.getIdUsoCfdiReal(), email, model.getAutorizoGuardado(), model.getNombreObra(), model.getResponsableObra(), model.getRegimenFiscal(), codigoPostal);
	}
		
}
