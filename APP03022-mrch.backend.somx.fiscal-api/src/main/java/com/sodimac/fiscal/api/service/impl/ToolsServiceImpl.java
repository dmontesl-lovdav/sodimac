package com.sodimac.fiscal.api.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sodimac.fiscal.api.model.dto.XmlFIscalDto;
import com.sodimac.fiscal.api.service.ToolsService;



@Service
public class ToolsServiceImpl implements ToolsService {


	public XmlFIscalDto getVauleFromXmlFile(MultipartFile file) throws IOException, SAXException,
			ParserConfigurationException, JsonProcessingException, JsonMappingException, Exception {
		Document xml = getDocument(file);
		String str = convertXmlStringToJson(convertDomToXmlString(xml));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(str);
		String versionCFDI = rootNode.get("Version").toString();
		String uuid = rootNode.get("Complemento").get("TimbreFiscalDigital").get("UUID").toString();
		String rfc = rootNode.get("Emisor").get("Rfc").toString();

		return new XmlFIscalDto(uuid, rfc, versionCFDI);
	}
	
	public Document getDocument(MultipartFile file) throws IOException, SAXException, ParserConfigurationException {
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


	private String convertDomToXmlString(Document document) throws Exception {
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

	private String convertXmlStringToJson(String xmlString) throws Exception {
		XmlMapper xmlMapper = new XmlMapper();
		JsonNode node = xmlMapper.readTree(xmlString.getBytes());
		ObjectMapper jsonMapper = new ObjectMapper();
		return jsonMapper.writeValueAsString(node);
	}
	
	public UUID getRandomUUID() {
        return UUID.randomUUID();
	}

}
