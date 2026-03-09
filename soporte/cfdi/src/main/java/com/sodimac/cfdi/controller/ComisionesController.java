/**
 * 
 */
package com.sodimac.cfdi.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.cfdi.models.ComisionesPagadasModel;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.service.CatTiendaService;
import com.sodimac.cfdi.service.ComisionesPagadasService;

@Controller
@RequestMapping("/comisiones")
public class ComisionesController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(ComisionesController.class);
	
	@Autowired
	private ComisionesPagadasService comisionesPagadasService;
	
	@Autowired
	private CatTiendaService catTiendaService;
	
	private MultipleModalResponse multipleModalResponse;

	@GetMapping("/index")
	public String comisionesIndex(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/comisiones/index");
		return "/comisiones";
	}
	
	@GetMapping("/listarComisiones")
	public String listarComisiones(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("pTicket") String pTicket
			, @RequestParam("pTienda") String pTienda
			, Model theModel
			, HttpServletRequest request) {
		
		List<ComisionesPagadasModel> datos = null;
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
			
			datos = comisionesPagadasService.obtenerComisionesPagadassByParams(dateDesdeParse, dateHastaParse, ticket, start, rowsPerPage, tienda);
			theModel.addAttribute("listComisiones", datos);

			if (datos.size() <= 0) {
				return "fragments/comisionesList :: noResults";
			} else {
				return "fragments/comisionesList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarComisiones ", e);
		}
		return "";
	}
	
	
	@RequestMapping("/listarComisiones/descargaXlsx")
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
		
		String nombreArchivo = "Reporte_Comisiones_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
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
		
		byte[] byteArray = this.comisionesPagadasService.createExcel(dateDesdeParse, dateHastaParse, ticket, tienda);
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
}
