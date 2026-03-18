package com.sodimac.wsconfiguracion.service.catalogs;

import com.sodimac.wsconfiguracion.dto.CatTipoTiendaDto;
import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.entity.config.CatTipoTiendaEntity;
import com.sodimac.wsconfiguracion.repository.config.CatTipoTiendaRepository;
import com.sodimac.wsconfiguracion.util.UtilsApi;
import com.sodimac.wsconfiguracion.util.enums.ECodigo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatTipoTiendaServiceImpl implements CatTipoTiendaService {
   @Autowired
   private ModelMapper modelMapper;
   @Autowired
   private CatTipoTiendaRepository catTipoTiendaRepository;

   public ClientResponseTYPE<List<CatTipoTiendaDto>> findAll() {
      ClientResponseTYPE<List<CatTipoTiendaDto>> clientResponse = new ClientResponseTYPE(new ArrayList());
      List<CatTipoTiendaEntity> responseData = this.catTipoTiendaRepository.findAll();
      if (responseData != null) {
         Iterator var4 = responseData.iterator();

         while(var4.hasNext()) {
            CatTipoTiendaEntity item = (CatTipoTiendaEntity)var4.next();
            CatTipoTiendaDto datosEmisor = (CatTipoTiendaDto)this.convertirADto(item, CatTipoTiendaDto.class);
            ((List)clientResponse.getData()).add(datosEmisor);
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
