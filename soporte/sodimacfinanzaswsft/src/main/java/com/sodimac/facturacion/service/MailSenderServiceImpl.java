package com.sodimac.facturacion.service;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.util.UtilsString;

@Service
public class MailSenderServiceImpl implements MailSenderService {

	private static final Logger logger = LoggerFactory.getLogger(MailSenderServiceImpl.class);
	
	@Autowired
	private ConfiguracionFacturacionService configFacService;
	
	public void enviarCorreo(String destinatario, String asunto, String destinatarioCC, String mensaje, Boolean EsHtml, String file) throws AddressException, MessagingException {

		File filePathName = new File(file);
		
		final String username = configFacService.getConfig().get("Mail.From");
		final String password = configFacService.getConfig().get("Mail.Password");
		final String puerto = configFacService.getConfig().get("Mail.Port");
		final String servidor = configFacService.getConfig().get("Mail.Server");
		final String conexionSegura = configFacService.getConfig().get("Mail.SSL");
		final String desde = configFacService.getConfig().get("Mail.MailAdressSource");
		final String desdeNombre = UtilsString.encodeText(configFacService.getConfig().get("Mail.FromName"));
		final String desdeCompleto = desdeNombre + "<" + desde + ">";
		
		logger.info("destinatario: " + destinatario);
		logger.info("asunto: " + asunto);
		logger.info("destinatarioCC: " + destinatarioCC);
		logger.info("desdeCompleto: " + desdeCompleto);
		logger.info("EsHtml: " + EsHtml);
		logger.info("file: " + file);
		
		//logger.info("mensaje: " + mensaje);

		String subType;
		if(EsHtml){
			subType = "html";
		}else{
			subType = "plain";
		}
		
		logger.info("creando propiedades del correo");
		Properties props = new Properties();
		props.put("mail.smtp.host", servidor);
		props.put("mail.smtp.port", puerto);
		props.put("mail.smtp.starttls.enable", conexionSegura);
		props.put("mail.smtp.auth", "true");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		
		logger.info("Autenticacion correcta");
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(desdeCompleto));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
		if (destinatarioCC != null && !destinatarioCC.isEmpty()) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(destinatarioCC));
		}
		message.setSubject(asunto);
		message.setSubject(UtilsString.encodeText(asunto));
		
		logger.info("se codifica el asunto: " + message.getSubject());
		
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		
		DataSource source = new FileDataSource(file);
		messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filePathName.getName());
        multipart.addBodyPart(messageBodyPart);

        BodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(mensaje, "text/" + subType + "; charset=UTF-8");
        
        multipart.addBodyPart(htmlBodyPart);
        
        message.setContent(multipart);
		
        logger.info("Enviando correo");
		Transport.send(message);
		logger.info("Correo enviado");
	}
	
	public void enviarTokenMultiple(String destinatario, String asunto, String mensaje, Boolean EsHtml) throws MessagingException {
				
		final String username = configFacService.getConfig().get("Mail.From");
		final String password = configFacService.getConfig().get("Mail.Password");
		final String puerto = configFacService.getConfig().get("Mail.Port");
		final String servidor = configFacService.getConfig().get("Mail.Server");
		final String conexionSegura = configFacService.getConfig().get("Mail.SSL");
		final String desde = configFacService.getConfig().get("Mail.MailAdressSource");
		final String desdeNombre = UtilsString.encodeText(configFacService.getConfig().get("Mail.FromName"));
		final String desdeCompleto = desdeNombre + "<" + desde + ">";
		
		logger.info("destinatario: " + destinatario);
		logger.info("asunto: " + asunto);
		logger.info("desdeCompleto: " + desdeCompleto);
		logger.info("EsHtml: " + EsHtml);
		
		String subType;
		if(EsHtml){
			subType = "html";
		}else{
			subType = "plain";
		}
		
		logger.info("creando propiedades del correo");
		Properties props = new Properties();
		props.put("mail.smtp.host", servidor);
		props.put("mail.smtp.port", puerto);
		props.put("mail.smtp.starttls.enable", conexionSegura);
		props.put("mail.smtp.auth", "true");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		
		logger.info("Autenticacion correcta");
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(desdeCompleto));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
		message.setSubject(asunto);
		message.setSubject(UtilsString.encodeText(asunto));
		
		Multipart multipart = new MimeMultipart();

        BodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(mensaje, "text/" + subType + "; charset=UTF-8");
        
        multipart.addBodyPart(htmlBodyPart);
        logger.info("body created");
        
        message.setContent(multipart);
        logger.info("setcontent");
		
        logger.info("Enviando correo");
		Transport.send(message);
		logger.info("Correo enviado");
	}
}
