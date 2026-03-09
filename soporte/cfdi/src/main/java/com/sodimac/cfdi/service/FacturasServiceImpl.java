package com.sodimac.cfdi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sodimac.cfdi.clientews.wsft.BodyCancelarTYPE;
import com.sodimac.cfdi.clientews.wsft.BodyCrearZipTYPE;
import com.sodimac.cfdi.clientews.wsft.BodyRetimbrarTYPE;
import com.sodimac.cfdi.clientews.wsft.BodyTimbrarTYPE;
import com.sodimac.cfdi.clientews.wsft.ClienteCancelarCfdiExpReqTYPE;
import com.sodimac.cfdi.clientews.wsft.ClienteCrearZipExpReqTYPE;
import com.sodimac.cfdi.clientews.wsft.ClienteTicketRetimbrarExpReqTYPE;
import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.cfdi.clientews.wsft.ClienteTimbrarExpReqTYPE;
import com.sodimac.cfdi.component.ErrorComponent;
import com.sodimac.cfdi.entity.fiscal.ComplementosEntity;
import com.sodimac.cfdi.entity.fiscal.FacturasEntity;
import com.sodimac.cfdi.model.BodyComplementoCorreoTYPE;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.models.ClientesTemporalModel;
import com.sodimac.cfdi.models.FacturasMultipleModel;
import com.sodimac.cfdi.models.TipoComprobanteModelItem;
import com.sodimac.cfdi.models.TipoOrigenModelItem;
import com.sodimac.cfdi.repository.fiscal.FacturasRepository;
import com.sodimac.cfdi.util.UtilsApi;
import com.sodimac.cfdi.util.UtilsFile;
import com.sodimac.cfdi.util.enums.ECodigo;
import com.sodimac.cfdi.util.enums.EProceso;

@Service
public class FacturasServiceImpl implements FacturasService {
	private int nxRow = 0;
	Logger logger = LoggerFactory.getLogger(FacturasServiceImpl.class);
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	@Autowired
	private FacturasRepository facturasRepository;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	private ComplementosService complementoService;
	
	String UrlLogin = "";
	String UrlTimbrar = "";
	String UrlCrearZip = "";
	String UrlBase64 = "";
	String UrlBase64Complemento = "";
	
	String userName = "";
	String userPass = "";
	String headerValue = "";
	String UrlServicioCancelar = "";
	String UrlServicioRefacturar = "";
	String UrlEnviarCorreoComplementoFactura = "";
	String UrlEnviarCorreoComplemento = "";
	String UrlEnviarCorreoFacturaUuid = "";
	
	public void crearZipXmlPdf(String facturaId) {
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		File fileNameXml = new File(path + facturaId + ".xml");
		File fileNamePdf = new File(path + facturaId + ".pdf");
		File fileNameZip = new File(path + facturaId + ".zip");

		try {
			FileOutputStream fos = new FileOutputStream(fileNameZip);
			ZipOutputStream zipOS = new ZipOutputStream(fos);

			UtilsFile.writeToZipFile(fileNameXml, zipOS);
			UtilsFile.writeToZipFile(fileNamePdf, zipOS);

			zipOS.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
		
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public int existFactura(String rfc, String ticket) {
		return facturasRepository.existFactura(seguridadService.encriptar(rfc), ticket);
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public int existFactura(String ticket) {
		return facturasRepository.existFactura(ticket);
	}

	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<FacturasMultipleModel> getFacturasFechas(String rfc, String email, String fechaInicial, String fechafinal, int start, int rowsPerPage) {
		List<FacturasMultipleModel> list = new ArrayList<FacturasMultipleModel>();
		
		facturasRepository.getFacturasFechas(seguridadService.encriptar(rfc), seguridadService.encriptar(email), fechaInicial, fechafinal, start, rowsPerPage).forEach(item -> {
			FacturasMultipleModel itemList = new FacturasMultipleModel();
			itemList.setIdFactura(Integer.parseInt(item[0].toString()));
			itemList.setUuid(item[1].toString());
			itemList.setTicket(item[2].toString());
			itemList.setRazonSocial(seguridadService.desencriptar(item[3].toString()));
			itemList.setFechaTimbrado(item[4].toString());
			itemList.setNombreEstatus(item[5].toString());
			itemList.setRfc(seguridadService.desencriptar(item[6].toString()));
			itemList.setNombreArchivo(item[7].toString());
			itemList.setChecked("".toString());
			list.add(itemList);
		});
		
		return list;
	}
	
	@Transactional
	public int actualizarDatos(ClientesTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String razonSocial = seguridadService.encriptar(model.getRazonSocial());
		String email = seguridadService.encriptar(model.getEmail());
		String xml = seguridadService.encriptar(model.getXml());

		return facturasRepository.actualizarDatosFactura(rfc
					, model.getTicket()
					, razonSocial
					, model.getIdUsoCfdi()
					, email
					, model.getAutorizoGuardado()
					, model.getPac()
					, model.getIdFacturaPac()
					, model.getUuid()
					, model.getFechaTimbrado()
					, model.getVersionFacturacionSat()
					, xml
					, model.getFechaCompra()
					, model.getIdEstatusFactura()
					, model.getTicketBct()
					, model.getVersionFactura()
					, model.getTransaccion()
					, model.getNombreObra()
					, model.getResponsableObra()
					, model.getIdComprobante()
					, model.getUuidRelacionado()
					, errorComponent.getLongitud()
					, errorComponent.getLatitud()
					, errorComponent.getPagina()
					, errorComponent.getExplorador()
					, errorComponent.getSistemaOper()
					, errorComponent.getIp()
					);			
	}

	public void crearZipMultiple(String archivoZip, String archivosZip) {
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		File fileNameZip = new File(path + archivoZip + ".zip");
		String archivo = "";

		try {
			FileOutputStream fos = new FileOutputStream(fileNameZip);
			ZipOutputStream zipOS = new ZipOutputStream(fos);

			if (archivosZip.indexOf(",") > 0) {
				
				String[] Archivos = archivosZip.split(",");
				for (String item : Archivos) {
					
					String uuid = item.substring(17);
					crearZipWs(uuid);
					
					File fileName = new File(path + item + ".zip");
					addToZipFile(fileName, zipOS);
				}
			} else {
				archivo = archivosZip;
				File fileName = new File(path + archivo + ".zip");
				addToZipFile(fileName, zipOS);				
			}
			
			zipOS.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	void addToZipFile(File fileName, ZipOutputStream zipOS) {
		try {
			UtilsFile.writeToZipFile(fileName, zipOS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}							
	}
	
	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaByUuid(String uuid) {
		return facturasRepository.findByUuid(uuid);
	}
		
	@Transactional
	public int countFacturas(String rfc, String email, String fechaInicial, String fechafinal) {
	return facturasRepository.getCountFacturas(seguridadService.encriptar(rfc), seguridadService.encriptar(email), fechaInicial, fechafinal);
	}
		
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<ClientesTemporalModel> obtenerDatosFactura(String rfc, String sessionid, String ticket ) {
		List<ClientesTemporalModel> list = new ArrayList<ClientesTemporalModel>();
		
		facturasRepository.obtenerDatosFactura(seguridadService.encriptar(rfc), sessionid, ticket).forEach(item -> {
			ClientesTemporalModel itemList = new ClientesTemporalModel();
			itemList.setId(Integer.parseInt(item[0].toString()));
			itemList.setUuid(item[1].toString());
			itemList.setTicket(item[2].toString());
			itemList.setRazonSocial(seguridadService.desencriptar(item[3].toString()));
			itemList.setFechaTimbrado(item[4].toString());
			itemList.setNombreEstatus(item[5].toString());
			itemList.setRfc(seguridadService.desencriptar(item[6].toString()));
			itemList.setNombreArchivo(item[7].toString());
			list.add(itemList);
		});
		
		return list;
	}

	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaByTicket(String ticket) {
		return facturasRepository.findByTicket(ticket);
	}
	
	public void eliminarArchivo(String fileName) {
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		UtilsFile.EliminarArchivo(path + fileName + ".xml");
		UtilsFile.EliminarArchivo(path + fileName + ".pdf");
		UtilsFile.EliminarArchivo(path + fileName + ".zip");
	}
	
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public boolean existRfcEmail(String rfc, String email) {
		return !facturasRepository.findByRfcAndEmail(seguridadService.encriptar(rfc), seguridadService.encriptar(email)).isEmpty() ? true : false;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public List<FacturasMultipleModel> getCfdiFechas(String fechaInicial, String fechafinal, String rfcEncriptado,
			String uuid, String ticket, int start, int rowsPerPage,String tipoComprobante, int pidOrigen, String monto) {
		List<FacturasMultipleModel> list = new ArrayList<FacturasMultipleModel>();
		
		facturasRepository.getCfdiFechas(fechaInicial, fechafinal, rfcEncriptado, uuid, ticket, start, rowsPerPage,tipoComprobante, pidOrigen, monto).forEach(item -> {
			FacturasMultipleModel itemList = new FacturasMultipleModel();
			itemList.setIdFactura(Integer.parseInt(item[0].toString()));
			itemList.setUuid(item[1].toString());
			itemList.setTicket(item[2].toString());
			itemList.setRazonSocial(item[3].toString());
			itemList.setFechaTimbrado(item[4].toString());
			itemList.setNombreEstatus(item[5].toString());
			itemList.setRfc(seguridadService.desencriptar(item[6].toString()));
			itemList.setNombreArchivo(item[7].toString());
			
			itemList.setTicketBct(item[8].toString());
			itemList.setIdFacturaPac(Integer.parseInt(item[9].toString()));
			itemList.setVersionFacturacionSat(item[10].toString());
			itemList.setFechaCompra(item[11].toString());
			itemList.setNombreObra(item[12].toString());
			itemList.setResponsableObra(item[13].toString());
			itemList.setIdComprobante(item[14].toString());
			itemList.setDescripcionTimbrado(item[15].toString());
			itemList.setDescripcionOrigen(item[16].toString());
			itemList.setTotal(item[17].toString());
			itemList.setChecked("".toString());
			list.add(itemList);
		});
		
		return list;
	}

	@Transactional
	public int countCfdi(String fechaInicial, String fechafinal, String rfcEncriptado, String uuid, String ticket) {
	return facturasRepository.getCountCfdi(fechaInicial, fechafinal, rfcEncriptado, uuid, ticket);
	}

	public int cancelar(int idFacturaPac) {
		return cancelarWs(idFacturaPac);
	}
		
	int cancelarWs (int idFacturaPac) {
		
		ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
		BodyCancelarTYPE body = new BodyCancelarTYPE();
		ClienteCancelarCfdiExpReqTYPE req = new ClienteCancelarCfdiExpReqTYPE();
		
		body.setFacturaId(Integer.toString(idFacturaPac));
		req.setBody(body);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();
		
		int contador = 0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			try {
				responseTimbrar = Unirest.post(UrlServicioCancelar)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();		
			} catch (Exception e) {
				logger.error("Cancelar idFacturaPac " + idFacturaPac + ": ", e);
			}
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new ClienteTicketTimbrarExpRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("Cancelar idFacturaPac " + idFacturaPac + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
		
		if (response == null || response.getRespuesta() == null) {
			logger.info("idFacturaPac " + idFacturaPac + " no cancelado");
			return 0;
		}
			
		if (response.getRespuesta().getCodigo().equals("1")) {
			logger.info("idFacturaPac " + idFacturaPac + " Cancelado satisfactoriamente");
		} else {
			logger.info("idFacturaPac " + idFacturaPac + " no cancelado");
			logger.info("idFacturaPac " + idFacturaPac + " codigo: " + response.getRespuesta().getCodigo());
			logger.info("idFacturaPac " + idFacturaPac + " descripcion: " + response.getRespuesta().getDescripcion());
		}
		
		return Integer.parseInt(response.getRespuesta().getCodigo());
	}
	
	@Transactional
	public int refacturar(ClientesTemporalModel clientes) {
		return refacturarWs(clientes);
	}
	
	int refacturarWs (ClientesTemporalModel model) {
		
		ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
		BodyRetimbrarTYPE body = new BodyRetimbrarTYPE();
		ClienteTicketRetimbrarExpReqTYPE req = new ClienteTicketRetimbrarExpReqTYPE();
		
		int idFacturaPac = model.getIdFacturaPac();
		String autorizoGuardado = "false";
		if (model.getAutorizoGuardado() == 1) autorizoGuardado = "true";

		body.setIdFacturaPac(Integer.toString(idFacturaPac));
		body.setDocumento(model.getTicket());
		body.setMonto(model.getTotal().toString());
		body.setRfc(model.getRfc());
		body.setRazonSocial(model.getRazonSocial());
		body.setUsoCfdi(model.getIdUsoCfdi());
		body.setMetodoPago(model.getMetodoPago());
		body.setCorreo(model.getEmail());
		body.setCorreoCC("");
		body.setNombreObra(model.getNombreObra());
		body.setResponsableObra(model.getResponsableObra());
		body.setAutorizoGuardado(autorizoGuardado);
		req.setBody(body);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

		int contador = 0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
		
		do {
			try {
				responseTimbrar = Unirest.post(UrlServicioRefacturar)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();		
			} catch (Exception e) {
				logger.error("Refacturar idFacturaPac " + idFacturaPac + ": ", e);
			}
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new ClienteTicketTimbrarExpRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("Refacturar idFacturaPac " + idFacturaPac + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
						
		if (response == null || response.getRespuesta() == null) {
			logger.info("idFacturaPac " + idFacturaPac + " no refacturado");
			return 0;
		}
		
		if (response.getRespuesta().getCodigo().equals("1")) {
			logger.info("idFacturaPac " + idFacturaPac + " Refacturado satisfactoriamente");
		} else {
			logger.info("idFacturaPac " + idFacturaPac + " no refacturado");
			logger.info("idFacturaPac " + idFacturaPac + " codigo: " + response.getRespuesta().getCodigo());
			logger.info("idFacturaPac " + idFacturaPac + " descripcion: " + response.getRespuesta().getDescripcion());
		}
		
		return Integer.parseInt(response.getRespuesta().getCodigo());
	}	
	
	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaByIdFacturaPac(int idFacturaPac) {
		return facturasRepository.findByIdFacturaPac(idFacturaPac);
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public boolean getCfdiExcelFechas(String fechaInicial, String fechafinal, String rfcEncriptado, String uuid,
			String ticket, String nombreArchivo,String tipoComprobante, int pidOrigen, String pmonto) throws Exception {

		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Reporte");
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 10000);
		sheet.setColumnWidth(3, 5500);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 4000);
		sheet.setColumnWidth(6, 6500);
		sheet.setColumnWidth(7, 4000);
		sheet.setColumnWidth(8, 6000);
		sheet.setColumnWidth(9, 7000);
		sheet.setColumnWidth(10, 10000);
		sheet.setColumnWidth(11, 4000);
		sheet.setColumnWidth(12, 6000);
		sheet.setColumnWidth(13, 10000);
		
		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());	
		headerStyle.setFont(font);

		String[] columns = {"RFC","Razón Social","UUID","Ticket","OC","Factura Id","Fecha Timbrado","Version","Fecha de Venta","Nombre de Obra","Responsable de Obra","Sucursal","Tipo de ticket","UUID Relacionado"};
		
		for (int i = 0; i< columns.length; i++) {
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(columns[i]);
			headerCell.setCellStyle(headerStyle);
		}
		
		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		
		CellStyle styleCenter = workbook.createCellStyle();
		styleCenter.setWrapText(true);
		styleCenter.setAlignment(HorizontalAlignment.CENTER);

		nxRow = 0;

		facturasRepository.getCfdiExcelFechas(fechaInicial, fechafinal, rfcEncriptado, uuid, ticket,tipoComprobante, pidOrigen, pmonto).forEach(item -> {

			nxRow = nxRow + 1;
			Row row = sheet.createRow(nxRow);

			// seguridadService.desencriptar(item[3].toString()
			Cell cell = row.createCell(0);
			cell.setCellValue(seguridadService.desencriptar(item[0].toString())); // RFC
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(seguridadService.desencriptar(item[1].toString())); // Razon Social
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue(item[2].toString()); // UUID
			cell.setCellStyle(style);

			cell = row.createCell(3);
			cell.setCellValue(item[3].toString()); // Ticket
			cell.setCellStyle(style);

			cell = row.createCell(4);
			cell.setCellValue(item[4].toString()); // OC
			cell.setCellStyle(styleCenter);
			

			cell = row.createCell(5);
			cell.setCellValue(item[13].toString()); // IdFACTURA
			cell.setCellStyle(styleCenter);

			cell = row.createCell(6);
			cell.setCellValue(item[6].toString()); // Fecha Timbrado
			cell.setCellStyle(styleCenter);

			cell = row.createCell(7);
			cell.setCellValue(item[7].toString()); // Version
			cell.setCellStyle(styleCenter);

			cell = row.createCell(8);
			cell.setCellValue(item[8].toString()); // Fecha Venta
			cell.setCellStyle(styleCenter);

			cell = row.createCell(9);
			cell.setCellValue(item[9].toString()); // Nombre Obra
			cell.setCellStyle(style);

			cell = row.createCell(10);
			cell.setCellValue(item[10].toString()); // Responsable de Obra
			cell.setCellStyle(style);

			cell = row.createCell(11);
			cell.setCellValue(item[11].toString()); // Sucursal
			cell.setCellStyle(styleCenter);

			cell = row.createCell(12);
			cell.setCellValue(item[12].toString()); // Tipo de ticket
			cell.setCellStyle(style);

			cell = row.createCell(13);
			cell.setCellValue(item[16].toString()); // UUID Relacionado
			cell.setCellStyle(style);

		});		
			
		File fileLocation = new File(path + nombreArchivo);

		FileOutputStream outputStream;

		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public String getTipodeComprobante() {
		List<TipoComprobanteModelItem> list = new ArrayList<TipoComprobanteModelItem>();
		
		facturasRepository.getTipodeComprobante().forEach(item -> {
			TipoComprobanteModelItem itemList = new TipoComprobanteModelItem();
			itemList.setIdComprobante(item[0].toString());
			itemList.setDescripcionTimbrado(item[1].toString());
			list.add(itemList);			
		});
		Gson gson= new Gson();
		String resultado = gson.toJson(list);
		return resultado;
	}
	
	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public String getTipoOrigen() {
		List<TipoOrigenModelItem> list = new ArrayList<TipoOrigenModelItem>();
		
		facturasRepository.getTipoOrigen().forEach(item -> {
			TipoOrigenModelItem itemList = new TipoOrigenModelItem();
			itemList.setIdOrigen(item[0].toString() );
			itemList.setDescripcion(item[1].toString());
			list.add(itemList);			
		});
		Gson gson= new Gson();
		String resultado = gson.toJson(list);
		return resultado;
	}

	public ClienteTicketTimbrarExpRespTYPE timbrarTicketWs (ClientesTemporalModel model) {
		
		ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
		BodyTimbrarTYPE body = new BodyTimbrarTYPE();
		ClienteTimbrarExpReqTYPE req = new ClienteTimbrarExpReqTYPE();
		
		body.setDocumento(model.getTicket());
		body.setMonto(model.getTotal().toString());
		body.setRfc(model.getRfc());
		body.setRazonSocial(model.getRazonSocial());
		body.setUsoCfdi(model.getIdUsoCfdi());
		body.setCorreo(model.getEmail());
		body.setCorreoCC("");
		body.setNombreObra(model.getNombreObra());
		body.setResponsableObra(model.getResponsableObra());
		body.setAutorizoGuardado(model.getAutorizoGuardado() == 1 ? "true" : "false");
		body.setTipoProceso(Integer.toString(EProceso.Autofacturador.getValor()));		
		req.setBody(body);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				responseTimbrar = Unirest.post(UrlTimbrar)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();
			} catch (Exception e) {
				logger.error("TimbrarTicket " + body.getDocumento() + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new ClienteTicketTimbrarExpRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("TimbrarTicket " + body.getDocumento() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}

		return response;
	}
	
	private void obtenerToken() {
 		int contador = 0;
 		HttpResponse<String> responseLogin = null;
 		
    	do {
			try {
				responseLogin = Unirest.post(UrlLogin)
					.header("Content-Type", "application/json")
					.body("{\"username\": \""+userName+"\",\r\n  \"password\": \""+userPass+"\"\r\n}")
					.asString();		
			} catch (Exception e) {
				logger.error("Token: ", e);
			}
			contador += 1;

		} while (responseLogin == null 
				&& contador <= 4);
		
    	if (responseLogin != null) {
    		Headers headers = responseLogin.getHeaders();
    		headerValue =  headers.getFirst("authorization");				    		
    	}
	}
	
	private void inicializarWsft() {

		if (UrlLogin == "") UrlLogin = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.Login");
		if (UrlTimbrar == "") UrlTimbrar = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.Timbrar");
		if (UrlServicioCancelar == "") UrlServicioCancelar = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.Cancelar");
		if (UrlServicioRefacturar == "") UrlServicioRefacturar = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.Refacturar");
		if (UrlCrearZip == "") UrlCrearZip = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.CrearZip");
		if (UrlBase64 == "") UrlBase64 = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.CrearBase64");
		if (UrlBase64Complemento == "") UrlBase64Complemento = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.CrearBase64Complemento");
		if (userName == "") userName = catConfiguracionService.findParameterByKey("WebService.Facturacion.Usuario");
		if (userPass == "") userPass = catConfiguracionService.findParameterByKey("WebService.Facturacion.Password");
		if (UrlEnviarCorreoComplementoFactura == "") UrlEnviarCorreoComplementoFactura = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.EnviarCorreoComplementoFactura");
		if (UrlEnviarCorreoComplemento == "") UrlEnviarCorreoComplemento = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.EnviarCorreoComplemento");
		if (UrlEnviarCorreoFacturaUuid == "") UrlEnviarCorreoFacturaUuid = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.EnviarCorreoFacturaUuid");
		
		try {
			Unirest.setHttpClient(ClientSSl());
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	* Metodo que permite realizar una llamada a un      
	* Servicio HTTPS      
	*/
	private CloseableHttpClient ClientSSl() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
	{
		SSLContext sslcontext = SSLContexts.custom()
	            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
	            .build();

	    @SuppressWarnings("deprecation")
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    CloseableHttpClient httpclient = HttpClients.custom()
	            .setSSLSocketFactory(sslsf)
	            .build();
	    
	    return httpclient;
	}
	
	@Transactional
	public int insertarLogFacturacion(ClientesTemporalModel model) {
		String rfc = seguridadService.encriptar(model.getRfc());
		String email = seguridadService.encriptar(model.getEmail());

		return facturasRepository.insertarLogFacturacion(rfc
					, model.getTicket()
					, email
					, model.getPac()
					, model.getIdFacturaPac()
					, model.getIdEstatusFactura()
					);			
	}

	public ClienteTicketTimbrarExpRespTYPE crearZipWs (String uuid) {
		
		ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();	
		BodyCrearZipTYPE body = new BodyCrearZipTYPE();
		ClienteCrearZipExpReqTYPE req = new ClienteCrearZipExpReqTYPE();
		
		body.setUuid(uuid);
		req.setBody(body);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				responseTimbrar = Unirest.post(UrlCrearZip)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();
			} catch (Exception e) {
				logger.error("uuid " + body.getUuid() + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new ClienteTicketTimbrarExpRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("uuid " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}

		return response;
	}

	public ClienteTicketTimbrarExpRespTYPE crearBase64Ws (String uuid) {
		
		ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();	
		BodyCrearZipTYPE body = new BodyCrearZipTYPE();
		ClienteCrearZipExpReqTYPE req = new ClienteCrearZipExpReqTYPE();
		
		body.setUuid(uuid);
		req.setBody(body);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				responseTimbrar = Unirest.post(UrlBase64)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();
			} catch (Exception e) {
				logger.error("uuid " + body.getUuid() + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new ClienteTicketTimbrarExpRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("uuid " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (responseTimbrar == null || response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}

		return response;
	}
	
	public ClienteTicketTimbrarExpRespTYPE crearBase64WsComplemento (String uuid) {
		
		ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();	
		BodyCrearZipTYPE body = new BodyCrearZipTYPE();
		ClienteCrearZipExpReqTYPE req = new ClienteCrearZipExpReqTYPE();
		
		body.setUuid(uuid);
		req.setBody(body);
		
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				responseTimbrar = Unirest.post(UrlBase64Complemento)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();
			} catch (Exception e) {
				logger.error("uuid " + body.getUuid() + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseTimbrar = null;
						response = new ClienteTicketTimbrarExpRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("uuid " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (responseTimbrar == null || response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}

		return response;
	}
	
	public boolean crearZip (String uuid) {
		
		FacturasEntity factura = getFacturaByUuid(uuid);
		if (factura == null) {
			return false;
		}

		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		ClienteTicketTimbrarExpRespTYPE response = crearBase64Ws(uuid);
		if (response.getRespuesta().getCodigo().equals("1")) {
			
			String fileNameXml = path + factura.getNombreArchivo() + ".xml";
			UtilsFile.crearArchivo(fileNameXml, seguridadService.desencriptar(factura.getXml()));

			String base64Pdf = response.getRespuesta().getDescripcion();
			String fileNamePdf = path + factura.getNombreArchivo() + ".pdf";
			UtilsFile.crearArchivoB64(fileNamePdf, base64Pdf);
			
			crearZipXmlPdf(factura.getNombreArchivo());
			
			return true;
		}

		return false;
	}
	
	@Override
	public boolean crearZipComplemento(String idComplemento) {
		ComplementosEntity complemento = complementoService.getComplemento(idComplemento);
		if (complemento == null) {
			return false;
		}

		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		ClienteTicketTimbrarExpRespTYPE response = crearBase64WsComplemento(complemento.getUuid());
		if (response.getRespuesta().getCodigo().equals("1")) {
			
			String fileNameXml = path + complemento.getNombreArchivo() + ".xml";
			UtilsFile.crearArchivo(fileNameXml, seguridadService.desencriptar(complemento.getXml()));

			String base64Pdf = response.getRespuesta().getDescripcion();
			String fileNamePdf = path + complemento.getNombreArchivo() + ".pdf";
			UtilsFile.crearArchivoB64(fileNamePdf, base64Pdf);
			
			crearZipXmlPdf(complemento.getNombreArchivo());
			
			return true;
		}

		return false;
	}
	
	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public FacturasEntity getFacturaById(Integer idFactura) {
		FacturasEntity factura = null;
		Optional<FacturasEntity> opFacturaEntity = facturasRepository.findById(idFactura);
		if(opFacturaEntity.isPresent()) {
			factura = opFacturaEntity.get();
		}
		return factura;
	}
	
	@Override
	public Document obtenerDocumentXml(String xml) {
		Document document = null;
		try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xml));
	        document = documentBuilder.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
            errorComponent.setXml(xml);
			errorComponent.setPagina("ObtenerDocumentXml");
			errorComponent.guardarLog(e);
        }
		
        return document;		
	}
	
	@Override
	public ClientResponseTYPE<String> enviarCorreoComplemento(BodyComplementoCorreoTYPE body) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				logger.info("#################################");
				String json = objectMapper.writeValueAsString(body);
				logger.info("Solicitud JSON");
				logger.info(json);
				
				responseHttp = Unirest.post(UrlEnviarCorreoComplemento)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(json)
					.asString();
			} catch (Exception e) {
				logger.error("Enviar Correo complemento " + body.getUuid() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<String>>() {});
					logger.info("Codigo de respuesta: " + response.getRespuesta().getCodigo());
					logger.info("#################################");
					logger.info(response.getRespuesta().getDescripcion());
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseHttp = null;
						response = new ClientResponseTYPE<String>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Enviar Correo " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			logger.info("ERROR de comunicacion");
		}
		return response;
	}
	
	@Override
	public ClientResponseTYPE<String> enviarCorreoFactura(BodyComplementoCorreoTYPE body) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				logger.info("#################################");
				String json = objectMapper.writeValueAsString(body);
				logger.info("Solicitud JSON");
				logger.info(json);
				
				responseHttp = Unirest.post(UrlEnviarCorreoComplementoFactura)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(json)
					.asString();
			} catch (Exception e) {
				logger.error("Enviar Correo complemento factura " + body.getUuid() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<String>>() {});
					logger.info("Codigo de respuesta: " + response.getRespuesta().getCodigo());
					logger.info("#################################");
					logger.info(response.getRespuesta().getDescripcion());
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseHttp = null;
						response = new ClientResponseTYPE<String>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Enviar Correo " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			logger.info("ERROR de comunicacion");
		}
		return response;
	}

	@Override
	public ClientResponseTYPE<String> enviarCorreoFactura(ClientesTemporalModel body) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

    	int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				logger.info("#################################");
				String json = objectMapper.writeValueAsString(body);
				logger.info("Solicitud JSON");
				logger.info(json);
				
				responseHttp = Unirest.post(UrlEnviarCorreoFacturaUuid)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(json)
					.asString();
			} catch (Exception e) {
				logger.error("Enviar Correo complemento factura " + body.getUuid() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), new TypeReference<ClientResponseTYPE<String>>() {});
					logger.info("Codigo de respuesta: " + response.getRespuesta().getCodigo());
					logger.info("#################################");
					logger.info(response.getRespuesta().getDescripcion());
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseHttp = null;
						response = new ClientResponseTYPE<String>();
					}
				} catch (JsonProcessingException e) {
					logger.error("Enviar Correo " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			logger.info("ERROR de comunicacion");
		}
		return response;
	}
}
