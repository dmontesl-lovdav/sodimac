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

import com.sodimac.cfdi.service.SeguridadService;

@Configuration
@PropertySource({
    "classpath:databaseFiscal.properties",
    "classpath:databaseFiscal-${spring.profiles.active:prod}.properties"
})
@ComponentScan({ "com.sodimac.cfdi" })
@EnableJpaRepositories(
		basePackages = {"com.sodimac.cfdi.repository.*"},
		entityManagerFactoryRef = "entityManagerFactoryFiscal", 
		transactionManagerRef = "transactionManagerFiscal"
		)
public class PersistenceJPAConfigFiscal {

    @Autowired
    private Environment env;

    @SuppressWarnings("unused")
	@Autowired
	private SeguridadService seguridadService;

    public PersistenceJPAConfigFiscal() {
        super();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryFiscal() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceFiscal());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.sodimac.cfdi.entity.*" });
        entityManagerFactoryBean.setPersistenceUnitName("entityManagerFactoryFiscal");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.fiscal.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.fiscal.dialect"));
        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.fiscal.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.fiscal.cache.use_query_cache"));
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");
        hibernateProperties.setProperty("hibernate.allow_update_outside_transaction", "true");
		hibernateProperties.setProperty("hibernate.c3p0.min_size",
		env.getProperty("c3p0.fiscal.min_size"));
		hibernateProperties.setProperty("hibernate.c3p0.max_size",
		env.getProperty("c3p0.fiscal.max_size"));
		hibernateProperties.setProperty("hibernate.c3p0.acquire_increment",
		env.getProperty("c3p0.fiscal.acquire_increment"));
		hibernateProperties.setProperty("hibernate.c3p0.timeout",
		env.getProperty("c3p0.fiscal.timeout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckout",
		env.getProperty("c3p0.fiscal.testConnectionOnCheckout"));
		hibernateProperties.setProperty("hibernate.c3p0.testConnectionOnCheckin",
		env.getProperty("c3p0.fiscal.testConnectionOnCheckin"));
		hibernateProperties.setProperty("hibernate.c3p0.preferredTestQuery",
		env.getProperty("c3p0.fiscal.preferredTestQuery"));
		hibernateProperties.setProperty("hibernate.c3p0.idleConnectionTestPeriod",
		env.getProperty("c3p0.fiscal.idleConnectionTestPeriod"));
		hibernateProperties.setProperty("hibernate.c3p0.privilegeSpawnedThreads",
		env.getProperty("c3p0.fiscal.privilegeSpawnedThreads"));
		hibernateProperties.setProperty("hibernate.c3p0.contextClassLoaderSource",
		env.getProperty("c3p0.fiscal.contextClassLoaderSource"));
		 
        return hibernateProperties;
    }
    
    @Bean
    @Primary
    public DataSource dataSourceFiscal() throws DataLengthException, IllegalStateException, InvalidCipherTextException, UnsupportedEncodingException {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("jdbc.fiscal.driverClassName"));
//        dataSource.setUrl(seguridadService.desencriptar(env.getProperty("jdbc.fiscal.url")));
//        dataSource.setUsername(seguridadService.desencriptar(env.getProperty("jdbc.fiscal.user")));
//        dataSource.setPassword(seguridadService.desencriptar(env.getProperty("jdbc.fiscal.pass")));
        
        dataSource.setDriverClassName(env.getProperty("jdbc.fiscal.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.fiscal.url"));
        dataSource.setUsername(env.getProperty("jdbc.fiscal.user"));
        dataSource.setPassword(env.getProperty("jdbc.fiscal.pass"));
        
//        dataSource.setUrl((env.getProperty("jdbc.fiscal.url")));
//        System.out.println(seguridadService.encriptar("jdbc:mysql://10.138.150.71:3306/sodimacfiscal?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CST6CDT"));
//        dataSource.setUsername((env.getProperty("jdbc.fiscal.user")));
//        System.out.println(seguridadService.encriptar("dba_mysql"));
//        dataSource.setPassword((env.getProperty("jdbc.fiscal.pass")));
//        System.out.println(seguridadService.encriptar("Sodimac123*"));
        return dataSource;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManagerFiscal(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationFiscal() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}