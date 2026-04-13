package com.sodimac.rebates.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sodimac.rebates.model.Documento;

public class Util {

	public File saveFile(MultipartFile multiPart, String ruta, Documento documento, Integer idPeriodo)
			throws IllegalStateException, IOException, JSchException, SftpException {

		String extension = "." + FilenameUtils.getExtension(multiPart.getOriginalFilename());
		Date date = new Date();
		SimpleDateFormat formatterDateComplete = new SimpleDateFormat("ddMMyyyy_HHmm");
		// SimpleDateFormat formatterDateMMYYY = new SimpleDateFormat("MMMMyyyy");
		String strDateComplete = formatterDateComplete.format(date);
		// String strDateMesAnio = formatterDateMMYYY.format(date);
		String path = "";

		path = ruta + idPeriodo + "_" + documento.getNomenclatura() + "_" + strDateComplete + extension;

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

	public static String getPathCifradoProperties() {

		String resourceName = "cifrado.properties";
		String absolutePath = "";

		File file = new File(Util.class.getClassLoader().getResource(resourceName).getFile());

		try {

			absolutePath = URLDecoder.decode(file.getAbsolutePath(), "UTF-8");

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}

		return absolutePath;
	}

}
