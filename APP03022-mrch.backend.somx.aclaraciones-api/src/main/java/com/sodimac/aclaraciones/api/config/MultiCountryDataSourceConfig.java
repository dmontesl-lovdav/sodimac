package com.sodimac.aclaraciones.api.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiCountryDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(MultiCountryDataSourceConfig.class);

    // SODIMAC
    @Value("${DATASOURCE_URL_SOMX}")
    private String soMx;
    @Value("${DATASOURCE_URL_SOPE}")
    private String soPe;
    @Value("${DATASOURCE_URL_SOCL}")
    private String soCl;
    @Value("${DATASOURCE_URL_SOCO}")
    private String soCo;
    @Value("${DATASOURCE_URL_SOAR}")
    private String soAr;
    @Value("${DATASOURCE_URL_SOUY}")
    private String soUy;
    @Value("${DATASOURCE_URL_SOBR}")
    private String soBr;

    // FALABELLA
    @Value("${DATASOURCE_URL_FACL}")
    private String faCl;
    @Value("${DATASOURCE_URL_FAPE}")
    private String faPe;
    @Value("${DATASOURCE_URL_FACO}")
    private String faCo;

    // TOTTUS
    @Value("${DATASOURCE_URL_TOCL}")
    private String toCl;
    @Value("${DATASOURCE_URL_TOPE}")
    private String toPe;

    @Value("${DATASOURCE_USERNAME}")
    private String username;
    @Value("${DATASOURCE_PASSWORD}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {

        Map<Object, Object> dataSources = new HashMap<>();

        // Sodimac
        dataSources.put("SOMX", createDataSource(soMx));
        dataSources.put("SOPE", createDataSource(soPe));
        dataSources.put("SOCL", createDataSource(soCl));
        dataSources.put("SOCO", createDataSource(soCo));
        dataSources.put("SOAR", createDataSource(soAr));
        dataSources.put("SOUY", createDataSource(soUy));
        dataSources.put("SOBR", createDataSource(soBr));

        // Falabella
        dataSources.put("FACL", createDataSource(faCl));
        dataSources.put("FAPE", createDataSource(faPe));
        dataSources.put("FACO", createDataSource(faCo));

        // Tottus
        dataSources.put("TOCL", createDataSource(toCl));
        dataSources.put("TOPE", createDataSource(toPe));

        // 🔍 Log de todos los datasources cargados
        dataSources.forEach((key, ds) -> {
            if (ds instanceof HikariDataSource hikari) {
                log.info("Datasource cargado [{}] → {}", key, hikari.getJdbcUrl());
            }
        });

        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {

                String commerce = CountryContextHolder.getCommerce();
                String country = CountryContextHolder.getCountry();

                // Arranque de Spring / Hibernate
                if (commerce == null || country == null) {
                    log.warn("Tenant no definido aún, usando DEFAULT = SOMX");
                    return "SOMX";
                }

                String tenantKey = normalizeTenantKey(commerce, country);

                DataSource ds = (DataSource) dataSources.get(tenantKey);
                if (ds instanceof HikariDataSource hikari) {
                    log.info(
                            "Datasource seleccionado → {} | JDBC URL → {} | User → {}",
                            tenantKey,
                            hikari.getJdbcUrl(),
                            hikari.getUsername());
                } else {
                    log.warn(
                            "Datasource seleccionado → {} (no se pudo resolver el datasource)",
                            tenantKey);
                }

                return tenantKey;
            }
        };

        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(dataSources.get("SOMX"));

        return routingDataSource;
    }

    /**
     * Normaliza lo que manda el frontend (commerce + country)
     * a la key interna del datasource
     *
     * Ej:
     * SOD + AR -> SOAR
     * FAL + CL -> FACL
     * TOT + PE -> TOPE
     */
    private String normalizeTenantKey(String commerce, String country) {

        if ("SOD".equals(commerce)) {
            return "SO" + country;
        }

        if ("FAL".equals(commerce)) {
            return "FA" + country;
        }

        if ("TOT".equals(commerce)) {
            return "TO" + country;
        }

        // fallback defensivo
        return commerce + country;
    }

    private DataSource createDataSource(String url) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }
}
