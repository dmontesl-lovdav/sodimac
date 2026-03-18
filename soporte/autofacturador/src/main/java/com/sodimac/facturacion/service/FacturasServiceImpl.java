package com.sodimac.facturacion.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.wsft.BodyClienteTemporalTYPE;
import com.sodimac.facturacion.clientews.wsft.BodyCrearZipTYPE;
import com.sodimac.facturacion.clientews.wsft.BodyObtenerTicketTYPE;
import com.sodimac.facturacion.clientews.wsft.BodyTimbrarTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteCrearZipExpReqTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteObtenerTicketReqTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteObtenerTicketRespTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteTimbrarExpReqTYPE;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.FacturasEntity;
import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.FacturasMultipleModel;
import com.sodimac.facturacion.models.ListaRegimenFiscal;
import com.sodimac.facturacion.models.RegimenFiscal;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.repository.FacturasRepository;
import com.sodimac.facturacion.repository.bct.TicketRepository;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.UtilsFile;
import com.sodimac.facturacion.util.UtilsString;
import com.sodimac.facturacion.util.enums.ECodigo;
import com.sodimac.facturacion.util.enums.EProceso;
import com.sodimac.facturacion.util.enums.ETipoPersonaEnum;
import com.sodimac.facturacion.util.enums.EVersionCFDI;

@Service
public class FacturasServiceImpl implements FacturasService {

	private Logger logger = LoggerFactory.getLogger(FacturasServiceImpl.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	@Autowired
	private FacturasRepository facturasRepository;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private SeguridadService seguridadService;
	@Autowired
	private ConfiguracionService configuracionService;
	@Autowired
	private TicketRepository ticketRepository;
	
	String UrlLogin = "";
	String UrlTimbrar = "";
	String UrlCrearZip = "";
	String UrlCrearPdf = "";
	String userName = "";
	String userPass = "";
	String headerValue = "";
	String UrlObtenerTicket = "";
	String UrlEnviarCorreo = "";
	String UrlEnviarCorreoToken = "";
	
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
		
	Document ObtenerDocumentXml (String xml) {
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
	
	public String getClaveUsoCfdiNC(String ticket) {
		String result = "";
		TicketEntity ticketBctHdr = ticketRepository.findByTicket(ticket);
		if (ticketBctHdr != null) {
			FacturasEntity factura = facturasRepository.findByTicket(ticketBctHdr.getOriginal());
			if (factura != null) {
				String xml = seguridadService.desencriptar(factura.getXml());
				Document document = ObtenerDocumentXml (xml);
				Node nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
		        Element eElement = (Element) nNode;
		        result = eElement.getAttribute("UsoCFDI");				
			}
		}
		return result;
	}
	
	@Override
	public String getDatosCfdiNC(String ticket) {
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        
		TicketEntity ticketBctHdr = ticketRepository.findByTicket(ticket);
		if (ticketBctHdr != null) {
			FacturasEntity factura = facturasRepository.findByTicket(ticketBctHdr.getOriginal());
			if (factura != null) {
				
				String correo = "";
				if (factura.getEmail() != null && !factura.getEmail().isEmpty()) {
					correo = UtilsString.emailMask(seguridadService.desencriptar(factura.getEmail()));
				}
				String xml = seguridadService.desencriptar(factura.getXml());
				Document document = ObtenerDocumentXml (xml);
				Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
				Element eElement = (Element) nNode;
		        String versionCfdi = eElement.getAttribute("Version");
				
		        nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
		        eElement = (Element) nNode;
		        String usoCfdi = eElement.getAttribute("UsoCFDI");
		        String rfc = eElement.getAttribute("Rfc");
		        String razonSocial = eElement.getAttribute("Nombre");
		        String codigoPostal = "";
		        
		        String regimenFiscal = "";
		        String regimenFiscalDescripcion = "";
		        String usoCfdiDescripcion = "";
		        String versionVigenteNC= "OK";
		        
		        Integer idTipoPersona = ETipoPersonaEnum.FISICA.getId();
		    	if (rfc.length()==12) { 
		    		idTipoPersona = ETipoPersonaEnum.MORAL.getId();
		    	}
		        
		        ClientResponseTYPE<List<UsoDeCfdi>> respCfdi = null;
		        
		        EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionCfdi);
		        if (eVersionCFDI.equals(EVersionCFDI.VERSION_40)) {
		        	regimenFiscal = eElement.getAttribute("RegimenFiscalReceptor");
		        	codigoPostal = eElement.getAttribute("DomicilioFiscalReceptor");
		        	
		        	respCfdi = this.configuracionService.consultarUsoCfdi40(idTipoPersona, regimenFiscal);
		        	if (respCfdi.getRespuesta().getCodigo().equals("1") ) {
		        		List<UsoDeCfdi> listUsoCfdi = respCfdi.getData();
		        		if (listUsoCfdi != null) {
			        		for (UsoDeCfdi usoCfdi40 : listUsoCfdi) {
			        			if(usoCfdi.equals(usoCfdi40.getClave())) {
			        				usoCfdiDescripcion = usoCfdi40.getDescripcionUso();
			        				break;
			        			}
			        		}
		        		}
		        	}//if (respCfdi.getRespuesta().getCodigo().equals("1") )
		        	
		        	
		        	ClientResponseTYPE<ListaRegimenFiscal> repRegimenFiscal = this.configuracionService.consultarRegimenFiscal(idTipoPersona);
		        	if (repRegimenFiscal.getRespuesta().getCodigo().equals("1")) {
		        		ListaRegimenFiscal listaRegimenFiscal = repRegimenFiscal.getData();
		        		if (listaRegimenFiscal != null) {
		        			for(RegimenFiscal regimen: listaRegimenFiscal) {
		        				if (regimenFiscal.equals( regimen.getRegimenfiscal())) {
		        					regimenFiscalDescripcion = regimen.getDescripcion();
		        				}
		        			}
		        		}
		        	}
		        	
		        }//if (eVersionCFDI.equals(EVersionCFDI.VERSION_40))
		        else {
		        	respCfdi = this.configuracionService.consultarUsoCfdi33(idTipoPersona);
		        	
		        	if (respCfdi.getRespuesta().getCodigo().equals("1")) {
		        		List<UsoDeCfdi> listUsoCfdi = respCfdi.getData();
		        		if (listUsoCfdi != null) {
			        		for (UsoDeCfdi usoCfdi33 : listUsoCfdi) {
			        			if(usoCfdi.equals(usoCfdi33.getClave())) {
			        				usoCfdiDescripcion = usoCfdi33.getDescripcionUso();
			        				break;
			        			}
			        		}
		        		}
		        	}
		        	
		        	try {
		        		String fechaLimiteNC33 = this.catConfiguracionService.findParameterByKey("Limite.NC.33");
			        	Date fechaActual = Calendar.getInstance().getTime();
						Date fechaLimite = sdf.parse(fechaLimiteNC33);
						
						if (fechaActual.compareTo(fechaLimite) > 0) {
							versionVigenteNC = "NO";
						}
		        	} catch (ParseException e) {
		        		logger.info("Error al parsear fecha limite 3.3");
		        		e.printStackTrace();
		        	}
		        }//else
		        
		        clienteJson.addProperty("usoCfdi", usoCfdi);
		        clienteJson.addProperty("usoCfdiDescripcion", usoCfdiDescripcion);
    			clienteJson.addProperty("regimenFiscal", regimenFiscal);
		        clienteJson.addProperty("regimenFiscalDescripcion", regimenFiscalDescripcion);
    			clienteJson.addProperty("versionCfdi", versionCfdi);
    			clienteJson.addProperty("rfc", rfc);
    			clienteJson.addProperty("versionVigenteNC", versionVigenteNC);
    			clienteJson.addProperty("razonSocial", razonSocial);
    			clienteJson.addProperty("codigoPostal", codigoPostal);
    			clienteJson.addProperty("correo", correo);
			}
		}
		result = gson.toJson(clienteJson);
		return result;
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
		body.setVersion(model.getVersionCfdi());
		body.setCodigoPostal( model.getCodigoPostal() );
		body.setRegimenFiscal( model.getRegimenFiscal());
		req.setBody(body);
		
		logger.info("Inicializando WSFT");
		inicializarWsft();
		
		if (headerValue.isEmpty()) obtenerToken();

		logger.info("headerValue: " + headerValue);
    	int contador=0;
		HttpResponse<String> responseTimbrar = null;
		ObjectMapper objectMapper = new ObjectMapper();
			
		
		do {
			try {
				String str = objectMapper.writeValueAsString(req);
				logger.info("#####################################################");
				logger.info("UrlTimbrar: " + this.UrlTimbrar);
				logger.info(str);
				logger.info("#####################################################");
				responseTimbrar = Unirest.post(UrlTimbrar)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(str)
					.asString();
			} catch (Exception e) {
				logger.error("TimbrarTicket " + body.getDocumento() + ": ", e);
			}
			
			if (responseTimbrar != null) {
				try {
					response = objectMapper.readValue(responseTimbrar.getBody(), ClienteTicketTimbrarExpRespTYPE.class);
					logger.info("Respuesta: " + response);
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
		if (UrlCrearZip == "") UrlCrearZip = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.CrearZip");
		if (UrlCrearPdf == "") UrlCrearPdf = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.CrearPdf");
		if (userName == "") userName = catConfiguracionService.findParameterByKey("WebService.Facturacion.Usuario");
		if (userPass == "") userPass = catConfiguracionService.findParameterByKey("WebService.Facturacion.Password");
		if (UrlObtenerTicket == "") UrlObtenerTicket = catConfiguracionService.findParameterByKey("WS.Configuracion.Url.ObtenerTicket");
		if (UrlEnviarCorreo == "") UrlEnviarCorreo = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.EnviarCorreo");
		if (UrlEnviarCorreoToken == "") UrlEnviarCorreoToken = catConfiguracionService.findParameterByKey("WebService.Facturacion.Url.EnviarCorreoToken");

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
				logger.error("CrearZip " + body.getUuid() + ": ", e);
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
					logger.error("CrearZip " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}

		return response;
	}

	public ClienteTicketTimbrarExpRespTYPE crearPdfWs (String uuid) {
		
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
				responseTimbrar = Unirest.post(UrlCrearPdf)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(objectMapper.writeValueAsString(req) )
					.asString();
			} catch (Exception e) {
				logger.error("CrearZip " + body.getUuid() + ": ", e);
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
					logger.error("CrearZip " + body.getUuid() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseTimbrar == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}

		return response;
	}

	@Override
	public ClienteObtenerTicketRespTYPE obtenerTicket(String ordenCompra) {
		ClienteObtenerTicketRespTYPE response = new ClienteObtenerTicketRespTYPE();
		BodyObtenerTicketTYPE body = new BodyObtenerTicketTYPE();
		ClienteObtenerTicketReqTYPE req = new ClienteObtenerTicketReqTYPE();
		
		body.setDocumento(ordenCompra);
		req.setBody(body);
		logger.info("ordenCompra: " + ordenCompra);
		
		this.inicializarWsft();
		
		if (headerValue.isEmpty()) {
			this.obtenerToken();
		}
		
		int contador=0;
		HttpResponse<String> responseHttp = null;
		ObjectMapper objectMapper = new ObjectMapper();
				
		do {
			try {
				String writeValueAsString = objectMapper.writeValueAsString(req);
				responseHttp = Unirest.post(UrlObtenerTicket)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body( writeValueAsString )
					.asString();
			} catch (Exception e) {
				logger.error("obtenerTicket " + body.getDocumento() + ": ", e);
			}
			
			if (responseHttp != null) {
				try {
					response = objectMapper.readValue(responseHttp.getBody(), ClienteObtenerTicketRespTYPE.class);
					if (response.getRespuesta().getCodigo().equals("5")) {
						obtenerToken();
						responseHttp = null;
						response = new ClienteObtenerTicketRespTYPE();
					}
				} catch (JsonProcessingException e) {
					logger.error("obtenerTicket " + body.getDocumento() + ": ", e);
				}
				
			}
			contador += 1;

		} while (responseHttp == null && contador <= 4);
							
		if (response.getRespuesta() == null) {
			logger.error("obtenerTicket " + body.getDocumento() + ": ");
		}

		return response;
	}

	@Override
	public void liberarTicket(String ticket) {
		this.facturasRepository.liberarTicket(ticket);
	}

	@Override
	public ClientResponseTYPE<String> enviarCorreo(ClientesTemporalModel model) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		BodyClienteTemporalTYPE body = new BodyClienteTemporalTYPE();
		body.setUuid( model.getUuid() );
		body.setXml( model.getXml() );
		body.setRfc( model.getRfc() );
		body.setEmail( model.getEmail() );
		body.setEmailCC(model.getEmailCC() );
		body.setTicket( model.getTicket() );
		body.setPac( model.getPac() );
		body.setIdFacturaPac( model.getIdFacturaPac() );
		body.setRazonSocial( model.getRazonSocial() );
		
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
				
				responseHttp = Unirest.post(UrlEnviarCorreo)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(json)
					.asString();
			} catch (Exception e) {
				logger.error("TimbrarTicket " + body.getTicket() + ": ", e);
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
					logger.error("Enviar Correo " + body.getTicket() + ": ", e);
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
	public ClientResponseTYPE<String> enviarCorreoToken(ClientesTemporalModel model) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();
		BodyClienteTemporalTYPE body = new BodyClienteTemporalTYPE();
		body.setEmail( model.getEmail() );
		body.setRazonSocial( model.getRazonSocial() );
		body.setToken( model.getToken() );
		
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
				
				responseHttp = Unirest.post(UrlEnviarCorreoToken)
					.header("Authorization", headerValue)
					.header("Content-Type", "application/json")
					.body(json)
					.asString();
			} catch (Exception e) {
				logger.error("TimbrarTicket " + body.getTicket() + ": ", e);
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
					logger.error("Enviar Correo " + body.getTicket() + ": ", e);
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
