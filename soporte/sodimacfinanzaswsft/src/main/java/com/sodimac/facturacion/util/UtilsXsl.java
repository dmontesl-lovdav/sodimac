package com.sodimac.facturacion.util;

public final class UtilsXsl {
	
	public static final String toLowerCase(String str) {
		return str.toLowerCase();
	}
	
	public static final String toUpperCase(String str) {
		return str.toUpperCase();
	}
	
	public static final String toDescConcept(String str) {
		StringBuilder sbStr = new StringBuilder("");
		if (str != null) {
			if (str.length() == 0) {
				sbStr.append(str.toUpperCase());
			} else {
				if(str.length() > 21) {
					str = str.substring(0,20);
				}
				
				sbStr.append(str.substring(0,1).toUpperCase())
				     .append(str.substring(1,str.length()).toLowerCase());
			}
		}
		return sbStr.toString();
	}
	
	public static final String toCamelCase(String str) {
		StringBuilder sbStr = new StringBuilder("");
		if (str != null) {
			if (str.length() == 0) {
				sbStr.append(str.toUpperCase());
			} else {
				sbStr.append(str.substring(0,1).toUpperCase())
				     .append(str.substring(1,str.length()).toLowerCase());
			}
		}
		return sbStr.toString();
	}
	
	public static void main(String args[]) {
		String sello = "MONOMLAVALTOVI";
		String concat = toCamelCase(sello);
		System.out.println(concat);
		
	}
}
