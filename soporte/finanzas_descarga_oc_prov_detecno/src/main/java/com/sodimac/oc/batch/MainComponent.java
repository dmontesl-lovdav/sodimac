package com.sodimac.oc.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sodimac.oc.batch.client.DetecnoClient;
import com.sodimac.oc.batch.dto.detecno.DetecnoResponse;
import com.sodimac.oc.batch.service.OrdenCompraService;

@Component
public class MainComponent {
	
	@Autowired
	DetecnoClient detecnoClient;
	
	@Autowired
	OrdenCompraService ordenCompraService;
	
	private Logger logger = LoggerFactory.getLogger(MainComponent.class);
	
	public void mainMethod() {
		logger.info("Inicia obtencion de ordenes de compra desde Detecno");
		
		DetecnoResponse ordenesCompra = detecnoClient.getOrdenesCompra();
		
		logger.info("Se obtuvieron {} ordenes", ordenesCompra.getTotalCount());
		
		logger.info("Inicia guardado de ordenes de compra en SODIMAC_SAP_PROD");
		
		ordenCompraService.saveOrdenesBatch(ordenesCompra.getData());
		
		logger.info("Inicia ejecución de SP [uspRegistroOrdenCompraProveedor]");
		
		ordenCompraService.ejecutaSP();
		
		logger.info("Termina proceso correctamente");
	}
}
