package com.sodimac.fiscal.api.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sodimac.fiscal.api.util.XmlSecureFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sodimac.fiscal.api.model.dto.LogDto;
import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.dto.ResponseValidationDetecnoDto;
import com.sodimac.fiscal.api.model.dto.XmlFIscalDto;
import com.sodimac.fiscal.api.response.ResponseHandler;
import com.sodimac.fiscal.api.service.LogService;
import com.sodimac.fiscal.api.service.PacCatalogService;
import com.sodimac.fiscal.api.service.PacService;
import com.sodimac.fiscal.api.service.ToolsService;
import com.sodimac.fiscal.api.service.ValidarXmlService;


@Service
public class ValidaXmlServiceImpl  implements ValidarXmlService {

    private static final String MSG_END_UNSUCCESS = "End request unsuccessfully";
    private static final String ERR_SUFFIX = ". ERROR: ";

	private static final Logger logger = LoggerFactory.getLogger(ValidaXmlServiceImpl.class);
	
    private final String DETECNO;
    
    private final String CARBAJAL;
    
    private int maxPrioridadPac;
    
    
    @Autowired
    @Qualifier("pacServiceDetecnoImpl")
    PacService pacServiceDetecnoImpl;
    
    @Autowired
    @Qualifier("pacServiceCabajalImpl")
    PacService pacServiceCabajalImpl;
    
    @Autowired
    ToolsService toolsService;
    
    @Autowired
    PacCatalogService pacCatalogService;
    
	@Autowired
	private LogService logService;
    


    public ValidaXmlServiceImpl(@Value("${pac.detecno.name}") String DETECNO 
    						, @Value("${pac.carbajal.name}") String CARBAJAL
    						,@Autowired PacCatalogService pacCatalogService) {
        this.DETECNO = DETECNO;
		this.CARBAJAL = CARBAJAL;
		this.pacCatalogService = pacCatalogService;
		setMaxPrioridadPac();
    }
    

    public ResponseEntity<Object> validaXmlFiscal(MultipartFile file, int pacPrioidad) {
	  	ResponseEntity<Object> response = null;
	  	PacCatalogDto pac = null;
	  	String requestData = null;
	  	boolean error = false;
	  	String errorMsg = "";
		String strXmlJson = null;
		XmlFIscalDto xmlFIscalDto = null;
    	try {
	    	pac = validaPacCorrecto(pacCatalogService.getPacList(), pacPrioidad);
			Document xml = getDocument(file);
			requestData = convertDomToXmlString(xml);
			strXmlJson = convertXmlStringToJson(requestData);
			xmlFIscalDto = getVauleFromXmlFile(strXmlJson);
		  	if(pac != null) {
	
			  	if(pac.getName().contains(DETECNO)) {
			  		response = pacServiceDetecnoImpl.validaXml(xml, requestData, pac, strXmlJson, xmlFIscalDto);
			  	}
			  	if(pac.getName().contains(CARBAJAL)) {
			  		response = pacServiceCabajalImpl.validaXml(xml, requestData, pac, strXmlJson, xmlFIscalDto);
			  	}
			  	
		  		if(response.getStatusCode().equals(HttpStatus.FAILED_DEPENDENCY)) {  //si Falla el pac primario se sigue con el secundario
		  			response = validaXmlFiscal(file, pacPrioidad + 1);
		  		}
		  	} else {
				return ResponseHandler.responseBuilderError("ERROR: PAC is null. There is no longer a PAC for the requested priority level. PacPriotiy = " + pacPrioidad , HttpStatus.BAD_REQUEST, xmlFIscalDto.getUUID(),null, null);
		  	}
		} catch (JsonMappingException e) {
			e.printStackTrace();
 			error = true;
 			logger.info(MSG_END_UNSUCCESS);
 			logger.error("ERROR: IOException " + ERR_SUFFIX + e.getMessage());
 			errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
 			error = true;
 			logger.info(MSG_END_UNSUCCESS);
 			logger.error("ERROR: IOException " + ERR_SUFFIX + e.getMessage());
 			errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
 			error = true;
 			logger.info(MSG_END_UNSUCCESS);
 			logger.error("ERROR: IOException " + ERR_SUFFIX + e.getMessage());
 			errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
		} catch (SAXException e) {
			e.printStackTrace();
 			error = true;
 			logger.info(MSG_END_UNSUCCESS);
 			logger.error("ERROR: IOException " + ERR_SUFFIX + e.getMessage());
 			errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
 			error = true;
 			logger.info(MSG_END_UNSUCCESS);
 			logger.error("ERROR: IOException " + ERR_SUFFIX + e.getMessage());
 			errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
 			error = true;
 			logger.info(MSG_END_UNSUCCESS);
 			logger.error("ERROR: IOException " + ERR_SUFFIX + e.getMessage());
 			errorMsg = "ERROR: IOException " + ERR_SUFFIX + e.getMessage();
		}
	
	
		if (error) {
			LogDto logdto = GuardarLogFiscal(pac,null,null, errorMsg, requestData, null);
			response = ResponseHandler.responseBuilderError(errorMsg, HttpStatus.BAD_REQUEST, null, null, logdto.getLogId().toString());
		}
		  	return response;
    }

	private Document getDocument(MultipartFile file) throws IOException, SAXException, ParserConfigurationException {
		Document content;
		try (InputStream inputStream = file.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			content = readBufferedReaderToDocument(reader);
		}
		return content;
	}
	
	private Document readBufferedReaderToDocument(BufferedReader reader)
			throws IOException, SAXException, ParserConfigurationException {
		StringWriter writer = new StringWriter();
		String line;
		while ((line = reader.readLine()) != null) {
			writer.write(line);
			// Optionally add a newline if the original content had them and you want to
			// preserve structure
		}

		DocumentBuilderFactory factory = XmlSecureFactory.newDocumentBuilderFactory();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(writer.toString()));
		return builder.parse(is);
	}
	
	private String convertDomToXmlString(Document document) throws TransformerException  {
		TransformerFactory transfac = XmlSecureFactory.newTransformerFactory();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes"); // For pretty-printing
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(document);
		trans.transform(source, result);
		return sw.toString();
	}
	
	private String convertXmlStringToJson(String xmlString) throws IOException, JsonProcessingException {
		XmlMapper xmlMapper = new XmlMapper();
		JsonNode node = xmlMapper.readTree(xmlString.getBytes());
		ObjectMapper jsonMapper = new ObjectMapper();
		return jsonMapper.writeValueAsString(node);
	}
	
	private XmlFIscalDto getVauleFromXmlFile(String strXmlJson) throws JsonMappingException, JsonProcessingException  {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(strXmlJson);
		String versionCFDI = rootNode.get("Version").toString();
		String UUID = rootNode.get("Complemento").get("TimbreFiscalDigital").get("UUID").toString();
		String Rfc = rootNode.get("Emisor").get("Rfc").toString();

		return new XmlFIscalDto(UUID, Rfc, versionCFDI);
	}

	private LogDto GuardarLogFiscal(PacCatalogDto pac, LocalDateTime recordStartDate
			,LocalDateTime recordEndDate, String errorMsg
			,String requestData, String responseData) {


	LogDto logdto = new LogDto(null
				,( pac != null ? Math.toIntExact(pac.getPacId()) : null), 1 //Integer.parseInt(xmlFIscalDto.getVersionCFDI())
						, null
						, "PREVALIDACION FACTURA", LocalDateTime.now(), recordStartDate, recordEndDate
						, "-1", errorMsg, requestData, responseData);
	
	logdto = logService.save(logdto);
	return logdto;
	}
    
  	private void setMaxPrioridadPac() {
  		List<PacCatalogDto> listPacs = pacCatalogService.getPacList();
  		maxPrioridadPac = listPacs.get(listPacs.size() - 1).getPriority();
  	}
  	
  	private PacCatalogDto validaPacCorrecto(List<PacCatalogDto> pacList, int prioridad) {
  		LocalDate today = LocalDate.now();
  		PacCatalogDto pac = null;
  		List<PacCatalogDto> pacListTmp = pacList.stream().filter(e -> e.getPriority() == prioridad
  													&& (e.getValidFrom() == null  ||today.isAfter(e.getValidFrom()) )
  													&& (e.getValidTo() == null || today.isBefore(e.getValidTo()) )
  													&& e.getStatus() == 1
  				
  				).collect(Collectors.toList());
  		
  		if(!pacListTmp.isEmpty()) {
  			pac = pacListTmp.get(0);   //Obtiene el Pac que cumple con las condicioens anteriores
  		} else { //En caso contrario intenta con el siguiente PAC
  			int prioridadAdded = prioridad + 1;
  			if(prioridadAdded <= maxPrioridadPac) {
	  			pac = validaPacCorrecto(pacList, prioridadAdded);
  			}

  		}
  	
  		return pac;
  	
  	}

}
