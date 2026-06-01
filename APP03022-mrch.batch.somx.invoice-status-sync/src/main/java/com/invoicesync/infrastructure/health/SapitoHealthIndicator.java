package com.invoicesync.infrastructure.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health Indicator para la base de datos SAPITO (Oracle)
 */
@Component("sapitoHealth")
public class SapitoHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public SapitoHealthIndicator(@Qualifier("sapitoJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            // Query de validación para Oracle
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);

            long responseTime = System.currentTimeMillis() - startTime;

            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("database", "odsrmxts")
                        .withDetail("server", "ensenada:1541")
                        .withDetail("type", "Oracle")
                        .withDetail("responseTimeMs", responseTime)
                        .withDetail("status", "Conexión exitosa")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "odsrmxts")
                        .withDetail("error", "Query de validación falló")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "odsrmxts")
                    .withDetail("server", "ensenada:1541")
                    .withDetail("type", "Oracle")
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}
