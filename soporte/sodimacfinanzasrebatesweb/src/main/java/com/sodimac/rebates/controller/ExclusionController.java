package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.SftpException;
import com.sodimac.rebates.dto.CatEstatusExclusionDto;
import com.sodimac.rebates.dto.CatTipoExclusionDto;
import com.sodimac.rebates.dto.CatTipoRebateDto;
import com.sodimac.rebates.dto.ExclusionCargaDetDto;
import com.sodimac.rebates.dto.ExclusionCargaDto;
import com.sodimac.rebates.dto.ExclusionDetCiscMxDto;
import com.sodimac.rebates.dto.ExclusionDto;
import com.sodimac.rebates.dto.ExclusionViewDetDto;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.dto.RebateProveedorDto;
import com.sodimac.rebates.dto.RebatesCicmxOcDto;
import com.sodimac.rebates.dto.UsuarioDto;
import com.sodimac.rebates.enums.EEstatus;
import com.sodimac.rebates.enums.EEstatusExclusion;
import com.sodimac.rebates.enums.ETipoExclusion;
import com.sodimac.rebates.filter.ExclusionCargaFilter;
import com.sodimac.rebates.filter.ExclusionFilter;
import com.sodimac.rebates.model.DocumentoValidadorModel;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.PeriodoRol;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.ICatTipoExclusionService;
import com.sodimac.rebates.service.IExclusionCargaDetService;
import com.sodimac.rebates.service.IExclusionCargaService;
import com.sodimac.rebates.service.IExclusionService;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IRebateProveedorService;
import com.sodimac.rebates.service.IRebatesCicmxOcService;
import com.sodimac.rebates.service.ITipoRebateService;
import com.sodimac.rebates.util.ExportExclusionExcel;

@Controller
@RequestMapping("/exclusiones")
public class ExclusionController extends BaseController {
	
	private static Logger logger = LoggerFactory.getLogger(ExclusionController.class);
	private static final int CONTABILIZADO_INICIAL = 0;
	private DecimalFormat dfolio = new DecimalFormat("000000");
	
	@Autowired
	private IPeriodoService servicePeriodo;
	
	@Autowired
	private ICatTipoExclusionService catTipoExclusionService;
	
	@Autowired
	private ITipoRebateService serviceTipoRebate;
	
	@Autowired
	private IExclusionService exclusionService;
	
	@Autowired
	private IExclusionCargaService exclusionCargaService; 
	
	@Autowired
	private IExclusionCargaDetService exclusionCargaDetService;
	
	@Autowired
	private IRebatesCicmxOcService rebatesCicmxOcService;
	
	@Autowired
	private IRebateProveedorService proveedorService;

	private String mensajeDescarga = null;
	private int countIndex = 0;
	
	@GetMapping("/index")
	public String index(Model model) throws ParseException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);
		
		List<PeriodoRol> catPeriodoConsult = this.servicePeriodo.getActiveOrderByDesc(sesion.getRoles());
		List<PeriodoRol> listPeriodosAbiertos = this.servicePeriodo.getPeriodoAbierto(sesion.getRoles());
		
		List<CatTipoExclusionDto> catTipoExclusiones = this.catTipoExclusionService.getCatTipoExclusion();
		List<CatTipoRebateDto> catRebates = this.serviceTipoRebate.getActive();
		
		List<CatTipoExclusionDto> catTipoExclusionesPerfil = this.catTipoExclusionService.getCatTipoExclusionPerfil(sesion.getIdUser());
		List<CatTipoRebateDto> catRebatesPerfil = this.serviceTipoRebate.getTiposRebatesPerfil(sesion.getIdUser());
		
		model.addAttribute("listPeriodos", catPeriodoConsult);
		model.addAttribute("listPeriodosAbiertos", listPeriodosAbiertos);
		model.addAttribute("listTipoExclusiones", catTipoExclusiones);
		model.addAttribute("listRebates", catRebates);
		
		model.addAttribute("listTipoExclusionesPerfil", catTipoExclusionesPerfil);
		model.addAttribute("listRebatesPerfil", catRebatesPerfil);
		
		if(mensajeDescarga!=null && countIndex == 0) {
			model.addAttribute("message", mensajeDescarga);
			countIndex++;
		}		
		
		return "exclusiones";
	}
	
	@PostMapping("/consult")
	public String consult(@RequestBody ExclusionFilter exclusionFilter
			 			, BindingResult result
			 			, Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		List<ExclusionDto> listExclusionDtos = null;
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {
				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/listExclusiones :: start";
		}
		
		exclusionFilter.setIdUsuario(sesion.getIdUser());
		
		listExclusionDtos = this.exclusionService.getExclusiones(exclusionFilter);
		if (listExclusionDtos.size() >= 0) {
			sesion.setExclusionFilter(exclusionFilter);
			model.addAttribute("lista", listExclusionDtos);
			model.addAttribute("exclusionFilter", exclusionFilter);
			
			mensajeDescarga = null;
			countIndex = 0;
			
			
			return "fragments/listExclusiones :: table";
		}
		return "fragments/listExclusiones :: noRecord";
	}
	
	@PostMapping("/periodos")
	@ResponseBody
	public List<Periodo> obtenerPeriodos(@RequestParam("idTipoExclusion") Integer idTipoExclusion, Model model) throws ParseException {
		List<Periodo> listPeriodos = null;
		if (idTipoExclusion == ETipoExclusion.ORDEN_COMPRA.getId()) {
			listPeriodos = this.servicePeriodo.getPeriodoAbiertoSinTodos();
		} else {
			listPeriodos = this.servicePeriodo.getPeriodoAbierto();
		}
		return listPeriodos; 
	}
	
	@ResponseBody
	@PostMapping(value = "/create")
	public Generic saveExclusion(ExclusionDto exclusion
			, @RequestParam("archivo") MultipartFile adjunto
			, @RequestParam("layout") MultipartFile layout) throws IOException, SftpException, ParseException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		Generic response = new Generic();
		String extension = null;
		List<ExclusionCargaDto> registros = null;
		Integer idUsuario = sesion.getIdUser();
		List<Integer> perfiles = sesion.getPerfilesDto().stream().map(x -> x.getId()).collect(Collectors.toList());
		
		if (idUsuario == null) {
			response.setTitle("Usuario");
			response.setMessage("Termino la sesion");
			response.setTypeMessage(2);
			response.setCode(false);
			return response;
		}
		
		PeriodoDto periodo = servicePeriodo.getById(exclusion.getPeriodo().getIdCatPeriodo());
		boolean periodoTodos = this.servicePeriodo.isPeriodoTodos(exclusion.getPeriodo().getIdCatPeriodo());
		if ( periodoTodos && exclusion.getCatTipoExclusion().getIdCatTipoExclusion().intValue() == ETipoExclusion.ORDEN_COMPRA.getId() ) {
			response.setTitle("Exclusi\u00f3n incorrecta");
			response.setMessage("No es posible configurar una Exclusi\u00f3n de tipo orden de compra con " + periodo.getDetallePeriodo());
			response.setTypeMessage(2);
			response.setCode(false);
			return response;
		}
		
		if (layout != null && !layout.isEmpty() && !layout.getOriginalFilename().isEmpty()) {
			extension = FilenameUtils.getExtension(layout.getOriginalFilename());
			// Validación: No se permite cargar documentos con una extensión que no le
			// corresponde
			if ( !extension.equals("xlsx")) {
				response.setTitle("Extensi\u00f3n Archivo");
				response.setMessage("Documento no coincide con la extension correspondiente");
				response.setTypeMessage(2);
				response.setCode(false);

				return response;
			}
			
			DocumentoValidadorModel<ExclusionCargaDto> validador = null;
			int idCatTipoExclusion = exclusion.getCatTipoExclusion().getIdCatTipoExclusion().intValue();
			if ( (idCatTipoExclusion == ETipoExclusion.SKU.getId()) || (idCatTipoExclusion == ETipoExclusion.FAMILIA.getId())) {
				validador = this.exclusionService.leerExcelProveedor(layout);
			} else {
				validador = this.exclusionService.leerExcel(layout);
			}
			
			if (validador.getStatus().equals("OK")) {
				logger.info("Validaci\u00f3n de archivo correcta");
				registros = validador.getRegistros();
				
				if (registros != null && registros.size() > 0) {
					//for (ExclusionCargaDto registroExcel : registros) {
					for(int i=0; i<registros.size(); i++) {
						//String numProveedorPlantilla = registroExcel.getNumProveedor();
						String numProveedorPlantilla = registros.get(i).getNumProveedor();
						String msgValidacion = this.exclusionService.validaExclusion( 
								numProveedorPlantilla
								, registros.get(i).getCarga()
								, exclusion.getCatTipoExclusion().getIdCatTipoExclusion()
								, exclusion.getCatTipoRebate().getIdCatTipoRebate()
								, exclusion.getPeriodo().getIdCatPeriodo());
						int registrosFaltantes = ( registros.size() - i - 1 );
						if ( !msgValidacion.equals("OK") ) {
							msgValidacion += registrosFaltantes == 0 ? "" :
								registrosFaltantes > 1 ? ". Faltan " + (registrosFaltantes) + " registros por validar."   
									                   : ". Falta " + (registrosFaltantes) + " registro por validar." ;
							
							
							response.setTitle("Plantilla exclusi\u00f3n incorrecta");
							response.setMessage(msgValidacion);
							response.setTypeMessage(2);
							response.setCode(false);
							return response;
						}
					}
				}
				
			} else {
				response.setTitle("Documento Inv\u00e1lido");
				response.setMessage(validador.getMessage());
				response.setTypeMessage(2);
				response.setCode(false);
				return response;
			}
		}
		
		if (adjunto != null && !adjunto.isEmpty() && !adjunto.getOriginalFilename().isEmpty()) {
			String archivoAdjunto = adjunto.getOriginalFilename();
			String extArchivoAdjunto = FilenameUtils.getExtension(archivoAdjunto);
			boolean archivoValido = this.isArchivoValido(extArchivoAdjunto);
			//jpg,.png,.jpeg,.csv
			if ( !archivoValido ) {
				response.setTitle("Extensi\u00f3n Evidencia");
				response.setMessage("Documento no coincide con la extension correspondiente (jpg, png, jpeg)");
				response.setTypeMessage(2);
				response.setCode(false);
				return response;
			}
			
			byte[] imagen =adjunto.getBytes();
			exclusion.setEvidencia(archivoAdjunto);
			exclusion.setImagen(imagen);
		}
		
		String numProveedorPantalla = exclusion.getNumeroProveedor();
		String msgValidacion = this.exclusionService.validaExclusion(
				numProveedorPantalla
				, exclusion.getExclusion()
				, exclusion.getCatTipoExclusion().getIdCatTipoExclusion()
				, exclusion.getCatTipoRebate().getIdCatTipoRebate()
				, exclusion.getPeriodo().getIdCatPeriodo());
		
		if ( !msgValidacion.equals("OK") ) {
			response.setTitle("Exclusi\u00f3n incorrecta");
			response.setMessage(msgValidacion);
			response.setTypeMessage(2);
			response.setCode(false);
			return response;
		}

		UsuarioDto usuarioAutorizador = null; //this.exclusionService.getUsuarioAutorizador();
		Integer folioFacturaMax = this.exclusionService.getMaxFolio();
		if (folioFacturaMax == null) {
			folioFacturaMax = 1;
		}
		String folio = dfolio.format(folioFacturaMax);
		 
		EEstatusExclusion eEstatusExclusion = EEstatusExclusion.PENDIENTE_AUTORIZACION;
		
		//2024-11-04 rmt Verifica que el peerfil-tipoExclusion se autoriza en automatico 
		if (this.exclusionService.getPerfilAutorizado(perfiles, exclusion.getCatTipoExclusion().getIdCatTipoExclusion())) {
			eEstatusExclusion = EEstatusExclusion.SOLICITUD;
			usuarioAutorizador = new UsuarioDto();
			usuarioAutorizador.setId(idUsuario);
			exclusion.setFechaHoraAutorizacion(new Date());
		}		

		if(ETipoExclusion.ORDEN_COMPRA.getId() == exclusion.getCatTipoExclusion().getIdCatTipoExclusion() 
				&& Strings.isNotBlank(exclusion.getExclusion())) {
			if(this.exclusionService.ordenCompraPertenecePeriodo(exclusion, periodo)) {
				eEstatusExclusion = EEstatusExclusion.SOLICITUD;
				usuarioAutorizador = new UsuarioDto();
				usuarioAutorizador.setId(idUsuario);
				exclusion.setFechaHoraAutorizacion(new Date());
			} else {
				//2024-10-15 rmt envia mensaje
				if(this.exclusionService.ordenCompraDespuesPeriodo(exclusion, periodo)) {
					response.setTitle("Valida Exclusi\u00f3n y Periodo");
					response.setMessage("No es posible agregar exclusiones de Orden de compra superiores a la fecha del periodo");
					response.setTypeMessage(2);
					response.setCode(false);
					return response;									
				}
			}			
		}

		// Se comento ya que el usuario solicito que no se validara		
//			VwRebateOrdenCompraEntity orden = vwRebateOrdenCompraService.leerOrdenCompra(Integer.parseInt(exclusion.getExclusion()));
//			
//			if (orden != null) {
//				String proveedor = orden.getProveedor();
//				String nombreProveedor = orden.getNombreProveedor();
//
//				if (!catCompradorProveedorService.tienePermiso(idUsuario, proveedor)) {
//					response.setTitle("Valida Usuario y Proveedor");
//					response.setMessage("Permisos insuficientes para gestionar la exclusión de ordenes de compra del proveedor [" + proveedor + "] - [" + nombreProveedor + "]");
//					response.setTypeMessage(2);
//					response.setCode(false);
//					return response;													
//				}
//			}
						

		
		exclusion.setContabilizado(CONTABILIZADO_INICIAL);
		exclusion.setActivo(true);
		exclusion.setCatEstatusExclusion(new CatEstatusExclusionDto(eEstatusExclusion.getIdEstatus()));
		exclusion.setFolio( folio );
		exclusion.setUsuarioSolicitud( new UsuarioDto( sesion.getIdUser() ));
		exclusion.setUsuarioAutorizacion(usuarioAutorizador);
		exclusion.setFechaHoraSolicitud(new Date());
		exclusion.setListExclusiones( registros );
		logger.info(exclusion.toString());
		 
		this.exclusionService.guardar(exclusion, idUsuario);
		 
		response.setTitle("OK");
		response.setMessage("Registro guardado correctamente con folio: " + folio);
		response.setTypeMessage(1);
		response.setCode(true);
		return response;	
	}
	
	@GetMapping("/detalle")
	@ResponseBody
	public ExclusionDto detalleExclusion(
			  @RequestParam("idExclusion") Integer idExclusion
			, Model theModel) {

		ExclusionDto model = new ExclusionDto();
		model = this.exclusionService.getExclusion(idExclusion);
		return model;
	}
	
	@GetMapping("/detalleproveedor")
	@ResponseBody
	public ExclusionDto detalleProveedorExclusion(
			  @RequestParam("idExclusion") Integer idExclusion, @RequestParam("proveedor") String proveedor
			, Model theModel) {

		ExclusionDto model = new ExclusionDto();
		model = this.exclusionService.getExclusion(idExclusion, proveedor);
		return model;
	}

	@ResponseBody
	@PostMapping(value = "/add/det")
	public Generic addExclusionDet(@RequestBody ExclusionCargaDto exclusionDetDto) {
		Generic response = new Generic();
		
		try {
			
			if ( !(exclusionDetDto.getJsonId() != null && !exclusionDetDto.getJsonId().isEmpty())) {
				response.setTitle("Valida Exclusi\u00f3n");
				response.setMessage("Seleccionar una exclusi\u00f3n");
				response.setTypeMessage(2);
				response.setCode(false);
				return response;
			}
			
			exclusionDetDto.setActivo(EEstatus.ACTIVO.isActivo());
			exclusionDetDto.setFechaRegistro(new Date());
			this.exclusionCargaService.guardarJson(exclusionDetDto);
			
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n agregada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch(Exception e) {
			e.printStackTrace();
			
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo guardar los cambios en la BD");
			response.setTypeMessage(3);
			response.setCode(false);
		}
		return response;
	}
	
	@ResponseBody
	@PostMapping(value = "/add/sku")
	public Generic addSkuDet(@RequestBody ExclusionCargaDto exclusionDetDto) {
		Generic response = new Generic();
		
		try {
			exclusionDetDto.setActivo(EEstatus.ACTIVO.isActivo());
			exclusionDetDto.setFechaRegistro(new Date());
			this.exclusionCargaService.guardarJson(exclusionDetDto);
			
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n agregada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch(Exception e) {
			e.printStackTrace();
			
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo guardar los cambios en la BD");
			response.setTypeMessage(3);
			response.setCode(false);
		}
		return response;
	}
	
	@ResponseBody
	@PutMapping("/delete/{id}")
	public Generic delete(@PathVariable("id") Integer idExclusion) {
		Generic response = new Generic();

		try {
			this.exclusionService.borradoLogico(idExclusion);
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n eliminada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
	
	@ResponseBody
	@PutMapping("/inactivar/{id}")
	public Generic inactivar(@PathVariable("id") Integer idExclusion) {
		Generic response = new Generic();

		try {
			this.exclusionService.inactivar(idExclusion);
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n inactiva correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
	
	@ResponseBody
	@PutMapping("/autorizar/{id}")
	public Generic autorizar(@PathVariable("id") Integer idExclusion) {
		Generic response = new Generic();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		try {
			this.exclusionService.autorizar(idExclusion, sesion.getIdUser());
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n autorizada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo autorizar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
	
	@ResponseBody
	@PutMapping("/rechazar/{id}")
	public Generic rechazar(@PathVariable("id") Integer idExclusion) {
		Generic response = new Generic();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		try {
			this.exclusionService.rechazar(idExclusion, sesion.getIdUser());
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n rechazada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo rechazar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
	
	@ResponseBody
	@PutMapping("/delete/det/{id}")
	public Generic deleteDet(@PathVariable("id") Long idExclusionCarga) {
		Generic response = new Generic();

		try {
			this.exclusionCargaService.borradoLogico(idExclusionCarga);
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n eliminada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
	
	@ResponseBody
	@PutMapping("/delete/carga/det/{id}")
	public Generic deleteCargaDet(@PathVariable("id") Long idExclusionCargaDet) {
		Generic response = new Generic();

		try {
			this.exclusionCargaDetService.borradoLogico(idExclusionCargaDet);
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n eliminada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
	
	@GetMapping("/detalle/exclusion/sku")
	@ResponseBody
	public ExclusionDetCiscMxDto detalleExclusionSku(@RequestParam("idExclusion") Integer idExclusion
											       , @RequestParam("idExclusionCarga") Long idExclusionCarga
											       , @RequestParam("proveedor") String proveedor
											       , @RequestParam("sku") String sku) {
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		
		//ExclusionCargaDto cargaDto = this.exclusionService.getExclusionById(idExclusion);
		RebateProveedorDto proveedorDto = this.proveedorService.getProveedor(proveedor);
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdExclusionCarga(idExclusionCarga);
		filter.setNumProveedor(proveedorDto.getNumProveedor());
		filter.setSku(sku);
		
		List<ExclusionCargaDetDto> exclusionCargaDetDtos = this.exclusionCargaDetService.obtenerSkus(filter);
		
		model.setProveedor( proveedorDto );
		model.setListExclusionCargaDet(exclusionCargaDetDtos);
		return model;
	}
	
	@GetMapping("/detalle/exclusion/sku/familia")
	@ResponseBody
	public ExclusionDetCiscMxDto detalleExclusionSkuFamilia(@RequestParam("idExclusion") Integer idExclusion
												  		  , @RequestParam("idExclusionCarga") Long idExclusionCarga
												  		  , @RequestParam("proveedor") String proveedor
												  		  , @RequestParam("clacom") String clacom) {
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		
		ExclusionCargaDto cargaDto = this.exclusionCargaService.getExclusionCargaById(idExclusionCarga);
		RebateProveedorDto proveedorDto = this.proveedorService.getProveedor(proveedor);
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdExclusionCarga(idExclusionCarga);
		filter.setNumProveedor(proveedor);
		filter.setClacom(clacom);
		
		List<ExclusionCargaDetDto> exclusionCargaDetDtos = this.exclusionCargaDetService.obtenerSkusUnicos(filter);	
		model.setProveedor( proveedorDto );
		model.setExclusionCarga(cargaDto);
		model.setListExclusionCargaDet(exclusionCargaDetDtos);
		return model;
	}
	
	@GetMapping("/detalle/exclusion/sku/oc")
	@ResponseBody
	public ExclusionDetCiscMxDto detalleExclusionSkuOrdenCompra(@RequestParam("idExclusion") Integer idExclusion
												  		  	, @RequestParam("idExclusionCarga") Long idExclusionCarga
												  		  	, @RequestParam("proveedor") String proveedor
												  		  	, @RequestParam("ordenCompra") String ordenCompra) {
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		
		ExclusionCargaDto cargaDto = this.exclusionCargaService.getExclusionCargaById(idExclusionCarga);
		RebateProveedorDto proveedorDto = this.proveedorService.getProveedor(proveedor);
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdExclusionCarga(idExclusionCarga);
		filter.setNumProveedor(proveedor);
		filter.setOrdenCompra(ordenCompra);
		
		List<ExclusionCargaDetDto> exclusionCargaDetDtos = this.exclusionCargaDetService.obtenerSkus(filter);
		model.setOrdenCompra(ordenCompra);
		model.setProveedor( proveedorDto );
		model.setExclusionCarga(cargaDto);
		model.setListExclusionCargaDet(exclusionCargaDetDtos);
		return model;
	}
	
	@GetMapping("/detalle/exclusion/oc/proveedor")
	@ResponseBody
	public ExclusionDetCiscMxDto detalleExclusionOcProveedor(@RequestParam("idExclusion") Integer idExclusion
												  		   , @RequestParam("idExclusionCarga") Long idExclusionCarga
												  		   , @RequestParam("proveedor") String proveedor) {
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		
		//ExclusionCargaDto cargaDto = this.exclusionService.getExclusionById(idExclusion);
		RebateProveedorDto proveedorDto = this.proveedorService.getProveedor(proveedor);
		ExclusionCargaDto cargaDto = new ExclusionCargaDto();
		//ProveedorDto proveedorDto = null;
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdExclusionCarga(idExclusionCarga);
		filter.setNumProveedor(proveedor);
		
		List<ExclusionCargaDetDto> exclusionCargaDetDtos = this.exclusionCargaDetService.obtenerOrdenesCompra(filter);
		if (exclusionCargaDetDtos != null && exclusionCargaDetDtos.size() > 0) {
			ExclusionCargaDetDto cargaDetDto = exclusionCargaDetDtos.get(0);
			cargaDto.setCarga( cargaDetDto.getOrdenCompra());
			cargaDto.setMotivo( cargaDetDto.getMotivo() );
		}
			
		model.setProveedor( proveedorDto );
		model.setExclusionCarga(cargaDto);
		model.setListExclusionCargaDet(exclusionCargaDetDtos);
		return model;
	}
	
	@GetMapping("/list/disponible/proveedor/sesion")
	@ResponseBody
	public ExclusionDetCiscMxDto listarProveedorCismx(@RequestParam("idExclusion") Integer idExclusion) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion); 
		RebateProveedorDto proveedorDto =  this.getProveedorUserId(sesion.getIdUser());
		
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		
		listCicmxDto = this.rebatesCicmxOcService.obtenerProveedoresDisponibles(filter);
		model.setProveedor( proveedorDto );
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/list/disponible/sku/sesion")
	@ResponseBody
	public ExclusionDetCiscMxDto listarAddCismx(@RequestParam("idExclusion") Integer idExclusion) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion);
		RebateProveedorDto proveedorDto =  this.getProveedorUserId(sesion.getIdUser());
		
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		filter.setNumProveedor(proveedorDto.getNumProveedor());
		
		listCicmxDto = this.rebatesCicmxOcService.obtenerSkuDisponible(filter);
		model.setProveedor( proveedorDto );
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/list/disponible/oc")
	@ResponseBody
	public ExclusionDetCiscMxDto listarDisponibleOC(@RequestParam("idExclusion") Integer idExclusion
												  , @RequestParam("proveedor") String proveedor) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion);
		RebateProveedorDto proveedorDto =  this.getProveedorUserId(sesion.getIdUser());
		
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		filter.setNumProveedor(proveedor);
		
		try {
			listCicmxDto = this.rebatesCicmxOcService.obtenerOrdenesCompraDisponible(filter);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		model.setProveedor( proveedorDto );
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/list/disponible/oc/sesion")
	@ResponseBody
	public ExclusionDetCiscMxDto listarDisponibleOcSesion(@RequestParam("idExclusion") Integer idExclusion) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion);
		
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		filter.setIdUsuario(sesion.getIdUser());
		
		try {
			listCicmxDto = this.rebatesCicmxOcService.obtenerOrdenesCompraDisponible(filter);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/list/disponible/familia/sesion")
	@ResponseBody
	public ExclusionDetCiscMxDto listarDisponibleFamiliaSesion(@RequestParam("idExclusion") Integer idExclusion) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion);
		RebateProveedorDto proveedorDto =  this.getProveedorUserId(sesion.getIdUser());
		ExclusionCargaDto cargaDto = null;
		
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		filter.setNumProveedor(proveedorDto.getNumProveedor());
		
		listCicmxDto = this.rebatesCicmxOcService.obtenerFamiliaDisponible(filter);
		
		model.setExclusionCarga(cargaDto);
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/list/disponible/sku/oc")
	@ResponseBody
	public ExclusionDetCiscMxDto listarDisponibleSkuOc(@RequestParam("idExclusion") Integer idExclusion
													 , @RequestParam("proveedor") String proveedor 
												   	 , @RequestParam("ordenCompra") String ordenCompra) {
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion);
		RebateProveedorDto proveedorDto =  this.proveedorService.getProveedor(proveedor);
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		filter.setNumProveedor(proveedorDto.getNumProveedor());
		filter.setOrdenCompra(ordenCompra);
		
		listCicmxDto = this.rebatesCicmxOcService.obtenerSkuDisponible(filter);
		
		model.setProveedor( proveedorDto );
		model.setOrdenCompra(ordenCompra);
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/list/disponible/sku/unico")
	@ResponseBody
	public ExclusionDetCiscMxDto listarDisponibleSkuUnico(@RequestParam("idExclusion") Integer idExclusion
												   		, @RequestParam("familia") String familia) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionDetCiscMxDto model = new ExclusionDetCiscMxDto();
		List<RebatesCicmxOcDto> listCicmxDto = null;
		ExclusionDto exclusion = this.exclusionService.getExclusion(idExclusion);
		RebateProveedorDto proveedorDto =  this.getProveedorUserId(sesion.getIdUser());
		//OrdenCompraDto ordenCompraDto = new OrdenCompraDto();
		
		logger.info(exclusion.toString());
		
		ExclusionCargaFilter filter = new ExclusionCargaFilter();
		filter.setIdExclusion(idExclusion);
		filter.setIdPeriodoCat( exclusion.getPeriodo().getIdCatPeriodo() );
		filter.setIdCatTipoRebate( exclusion.getCatTipoRebate().getIdCatTipoRebate() );
		filter.setNumProveedor(proveedorDto.getNumProveedor());
		filter.setClacom(familia);
		
		listCicmxDto = this.rebatesCicmxOcService.obtenerSkuUnicoDisponible(filter);
		
		model.setClacom(familia);
		model.setProveedor( proveedorDto );
		//model.setOrdenCompra(ordenCompraDto);
		model.setCicmxOcDtos(listCicmxDto);
		return model;
	}
	
	@GetMapping("/report")
	public void exportReportUser(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		ExclusionFilter exclusionFilter = sesion.getExclusionFilter();

		if(exclusionFilter == null) {
			ServletContext servletContext = request.getServletContext();
			mensajeDescarga = "No fue posible generar el reporte, intenta con otra consulta.";
			countIndex = 0;
			response.sendRedirect(servletContext.getContextPath() + "/exclusiones/index");
		} else {
			String name_report_user = "Exclusiones.xlsx";
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + name_report_user);

			exclusionFilter.setRowsPerPage(-1);
			List<ExclusionDto> listExclusionDtos = this.exclusionService.getExclusiones(exclusionFilter);
			List<ExclusionViewDetDto> exclusionesDetDtos = this.exclusionService.getExclusionesDet(exclusionFilter);
			ByteArrayInputStream stream = ExportExclusionExcel.createExcel(listExclusionDtos, exclusionesDetDtos);
			IOUtils.copy(stream, response.getOutputStream());	
		}
	}
	
	@GetMapping("/download/layout")
	public void downloadLayout(@RequestParam("tipo") int tipo, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		File layout = null;
		switch (tipo) {
			case 1: // ID_ORDEN_COMPRA
				layout = ResourceUtils.getFile("classpath:/layouts/Layout_Exclusion_OC.xlsx") ;
			break;
			case 2: // ID_SKU
				layout = ResourceUtils.getFile("classpath:/layouts/Layout_Exclusion_Sku.xlsx");
			break; 
			case 3: //ID_FAMILIA
				layout = ResourceUtils.getFile("classpath:/layouts/Layout_Exclusion_Familia.xlsx");
			break;
			case 4: // ID_PROVEEDORES
				layout = ResourceUtils.getFile("classpath:/layouts/Layout_Exclusion_Proveedor.xlsx");
			break;
		}
		
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + layout.getName());
		ByteArrayInputStream stream  = new ByteArrayInputStream(FileUtils.readFileToByteArray(layout));
		
		IOUtils.copy(stream, response.getOutputStream());	

	}
	
	@GetMapping("/evidencia/{idExclusion}")
	@ResponseBody
	public void exportReportUser(HttpServletResponse response, @PathVariable Integer idExclusion)
			throws IOException {
		ExclusionDto dto = this.exclusionService.getEvidenciaExclusion(idExclusion);
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" +  dto.getEvidencia());
		
		ByteArrayInputStream stream = new ByteArrayInputStream(dto.getImagen());
		IOUtils.copy(stream, response.getOutputStream());
	}
	
	private boolean isArchivoValido(String extension) {
		String[] extensiones = {"jpg","png","jpeg","eml","msg","pdf"};
		for (String ext : extensiones) {
			if (ext.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}
	
	public RebateProveedorDto getProveedorUserId(Integer idUser) {
		RebateProveedorDto proveedorDto = new RebateProveedorDto();
		return proveedorDto;
	}

	@ResponseBody
	@PostMapping("/updateComentario")
	public Generic updateComentario(@RequestBody ExclusionDto exclusion) {
		Generic response = new Generic();

		try {
			this.exclusionService.modificarComentario(exclusion.getIdExclusion(), exclusion.getComentario());
			response.setTitle("OK");
			response.setMessage("Exclusi\u00f3n modificada correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo modificar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());
		}
		return response;
	}

}
