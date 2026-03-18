package com.sodimac.wsconfiguracion.service.catalogs;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorDto;
import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorEntity;
import com.sodimac.wsconfiguracion.repository.config.ConfDatosEmisorRepository;
import com.sodimac.wsconfiguracion.service.SeguridadService;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ConfDatosEmisorServiceImpl implements ConfDatosEmisorService {
   @Autowired
   private ModelMapper modelMapper;
   @Autowired
   private SeguridadService seguridadService;
   @Autowired
   @Qualifier("confDatosEmisorRepositoryConfig")
   private ConfDatosEmisorRepository confDatosEmisorRepository;

   public ClientResponseTYPE<List<ConfDatosEmisorDto>> findAll() {
      ClientResponseTYPE<List<ConfDatosEmisorDto>> clientResponse = new ClientResponseTYPE(new ArrayList());
      List<ConfDatosEmisorEntity> responseData = this.confDatosEmisorRepository.findAll();
      ConfDatosEmisorDto datosEmisor;
      if (responseData != null) {
         for(Iterator var4 = responseData.iterator(); var4.hasNext(); ((List)clientResponse.getData()).add(datosEmisor)) {
            ConfDatosEmisorEntity item = (ConfDatosEmisorEntity)var4.next();
            datosEmisor = (ConfDatosEmisorDto)this.convertirADto(item, ConfDatosEmisorDto.class);

            try {
               datosEmisor.setRfc(this.seguridadService.desencriptar(datosEmisor.getRfc()));
               datosEmisor.setRazonSocial(this.seguridadService.desencriptar(datosEmisor.getRazonSocial()));
               datosEmisor.setCodigoPostal(String.valueOf(item.getCatCodigoPostalEntity().getCodigopostal()));
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      } else {
         UtilsApi.setRespuesta(clientResponse.getRespuesta(), ECodigo.ConfDatsEmisorTiendaNoEncontrado);
      }

      return clientResponse;
   }

   private Object convertirADto(Object obj, Class<?> destinationClass) {
      Object dto = this.modelMapper.map(obj, destinationClass);
      return dto;
   }
}
