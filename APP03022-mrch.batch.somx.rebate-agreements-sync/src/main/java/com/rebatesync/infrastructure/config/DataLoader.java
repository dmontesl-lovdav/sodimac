package com.rebatesync.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    @Bean
    @Profile("dev")
    public CommandLineRunner loadData() {
        return args -> {
            log.info("==============================================");
            log.info("Rebate Agreements Sync Application Started");
            log.info("Profile: dev");
            log.info("==============================================");
            log.info("Application is ready to sync rebate agreements");
            log.info("Scheduled sync time: 03:00 AM (Mexico City time)");
            log.info("Manual sync available via POST /api/sync/full");
            log.info("==============================================");
        };
    }
}
