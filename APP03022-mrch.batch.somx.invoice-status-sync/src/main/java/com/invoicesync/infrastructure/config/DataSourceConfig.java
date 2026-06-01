package com.invoicesync.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sodimacSapDataSource")
    @ConfigurationProperties(prefix = "datasource.sodimac-sap")
    public DataSource sodimacSapDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sapitoDataSource")
    @ConfigurationProperties(prefix = "datasource.sapito")
    public DataSource sapitoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "i213DataSource")
    @ConfigurationProperties(prefix = "datasource.i213")
    public DataSource i213DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sodimacSapJdbcTemplate")
    public JdbcTemplate sodimacSapJdbcTemplate(@Qualifier("sodimacSapDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sapitoJdbcTemplate")
    public JdbcTemplate sapitoJdbcTemplate(@Qualifier("sapitoDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "i213JdbcTemplate")
    public JdbcTemplate i213JdbcTemplate(@Qualifier("i213DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
