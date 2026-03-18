package com.sodimac.facturacion.config;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.sql.DataSource;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.sodimac.facturacion.service.SeguridadServiceWs;

@Configuration
@PropertySource({ "classpath:databaseBct.properties" })
@ComponentScan({ "com.sodimac.facturacion" })
@EnableJpaRepositories(
			basePackages = "com.sodimac.facturacion.repository.bct",
			entityManagerFactoryRef = "entityManagerFactoryBct", 
			transactionManagerRef = "transactionManagerBct"
		)
public class PersistenceJPAConfigBct {

	@Autowired
    private Environment env;
	
	@Autowired
	private SeguridadServiceWs seguridadServiceWs;
	
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBct() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource( dataSourceBct() );
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.facturacion.entity.bct" });
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }
	
	private Properties additionalProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.bct.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.bct.dialect"));
        
        hibernateProperties.setProperty("hibernate.showSql", env.getProperty("hibernate.bct.show_sql"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.bct.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.bct.cache.use_query_cache"));
        hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernateProperties.setProperty("hibernate.c3p0.min_size", env.getProperty("c3p0.bct.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size", env.getProperty("c3p0.bct.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment", env.getProperty("c3p0.bct.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout", env.getProperty("c3p0.bct.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout", env.getProperty("c3p0.bct.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin", env.getProperty("c3p0.bct.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery", env.getProperty("c3p0.bct.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod", env.getProperty("c3p0.bct.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads", env.getProperty("c3p0.bct.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource", env.getProperty("c3p0.bct.contextClassLoaderSource"));
        return hibernateProperties;
    }
    
    @Bean
    public DataSource dataSourceBct() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.bct.driverClassName"));
        
        dataSource.setUrl( seguridadServiceWs.desencriptar( env.getProperty("jdbc.bct.url") ));
        dataSource.setUsername( seguridadServiceWs.desencriptar( env.getProperty("jdbc.bct.user") ));
        dataSource.setPassword( seguridadServiceWs.desencriptar( env.getProperty("jdbc.bct.pass") ));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManagerBct() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory( entityManagerFactoryBct().getObject() );
        return transactionManager;
    }
	
}
