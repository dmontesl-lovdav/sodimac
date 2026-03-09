package com.sodimac.cfdi.repository.fiscal.documento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.cfdi.entity.fiscal.ConfiguracionFtpEntity;

@Repository
public interface ConfiguracionFtpRepository extends JpaRepository<ConfiguracionFtpEntity, Integer>  {

	public ConfiguracionFtpEntity findByIdConfiguracionFtp(Integer idConfiguracionFtp);
	
}
