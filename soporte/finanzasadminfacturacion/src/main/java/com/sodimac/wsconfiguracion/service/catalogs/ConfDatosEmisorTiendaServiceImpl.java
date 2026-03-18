package com.sodimac.wsconfiguracion.service.catalogs;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDtoVM;
import com.sodimac.wsconfiguracion.entity.config.CatCodigoPostalEntity;
import com.sodimac.wsconfiguracion.entity.config.CatTipoTiendaEntity;
import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorEntity;
import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorTiendaEntity;
import com.sodimac.wsconfiguracion.repository.config.CatCodigoPostalRepository;
import com.sodimac.wsconfiguracion.repository.config.CatTipoTiendaRepository;
import com.sodimac.wsconfiguracion.repository.config.ConfDatosEmisorRepository;
import com.sodimac.wsconfiguracion.repository.config.ConfDatosEmisorTiendaRepository;
import com.sodimac.wsconfiguracion.service.SeguridadService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ConfDatosEmisorTiendaServiceImpl implements ConfDatosEmisorTiendaService {
   @Autowired
   @Qualifier("ConfDatosEmisorTiendaRepositoryConfig")
   private ConfDatosEmisorTiendaRepository confDatosEmisorTiendaRepository;
   @Autowired
   @Qualifier("catCodigoPostalRepositoryConfig")
   private CatCodigoPostalRepository catCodigoPostalRepository;
   @Autowired
   private CatTipoTiendaRepository catTipoTiendaRepository;
   @Autowired
   private SeguridadService seguridadService;
   @Autowired
   @Qualifier("confDatosEmisorRepositoryConfig")
   private ConfDatosEmisorRepository confDatosEmisorRepository;

   public ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> findAllVM() {
      ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> clientResponse = new ClientResponseTYPE(new ArrayList());
      List<ConfDatosEmisorTiendaDtoVM> responseData = this.confDatosEmisorTiendaRepository.findAllVM();
      if (responseData != null) {
         responseData.stream().forEach((p) -> {
            try {
               p.setEmisor(this.seguridadService.desencriptar(p.getEmisor()));
            } catch (Exception var3) {
               var3.printStackTrace();
            }

         });
         clientResponse.setData(responseData);
      } else {
         UtilsApi.setRespuesta(clientResponse.getRespuesta(), ECodigo.ConfDatsEmisorTiendaNoEncontrado);
      }

      return clientResponse;
   }

   public ClientResponseTYPE<String> update(ConfDatosEmisorTiendaDtoVM request) {
      ClientResponseTYPE<String> clientResponse = new ClientResponseTYPE();
      ConfDatosEmisorTiendaEntity emisorTiendaEntity = (ConfDatosEmisorTiendaEntity)this.confDatosEmisorTiendaRepository.findById(request.getId()).get();
      if (emisorTiendaEntity != null) {
         ConfDatosEmisorEntity entityEmisor = null;

         try {
            entityEmisor = this.confDatosEmisorRepository.findByRfc(this.seguridadService.encriptar(request.getEmisor()));
            emisorTiendaEntity.setIdConfDatosEmisor(entityEmisor.getId());
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         this.setEntity(request, emisorTiendaEntity);
         if (this.confDatosEmisorTiendaRepository.save(emisorTiendaEntity) == null) {
            UtilsApi.setRespuesta(clientResponse.getRespuesta(), ECodigo.ConfDatsEmisorTiendaNoGuardado);
         }
      } else {
         UtilsApi.setRespuesta(clientResponse.getRespuesta(), ECodigo.ConfDatsEmisorTiendaNoEncontrado);
      }

      return clientResponse;
   }

   public ClientResponseTYPE<String> create(ConfDatosEmisorTiendaDtoVM request) {
      ClientResponseTYPE<String> clientResponse = new ClientResponseTYPE();
      ConfDatosEmisorTiendaEntity emisorTiendaEntity = new ConfDatosEmisorTiendaEntity();
      ConfDatosEmisorEntity entityEmisor = null;

      try {
         entityEmisor = this.confDatosEmisorRepository.findByRfc(this.seguridadService.encriptar(request.getEmisor()));
         emisorTiendaEntity.setIdConfDatosEmisor(entityEmisor.getId());
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      this.setEntity(request, emisorTiendaEntity);
      if (this.confDatosEmisorTiendaRepository.save(emisorTiendaEntity) == null) {
         UtilsApi.setRespuesta(clientResponse.getRespuesta(), ECodigo.ConfDatsEmisorTiendaCreacion);
      }

      return clientResponse;
   }

   private void setEntity(ConfDatosEmisorTiendaDtoVM request, ConfDatosEmisorTiendaEntity emisorTiendaEntity) {
      emisorTiendaEntity.setIdTienda(request.getIdTienda());
      emisorTiendaEntity.setDescripcion(request.getDescripcion());
      emisorTiendaEntity.setCalle(request.getCalle());
      emisorTiendaEntity.setNoExterior(request.getNoExterior());
      emisorTiendaEntity.setNoInterior(request.getNoInterior());
      emisorTiendaEntity.setColonia(request.getColonia());
      emisorTiendaEntity.setReferencia(request.getReferencia());
      CatCodigoPostalEntity codigoPostal = (CatCodigoPostalEntity)this.catCodigoPostalRepository.findById(request.getIdCatCodigoPostal()).get();
      emisorTiendaEntity.setLocalidad(codigoPostal.getC_localidad());
      emisorTiendaEntity.setMunicipio(codigoPostal.getC_municipio());
      emisorTiendaEntity.setEstado(codigoPostal.getC_estado());
      emisorTiendaEntity.setPais("México");
      emisorTiendaEntity.setCatCodigoPostalEntity(codigoPostal);
      CatTipoTiendaEntity tipoTienda = (CatTipoTiendaEntity)this.catTipoTiendaRepository.findById(request.getIdCatTipoTienda()).get();
      emisorTiendaEntity.setCatTipoTiendaEntity(tipoTienda);
      emisorTiendaEntity.setActivo(request.getActivo());
      emisorTiendaEntity.setFechaInicio(request.getFechaInicio());
   }
}
