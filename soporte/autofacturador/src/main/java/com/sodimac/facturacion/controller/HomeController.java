package com.sodimac.facturacion.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.sodimac.facturacion.clientews.model.ClientResponseTYPE;
import com.sodimac.facturacion.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.component.ActividadesComponent;
import com.sodimac.facturacion.component.ErrorComponent;
import com.sodimac.facturacion.entity.ClientesEntity;
import com.sodimac.facturacion.entity.ClientesTemporalEntity;
import com.sodimac.facturacion.entity.ConfiguracionTokenEntity;
import com.sodimac.facturacion.entity.FacturasEntity;
import com.sodimac.facturacion.entity.ListaFacturasEntity;
import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.models.ClientesTemporalModel;
import com.sodimac.facturacion.models.FacturasMultipleModel;
import com.sodimac.facturacion.models.MultipleModalItem;
import com.sodimac.facturacion.models.MultipleModalResponse;
import com.sodimac.facturacion.models.ResponseBase;
import com.sodimac.facturacion.models.UsoDeCfdi;
import com.sodimac.facturacion.service.CatConfiguracionService;
import com.sodimac.facturacion.service.CatErrorSatService;
import com.sodimac.facturacion.service.CatMensajesService;
import com.sodimac.facturacion.service.CatUsosCfdiService;
import com.sodimac.facturacion.service.ClientesService;
import com.sodimac.facturacion.service.ClientesTemporalService;
import com.sodimac.facturacion.service.FacturasService;
import com.sodimac.facturacion.service.ListaFacturasService;
import com.sodimac.facturacion.service.MailSenderService;
import com.sodimac.facturacion.service.SeguridadService;
import com.sodimac.facturacion.service.TicketsService;
import com.sodimac.facturacion.service.TokenService;
import com.sodimac.facturacion.util.Convert;
import com.sodimac.facturacion.util.UtilsFile;
import com.sodimac.facturacion.util.UtilsString;
import com.sodimac.facturacion.util.enums.ETipoPersonaEnum;
import com.sodimac.facturacion.util.enums.EVersionCFDI;

@Controller
@RequestMapping("/")
@SessionAttributes({"mapConfiguracion", "listTicket"})
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
	private ClientesTemporalService clientesTemporalService;
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	@Autowired
	private ClientesService clientesService;
	@Autowired
	private TicketsService ticketsService;
	@Autowired
	private CatUsosCfdiService catUsosCfdiService;
	@Autowired
	private CatMensajesService catMensajesService;
	@Autowired
	private FacturasService facturasService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private ListaFacturasService listaFacturasService;

	@Autowired
	private ActividadesComponent actividadesModel;
	@Autowired
	private ErrorComponent errorComponent;
	@Autowired
	private SeguridadService seguridadService;
	@Autowired
	private CatErrorSatService catErrorSatService;
	@Autowired
	private MailSenderService mailSenderService;
	
	MultipleModalResponse multipleModalResponse;

	@ModelAttribute("listTicket")
	public List<String> setListTicket() {
		List<String> listTicket = new ArrayList<String>();
		return listTicket;
	}
	   
	@GetMapping("/")
	public String showForm(@RequestParam(value = "7g8dqd89h", required = false) String descargarUrl, Model theModel, @ModelAttribute("listTicket") List<String> listTicket, HttpSession session) {

		String descargar = "false";
		if (errorComponent.getDescargar()==null || errorComponent.getDescargar()=="") {
			if (descargarUrl != null && descargarUrl.equals("vvee")) {
				descargar = "true";
			}
		} else {
			descargar = errorComponent.getDescargar();
		}

		session.invalidate();
		System.out.println("Session ID Generated ----------------------------- " + session.getId());
		listTicket.clear();

		errorComponent.setDescargar(descargar);
		
		return "factura-form";
	}
	
	@PostMapping("/InicializarBitacora")
	@ResponseBody
	public String inicializarBitacora(@RequestParam("ipClave") String ip
			, @RequestParam("latitud") String latitud
			, @RequestParam("longitud") String longitud
			, @RequestParam("sistemaOperativo") String sistemaOperativo
			, @RequestParam("explorador") String explorador
			, HttpSession session
			, HttpServletRequest request
			) {
		
		actividadesModel.setActividadesProperties(longitud, latitud, explorador, sistemaOperativo, ip, "", session.getId());
		errorComponent.setErrorProperties(longitud, latitud, explorador, sistemaOperativo, ip, "", session.getId());
		session.setAttribute("validSession", true);
		return "OK";
	}

	@PostMapping("/GuardarTicket")
	@ResponseBody
	public String guardarTicket(@RequestParam("ticket") String ticket, @RequestParam("monto") String monto, HttpSession session) throws ParseException {
		
		if (actividadesModel.getExplorador() == "") {
			return "redirect";
		}
		
		errorComponent.setPagina("GuardarTicket");
		String result = "";
		List<String> datosArr = new ArrayList <String>();
		
		String ticketValidado = ticketsService.validarTicketExpresionRegular(ticket);
		if (ticketValidado.isEmpty()) {
			return "Invalido";
		}
		
		monto = monto.replace(",", "");
		if (!ticketsService.validarMontoExpresionRegular(monto)) {
			return "Invalido";
		}

// rmt 2024-12-04 Se elimina validacion por PUNTOS_CES
//		BigDecimal total = BigDecimal.valueOf(Double.parseDouble(monto));
//		BigDecimal cero = BigDecimal.valueOf(Double.parseDouble("0"));
//		if (total.equals(cero)) {
//			return "Total Inv�lido";
//		}
		
		//actividadesModel.setSessionId(sessionId);
//		actividadesModel.setTicket(ticketValidado);
//		datosArr.clear();
//		datosArr.add(ticketValidado);
//		datosArr.add(total.toString());
//		actividadesModel.registrarActividad(1, datosArr, errorComponent.getPagina());

		result = ticketsService.validarTicketWS(ticket, monto);
		if (result.equals("OK") || result.equals("OKNC")) {
			if (listaFacturasService.addListaFacturas(ticketValidado, BigDecimal.valueOf(Double.parseDouble(monto)), session.getId()) == null) {
				errorComponent.setTicket(ticketValidado);
				errorComponent.guardarLog("No se grab� el ticket " + ticketValidado + " en listaFacturas");
				return "Invalido";
			} 
		}
		
		return result;
	}
	
	@PostMapping("/ValidarRfc")
	@ResponseBody
	public String validarRfc(@RequestParam("rfc") String rfc) {

		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return "Invalido";
		}
		String strHabilitar = catConfiguracionService.findParameterByKey("WebService.RfcCheck.Habilitar");
		boolean habilitarValidacion = Boolean.parseBoolean(strHabilitar);
		
		try {
			int existe = 0;//clientesService.existRfcFactura(rfc);

			if (existe==0 && !rfc.contentEquals("AAA010101AAA")) {
				
				if (!habilitarValidacion) {
					actividadesModel.setRfc(rfc);
					return "OK";
				} 
				
//				if (!checkRfcService.validarRfc(rfc)) {
//					return "Invalido";
//				}						
			}
		} catch (Exception e) {
				errorComponent.setRfc(rfc);
				errorComponent.setPagina("ValidarRfc");
				errorComponent.guardarLog(e);
				e.printStackTrace();
				return "Invalido";
		}
		
		actividadesModel.setRfc(rfc);
		return "OK";
	}	

	@PostMapping("/ValidarRfcConsulta")
	@ResponseBody
	public String validarRfcConsulta(@RequestParam("rfc") String rfc) {

		String result = "Invalido";
		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return result;
		}
		
		try {
			int existe = clientesService.existRfcFactura(rfc);
			if (existe==1) {
				actividadesModel.setRfc(rfc);
				result = "OK";
			}
			
		} catch (Exception e) {
				errorComponent.setRfc(rfc);
				errorComponent.setPagina("validarRfcConsulta");
				errorComponent.guardarLog(e);
				e.printStackTrace();
				return result;
		}
				
		return result;
	}
	
	@PostMapping("/ValidarRfcMultiple")
	@ResponseBody
	public String validarRfcMultiple(@RequestParam("rfc") String rfc) {

		String result = "Invalido";
		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return result;
		}
		
		try {
			if (clientesService.isExistRfc(rfc)) {
				actividadesModel.setRfc(rfc);
				result = "OK";
			}
			
		} catch (Exception e) {
			errorComponent.setRfc(rfc);
			errorComponent.setPagina("validarRfcMultiple");
			errorComponent.guardarLog(e);
				e.printStackTrace();
				return result;
		}
				
		return result;
	}

	@PostMapping("/ValidarTokenConsulta")
	@ResponseBody
	public String validarTokenConsulta(@RequestParam("token") String token, @RequestParam("rfc") String rfc, HttpSession session, @RequestParam("email") String email) {

		String result = "Invalido";
		List<String> datosArr = new ArrayList <String>();
		
		if (!tokenService.validarExpresionRegular(token)) {
			return result;
		}
		String sessionId = session.getId();
		
		try {
			int existe = tokenService.existToken(sessionId, token, rfc, email);
			if (existe==1) {
				result = "OK";
			} else {
				datosArr.clear();
				datosArr.add(token);
				actividadesModel.setTicket("");
				actividadesModel.registrarActividad(38, datosArr,"ValidarTokenConsulta");
			}
			
		} catch (Exception e) {
				errorComponent.setRfc(rfc);
				errorComponent.setPagina("ValidarTokenConsulta");
				errorComponent.guardarLog(e);
				e.printStackTrace();
				return result;
		}
				
		return result;
	}

	@PostMapping("/ObtenerCliente")
	@ResponseBody
	public String obtenerCliente(@RequestParam("rfc") String rfc) {

		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return null;
		}
		        
		return clientesService.getInformacionCliente(rfc);
	}
	
	@PostMapping("/ObtenerClienteVersion")
	@ResponseBody
	public String obtenerCliente(@RequestParam("rfc") String rfc, @RequestParam("versionCfdi") String versionCfdi) {

		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return null;
		}
		
		return clientesService.getInformacionCliente(rfc, versionCfdi); 
	}	

	@PostMapping("/ObtenerClaveUsoCfdiNotaCredito")
	@ResponseBody
	public String obtenerClaveUsoCfdiNotaCredito(HttpSession session) {
	        
		String result = "";
		List< ListaFacturasEntity> listaFacturas = listaFacturasService.getListaFacturasBySessionId(session.getId());
		
		if (listaFacturas.size() > 0) {
			ListaFacturasEntity facturaItem = listaFacturas.get(0);
			String ticket =  facturaItem.getTicket();
			result = facturasService.getClaveUsoCfdiNC(ticket);
		}

		return result;
	}	
	
	@PostMapping("/ObtenerDatosCfdiNotaCredito")
	@ResponseBody
	public String obtenerDatosCfdiNotaCredito(HttpSession session) {
	        
		String result = "";
		List< ListaFacturasEntity> listaFacturas = listaFacturasService.getListaFacturasBySessionId(session.getId());
		
		if (listaFacturas.size() > 0) {
			ListaFacturasEntity facturaItem = listaFacturas.get(0);
			String ticket =  facturaItem.getTicket();
			result = facturasService.getDatosCfdiNC(ticket);
		}
		return result;
	}

	@PostMapping("/GenerarFactura")
	@ResponseBody
	public MultipleModalResponse generarFactura(
			@RequestParam String rfc
            ,@RequestParam String usoCfdi
            ,@RequestParam String razonSocial
            ,@RequestParam String email
            ,@RequestParam String guardarDatos
            ,@RequestParam String nombreObra
            ,@RequestParam String responsableObra
            ,@RequestParam String codigoPostal
            ,@RequestParam String regimenFiscal
            ,@RequestParam String versionCfdi
            ,HttpSession session) {

		
		logger.info("Entra al metodo generarFactura");
		logger.info("RFC: " + rfc);
		logger.info("versionCfdi: " + versionCfdi);
		
		multipleModalResponse = new MultipleModalResponse(true,"", null);
		errorComponent.setPagina("GenerarFactura");

		validarDatosGenerarFactura(rfc, usoCfdi, razonSocial, email, guardarDatos, nombreObra, responsableObra, codigoPostal, regimenFiscal, versionCfdi);
		logger.info("datos validados");
		if (!multipleModalResponse.isSuccess()) {
			return multipleModalResponse;
		}
		ClientesEntity cliente = clientesService.getCliente(rfc);
		//Si la bandera es "" y existe el cliente
		if( (guardarDatos == null || guardarDatos.isEmpty()) && (cliente!= null && cliente.getIdCliente() > 0)) {
			//Si no se muestra la bandera en el front y si existe el cliente en la tabla de facturas se deben de actualizar en automatico
			guardarDatos = "true";
		}
		if (razonSocial=="") {
			if (cliente != null) {
				if (razonSocial=="") {
					razonSocial = seguridadService.desencriptar(cliente.getRazonSocial());
				}
			}
		}
		
		logger.info("codigoPostal y regimenFiscal");
		if (codigoPostal =="" || regimenFiscal=="") {
			if (cliente != null) {
				if (codigoPostal=="" && cliente.getCodigoPostal() != null && !cliente.getCodigoPostal().isEmpty()) { 
					codigoPostal = seguridadService.desencriptar(cliente.getCodigoPostal());
				}
				if (regimenFiscal=="" && cliente.getRegimenFiscal() != null && !cliente.getRegimenFiscal().isEmpty()) { 
					regimenFiscal = cliente.getRegimenFiscal();
				}
			}
		}

		boolean blnGenerarOK = false;
		boolean timbradoExitoso = false;
		String mensaje = "";
		int autorizoGuardado = guardarDatos.equalsIgnoreCase("true") ? 1 : 0;
		List<MultipleModalItem> multipleModalItems = new ArrayList<MultipleModalItem>();
		
		List<ListaFacturasEntity> listaFacturas = listaFacturasService.getListaFacturasBySessionId(session.getId());

		for (int i = 0; i < listaFacturas.size(); i++) {
			ListaFacturasEntity facturaItem = listaFacturas.get(i);
			logger.info("Ticket: " + facturaItem.getTicket());
			MultipleModalItem multipleModalItem = new MultipleModalItem();
			
			actividadesModel.setTicket(facturaItem.getTicket());
			
			if (email=="") {
				logger.info("Correo viene vacio, se valida si es NC");
				TicketEntity ticketBctHdrVal = ticketsService.getTicket(facturaItem.getTicket());
				if (ticketBctHdrVal != null) {
					if (ticketBctHdrVal.getTipoTicket() == 9 || ticketBctHdrVal.getTipoTicket() == 10) {
						FacturasEntity factura = this.facturasService.getFacturaByTicket(ticketBctHdrVal.getOriginal());
						if (factura != null) {
							email = seguridadService.desencriptar(factura.getEmail());
						}
					}
				}
			}
			
			if (email == "") {
				if (cliente != null) {
					email = seguridadService.desencriptar(cliente.getEmail());
				}
			}
			
			ClientesTemporalModel model = new ClientesTemporalModel();
	        model.setAutorizoGuardado(autorizoGuardado);
	        model.setEmail(email);
	        model.setIdUsoCfdi(usoCfdi);
	        model.setRazonSocial(razonSocial);
	        model.setRfc(rfc);
	        model.setTicket(facturaItem.getTicket());
	        model.setTotal(facturaItem.getTotal());
	        model.setNombreObra(nombreObra);
	        model.setResponsableObra(responsableObra);
	        model.setCodigoPostal(codigoPostal);
	        model.setRegimenFiscal(regimenFiscal);
	        model.setVersionCfdi( versionCfdi);
	        multipleModalItem.setTicket(facturaItem.getTicket());
			try {
				
				blnGenerarOK = true;
				logger.info("Timbrando ticket");
				ClienteTicketTimbrarExpRespTYPE response = facturasService.timbrarTicketWs(model);
				
				
				if (response.getRespuesta().getCodigo().equals("1")) {
	        		facturaItem.setStatus(2);
					multipleModalItem.setStatus("0");
					mensaje = catMensajesService.get(300).getDescripcionMensaje();
					timbradoExitoso = true;
				} else {
					Integer codigo = Integer.valueOf( response.getRespuesta().getCodigo() );
					logger.info("Ticket con error, procesando respuesta");
	        		facturaItem.setStatus(3);
					multipleModalItem.setStatus(response.getRespuesta().getCodigo());
					mensaje = response.getRespuesta().getDescripcion();
					
					if (codigo.intValue() == 202) {
						String ticket = facturaItem.getTicket();
						logger.info("Validando si existe el mensaje de error en el SAT: " + ticket);
						String msgError = this.catErrorSatService.obtenerMensajeErrorSat(ticket);
						logger.info("Mensaje: " + msgError);
					
					
						if (!msgError.equals("SIN_CATALOGO")) {
							mensaje = msgError.replace("$P_RFC", rfc);
							mensaje = mensaje + "<br /><b>" + "Documento disponible para timbrar nuevamente" + "</b>";
							this.facturasService.liberarTicket(ticket);
							logger.info("#################### Ticket liberado: " + ticket + "#####################");
						}
					}
				}
				
				listaFacturasService.updateListasFacturas(facturaItem);
	        	multipleModalItem.setMsg(mensaje);
			} catch (Exception e) {
				e.printStackTrace();
				errorComponent.setRfc(rfc);
				errorComponent.setTicket(facturaItem.getTicket());
				errorComponent.guardarLog(e, model);
	        	mensaje = catMensajesService.get(202).getDescripcionMensaje();
	        	multipleModalItem.setMsg(mensaje);
			}
			
			multipleModalItems.add(multipleModalItem);
        };
		
		multipleModalResponse.setMultipleModalItems(multipleModalItems);
		        
        mensaje = autorizoGuardado == 1 && blnGenerarOK ? catMensajesService.get(53).getDescripcionMensaje() : "";
        if (timbradoExitoso) mensaje += "<br>" + catMensajesService.get(203).getDescripcionMensaje();
        multipleModalResponse.setMessage(mensaje);
        
		return multipleModalResponse;
	}
	
	void validarDatosGenerarFactura (String rfc, String usoCfdi, String razonSocial, String email, String guardarDatos, String nombreObra, String responsableObra, 
			String codigoPostal, String regimenFiscal, String versionCfdi) {

		if (actividadesModel.getExplorador() == "") {
			multipleModalResponse.setMessage("redirect");
			multipleModalResponse.setSuccess(false);
		}

		if (rfc=="" || usoCfdi=="") {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		} 
		
		if (!razonSocial.isEmpty() && !clientesService.validarRZExpresionRegular(razonSocial)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}

		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}
		
		EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionCfdi);
		UsoDeCfdi usosCfdi = catUsosCfdiService.getUsoCfdi(usoCfdi, eVersionCFDI.getId());
		if (usosCfdi == null) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);			
		}
		
		if (!email.isEmpty() && !clientesService.validarEmailExpresionRegular(email)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}

		if (!guardarDatos.isEmpty() && (!guardarDatos.equalsIgnoreCase("true") && !guardarDatos.equalsIgnoreCase("false"))) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}
		
		if (!nombreObra.isEmpty() && !clientesService.validarObraExpresionRegular(nombreObra)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}
		
		if (!responsableObra.isEmpty() && !clientesService.validarResponsableObraExpresionRegular(responsableObra)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}

		// A�adir validaciones para cada dato NO APLICA CUANDO ES EDICION, LA VALIDACION SE REALIZA EN FRONT
		/*if (versionCfdi.equals( CFDI_40 )) {
			if (codigoPostal=="" || regimenFiscal=="") {
				multipleModalResponse.setMessage("Datos invalidos para versi\u00f3n 4.0");
				multipleModalResponse.setSuccess(false);
			} 
		}*/
		
	}	
	
	void eliminarArchivosMultiple(String archivos) {
		String[] arrArchivos = archivos.split(",");
		for (String item : arrArchivos) {
			facturasService.eliminarArchivo(item);
		}
	}

	@GetMapping("/ListarFactura/{rfc}")
	public String listarFactura(@PathVariable("rfc") String rfc
			, @RequestParam(value = "ticket", required=false) String ticket
			, Model theModel
			, HttpSession session) {
		List<String> datosArr = new ArrayList <String>();
		errorComponent.setPagina("ListarFactura");
		
		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return "Invalido";
		}

		if (ticket != null) {
			String ticketValidado = ticketsService.validarTicketExpresionRegular(ticket);
			if (ticketValidado.isEmpty()) {
				return "Invalido";
			}
			ticket = ticketValidado;
		}
		
		actividadesModel.setTicket(ticket);
		datosArr.clear();
		datosArr.add(ticket == null ? "":ticket);
		actividadesModel.setRfc(rfc);
		actividadesModel.registrarActividad(34, datosArr, "ListarFactura");
		
		List<ClientesTemporalModel> clientesTemporal = new ArrayList <ClientesTemporalModel>();

		try {
			if (ticket==null) {
				clientesTemporal = facturasService.obtenerDatosFactura(rfc, session.getId(), "");
			} else {
				clientesTemporal = facturasService.obtenerDatosFactura(rfc, session.getId(), ticket);
			}
			datosArr.clear();
			datosArr.add(ticket == null ? "":ticket);
			actividadesModel.setRfc(rfc);
			actividadesModel.registrarActividad(35, datosArr, "ListarFactura");
			
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.setRfc(rfc);
			errorComponent.setTicket(ticket == null ? "":ticket);
			errorComponent.guardarLog(e);
			
		}
		
		String descargar = errorComponent.getDescargar();
		
		theModel.addAttribute("dDescargar", descargar);
		theModel.addAttribute("dClientes", clientesTemporal);
		return "factura-list";
	}

	@GetMapping("/ListarFacturaMultiple")
	public String listarFacturaMultiple(@RequestParam("rfc") String rfc
			, @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam(value = "email", required=true) String email
			, Model theModel) {

		multipleModalResponse = new MultipleModalResponse(true,"", null);

		validarDatosMultiple(rfc, dateDesde, dateHasta, pageNumber, email);
		
		if (!multipleModalResponse.isSuccess()) {
			return "Datos invalidos";
		}

		start = (Integer.parseInt(pageNumber) * rowsPerPage) - rowsPerPage;
		List<FacturasMultipleModel> datos = new ArrayList <FacturasMultipleModel>();
		List<FacturasMultipleModel> TodasFacturas = new ArrayList <FacturasMultipleModel>();
		int countFacturas = 0;
		List<String> datosArr = new ArrayList <String>();
		
		String[] partsDesde = dateDesde.split("/");
		String[] partsHasta = dateHasta.split("/");
		String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
		String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
		
		try {
			 datos = facturasService.getFacturasFechas(rfc, email, dateDesdeParse, dateHastaParse, start, rowsPerPage);
			 TodasFacturas = facturasService.getFacturasFechas(rfc, email, dateDesdeParse, dateHastaParse, 0, 0);
			 countFacturas = facturasService.countFacturas(rfc, email, dateDesdeParse, dateHastaParse);
			 datosArr.clear();
			 datosArr.add(dateDesdeParse);
			 datosArr.add(dateHastaParse);
			 actividadesModel.setRfc(rfc);
			 actividadesModel.setTicket("");
			 actividadesModel.registrarActividad(36, datosArr,"ListarFacturaMultiple");
		} catch (Exception e) {
			errorComponent.setRfc(rfc);
			errorComponent.setPagina("ListarFacturaMultiple");
			errorComponent.guardarLog(e);
			e.printStackTrace();
		}

		String descargar = errorComponent.getDescargar();
		
		theModel.addAttribute("dDescargar", descargar);
		theModel.addAttribute("dClientesMultiple", datos);
		theModel.addAttribute("dClientesMultipleAll", TodasFacturas);
		theModel.addAttribute("dClientesMultipleCount", countFacturas);
		theModel.addAttribute("dRowsPerPage", catConfiguracionService.findParameterByKey("Multiple.data.grid.rowsPerPage"));

		actividadesModel.registrarActividad(39, datosArr,"ListarFacturaMultiple");
		return "factura-list-multiple.jsp?pageNumber=" + pageNumber + "&clearMultipleObj=true";
	}

	void validarDatosMultiple (String rfc, String dateDesde, String dateHasta, String pageNumber, String email) {

		if (actividadesModel.getExplorador() == "") {
			multipleModalResponse.setMessage("redirect");
			multipleModalResponse.setSuccess(false);
		}

		if (rfc=="" || dateDesde=="" || dateHasta=="" || pageNumber=="" || email=="") {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		} 

		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}
		
		if (!clientesService.validarEmailExpresionRegular(email)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}
		
		if (dateDesde.length() != 10) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);		
		}
	
		if (dateHasta.length() != 10) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);		
		}
		
		// A�adir validaciones para cada dato

	}
	
	@GetMapping("/multiple/getStartDate")
	@ResponseBody
	public String getStartDate() {
		
		return catConfiguracionService.findParameterByKey("Multiple.getStartDate");

	}
	
	@PostMapping("/multiple/getLongitudToken")
	@ResponseBody
	public String getLongitudToken() {
		String expresionRegularToken = "";
		String longitud = "";
		
		ConfiguracionTokenEntity configuracion = tokenService.getConfiguracion();
		
		if (configuracion!=null) {
			longitud = String.valueOf(configuracion.getLongitud());
			
			if (configuracion.isMayusculas()) {
				expresionRegularToken += "A-Z";
			}
			if (configuracion.isMinusculas()) {
				expresionRegularToken += "a-z";
			}
			if (configuracion.isNumeros()) {
				expresionRegularToken += "0-9";
			}

			expresionRegularToken = "^[" + expresionRegularToken + "]*$";
			
		}
		
		return longitud+"@"+expresionRegularToken;

	}

	@PostMapping("/getConfiguracion")
	@ResponseBody
	public String getConfiguracion(@RequestParam("NombreCampo") String NombreCampo) {
		return catConfiguracionService.findParameterByKey(NombreCampo);

	}
	
	@GetMapping("/VisualizarPdf")
	@ResponseBody
	public String visualizarPdf(@RequestParam("id") String id, @RequestParam("method") String method) {
		
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		String fileNamePdf = path + id + ".pdf";
		String result = "";
		String rfc = "";
		String ticket = "";
		List<String> datosArr = new ArrayList <String>();
		FacturasEntity factura = null;
		errorComponent.setPagina("VisualizarPdf");
		
		try {
			String uuid = id.substring(17);
			factura = facturasService.getFacturaByUuid(uuid);
			if (factura != null) {
				rfc = seguridadService.desencriptar(factura.getRfc());
				ticket = factura.getTicket();
			}
			errorComponent.setRfc(rfc);
			errorComponent.setTicket(ticket);	
			facturasService.crearPdfWs(uuid);
			result =  Convert.fileToB64(fileNamePdf);
			UtilsFile.EliminarArchivo(fileNamePdf);

			actividadesModel.setTicket(ticket);
			if (method.equals("visualizar") == true) {
				datosArr.clear();
				datosArr.add(uuid);
				actividadesModel.setRfc(rfc);
				actividadesModel.registrarActividad(30, datosArr,"VisualizarPdf");
			} else {
				datosArr.clear();
				datosArr.add(uuid);
				actividadesModel.setRfc(rfc);
				actividadesModel.registrarActividad(47, datosArr,"ImprmirPDF");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.guardarLog(e);
			result = "";
		}
		
		return result;
	}
	
	@GetMapping("/ReenvioFactura")
	@ResponseBody
    public String reenviarFactura(@RequestParam(value = "rfc") String rfc, 
    							  @RequestParam(value = "id") String id, 
    		                      @RequestParam(value = "eMailCC") String eMailCC) 
    {
		String result = "";
		List<String> datosArr = new ArrayList <String>();
		String eMailTo = "";
		String razonSocial = "";
		String ticket = "";
		String uuid = "";
		String xml = "";
		Integer idFacturaPac = null;
		FacturasEntity factura = null;
		errorComponent.setPagina("ReenvioFactura");
		
		try{
			
			if (!clientesService.validarRfcExpresionRegular(rfc)) {
				return "error";
			}
			ClientesEntity cliente = clientesService.getCliente(rfc);
			if (cliente == null) {
				ClientesTemporalEntity clienteTmp = clientesTemporalService.getCliente(rfc);
				if (clienteTmp != null) {
					razonSocial = seguridadService.desencriptar(clienteTmp.getRazonSocial());					
				}
			} else {
				razonSocial = seguridadService.desencriptar(cliente.getRazonSocial());
			}
			
			factura = facturasService.getFacturaByUuid(id);
			if (factura != null) {
				eMailTo = seguridadService.desencriptar(factura.getEmail());
				rfc = seguridadService.desencriptar(factura.getRfc());
				xml = seguridadService.desencriptar(factura.getXml());
				ticket = factura.getTicket();
				uuid = factura.getUuid();
				idFacturaPac = factura.getIdFacturaPac();
			} else {
				return "error"; 
			}
			
			if (eMailTo.isEmpty()) {
				return "error";
			}

			errorComponent.setRfc(rfc);
			errorComponent.setTicket(ticket);
			
			actividadesModel.setTicket(ticket);
			actividadesModel.setRfc(rfc);
			datosArr.clear();
			datosArr.add(id);
			actividadesModel.registrarActividad(32, datosArr, "ReenvioFactura");
			
			
			String destinatario = eMailTo;
			String destinatarioCC = eMailCC;
			actividadesModel.registrarActividad(28, null,"ReenvioFactura");
			
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
				ClientResponseTYPE<String> response = facturasService.enviarCorreo(model);
				if (response.getRespuesta().getCodigo().equals("1")) {
					logger.info("Ticket enviado por correo: " + model.getTicket());
					result = "success";
				} else {
					logger.info("Ticket error:     " +  model.getTicket() + " (" + response.getRespuesta().getCodigo() + ")");
					result = "error";
				}
            } catch (Exception e) {
    			e.printStackTrace();
    			result = "error";
    		}
		} catch(Exception e) {
			e.printStackTrace();
			errorComponent.guardarLog(e);
			result = "error";
		}
		
		return result;
    }
	
	@GetMapping("/ImprimirFactura")
	@ResponseBody
	public String imprimirFactura(@RequestParam(value = "id") String id) throws IOException
	{
		
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		String file = path + id + ".pdf";
		List<String> datosArr = new ArrayList <String>();
		
		FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            return "";
        }
        DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc document = new SimpleDoc(inputStream, docFormat, null);
        
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
        
        if (defaultPrintService != null) {
            DocPrintJob printJob = defaultPrintService.createPrintJob();
            try {
                printJob.print(document, attributeSet);
                datosArr.clear();
                datosArr.add(id);
                actividadesModel.registrarActividad(47, datosArr, "ImprimirFactura");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No existen impresoras instaladas");
        }
        inputStream.close();
		
        return "";
	}
	
	@GetMapping("/ConsultarFactura")
	public String consultarFactura(Model theModel) {
				
		return "factura-consulta";
	}
	
	@GetMapping("/ConsultaMultiple")
	public String consultarMultiple(Model theModel) {
							
		return "factura-multiple";
	}
	
	@GetMapping("/GeneracionFactura")
	public String generacionFactura(Model theModel, @ModelAttribute("listTicket") List<String> listTicket, HttpSession session) {
							
		session.invalidate();
		listTicket.clear();
		actividadesModel.setSessionId(session.getId());
		return "factura-form";
	}
	
	@GetMapping("/GenerarToken")
	@ResponseBody
	public ResponseBase GenerarToken (@RequestParam("rfc") String rfc
			,@RequestParam("email") String email
			, HttpSession session) {

		String razonSocial = "";
		boolean result;
		String message = "";

	
		if (!clientesService.validarRfcExpresionRegular(rfc)) {
			return new ResponseBase(false, "Datos invalidos");
		}
		if (!clientesService.validarEmailExpresionRegular(email)) {
			return new ResponseBase(false, "Datos invalidos");
		}
		
		ClientesEntity cliente = clientesService.getCliente(rfc);
		if (cliente == null) {
			//return new ResponseBase(false, catMensajesService.get(112).getDescripcionMensaje());
			return new ResponseBase(false, catMensajesService.get(113).getDescripcionMensaje());
		} else {
			razonSocial = seguridadService.desencriptar(cliente.getRazonSocial());
		}
		
		if (!facturasService.existRfcEmail(rfc, email)) {
			//return new ResponseBase(false, catMensajesService.get(112).getDescripcionMensaje());
			return new ResponseBase(false, catMensajesService.get(113).getDescripcionMensaje());
		} 
		
		String sessionId = session.getId();
		String token = "";
		
		try {
			token = tokenService.GenerarToken(sessionId, rfc, email);
			
		} catch(Exception e) {
			errorComponent.setRfc(rfc);
			errorComponent.setTicket("");
			errorComponent.setPagina("GenerarToken");
			errorComponent.guardarLog(e);
			e.printStackTrace();
		}

		if (!token.equals("-1")) {

			String destinatario = email;
			
			ClientesTemporalModel model = new ClientesTemporalModel();
			model.setEmail( destinatario );
			model.setRazonSocial( razonSocial );
			model.setToken(token);
			
			try {                        	
				ClientResponseTYPE<String> response = facturasService.enviarCorreoToken(model);
				if (response.getRespuesta().getCodigo().equals("1")) {
					logger.info("Ticket enviado por correo: " + model.getTicket());
					result = true;
				} else {
					logger.info("Ticket error:     " +  model.getTicket() + " (" + response.getRespuesta().getCodigo() + ")");
					result = false;
				}
            } catch (Exception e) {
    			e.printStackTrace();
    			errorComponent.guardarLog(e);
    			result = false;
    		}
			
			if (result) {
				message = catMensajesService.get(301).getDescripcionMensaje();
			} else {
				message = catMensajesService.get(302).getDescripcionMensaje();
			}
		}else {
			result = false;
			message = catMensajesService.get(303).getDescripcionMensaje();
		}
		return new ResponseBase(result, message);
	}
	
	@PostMapping("/GuardaEdicionCliente")
	@ResponseBody
	public String GuardaEdicionCliente(@RequestParam(value = "rfc") String rfc,
			                           @RequestParam(value = "cfdi") String cfdi,
			                           @RequestParam(value = "razon") String razon,
		                               @RequestParam(value = "eMailAnt") String eMailAnt,
		                               @RequestParam(value = "eMailNuevo") String eMailNuevo,
		                               @RequestParam(value = "codigoPostal") String codigoPostal,
		                               @RequestParam(value = "regimenFiscal") String regimenFiscal,
		                               @RequestParam(value = "versionCfdi") String versionCfdi
		                               ) {
		
		multipleModalResponse = new MultipleModalResponse(true,"", null);

		validarDatosGenerarFactura(rfc, cfdi, razon, eMailNuevo, "false", "", "", codigoPostal, regimenFiscal, versionCfdi);
		
		if (!multipleModalResponse.isSuccess()) {
			return "Datos invalidos";
		}

		String Mensaje = "";
		ClientesEntity cliente = clientesService.getCliente(rfc);
		EVersionCFDI eVersionCFDI = EVersionCFDI.getVersionByDesc(versionCfdi);
		UsoDeCfdi usoCfdi = catUsosCfdiService.getUsoCfdi(cfdi, eVersionCFDI.getId());
		int idCfdi = usoCfdi.getIdUsoCfdi();
		
		if (cliente != null && usoCfdi != null )
		{
			if (cliente.getEmail().equals(eMailAnt))
			{
				cliente.setIdUsoCfdi(idCfdi);
				cliente.setRazonSocial(razon);

				if(!eMailNuevo.isEmpty())
				{
					cliente.setEmail(eMailNuevo);
				}
				clientesService.saveClientes(cliente);
				Mensaje = catMensajesService.get(53).getDescripcionMensaje();
			}
			else
			{
				Mensaje = catMensajesService.get(111).getDescripcionMensaje();
			}
		}
		
		return Mensaje;
	}
	
	@GetMapping("/ObtenerCorreo")
	@ResponseBody
	public String obtenerCorreo(@RequestParam("uuid") String uuid) {

		String result = "";

		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		if (factura != null) {
			result = UtilsString.emailMask(seguridadService.desencriptar(factura.getEmail()));
		}
				        
		return result;
	}	

	@RequestMapping("/DescargarArchivo/{file_name}") 
	public void DescargarArchivo( @PathVariable("file_name") String fileName, 
		                          HttpServletResponse response) throws IOException {
	  
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		String uuid = fileName.substring(17);
		
		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		if (factura == null) {
			return;
		}
		
		facturasService.crearZipWs(uuid);
		  
		fileName += ".zip";
		
		Path file = Paths.get(path, fileName); 
		  
		if (Files.exists(file)) {
			response.setContentType("application/force-download");
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName); 
			try { 
				  Files.copy(file, response.getOutputStream());
				  response.getOutputStream().flush(); 
		    } catch (IOException ex) {
		         	ex.printStackTrace(); } 
		}
	  
	}
	
	@RequestMapping("/DescargarMultiple/{archivos}") 
	public void DescargarMultiple( @PathVariable("archivos") String archivos, 
		                          HttpServletResponse response) throws IOException {
		
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		
		if (archivos.trim()=="") {
			return;
		}
		
		String fileName = ArmarNombreArchivo(archivos);		
		facturasService.crearZipMultiple(fileName, archivos);
		fileName += ".zip";
		
		Path file = Paths.get(path, fileName); 
		  
		if (Files.exists(file)) {
			response.setContentType("application/force-download");
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName); 
			try { 
				Files.copy(file, response.getOutputStream());
				response.getOutputStream().flush(); 
		    } catch (IOException ex) {
		     	ex.printStackTrace();
		    } 
		}
	  
	}
	
	private String ArmarNombreArchivo (String archivos) {
		Date fecha = new Date(Calendar.getInstance().getTimeInMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = "SODIMAC";
		String uuid = "";
		
		if (archivos.indexOf(",") > 0) {
			String[] arrArchivos = archivos.split(",");
			uuid = arrArchivos[0].substring(17);
		} else {
			uuid = archivos.substring(17);
		}
		
		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		if (factura != null) {
			fileName += "_" + seguridadService.desencriptar(factura.getRfc().trim());
		}
		
		fileName += "_" + formatter.format(fecha);
		
		return fileName;
	}
	
	@GetMapping("/ReenvioFacturaMultiple")
	@ResponseBody
    public String reenviarFacturaMultiple(@RequestParam(value = "rfc") String rfc, 
    							  @RequestParam(value = "id") String archivos, 
    		                      @RequestParam(value = "eMailCC") String eMailCC
    		                      )
    {
		String result = "";
		String eMailTo = "";
		String razonSocial = "";
		
		try{
			
			if (!clientesService.validarRfcExpresionRegular(rfc)) {
				return "error";
			}
			if (!clientesService.validarEmailExpresionRegular(eMailCC)) {
				return "error";
			}
			
			ClientesEntity cliente = clientesService.getCliente(rfc);
			if (cliente == null) {
				ClientesTemporalEntity clienteTmp = clientesTemporalService.getCliente(rfc);
				if (clienteTmp != null) {
					razonSocial = seguridadService.desencriptar(clienteTmp.getRazonSocial());					
				}
			} else {
				razonSocial = seguridadService.desencriptar(cliente.getRazonSocial());
			}
			
			eMailTo = obtenerCorreoFacturaMultiple(archivos);
			if (eMailTo.isEmpty()) {
				return "error";
			}

			String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
			String asunto = catConfiguracionService.findParameterByKey("Mail.Usuario.EnvioFactura.Subject");
			String destinatario = eMailTo;
			String destinatarioCC = eMailCC;
			String mensaje = catConfiguracionService.findParameterByKey("Mail.Usuario.EnvioFactura.BodyMessage");
			mensaje = mensaje.replace("{nombreCliente}", razonSocial);
			boolean esHtml = Boolean.parseBoolean(catConfiguracionService.findParameterByKey("Mail.Usuario.EnvioFactura.IsHtml"));
			
			String fileName = ArmarNombreArchivo(archivos);		
			facturasService.crearZipMultiple(fileName, archivos);
			String file = path + fileName + ".zip";

			actividadesModel.setRfc(rfc);
			actividadesModel.registrarActividad(28, null,"ReenvioFacturaMultiple");
			
			errorComponent.setRfc(rfc);
			errorComponent.setTicket("");
			
			boolean statusEnvio = mailSenderService.enviarCorreo(destinatario, asunto, destinatarioCC, mensaje, esHtml, file);
			
			if (statusEnvio) {
				result = "success";
			} else {
				result = "error";
			}
			
			eliminarArchivosMultiple(archivos);
			UtilsFile.EliminarArchivo(file);

			
		} catch(Exception e) {
			e.printStackTrace();
			errorComponent.setRfc(rfc);
			errorComponent.setTicket("");
			errorComponent.setPagina("reenviarFacturaMultiple");
			errorComponent.guardarLog(e);
			result = "error";
		}
		
		return result;
    }
	
	private String obtenerCorreoFacturaMultiple (String archivos) {
		String uuid = "";
		String result = "";
		
		if (archivos.indexOf(",") > 0) {
			String[] arrArchivos = archivos.split(",");
			uuid = arrArchivos[0].substring(17);
		} else {
			uuid = archivos.substring(17);
		}
		
		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		if (factura != null) {
			result = seguridadService.desencriptar(factura.getEmail());
		}
				
		return result;
	}
	
	@PostMapping("/EliminarTicket")
	@ResponseBody
	public String EliminarTicket (@RequestParam("ticket") String ticket, HttpSession session) {
		
		String ticketValidado = ticketsService.validarTicketExpresionRegular(ticket);
		
		listaFacturasService.deleteItem(ticketValidado, session.getId());
		return "OK";
	}
	
	@GetMapping("/ObtenerTickets")
	@ResponseBody
	public String obtenerTickets(HttpSession session) {

		String result = "";
		List< ListaFacturasEntity> listaFacturas = listaFacturasService.getListaFacturasBySessionId(session.getId());
		for (int i = 0; i < listaFacturas.size(); i++) {
			ListaFacturasEntity facturaItem = listaFacturas.get(i);
			if (i < (listaFacturas.size()-1)) {
				result +=  facturaItem.getTicket() + ",";
			} else {
				result +=  facturaItem.getTicket();
			}
			
		}
				
		return result;
	}
	
	@PostMapping("/getExpresionRegular")
	@ResponseBody
	public String getExpresionRegular() {
		
		String expresionRegularRfcCaracteres = catConfiguracionService.findParameterByKey("ExpresionRegular.Rfc.Caracteres");
		String expresionRegularRfc = catConfiguracionService.findParameterByKey("ExpresionRegular.Rfc");
		String expresionRegularEmail = catConfiguracionService.findParameterByKey("ExpresionRegular.Email");
		
		return expresionRegularRfcCaracteres+"@"+expresionRegularRfc+"@"+expresionRegularEmail;

	}
	
	@PostMapping("/getIsDescargar")
	@ResponseBody
	public String getIsDescargar() {
		return errorComponent.getDescargar();

	}
	
	@PostMapping("/ValidarCodigoPostal")
	@ResponseBody
	public String validarCodigoPostal(@RequestParam("codigoPostal") String codigoPostal) {
		return clientesService.validarCodigoPostal(codigoPostal);
	}

	@PostMapping("/VersionTimbrado")
	@ResponseBody
	public String versionTimbrado(@RequestParam("idAplicacion") Integer idAplicacion) {
		return clientesService.consultarVersionCFDI(idAplicacion);
	}
	
	@PostMapping("/ValidarRegimenCapital")
	@ResponseBody
	public String validarRegimenCapital(@RequestParam("razonSocial") String razonSocial) {
		return clientesService.validarRegimenCapital(razonSocial);
	}
	
	@PostMapping("/VersionEspecificaTimbrado")
	@ResponseBody
	public String versionTimbradoEspecifico(@RequestParam("ticket") String ticket, @RequestParam("version") String version, @RequestParam("vvee") String vvee) {
		return clientesService.consultarVersionCFDIEspecifico(ticket, version, vvee);
	}
	
	@PostMapping("/UsoDeCfdiVersion")
	@ResponseBody
	public String consultarUsoCfdiVersion(@RequestParam("rfc") String rfc
									    , @RequestParam("version") String version
										, @RequestParam("regimenFiscal") String regimenFiscal) {
		
		EVersionCFDI versionCfdiEnum = EVersionCFDI.getVersionByDesc(version);
		Integer idTipoPersona = ETipoPersonaEnum.FISICA.getId();
    	if (rfc.length()==12) { 
    		idTipoPersona = ETipoPersonaEnum.MORAL.getId();
    	}
		return clientesService.consultarUsoCfdiVersion(idTipoPersona, versionCfdiEnum.getId(), regimenFiscal);
	}
}
