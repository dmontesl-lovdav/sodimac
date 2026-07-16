package com.sodimac.fiscal.api.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.UUID;


import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sodimac.fiscal.api.detecno.wsdl.ObjectFactory;
import com.sodimac.fiscal.api.detecno.wsdl.ValidarXML;
import com.sodimac.fiscal.api.detecno.wsdl.ValidarXMLResponse;
import com.sodimac.fiscal.api.model.dto.LogDto;
import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.dto.ResponseValidationDetecnoDto;
import com.sodimac.fiscal.api.model.dto.XmlFIscalDto;
import com.sodimac.fiscal.api.response.ResponseHandler;
import com.sodimac.fiscal.api.service.LogService;
import com.sodimac.fiscal.api.service.PacService;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;


@Service
@Qualifier("pacServiceDetecnoImpl")
public class PacServiceDetecnoImpl extends WebServiceGatewaySupport implements PacService {

    private static final String ERR_SUFFIX = ". ERROR: ";
	
	
	
	@Autowired
	private LogService logService;
		
    public PacServiceDetecnoImpl (Jaxb2Marshaller marshallerDetecno) {
    	this.setMarshaller(marshallerDetecno);
    	this.setUnmarshaller(marshallerDetecno);
    }
    
	public ResponseEntity<Object> validaXml(Document xml, String requestData, PacCatalogDto pac, String strXmlJson, XmlFIscalDto xmlFIscalDto) 
			{

		ValidarXML request = new ValidarXML();
		ValidarXML.XmlPassed xmlPassed = new ValidarXML.XmlPassed();
		ResponseEntity<Object> response = null;
		boolean error = false;
		String errorMsg = "";

		try {
				xmlPassed.setAny(xml.getDocumentElement());
				
				ObjectFactory objectFactory = new ObjectFactory();
				request.setXmlPassed(objectFactory.createValidarXMLXmlPassed(xmlPassed));
				request.setPassword(objectFactory.createValidarXMLPassword(pac.getPassword()));
				request.setUsuario(objectFactory.createValidarXMLUsuario(pac.getUserName()));
				request.setLicencia(objectFactory.createValidarXMLLicencia(pac.getLicense()));
		
				response = validarXMLDetecno(request,xml, pac.getPriority(), pac,requestData, xmlFIscalDto);
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
					error = true;
					errorMsg = "ERROR: TransformerConfigurationException " + ERR_SUFFIX + e.getMessage();
				} catch (TransformerException e) {
					error = true;
					errorMsg = "ERROR: TransformerException " + ERR_SUFFIX + e.getMessage();
					e.printStackTrace();
				} catch (PropertyException e) {
					error = true;
					errorMsg = "ERROR: PropertyException " + ERR_SUFFIX + e.getMessage();
					e.printStackTrace();
				} catch (JAXBException e) {
					error = true;
					errorMsg = "ERROR: JAXBException " + ERR_SUFFIX + e.getMessage();
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					error = true;
					errorMsg = "ERROR: JsonProcessingException " + ERR_SUFFIX + e.getMessage();
					e.printStackTrace();
				} catch (IOException e) {
					error = true;
					errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
					e.printStackTrace();
				} catch (Exception e) {
					error = true;
					errorMsg = "ERROR: Exception " + ERR_SUFFIX + e.getMessage();
					e.printStackTrace();
				}

		if (error) {
			LogDto logdto = guardarLogFiscal(xmlFIscalDto,pac,null,null, null, requestData, null);
			response = ResponseHandler.responseBuilderError(errorMsg, HttpStatus.BAD_REQUEST, request.getXmlPassed().getValue(), null, logdto.getLogId().toString());
		}
		return response;
	}

	private ResponseEntity<Object> validarXMLDetecno(ValidarXML request,Document xml, int pacPrioidad,
			PacCatalogDto pac, String requestData, XmlFIscalDto xmlFIscalDto) throws TransformerException, IOException, JsonMappingException, JsonProcessingException, PropertyException, JAXBException   {
		ResponseEntity<Object> response = null;
		ValidarXMLResponse validarXMLResponse = null;
		//Guarda Log
		String responseData = null;
		LocalDateTime recordStartDate = null;
		LocalDateTime recordEndDate = null;
		ResponseValidationDetecnoDto responseDTO = null;

			
				recordStartDate = LocalDateTime.now();
				validarXMLResponse = (ValidarXMLResponse) getWebServiceTemplate().marshalSendAndReceive(pac.getUrl(), request, null);
				recordEndDate = LocalDateTime.now();
				responseData = convertJaxbXmlObjToString(validarXMLResponse);

	
				responseDTO = buildResponse(validarXMLResponse);
				response = ResponseHandler.responseBuilder("OK", HttpStatus.OK, responseDTO, 0, null);
		guardarLogFiscal(xmlFIscalDto,pac,recordStartDate,recordEndDate, responseDTO, requestData, responseData);
		return response;
	}

	private String convertJaxbXmlObjToString(ValidarXMLResponse validarXMLResponse)  throws JAXBException, PropertyException {
		JAXBContext jaxbContext;
		StringWriter sw = new StringWriter();

		jaxbContext = JAXBContext.newInstance(ValidarXMLResponse.class);
		Marshaller marshallert = jaxbContext.createMarshaller();

		marshallert.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // For pretty-printed XM
		marshallert.marshal(validarXMLResponse, sw);

		return sw.toString();
	}

	private ResponseValidationDetecnoDto buildResponse(ValidarXMLResponse validarXMLResponse) {

		String creditoActual = validarXMLResponse.getValidarXMLResult().getValue().getCreditoActual().getValue();
		String creditoDisponible = validarXMLResponse.getValidarXMLResult().getValue().getCreditoDisponible()
				.getValue();
		String creditoFinal = validarXMLResponse.getValidarXMLResult().getValue().getCreditoFinal().getValue();
		String creditoInicio = validarXMLResponse.getValidarXMLResult().getValue().getCreditoInicio().getValue();

		String errorCode = validarXMLResponse.getValidarXMLResult().getValue().getErrorCode().getValue();
		String errorDesc = validarXMLResponse.getValidarXMLResult().getValue().getErrorDesc().getValue();
		String errorMessage = validarXMLResponse.getValidarXMLResult().getValue().getErrorMessage().getValue();
		String errorModule = validarXMLResponse.getValidarXMLResult().getValue().getErrorModule().getValue();
		String errorType = validarXMLResponse.getValidarXMLResult().getValue().getErrorType().getValue();

		String status = validarXMLResponse.getValidarXMLResult().getValue().getStatus().getValue();
		String validate = validarXMLResponse.getValidarXMLResult().getValue().getValidate().getValue();

		return new ResponseValidationDetecnoDto(creditoActual, creditoDisponible, creditoFinal, creditoInicio,
				errorCode, errorDesc, errorMessage, errorModule, errorType, status, validate);
	}


	private LogDto guardarLogFiscal(XmlFIscalDto xmlFIscalDto, PacCatalogDto pac, LocalDateTime recordStartDate
									,LocalDateTime recordEndDate, ResponseValidationDetecnoDto responseDTO
									,String requestData, String responseData) {

		UUID uuid = UUID.fromString(xmlFIscalDto.getUUID().replace("\"", ""));
		LogDto logdto = new LogDto(null
									,Math.toIntExact( pac.getPacId()), 1 //Integer.parseInt(xmlFIscalDto.getVersionCFDI())
											, uuid
											, "Validacion Factura", LocalDateTime.now(), recordStartDate, recordEndDate
											, responseDTO.getErrorCode(), responseDTO.getErrorMessage(), requestData, responseData);
		
		logdto = logService.save(logdto);
		return logdto;
	}

}
