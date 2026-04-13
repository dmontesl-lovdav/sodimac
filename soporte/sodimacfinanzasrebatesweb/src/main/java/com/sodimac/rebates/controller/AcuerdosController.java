package com.sodimac.rebates.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sodimac.rebates.dto.RebateAcuerdosDto;
import com.sodimac.rebates.model.ProgramaPago;
import com.sodimac.rebates.model.RebateAcuerdos;
import com.sodimac.rebates.model.Sesion;
import com.sodimac.rebates.service.IProgramaPagoService;
import com.sodimac.rebates.service.IRebateAcuerdosService;
import com.sodimac.rebates.util.ExportAcuerdosExcel;

@Controller
@RequestMapping("/acuerdos")
public class AcuerdosController extends BaseController {
	
	@Autowired
	private IProgramaPagoService programaPagoService;
	
	@Autowired
	private IRebateAcuerdosService acuerdosService;
	
	@GetMapping("/index")
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Sesion sesion = (Sesion) authentication.getPrincipal();
		getModelAttributes(model, sesion);
		
		List<ProgramaPago> listPagos = programaPagoService.getActive();
		List<String> listTipoAcuerdos = acuerdosService.findTiposAcuerdos();

		model.addAttribute("lista", null);
		model.addAttribute("listPagos", listPagos);
		model.addAttribute("listTipoAcuerdos", listTipoAcuerdos);

		return "acuerdos";
	}
	
	@PostMapping("/consult")
	public String consult(@RequestBody RebateAcuerdosDto request,
			BindingResult result, RedirectAttributes attributes, Model model) {
		
		if (request.getTipoAcuerdo() == null) {
			model.addAttribute("titulo", "Valida tipo de acuerdo");
			model.addAttribute("msg", "Seleccionar un tipo de acuerdo");
			model.addAttribute("tipo", 2);
			model.addAttribute("code", false);

			return "fragments/acuerdos :: start";			
		}
		
		List<RebateAcuerdos> list = new ArrayList<RebateAcuerdos>(); 
		
		if (result.hasErrors()) {

			for (ObjectError error : result.getAllErrors()) {

				System.out.println("Ocurrió un error: " + error.getDefaultMessage());
			}

			model.addAttribute("titulo", "Error desconocido");
			model.addAttribute("msg", "");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);

			return "fragments/acuerdos :: start";
		}
		
		try {
			list = acuerdosService.findAcuerdosConsultParams(request.getProveedor(), request.getRazonSocial(), request.getTipoAcuerdo(), request.getProgramaPago()) ;
			
		} catch (Exception ex) {
			model.addAttribute("titulo", "Error en Base de Datos");
			model.addAttribute("msg", "No se consiguió realizar la consulta");
			model.addAttribute("tipo", 3);
			model.addAttribute("code", false);
			model.addAttribute("lista", null);
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return "fragments/acuerdos :: noRecord";
		}
		
		if (list.size() >= 1) {
			model.addAttribute("lista", list);
			return "fragments/acuerdos :: table";
		}

		return "fragments/acuerdos :: noRecord";
		
	}
	
	@GetMapping("/report")
	public void download(HttpServletResponse response) throws IOException {

		String name_report = "Acuerdos Comerciales.xlsx";
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + name_report);
		
		List<RebateAcuerdos> dataReport = acuerdosService.findAll(); 

		ByteArrayInputStream stream = ExportAcuerdosExcel.acuerdosListToExcelFile(dataReport);
		IOUtils.copy(stream, response.getOutputStream());

	}
}
