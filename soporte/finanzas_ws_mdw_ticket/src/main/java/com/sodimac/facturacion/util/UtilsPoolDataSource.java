package com.sodimac.facturacion.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.lib.cifrado.service.CryptoService;
import com.sodimac.lib.cifrado.service.CryptoServiceImpl;

public class UtilsPoolDataSource {

	private static Logger logger = LoggerFactory.getLogger(UtilsPoolDataSource.class);

	public static DataSource buildPoolDataSource(String propertiesConexion) {
		Properties prop = new Properties();

		// Extraer el tipo de conexión de forma segura desde el nombre del archivo
		// Ejemplo: "databaseBct.properties" -> "bct"
		String tipoConexion = extractConnectionType(propertiesConexion);

		// Usar try-with-resources para cerrar automáticamente el InputStream
		try (InputStream is = UtilsPoolDataSource.class.getClassLoader().getResourceAsStream(propertiesConexion)) {
			if (is == null) {
				String error = String.format("No se pudo encontrar el archivo de propiedades: %s", propertiesConexion);
				logger.error(error);
				throw new IllegalArgumentException(error);
			}
			prop.load(is);
		} catch (IOException e) {
			logger.error("Error al cargar el archivo de propiedades: " + propertiesConexion, e);
			throw new RuntimeException("No se pudo cargar la configuración de base de datos", e);
		}

		CryptoService crypto = new CryptoServiceImpl(UtilsApi.getPathCifradoProperties());

        PoolProperties p = new PoolProperties();

        // Configurar propiedades JDBC con validación
        String driverClassName = getRequiredProperty(prop, "jdbc." + tipoConexion + ".driverClassName");
        p.setDriverClassName(driverClassName);

        try {
        	String encryptedUrl = getRequiredProperty(prop, "jdbc." + tipoConexion + ".url");
        	String encryptedUser = getRequiredProperty(prop, "jdbc." + tipoConexion + ".user");
        	String encryptedPass = getRequiredProperty(prop, "jdbc." + tipoConexion + ".pass");

			p.setUrl(crypto.decrypt(encryptedUrl));
	        p.setUsername(crypto.decrypt(encryptedUser));
	        p.setPassword(crypto.decrypt(encryptedPass));
	        
	        String url = "jdbc:oracle:thin:@10.222.109.24:1541:arsmxts";
	        String urlEncriptada = crypto.encrypt(url);
	        logger.info("url: " + url);
	        logger.info("urlEncriptada: " + urlEncriptada);
	        
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException | UnsupportedEncodingException e) {
			logger.error("Error al descifrar las credenciales de base de datos", e);
			throw new RuntimeException("No se pudieron descifrar las credenciales de base de datos", e);
		}

        // Configurar propiedades del pool con valores por defecto si no existen
        p.setJmxEnabled(getBooleanProperty(prop, "pool." + tipoConexion + ".jmxEnabled", true));
        p.setTestWhileIdle(getBooleanProperty(prop, "pool." + tipoConexion + ".testWhileIdle", false));
        p.setTestOnBorrow(getBooleanProperty(prop, "pool." + tipoConexion + ".testOnBorrow", true));
        p.setValidationQuery(getProperty(prop, "pool." + tipoConexion + ".validationQuery", "SELECT 1"));
        p.setTestOnReturn(getBooleanProperty(prop, "pool." + tipoConexion + ".testOnReturn", false));
        p.setValidationInterval(getLongProperty(prop, "pool." + tipoConexion + ".validationInterval", 30000L));
        p.setTimeBetweenEvictionRunsMillis(getIntProperty(prop, "pool." + tipoConexion + ".timeBetweenEvictionRunsMillis", 30000));
        p.setMaxActive(getIntProperty(prop, "pool." + tipoConexion + ".maxActive", 100));
        p.setInitialSize(getIntProperty(prop, "pool." + tipoConexion + ".initialSize", 10));
        p.setMaxWait(getIntProperty(prop, "pool." + tipoConexion + ".maxWait", 10000));
        p.setRemoveAbandonedTimeout(getIntProperty(prop, "pool." + tipoConexion + ".removeAbandonedTimeout", 300));
        p.setMinEvictableIdleTimeMillis(getIntProperty(prop, "pool." + tipoConexion + ".minEvictableIdleTimeMillis", 30000));
        p.setMinIdle(getIntProperty(prop, "pool." + tipoConexion + ".minIdle", 10));
        p.setLogAbandoned(getBooleanProperty(prop, "pool." + tipoConexion + ".logAbandoned", true));
        p.setRemoveAbandoned(getBooleanProperty(prop, "pool." + tipoConexion + ".removeAbandoned", true));
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"+
          "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);

		return datasource;
	}

	/**
	 * Extrae el tipo de conexión del nombre del archivo de propiedades.
	 * Ejemplo: "databaseBct.properties" -> "bct"
	 * Ejemplo: "databaseBct-dev.properties" -> "bct"
	 * Ejemplo: "databaseBct-prod.properties" -> "bct"
	 */
	private static String extractConnectionType(String propertiesFileName) {
		if (propertiesFileName == null || propertiesFileName.isEmpty()) {
			throw new IllegalArgumentException("El nombre del archivo de propiedades no puede ser nulo o vacío");
		}

		// Buscar el patrón "database{TIPO}.properties" o "database{TIPO}-{perfil}.properties"
		if (propertiesFileName.startsWith("database") && propertiesFileName.endsWith(".properties")) {
			// Extraer la parte entre "database" y ".properties"
			String tipo = propertiesFileName.substring(8, propertiesFileName.length() - 11);
			if (tipo.isEmpty()) {
				throw new IllegalArgumentException("No se pudo extraer el tipo de conexión del archivo: " + propertiesFileName);
			}
			// Si contiene guión (perfil), extraer solo la parte antes del guión
			if (tipo.contains("-")) {
				tipo = tipo.substring(0, tipo.indexOf("-"));
			}
			return tipo.toLowerCase();
		}

		throw new IllegalArgumentException(
			"El nombre del archivo debe seguir el formato 'database{TIPO}.properties'. Recibido: " + propertiesFileName
		);
	}

	/**
	 * Obtiene una propiedad requerida o lanza excepción si no existe
	 */
	private static String getRequiredProperty(Properties prop, String key) {
		String value = prop.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Propiedad requerida no encontrada o vacía: " + key);
		}
		return value.trim();
	}

	/**
	 * Obtiene una propiedad String con valor por defecto
	 */
	private static String getProperty(Properties prop, String key, String defaultValue) {
		String value = prop.getProperty(key);
		return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
	}

	/**
	 * Obtiene una propiedad int con valor por defecto
	 */
	private static int getIntProperty(Properties prop, String key, int defaultValue) {
		String value = prop.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			logger.warn("Valor inválido para propiedad '{}': '{}'. Usando valor por defecto: {}", key, value, defaultValue);
			return defaultValue;
		}
	}

	/**
	 * Obtiene una propiedad long con valor por defecto
	 */
	private static long getLongProperty(Properties prop, String key, long defaultValue) {
		String value = prop.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			logger.warn("Valor inválido para propiedad '{}': '{}'. Usando valor por defecto: {}", key, value, defaultValue);
			return defaultValue;
		}
	}

	/**
	 * Obtiene una propiedad boolean con valor por defecto
	 */
	private static boolean getBooleanProperty(Properties prop, String key, boolean defaultValue) {
		String value = prop.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value.trim());
	}
}
