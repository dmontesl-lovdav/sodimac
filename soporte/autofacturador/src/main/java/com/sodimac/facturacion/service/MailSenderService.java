package com.sodimac.facturacion.service;

public interface MailSenderService {

	public Boolean enviarCorreo(String destinatario, String asunto, String destinatarioCC, String mensaje, Boolean EsHtml, String file);
	public  Boolean enviarTokenMultiple(String destinatario, String asunto, String mensaje, Boolean EsHtml);

}
