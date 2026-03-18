package com.sodimac.wsconfiguracion.service.catalogs;

import com.sodimac.wsconfiguracion.dto.CatTipoTiendaDto;
import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import java.util.List;

public interface CatTipoTiendaService {
   ClientResponseTYPE<List<CatTipoTiendaDto>> findAll();
}
