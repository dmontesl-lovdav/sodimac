package com.sodimac.cfdi.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.cfdi.dto.vista.DtoFactura;
import com.sodimac.cfdi.dto.vista.DtoFolioFactura;

import com.sodimac.cfdi.models.FacturaRelacionadaModel;
import com.sodimac.cfdi.models.FolioFacturaModel;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.service.ClientesService;
import com.sodimac.cfdi.service.FolioFacturaService;

import com.sodimac.cfdi.service.SeguridadService;

@Controller
@RequestMapping("/folios")
public class FoliosController extends BaseController {
	
	private Logger logger = LoggerFactory.getLogger(FoliosController.class);
	private DecimalFormat df = new DecimalFormat("000000");

	private MultipleModalResponse multipleModalResponse;
	
	@Value("${cfdiVersion}")
	private String version;
	
	@Autowired
	private FolioFacturaService folioFacturaService;
	
	@Autowired
	private ClientesService clientesService;
	
	@Autowired
	private SeguridadService seguridadService;
	
	
	@GetMapping("/index")
	public String consultarFacturas(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/folios/index");
		return "/folios";
		
//		HttpSession session = request.getSession();
//		
//		if(session == null || session.getAttribute("usuario") == null) {
//			return "redirect:/index";
//		} else {
//			if (getModelAttributes(model, request, "", "/folios/index")) {
//				return "/folios";
//			} else {
//				return "redirect:/inicio";
//			}
//			
//		}
	}
	
	@GetMapping("/listarFoliosFacturas")
	public String listarFolios(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("folio") String folio
			, Model theModel) {

		// LOGS DE EVIDENCIA - INICIO
		logger.info("[FOLIOS-BACKEND] ========== LISTAR FOLIOS FACTURAS ==========");
		logger.info("[FOLIOS-BACKEND] Parámetros recibidos:");
		logger.info("[FOLIOS-BACKEND] - dateDesde: {}", dateDesde);
		logger.info("[FOLIOS-BACKEND] - dateHasta: {}", dateHasta);
		logger.info("[FOLIOS-BACKEND] - rfc: {}", rfc);
		logger.info("[FOLIOS-BACKEND] - pageNumber: {}", pageNumber);
		logger.info("[FOLIOS-BACKEND] - start: {}", start);
		logger.info("[FOLIOS-BACKEND] - rowsPerPage: {}", rowsPerPage);
		logger.info("[FOLIOS-BACKEND] - folio: {}", folio);
		// LOGS DE EVIDENCIA - FIN

		this.multipleModalResponse = new MultipleModalResponse(true,"", null);

		try {
			validarDatosMultiple(rfc, dateDesde, dateHasta, pageNumber);
			
			if (!this.multipleModalResponse.isSuccess()) {
				return "Datos invalidos";
			}

			start = (Integer.parseInt(pageNumber) * rowsPerPage) - rowsPerPage;
			if (start < 0) {
				start = 0;
			}
			
			Integer idFolio = null;
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			String rfcEncriptado = null;
			if (!rfc.isEmpty()) {
				rfcEncriptado = seguridadService.encriptar(rfc);
			}
			
			if (folio != null && !folio.isEmpty() ) {
				idFolio = Integer.valueOf(folio);
			}

			// LOGS DE EVIDENCIA - INICIO CONSULTA BD
			long startTime = System.currentTimeMillis();
			logger.info("[FOLIOS-BD] ========== INICIANDO CONSULTA A BASE DE DATOS ==========");
			logger.info("[FOLIOS-BD] Timestamp inicio: {}", startTime);
			// LOGS DE EVIDENCIA - FIN

			List<FolioFacturaModel> listFolios = folioFacturaService.getFoliosFacturaPPDByParams(dateDesdeParse, dateHastaParse, idFolio, rfcEncriptado, start, rowsPerPage);

			// LOGS DE EVIDENCIA - INICIO RESULTADO BD
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			logger.info("[FOLIOS-BD] ========== CONSULTA A BASE DE DATOS COMPLETADA ==========");
			logger.info("[FOLIOS-BD] Timestamp fin: {}", endTime);
			logger.info("[FOLIOS-BD] Tiempo transcurrido: {} ms ({} segundos)", duration, duration / 1000.0);
			logger.info("[FOLIOS-BD] Registros obtenidos: {}", listFolios != null ? listFolios.size() : 0);
			logger.info("[FOLIOS-BD] Tamaño de la lista: {} registros", listFolios != null ? listFolios.size() : "NULL");
			// LOGS DE EVIDENCIA - FIN

			theModel.addAttribute("listFoliosFactura", listFolios);

			// LOGS DE EVIDENCIA - INICIO
			logger.info("[FOLIOS-BACKEND] Consulta ejecutada:");
			logger.info("[FOLIOS-BACKEND] - Fecha desde parseada: {}", dateDesdeParse);
			logger.info("[FOLIOS-BACKEND] - Fecha hasta parseada: {}", dateHastaParse);
			logger.info("[FOLIOS-BACKEND] - RFC encriptado: {}", rfcEncriptado);
			logger.info("[FOLIOS-BACKEND] - Folio ID: {}", idFolio);
			logger.info("[FOLIOS-BACKEND] - Start: {}", start);
			logger.info("[FOLIOS-BACKEND] - RowsPerPage: {}", rowsPerPage);
			logger.info("[FOLIOS-BACKEND] - Registros encontrados: {}", listFolios.size());
			// LOGS DE EVIDENCIA - FIN

			if (listFolios.size() <= 0) {
				logger.info("[FOLIOS-BACKEND] Sin resultados - retornando fragment noResults");
				return "fragments/foliosList :: noResults";
			} else {
				logger.info("[FOLIOS-BACKEND] Con resultados - retornando fragment fragmentTable");
				return "fragments/foliosList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listFoliosFactura ", e);
		}
		return "";
	}
	
	@ResponseBody
	@PostMapping(value="/generarFolio")
	public String generarFolio(@RequestBody DtoFolioFactura folioFactura,
			Model theModel, HttpServletRequest request) {
		
		this.multipleModalResponse = new MultipleModalResponse(true,"", null);
		Integer folioFacturaMax = null;
		String folioFacturaResp = null;
		try {
			
			if (folioFactura.getListFacturas() != null) {
				folioFacturaMax = this.folioFacturaService.getMaxFolioFactura();
				if (folioFacturaMax == null) {
					folioFacturaMax = 1;
				}
				folioFacturaResp = df.format(folioFacturaMax);
				folioFacturaService.registrarFolioFactura( folioFactura.getListFacturas(), folioFacturaMax);
			}
			return folioFacturaResp;
		} catch (Exception e) {
			logger.error("listFoliosFactura ", e);
		}
		return "";
	}
	
	@ResponseBody
	@PostMapping(value="/desvincularFolio")
	public String desvincularFolio(@RequestBody DtoFactura dtoFactura,
			Model theModel, HttpServletRequest request) {
		
		this.multipleModalResponse = new MultipleModalResponse(true,"", null);
		try {
			this.folioFacturaService.desvincularFolio(dtoFactura.getIdFactura());
			return "OK";
		} catch (Exception e) {
			logger.error("listFoliosFactura ", e);
		}
		return "";
	}
	
	@RequestMapping("/listarFoliosFactura/descargaXlsx")
	public void	descargaExcel(	
			@RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam("rfc") String rfc
			, @RequestParam("folio") String folio
			, Model theModel,
			HttpServletResponse response) throws Exception{

		multipleModalResponse = new MultipleModalResponse(true,"", null);
		
		validarDatosMultiple(rfc, dateDesde, dateHasta, "0");
		
		Integer idFolio = null;
		String[] partsDesde = dateDesde.split("/");
		String[] partsHasta = dateHasta.split("/");
		String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
		String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
		String rfcEncriptado = null;
		if (!rfc.isEmpty()) {
			logger.info("[FOLIOS-RFC] RFC normal (sin encriptar): {}", rfc);
			rfcEncriptado = seguridadService.encriptar(rfc);
			logger.info("[FOLIOS-RFC] RFC encriptado: {}", rfcEncriptado);
		} else {
			logger.info("[FOLIOS-RFC] RFC vacío - no se encripta");
		}

		if (folio != null && !folio.isEmpty() ) {
			idFolio = Integer.valueOf(folio);
		}
		
		SimpleDateFormat MI_FORMATO = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		Date Ahora = new Date();
		
		String nombreArchivo = "Reporte_Folios_" + MI_FORMATO.format(Ahora).replaceAll(" ","") + ".xlsx";
		nombreArchivo = nombreArchivo.replaceAll("/","");
		nombreArchivo = nombreArchivo.replaceAll(":","");
		
		byte[] byteArray = this.folioFacturaService.createExcel(dateDesdeParse, dateHastaParse, idFolio, rfcEncriptado);
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
	
	@GetMapping("/listarFacturasRelacionadas")
	@ResponseBody
	public List<FacturaRelacionadaModel> listarFacturasRelacionadas(
			  @RequestParam("uuidRelacionado") String uuidRelacionado) {

		List<FacturaRelacionadaModel> listFacturasRelacionadas = this.folioFacturaService.getMontosUuiRelacionados(uuidRelacionado);
		return listFacturasRelacionadas;
	}
	
	private void validarDatosMultiple (String rfc, String dateDesde, String dateHasta, String pageNumber) {
		// LOGS DE EVIDENCIA - INICIO
		logger.info("[FOLIOS-VALIDACION] Validando datos de entrada:");
		// LOGS DE EVIDENCIA - FIN

		if (dateDesde=="" || dateHasta=="" || pageNumber=="") {
			// LOGS DE EVIDENCIA - INICIO
			logger.warn("[FOLIOS-VALIDACION] Error: Campos vacíos - dateDesde: '{}', dateHasta: '{}', pageNumber: '{}'",
					dateDesde, dateHasta, pageNumber);
			// LOGS DE EVIDENCIA - FIN
			multipleModalResponse.setMessage("Datos invalidos - Campos requeridos vacíos");
			multipleModalResponse.setSuccess(false);
		}

		if (dateDesde.length() != 10) {
			// LOGS DE EVIDENCIA - INICIO
			logger.warn("[FOLIOS-VALIDACION] Error: Formato de fecha 'desde' inválido - longitud: {}, valor: '{}'",
					dateDesde.length(), dateDesde);
			// LOGS DE EVIDENCIA - FIN
			multipleModalResponse.setMessage("Datos invalidos - Formato de fecha desde incorrecto");
			multipleModalResponse.setSuccess(false);
		}

		if (dateHasta.length() != 10) {
			// LOGS DE EVIDENCIA - INICIO
			logger.warn("[FOLIOS-VALIDACION] Error: Formato de fecha 'hasta' inválido - longitud: {}, valor: '{}'",
					dateHasta.length(), dateHasta);
			// LOGS DE EVIDENCIA - FIN
			multipleModalResponse.setMessage("Datos invalidos - Formato de fecha hasta incorrecto");
			multipleModalResponse.setSuccess(false);
		}

		if (!rfc.isEmpty() && ! this.clientesService.validarRfcExpresionRegular(rfc)) {
			// LOGS DE EVIDENCIA - INICIO
			logger.warn("[FOLIOS-VALIDACION] Error: RFC con formato inválido: '{}'", rfc);
			// LOGS DE EVIDENCIA - FIN
			multipleModalResponse.setMessage("Datos invalidos - RFC con formato incorrecto");
			multipleModalResponse.setSuccess(false);
		}

		// LOGS DE EVIDENCIA - INICIO
		if (multipleModalResponse.isSuccess()) {
			logger.info("[FOLIOS-VALIDACION] Validación exitosa - Todos los datos son correctos");
		} else {
			logger.error("[FOLIOS-VALIDACION] Validación fallida - Mensaje: {}", multipleModalResponse.getMessage());
		}
		// LOGS DE EVIDENCIA - FIN
	}

}
