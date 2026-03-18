package com.sodimac.facturacion.config;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
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

import com.sodimac.facturacion.service.SeguridadServiceWs;

@Configuration
@PropertySource({ "classpath:databaseReb.properties" })
@ComponentScan({ "com.sodimac.facturacion" })
@EnableJpaRepositories(
		basePackages = "com.sodimac.facturacion.repository.reb",
		entityManagerFactoryRef = "entityManagerFactoryReb", 
		transactionManagerRef = "transactionManagerReb"
		)
public class PersistenceJPAConfigReb {

    @Autowired
    private Environment env;

    @Autowired
	private SeguridadServiceWs seguridadServiceWs;

    public PersistenceJPAConfigReb() {
        super();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryReb() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceReb());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.facturacion.entity.reb" });
        entityManagerFactoryBean.setPersistenceUnitName("entityManagerFactoryReb");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.reb.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.reb.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.reb.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.reb.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernateProperties.setProperty("hibernate.c3p0.min_size",
		env.getProperty("c3p0.reb.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size",
		env.getProperty("c3p0.reb.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment",
		env.getProperty("c3p0.reb.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout",
		env.getProperty("c3p0.reb.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout",
		env.getProperty("c3p0.reb.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin",
		env.getProperty("c3p0.reb.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery",
		env.getProperty("c3p0.reb.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod",
		env.getProperty("c3p0.reb.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads",
		env.getProperty("c3p0.reb.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource",
		env.getProperty("c3p0.reb.contextClassLoaderSource"));
		         
        return hibernateProperties;
    }
    
    @Bean
    public DataSource dataSourceReb() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.reb.driverClassName"));
        dataSource.setUrl(seguridadServiceWs.desencriptar(env.getProperty("jdbc.reb.url")));
        dataSource.setUsername(seguridadServiceWs.desencriptar(env.getProperty("jdbc.reb.user")));
        dataSource.setPassword(seguridadServiceWs.desencriptar(env.getProperty("jdbc.reb.pass")));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManagerReb(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationReb() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}