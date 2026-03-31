package com.sodimac.bctfacturacion.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FechaUtils {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private FechaUtils() {
		super();
	}
	
	public static Date getDate() {
		return new Date();
	}
	
	public static String getStrLong(Date date) {
		return sdfLong.format(date);
	}
	
	public static String getStrShort(Date date) {
		return sdf.format(date);
	}
	
	public static Date getDateLong(String strDate) throws ParseException {
		return sdfLong.parse(strDate);
	}
	
	public static Date getDateShort(String strDate) throws ParseException {
		return sdf.parse(strDate);
	}
}
