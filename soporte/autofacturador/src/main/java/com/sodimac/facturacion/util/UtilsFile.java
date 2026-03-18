package com.sodimac.facturacion.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class UtilsFile {
	
	public static void writeToZipFile(File file, ZipOutputStream zipStream) throws FileNotFoundException, IOException {

		String path = file.getPath();
		String name = file.getName();
		File aFile = new File(path);
		FileInputStream fis = new FileInputStream(aFile);
		ZipEntry zipEntry = new ZipEntry(name);
		zipStream.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024 * 20];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipStream.write(bytes, 0, length);
		}

		zipStream.closeEntry();
		fis.close();
	}
	
	public static Document ObtenerDocumentXml (String xml) {
		Document document = null;
		
		try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xml));
	        document = documentBuilder.parse(is);
			
        } catch (Exception e) {
            e.printStackTrace();
        }
		
        return document;		
	}
	
	public static boolean EliminarArchivo (String path) {
		File archivo = new File(path);
		if (archivo.exists()) {
			archivo.delete();
			return true;
		}
		
		return false;
	}

}
