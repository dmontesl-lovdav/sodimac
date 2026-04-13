package com.sodimac.rebates.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.model.ControlDocumento;
import com.sodimac.rebates.model.Documento;
import com.sodimac.rebates.model.EnviosAp;
import com.sodimac.rebates.model.EnviosApCsvModel;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.SPCargaModel;
import com.sodimac.rebates.model.Usuario;
import com.sodimac.rebates.repository.ControlDocumentoRepository;
import com.sodimac.rebates.repository.EnviosApRepository;
import com.sodimac.rebates.repository.UsuarioRepository;
import com.sodimac.rebates.util.Conexion;

@Service
public class ControlDocumentoService implements IControlDocumentoService {
	
	private static final String VARIABLE_SFTP_HABILITADO = "SftpHabilitado";

	Logger logger = LoggerFactory.getLogger(ControlDocumentoService.class);
	
	private final SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");    
	
	public final String[] HEADER = { "EMPRESA", "FECHA_DOCUMENTO", "REFERENCIA_DOCUMENTO", "NUMERO_DOCUMENTO", "MONEDA", "TIPO_CAMBIO", "DEBITO_CREDITO",
            "CUENTA_CONTABLE", "CODIGO_PROVEEDOR", "IMPORTE", "SUCURSAL", "CONDICION_PAGO", "FECHA_VENCIMIENTO", "BLOQUEO_PAGO",
            "SISTEMA_ORIGEN", "FECHA_ENVIO", "FECHA_CONTABLE", "CLASE_DOCUMENTO", "NUMERO_REFERENCIA", "CENTRO_COSTO", "CENTRO_BENEFICIO", "NUMERO_UUID",
            "FLAG_ENVIADO", "FECHA_RECEPCION", "TIPO_DOCUMENTO", "ORIGEN_ETL", "IdPeriodo", "Timbrado", "TipoRebate"};

	@Autowired
	private ControlDocumentoRepository controlDocumentoRepo;
	
	@Autowired
	private EnviosApRepository enviosApRepository = null;
	
	@Autowired
	private Conexion conexion;

	@Value("${sodimac.rebates.sftp.host}")
	private String sftpHost;

	@Value("${sodimac.rebates.sftp.port}")
	private int sftpPort;

	@Value("${sodimac.rebates.sftp.user}")
	private String sftpUser;

	@Value("${sodimac.rebates.sftp.password}")
	private String sftpPassword;

	@Value("${sodimac.rebates.sftp.remote.directory}")
	private String sftpRemoteDirectory;
	
	@Autowired
	private IConfiguracionService configuracionService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public List<ControlDocumento> getAll() {

		return controlDocumentoRepo.findAll();
	}

	@Override
	public void save(ControlDocumento controlDocumento) {

		controlDocumentoRepo.save(controlDocumento);
	}

	@Override
	public List<ControlDocumento> getActive() {

		return controlDocumentoRepo.findByActivo(true);
	}

	@Override
	public boolean logicDelete(Integer id) {

		Optional<ControlDocumento> optional = controlDocumentoRepo.findById(id);
		ControlDocumento documento = new ControlDocumento();
		if (optional.isPresent()) {
			documento = optional.get();
			documento.setActivo(false); // borrado lógico
			controlDocumentoRepo.save(documento);
			return true;
		}
		return false;
	}
	
	@Override
	public ControlDocumento getControlDocumento(Integer id) {

		Optional<ControlDocumento> optional = controlDocumentoRepo.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	@Override
	public List<ControlDocumento> getCargas(Documento documento, Periodo periodo, boolean activo) {

		return controlDocumentoRepo.findByDocumentoAndPeriodoAndActivo(documento, periodo, activo);
	}

	@Override
	public List<ControlDocumento> getActiveAndStatusDistinticContabilizado() {

		List<ControlDocumento> list = controlDocumentoRepo.findDistinctByPeriodoAndActivo();
		List<Integer> idList = list.stream()
				.filter(l -> l.getUsuario() != null)
				.map(l -> l.getUsuario()).distinct()
				.collect(Collectors.toList());
		List<Usuario> usuariosList = usuarioRepository.findAllById(idList);
		list.forEach(l -> {
			if (l.getUsuario() != null) {
				Usuario item = usuariosList.stream()
						.filter(usu -> usu.getId().equals(l.getUsuario()))
						.findAny()
						.orElse(null);
				l.setNombreUsuario(item.getNombre());				
			}
		});
		
		return list;
	}

	@Override
	public List<ControlDocumento> getActiveQeryWithDates(Date fechaCargaIni, Date fechaCargaFin, String nombreArchivo,
			String idDocumento, String idPeriodo) {

		return controlDocumentoRepo.findByActivoWithDates(fechaCargaIni, fechaCargaFin, nombreArchivo, idDocumento,
				idPeriodo);
	}

	@Override
	public List<ControlDocumento> getActiveQeryWithOutDates(String nombreArchivo, String idDocumento,
			String idPeriodo) {

		return controlDocumentoRepo.findByActivoWithOutDates(nombreArchivo, idDocumento, idPeriodo);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Generic readCsvEnviosAp(MultipartFile multiPart, Integer usuario) {
		Generic response = new Generic();
		response.setTypeMessage(2);
		response.setCode(false);
		logger.info("Comienza procesamiento de archivo AP");
		List<EnviosApCsvModel> rows = new ArrayList<>();
		String nameFile = multiPart.getOriginalFilename();
		System.out.println(nameFile);
		logger.info(nameFile);
		try {
			Reader reader = new InputStreamReader(multiPart.getInputStream());
			Reader reader2 = new InputStreamReader(multiPart.getInputStream());
			CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); // custom separator
			CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(csvParser).build();
			
            List<String[]> r = csvReader.readAll();

            // Validamos que el archivo no este vacio
            if (r.isEmpty()) {
            	logger.info("El archivo esta vacio");
                System.out.println("El archivo esta vacio");
                response.setTitle("Archivo vacío");
        		response.setMessage("El archivo esta vacío, seleccione otro archivo");
        		return response;
            } else {
            	r.forEach(x -> System.out.println(Arrays.toString(x)));
               
            }
            
            // Validamos el header
            if (!validHeader(r.get(0))) {
            	logger.info("La cabecera es inválida");
            	System.out.println("La cabecera es inválida");
                response.setTitle("La cabecera es inválida");
        		response.setMessage("La cabecera es inválida");
        		return response;
            } else {
            	System.out.println("Header OK");	
            }
            
            // Validamos que haya más informacion que el header
            if (r.size() < 2) { // Que tenga más de dos filas
            	logger.info("Falta informacion");
            	System.out.println("Falta informacion");
                response.setTitle("Falta información");
        		response.setMessage("No hay suficiente información para ser procesado.");
        		return response;
            }
            
            // Parceamos a objecto 
            rows = new CsvToBeanBuilder(reader2)
                    .withType(EnviosApCsvModel.class)
                    .withSkipLines(1)
                    .build()
                    .parse();
            
            // Validamos informacion completa y devito VS credito,
            String validarData = validarData(rows);
            logger.info("Validacion de data : " + validarData);
            if (!validarData.equals("")) {
                response.setTitle("Datos no validos");
        		response.setMessage(validarData);
        		return response;
            }
            
		} catch (Exception e) {
            e.printStackTrace();
            logger.error("Error inesperado", e);
            response.setTitle("Error desconocido");
    		response.setMessage("Ocurrió un problema al procesar el archivo");
    		response.setTypeMessage(3);
    		response.setCode(false);
    		return response;
        }
            
		try {
			SPCargaModel result = registrarCsvEnviosAp(rows, nameFile, usuario);
			logger.info("Resultado de SP : " + result);
			if(result != null) {
				response.setTitle("Resultado ejecucion SP");
				response.setMessage(result.getDescripcion());
				if (result.getCodigo() == 1) {
					response.setTypeMessage(1);
					response.setCode(true);
				} else {
					response.setTypeMessage(3);
					response.setCode(false);
				}
				return response;
			} else {
				logger.error("No se ejecutó el SP correctamente");
				response.setTitle("Problema en SP");
        		response.setMessage("No se ejecutó el SP correctamente");
        		response.setTypeMessage(3);
        		return response;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error de BD", e);
            response.setTitle("Error al registrar en BD");
    		response.setMessage("Ocurrió un problema al registrar la información en BD");
    		response.setTypeMessage(3);
    		response.setCode(false);
    		return response;
		}

	}

	private SPCargaModel registrarCsvEnviosAp(List<EnviosApCsvModel> rows, String nameFile, Integer usuario) throws Exception {
		logger.info("========== INICIO REGISTRO DE " + rows.size() + " REGISTROS ==========");
		int contador = 0;
		int exitosos = 0;
		int fallidos = 0;

		for (EnviosApCsvModel item : rows) {
			contador++;
			logger.info("Procesando registro " + contador + " de " + rows.size());

			try {
				EnviosAp enviosAp = new EnviosAp();
				enviosAp.setEMPRESA(item.getEmpresa());
				enviosAp.setFECHA_DOCUMENTO(new java.sql.Date(format.parse(item.getFechaDocumento()).getTime()));
				enviosAp.setREFERENCIA_DOCUMENTO(item.getReferenciaDocumento());
				enviosAp.setNUMERO_DOCUMENTO(item.getNumeroDocumento().replaceAll("[\\r\\n]+", " ").trim());
				enviosAp.setMONEDA(item.getMoneda());
				enviosAp.setTIPO_CAMBIO(new BigDecimal(item.getTipoCambio()));
				enviosAp.setDEBITO_CREDITO(item.getDebitoCredito());
				enviosAp.setCUENTA_CONTABLE(item.getCuentaContable());
				enviosAp.setCODIGO_PROVEEDOR(item.getCodigoProveedor());
				enviosAp.setIMPORTE(new BigDecimal(item.getImporte()));
				enviosAp.setSUCURSAL(item.getSucursal());
				enviosAp.setCONDICION_PAGO(item.getCondicionPago());
				enviosAp.setFECHA_VENCIMIENTO(new java.sql.Date(format.parse(item.getFechaVencimiento()).getTime()));
				enviosAp.setBLOQUEO_PAGO(item.getBloqueoPago().equalsIgnoreCase("NULL") ? null : item.getBloqueoPago());
				enviosAp.setSISTEMA_ORIGEN(item.getSistemaOrigen());
				enviosAp.setFECHA_ENVIO(new java.sql.Date(new Date().getTime())); // Fecha actual // DATO FIJO
				enviosAp.setFECHA_CONTABLE(new java.sql.Date(format.parse(item.getFechaContable()).getTime()));
				enviosAp.setCLASE_DOCUMENTO(item.getClaseDocumento());
				enviosAp.setNUMERO_REFERENCIA(item.getNumeroReferencia());
				enviosAp.setCENTRO_COSTO(item.getCentroCosto());
				enviosAp.setCENTRO_BENEFICIO(item.getCentroBeneficio());
				enviosAp.setNUMERO_UUID(item.getNumeroUuid());
				enviosAp.setFLAG_ENVIADO(item.getFlagEnviado());
				enviosAp.setFECHA_RECEPCION(new java.sql.Date(format.parse(item.getFechaRecepcion()).getTime()));
				enviosAp.setTIPO_DOCUMENTO(item.getTipoDocumento());
				enviosAp.setORIGEN_ETL("REBATES"); // DATO FIJO
				enviosAp.setIdPeriodo(Integer.parseInt(item.getIdPeriodo()));
				enviosAp.setTimbrado(Integer.parseInt(item.getTimbrado()));
				enviosAp.setTipoRebate(Integer.parseInt(item.getTipoRebate()));

				logger.info("Datos a insertar: " + enviosAp.toString());
				enviosApRepository.save(enviosAp);
				exitosos++;
				logger.info("Registro " + contador + " guardado exitosamente");

			} catch (Exception e) {
				fallidos++;
				logger.error("ERROR al guardar registro " + contador + ": " + e.getMessage(), e);
				logger.error("Datos del registro que falló: " + item.toString());
				// Re-lanzar la excepción para que se maneje en el nivel superior
				throw new Exception("Error al insertar registro " + contador + " en Envios_Ap_Temp: " + e.getMessage(), e);
			}
		}

		logger.info("========== RESUMEN DE INSERCIÓN ==========");
		logger.info("Total procesados: " + contador);
		logger.info("Exitosos: " + exitosos);
		logger.info("Fallidos: " + fallidos);

		logger.info("========== TODOS LOS REGISTROS GUARDADOS, VERIFICANDO EN BD ==========");

		// Verificar cuántos registros se insertaron realmente
		long totalInsertados = enviosApRepository.count();
		logger.info("Total de registros en Envios_Ap_Temp: " + totalInsertados);
		logger.info("Total esperado: " + rows.size());

		if (totalInsertados != rows.size()) {
			logger.error("ERROR: Se esperaban " + rows.size() + " registros pero solo hay " + totalInsertados + " en la tabla");
		}

		logger.info("Nombre archivo: " + nameFile);
		logger.info("Usuario: " + usuario);
		logger.info("Ejecutando SP sp_carga_envios_ap...");

		SPCargaModel sp = ejecutaSP(nameFile, usuario);
		logger.info("Resultado SP: " + sp.toString());
		System.out.println("Resultado SP: " + sp.toString());

		return sp;
	}
	
	private SPCargaModel ejecutaSP(String nameFile, Integer usuario) {
		SPCargaModel result = null;
		Connection con = null;
		CallableStatement cs = null;

		try {
			logger.info("Abriendo conexión para ejecutar SP...");
			con = conexion.abreConexion();

			logger.info("Preparando llamada al SP sp_carga_envios_ap");
			cs = con.prepareCall("{call sp_carga_envios_ap (?, ?, ?, ?)}");

			cs.setString("V_NOMBRE_ARCHIVO", nameFile);
			cs.setInt("V_IDUSUARIO", usuario);
			cs.registerOutParameter("V_CODIGO", 4);
			cs.registerOutParameter("V_DESC", 12);

			logger.info("Ejecutando SP con parámetros: nombreArchivo=" + nameFile + ", usuario=" + usuario);
			cs.execute();

			result = new SPCargaModel();
			result.setCodigo(cs.getInt(3));
			result.setDescripcion(cs.getString(4));

			logger.info("SP ejecutado. Código retornado: " + result.getCodigo() + ", Descripción: " + result.getDescripcion());

			if (result.getCodigo() != 1) {
				logger.error("ERROR EN SP - Código: " + result.getCodigo() + ", Descripción: " + result.getDescripcion());
			}

		} catch(Exception e) {
			logger.error("Excepción al ejecutar SP sp_carga_envios_ap", e);
			e.printStackTrace();
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					logger.error("Error al cerrar CallableStatement", e);
					e.printStackTrace();
				}
			}

			conexion.cierraConexion(con);
		}

		return result;
	}
	
	
	private String validarData(List<EnviosApCsvModel> rows) {
		
		String valid = "";
		
		BigDecimal totalDebito = BigDecimal.ZERO;
		BigDecimal totalCredito = BigDecimal.ZERO;
		int index = 2;
		for (EnviosApCsvModel item : rows) {
			logger.info(index + " - "+ item.toString());
			System.out.println(item.toString());
			// Validamos campos no nulos ni vacios;
			
			String column = columnInvalid(item);
			
			if (!column.equals("")) {
				// Si no viene algun dato obligatorio se responde data invalida
				valid = "Faltan datos, favor de verificar la fila número: " + index + " y la columna " + column;
				break;
			} else { // Si no vienen datos vacios o nulos				
				//Validamos importe
				try {
					item.setImporte(item.getImporte().replaceAll(",", "").trim()); // Se quitan comas y espacios

					BigDecimal auxImporte = new BigDecimal(item.getImporte());
					
					if(auxImporte.compareTo(BigDecimal.ZERO) == 0) {
						valid = "No se permiten importes en 0, validar la fila número: " + index;
						break;
					}
				} catch(Exception ex) {
					//Error al convertir String a BigDecimal
					valid = "El importe no es válido en la fila número: " + index;
					break;
				}
				
				
				// Se suman montos de creditos y debitos
				if (item.getDebitoCredito().trim().equalsIgnoreCase("D")) {
					totalDebito = totalDebito.add(new BigDecimal(item.getImporte()));
				} else if (item.getDebitoCredito().trim().equalsIgnoreCase("C")) {
					totalCredito = totalCredito.add(new BigDecimal(item.getImporte()));
				}
				
				
				
				// Validamos fechas
				try {
					new java.sql.Date(format.parse(item.getFechaDocumento()).getTime());
					new java.sql.Date(format.parse(item.getFechaVencimiento()).getTime());
					new java.sql.Date(format.parse(item.getFechaContable()).getTime());
					new java.sql.Date(format.parse(item.getFechaRecepcion()).getTime());
				} catch(Exception ex) {
					valid = "Las fechas no son correctas, validar fila número " + index;
					break;
				}
				
				
			}
			index++;
		}
		
		if (valid.equals("")) {
			logger.info("totalDebito: " + String.valueOf(totalDebito));
			logger.info("totalCredito:" + String.valueOf(totalCredito));
			if(totalCredito.compareTo(totalDebito) != 0) {
				valid = "La suma de los montos de débito y crédito son diferentes favor de verificar.";
			}
		}
		
		return valid;
	}

	private String columnInvalid(EnviosApCsvModel item) {

		String column = "";
		
		if (item.getEmpresa() == null) column = HEADER[0];
		else if ( item.getFechaDocumento() == null)  column = HEADER[1];
		else if ( Strings.isBlank(item.getReferenciaDocumento()) )  column = HEADER[2];
		else if ( Strings.isBlank(item.getNumeroDocumento()) )  column = HEADER[3];
		else if ( Strings.isBlank(item.getMoneda()) ) column = HEADER[4]; 
		else if ( item.getTipoCambio() == null ) column = HEADER[5];
		else if ( Strings.isBlank(item.getDebitoCredito()) ) column = HEADER[6];
		else if ( Strings.isBlank(item.getCuentaContable()) ) column = HEADER[7];
		else if ( item.getCodigoProveedor() == null ) column = HEADER[8];
		else if ( item.getImporte() == null ) column = HEADER[9];
		else if ( item.getSucursal() == null )  column = HEADER[10];
		else if ( Strings.isBlank(item.getCondicionPago()) ) column = HEADER[11];
		else if ( item.getFechaVencimiento() == null ) column = HEADER[12];
//		else if ( Strings.isBlank(item.getBloqueoPago()) )  13
		else if ( Strings.isBlank(item.getSistemaOrigen()) ) column = HEADER[14];
		else if ( item.getFechaEnvio() == null ) column = HEADER[15];
		else if ( item.getFechaContable() == null ) column = HEADER[16];
		else if ( Strings.isBlank(item.getClaseDocumento()) ) column = HEADER[17];
		else if ( Strings.isBlank(item.getNumeroReferencia()) ) column = HEADER[18];
		else if ( Strings.isBlank(item.getCentroCosto()) && Strings.isBlank(item.getCentroBeneficio()) ) column = HEADER[19] + " o " + HEADER[20];
//		else if ( Strings.isBlank(item.getNumeroUuid())   21 
		else if ( Strings.isBlank(item.getFlagEnviado()) ) column = HEADER[22];
		else if ( item.getFechaRecepcion() == null ) column = HEADER[23];
		else if ( Strings.isBlank(item.getTipoDocumento()) ) column = HEADER[24];
		else if ( Strings.isBlank(item.getOrigenEtl()) ) column = HEADER[25];
		else if ( Strings.isBlank(item.getIdPeriodo()) ) column = HEADER[26];
		else if ( Strings.isBlank(item.getTimbrado()) ) column = HEADER[27];
		else if ( Strings.isBlank(item.getTipoRebate()) ) column = HEADER[28];
		
		return column;
	}

	private boolean validHeader(String[] headerCsv) {
		String originalHeader = "";
		String headerReadCsv = "";
		
		for(String s: HEADER) {
			originalHeader += s;
		}
		
		for(String r: headerCsv) {
			headerReadCsv += r;
		}
		
		System.out.println("Original H : " + originalHeader);
		System.out.println("Lectura H : " + headerReadCsv);
		
		return originalHeader.toLowerCase().equals(headerReadCsv.replaceAll(",", "").toLowerCase());
	}

	@Override
	public boolean existeDocumento(Documento documento, Periodo periodo) {
		int count = this.controlDocumentoRepo.countByDocumentoAndPeriodoAndActivo(documento, periodo, EEstatus.ACTIVO.isActivo());
		if (count > 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isSftpHabilitado() {
		String valor = this.configuracionService.getValor(VARIABLE_SFTP_HABILITADO);
		return  (valor != null && Boolean.parseBoolean(valor));
	}

}
