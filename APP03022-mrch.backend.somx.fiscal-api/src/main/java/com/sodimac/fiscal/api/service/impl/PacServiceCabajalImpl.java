package com.sodimac.fiscal.api.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.w3c.dom.Document;

import com.sodimac.fiscal.api.model.dto.PacCatalogDto;
import com.sodimac.fiscal.api.model.dto.XmlFIscalDto;
import com.sodimac.fiscal.api.service.PacService;

@Service
@Qualifier("pacServiceCabajalImpl")
public class PacServiceCabajalImpl extends WebServiceGatewaySupport implements PacService {

	@Override
	public ResponseEntity<Object> validaXml(Document xml, String requestData, PacCatalogDto pac, String strXmlJson, XmlFIscalDto xmlFIscalDto) {
		// TODO: Implementar validación real con PAC Carbajal
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
				.body("PAC Carbajal no implementado aún");
	}
}
