package com.sodimac.facturacion.service.ws;

import java.math.BigInteger;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018.BodyTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018.ClienteTicketObtenerExpReqTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.ticket.obtener_v1.ClienteTicketObtenerPt;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.ticket.obtener_v1.ClienteTicketObtenerService;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.ticket.obtener_v1.FaultMsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.clientews.confirmar.CredentialsHandler;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.service.ConfiguracionFacturacionService;

@Service
public class ObtenerTicketServiceImpl implements ObtenerTicketService {

	@Autowired
	private ConfiguracionFacturacionService configFacService;
	@Autowired
	private ErrorComponent errorComponent;
	
	private String url = "";
	private String country = "";
	private String comercio = "";
	private String canal = "";
	
	public ClienteTicketObtenerExpRespTYPE getTicket(String ticket) {

		url = configFacService.getConfig().get("WebService.ObtenerTicket.Url");
    	country = configFacService.getConfig().get("WebService.Sodimac.Pais");
		comercio = configFacService.getConfig().get("WebService.Sodimac.Comercio");
		canal = configFacService.getConfig().get("WebService.Sodimac.Canal");

		String NumOc = "";
		String fecha = "";
		String tienda = "";
		String caja = "";
		BigInteger noTicket = null;
		ClienteTicketObtenerExpRespTYPE serviceResponse = null;
		BodyTYPE body = new BodyTYPE();
		ClienteTicketObtenerService wsObtenerTicket = null;
		
		if (ticket.length()==10) {
			NumOc = ticket;
			body.setINumOc(NumOc);
		} else {
			fecha = ticket.substring(6, 8) + "-" + ticket.substring(4, 6) + "-" + ticket.substring(0, 4);
			tienda = ticket.substring(8, 12);
			caja = ticket.substring(12, 15);
			noTicket = new BigInteger(ticket.substring(15, 19));
			body.setFechaTrx(fecha);
			body.setINumCaja(caja);
			body.setINumTicket(noTicket);
			body.setINumTienda(tienda);
		}
		
		
		ClienteTicketObtenerExpReqTYPE clienteTicketObtenerReqParam = new ClienteTicketObtenerExpReqTYPE();
		clienteTicketObtenerReqParam.setBody(body);
		
		try {
			wsObtenerTicket = new ClienteTicketObtenerService();
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setPagina("getTicket");
			errorComponent.setTicket(ticket);
			errorComponent.guardarLog(e);
			return null;
		}
		
		ClienteTicketObtenerPt iWsObtenerTicket = wsObtenerTicket.getClienteTicketObtenerPort();
		
		BindingProvider bp = (BindingProvider)iWsObtenerTicket;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		Binding binding = bp.getBinding();
		List<Handler> handlerList = binding.getHandlerChain();
		handlerList.add(new CredentialsHandler(country, comercio, canal));
		binding.setHandlerChain(handlerList);

		try {
			serviceResponse = iWsObtenerTicket.clienteTicketObtenerOp(clienteTicketObtenerReqParam);
			return serviceResponse;
		} catch (FaultMsg e) {
			e.printStackTrace();
			errorComponent.setPagina("getTicket");
			errorComponent.setTicket(ticket);
			errorComponent.guardarLog(e);
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setPagina("getTicket");
			errorComponent.setTicket(ticket);
			errorComponent.guardarLog(e);
		}
		
		return serviceResponse;
	}
}

