package com.sodimac.wsconfiguracion.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorDto;
import com.sodimac.wsconfiguracion.service.catalogs.ConfDatosEmisorService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping({"/api/catologo/confdatosemisor"})
public class DatosEmisorController extends BaseController {
   Logger logger = LoggerFactory.getLogger(DatosEmisorController.class);
   @Autowired
   private ConfDatosEmisorService confDatosEmisorService;

   @GetMapping({"/findAll"})
   @ApiOperation(
      value = "Obtiene el catalogo de confdatosemisor",
      notes = ""
   )
   public ClientResponseTYPE<List<ConfDatosEmisorDto>> findAll() {
	   ClientResponseTYPE<List<ConfDatosEmisorDto>> confdatosemisor = 
			   new ClientResponseTYPE<List<ConfDatosEmisorDto>>(new ArrayList<ConfDatosEmisorDto>());

      try {
         this.registrarActividad(13, "Obtiene el catalogo de confdatosemisor ", "/api/catologo/confdatosemisortienda/findAll");
         confdatosemisor = this.confDatosEmisorService.findAll();
      } catch (Exception var3) {
         this.logger.error("Error al obtener el catalogo de confdatosemisor.  /api/catologo/confdatosemisor/findAll", var3);
         this.guardarLogerrores("/api/catologo/confdatosemisor/findAll", "Obtiene el catalogo de confdatosemisor.", var3);
         UtilsApi.setRespuesta(confdatosemisor.getRespuesta(), ECodigo.Error, var3.getMessage() != null ? var3.getMessage() : "");
      }

      return confdatosemisor;
   }
}
