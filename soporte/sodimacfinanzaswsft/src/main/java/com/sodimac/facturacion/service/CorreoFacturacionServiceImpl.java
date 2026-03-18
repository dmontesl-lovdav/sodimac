package com.sodimac.facturacion.service;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.service.catalogospdf.PacsService;
import com.sodimac.facturacion.util.enums.EFacturaMailEstatus;
import com.sodimac.facturacion.util.enums.EFacturas;

@Service
public class CorreoFacturacionServiceImpl implements CorreoFacturacionService {
	
	private Logger logger = LoggerFactory.getLogger(CorreoFacturacionServiceImpl.class);
	
	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	private ConfiguracionFacturacionService configFacService;
	
	@Autowired
	private MailSenderService mailSenderService;
	
	@Autowired
	private PacsService pacsService;

	@Override
	@Async
	public void enviarCorreoAsincrono(ClientesTemporalModel model) {
		this.enviarCorreo(model);
	}
	
	@Override
	public boolean enviarCorreo(ClientesTemporalModel model) {
		logger.info("Entra al proceso de correo: " + model.getTicket());
		logger.info(model.toString());
		logger.info("Entra al proceso de correo: " + model.getTicket());
		
		Integer idFacturaPac = model.getIdFacturaPac();
		logger.info("idFacturaPac: " + idFacturaPac);
		String fileName = facturasService.getFacturaByUuid(model.getUuid()).getNombreArchivo();
		int pacDefault = pacsService.getIdDefault();
		
		model.setPac(pacDefault);
		
		logger.info("Se crea PDF y XML para envio a correo");
		this.facturasService.crearArchivoXml(fileName, model.getXml());
		this.facturasService.crearPdfFromXml(fileName, model.getXml());
		this.facturasService.crearZipXmlPdf(fileName);
		logger.info("Se termina creacion de PDF y XML para envio a correo");
		
		this.facturasService.actualizarEstatusLog(model, EFacturas.PendienteEnviar.getValor());
		
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
				logger.info("Enviando correo: " + model.getTicket());
				this.mailSenderService.enviarCorreo(model.getEmail(), asunto, destinatarioCC, mensajeCorreo, esHtml, file);
				this.facturasService.actualizarEstatusLog(model, EFacturas.FacturaEnviada.getValor());
				this.facturasService.actualizarFacturaMail(idFacturaPac, EFacturaMailEstatus.ENVIADO.getId());
				logger.info("Termina de enviar correo: " +  model.getTicket());
			}
		} catch (Exception e) {
			logger.error("XXXXXXXXXXXX ERROR ENVIAR CORREO: " +  model.getTicket());
			this.facturasService.actualizarFacturaMail(idFacturaPac, EFacturaMailEstatus.ERROR.getId());
			e.printStackTrace();
			return false;
		}
		this.facturasService.eliminarArchivo(fileName);
		logger.info("Sale del proceso de correo: " + model.getTicket());
		return true;
	}

	@Override
	public boolean enviarTokenMultiple(ClientesTemporalModel model) {
		String destinatario = model.getEmail();
		String asunto = configFacService.getConfig().get("Mail.Usuario.Token.Subject");
		String mensaje = configFacService.getConfig().get("Mail.Usuario.Token.BodyMessage"); 
		boolean esHtml = Boolean.parseBoolean(configFacService.getConfig().get("Mail.Usuario.Token.IsHtml"));
		
		mensaje = mensaje.replace("{nombreCliente}", model.getRazonSocial());
		mensaje = mensaje.replace("{token}", model.getToken());
		
		try {
			this.mailSenderService.enviarTokenMultiple(destinatario, asunto, mensaje, esHtml);
		} catch (MessagingException e) {
			logger.error("XXXXXXXX ERROR Token Multiple " + model.getRazonSocial());
			logger.error("Correo: " + model.getEmail());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
