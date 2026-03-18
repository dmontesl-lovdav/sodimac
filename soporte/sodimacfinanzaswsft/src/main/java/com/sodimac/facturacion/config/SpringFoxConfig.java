package com.sodimac.facturacion.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Lists;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SpringFoxConfig {
	
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String DEFAULT_INCLUDE_PATTERN = "/api/(obt|tim|ret|can|cre|con|env|val).*";
	 
	@Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()
          .apis(RequestHandlerSelectors.basePackage("com.sodimac.facturacion.controller"))
          .paths(PathSelectors.any())
          .build()
          .apiInfo(apiInfo())
          .useDefaultResponseMessages(false)                                   
          .globalResponseMessage(RequestMethod.POST, newArrayList(
        		  new ResponseMessageBuilder()
        		  .code(400)
        		  .message("400 Solicitud err\u00f3nea")
        		  .responseModel(new ModelRef("string"))
        		  .build(),
        		  new ResponseMessageBuilder()
        		  .code(403)
        		  .message("403 Acceso denegado, usuario/token no autorizado")
        		  .responseModel(new ModelRef("string"))
        		  .build(),
        		  new ResponseMessageBuilder()
        		  .code(409)
        		  .message("409 Hubo un conflicto al intentar la conexi\u00f3n con la red")
        		  .responseModel(new ModelRef("string"))
        		  .build(),
        		  new ResponseMessageBuilder()
        		  .code(500)
        		  .message("500 Ocurri\u00f3 un error interno")
        		  .responseModel(new ModelRef("string"))
        		  .build()
              ))
          .securityContexts(Lists.newArrayList(securityContext()))
          .securitySchemes(Lists.newArrayList(apiKey()));                                           
    }
	
	private SecurityContext securityContext() {
		    return SecurityContext.builder()
		        .securityReferences(defaultAuth())
		        .forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
		        .build();
	}
	 
	List<SecurityReference> defaultAuth() {
	    AuthorizationScope authorizationScope
	        = new AuthorizationScope("global", "accessEverything");
	    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
	    authorizationScopes[0] = authorizationScope;
	    return Lists.newArrayList(
	        new SecurityReference("AUTHORIZATION", authorizationScopes));
	}
	
	private ApiKey apiKey() {
		return new ApiKey("AUTHORIZATION", AUTHORIZATION_HEADER, "header");
	}

    @Bean
    public SecurityConfiguration security() {
    return SecurityConfigurationBuilder
    	.builder()
    	.scopeSeparator(",")
        .additionalQueryStringParams(null)
        .useBasicAuthenticationWithAccessCodeGrant(false)
        .build();
    }
    
	private ApiInfo apiInfo() {
	    return new ApiInfo(
	      "WS de timbrado de tickets de Sodimac REST API Documentaci\u00f3n", 
	      "Este servicio te permite timbrar un ticket de venta o un ticket de devoluci\u00f3n generando la factura o bien la nota de cr\u00e9dito respectiva. Además te permite cancelar un documento fiscal. Es posible timbrar con la versi\u00f3n 3.3 y tambi\u00e9n con la versi\u00f3n 4.0, seg\u00fan se especifique o bien dependiendo de los parámetros de configuraci\u00f3n. El tipo de timbrado puede ser tanto de clientes como global.", 
	      "2.0.1", 
	      null, 
	      null, 
	      null, null, Collections.emptyList());
	}    
}