package com.sodimac.rebates.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.jcraft.jsch.SftpException;
import com.sodimac.rebates.dto.PeriodoDto;
import com.sodimac.rebates.enums.EEstatusPeriodo;
import com.sodimac.rebates.enums.EMensaje;
import com.sodimac.rebates.model.AdminCatalogo;
import com.sodimac.rebates.model.ControlDocumento;
import com.sodimac.rebates.model.Documento;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.ICatMensajeService;
import com.sodimac.rebates.service.ICatalogoService;
import com.sodimac.rebates.service.IControlDocumentoService;
import com.sodimac.rebates.service.IDocumentoService;
import com.sodimac.rebates.service.IPeriodoService;
import com.sodimac.rebates.service.IUploadGateway;
import com.sodimac.rebates.util.Util;

@Controller
@RequestMapping("/documents")
public class ControlDocumentoController extends BaseController {

	@Value("${spring.servlet.multipart.location}")
	private String ruta;

	@Autowired
	private IPeriodoService servicePeriodo;

	@Autowired
	private IControlDocumentoService serviceControlDocumento;

	@Autowired
	private IDocumentoService serviceDocumento;

	@Autowired
	private ICatalogoService serviceCatalogo;

	@Autowired
	private IUploadGateway serviceGateway;
	
	@Autowired
	private ICatMensajeService catMensajeService;

	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);

		List<Periodo> catPeriodoCreate = new LinkedList<Periodo>();
		List<Periodo> catPeriodoConsult = new LinkedList<Periodo>();
		List<Documento> catDocumento = new LinkedList<Documento>();
		// List<ControlDocumento> listControlDocumento = new
		// LinkedList<ControlDocumento>();
		List<AdminCatalogo> catEstatusDocumento = new LinkedList<AdminCatalogo>();
		List<ControlDocumento> listControlDocumentoDistinctContabilizado = new LinkedList<ControlDocumento>();

		try {

			catPeriodoCreate = servicePeriodo.getActiveAndEstatus();
			// TODO: ¿Qué períodos traer para consultar?
			catPeriodoConsult = servicePeriodo.getActive();
			catDocumento = serviceDocumento.getActive();
			// listControlDocumento = serviceControlDocumento.getActive();
			catEstatusDocumento = serviceCatalogo.getCatalogoEstatusDocumento();
			listControlDocumentoDistinctContabilizado = serviceControlDocumento
					.getActiveAndStatusDistinticContabilizado();
			this.reglasEliminarDocumento(listControlDocumentoDistinctContabilizado);

		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error en Base de Datos");
			model.addAttribute("msg", "No se consiguió realizar la consulta");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);
			model.addAttribute("periodos", null);
			model.addAttribute("documentos", null);
			model.addAttribute("lista", null);
			System.out.println(ex.getMessage());

			return "documento";
		}

		model.addAttribute("periodos", catPeriodoCreate);
		model.addAttribute("periodosConsult", catPeriodoConsult);
		model.addAttribute("documentos", catDocumento);
		model.addAttribute("catEstatusDoc", catEstatusDocumento);
		model.addAttribute("lista", listControlDocumentoDistinctContabilizado);
		return "documento";
	}

	@PostMapping("/consult")
	public String consult(@RequestBody ControlDocumento controlDocumento,
			BindingResult result, Model model) {

		List<AdminCatalogo> catEstatusDocumento = new LinkedList<AdminCatalogo>();
		List<ControlDocumento> listControlDocumentoDistinctContabilizado = new LinkedList<ControlDocumento>();
		String idDocumento = "";
		String idPeriodo = "";

		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/documento :: start";
		}

		if (controlDocumento.getFechaCargaIni() == null && controlDocumento.getFechaCargaFin() == null) {

			if (controlDocumento.getDocumento().getIdDocumento() != null) {

				idDocumento = controlDocumento.getDocumento().getIdDocumento().toString();

			} else {

				idDocumento = "%";
			}

			if (controlDocumento.getPeriodo().getIdCatPeriodo() != null) {

				idPeriodo = controlDocumento.getPeriodo().getIdCatPeriodo().toString();

			} else {

				idPeriodo = "%";
			}

			try {

				catEstatusDocumento = serviceCatalogo.getCatalogoEstatusDocumento();
				listControlDocumentoDistinctContabilizado = serviceControlDocumento
						.getActiveQeryWithOutDates(controlDocumento.getNombreArchivo(), idDocumento, idPeriodo);

			} catch (Exception ex) {

				// TODO: cambiar dinámicamente / añadir a bitacora
				model.addAttribute("titulo", "Error en Base de Datos");
				model.addAttribute("msg", "No se consiguió realizar la consulta");
				model.addAttribute("tipo", 3);
				model.addAttribute("code", false);
				model.addAttribute("lista", null);
				System.out.println(ex.getMessage());

				return "fragments/documento :: noRecord";
			}

		} else {

			// Validación: Si se elige una Fecha Inicio debe seleccionar Fecha final
			if (controlDocumento.getFechaCargaIni() != null && controlDocumento.getFechaCargaFin() == null) {

				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "Debe seleccionar una fecha final, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);

				return "fragments/documento :: start";

			} else if (controlDocumento.getFechaCargaFin() != null && controlDocumento.getFechaCargaIni() == null) {
				// Validación: Si se elige una Fecha Final debe seleccionar Fecha Inicio

				model.addAttribute("titulo", "Validación de Fecha");
				model.addAttribute("msg", "Debe seleccionar una fecha inicio, favor de validar");
				model.addAttribute("tipo", 2);
				model.addAttribute("code", false);

				return "fragments/documento :: start";

			} else {

				// Validación: La fecha Final no puede ser menor a Fecha inicio
				if (controlDocumento.getFechaCargaFin().before(controlDocumento.getFechaCargaIni())) {

					// TODO: cambiar dinámicamente / añadir a bitacora

					model.addAttribute("titulo", "Validación de Fecha");
					model.addAttribute("msg", "La fecha final no puede ser menor a la fecha inicio, favor de validar");
					model.addAttribute("tipo", 2);
					model.addAttribute("code", false);

					return "fragments/documento :: start";
				}

				if (controlDocumento.getDocumento().getIdDocumento() != null) {

					idDocumento = controlDocumento.getDocumento().getIdDocumento().toString();

				} else {

					idDocumento = "%";
				}

				if (controlDocumento.getPeriodo().getIdCatPeriodo() != null) {

					idPeriodo = controlDocumento.getPeriodo().getIdCatPeriodo().toString();

				} else {

					idPeriodo = "%";
				}

				// TODO: Qery con fechas
				try {

					catEstatusDocumento = serviceCatalogo.getCatalogoEstatusDocumento();
					listControlDocumentoDistinctContabilizado = serviceControlDocumento.getActiveQeryWithDates(
							controlDocumento.getFechaCargaIni(), controlDocumento.getFechaCargaFin(),
							controlDocumento.getNombreArchivo(), idDocumento, idPeriodo);

				} catch (Exception ex) {

					// TODO: cambiar dinámicamente / añadir a bitacora
					model.addAttribute("titulo", "Error en Base de Datos");
					model.addAttribute("msg", "No se consiguió realizar la consulta");
					model.addAttribute("tipo", 3);
					model.addAttribute("code", false);
					model.addAttribute("lista", null);
					System.out.println(ex.getMessage());

					return "fragments/documento :: noRecord";
				}

			}

		}

		this.reglasEliminarDocumento(listControlDocumentoDistinctContabilizado);
		if (listControlDocumentoDistinctContabilizado.size() >= 1) {

			model.addAttribute("lista", listControlDocumentoDistinctContabilizado);
			model.addAttribute("catEstatusDoc", catEstatusDocumento);

			return "fragments/documento :: table";
		}

		return "fragments/documento :: noRecord";
	}
	
	private void reglasEliminarDocumento(List<ControlDocumento> listControlDocumentoDistinctContabilizado) {
		if (listControlDocumentoDistinctContabilizado != null) {
			for (ControlDocumento controlDoc : listControlDocumentoDistinctContabilizado) {
				Periodo periodo = controlDoc.getPeriodo();
				if ( periodo.getEstatus().intValue() == EEstatusPeriodo.PENDIENTE_CALCULAR.getId() ) {
					controlDoc.setEliminar(true);
				}
			}
			
		}
	}

	/**
	 * Request GET, return boolean.
	 * 
	 * @throws IOException
	 */
	@ResponseBody
	@GetMapping(value = "/validate")
	public boolean validateControlDocumento(ControlDocumento controlDocumento)  {
		return this.serviceControlDocumento.existeDocumento(controlDocumento.getDocumento(), controlDocumento.getPeriodo());
	}

	/**
	 * Request POST, return Control Documento List Paginate.
	 * 
	 * @throws IOException
	 * @throws SftpException
	 */
	@ResponseBody
	@PostMapping(value = "/create")
	public Generic saveControlDocumento(ControlDocumento controlDocumento, @RequestParam("archivo") MultipartFile multiPart) throws IOException, SftpException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		Generic response = new Generic();
		Util util = new Util();
		String extension = FilenameUtils.getExtension(multiPart.getOriginalFilename());
		Documento documento = serviceDocumento.getById(controlDocumento.getDocumento().getIdDocumento());
		int idPeriodoTodos = this.servicePeriodo.getIdPeriodoTodos();
		boolean sftpHabilitado = this.serviceControlDocumento.isSftpHabilitado();
		
		// Validación: No se permite cargar documentos con una extensión que no le
		// corresponde
		if (!documento.getExtension().contains(extension)) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Extensión Archivo");
			response.setMessage("Documento no coincide con la extension correspondiente");
			response.setTypeMessage(2);
			response.setCode(false);

			return response;
		}

		if ( controlDocumento.getDocumento().getIdDocumento() == 14 ) { // Envios AP
			System.out.println("Comienza lectura del archivo");
			return serviceControlDocumento.readCsvEnviosAp(multiPart, sesion.getIdUser());
		}
		
		PeriodoDto periodo = servicePeriodo.getById(controlDocumento.getPeriodo().getIdCatPeriodo());
		// Validación: No se permite cargar documentos de Validación a un Periodo en
		// estatus 0
		if (periodo.getEstatus().equals(0)
				&& (documento.getIdDocumento().equals(8) || documento.getIdDocumento().equals(13))) {

			response.setTitle("Documento Inválido");
			response.setMessage("No se permite cargar el documento en este momento");
			response.setTypeMessage(2);
			response.setCode(false);

			return response;
		}

		boolean isPeriodoComun = this.serviceDocumento.aceptaTodosPeriodos(documento);
		if (!isPeriodoComun && (idPeriodoTodos == periodo.getIdCatPeriodo().intValue()) ) {
			response.setTitle("Periodo Inválido");
			response.setMessage("Para el documento " + documento.getNombreDocumento() + " no es posible asociar Todos los periodos" );
			response.setTypeMessage(2);
			response.setCode(false);
			return response;
		}
		
		boolean existeDocumento = serviceControlDocumento.existeDocumento(controlDocumento.getDocumento(), controlDocumento.getPeriodo());
		if (existeDocumento) {
			String mensaje = this.catMensajeService.getMensaje(EMensaje.CATALOGO_CARGADO);
			String msgCargado = String.format(mensaje, documento.getNombreDocumento());
			response.setTitle("Documento Inválido");
			response.setMessage(msgCargado);
			response.setTypeMessage(2);
			response.setCode(false);

			return response;
		}

		String sftpPath = documento.getRutaDocumento();
		File uploadFile = null;

		if (!multiPart.isEmpty()) {

			try {
				uploadFile = util.saveFile(multiPart, ruta, documento, controlDocumento.getPeriodo().getIdCatPeriodo());
			} catch (Exception ex_) {
				// TODO: cambiar dinámicamente / añadir a bitacora
				response.setTitle("Error en el proceso");
				response.setMessage("No se pudo cargar documento al SFTP");
				response.setTypeMessage(3);
				response.setCode(false);
				System.out.println("Ocurrió un error: " + ex_.getMessage());
				return response;
			}

			if (uploadFile != null) {
				try {
					if (sftpHabilitado) {
						boolean upload = serviceGateway.uploadFile(uploadFile, sftpPath);
						if (!upload) {
							response.setTitle("Error al cargar Documento");
							response.setMessage("No se pudo cargar documento al SFTP");
							response.setTypeMessage(3);
							response.setCode(false);
							return response;
						}
					}
					if (uploadFile.exists() ) {
						uploadFile.delete();
					}
				} catch (Exception ex) {
					// TODO: cambiar dinámicamente / añadir a bitacora
					response.setTitle("Error al cargar Documento");
					response.setMessage("No se pudo cargar documento al SFTP");
					response.setTypeMessage(3);
					response.setCode(false);
					System.out.println("Ocurrió un error: " + ex.getMessage());
					return response;
				}

				// Registrar en BD
				try {
					controlDocumento.setFechaHoraCarga(new Date());
					controlDocumento.setNombreArchivo(uploadFile.getName());
					controlDocumento.setIdEstatusArchivo((short) 0);
					controlDocumento.setPeso((int) multiPart.getSize());
					controlDocumento.setRutaDocumento(sftpPath);
					controlDocumento.setActivo(true);
					controlDocumento.setUsuario(sesion.getIdUser());

					serviceControlDocumento.save(controlDocumento);

				} catch (Exception ex) {
					// TODO: cambiar dinámicamente / añadir a bitacora
					response.setTitle("Error en BD");
					response.setMessage("No se pudo guardar el registro en la BD");
					response.setTypeMessage(3);
					response.setCode(false);
					System.out.println("Ocurrió un error: " + ex.getMessage());
					return response;
				}

				response.setTitle("OK");
				response.setMessage("Registro guardado correctamente");
				response.setTypeMessage(1);
				response.setCode(true);
				return response;
			}
		}

		response.setTitle("Error");
		response.setMessage("El archivo no se pudo cargar");
		response.setTypeMessage(3);
		response.setCode(false);
		return response;
	}

	@ResponseBody
	@GetMapping("/delete/{id}")
	public Generic delete(@PathVariable("id") Integer idCarga) {

		Generic response = new Generic();
		boolean borrado = false;
		boolean borradoSftp = true;
		try {
			
			ControlDocumento controlDocumento = this.serviceControlDocumento.getControlDocumento(idCarga);
			if (controlDocumento != null) {
				Documento documento = serviceDocumento.getById(controlDocumento.getDocumento().getIdDocumento());
				String sftpPath = documento.getRutaDocumento();
				String nombreArchivo = controlDocumento.getNombreArchivo();
				borradoSftp = this.serviceGateway.delete(sftpPath, nombreArchivo);
				
				
			}
			if (borradoSftp){
				borrado = serviceControlDocumento.logicDelete(idCarga);
			} else {
				response.setTitle("Error en Base de Datos");
				response.setMessage("No se pudo eliminar el registro por error en SFTP");
				response.setTypeMessage(3);
				response.setCode(false);
				return response;
			}
			
		} catch (Exception ex) {
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());
			return response;
		}

		if (!borrado) {
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

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

}
