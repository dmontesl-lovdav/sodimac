package com.sodimac.facturacion.config;

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sodimac.facturacion.endpoint.ClienteFacturaConfirmarPortImpl;
import com.sodimac.facturacion.endpoint.ClienteTicketObtenerPortImpl;

@Configuration
public class WebServiceConfig {

	@Autowired
	private Bus bus;
	
	@Bean
	public Endpoint endpointObtener() {
		EndpointImpl endpointImpl = new EndpointImpl(bus, new ClienteTicketObtenerPortImpl());
		endpointImpl.publish("/Ticket/Obtener/v1.0");
		return endpointImpl;
	}
	
	@Bean
	public Endpoint endpointConfirmar() {
		EndpointImpl endpointImpl = new EndpointImpl(bus, new ClienteFacturaConfirmarPortImpl());
		endpointImpl.publish("/Factura/Confirmar/v1.0");
		return endpointImpl;
	}
}
