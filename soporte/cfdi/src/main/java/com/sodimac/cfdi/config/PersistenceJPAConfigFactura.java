package com.sodimac.cfdi.config;

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

import com.sodimac.cfdi.service.SeguridadService;

@Configuration
@PropertySource({
    "classpath:databaseFactura.properties",
    "classpath:databaseFactura-${spring.profiles.active:prod}.properties"
})
@ComponentScan({ "com.sodimac.cfdi" })
@EnableJpaRepositories(
		basePackages = {"com.sodimac.cfdi.repositoryFactura"},
		entityManagerFactoryRef = "entityManagerFactoryFactura", 
		transactionManagerRef = "transactionManagerFactura"
		)
public class PersistenceJPAConfigFactura {

    @Autowired
    private Environment env;

    @SuppressWarnings("unused")
	@Autowired
	private SeguridadService seguridadService;

    public PersistenceJPAConfigFactura() {
        super();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryFactura() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceFactura());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.cfdi.entityFactura" });
        entityManagerFactoryBean.setPersistenceUnitName("entityManagerFactoryFactura");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.factura.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.factura.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.factura.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.factura.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        hibernateProperties.setProperty("hibernate.allow_update_outside_transaction", "true");
		hibernateProperties.setProperty("hibernate.c3p0.min_size",
		env.getProperty("c3p0.factura.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size",
		env.getProperty("c3p0.factura.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment",
		env.getProperty("c3p0.factura.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout",
		env.getProperty("c3p0.factura.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout",
		env.getProperty("c3p0.factura.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin",
		env.getProperty("c3p0.factura.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery",
		env.getProperty("c3p0.factura.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod",
		env.getProperty("c3p0.factura.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads",
		env.getProperty("c3p0.factura.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource",
		env.getProperty("c3p0.factura.contextClassLoaderSource"));
		 
        return hibernateProperties;
    }
    
    @Bean
    public DataSource dataSourceFactura() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("jdbc.factura.driverClassName"));
//        dataSource.setUrl(seguridadService.desencriptar(env.getProperty("jdbc.factura.url")));
//        dataSource.setUsername(seguridadService.desencriptar(env.getProperty("jdbc.factura.user")));
//        dataSource.setPassword(seguridadService.desencriptar(env.getProperty("jdbc.factura.pass")));
        
        dataSource.setDriverClassName(env.getProperty("jdbc.factura.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.factura.url"));
        dataSource.setUsername(env.getProperty("jdbc.factura.user"));
        dataSource.setPassword(env.getProperty("jdbc.factura.pass"));
        
//        dataSource.setUrl((env.getProperty("jdbc.factura.url")));
//        System.out.println(seguridadService.encriptar("jdbc:mysql://10.138.150.71:3306/sodimacfiscal?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CST6CDT"));
//        dataSource.setUsername((env.getProperty("jdbc.factura.user")));
//        System.out.println(seguridadService.encriptar("dba_mysql"));
//        dataSource.setPassword((env.getProperty("jdbc.factura.pass")));
//        System.out.println(seguridadService.encriptar("Sodimac123*"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManagerFactura(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationFactura() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}