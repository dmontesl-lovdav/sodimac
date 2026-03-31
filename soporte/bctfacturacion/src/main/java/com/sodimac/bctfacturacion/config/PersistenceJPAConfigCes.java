package com.sodimac.bctfacturacion.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.sodimac.bctfacturacion.service.SeguridadService;

@Configuration
@ComponentScan({ "com.sodimac.bctfacturacion" })
@EnableJpaRepositories(
		basePackages = "com.sodimac.bctfacturacion.repository.ces",
		entityManagerFactoryRef = "entityManagerFactoryCes", 
		transactionManagerRef = "transactionManagerCes"
		)

public class PersistenceJPAConfigCes	 {

	@Autowired
    private Environment env;
	
	@Autowired
	private SeguridadService seguridadService;

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryCes() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource( dataSourceCes() );
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.bctfacturacion.entity.ces" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    private Properties additionalProperties() {
        Properties hibernateProperties = new Properties();
        
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.ces.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.ces.dialect"));
        
        hibernateProperties.setProperty("hibernate.showSql", env.getProperty("hibernate.ces.show_sql"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.ces.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.ces.cache.use_query_cache"));
        hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernateProperties.setProperty("hibernate.c3p0.min_size", env.getProperty("c3p0.ces.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size", env.getProperty("c3p0.ces.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment", env.getProperty("c3p0.ces.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout", env.getProperty("c3p0.ces.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout", env.getProperty("c3p0.ces.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin", env.getProperty("c3p0.ces.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery", env.getProperty("c3p0.ces.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod", env.getProperty("c3p0.ces.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads", env.getProperty("c3p0.ces.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource", env.getProperty("c3p0.ces.contextClassLoaderSource"));
        
        return hibernateProperties;
    }
    
    @Bean
    public DataSource dataSourceCes() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.ces.driverClassName"));
        dataSource.setUrl( seguridadService.desencriptar(env.getProperty("jdbc.ces.url")));
        dataSource.setUsername( seguridadService.desencriptar(env.getProperty("jdbc.ces.user")));
        dataSource.setPassword( seguridadService.desencriptar(env.getProperty("jdbc.ces.pass")));
	    return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManagerCes() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory( entityManagerFactoryCes().getObject() );
        return transactionManager;
    }

}
