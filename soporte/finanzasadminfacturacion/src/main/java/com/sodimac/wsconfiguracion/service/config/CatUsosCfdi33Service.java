package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.UsoDeCfdiDto;
import com.sodimac.wsconfiguracion.entity.config.CatUsosCfdi33Entity;
import java.util.List;

public interface CatUsosCfdi33Service {
   List<CatUsosCfdi33Entity> getAll(int tipo);

   CatUsosCfdi33Entity getUsoCfdi(int id);

   CatUsosCfdi33Entity getUsoCfdi(String clave);

   ClientResponseTYPE<List<UsoDeCfdiDto>> getUsoCfdi33(int idTipoPersona);

   ClientResponseTYPE<List<UsoDeCfdiDto>> getUsoCfdi33All();
}
