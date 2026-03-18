package com.sodimac.wsconfiguracion.repository.config;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.FolioEntity;

@Repository("folioRepositoryConfig")
public interface FolioRepository extends JpaRepository<FolioEntity, BigInteger>  {

	public FolioEntity findByIdcatserieAndIdconfdatosemisortienda(Integer idcatserie, Integer idconfdatosemisortienda);
}
