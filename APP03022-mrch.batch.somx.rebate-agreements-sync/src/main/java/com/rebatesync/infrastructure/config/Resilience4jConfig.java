package com.rebatesync.infrastructure.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    /**
     * Retry configuration for rebate sync operations.
     * - Max attempts: 2 retries (3 total attempts including initial)
     * - Wait duration: 30 minutes between retries
     * - Retry on: All exceptions
     */
    @Bean
    public Retry rebateSyncRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // Initial attempt + 2 retries
                .waitDuration(Duration.ofMinutes(30))
                .retryExceptions(Exception.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        return registry.retry("rebateSync");
    }
}
