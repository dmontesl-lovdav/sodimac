package com.sodimac.fiscal.api.service;

import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sodimac.fiscal.api.model.dto.XmlFIscalDto;


public interface ToolsService {

	public Document getDocument(MultipartFile file) throws IOException, SAXException, ParserConfigurationException;
	public XmlFIscalDto getVauleFromXmlFile(MultipartFile file) throws IOException, SAXException,
	ParserConfigurationException, Exception, JsonProcessingException, JsonMappingException;
	
	public UUID getRandomUUID();
}
