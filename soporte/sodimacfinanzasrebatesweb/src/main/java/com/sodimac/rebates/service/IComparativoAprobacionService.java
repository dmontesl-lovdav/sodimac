package com.sodimac.rebates.service;

import com.sodimac.rebates.model.ComparativoAprobacion;

public interface IComparativoAprobacionService {

	ComparativoAprobacion getByIDderegistroAndIdperiodoAndTipodeRebateAndCuenta(String iDderegistro, Integer idperiodo,
			Integer tipodeRebate, String cuenta);

	ComparativoAprobacion getById(Integer idComparativoAprobacion);

	boolean save(ComparativoAprobacion comparativoAprobacion);

}
