package com.sodimac.facturacion.service.ws;

import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.BodyTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.ClienteFacturaConfirmarExpReqTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.ComprobanteTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.ComprobantesTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.factura.confirmar_v1.ClienteFacturaConfirmarPt;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.factura.confirmar_v1.ClienteFacturaConfirmarService;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.factura.confirmar_v1.FaultMsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.clientews.confirmar.CredentialsHandler;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.service.ConfiguracionFacturacionService;

@Service
public class ConfirmarServiceImpl implements ConfirmarService {

	@Autowired
	private ConfiguracionFacturacionService configFacService;
	@Autowired
	private ErrorComponent errorComponent;
	
	private String url = "";
	private String country = "";
	private String comercio = "";
	private String canal = "";
	
	public ClienteFacturaConfirmarExpRespTYPE confirmar(String numeroTicket, String comprobanteFiscal, String codigo) {
		
		url = configFacService.getConfig().get("WebService.Confirmar.Url");
    	country = configFacService.getConfig().get("WebService.Sodimac.Pais");
		comercio = configFacService.getConfig().get("WebService.Sodimac.Comercio");
		canal = configFacService.getConfig().get("WebService.Sodimac.Canal");

		BodyTYPE body = new BodyTYPE();
		
		ComprobanteTYPE comprobante = new ComprobanteTYPE();
		comprobante.setNumeroTicket(numeroTicket);
		
		ComprobantesTYPE comprobantes = new ComprobantesTYPE();
		comprobantes.getComprobante().add(comprobante);

		body.setComprobanteFiscalDigital(comprobanteFiscal);
		body.setComprobantes(comprobantes);
		body.setCodigoError(codigo);
		body.setDetalleError("");
		
		ClienteFacturaConfirmarExpReqTYPE clienteFacturaConfirmarReqParam = new ClienteFacturaConfirmarExpReqTYPE();
		clienteFacturaConfirmarReqParam.setBody(body);
		
		ClienteFacturaConfirmarService wsConfirmar = new ClienteFacturaConfirmarService();
		ClienteFacturaConfirmarPt iWsConfirmar = wsConfirmar.getClienteFacturaConfirmarPort();
		
		BindingProvider bp = (BindingProvider)iWsConfirmar;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		Binding binding = bp.getBinding();
		List<Handler> handlerList = binding.getHandlerChain();
		handlerList.add(new CredentialsHandler(country, comercio, canal));
		binding.setHandlerChain(handlerList);

		try {
			ClienteFacturaConfirmarExpRespTYPE serviceResponse = iWsConfirmar.clienteFacturaConfirmarOp(clienteFacturaConfirmarReqParam);
			return serviceResponse;
		} catch (FaultMsg e) {
			e.printStackTrace();
			errorComponent.setTicket(numeroTicket);
			errorComponent.setPagina("confirmar FaultMsg");
			errorComponent.guardarLog(e);
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setTicket(numeroTicket);
			errorComponent.setPagina("confirmar Exception");
			errorComponent.guardarLog(e);
		}
		
		return null;
	}	
}
