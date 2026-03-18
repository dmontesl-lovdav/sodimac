package com.sodimac.facturacion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.cliente.BodyComplementoCorreoTYPE;
import com.sodimac.facturacion.repository.fis.ComplementosRepository;

@Service
public class CorreoComplementoServiceImpl implements CorreoComplementoService {
	
	private Logger logger = LoggerFactory.getLogger(CorreoFacturacionServiceImpl.class);
	
	@Autowired
	private ComplementosRepository complementosRepository;
	
	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	private ConfiguracionFacturacionService configFacService;
	
	@Autowired
	private MailSenderService mailSenderService;
	
	@Override
	public boolean enviarCorreoComplemento(BodyComplementoCorreoTYPE model) {
		String fileName = complementosRepository.findByUuid(model.getUuid()).getNombreArchivo();
		facturasService.crearArchivoXml(fileName, model.getXml());
		
		this.facturasService.crearPdfComplementoFromXml(fileName, model.getXml());
		facturasService.crearZipXmlPdf(fileName);
		
		model.setIdFacturaPac(0);

		String path = configFacService.getConfig().get("Mail.PathFile");
		String asunto = configFacService.getConfig().get("Mail.Usuario.EnvioFactura.Subject");
		String destinatarioCC = model.getEmailCC();
		String mensajeCorreo = configFacService.getConfig().get("Mail.Usuario.EnvioFactura.BodyMessage");
		mensajeCorreo = mensajeCorreo.replace("{nombreCliente}", model.getRazonSocial());
		boolean esHtml = Boolean.parseBoolean(configFacService.getConfig().get("Mail.Usuario.EnvioFactura.IsHtml"));

		String file = path + fileName + ".zip";
		try {
			if (!model.getEmail().isEmpty()) {
				mailSenderService.enviarCorreo(model.getEmail(), asunto, destinatarioCC, mensajeCorreo, esHtml, file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error al enviar el complemento");
			return false;
		}
		facturasService.eliminarArchivo(fileName);
		return true;
	}
	
	@Override
	public boolean enviarCorreoFactura(BodyComplementoCorreoTYPE model) {
		String fileName = facturasService.getFacturaByUuid(model.getUuid()).getNombreArchivo();
		facturasService.crearArchivoXml(fileName, model.getXml());
		
		this.facturasService.crearPdfComplementoFromXml(fileName, model.getXml());
		facturasService.crearZipXmlPdf(fileName);
		
		model.setIdFacturaPac(0);

		String path = configFacService.getConfig().get("Mail.PathFile");
		String asunto = configFacService.getConfig().get("Mail.Usuario.EnvioFactura.Subject");
		String destinatarioCC = model.getEmailCC();
		String mensajeCorreo = configFacService.getConfig().get("Mail.Usuario.EnvioFactura.BodyMessage");
		mensajeCorreo = mensajeCorreo.replace("{nombreCliente}", model.getRazonSocial());
		boolean esHtml = Boolean.parseBoolean(configFacService.getConfig().get("Mail.Usuario.EnvioFactura.IsHtml"));

		String file = path + fileName + ".zip";
		try {
			if (!model.getEmail().isEmpty()) {
				mailSenderService.enviarCorreo(model.getEmail(), asunto, destinatarioCC, mensajeCorreo, esHtml, file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error al enviar el complemento");
			return false;
		}
		facturasService.eliminarArchivo(fileName);
		return true;
		
	}

}
