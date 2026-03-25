
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package com.sodimac.facturacion.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.HandlerChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.BodyTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.ComprobanteTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.factura.confirmar_v1_0.ClienteFacturaConfirmarPt;
import com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.factura.confirmar_v1_0.FaultMsg;
import com.sodimac.facturacion.service.FacturasService;
import com.sodimac.facturacion.service.FacturasServiceImpl;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.Constantes;

@javax.jws.WebService(
                      serviceName = "ClienteFacturaConfirmarService",
                      portName = "ClienteFacturaConfirmarPort",
                      targetNamespace = "http://mdwcorp.falabella.com/SOD/CORP/OSB/wsdl/Cliente/Factura/Confirmar-v1.0",
                      endpointInterface = "com.falabella.mdwcorp.sod.corp.osb.wsdl.cliente.factura.confirmar_v1_0.ClienteFacturaConfirmarPt")
@HandlerChain(file="/handler-chain-confirmar.xml")
public class ClienteFacturaConfirmarPortImpl implements ClienteFacturaConfirmarPt {

	private Logger logger = LoggerFactory.getLogger(ClienteFacturaConfirmarPortImpl.class);

    public ClienteFacturaConfirmarExpRespTYPE clienteFacturaConfirmarOp(com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018.ClienteFacturaConfirmarExpReqTYPE clienteFacturaConfirmarReqParam) throws FaultMsg   {
        
    	ClienteFacturaConfirmarExpRespTYPE response = new ClienteFacturaConfirmarExpRespTYPE();
    	List<ComprobanteTYPE> comprobantes = new ArrayList<ComprobanteTYPE>();
    	String ticket = "";
    	UtilsApi.setRespuesta(response, 99, Constantes.ERROR_99);
    	
    	BodyTYPE body = clienteFacturaConfirmarReqParam.getBody();
    	if (body == null) return response;
    	
        try {
        	String codigo = (body.getCodigoError() == null)? "": body.getCodigoError().trim();
        	String uuid = (body.getComprobanteFiscalDigital() == null)? "": body.getComprobanteFiscalDigital().trim();
        	
        	if (body.getComprobantes() != null) {
        		comprobantes = body.getComprobantes().getComprobante();         		
        	}
        	 
			if (!validarDatos(codigo, uuid, comprobantes)) {
				return response;
	    	}
			
			//if (!codigo.equals("2") && uuid != null) uuid = null;
			if (uuid.isEmpty()) uuid = null;
			ticket = comprobantes.get(0).getNumeroTicket();
			
			logger.info(String.format("Ticket: %s Status: %s uuid: %s", ticket, codigo, uuid) );
			
			String ticketXml = "<comprobantes><comprobante><numeroTicket>" + ticket + "</numeroTicket></comprobante></comprobantes>";
        	
			FacturasService facturasService = new FacturasServiceImpl();
			
			response = facturasService.confirmaFactura(ticketXml, codigo, uuid);
            
        } catch (Exception ex) {
			logger.error("Ticket " + ticket + ": ", ex);
        }

        return response;
    }
    
    boolean validarDatos (String codigo, String uuid, List<ComprobanteTYPE> comprobantes) {

    	if (comprobantes.size() == 0 || comprobantes.get(0).getNumeroTicket() == null || comprobantes.get(0).getNumeroTicket().isEmpty()) {
    		return false;
    	}
    	if (codigo.isEmpty()) {
    		return false;
    	}
//    	if (!codigo.equals("1") && !codigo.equals("2")  && !codigo.equals("3")) {
//    		return false;
//    	}
//    	if (codigo.equals("2") && uuid.isEmpty()) {
//    		return false;
//    	}
		
		if (!uuid.isEmpty() && !validarUuidExpresionRegular(uuid)) {
			return false;
		}
		
		return true;
	}
    
	private boolean validarUuidExpresionRegular(String uuid) {
		Pattern pat = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
        Matcher mat = pat.matcher(uuid);
        return mat.matches();
	}
	
}
