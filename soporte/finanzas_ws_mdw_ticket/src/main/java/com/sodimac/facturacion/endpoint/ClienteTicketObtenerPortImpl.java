package com.sodimac.facturacion.endpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018.BodyTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018.ClienteTicketObtenerExpReqTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.ticket.obtener_v1.ClienteTicketObtenerPt;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.ticket.obtener_v1.FaultMsg;
import com.sodimac.facturacion.service.FacturasService;
import com.sodimac.facturacion.service.FacturasServiceImpl;
import com.sodimac.facturacion.util.Constantes;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.UtilsNumber;

@WebService (serviceName="ClienteTicketObtenerService", portName = "ClienteTicketObtenerPort", targetNamespace = "http://mdwcorp.falabella.com/SOD/CORP/OSB/wsdl/Cliente/Ticket/Obtener-v1.0", endpointInterface ="com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.ticket.obtener_v1.ClienteTicketObtenerPt")
@HandlerChain(file="/handler-chain.xml")
public class ClienteTicketObtenerPortImpl implements ClienteTicketObtenerPt {

	private Logger logger = LoggerFactory.getLogger(ClienteTicketObtenerPortImpl.class);
	
	@WebMethod(operationName = "ClienteTicketObtenerOp", action = "http://mdwcorp.falabella.com/SOD/CORP/OSB/wsdl/Cliente/Ticket/Obtener-v1.0/Op")
	public ClienteTicketObtenerExpRespTYPE clienteTicketObtenerOp(
			ClienteTicketObtenerExpReqTYPE clienteTicketObtenerReqParam) throws FaultMsg {

		ClienteTicketObtenerExpRespTYPE response = new ClienteTicketObtenerExpRespTYPE();
		String ticket = "";
		UtilsApi.setRespuesta(response, 99, Constantes.ERROR_99);

		BodyTYPE body = clienteTicketObtenerReqParam.getBody();	
    	if (body == null) return response;

    	try {
				
			String fecha = (body.getFechaTrx()==null)? "": body.getFechaTrx().trim();
			String tienda = (body.getINumTienda()==null)? "": body.getINumTienda().trim();
			String caja = (body.getINumCaja()==null)? "": body.getINumCaja().trim();
			String transaccion = (body.getINumTicket()==null)? "0": body.getINumTicket().toString();
			String numeroOrden = (body.getINumOc()==null)? "0": body.getINumOc().trim();
			String fechaDocto ="";
			
			if (numeroOrden.isEmpty()) numeroOrden ="0";
			
			if ( Long.valueOf(numeroOrden).longValue() > 0) {
				ticket = numeroOrden;
			}
			else {
				if (!validarDatosObtenerTicket(fecha, tienda, caja, transaccion, numeroOrden)) {
					return response;
		    	}
				
				fechaDocto = fecha.substring(6, 10) + fecha.substring(3, 5) + fecha.substring(0, 2);
				tienda = String.valueOf(Integer.parseInt(tienda));
				caja = String.valueOf(Integer.parseInt(caja));
				transaccion = String.valueOf(Integer.parseInt(transaccion));				
				
				tienda = StringUtils.leftPad(tienda, 4, '0');
				caja = StringUtils.leftPad(caja, 3, '0');
				transaccion = StringUtils.leftPad(transaccion, 4, '0');
				ticket = fechaDocto + tienda + caja + transaccion;
			}
						
			logger.info("Ticket: " + ticket);
			
			FacturasService facturasService = new FacturasServiceImpl();
			response = facturasService.obtenerTicket(numeroOrden, transaccion, tienda, caja, fecha);
									
		}
		catch (Exception ex) {
			logger.error("Ticket " + ticket + ": ", ex);
		}
    	
		return response;
	}
	
    boolean validarDatosObtenerTicket (String fecha, String tienda, String caja, String transaccion, String numeroOrden) {

		if (fecha.isEmpty() && tienda.isEmpty() && caja.isEmpty() && transaccion.equals("0") && numeroOrden.isEmpty()) {
			return false;
		}
		
		if (!numeroOrden.isEmpty() && !UtilsNumber.isNumeric(numeroOrden)) {
			return false;
		}
		
		long ordenCompra = 0;
		if (!numeroOrden.isEmpty()) ordenCompra = Long.parseLong(numeroOrden);
		
		if (ordenCompra == 0 && (fecha.isEmpty() || tienda.isEmpty() || caja.isEmpty() || transaccion.isEmpty())) {
			return false;	
		}

		if (!fecha.isEmpty() && !validarFechaExpresionRegular(fecha)) {
			return false;
		}
		if (!tienda.isEmpty() && (!UtilsNumber.isNumeric(tienda))) {
			return false;
		}
		int itienda = Integer.parseInt(tienda);
		if (itienda > 9999) {
			return false;
		}
		if (!caja.isEmpty() && (!UtilsNumber.isNumeric(caja))) {
			return false;
		}
		int icaja = Integer.parseInt(caja);
		if (icaja > 999) {
			return false;
		}
		if (!transaccion.isEmpty() && (!UtilsNumber.isNumeric(transaccion))) {
			return false;
		}
		int itransaccion = Integer.parseInt(transaccion);
		if (itransaccion > 9999) {
			return false;
		}
		
		return true;
	}    

	private boolean validarFechaExpresionRegular(String fecha) {
		Pattern pat = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
        Matcher mat = pat.matcher(fecha);
        return mat.matches();
		
	}
	
}
