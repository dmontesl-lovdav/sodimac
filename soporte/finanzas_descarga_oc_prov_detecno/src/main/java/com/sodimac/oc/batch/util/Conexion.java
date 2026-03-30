package com.sodimac.oc.batch.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Conexion {

	@Autowired
	private Environment env;

	public Connection getConexion() {
		Connection conn = null;
		try {
			String driver = this.env.getProperty("spring.datasource.driver-class-name");
			String url = this.env.getProperty("spring.datasource.url");
			String usr = this.env.getProperty("spring.datasource.username");
			String pass = this.env.getProperty("spring.datasource.password");
			Class.forName(driver);
			conn = DriverManager.getConnection(url, usr, pass);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public void close(Connection c) {
		try {
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
