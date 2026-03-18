package com.sodimac.wsconfiguracion.service.config;

import java.util.List;

import com.sodimac.wsconfiguracion.dto.CatRegimenFiscalDto;
import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;

public interface CatRegimenFiscalService {

	ClientResponseTYPE<List<CatRegimenFiscalDto>> obtieneRegimenFiscal(Integer idTipoPersona);
}
