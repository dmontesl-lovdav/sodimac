package com.sodimac.facturacion.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsNumber {

	public static boolean isNumeric(String cadena) {
		Pattern pat = Pattern.compile("\\d*");
		Matcher mat = pat.matcher(cadena);
		if (mat.matches()) {
			return true;
		} else {
			return false;
		}
	}
}
