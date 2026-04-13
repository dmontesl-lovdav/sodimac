package com.sodimac.rebates.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.model.Autorizacion;
import com.sodimac.rebates.model.ComentarioComparativoAprobacion;
import com.sodimac.rebates.model.ComparativoAprobacion;
import com.sodimac.rebates.model.ComparativoDTO;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.IAutorizacionService;
import com.sodimac.rebates.service.IComentarioComparativoAprobacionService;
import com.sodimac.rebates.service.IComparativoAprobacionService;
import com.sodimac.rebates.service.IProgramaPagoService;
import com.sodimac.rebates.service.ITipoRebateService;

@Controller
@RequestMapping("/authorization")
public class AuthorizationController extends BaseController {

	@Autowired
	private IProgramaPagoService serviceProgramaPago;

	@Autowired
	private IAutorizacionService serviceAutorizacion;

	@Autowired
	private IComparativoAprobacionService serviceComparativoAprobacion;

	@Autowired
	private IComentarioComparativoAprobacionService serviceComentarioComparativoAprobacion;

	@Autowired
	private ITipoRebateService serviceTipoRebate;

	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);
		
		List<ProgramaPago> tipos = serviceProgramaPago.getActive();
		List<CatTipoRebateDto> catRebates = serviceTipoRebate.getActive();

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
		model.addAttribute("fechaInicio", fechaInicioMax);
		model.addAttribute("fechaFinal", fechaFinalMax);

		return "authorization";
	}

	@PostMapping("/consult")
	public String consult(@RequestBody Autorizacion autorizacion, BindingResult result, RedirectAttributes attributes,
			Model model) {

		List<Autorizacion> listAutorizacion = new LinkedList<Autorizacion>();
		String idPeriodo = "";
		String tipoPeriodo = "";
		String tipoRebate = "";

		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/authorization :: start";
		}

		// Validar que no sean nulos o vacíos idperiodo, descripcionPeriodo,
		// idCatProgramaPago, fechaInicio y fechaFina
		if (autorizacion.getDescripcionPeriodo() == "" && autorizacion.getIdperiodo() == null
				&& autorizacion.getFechaInicio() == null && autorizacion.getFechaFinal() == null
				&& autorizacion.getIdCatProgramaPago() == null && autorizacion.getTipodeRebate() == null) {

			model.addAttribute("titulo", "Validación de Filtros");
			model.addAttribute("msg", "Favor de seleccionar un filtro de búsqueda");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/authorization :: start";

		}

		if (autorizacion.getFechaInicio() == null && autorizacion.getFechaFinal() == null) {

			if (autorizacion.getIdCatProgramaPago() != null) {

				tipoPeriodo = autorizacion.getIdCatProgramaPago().toString();

			} else {

				tipoPeriodo = "%";
			}

			if (autorizacion.getIdperiodo() != null) {

				idPeriodo = autorizacion.getIdperiodo().toString();

			} else {

				idPeriodo = "%";
			}

			if (autorizacion.getTipodeRebate() != null) {

				tipoRebate = autorizacion.getTipodeRebate().toString();

			} else {

				tipoRebate = "%";
			}

			try {

				listAutorizacion = serviceAutorizacion.getAutorizacionWithOutDates(autorizacion.getDescripcionPeriodo(),
						tipoPeriodo, idPeriodo, tipoRebate);

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				model.addAttribute("titulo", "Error en Base de Datos");
				model.addAttribute("msg", "No se consiguió realizar la consulta");
				model.addAttribute("tipo", 3);
				model.addAttribute("code", false);
				model.addAttribute("lista", null);
				System.out.println(ex.getMessage());

				return "fragments/authorization :: noRecord";
			}

		} else {

			// Validación: Si se elige una Fecha Inicio debe seleccionar Fecha final
			if (autorizacion.getFechaInicio() != null && autorizacion.getFechaFinal() == null) {

				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "Debe seleccionar una fecha final, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);

				return "fragments/authorization :: start";

			} else if (autorizacion.getFechaFinal() != null && autorizacion.getFechaInicio() == null) {
				// Validación: Si se elige una Fecha Final debe seleccionar Fecha Inicio

				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "Debe seleccionar una fecha inicio, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);

				return "fragments/authorization :: start";

			} else {

				// Validación: La fecha Final no puede ser menor a Fecha inicio
				if (autorizacion.getFechaFinal().before(autorizacion.getFechaInicio())) {

					// TODO: cambiar dinámicamente / añadir a bitacora

					model.addAttribute("titulo", "Validación de Fecha");
					model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
					model.addAttribute("tipo", 2);
					model.addAttribute("code", false);

					return "fragments/authorization :: start";
				}

				if (autorizacion.getIdCatProgramaPago() != null) {

					tipoPeriodo = autorizacion.getIdCatProgramaPago().toString();

				} else {

					tipoPeriodo = "%";
				}

				if (autorizacion.getIdperiodo() != null) {

					idPeriodo = autorizacion.getIdperiodo().toString();

				} else {

					idPeriodo = "%";
				}

				if (autorizacion.getTipodeRebate() != null) {

					tipoRebate = autorizacion.getTipodeRebate().toString();

				} else {

					tipoRebate = "%";
				}

				// TODO: Qery con fechas
				try {

					listAutorizacion = serviceAutorizacion.getAutorizacionWithDates(autorizacion.getFechaInicio(),
							autorizacion.getFechaFinal(), autorizacion.getDescripcionPeriodo(), tipoPeriodo, idPeriodo,
							tipoRebate);

				} catch (Exception ex) {

					// TODO: cambiar dinámicamente / añadir a bitacora
					model.addAttribute("titulo", "Error en Base de Datos");
					model.addAttribute("msg", "No se consiguió realizar la consulta");
					model.addAttribute("tipo", 3);
					model.addAttribute("code", false);
					model.addAttribute("lista", null);
					System.out.println(ex.getMessage());

					return "fragments/authorization :: noRecord";
				}

			}

		}

		if (listAutorizacion.size() >= 1) {

			BigDecimal importe = new BigDecimal(0);
			BigDecimal importeAnterior = new BigDecimal(0);

			for (Autorizacion a : listAutorizacion) {

				importe = importe.add(a.getImporte());

				importeAnterior = importeAnterior.add(a.getImporteAnterior());

			}

			if (BigDecimal.ZERO.compareTo(importeAnterior) == 0) {

				importeAnterior = new BigDecimal(0.00);
			}

			model.addAttribute("lista", listAutorizacion);
			model.addAttribute("totalImporte", importe);
			model.addAttribute("totalImporteAnterior", importeAnterior);

			return "fragments/authorization :: table";
		}

		return "fragments/authorization :: noRecord";
	}

	@ResponseBody
	@PostMapping("/process")
	public Generic process(@RequestBody ComparativoAprobacion comparativoAprobacion, BindingResult result,
			RedirectAttributes attributes, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		Generic response = new Generic();
		ComparativoAprobacion comparativoSave;

		// Servicio obtener registro de la BD
		try {

			comparativoSave = serviceComparativoAprobacion.getByIDderegistroAndIdperiodoAndTipodeRebateAndCuenta(
					comparativoAprobacion.getiDderegistro(), comparativoAprobacion.getIdperiodo(),
					comparativoAprobacion.getTipodeRebate(), comparativoAprobacion.getCuenta());

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo procesar/contabilizar el rebate");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		// Actualizar datos en BD
		try {

			// Update [ComparativoAprobacion] en DB
			comparativoSave.setFechaActualizacion(new Date());
			comparativoSave.setUsuarioAutorizacion(sesion.getIdUser());
			comparativoSave.setEstatus("A");

			serviceComparativoAprobacion.save(comparativoSave);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en BD");
			response.setMessage("No se pudo autorizar el proceso");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println("Ocurrió un error: " + ex.getMessage());

			return response;
		}

		response.setTitle("Autorización");
		response.setMessage("Autorizado correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

	@ResponseBody
	@PostMapping("/cancel")
	public Generic cancel(@RequestBody ComparativoAprobacion comparativoAprobacion, BindingResult result,
			RedirectAttributes attributes, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		Generic response = new Generic();
		// Integer idComparativoAprobacion;
		ComparativoAprobacion comparativoSave;
		ComentarioComparativoAprobacion comentarioComparativoSave = new ComentarioComparativoAprobacion();

		// Servicio obtener registro de la BD
		try {

			comparativoSave = serviceComparativoAprobacion.getByIDderegistroAndIdperiodoAndTipodeRebateAndCuenta(
					comparativoAprobacion.getiDderegistro(), comparativoAprobacion.getIdperiodo(),
					comparativoAprobacion.getTipodeRebate(), comparativoAprobacion.getCuenta());

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo procesar/contabilizar el rebate");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		// Actualizar datos en BD
		try {

			comparativoSave.setFechaActualizacion(new Date());
			comparativoSave.setUsuarioAutorizacion(sesion.getIdUser());
			comparativoSave.setEstatus("R");

			serviceComparativoAprobacion.save(comparativoSave);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en BD");
			response.setMessage("No se pudo rechazar el proceso");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println("Ocurrió un error: " + ex.getMessage());

			return response;
		}

		// Crete 1 registro [ComentarioComparativoAprobacion] en DB
		try {

			comentarioComparativoSave.setFechaCreacion(new Date());
			comentarioComparativoSave.setIdUsuario(sesion.getIdUser());
			comentarioComparativoSave.setIdPeriodo(comparativoSave.getIdperiodo());
			comentarioComparativoSave.setIdProveedor(Integer.parseInt(comparativoSave.getCuenta()));
			comentarioComparativoSave.setTipoRebate(comparativoSave.getTipodeRebate());
			comentarioComparativoSave.setComentario(comparativoAprobacion.getMessage());

			serviceComentarioComparativoAprobacion.save(comentarioComparativoSave);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en BD");
			response.setMessage("No se pudo crear registro del comentario");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println("Ocurrió un error: " + ex.getMessage());

			return response;
		}

		response.setTitle("Rachazo");
		response.setMessage("Rechazado correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

	@ResponseBody
	@PostMapping("/multipleProcess")
	public Generic multipleProcess(@RequestBody ComparativoDTO comparativoDto, BindingResult result,
			RedirectAttributes attributes, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		Generic response = new Generic();
		Integer contador = 0;
		ComparativoAprobacion comparativoSave;

		for (ComparativoAprobacion comparativoAprobacion : comparativoDto.getComparativoAprobacionList()) {

			// Servicio obtener registro de la BD
			try {

				comparativoSave = serviceComparativoAprobacion.getByIDderegistroAndIdperiodoAndTipodeRebateAndCuenta(
						comparativoAprobacion.getiDderegistro(), comparativoAprobacion.getIdperiodo(),
						comparativoAprobacion.getTipodeRebate(), comparativoAprobacion.getCuenta());

				contador += 1;

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				response.setTitle("Error en Base de Datos");
				response.setMessage("No se pudo procesar/contabilizar el rebate. Falla en el rebate número: "
						+ contador.toString());
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println(ex.getMessage());

				return response;
			}

			// Actualizar datos en BD
			try {

				// Update [ComparativoAprobacion] en DB
				comparativoSave.setFechaActualizacion(new Date());
				comparativoSave.setUsuarioAutorizacion(sesion.getIdUser());
				comparativoSave.setEstatus("A");

				serviceComparativoAprobacion.save(comparativoSave);

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				response.setTitle("Error en BD");
				response.setMessage(
						"No se pudo autorizar el proceso. Falla en el rebate número: " + contador.toString());
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println("Ocurrió un error: " + ex.getMessage());

				return response;
			}

		}

		response.setTitle("Autorización");
		response.setMessage("Autorizados correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

	@ResponseBody
	@PostMapping("/multipleNegate")
	public Generic multipleNegate(@RequestBody ComparativoDTO comparativoDto, BindingResult result,
			RedirectAttributes attributes, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		
		Generic response = new Generic();
		Integer contador = 0;
		ComparativoAprobacion comparativoSave;

		for (ComparativoAprobacion comparativoAprobacion : comparativoDto.getComparativoAprobacionList()) {

			// Servicio obtener registro de la BD
			try {

				comparativoSave = serviceComparativoAprobacion.getByIDderegistroAndIdperiodoAndTipodeRebateAndCuenta(
						comparativoAprobacion.getiDderegistro(), comparativoAprobacion.getIdperiodo(),
						comparativoAprobacion.getTipodeRebate(), comparativoAprobacion.getCuenta());

				contador += 1;

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				response.setTitle("Error en Base de Datos");
				response.setMessage("No se pudo rechazar el rebate. Falla en el rebate número: " + contador.toString());
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println(ex.getMessage());

				return response;
			}

			// Actualizar datos en BD
			try {

				// Update [ComparativoAprobacion] en DB
				comparativoSave.setFechaActualizacion(new Date());
				comparativoSave.setUsuarioAutorizacion(sesion.getIdUser());
				comparativoSave.setEstatus("R");

				serviceComparativoAprobacion.save(comparativoSave);

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				response.setTitle("Error en BD");
				response.setMessage(
						"No se pudo rechazar el proceso. Falla en el rebate número: " + contador.toString());
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println("Ocurrió un error: " + ex.getMessage());

				return response;
			}

			ComentarioComparativoAprobacion comentarioComparativoSave = new ComentarioComparativoAprobacion();

			// Crete 1 registro [ComentarioComparativoAprobacion] en DB
			try {

				comentarioComparativoSave.setFechaCreacion(new Date());
				comentarioComparativoSave.setIdUsuario(sesion.getIdUser());
				comentarioComparativoSave.setIdPeriodo(comparativoSave.getIdperiodo());
				comentarioComparativoSave.setIdProveedor(Integer.parseInt(comparativoSave.getCuenta()));
				comentarioComparativoSave.setTipoRebate(comparativoSave.getTipodeRebate());
				comentarioComparativoSave.setComentario(comparativoDto.getMessage());

				serviceComentarioComparativoAprobacion.save(comentarioComparativoSave);

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				response.setTitle("Error en BD");
				response.setMessage(
						"No se pudo crear registro del comentario. Falla en el rebate número: " + contador.toString());
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println("Ocurrió un error: " + ex.getMessage());

				return response;
			}

		}

		response.setTitle("Rachazo");
		response.setMessage("Los rebates fueron Rechazados correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

}
