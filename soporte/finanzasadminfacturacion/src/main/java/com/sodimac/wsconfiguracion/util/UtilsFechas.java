package com.sodimac.wsconfiguracion.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;

public class UtilsFechas {
	
	public static String formatear (Date fecha, String formato) {
		SimpleDateFormat sdf = new SimpleDateFormat(formato);
		return sdf.format(fecha);
	}
	
	public static String sumarMeses(String date, String formatoOrigen, int meses, String formatoDestino) 
			  throws ParseException {
			 
		SimpleDateFormat sdfori
		  = new SimpleDateFormat(formatoOrigen);
		SimpleDateFormat sdfdes
		  = new SimpleDateFormat(formatoDestino);
		Date incrementedDate = DateUtils
		  .addMonths(sdfori.parse(date), meses);
		return sdfdes.format(incrementedDate);
	}
	
	public static Date convertirDate (String fecha, String formato) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(formato);
		sdf.setLenient(false);
		return sdf.parse(fecha);
	}

	public static boolean validarExpresionRegular(String fecha) {
		boolean result = true;
		
		fecha = fecha.trim();
		
		if (fecha.isEmpty() || fecha.length()!= 10) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile("^[0-3][0-9]-[0-1][0-9]-[2][0][0-9][0-9]$");
	        Matcher mat = pat.matcher(fecha);
	        if (!mat.matches()) {
	        	result = false;
	        }	        
		}
		
		return result;
	}
	
	public static LocalDate getLocalDate() {
	    return LocalDate.now();
	}
	
}