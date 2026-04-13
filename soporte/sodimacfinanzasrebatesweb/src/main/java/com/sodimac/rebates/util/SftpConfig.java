package com.sodimac.rebates.util;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;

//@Configuration
public class SftpConfig {

//	@Value("${sodimac.rebates.sftp.host}")
	private String sftpHost;

//	@Value("${sodimac.rebates.sftp.port}")
	private int sftpPort;

//	@Value("${sodimac.rebates.sftp.user}")
	private String sftpUser;

//	@Value("${sodimac.rebates.sftp.password}")
	private String sftpPassword;

//	@Value("${sodimac.rebates.sftp.remote.directory}")
	private String sftpRemoteDirectory;

	public DefaultSftpSessionFactory gimmeFactory() {

		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
		factory.setHost(sftpHost);
		factory.setPort(sftpPort);
		factory.setUser(sftpUser);
		factory.setPassword(sftpPassword);
		factory.setAllowUnknownKeys(true);

		return factory;
	}

//	@Bean
//	@ServiceActivator(inputChannel = "sftpUpload")
	public MessageHandler handlerIndex() {

		SftpMessageHandler handler = new SftpMessageHandler(gimmeFactory());
		ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
		// handler.setRemoteDirectoryExpression(new
		// LiteralExpression(sftpRemoteDirectory));
		handler.setRemoteDirectoryExpression(EXPRESSION_PARSER.parseExpression("headers['pathSftp']"));

		return handler;
	}

}
