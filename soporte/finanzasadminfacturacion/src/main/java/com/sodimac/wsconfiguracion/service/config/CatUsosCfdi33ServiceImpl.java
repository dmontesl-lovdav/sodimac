package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.UsoDeCfdiDto;
import com.sodimac.wsconfiguracion.entity.config.CatUsosCfdi33Entity;
import com.sodimac.wsconfiguracion.repository.config.CatUsosCfdi33Repository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatUsosCfdi33ServiceImpl implements CatUsosCfdi33Service {
   @Autowired
   private CatUsosCfdi33Repository catUsosCfdi33Repository;

   @Transactional
   public List<CatUsosCfdi33Entity> getAll(int tipo) {
      return tipo == 1 ? this.catUsosCfdi33Repository.findByFisica(true) : this.catUsosCfdi33Repository.findByMoral(true);
   }

   @Transactional
   public CatUsosCfdi33Entity getUsoCfdi(int id) {
      return (CatUsosCfdi33Entity)this.catUsosCfdi33Repository.findById(id).get();
   }

   public CatUsosCfdi33Entity getUsoCfdi(String clave) {
      return this.catUsosCfdi33Repository.findByClave(clave);
   }

   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsoCfdi33(int idTipoPersona) {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = new ClientResponseTYPE(new ArrayList());
      List<CatUsosCfdi33Entity> listCfdi = this.getAll(idTipoPersona);
      if (listCfdi != null) {
         Iterator var5 = listCfdi.iterator();

         while(var5.hasNext()) {
            CatUsosCfdi33Entity catusoCfdi = (CatUsosCfdi33Entity)var5.next();
            UsoDeCfdiDto cfdiDto = new UsoDeCfdiDto();
            cfdiDto.setIdUsoCfdi(catusoCfdi.getIdUsoCfdi());
            cfdiDto.setDescripcionUso(catusoCfdi.getDescripcionUso());
            cfdiDto.setClave(catusoCfdi.getClave());
            cfdiDto.setActivo(1);
            ((List)usosCfdi.getData()).add(cfdiDto);
         }
      }

      return usosCfdi;
   }

   public ClientResponseTYPE<List<UsoDeCfdiDto>> getUsoCfdi33All() {
      ClientResponseTYPE<List<UsoDeCfdiDto>> usosCfdi = new ClientResponseTYPE(new ArrayList());
      List<CatUsosCfdi33Entity> listCfdi = this.catUsosCfdi33Repository.findAll();
      if (listCfdi != null) {
         Iterator var4 = listCfdi.iterator();

         while(var4.hasNext()) {
            CatUsosCfdi33Entity catusoCfdi = (CatUsosCfdi33Entity)var4.next();
            UsoDeCfdiDto cfdiDto = new UsoDeCfdiDto();
            cfdiDto.setIdUsoCfdi(catusoCfdi.getIdUsoCfdi());
            cfdiDto.setDescripcionUso(catusoCfdi.getDescripcionUso());
            cfdiDto.setClave(catusoCfdi.getClave());
            ((List)usosCfdi.getData()).add(cfdiDto);
         }
      }

      return usosCfdi;
   }
}
