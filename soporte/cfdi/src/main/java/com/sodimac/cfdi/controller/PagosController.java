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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.models.PagosModel;
import com.sodimac.cfdi.service.CatConfiguracionService;
import com.sodimac.cfdi.service.PagosService;
import com.sodimac.cfdi.util.UtilsFile;

@Controller
@RequestMapping("/pagos")
public class PagosController extends BaseController {

	Logger logger = LoggerFactory.getLogger(PagosController.class);
	
	@Autowired
	private PagosService pagosService;
	
	@Autowired
	private CatConfiguracionService catConfiguracionService;

	@Value("${cfdiVersion}")
	private String version;
	
	MultipleModalResponse multipleModalResponse;

	@GetMapping("/index")
	public String index(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/pagos/index");
		return "/pagos";
		
//		HttpSession session = request.getSession();
//		
//		if(session == null || session.getAttribute("usuario") == null) {
//			return "redirect:/index";
//		} else {
//			if (getModelAttributes(model, request, "", "/pagos/index")) {
//				return "/pagos";
//			} else {
//				return "redirect:/inicio";
//			}
//		}
	}
	
	
	@GetMapping("/listarPagos")
	public String listarPagos(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("tipopago") String tipopago
			, @RequestParam("pmonto") String pmonto
			, Model theModel) {

		List<PagosModel> datos = new ArrayList <PagosModel>();
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
			Double monto = 0.0d;
			if (pmonto != null && !pmonto.isEmpty()) {
				monto = Double.valueOf(pmonto);
			}
			
			datos = pagosService.getPagosByParams(dateDesdeParse, dateHastaParse, start, rowsPerPage, tipopago, monto);
			
			theModel.addAttribute("listPagos", datos);
			if (datos.size() <= 0) {
				return "fragments/pagoList :: noResults";
			} else {
				return "fragments/pagoList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarPagos ", e);
		}
		return "";
				
	}
	
	@PostMapping("/cambiarEstatusPago")
	@ResponseBody
	public String cambiarEstatusPago(@RequestParam(value = "idPago", required=true) int idPago, @RequestParam(value = "estatusPago", required=true) String estatusPago, Model theModel) {
		String result = "";
		try {
			result = pagosService.cambiarEstatusPago(idPago, estatusPago);	
		} catch (Exception e) {
			result = "Ocurrio un problema al intentar liberar el pago.";
		}
		return result;
	}
	
	@PostMapping("/cambiarFolioCliente")
	@ResponseBody
	public String cambiarFolioCliente(@RequestParam(value = "idPago", required=true) int idPago, @RequestParam(value = "folioCliente", required=true) String folioCliente, Model theModel) {
		String result = "";
		try {
			result = pagosService.cambiarFolioCliente(idPago, folioCliente);	
		} catch (Exception e) {
			result = "Ocurrio un problema al cambiar el numero de cuenta";
		}
		return result;
	}
	
	@PostMapping("/obtenerStatusPagos")
	@ResponseBody
	public String listaTipoComprobante(@RequestParam(value = "tipoPago", required=true) String tipoPago) {
		return pagosService.getStatusPagos(tipoPago);
	}
	
	
	@RequestMapping("/listarPagos/descargaXlsx")
	public void	descargaExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("tipopago") String tipopago
			, @RequestParam("pmonto") String pmonto
			, Model theModel
			, HttpServletResponse response) throws Exception{

			multipleModalResponse = new MultipleModalResponse(true,"", null);
			String path = catConfiguracionService.findParameterByKey("Mail.PathFile"); 
		
			validarDatosMultiple(dateDesde, dateHasta, "0");
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			
			SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			Date Ahora = new Date();
			
			String nombreArchivo = "Reporte_Pagos_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
			nombreArchivo = nombreArchivo.replaceAll("/","");
			nombreArchivo = nombreArchivo.replaceAll(":","");
			
			
			UtilsFile.EliminarArchivos(path, ".xlsx",nombreArchivo.substring(0, 16));
			
			Path file = Paths.get(path, nombreArchivo); 
			
			Double monto = 0.0d;
			if (pmonto != null && !pmonto.isEmpty()) {
				monto = Double.valueOf(pmonto);
			}
			pagosService.getPagosExcelFechas(dateDesdeParse, dateHastaParse, nombreArchivo, tipopago, monto);

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
