package com.sodimac.batch.fiscal.download.config;

import javax.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.sodimac.batch.fiscal.download.repository.sap",
        entityManagerFactoryRef = "sapEntityManagerFactory",
        transactionManagerRef = "sapTransactionManager"
)
public class SapDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.sap")
    public DataSourceProperties sapDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource sapDataSource() {
        return sapDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sapEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(sapDataSource())
                .packages("com.sodimac.batch.fiscal.download.model.entity.sap")
                .persistenceUnit("sap")
                .properties(jpaProperties())
                .build();
    }

    private Map<String, String> jpaProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        return props;
    }

    @Bean
    public PlatformTransactionManager sapTransactionManager(
            @Qualifier("sapEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
