package com.invoicesync.infrastructure.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health Indicator para la base de datos i213 (SQL Server)
 */
@Component("i213Health")
public class I213HealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public I213HealthIndicator(@Qualifier("i213JdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            // Query de validación para SQL Server
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            long responseTime = System.currentTimeMillis() - startTime;

            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("database", "AdmIF213ProdDB")
                        .withDetail("server", "10.138.153.10:1433")
                        .withDetail("type", "SQL Server")
                        .withDetail("responseTimeMs", responseTime)
                        .withDetail("status", "Conexión exitosa")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "AdmIF213ProdDB")
                        .withDetail("error", "Query de validación falló")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "AdmIF213ProdDB")
                    .withDetail("server", "10.138.153.10:1433")
                    .withDetail("type", "SQL Server")
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}
