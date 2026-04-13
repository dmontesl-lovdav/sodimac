package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;
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
import com.sodimac.rebates.dto.PolizaContableDto;
import com.sodimac.rebates.dto.PolizaContableReporteDto;
import com.sodimac.rebates.filter.PolizaContableFilter;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IPolizaContableService;
import com.sodimac.rebates.service.ITipoRebateService;
import com.sodimac.rebates.util.ExportPolizaContableExcel;

@Controller
@RequestMapping("/polizas")
public class PolizasController extends BaseController {

	@Autowired
	private IPolizaContableService service;

	@Autowired
	private IPeriodoService servicePeriodo;

	@Autowired
	private ITipoRebateService serviceTipoRebate;

	@GetMapping("/index")
	public String index(Model model) throws ParseException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);

		List<Periodo> catPeriodoConsult = this.servicePeriodo.getPeriodosTerminados();
		List<CatTipoRebateDto> catRebates = this.serviceTipoRebate.getActive();

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

		model.addAttribute("listPeriodos", catPeriodoConsult);
		model.addAttribute("listRebates", catRebates);
		model.addAttribute("fechaInicio", fechaInicioMax);
		model.addAttribute("fechaFinal", fechaFinalMax);
		model.addAttribute("lista", null);

		return "polizas";
	}

	@PostMapping("/consult")
	public String consult(@RequestBody PolizaContableFilter request, BindingResult result, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		List<PolizaContableDto> list = new ArrayList<>();
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {
				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);
			return "fragments/polizas :: start";
		}

		if (Strings.isBlank(request.getIdProveedor()) && request.getIdPeriodo() == null && request.getTipoRebate() == null 
				&& request.getFechaCargaIni() == null && request.getFechaCargaFin() == null) {
			model.addAttribute("titulo", "Validación de Datos");
			model.addAttribute("msg", "Debe seleccionar al menos un parámetro para la búsqueda");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);
			return "fragments/polizas :: start";
		} 

		Date fechaInicio = this.getTruncateDate( request.getFechaCargaIni() );
		Date fechaFin = this.getTruncateDate( request.getFechaCargaFin() );
		
		request.setFechaCargaIni(fechaInicio);
		request.setFechaCargaFin(fechaFin);
		
		if (request.getFechaCargaIni() != null && 
			request.getFechaCargaFin() != null && 
			request.getFechaCargaFin().before(request.getFechaCargaIni())) {
			
			model.addAttribute("titulo", "Validación de Fecha");
			model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/polizas :: start";
		}
		list = this.service.getPolizasContables(request);
		sesion.setPolizaContableFilter(request);
		if (list.size() > 0) {
			Map<String, Double> rebates = new HashMap<String, Double>();
			list.stream().forEach(item -> {
				String tipoRebate = item.getTipoRebate() == null ? "No identificado" : item.getTipoRebate();
				if (rebates.containsKey(tipoRebate)) {
					rebates.put(tipoRebate,
							Double.sum(rebates.get(tipoRebate), (item.getMontoCalculado() == null ? 0 : item.getMontoCalculado())));
				} else {
					rebates.put(tipoRebate, (item.getMontoCalculado() == null ? 0 : item.getMontoCalculado()));
				}
			});
			
			for(String key : rebates.keySet()) {
				rebates.put(key, rebates.get(key)/1000);
			}

			model.addAttribute("lista", list);
			model.addAttribute("rebates", rebates);
//				sesion.setPolizaContable(request);
			return "fragments/polizas :: table";
		} else {
			return "fragments/polizas :: noRecord";
		}
	}

	@GetMapping("/report")
	public void download(HttpServletResponse response) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		String name_report = "Detalle Poliza Contable.xlsx";
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + name_report);
		
		PolizaContableFilter filter = sesion.getPolizaContableFilter();
		List<PolizaContableReporteDto> dataReport = service.getReportePolizasContables(filter); 

		ByteArrayInputStream stream = ExportPolizaContableExcel.polizaContableListToExcelFile(dataReport);
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
