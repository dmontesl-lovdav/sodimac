package com.sodimac.rebates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sodimac.rebates.model.TipoRebate;

public interface TipoRebateRepository extends JpaRepository<TipoRebate, Integer> {

	List<TipoRebate> findByActivo(boolean activo);
}
