package com.sodimac.wsconfiguracion.controller;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.CodigoPostal;
import com.sodimac.wsconfiguracion.service.config.CatCodigoPostalService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/catologo/catcodigopostal"})
public class CatCodigoPostalController extends BaseController {
   Logger logger = LoggerFactory.getLogger(CatCodigoPostalController.class);
   @Autowired
   @Qualifier("catCodigoPostalServiceImplConfig")
   private CatCodigoPostalService catCodigoPostalService;

   @GetMapping({"/findById/{id}"})
   @ApiOperation(
      value = "Obtiene el Codigo Postal por ID",
      notes = ""
   )
   public ClientResponseTYPE<CodigoPostal> findById(@PathVariable("id") int id) {
      ClientResponseTYPE<CodigoPostal> codigoPostal = null;

      try {
         this.registrarActividad(6, "Verfica el codigo Postal. codigoPostal: " + id, "/api/catologo/catcodigopostal");
         codigoPostal = this.catCodigoPostalService.verificaCodigoPostal(id);
      } catch (Exception var4) {
         this.logger.error("Error al verificar codigo postal:" + id + " . /api/catologo/catcodigopostal ", var4);
         this.guardarLogerrores("/codigopostal", "Verfica el codigo postal existente.  codigo postal:" + id, var4);
         UtilsApi.setRespuesta(codigoPostal.getRespuesta(), ECodigo.Error);
      }

      return codigoPostal;
   }
}
