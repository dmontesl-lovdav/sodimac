package com.sodimac.rebates.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.rebates.dto.CatFlujoEstatusDto;
import com.sodimac.rebates.dto.CatPerfilDto;
import com.sodimac.rebates.dto.CatRolDto;
import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.dto.CatUsuarioPerfilDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.dto.RelPeriodoTipoRebateDto;
import com.sodimac.rebates.dto.RelacionPeriodoRebate;
import com.sodimac.rebates.enums.EEstatusPeriodo;
import com.sodimac.rebates.enums.EEvento;
import com.sodimac.rebates.enums.EPerfil;
import com.sodimac.rebates.model.AdminCatalogo;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.model.entity.CatPerfilEntity;
import com.sodimac.rebates.service.ICatFlujoEstatusService;
import com.sodimac.rebates.service.ICatPerfilService;
import com.sodimac.rebates.service.ICatUsuarioPerfilService;
import com.sodimac.rebates.service.ICatalogoService;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IProgramaPagoService;
import com.sodimac.rebates.service.ITipoRebateService;

@Controller
@RequestMapping("/fillrate")
public class FillRateController extends BaseController {

	@Autowired
	private IProgramaPagoService serviceProgramaPago;

	@Autowired
	private IPeriodoService servicePeriodo;

	@Autowired
	private ICatalogoService serviceCatalogo;
	
	@Autowired
	private ITipoRebateService serviceTipoRebate;
	
	@Autowired
	private ICatUsuarioPerfilService catUsuarioPerfilService;
	
	@Autowired
	private ICatFlujoEstatusService catFlujoEstatusService;
	
	@Autowired
	private ICatPerfilService catPerfilService;

	// private List<AdminCatalogo> catCompletado =
	// serviceCatalogo.getCatalogoCompletado();

	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);
		
		List<ProgramaPago> tipos = serviceProgramaPago.getActive();

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
		model.addAttribute("fechaIni", fechaInicio);
		model.addAttribute("fechaFin", fechaFinal);

		return "fillrate";
	}

	@PostMapping("/consult")
	public String consult(@RequestBody Periodo periodo, BindingResult result, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		List<CatRolDto> roles = sesion.getRoles();
		List<CatPerfilEntity> perfiles;
		
		List<PeriodoDto> periodos = null;
		List<AdminCatalogo> catCompletado = new LinkedList<AdminCatalogo>();

		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/fillrate :: start";
		}

		if (periodo.getFechaFin().before(periodo.getFechaIni())) {

			// TODO: cambiar dinámicamente / añadir a bitacora

			model.addAttribute("titulo", "Validación de Fecha");
			model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/fillrate :: start";
		}

		try {

			if (periodo.getProgramaPago().getIdCatProgramaPago() == null) {
				periodos = servicePeriodo.getPeriodoBetweenFechasAndDetallePeriodoLike(periodo.getFechaIni(),
						periodo.getFechaFin(), periodo.getDetallePeriodo());
			} else {
				periodos = servicePeriodo.getPeriodoByOptions(periodo.getFechaIni(), periodo.getFechaFin(),
						periodo.getProgramaPago(), periodo.getDetallePeriodo());
			}
			catCompletado = serviceCatalogo.getCatalogoCompletado();
			perfiles = catPerfilService.getPerfiles();

		} catch (Exception ex) {
			model.addAttribute("titulo", "Error en Base de Datos");
			model.addAttribute("msg", "No se consiguió realizar la consulta");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);
			System.out.println(ex.getMessage());

			return "fragments/fillrate :: start";
		}

		if (periodos != null) {
			List<CatTipoRebateDto> rebates = serviceTipoRebate.getActive();
			
			periodos.stream().forEach(p -> {
				
				CatFlujoEstatusDto flujoEstatus = this.getFlujoEstatus(p, roles);
				p.setFlujoEstatus(flujoEstatus);
				
				if(p.getRelPeriodoTipoRebate() == null || p.getRelPeriodoTipoRebate().isEmpty()) {
					
					List<RelPeriodoTipoRebateDto> listRelacion = new ArrayList<RelPeriodoTipoRebateDto>();
					rebates.stream().forEach(r -> {
							RelPeriodoTipoRebateDto relacion = new RelPeriodoTipoRebateDto();
							PeriodoDto periodoDto = new PeriodoDto();
							periodoDto.setIdCatPeriodo( p.getIdCatPeriodo() );
							
							relacion.setPeriodo(periodoDto);
							relacion.setCatTipoRebate(r);
							relacion.setActivo(false);
							listRelacion.add(relacion);
					});
					p.setRelPeriodoTipoRebate(listRelacion);
				}
				
				ObjectMapper mapper = new ObjectMapper();
				try {
					List<RelPeriodoTipoRebateDto> relPeriodoTipoRebate = p.getRelPeriodoTipoRebate();
					String json =  mapper.writeValueAsString(relPeriodoTipoRebate);
					p.setRelJsonString(json);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

			boolean perfilAdmin = catUsuarioPerfilService.containsPerfil(sesion.getIdUser(), EPerfil.GESTOR_DE_CUENTAS_POR_PAGAR);
			if (!perfilAdmin) { //Solo al administrador no se le aplica el filtro
				List<CatTipoRebateDto> lisCatRebatesPerfil = this.serviceTipoRebate.getTiposRebatesPerfil(sesion.getIdUser());
				periodos = this.reglasMostrarPeriodo(periodos, lisCatRebatesPerfil);
			}
			
			this.reglasProcesarCalculo(periodos, sesion.getPerfilesDto());
			this.reglasReProcesarCalculo(periodos, sesion.getPerfilesDto());
			model.addAttribute("lista", periodos);
			model.addAttribute("catCompletado", catCompletado);
			model.addAttribute("perfiles", perfiles);

			return "fragments/fillrate :: table";
		}

		return "fragments/fillrate :: noRecord";
	}

	private CatFlujoEstatusDto getFlujoEstatus(PeriodoDto p, List<CatRolDto> roles) {
		CatFlujoEstatusDto flujoEstatus = new CatFlujoEstatusDto();
		flujoEstatus.setIdCatFlujoEstatus(-1);
		
		EEvento evento = EEvento.FILLRATE_CALCULO;
		Integer estatus = p.getEstatus();
		
		List<CatFlujoEstatusDto> listFlujos = this.catFlujoEstatusService.getCatFlujoEstatus(roles, evento, estatus);
		if (listFlujos != null && listFlujos.size() > 0) {
			flujoEstatus = listFlujos.get(0);
		}
		return flujoEstatus;
	}

	private List<PeriodoDto> reglasMostrarPeriodo(List<PeriodoDto> periodos, List<CatTipoRebateDto> lisCatRebatesPerfil) {
		List<PeriodoDto> listPeriodos = new ArrayList<>();
		if (periodos != null && lisCatRebatesPerfil != null) {
			siguientePeriodo:
			for (PeriodoDto periodo : periodos) {
				List<RelPeriodoTipoRebateDto> relPeriodoTipoRebate = periodo.getRelPeriodoTipoRebate();
				boolean existeTipoRebate = this.existeTipoRebate(relPeriodoTipoRebate, lisCatRebatesPerfil);
				if (existeTipoRebate) {
					listPeriodos.add(periodo);
					continue siguientePeriodo;
				}	
			}
		}
		return listPeriodos;
	}

	private boolean existeTipoRebate(List<RelPeriodoTipoRebateDto> listRel, List<CatTipoRebateDto> listTipoRebate) {
		if (listRel != null && listTipoRebate != null) {
			for (RelPeriodoTipoRebateDto relacion : listRel) {
				CatTipoRebateDto catTipoRebate = relacion.getCatTipoRebate();
				if (relacion.isActivo()) {
					for(CatTipoRebateDto tp : listTipoRebate) {
						if(tp.getIdCatTipoRebate().intValue() == catTipoRebate.getIdCatTipoRebate().intValue()) {
							return true;
						}
					}
				}
			}
			
		}			
		return false;
	}
	
	private void reglasProcesarCalculo(List<PeriodoDto> periodos, List<CatPerfilDto> perfiles) {
		if (perfiles != null) {
			for (PeriodoDto periodo : periodos) {

				boolean modifCXP = false;
				Integer idUsuarioModifEstatus = periodo.getIdUsuarioModifEstatus();
				if (idUsuarioModifEstatus != null) {
					 List<CatUsuarioPerfilDto> perfilesUsuarioModifEstatus = catUsuarioPerfilService.getUsuarioPerfiles(idUsuarioModifEstatus);
						for ( CatUsuarioPerfilDto per : perfilesUsuarioModifEstatus) {
							if (per.getPerfil().getId() == EPerfil.GESTOR_DE_CUENTAS_POR_PAGAR.getId()) {
								modifCXP = true;
							}					
						}					
				}
				
				int estatus = periodo.getEstatus().intValue();
				for (CatPerfilDto perfil : perfiles) {
					if ( perfil.getId() == EPerfil.GESTOR_DE_CUENTAS_POR_PAGAR.getId()  && 
							( 
								(estatus == EEstatusPeriodo.PENDIENTE_CALCULAR.getId())  ||
								(estatus == EEstatusPeriodo.TERMINO_CALCULO.getId()) || 
								(estatus == EEstatusPeriodo.SOLICITUD_CONTABILIDAD.getId()) ||
								(estatus == EEstatusPeriodo.AUTORIZACION_CONTABILIDAD.getId())
							)
						) {
						
						periodo.setProcesar(true);
					} else if ( perfil.getId() == EPerfil.GERENTE_DE_LOGISTICA.getId()  && !modifCXP &&
							( 
								(estatus == EEstatusPeriodo.PENDIENTE_CALCULAR.getId())  ||
								(estatus == EEstatusPeriodo.TERMINO_CALCULO.getId())
							)
						) {
						
						periodo.setProcesar(true);
					}
					
				} //for (String perfil : perfiles)
			} // for (PeriodoDto periodo : periodos)
		} //if (perfiles != null) {
	}
	
	private void reglasReProcesarCalculo(List<PeriodoDto> periodos, List<CatPerfilDto> perfiles) {
		if (perfiles != null) {
			for (PeriodoDto periodo : periodos) {
				
				boolean modifCXP = false;
				Integer idUsuarioModifEstatus = periodo.getIdUsuarioModifEstatus();
				if (idUsuarioModifEstatus != null) {
					 List<CatUsuarioPerfilDto> perfilesUsuarioModifEstatus = catUsuarioPerfilService.getUsuarioPerfiles(idUsuarioModifEstatus);
						for ( CatUsuarioPerfilDto per : perfilesUsuarioModifEstatus) {
							if (per.getPerfil().getId() == EPerfil.GESTOR_DE_CUENTAS_POR_PAGAR.getId()) {
								modifCXP = true;
							}					
						}					
				}
				
				int estatus = periodo.getEstatus().intValue();
				for (CatPerfilDto perfil : perfiles) {
					if ( perfil.getId() == EPerfil.GESTOR_DE_CUENTAS_POR_PAGAR.getId()  && 
							((estatus == EEstatusPeriodo.SOLICITUD_CONTABILIDAD.getId() ) ||
							 (estatus == EEstatusPeriodo.AUTORIZACION_CONTABILIDAD.getId() ) || 
							 (estatus == EEstatusPeriodo.TERMINO_CALCULO.getId()) )
						) {
						periodo.setReprocesar(true);
					}
					else if ( perfil.getId() == EPerfil.GERENTE_DE_LOGISTICA.getId() && !modifCXP && 
							estatus == EEstatusPeriodo.TERMINO_CALCULO.getId() ) {
						periodo.setReprocesar(true);
					}
				} //for (String perfil : perfiles)
			} // for (PeriodoDto periodo : periodos)
		} //if (perfiles != null) {
	}

	@ResponseBody
	@PostMapping("/create")
	public Generic create(@RequestBody PeriodoDto periodo, BindingResult result) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();

		Generic response = new Generic();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.format(periodo.getFechaIni());
		dateFormat.format(periodo.getFechaFin());

		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {
				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}
			response.setTitle("Error desconocido");
			response.setMessage("");
			response.setTypeMessage(3);
			response.setCode(false);

			return response;
		}

		if (periodo.getFechaFin().before(periodo.getFechaIni())) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Validación de Fecha");
			response.setMessage("La fecha final no puede ser menor a la fecha inicio, favor de validar");
			response.setTypeMessage(2);
			response.setCode(false);

			return response;
		}

		periodo.setActivo(true);
		periodo.setEstatus(0);

		try {
			
			List<CatTipoRebateDto> rebates = serviceTipoRebate.getActive();
			List<RelPeriodoTipoRebateDto> listRelacion = new ArrayList<RelPeriodoTipoRebateDto>();
			rebates.stream().forEach(r -> {
				RelPeriodoTipoRebateDto relacion = new RelPeriodoTipoRebateDto();
				relacion.setPeriodo(periodo);
				relacion.setCatTipoRebate(r);
				relacion.setActivo(true);
				listRelacion.add(relacion);
			});
			periodo.setRelPeriodoTipoRebate(listRelacion);
			
			periodo.setIdUsuarioCreacion(sesion.getIdUser());
			
			servicePeriodo.save(periodo);
		} catch (Exception ex) {

			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo guardar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());
			return response;
		}

		response.setTitle("OK");
		response.setMessage("Registro guardado correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

	@ResponseBody
	@GetMapping("/process/{id}/{estatusDestino}")
	public Generic process(@PathVariable("id") Integer idCatPeriodo
			, @PathVariable("estatusDestino") Integer estatusDestino) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();

		Generic response = new Generic();
		Generic procesar = new Generic();
		Generic procesado = new Generic();

		// Servicio: valida si hay un período del mismo tipo en proceso no permitir
		//DML 
		/*try {

			procesar = servicePeriodo.getPeriodoEnProceso(idCatPeriodo);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo procesar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}*/

		// Validar: respuesta del servicio
		/*if (!procesar.isCode()) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Existe un cálculo de rebate en proceso");
			response.setMessage("El rebate " + procesar.getIdResponse() + " se encuentra en proceso de cálculo");
			response.setTypeMessage(2);
			response.setCode(false);

			return response;
		}*/

		// Servicio: valida si cumple con los documentos requeridos
		try {

			procesar = servicePeriodo.getRequiredProcesarPeriodo(idCatPeriodo);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo procesar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		// Validar: respuesta del servicio
		if (!procesar.isCode()) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle(procesar.getTitle());
			response.setMessage(procesar.getMessage());
			response.setTypeMessage(procesar.getTypeMessage());
			response.setCode(false);

			return response;
		}

		// Servicio Procesar
		try {

			procesado = servicePeriodo.processPeriodo(idCatPeriodo, estatusDestino, sesion.getIdUser());

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo procesar/contabilizar el rebate");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		// Validar: respuesta del servicio
		if (!procesado.isCode()) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle(procesado.getTitle());
			response.setMessage(procesado.getMessage());
			response.setTypeMessage(procesado.getTypeMessage());
			response.setCode(false);

			return response;
		}

		response.setTitle(procesado.getTitle());
		response.setMessage(procesado.getMessage());
		response.setTypeMessage(procesado.getTypeMessage());
		response.setCode(true);

		return response;
	}

	@ResponseBody
	@GetMapping("/reprocesado/{id}")
	public Generic reprocesar(@PathVariable("id") Integer idCatPeriodo) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();

		Generic response = new Generic();
		boolean reprocesado = false;

		try {

			reprocesado = servicePeriodo.reprocesarPeriodo(idCatPeriodo, sesion.getIdUser());

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo cancelar el rebate");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		if (!reprocesado) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error");
			response.setMessage("No se pudo cancelar el rebate");
			response.setTypeMessage(3);
			response.setCode(false);

			return response;
		}

		response.setTitle("OK");
		response.setMessage("Periodo disponible nuevamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

	@ResponseBody
	@GetMapping("/delete/{id}")
	public Generic delete(@PathVariable("id") Integer idCatPeriodo) {

		Generic response = new Generic();
		boolean borrado = false;

		try {

			borrado = servicePeriodo.deletePeriodo(idCatPeriodo);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		if (!borrado) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);

			return response;
		}

		response.setTitle("OK");
		response.setMessage("Registro eliminado correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}
	
	@ResponseBody
	@PostMapping("/editRelacion")
	public Generic editRelacion(@RequestBody PeriodoDto periodo, BindingResult result) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();

		Generic response = new Generic();

		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}
			response.setTitle("Error desconocido");
			response.setMessage("");
			response.setTypeMessage(3);
			response.setCode(false);
			return response;
		}
		
		try {
			List<RelPeriodoTipoRebateDto> listRelacion = new ArrayList<RelPeriodoTipoRebateDto>();
			PeriodoDto entityPeriodo = servicePeriodo.getById(periodo.getIdCatPeriodo());
			for(RelacionPeriodoRebate item : periodo.getTiposRebateRelacion()) {
				CatTipoRebateDto rebateEntity = serviceTipoRebate.getById(item.getIdRebate());
				RelPeriodoTipoRebateDto relacion = servicePeriodo.existeRelacion(entityPeriodo, rebateEntity);
				if (relacion != null) {
					relacion.setActivo(item.getActivo());
				} else {
					relacion = new RelPeriodoTipoRebateDto();
					relacion.setCatTipoRebate(rebateEntity);
					relacion.setPeriodo(entityPeriodo);
					relacion.setActivo(item.getActivo());
				}
				listRelacion.add(relacion);
			}
			
			entityPeriodo.setIdUsuarioModificacion(sesion.getIdUser());
			entityPeriodo.setFechaHoraModificacion(new Date(System.currentTimeMillis()));
			servicePeriodo.saveOrUpdate(entityPeriodo);

			servicePeriodo.editRelacion(listRelacion);
			
		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo guardar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		response.setTitle("OK");
		response.setMessage("Registro guardado correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}
	
	@ResponseBody
	@PostMapping("/editPeriodo")
	public Generic editPeriodo(@RequestBody Periodo periodo, BindingResult result) {

		Generic response = new Generic();

		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}
			response.setTitle("Error desconocido");
			response.setMessage("");
			response.setTypeMessage(3);
			response.setCode(false);

			return response;
		}

		
		try {
			
			if (periodo.getFechaFin().before(periodo.getFechaIni())) {
				response.setTitle("Validación de Fecha");
				response.setMessage("La fecha final no puede ser menor a la fecha inicio, favor de validar");
				response.setTypeMessage(2);
				response.setCode(false);

				return response;
			}
			
			PeriodoDto periodoDb = this.servicePeriodo.getById(periodo.getIdCatPeriodo());
			periodoDb.setFechaIni(periodo.getFechaIni());
			periodoDb.setFechaFin(periodo.getFechaFin());
			servicePeriodo.saveOrUpdate(periodoDb);
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo guardar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}

		response.setTitle("OK");
		response.setMessage("Periodo guardado correctamente");
		response.setTypeMessage(1);
		response.setCode(true);

		return response;
	}

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

}
