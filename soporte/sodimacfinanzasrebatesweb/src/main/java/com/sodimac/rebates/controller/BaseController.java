package com.sodimac.rebates.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.sodimac.rebates.model.Sesion;

@Controller
public class BaseController {
	
	@Value("${sodimac.rebates.version}")
	private String version;
	
	protected void getModelAttributes(Model model, Sesion sesion) {
		model.addAttribute("idUser", sesion.getIdUser() );
		model.addAttribute("usuario", sesion.getNombre() );
		model.addAttribute("perfiles", sesion.getPerfiles());
		model.addAttribute("version", version);
	}
}
