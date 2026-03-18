package com.sodimac.facturacion.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.util.UtilsString;

@Service
public class MailSenderServiceImpl implements MailSenderService {

	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	@Autowired
	private ErrorComponent errorComponent;
	
	public  Boolean enviarCorreo(String destinatario, String asunto, String destinatarioCC, String mensaje, Boolean EsHtml, String file) {

		Boolean envioEmail = false;
		File filePathName = new File(file);
		
		final String username = catConfiguracionService.findParameterByKey("Mail.From");
		final String password = catConfiguracionService.findParameterByKey("Mail.Password");
		final String puerto = catConfiguracionService.findParameterByKey("Mail.Port");
		final String servidor = catConfiguracionService.findParameterByKey("Mail.Server");
		final String conexionSegura = catConfiguracionService.findParameterByKey("Mail.SSL");
		final String desde = catConfiguracionService.findParameterByKey("Mail.MailAdressSource");
		final String desdeNombre = UtilsString.encodeText(catConfiguracionService.findParameterByKey("Mail.FromName"));
		final String desdeCompleto = desdeNombre + "<" + desde + ">";

		String subType;
		if(EsHtml){
			subType = "html";
		}else{
			subType = "plain";
		}
		
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

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(desdeCompleto));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(destinatarioCC));
			message.setSubject(asunto);
			message.setSubject(UtilsString.encodeText(asunto));
			
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
			
			
			Transport.send(message);
		
			envioEmail = true;
		} catch (MessagingException e) {
			e.printStackTrace();
			errorComponent.setPagina("enviarCorreo");
			errorComponent.guardarLog(e);
			//throw new RuntimeException(e);
		}
		return envioEmail;
	}
	
	public  Boolean enviarTokenMultiple(String destinatario, String asunto, String mensaje, Boolean EsHtml) {
				
		Boolean envioEmail = false;
		final String username = catConfiguracionService.findParameterByKey("Mail.From");
		final String password = catConfiguracionService.findParameterByKey("Mail.Password");
		final String puerto = catConfiguracionService.findParameterByKey("Mail.Port");
		final String servidor = catConfiguracionService.findParameterByKey("Mail.Server");
		final String conexionSegura = catConfiguracionService.findParameterByKey("Mail.SSL");
		final String desde = catConfiguracionService.findParameterByKey("Mail.MailAdressSource");
		final String desdeNombre = UtilsString.encodeText(catConfiguracionService.findParameterByKey("Mail.FromName"));
		final String desdeCompleto = desdeNombre + "<" + desde + ">";
		List<String> datosArr = new ArrayList <String>();
		String subType;
		if(EsHtml){
			subType = "html";
		}else{
			subType = "plain";
		}
		
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
		
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(desdeCompleto));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
			message.setSubject(asunto);
			message.setSubject(UtilsString.encodeText(asunto));
			
			Multipart multipart = new MimeMultipart();

	        BodyPart htmlBodyPart = new MimeBodyPart();
	        htmlBodyPart.setContent(mensaje, "text/" + subType + "; charset=UTF-8");
	        
	        multipart.addBodyPart(htmlBodyPart);
	        
	        message.setContent(multipart);
			
			
			Transport.send(message);
			actividadesModel.setTicket("");
			actividadesModel.registrarActividad(37, null,"MailSender");
			envioEmail = true;
		} catch (MessagingException e) {
			datosArr.clear();
			datosArr.add(actividadesModel.getToken());
			actividadesModel.registrarActividad(40, datosArr,"MailSender");
			e.printStackTrace();
			errorComponent.setRfc(actividadesModel.getRfc());
			errorComponent.setPagina("enviarTokenMultiple");
			errorComponent.guardarLog(e);
			throw new RuntimeException(e);
		}
		
		
		return envioEmail;
	}
}
