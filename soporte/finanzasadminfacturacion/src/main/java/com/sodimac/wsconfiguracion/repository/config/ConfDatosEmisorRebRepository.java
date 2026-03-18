package com.sodimac.wsconfiguracion.repository.config;


import com.sodimac.wsconfiguracion.entity.config.ConfDatosEmisorRebEntity;

//@Repository("confDatosEmisorRebRepositoryConfig")
public interface ConfDatosEmisorRebRepository {

	ConfDatosEmisorRebEntity findByRfc(String rfc);
}
