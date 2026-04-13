package com.sodimac.rebates.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Conexion {

	@Value("${spring.datasource.driver-class-name}")
	String driver;
	@Value("${spring.datasource.url}")
	String url;
	@Value("${spring.datasource.username}")
	String user;
	@Value("${spring.datasource.password}")
	String pass;
	
	public Connection abreConexion() {
		Connection connection = null;
		try {
			Class.forName(driver).newInstance();
			connection = DriverManager.getConnection(url,user,pass);
			System.out.println(driver);
			System.out.println(url);
			System.out.println(user);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return connection;	
	}
	
	public void cierraConexion(Connection con) {
		try {
			con.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
