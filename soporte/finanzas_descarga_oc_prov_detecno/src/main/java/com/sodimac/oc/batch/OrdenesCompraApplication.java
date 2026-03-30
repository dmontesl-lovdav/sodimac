package com.sodimac.oc.batch;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class OrdenesCompraApplication {
	
	public static void main(String[] args) {
        SpringApplication sa = new SpringApplication(OrdenesCompraApplication.class);
        sa.setWebApplicationType(WebApplicationType.NONE);
        
        sa.setBannerMode(Banner.Mode.OFF);
        sa.setLogStartupInfo(false);

        ApplicationContext c = sa.run(args);
        MainComponent bean = c.getBean(MainComponent.class);
        bean.mainMethod();
	}
	
}
