package com.sodimac.rebates.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.util.Mail;

@Service
public class MailerService implements IMailerService {

	private JavaMailSender mailSender;

	@Autowired
	public MailerService(JavaMailSender javamailSender) {

		this.mailSender = javamailSender;
	}

	@Override
	public boolean sendMail(Mail message, boolean isHtml) throws MessagingException {

		MimeMessage emailMessage = mailSender.createMimeMessage();
		MimeMessageHelper mailBuilder = new MimeMessageHelper(emailMessage, true);

		mailBuilder.setTo(message.getMailTo());
		mailBuilder.setFrom(message.getMailFrom());
		mailBuilder.setText(message.getMailContent(), isHtml); // Second parameter indicates that this is HTML mail
		mailBuilder.setSubject(message.getMailSubject());

		try {

			mailSender.send(emailMessage);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			System.out.println(ex.getMessage());

			return false;
		}

		return true;
	}

}
