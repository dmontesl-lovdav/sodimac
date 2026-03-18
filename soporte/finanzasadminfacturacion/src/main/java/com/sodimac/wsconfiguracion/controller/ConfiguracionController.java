package com.sodimac.wsconfiguracion.controller;

import com.sodimac.wsconfiguracion.dto.CatRegimenFiscalDto;
import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.CodigoPostal;
import com.sodimac.wsconfiguracion.dto.ComprobanteDto;
import com.sodimac.wsconfiguracion.dto.InformacionGlobal;
import com.sodimac.wsconfiguracion.dto.MesDto;
import com.sodimac.wsconfiguracion.dto.PeriodicidadDto;
import com.sodimac.wsconfiguracion.dto.RegimenDeCapitalDto;
import com.sodimac.wsconfiguracion.dto.UsoDeCfdiDto;
import com.sodimac.wsconfiguracion.dto.VersionTimbradoDto;
import com.sodimac.wsconfiguracion.models.config.CatalogoReq;
import com.sodimac.wsconfiguracion.models.config.CodigoPostalReq;
import com.sodimac.wsconfiguracion.models.config.EmisorReq;
import com.sodimac.wsconfiguracion.models.config.LoginReq;
import com.sodimac.wsconfiguracion.models.config.RegimenDeCapitalReq;
import com.sodimac.wsconfiguracion.models.config.RegimenFiscalReq;
import com.sodimac.wsconfiguracion.models.config.UsoDeCfdi33Req;
import com.sodimac.wsconfiguracion.models.config.UsoDeCfdi40Req;
import com.sodimac.wsconfiguracion.models.config.VersionTimbradoReq;
import com.sodimac.wsconfiguracion.service.config.CatCodigoPostalService;
import com.sodimac.wsconfiguracion.service.config.CatConfiguracionService;
import com.sodimac.wsconfiguracion.service.config.CatRegimenFiscalService;
import com.sodimac.wsconfiguracion.service.config.CatUsoCfdiService;
import com.sodimac.wsconfiguracion.service.config.CatUsosCfdi33Service;
import com.sodimac.wsconfiguracion.service.config.CatalogosService;
import com.sodimac.wsconfiguracion.service.config.EmisorService;
import com.sodimac.wsconfiguracion.service.config.RegimenSocietarioVarianteService;
import com.sodimac.wsconfiguracion.service.config.VersiontimbradoAplicacionService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import com.sodimac.wsconfiguracion.util.enums.EVersionCFDI;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api"})
public class ConfiguracionController extends BaseController {
   Logger logger = LoggerFactory.getLogger(ConfiguracionController.class);
   @Autowired
   private EmisorService emisorService;
   @Autowired
   private VersiontimbradoAplicacionService versiontimbradoAplicacionService;
   @Autowired
   @Qualifier("catRegimenFiscalServiceImplConfig")
   private CatRegimenFiscalService catRegimenFiscalService;
   @Autowired
   @Qualifier("catConfiguracionServiceImplConfig")
   private CatConfiguracionService catConfiguracionService;
   @Autowired
   @Qualifier("catCodigoPostalServiceImplConfig")
   private CatCodigoPostalService catCodigoPostalService;
   @Autowired
   @Qualifier("regimenSocietarioVarianteServiceImplConfig")
   private RegimenSocietarioVarianteService regimenSocietarioVarianteService;
   @Autowired
   @Qualifier("catalogosServiceImplConfig")
   private CatalogosService catalogosService;
   @Autowired
   @Qualifier("catUsoCfdiServiceImplConfig")
   private CatUsoCfdiService catUsoCfdiService;
   @Autowired
   private CatUsosCfdi33Service catUsosCfdi33Service;

   @PostMapping({"/login"})
   @ApiOperation(
      value = "Logeo de un usuario con su contraseña",
      notes = "Este método permite a un usuario logearse generando un token que le permitirá accesar los métodos a los cuales tiene acceso su perfil."
   )
   public String login(@RequestBody LoginReq request) {
      return "";
   }

   @PostMapping({"/emisor"})
   @ApiOperation(
      value = "Obtiene Datos del Emisor y Tienda",
      notes = "Obtiene los datos del Emisor y Tienda de acuerdo el RFC y Tienda solicitado. rfc= Rfc del emisor. version=version de facturacion '3.3' o '4.0'. sucursal= Tienda/sucursal donde se realiza la factura."
   )
   public ClientResponseTYPE<ComprobanteDto> emisor(@RequestBody EmisorReq request) {
      ClientResponseTYPE<ComprobanteDto> comprobante = null;

      try {
         this.registrarActividad(2, "Usuario Obtiene datos del Emisor y Tienda. RFC: " + request.getRfc() + " Tienda: " + request.getSucursal(), "/emisor");
         comprobante = this.emisorService.obtenerEmisorYLugarExpedicion(request);
      } catch (Exception var4) {
         this.logger.error("Error al obtener los datos del Emisor. /emisor ", var4);
         this.errorComponent.setPagina("/emisor");
         this.errorComponent.setParametrosLlamado("Usuario Obtiene datos del Emisor y Tienda. RFC: " + request.getRfc() + " Tienda: " + request.getSucursal());
         this.errorComponent.guardarLog(var4);
         if (comprobante == null) {
            comprobante = new ClientResponseTYPE<ComprobanteDto>();
         }

         UtilsApi.setRespuesta(comprobante.getRespuesta(), ECodigo.Error, var4.getMessage());
      }

      return comprobante;
   }

   @PostMapping({"/versiontimbrado"})
   @ApiOperation(
      value = "Obtiene la version del timbrado que tiene asignada la aplicacion",
      notes = "Como parametro se envia el id de la aplicación"
   )
   public ClientResponseTYPE<VersionTimbradoDto> VersionTimbrado(@RequestBody VersionTimbradoReq request) {
      ClientResponseTYPE<VersionTimbradoDto> versionTimbrado = null;

      try {
         this.registrarActividad(3, "Obtiene la versión del timbrado que aplica a la aplicacion. idApliación: " + request.getIdAplicacion(), "/versiontimbrado");
         versionTimbrado = this.versiontimbradoAplicacionService.ObtieneVersionTimbrado(request.getIdAplicacion());
      } catch (Exception var4) {
         this.logger.error("Error al obtener la versiónd el timbrado.  /versiontimbrado", var4);
         this.guardarLogerrores("/versiontimbrado", "Obtiene la versión del timbrado que aplica a la aplicacion. idApliación:" + request.getIdAplicacion(), var4);
         UtilsApi.setRespuesta(versionTimbrado.getRespuesta(), ECodigo.Error);
      }

      return versionTimbrado;
   }

   @PostMapping({"/regimenfiscal"})
   @ApiOperation(
      value = "Obtiene una lista del regimen fiscal de acuerdo al tipo de persona.",
      notes = "Como parametro se envia el id del tipo de Persona. 1=Fisica, 2=Moral, 3=Todos"
   )
   public ClientResponseTYPE<List<CatRegimenFiscalDto>> RegimenFiscal(@RequestBody RegimenFiscalReq request) {
      ClientResponseTYPE<List<CatRegimenFiscalDto>> regimenFiscal = null;

      try {
         this.registrarActividad(4, "Obtiene una lista del regimen fiscal de acuerdo al tipo de persona. idTipoPersona: " + request.getIdTipoPersona(), "/regimenfiscal");
         regimenFiscal = this.catRegimenFiscalService.obtieneRegimenFiscal(request.getIdTipoPersona());
      } catch (Exception var4) {
         this.logger.error("Error al obtener la lista del regimen fiscal. /regimenfiscal ", var4);
         this.guardarLogerrores("/regimenfiscal", "Obtiene una lista del regimen fiscal de acuerdo al tipo de persona. idApliación:" + request.getIdTipoPersona(), var4);
         UtilsApi.setRespuesta(regimenFiscal.getRespuesta(), ECodigo.Error);
      }

      return regimenFiscal;
   }

   @GetMapping({"/informacionglobal"})
   @ApiOperation(
      value = "Obtiene la periodicidad de la informacion global",
      notes = ""
   )
   public ClientResponseTYPE<InformacionGlobal> InformacionGlobal() {
      ClientResponseTYPE<InformacionGlobal> informacionGlobal = null;

      try {
         this.registrarActividad(5, "Obtiene la periodicidad de la informacion global. ", "/informacionglobal");
         informacionGlobal = this.catConfiguracionService.obtieneParamsFG();
      } catch (Exception var3) {
         this.logger.error("Error al obtener la configuración de la informacion global. /informacionglobal", var3);
         this.guardarLogerrores("/informacionglobal", "Obtiene una lista del regimen fiscal de acuerdo al tipo de persona.", var3);
         UtilsApi.setRespuesta(informacionGlobal.getRespuesta(), ECodigo.Error);
      }

      return informacionGlobal;
   }

   @PostMapping({"/codigopostal"})
   @ApiOperation(
      value = "Verifica el codigo postal existente.",
      notes = "Como parametro se envia el codigo postal"
   )
   public ClientResponseTYPE<CodigoPostal> verificaCodigoPostal(@RequestBody CodigoPostalReq request) {
      ClientResponseTYPE<CodigoPostal> codigoPostal = null;

      try {
         this.registrarActividad(6, "Verfica el codigo Postal. codigoPostal: " + request.getCodigoPostal(), "/codigopostal");
         codigoPostal = this.catCodigoPostalService.verificaCodigoPostal(request.getCodigoPostal());
      } catch (Exception var4) {
         this.logger.error("Error al verificar codigo postal:" + request.getCodigoPostal() + " . /codigopostal ", var4);
         this.guardarLogerrores("/codigopostal", "Verfica el codigo postal existente.  codigo postal:" + request.getCodigoPostal(), var4);
         UtilsApi.setRespuesta(codigoPostal.getRespuesta(), ECodigo.Error);
      }

      return codigoPostal;
   }

   @PostMapping({"/regimendecapital"})
   @ApiOperation(
      value = "Verifica que la razón social no contenga el regimen de capital.",
      notes = "Como parametro se envia la razón social"
   )
   public ClientResponseTYPE<RegimenDeCapitalDto> RegimenDeCapital(@RequestBody RegimenDeCapitalReq request) {
      ClientResponseTYPE<RegimenDeCapitalDto> regimenDeCapital = null;

      try {
         this.registrarActividad(7, "Verifica el regimen de capital en la razón social. razón social: " + request.getRazonSocial(), "/regimendecapital");
         regimenDeCapital = this.regimenSocietarioVarianteService.validaRazonSocial(request.getRazonSocial());
      } catch (Exception var4) {
         this.logger.error("Error al validar el regimen de capital. /regimendecapital ", var4);
         this.guardarLogerrores("/regimendecapital", "Valida regimen de capital.  razon social:" + request.getRazonSocial(), var4);
         UtilsApi.setRespuesta(regimenDeCapital.getRespuesta(), ECodigo.Error);
      }

      return regimenDeCapital;
   }

   @PostMapping({"/mes"})
   @ApiOperation(
      value = "Obtiene configuracion de mes por clave.",
      notes = "Como parametro se envia la clave"
   )
   public ClientResponseTYPE<MesDto> getConfigMes(@RequestBody CatalogoReq request) {
      ClientResponseTYPE<MesDto> mes = null;

      try {
         this.registrarActividad(8, "Obtiene configuracion de mes. Clave mes: " + request.getClave(), "/mes");
         mes = this.catalogosService.getMesByClave(request.getClave());
      } catch (Exception var4) {
         this.logger.error("Error al obtener configuracion de mes:" + request.getClave() + " . /mes ", var4);
         this.guardarLogerrores("/mes", "Obtiene configuracion de mes por clave.  Clave Mes:" + request.getClave(), var4);
         UtilsApi.setRespuesta(mes.getRespuesta(), ECodigo.Error);
      }

      return mes;
   }

   @PostMapping({"/periodicidad"})
   @ApiOperation(
      value = "Obtiene periodicidad.",
      notes = "Como parametro se envia la clave"
   )
   public ClientResponseTYPE<PeriodicidadDto> getPeriodicidad(@RequestBody CatalogoReq request) {
      ClientResponseTYPE<PeriodicidadDto> periodicidad = null;

      try {
         this.registrarActividad(9, "Obtiene Periodicidad. codigoPostal: " + request.getClave(), "/periodicidad");
         periodicidad = this.catalogosService.getPeriodicidadByClave(request.getClave());
      } catch (Exception var4) {
         this.logger.error("Error al obtener periodicidad:" + request.getClave() + " . /periodicidad ", var4);
         this.guardarLogerrores("/periodicidad", "Obtiene periodicidad.  Clave Periodicidad:" + request.getClave(), var4);
         UtilsApi.setRespuesta(periodicidad.getRespuesta(), ECodigo.Error);
      }

      return periodicidad;
   }

   @PostMapping({"/usoscfdi"})
   @ApiOperation(
      value = "Obtiene usos de cfdi.",
      notes = "se obtienen los activos"
   )
   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi() {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = null;

      try {
         usosCfdi = this.catUsoCfdiService.getUsosCfdi40();
      } catch (Exception var3) {
         this.logger.error("Error al obtener catálogo de usos de cfdi . /usoscfdi ", var3);
         this.guardarLogerrores("/usoscfdi", "usos de cfdi", var3);
         UtilsApi.setRespuesta(usosCfdi.getRespuesta(), ECodigo.Error);
      }

      return usosCfdi;
   }

   @PostMapping({"/usoscfdi40/all"})
   @ApiOperation(
      value = "Obtiene catálogo de usos de cfdi en la versión 4.0",
      notes = "se obtiene todo el catálogo"
   )
   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi40All() {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = null;

      try {
         usosCfdi = this.catUsoCfdiService.getUsosCfdi40All();
      } catch (Exception var3) {
         this.logger.error("Error al obtener catálogo de usos de cfdi . /usoscfdi40/all ", var3);
         this.guardarLogerrores("/usoscfdi", "usos de cfdi", var3);
         UtilsApi.setRespuesta(usosCfdi.getRespuesta(), ECodigo.Error);
      }

      return usosCfdi;
   }

   @PostMapping({"/usoscfdi33/all"})
   @ApiOperation(
      value = "Obtiene el catálogo de usos de cfdi en versión 3.3",
      notes = "se obtienen todo el catálogo"
   )
   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi33All() {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = null;

      try {
         usosCfdi = this.catUsosCfdi33Service.getUsoCfdi33All();
      } catch (Exception var3) {
         this.logger.error("Error al obtener catálogo de usos de cfdi . /usoscfdi33/all ", var3);
         this.guardarLogerrores("/usoscfdi", "usos de cfdi", var3);
         UtilsApi.setRespuesta(usosCfdi.getRespuesta(), ECodigo.Error);
      }

      return usosCfdi;
   }

   @PostMapping({"/usoscfdi33"})
   @ApiOperation(
      value = "Obtiene usos de cfdi para la versión 3.3",
      notes = "se obtienen usos de cfdi por versión de cfdi, tipo de persona"
   )
   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi33(@RequestBody UsoDeCfdi33Req request) {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = null;

      try {
         usosCfdi = this.catUsosCfdi33Service.getUsoCfdi33(request.getIdTipoPersona());
      } catch (Exception var4) {
         this.logger.error("Error al obtener usos de cfdi 3.3 /usoscfdi33 ", var4);
         this.guardarLogerrores("/usoscfdi33", "usos de cfdi 33", var4);
         UtilsApi.setRespuesta(usosCfdi.getRespuesta(), ECodigo.Error);
      }

      return usosCfdi;
   }

   @PostMapping({"/usoscfdi40"})
   @ApiOperation(
      value = "Obtiene usos de cfdi pora versión 40",
      notes = "se obtienen usos de cfdi por versión de cfdi, tipo de persona y régimen fiscal"
   )
   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi40(@RequestBody UsoDeCfdi40Req request) {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = null;

      try {
         Integer pIdVersionCfdi = EVersionCFDI.VERSION_40.getId();
         usosCfdi = this.catUsoCfdiService.getUsoCfdi40(pIdVersionCfdi, request.getIdTipoPersona(), request.getRegimenFiscal());
      } catch (Exception var4) {
         this.logger.error("Error al obtener usos de cfdi por regimen fiscal. /usoscfdi40 ", var4);
         this.guardarLogerrores("/usoscfdiregimen", "usos de cfdi", var4);
         UtilsApi.setRespuesta(usosCfdi.getRespuesta(), ECodigo.Error);
      }

      return usosCfdi;
   }
}
