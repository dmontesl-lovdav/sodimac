package com.sodimac.cfdi.controller.wsadministracion;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sodimac.cfdi.cliente.wsadministracion.CatCodigoPostalDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.CatTipoTiendaDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorDtoVM;
import com.sodimac.cfdi.cliente.wsadministracion.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.cfdi.controller.BaseController;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.service.ConfiguracionService;

@Controller
@RequestMapping("/wsadministracion")
public class WsAdmnistracionController extends BaseController {
	
	private Logger logger = LoggerFactory.getLogger(WsAdmnistracionController.class);
	
	@Autowired
	private ConfiguracionService configuracionService;
	
	@Value("${cfdiVersion}")
	private String version;
	
	@GetMapping("/index")
	public String index(Model model, HttpServletRequest request, @RequestParam(name="accionPrevia",required=false) String accionPrevia
			, @RequestParam(name="success",required=false) String success) {
		HttpSession session = request.getSession();
		getModelAttributes(model, request, "", "/wsadministracion/index");
		ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> response = configuracionService.consultaConfDatosEmisorTiendaAll();
		if(response.getRespuesta().getCodigo().equals("1")) {
			model.addAttribute("confDatosEmisorTienda", response.getData());
			
		} else {
			model.addAttribute("confDatosEmisorTienda", new ArrayList<List<ConfDatosEmisorTiendaDtoVM>>());
			
		}
		
		if(success != null) {
			model.addAttribute("modalMsg", session.getAttribute("ucMsg"));
		} 
		
		return "/catalogos/wsadministracion";
	}
	
	@PostMapping("/update")
	public String update(Model model, HttpServletRequest request, ConfDatosEmisorTiendaDtoVM item, RedirectAttributes redirectAttributes
			,@RequestParam(value="action", required=true) String action) {
		HttpSession session = request.getSession();
		getModelAttributes(model, request, "", "/wsadministracion/index");
		ClientResponseTYPE<String> response = null;
		String accionPrevia = "";
		String responseReturn = null;
		String msg = "";
		if(action.equals("create")) {
			 response = configuracionService.consultaConfDatosEmisorTiendaCreate(item);
			 accionPrevia = "create";
		} else {
			 response = configuracionService.consultaConfDatosEmisorTiendaUpdate(item);
			 accionPrevia = "update";
		}
		
		if(response.getRespuesta().getCodigo().equals("1")) {
			model.addAttribute("confDatosEmisorTienda", response.getData());
			responseReturn = "redirect:/wsadministracion/index?accionPrevia=" + accionPrevia  + "&success=true";
			session.setAttribute("ucMsg", (accionPrevia.equals("update") ? "Actualización " : "Creación ") + "exitosa para la tienda " + item.getIdTienda());

		} else {
			model.addAttribute("confDatosEmisorTienda", new ArrayList<List<ConfDatosEmisorTiendaDtoVM>>());
			responseReturn = "redirect:/wsadministracion/index?accionPrevia=" + accionPrevia + "&success=false";
			session.setAttribute("ucMsg", "Error al " + (accionPrevia.equals("update") ? "actualizar " : "crear ")  + "la tienda:" +  " ERROR: " + response.getRespuesta().getDescripcion());

		}
		return responseReturn;
	}
	

	@GetMapping("/findById/{id}/{action}")
	public String findById(Model model, HttpServletRequest request, @PathVariable("id") int id, @PathVariable(name="action", required=false) String action) {
		ConfDatosEmisorTiendaDtoVM item = null;
		ClientResponseTYPE<List<CatTipoTiendaDtoVM>> catTipoTienda = configuracionService.catTipoTiendaFindAll();
		ClientResponseTYPE<List<ConfDatosEmisorDtoVM>> datosEmisor = configuracionService.consultaConfDatosEmisorAll();
		if(action.equals("UPDATE")) {
			ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> response = configuracionService.consultaConfDatosEmisorTiendaAll();
			item = response.getData().stream().filter(p -> p.getId() == id).collect(Collectors.toList()).get(0);
			//item = response.getData().stream().filter(p -> p.getEmisor() == rfc).collect(Collectors.toList()).get(0);
			model.addAttribute("readonly", "readonly");
		} else { //CREATE
			item = new ConfDatosEmisorTiendaDtoVM();
			item.setId(0);
			model.addAttribute("readonly", "");
		}
		model.addAttribute("confDatosEmisorTiendaEdit", item);
		model.addAttribute("catTipoTienda", catTipoTienda.getData());
		model.addAttribute("datosEmisor", datosEmisor.getData());
		return "fragments/wsadministracion/catConfDatosEmisorTienda :: fragmentEdit";
	}
	
	private String getComputerName()
	{
		Enumeration e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(e.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) e.nextElement();
		    Enumeration ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		        InetAddress i = (InetAddress) ee.nextElement();
		        System.out.println(i.getHostAddress());
		    }
		}
		
	    Map<String, String> env = System.getenv();
	    if (env.containsKey("COMPUTERNAME"))
	        return env.get("COMPUTERNAME");
	    else if (env.containsKey("HOSTNAME"))
	        return env.get("HOSTNAME");
	    else
	        return "Unknown Computer";
	}

	@GetMapping("/findByCP/{id}")
	@ResponseBody
	public ClientResponseTYPE<CatCodigoPostalDtoVM> findByCP(Model model, HttpServletRequest request, @PathVariable("id") int id) {
		ClientResponseTYPE<CatCodigoPostalDtoVM> response = configuracionService.consultaCodigoPostalById(id);
		return response;
	}

}
