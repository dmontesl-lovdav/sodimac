package com.sodimac.wsconfiguracion.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.wsconfiguracion.service.catalogs.ConfDatosEmisorTiendaService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping({"/api/catologo/confdatosemisortienda"})
public class DatosEmisorTiendaController extends BaseController {
   Logger logger = LoggerFactory.getLogger(DatosEmisorTiendaController.class);
   @Autowired
   private ConfDatosEmisorTiendaService confDatosEmisorTiendaService;

   @GetMapping({"/findAll"})
   @ApiOperation(
      value = "Obtiene el catalogo de confdatosemisortienda",
      notes = ""
   )
   public ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> findAll() {
      ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> confdatosemisortienda = 
    		  new ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>>(new ArrayList<ConfDatosEmisorTiendaDtoVM>());

      try {
         this.registrarActividad(10, "Obtiene el catalogo de confdatosemisortienda ", "/api/catologo/confdatosemisortienda/findAll");
         confdatosemisortienda = this.confDatosEmisorTiendaService.findAllVM();
      } catch (Exception var3) {
         this.logger.error("Error al obtener el catalogo de confdatosemisortienda.  /api/catologo/confdatosemisortienda/findAll", var3);
         this.guardarLogerrores("/api/catologo/confdatosemisortienda/findAll", "Obtiene el catalogo de confdatosemisortienda.", var3);
         UtilsApi.setRespuesta(confdatosemisortienda.getRespuesta(), ECodigo.Error, var3.getMessage() != null ? var3.getMessage() : "");
      }

      return confdatosemisortienda;
   }

   @PostMapping({"/update"})
   public ClientResponseTYPE<String> update(@RequestBody ConfDatosEmisorTiendaDtoVM request) {
      ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();

      try {
         this.registrarActividad(11, "Actualiza entidad de confdatosemisortienda ", "/api/catologo/confdatosemisortienda/update");
         response = this.confDatosEmisorTiendaService.update(request);
      } catch (Exception var4) {
         this.logger.error("Error al actualizar la entidad.  /api/catologo/confdatosemisortienda/update", var4);
         this.guardarLogerrores("/api/catologo/confdatosemisortienda/update", "Actualiza entidad de confdatosemisortienda", var4);
         UtilsApi.setRespuesta(response.getRespuesta(), ECodigo.Error, var4.getMessage() != null ? var4.getMessage() : "");
      }

      return response;
   }

   @PostMapping({"/create"})
   public ClientResponseTYPE<String> create(@RequestBody ConfDatosEmisorTiendaDtoVM request) {
      ClientResponseTYPE<String> response = new ClientResponseTYPE<String>();

      try {
         this.registrarActividad(12, "Crea entidad de confdatosemisortienda ", "/api/catologo/confdatosemisortienda/create");
         response = this.confDatosEmisorTiendaService.create(request);
      } catch (Exception var4) {
         this.logger.error("Error al crear la entidad.  /api/catologo/confdatosemisortienda/create", var4);
         this.guardarLogerrores("/api/catologo/confdatosemisortienda/create", "Crea entidad de confdatosemisortienda", var4);
         UtilsApi.setRespuesta(response.getRespuesta(), ECodigo.Error, var4.getMessage() != null ? var4.getMessage() : "");
      }

      return response;
   }
}
