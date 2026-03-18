package com.sodimac.wsconfiguracion.controller;

import com.sodimac.wsconfiguracion.dto.CatTipoTiendaDto;
import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.service.catalogs.CatTipoTiendaService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/catologo/cattipotienda"})
public class TipoTiendaController extends BaseController {
   Logger logger = LoggerFactory.getLogger(TipoTiendaController.class);
   @Autowired
   private CatTipoTiendaService catTipoTiendaService;

   @GetMapping({"/findAll"})
   @ApiOperation(
      value = "Obtiene el catalogo de cattipotienda",
      notes = ""
   )
   public ClientResponseTYPE<List<CatTipoTiendaDto>> findAll() {
      ClientResponseTYPE<List<CatTipoTiendaDto>> confdatosemisor = new ClientResponseTYPE<List<CatTipoTiendaDto>>(new ArrayList<CatTipoTiendaDto>());

      try {
         this.registrarActividad(14, "Obtiene el catalogo de cattipotienda ", "/api/catologo/cattipotienda/findAll");
         confdatosemisor = this.catTipoTiendaService.findAll();
      } catch (Exception var3) {
         this.logger.error("Error al obtener el catalogo de cattipotienda.  /api/catologo/cattipotienda/findAll", var3);
         this.guardarLogerrores("/api/catologo/cattipotienda/findAll", "Obtiene el catalogo de cattipotienda.", var3);
         UtilsApi.setRespuesta(confdatosemisor.getRespuesta(), ECodigo.Error, var3.getMessage() != null ? var3.getMessage() : "");
      }

      return confdatosemisor;
   }
}
