package com.sodimac.cfdi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.web.multipart.MultipartFile;
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
	
	public static boolean EliminarArchivos(String dir, String extencion, String iniciaCon) {
		Set<String> archivos = UtilsFile.listarArchivos(dir, extencion);
		Boolean ban;
		
		for (String value : archivos) {
			if (iniciaCon != "")
				ban = !value.contains(iniciaCon);
			else
				ban = true;

			if (ban) {

				File archivo = new File(dir + value);
				if (archivo.exists()) {
					archivo.delete();
				}
			}
		}
		return true;
	}

	public static Set<String> listarArchivos(String dir, String extencion) {
		FilenameFilter filter = (dir1, name) -> name.endsWith(extencion);
		// FilenameFilter filter = (dir1, name) -> name.endsWith(".xml");

		return Stream.of(new File(dir).listFiles(filter)).filter(file -> !file.isDirectory()).map(File::getName)
				.collect(Collectors.toSet());
	}
	
	public static boolean crearArchivoB64(String fileName, String pdfB64) {

		File fileNamePdf = new File(fileName);

		try (FileOutputStream fos = new FileOutputStream(fileNamePdf);) {
			byte[] decoder = Base64.getDecoder().decode(pdfB64);
			fos.write(decoder);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean crearArchivo(String fileName, String contenido) {

		File file = new File(fileName);

		try (FileOutputStream fos = new FileOutputStream(file);) {
			byte[] decoder = contenido.getBytes();
			fos.write(decoder);
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
		return true;
	}
	
	public static File saveFile(MultipartFile multiPart, String tmpdir, String documento)
			throws IllegalStateException, IOException {
		String path = tmpdir + documento;
		File localFile;

		try {
			localFile = new File(path);
			multiPart.transferTo(localFile);
		} catch (Exception ex) {
			System.out.println("Error: " + ex.getMessage());
			return null;
		}
		return localFile;
	}
	
}
