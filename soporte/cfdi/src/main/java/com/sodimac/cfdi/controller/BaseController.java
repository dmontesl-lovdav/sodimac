package com.sodimac.cfdi.controller;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.sodimac.cfdi.entity.fiscal.menu.CatPerfilEntity;
import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.model.menu.MenuByUsuario;
import com.sodimac.cfdi.service.CatalogoService;
import com.sodimac.cfdi.service.MenuService;

@Controller
public class BaseController {
	
	
	@Value("${cfdiVersion}")
	private String version;
	
	@Autowired
	private MenuService menuService;
	
//	@Autowired
//	private WsAdminRestClientService wsAdminRestClientService;
	
//	@Autowired
//	private ConfiguracionService configuracionService;
	
	@Autowired
	private CatalogoService catalogoService;
	

	protected void getModelAttributes(Model model, HttpServletRequest request, String Action, String url) {
		//ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> responseGet = configuracionService.consultaConfDatosEmisorTiendaAll();
		//ClientResponseTYPE<String> responseCreate = configuracionService.consultaConfDatosEmisorTiendaCreate(responseGet.getData().get(0));
		//ClientResponseTYPE<String> responseUpdate = configuracionService.consultaConfDatosEmisorTiendaUpdate(responseGet.getData().get(0));
		
		//ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> responseGet = wsAdminRestClientService.get("URL_FINDALL", new ArrayList<ConfDatosEmisorTiendaDtoVM>());
		//ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> responseGet = WsAdminRestClient.get(null,  new ArrayList<ConfDatosEmisorTiendaDtoVM>());
		try {
			//ClientResponseTYPE<String> responsePost = WsAdminRestClient.post(null,  "", responseGet.getData().get(0)); //, responseGet.getData().get(0));
		} catch (Exception e) {
			System.out.println();
		}
		
		HttpSession session = request.getSession();
		VMLogin responseLogin = (VMLogin) session.getAttribute("usuario");
		
		String contextPath = request.getContextPath();
		MenuByUsuario menu = menuService.getMenuByUser(responseLogin.getUser().getIdUsuario(),Action, contextPath);
		model.addAttribute("version", version);
		model.addAttribute("usuario", responseLogin.getUser().getNombre() + " " + responseLogin.getUser().getApellidoP());
		model.addAttribute("menu", menu.getLstMenuItem());
		model.addAttribute("perfiles", menu.getPerfiles());
		session.setAttribute("paginas", menu.getPaginas());
		session.setAttribute("url", url);
		//return validaAcceso(menu.getPaginas().toString(), url);
		//model.addAttribute("menu", menuService.getMenuByPerfil(responseLogin.getUser().getIdPerfil(),Action, contextPath));
	
		List<CatPerfilEntity> datos = new ArrayList <CatPerfilEntity>();
		datos = catalogoService.getPerfiles();		
		model.addAttribute("catPerfiles", datos);
	}
	

	
}
