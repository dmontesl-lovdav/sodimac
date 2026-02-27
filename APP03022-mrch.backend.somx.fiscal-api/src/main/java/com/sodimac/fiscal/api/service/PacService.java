package com.sodimac.fiscal.api.service;

import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;

import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.dto.XmlFIscalDto;


public interface PacService {

	ResponseEntity<Object> validaXml(Document xml, String requestData, PacCatalogDto pac,  String strXmlJson, XmlFIscalDto xmlFIscalDto) ; //throws IOException, SAXException, ParserConfigurationException, Exception;
}
