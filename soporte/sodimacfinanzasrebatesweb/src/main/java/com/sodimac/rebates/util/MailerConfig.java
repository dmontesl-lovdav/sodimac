package com.sodimac.rebates.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.sodimac.rebates.service.ISeguridadService;

@Configuration
public class MailerConfig {

	@Value("${spring.mail.protocol}")
	private String protocol;

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private int port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${spring.mail.password}")
	private String password;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private boolean auth;

	@Value("${mail.smtp.starttls.enable}")
	private boolean starttls;

	@Value("${mail.smtp.starttls.required}")
	private boolean startlls_required;

//	@Autowired
//	private IConfigService serviceConfig;

	@Autowired
	private ISeguridadService seguridadService;

	@Bean
	public JavaMailSender javaMailSender() throws DataLengthException, IllegalStateException,
			InvalidCipherTextException, UnsupportedEncodingException {

		Properties mailProperties = new Properties();

//		Configuracion Conf = serviceConfig.configByName("Mail.Server");
//		host = Conf.Valor;
//
//		Conf = serviceConfig.configByName("Mail.Port");
//		port = Integer.parseInt(Conf.Valor);

		mailProperties.put("mail.smtp.auth", "true");
		mailProperties.put("mail.smtp.starttls.enable", starttls);
		mailProperties.put("mail.smtp.starttls.required", startlls_required);
		mailProperties.put("mail.smtp.host", host);
		mailProperties.put("mail.smtp.port", port);

//		Conf = serviceConfig.configByName("Mail.MailAddressSource");
		// final String username = "Notificaciones.TI.Sodimac.MX@gmail.com";
		// final String username = "fabian140290@gmail.com";

//		Conf = serviceConfig.configByName("Mail.Password");
		// final String password = seguridadService.desencriptar(Conf.Valor);
		// final String password = "#Pa55word";
		// TODO: final String password = "#miPassword";

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setJavaMailProperties(mailProperties);
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setProtocol(protocol);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		return mailSender;
	}

}
