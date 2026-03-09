/**
 * 
 */
package com.sodimac.cfdi.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.cfdi.clientews.wsft.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.cfdi.component.ErrorComponent;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.models.ClientesTemporalModel;
import com.sodimac.cfdi.models.FacturacionVveeModel;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.CatTiendaService;
import com.sodimac.cfdi.service.FacturacionVveeService;
import com.sodimac.cfdi.service.FacturasService;

@Controller
@RequestMapping("/facturacionVvee")
public class FacturacionVveeController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(ComisionesController.class);
	
	@Autowired
	private FacturacionVveeService facturacionVveeService;
	
	@Autowired
	private CatTiendaService catTiendaService;
	
	private MultipleModalResponse multipleModalResponse;
	
	@Autowired
	private ErrorComponent errorComponent;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private FacturasService facturasService;
	
	@GetMapping("/index")
	public String comisionesIndex(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/facturacionVvee/index");
		return "/facturacionVvee";
	}
	
	@GetMapping("/listarFacturacionVvee")
	public String listarFacturacionVvee(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("pTicket") String pTicket
			, @RequestParam("pTienda") String pTienda
			, Model theModel
			, HttpServletRequest request) {
		
		List<FacturacionVveeModel> datos = null;
		multipleModalResponse = new MultipleModalResponse(true,"", null);

		try {
			validarDatosMultiple(dateDesde, dateHasta, pageNumber);
			
			if (!multipleModalResponse.isSuccess()) {
				return "Datos invalidos";
			}

			start = (Integer.parseInt(pageNumber) * rowsPerPage) - rowsPerPage;
			if (start < 0) {
				start = 0;
			}
			String ticket = null;
			Integer tienda = 0;
			
			if (pTienda != null && !pTienda.isEmpty()) {
				tienda = Integer.valueOf(pTienda);
			}
			
			if (pTicket != null && !pTicket.isEmpty()) {
				ticket = pTicket;
			}
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			
			datos = this.facturacionVveeService.obtenerFacturacionVveeByParams(dateDesdeParse, dateHastaParse, ticket, start, rowsPerPage, tienda);
			theModel.addAttribute("listFacturas", datos);

			if (datos.size() <= 0) {
				return "fragments/facturacionVveeList :: noResults";
			} else {
				return "fragments/facturacionVveeList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listFacturas ", e);
		}
		return "";
	}
	
	
	@RequestMapping("/listarFacturacionVvee/descargaXlsx")
	public void	descargaExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("pTicket") String pTicket
			, @RequestParam("pTienda") String pTienda
			, Model theModel
			, HttpServletResponse response
			, HttpServletRequest request) throws Exception{

		multipleModalResponse = new MultipleModalResponse(true,"", null);
		
		validarDatosMultiple(dateDesde, dateHasta, "0");
		
		String[] partsDesde = dateDesde.split("/");
		String[] partsHasta = dateHasta.split("/");
		String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
		String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
		SimpleDateFormat MI_FORMATO = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date Ahora = new Date();
		
		String nombreArchivo = "Reporte_FacturacionVvee_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
		nombreArchivo = nombreArchivo.replaceAll("/","");
		nombreArchivo = nombreArchivo.replaceAll(":","");
		String ticket = null;
		Integer tienda = 0;
		if (pTienda != null && !pTienda.isEmpty()) {
			tienda = Integer.valueOf(pTienda);
		}
		if (pTicket != null && !pTicket.isEmpty()) {
			ticket = pTicket;
		}
		
		byte[] byteArray = this.facturacionVveeService.createExcel(dateDesdeParse, dateHastaParse, ticket, tienda);
		try {
			
			if (byteArray != null) {
				
				response.setContentType("application/force-download");
				response.addHeader("Content-Disposition", "attachment; filename="+nombreArchivo); 
				
				response.getOutputStream().write(byteArray , 0, byteArray.length);
				response.getOutputStream().flush(); 
			}
	    } catch (IOException ex) {
	         ex.printStackTrace(); 
	    }
	}
	
	@PostMapping("/obtenerTiendas")
	@ResponseBody
	public String listaTiendas() {
		return catTiendaService.getGsonTiendas();
	}

	private void validarDatosMultiple (String dateDesde, String dateHasta, String pageNumber) {

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
	}
	
	@GetMapping("/visualizarPdf")
	@ResponseBody
	public String visualizarPdf(@RequestParam("uuid") String uuid
							  , @RequestParam("method") String method) {
		String result = "";
		errorComponent.setPagina("VisualizarPdf");
		try {
			errorComponent.setTicket(uuid);
			//uuid = "B4296ABB-397E-4D9E-B81D-E8085A83BE34"; //TODO Cambiar
			
			ClienteTicketTimbrarExpRespTYPE response = facturasService.crearBase64Ws(uuid);
			if (response != null && response.getRespuesta() != null && response.getRespuesta().getCodigo().equals("1")) {
				result = response.getRespuesta().getDescripcion();
			}			
		} catch (Exception e) {
			e.printStackTrace();
			errorComponent.guardarLog(e);
			result = "";
		}
		return result;
	}
	
	@RequestMapping("/descargarArchivo/{uuid}") 
	public void DescargarArchivo( @PathVariable("uuid") String uuid, 
		                          HttpServletResponse response) throws IOException {
	  
		//uuid = "B4296ABB-397E-4D9E-B81D-E8085A83BE34"; //TODO Cambiar
		String path = catConfiguracionService.findParameterByKey("Mail.PathFile");
		this.facturasService.crearZipWs(uuid);
				  
		String fileName = uuid +".zip";
		 
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
	
	@GetMapping("/reenvioFactura")
	@ResponseBody
    public String reenviarFactura(@RequestParam(value = "uuid") String uuid, 
    		                      @RequestParam(value = "eMailCC") String eMailCC,
    		                      @RequestParam(value = "email") String eMailTo) 
    {
		String result = "error";
		String xml = "";
		
		try {
			if (eMailTo.isEmpty()) {
				return "error";
			}
			
			//uuid = "B4296ABB-397E-4D9E-B81D-E8085A83BE34"; //TODO Cambiar
			ClientesTemporalModel model = new ClientesTemporalModel();
			model.setEmail(eMailTo);
			model.setEmailCC(eMailCC);
			model.setUuid(uuid);
			model.setXml( xml );
			
			try {                        	
				ClientResponseTYPE<String> response = facturasService.enviarCorreoFactura(model);
				if (response.getRespuesta().getCodigo().equals("1")) {
					logger.info("Venta EE enviado por correo: " + uuid);
					result = "success";
				} else {
					logger.info("Venta EE error:     " +  uuid + " (" + response.getRespuesta().getCodigo() + ")");
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
}
