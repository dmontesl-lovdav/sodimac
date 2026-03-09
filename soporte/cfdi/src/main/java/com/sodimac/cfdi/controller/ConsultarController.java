/**
 * 
 */
package com.sodimac.cfdi.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.cfdi.component.ActividadesComponent;
import com.sodimac.cfdi.component.ErrorComponent;
import com.sodimac.cfdi.entity.fiscal.FacturasEntity;
import com.sodimac.cfdi.model.BodyComplementoCorreoTYPE;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.model.menu.MenuItem;
import com.sodimac.cfdi.models.ClientesTemporalModel;
import com.sodimac.cfdi.models.FacturasMultipleModel;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.ClientesService;
import com.sodimac.cfdi.service.FacturasService;
import com.sodimac.cfdi.service.MenuService;
import com.sodimac.cfdi.service.SeguridadService;
import com.sodimac.cfdi.util.UtilsFile;
import com.sodimac.cfdi.util.UtilsString;
import com.sodimac.cfdi.util.enums.EMenuType;
import com.sodimac.cfdi.service.CatMensajesService;

@Controller
public class ConsultarController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(ConsultarController.class);
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private ClientesService clientesService;
	
	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	private ActividadesComponent actividadesModel;
	
	@Autowired
	private ErrorComponent errorComponent;
	
	@Autowired
	private CatMensajesService catMensajesService;
	
	MultipleModalResponse multipleModalResponse;

	@Value("${cfdiVersion}")
	private String version;
	

	
	@GetMapping("/consultar")
	public String consultarCfdi(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/consultar");
		return "/consultarCfdi2";//consultarCfdi2
		
//		if (getModelAttributes(model, request, "", "/consultar")) {
//			return "/consultarCfdi2";//consultarCfdi2
//		} else {
//			return "redirect:/inicio";
//		}
	}
	

	@PostMapping("/consultar/inicializarBitacora")
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
		return "OK";
	}
	
	@PostMapping("/consultar/getConfiguracion")
	@ResponseBody
	public String getConfiguracion(@RequestParam("NombreCampo") String NombreCampo) {
		String result = "";
		try {
			result = catConfiguracionService.findParameterByKey(NombreCampo);

			// LOGS DE EVIDENCIA - INICIO
			logger.info("[CONFIGURACION] getConfiguracion llamado:");
			logger.info("[CONFIGURACION] - NombreCampo solicitado: {}", NombreCampo);
			logger.info("[CONFIGURACION] - Valor retornado: {}", result);
			// LOGS DE EVIDENCIA - FIN

		} catch (Exception e) {
			logger.error("getConfiguracion ", e);
		}
		return result;

	}
	
	@PostMapping("/consultar/getExpresionRegular")
	@ResponseBody
	public String getExpresionRegular() {
		String result = "";
		try {
			String expresionRegularRfcCaracteres = catConfiguracionService.findParameterByKey("ExpresionRegular.Rfc.Caracteres");
			String expresionRegularRfc = catConfiguracionService.findParameterByKey("ExpresionRegular.Rfc");
			String expresionRegularEmail = catConfiguracionService.findParameterByKey("ExpresionRegular.Email");
			
			result = expresionRegularRfcCaracteres+"@"+expresionRegularRfc+"@"+expresionRegularEmail;

		} catch (Exception e) {
			logger.error("getExpresionRegular ", e);
		}
		return result;
		
	}
	
	@GetMapping("/consultar/getStartDate")
	@ResponseBody
	public String getStartDate() {
		String result = "";
		try {
			result = catConfiguracionService.findParameterByKey("Multiple.cfdi.getStartDate");

			// LOGS DE EVIDENCIA - INICIO
			logger.info("[CONFIGURACION] getStartDate llamado:");
			logger.info("[CONFIGURACION] - Valor retornado (fecha inicial): {}", result);
			// LOGS DE EVIDENCIA - FIN

		} catch (Exception e) {
			logger.error("getStartDate ", e);
		}
		return result;

	}
	
	@PostMapping("/consultar/ObtenerTipoComprobante")
	@ResponseBody
	public String listaTipoComprobante() {

		return  facturasService.getTipodeComprobante();
	}	
	
	@PostMapping("/consultar/ObtenerTipoOrigen")
	@ResponseBody
	public String listaTipoOrigen() {
		String listOrigen = facturasService.getTipoOrigen();
		return listOrigen;
	}	

	@GetMapping("/consultar/listarFacturaMultiple")
	public String listarCfdis(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam("uuid") String uuid
			, @RequestParam("ticket") String ticket
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("tipocomprobante") String tipoCom
			, @RequestParam("pidOrigen") int pidOrigen
			, @RequestParam("pmonto") String pmonto
			, Model theModel) {

		List<FacturasMultipleModel> datos = new ArrayList <FacturasMultipleModel>();
		multipleModalResponse = new MultipleModalResponse(true,"", null);

		try {
			validarDatosMultiple(rfc, dateDesde, dateHasta, pageNumber);
			
			if (!multipleModalResponse.isSuccess()) {
				return "Datos invalidos";
			}

			start = (Integer.parseInt(pageNumber) * rowsPerPage) - rowsPerPage;
			if (start < 0) {
				start = 0;
			}
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			String rfcEncriptado = "";
			if (!rfc.isEmpty()) {
				rfcEncriptado = seguridadService.encriptar(rfc);
			}
			
			datos = facturasService.getCfdiFechas(dateDesdeParse, dateHastaParse, rfcEncriptado, uuid, ticket, start, rowsPerPage,tipoCom, pidOrigen, pmonto);
			
			theModel.addAttribute("dClientesMultiple", datos);

			if (datos.size() <= 0) {
				return "fragments/cfdiList :: noResults";
			} else {
				return "fragments/cfdiList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarCfdis ", e);
		}
		return "";
				
	}
	
	void validarDatosMultiple (String rfc, String dateDesde, String dateHasta, String pageNumber) {

		if (dateDesde=="" || dateHasta=="" || pageNumber=="") {
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

		if (!rfc.isEmpty() && !clientesService.validarRfcExpresionRegular(rfc)) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		}
				
		
		// Añadir validaciones para cada dato

	}	

	@PostMapping("/consultar/validarRfcMultiple")
	@ResponseBody
	public String validarRfcMultiple(@RequestParam("rfc") String rfc) {
		String result = "Invalido";
		try {
			if (!clientesService.validarRfcExpresionRegular(rfc)) {
				return result;
			}
			
			if (clientesService.isExistRfc(rfc)) {
				result = "OK";
			}
			
		} catch (Exception e) {
			logger.error("validarRfcMultiple ", e);
		}
		return result;
	
	}

	@GetMapping("/consultar/visualizarPdf")
	@ResponseBody
	public String visualizarPdf(@RequestParam("id") String id, @RequestParam("method") String method) {
		
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
			
			ClienteTicketTimbrarExpRespTYPE response = facturasService.crearBase64Ws(uuid);
			if (response != null && response.getRespuesta() != null && response.getRespuesta().getCodigo().equals("1")) {
				result = response.getRespuesta().getDescripcion();
			}

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

	@GetMapping("/consultar/obtenerCorreo")
	@ResponseBody
	public String obtenerCorreo(@RequestParam("uuid") String uuid) {

		String result = "";

		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		if (factura != null) {
			result = UtilsString.emailMask(seguridadService.desencriptar(factura.getEmail()));
		}
				        
		return result;
	}	

	@GetMapping("/consultar/reenvioFactura")
	@ResponseBody
    public String reenviarFactura(@RequestParam(value = "rfc") String rfc, 
    							  @RequestParam(value = "id") String id, 
    		                      @RequestParam(value = "eMailCC") String eMailCC) 
    {
		String result = "error";
		List<String> datosArr = new ArrayList <String>();
		String eMailTo = "";
		String razonSocial = "";
		String nombreArchivo = "";
		String fileName = "";
		String ticket = "";
		FacturasEntity factura = null;
		String xml = "";
		errorComponent.setPagina("ReenvioFactura");
		
		try{
			
			if (!clientesService.validarRfcExpresionRegular(rfc)) {
				return "error";
			}
			
			factura = facturasService.getFacturaByUuid(id);
			if (factura != null) {
				eMailTo = seguridadService.desencriptar(factura.getEmail());
				nombreArchivo = factura.getNombreArchivo();
				rfc = seguridadService.desencriptar(factura.getRfc());
				fileName = factura.getNombreArchivo();
				ticket = factura.getTicket();
				
				logger.info("eMailTo: " + eMailTo);
				logger.info("nombreArchivo: " + nombreArchivo);
				logger.info("rfc: " + rfc);
				logger.info("fileName: " + fileName);
				logger.info("ticket: " + ticket);
				
				xml = seguridadService.desencriptar(factura.getXml());
				Document document = UtilsFile.ObtenerDocumentXml (xml);
				Node nNode = document.getElementsByTagName("cfdi:Receptor").item(0);
				Element eElement = (Element) nNode;
				razonSocial = eElement.getAttribute("Nombre");
			
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
				
				BodyComplementoCorreoTYPE model = new BodyComplementoCorreoTYPE();
				model.setEmail(eMailTo);
				model.setEmailCC(eMailCC);
				model.setRazonSocial(razonSocial);
				model.setUuid(factura.getUuid());
				model.setXml( xml );
			
				try {                        	
					ClientResponseTYPE<String> response = facturasService.enviarCorreoFactura(model);
					if (response.getRespuesta().getCodigo().equals("1")) {
						logger.info("Factura enviado por correo: " + id);
						result = "success";
					} else {
						logger.info("Factura error:     " +  id + " (" + response.getRespuesta().getCodigo() + ")");
						result = "error";
					}
	            } catch (Exception e) {
	    			e.printStackTrace();
	    			result = "error";
	    		}
			}
		} catch(Exception e) {
			e.printStackTrace();
			errorComponent.guardarLog(e);
			result = "error";
		}
		return result;
    }

	@RequestMapping("/consultar/descargarArchivo/{file_name}") 
	public void DescargarArchivo( @PathVariable("file_name") String fileName, 
		                          HttpServletResponse response) throws IOException {
	  
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		String uuid = fileName.substring(17);
		
		FacturasEntity factura = facturasService.getFacturaByUuid(uuid);
		if (factura == null) {
			return;
		}
		
		facturasService.crearZip(uuid);
				  
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

	@RequestMapping("/consultar/descargarMultiple/{archivos}") 
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

	@GetMapping("/consultar/refacturar")
	@ResponseBody
	public String refacturar(
			@RequestParam String idFactura
            ,@RequestParam String rfc
            ,@RequestParam String usoCfdi
            ,@RequestParam String metodoPago
            ,@RequestParam String razonSocial
            ,@RequestParam String email
            ,@RequestParam String nombreObra
            ,@RequestParam String responsableObra
            ,HttpSession session) {
		
		String result = "";
		int codigoRetorno = 0;
		int autorizoGuardado = 0;
		int idFacturaPac = Integer.parseInt(idFactura);
		
		FacturasEntity factura = facturasService.getFacturaByIdFacturaPac(idFacturaPac);
		if (factura == null) {
			return catMensajesService.get(0).getDescripcionMensaje();
		}
		String ticket = factura.getTicketBct();
		String xml = seguridadService.desencriptar(factura.getXml());
		Document document = UtilsFile.ObtenerDocumentXml (xml);
		Node nNode = document.getElementsByTagName("cfdi:Comprobante").item(0);
        Element eElement = (Element) nNode;
        BigDecimal total = BigDecimal.valueOf(Double.parseDouble(eElement.getAttribute("Total")));
		
		ClientesTemporalModel model = new ClientesTemporalModel();
		model.setIdFacturaPac(idFacturaPac);
        model.setAutorizoGuardado(autorizoGuardado);
        model.setEmail(email);
        model.setIdUsoCfdi(usoCfdi);
        model.setMetodoPago(metodoPago);
        model.setRazonSocial(razonSocial);
        model.setRfc(rfc);
        model.setTicket(ticket);
        model.setTotal(total);
        model.setNombreObra(nombreObra);
        model.setResponsableObra(responsableObra);
        try {
        	codigoRetorno = facturasService.cancelar(idFacturaPac);
//        	if (codigoRetorno != 1) {
//	        	String mensaje = catMensajesService.get(codigoRetorno).getDescripcionMensaje();
//	        	mensaje = mensaje.replace("{ticket}", model.getTicket()).replace("{rfc}", model.getRfc());
//        		
//        		return mensaje;
//        	}
        	
    		codigoRetorno = facturasService.refacturar(model);
        	if (codigoRetorno != 1) {
	        	String mensaje = catMensajesService.get(codigoRetorno).getDescripcionMensaje();
	        	mensaje = mensaje.replace("{ticket}", model.getTicket()).replace("{rfc}", model.getRfc());
        		
        		return mensaje;
        	}

    		result = "success";
        	
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	//VIVE
	@RequestMapping("/consultar/descargaXlsx")
	public void	descargaExcel(	
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam("uuid") String uuid
			, @RequestParam("ticket") String ticket
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("tipocomprobante") String tipoCom
			, @RequestParam("pidOrigen") int pidOrigen
			, @RequestParam("pmonto") String pmonto
			, Model theModel,
			HttpServletResponse response) throws Exception{

		multipleModalResponse = new MultipleModalResponse(true,"", null);
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		
			validarDatosMultiple(rfc, dateDesde, dateHasta, pageNumber);
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			String rfcEncriptado = "";
			if (!rfc.isEmpty()) {
				rfcEncriptado = seguridadService.encriptar(rfc);
			}
			
			SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			Date Ahora = new Date();
			
			String nombreArchivo = "Reporte_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
			nombreArchivo = nombreArchivo.replaceAll("/","");
			nombreArchivo = nombreArchivo.replaceAll(":","");
			
			
			UtilsFile.EliminarArchivos(path, ".xlsx",nombreArchivo.substring(0, 16));
			
			Path file = Paths.get(path, nombreArchivo); 
			
			facturasService.getCfdiExcelFechas(dateDesdeParse, dateHastaParse, rfcEncriptado, uuid, ticket,nombreArchivo,tipoCom, pidOrigen, pmonto);

			if (Files.exists(file)) {
				response.setContentType("application/force-download");
				response.addHeader("Content-Disposition", "attachment; filename="+nombreArchivo); 
				
				try { 
					  Files.copy(file, response.getOutputStream());
					  response.getOutputStream().flush(); 
			    } catch (IOException ex) {
			         	ex.printStackTrace(); }
			}
			
	}	
	
}
