package com.sodimac.cfdi.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class UtilsTextos {

	public static String truncarDecimales (float numero, int decimales, String format) {
    	NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
    	nf.setMaximumFractionDigits(decimales);
    	nf.setRoundingMode(RoundingMode.FLOOR);
    	DecimalFormat df = (DecimalFormat)nf;
    	df.applyPattern(format);
    	String result = df.format(numero);
    	
		return result;
	}

}
