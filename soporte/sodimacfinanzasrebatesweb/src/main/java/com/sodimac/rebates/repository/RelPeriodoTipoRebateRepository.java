package com.sodimac.rebates.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sodimac.rebates.model.Periodo;
import com.sodimac.rebates.model.RelPeriodoTipoRebate;
import com.sodimac.rebates.model.TipoRebate;

public interface RelPeriodoTipoRebateRepository extends JpaRepository<RelPeriodoTipoRebate, Integer>{

	Optional<RelPeriodoTipoRebate> findByPeriodoAndCatTipoRebate(Periodo entityPeriodo, TipoRebate rebateEntity);

}
