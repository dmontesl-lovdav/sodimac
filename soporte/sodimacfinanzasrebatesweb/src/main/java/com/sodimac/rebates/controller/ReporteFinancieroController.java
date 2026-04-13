package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.sodimac.rebates.dto.ReporteFinancieroDto;
import com.sodimac.rebates.enums.EEstatusPeriodo;
import com.sodimac.rebates.filter.ReporteFinancieroFilter;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IProgramaPagoService;
import com.sodimac.rebates.service.IReporteFinancieroService;
import com.sodimac.rebates.service.ITipoRebateService;
import com.sodimac.rebates.util.ExportReporteFinancieroExcel;

@Controller
@RequestMapping("/reporteFinanciero")
public class ReporteFinancieroController extends BaseController {
	
	private static int DIFERENCIA_MESES = 6;
	
	@Autowired
	private IProgramaPagoService serviceProgramaPago;

	@Autowired
	private ITipoRebateService serviceTipoRebate;
	
	@Autowired
	private IReporteFinancieroService reporteFinancieroService;
	
	@Autowired
	private IPeriodoService periodoService;
	
	@GetMapping("/index")
	public String index(Model model) throws ParseException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);

		List<ProgramaPago> tipos = serviceProgramaPago.getActive();
		List<CatTipoRebateDto> catRebates = serviceTipoRebate.getActive();
		
		List<EEstatusPeriodo> listPeriodos = new ArrayList<>();
		listPeriodos.add(EEstatusPeriodo.TERMINO_CALCULO);
		listPeriodos.add(EEstatusPeriodo.SOLICITUD_CONTABILIDAD);
		listPeriodos.add(EEstatusPeriodo.PROCESO_CONTABILIDAD);
		listPeriodos.add(EEstatusPeriodo.CONTABILIZADO);
		
		List<Periodo> periodos = periodoService.getPeriodosSinTodos(listPeriodos);
		
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

		return "reporteFinanciero";
	}
	
	@PostMapping("/consult")
	public String consult(@RequestBody ReporteFinancieroFilter request, BindingResult result, Model model) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		List<ReporteFinancieroDto> reporteFinanciero = null;
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/reporteFinanciero :: start";
		}
		
		Date fechaInicio = this.getTruncateDate( request.getFechaIni() );
		Date fechaFin = this.getTruncateDate( request.getFechaFin() );
		
		request.setFechaIni(fechaInicio);
		request.setFechaFin(fechaFin);
		
		if (request.getFechaIni() != null && request.getFechaFin() != null) {
			if (request.getFechaFin().before(request.getFechaIni())) {
	
				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);
	
				return "fragments/reporteFinanciero :: start";
			}
			
			Calendar inicio = Calendar.getInstance();
	        Calendar fin = Calendar.getInstance();
	        inicio.setTime(request.getFechaIni());
	        fin.setTime(request.getFechaFin());
			int diffA = fin.get(Calendar.YEAR) - inicio.get(Calendar.YEAR);
			int diffM = diffA * 12 + fin.get(Calendar.MONTH) - inicio.get(Calendar.MONTH);
			
			if (diffM > DIFERENCIA_MESES) {
				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "El periodo de fechas no debe ser mayor a " + DIFERENCIA_MESES + " seses, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);

				return "fragments/reporteFinanciero :: start";
			}
		}
		
		reporteFinanciero = this.reporteFinancieroService.getReporteFinanciero(request);
		if (reporteFinanciero.size() >= 1) {
			sesion.setReporteFinancieroFilter(request);
			model.addAttribute("lista", reporteFinanciero);
			model.addAttribute("reporteFinancieroFilter", request);
			return "fragments/reporteFinanciero :: table";
		}
		return "fragments/reporteFinanciero :: noRecord";
	}
	
	@GetMapping("/report")
	public void exportReportUser(HttpServletResponse response)
			throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		String name_report_user = "ReporteFinanciero.xlsx";
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + name_report_user);

		ReporteFinancieroFilter reporteFinancieroFilter = sesion.getReporteFinancieroFilter();
		reporteFinancieroFilter.setRowsPerPage(-1);
		List<ReporteFinancieroDto> dataReport =  this.reporteFinancieroService.getReporteFinanciero(reporteFinancieroFilter);
		ByteArrayInputStream stream = ExportReporteFinancieroExcel.reporteFinancieroListToExcelFile(dataReport);
		IOUtils.copy(stream, response.getOutputStream());
		
	}
	
	private Date getTruncateDate(Date date) {
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime(); 
		}
		return date;
	}
}
