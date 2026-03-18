package com.sodimac.facturacion.util;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.internet.MimeUtility;

import com.sodimac.facturacion.entity.CatActividadesEntity;

public class UtilsString {
	
	public static String emailMask (String mail) {
		String mask = "";
		int indexArroba = mail.indexOf("@");
		int posfinCorreo1Parte = 3;
		if (posfinCorreo1Parte > indexArroba) posfinCorreo1Parte = indexArroba;
		String uno = mail.substring(0, posfinCorreo1Parte);
		String dos = "";
		if (posfinCorreo1Parte < indexArroba) dos = mail.substring(posfinCorreo1Parte, indexArroba);
	    int posFinal = mail.length();
	    int indexPunto = mail.lastIndexOf(".");
	    int posfinDominio1Parte = indexArroba + 4; 
	    if (posfinDominio1Parte > indexPunto) posfinDominio1Parte = indexPunto;
	    String tres = mail.substring(indexArroba+1, posfinDominio1Parte);
	    String cuatro = "";
	    if (posfinDominio1Parte < indexPunto) cuatro = mail.substring(posfinDominio1Parte, indexPunto);
	    String finalMail= mail.substring(indexPunto, posFinal);
	    dos = repetirCaracter(dos, "•");
	    cuatro = repetirCaracter(cuatro, "•");
	    mask =uno + dos + "@" + tres + cuatro + finalMail;
		return mask;
	}

	public static String repetirCaracter (String cadena, String caracter) {
		String result = "";

		for (int i = 0; i < cadena.length(); i++) {
		    result += caracter;
		}
		return	result;
	}

	public static String formatearFecha (String fecha, String formato) {
		String result = "";
		
		String anio = fecha.substring(0, 4);
		String mes = fecha.substring(5, 7);
		String dia = fecha.substring(8, 10);
		
		if (formato.equals("ddmmaaaa")) {
			result = dia + mes + anio;
		}

		return	result;
	}
	
	public static String encodeText (String texto) {
		String result = "";
		
		try {
			result = MimeUtility.encodeText(texto,"UTF-8","B");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return	result;
	}
	
	public static String parseActivityDesc(int idActividad, List<String> textoArr, CatActividadesEntity catActividadesEntity) {
		
		String descripcion = catActividadesEntity.getDescripcion();
		
		
		if (textoArr != null) {
			for (int i = 0; i < textoArr.size(); i++) {
				int firstIdx = descripcion.indexOf("{");
				int lastIdx = descripcion.indexOf("}");
				String pattern = descripcion.substring(firstIdx, lastIdx + 1);
				descripcion = descripcion.replace(pattern, textoArr.get(i));
			}
			
		}


		
			
		return descripcion;
		
	}
}
