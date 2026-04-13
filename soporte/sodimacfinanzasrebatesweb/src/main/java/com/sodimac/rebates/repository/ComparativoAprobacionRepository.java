package com.sodimac.rebates.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sodimac.rebates.model.ComparativoAprobacion;

public interface ComparativoAprobacionRepository extends JpaRepository<ComparativoAprobacion, Integer> {

	ComparativoAprobacion findByiDderegistroAndIdperiodoAndTipodeRebateAndCuenta(
			String iDderegistro, Integer idperiodo,
			Integer tipodeRebate, String cuenta);

}
