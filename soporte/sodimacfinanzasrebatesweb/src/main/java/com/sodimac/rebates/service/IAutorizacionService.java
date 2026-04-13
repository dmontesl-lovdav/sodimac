package com.sodimac.rebates.service;

import java.util.Date;
import java.util.List;

import com.sodimac.rebates.model.Autorizacion;

public interface IAutorizacionService {

	List<Autorizacion> getAutorizacionWithDates(Date fechaInicio, Date fechaFinal, String descripcionPeriodo,
			String tipoPeriodo, String idPeriodo, String tipodeRebate);

	List<Autorizacion> getAutorizacionWithOutDates(String descripcionPeriodo, String tipoPeriodo, String idPeriodo,
			String tipodeRebate);

}
