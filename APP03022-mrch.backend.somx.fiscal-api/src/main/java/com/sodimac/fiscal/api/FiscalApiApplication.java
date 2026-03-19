package com.sodimac.fiscal.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FiscalApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiscalApiApplication.class, args);
	}

}
