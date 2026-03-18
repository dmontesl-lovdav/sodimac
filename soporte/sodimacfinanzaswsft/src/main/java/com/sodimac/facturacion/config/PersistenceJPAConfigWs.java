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
import org.springframework.context.annotation.Primary;
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
@PropertySource({ "classpath:databaseWs.properties" })
@ComponentScan({ "com.sodimac.facturacion" })
@EnableJpaRepositories(
		basePackages = "com.sodimac.facturacion.repository.ws",
		entityManagerFactoryRef = "entityManagerFactoryWs", 
		transactionManagerRef = "transactionManagerWs"
		)
public class PersistenceJPAConfigWs {

    @Autowired
    private Environment env;

    @Autowired
	private SeguridadServiceWs seguridadServiceWs;

    public PersistenceJPAConfigWs() {
        super();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryWs() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceWs());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.facturacion.entity.ws" });
        entityManagerFactoryBean.setPersistenceUnitName("entityManagerFactoryWs");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.ws.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.ws.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.ws.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.ws.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernateProperties.setProperty("hibernate.c3p0.min_size",
		env.getProperty("c3p0.ws.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size",
		env.getProperty("c3p0.ws.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment",
		env.getProperty("c3p0.ws.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout",
		env.getProperty("c3p0.ws.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout",
		env.getProperty("c3p0.ws.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin",
		env.getProperty("c3p0.ws.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery",
		env.getProperty("c3p0.ws.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod",
		env.getProperty("c3p0.ws.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads",
		env.getProperty("c3p0.ws.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource",
		env.getProperty("c3p0.ws.contextClassLoaderSource"));
		 
        return hibernateProperties;
    }
    
    @Bean
    @Primary
    public DataSource dataSourceWs() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.ws.driverClassName"));
        dataSource.setUrl(seguridadServiceWs.desencriptar(env.getProperty("jdbc.ws.url")));
        dataSource.setUsername(seguridadServiceWs.desencriptar(env.getProperty("jdbc.ws.user")));
        dataSource.setPassword(seguridadServiceWs.desencriptar(env.getProperty("jdbc.ws.pass")));
        return dataSource;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManagerWs(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationWs() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}