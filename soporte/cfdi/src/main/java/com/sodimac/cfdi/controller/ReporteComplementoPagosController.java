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
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.models.ReporteComplementoModel;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.ClientesService;
import com.sodimac.cfdi.service.ReporteComplementoService;
import com.sodimac.cfdi.service.SeguridadService;
import com.sodimac.cfdi.util.UtilsFile;

@Controller
@RequestMapping("/reporteComplemento")
public class ReporteComplementoPagosController extends BaseController {
	
	private Logger logger = LoggerFactory.getLogger(ReporteComplementoPagosController.class);
	
	@Autowired
	private ReporteComplementoService reporteComplementoService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private ClientesService clientesService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	@Value("${cfdiVersion}")
	private String version;
	
	private MultipleModalResponse multipleModalResponse;
	
	@GetMapping("/index")
	public String consultarCfdi(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/reporteComplemento/index");
		return "/reporteComplemento";
		
//		HttpSession session = request.getSession();
//		
//		if(session == null || session.getAttribute("usuario") == null) {
//			return "redirect:/index";
//		} else {
//			if (getModelAttributes(model, request, "", "/reporteComplemento/index")) {
//				return "/reporteComplemento";
//			} else {
//				return "redirect:/inicio";
//			}
//		}
	}
	
	@GetMapping("/listarReporteComplementos")
	public String listarReporteComplementos(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam("ticket") String ticket
			, @RequestParam("uuid") String uuid
			, @RequestParam("monto") String monto
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, Model theModel
			, HttpServletRequest request) {
		
		List<ReporteComplementoModel> datos = new ArrayList <ReporteComplementoModel>();
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
			
			datos = reporteComplementoService.getReporteComplementosByParams(dateDesdeParse, dateHastaParse, rfcEncriptado, start, rowsPerPage, ticket, uuid, monto);
			theModel.addAttribute("listReporteComplementos", datos);

			if (datos.size() <= 0) {
				return "fragments/reporteComplementoList :: noResults";
			} else {
				return "fragments/reporteComplementoList  :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarReporteComplementos ", e);
		}
		return "";
	}
	
	@RequestMapping("/listarReporteComplementos/descargaXlsx")
	public void	descargaExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam("ticket") String ticket
			, @RequestParam("uuid") String uuid
			, @RequestParam("monto") String monto
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
		
		SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		Date Ahora = new Date();
		
		String nombreArchivo = "Reporte_Historico_Pago_Complementos_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
		nombreArchivo = nombreArchivo.replaceAll("/","");
		nombreArchivo = nombreArchivo.replaceAll(":","");
		
		
		UtilsFile.EliminarArchivos(path, ".xlsx",nombreArchivo.substring(0, 16));
		
		Path file = Paths.get(path, nombreArchivo); 
		
		reporteComplementoService.getReporteComplementosExcelFechas(dateDesdeParse, dateHastaParse, rfcEncriptado, ticket, uuid, monto, nombreArchivo);

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
}
