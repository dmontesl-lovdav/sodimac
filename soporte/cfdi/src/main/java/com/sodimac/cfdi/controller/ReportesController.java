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

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.models.TableroControlTimbradoModel;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.DescargaService;
import com.sodimac.cfdi.service.ReportesService;
import com.sodimac.cfdi.util.UtilsFile;

@Controller
@RequestMapping("/reportes")
public class ReportesController extends BaseController {
	
	Logger logger = LoggerFactory.getLogger(ReportesController.class);
	
	@Value("${cfdiVersion}")
	private String version;
	
	@Autowired
	private ReportesService reportesService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;
	
	@Autowired
	private DescargaService descargaService;
	
	MultipleModalResponse multipleModalResponse;
	
	private final static SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
	
	@GetMapping("/index")
	public String consultarCfdi(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/reportes/index");
		return "/reportes";
		
//		HttpSession session = request.getSession();
//		
//		if(session == null || session.getAttribute("usuario") == null) {
//			return "redirect:/index";
//		} else {
//			if (getModelAttributes(model, request, "", "/reportes/index")) {
//				return "/reportes";
//			} else {
//				return "redirect:/inicio";
//			}
//		}
	}
	
	@PostMapping("/obtenerTiendas")
	@ResponseBody
	public String listaTiendas() {
		return reportesService.getTiendas();
	}
	
	@PostMapping("/obtenerCanales")
	@ResponseBody
	public String listaCanales() {
		return reportesService.getCanales();
	}
	
	@GetMapping("/listarTablero")
	public String listarTablero(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("ticket") String ticket
			, @RequestParam("canal") String canal
			, @RequestParam("tienda") String tienda
			, Model theModel) {

		List<TableroControlTimbradoModel> datos = new ArrayList <TableroControlTimbradoModel>();
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
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			
			datos = reportesService.getTableroByParams(dateDesdeParse, dateHastaParse, start, rowsPerPage, ticket, canal, tienda);
			
			theModel.addAttribute("listTablero", datos);
			if (datos.size() <= 0) {
				return "fragments/reportesList :: noResults";
			} else {
				return "fragments/reportesList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarTablero ", e);
		}
		return "";
				
	}
	
	@RequestMapping("/listarTablero/descargaXlsx")
	public void	descargaExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("ticket") String ticket
			, @RequestParam("canal") String canal
			, @RequestParam("tienda") String tienda
			, Model theModel
			, HttpServletResponse response) throws Exception{

			multipleModalResponse = new MultipleModalResponse(true,"", null);
			String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		
			validarDatosMultiple(dateDesde, dateHasta, "0");
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			
			Date Ahora = new Date();
			
			String nombreArchivo = "Reporte_Tablero_Control_Timbrado_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
			nombreArchivo = nombreArchivo.replaceAll("/","");
			nombreArchivo = nombreArchivo.replaceAll(":","");
			
			
			UtilsFile.EliminarArchivos(path, ".xlsx",nombreArchivo.substring(0, 16));
			
			Path file = Paths.get(path, nombreArchivo); 
			
			reportesService.getTableroByParamsExcel(dateDesdeParse, dateHastaParse, nombreArchivo, ticket, canal, tienda);

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
	
	
	@RequestMapping("/listarTablero/procesarDetalleXlsx")
	@ResponseBody
	public String procesarDetalleExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("ticket") String ticket
			, @RequestParam("canal") String canal
			, @RequestParam("tienda") String tienda
			, HttpServletRequest request
			, HttpServletResponse response) {
		
		String idEjecucion = null;
		try {
			multipleModalResponse = new MultipleModalResponse(true,"", null);
//			String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		
			validarDatosMultiple(dateDesde, dateHasta, "0");
			
			HttpSession session = request.getSession();
			VMLogin responseLogin = (VMLogin) session.getAttribute("usuario");
			
			StringBuilder parametros = new StringBuilder();
			parametros.append("Fecha Inicio: ").append(dateDesde);
			parametros.append(", Fecha Fin: ").append(dateHasta);
			if(Strings.isNotBlank(ticket)) {
				parametros.append(", Ticket: ").append(ticket);
			}
			if(Strings.isNotBlank(canal)) {
				parametros.append(", Canal: ").append(canal);	
			}
			if(Strings.isNotBlank(tienda)) {
				parametros.append(", Tienda: ").append(tienda);	
			}
			
			
			idEjecucion = descargaService.registrarProceso(parametros.toString(), "Reportes Detalle", responseLogin.getUser().getNombre() + " " + responseLogin.getUser().getApellidoP());
			
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			
			
			// Se ejecuta hilo para el proceso en segundo plano
			reportesService.ejecutarProcesamientoEnSegundoPlano(idEjecucion, dateDesdeParse, dateHastaParse, ticket, canal, tienda);
			
			
			response.setStatus(HttpServletResponse.SC_OK);
			
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return idEjecucion;
	}
	
	
	void validarDatosMultiple (String dateDesde, String dateHasta, String pageNumber) {

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
