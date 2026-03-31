package com.sodimac.bctfacturacion.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sodimac.bctfacturacion.service.SeguridadService;

@Configuration
@EnableTransactionManagement
@ComponentScan({ "com.sodimac.bctfacturacion" })
@EnableJpaRepositories(
		basePackages = "com.sodimac.bctfacturacion.repository.facturacion",
		entityManagerFactoryRef = "entityManagerFactoryFacturacion", 
		transactionManagerRef = "transactionManagerFacturacion"
	)
public class PersistenceJPAConfigFacturacion {

    @Autowired
    private Environment env;
    
    @Autowired
    private SeguridadService seguridadService;

    public PersistenceJPAConfigFacturacion() {
        super();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryFacturacion() {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceFacturacion());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.bctfacturacion.entity.facturacion" });

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    private Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        return hibernateProperties;
    }
    
    @Bean
    public DataSource dataSourceFacturacion() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl( seguridadService.desencriptar( env.getProperty("jdbc.url")) );
        dataSource.setUsername(seguridadService.desencriptar( env.getProperty("jdbc.user")) );
        dataSource.setPassword( seguridadService.desencriptar( env.getProperty("jdbc.pass")) );
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManagerFacturacion() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory( entityManagerFactoryFacturacion().getObject() );
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}