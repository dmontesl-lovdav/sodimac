package com.sodimac.bctfacturacion.service.impl;

import java.util.Date;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.bctfacturacion.entity.fiscal.FacturacionClienteEntity;
import com.sodimac.bctfacturacion.model.FacturaModel;
import com.sodimac.bctfacturacion.repository.fiscal.FacturacionClienteRepository;
import com.sodimac.bctfacturacion.service.FacturacionClienteService;

@Service
public class FacturacionClienteServiceImpl implements FacturacionClienteService {
	
	//private Logger logger = LoggerFactory.getLogger( FacturacionClienteServiceImpl.class );

	@Autowired
	private FacturacionClienteRepository clienteRepository;
	
	@Override
	@Transactional("transactionManagerFiscal")
	public void registraFacturacionCliente(FacturaModel pFactura) {
		
		String ticket = pFactura.getTicketBct();
		String numTicket = "0000";
		String numTienda = null;
		if (ticket.length() > 18) {
			numTicket = ticket.substring(15,19);
		}
		
		FacturacionClienteEntity entity = new FacturacionClienteEntity();
		entity.setTicket(pFactura.getTicket());
		entity.setFechaTimbrado( pFactura.getFechaTimbrado() );
		entity.setUuid( pFactura.getUuid() );
		entity.setTransaccion(numTicket);
		entity.setSerie(pFactura.getSerie());
		entity.setFolio(pFactura.getFolio());
		entity.setTienda(numTienda);
		entity.setFechaCarga( new Date()) ;
		entity.setSubtotal(null);
		entity.setTotal(null);
		entity.setFechaTicket(null);
		
		//logger.info( entity.toString() );
		
		this.clienteRepository.save(entity);

	}

	@Override
	public Integer existeTicket(String uuid) {
		return this.clienteRepository.existeTicket(uuid);
	}

}
