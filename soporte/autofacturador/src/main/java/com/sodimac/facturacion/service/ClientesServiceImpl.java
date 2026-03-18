package com.sodimac.facturacion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteObtenerTicketRespTYPE;
import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.ClientesEntity;
import com.sodimac.facturacion.models.CodigoPostal;
import com.sodimac.facturacion.models.ListaRegimenFiscal;
import com.sodimac.facturacion.models.RegimenCapital;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.models.VersionCfdi;
import com.sodimac.facturacion.repository.ClientesRepository;
import com.sodimac.facturacion.repository.FormaPago33Repository;
import com.sodimac.facturacion.repository.Tiendas40Repository;
import com.sodimac.facturacion.service.ws.ObtenerTicketService;
import com.sodimac.facturacion.util.UtilsNumber;
import com.sodimac.facturacion.util.UtilsString;
import com.sodimac.facturacion.util.enums.EAplicacion;
import com.sodimac.facturacion.util.enums.ECatalogoMensajes;
import com.sodimac.facturacion.util.enums.ERegimenFiscal;
import com.sodimac.facturacion.util.enums.EVersionCFDI;

@Service
public class ClientesServiceImpl implements ClientesService {

	private Logger logger = LoggerFactory.getLogger(ClientesServiceImpl.class);
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	@Autowired
	private ClientesRepository clientesRepository;
	@Autowired
	private Tiendas40Repository tiendas40Repository;
	@Autowired
	private FormaPago33Repository formaPago33Respository;
	@Autowired
	private CatUsosCfdiService catUsosCfdiService;
	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private SeguridadService seguridadService;
	@Autowired
	private ConfiguracionService configuracionService;
	@Autowired
	private CatMensajesService catMensajesService;
	
	@Autowired
	private ObtenerTicketService obtenerTicketService;
	
	@Autowired
	private FacturasService facturaService;
	
	@Override
	@Transactional
	public ClientesEntity getCliente(String rfc) {
		String rfcEncrip = seguridadService.encriptar(rfc);
		return clientesRepository.findByRfc(rfcEncrip);
	}
	
	@Transactional
	public int existRfcFactura(String rfc) {
		String encriptar = seguridadService.encriptar(rfc);
		return clientesRepository.existRfcFactura(seguridadService.encriptar(encriptar));
	}
	
	@Transactional
	public int inicializarRfcTicket(String rfc, String ticket) {
		return clientesRepository.inicializarRfcTicket(seguridadService.encriptar(rfc), ticket);
	}
	
	@Transactional
	public boolean validarRZExpresionRegular(String razonSocial) {
		boolean result = true;
		int longitudRazonSocial = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.RazonSocial.longitud"));
		
		razonSocial = razonSocial.trim();
		
		if (razonSocial.isEmpty() || razonSocial.length() > longitudRazonSocial) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.RazonSocial.Caracteres"));
	        Matcher mat = pat.matcher(razonSocial);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarRfcExpresionRegular(String rfc) {
		boolean result = true;
		int longitudRfcMinima = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.Rfc.longitud.Minima"));
		int longitudRfcMaxima = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.Rfc.longitud.Maxima"));
		
		rfc = rfc.trim();
		
		if (rfc.isEmpty() || (rfc.length() < longitudRfcMinima || rfc.length() > longitudRfcMaxima)) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.Rfc"));
	        Matcher mat = pat.matcher(rfc);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarEmailExpresionRegular(String email) {
		boolean result = true;
		int longitudEmail = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.Correo.longitud"));
		
		email = email.trim();
		
		if (email.isEmpty() || email.length() > longitudEmail) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.Email"));
	        Matcher mat = pat.matcher(email);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarObraExpresionRegular(String nombreObra) {
		boolean result = true;
		int longitudNombreObra = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.NombreObra.longitud"));
		
		nombreObra = nombreObra.trim();
		
		if (nombreObra.isEmpty() || nombreObra.length() > longitudNombreObra) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.NombreObra.Caracteres"));
	        Matcher mat = pat.matcher(nombreObra);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}
	
	@Transactional
	public boolean validarResponsableObraExpresionRegular(String responsableObra) {
		boolean result = true;
		int longitudResponsableObra = Integer.parseInt(catConfiguracionService.findParameterByKey("Request.Timbrado.ResponsableObra.longitud"));
		
		responsableObra = responsableObra.trim();
		
		if (responsableObra.isEmpty() || responsableObra.length() > longitudResponsableObra) {
			result = false;
		} else {
	        Pattern pat = Pattern.compile(catConfiguracionService.findParameterByKey("ExpresionRegular.ResponsableObra.Caracteres"));
	        Matcher mat = pat.matcher(responsableObra);
	        if (!mat.matches()) {
	        	result = false;
	        }					
		}
		return result;
	}

	@Transactional
	public String getInformacionCliente(String rfc) {
		
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        List<String> datosArr = new ArrayList <String>();
        
        try {
        	actividadesModel.registrarActividad(12, null, "ObtenerCliente");
        	ClientesEntity cliente = getCliente(rfc);
            boolean existe = false; 
            if (cliente == null) {
            	datosArr.clear();
            	actividadesModel.registrarActividad(15, datosArr, "ObtenerCliente");
            } else {
            	existe = true;
    			clienteJson.addProperty("razonSocial", seguridadService.desencriptar(cliente.getRazonSocial()));
    			String email = UtilsString.emailMask(seguridadService.desencriptar(cliente.getEmail()));
    			clienteJson.addProperty("email", email);
    			String codigoPostal = cliente.getCodigoPostal();
    			String regimeFiscal = cliente.getRegimenFiscal();
    			
    			if (codigoPostal != null) {
    				codigoPostal = seguridadService.desencriptar(codigoPostal);
    			}
    			
    			clienteJson.addProperty("codigoPostal", (codigoPostal == null) ? "" : codigoPostal);
    			clienteJson.addProperty("regimenFiscal", (regimeFiscal == null) ? "" : regimeFiscal);
    			
    			int idUsoCfdi = cliente.getIdUsoCfdi();
    			EVersionCFDI eVersionCFDI = EVersionCFDI.VERSION_33;
    			String claveUsoCfdi = catUsosCfdiService.getUsoCfdi(idUsoCfdi, eVersionCFDI.getId()).getClave();
    			clienteJson.addProperty("claveUsoCfdi", claveUsoCfdi);
            	datosArr.clear();
    			actividadesModel.registrarActividad(13, datosArr, "ObtenerCliente");
            }
            clienteJson.addProperty("existe", existe);
            
        	String tipo = "fisica";
        	if (rfc.length()==12) tipo = "moral";
        	ERegimenFiscal regimenFiscalEnum = ERegimenFiscal.getRegimenFiscalByDescripcion(tipo);
        	ClientResponseTYPE<ListaRegimenFiscal> listRegimenFiscal = this.configuracionService.consultarRegimenFiscal(regimenFiscalEnum.getId());
    		JsonArray listRegimenfiscal = new JsonArray();
    		if (listRegimenFiscal != null) {
	    		listRegimenFiscal.getData().forEach(item -> {
	    			JsonObject itemJson = new JsonObject();
	    			itemJson.addProperty("id", item.getRegimenfiscal());
	    			itemJson.addProperty("descripcion", item.getDescripcion());
	    			listRegimenfiscal.add(itemJson);
	    		});
    		}    		
    		clienteJson.add("listRegimenfiscal", listRegimenfiscal);
    		
    		result = gson.toJson(clienteJson);

    		return result;
    		
		} catch (Exception e) {
			errorComponent.setRfc(rfc);
			errorComponent.setPagina("getInformacionCliente");
			errorComponent.guardarLog(e);
			e.printStackTrace();
			return null;
		}
	}
	
	@Transactional
	public String getInformacionCliente(String rfc, String versionCfdi) {
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        List<String> datosArr = new ArrayList <String>();
        
        try {
        	actividadesModel.registrarActividad(12, null, "ObtenerCliente");
        	ClientesEntity cliente = getCliente(rfc);
            boolean existe = false; 
            if (cliente == null) {
            	datosArr.clear();
            	actividadesModel.registrarActividad(15, datosArr, "ObtenerCliente");
            } else {
            	boolean limpiarDatos40 = false;
            	existe = true;
    			
            	String razonSocial = seguridadService.desencriptar(cliente.getRazonSocial());
    			String email = UtilsString.emailMask(seguridadService.desencriptar(cliente.getEmail()));
    			String codigoPostal = cliente.getCodigoPostal();
    			String regimenFiscal = (cliente.getRegimenFiscal() != null ) ? cliente.getRegimenFiscal().trim() : "";
    			
    			if (codigoPostal != null) {
    				codigoPostal = seguridadService.desencriptar(codigoPostal);
    			}
    			
    			int idUsoCfdi = cliente.getIdUsoCfdi();
    			EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionCfdi);
    			String claveUsoCfdi = catUsosCfdiService.getUsoCfdi(idUsoCfdi, eVersionCFDI.getId()).getClave();
            	datosArr.clear();
    			actividadesModel.registrarActividad(13, datosArr, "ObtenerCliente");
    			
    			if (regimenFiscal.isEmpty() && versionCfdi.equals(EVersionCFDI.VERSION_40.getVersion())) {
    			//if (versionCfdi.equals( VERSION_CFDI_40 )) {
    				limpiarDatos40 = true;
    			}
    			
    			if (limpiarDatos40) {
    				codigoPostal = null;
    				regimenFiscal = null;
    				razonSocial = null;
    			}
    			
    			clienteJson.addProperty("razonSocial", (razonSocial == null) ? "" : razonSocial);
    			clienteJson.addProperty("codigoPostal", (codigoPostal == null) ? "" : codigoPostal);
    			clienteJson.addProperty("regimenFiscal", (regimenFiscal == null) ? "" : regimenFiscal);
    			
    			clienteJson.addProperty("email", email);
    			clienteJson.addProperty("claveUsoCfdi", claveUsoCfdi);
            }
            clienteJson.addProperty("existe", existe);
            
        	String tipo = "fisica";
        	if (rfc.length()==12) tipo = "moral";
        	ERegimenFiscal regimenFiscalEnum = ERegimenFiscal.getRegimenFiscalByDescripcion(tipo);
        	
        	ClientResponseTYPE<ListaRegimenFiscal> listRegimenFiscal = this.configuracionService.consultarRegimenFiscal(regimenFiscalEnum.getId()); //Versión
    		JsonArray listRegimenfiscal = new JsonArray();
    		if (listRegimenFiscal != null) {
	    		listRegimenFiscal.getData().forEach(item -> {
	    			JsonObject itemJson = new JsonObject();
	    			itemJson.addProperty("id", item.getRegimenfiscal());
	    			itemJson.addProperty("descripcion", item.getDescripcion());
	    			listRegimenfiscal.add(itemJson);
	    		});
    		}    		
    		clienteJson.add("listRegimenfiscal", listRegimenfiscal);
    		
    		result = gson.toJson(clienteJson);

    		return result;
    		
		} catch (Exception e) {
			errorComponent.setRfc(rfc);
			errorComponent.setPagina("getInformacionCliente");
			errorComponent.guardarLog(e);
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	@Transactional
	public void saveClientes(ClientesEntity model) {
		clientesRepository.save(model);
		actividadesModel.registrarActividad(14, null, "GuardaEdicionCliente");
	}
	
	@Transactional
	public boolean isExistRfc(String rfc) {
		boolean result = false;
		ClientesEntity cliente = clientesRepository.findByRfc(seguridadService.encriptar(rfc));
		if (cliente != null) result = true;
		return result;
	}
	
	@Override
	public String validarCodigoPostal(String codigoPostal) {
		
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        
        try {
        	
        	if (UtilsNumber.isNumeric(codigoPostal)) {
        		ClientResponseTYPE<CodigoPostal> respCP = this.configuracionService.consultarCodigoPostal(codigoPostal);
            	clienteJson.addProperty("codigoPostalEstatus", respCP.getRespuesta().getCodigo());
            	
            	if (!respCP.getRespuesta().getCodigo().equals("1")) {
            		clienteJson.addProperty("codigoPostalInvalidoMsg", catMensajesService.get(ECatalogoMensajes.CODIGO_POSTAL_INVALIDO.getId()).getDescripcionMensaje() );
            	}
        		result = gson.toJson(clienteJson);
        	}
    		return result;
		} catch (Exception e) {
			errorComponent.setCodigoPostal(codigoPostal);
			errorComponent.setPagina("validarCodigoPostal");
			errorComponent.guardarLog(e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String consultarVersionCFDI(Integer idAplicacion) {
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        	
		ClientResponseTYPE<VersionCfdi> resp = this.configuracionService.consultarVersionCFDI(idAplicacion);
    	clienteJson.addProperty("versionCFDIEstatus", resp.getRespuesta().getCodigo());
    	if (resp.getData() != null) {
    		VersionCfdi cfdi = resp.getData();
    		clienteJson.addProperty("versionCFDI", cfdi.getVersion());
    	}
    	result = gson.toJson(clienteJson);
		return result;
	}

	@Override
	public String validarRegimenCapital(String razonSocial) {
		String result = "";
		Gson gson= new Gson(); 
        JsonObject clienteJson = new JsonObject();
        
        try {
        	
    		ClientResponseTYPE<RegimenCapital> resp = this.configuracionService.validarRegimenCapital(razonSocial);
        	clienteJson.addProperty("regimenCapitalEstatus", resp.getRespuesta().getCodigo());
        	
        	if (!resp.getRespuesta().getCodigo().equals("1")) {
        		clienteJson.addProperty("regimenCapitalInvalidoMsg", catMensajesService.get(ECatalogoMensajes.REGIMEN_CAPITAL_INVALIDO.getId()).getDescripcionMensaje() );
        	}
    		result = gson.toJson(clienteJson);
    		return result;
		} catch (Exception e) {
			errorComponent.setPagina("validarRegimenCapital");
			errorComponent.guardarLog(e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String consultarVersionCFDIEspecifico(String ticket, String version, String vvee) {
		String result = "";
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
        String versionEsp = version;
        logger.info("consultarVersionCFDIEspecifico");
        logger.info("ticket: " + ticket);
        logger.info("version: " + version);
        logger.info("vvee:" + vvee);
        if (version.equals(EVersionCFDI.VERSION_40.getVersion())) {
        	if (vvee.equals("SI")) {
            	
        		logger.info("Entra a venta empresa");
            	//Si es orden de compra se obtiene ticket
            	if(ticket != null && ticket.length()<12) {
            		logger.info("Es orden de compra");
    	        	ClienteObtenerTicketRespTYPE obtenerTicket = facturaService.obtenerTicket(ticket);
    	    		if (obtenerTicket.getRespuesta().getCodigo().equals("1")) {
    	    			ticket = obtenerTicket.getRespuesta().getDescripcion();
    	    			logger.info("Ticket: " + ticket);
    	    		}
            	}
            	
            	//Si la orden de compra no cumple con el canal se deja pasar con la versión del aplicativo y se manda a 24HRS
            	if (ticket.length() > 12) {
            		logger.info("NO es orden de compra, se valida la tienda");
	            	int tienda = Integer.parseInt(ticket.substring(8, 12));
	            	logger.info("Tienda: " + tienda);
	    			int idAplicacion = EAplicacion.PortalInHouseVVEE.getValor();
	    			int exist = tiendas40Repository.existeTienda(tienda, idAplicacion);
	    			logger.info("uspExisteTienda40");
	    			logger.info("idAplicacion: " + idAplicacion);
	    			logger.info("exist: " + exist);
	    			
	    			if (exist == 0) {
	    				versionEsp = EVersionCFDI.VERSION_33.getVersion();
	    			} else {
	    				versionEsp = EVersionCFDI.VERSION_40.getVersion();
	    			}
            	}
            } else {
            	logger.info("NO es venta empresa");
	        	if(ticket != null && ticket.length()<12) {
	        		logger.info("Es orden de compra");
	        		ClienteObtenerTicketRespTYPE obtenerTicket = facturaService.obtenerTicket(ticket);
	        		if (obtenerTicket.getRespuesta().getCodigo().equals("1")) {
	        			ticket = obtenerTicket.getRespuesta().getDescripcion();
	        			logger.info("Ticket: " + ticket);
	        		}
	        	}
	        	
	        	//Si la orden de compra no cumple con el canal se deja pasar con la versión del aplicativo y se manda a 24HRS
            	if (ticket.length() > 12) {
            		logger.info("NO es orden de compra, se valida la tienda");
		        	int tienda = Integer.parseInt(ticket.substring(8, 12));
		        	logger.info("Tienda: " + tienda);
	    			int idAplicacion = EAplicacion.PortalInHouse.getValor();
					int exist = tiendas40Repository.existeTienda(tienda, idAplicacion);
					logger.info("uspExisteTienda40");
	    			logger.info("idAplicacion: " + idAplicacion);
	    			logger.info("exist: " + exist);
					if (exist == 0) {
						versionEsp = EVersionCFDI.VERSION_33.getVersion();
					}
            	}
            }
        	
        	
        	//Si la versión sigue siendo la 4.0 se valida la forma de Pago tanto para vvee y portal in house
			if (versionEsp.equals(EVersionCFDI.VERSION_40.getVersion())) {
				logger.info("Continuamos con la versión 4.0, se valida la forma de pago");
				ClienteTicketObtenerExpRespTYPE ticketResp = obtenerTicketService.getTicket(ticket);
				
				if (ticketResp != null && 
					ticketResp.getComprobante() != null && 
					ticketResp.getComprobante().getFormaPago() != null) {
					
					String formaPago = ticketResp.getComprobante().getFormaPago();
					logger.info("formaPago: " + formaPago);
					int existeFormaPago33 = formaPago33Respository.existeFormaPago33(Integer.valueOf(formaPago));
					logger.info("call uspExisteFormaPago33 (:idFormaPago)");
					logger.info("existeFormaPago33: " + existeFormaPago33);
					if (existeFormaPago33 > 0) {
						versionEsp = EVersionCFDI.VERSION_33.getVersion();
					}
				}
			}
		}
        
        logger.info("Version FINAL: " + versionEsp);
        clienteJson.addProperty("versionCFDIEstatus","1");
		clienteJson.addProperty("versionEspecificaCFDI", versionEsp);
        result = gson.toJson(clienteJson);
		return result;
	}
	
	@Override
	public String consultarUsoCfdiVersion(Integer idTipoPersona, Integer idVersionCfdi, String regimenFiscal) {
		String result = "";
		Gson gson= new Gson(); 
        JsonObject clienteJson = new JsonObject();
        try {
    		ClientResponseTYPE<List<UsoDeCfdi>> resp = new ClientResponseTYPE<>();
    		JsonArray list = new JsonArray();
    		
    		if (EVersionCFDI.VERSION_40.getId() == idVersionCfdi) { 
	    		resp = this.configuracionService.consultarUsoCfdi40(idTipoPersona, regimenFiscal);
	        	clienteJson.addProperty("respUsoCfdi", resp.getRespuesta().getCodigo());
	        	
	        	if (resp.getRespuesta().getCodigo().equals("1")) {
	        		List<UsoDeCfdi> listUsoCfdi = resp.getData();
	        		if (listUsoCfdi != null) {
		        		for (UsoDeCfdi usoCfdi : listUsoCfdi) {
		        			JsonObject itemJson = new JsonObject();
		        			itemJson.addProperty("id", usoCfdi.getClave());
		        			itemJson.addProperty("descripcion", usoCfdi.getDescripcionUso());
		        			list.add(itemJson);
		        		}
	        		}
	        	}
    		} else {
    			resp = this.configuracionService.consultarUsoCfdi33(idTipoPersona);
	        	clienteJson.addProperty("respUsoCfdi", resp.getRespuesta().getCodigo());
	        	
	        	if (resp.getRespuesta().getCodigo().equals("1")) {
	        		List<UsoDeCfdi> listUsoCfdi = resp.getData();
	        		if (listUsoCfdi != null) {
		        		for (UsoDeCfdi usoCfdi : listUsoCfdi) {
		        			JsonObject itemJson = new JsonObject();
		        			itemJson.addProperty("id", usoCfdi.getClave());
		        			itemJson.addProperty("descripcion", usoCfdi.getDescripcionUso());
		        			list.add(itemJson);
		        		}
	        		}
	        	}
    		}
        	clienteJson.add("usosCfdi", list);
    		result = gson.toJson(clienteJson);
    		return result;
		} catch (Exception e) {
			errorComponent.setPagina("validarRegimenCapital");
			errorComponent.guardarLog(e);
			e.printStackTrace();
			return null;
		}
	}
}
