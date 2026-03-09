package com.sodimac.cfdi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.bouncycastle.crypto.DataLengthException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.lib.crypto.service.CryptoService;
import com.sodimac.lib.crypto.service.CryptoServiceImpl;

public class UtilsConexion {

	private static Logger logger = LoggerFactory.getLogger(UtilsConexion.class);
	private static EntityManagerFactory factory;
	
	public static EntityManagerFactory buildFactory(String propertiesConexion) {
		Properties prop = new Properties();
		Map<String, String> settings = new HashMap<>();
		
		String tipoConexion = propertiesConexion.substring(8, 11).toLowerCase().replace(".", "");
		InputStream is = UtilsConexion.class.getClassLoader().getResourceAsStream(propertiesConexion);
		try {
			prop.load(is);
		} catch (IOException e) {
			logger.error("BuildFactory1: ", e);
		}

		CryptoService crypto = new CryptoServiceImpl(UtilsApi.getPathCifradoProperties());
		
		settings.put("hibernate.dialect", prop.getProperty("hibernate." + tipoConexion + ".dialect"));
		settings.put("hibernate.show_sql", prop.getProperty("hibernate." + tipoConexion + ".show_sql"));
		settings.put("hibernate.hbm2ddl.auto", prop.getProperty("hibernate." + tipoConexion + ".hbm2ddl.auto"));
		settings.put("hibernate.cache.use_second_level_cache", prop.getProperty("hibernate." + tipoConexion + ".cache.use_second_level_cache"));
		settings.put("hibernate.cache.use_query_cache", prop.getProperty("hibernate." + tipoConexion + ".cache.use_query_cache"));
		
		settings.put("hibernate.c3p0.min_size", prop.getProperty("c3p0." + tipoConexion + ".min_size"));
		settings.put("hibernate.c3p0.max_size", prop.getProperty("c3p0." + tipoConexion + ".max_size"));
		settings.put("hibernate.c3p0.acquire_increment", prop.getProperty("c3p0." + tipoConexion + ".acquire_increment"));
		settings.put("hibernate.c3p0.timeout", prop.getProperty("c3p0." + tipoConexion + ".timeout"));
		settings.put("hibernate.c3p0.testConnectionOnCheckout", prop.getProperty("c3p0." + tipoConexion + ".testConnectionOnCheckout"));
		settings.put("hibernate.c3p0.testConnectionOnCheckin", prop.getProperty("c3p0." + tipoConexion + ".testConnectionOnCheckin"));
		settings.put("hibernate.c3p0.preferredTestQuery", prop.getProperty("c3p0." + tipoConexion + ".preferredTestQuery"));
		settings.put("hibernate.c3p0.idleConnectionTestPeriod", prop.getProperty("c3p0." + tipoConexion + ".idleConnectionTestPeriod"));
		settings.put("hibernate.c3p0.privilegeSpawnedThreads", prop.getProperty("c3p0." + tipoConexion + ".privilegeSpawnedThreads"));
		settings.put("hibernate.c3p0.contextClassLoaderSource", prop.getProperty("c3p0." + tipoConexion + ".contextClassLoaderSource"));
		
		settings.put("hibernate.connection.driver_class", prop.getProperty("jdbc." + tipoConexion + ".driverClassName"));
		
		try {
			settings.put("hibernate.connection.url", crypto.decrypt(prop.getProperty("jdbc." + tipoConexion + ".url")));
			settings.put("hibernate.connection.username", crypto.decrypt(prop.getProperty("jdbc." + tipoConexion + ".user")));
			settings.put("hibernate.connection.password", crypto.decrypt(prop.getProperty("jdbc." + tipoConexion + ".pass")));
		} catch (DataLengthException | IllegalStateException e) {
			logger.error("BuildFactory2: ", e);
		}
		  
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings).build();
		MetadataSources metadataSources = new MetadataSources(serviceRegistry);
		Metadata metadata = metadataSources.buildMetadata();
		factory = metadata.getSessionFactoryBuilder().build();
		
		return factory;
		
	}	
	
}
