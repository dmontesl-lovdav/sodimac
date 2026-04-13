package com.sodimac.rebates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.jupiter.api.Test;

public class FufTest {

	private static SimpleDateFormat sdfYYYMMDD = new SimpleDateFormat("yyyyMMdd");
	
	@Test
	public void test() throws ParseException {
		String liquidacionOriginal = "P01";
		String ful = "20240221G104001-F30182";
		String subcuenta = ful.substring(8,15);
		String fecha = ful.substring(0,8);
		System.out.println("subcuenta:" + subcuenta);
		System.out.println("fecha:" + fecha); 
		
		int contador=0;
		Calendar cal = Calendar.getInstance();
		cal.setTime( sdfYYYMMDD.parse(fecha) );
		
		String fuf = null;
		while(contador <= 1000) {
			String fechaDinamica = sdfYYYMMDD.format( cal.getTime() );
			fuf = fechaDinamica + subcuenta + liquidacionOriginal;
			System.out.println("fuf:" + fuf);
			
			cal.add(Calendar.DATE, -1);
			
			if (contador ==5 ) {
				break;
			}
			
			contador ++;
		}
		
		System.out.println("FUF FINAL:" + fuf);
	}

}
