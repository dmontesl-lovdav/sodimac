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
		basePackages = "com.sodimac.bctfacturacion.repository.fiscal",
		entityManagerFactoryRef = "entityManagerFactoryFiscal", 
		transactionManagerRef = "transactionManagerFiscal"
		)

public class PersistenceJPAConfigFiscal	 {

	@Autowired
    private Environment env;
	
	@Autowired
	private SeguridadService seguridadService;

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryFiscal() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource( dataSourceFiscal() );
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.bctfacturacion.entity.fiscal" });

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    private Properties additionalProperties() {
        Properties hibernateProperties = new Properties();
        
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.fiscal.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.fiscal.dialect"));
        
        hibernateProperties.setProperty("hibernate.showSql", env.getProperty("hibernate.fiscal.show_sql"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.fiscal.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.fiscal.cache.use_query_cache"));
        hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        
		hibernateProperties.setProperty("hibernate.c3p0.min_size", env.getProperty("c3p0.fiscal.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size", env.getProperty("c3p0.fiscal.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment", env.getProperty("c3p0.fiscal.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout", env.getProperty("c3p0.fiscal.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout", env.getProperty("c3p0.fiscal.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin", env.getProperty("c3p0.fiscal.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery", env.getProperty("c3p0.fiscal.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod", env.getProperty("c3p0.fiscal.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads", env.getProperty("c3p0.fiscal.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource", env.getProperty("c3p0.fiscal.contextClassLoaderSource"));
        
        return hibernateProperties;
    }
    
    @Primary
    @Bean
    public DataSource dataSourceFiscal() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.fiscal.driverClassName"));
        dataSource.setUrl( seguridadService.desencriptar(env.getProperty("jdbc.fiscal.url")));
        dataSource.setUsername( seguridadService.desencriptar(env.getProperty("jdbc.fiscal.user")));
        dataSource.setPassword( seguridadService.desencriptar(env.getProperty("jdbc.fiscal.pass")));
	    return dataSource;
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManagerFiscal() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory( entityManagerFactoryFiscal().getObject() );
        return transactionManager;
    }

}
