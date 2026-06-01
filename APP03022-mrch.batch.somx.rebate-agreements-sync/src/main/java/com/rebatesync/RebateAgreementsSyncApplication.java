package com.rebatesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RebateAgreementsSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(RebateAgreementsSyncApplication.class, args);
    }
}
