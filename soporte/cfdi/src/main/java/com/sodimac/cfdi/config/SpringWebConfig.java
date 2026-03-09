package com.sodimac.cfdi.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class SpringWebConfig {

	private static final String [] PROPERTIES_FILES_BASEMESSAGE = {"classpath:/bundles/messages"};
	private static final String ENCODING = "UTF-8";
	
	@Bean
    public MessageSource messageSource()
    {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(PROPERTIES_FILES_BASEMESSAGE);
        messageSource.setDefaultEncoding(ENCODING);
        return messageSource;
    }
}
