package com.sodimac.wsconfiguracion.service.catalogs;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorDto;
import java.util.List;

public interface ConfDatosEmisorService {
   ClientResponseTYPE<List<ConfDatosEmisorDto>> findAll();
}
