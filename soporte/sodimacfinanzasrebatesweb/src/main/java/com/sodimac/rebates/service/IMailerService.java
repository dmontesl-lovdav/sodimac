package com.sodimac.rebates.service;

import javax.mail.MessagingException;

import com.sodimac.rebates.util.Mail;

public interface IMailerService {

	public boolean sendMail(Mail message, boolean isHtml) throws MessagingException;

}
