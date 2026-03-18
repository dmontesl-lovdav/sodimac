package com.sodimac.wsconfiguracion.controller;

import com.sodimac.wsconfiguracion.component.ActividadesComponent;
import com.sodimac.wsconfiguracion.component.ErrorComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
   @Autowired
   private ActividadesComponent actividadesModel;
   @Autowired
   protected ErrorComponent errorComponent;

   protected void registrarActividad(int idActividad, String actividadDesc, String pagina) {
      this.actividadesModel.setActividadesProperties("", "", "", "", "", "", "");
      this.actividadesModel.registrarActividad(idActividad, actividadDesc, pagina);
   }

   protected void guardarLogerrores(String pagina, String parametrosLLamado, Exception e) {
      this.errorComponent.setPagina(pagina);
      this.errorComponent.setParametrosLlamado(parametrosLLamado);
      this.errorComponent.guardarLog(e);
   }
}
