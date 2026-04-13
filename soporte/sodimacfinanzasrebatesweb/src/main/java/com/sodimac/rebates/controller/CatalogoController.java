package com.sodimac.rebates.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.rebates.model.Catalogo;
import com.sodimac.rebates.model.Generic;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.ICatalogosService;

@Controller
@RequestMapping("/catalogos")
public class CatalogoController extends BaseController {
	
	@Autowired
	private ICatalogosService catalogosService;
	
	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);
		
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

		model.addAttribute("fechaInicio", fechaInicioMax);
		model.addAttribute("fechaFinal", fechaFinalMax);

		return "catalogos";
	}
	
	@PostMapping("/consult")
	public String consult(@RequestBody Catalogo catalogo, BindingResult result, Model model) {
		
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			// TODO: cambiar dinámicamente / añadir a bitacora
			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/catalogos :: start";
		}
		
		List<Catalogo> lista = new ArrayList<Catalogo>();
		
		if(catalogo.getIdCatalogo() == null && Strings.isBlank(catalogo.getNombre()) && Strings.isBlank(catalogo.getDescripcion())) {
			lista = catalogosService.findAll();
		} else {
			lista = catalogosService.findByParams(catalogo);
		}
		
		if(lista.isEmpty()) {
			return "fragments/catalogos :: noRecord";
		} else {
			model.addAttribute("lista", lista);
			return "fragments/catalogos :: table";
		}
		
	}
	
	@ResponseBody
	@PostMapping(value = "/create")
	public Generic saveCatalogo(@RequestBody Catalogo catalogo) {
		Generic response = new Generic();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		try {
			if(!catalogosService.existeCatalogo(catalogo.getNombre())) {
				catalogo.setUsuarioCreacion(sesion.getIdUser());
				catalogosService.addCatalogo(catalogo);
			} else {
				response.setTitle("Catalogo existente");
				response.setMessage("Ya existe un catalogo registrado con ese nombre");
				response.setTypeMessage(2);
				response.setCode(false);
			}
			
			response.setTitle("OK");
			response.setMessage("Registro guardado correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch(Exception e) {
			e.printStackTrace();
			
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo guardar el registro en la BD");
			response.setTypeMessage(3);
			response.setCode(false);
		}
		

		return response;
	}
	
	@ResponseBody
	@PostMapping(value = "/edit")
	public Generic editCatalogo(@RequestBody Catalogo catalogo) {
		Generic response = new Generic();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		try {
			if(catalogosService.existeCatalogo(catalogo.getIdCatalogo())) {
				catalogo.setUsuarioActualizacion(sesion.getIdUser());
				catalogosService.editCatalogo(catalogo);
			} else {
				response.setTitle("Catálogo no existe");
				response.setMessage("El catálogo que desea modificar no existe");
				response.setTypeMessage(2);
				response.setCode(false);
			}
			
			response.setTitle("OK");
			response.setMessage("Registro editado correctamente");
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
	@GetMapping("/delete/{id}")
	public Generic delete(@PathVariable("id") Integer idCatalogo) {
		Generic response = new Generic();

		try {
			if(catalogosService.existeCatalogo(idCatalogo)) {
				catalogosService.logicDelete(idCatalogo);
			} else {
				response.setTitle("Catálogo no existe");
				response.setMessage("El catálogo que desea modificar no existe");
				response.setTypeMessage(2);
				response.setCode(false);
			}
	
			response.setTitle("OK");
			response.setMessage("Registro eliminado correctamente");
			response.setTypeMessage(1);
			response.setCode(true);
		} catch (Exception ex) {

			// TODO: cambiar dinámicamente / añadir a bitacora
			response.setTitle("Error en Base de Datos");
			response.setMessage("No se pudo eliminar el registro");
			response.setTypeMessage(3);
			response.setCode(false);
			System.out.println(ex.getMessage());

			return response;
		}
		return response;
	}
}
