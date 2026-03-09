/**
 * 
 */
package com.sodimac.cfdi.controller;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sodimac.cfdi.client.ServiceLogin;
import com.sodimac.cfdi.dto.DtoUser;
import com.sodimac.cfdi.entity.fiscal.UsuariosEntity;
import com.sodimac.cfdi.model.ModelGeneric;
import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.service.LoginService;


@Controller
@RequestMapping("/")
public class LoginController extends BaseController {

	Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	ServiceLogin serviceLogin;

	@Autowired
	LoginService loginService;
	

	@Value("${cfdiVersion}")
	private String version;

	@GetMapping("/")
	public String index(Model model, HttpServletRequest request) {

		HttpSession session = request.getSession();
		if (session != null && session.getAttribute("validSession") != null && session.getAttribute("validSession").toString() == "true") {
			return "/consultarCfdi2";
		}

		model.addAttribute("version", version);
		return "index";
	}

	@GetMapping("/index")
	public String index2(Model model, HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		if (session != null && session.getAttribute("validSession") != null && session.getAttribute("validSession").toString() == "true") {
			return "/consultarCfdi2";
		}

		model.addAttribute("version", version);
		return "index";
	}

	@ResponseBody
	@PostMapping(value="/login")
	public ModelGeneric validarPassword(@RequestBody DtoUser dtoUser,
			Model model, HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException, KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, UnknownHostException, SocketException {

		VMLogin responseLogin = new VMLogin();

		String usuario = dtoUser.getUsuario().trim().toLowerCase();
		String password = dtoUser.getPassword().trim();
		dtoUser.setUsuario(usuario);
		dtoUser.setPassword(password);

		try {

			UsuariosEntity usuarioLogin = loginService.validarLogin(usuario, password);
			if (usuarioLogin != null) {
				dtoUser.setIdUsuario(usuarioLogin.getIdUsuario());;
				dtoUser.setNombre(usuarioLogin.getPrimerNombre());
				dtoUser.setApellidoP(usuarioLogin.getApellidoPaterno());
				dtoUser.setApellidoM(usuarioLogin.getApellidoMaterno());
				//dtoUser.setIdPerfil(usuarioLogin.getCatPerfilEntity().getId());
				dtoUser.setActivo(true);
				dtoUser.setCambiarPass(false);

				responseLogin.setCode(true);
				responseLogin.setTypeMessage("1");
				responseLogin.setMessageGlobal("Ok");
				responseLogin.setUser(dtoUser);
				responseLogin.setIdRol(usuarioLogin.getIdRol());
				responseLogin.setIdUser(usuarioLogin.getIdUsuario());
				//responseLogin.setStrMenu(GetMenu(loginService.obtenerOpcionesRol(usuarioLogin.getIdRol()), dtoUser));
				
			 } else {
				responseLogin.setCode(false);
				responseLogin.setTypeMessage("10");
				responseLogin.setMessageGlobal("Usuario y/o contraseña incorrectos");
			 }

		} catch (Exception e) {

			logger.error("validarPassword ", e);
			responseLogin.setCode(false);
			responseLogin.setMessageGlobal("Se ha producido un error");
			responseLogin.setTypeMessage("4");
		}

		if (!responseLogin.isCode()) {
			return responseLogin;
		}

		if (responseLogin.getUser().isCambiarPass()) {
			return responseLogin;
		}

		HttpSession session = request.getSession();
		session.setAttribute("validSession", "true");
		session.setAttribute("usuario", responseLogin);

		return responseLogin;
	}
	
	@GetMapping("/inicio")
	public String inicio(Model model, HttpServletRequest request) {	
		getModelAttributes(model, request, "LOGIN", "/inicio");
		return "/inicio";//"/consultarCfdi2";//consultarCfdi2
	}
	
	@GetMapping("/logout")
	public String logout(Model model, SessionStatus status, HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute("validSession", null);
		session.setAttribute("usuario", null);
		session.setAttribute("paginas", null);
		session.setAttribute("url", null);
		status.setComplete();
		model.addAttribute("version", version);
		return "index";
	}

	@GetMapping("/error")
	public String error() {
		return "redirect:/index";
	}
	
}
