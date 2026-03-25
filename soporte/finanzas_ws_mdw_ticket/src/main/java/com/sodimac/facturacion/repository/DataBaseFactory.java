package com.sodimac.facturacion.repository;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.facturacion.pool.DataSourceBct;

public final class DataBaseFactory {

	private static Logger logger = LoggerFactory.getLogger(DataBaseFactory.class);

	public static Connection getConn() {

		int contador = 0;
		int reintentos = 7;
		boolean primeraVez = true;
		Connection conn = null;
		DataSource ds = new DataSourceBct().getDataSource();

		do {
			try {
				if (primeraVez) {
					primeraVez = false;
				} else {
					Thread.sleep(1 * 200);
					logger.debug("Lee intento: " + contador);
				}
				conn = ds.getConnection();

			} catch (SQLException e) {
				logger.error(String.format("getConn SQL State: %s Mensaje: %s", e.getSQLState(), e.getMessage()));
			} catch (Exception e) {
				logger.error("getConn: ", e);
			}
			contador += 1;

		} while (conn == null && contador <= reintentos);

		return conn;
	}

	public static void close(Connection conn) {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error(String.format("close SQL State: %s Mensaje: %s", e.getSQLState(), e.getMessage()));
		}
	}

}
