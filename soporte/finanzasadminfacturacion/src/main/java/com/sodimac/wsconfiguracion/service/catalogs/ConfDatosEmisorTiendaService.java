package com.sodimac.wsconfiguracion.service.catalogs;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.ConfDatosEmisorTiendaDtoVM;
import java.util.List;

public interface ConfDatosEmisorTiendaService {
   ClientResponseTYPE<List<ConfDatosEmisorTiendaDtoVM>> findAllVM();

   ClientResponseTYPE<String> update(ConfDatosEmisorTiendaDtoVM request);

   ClientResponseTYPE<String> create(ConfDatosEmisorTiendaDtoVM request);
}
