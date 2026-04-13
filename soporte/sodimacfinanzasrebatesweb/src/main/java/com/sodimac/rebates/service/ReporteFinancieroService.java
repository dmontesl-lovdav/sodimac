package com.sodimac.rebates.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.ReporteFinancieroDto;
import com.sodimac.rebates.filter.ReporteFinancieroFilter;
import com.sodimac.rebates.mapper.ReporteFinancieroMapper;
import com.sodimac.rebates.model.ReporteFinancieroEntity;

@Service
public class ReporteFinancieroService implements IReporteFinancieroService {

	private static Logger logger = LoggerFactory.getLogger(ReporteFinancieroService.class);
	
	@Autowired
	private EntityManager em;
	
	@Override
	public List<ReporteFinancieroDto> getReporteFinanciero(ReporteFinancieroFilter filter) {
		List<ReporteFinancieroEntity> list = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<ReporteFinancieroEntity> cq = cb.createQuery(ReporteFinancieroEntity.class);
		Root<ReporteFinancieroEntity> oc = cq.from(ReporteFinancieroEntity.class);
		List<Predicate> predicates = new ArrayList<>();
		
		long inicio = System.currentTimeMillis();
		
		logger.info("Fecha Ini: " + filter.getFechaIni());
		logger.info("Fecha Fin: " + filter.getFechaFin());
		logger.info("Id Periodo: " + filter.getIdPeriodo());
		logger.info("Id Proveedor: " + filter.getIdProveedor());
		logger.info("Tipo Periodo: " + filter.getTipoPeriodo());
		logger.info("Tipo Rebate: " + filter.getTipoRebate());
		
		if (filter.getFechaIni() != null) {
			
			Predicate onStart = cb.greaterThanOrEqualTo(oc.get("fechaContabilizacion"), filter.getFechaIni());
			predicates.add( onStart );
		}
		
		if (filter.getFechaFin() != null ) {
			Predicate onEnd = cb.lessThanOrEqualTo(oc.get("fechaContabilizacion"), filter.getFechaFin());
			predicates.add( onEnd );
		}
		
		if (filter.getIdProveedor() != null && filter.getIdProveedor() != 0) {
			predicates.add( cb.equal(oc.get("numeroProveedor"), filter.getIdProveedor().toString()));
		}
		
		if (filter.getIdPeriodo() != null && filter.getIdPeriodo() != 0) {
			predicates.add( cb.equal(oc.get("idCatPeriodo"), filter.getIdPeriodo().toString()));
		}
		
		if (Strings.isNotBlank(filter.getTipoPeriodo())) {
			predicates.add( cb.equal(oc.get("programaPago"), filter.getTipoPeriodo() ));
		}
		
		if (filter.getTipoRebate() != null && filter.getTipoRebate() != 0) {
			predicates.add( cb.equal(oc.get("referencia3"), filter.getTipoRebate() ));
		}
		
//		predicates.add( cb.equal(oc.get("claveContabilizacion"), "50" ));
		
		cq.where(predicates.toArray(new Predicate[0]));
		if (filter.getRowsPerPage() > 0) {
			list = em.createQuery(cq).setMaxResults(filter.getRowsPerPage()).getResultList();
		} else {
			list = em.createQuery(cq).getResultList();
		}
		
		List<ReporteFinancieroDto> dtos = ReporteFinancieroMapper.convertToDtos(list);
		
		long tempsFinal = System.currentTimeMillis();
		long diferencia = tempsFinal - inicio;
		
		long minutos = TimeUnit.MILLISECONDS.toMinutes(diferencia);
		logger.debug("diferencia: " + diferencia);
		logger.debug("minutos: " + minutos);
		logger.debug("total registros: " + dtos.size());
		return dtos;
	}
}
