package com.sodimac.cfdi.controller;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.model.puntosces.CatalogoDto;
import com.sodimac.cfdi.model.puntosces.PolizaContableDto;
import com.sodimac.cfdi.model.puntosces.PolizasFilterDto;
import com.sodimac.cfdi.service.PolizaContableService;


@Controller
@RequestMapping("/polizascontables")
public class PolizaContableController extends BaseController {
	
	@Autowired
	private PolizaContableService polizaContableService;
	
	private VMLogin responseLogin;
	
	PolizasFilterDto ultimaBusqueda;
	
	@GetMapping("/index")
	public String viewIndexPolizasContables(Model model, HttpServletRequest request) {
		
		getModelAttributes(model, request, "", "/paramAdmin/index");
		
		HttpSession session = request.getSession();

		if (session == null || session.getAttribute("usuario") == null) {
			return "redirect:/index";
		} else {
			responseLogin = (VMLogin) session.getAttribute("usuario");

			model.addAttribute("usuario",responseLogin.getUser().getNombre() + " " + responseLogin.getUser().getApellidoP());
			
			model.addAttribute("polizacontable", new PolizaContableDto());
			
			List<Object[]> estados = new ArrayList<Object[]>();
			estados.add(new Object[] { "1", "Activo" });
			estados.add(new Object[] { "0", "Inactivo" });
		
			model.addAttribute("estados", estados);

			Map<String, List<CatalogoDto>> catalogs = polizaContableService.getCatalogos();
			
			catalogs = polizaContableService.getCatalogos();
			
			model.addAttribute("catalogs", catalogs);
			
			return "/polizascontables";
		}
		
	}
	
	@GetMapping("/listarPolizas")
	public String listarParametros(Model model, PolizasFilterDto request) {
		request.setEstatus(1); // Listar solo las activas
		List<PolizaContableDto> params = polizaContableService.findParameters(request);
		ultimaBusqueda = request;
		model.addAttribute("listPolizas", params);

		return "fragments/polizasList :: fragmentTable";

	}

	@PostMapping("/guardarPoliza")
	public String guardarPoliza(PolizaContableDto model, @RequestParam("action") String action, HttpServletResponse response) throws Exception {
		model.setUsuario(responseLogin.getIdUser());
		String message = polizaContableService.guardarPolizaContable(model, action.equals("newPoliza"));
		if(message!=null) {
			response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
		}
		
		return "fragments/polizasList :: fragmentTable";

	}
	
	@PostMapping("/eliminarPoliza")
	public String eliminarPoliza(@RequestParam(value = "idConfigContable", required = true) String idConfigContable) {
		
		polizaContableService.eliminarPolizaContable(idConfigContable);
		
		return "fragments/polizasList :: fragmentTable";
	}

	@GetMapping("/listarParametros/toExcel")
	public ResponseEntity<byte[]> generarExcel() {

		try {

			ByteArrayOutputStream file = (ByteArrayOutputStream) polizaContableService.getExcel(ultimaBusqueda);

			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			String nombreArchivo = "Reporte_Polizas_Contables_" + df.format(new Date()).replaceAll(" ", "") + ".xlsx";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("filename", nombreArchivo);

			// Return the response entity with byte array and headers
			return new ResponseEntity<>(file.toByteArray(), headers, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}
