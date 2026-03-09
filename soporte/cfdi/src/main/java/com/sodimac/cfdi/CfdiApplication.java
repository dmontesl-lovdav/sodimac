package com.sodimac.cfdi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CfdiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfdiApplication.class, args);
	}

}
