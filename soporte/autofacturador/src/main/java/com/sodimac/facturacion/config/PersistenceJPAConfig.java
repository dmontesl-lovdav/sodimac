package com.sodimac.facturacion.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@PropertySource({ "classpath:database.properties" })
@ComponentScan({ "com.sodimac.facturacion" })
@EnableJpaRepositories(basePackages = "com.sodimac.facturacion.repository")
public class PersistenceJPAConfig {

    @Autowired
    private Environment env;

    public PersistenceJPAConfig() {
        super();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.facturacion.entity" });

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.fac.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.fac.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.fac.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.fac.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernateProperties.setProperty("hibernate.c3p0.min_size",
		env.getProperty("c3p0.fac.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size",
		env.getProperty("c3p0.fac.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment",
		env.getProperty("c3p0.fac.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout",
		env.getProperty("c3p0.fac.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout",
		env.getProperty("c3p0.fac.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin",
		env.getProperty("c3p0.fac.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery",
		env.getProperty("c3p0.fac.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod",
		env.getProperty("c3p0.fac.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads",
		env.getProperty("c3p0.fac.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource",
		env.getProperty("c3p0.fac.contextClassLoaderSource"));
		
		return hibernateProperties;
    }
    
    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.fac.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.fac.url"));
        dataSource.setUsername(env.getProperty("jdbc.fac.user"));
        dataSource.setPassword(env.getProperty("jdbc.fac.pass"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}