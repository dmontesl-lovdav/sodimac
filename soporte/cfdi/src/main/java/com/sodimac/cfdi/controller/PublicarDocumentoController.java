package com.sodimac.cfdi.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jcraft.jsch.SftpException;
import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.models.documento.ControlDocumento;
import com.sodimac.cfdi.models.documento.DocumentoPublicadoModel;
import com.sodimac.cfdi.models.documento.Generic;
import com.sodimac.cfdi.service.documento.CatTipoDocumentoService;
import com.sodimac.cfdi.service.documento.DocumentoPublicadoService;
import com.sodimac.cfdi.util.MemoryUtil;

@Controller
@RequestMapping("/publicar")
public class PublicarDocumentoController extends BaseController {
	
	private Logger logger = LoggerFactory.getLogger(PublicarDocumentoController.class);
	
	@Autowired
	private CatTipoDocumentoService catTipoDocumentoService;
	
	@Autowired
	private DocumentoPublicadoService documentoPublicadoService;
	
	private MultipleModalResponse multipleModalResponse;
	
	@GetMapping("/index")
	public String consultarDocumentos(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/publicar/index");
		logger.info("Menu publicar documento");
		return "/publicar";
	}
	
	@PostMapping("/tiposDocumento")
	@ResponseBody
	public String listaTiposDocumento() {
		return this.catTipoDocumentoService.getTiposDocumentoGson();
	}
	
	@GetMapping("/listar/documentos")
	public String listarDocumentos(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam("tipoDocumento") String tipoDocumento
			, Model theModel) {

		List<DocumentoPublicadoModel> listDocumentos = new ArrayList<>();
		this.multipleModalResponse = new MultipleModalResponse(true,"", null);
		int idTipoDocumento = 0; 
		
		try {
			validarDatosMultiple(dateDesde, dateHasta, pageNumber);
			
			if (!multipleModalResponse.isSuccess()) {
				return "Datos invalidos";
			}

			start = (Integer.parseInt(pageNumber) * rowsPerPage) - rowsPerPage;
			if (start < 0) {
				start = 0;
			}
			
			String[] partsDesde = dateDesde.split("/");
			String[] partsHasta = dateHasta.split("/");
			String dateDesdeParse = partsDesde[2] + "-" + partsDesde[1] + "-" + partsDesde[0];
			String dateHastaParse = partsHasta[2] + "-" + partsHasta[1] + "-" + partsHasta[0];
			
			if (tipoDocumento != null && !tipoDocumento.isEmpty()) {
				idTipoDocumento = Integer.valueOf(tipoDocumento).intValue(); 
			}
			
			listDocumentos = this.documentoPublicadoService.getDocumentosPublicados(dateDesdeParse, dateHastaParse, idTipoDocumento);
			
			
			theModel.addAttribute("listDocumentos", listDocumentos);
			if (listDocumentos.size() <= 0) {
				return "fragments/publicarList :: noResults";
			} else {
				return "fragments/publicarList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("/listar/documentos ", e);
		}
		return "";
				
	}
	
	/**
	 * Request POST, return Control Documento List Paginate.
	 * 
	 * @throws IOException
	 * @throws SftpException
	 */
	@ResponseBody
	@PostMapping(value = "/create")
	public String saveControlDocumento(
			  HttpServletRequest request
			, ControlDocumento controlDocumento
			, @RequestParam(value = "archivo", required=true) MultipartFile multiPart) throws IOException, SftpException {
			//, @RequestParam(value = "idTipoDocumento", required=true) Integer idTipoDocumento) throws IOException, SftpException {
		
		Gson gson= new Gson();
		JsonObject clienteJson = new JsonObject();
		HttpSession session = request.getSession();
		VMLogin responseLogin = (VMLogin) session.getAttribute("usuario");
		logger.info("responseLogin.getIdUser(): " + responseLogin.getIdUser() );
		Integer idTipoDocumento = Integer.valueOf( controlDocumento.getListTipoDocumentoModal().toString() );
		MemoryUtil.showMemoryStats();
		Generic response = this.documentoPublicadoService.createDocument(multiPart, idTipoDocumento, responseLogin.getIdUser());
		clienteJson.addProperty("STATUS", response.getStatus());
		clienteJson.addProperty("MSG_ERROR", response.getMessage());
		MemoryUtil.showMemoryStats();
		return gson.toJson(clienteJson);
	}
	
	@PostMapping("/borrarArchivo")
	@ResponseBody
	public String borrarArchivo(
			  @RequestParam("idDocumentoPublicado") String idDocumentoPublicado
			, Model theModel) {
		
		Gson gson= new Gson();
        JsonObject clienteJson = new JsonObject();
		String estatus = this.documentoPublicadoService.borrarArchivo(Integer.valueOf(idDocumentoPublicado));
		clienteJson.addProperty("STATUS", estatus);
		return gson.toJson(clienteJson);
	}
	
	private void validarDatosMultiple (String dateDesde, String dateHasta, String pageNumber) {

		if (dateDesde=="" || dateHasta=="" || pageNumber=="") {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);
		} 

		if (dateDesde.length() != 10) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);		
		}
	
		if (dateHasta.length() != 10) {
			multipleModalResponse.setMessage("Datos invalidos");
			multipleModalResponse.setSuccess(false);		
		}		

	}	
}
