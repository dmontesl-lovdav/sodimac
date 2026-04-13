package com.sodimac.rebates.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.Autorizacion;
import com.sodimac.rebates.repository.AutorizacionRepository;

@Service
public class AutorizacionService implements IAutorizacionService {

	@Autowired
	private AutorizacionRepository autorizacionRepo = null;

	@Override
	public List<Autorizacion> getAutorizacionWithDates(Date fechaInicio, Date fechaFinal, String descripcionPeriodo,
			String tipoPeriodo, String idPeriodo, String tipodeRebate) {

		List<Autorizacion> lista = autorizacionRepo.findByAutorizacionoWithDates(fechaInicio, fechaFinal,
				descripcionPeriodo, tipoPeriodo, idPeriodo, tipodeRebate);

		return lista;
	}

	@Override
	public List<Autorizacion> getAutorizacionWithOutDates(String descripcionPeriodo, String tipoPeriodo,
			String idPeriodo, String tipodeRebate) {

		List<Autorizacion> lista = autorizacionRepo.findByAutorizacionWithOutDates(descripcionPeriodo, tipoPeriodo,
				idPeriodo, tipodeRebate);

//		DecimalFormat df = new DecimalFormat("#.00");
//		df.setRoundingMode(RoundingMode.DOWN);
//		df.setGroupingUsed(true);
//		df.setGroupingSize(3);
//		String format = "";
//
//		if (lista.size() >= 1) {
//
//			for (Autorizacion a : lista) {
//
//				format = df.format(a.getImporte());
//				a.setImporte(Float.parseFloat(format));
//			}
//		}

		return lista;
	}

}
