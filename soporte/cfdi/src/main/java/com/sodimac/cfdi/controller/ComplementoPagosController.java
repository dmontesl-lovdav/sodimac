/**
 * 
 */
package com.sodimac.cfdi.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.cfdi.component.ActividadesComponent;
import com.sodimac.cfdi.component.ErrorComponent;
import com.sodimac.cfdi.entity.fiscal.ComplementosEntity;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.BodyComplementoCorreoTYPE;
import com.sodimac.cfdi.models.ComplementosModel;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.models.PagoComplementoDetalleModel;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.ClientesService;
import com.sodimac.cfdi.service.ComplementoPagoService;
import com.sodimac.cfdi.service.ComplementosService;
import com.sodimac.cfdi.service.FacturasService;
import com.sodimac.cfdi.service.SeguridadService;
import com.sodimac.cfdi.util.UtilsFile;
import com.sodimac.cfdi.util.UtilsString;
import com.sodimac.cfdi.util.enums.EComplementoPago;

@Controller
@RequestMapping("/complementoPagos")
public class ComplementoPagosController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(ComplementoPagosController.class);
	
	@Autowired
	private ComplementoPagoService complementoPagoService;

	@Autowired
	private ComplementosService complementoService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Autowired
	private ClientesService clientesService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	private ErrorComponent errorComponent;
	
	@Autowired
	private ActividadesComponent actividadesModel;
	
	
	@Value("${cfdiVersion}")
	private String version;
	
	private MultipleModalResponse multipleModalResponse;

	@GetMapping("/index")
	public String consultarCfdi(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/complementoPagos/index");
		return "/complementos";
		
//		HttpSession session = request.getSession();
//		
//		if(session == null || session.getAttribute("usuario") == null) {
//			return "redirect:/index";
//		} else {
//			if (getModelAttributes(model, request, "", "/complementoPagos/index")) {
//				return "/complementos";
//			} else {
//				return "redirect:/inicio";
//			}
//			
//		}
	}
	
	@GetMapping("/listarComplementos")
	public String listarCfdis(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("tipocomplemento") String tipoCom
			, @RequestParam("pmonto") String pmonto
			, Model theModel
			, HttpServletRequest request) {
		
		List<ComplementosModel> datos = new ArrayList <ComplementosModel>();
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
			
			Double monto = 0.0d;
			if (pmonto != null && !pmonto.isEmpty()) {
				monto = Double.valueOf(pmonto);
			}
			
			datos = complementoService.getComplementosByParams(dateDesdeParse, dateHastaParse, rfcEncriptado, start, rowsPerPage, tipoCom, monto);
			theModel.addAttribute("listComplementos", datos);

			if (datos.size() <= 0) {
				return "fragments/complementoList :: noResults";
			} else {
				return "fragments/complementoList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarComplementos ", e);
		}
		return "";
	}
	
	
	@RequestMapping("/listarComplementos/descargaXlsx")
	public void	descargaExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam("tipocomplemento") String tipoCom
			, @RequestParam("pmonto") String pmonto
			, Model theModel
			, HttpServletResponse response
			, HttpServletRequest request) throws Exception{

		multipleModalResponse = new MultipleModalResponse(true,"", null);
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		
		validarDatosMultiple(rfc, dateDesde, dateHasta, "0");
		
		String[] partsDesde = dateDesde.split("/");
		String[] partsHasta = dateHasta.split("/");
		String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
		String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
		String rfcEncriptado = "";
		if (!rfc.isEmpty()) {
			rfcEncriptado = seguridadService.encriptar(rfc);
		}
		
		Double monto = 0.0d;
		if (pmonto != null && !pmonto.isEmpty()) {
			monto = Double.valueOf(pmonto);
		}
		
		SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		Date Ahora = new Date();
		
		String nombreArchivo = "Reporte_Complementos_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
		nombreArchivo = nombreArchivo.replaceAll("/","");
		nombreArchivo = nombreArchivo.replaceAll(":","");
		
		
		UtilsFile.EliminarArchivos(path, ".xlsx",nombreArchivo.substring(0, 16));
		
		Path file = Paths.get(path, nombreArchivo); 
		
		complementoService.getComplementosExcelFechas(dateDesdeParse, dateHastaParse, rfcEncriptado, nombreArchivo, tipoCom, monto);

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
	
	@GetMapping("/listarComplementos/folio")
	@ResponseBody
	public PagoComplementoDetalleModel listarFolio(
			  @RequestParam("idPagoComplemento") String idPagoComplemento
			, @RequestParam("folioFactura") String folioFactura
			, @RequestParam("realizaBusqueda") String realizaBusqueda
			, @RequestParam("estatusComplemento") String estatusComplemento
			, Model theModel) {

		PagoComplementoDetalleModel model = new PagoComplementoDetalleModel();
		boolean busqueda = Boolean.valueOf(realizaBusqueda);
		EComplementoPago eComplementoPago = EComplementoPago.getComplementoByEstatus(estatusComplemento);
		model = this.complementoPagoService.getDetalleComplementoPagoPorFolio(Integer.valueOf(idPagoComplemento), Integer.valueOf(folioFactura), busqueda, eComplementoPago);
		return model;
	}
	
	@PostMapping("/asignarComplementosFolio")
	@ResponseBody
	public String asignarComplementosFolio(
			  @RequestParam("idPagoComplemento") String idPagoComplemento
			, @RequestParam("folioFactura") String folioFactura
			, @RequestParam("estatusComplemento") String estatusComplemento
			, Model theModel) {
		
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
		Integer intFolioFactura = 0;
		boolean busqueda = false;
		try {
			if (folioFactura != null && !folioFactura.isEmpty()) {
				intFolioFactura = Integer.valueOf(folioFactura);
			}
			
			EComplementoPago eComplementoPago = EComplementoPago.getComplementoByEstatus(estatusComplemento);
			this.complementoPagoService.asignarComplementoPagoFolio(Integer.valueOf(idPagoComplemento), intFolioFactura, eComplementoPago);
			PagoComplementoDetalleModel model = this.complementoPagoService.getDetalleComplementoPagoPorFolio(Integer.valueOf(idPagoComplemento), 0, busqueda, eComplementoPago);
			this.complementoPagoService.actualizarSaldosPagoComplemento(Integer.valueOf(idPagoComplemento), model);
			clienteJson.addProperty("STATUS", "OK");
		} catch (Exception e) {
			e.printStackTrace();
			clienteJson.addProperty("STATUS", "ERROR");
		}
		return gson.toJson(clienteJson);
	}
	
	@PostMapping("/desvincularComplementosFolio")
	@ResponseBody
	public String desvincularComplementosFolio(
			  @RequestParam("idPagoComplemento") String idPagoComplemento
			, Model theModel) {
		
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
		this.complementoPagoService.desvincularPagoComplemento(Integer.valueOf(idPagoComplemento));
		this.complementoPagoService.actualizarSaldosPagoComplemento(Integer.valueOf(idPagoComplemento), null, null, null);
		clienteJson.addProperty("STATUS", "OK");
		return gson.toJson(clienteJson);
	}
	
	@PostMapping("/timbrarComplementoPago")
	@ResponseBody
	public String timbrarComplementosFolio(
			  @RequestParam("idPagoComplemento") String idPagoComplemento, Model theModel) {
		return this.complementoService.timbrarComplementoPago(Integer.valueOf(idPagoComplemento));
	}
	
	private void validarDatosMultiple (String rfc, String dateDesde, String dateHasta, String pageNumber) {

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
	}
	
	@GetMapping("/visualizarPdf")
	@ResponseBody
	public String visualizarPdf(@RequestParam("id") String idComplemento
							  , @RequestParam("method") String method) {
		String result = "";
		String rfc = "";
		String ticket = "";
		List<String> datosArr = new ArrayList <String>();
		ComplementosEntity complemento = null;
		errorComponent.setPagina("VisualizarPdf");
		
		try {
			//idComplemento = "22";
			complemento = complementoService.getComplemento(idComplemento);
			if (complemento != null) {
				rfc = seguridadService.desencriptar(complemento.getRfc());
				ticket = complemento.getTicket();
			}
			errorComponent.setRfc(rfc);
			errorComponent.setTicket(ticket);	
			
			ClienteTicketTimbrarExpRespTYPE response = facturasService.crearBase64WsComplemento(complemento.getUuid());
			if (response != null && response.getRespuesta() != null && response.getRespuesta().getCodigo().equals("1")) {
				result = response.getRespuesta().getDescripcion();
			}

			actividadesModel.setTicket(ticket);
			if (method.equals("visualizar") == true) {
				datosArr.clear();
				datosArr.add(complemento.getUuid());
				actividadesModel.setRfc(rfc);
				actividadesModel.registrarActividad(30, datosArr,"VisualizarPdf");
			} else {
				datosArr.clear();
				datosArr.add(complemento.getUuid());
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
	
	@RequestMapping("/descargarArchivo/{idComplemento}") 
	public void DescargarArchivo( @PathVariable("idComplemento") String idComplemento, 
		                          HttpServletResponse response) throws IOException {
	  
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		ComplementosEntity complemento = null;
		
		complemento = complementoService.getComplemento(idComplemento);
		if (complemento == null) {
			return;
		}
		facturasService.crearZipComplemento(idComplemento);
				  
		String fileName = complemento.getNombreArchivo() +".zip";
		 
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
	
	@GetMapping("/consultar/obtenerCorreo")
	@ResponseBody
	public String obtenerCorreo(@RequestParam(value = "idComplemento") String idComplemento) {

		String result = "";
		ComplementosEntity complemento = complementoService.getComplemento(idComplemento);
		if (complemento != null) {
			result = UtilsString.emailMask(seguridadService.desencriptar(complemento.getEmail()));
		}
		return result;
	}	
	
	@GetMapping("/reenvioFactura")
	@ResponseBody
    public String reenviarFactura(@RequestParam(value = "rfc") String rfc, 
    							  @RequestParam(value = "idComplemento") String idComplemento, 
    		                      @RequestParam(value = "eMailCC") String eMailCC,
    		                      @RequestParam(value = "email") String eMailTo) 
    {
		String result = "error";
		List<String> datosArr = new ArrayList <String>();
		String razonSocial = "";
		String nombreArchivo = "";
		String fileName = "";
		String ticket = "";
		String xml = "";
		
		errorComponent.setPagina("ReenvioFactura");
		try{
			
			ComplementosEntity complemento = null;
			complemento = complementoService.getComplemento(idComplemento);
			if (complemento != null) {
				nombreArchivo = complemento.getNombreArchivo();
				rfc = seguridadService.desencriptar(complemento.getRfc());
				fileName = complemento.getNombreArchivo();
				ticket = complemento.getTicket();
				
				logger.info("eMailTo: " + eMailTo);
				logger.info("nombreArchivo: " + nombreArchivo);
				logger.info("rfc: " + rfc);
				logger.info("fileName: " + fileName);
				logger.info("ticket: " + ticket);
				
				xml = seguridadService.desencriptar(complemento.getXml());
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
				datosArr.add(idComplemento);
				actividadesModel.registrarActividad(32, datosArr, "ReenvioFactura");
				
				BodyComplementoCorreoTYPE model = new BodyComplementoCorreoTYPE();
				model.setEmail(eMailTo);
				model.setEmailCC(eMailCC);
				model.setRazonSocial(razonSocial);
				model.setUuid(complemento.getUuid());
				model.setXml( xml );
				
				try {                        	
					ClientResponseTYPE<String> response = facturasService.enviarCorreoComplemento(model);
					if (response.getRespuesta().getCodigo().equals("1")) {
						logger.info("Complemento enviado por correo: " + idComplemento);
						result = "success";
					} else {
						logger.info("Complemento error:     " +  idComplemento + " (" + response.getRespuesta().getCodigo() + ")");
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
}
