package com.sodimac.wsconfiguracion.repository.config;

import com.sodimac.wsconfiguracion.entity.config.CatTipoTiendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatTipoTiendaRepository extends JpaRepository<CatTipoTiendaEntity, Integer> {
}
