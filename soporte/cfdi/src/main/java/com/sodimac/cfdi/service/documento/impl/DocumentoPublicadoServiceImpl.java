package com.sodimac.cfdi.service.documento.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.monitorjbl.xlsx.StreamingReader;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.sodimac.cfdi.entity.fiscal.DocumentoPublicadoEntity;
import com.sodimac.cfdi.models.documento.CatDocumentoCabeceraDetModel;
import com.sodimac.cfdi.models.documento.CatDocumentoCabeceraModel;
import com.sodimac.cfdi.models.documento.CatDocumentoConfModel;
import com.sodimac.cfdi.models.documento.ConfiguracionFtpModel;
import com.sodimac.cfdi.models.documento.DocumentoPublicadoModel;
import com.sodimac.cfdi.models.documento.DocumentoValidadorModel;
import com.sodimac.cfdi.models.documento.Generic;
import com.sodimac.cfdi.repository.fiscal.documento.DocumentoPublicadoRepository;
import com.sodimac.cfdi.service.CatMensajesService;
import com.sodimac.cfdi.service.documento.CatDocumentoCabeceraDetService;
import com.sodimac.cfdi.service.documento.CatDocumentoCabeceraService;
import com.sodimac.cfdi.service.documento.CatDocumentoConfService;
import com.sodimac.cfdi.service.documento.ConfiguracionFtpService;
import com.sodimac.cfdi.service.documento.DocumentoPublicadoService;
import com.sodimac.cfdi.util.MemoryUtil;
import com.sodimac.cfdi.util.UtilsFile;
import com.sodimac.cfdi.util.enums.EEstatusDocumento;
import com.sodimac.cfdi.util.enums.EExtensionDocumento;
import com.sodimac.cfdi.util.enums.EMensajes;
import com.sodimac.cfdi.util.enums.EMensajesArchivos;

@Service
public class DocumentoPublicadoServiceImpl implements DocumentoPublicadoService {
	
	private static Logger logger = LoggerFactory.getLogger(DocumentoPublicadoServiceImpl.class);
	private static int ESTATUS_ACTIVO = 1;
	private DecimalFormat formatter = new DecimalFormat("###,###.##");
	
	private static final int INDEX_ID_DOCUMENTO_PUBLICADO 	= 0;
	private static final int INDEX_ID_ESTATUS_DOCUMENTO 	= 1;
	private static final int INDEX_ID_DOCUMENTO_CONF 		= 2;
	private static final int INDEX_ID_MENSAJE 				= 3;
	private static final int INDEX_NOMBRE_ARCHIVO 			= 4;
	private static final int INDEX_NUMERO_REGISTROS 		= 5;
	private static final int INDEX_PESO 					= 6;
	private static final int INDEX_ESTATUS 					= 7;
	private static final int INDEX_FECHA_CREACION 			= 8;
	private static final int INDEX_FECHA_PUBLICACION 		= 9;
	private static final int INDEX_FECHA_ACTUALIZACION 		= 10;
	private static final int INDEX_USUARIO_CREACION 		= 11;
	private static final int INDEX_MENSAJE_DESCRIPCION 		= 12;
	private static final int INDEX_TIPO_DOCUMENTO 			= 13;
	private static final int INDEX_ESTATUS_DOCUMENTO 		= 14;
	
	@Autowired
	private CatDocumentoConfService catDocumentoConfService;
	
	@Autowired
	private ConfiguracionFtpService configuracionFtpService;
	
	@Autowired
	private DocumentoPublicadoRepository publicadoRepository;
	
	@Autowired
	private DocumentoPublicadoRepository documentoPublicadoRepository;
	
	@Autowired
	private CatDocumentoCabeceraService cabeceraService;
	
	@Autowired
	private CatDocumentoCabeceraDetService cabeceraDetService;
	
	@Autowired
	private CatMensajesService catMensajesService;
	
	@Override
	public List<DocumentoPublicadoModel> getDocumentosPublicados(String pfechaInicial, String pfechafinal, Integer pIdTipoDocumento) {
		List<DocumentoPublicadoModel> listDoc = new ArrayList<>();
		
		List<Object[]> listPublicados = this.documentoPublicadoRepository.getDocumentosPublicados(pfechaInicial, pfechafinal, pIdTipoDocumento);
		if (listPublicados != null) {
			for (Object[] object : listPublicados) {
				
				Integer idDocumentoPublicado = (object[INDEX_ID_DOCUMENTO_PUBLICADO] != null) ? Integer.valueOf(object[INDEX_ID_DOCUMENTO_PUBLICADO].toString()) : null;
				Integer idEstatusDocumento = (object[INDEX_ID_ESTATUS_DOCUMENTO] != null) ? Integer.valueOf(object[INDEX_ID_ESTATUS_DOCUMENTO].toString()) : null;
				Integer idDocumentoConf = (object[INDEX_ID_DOCUMENTO_CONF] != null) ? Integer.valueOf(object[INDEX_ID_DOCUMENTO_CONF].toString()) : null;
				Integer idMensaje = (object[INDEX_ID_MENSAJE] != null) ? Integer.valueOf(object[INDEX_ID_MENSAJE].toString()) : null;
				String nombreArchivo = (object[INDEX_NOMBRE_ARCHIVO] != null) ? object[INDEX_NOMBRE_ARCHIVO].toString() : null;
				Integer numeroRegistros = (object[INDEX_NUMERO_REGISTROS] != null) ? Integer.valueOf(object[INDEX_NUMERO_REGISTROS].toString()) : null;
				String peso = (object[INDEX_PESO] != null) ? object[INDEX_PESO].toString() : null;
				Integer estatus = (object[INDEX_ESTATUS] != null) ? Integer.valueOf(object[INDEX_ESTATUS].toString()) : null;
				String fechaCreacion = (object[INDEX_FECHA_CREACION] != null) ? object[INDEX_FECHA_CREACION].toString() : null;
				String fechaPublicacion = (object[INDEX_FECHA_PUBLICACION] != null) ? object[INDEX_FECHA_PUBLICACION].toString() : null;
				String fechaActualizacion = (object[INDEX_FECHA_ACTUALIZACION] != null) ? object[INDEX_FECHA_ACTUALIZACION].toString() : null;
				Integer idUsuarioCreacion = (object[INDEX_USUARIO_CREACION] != null) ? Integer.valueOf(object[INDEX_USUARIO_CREACION].toString()) : null;
				String mensajeDescripcion = (object[INDEX_MENSAJE_DESCRIPCION] != null) ? object[INDEX_MENSAJE_DESCRIPCION].toString() : null;
				String tipoDocumento = (object[INDEX_TIPO_DOCUMENTO] != null) ? object[INDEX_TIPO_DOCUMENTO].toString() : null;
				String estatusDocumento = (object[INDEX_ESTATUS_DOCUMENTO] != null) ? object[INDEX_ESTATUS_DOCUMENTO].toString() : null;
				
				DocumentoPublicadoModel model = new DocumentoPublicadoModel();
				model.setIdDocumentoPublicado(idDocumentoPublicado);
				model.setIdEstatusDocumento(idEstatusDocumento);
				model.setIdDocumentoConf(idDocumentoConf);
				model.setIdMensaje(idMensaje);
				model.setNombreArchivo(nombreArchivo);
				model.setNumeroRegistros(numeroRegistros);
				model.setNumeroRegistrosStr( this.formatter.format(numeroRegistros));
				model.setPeso(peso);
				model.setEstatus(estatus);
				model.setFechaCreacion(fechaCreacion);
				model.setFechaPublicacion(fechaPublicacion);
				model.setFechaActualizacion(fechaActualizacion);
				model.setIdUsuarioCreacion(idUsuarioCreacion);
				model.setMensajeDescripcion(mensajeDescripcion);
				model.setTipoDocumento(tipoDocumento);
				model.setEstatusDocumento(estatusDocumento);
				
				listDoc.add(model);;
			}
		}
		return listDoc;
	}
	
	@Override
	public Generic createDocument(MultipartFile multiPart, Integer idTipoDocumento, Integer idUser) {
		Generic response = new Generic();
		response.setStatus("ERROR");
		CatDocumentoConfModel catDocumentoConf = this.catDocumentoConfService.getCatDocumentoConf(idTipoDocumento);
		
		String sftpPath = catDocumentoConf.getRutaDeposito();
		String nameFile = multiPart.getOriginalFilename();
		String extension = FilenameUtils.getExtension(multiPart.getOriginalFilename());
		
		File uploadFile = null;
		String tmpdir = System.getProperty("java.io.tmpdir");
		
		if (!multiPart.isEmpty()) {
			
			if (!extension.equalsIgnoreCase( catDocumentoConf.getExtension() )) {
				String mensaje = this.catMensajesService.get( EMensajesArchivos.EXTENSION_INCORRECTA.getId() ).getDescripcionMensaje();
				response.setStatus("ERROR");
				response.setMessage(mensaje);
				return response;
			}
			
			DocumentoValidadorModel readFile = null;
			
			 Double bytes = multiPart.getSize() * 1.0;
	         Double kilobytes = (bytes / 1024);
	         Double megabytes = (kilobytes / 1024);
	         
	         logger.info("Tamaño del archivo en bytes: " + bytes);
	         logger.info("Tamaño del archivo en kilobytes: " + kilobytes.toString());
	         logger.info("Tamaño del archivo en megabytes: " + megabytes);
			
			if (extension.equalsIgnoreCase(EExtensionDocumento.CSV.toString())) {
				MemoryUtil.showMemoryStats();
				logger.info("Archivo CSV");
				readFile = this.readCsv(multiPart, idTipoDocumento, catDocumentoConf.getIdDocumentoCabecera());
			} else if (extension.equalsIgnoreCase(EExtensionDocumento.XLSX.toString())) {
				logger.info("Archivo XLSX");
				MemoryUtil.showMemoryStats();
				readFile = this.readExcel(multiPart, idTipoDocumento, catDocumentoConf.getIdDocumentoCabecera());
			} else {
				String mensaje = this.catMensajesService.get( EMensajesArchivos.ERROR_CARGAR_ARCHIVO.getId() ).getDescripcionMensaje();
				response.setStatus("ERROR");
				response.setMessage(mensaje);
				return response;
			}
			
			if (readFile.getStatus().equals("ERROR")) {
				response.setStatus("ERROR");
				response.setMessage(readFile.getMessage());
				return response;
			}

			try {
				logger.info("Guardando documento en tmp: " + tmpdir + nameFile);
				uploadFile = UtilsFile.saveFile(multiPart, tmpdir, nameFile);
				logger.info("Archivo guardado en TMP");
			} catch (Exception ex_) {
				String mensaje = this.catMensajesService.get( EMensajesArchivos.ERROR_CARGAR_SFTP.getId() ).getDescripcionMensaje();
				response.setStatus("ERROR");
				response.setMessage(mensaje);
				System.out.println("Ocurrió un error: " + ex_.getMessage());
				return response;
			}

			if (uploadFile != null) {
				try {
					
					ConfiguracionFtpModel configuracionFtp = this.configuracionFtpService.getConfiguracion( catDocumentoConf.getIdConfiguracionFtp() );
					boolean sendSFT = this.sendSFT(configuracionFtp, sftpPath, nameFile, uploadFile);
					uploadFile.delete();
					
					if (sendSFT) {
						logger.info("El archivo se cargo correctamente: " + sftpPath + nameFile);
						DocumentoPublicadoEntity documento = new DocumentoPublicadoEntity();
						documento.setIdDocumentoConf( catDocumentoConf.getIdDocumentoConf() );
						documento.setIdMensaje( EMensajes.DOCUMENTO_ENVIADO_SFTP.getIdMensaje() );
						documento.setNombreArchivo( nameFile );
						documento.setNumeroRegistros( readFile.getTotalRegistros() );
						documento.setPeso( readFile.getPeso() );
						documento.setIdEstatusDocumento(EEstatusDocumento.EN_PROCESO.getIdEstatus());
						documento.setEstatus(1);
						documento.setFechaCreacion( new Date() );
						documento.setFechaPublicacion(null);
						documento.setFechaActualizacion(null);
						documento.setUsuarioCreacion( idUser );
						
						this.publicadoRepository.save(documento);
					}
				} catch (Exception ex) {
					String mensaje = this.catMensajesService.get( EMensajesArchivos.ERROR_CARGAR_SFTP.getId() ).getDescripcionMensaje();
					response.setStatus("ERROR");
					response.setMessage(mensaje);
					System.out.println("Ocurrió un error: " + ex.getMessage());
					return response;
				}
				
				String mensaje = this.catMensajesService.get( EMensajesArchivos.DOCUMENTO_CARGADO_CORRECTAMENTE.getId() ).getDescripcionMensaje();
				response.setStatus("OK");
				response.setMessage(mensaje);
				return response;
			}
		}
		return response;
	}
	
	
	@SuppressWarnings({ "deprecation", "incomplete-switch" })
	private DocumentoValidadorModel readExcel(MultipartFile multiPart, Integer idTipoDocumento,
			Integer idDocumentoCabecera) {
		DocumentoValidadorModel response = new DocumentoValidadorModel();
		response.setStatus("OK");
		logger.info("Comienza procesamiento de archivo EXCEL");
		
		String nameFile = multiPart.getOriginalFilename();
		System.out.println(nameFile);
		logger.info(nameFile);
		Workbook workbook = null;
		
		try {
			CatDocumentoCabeceraModel documentoCabecera = this.cabeceraService.getCatDocumentoCabecera(idDocumentoCabecera);
			List<CatDocumentoCabeceraDetModel> cabeceras = this.cabeceraDetService.getCabecera(idDocumentoCabecera, ESTATUS_ACTIVO);
			
			logger.info("En este punto aun no se guarda en TMP");
			
			logger.info("Creando objeto: Workbook workbook = new XSSFWorkbook(multiPart.getInputStream());");
			MemoryUtil.showMemoryStats();
			//Workbook workbook = new XSSFWorkbook(multiPart.getInputStream());
			workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(8192).open(multiPart.getInputStream());
			logger.info("Crear objeto: " + workbook);
			MemoryUtil.showMemoryStats();
			
			Integer filaComienza = documentoCabecera.getFilaComienza();
			Sheet sheet = null;
			Row rowHeader = null;
			for (Sheet sheetWb : workbook) {
				
				sheet = sheetWb;
				for(Row r : sheetWb) {
					
					if (r.getRowNum() == filaComienza.intValue()) {
						rowHeader = r;
						logger.info( ":" + rowHeader.getRowNum());
						break;
					}
				}
			}
			//Sheet sheet = workbook.getSheetAt(0);
			
			int numRows = sheet.getLastRowNum();
			logger.info("Total de registros:" + numRows);

            // Validamos que el archivo no este vacio
            if (numRows == 0) {
            	logger.info("El archivo esta vacio");
                System.out.println("El archivo esta vacio");
                
                String mensaje = this.catMensajesService.get( EMensajesArchivos.DOCUMENTO_VACIO.getId() ).getDescripcionMensaje();
                response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            if (numRows < 2) { // Que tenga más de dos filas
            	logger.info("Falta informacion");
            	System.out.println("Falta informacion");
            	
            	String mensaje = this.catMensajesService.get( EMensajesArchivos.INFORMACION_INCOMPLETA.getId() ).getDescripcionMensaje();
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            
            
            String[] headerConfig = new String[cabeceras.size()];
            int i=0;
            for (CatDocumentoCabeceraDetModel cabecera : cabeceras) {
            	headerConfig[i] = cabecera.getNombre();
            	i++;
            }
            
            //Row rowHeader = sheet.getRow(filaComienza);
            String[] headerExcel = new String[rowHeader.getLastCellNum()];
            int x=0;
            if (rowHeader != null) {
	        	for (Cell cell : rowHeader) {
	        		switch (cell.getCellTypeEnum()) {
	                    case STRING: headerExcel[x] = cell.getStringCellValue(); break;
	                    case NUMERIC: break;
	                    case BOOLEAN: break;
	                    case FORMULA: break;
	        		}
	        		x ++;
	        	}
            }
            
            logger.info("Validando cabecera");
            // Validamos el header
            if (!validHeader(headerExcel, headerConfig)) {
            	logger.info("La cabecera es inválida");
            	System.out.println("La cabecera es inválida");
            	
            	String mensaje = this.catMensajesService.get( EMensajesArchivos.CABECERA_INCORRECTA.getId() ).getDescripcionMensaje();
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            } else {
            	System.out.println("Header OK");	
            }
            
            Double bytes = multiPart.getSize() * 1.0;

            Double kilobytes = (bytes / 1024);
            Double megabytes = (kilobytes / 1024);
            
            response.setTotalRegistros( numRows );
            response.setPeso( megabytes ) ;
		} catch (Exception e) {
            e.printStackTrace();
            logger.error("Error inesperado", e);
            
            String mensaje = this.catMensajesService.get( EMensajesArchivos.INFORMACION_INCOMPLETA.getId() ).getDescripcionMensaje();
            response.setStatus("ERROR");
    		response.setMessage(mensaje);
    		return response;
        }   finally {
        	if (workbook != null) {
        		try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		return response;
	}

	public DocumentoValidadorModel readCsv(MultipartFile multiPart, Integer idTipoDocumento, Integer idDocumentoCabecera) {
		DocumentoValidadorModel response = new DocumentoValidadorModel();
		response.setStatus("OK");
		logger.info("Comienza procesamiento de archivo CSV");
		
		String nameFile = multiPart.getOriginalFilename();
		System.out.println(nameFile);
		logger.info(nameFile);
		
		try {
			
			CatDocumentoCabeceraModel documentoCabecera = this.cabeceraService.getCatDocumentoCabecera(idDocumentoCabecera);
			List<CatDocumentoCabeceraDetModel> cabeceras = this.cabeceraDetService.getCabecera(idDocumentoCabecera, ESTATUS_ACTIVO);
			
			Reader reader = new InputStreamReader(multiPart.getInputStream());
			CSVParser csvParser = new CSVParserBuilder().withSeparator(documentoCabecera.getCaracterSeparador().charAt(0) ).build(); // custom separator
			CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).build();
            List<String[]> r = csvReader.readAll();

            // Validamos que el archivo no este vacio
            if (r.isEmpty()) {
            	logger.info("El archivo esta vacio");
                System.out.println("El archivo esta vacio");
                
                String mensaje = this.catMensajesService.get( EMensajesArchivos.DOCUMENTO_VACIO.getId() ).getDescripcionMensaje();
                response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            } else {
            	//r.forEach(x -> System.out.println(Arrays.toString(x)));
            }
            
            String[] headerConfig = new String[cabeceras.size()];
            int i=0;
            for (CatDocumentoCabeceraDetModel cabecera : cabeceras) {
            	headerConfig[i] = cabecera.getNombre();
            	i++;
            }
            
            // Validamos el header
            if (!validHeader(r.get(0), headerConfig)) {
            	logger.info("La cabecera es inválida");
            	System.out.println("La cabecera es inválida");
            	
            	String mensaje = this.catMensajesService.get( EMensajesArchivos.CABECERA_INCORRECTA.getId() ).getDescripcionMensaje();
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            } else {
            	System.out.println("Header OK");	
            }
            
            // Validamos que haya más informacion que el header
            if (r.size() < 2) { // Que tenga más de dos filas
            	logger.info("Falta informacion");
            	System.out.println("Falta informacion");
            	
            	String mensaje = this.catMensajesService.get( EMensajesArchivos.INFORMACION_INCOMPLETA.getId() ).getDescripcionMensaje();
            	response.setStatus("ERROR");
        		response.setMessage(mensaje);
        		return response;
            }
            
            Double bytes = multiPart.getSize() * 1.0;

            Double kilobytes = (bytes / 1024);
            Double megabytes = (kilobytes / 1024);
            
            response.setTotalRegistros( r.size() -1 );
            response.setPeso( megabytes ) ;
		} catch (Exception e) {
            e.printStackTrace();
            logger.error("Error inesperado", e);
            
            String mensaje = this.catMensajesService.get( EMensajesArchivos.INFORMACION_INCOMPLETA.getId() ).getDescripcionMensaje();
            response.setStatus("ERROR");
    		response.setMessage(mensaje);
    		return response;
        }   
		return response;
	}
	
	
	private boolean borrarDocumento(ConfiguracionFtpModel configuracionFtp, String pathSftp, String nomArchivo, Integer idDocumentoPublicado) {
		
		String host = configuracionFtp.getUrl();
		Integer port = configuracionFtp.getPuerto();
		String username = configuracionFtp.getUsuario();
		String password = configuracionFtp.getContrasenia();
		
		ChannelSftp channel = null;
		Session session = null;									    	    			
		JSch jsch = new JSch();
		try {
			
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();                                                   
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            
            System.out.println("Conectado");
            ChannelSftp sftp = (ChannelSftp) channel;

            sftp.cd(pathSftp);
            sftp.rm(nomArchivo);
            //sftp.put( inputStream, nomArchivo, null, mode);
            System.out.println("Se borro el Archivo:" + nomArchivo);
            session.disconnect();
    		channel.disconnect();    		
		} catch (NumberFormatException | JSchException | SftpException e) {
			e.printStackTrace();
			return false;
		}
		return true;

		//CatDocumentoConfModel catDocumentoConf = this.catDocumentoConfService.getCatDocumentoConf(idTipoDocumento);
		/*
		try {

			JSch ssh = new JSch();
			Session session = ssh.getSession(sftpUser, sftpHost, sftpPort);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword(sftpPassword);

			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftp = (ChannelSftp) channel;

			// Usa el método rm para eliminar el archivo del directorio remoto
			sftp.rm(documento.getRutaDocumento() + documento.getNombreArchivo());

			channel.disconnect();
			session.disconnect();
			
			documento.setActivo(false); // borrado lógico
			controlDocumentoRepo.save(documento);
			return true;

		} catch (JSchException e) {

			System.out.println(e.getMessage().toString());
			e.printStackTrace();
			return false;

		} catch (SftpException e) {

			System.out.println(e.getMessage().toString());
			e.printStackTrace();
			return false;
		}*/
	}

	
	private void borrarArchivoLogico(Integer idDocumentoPublicado) {
		this.documentoPublicadoRepository.borrarArchivo(idDocumentoPublicado);
		
	}

	private boolean validHeader(String[] headerCsv, String[] headerConfig) {
		String originalHeader = "";
		String headerReadCsv = "";
		
		for(String s: headerConfig) {
			originalHeader += s;
		}
		
		for(String r: headerCsv) {
			headerReadCsv += r;
		}
		
		System.out.println("Original H : " + originalHeader);
		System.out.println("Lectura H : " + headerReadCsv);
		
		return originalHeader.toLowerCase().equals(headerReadCsv.replaceAll(",", "").toLowerCase());
	}
	
	private boolean sendSFT(ConfiguracionFtpModel configuracionFtp, String pathSftp, String nomArchivo, File uploadFile) {
		String host = configuracionFtp.getUrl();
		Integer port = configuracionFtp.getPuerto();
		String username = configuracionFtp.getUsuario();
		String password = configuracionFtp.getContrasenia();
		
		int mode = ChannelSftp.OVERWRITE;
		
		ChannelSftp channel = null;
		Session session = null;									    	    			
		JSch jsch = new JSch();
		try {
			InputStream inputStream = new FileInputStream(uploadFile);
			session = jsch.getSession(username, host, port);
			session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();                                                   
            channel = (ChannelSftp)session.openChannel("sftp");
            channel.connect();
            
            System.out.println("Conectado");
            ChannelSftp sftp = (ChannelSftp) channel;

            sftp.cd(pathSftp);
            sftp.put( inputStream, nomArchivo, null, mode);
            System.out.println("Se subió el Archivo:" + nomArchivo);
            session.disconnect();
    		channel.disconnect();
		} catch (NumberFormatException | JSchException | SftpException | FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String borrarArchivo(Integer idDocumentoPublicado) {
		DocumentoPublicadoEntity documentoPublicado = this.documentoPublicadoRepository.findByIdDocumentoPublicado(idDocumentoPublicado);
		if (documentoPublicado != null) {
			boolean borradoFtp = true;
			CatDocumentoConfModel catDocumentoConf = this.catDocumentoConfService.getCatDocumentoConf(documentoPublicado.getIdDocumentoConf());
			String sftpPath = catDocumentoConf.getRutaDeposito();
			
			ConfiguracionFtpModel configuracionFtp = this.configuracionFtpService.getConfiguracion( catDocumentoConf.getIdConfiguracionFtp() );
			if(documentoPublicado.getIdEstatusDocumento().intValue() == EEstatusDocumento.EN_PROCESO.getIdEstatus().intValue()) {
				borradoFtp = this.borrarDocumento(configuracionFtp, sftpPath, documentoPublicado.getNombreArchivo(), idDocumentoPublicado);
			}
			//this.borrarArchivoLogico(idDocumentoPublicado);
			if (borradoFtp) {
				this.borrarArchivoLogico(idDocumentoPublicado);
				return "OK";
			} else {
				return "ERROR";
			}
		}
		return "ERROR";
	}

}
