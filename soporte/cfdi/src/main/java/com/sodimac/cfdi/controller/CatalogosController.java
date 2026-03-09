package com.sodimac.cfdi.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sodimac.cfdi.entity.fiscal.menu.CatPerfilEntity;

import com.sodimac.cfdi.service.CatalogoService;


@Controller
public class CatalogosController extends BaseController {
	
	private Logger logger = LoggerFactory.getLogger(CatalogosController.class);

	@Autowired
	private CatalogoService catalogoService;
	
	@GetMapping("/catalogos/obtenerCatalogos")
	public String getCatalgos(Model theModel) {
		List<CatPerfilEntity> datos = new ArrayList <CatPerfilEntity>();
		try {
		
			datos = catalogoService.getPerfiles();		
			theModel.addAttribute("catPerfiles", datos);
			if (datos.size() <= 0) {
				return "fragments/perfilList :: noResults";
			} else {
				return "fragments/perfilList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("getCatalgos ", e);
		}
		return "";
	}
}
