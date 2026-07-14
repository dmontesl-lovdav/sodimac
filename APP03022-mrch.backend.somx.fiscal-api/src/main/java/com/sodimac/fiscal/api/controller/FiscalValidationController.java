package com.sodimac.fiscal.api.controller;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.sodimac.fiscal.api.response.ResponseHandler;
import com.sodimac.fiscal.api.service.ValidarXmlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * ============================================================================
 * CONTROLADOR BFF: Validación de XML Fiscales
 * ============================================================================
 * Valida documentos XML fiscales contra las especificaciones del SAT
 * utilizando el PAC configurado (primario o secundario en caso de fallo).
 *
 * RUTA BASE: /validacion
 * FUNCIONALIDAD: Validación con PAC (DETECNO/CARBAJAL)
 *
 * PROCESO DE VALIDACIÓN:
 * 1. Recibe archivo XML multipart
 * 2. Valida parámetros (file, pais, rfc)
 * 3. Intenta validar con PAC primario
 * 4. Si falla, intenta con PAC secundario (failover)
 * 5. Retorna resultado de validación
 *
 * @author jlopez
 */
@RestController
@RequestMapping()
@Tag(name = "XML Validacion", description = "Validacion de XML de factura")
public class FiscalValidationController {

	private static final Logger logger = LoggerFactory.getLogger(FiscalValidationController.class);


	@Autowired
	private ValidarXmlService validarXmlService;
	
	@GetMapping("/get")
	public String getTest() {
		
		return "Conectado";
	}
	
    @Operation(summary = "Subir archivo xml para su validacion", description = "Hace la validacion del factura en el pac configurado como primario en caso contrario se va con el secundario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "XML validado y sin errores"),
            @ApiResponse(responseCode = "400", description = "XML validado con error en el formato")
    })
    @PostMapping(value = "/validacion/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file
    										, @RequestParam("pais") String pais
    										, @RequestParam("rfc") String rfc
    										) 
    												throws IOException, SAXException, ParserConfigurationException {
        logger.info("Start request validation");
        
        if (validaParametros(file, pais, rfc)) {
        	ResponseEntity<Object> response = null;
			response = validarXmlService.validaXmlFiscal(file, 1);
            logger.info("request successfully executed");
            return response;
        } else {
			return ResponseHandler.responseBuilderError("ERROR: Validation " + ". ERROR: file or pais or rfs is empty or file is not xml file." , HttpStatus.BAD_REQUEST, rfc, null, null);
        }
        
    }

	private Boolean validaParametros(MultipartFile file, String pais, String rfc) {
		if(file.isEmpty() || pais == null || pais.isBlank() || pais.isEmpty() || 
        		rfc == null || rfc.isBlank() || rfc.isEmpty()) {
            logger.warn("file or pais or rfs is empty");
            return false;
        }
		if(!validaExtension(file)) {
			 logger.warn("file is not xml file");
			 return false;
		}
		return true;
	}

	private boolean validaExtension(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
	    String fileExtension = "";
	    if (originalFilename != null && originalFilename.contains(".")) {
	        int dotIndex = originalFilename.lastIndexOf('.');
	        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) { // Ensure dot is not the first or last character
	            fileExtension = originalFilename.substring(dotIndex + 1);
	        }
	    } else {
	    	return false;
	    }
		return fileExtension.equals("xml");
	}
    

}
