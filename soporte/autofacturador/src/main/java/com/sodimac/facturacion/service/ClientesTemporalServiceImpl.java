package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sodimac.facturacion.entity.ClientesTemporalEntity;
import com.sodimac.facturacion.repository.ClientesTemporalRepository;

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

}
