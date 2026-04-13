package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import com.sodimac.rebates.dto.CalculoRebateMSIDto;
import com.sodimac.rebates.model.CalculoRebateMSI;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.ICalculoRebateMSI3Service;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.util.ExportCalculoRebateMSI3Excel;

@Controller
@RequestMapping("/rebatesMSI3")
public class CalculoRebateMSI3Controller extends BaseController {

	private static int DIFERENCIA_MESES = 6;
	
	@Autowired
	private ICalculoRebateMSI3Service iCalculoRebateMSI3Service;
	
	@Autowired
	private IPeriodoService servicePeriodo;
	
	
	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);

		List<Periodo> catPeriodoConsult = this.servicePeriodo.getActive();

		String fechaInicio = null;
		String fechaFinal = null;

		model.addAttribute("periodosConsult", catPeriodoConsult);
		model.addAttribute("fechaIni", fechaInicio);
		model.addAttribute("fechaFin", fechaFinal);
		return "rebatesMSI3";
	}
	
	@PostMapping("/consult")
	public String consult(@RequestBody CalculoRebateMSI calculoRebateMSI
			 			, BindingResult result
			 			, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		List<CalculoRebateMSIDto> listCalculoRebates = null;
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/rebatesMSI3 :: start";
		}

		if (calculoRebateMSI.getFechaIni() != null && calculoRebateMSI.getFechaFin() != null) {
			if (calculoRebateMSI.getFechaFin().before(calculoRebateMSI.getFechaIni())) {
	
				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);
	
				return "fragments/rebatesMSI3 :: start";
			}
			
			Calendar inicio = Calendar.getInstance();
	        Calendar fin = Calendar.getInstance();
	        inicio.setTime(calculoRebateMSI.getFechaIni());
	        fin.setTime(calculoRebateMSI.getFechaFin());
			int diffA = fin.get(Calendar.YEAR) - inicio.get(Calendar.YEAR);
			int diffM = diffA * 12 + fin.get(Calendar.MONTH) - inicio.get(Calendar.MONTH);
			
			if (diffM > DIFERENCIA_MESES) {
				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "El periodo de fechas no debe ser mayor a " + DIFERENCIA_MESES + " seses, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);

				return "fragments/rebatesMSI3 :: start";
			}
		}
		
		try {
			listCalculoRebates = this.iCalculoRebateMSI3Service.getCalculoRebateMSIView(calculoRebateMSI);
			if (listCalculoRebates.size() >= 1) {
				sesion.setCalculoRebateMSI(calculoRebateMSI);
				model.addAttribute("lista", listCalculoRebates);
				model.addAttribute("rebatesMSI3Filter", calculoRebateMSI);
				return "fragments/rebatesMSI3 :: table";
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "fragments/rebatesMSI3 :: noRecord";
	}
	
	@GetMapping("/report")
	public void exportReportUser(HttpServletResponse response)
			throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		// Obtienes el mes actual
		Month mes = LocalDate.now().getMonth();
		// Obtienes el nombre del mes
		String nombreMes = mes.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
		// Convierte a mayúscula la primera letra del nombre.
		String primeraLetra = nombreMes.substring(0, 1);
		String mayuscula = primeraLetra.toUpperCase();
		String demasLetras = nombreMes.substring(1, nombreMes.length());
		nombreMes = mayuscula + demasLetras;
		// Obtener año actual
		Integer anio = LocalDate.now().getYear();

		Date date = new Date();
		SimpleDateFormat formatterDateComplete = new SimpleDateFormat("yyyyMMddHHmm");
		String strDateComplete = formatterDateComplete.format(date);

		String name_report_user = nombreMes + anio.toString() + " rebatesMSI3 " + strDateComplete + ".xlsx";
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + name_report_user);

		CalculoRebateMSI calculoRebateMSI = sesion.getCalculoRebateMSI();
		calculoRebateMSI.setRowsPerPage(-1);
		
		try {
			List<CalculoRebateMSIDto> listCalculoRebates = this.iCalculoRebateMSI3Service.getCalculoRebateMSIView(calculoRebateMSI);
			ByteArrayInputStream stream = ExportCalculoRebateMSI3Excel.calculoRebateMSIListToExcelFile(listCalculoRebates);
			IOUtils.copy(stream, response.getOutputStream());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
