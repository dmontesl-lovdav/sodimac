package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.UsoDeCfdiDto;
import com.sodimac.wsconfiguracion.entity.config.CatUsoCfdiEntity;
import com.sodimac.wsconfiguracion.repository.config.CatUsoCfdiRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("catUsoCfdiServiceImplConfig")
public class CatUsoCfdiServiceImpl implements CatUsoCfdiService {
   @Autowired
   private CatUsoCfdiRepository catUsoCfdiRepository;

   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsoCfdi40(Integer pIdVersionCfdi, Integer pIdTipoPersona, String regimenFiscal) {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = new ClientResponseTYPE(new ArrayList());
      List<Object[]> usoCfdiArray = this.catUsoCfdiRepository.getUsoCfdi(pIdVersionCfdi, pIdTipoPersona, regimenFiscal);
      if (usoCfdiArray != null) {
         for(int i = 0; i < usoCfdiArray.size(); ++i) {
            Object[] obj = (Object[])usoCfdiArray.get(i);
            UsoDeCfdiDto cfdiDto = new UsoDeCfdiDto();
            cfdiDto.setIdUsoCfdi(Integer.valueOf(obj[0].toString()));
            cfdiDto.setDescripcionUso(obj[2].toString());
            cfdiDto.setClave(obj[1].toString());
            cfdiDto.setActivo(1);
            ((List)usosCfdi.getData()).add(cfdiDto);
         }
      }

      return usosCfdi;
   }

   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi40() {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = new ClientResponseTYPE(new ArrayList());
      List<CatUsoCfdiEntity> listUsosCfdi = this.catUsoCfdiRepository.findByActivo(Boolean.TRUE);
      if (listUsosCfdi != null) {
         Iterator var4 = listUsosCfdi.iterator();

         while(var4.hasNext()) {
            CatUsoCfdiEntity catUsoCfdi = (CatUsoCfdiEntity)var4.next();
            UsoDeCfdiDto cfdiDto = new UsoDeCfdiDto();
            cfdiDto.setIdUsoCfdi(catUsoCfdi.getIdUsoCfdi());
            cfdiDto.setDescripcionUso(catUsoCfdi.getDescripcionUso());
            cfdiDto.setClave(catUsoCfdi.getClave());
            cfdiDto.setActivo(1);
            ((List)usosCfdi.getData()).add(cfdiDto);
         }
      }

      return usosCfdi;
   }

   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi40All() {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = new ClientResponseTYPE(new ArrayList());
      List<CatUsoCfdiEntity> listUsosCfdi = this.catUsoCfdiRepository.findAll();
      if (listUsosCfdi != null) {
         Iterator var4 = listUsosCfdi.iterator();

         while(var4.hasNext()) {
            CatUsoCfdiEntity catUsoCfdi = (CatUsoCfdiEntity)var4.next();
            UsoDeCfdiDto cfdiDto = new UsoDeCfdiDto();
            cfdiDto.setIdUsoCfdi(catUsoCfdi.getIdUsoCfdi());
            cfdiDto.setDescripcionUso(catUsoCfdi.getDescripcionUso());
            cfdiDto.setClave(catUsoCfdi.getClave());
            ((List)usosCfdi.getData()).add(cfdiDto);
         }
      }

      return usosCfdi;
   }
}
