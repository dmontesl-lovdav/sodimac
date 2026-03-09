package com.sodimac.cfdi.controller.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sodimac.cfdi.controller.BaseController;
import com.sodimac.cfdi.entityFactura.ClientesEntity;
import com.sodimac.cfdi.entityFactura.ClientesTemporalEntity;
import com.sodimac.cfdi.entityFactura.FacturasEntity;
import com.sodimac.cfdi.model.ClientResponseTYPE;
import com.sodimac.cfdi.model.admin.PrivilegiosUsuario;
import com.sodimac.cfdi.model.admin.SysParameter;
import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.models.ClientesTemporalModel;
import com.sodimac.cfdi.models.factura.IncidentesTimbradoModel;
import com.sodimac.cfdi.repositoryFactura.ClientesFacturaRepository;
import com.sodimac.cfdi.repositoryFactura.ClientesTemporalRepository;
import com.sodimac.cfdi.repositoryFactura.FacturasFacturaRepository;
import com.sodimac.cfdi.service.FacturasService;
import com.sodimac.cfdi.service.SeguridadService;
import com.sodimac.cfdi.service.admin.IncidentesTimbradoService;
import com.sodimac.cfdi.service.admin.SysParametersService;

@Controller
@RequestMapping("/incidenciasTimbrado")
public class IncidenciasTimbradoController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(IncidenciasTimbradoController.class);
	
	@Autowired
	private IncidentesTimbradoService service;
	
	@Autowired
	private SysParametersService serviceParam;

	@Autowired
	private SeguridadService seguridadService;

	@Autowired
	private FacturasService facturasService;
	
	@Autowired
	FacturasFacturaRepository facturasFacturaRepository;

	@Autowired
	ClientesFacturaRepository clientesFacturaRepository;

	@Autowired
	ClientesTemporalRepository clientesTemporalRepository;

	private VMLogin responseLogin;
	private PrivilegiosUsuario privsUsuario;

	@GetMapping("/index")
	public String viewIndexAdminParametros(Model model, HttpServletRequest request) {
		
		getModelAttributes(model, request, "", "/incidenciasTimbrado/index");
		
		HttpSession session = request.getSession();

		if (session == null || session.getAttribute("usuario") == null) {
			return "redirect:/index";
		} else {
			responseLogin = (VMLogin) session.getAttribute("usuario");

			model.addAttribute("usuario",responseLogin.getUser().getNombre() + " " + responseLogin.getUser().getApellidoP());
			model.addAttribute("parameter", new SysParameter());

			privsUsuario = serviceParam.getPrivilegiosUsuario(responseLogin.getIdUser());

			request.getSession().setAttribute("privsUsuario", privsUsuario);
			
			Integer idRol = responseLogin.getIdRol();
			model.addAttribute("idRol", idRol);

			return "/incidentesTimbradoPage";
		}
	}

	@GetMapping("/listarIncidentesTimbrado")
	public String listarIncidentesTimbrado(Model model, @RequestParam(value = "sparam", required = true) String ticket) {

		List<IncidentesTimbradoModel> params = service.findTicket(ticket, responseLogin.getIdUser());

		logger.info("=== DEBUG listarIncidentesTimbrado ===");
		logger.info("Ticket: " + ticket);
		logger.info("Cantidad de registros: " + params.size());

		int hayFactura = 0;
		int hayError = 0;
		int nivel = 0;
		if (params.size()> 0) {
			logger.info("UUID: " + params.get(0).getUuid());
			logger.info("MensajeError: " + params.get(0).getMensajeError());
			logger.info("Nivel: " + params.get(0).getNivel());

			if (!params.get(0).getUuid().isEmpty()) {
				hayFactura = 1;
			}
			if (!params.get(0).getMensajeError().isEmpty()) {
				hayError = 1;
			}
			nivel = params.get(0).getNivel();
		}

		Integer idRol = responseLogin.getIdRol();

		logger.info("hayFactura: " + hayFactura);
		logger.info("hayError: " + hayError);
		logger.info("nivel: " + nivel);
		logger.info("idRol: " + idRol);
		logger.info("======================================");

		model.addAttribute("listParametros", params);
		model.addAttribute("hayFactura", hayFactura);
		model.addAttribute("hayError", hayError);
		model.addAttribute("nivel", nivel);
		model.addAttribute("idRol", idRol);

		return "fragments/incidentesTimbradoList :: fragmentTable";

	}
	@ResponseBody
	@PostMapping(value="/eliminarTicket")
	public String eliminarTicket(@RequestBody String ticket,
			Model theModel, HttpServletRequest request) {
		
		try {
			service.deleteTicket(ticket);
		} catch (Exception e) {
			logger.error("ticket: " + ticket + ":", e);
		}
		return ticket;
	}

	@ResponseBody
	@PostMapping(value="/eliminarIntentos")
	public String eliminarIntentos(@RequestBody String ticket,
			Model theModel, HttpServletRequest request) {
		
		try {
			service.deleteIntentos(ticket);
		} catch (Exception e) {
			logger.error("ticket: " + ticket + ":", e);
		}
		return ticket;
	}
	
	@GetMapping("/ReenvioFactura")
	@ResponseBody
    public String reenviarFactura(@RequestParam(value = "id") String id,
    		                      @RequestParam(value = "eMailCC") String eMailCC) 
    {
		String result = "";
		String rfc = "";
		String razonSocial = "";
		String eMailTo = "";
		String ticket = "";
		String uuid = "";
		String xml = "";
		Integer idFacturaPac = 0;
		FacturasEntity factura = null;
		
		try{

			factura = facturasFacturaRepository.findByUuid(id);
			if (factura != null) {
				eMailTo = seguridadService.desencriptar(factura.getEmail());
				rfc = seguridadService.desencriptar(factura.getRfc());
				xml = seguridadService.desencriptar(factura.getXml());
				ticket = factura.getTicket();
				uuid = factura.getUuid();
				idFacturaPac = factura.getIdFacturaPac();

				String rfcEncrip = seguridadService.encriptar(rfc);
				ClientesEntity cliente = clientesFacturaRepository.findByRfc(rfcEncrip);
				if (cliente == null) {
					ClientesTemporalEntity clienteTmp = clientesTemporalRepository.findTop1ByRfcOrderByFechaCreacionDesc(rfcEncrip);
					if (clienteTmp != null) {
						razonSocial = seguridadService.desencriptar(clienteTmp.getRazonSocial());					
					}
				} else {
					razonSocial = seguridadService.desencriptar(cliente.getRazonSocial());
				}
			
			} else {
				return "error"; 
			}
			
			String destinatario = eMailTo;
			String destinatarioCC = eMailCC;
			
			ClientesTemporalModel model = new ClientesTemporalModel();
			model.setUuid( uuid );
			model.setXml( xml );
			model.setRfc( rfc );
			model.setEmail( destinatario );
			model.setEmailCC(destinatarioCC);
			model.setTicket( ticket );
			model.setIdFacturaPac( idFacturaPac );
			model.setRazonSocial( razonSocial );
			
			try {                        	
				ClientResponseTYPE<String> response = facturasService.enviarCorreoFactura(model);
				if (response.getRespuesta().getCodigo().equals("1")) {
					logger.info("Ticket enviado por correo: " + model.getTicket());
					result = "success";
				} else {
					logger.info("Ticket error:     " +  model.getTicket() + " (" + response.getRespuesta().getCodigo() + ")");
					result = "error";
				}
            } catch (Exception e) {
    			e.printStackTrace();
    			result = "error";
    		}
		} catch(Exception e) {
			e.printStackTrace();
			result = "error";
		}
		
		return result;
    }

}
