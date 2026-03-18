package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.UsoDeCfdiDto;
import java.util.List;

public interface CatUsoCfdiService {
   ClientResponseTYPE<List<UsoDeCfdiDto>> getUsoCfdi40(Integer pIdVersionCfdi, Integer pIdTipoPersona, String regimenFiscal);

   ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi40();

   ClientResponseTYPE<List<UsoDeCfdiDto>> getUsosCfdi40All();
}
