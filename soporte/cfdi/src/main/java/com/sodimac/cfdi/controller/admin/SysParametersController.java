package com.sodimac.cfdi.controller.admin;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sodimac.cfdi.controller.BaseController;
import com.sodimac.cfdi.model.admin.PrivilegiosUsuario;
import com.sodimac.cfdi.model.admin.SysParameter;
import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.service.admin.SysParametersService;

@Controller
@RequestMapping("/paramAdmin")
public class SysParametersController extends BaseController {

	@Autowired
	private SysParametersService service;
	
	private VMLogin responseLogin;
	private PrivilegiosUsuario privsUsuario;

	private static String ultimaBusqueda;
	public static final int SUPERVISOR_PAGOS = 9;

	@GetMapping("/index")
	public String viewIndexAdminParametros(Model model, HttpServletRequest request) {
		
		getModelAttributes(model, request, "", "/paramAdmin/index");
		
		HttpSession session = request.getSession();

		if (session == null || session.getAttribute("usuario") == null) {
			return "redirect:/index";
		} else {
			responseLogin = (VMLogin) session.getAttribute("usuario");

			model.addAttribute("usuario",responseLogin.getUser().getNombre() + " " + responseLogin.getUser().getApellidoP());

			model.addAttribute("parameter", new SysParameter());

			privsUsuario = service.getPrivilegiosUsuario(responseLogin.getIdUser());
			List<Object[]> aplicaciones = service.getAllAplicaciones();
			
			if (responseLogin.getIdRol()==SUPERVISOR_PAGOS) {
				aplicaciones.remove(0);
				aplicaciones.remove(0);
				aplicaciones.remove(0);
			}
			
			List<Object[]> roles = service.getAllRoles();
			List<Object[]> tiposParametro = new ArrayList<Object[]>();
			tiposParametro.add(new Object[] { "1", "Aplicación" });
			tiposParametro.add(new Object[] { "2", "Batch" });
			tiposParametro.add(new Object[] { "3", "Web Service" });
			tiposParametro.add(new Object[] { "4", "ETL" });
			tiposParametro.add(new Object[] { "5", "Procedimiento" });

			List<Object[]> estados = new ArrayList<Object[]>();
			estados.add(new Object[] { "1", "Activo" });
			estados.add(new Object[] { "0", "Inactivo" });

			model.addAttribute("roles", roles);
			model.addAttribute("aplicaciones", aplicaciones);
			model.addAttribute("tiposParametro", tiposParametro);
			model.addAttribute("estados", estados);

			request.getSession().setAttribute("privsUsuario", privsUsuario);

			return "/adminParametros";
		}
	}

	@GetMapping("/listarParametros")
	public String listarParametros(Model model, @RequestParam(value = "sparam", required = true) String sparam) {

		List<SysParameter> params = service.findParameters(sparam, responseLogin.getIdUser(), responseLogin.getIdRol());
		model.addAttribute("listParametros", params);
		ultimaBusqueda = sparam;

		return "fragments/parametrosList :: fragmentTable";

	}

	@PostMapping("/guardarParametro")
	public String guardarParametro(SysParameter model, @RequestParam("action") String action) throws Exception {
		model.setAccion(action);
		model.setIdGrupoUsuario(responseLogin.getUser().getIdPerfil());
		service.guardarParametro(model, responseLogin.getUser());
		return "fragments/parametrosList :: fragmentTable";

	}

	@GetMapping("/listarParametros/toExcel")
	public ResponseEntity<byte[]> generarExcel() {

		try {

			ByteArrayOutputStream file = (ByteArrayOutputStream) service.getExcel(ultimaBusqueda, responseLogin.getIdUser(), responseLogin.getIdRol());

			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			String nombreArchivo = "Reporte_Parametros_" + df.format(new Date()).replaceAll(" ", "") + ".xlsx";

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
