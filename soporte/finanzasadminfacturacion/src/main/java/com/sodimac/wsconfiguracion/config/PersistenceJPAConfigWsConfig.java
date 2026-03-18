package com.sodimac.wsconfiguracion.config;

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
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sodimac.wsconfiguracion.service.SeguridadServiceWs;

@Configuration
@PropertySource({ "classpath:databaseConfig.properties" })
@ComponentScan({ "com.sodimac.wsconfiguracion" })
@EnableJpaRepositories(
		basePackages = "com.sodimac.wsconfiguracion.repository.config",
		entityManagerFactoryRef = "entityManagerFactoryConfig", 
		transactionManagerRef = "transactionManagerConfig"
		)
@EnableTransactionManagement
public class PersistenceJPAConfigWsConfig {

    @Autowired
    private Environment env;

    @Autowired
	private SeguridadServiceWs seguridadServiceWs;

    public PersistenceJPAConfigWsConfig() {
        super();
    }
	
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryConfig() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryConfigBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryConfigBean.setDataSource(dataSourceConfig());
        entityManagerFactoryConfigBean.setPackagesToScan(new String[] { "com.sodimac.wsconfiguracion.entity.config" });
        entityManagerFactoryConfigBean.setPersistenceUnitName("entityManagerFactoryConfig");
        
        final HibernateJpaVendorAdapter vendorAdapterConfig = new HibernateJpaVendorAdapter();
        vendorAdapterConfig.setGenerateDdl(true);
        vendorAdapterConfig.setShowSql(true);
        entityManagerFactoryConfigBean.setJpaVendorAdapter(vendorAdapterConfig);
        entityManagerFactoryConfigBean.setJpaProperties(additionalPropertiesConfig());
        
        HibernateJpaDialect jpaDialect = new HibernateJpaDialect();
        entityManagerFactoryConfigBean.setJpaDialect(jpaDialect);

        return entityManagerFactoryConfigBean;
    }

    final Properties additionalPropertiesConfig() {
        final Properties hibernatePropertiesConfig = new Properties();
        hibernatePropertiesConfig.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.config.hbm2ddl.auto"));
        hibernatePropertiesConfig.setProperty("hibernate.dialect", env.getProperty("hibernate.config.dialect"));
        hibernatePropertiesConfig.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.config.cache.use_second_level_cache"));
        hibernatePropertiesConfig.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.config.cache.use_query_cache"));
        hibernatePropertiesConfig.setProperty("hibernate.ws.show_sql", env.getProperty("hibernate.config.show_sql"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.min_size",
		env.getProperty("c3p0.config.min_size"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.max_size",
		env.getProperty("c3p0.config.max_size"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.acquire_increment",
		env.getProperty("c3p0.config.acquire_increment"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.timeout",
		env.getProperty("c3p0.config.timeout"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.testConnectionOnCheckout",
		env.getProperty("c3p0.config.testConnectionOnCheckout"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.preferredTestQuery",
		env.getProperty("c3p0.config.preferredTestQuery"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.idleConnectionTestPeriod",
		env.getProperty("c3p0.config.idleConnectionTestPeriod"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.privilegeSpawnedThreads",
		env.getProperty("c3p0.config.privilegeSpawnedThreads"));
		hibernatePropertiesConfig.setProperty("hibernate.c3p0.contextClassLoaderSource",
		env.getProperty("c3p0.config.contextClassLoaderSource"));
		 
        return hibernatePropertiesConfig;
    }
    
    @Bean
    public DataSource dataSourceConfig() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final DriverManagerDataSource dataSourceConfig = new DriverManagerDataSource();
        dataSourceConfig.setDriverClassName(env.getProperty("jdbc.config.driverClassName"));
        dataSourceConfig.setUrl(seguridadServiceWs.desencriptar(env.getProperty("jdbc.config.url")));
        dataSourceConfig.setUsername(seguridadServiceWs.desencriptar(env.getProperty("jdbc.config.user")));
        dataSourceConfig.setPassword(seguridadServiceWs.desencriptar(env.getProperty("jdbc.config.pass")));
        return dataSourceConfig;
    }

    @Bean(name="transactionManagerConfig")
    public PlatformTransactionManager transactionManagerConfig(final EntityManagerFactory emf) throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final JpaTransactionManager transactionManagerConfig = new JpaTransactionManager();
        transactionManagerConfig.setEntityManagerFactory( entityManagerFactoryConfig().getObject());
        return transactionManagerConfig;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationWsConfig() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
    
}
