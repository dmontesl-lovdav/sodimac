package com.sodimac.wsconfiguracion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class Convert {

	public static String fileToB64 (String file) {
        File f = new File(file);
        try {
            FileInputStream fin = new FileInputStream(f);
            byte fileContent[] = new byte[(int)f.length()];
            fin.read(fileContent);
            fin.close();
            return toB64(fileContent);

        } catch (IOException ex) {
            System.out.println("Error al cargar archivo: " + file);
            return "";
        }
    }
   public static String toB64(byte[] data){
	   Base64.Encoder encoder = Base64.getEncoder();
       String b = encoder.encodeToString(data);	   
	      return b;
	   }

}
