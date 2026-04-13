package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
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

import com.sodimac.rebates.model.CatEstadoOrdenCompraEntity;
import com.sodimac.rebates.model.CatTiendaEntity;
import com.sodimac.rebates.model.CatTipoOrdenCompraEntity;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.RebateOrdenCompraFillEntity;
import com.sodimac.rebates.model.ReporteOrdenCompraFill;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.IEstadoOrdenCompraService;
import com.sodimac.rebates.service.IProgramaPagoService;
import com.sodimac.rebates.service.IRebateOrdenCompraFillService;
import com.sodimac.rebates.service.ITiendaService;
import com.sodimac.rebates.service.ITipoOrdenCompraService;
import com.sodimac.rebates.util.ExportOrdenCompraFillExcel;

@Controller
@RequestMapping("/ordenCompraFill")
public class RebateOrdenCompraFillController extends BaseController {

	private static int DIFERENCIA_MESES = 6;
	
	@Autowired
	private IProgramaPagoService serviceProgramaPago;
	
	@Autowired
	private ITipoOrdenCompraService tipoOrdenCompraService;
	
	@Autowired
	private IRebateOrdenCompraFillService ordenCompraFillService;
	
	@Autowired
	private IEstadoOrdenCompraService estadoOrdenCompraService;
	
	@Autowired
	private ITiendaService tiendaService;
	
	
	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);

		List<ProgramaPago> tipos = serviceProgramaPago.getActive();
		List<CatTipoOrdenCompraEntity> tiposOrdenCompra = this.tipoOrdenCompraService.getTiposOrdenCompra();
		List<CatEstadoOrdenCompraEntity> estadosOrdenCompra = this.estadoOrdenCompraService.getEstadosOrdenCompra();
		List<CatTiendaEntity> tiendas = this.tiendaService.getTiendas();

		Date date_1 = new Date();
		Date date_2 = new Date();
		Calendar calendar_1 = Calendar.getInstance();
		Calendar calendar_2 = Calendar.getInstance();
		calendar_1.setTime(date_1);
		calendar_2.setTime(date_2);
		calendar_1.add(Calendar.DATE, -30);
		date_1 = calendar_1.getTime();
		date_2 = calendar_2.getTime();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicio = dateFormat.format(date_1);
		String fechaFinal = dateFormat.format(date_2);

		model.addAttribute("tiposPeriodo", tipos);
		model.addAttribute("tiposOrdenCompra", tiposOrdenCompra);
		model.addAttribute("estadosOrdenCompra", estadosOrdenCompra);
		model.addAttribute("tiendas", tiendas);
		model.addAttribute("fechaIni", fechaInicio);
		model.addAttribute("fechaFin", fechaFinal);

		return "ordenCompraFill";
	}
	
	@PostMapping("/consult")
	public String consult(@RequestBody ReporteOrdenCompraFill ordenCompraFill
			 			, BindingResult result
			 			, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		List<RebateOrdenCompraFillEntity> ordenesCompra = null;
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/ordenCompraFill :: start";
		}

		if (ordenCompraFill.getFechaFin().before(ordenCompraFill.getFechaIni())) {

			// TODO: cambiar dinámicamente / añadir a bitacora

			model.addAttribute("titulo", "Validación de Fecha");
			model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/ordenCompraFill :: start";
		}
		
		Calendar inicio = Calendar.getInstance();
        Calendar fin = Calendar.getInstance();
        inicio.setTime(ordenCompraFill.getFechaIni());
        fin.setTime(ordenCompraFill.getFechaFin());
		int diffA = fin.get(Calendar.YEAR) - inicio.get(Calendar.YEAR);
		int diffM = diffA * 12 + fin.get(Calendar.MONTH) - inicio.get(Calendar.MONTH);
		
		if (diffM > DIFERENCIA_MESES) {
			model.addAttribute("titulo", "Validación de Fecha");
			model.addAttribute("msg", "El periodo de fechas no debe ser mayor a " + DIFERENCIA_MESES + " seses, favor de validar");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/ordenCompraFill :: start";
		}
		
		ordenesCompra = this.ordenCompraFillService.getOrdenCompraFill(ordenCompraFill);
		if (ordenesCompra.size() >= 1) {
			sesion.setReporteOrdenCompraFill(ordenCompraFill);
			model.addAttribute("lista", ordenesCompra);
			model.addAttribute("ordenCompraFilter", ordenCompraFill);
			return "fragments/ordenCompraFill :: table";
		}
		return "fragments/ordenCompraFill :: noRecord";
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

		String name_report_user = nombreMes + anio.toString() + " OrdenCompraFill " + strDateComplete + ".xlsx";
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + name_report_user);

		ReporteOrdenCompraFill ordenCompraFill = sesion.getReporteOrdenCompraFill();
		ordenCompraFill.setRowsPerPage(-1);
		List<RebateOrdenCompraFillEntity> dataReport = this.ordenCompraFillService.getOrdenCompraFill(ordenCompraFill);
		ByteArrayInputStream stream = ExportOrdenCompraFillExcel.ordenCompraFillListToExcelFile(dataReport);
		IOUtils.copy(stream, response.getOutputStream());
	}
	
}
