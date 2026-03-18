package com.sodimac.wsconfiguracion.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sodimac.wsconfiguracion.entity.config.RegimenSocietarioVarianteEntity;

@Repository("regimenSocietarioVarianteRepositoryConfig")
public interface RegimenSocietarioVarianteRepository extends JpaRepository<RegimenSocietarioVarianteEntity, Integer> {

}
