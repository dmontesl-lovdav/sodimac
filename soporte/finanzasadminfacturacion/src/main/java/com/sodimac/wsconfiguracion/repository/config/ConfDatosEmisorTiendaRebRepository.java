package com.sodimac.wsconfiguracion.repository.config;


import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorTiendaRebEntity;

//@Repository("ConfDatosEmisorTiendaRebRepositoryConfig")
public interface ConfDatosEmisorTiendaRebRepository  {

	ConfDatosEmisorTiendaRebEntity findByIdTienda(Integer idTienda);
}
