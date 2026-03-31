package com.sodimac.bctfacturacion.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.bctfacturacion.entity.bct.EstFcturasEntity;
import com.sodimac.bctfacturacion.model.FacturaModel;
import com.sodimac.bctfacturacion.repository.bct.EstFacturasRepository;
import com.sodimac.bctfacturacion.service.EstFacturasService;

@Service
public class EstFacturasServiceImpl implements EstFacturasService {
	
	private static int COD_ESTADO = 2;
	private static int EST_FACT = 1;
	private static String DET_ESTADO = "Facturado";

	@Autowired
	private EstFacturasRepository estFacturasRepository;
	
	@Override
	public Integer existeTicket(String pUuid) {
		return this.estFacturasRepository.existeTicket(pUuid);
	}

	@Override
	public List<String> getTickets(List<String> pUuid) {
		return this.estFacturasRepository.getTickets(pUuid);
	}
	
	@Override
	@Transactional("transactionManagerBct")
	public void insertar(FacturaModel pFactura) {
		
		String ticket = pFactura.getTicketBct();
		//20180728 1010 011 0213
		//01234567 8901 234 5678
		String numTicket = "0000";
		Integer numTienda = null;
		if (ticket.length() > 18) {
			numTicket = ticket.substring(15,19);
		}
		
		EstFcturasEntity facturaEntity = new EstFcturasEntity();
		facturaEntity.setFecha( pFactura.getFechaTimbrado() );
		facturaEntity.setNumFactura( pFactura.getUuid() );
		facturaEntity.setNumTicket( numTicket );
		facturaEntity.setCodEstado( COD_ESTADO );
		facturaEntity.setDetEstado( DET_ESTADO );
		facturaEntity.setNumTrx(ticket);
		facturaEntity.setNroSerie( pFactura.getSerie() );
		if (pFactura.getFolio() != null) {
			facturaEntity.setNroFolio( Integer.valueOf(pFactura.getFolio() ));
		}
		facturaEntity.setNumTienda( numTienda);
		facturaEntity.setFechaCierre(null);
		facturaEntity.setEstFact( EST_FACT );
		
		this.estFacturasRepository.save(facturaEntity);
	}

}
