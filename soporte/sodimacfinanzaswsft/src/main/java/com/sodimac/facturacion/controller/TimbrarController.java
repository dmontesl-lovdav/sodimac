package com.sodimac.facturacion.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sodimac.facturacion.cliente.BodyTimbrarTipoTimbradoTYPE;
import com.sodimac.facturacion.cliente.BodyTimbrarVersionTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.cliente.BodyCancelarTYPE;
import com.sodimac.facturacion.cliente.BodyClienteTemporalTYPE;
import com.sodimac.facturacion.cliente.BodyConsultarFacturaIdTYPE;
import com.sodimac.facturacion.cliente.BodyCrearZipTYPE;
import com.sodimac.facturacion.cliente.BodyNCTYPE;
import com.sodimac.facturacion.cliente.BodyObtenerDetalleTicketTYPE;
import com.sodimac.facturacion.cliente.BodyObtenerTicketTYPE;
import com.sodimac.facturacion.cliente.BodyRetimbrarTYPE;
import com.sodimac.facturacion.cliente.ClienteCancelarCfdiExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteConsultarFacturaIdExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteConsultarFacturaIdExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteCrearZipExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteLoginExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteObtenerDetalleTicketExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteObtenerDetalleTicketExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteObtenerTicketExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteTicketRetimbrarExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteTimbrarTipoExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteTimbrarTipoExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteTimbrarVersionExpReqTYPE;
import com.sodimac.facturacion.cliente.BodyComplementoCorreoTYPE;
import com.sodimac.facturacion.cliente.BodyComprobanteTYPE;
import com.sodimac.facturacion.cliente.ws.model.ClientResponseTYPE;
import com.sodimac.facturacion.cliente.ws.model.RespuestaClient;
import com.sodimac.facturacion.clientews.configuracion.VersionTimbradoRes;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml;
import com.sodimac.facturacion.cliente.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.cliente.ClienteTicketTimbrarNCExpReqTYPE;
import com.sodimac.facturacion.cliente.ClienteTimbrarComplementoExpReqTYPE;
import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.dto.ConfiguracionWsDto;
import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.entity.fac.FacturasEntity;
import com.sodimac.facturacion.entity.fac.catalogospdf.CatMetodoPagoEntity;
import com.sodimac.facturacion.entity.ws.ConfiguracionWsEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.CompTemporalModel;
import com.sodimac.facturacion.models.DescuentosRebatesModel;
import com.sodimac.facturacion.models.RespuestaPac;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.service.CatMensajesService;
import com.sodimac.facturacion.service.CatUsosCfdiService;
import com.sodimac.facturacion.service.ClientesService;
import com.sodimac.facturacion.service.ComplementosService;
import com.sodimac.facturacion.service.ConfiguracionFacturacionService;
import com.sodimac.facturacion.service.ConfiguracionWsService;
import com.sodimac.facturacion.service.CorreoComplementoService;
import com.sodimac.facturacion.service.CorreoFacturacionService;
import com.sodimac.facturacion.service.DocumentoService;
import com.sodimac.facturacion.service.FacturasService;
import com.sodimac.facturacion.service.RebatesService;
import com.sodimac.facturacion.service.SeguridadService;
import com.sodimac.facturacion.service.TicketsBctService;
import com.sodimac.facturacion.service.TicketsService;
import com.sodimac.facturacion.service.catalogospdf.CatMetodoPagoService;
import com.sodimac.facturacion.service.ws.Emision40Service;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.UtilsFechas;
import com.sodimac.facturacion.util.UtilsFile;
import com.sodimac.facturacion.util.UtilsString;
import com.sodimac.facturacion.util.enums.EAplicacion;
import com.sodimac.facturacion.util.enums.ECodigo;
import com.sodimac.facturacion.util.enums.EProceso;
import com.sodimac.facturacion.util.enums.EVersionCFDI;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
class TimbrarController {
 
	Logger logger = LoggerFactory.getLogger(TimbrarController.class);
	static HashMap<String, String> configuracionBct = new HashMap<>();
	
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ConfiguracionFacturacionService configFacService;
    @Autowired
    private ConfiguracionWsService configuracionWsService;
    @Autowired
    private DocumentoService documentoService;
	@Autowired
	private ClientesService clientesService;
	@Autowired
	private CatUsosCfdiService catUsosCfdiService;
	@Autowired
	private TicketsService ticketsService;
	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private CatMensajesService catMensajesService;
	@Autowired
	private FacturasService facturasService;
	@Autowired
	private RebatesService rebatesService;
	@Autowired
	private CatMetodoPagoService catMetodoPagoService;
	@Autowired
	private ComplementosService complementosService;
	
	@Autowired
	private CorreoFacturacionService correoFacturacionService;
	
	@Autowired
	private CorreoComplementoService correoComplementoService;
	
	@Autowired
	private Emision40Service emision40Service;
	   
	@Autowired
	private SeguridadService seguridadService;

	@Autowired
	private TicketsBctService ticketsBctService;

	@PostMapping("/login")
	@ApiOperation(value = "Logeo de un usuario con su contraseña", notes = "Este m\u00e9todo permite a un usuario logearse generando un token que le permitir\u00e1 accesar los m\u00e9todos a los cuales tiene acceso su perfil.")
	public String login(@RequestBody ClienteLoginExpReqTYPE request) {
		return "";
	}
	
    @GetMapping("/obtenerConfiguracion")
    @ApiOperation(value = "Obtener los par\u00e1metros de configuraci\u00f3n", notes = "Este m\u00e9todo permite obtener los par\u00e1metros de configuraci\u00f3n del servicio.")
    public List<ConfiguracionWsDto> obtenerConfiguracion() {

    	try {
	    	List<ConfiguracionWsEntity> list = configuracionWsService.getAll(); 
	        return list.stream()
	        		.map(this::convertToDto)
	        		.collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("obtenerConfiguracion ", e);
		}
    	
        return null;
        
    }

    private ConfiguracionWsDto convertToDto(ConfiguracionWsEntity post) {
    	ConfiguracionWsDto dto = modelMapper.map(post, ConfiguracionWsDto.class);
        return dto;
    }
    
	boolean validarDatosGenerarFactura (String documento, String monto, String rfc, String razonSocial, String usoCfdi, String metodoPago, String email, String emailCC, String nombreObra, String responsableObra, String guardarDatos) {

		if (documento.isEmpty() || monto.isEmpty() || rfc.isEmpty() || razonSocial.isEmpty() || usoCfdi.isEmpty() || email.isEmpty() || metodoPago.isEmpty()) {
			return false;
		} 
		
    	if (!documentoService.validarExpresionRegular(documento)) {
    		return false;
		}
    	
    	if (!ticketsService.validarMontoExpresionRegular(monto)) {
    		return false;
		} else {
			BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
			BigDecimal cero = BigDecimal.valueOf(Double.parseDouble("0"));
			if (total.equals(cero)) {
				return false;
			}
		}

    	if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return false;
		}
		
		if (!clientesService.validarRZExpresionRegular(razonSocial)) {
			return false;
		}
		
		/* DML No valida uso de CFDI hasta ver cancelación
		UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi, EVersionCFDI.VERSION_33.getId());
		if (usosCfdi == null) {
			return false;
		}*/
		
		CatMetodoPagoEntity metodosPago = catMetodoPagoService.getByIdFormaPago(metodoPago);
		if (metodosPago == null) {
			return false;
		}
		
		if (!clientesService.validarEmailExpresionRegular(email)) {
			return false;
		}
		
		if (!emailCC.isEmpty() && !clientesService.validarEmailExpresionRegular(emailCC)) {
			return false;
		}
		
		if (!nombreObra.isEmpty() && !clientesService.validarObraExpresionRegular(nombreObra)) {
			return false;
		}
		
		if (!responsableObra.isEmpty() && !clientesService.validarResponsableObraExpresionRegular(responsableObra)) {
			return false;
		}
		
		if (!guardarDatos.isEmpty() && (!guardarDatos.equalsIgnoreCase("true") && !guardarDatos.equalsIgnoreCase("false"))) {
			return false;
		}
		
		// Añadir validaciones para cada dato
		return true;
	}

	@PostMapping("/timbrarNCTicket")
    @ApiOperation(value = "Timbrar un ticket de devoluci\u00f3n u orden de compra", notes = "Este m\u00e9todo permite timbrar un ticket de devoluci\u00f3n u orden de compra generando la nota de cr\u00e9dito respectiva. El c\u00f3digo igual a 1 significa que la nota de cr\u00e9dito se gener\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE timbrarNCTicket(@RequestBody ClienteTicketTimbrarNCExpReqTYPE request) {
    	
		logger.info("timbrarNCTicket");
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	BodyNCTYPE body = request.getBody();
    	String ticket = "";
    	
    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
	    	String documento = (body.getDocumento()==null)? "": body.getDocumento().trim();
	    	String monto = (body.getMonto()==null)? "": body.getMonto().trim();
	    	String email = (body.getCorreo()==null)? "": body.getCorreo().trim();
	    	String emailCC = (body.getCorreoCC()==null)? "": body.getCorreoCC().trim();
	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosGenerarNC(documento, monto, email, emailCC)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
				return response;
	    	}
	
	    	int autorizoGuardado = 0;
	    	BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
	    	String ticketBct = documento;
	    	ticket = documento;
	    	logger.info("ticketBct: " + ticketBct);
	    	
			if (documento.trim().length()==10) {
				ticketBct = documentoService.obtenerTicketOrdenCompra(body.getDocumento().trim());
				if (ticketBct.isEmpty()){
		    		UtilsApi.setRespuesta(response, ECodigo.DocumentoInvalido.getValor());
					return response;
				}		
			}

    		//String version = "";
			ClientesTemporalModel model = new ClientesTemporalModel();
        	facturasService.getInformacionFacturaRelacionadaByTicket(ticket, model);
        	
        	if (model.getRfc().isEmpty()) {
        		UtilsApi.setRespuestaFacturacion(response, 117);
    			return response;        		
        	}
        	if (!email.isEmpty()) {
        		model.setEmail(email);
        	}
        	model.setTicket(ticket);
        	model.setTicketBct(ticketBct);
        	model.setTotal(total);
        	model.setEmailCC(emailCC);
        	model.setAutorizoGuardado(autorizoGuardado);
        	//model.setVersionFacturacionSat(version);
        	model.setIdAplicacion(EAplicacion.NotasCredito.getValor());
            
            actividadesModel.setTicket(ticket);
            actividadesModel.setRfc(model.getRfc());
            errorComponent.setIdFacturaPac(0);

            int codigoRetorno = facturasService.timbrarTipoProceso(model, EProceso.TimbradoNormal.getValor());
			
        	if (codigoRetorno == 300) {
        		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
        	} else {
        		UtilsApi.setRespuesta(response, codigoRetorno, model);
        	}
			
		} catch (Exception e) {
			logger.error("Ticket " + ticket + ": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }

	boolean validarDatosGenerarNC (String documento, String monto, String email, String emailCC) {

		if (documento=="" || monto=="") {
			return false;
		} 
		
    	if (!documentoService.validarExpresionRegular(documento)) {
    		return false;
		}
    	
    	if (!ticketsService.validarMontoExpresionRegular(monto)) {
    		return false;
		} else {
			BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
			BigDecimal cero = BigDecimal.valueOf(Double.parseDouble("0"));
			if (total.equals(cero)) {
				return false;
			}
		}
		
		if (!email.isEmpty() && !clientesService.validarEmailExpresionRegular(email)) {
			return false;
		}
		
		if (!emailCC.isEmpty() && !clientesService.validarEmailExpresionRegular(emailCC)) {
			return false;
		}
		
		// Añadir validaciones para cada dato
		return true;
	}
	
    @PostMapping("/cancelarCfdi")
    @ApiOperation(value = "Cancelar un comprobante fiscal digital", notes = "Este m\u00e9todo permite cancelar un comprobante fiscal en base a su Factura Id. El c\u00f3digo igual a 1 significa que el comprobante fiscal fu\u00e9 cancelado satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE cancelarCfdi(@RequestBody ClienteCancelarCfdiExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	BodyCancelarTYPE body = request.getBody();
    	String facturaId = "";

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
	    	facturaId = (body.getFacturaId()==null)? "": body.getFacturaId().trim();
	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosCancelar(facturaId)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
				return response;
	    	}
		    	
        	ClientesTemporalModel model = new ClientesTemporalModel();
        	model.setIdFacturaPac(Integer.parseInt(facturaId));
            errorComponent.setIdFacturaPac(Integer.parseInt(facturaId));

            int codigoRetorno = facturasService.cancelar(facturaId, model);
			
        	if (codigoRetorno == 300) {
        		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
        	} else {
        		UtilsApi.setRespuesta(response, codigoRetorno, model);
        	}
			
		} catch (Exception e) {
			logger.error("facturaId " + facturaId + ": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }	

	boolean validarDatosCancelar (String facturaId) {

		if (facturaId=="") {
			return false;
		} 
		
	     Pattern pat = Pattern.compile("\\d+");
	     Matcher mat = pat.matcher(facturaId);                                                                           
	     if (mat.matches()) {
	    	 int factura = Integer.parseInt(facturaId);
	    	 if (factura==0) {
	    		 return false;
	    	 }
	     } else {
	    	 return false;                                                                               
	     }
				
		// Añadir validaciones para cada dato
		return true;
	}
    
	@PostMapping("/retimbrarTicket")
    @ApiOperation(value = "Re-timbrar un ticket de venta u orden de compra", notes = "Este m\u00e9todo permite re-timbrar un ticket de venta u orden de compra generando la factura respectiva. El c\u00f3digo igual a 1 significa que la factura se gener\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE retimbrarTicket(@RequestBody ClienteTicketRetimbrarExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	BodyRetimbrarTYPE body = request.getBody();
    	String ticket = "";

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {	    	
	    	String documento = (body.getDocumento()==null)? "": body.getDocumento().trim();
	    	String monto = (body.getMonto()==null)? "": body.getMonto().trim();
	    	String rfc = (body.getRfc()==null)? "": body.getRfc().toUpperCase().trim();
	    	String razonSocial = (body.getRazonSocial()==null)? "": body.getRazonSocial().trim();
	    	String usoCfdi = (body.getUsoCfdi()==null)? "": body.getUsoCfdi().toUpperCase().trim();
	    	String metodoPago = (body.getMetodoPago()==null)? "": body.getMetodoPago().trim();
	    	String email = (body.getCorreo()==null)? "": body.getCorreo().trim();
	    	String emailCC = (body.getCorreoCC()==null)? "": body.getCorreoCC().trim();
	    	String nombreObra = (body.getNombreObra()==null)? "": body.getNombreObra().trim();
	    	String responsableObra = (body.getResponsableObra()==null)? "": body.getResponsableObra().trim();
	    	String idFacturaPac = (body.getIdFacturaPac()==null)? "": body.getIdFacturaPac().trim();
	    	String guardarDatos = (body.getAutorizoGuardado()==null)? "": body.getAutorizoGuardado().trim();
	    	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosGenerarFactura(documento, monto, rfc, razonSocial, usoCfdi, metodoPago, email, emailCC, nombreObra, responsableObra, guardarDatos)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
	    	int autorizoGuardado = 0;
	    	if (guardarDatos.equalsIgnoreCase("true")) autorizoGuardado = 1;
	    	BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
	    	String ticketBct = documento;
	    	ticket = documento;
	    	
			if (documento.trim().length()==10) {
				ticketBct = documentoService.obtenerTicketOrdenCompra(body.getDocumento().trim());
				if (ticketBct.isEmpty()){
		    		UtilsApi.setRespuesta(response, ECodigo.DocumentoInvalido.getValor());
					return response;
				}		
			}
			
			int idAplicacion = 9;
			VersionTimbradoRes result = facturasService.obtenerVersionActiva(idAplicacion);
			
			String version = "";
			if (!result.getRespuesta().getCodigo().equals("1")) {
	    		UtilsApi.setRespuesta(response, 116);
				return response;				
			}
			version = result.getData().getVersion();
			
            actividadesModel.setTicket(ticket);
            actividadesModel.setRfc(rfc);
            errorComponent.setIdFacturaPac(0);

        	ClientesTemporalModel model = new ClientesTemporalModel();
        	model.setTicket(ticket);
        	model.setTicketBct(ticketBct);
        	model.setTotal(total);
        	model.setRfc(rfc);
        	model.setRazonSocial(razonSocial);
        	model.setIdUsoCfdi(usoCfdi);
        	model.setMetodoPago(metodoPago);
        	model.setEmail(email);
        	model.setEmailCC(emailCC);
        	model.setNombreObra(nombreObra);
        	model.setResponsableObra(responsableObra);
        	model.setIdFacturaPac(Integer.parseInt(idFacturaPac));
        	model.setAutorizoGuardado(autorizoGuardado);
        	model.setVersionFacturacionSat(version);
        	model.setIdAplicacion(EAplicacion.Refacturacion.getValor());

        	FacturasEntity factura = facturasService.getFacturaByIdFacturaPac(idFacturaPac);
        	if (factura == null) {
        		UtilsApi.setRespuestaFacturacion(response, 120);
        		return response;
        	}
//        	Esta validaci\u00f3n se omite ya que no importa si esta cancelada la factura o no
//        	if (factura.getAcuse().isEmpty()) {
//	    		UtilsApi.setRespuestaFacturacion(response, 123);
//	    		return response;
//        	}
        	
    		//Validar fecha del ticket en Bct
    		TicketEntity ticketBctHdrVal = ticketsBctService.findByTicket(ticket);
    		if (ticketBctHdrVal == null) {
        		UtilsApi.setRespuestaFacturacion(response, 120);
        		return response;
    		}
    		
    		int diasPermitidos = Integer.parseInt(configFacService.getConfig().get("Aplicacion.DiasPermitidosReFacturar")) * -1;
    		String fechaActual = UtilsFechas.getLocalDate().toString() + " 00:00:00";
    		Date fechaActualDate = UtilsFechas.convertirDate(fechaActual, "yyyy-MM-dd HH:mm:ss");
    		Date fechalimite = DateUtils.addDays(fechaActualDate, diasPermitidos);
    		if (ticketBctHdrVal.getFecha().before(fechalimite)) {
        		int codigoRetorno = 125;
        		String mensaje = catMensajesService.get(codigoRetorno).getDescripcionMensaje();
        		mensaje = mensaje.replace("{dias}", Integer.toString(diasPermitidos));
        		UtilsApi.setRespuesta(response, codigoRetorno, mensaje);	
        		return response;        		
    		}

        	model.setUuidRelacionado(factura.getUuid());
        	
        	int codigoRetorno = facturasService.timbrarTipoProceso(model, EProceso.Refacturacion.getValor());
			
        	if (codigoRetorno == 300) {
        		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
        	} else {
        		UtilsApi.setRespuesta(response, codigoRetorno, model);
        	}
			
		} catch (Exception e) {
			logger.error("Ticket " + ticket + ": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }

	@PostMapping("/timbrarRebates")
    @ApiOperation(value = "Timbrar rebates", notes = "Este m\u00e9todo permite timbrar rebates generando la factura respectiva. El c\u00f3digo igual a 1 significa que la factura se gener\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE timbrarTicket() {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	
    	try {
    		actividadesModel.inicializar();
    		errorComponent.inicializar();
    	    	    	
	    	List<DescuentosRebatesModel> list = rebatesService.getDescuentos();
	
	    	if (list.size() == 0) {
	    		UtilsApi.setRespuestaFacturacion(response, 127);	
	    		return response;
	    	}
	    	
	    	List<String> ticketsError = new ArrayList <String>();
	    	
	    	for (DescuentosRebatesModel item:list) {
	    		procesarDescuento(item, ticketsError);    		
	    	}
	    	
        	if (ticketsError.size() == 0) {
        		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
        	} else {
        		int codigoRetorno = 126;
        		String mensaje = catMensajesService.get(codigoRetorno).getDescripcionMensaje();
        		mensaje = mensaje.replace("{tickets}", StringUtils.join(ticketsError, ", "));
        		UtilsApi.setRespuesta(response, codigoRetorno, mensaje);	
        	}
        	
		} catch (Exception e) {
			logger.error("timbrarRebates: ", e);
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	    	
    	return response;
    }
    
    private void procesarDescuento (DescuentosRebatesModel rebate, List<String> ticketsError) {

    	String usoCfdiDefault = "G03";
    	String emailDefault = rebate.getCorreo();
		String ticket = rebate.getTicket();
		String rfc = rebate.getRfcProveedor();
		String razonSocial = rebate.getNombreProveedor();
		String regimenFiscal = rebate.getRegimenFiscalProveedor();
		String codigoPostal = rebate.getCodigoPostalProveedor();
		String usoCfdi = usoCfdiDefault;
		String email = emailDefault;
		String emailCC = "";
		String nombreObra = "";
		String responsableObra = "";

    	int autorizoGuardado = 0;
    	BigDecimal total = BigDecimal.valueOf(rebate.getTotal());
    	
		try {

			VersionTimbradoRes result = facturasService.obtenerVersionActiva(EAplicacion.Rebates.getValor());

			String version = "";
			if (!result.getRespuesta().getCodigo().equals("1")) {
	        	String mensaje = "API de configuracion no disponible";
	        	ticketsError.add(mensaje);
				return;				
			}
			version = result.getData().getVersion();
			
			EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(version); 
			UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi, eVersionCFDI.getId());

			ClientesTemporalModel model = new ClientesTemporalModel();
        	
            actividadesModel.setTicket(ticket);
            actividadesModel.setRfc(rfc);
            errorComponent.setIdFacturaPac(0);

        	model.setTicket(ticket);
        	model.setTicketBct(ticket);
        	model.setTotal(total);
        	model.setRfc(rfc);
        	model.setRazonSocial(razonSocial);
        	model.setRegimenFiscal(regimenFiscal);
        	model.setCodigoPostal(codigoPostal);
        	model.setIdUsoCfdi(usoCfdi);
        	model.setEmail(email);
        	model.setEmailCC(emailCC);
        	model.setNombreObra(nombreObra);
        	model.setResponsableObra(responsableObra);
        	model.setAutorizoGuardado(autorizoGuardado);
        	model.setUuidRelacionado("");
        	model.setRebate(rebate);
        	model.setVersionFacturacionSat(version);
        	model.setIdAplicacion(EAplicacion.Rebates.getValor());
        	model.setIdUsoCfdiReal(usosCfdi.getIdUsoCfdi());
        	
        	int codigoRetorno = facturasService.timbrarTipoProceso(model, EProceso.Rebates.getValor());
			
        	if (codigoRetorno != 300) {
	        	String mensaje = catMensajesService.get(codigoRetorno).getDescripcionMensaje();
	        	mensaje = mensaje.replace("{ticket}", model.getTicket()).replace("{rfc}", model.getRfc());
	        	
	        	ticketsError.add("[" + ticket + "] " + mensaje);
        	}
			
		} catch (Exception e) {
			logger.error("procesarDescuento ", e);
			ticketsError.add(ticket + ": Ocurrio una excepcion");
		}            		
		
    }
    
    @PostMapping("/crearZip")
    @ApiOperation(value = "Crear archivo .ZIP a partir del uuid ", notes = "Este m\u00e9todo permite crear un archivo comprimido conteniendo el pdf y el xml correspondientes al folio fiscal. El archivo se depositar\u00e1 en la carpeta especificada en el parametro: 'Mail.PathFile'. El c\u00f3digo igual a 1 significa que elarchivo se creo satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE crearZip(@RequestBody ClienteCrearZipExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	 BodyCrearZipTYPE body = request.getBody();
    	 String uuid = "";

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
    	
	    	uuid = (body.getUuid()==null)? "": body.getUuid().trim();
	    	
			if (uuid.isEmpty()) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
			facturasService.crearZip(uuid);
			
       		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
			
		} catch (Exception e) {
			logger.error("Uuid " + uuid +": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }
    
    @PostMapping("/crearPdf")
    @ApiOperation(value = "Crear archivo .PDF a partir del uuid ", notes = "Este m\u00e9todo permite crear el archivo pdf correspondientes al folio fiscal. El archivo se depositar\u00e1 en la carpeta especificada en el parametro: 'Mail.PathFile'. El c\u00f3digo igual a 1 significa que elarchivo se creo satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE crearPdf(@RequestBody ClienteCrearZipExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	 BodyCrearZipTYPE body = request.getBody();
    	 String uuid = "";

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
    	
	    	uuid = (body.getUuid()==null)? "": body.getUuid().trim();
	    	
			if (uuid.isEmpty()) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
			facturasService.crearPdf(uuid);
			
       		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
			
		} catch (Exception e) {
			logger.error("Uuid " + uuid +": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }    
     	
    @PostMapping("/crearPdfComplemento")
    @ApiOperation(value = "Crear archivo .PDF a partir del uuid del complemento", notes = "Este m\u00e9todo permite crear el archivo pdf correspondientes al folio fiscal del complemento. El archivo se depositar\u00e1 en la carpeta especificada en el parametro: 'Mail.PathFile'. El c\u00f3digo igual a 1 significa que elarchivo se creo satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE crearPdfComplemento(@RequestBody ClienteCrearZipExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	 BodyCrearZipTYPE body = request.getBody();
    	 String uuid = "";

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
    	
	    	uuid = (body.getUuid()==null)? "": body.getUuid().trim();
	    	
			if (uuid.isEmpty()) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
			facturasService.crearPdfComplemento(uuid);
			
       		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
			
		} catch (Exception e) {
			logger.error("Uuid " + uuid +": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }    

    private boolean validarTipoProcesoExpresionRegular(String tipoProceso) {
        //El proceso de refacturacion (4) y de rebates (5) no se consideran en el metodo de Timbrar
		Pattern pat = Pattern.compile("[01236]");
        Matcher mat = pat.matcher(tipoProceso);
        return mat.matches();
		
	}
	
	private boolean validarIdFacturaPacExpresionRegular(String idFacturaPac) {
        //El proceso de refactguracion y de rebates no se consideran en el metodo de Timbrar
		Pattern pat = Pattern.compile("[0-9]*");
        Matcher mat = pat.matcher(idFacturaPac);
        return mat.matches();
		
	}
 
    @PostMapping("/crearBase64")
    @ApiOperation(value = "Crear archivo .PDF en base 64 a partir del uuid ", notes = "Este m\u00e9todo permite crear el archivo pdf en base 64 correspondientes al folio fiscal. El c\u00f3digo igual a 1 significa que el archivo se creo satisfactoriamente y en la descripcion estar\u00e1 el archivo en base 64, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE crearB64(@RequestBody ClienteCrearZipExpReqTYPE request) {
    	
    	String uuid = "";
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	
    	BodyCrearZipTYPE body = request.getBody();

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
    	
	    	uuid = (body.getUuid()==null)? "": body.getUuid().trim();
	    	
			if (uuid.isEmpty()) {
				UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
			String base64 = facturasService.obtenerBase64(uuid);
			
			UtilsApi.setRespuesta(response, ECodigo.Ok.getValor(), base64);
			
		} catch (Exception e) {
			logger.error("Uuid " + uuid +": ", e);
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }    

    @PostMapping("/crearBase64Complemento")
    @ApiOperation(value = "Crear archivo .PDF en base 64 a partir del uuid del complemento", notes = "Este m\u00e9todo permite crear el archivo pdf en base 64 correspondientes al folio fiscal del complemento. El c\u00f3digo igual a 1 significa que el archivo se creo satisfactoriamente y en la descripcion estar\u00e1 el archivo en base 64, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE crearB64Complemento(@RequestBody ClienteCrearZipExpReqTYPE request) {
    	
    	String uuid = "";
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	
    	BodyCrearZipTYPE body = request.getBody();

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
    	
	    	uuid = (body.getUuid()==null)? "": body.getUuid().trim();
	    	
			if (uuid.isEmpty()) {
				UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
			String base64 = facturasService.obtenerBase64Complemento(uuid);
			
			UtilsApi.setRespuesta(response, ECodigo.Ok.getValor(), base64);
			
		} catch (Exception e) {
			logger.error("Uuid " + uuid +": ", e);
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }    

    @PostMapping("/timbrarVersion")
    @ApiOperation(value = "Timbrar un ticket de venta u orden de compra.", notes = "Este m\u00e9todo permite timbrar un ticket de venta u orden de compra generando la factura respectiva. El c\u00f3digo igual a 1 significa que la factura se gener\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE timbrar(@RequestBody ClienteTimbrarVersionExpReqTYPE request) {
    	logger.info("Entra al metodo timbrar");
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	BodyTimbrarVersionTYPE body = request.getBody();
    	String documento = "";
    	String ticket = "";

    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
	    	documento = (body.getDocumento()==null)? "": body.getDocumento().trim();
	    	String monto = (body.getMonto()==null)? "": body.getMonto().trim();
	    	String rfc = (body.getRfc()==null)? "": body.getRfc().toUpperCase().trim();
	    	String razonSocial = (body.getRazonSocial()==null)? "": body.getRazonSocial().trim();
	    	String usoCfdi = (body.getUsoCfdi()==null)? "": body.getUsoCfdi().toUpperCase().trim();
	    	String email = (body.getCorreo()==null)? "": body.getCorreo().trim();
	    	String emailCC = (body.getCorreoCC()==null)? "": body.getCorreoCC().trim();
	    	String nombreObra = (body.getNombreObra()==null)? "": body.getNombreObra().trim();
	    	String responsableObra = (body.getResponsableObra()==null)? "": body.getResponsableObra().trim();
	    	String guardarDatos = (body.getAutorizoGuardado()==null)? "": body.getAutorizoGuardado().trim();
	    	String sidFacturaPac = (body.getIdFacturaPac() ==null)? "": body.getIdFacturaPac().trim();
	    	String tipoProceso = (body.getTipoProceso()==null)? "": body.getTipoProceso().trim();
	    	String version = (body.getVersion() ==null)? "": body.getVersion().trim();
	    	String regimenFiscal = (body.getRegimenFiscal() ==null)? "": body.getRegimenFiscal().trim();
	    	String codigoPostal = (body.getCodigoPostal() ==null)? "": body.getCodigoPostal().trim();
	    	
	    	logger.info("Inicia Timbrar ticket: " + documento);
	    	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosTimbrarVersionGenerarFactura(documento, monto, rfc, razonSocial, usoCfdi, email, emailCC, nombreObra, responsableObra, guardarDatos, sidFacturaPac, tipoProceso, version, regimenFiscal, codigoPostal)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
			
			logger.info("Se validaron datos OK ticket: " + documento);
	    	int autorizoGuardado = 0;
	    	if (guardarDatos.equalsIgnoreCase("true")) autorizoGuardado = 1;
	    	boolean sincronizacion = (tipoProceso.equals(Integer.toString(EProceso.Sincronizacion.getValor())));
	    	
	    	
	    	int idFacturaPac = 0;
	    	if (!sidFacturaPac.isEmpty()) idFacturaPac = Integer.parseInt(sidFacturaPac);
	    	
	    	if (monto.isEmpty()) monto = "0";
	    	BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
	    	
	    	String ticketBct = documento;
	    	ticket = documento;
	    	
	    	Integer idCfdiReal = 0;
			if (!sincronizacion) {
				
				if (version != null && !version.isEmpty()) {
					EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(version);
					UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi, eVersionCFDI.getId());
					if (usosCfdi == null) {
						logger.error("XXXX El documento: " + documento + " no cuenta con un uso de cfdi correcto [Debe de llegar una clave]: " + usoCfdi);
						UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
						return response;
					}
					idCfdiReal = usosCfdi.getIdUsoCfdi();
				} else {
					logger.error("XXXX El documento: " + documento + " no cuenta con una version cfdi correcto: " + version);
					UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
					return response;
				}
				
		    	if ( documento.trim().length() == 10) {
					ticketBct = documentoService.obtenerTicketOrdenCompra(body.getDocumento().trim());
					if (ticketBct.isEmpty()){
			    		UtilsApi.setRespuesta(response, ECodigo.DocumentoInvalido.getValor());
						return response;
					}		
				}				
			}
			
			if (version.equals("3.3")) {
				regimenFiscal = "";
				codigoPostal = "";
			}
			
            actividadesModel.setTicket(ticket);
            actividadesModel.setRfc(rfc);
            errorComponent.setIdFacturaPac(0);

        	ClientesTemporalModel model = new ClientesTemporalModel();
        	model.setTicket(ticket);
        	model.setTicketBct(ticketBct);
        	model.setTotal(total);
        	model.setRfc(rfc);
        	model.setRazonSocial(razonSocial);
        	model.setIdUsoCfdi(usoCfdi);
        	model.setIdUsoCfdiReal( idCfdiReal );
        	model.setEmail(email);
        	model.setEmailCC(emailCC);
        	model.setNombreObra(nombreObra);
        	model.setResponsableObra(responsableObra);
        	model.setAutorizoGuardado(autorizoGuardado);
        	model.setIdFacturaPac(idFacturaPac);
        	model.setVersionFacturacionSat(version);
        	model.setRegimenFiscal(regimenFiscal);
        	model.setCodigoPostal(codigoPostal);
        	
        	int itipoProceso = Integer.parseInt(tipoProceso);
        	
        	int idAplicacion = EAplicacion.Wsft.getValor();
			if (itipoProceso == EProceso.Autofacturador.getValor()) idAplicacion  = EAplicacion.PortalInHouse.getValor();
			if (itipoProceso == EProceso.PendientePac.getValor() || itipoProceso == EProceso.NoBct.getValor()) idAplicacion  = EAplicacion.ProcesoBatch.getValor();
			if (itipoProceso == EProceso.Refacturacion.getValor()) idAplicacion  = EAplicacion.Refacturacion.getValor();
			if (itipoProceso == EProceso.Rebates.getValor()) idAplicacion  = EAplicacion.Rebates.getValor();
			
			model.setIdAplicacion(idAplicacion);
			logger.info("Entra a timbrarTipoProceso ticket: " + documento );
        	int codigoRetorno = facturasService.timbrarTipoProceso(model, itipoProceso);
			
        	if (codigoRetorno == 300) {
        		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
        	} else {
        		UtilsApi.setRespuesta(response, codigoRetorno, model);        		
        	}
			
        	logger.info("Fin Timbrar ticket: " + documento);
			
		} catch (Exception e) {
			logger.error("Exception Ticket: " + documento + " ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }
    
    boolean validarDatosTimbrarVersionGenerarFactura (String documento, String monto, String rfc, String razonSocial, String usoCfdi, String email, String emailCC, String nombreObra, String responsableObra, String guardarDatos, String idFacturaPac, String stipoProceso, String version, String regimenFiscal, String codigoPostal) {

		if (stipoProceso.isEmpty() || !validarTipoProcesoExpresionRegular(stipoProceso) ) {
			return false;
		}
		
		int tipoProceso = Integer.parseInt(stipoProceso);
		boolean pendientePac = (tipoProceso == EProceso.PendientePac.getValor());
		boolean pendienteNoBct = (tipoProceso == EProceso.NoBct.getValor());
		boolean sincronizacion = (tipoProceso == EProceso.Sincronizacion.getValor());
		boolean batch = (pendienteNoBct || pendientePac || sincronizacion);

		if (!sincronizacion && (version.isEmpty() || !validarVersionExpresionRegular(version))) {
			return false;
		}
		
		if (!sincronizacion && (documento.isEmpty() || rfc.isEmpty() || razonSocial.isEmpty() || usoCfdi.isEmpty() || (!batch && email.isEmpty()) || guardarDatos.isEmpty())) {
			return false;
		}
		if (version.equals("4.0") && (regimenFiscal.isEmpty() || codigoPostal.isEmpty()) ) {
			return false;
		}

		if (!pendientePac && (!documento.isEmpty() && !documentoService.validarExpresionRegular(documento))) {
    		return false;
		}
    	
		if (monto.isEmpty()) monto = "0";
    	if (!ticketsService.validarMontoExpresionRegular(monto)) {
    		return false;
		} //else {
//          2024-09-25 Se comenta esta validacion para la forma de pago "102" dinero electronico (puntos CES)
//			BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
//			BigDecimal cero = BigDecimal.valueOf(Double.parseDouble("0"));
//			if (total.equals(cero) && !batch) {
//				return false;
//			}
//		}

    	if (!rfc.isEmpty() && !clientesService.validarRfcExpresionRegular(rfc)) {
			return false;
		}
		
		if (!razonSocial.isEmpty() && !clientesService.validarRZExpresionRegular(razonSocial)) {
			return false;
		}
		
		if (!usoCfdi.isEmpty()) {
			EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(version);
			UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi, eVersionCFDI.getId());
			if (usosCfdi == null) {
				return false;
			}			
		}
		
		if (!email.isEmpty() && !clientesService.validarEmailExpresionRegular(email)) {
			return false;
		}
		
		if (!emailCC.isEmpty() && !clientesService.validarEmailExpresionRegular(emailCC)) {
			return false;
		}
		
		if (!nombreObra.isEmpty() && !clientesService.validarObraExpresionRegular(nombreObra)) {
			return false;
		}
		
		if (!responsableObra.isEmpty() && !clientesService.validarResponsableObraExpresionRegular(responsableObra)) {
			return false;
		}
		
		if (!guardarDatos.isEmpty() && (!guardarDatos.equalsIgnoreCase("true") && !guardarDatos.equalsIgnoreCase("false"))) {
			return false;
		}
		
		if (((pendientePac || sincronizacion) && idFacturaPac.isEmpty()) || (!idFacturaPac.isEmpty() && !validarIdFacturaPacExpresionRegular(idFacturaPac))) {
			return false;
		}

		if (!regimenFiscal.isEmpty()) {
//			CatUsosCfdiEntity usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi);
//			if (usosCfdi == null) {
//				return false;
//			}			
		}

		if (!codigoPostal.isEmpty()) {
//			CatUsosCfdiEntity usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi);
//			if (usosCfdi == null) {
//				return false;
//			}			
		}
		// Añadir validaciones para cada dato
		return true;
	}    

	private boolean validarVersionExpresionRegular(String version) {
		Pattern pat = Pattern.compile("(3.3|4.0)");
        Matcher mat = pat.matcher(version);
        return mat.matches();
		
	}

    @PostMapping("/timbrarTipo")
    @ApiOperation(value = "Timbrar por tipo de timbrado", notes = "Este m\u00e9todo permite timbrar en base a la clase y al xml en base 64. El Pac activo ser\u00e1 quien realice el timbrado. El c\u00f3digo igual a 1 significa que se timbr\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTimbrarTipoExpRespTYPE timbrarTipo(@RequestBody ClienteTimbrarTipoExpReqTYPE request) {
    	
    	ClienteTimbrarTipoExpRespTYPE response = new ClienteTimbrarTipoExpRespTYPE();
    	BodyTimbrarTipoTimbradoTYPE body = request.getBody();
    	String tipoTimbrado = "";
    	String xmlBase64 = "";

    	if (body == null) {
    		ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
			respuesta.setCodigo(Integer.toString(ECodigo.Error.getValor()));
        	respuesta.setDescripcion("La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
        	response.setRespuesta(respuesta);
			return response;
    	}
    	
    	try {
    	
    		tipoTimbrado = (body.getTipoTimbrado()==null)? "": body.getTipoTimbrado().trim();
    		xmlBase64 = (body.getXmlBase64()==null)? "": body.getXmlBase64().trim();
	    	
			if (tipoTimbrado.isEmpty() || xmlBase64.isEmpty()) {
	    		ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
				respuesta.setCodigo("116");
	        	respuesta.setDescripcion("Algunos de los par\u00e1metros son inv\u00e1lidos, favor de validar.");
	        	response.setRespuesta(respuesta);
	    		return response;
	    	}
	
			int idAplicacion = 9;
			if (tipoTimbrado.contains("G")) idAplicacion = 5;
			VersionTimbradoRes result = facturasService.obtenerVersionActiva(idAplicacion);
			
			String version = "";
			if (!result.getRespuesta().getCodigo().equals("1")) {
	    		ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
				respuesta.setCodigo("116");
	        	respuesta.setDescripcion("Algunos de los par\u00e1metros son inv\u00e1lidos, favor de validar.");
	        	response.setRespuesta(respuesta);
				return response;				
			}
			version = result.getData().getVersion();
			
			if (version.equals("4.0")) {
				return facturasService.timbrarTipo40(tipoTimbrado, xmlBase64);
			} else {
				return facturasService.timbrarTipo(tipoTimbrado, xmlBase64);
			}
						
		} catch (Exception e) {
			logger.error("tipoTimbrado-xmlBase64 " + tipoTimbrado + "-" + xmlBase64 +": ", e);
			
    		ClienteTimbrarTipoExpRespTYPE.Respuesta respuesta = new ClienteTimbrarTipoExpRespTYPE.Respuesta();
			respuesta.setCodigo("2");
        	respuesta.setDescripcion("Error al intentar timbrar");
        	response.setRespuesta(respuesta);
        	response.setFacturaId("-1");
		}            		
    	
    	return response;
    }

    @PostMapping("/obtenerDetalleTicket")
    @ApiOperation(value = "Obtener del detalle de un ticket de venta u orden de compra.", notes = "Este m\u00e9todo permite obtener del detalle de un ticket de venta u orden de compra. El c\u00f3digo igual a 1 significa que la factura se gener\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteObtenerDetalleTicketExpRespTYPE obtenerTicketDetecno(@RequestBody ClienteObtenerDetalleTicketExpReqTYPE request) {
    	
    	ClienteObtenerDetalleTicketExpRespTYPE response = new ClienteObtenerDetalleTicketExpRespTYPE();
    	BodyObtenerDetalleTicketTYPE body = request.getBody();
    	String ticket = "";

    	if (body == null) {
			UtilsApi.setRespuesta(response);
			return response;			
    	}
    	
    	try {
	    	String documento = (body.getDocumento()==null)? "": body.getDocumento().trim();
	    	String pais = (body.getPais()==null)? "": body.getPais().toUpperCase().trim();
	    	String comercio = (body.getComercio()==null)? "": body.getComercio().trim();
	    	String canal = (body.getCanal()==null)? "": body.getCanal().toUpperCase().trim();
	    	String version = (body.getVersion()==null)? "": body.getVersion().trim();
	    	int idAplicacion = (body.getVersion()==null)? 0: body.getIdAplicacion();
	    	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosObtenerDetalleTicket(documento, pais, comercio, canal, version, idAplicacion)) {
				logger.info("Algunos de los par\u00e1metros son inv\u00e1lidos, documento: " + documento);
				UtilsApi.setRespuesta(response);
	    		return response;	    		
	    	}
	
	    	String ticketBct = documento;
	    	ticket = documento;
	    	logger.info("ticketBct: " + ticketBct);
	    	
			if (documento.trim().length()==10) {
				ticketBct = documentoService.obtenerTicketOrdenCompra(body.getDocumento().trim());
				if (ticketBct.isEmpty()){
		    		UtilsApi.setRespuesta(response);
					return response;
				}		
			}
	    	
	    	logger.info("obtener-documento: " + documento);	    	
	    	logger.info("version: " + version);
			
            actividadesModel.setTicket(ticket);
            errorComponent.setIdFacturaPac(0);

			ClientesTemporalModel model = new ClientesTemporalModel();
        	model.setTicket(ticket);
        	model.setTicketBct(ticketBct);
        	model.setPais(pais);
        	model.setComercio(comercio);
        	model.setCanal(canal);
        	model.setVersionFacturacionSat(version);
        	model.setIdAplicacion(idAplicacion);
            
        	ClienteTicketObtenerExpRespTYPE responseWsObtenerTicket = facturasService.obtenerDetalleTicket(model);
			
        	if (responseWsObtenerTicket != null) {
        		ClienteObtenerDetalleTicketExpRespTYPE.Respuesta respuesta = new ClienteObtenerDetalleTicketExpRespTYPE.Respuesta();
				respuesta.setCodigo(Integer.toString(ECodigo.Ok.getValor()));
	        	respuesta.setDescripcion("Ok");
	        	response.setRespuesta(respuesta);
	        	response.setResponseWSObtenerTicket(responseWsObtenerTicket);
        	} else {
        		logger.info("ClienteTicketObtenerExpRespTYPE nulo, documento: " + documento);
        		UtilsApi.setRespuesta(response);
        	}
        	
        	logger.info("obtener-documento-fin: " + documento);
			
		} catch (Exception e) {
			logger.error("Ticket " + ticket + ": ", e);
			UtilsApi.setRespuesta(response);
		}            		
    	
    	return response;
    }
    
    boolean validarDatosObtenerDetalleTicket (String documento, String pais, String comercio, String canal, String version, int idAplicacion) {

		// Añadir validaciones para cada dato
		return true;
	}    

    @PostMapping("/consultarFacturaId")
    @ApiOperation(value = "Consultar un folio fiscal por su facturaId", notes = "Este m\u00e9todo permite consultar un folio fiscal (uuid) por su facturaId. El Pac proporcion\u00f3 dos url: una para facturaci\u00f3n cliente (C) y otra para facturaci\u00f3n global (G), por lo que es necesario indicar de que tipo es. El c\u00f3digo igual a 1 significa que se consult\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteConsultarFacturaIdExpRespTYPE consultarFacturaId(@RequestBody ClienteConsultarFacturaIdExpReqTYPE request) {
    	
    	ClienteConsultarFacturaIdExpRespTYPE response = new ClienteConsultarFacturaIdExpRespTYPE();
    	BodyConsultarFacturaIdTYPE body = request.getBody();
    	String facturaId = "";
    	String tipo = "";

    	if (body == null) {
    		ClienteConsultarFacturaIdExpRespTYPE.Respuesta respuesta = new ClienteConsultarFacturaIdExpRespTYPE.Respuesta();
			respuesta.setCodigo(Integer.toString(ECodigo.Error.getValor()));
        	respuesta.setDescripcion("La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
        	response.setRespuesta(respuesta);
			return response;
    	}
    	
    	try {
    	
    		facturaId = (body.getFacturaId()==null)? "": body.getFacturaId().trim();
    		tipo = (body.getTipo()==null)? "": body.getTipo().trim();
	    	
			if (facturaId.isEmpty() || tipo.isEmpty()) {
				ClienteConsultarFacturaIdExpRespTYPE.Respuesta respuesta = new ClienteConsultarFacturaIdExpRespTYPE.Respuesta();
				respuesta.setCodigo("116");
	        	respuesta.setDescripcion("Algunos de los par\u00e1metros son inv\u00e1lidos, favor de validar.");
	        	response.setRespuesta(respuesta);
	    		return response;
	    	}
	
			return facturasService.consultarTipo(facturaId, tipo);
						
		} catch (Exception e) {
			logger.error("facturaId-tipo " + facturaId + "-" + tipo +": ", e);
			
			ClienteConsultarFacturaIdExpRespTYPE.Respuesta respuesta = new ClienteConsultarFacturaIdExpRespTYPE.Respuesta();
			respuesta.setCodigo(Integer.toString(ECodigo.Error.getValor()));
        	respuesta.setDescripcion("La solicitud es inv\u00e1lida u ocurri\u00f3 un error");
        	response.setRespuesta(respuesta);
		}            		
    	
    	return response;
    }   
    
    
    @PostMapping("/timbrarComplemento")
    @ApiOperation(value = "Timbrar un complemento de pago.", notes = "Este m\u00e9todo permite timbrar un complemento de pago generando su cfdi respectivo. El c\u00f3digo igual a 1 significa que el complemento se gener\u00f3 satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE timbrar(@RequestBody ClienteTimbrarComplementoExpReqTYPE request) {
    	    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	String ticket = "";
    	
    	if (request != null && request.getIdTransaccion() != null && !request.getIdTransaccion().isEmpty()) {
    		ticket = request.getIdTransaccion().trim();
    	}

    	if (ticket.isEmpty()) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
	    	actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosTimbrarComplemento(ticket)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
	    		return response;
	    	}
	
            actividadesModel.setTicket(ticket);
            errorComponent.setIdFacturaPac(0);

            CompTemporalModel model = new CompTemporalModel();
            model.setVersionFacturacionSat("4.0");
        	model.setTicket(ticket);
//        	model.setTotal(total);
//        	model.setRfc(rfc);
//        	model.setRazonSocial(razonSocial);
//        	model.setIdUsoCfdi(usoCfdi);
//        	model.setEmail(email);
//        	model.setEmailCC(emailCC);
//        	model.setNombreObra(nombreObra);
//        	model.setResponsableObra(responsableObra);
//        	model.setAutorizoGuardado(autorizoGuardado);
//        	model.setIdFacturaPac(idFacturaPac);
//        	model.setVersionFacturacionSat(version);
//        	model.setRegimenFiscal(regimenFiscal);
//        	model.setCodigoPostal(codigoPostal);
//			model.setIdAplicacion(idAplicacion);
            
        	int codigoRetorno = complementosService.timbrarTipoProceso(model, EProceso.TimbradoNormal.getValor());
			
        	if (codigoRetorno == 300) {
        		UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
        	} else {
        		UtilsApi.setRespuesta(response, codigoRetorno, model);        		
        	}
			
		} catch (Exception e) {
			logger.error("IdTransaccionPago " + ticket + ": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }
    
    boolean validarDatosTimbrarComplemento (String idTransaccion) {

		if (idTransaccion.isEmpty()) {
			return false;
		}
		
		if (!UtilsString.isNumeric(idTransaccion)) {
			return false;
		}
		
		// Añadir validaciones para cada dato
		return true;
	}    
	@PostMapping("/obtenerTicket")
    @ApiOperation(value = "Obtener un ticket en base a la orden de compra", notes = "Este m\u00e9todo permite obtener un ticket de venta o devoluci\u00f3n en base a su orden de compra. El c\u00f3digo igual a 1 significa que el ticket se obtuvo satisfactoriamente, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE obtenerTicket(@RequestBody ClienteObtenerTicketExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	BodyObtenerTicketTYPE body = request.getBody();
    	String documento = "";
    	String ticket = "";
    	
    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
	    	documento = (body.getDocumento()==null)? "": body.getDocumento().trim();
	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosObtenerTicket(documento)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
				return response;
	    	}
	    		
			ticket = documentoService.obtenerTicketOrdenCompra(body.getDocumento().trim());
			if (ticket.isEmpty()){
	    		UtilsApi.setRespuesta(response, ECodigo.DocumentoInvalido.getValor());
				return response;
			}		
		
			UtilsApi.setRespuesta(response, ECodigo.Ok.getValor(), ticket);
						
		} catch (Exception e) {
			logger.error("Documento " + documento + ": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }
	
	@PostMapping("/enviarCorreo")
	@ApiOperation(value = "Envia correo", notes = "Enviar correos pendientes")
	public ClientResponseTYPE<String> enviarCorreo(@RequestBody BodyClienteTemporalTYPE request) {
	    
		ClientResponseTYPE<String> response = new ClientResponseTYPE<>();
    	ClientesTemporalModel model = new ClientesTemporalModel();
		model.setUuid( request.getUuid() );
		model.setXml( request.getXml() );
		model.setRfc( request.getRfc() );
		model.setEmail( request.getEmail() );
		model.setEmailCC( request.getEmailCC() );
		model.setTicket( request.getTicket() );
		model.setPac( request.getPac() );
		model.setIdFacturaPac( request.getIdFacturaPac() );
		model.setRazonSocial( request.getRazonSocial() );
		
		try {
			boolean enviaCorreo = this.correoFacturacionService.enviarCorreo(model);
			
			if (enviaCorreo) {
				response.setData("OK");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("1");
				respuestaClient.setDescripcion("Se envio el correo correctamente");
				response.setRespuesta(respuestaClient);
			} else {
				response.setData("ERROR");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("0");
				respuestaClient.setDescripcion("Error al enviar correo");
				response.setRespuesta(respuestaClient);
			}
		} catch(Exception ex) {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}
    	return response;
    }
	
	@PostMapping("/enviarCorreoToken")
	@ApiOperation(value = "Envia correo Token", notes = "Enviar correos token")
	public ClientResponseTYPE<String> enviarCorreoToken(@RequestBody BodyClienteTemporalTYPE request) {
	    
		ClientResponseTYPE<String> response = new ClientResponseTYPE<>();
    	ClientesTemporalModel model = new ClientesTemporalModel();
		model.setEmail( request.getEmail() );
		model.setRazonSocial( request.getRazonSocial() );
		model.setToken( request.getToken() );
		
		try {
			boolean enviaCorreo = this.correoFacturacionService.enviarTokenMultiple(model);
			
			if (enviaCorreo) {
				response.setData("OK");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("1");
				respuestaClient.setDescripcion("Se envio el correo correctamente");
				response.setRespuesta(respuestaClient);
			} else {
				response.setData("ERROR");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("0");
				respuestaClient.setDescripcion("Error al enviar correo");
				response.setRespuesta(respuestaClient);
			}
		} catch(Exception ex) {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}
    	return response;
    }
	
	@PostMapping("/enviarCorreoComplemento")
	@ApiOperation(value = "Envia correo", notes = "Enviar correo complemento")
	public ClientResponseTYPE<String> enviarCorreoComplemento(@RequestBody BodyComplementoCorreoTYPE request) {
	    
		ClientResponseTYPE<String> response = new ClientResponseTYPE<>();
		try {
			boolean enviaCorreo = this.correoComplementoService.enviarCorreoComplemento(request);
			if (enviaCorreo) {
				response.setData("OK");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("1");
				respuestaClient.setDescripcion("Se envio el correo correctamente");
				response.setRespuesta(respuestaClient);
			} else {
				response.setData("ERROR");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("0");
				respuestaClient.setDescripcion("Error al enviar correo");
				response.setRespuesta(respuestaClient);
			}
		} catch(Exception ex) {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}
    	return response;
    }
	
	@PostMapping("/enviarCorreoComplementoFactura")
	@ApiOperation(value = "Envia correo", notes = "Enviar correos complemento factura")
	public ClientResponseTYPE<String> enviarCorreoComplementoFactura(@RequestBody BodyComplementoCorreoTYPE request) {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<>();
		try {
			boolean enviaCorreo = this.correoComplementoService.enviarCorreoFactura(request);
			if (enviaCorreo) {
				response.setData("OK");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("1");
				respuestaClient.setDescripcion("Se envio el correo correctamente");
				response.setRespuesta(respuestaClient);
			} else {
				response.setData("ERROR");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("0");
				respuestaClient.setDescripcion("Error al enviar correo");
				response.setRespuesta(respuestaClient);
			}
		} catch(Exception ex) {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}
    	return response;
    }
	
	@PostMapping("/obtenerComprobantePac")
    @ApiOperation(value = "Obtener comprobante", notes = "Este m\u00e9todo permite obtener el combrobante")
	public ClientResponseTYPE<RespuestaPac> comprobante(@RequestBody BodyComprobanteTYPE request) {
		ClientResponseTYPE<RespuestaPac> respuesta = new ClientResponseTYPE<>();
		RespuestaXml respuestaComprobante = null;
		
		try {
			respuestaComprobante = this.emision40Service.getComprobante(request.getFacturaId(), request.getTipoTimbrado());
			if (respuestaComprobante != null) {
				RespuestaPac respuestaPac = new RespuestaPac();
				respuestaPac.setFacturaId( request.getFacturaId() );
				respuestaPac.setFolio( respuestaComprobante.getFolio() );
				respuestaPac.setUuid( respuestaComprobante.getUuid() );
				respuestaPac.setXml( respuestaComprobante.getXml() );
				respuestaPac.setErrorDesc( respuestaComprobante.getErrorDesc() );
				respuestaPac.setEstatusId( respuestaComprobante.getEstatusId() );
				respuesta.setData(respuestaPac);
				
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("1");
				respuestaClient.setDescripcion("Se obtiene informacion de la factura Id");
				respuesta.setRespuesta(respuestaClient);
			}
		} catch(Exception ex) {
			RespuestaClient client = new RespuestaClient();
			client.setCodigo("0");
			client.setDescripcion(ex.getMessage());
			respuesta.setRespuesta(client);
			
			ex.printStackTrace();
		}
		return respuesta;
	}
	
	@PostMapping("/enviarCorreoUuid")
    public ClientResponseTYPE<String> reenviarFactura(@RequestBody BodyClienteTemporalTYPE request) 
    {
		ClientResponseTYPE<String> response = new ClientResponseTYPE<>();
		
		String eMailTo = request.getEmail();
		String eMailCC = request.getEmailCC();
		String uuid = request.getUuid();
		String razonSocial = "";
		String ticket = "";
		String xml = "";
		String rfc = "";
		
		Integer idFacturaPac = null;	
		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		
		if (factura != null) {
			eMailTo = request.getEmail();
			rfc = seguridadService.desencriptar(factura.getRfc());
			xml = seguridadService.desencriptar(factura.getXml());
			
			ticket = factura.getTicket();
			uuid = factura.getUuid();
			idFacturaPac = factura.getIdFacturaPac();
			
			Document document = UtilsFile.ObtenerDocumentXml (xml);
			Node nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
			Element eElement = (Element) nNode;
			razonSocial = eElement.getAttribute("Nombre");
			
		} else {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}
		
		if (eMailTo.isEmpty()) {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}			
		
		String destinatario = eMailTo;
		String destinatarioCC = eMailCC;
		
		ClientesTemporalModel model = new ClientesTemporalModel();
		model.setUuid( uuid );
		model.setXml( xml );
		model.setRfc( rfc );
		model.setEmail( destinatario );
		model.setEmailCC(destinatarioCC);
		model.setTicket( ticket );
		model.setIdFacturaPac( idFacturaPac );
		model.setRazonSocial( razonSocial );
		
		try {
			boolean enviaCorreo = this.correoFacturacionService.enviarCorreo(model);
			if (enviaCorreo) {
				response.setData("OK");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("1");
				respuestaClient.setDescripcion("Se envio el correo correctamente");
				response.setRespuesta(respuestaClient);
			} else {
				response.setData("ERROR");
				RespuestaClient respuestaClient = new RespuestaClient();
				respuestaClient.setCodigo("0");
				respuestaClient.setDescripcion("Error al enviar correo");
				response.setRespuesta(respuestaClient);
			}
		} catch(Exception ex) {
			response.setData("ERROR");
			RespuestaClient respuestaClient = new RespuestaClient();
			respuestaClient.setCodigo("0");
			respuestaClient.setDescripcion("Error al enviar correo");
			response.setRespuesta(respuestaClient);
		}
    	return response;
    }
	
	boolean validarDatosObtenerTicket (String documento) {

		if (documento=="") {
			return false;
		} 
		
    	if (!documentoService.validarExpresionRegular(documento)) {
    		return false;
		}
    	
    	if (documento.length()!=10) {
    		return false;
    	}
    			
		// Añadir validaciones para cada dato
		return true;
	}
	
	@PostMapping("/validarTicket")
    @ApiOperation(value = "Validar si el ticket esta facturado o en proceso", notes = "Este m\u00e9todo permite validar un ticket de venta o devoluci\u00f3n. Se validar\u00e1 si ya esta facturado o bien esta en proceso de facturaci\u00f3n. El c\u00f3digo igual a 1 significa que el ticket se puede timbrar, cualquier otro n\u00famero significa que hubo alg\u00fan error y se mostrar\u00e1 en su descripci\u00f3n.")
    public ClienteTicketTimbrarExpRespTYPE validarTicket(@RequestBody ClienteObtenerTicketExpReqTYPE request) {
    	
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	BodyObtenerTicketTYPE body = request.getBody();
    	String documento = "";
    	String ticket = "";
    	
    	if (body == null) {
    		UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
			return response;
    	}
    	
    	try {
	    	documento = (body.getDocumento()==null)? "": body.getDocumento().trim();
	
			actividadesModel.inicializar();
			errorComponent.inicializar();

			if (!validarDatosValidarTicket(documento)) {
	    		UtilsApi.setRespuestaFacturacion(response, 116);
				return response;
	    	}
	    		
	    	ticket = documento;

			if (documento.trim().length()==10) {
				ticket = documentoService.obtenerTicketOrdenCompra(body.getDocumento().trim());
				if (ticket.isEmpty()){
		    		UtilsApi.setRespuesta(response, ECodigo.DocumentoInvalido.getValor());
					return response;
				}		
			}
			ClientesTemporalModel model = new ClientesTemporalModel();
			model.setTicket(ticket);
			
			int codigoRetorno = ticketsService.validarTicket(ticket);

			if (codigoRetorno == 300) { 
				UtilsApi.setRespuesta(response, ECodigo.Ok.getValor());
			} else {
				UtilsApi.setRespuesta(response, codigoRetorno, model);
			}
						
		} catch (Exception e) {
			logger.error("Documento " + documento + ": ", e);
			
			UtilsApi.setRespuesta(response, ECodigo.Error.getValor());
		}            		
    	
    	return response;
    }
	
	boolean validarDatosValidarTicket (String documento) {

		if (documento=="") {
			return false;
		} 
		
    	if (!documentoService.validarExpresionRegular(documento)) {
    		return false;
		}
    			
		// Añadir validaciones para cada dato
		return true;
	}
    
}