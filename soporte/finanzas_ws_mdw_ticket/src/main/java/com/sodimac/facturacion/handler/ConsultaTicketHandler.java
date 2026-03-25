package com.sodimac.facturacion.handler;

import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ConsultaTicketHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String REQUEST_XML="REQUEST_XML";

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		
		Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

	    if(isRequest){

		    try{
		        SOAPMessage soapMsg = context.getMessage();
		        SOAPEnvelope soapEnv = soapMsg.getSOAPPart().getEnvelope();
		        SOAPBody body = soapMsg.getSOAPBody();
		        
		        String prefix = soapEnv.getPrefix();
		        soapEnv.removeNamespaceDeclaration(prefix);
		        soapEnv.setPrefix("soapenv");
		        body.setPrefix("S");
		        
		        String s = this.writeMessageLogging(context);
		        s = s
		        	.replace(" xmlns:ns2=\"http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Req-v2018.02\"", "")
		        	.replace("xmlns:ns3=", "xmlns:ns2=")
		        	.replace("ns3:", "ns2:");
		        
		        //MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		        SOAPMessage message = MessageFactory.newInstance().createMessage();
		        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		        try (InputStream inputStream = new ByteArrayInputStream(s.getBytes())) {
		            Document document = documentBuilder.parse(inputStream);
		            //message = messageFactory.createMessage();
		            message.getSOAPPart().setContent(new DOMSource(document));
		            //SOAPBody soapBody = message.getSOAPBody();
		            //System.out.println(soapBody);
		        } catch (SAXException | IOException e) {
					e.printStackTrace();
				}
		        
		        context.setMessage(message);
		        
	            this.writeMessageLogging(context);
		    }catch(SOAPException | ParserConfigurationException  e){
		    	System.err.println(e);
		    }
		}
	    return true;
	}
	

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {
		
	}

	@Override
	public Set<QName> getHeaders() {
		return null;
	}
	
	private String writeMessageLogging(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean) smc
                .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        SOAPMessage message = smc.getMessage();
        ByteArrayOutputStream out=null;
        String strMsg = null;
        try {
             out = new ByteArrayOutputStream();
             message.writeTo(out);
             strMsg = new String(out.toByteArray());

            if (!outboundProperty.booleanValue()) {
                String requestXML=(String)smc.get(REQUEST_XML);
                System.out.println("Request of Response:"+requestXML);
            }else{
                smc.put(REQUEST_XML,strMsg);
            }

        } catch (Exception e) {
        	System.out.println("Exception in handler: " + e.getMessage());
        }finally{
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        return strMsg;
    }
}
