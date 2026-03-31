package com.sodimac.bctfacturacion;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class FechaTest {

	private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
	private DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
	
	
	@Test
	public void test() {
		
		LocalDateTime localDateTimeByString = this.getLocalDateTimeByString();
		System.out.println(localDateTimeByString);
		
		String fechaSiguienteHabil = format.format(new Date()) + " 00:00";
		LocalDateTime  fechaSiguiente = LocalDateTime.parse(fechaSiguienteHabil , formatoFecha);
		LocalDateTime  fechaActual = LocalDateTime.now();
		
		System.out.println(fechaSiguiente);
		System.out.println(fechaActual);
		
	}
	
	public LocalDateTime getLocalDateTimeByString() {
		
		Date date = Calendar.getInstance().getTime();  
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		String fechaCreacion = dateFormat.format(date) +" 00:00" ;  
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return LocalDateTime.parse(fechaCreacion, formatter);
	}

}
