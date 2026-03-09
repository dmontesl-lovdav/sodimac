package com.sodimac.cfdi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sodimac.cfdi.model.login.VMLogin;
import com.sodimac.cfdi.models.MultipleModalResponse;
import com.sodimac.cfdi.models.ProcesoDescargaModel;
import com.sodimac.cfdi.service.DescargaService;
import com.sodimac.cfdi.service.MenuService;

@Controller
@RequestMapping("/descarga")
public class DescargaController extends BaseController {

	Logger logger = LoggerFactory.getLogger(ReportesController.class);
	
	@Value("${cfdiVersion}")
	private String version;
	
	MultipleModalResponse multipleModalResponse;
	
	@Autowired
	private DescargaService descargaService;
	
	
	@GetMapping("/index")
	public String consultarDescargas(Model model, HttpServletRequest request) {
		getModelAttributes(model, request, "", "/descarga/index");
		return "/descarga";
//		HttpSession session = request.getSession();
//		
//		if(session == null || session.getAttribute("usuario") == null) {
//			return "redirect:/index";
//		} else {		
//			if (getModelAttributes(model, request, "", "/descarga/index")) {
//				return "/descarga";
//			} else {
//				return "redirect:/inicio";
//			}
//		}
	}
	
	@GetMapping("/listarDescarga")
	public String listarTablero(
			  @RequestParam(value = "dateDesde", required=true) String dateDesde
			, @RequestParam(value = "dateHasta", required=true) String dateHasta
			, @RequestParam(value = "pageNumber", required=true) String pageNumber
			, @RequestParam(value = "start", required=true) int start
			, @RequestParam(value = "rowsPerPage", required=true) int rowsPerPage
			, @RequestParam(value = "idEjecucion") String idEjecucion
			, @RequestParam(value = "estatus") String estatus
			, Model theModel) {

		List<ProcesoDescargaModel> datos = new ArrayList <ProcesoDescargaModel>();
		multipleModalResponse = new MultipleModalResponse(true,"", null);

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
			
			datos = descargaService.obtenerDescargaByParams(dateDesdeParse, dateHastaParse, start, rowsPerPage, idEjecucion, estatus);
			
			theModel.addAttribute("listDescarga", datos);
			if (datos.size() <= 0) {
				return "fragments/descargaList :: noResults";
			} else {
				return "fragments/descargaList :: fragmentTable";
			}
			
		} catch (Exception e) {
			logger.error("listarDescarga ", e);
		}
		return "";
				
	}
	
	
	@RequestMapping("/descarga/descargarArchivo/{file}") 
	public void DescargarArchivo( @PathVariable("file") String idEjecucion, HttpServletResponse response) throws Exception {
	  
		String idEjecucionTemp =  idEjecucion.substring(0,13);
		
		response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=download.zip");
        response.setStatus(HttpServletResponse.SC_OK);
        String[] listArchivos = null;
		try  {
			ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream());
			String archivos = descargaService.obtenerNombreArchivosByIdEjecucion(idEjecucionTemp);
			
			listArchivos = archivos.split(",");
			
            for (String archivo : listArchivos) {
                FileSystemResource resource = new FileSystemResource(archivo);

                ZipEntry e = new ZipEntry(resource.getFilename());
                e.setSize(resource.contentLength());
                e.setTime(System.currentTimeMillis());
                zippedOut.putNextEntry(e);
                StreamUtils.copy(resource.getInputStream(), zippedOut);
                zippedOut.closeEntry();
            }
            zippedOut.finish();
            
            //procesoDespuesDescarga(idEjecucionTemp,true,listArchivos);
            
        } catch (Exception e) {
        	e.printStackTrace();
        	descargaService.updateStatusProceso(idEjecucion, 4, "Problema al descargar", null);
        }
		
	}
	
	
	void validarDatosMultiple (String dateDesde, String dateHasta, String pageNumber) {

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
