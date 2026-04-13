package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.filter.UsuarioFillRateFilter;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.RebateUsuarioFillRateEntity;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IProgramaPagoService;
import com.sodimac.rebates.service.IRebateUsuarioFillRateService;
import com.sodimac.rebates.service.ITipoRebateService;
import com.sodimac.rebates.util.ExportUsuarioFillRateExcel;

@Controller
@RequestMapping("/usuarioFillRate")
public class RebateUsuarioFillRateController extends BaseController {

	@Autowired
	private IProgramaPagoService serviceProgramaPago;

	@Autowired
	private ITipoRebateService serviceTipoRebate;

	@Autowired
	private IRebateUsuarioFillRateService rebateUsuarioFillRateService;

	@Autowired
	private IPeriodoService periodoService;

	private String mensajeDescarga = null;
	private int countIndex = 0;
	
	@GetMapping("/index")
	public String index(Model model) throws ParseException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);

		List<ProgramaPago> tipos = serviceProgramaPago.getActive();
		List<CatTipoRebateDto> catRebates = serviceTipoRebate.getActive();
		List<Periodo> periodos = periodoService.getPeriodosTerminadosAndContabilizados();

		Date date_1 = new Date();
		Date date_2 = new Date();
		Calendar calendar_1 = Calendar.getInstance();
		Calendar calendar_2 = Calendar.getInstance();
		calendar_1.setTime(date_1);
		calendar_2.setTime(date_2);
		calendar_1.add(Calendar.DATE, -1);
		date_1 = calendar_1.getTime();
		date_2 = calendar_2.getTime();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicioMax = dateFormat.format(date_1);
		String fechaFinalMax = dateFormat.format(date_2);

		model.addAttribute("tiposPeriodo", tipos);
		model.addAttribute("rebates", catRebates);
		model.addAttribute("periodos", periodos);
		model.addAttribute("fechaInicio", fechaInicioMax);
		model.addAttribute("fechaFinal", fechaFinalMax);

		if(mensajeDescarga!=null && countIndex == 0) {
			model.addAttribute("message", mensajeDescarga);
			countIndex++;
		}
		
		return "usuarioFillRate";
	}

	@PostMapping("/consult")
	public String consult(@RequestBody UsuarioFillRateFilter request, BindingResult result, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		List<RebateUsuarioFillRateEntity> usuarioFillRate = null;
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/usuarioFillRate :: start";
		}
		
		if (request.getFechaFin() != null &&
			request.getFechaIni() != null && 
			request.getFechaFin().before(request.getFechaIni())) {

			model.addAttribute("titulo", "Validación de Fecha");
			model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/usuarioFillRate :: start";
		}

		usuarioFillRate = this.rebateUsuarioFillRateService.getUsuarioFillRate(request, sesion.getIdUser());
		if (usuarioFillRate.size() >= 1) {
			sesion.setUsuarioFillRateFilter(request);
			model.addAttribute("lista", usuarioFillRate);
			model.addAttribute("usuarioFillRateFilter", request);
			
			mensajeDescarga = null;
			countIndex = 0;
			
			
			return "fragments/usuarioFillRate :: table";
		}
		return "fragments/usuarioFillRate :: noRecord";
	}

	@GetMapping("/report")
	public void exportReportUser(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		UsuarioFillRateFilter filter = sesion.getUsuarioFillRateFilter();

		if (filter == null) {
			ServletContext servletContext = request.getServletContext();
			mensajeDescarga = "No fue posible generar el reporte, intenta con otra consulta.";
			countIndex = 0;
			response.sendRedirect(servletContext.getContextPath() + "/usuarioFillRate/index");
		} else {	
			String name_report_user = "ReporteUsuarioFillRate.xlsx";
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + name_report_user);
			
			filter.setRowsPerPage(-1);
			List<RebateUsuarioFillRateEntity> dataReport = this.rebateUsuarioFillRateService.getUsuarioFillRate(filter, sesion.getIdUser());
			ByteArrayInputStream stream = ExportUsuarioFillRateExcel.usuarioFillRateListToExcelFile(dataReport);
			IOUtils.copy(stream, response.getOutputStream());
		}
	}

}
