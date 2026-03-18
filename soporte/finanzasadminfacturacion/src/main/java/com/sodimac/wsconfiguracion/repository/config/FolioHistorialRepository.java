package com.sodimac.wsconfiguracion.repository.config;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.FolioHistorialEntity;

@Repository("folioHistorialRepositoryConfig")
public interface FolioHistorialRepository extends JpaRepository<FolioHistorialEntity, BigInteger> {

	public FolioHistorialEntity findFirstByIdcatserieAndIdconfdatosemisortiendaOrderByFechaCreacionDesc(Integer idcatserie, Integer idconfdatosemisortienda);
}
