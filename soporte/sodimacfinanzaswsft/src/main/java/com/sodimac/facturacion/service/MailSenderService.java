package com.sodimac.facturacion.service;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public interface MailSenderService {

	public void enviarCorreo(String destinatario, String asunto, String destinatarioCC, String mensaje, Boolean EsHtml, String file) throws AddressException, MessagingException;
	
	public void enviarTokenMultiple(String destinatario, String asunto, String mensaje, Boolean EsHtml) throws MessagingException;

}
